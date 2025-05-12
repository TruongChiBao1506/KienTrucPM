package iuh.fit.se.chatbotservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.chatbotservice.client.ProductServiceClient;
import iuh.fit.se.chatbotservice.dto.GlassDTO;
import iuh.fit.se.chatbotservice.dto.ProductSuggestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDataService {

    private final ProductServiceClient productServiceClient;
    private final ObjectMapper objectMapper;
    
    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    // Cache data
    private List<GlassDTO> allProducts = new ArrayList<>();
    private List<String> allBrands = new ArrayList<>();
    private List<String> allShapes = new ArrayList<>();
    private List<String> allMaterials = new ArrayList<>();
    private List<String> allColors = new ArrayList<>();

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void refreshProductCache() {
        log.info("Refreshing product cache");
        try {
            // Load all products
            ResponseEntity<Map<String, Object>> response = productServiceClient.getAllGlasses();
            if (response.getBody() != null && response.getBody().containsKey("data")) {
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) response.getBody().get("data");
                allProducts = productMaps.stream()
                        .map(this::mapToGlassDTO)
                        .collect(Collectors.toList());
                log.info("Loaded {} products", allProducts.size());
            }

            // Load attributes
            loadAttributes();

            log.info("Product cache refreshed successfully");
        } catch (Exception e) {
            log.error("Failed to refresh product cache", e);
        }
    }

    private void loadAttributes() {
        try {
            // Load brands
            ResponseEntity<Map<String, Object>> brandsResponse = productServiceClient.getAllBrands();
            if (brandsResponse.getBody() != null && brandsResponse.getBody().containsKey("data")) {
                allBrands = (List<String>) brandsResponse.getBody().get("data");
            }

            // Load shapes
            ResponseEntity<Map<String, Object>> shapesResponse = productServiceClient.getAllShapes();
            if (shapesResponse.getBody() != null && shapesResponse.getBody().containsKey("data")) {
                allShapes = (List<String>) shapesResponse.getBody().get("data");
            }

            // Load materials
            ResponseEntity<Map<String, Object>> materialsResponse = productServiceClient.getAllMaterials();
            if (materialsResponse.getBody() != null && materialsResponse.getBody().containsKey("data")) {
                allMaterials = (List<String>) materialsResponse.getBody().get("data");
            }

            // Load colors
            ResponseEntity<Map<String, Object>> colorsResponse = productServiceClient.getAllColors();
            if (colorsResponse.getBody() != null && colorsResponse.getBody().containsKey("data")) {
                allColors = (List<String>) colorsResponse.getBody().get("data");
            }
        } catch (Exception e) {
            log.error("Error loading product attributes", e);
        }
    }

    private GlassDTO mapToGlassDTO(Map<String, Object> map) {
        try {
            GlassDTO dto = new GlassDTO();
            
            // Kiểm tra null trước khi gọi toString()
            Object idObj = map.get("id");
            if (idObj != null) {
                dto.setId(Long.valueOf(idObj.toString()));
            }
            
            dto.setName((String) map.get("name"));
            dto.setBrand((String) map.get("brand"));
            
            Object priceObj = map.get("price");
            if (priceObj != null) {
                dto.setPrice(Double.valueOf(priceObj.toString()));
            }
            
            dto.setColorName((String) map.get("colorName"));
            dto.setColorCode((String) map.get("colorCode"));
            
            // Xử lý đặc biệt và log URL hình ảnh để debug
            String imageFrontUrl = (String) map.get("imageFrontUrl");
            if (imageFrontUrl != null) {
                log.debug("Product ID: {}, Original imageFrontUrl: {}", map.get("id"), imageFrontUrl);
                dto.setImageFrontUrl(imageFrontUrl);
            } else {
                log.warn("Product ID: {} has null imageFrontUrl", map.get("id"));
            }
            
            dto.setImageSideUrl((String) map.get("imageSideUrl"));
            
            Object genderObj = map.get("gender");
            if (genderObj != null) {
                dto.setGender(Boolean.parseBoolean(genderObj.toString()));
            }
            
            Object stockObj = map.get("stock");
            if (stockObj != null) {
                dto.setStock(Integer.parseInt(stockObj.toString()));
            }
            
            dto.setDescription((String) map.get("description"));

            // Extract specifications - Cải thiện cách trích xuất chất liệu
            Map<String, Object> specifications = (Map<String, Object>) map.get("specifications");
            if (specifications != null) {
                String shape = (String) specifications.get("shape");
                String material = (String) specifications.get("material");
                
                dto.setShape(shape);
                dto.setMaterial(material);
                
                // Log thông tin chất liệu để debug
                if (material != null && 
                    (material.toLowerCase().contains("carbon fiber") || 
                     material.toLowerCase().contains("stainless steel") ||
                     material.toLowerCase().contains("mixed"))) {
                    log.info("Đã trích xuất sản phẩm có chất liệu đặc biệt - ID: {}, Tên: {}, Chất liệu: {}",
                            dto.getId(), dto.getName(), material);
                }
            } else {
                // Thử trích xuất từ nơi khác nếu specifications là null
                Object materialObj = map.get("material");
                if (materialObj != null) {
                    String material = materialObj.toString();
                    dto.setMaterial(material);
                    log.info("Trích xuất chất liệu từ trường material trực tiếp: {}", material);
                }
            }

            // Extract frame size
            Map<String, Object> frameSize = (Map<String, Object>) map.get("frameSize");
            if (frameSize != null) {
                dto.setFrameWidth(parseDoubleOrNull(frameSize.get("frameWidth")));
                dto.setTempleLength(parseDoubleOrNull(frameSize.get("templeLength")));
                dto.setLensHeight(parseDoubleOrNull(frameSize.get("lensHeight")));
                dto.setLensWidth(parseDoubleOrNull(frameSize.get("lensWidth")));
                dto.setBridgeWidth(parseDoubleOrNull(frameSize.get("bridgeWidth")));
            }

            // Extract category
            Map<String, Object> category = (Map<String, Object>) map.get("category");
            if (category != null) {
                Object categoryIdObj = category.get("id");
                if (categoryIdObj != null) {
                    dto.setCategoryId(Long.valueOf(categoryIdObj.toString()));
                }
                dto.setCategoryName((String) category.get("name"));
            }

            return dto;
        } catch (Exception e) {
            log.error("Error mapping product data for ID: {}", map.get("id"), e);
            return new GlassDTO();
        }
    }

    private Double parseDoubleOrNull(Object value) {
        if (value == null) return null;
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<GlassDTO> getAllProducts() {
        if (allProducts.isEmpty()) {
            refreshProductCache();
        }
        return allProducts;
    }

    public List<GlassDTO> getEyeglasses() {
        return getAllProducts().stream()
                .filter(glass -> glass.getCategoryId() != null && glass.getCategoryId() == 2L)
                .collect(Collectors.toList());
    }

    public List<GlassDTO> getSunglasses() {
        return getAllProducts().stream()
                .filter(glass -> glass.getCategoryId() != null && glass.getCategoryId() == 1L)
                .collect(Collectors.toList());
    }

    public List<GlassDTO> searchProducts(String keyword) {
        try {
            ResponseEntity<Map<String, Object>> response = productServiceClient.searchProducts(keyword);
            if (response.getBody() != null && response.getBody().containsKey("data")) {
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) response.getBody().get("data");
                return productMaps.stream()
                        .map(this::mapToGlassDTO)
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error searching products with keyword: {}", keyword, e);
            return new ArrayList<>();
        }
    }

    public GlassDTO getProductById(Long id) {
        try {
            ResponseEntity<Map<String, Object>> response = productServiceClient.getGlassById(id);
            if (response.getBody() != null && response.getBody().containsKey("data")) {
                Map<String, Object> productMap = (Map<String, Object>) response.getBody().get("data");
                return mapToGlassDTO(productMap);
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting product by id: {}", id, e);
            return null;
        }
    }

    public List<GlassDTO> getFilteredProducts(boolean isEyeglasses, boolean forMen,
                                              String brand, String shape, String material,
                                              String color, Double minPrice, Double maxPrice) {
        try {
            String minPriceStr = minPrice != null ? minPrice.toString() : null;
            String maxPriceStr = maxPrice != null ? maxPrice.toString() : null;

            ResponseEntity<Map<String, Object>> response;
            if (isEyeglasses) {
                if (forMen) {
                    response = productServiceClient.getMenEyeglasses(brand, shape, material, color, minPriceStr, maxPriceStr);
                } else {
                    response = productServiceClient.getWomenEyeglasses(brand, shape, material, color, minPriceStr, maxPriceStr);
                }
            } else {
                if (forMen) {
                    response = productServiceClient.getMenSunglasses(brand, shape, material, color, minPriceStr, maxPriceStr);
                } else {
                    response = productServiceClient.getWomenSunglasses(brand, shape, material, color, minPriceStr, maxPriceStr);
                }
            }

            if (response.getBody() != null && response.getBody().containsKey("data")) {
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) response.getBody().get("data");
                return productMaps.stream()
                        .map(this::mapToGlassDTO)
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error getting filtered products", e);
            return new ArrayList<>();
        }
    }

    public String getFormattedProductInfo() {
        StringBuilder sb = new StringBuilder("Thông tin sản phẩm hiện có trong cửa hàng:\n\n");

        // Group by category (Eyeglasses/Sunglasses)
        Map<Long, List<GlassDTO>> productsByCategory = getAllProducts().stream()
                .filter(p -> p.getCategoryId() != null)
                .collect(Collectors.groupingBy(GlassDTO::getCategoryId));

        // Process categories
        for (Map.Entry<Long, List<GlassDTO>> entry : productsByCategory.entrySet()) {
            // Sửa lại phân loại: ID 1 là kính râm (Sunglasses), ID 2 là kính cận (Eyeglasses)
            String categoryName = entry.getKey() == 1L ? "Kính râm (Sunglasses)" : "Kính cận (Eyeglasses)";
            sb.append("Danh mục: ").append(categoryName).append("\n");

            // Group by brand for cleaner presentation
            Map<String, List<GlassDTO>> productsByBrand = entry.getValue().stream()
                    .collect(Collectors.groupingBy(GlassDTO::getBrand));

            for (Map.Entry<String, List<GlassDTO>> brandEntry : productsByBrand.entrySet()) {
                sb.append("  Thương hiệu: ").append(brandEntry.getKey()).append("\n");

                // Group by gender
                Map<Boolean, List<GlassDTO>> productsByGender = brandEntry.getValue().stream()
                        .collect(Collectors.groupingBy(GlassDTO::isGender));

                // Men products
                if (productsByGender.containsKey(true)) {
                    sb.append("    Nam:\n");
                    List<GlassDTO> menProducts = productsByGender.get(true).stream()
                            .limit(3) // Limit to 3 per gender to keep prompt size reasonable
                            .collect(Collectors.toList());

                    for (GlassDTO product : menProducts) {
                        sb.append("      - ")
                                .append(product.getName())
                                .append(" (ID: ").append(product.getId()).append(")")
                                .append(": Giá ").append(String.format("%,.0f", product.getPrice())).append("đ")
                                .append(", Hình dạng: ").append(product.getShape())
                                .append(", Màu: ").append(product.getColorName())
                                .append("\n");
                    }
                }

                // Women products
                if (productsByGender.containsKey(false)) {
                    sb.append("    Nữ:\n");
                    List<GlassDTO> womenProducts = productsByGender.get(false).stream()
                            .limit(3) // Limit to 3 per gender to keep prompt size reasonable
                            .collect(Collectors.toList());

                    for (GlassDTO product : womenProducts) {
                        sb.append("      - ")
                                .append(product.getName())
                                .append(" (ID: ").append(product.getId()).append(")")
                                .append(": Giá ").append(String.format("%,.0f", product.getPrice())).append("đ")
                                .append(", Hình dạng: ").append(product.getShape())
                                .append(", Màu: ").append(product.getColorName())
                                .append("\n");
                    }
                }

                sb.append("\n");
            }
        }

        // Add attribute information
        sb.append("Các thương hiệu có sẵn: ").append(String.join(", ", allBrands)).append("\n");
        sb.append("Các hình dạng gọng kính: ").append(String.join(", ", allShapes)).append("\n");
        sb.append("Các chất liệu: ").append(String.join(", ", allMaterials)).append("\n");
        sb.append("Các màu sắc: ").append(String.join(", ", allColors)).append("\n");

        return sb.toString();
    }

    public ProductSuggestion[] convertToProductSuggestions(List<GlassDTO> products) {
        // Kiểm tra kết quả rỗng
        if (products == null || products.isEmpty()) {
            return new ProductSuggestion[0];
        }
        
        log.info("Bắt đầu chuyển đổi {} sản phẩm thành gợi ý", products.size());
        
        // Tìm kính có giá cao nhất
        GlassDTO mostExpensiveProduct = products.stream()
            .max(Comparator.comparing(GlassDTO::getPrice))
            .orElse(null);
            
        if (mostExpensiveProduct != null) {
            log.info("Sản phẩm có giá cao nhất trong danh sách: {} - {}", 
                     mostExpensiveProduct.getName(), mostExpensiveProduct.getPrice());
        }
        
        // Always return just the most expensive product for now
        // This is a temporary fix until we can diagnose the issue more thoroughly
        if (products.size() > 1 && mostExpensiveProduct != null) {
            log.warn("Phát hiện nhiều sản phẩm trong kết quả tìm kiếm kính mắc nhất. " +
                    "Chủ động chỉ giữ lại sản phẩm có giá cao nhất: {} - {}", 
                    mostExpensiveProduct.getName(), mostExpensiveProduct.getPrice());
            
            List<GlassDTO> singleProduct = Collections.singletonList(mostExpensiveProduct);
            
            return singleProduct.stream()
                .map(this::createProductSuggestion)
                .toArray(ProductSuggestion[]::new);
        }

        // Limit to 5 products maximum
        List<GlassDTO> limitedProducts = products;
        if (products.size() > 5) {
            limitedProducts = products.subList(0, 5);
        }

        return limitedProducts.stream()
                .map(this::createProductSuggestion)
                .toArray(ProductSuggestion[]::new);
    }
    
    // Helper method to create a product suggestion from a glass DTO
    private ProductSuggestion createProductSuggestion(GlassDTO glass) {
        if (glass == null) return null;
        
        String imageUrl = glass.getImageFrontUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty() || imageUrl.contains("example.com")) {
            imageUrl = frontendBaseUrl + "/placeholder.pics/svg/200x150/DEDEDE/555555/No%20Image";
        }
        
        String category = "Kính mắt";
        if (glass.getCategoryName() != null && !glass.getCategoryName().isEmpty()) {
            category = glass.getCategoryName();
        } else if (glass.getCategoryId() != null) {
            // Sửa lại đúng phân loại: ID 1 là kính râm (Sunglasses), ID 2 là kính cận (Eyeglasses)
            category = glass.getCategoryId() == 1L ? "Kính râm" : "Kính cận";
        }
        
        log.info("Tạo gợi ý sản phẩm: ID={}, Name={}, CategoryID={}, CategoryName={}, ShowingCategory={}", 
                 glass.getId(), glass.getName(), glass.getCategoryId(), glass.getCategoryName(), category);
        
        return ProductSuggestion.builder()
                .productId(glass.getId().toString())
                .name(glass.getName())
                .imageUrl(imageUrl)
                .price(glass.getPrice())
                .category(category)
                .detailUrl(frontendBaseUrl + "/products/glasses/" + glass.getId())
                .build();
    }

    /**
     * Trả về danh sách tất cả các màu sắc có sẵn
     * @return Danh sách các màu sắc
     */
    public List<String> getAllColors() {
        if (allColors.isEmpty()) {
            refreshProductCache();
        }
        return allColors;
    }

    /**
     * Trả về danh sách tất cả các thương hiệu có sẵn
     * @return Danh sách các thương hiệu
     */
    public List<String> getAllBrands() {
        if (allBrands.isEmpty()) {
            refreshProductCache();
        }
        return allBrands;
    }

    /**
     * Trả về danh sách tất cả các hình dạng gọng kính có sẵn
     * @return Danh sách các hình dạng
     */
    public List<String> getAllShapes() {
        if (allShapes.isEmpty()) {
            refreshProductCache();
        }
        return allShapes;
    }

    /**
     * Trả về danh sách tất cả các chất liệu có sẵn
     * @return Danh sách các chất liệu
     */
    public List<String> getAllMaterials() {
        if (allMaterials.isEmpty()) {
            refreshProductCache();
        }
        return allMaterials;
    }
}