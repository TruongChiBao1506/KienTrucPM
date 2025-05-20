package iuh.fit.se.chatbotservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import iuh.fit.se.chatbotservice.dto.ChatRequest;
import iuh.fit.se.chatbotservice.dto.ChatResponse;
import iuh.fit.se.chatbotservice.dto.GlassDTO;
import iuh.fit.se.chatbotservice.dto.NavigationSuggestion;
import iuh.fit.se.chatbotservice.dto.ProductSuggestion;
import iuh.fit.se.chatbotservice.dto.SearchCriteria;
import iuh.fit.se.chatbotservice.model.Conversation;
import iuh.fit.se.chatbotservice.model.Message;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
import java.util.*;
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
    private final ProductSearchService productSearchService;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s";

    // Constants for product categories
    private static final long SUNGLASSES_CATEGORY_ID = 1L;
    private static final long EYEGLASSES_CATEGORY_ID = 2L;

    // Bản đồ màu sắc tĩnh cơ bản cho các màu phổ biến
    private static final Map<String, String> BASE_COLOR_MAPPING = Map.ofEntries(
            // Màu nâu cần tách biệt với gold
            Map.entry("nâu", "brown"),
            Map.entry("nau", "brown"),
            Map.entry("brown", "brown"),
            Map.entry("chocolate", "brown"),
            Map.entry("coffee", "brown"),

            // Màu đen
            Map.entry("đen", "black"),
            Map.entry("den", "black"),
            Map.entry("black", "black"),

            // Màu bạc/xám
            Map.entry("bạc", "silver"),
            Map.entry("bac", "silver"),
            Map.entry("silver", "silver"),
            Map.entry("xám", "silver"),
            Map.entry("xam", "silver"),
            Map.entry("gray", "silver"),
            Map.entry("grey", "silver"),

            // Màu đỏ
            Map.entry("đỏ", "red"),
            Map.entry("do", "red"),
            Map.entry("red", "red"),

            // Tách biệt xanh lá và xanh dương
            Map.entry("xanh lá", "green"),
            Map.entry("xanh lá cây", "green"),
            Map.entry("xanh rêu", "green"),
            Map.entry("green", "green"),
            Map.entry("lá", "green"),

            Map.entry("xanh", "blue"), // Mặc định "xanh" không rõ ràng sẽ là xanh dương
            Map.entry("xanh dương", "blue"),
            Map.entry("xanh biển", "blue"),
            Map.entry("blue", "blue"),
            Map.entry("navy", "blue"),

            // Màu vàng
            Map.entry("vàng", "yellow"),
            Map.entry("yellow", "yellow"),

            // Màu hồng
            Map.entry("hồng", "pink"),
            Map.entry("pink", "pink"),

            // Màu tím
            Map.entry("tím", "purple"),
            Map.entry("purple", "purple"),

            // Tách biệt gold rose với gold
            Map.entry("vàng hồng", "gold rose"),
            Map.entry("gold rose", "gold rose"),
            Map.entry("rose gold", "gold rose"),
            Map.entry("hồng vàng", "gold rose"),

            // Màu gold riêng biệt
            Map.entry("gold", "gold"),
            Map.entry("vàng gold", "gold")
    );

    // Bản đồ động chất liệu cơ bản
    private static final Map<String, String> BASE_MATERIAL_MAPPING = Map.ofEntries(
            // Nhựa
            Map.entry("plastic", "plastic"),
            Map.entry("nhựa", "plastic"),

            // Kim loại
            Map.entry("metal", "metal"),
            Map.entry("kim loại", "metal"),
            Map.entry("kim loai", "metal"),
            Map.entry("titanium", "metal"),
            Map.entry("alloy", "metal"),
            Map.entry("hợp kim", "metal"),
            Map.entry("hop kim", "metal"),

            // Thép không gỉ
            Map.entry("stainless steel", "stainless steel"),
            Map.entry("stainless-steel", "stainless steel"),
            Map.entry("thép không gỉ", "stainless steel"),
            Map.entry("thep khong gi", "stainless steel"),
            Map.entry("steel", "stainless steel"),
            Map.entry("thép", "stainless steel"),
            Map.entry("thep", "stainless steel"),

            // Acetate
            Map.entry("acetate", "acetate"),

            // Kết hợp
            Map.entry("mixed", "mixed"),
            Map.entry("kết hợp", "mixed"),
            Map.entry("ket hop", "mixed"),
            Map.entry("composite", "mixed"),

            // Gỗ
            Map.entry("wood", "wood"),
            Map.entry("gỗ", "wood"),
            Map.entry("go", "wood"),

            // Carbon Fiber
            Map.entry("carbon fiber", "carbon fiber"),
            Map.entry("carbon-fiber", "carbon fiber"),
            Map.entry("carbonfiber", "carbon fiber"),
            Map.entry("carbon", "carbon fiber"),
            Map.entry("fiber", "carbon fiber"),
            Map.entry("sợi carbon", "carbon fiber"),
            Map.entry("soi carbon", "carbon fiber")
    );

    // Bản đồ động được tạo từ database + mapping cơ bản
    private Map<String, String> dynamicColorMapping;
    private Map<String, String> dynamicMaterialMapping;

    @PostConstruct
    public void initializeDynamicColorMapping() {
        // Khởi tạo với mapping cơ bản
        dynamicColorMapping = new HashMap<>(BASE_COLOR_MAPPING);
        dynamicMaterialMapping = new HashMap<>(BASE_MATERIAL_MAPPING);

        try {
            // Lấy tất cả màu từ database
            List<String> dbColors = productDataService.getAllColors();
            log.info("Đã lấy {} màu sắc từ database", dbColors.size());

            // Thêm màu từ database vào mapping động
            for (String color : dbColors) {
                if (color == null || color.trim().isEmpty()) continue;

                String normalizedColor = color.toLowerCase().trim();

                // Thêm mapping màu gốc -> màu gốc (chuẩn hóa)
                dynamicColorMapping.put(normalizedColor, normalizedColor);

                // Thêm mapping không dấu nếu màu có dấu tiếng Việt
                String withoutAccent = removeAccents(normalizedColor);
                if (!withoutAccent.equals(normalizedColor)) {
                    dynamicColorMapping.put(withoutAccent, normalizedColor);
                }

                log.debug("Đã thêm mapping màu sắc: '{}' -> '{}'", normalizedColor, normalizedColor);
                if (!withoutAccent.equals(normalizedColor)) {
                    log.debug("Đã thêm mapping màu không dấu: '{}' -> '{}'", withoutAccent, normalizedColor);
                }
            }

            log.info("Đã khởi tạo xong bản đồ màu sắc động với {} mapping", dynamicColorMapping.size());

            // Lấy tất cả chất liệu từ database
            List<String> dbMaterials = productDataService.getAllMaterials();
            log.info("Đã lấy {} loại chất liệu từ database", dbMaterials.size());

            // Thêm chất liệu từ database vào mapping động
            for (String material : dbMaterials) {
                if (material == null || material.trim().isEmpty()) continue;

                String normalizedMaterial = material.toLowerCase().trim();

                // Thêm mapping chất liệu gốc -> chất liệu gốc (chuẩn hóa)
                dynamicMaterialMapping.put(normalizedMaterial, normalizedMaterial);

                // Thêm mapping không dấu nếu chất liệu có dấu tiếng Việt
                String withoutAccent = removeAccents(normalizedMaterial);
                if (!withoutAccent.equals(normalizedMaterial)) {
                    dynamicMaterialMapping.put(withoutAccent, normalizedMaterial);
                }

                log.debug("Đã thêm mapping chất liệu: '{}' -> '{}'", normalizedMaterial, normalizedMaterial);
                if (!withoutAccent.equals(normalizedMaterial)) {
                    log.debug("Đã thêm mapping chất liệu không dấu: '{}' -> '{}'", withoutAccent, normalizedMaterial);
                }
            }

            log.info("Đã khởi tạo xong bản đồ chất liệu động với {} mapping", dynamicMaterialMapping.size());
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo bản đồ màu sắc và chất liệu động: {}", e.getMessage(), e);
        }
    }

    // Phương thức loại bỏ dấu tiếng Việt
    private String removeAccents(String input) {
        if (input == null) return null;
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private String normalizeColor(String color) {
        if (color == null || color.isEmpty()) {
            return null;
        }

        String lowercaseColor = color.toLowerCase().trim();

        // Kiểm tra trong bản đồ động
        String normalizedColor = dynamicColorMapping.get(lowercaseColor);
        if (normalizedColor != null) {
            log.debug("Tìm thấy màu '{}' trong bản đồ động -> '{}'", lowercaseColor, normalizedColor);
            return normalizedColor;
        }

        // Nếu không tìm thấy trong bản đồ động, thử loại bỏ dấu và tìm lại
        String withoutAccent = removeAccents(lowercaseColor);
        normalizedColor = dynamicColorMapping.get(withoutAccent);
        if (normalizedColor != null) {
            log.debug("Tìm thấy màu không dấu '{}' trong bản đồ động -> '{}'", withoutAccent, normalizedColor);
            return normalizedColor;
        }

        // Nếu vẫn không tìm thấy, trả về giá trị gốc đã được chuẩn hóa cơ bản
        log.debug("Không tìm thấy màu '{}' trong bản đồ động, sử dụng giá trị gốc", lowercaseColor);
        return lowercaseColor;
    }

    private String normalizeMaterial(String material) {
        if (material == null || material.isEmpty()) {
            return null;
        }

        String lowercaseMaterial = material.toLowerCase().trim();

        // Xử lý các trường hợp đặc biệt
        // 1. Carbon Fiber
        if (lowercaseMaterial.contains("carbon fiber") ||
                lowercaseMaterial.contains("carbon-fiber") ||
                lowercaseMaterial.contains("carbonfiber") ||
                (lowercaseMaterial.contains("carbon") && lowercaseMaterial.contains("fiber")) ||
                lowercaseMaterial.contains("sợi carbon")) {
            log.info("Chuẩn hóa chất liệu: '{}' -> 'carbon fiber'", lowercaseMaterial);
            return "carbon fiber";
        }

        // 2. Stainless Steel
        if (lowercaseMaterial.contains("stainless steel") ||
                lowercaseMaterial.contains("stainless-steel") ||
                (lowercaseMaterial.contains("stainless") && lowercaseMaterial.contains("steel")) ||
                lowercaseMaterial.contains("thép không gỉ") ||
                lowercaseMaterial.contains("inox")) {
            log.info("Chuẩn hóa chất liệu: '{}' -> 'stainless steel'", lowercaseMaterial);
            return "stainless steel";
        }

        // 3. Mixed/Composite
        if (lowercaseMaterial.contains("mixed") ||
                lowercaseMaterial.contains("composite") ||
                lowercaseMaterial.contains("hỗn hợp") ||
                lowercaseMaterial.contains("kết hợp")) {
            log.info("Chuẩn hóa chất liệu: '{}' -> 'mixed'", lowercaseMaterial);
            return "mixed";
        }

        // 4. Plastic/Nhựa
        if (lowercaseMaterial.equals("plastic") ||
                lowercaseMaterial.equals("nhựa") ||
                lowercaseMaterial.contains("acetate")) {
            log.info("Chuẩn hóa chất liệu: '{}' -> 'plastic'", lowercaseMaterial);
            return "plastic";
        }

        // 5. Metal/Kim loại
        if (lowercaseMaterial.equals("metal") ||
                lowercaseMaterial.equals("kim loại") ||
                lowercaseMaterial.equals("kim loai")) {
            log.info("Chuẩn hóa chất liệu: '{}' -> 'metal'", lowercaseMaterial);
            return "metal";
        }

        // Kiểm tra trong bản đồ động
        String normalizedMaterial = dynamicMaterialMapping.get(lowercaseMaterial);
        if (normalizedMaterial != null) {
            log.debug("Tìm thấy chất liệu '{}' trong bản đồ động -> '{}'", lowercaseMaterial, normalizedMaterial);
            return normalizedMaterial;
        }

        // Nếu không tìm thấy trong bản đồ động, thử loại bỏ dấu và tìm lại
        String withoutAccent = removeAccents(lowercaseMaterial);
        normalizedMaterial = dynamicMaterialMapping.get(withoutAccent);
        if (normalizedMaterial != null) {
            log.debug("Tìm thấy chất liệu không dấu '{}' trong bản đồ động -> '{}'", withoutAccent, normalizedMaterial);
            return normalizedMaterial;
        }

        // Nếu vẫn không tìm thấy, trả về giá trị gốc đã được chuẩn hóa cơ bản
        log.debug("Không tìm thấy chất liệu '{}' trong bản đồ động, sử dụng giá trị gốc", lowercaseMaterial);
        return lowercaseMaterial;
    }

    // Phương thức cập nhật lại bản đồ màu sắc động, có thể gọi mỗi khi database thay đổi
    public void refreshColorMapping() {
        initializeDynamicColorMapping();
    }

    @Retry(name = "geminiApiRetry", fallbackMethod = "generateChatResponseFallback")
    public ChatResponse generateChatResponse(ChatRequest request, Conversation conversation) {
        // Kiểm tra rate limit với Resilience4j
        try {
            rateLimitService.tryConsumeOrThrow(request.getUserId() != null ? request.getUserId() : "anonymous-user", "chatEndpoint");
        } catch (Exception e) {
            log.warn("Rate limit exceeded for user: {}", request.getUserId());
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

        // Luôn refresh cache để đảm bảo sử dụng dữ liệu mới nhất
        productDataService.refreshProductCache();

        // Phân tích ý định từ tin nhắn người dùng
        ProductIntentAnalyzer.ProductIntent intent = intentAnalyzer.analyzeIntent(request.getMessage());

        // Chuyển đổi ProductIntent thành SearchCriteria cho ProductSearchService
        SearchCriteria searchCriteria = convertIntentToSearchCriteria(intent);

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
                    // Cập nhật cache trước khi tìm kiếm sản phẩm để đảm bảo dữ liệu mới nhất
                    productDataService.refreshProductCache();

                    // Kiểm tra xem có đề xuất sản phẩm hay không
                    hasProductSuggestion = responseContent.contains("PRODUCT_SUGGESTION:");

                    if (hasProductSuggestion) {
                        suggestedProducts = extractProductSuggestions(responseContent);
                        // Loại bỏ thẻ PRODUCT_SUGGESTION khỏi phản hồi
                        responseContent = responseContent.replaceAll("PRODUCT_SUGGESTION:.*", "").trim();
                    } else if (shouldSuggestProducts(intent)) {
                        // Sử dụng service mới để tìm sản phẩm phù hợp
                        // Tạo tiêu chí tìm kiếm từ intent
                        iuh.fit.se.chatbotservice.dto.SearchCriteria criteria = new iuh.fit.se.chatbotservice.dto.SearchCriteria();
                        criteria.setWantsEyeglasses(intent.isWantsEyeglasses());
                        criteria.setWantsSunglasses(intent.isWantsSunglasses());
                        criteria.setWantsBoth(intent.isWantsBoth());
                        criteria.setForMen(intent.isForMen());
                        criteria.setForWomen(intent.isForWomen());
                        criteria.setBrand(intent.getBrand());
                        criteria.setShape(intent.getShape());
                        criteria.setMaterial(intent.getMaterial());
                        criteria.setColor(normalizeColor(intent.getColor()));
                        criteria.setMinPrice(intent.getMinPrice());
                        criteria.setMaxPrice(intent.getMaxPrice());
                        criteria.setWantsMostExpensive(intent.isWantsMostExpensive());
                        criteria.setWantsCheapest(intent.isWantsCheapest());

                        log.info("Tìm kiếm sản phẩm với các tiêu chí từ intent phân tích được");
                        List<GlassDTO> relevantProducts = productSearchService.findProducts(criteria);

                        // Kiểm tra trường hợp đặc biệt: nếu người dùng muốn tìm kính mắc nhất
                        boolean askingForMostExpensive = intent.isWantsMostExpensive() ||
                                responseContent.toLowerCase().contains("đắt nhất") ||
                                responseContent.toLowerCase().contains("mắc nhất");

                        if (askingForMostExpensive && relevantProducts.size() > 1) {
                            log.info("Phát hiện người dùng tìm kính mắc nhất, chỉ giữ lại kính có giá cao nhất");

                            // Tìm và chỉ giữ lại kính có giá cao nhất
                            GlassDTO mostExpensive = relevantProducts.stream()
                                    .max(Comparator.comparing(GlassDTO::getPrice))
                                    .orElse(null);

                            if (mostExpensive != null) {
                                log.info("Chỉ giữ lại kính {} với giá cao nhất {}",
                                        mostExpensive.getName(), mostExpensive.getPrice());
                                relevantProducts = Collections.singletonList(mostExpensive);
                            }
                        }

                        // Kiểm tra trường hợp đặc biệt: nếu người dùng muốn tìm kính rẻ nhất
                        boolean askingForCheapest = intent.isWantsCheapest() ||
                                responseContent.toLowerCase().contains("rẻ nhất") ||
                                responseContent.toLowerCase().contains("giá thấp nhất");

                        if (askingForCheapest && relevantProducts.size() > 1) {
                            log.info("Phát hiện người dùng tìm kính rẻ nhất, chỉ giữ lại kính có giá thấp nhất");

                            // Tìm và chỉ giữ lại kính có giá thấp nhất
                            GlassDTO cheapest = relevantProducts.stream()
                                    .min(Comparator.comparing(GlassDTO::getPrice))
                                    .orElse(null);

                            if (cheapest != null) {
                                log.info("Chỉ giữ lại kính {} với giá thấp nhất {}",
                                        cheapest.getName(), cheapest.getPrice());
                                relevantProducts = Collections.singletonList(cheapest);
                            }
                        }

                        if (!relevantProducts.isEmpty()) {
                            suggestedProducts = productSearchService.convertToSuggestions(relevantProducts);
                            hasProductSuggestion = true;

                            // Thêm thông báo chỉ khi không đề cập "Dựa trên yêu cầu của bạn..." trong nội dung
                            if (!responseContent.contains("Dựa trên yêu cầu của bạn")) {
                                responseContent += "\n\nDựa trên yêu cầu của bạn, tôi tìm thấy một số sản phẩm phù hợp.";
                            }
                        }
                    }

                    // Kiểm tra xem response có đề cập đến kính mắc nhất không (giúp xác định ý định)
                    boolean mentionsMostExpensive = responseContent.toLowerCase().contains("đắt nhất") ||
                            responseContent.toLowerCase().contains("mắc nhất") ||
                            responseContent.toLowerCase().contains("giá cao nhất");

                    // Kiểm tra response có đề cập đến cả hai loại kính không
                    boolean mentionsBoth = (responseContent.toLowerCase().contains("eyeglasses") || responseContent.toLowerCase().contains("kính cận")) &&
                            (responseContent.toLowerCase().contains("sunglasses") || responseContent.toLowerCase().contains("kính râm"));

                    // Nếu phản hồi đề cập đến cả hai loại kính và kính mắc nhất, đảm bảo gợi ý phản ánh đúng điều đó
                    if (mentionsBoth && mentionsMostExpensive && suggestedProducts.length == 1) {
                        log.info("Phản hồi đề cập đến cả hai loại kính mắc nhất, nhưng chỉ có một loại được gợi ý. Bổ sung thêm.");

                        // Kiểm tra loại kính hiện có
                        String currentCategory = suggestedProducts[0].getCategory().toLowerCase();
                        boolean hasEyeglasses = currentCategory.contains("cận") || currentCategory.contains("eye");

                        // Tìm loại kính còn thiếu
                        List<GlassDTO> additionalGlasses;
                        if (hasEyeglasses) {
                            // Nếu đã có kính cận, tìm kính râm đắt nhất
                            additionalGlasses = productDataService.getSunglasses().stream()
                                    .sorted(Comparator.comparing(GlassDTO::getPrice).reversed())
                                    .limit(1)
                                    .collect(Collectors.toList());
                        } else {
                            // Nếu đã có kính râm, tìm kính cận đắt nhất
                            additionalGlasses = productDataService.getEyeglasses().stream()
                                    .sorted(Comparator.comparing(GlassDTO::getPrice).reversed())
                                    .limit(1)
                                    .collect(Collectors.toList());
                        }

                        if (!additionalGlasses.isEmpty()) {
                            // Thêm kính còn thiếu vào danh sách gợi ý
                            ProductSuggestion[] newSuggestions = new ProductSuggestion[suggestedProducts.length + 1];
                            System.arraycopy(suggestedProducts, 0, newSuggestions, 0, suggestedProducts.length);
                            newSuggestions[suggestedProducts.length] = createProductSuggestion(additionalGlasses.get(0));
                            suggestedProducts = newSuggestions;
                            log.info("Đã thêm kính {} vào danh sách gợi ý để đủ cả hai loại",
                                    additionalGlasses.get(0).getName());
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

    // Hàm chuyển đổi từ ProductIntent sang SearchCriteria
    private SearchCriteria convertIntentToSearchCriteria(ProductIntentAnalyzer.ProductIntent intent) {
        SearchCriteria criteria = new SearchCriteria();

        // Chuyển tất cả các thuộc tính
        criteria.setWantsEyeglasses(intent.isWantsEyeglasses());
        criteria.setWantsSunglasses(intent.isWantsSunglasses());
        criteria.setWantsBoth(intent.isWantsBoth());
        criteria.setForMen(intent.isForMen());
        criteria.setForWomen(intent.isForWomen());
        criteria.setBrand(intent.getBrand());
        criteria.setShape(intent.getShape());

        // Chuẩn hóa color và material
        if (intent.getColor() != null && !intent.getColor().isEmpty()) {
            criteria.setColor(normalizeColor(intent.getColor()));
        }

        if (intent.getMaterial() != null && !intent.getMaterial().isEmpty()) {
            criteria.setMaterial(normalizeMaterial(intent.getMaterial()));
        }

        // Giá cả và các thuộc tính đặc biệt
        criteria.setMinPrice(intent.getMinPrice());
        criteria.setMaxPrice(intent.getMaxPrice());
        criteria.setWantsMostExpensive(intent.isWantsMostExpensive());
        criteria.setWantsCheapest(intent.isWantsCheapest());

        return criteria;
    }

    // Phương thức fallback khi tất cả các lần thử gọi API Gemini đều thất bại
    public ChatResponse generateChatResponseFallback(ChatRequest request, Conversation conversation, Exception ex) {
        log.error("Không thể kết nối đến Gemini API sau nhiều lần thử lại: {}", ex.getMessage());

        // Ghi log chi tiết lỗi để debug
        if (log.isDebugEnabled()) {
            log.debug("Chi tiết lỗi:", ex);
        }

        // Tạo thông báo lỗi tùy thuộc vào ngôn ngữ của người dùng
        String errorMessage;
        if (conversation.getLanguage() != null && conversation.getLanguage().equals("vi")) {
            errorMessage = "Rất tiếc, không thể kết nối đến dịch vụ AI vào lúc này. Vui lòng thử lại sau giây lát.";
        } else {
            errorMessage = "Sorry, we're having trouble connecting to our AI service at the moment. Please try again in a moment.";
        }

        // Trả về response với thông báo lỗi
        return ChatResponse.builder()
                .message(errorMessage)
                .conversationId(conversation.getId())
                .productSuggestion(false)
                .suggestedProducts(new ProductSuggestion[0])
                .hasNavigationSuggestion(false)
                .build();
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

    /**
     * Kiểm tra xem người dùng có đang tìm kiếm kính giá rẻ với màu cụ thể không
     */
    private boolean isAskingForCheapestOfColor(String response, String mentionedColor) {
        if (mentionedColor == null || mentionedColor.isEmpty()) return false;

        // Kiểm tra nếu người dùng đang tìm kính giá rẻ của một màu cụ thể
        boolean hasCheapKeyword = response.toLowerCase().contains("rẻ") ||
                response.toLowerCase().contains("giá thấp") ||
                response.toLowerCase().contains("phải chăng") ||
                response.toLowerCase().contains("cheap") ||
                response.toLowerCase().contains("giá tốt");

        boolean hasColorMention = response.toLowerCase().contains("màu " + mentionedColor) ||
                response.toLowerCase().contains(mentionedColor + " rẻ") ||
                response.toLowerCase().contains("kính " + mentionedColor);

        if (hasCheapKeyword && hasColorMention) {
            log.info("Phát hiện yêu cầu tìm kính {} rẻ", mentionedColor);
            return true;
        }

        return false;
    }

    private boolean shouldSuggestProducts(ProductIntentAnalyzer.ProductIntent intent) {
        return intent.isWantsEyeglasses() || intent.isWantsSunglasses() ||
                intent.isForMen() || intent.isForWomen() ||
                intent.getBrand() != null || intent.getShape() != null ||
                intent.getMaterial() != null || intent.getColor() != null ||
                intent.getMinPrice() != null || intent.getMaxPrice() != null ||
                intent.isWantsMostExpensive() || intent.isWantsCheapest();
    }

    private ProductSuggestion[] extractProductSuggestions(String response) {
        try {
            // Trích xuất màu sắc được đề cập
            String mentionedColor = extractMentionedColor(response);

            // Kiểm tra các loại câu hỏi đặc biệt
            boolean isAskingForMostExpensive = response.toLowerCase().contains("đắt nhất") ||
                    response.toLowerCase().contains("mắc nhất") ||
                    response.toLowerCase().contains("giá cao nhất") ||
                    response.toLowerCase().contains("most expensive");

            boolean isAskingForCheapest = response.toLowerCase().contains("rẻ nhất") ||
                    response.toLowerCase().contains("giá rẻ nhất") ||
                    response.toLowerCase().contains("thấp nhất") ||
                    response.toLowerCase().contains("cheapest");

            boolean isAskingForCheapOfColor = isAskingForCheapestOfColor(response, mentionedColor);

            if (isAskingForMostExpensive) {
                log.info("Phát hiện câu hỏi về kính đắt nhất");
            }

            if (isAskingForCheapest || isAskingForCheapOfColor) {
                log.info("Phát hiện câu hỏi về kính rẻ nhất{}",
                        mentionedColor != null ? " màu " + mentionedColor : "");
            }

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

                // Nếu đang hỏi về kính đắt nhất, chỉ giữ lại kính có giá cao nhất
                if (isAskingForMostExpensive && extractedSuggestions.length > 1) {
                    log.info("Đang lọc để chỉ giữ lại kính có giá cao nhất");

                    // Tìm kính có giá cao nhất
                    ProductSuggestion mostExpensiveGlass = Arrays.stream(extractedSuggestions)
                            .filter(Objects::nonNull)
                            .max(Comparator.comparing(ProductSuggestion::getPrice))
                            .orElse(null);

                    if (mostExpensiveGlass != null) {
                        log.info("Đã lọc, chỉ giữ lại kính {} với giá {}",
                                mostExpensiveGlass.getName(), mostExpensiveGlass.getPrice());
                        extractedSuggestions = new ProductSuggestion[]{mostExpensiveGlass};
                    }
                }

                // Nếu đang hỏi về kính rẻ nhất hoặc kính màu X rẻ, chỉ giữ lại kính có giá thấp nhất
                if ((isAskingForCheapest || isAskingForCheapOfColor) && extractedSuggestions.length > 1) {
                    log.info("Đang lọc để chỉ giữ lại kính có giá thấp nhất");

                    // Tìm kính có giá thấp nhất
                    ProductSuggestion cheapestGlass = Arrays.stream(extractedSuggestions)
                            .filter(Objects::nonNull)
                            .min(Comparator.comparing(ProductSuggestion::getPrice))
                            .orElse(null);

                    if (cheapestGlass != null) {
                        log.info("Đã lọc, chỉ giữ lại kính {} với giá {}",
                                cheapestGlass.getName(), cheapestGlass.getPrice());
                        extractedSuggestions = new ProductSuggestion[]{cheapestGlass};
                    }
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
            // Trích xuất các ID sản phẩm được đề cập trực tiếp trong text
            Set<Long> mentionedProductIds = new HashSet<>();

            // Pattern 1: Tìm kiếm ID sản phẩm rõ ràng trong văn bản
            // VD: **COACH GOLD 7064 (ID: 21)** hoặc (ID: 123) hoặc #123
            Pattern idPattern = Pattern.compile("(?:ID:?\\s*(\\d+)|#(\\d+))");
            Matcher idMatcher = idPattern.matcher(text);

            while (idMatcher.find()) {
                String idStr = idMatcher.group(1) != null ? idMatcher.group(1) : idMatcher.group(2);
                try {
                    Long productId = Long.parseLong(idStr);
                    mentionedProductIds.add(productId);
                    log.info("Tìm thấy ID sản phẩm trực tiếp: {}", productId);
                } catch (NumberFormatException e) {
                    log.warn("ID sản phẩm không hợp lệ: {}", idStr);
                }
            }

            // Pattern 2: Tìm tên sản phẩm kèm ID trong ngoặc
            // VD: COACH GOLD 7064 (ID: 21)
            Pattern productWithIdPattern = Pattern.compile("([\\w\\s-]+)\\s*\\(?(?:ID:?\\s*(\\d+)|#(\\d+))\\)?");
            Matcher productWithIdMatcher = productWithIdPattern.matcher(text);

            while (productWithIdMatcher.find()) {
                String productName = productWithIdMatcher.group(1).trim();
                String idStr = productWithIdMatcher.group(2) != null ? productWithIdMatcher.group(2) : productWithIdMatcher.group(3);

                try {
                    Long productId = Long.parseLong(idStr);
                    mentionedProductIds.add(productId);
                    log.info("Tìm thấy sản phẩm '{}' với ID: {}", productName, productId);
                } catch (NumberFormatException e) {
                    log.warn("ID sản phẩm không hợp lệ cho {}: {}", productName, idStr);
                }
            }

            // Pattern 3: Tìm kiếm chuỗi Product ID: X trong văn bản
            Pattern productIdPattern = Pattern.compile("Product\\s+ID:?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher productIdMatcher = productIdPattern.matcher(text);

            while (productIdMatcher.find()) {
                try {
                    Long productId = Long.parseLong(productIdMatcher.group(1));
                    mentionedProductIds.add(productId);
                    log.info("Tìm thấy Product ID: {}", productId);
                } catch (NumberFormatException e) {
                    log.warn("Product ID không hợp lệ: {}", productIdMatcher.group(1));
                }
            }

            // Nếu tìm được ID sản phẩm, lấy thông tin chi tiết và tạo gợi ý
            if (!mentionedProductIds.isEmpty()) {
                for (Long productId : mentionedProductIds) {
                    GlassDTO product = productDataService.getProductById(productId);
                    if (product != null) {
                        ProductSuggestion suggestion = createProductSuggestion(product);
                        result.add(suggestion);
                        log.info("Đã thêm gợi ý sản phẩm ID: {}, Tên: {}", productId, product.getName());
                    } else {
                        log.warn("Không tìm thấy sản phẩm với ID: {}", productId);
                    }
                }
            }

            // Nếu không tìm thấy ID nào từ các pattern trên, thử tìm sản phẩm từ tên được đề cập
            if (result.isEmpty()) {
                // Tìm tên sản phẩm được đề cập trong văn bản
                Map<String, String> productMentions = extractProductMentions(text);

                // Xử lý màu sắc được đề cập
                String mentionedColor = extractMentionedColor(text);

                // Nếu có màu sắc được đề cập
                if (mentionedColor != null && !mentionedColor.isEmpty()) {
                    log.info("Phát hiện màu sắc được đề cập: {}", mentionedColor);

                    // Chuẩn hóa màu
                    String normalizedColor = normalizeColor(mentionedColor);

                    // Tìm sản phẩm với màu sắc đề cập
                    List<GlassDTO> productsByColor = productDataService.getAllProducts().stream()
                            .filter(p -> {
                                if (p.getColorName() == null) return false;

                                String productColorName = p.getColorName().toLowerCase();

                                // So sánh màu sản phẩm với màu được đề cập
                                return productColorName.equals(normalizedColor) ||
                                        productColorName.contains(normalizedColor) ||
                                        normalizedColor.contains(productColorName);
                            })
                            .collect(Collectors.toList());

                    log.info("Tìm thấy {} sản phẩm với màu '{}'", productsByColor.size(), normalizedColor);

                    // Thêm sản phẩm có màu phù hợp vào kết quả
                    for (GlassDTO product : productsByColor) {
                        // Chỉ thêm tối đa 3 sản phẩm để tránh hiển thị quá nhiều
                        if (result.size() < 3) {
                            ProductSuggestion suggestion = createProductSuggestion(product);
                            result.add(suggestion);
                            log.info("Thêm sản phẩm màu '{}': {} (ID: {})",
                                    product.getColorName(), product.getName(), product.getId());
                        }
                    }
                }

                // Nếu vẫn không có sản phẩm nào, thử tìm từ thương hiệu được đề cập
                if (result.isEmpty() && !productMentions.isEmpty()) {
                    for (Map.Entry<String, String> entry : productMentions.entrySet()) {
                        String brand = entry.getKey();
                        log.info("Tìm kiếm sản phẩm theo thương hiệu: {}", brand);

                        List<GlassDTO> productsByBrand = productDataService.getAllProducts().stream()
                                .filter(p -> p.getBrand() != null && p.getBrand().toLowerCase().contains(brand.toLowerCase()))
                                .collect(Collectors.toList());

                        // Thêm các sản phẩm phù hợp với thương hiệu
                        for (GlassDTO product : productsByBrand) {
                            // Chỉ thêm tối đa 3 sản phẩm để tránh hiển thị quá nhiều
                            if (result.size() < 3) {
                                ProductSuggestion suggestion = createProductSuggestion(product);
                                result.add(suggestion);
                                log.info("Thêm sản phẩm thương hiệu '{}': {} (ID: {})",
                                        product.getBrand(), product.getName(), product.getId());
                            }
                        }
                    }
                }
            }

            log.info("Đã tìm thấy tổng cộng {} sản phẩm để gợi ý", result.size());
        } catch (Exception e) {
            log.error("Lỗi khi xử lý gợi ý sản phẩm từ text", e);
        }

        return result.toArray(new ProductSuggestion[0]);
    }

    /**
     * Tạo đối tượng ProductSuggestion từ GlassDTO
     */
    private ProductSuggestion createProductSuggestion(GlassDTO product) {
        String imageUrl = product.getImageFrontUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty() || imageUrl.contains("example.com")) {
            imageUrl = "https://placeholder.pics/svg/200x150/DEDEDE/555555/No%20Image";
        }

        String category = "Kính mắt";
        if (product.getCategoryName() != null && !product.getCategoryName().isEmpty()) {
            category = product.getCategoryName();
        } else if (product.getCategoryId() != null) {
            category = product.getCategoryId() == SUNGLASSES_CATEGORY_ID ? "Kính râm" : "Kính cận";
        }

        return ProductSuggestion.builder()
                .productId(product.getId().toString())
                .name(product.getName())
                .imageUrl(imageUrl)
                .price(product.getPrice())
                .category(category)
                .detailUrl(frontendBaseUrl + "/products/glasses/" + product.getId())
                .build();
    }

    /**
     * Extracts product mentions from text based on brand and product names
     * @param text the text to analyze
     * @return a map of brand names to product mentions
     */
    private Map<String, String> extractProductMentions(String text) {
        log.info("Extracting product mentions from text: {}", text.length() > 100 ? text.substring(0, 100) + "..." : text);
        Map<String, String> result = new HashMap<>();

        // Common brand phrases in both Vietnamese and English
        String[] brandKeywords = {
            "rayban", "ray-ban", "ray ban", 
            "gucci", "prada", "oakley", 
            "dior", "chanel", "versace", 
            "armani", "dolce", "gabbana", 
            "burberry", "tom ford", "coach", 
            "fendi", "persol", "miu miu", 
            "giorgio armani", "michael kors", 
            "polaroid", "carrera", "police"
        };

        // Convert text to lowercase for case-insensitive matching
        String lowercaseText = text.toLowerCase();

        // Look for brand mentions
        for (String brand : brandKeywords) {
            if (lowercaseText.contains(brand)) {
                result.put(brand, "");
                log.info("Found brand mention: {}", brand);
            }
        }

        // Look for specific product mentions in format "Brand Model"
        // Pattern to match "Brand Model" or "Brand Model Number"
        Pattern productPattern = Pattern.compile("(rayban|ray-ban|ray ban|gucci|prada|oakley|dior|chanel|versace|armani|coach)\\s+([a-zA-Z0-9-]+(?:\\s+[a-zA-Z0-9-]+){0,2})",
                Pattern.CASE_INSENSITIVE);
        Matcher productMatcher = productPattern.matcher(lowercaseText);

        while (productMatcher.find()) {
            String brand = productMatcher.group(1).toLowerCase();
            String model = productMatcher.group(2);
            result.put(brand, model);
            log.info("Found specific product mention: {} {}", brand, model);
        }

        return result;
    }

    /**
     * Extracts color mentions from text
     * @param text the text to analyze for color mentions
     * @return the mentioned color or null if none is found
     */
    private String extractMentionedColor(String text) {
        log.info("Phân tích màu sắc từ văn bản: {}", text.length() > 100 ? text.substring(0, 100) + "..." : text);

        // 1. Phân tích tên kính kèm màu sắc
        // Tạo một pattern tổng quát hơn cho tên kính có màu trong tên
        Pattern colorInNamePattern = Pattern.compile("\\b([A-Za-z0-9]+(?:\\s+[A-Za-z0-9]+){0,3})\\s+(green|blue|red|black|silver|gold|brown|pink|purple|yellow|white|gray|grey)\\b",
                Pattern.CASE_INSENSITIVE);
        Matcher colorInNameMatcher = colorInNamePattern.matcher(text);

        if (colorInNameMatcher.find()) {
            String glassName = colorInNameMatcher.group(1);
            String colorInName = colorInNameMatcher.group(2).toLowerCase();
            log.info("Tìm thấy kính có tên chứa màu: {} - màu: {}", glassName, colorInName);

            // Xử lý đặc biệt cho màu xanh
            if (colorInName.equals("green")) {
                return "green";
            } else if (colorInName.equals("blue")) {
                return "blue";
            }

            return colorInName;
        }

        // 2. Tìm màu sắc được đề cập sau từ "màu"
        Pattern colorPattern = Pattern.compile("màu\\s+([\\w\\s]+?)(?:\\s+|\\.|,|\\)|\"|$)");
        Matcher colorMatcher = colorPattern.matcher(text);

        if (colorMatcher.find()) {
            String color = colorMatcher.group(1).trim().toLowerCase();
            log.info("Tìm thấy màu từ cụm 'màu X': {}", color);

            // Xử lý đặc biệt cho "xanh lá"
            if (color.contains("xanh") && (color.contains("lá") || color.contains("green") || color.contains("cây") || color.contains("rêu"))) {
                log.info("Xác định đây là màu xanh lá (green)");
                return "green";
            }

            return color;
        }

        // 3. Tìm màu xanh lá từ các biến thể khác nhau
        String[] greenVariants = {"xanh lá", "green", "màu lá", "xanh lá cây", "xanh rêu", "xanh cây"};
        for (String variant : greenVariants) {
            if (text.toLowerCase().contains(variant)) {
                log.info("Tìm thấy từ khóa xanh lá: {}", variant);
                return "green";
            }
        }

        // 4. Tìm từ các key phrases liên quan đến màu sắc
        Map<String, String> colorKeyPhrases = new HashMap<>();
        colorKeyPhrases.put("kính màu xanh lá", "green");
        colorKeyPhrases.put("kính xanh lá", "green");
        colorKeyPhrases.put("kính green", "green");
        colorKeyPhrases.put("kính màu xanh dương", "blue");
        colorKeyPhrases.put("kính xanh dương", "blue");
        colorKeyPhrases.put("kính blue", "blue");
        colorKeyPhrases.put("kính màu đen", "black");
        colorKeyPhrases.put("kính đen", "black");
        colorKeyPhrases.put("kính màu nâu", "brown");
        colorKeyPhrases.put("kính nâu", "brown");

        for (Map.Entry<String, String> entry : colorKeyPhrases.entrySet()) {
            if (text.toLowerCase().contains(entry.getKey())) {
                log.info("Tìm thấy key phrase: {} -> màu: {}", entry.getKey(), entry.getValue());
                return entry.getValue();
            }
        }

        // 5. Tìm màu sắc đặc biệt
        String[] specialColors = {"gold rose", "rose gold", "vàng hồng", "hồng vàng", "gold"};
        for (String color : specialColors) {
            if (text.toLowerCase().contains(color)) {
                log.info("Tìm thấy màu đặc biệt: {}", color);
                return color;
            }
        }

        // 6. Tìm các màu sắc cơ bản
        Map<String, String> basicColors = new HashMap<>();
        basicColors.put("đen", "black");
        basicColors.put("black", "black");
        basicColors.put("trắng", "white");
        basicColors.put("white", "white");
        basicColors.put("đỏ", "red");
        basicColors.put("red", "red");
        basicColors.put("xanh dương", "blue");
        basicColors.put("xanh biển", "blue");
        basicColors.put("blue", "blue");
        basicColors.put("navy", "blue");
        basicColors.put("vàng", "yellow");
        basicColors.put("yellow", "yellow");
        basicColors.put("nâu", "brown");
        basicColors.put("brown", "brown");
        basicColors.put("bạc", "silver");
        basicColors.put("silver", "silver");
        basicColors.put("xám", "silver");
        basicColors.put("gray", "silver");
        basicColors.put("grey", "silver");
        basicColors.put("hồng", "pink");
        basicColors.put("pink", "pink");
        basicColors.put("tím", "purple");
        basicColors.put("purple", "purple");

        for (Map.Entry<String, String> entry : basicColors.entrySet()) {
            if (text.toLowerCase().contains(entry.getKey())) {
                log.info("Tìm thấy màu cơ bản: {} -> {}", entry.getKey(), entry.getValue());
                return entry.getValue();
            }
        }

        // 7. Xử lý trường hợp chỉ có từ "xanh" không rõ ràng
        if (text.toLowerCase().contains("xanh")) {
            log.info("Tìm thấy từ 'xanh' không rõ loại, phân tích ngữ cảnh");

            // Tìm trong context để xác định loại xanh
            boolean hasGreenContext = text.toLowerCase().contains("cây") ||
                    text.toLowerCase().contains("lá") ||
                    text.toLowerCase().contains("rêu") ||
                    text.toLowerCase().contains("green");

            boolean hasBlueContext = text.toLowerCase().contains("biển") ||
                    text.toLowerCase().contains("dương") ||
                    text.toLowerCase().contains("da trời") ||
                    text.toLowerCase().contains("blue") ||
                    text.toLowerCase().contains("navy");

            if (hasGreenContext) {
                log.info("Ngữ cảnh cho thấy đây là xanh lá (green)");
                return "green";
            } else if (hasBlueContext) {
                log.info("Ngữ cảnh cho thấy đây là xanh dương (blue)");
                return "blue";
            } else {
                // Mặc định khi không chắc chắn
                log.info("Không xác định rõ loại xanh, mặc định là xanh lá");
                return "green";
            }
        }

        return null;
    }
}