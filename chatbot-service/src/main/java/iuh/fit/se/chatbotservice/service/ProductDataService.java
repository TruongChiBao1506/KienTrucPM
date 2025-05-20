package iuh.fit.se.chatbotservice.service;

import iuh.fit.se.chatbotservice.client.ProductServiceClient;
import iuh.fit.se.chatbotservice.dto.GlassDTO;
import iuh.fit.se.chatbotservice.dto.ProductSuggestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDataService {

    private final ProductServiceClient productServiceClient;
    private final ProductDataCache productDataCache;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    public void refreshProductCache() {
        productDataCache.refreshCache();
    }

    public List<GlassDTO> getAllProducts() {
        return productDataCache.getAllProducts();
    }

    public List<GlassDTO> getEyeglasses() {
        return productDataCache.getEyeglasses();
    }

    public List<GlassDTO> getSunglasses() {
        return productDataCache.getSunglasses();
    }

    public GlassDTO getProductById(Long id) {
        // Try cache first
        GlassDTO cachedProduct = productDataCache.getProductById(id);
        if (cachedProduct != null) {
            return cachedProduct;
        }

        // If not in cache, try API
        try {
            ResponseEntity<Map<String, Object>> response = productServiceClient.getGlassById(id);
            if (response.getBody() != null && response.getBody().containsKey("data")) {
                Map<String, Object> productMap = (Map<String, Object>) response.getBody().get("data");
                return mapToGlassDTO(productMap);
            }
        } catch (Exception e) {
            log.error("Error getting product by id: {}", id, e);
        }
        return null;
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
        sb.append("Các thương hiệu có sẵn: ").append(String.join(", ", productDataCache.getAllBrands())).append("\n");
        sb.append("Các hình dạng gọng kính: ").append(String.join(", ", productDataCache.getAllShapes())).append("\n");
        sb.append("Các chất liệu: ").append(String.join(", ", productDataCache.getAllMaterials())).append("\n");
        sb.append("Các màu sắc: ").append(String.join(", ", productDataCache.getAllColors())).append("\n");

        return sb.toString();
    }

    /**
     * Returns all available colors from the product data cache
     * @return List of color names
     */
    public List<String> getAllColors() {
        return productDataCache.getAllColors();
    }

    /**
     * Returns all available materials from the product data cache
     * @return List of material names
     */
    public List<String> getAllMaterials() {
        return productDataCache.getAllMaterials();
    }

    // Helper method to map product data
    private GlassDTO mapToGlassDTO(Map<String, Object> map) {
        try {
            GlassDTO dto = new GlassDTO();

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

            String imageFrontUrl = (String) map.get("imageFrontUrl");
            dto.setImageFrontUrl(imageFrontUrl);
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

            Map<String, Object> specifications = (Map<String, Object>) map.get("specifications");
            if (specifications != null) {
                dto.setShape((String) specifications.get("shape"));
                dto.setMaterial((String) specifications.get("material"));
            }

            Map<String, Object> frameSize = (Map<String, Object>) map.get("frameSize");
            if (frameSize != null) {
                dto.setFrameWidth(parseDoubleOrNull(frameSize.get("frameWidth")));
                dto.setTempleLength(parseDoubleOrNull(frameSize.get("templeLength")));
                dto.setLensHeight(parseDoubleOrNull(frameSize.get("lensHeight")));
                dto.setLensWidth(parseDoubleOrNull(frameSize.get("lensWidth")));
                dto.setBridgeWidth(parseDoubleOrNull(frameSize.get("bridgeWidth")));
            }

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
}