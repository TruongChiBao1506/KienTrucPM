package iuh.fit.se.chatbotservice.service;

import iuh.fit.se.chatbotservice.dto.GlassDTO;
import iuh.fit.se.chatbotservice.dto.NavigationSuggestion;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NavigationService {

    @Value("${frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    private final ProductDataService productDataService;
    private Map<String, String> navigationKeywords;

    public NavigationService(ProductDataService productDataService) {
        this.productDataService = productDataService;
    }

    @PostConstruct
    public void init() {
        navigationKeywords = new HashMap<>();

        // Đăng ký các từ khóa để tìm kiếm sản phẩm
        navigationKeywords.put("kính râm", "sunglasses");
        navigationKeywords.put("kính mát", "sunglasses");
        navigationKeywords.put("sunglasses", "sunglasses");

        navigationKeywords.put("kính cận", "eyeglasses");
        navigationKeywords.put("kính mắt", "eyeglasses");
        navigationKeywords.put("kính đọc sách", "eyeglasses");
        navigationKeywords.put("eyeglasses", "eyeglasses");

        navigationKeywords.put("kính râm nam", "sunglasses-men");
        navigationKeywords.put("kính mát nam", "sunglasses-men");

        navigationKeywords.put("kính râm nữ", "sunglasses-women");
        navigationKeywords.put("kính mát nữ", "sunglasses-women");

        navigationKeywords.put("kính cận nam", "eyeglasses-men");
        navigationKeywords.put("kính mắt nam", "eyeglasses-men");

        navigationKeywords.put("kính cận nữ", "eyeglasses-women");
        navigationKeywords.put("kính mắt nữ", "eyeglasses-women");

        navigationKeywords.put("ray-ban", "ray-ban");
        navigationKeywords.put("rayban", "ray-ban");
        navigationKeywords.put("gucci", "gucci");
        navigationKeywords.put("oakley", "oakley");
        navigationKeywords.put("prada", "prada");
    }

    public NavigationSuggestion findNavigationSuggestion(String message) {
        if (message == null) return null;

        String messageLower = message.toLowerCase();

        // Kiểm tra từ khóa có trong message không
        for (Map.Entry<String, String> entry : navigationKeywords.entrySet()) {
            if (messageLower.contains(entry.getKey())) {
                return createNavigationSuggestionIfProductsExist(entry.getValue());
            }
        }

        // Phân tích cú pháp phức tạp hơn để xác định ý định
        if (containsProductSearchIntent(messageLower)) {
            return determineProductNavigationFromIntent(messageLower);
        }

        return null;
    }

    private NavigationSuggestion createNavigationSuggestionIfProductsExist(String type) {
        List<GlassDTO> products = null;
        String path;
        String title;
        String description;

        switch (type) {
            case "sunglasses":
                products = productDataService.getSunglasses();
                path = "/products/sunglasses";
                title = "Kính râm";
                description = "Xem tất cả các mẫu kính râm";
                break;
            case "eyeglasses":
                products = productDataService.getEyeglasses();
                path = "/products/eyeglasses";
                title = "Kính cận";
                description = "Xem tất cả các mẫu kính cận";
                break;
            case "sunglasses-men":
                products = productDataService.getFilteredProducts(false, true, null, null, null, null, null, null);
                path = "/products/sunglasses/men";
                title = "Kính râm nam";
                description = "Xem các mẫu kính râm cho nam";
                break;
            case "sunglasses-women":
                products = productDataService.getFilteredProducts(false, false, null, null, null, null, null, null);
                path = "/products/sunglasses/women";
                title = "Kính râm nữ";
                description = "Xem các mẫu kính râm cho nữ";
                break;
            case "eyeglasses-men":
                products = productDataService.getFilteredProducts(true, true, null, null, null, null, null, null);
                path = "/products/eyeglasses/men";
                title = "Kính cận nam";
                description = "Xem các mẫu kính cận cho nam";
                break;
            case "eyeglasses-women":
                products = productDataService.getFilteredProducts(true, false, null, null, null, null, null, null);
                path = "/products/eyeglasses/women";
                title = "Kính cận nữ";
                description = "Xem các mẫu kính cận cho nữ";
                break;
            case "ray-ban":
            case "gucci":
            case "oakley":
            case "prada":
                products = productDataService.getFilteredProducts(true, true, type, null, null, null, null, null);
                if (products.isEmpty()) {
                    products = productDataService.getFilteredProducts(false, true, type, null, null, null, null, null);
                }
                path = "/products/brands/" + type;
                title = type;
                description = "Xem sản phẩm thương hiệu " + type;
                break;
            default:
                return null;
        }

        // Chỉ tạo gợi ý chuyển hướng nếu có sản phẩm trong database
        if (products != null && !products.isEmpty()) {
            return NavigationSuggestion.builder()
                    .url(frontendBaseUrl + path)
                    .title(title)
                    .description(description)
                    .type("category")
                    .autoRedirect(false)
                    .build();
        }
        
        return null;
    }

    private boolean containsProductSearchIntent(String message) {
        // Các mẫu câu chỉ ra rằng người dùng muốn xem sản phẩm
        return message.matches(".*?(muốn (xem|tìm|mua)|cho.*(xem|mua)|xem.*(sản phẩm|danh sách|các loại)|tìm.*(kính|sản phẩm)|danh sách|tôi cần|cần mua).*");
    }

    private NavigationSuggestion determineProductNavigationFromIntent(String message) {
        boolean hasSunglassesKeyword = message.matches(".*(kính râm|kính mát|sunglasses|chống nắng).*");
        boolean hasEyeglassesKeyword = message.matches(".*(kính cận|kính mắt|kính đọc|eyeglasses).*");
        boolean hasMenKeyword = message.matches(".*(nam giới|đàn ông|nam|men|man|cho nam).*");
        boolean hasWomenKeyword = message.matches(".*(nữ giới|phụ nữ|nữ|women|woman|cho nữ).*");

        // Xác định loại kính và giới tính
        if (hasSunglassesKeyword) {
            if (hasMenKeyword) {
                return createNavigationSuggestionIfProductsExist("sunglasses-men");
            } else if (hasWomenKeyword) {
                return createNavigationSuggestionIfProductsExist("sunglasses-women");
            } else {
                return createNavigationSuggestionIfProductsExist("sunglasses");
            }
        } else if (hasEyeglassesKeyword) {
            if (hasMenKeyword) {
                return createNavigationSuggestionIfProductsExist("eyeglasses-men");
            } else if (hasWomenKeyword) {
                return createNavigationSuggestionIfProductsExist("eyeglasses-women");
            } else {
                return createNavigationSuggestionIfProductsExist("eyeglasses");
            }
        }

        // Không còn tạo gợi ý điều hướng chung đến tất cả sản phẩm nữa
        return null;
    }
}