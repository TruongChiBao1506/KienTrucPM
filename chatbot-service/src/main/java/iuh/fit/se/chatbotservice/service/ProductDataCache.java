package iuh.fit.se.chatbotservice.service;

import jakarta.annotation.PostConstruct;
import iuh.fit.se.chatbotservice.dto.GlassDTO;
import iuh.fit.se.chatbotservice.client.ProductServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductDataCache {

    private final ProductServiceClient productServiceClient;

    // Cached data
    private List<GlassDTO> allProducts = new ArrayList<>();
    private List<String> allBrands = new ArrayList<>();
    private List<String> allShapes = new ArrayList<>();
    private List<String> allMaterials = new ArrayList<>();
    private List<String> allColors = new ArrayList<>();

    // Cache timestamps
    private Date lastCacheRefresh = null;

    @PostConstruct
    public void initialize() {
        refreshCache();
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void refreshCache() {
        log.info("Refreshing product cache");
        try {
            // Load all products
            ResponseEntity<Map<String, Object>> response = productServiceClient.getAllGlasses();
            if (response.getBody() != null && response.getBody().containsKey("data")) {
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) response.getBody().get("data");
                allProducts = productMaps.stream()
                        .map(this::mapToGlassDTO)
                        .collect(Collectors.toList());
                log.info("Loaded {} products into cache", allProducts.size());
            }

            // Load attributes
            loadAttributes();

            lastCacheRefresh = new Date();
            log.info("Product cache refreshed successfully at {}", lastCacheRefresh);
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
                log.info("Loaded {} brands", allBrands.size());
            }

            // Load shapes
            ResponseEntity<Map<String, Object>> shapesResponse = productServiceClient.getAllShapes();
            if (shapesResponse.getBody() != null && shapesResponse.getBody().containsKey("data")) {
                allShapes = (List<String>) shapesResponse.getBody().get("data");
                log.info("Loaded {} shapes", allShapes.size());
            }

            // Load materials
            ResponseEntity<Map<String, Object>> materialsResponse = productServiceClient.getAllMaterials();
            if (materialsResponse.getBody() != null && materialsResponse.getBody().containsKey("data")) {
                allMaterials = (List<String>) materialsResponse.getBody().get("data");
                log.info("Loaded {} materials", allMaterials.size());
            }

            // Load colors
            ResponseEntity<Map<String, Object>> colorsResponse = productServiceClient.getAllColors();
            if (colorsResponse.getBody() != null && colorsResponse.getBody().containsKey("data")) {
                allColors = (List<String>) colorsResponse.getBody().get("data");
                log.info("Loaded {} colors", allColors.size());
            }
        } catch (Exception e) {
            log.error("Error loading product attributes", e);
        }
    }

    // Map product data from API to GlassDTO
    private GlassDTO mapToGlassDTO(Map<String, Object> map) {
        try {
            GlassDTO dto = new GlassDTO();

            // Basic properties
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

            // Image URLs
            String imageFrontUrl = (String) map.get("imageFrontUrl");
            dto.setImageFrontUrl(imageFrontUrl);
            dto.setImageSideUrl((String) map.get("imageSideUrl"));

            // Gender
            Object genderObj = map.get("gender");
            if (genderObj != null) {
                dto.setGender(Boolean.parseBoolean(genderObj.toString()));
            }

            // Stock
            Object stockObj = map.get("stock");
            if (stockObj != null) {
                dto.setStock(Integer.parseInt(stockObj.toString()));
            }

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

    // Getters for cached data
    public List<GlassDTO> getAllProducts() {
        return allProducts;
    }

    public List<GlassDTO> getEyeglasses() {
        return allProducts.stream()
                .filter(glass -> glass.getCategoryId() != null && glass.getCategoryId() == 2L)
                .collect(Collectors.toList());
    }

    public List<GlassDTO> getSunglasses() {
        return allProducts.stream()
                .filter(glass -> glass.getCategoryId() != null && glass.getCategoryId() == 1L)
                .collect(Collectors.toList());
    }

    public GlassDTO getProductById(Long id) {
        return allProducts.stream()
                .filter(glass -> glass.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Date getLastCacheRefresh() {
        return lastCacheRefresh;
    }

    public List<String> getAllColors() {
        return allColors;
    }

    public List<String> getAllBrands() {
        return allBrands;
    }

    public List<String> getAllShapes() {
        return allShapes;
    }

    public List<String> getAllMaterials() {
        return allMaterials;
    }
}