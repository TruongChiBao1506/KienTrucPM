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
            dto.setId(Long.valueOf(map.get("id").toString()));
            dto.setName((String) map.get("name"));
            dto.setBrand((String) map.get("brand"));
            dto.setPrice(Double.valueOf(map.get("price").toString()));
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
            dto.setGender(Boolean.parseBoolean(map.get("gender").toString()));
            dto.setStock(Integer.parseInt(map.get("stock").toString()));
            dto.setDescription((String) map.get("description"));

            // Extract specifications
            Map<String, Object> specifications = (Map<String, Object>) map.get("specifications");
            if (specifications != null) {
                dto.setShape((String) specifications.get("shape"));
                dto.setMaterial((String) specifications.get("material"));
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
                dto.setCategoryId(Long.valueOf(category.get("id").toString()));
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
                .filter(glass -> glass.getCategoryId() != null && glass.getCategoryId() == 1L)
                .collect(Collectors.toList());
    }

    public List<GlassDTO> getSunglasses() {
        return getAllProducts().stream()
                .filter(glass -> glass.getCategoryId() != null && glass.getCategoryId() == 2L)
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
            String categoryName = entry.getKey() == 1L ? "Kính cận (Eyeglasses)" : "Kính râm (Sunglasses)";
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
        if (products == null || products.isEmpty()) {
            return new ProductSuggestion[0];
        }
        
        // Debug: In ra thông tin hình ảnh của tất cả sản phẩm để kiểm tra
        for (GlassDTO product : products) {
            log.info("Debug - Product: ID={}, Name={}, Color={}, ImageFrontUrl={}",
                    product.getId(), product.getName(), product.getColorName(), product.getImageFrontUrl());
        }

        return products.stream()
                .map(product -> {
                    String imageUrl = product.getImageFrontUrl();
                    log.debug("Converting to ProductSuggestion - Product ID: {}, imageUrl: {}", product.getId(), imageUrl);
                    
                    // Đảm bảo luôn có URL hình ảnh hợp lệ và KHÔNG BAO GIỜ sử dụng URL example.com
                    if (imageUrl == null || imageUrl.trim().isEmpty() || imageUrl.contains("example.com")) {
                        imageUrl = "https://placeholder.pics/svg/200x150/DEDEDE/555555/No%20Image";
                        log.warn("Using default image URL for product ID: {} because original URL was invalid or missing", product.getId());
                    }
                    
                    // Đảm bảo luôn lấy category name từ database
                    String category = "Kính mắt";
                    if (product.getCategoryName() != null && !product.getCategoryName().isEmpty()) {
                        category = product.getCategoryName();
                    } else if (product.getCategoryId() != null) {
                        category = product.getCategoryId() == 1L ? "Kính râm" : "Kính cận";
                    }
                    
                    return ProductSuggestion.builder()
                            .productId(product.getId().toString())
                            .name(product.getName())
                            .imageUrl(imageUrl)
                            .price(product.getPrice())
                            .category(category)
                            .detailUrl("http://localhost:8889/products/glasses/" + product.getId())
                            .build();
                })
                .toArray(ProductSuggestion[]::new);
    }
}