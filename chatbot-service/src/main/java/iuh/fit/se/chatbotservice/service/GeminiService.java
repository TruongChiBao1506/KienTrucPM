package iuh.fit.se.chatbotservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.chatbotservice.dto.ChatRequest;
import iuh.fit.se.chatbotservice.dto.ChatResponse;
import iuh.fit.se.chatbotservice.dto.GlassDTO;
import iuh.fit.se.chatbotservice.dto.NavigationSuggestion;
import iuh.fit.se.chatbotservice.dto.ProductSuggestion;
import iuh.fit.se.chatbotservice.model.Conversation;
import iuh.fit.se.chatbotservice.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.temperature}")
    private Double temperature;

    @Value("${gemini.max-output-tokens}")
    private Integer maxOutputTokens;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChatbotPromptService promptService;
    private final RateLimitService rateLimitService;
    private final ProductDataService productDataService;
    private final ProductIntentAnalyzer intentAnalyzer;
    private final NavigationService navigationService;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s";

    public ChatResponse generateChatResponse(ChatRequest request, Conversation conversation) {
        // Kiểm tra rate limit
        if (!rateLimitService.tryConsume("gemini-api")) {
            String errorMessage = conversation.getLanguage().equals("vi")
                    ? "Hệ thống đang xử lý quá nhiều yêu cầu. Vui lòng thử lại sau ít phút."
                    : "The system is processing too many requests. Please try again in a few minutes.";

            return ChatResponse.builder()
                    .message(errorMessage)
                    .conversationId(conversation.getId())
                    .productSuggestion(false)
                    .suggestedProducts(new ProductSuggestion[0])
                    .hasNavigationSuggestion(false)
                    .build();
        }

        // Phân tích ý định từ tin nhắn người dùng
        ProductIntentAnalyzer.ProductIntent intent = intentAnalyzer.analyzeIntent(request.getMessage());

        // Phân tích ý định chuyển hướng từ tin nhắn
        NavigationSuggestion navigationSuggestion = navigationService.findNavigationSuggestion(request.getMessage());

        // Chuẩn bị URL API với model và API key
        String apiUrl = String.format(GEMINI_API_URL, model, apiKey);

        // Chuẩn bị danh sách các message
        List<Map<String, Object>> contents = new ArrayList<>();

        // Lấy thông tin sản phẩm động từ database
        String productInfo = productDataService.getFormattedProductInfo();
        
        // Kiểm tra danh sách sản phẩm
        boolean hasProducts = !productDataService.getAllProducts().isEmpty();
        
        // Điều chỉnh product info nếu không có sản phẩm
        if (!hasProducts) {
            productInfo = "Hiện tại cửa hàng chưa có sản phẩm nào. Bạn có thể hỏi về các loại kính nói chung hoặc để lại thông tin để chúng tôi thông báo khi có sản phẩm mới.";
        }

        // Kiểm tra xem đây có phải là tin nhắn đầu tiên trong cuộc trò chuyện hay không
        List<Message> existingMessages = conversation.getMessages();
        // Trừ đi tin nhắn hiện tại của người dùng đã thêm vào
        int messageCountBeforeCurrentMessage = Math.max(0, existingMessages.size() - 1);
        boolean isFirstMessage = messageCountBeforeCurrentMessage <= 0;

        // Thêm system prompt dựa vào ngôn ngữ và bổ sung thông tin sản phẩm
        Map<String, Object> systemContent = new HashMap<>();
        systemContent.put("role", "user");
        String systemPrompt = promptService.getSystemPrompt(conversation.getLanguage()) + "\n\n" + productInfo;
        
        // Thêm thông tin về vị trí của tin nhắn trong cuộc hội thoại - đánh dấu rõ hơn
        systemPrompt += "\n\nCONTEXT_INFO: This is " + (isFirstMessage ? "THE FIRST" : "a follow-up") + 
                        " message in the conversation. " + (isFirstMessage ? "YOU MUST START WITH A POLITE GREETING in Vietnamese or English depending on user language." : "Skip the greeting and respond directly to the question.");
        
        // Nếu không có sản phẩm, thêm hướng dẫn cho AI
        if (!hasProducts) {
            systemPrompt += "\n\nLưu ý quan trọng: Hiện tại cửa hàng chưa có sản phẩm nào. Khi người dùng hỏi về sản phẩm cụ thể, hãy trả lời rằng hiện tại cửa hàng chưa có sản phẩm đó và đang trong quá trình nhập hàng. Không đề xuất sản phẩm cụ thể hay đưa ra thông tin chi tiết về sản phẩm không có thực. Hãy giúp người dùng bằng cách cung cấp thông tin chung về loại kính họ quan tâm và mời họ để lại thông tin liên hệ.";
        }
        
        systemContent.put("parts", List.of(Map.of("text", systemPrompt)));
        contents.add(systemContent);

        // Thêm các tin nhắn trước đó trong cuộc hội thoại (tối đa 5 tin nhắn gần nhất để tiết kiệm token)
        List<Message> recentMessages = getRecentMessages(conversation.getMessages(), 5);
        for (Message msg : recentMessages) {
            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("role", msg.getRole().equals("assistant") ? "model" : "user");
            messageContent.put("parts", List.of(Map.of("text", msg.getContent())));
            contents.add(messageContent);
        }

        // Thêm tin nhắn hiện tại của người dùng
        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");
        userContent.put("parts", List.of(Map.of("text", request.getMessage())));
        contents.add(userContent);

        // Tạo request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", contents);

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", temperature);
        generationConfig.put("maxOutputTokens", maxOutputTokens);
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 40);
        requestBody.put("generationConfig", generationConfig);

        // Chuẩn bị HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Tạo HTTP entity
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Gọi Gemini API
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, httpEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Xử lý phản hồi từ Gemini
                String responseContent = extractResponseText(response.getBody());

                // Chỉ xử lý đề xuất sản phẩm nếu có sản phẩm trong database
                boolean hasProductSuggestion = false;
                ProductSuggestion[] suggestedProducts = new ProductSuggestion[0];
                
                if (hasProducts) {
                    // Kiểm tra xem có đề xuất sản phẩm hay không
                    hasProductSuggestion = responseContent.contains("PRODUCT_SUGGESTION:");
                    
                    if (hasProductSuggestion) {
                        suggestedProducts = extractProductSuggestions(responseContent);
                        // Loại bỏ thẻ PRODUCT_SUGGESTION khỏi phản hồi
                        responseContent = responseContent.replaceAll("PRODUCT_SUGGESTION:.*", "").trim();
                    } else if (shouldSuggestProducts(intent)) {
                        // Nếu AI không đề xuất, nhưng intent rõ ràng, tự động đề xuất
                        List<GlassDTO> relevantProducts = findRelevantProducts(intent);
                        if (!relevantProducts.isEmpty()) {
                            suggestedProducts = productDataService.convertToProductSuggestions(relevantProducts);
                            responseContent += "\n\nDựa trên yêu cầu của bạn, tôi tìm thấy một số sản phẩm phù hợp.";
                            hasProductSuggestion = true;
                        }
                    }
                }

                // Chỉ đưa ra gợi ý chuyển hướng nếu có sản phẩm
                if (navigationSuggestion != null && hasProducts) {
                    String language = conversation.getLanguage();
                    String navigationMessage = language.equals("vi")
                            ? "\n\nBạn có thể nhấn vào nút bên dưới để xem danh sách sản phẩm."
                            : "\n\nYou can click the button below to view the product list.";

                    responseContent += navigationMessage;

                    return ChatResponse.builder()
                            .message(responseContent)
                            .conversationId(conversation.getId())
                            .productSuggestion(hasProductSuggestion)
                            .suggestedProducts(suggestedProducts)
                            .navigationSuggestion(navigationSuggestion)
                            .hasNavigationSuggestion(true)
                            .build();
                } else {
                    return ChatResponse.builder()
                            .message(responseContent)
                            .conversationId(conversation.getId())
                            .productSuggestion(hasProductSuggestion)
                            .suggestedProducts(suggestedProducts)
                            .hasNavigationSuggestion(false)
                            .build();
                }
            } else {
                throw new RuntimeException("Failed to get response from Gemini API");
            }
        } catch (Exception e) {
            log.error("Error generating response from Gemini API", e);
            String errorMessage = conversation.getLanguage().equals("vi")
                    ? "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này. Vui lòng thử lại sau."
                    : "Sorry, I cannot process your request at this time. Please try again later.";

            return ChatResponse.builder()
                    .message(errorMessage)
                    .conversationId(conversation.getId())
                    .productSuggestion(false)
                    .suggestedProducts(new ProductSuggestion[0])
                    .hasNavigationSuggestion(false)
                    .build();
        }
    }

    private List<Message> getRecentMessages(List<Message> messages, int maxCount) {
        if (messages.size() <= maxCount) {
            return messages;
        }
        return messages.subList(messages.size() - maxCount, messages.size());
    }

    @SuppressWarnings("unchecked")
    private String extractResponseText(Map responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    String originalText = (String) parts.get(0).get("text");
                    
                    // Loại bỏ phần JSON sản phẩm hiển thị trực tiếp trong tin nhắn
                    // Tìm và xóa cấu trúc JSON trực tiếp (bắt đầu bằng { và bao gồm productId)
                    originalText = originalText.replaceAll("\\{\\s*\"productId\"[\\s\\S]*?\\},?\\s*\\{\\s*\"productId\"[\\s\\S]*?\\}\\s*\\]", "");
                    
                    // Loại bỏ cả những trường hợp chỉ có một sản phẩm
                    originalText = originalText.replaceAll("\\{\\s*\"productId\"[\\s\\S]*?\\}\\s*\\]", "");
                    
                    // Loại bỏ dấu ngoặc vuông đầu nếu còn sót
                    originalText = originalText.replaceAll("\\[\\s*\\n*\\r*", "");
                    
                    // Loại bỏ các dòng trống liên tiếp
                    originalText = originalText.replaceAll("\\n\\s*\\n+", "\n\n");
                    
                    // Loại bỏ khoảng trắng thừa ở đầu và cuối
                    originalText = originalText.trim();
                    
                    return originalText;
                }
            }
        } catch (Exception e) {
            log.error("Error extracting text from Gemini response", e);
        }
        return "Không thể xử lý phản hồi từ AI.";
    }

    private ProductSuggestion[] extractProductSuggestions(String response) {
        try {
            // Tìm phần PRODUCT_SUGGESTION trong phản hồi
            Pattern pattern = Pattern.compile("PRODUCT_SUGGESTION:(.*?)(?:\\n\\s*\\n|$)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(response);

            if (matcher.find()) {
                // Lấy phần nội dung sau tag PRODUCT_SUGGESTION:
                String content = matcher.group(1).trim();
                ProductSuggestion[] extractedSuggestions = null;
                
                // Kiểm tra xem nội dung có phải là JSON hợp lệ không
                if (content.startsWith("[") || content.startsWith("{")) {
                    // Nếu là JSON hợp lệ, thử phân tích như thông thường
                    try {
                        if (content.startsWith("[")) {
                            extractedSuggestions = objectMapper.readValue(content, ProductSuggestion[].class);
                        } else {
                            // Đối tượng JSON đơn, bọc trong mảng
                            ProductSuggestion suggestion = objectMapper.readValue(content, ProductSuggestion.class);
                            extractedSuggestions = new ProductSuggestion[]{suggestion};
                        }
                    } catch (JsonProcessingException e) {
                        log.warn("Invalid JSON structure in product suggestion, attempting to fix: {}", e.getMessage());
                        extractedSuggestions = extractProductsFromInvalidJson(content);
                    }
                } else {
                    // Không phải JSON, mà là văn bản thông thường
                    log.warn("Non-JSON content found in PRODUCT_SUGGESTION tag: {}", 
                             content.length() > 50 ? content.substring(0, 50) + "..." : content);
                    
                    // Thử tách và chuyển đổi văn bản thành JSON
                    extractedSuggestions = textToProductSuggestions(content);
                }
                
                // Nếu vẫn không có sản phẩm, trả về mảng trống
                if (extractedSuggestions == null || extractedSuggestions.length == 0) {
                    return new ProductSuggestion[0];
                }
                
                // Thay thế URL hình ảnh giả bằng URL hình ảnh thực tế từ database
                for (ProductSuggestion suggestion : extractedSuggestions) {
                    if (suggestion != null) {
                        String imageUrl = suggestion.getImageUrl();
                        
                        // Nếu URL là URL giả hoặc không có URL
                        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.contains("example.com")) {
                            log.debug("Found invalid image URL: {} for product ID: {}", 
                                      imageUrl, suggestion.getProductId());
                            
                            // Tìm sản phẩm thực trong database theo ID
                            try {
                                Long productId = Long.parseLong(suggestion.getProductId());
                                GlassDTO glass = productDataService.getProductById(productId);
                                
                                if (glass != null && glass.getImageFrontUrl() != null 
                                    && !glass.getImageFrontUrl().isEmpty()) {
                                    suggestion.setImageUrl(glass.getImageFrontUrl());
                                    log.info("Updated image URL for product ID: {} to: {}", 
                                            productId, glass.getImageFrontUrl());
                                } else {
                                    suggestion.setImageUrl(frontendBaseUrl + "/placeholder.pics/svg/200x150/DEDEDE/555555/No%20Image");
                                }
                            } catch (NumberFormatException e) {
                                log.warn("Invalid product ID format: {}", suggestion.getProductId());
                                suggestion.setImageUrl(frontendBaseUrl + "/placeholder.pics/svg/200x150/DEDEDE/555555/Invalid%20ID");
                            }
                        }
                    }
                }
                
                return extractedSuggestions;
            }
        } catch (Exception e) {
            log.error("Error extracting product suggestions: {}", e.getMessage(), e);
        }

        return new ProductSuggestion[0];
    }
    
    /**
     * Phương thức hỗ trợ để trích xuất thông tin sản phẩm từ JSON không hợp lệ
     * sử dụng regex để tìm các trường quan trọng
     */
    private ProductSuggestion[] extractProductsFromInvalidJson(String text) {
        List<ProductSuggestion> result = new ArrayList<>();
        
        // Tìm các ID sản phẩm
        Pattern idPattern = Pattern.compile("\"productId\"\\s*:\\s*\"([^\"]+)\"");
        Matcher idMatcher = idPattern.matcher(text);
        
        while (idMatcher.find()) {
            String productId = idMatcher.group(1);
            
            // Tạo một ProductSuggestion với ID đã tìm thấy
            ProductSuggestion suggestion = new ProductSuggestion();
            suggestion.setProductId(productId);
            
            // Tìm cả tên sản phẩm
            Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
            Matcher nameMatcher = namePattern.matcher(text);
            if (nameMatcher.find()) {
                suggestion.setName(nameMatcher.group(1));
            } else {
                suggestion.setName("Unknown Product");
            }
            
            // Tìm giá
            Pattern pricePattern = Pattern.compile("\"price\"\\s*:\\s*(\\d+)");
            Matcher priceMatcher = pricePattern.matcher(text);
            if (priceMatcher.find()) {
                suggestion.setPrice(Double.parseDouble(priceMatcher.group(1)));
            }
            
            // Tìm category
            Pattern categoryPattern = Pattern.compile("\"category\"\\s*:\\s*\"([^\"]+)\"");
            Matcher categoryMatcher = categoryPattern.matcher(text);
            if (categoryMatcher.find()) {
                suggestion.setCategory(categoryMatcher.group(1));
            } else {
                suggestion.setCategory("Kính mắt");
            }
            
            // Tìm URL chi tiết
            Pattern detailUrlPattern = Pattern.compile("\"detailUrl\"\\s*:\\s*\"([^\"]+)\"");
            Matcher detailUrlMatcher = detailUrlPattern.matcher(text);
            if (detailUrlMatcher.find()) {
                suggestion.setDetailUrl(detailUrlMatcher.group(1));
            } else {
                suggestion.setDetailUrl(frontendBaseUrl + "/products/detail/" + productId);
            }
            
            // Để trống URL hình ảnh, sẽ được cập nhật từ database
            suggestion.setImageUrl("");
            
            result.add(suggestion);
            
            // Nếu đã tìm thấy, dừng vòng lặp
            // Các tìm kiếm khác có thể bị trùng do sử dụng các matcher khác nhau
            break;
        }
        
        return result.toArray(new ProductSuggestion[0]);
    }
    
    /**
     * Phương thức chuyển văn bản mô tả sản phẩm thành đối tượng ProductSuggestion
     * Ví dụ: "Chúng tôi có kính Aviator Deluxe (kính cận) và Modern Aviator (kính râm) màu bạc"
     */
    private ProductSuggestion[] textToProductSuggestions(String text) {
        log.info("Converting text to product suggestions: {}", text);
        List<ProductSuggestion> result = new ArrayList<>();
        
        try {
            // Lọc ra các ID sản phẩm từ văn bản bằng các pattern thông minh
            
            // Pattern 1: Tìm kiếm các từ khóa "kính" + tên + các dấu hiệu ID trong ngoặc
            Pattern productPattern = Pattern.compile("(kính|glasses|eyeglasses|sunglasses)\\s+([\\w\\s-]+)\\s*\\(?(?:ID:?\\s*(\\d+)|kính\\s*(cận|râm|mát)|#(\\d+))?\\)?", Pattern.CASE_INSENSITIVE);
            Matcher productMatcher = productPattern.matcher(text);
            
            // Tạo bản đồ các sản phẩm đã tìm thấy (ID -> Tên)
            Map<String, String> foundProducts = new HashMap<>();

            // Thu thập tất cả các sản phẩm có thể từ văn bản
            while (productMatcher.find()) {
                String productName = productMatcher.group(2).trim();
                
                // Tìm ID trong các nhóm
                String productId = null;
                if (productMatcher.group(3) != null) {
                    productId = productMatcher.group(3);
                } else if (productMatcher.group(5) != null) {
                    productId = productMatcher.group(5);
                } else {
                    // Nếu không có ID rõ ràng, thử tìm sản phẩm dựa trên tên
                    List<GlassDTO> matchingProducts = productDataService.getAllProducts().stream()
                        .filter(p -> p.getName().toLowerCase().contains(productName.toLowerCase()))
                        .collect(Collectors.toList());
                    
                    if (!matchingProducts.isEmpty()) {
                        productId = String.valueOf(matchingProducts.get(0).getId());
                    }
                }
                
                // Lưu sản phẩm nếu tìm thấy ID
                if (productId != null) {
                    foundProducts.put(productId, productName);
                }
            }
            
            // Pattern 2: Tìm kiếm tên sản phẩm xác định + mẫu cụ thể sau đó
            Pattern namedProductPattern = Pattern.compile("\\b(?:kính|glasses|model|mẫu)\\s+([\\w\\s-]+)\\s*(?:\\(ID:?\\s*(\\d+)\\)|#(\\d+))?", Pattern.CASE_INSENSITIVE);
            Matcher namedProductMatcher = namedProductPattern.matcher(text);
            
            while (namedProductMatcher.find()) {
                String productName = namedProductMatcher.group(1).trim();
                
                // Tìm ID trong các nhóm
                String productId = null;
                if (namedProductMatcher.group(2) != null) {
                    productId = namedProductMatcher.group(2);
                } else if (namedProductMatcher.group(3) != null) {
                    productId = namedProductMatcher.group(3);
                } else {
                    // Nếu không có ID rõ ràng, thử tìm sản phẩm dựa trên tên
                    List<GlassDTO> matchingProducts = productDataService.getAllProducts().stream()
                        .filter(p -> p.getName().toLowerCase().contains(productName.toLowerCase()))
                        .collect(Collectors.toList());
                    
                    if (!matchingProducts.isEmpty()) {
                        productId = String.valueOf(matchingProducts.get(0).getId());
                    }
                }
                
                // Lưu sản phẩm nếu tìm thấy ID
                if (productId != null) {
                    foundProducts.put(productId, productName);
                }
            }
            
            // Pattern 3: Tìm kiếm trực tiếp các con số có thể là ID sản phẩm sau từ ID hoặc mã
            Pattern idPattern = Pattern.compile("\\b(?:ID|id|mã)\\s*[:#]?\\s*(\\d+)\\b");
            Matcher idMatcher = idPattern.matcher(text);
            
            while (idMatcher.find()) {
                String productId = idMatcher.group(1);
                GlassDTO product = productDataService.getProductById(Long.parseLong(productId));
                
                if (product != null) {
                    foundProducts.put(productId, product.getName());
                }
            }
            
            // Nếu vẫn không tìm thấy sản phẩm nào, thử tìm từ các tên sản phẩm cụ thể
            if (foundProducts.isEmpty()) {
                List<GlassDTO> allProducts = productDataService.getAllProducts();
                
                for (GlassDTO product : allProducts) {
                    if (text.toLowerCase().contains(product.getName().toLowerCase())) {
                        foundProducts.put(String.valueOf(product.getId()), product.getName());
                    }
                }
            }
            
            // Tạo đối tượng ProductSuggestion từ các sản phẩm đã tìm thấy
            for (Map.Entry<String, String> entry : foundProducts.entrySet()) {
                String productId = entry.getKey();
                String productName = entry.getValue();
                
                try {
                    // Lấy thông tin chi tiết từ database
                    GlassDTO product = productDataService.getProductById(Long.parseLong(productId));
                    
                    if (product != null) {
                        ProductSuggestion suggestion = new ProductSuggestion();
                        suggestion.setProductId(productId);
                        suggestion.setName(product.getName());
                        suggestion.setPrice(product.getPrice());
                        suggestion.setImageUrl(product.getImageFrontUrl());
                        suggestion.setCategory(product.getCategoryId() == 1L ? "Kính râm" : "Kính cận");
                        suggestion.setDetailUrl(frontendBaseUrl + "/products/glasses/" + productId);
                        
                        result.add(suggestion);
                    }
                } catch (Exception e) {
                    log.warn("Error fetching product details for ID: {}", productId, e);
                }
            }
            
            log.info("Converted text to {} product suggestions", result.size());
        } catch (Exception e) {
            log.error("Error converting text to product suggestions", e);
        }
        
        return result.toArray(new ProductSuggestion[0]);
    }

    private boolean shouldSuggestProducts(ProductIntentAnalyzer.ProductIntent intent) {
        return intent.isWantsEyeglasses() || intent.isWantsSunglasses() ||
                intent.isForMen() || intent.isForWomen() ||
                intent.getBrand() != null || intent.getShape() != null ||
                intent.getMaterial() != null || intent.getColor() != null ||
                intent.getMinPrice() != null || intent.getMaxPrice() != null;
    }

    private List<GlassDTO> findRelevantProducts(ProductIntentAnalyzer.ProductIntent intent) {
        // Xác định loại kính
        boolean isEyeglasses = intent.isWantsEyeglasses() || !intent.isWantsSunglasses();

        // Xác định giới tính
        boolean forMen = intent.isForMen() || !intent.isForWomen();

        // Lọc sản phẩm
        return productDataService.getFilteredProducts(
                isEyeglasses,
                forMen,
                intent.getBrand(),
                intent.getShape(),
                intent.getMaterial(),
                intent.getColor(),
                intent.getMinPrice(),
                intent.getMaxPrice()
        );
    }
}