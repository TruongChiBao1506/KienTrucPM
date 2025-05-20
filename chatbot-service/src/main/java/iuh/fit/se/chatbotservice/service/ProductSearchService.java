package iuh.fit.se.chatbotservice.service;

import iuh.fit.se.chatbotservice.dto.GlassDTO;
import iuh.fit.se.chatbotservice.dto.ProductSuggestion;
import iuh.fit.se.chatbotservice.dto.SearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductDataService productDataService;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    // Constants for product categories
    private static final long SUNGLASSES_CATEGORY_ID = 1L;
    private static final long EYEGLASSES_CATEGORY_ID = 2L;

    /**
     * Search for products based on search criteria extracted from user intent
     */
    public List<GlassDTO> findProducts(SearchCriteria criteria) {
        // Always refresh cache before searching to ensure latest data
        productDataService.refreshProductCache();
        log.info("Searching products with criteria: {}", criteria);

        // Get all products
        List<GlassDTO> allProducts = productDataService.getAllProducts();
        log.info("Total products in database: {}", allProducts.size());

        // Apply filters
        List<GlassDTO> filteredProducts = applyFilters(allProducts, criteria);
        log.info("After applying all filters: {} products found", filteredProducts.size());

        // Apply special search if needed (most expensive, cheapest)
        List<GlassDTO> result = applySpecialSearch(filteredProducts, criteria);
        log.info("Final result after special search: {} products", result.size());

        return result;
    }

    /**
     * Apply all filters to the product list
     */
    private List<GlassDTO> applyFilters(List<GlassDTO> products, SearchCriteria criteria) {
        List<GlassDTO> filtered = new ArrayList<>(products);

        // Filter by product type (eyeglasses vs sunglasses)
        filtered = filterByProductType(filtered, criteria);

        // Filter by gender
        filtered = filterByGender(filtered, criteria);

        // Filter by brand
        if (criteria.getBrand() != null && !criteria.getBrand().isEmpty()) {
            filtered = filtered.stream()
                    .filter(glass -> glass.getBrand() != null &&
                            glass.getBrand().toLowerCase().contains(criteria.getBrand().toLowerCase()))
                    .collect(Collectors.toList());
            log.info("After brand filter: {} products", filtered.size());
        }

        // Filter by shape
        if (criteria.getShape() != null && !criteria.getShape().isEmpty()) {
            filtered = filtered.stream()
                    .filter(glass -> glass.getShape() != null &&
                            glass.getShape().toLowerCase().contains(criteria.getShape().toLowerCase()))
                    .collect(Collectors.toList());
            log.info("After shape filter: {} products", filtered.size());
        }

        // Filter by material
        if (criteria.getMaterial() != null && !criteria.getMaterial().isEmpty()) {
            filtered = filterByMaterial(filtered, criteria.getMaterial());
            log.info("After material filter: {} products", filtered.size());
        }

        // Filter by color
        if (criteria.getColor() != null && !criteria.getColor().isEmpty()) {
            filtered = filterByColor(filtered, criteria.getColor());
            log.info("After color filter: {} products", filtered.size());
        }

        // Filter by price range
        if (criteria.getMinPrice() != null) {
            filtered = filtered.stream()
                    .filter(glass -> glass.getPrice() >= criteria.getMinPrice())
                    .collect(Collectors.toList());
            log.info("After min price filter: {} products", filtered.size());
        }

        if (criteria.getMaxPrice() != null) {
            filtered = filtered.stream()
                    .filter(glass -> glass.getPrice() <= criteria.getMaxPrice())
                    .collect(Collectors.toList());
            log.info("After max price filter: {} products", filtered.size());
        }

        return filtered;
    }

    /**
     * Filter products by type (eyeglasses vs sunglasses)
     */
    private List<GlassDTO> filterByProductType(List<GlassDTO> products, SearchCriteria criteria) {
        // Check what type of glasses user is looking for
        if (criteria.isWantsEyeglasses() && !criteria.isWantsSunglasses()) {
            // Looking for eyeglasses only
            log.info("Filtering for eyeglasses only");
            return products.stream()
                    .filter(glass -> glass.getCategoryId() != null && glass.getCategoryId().equals(EYEGLASSES_CATEGORY_ID))
                    .collect(Collectors.toList());
        } else if (criteria.isWantsSunglasses() && !criteria.isWantsEyeglasses()) {
            // Looking for sunglasses only
            log.info("Filtering for sunglasses only");
            return products.stream()
                    .filter(glass -> glass.getCategoryId() != null && glass.getCategoryId().equals(SUNGLASSES_CATEGORY_ID))
                    .collect(Collectors.toList());
        } else if (criteria.isWantsBoth()) {
            // Looking for both, no filtering needed
            log.info("User wants both types, no filtering by type");
            return products;
        } else {
            // No specific type mentioned, return all
            log.info("No specific type requested, returning all products");
            return products;
        }
    }

    /**
     * Filter products by gender
     */
    private List<GlassDTO> filterByGender(List<GlassDTO> products, SearchCriteria criteria) {
        if (criteria.isForMen() && !criteria.isForWomen()) {
            // Looking for men's glasses
            log.info("Filtering for men's glasses");
            return products.stream()
                    .filter(GlassDTO::isGender)  // true means men's glasses
                    .collect(Collectors.toList());
        } else if (criteria.isForWomen() && !criteria.isForMen()) {
            // Looking for women's glasses
            log.info("Filtering for women's glasses");
            return products.stream()
                    .filter(glass -> !glass.isGender())  // false means women's glasses
                    .collect(Collectors.toList());
        } else {
            // No gender preference or both genders, return all
            log.info("No specific gender preference, returning all products");
            return products;
        }
    }

    /**
     * Filter products by material with enhanced flexibility
     */
    private List<GlassDTO> filterByMaterial(List<GlassDTO> products, String material) {
        String normalizedMaterial = normalizeMaterial(material);
        log.info("Filtering by material: {} (normalized to: {})", material, normalizedMaterial);

        // Handle special materials
        boolean isCarbonFiber = normalizedMaterial.contains("carbon") || normalizedMaterial.contains("fiber");
        boolean isStainlessSteel = normalizedMaterial.contains("stainless") || normalizedMaterial.contains("steel");
        boolean isPlastic = normalizedMaterial.equals("plastic") || normalizedMaterial.contains("acetate");
        boolean isMetal = normalizedMaterial.equals("metal") || normalizedMaterial.equals("kim loại");

        return products.stream()
                .filter(glass -> {
                    if (glass.getMaterial() == null) return false;

                    String productMaterial = glass.getMaterial().toLowerCase();

                    // Carbon fiber check
                    if (isCarbonFiber && (productMaterial.contains("carbon") || productMaterial.contains("fiber"))) {
                        return true;
                    }

                    // Stainless steel check
                    if (isStainlessSteel && (productMaterial.contains("stainless") ||
                            productMaterial.contains("steel") ||
                            productMaterial.contains("thép"))) {
                        return true;
                    }

                    // Plastic check
                    if (isPlastic && (productMaterial.contains("plastic") ||
                            productMaterial.contains("nhựa") ||
                            productMaterial.contains("acetate"))) {
                        return true;
                    }

                    // Metal check
                    if (isMetal && (productMaterial.contains("metal") ||
                            productMaterial.contains("kim loại"))) {
                        return true;
                    }

                    // General contains check
                    return productMaterial.contains(normalizedMaterial) ||
                            normalizedMaterial.contains(productMaterial);
                })
                .collect(Collectors.toList());
    }

    /**
     * Filter products by color with enhanced flexibility
     */
    private List<GlassDTO> filterByColor(List<GlassDTO> products, String color) {
        String normalizedColor = normalizeColor(color);
        log.info("Filtering by color: {} (normalized to: {})", color, normalizedColor);

        return products.stream()
                .filter(glass -> {
                    if (glass.getColorName() == null) return false;

                    String productColor = glass.getColorName().toLowerCase();
                    String normalizedProductColor = normalizeColor(productColor);

                    // Exact match after normalization
                    if (normalizedProductColor != null && normalizedProductColor.equals(normalizedColor)) {
                        return true;
                    }

                    // Special case for green
                    if (normalizedColor.equals("green") &&
                            (productColor.contains("green") || productColor.contains("xanh lá"))) {
                        return true;
                    }

                    // Special case for blue
                    if (normalizedColor.equals("blue") &&
                            (productColor.contains("blue") || productColor.contains("xanh dương") ||
                                    productColor.contains("navy"))) {
                        return true;
                    }

                    // Special case for brown
                    if (normalizedColor.equals("brown") &&
                            (productColor.contains("brown") || productColor.contains("nâu") ||
                                    productColor.contains("coffee") || productColor.contains("tortoise"))) {
                        return true;
                    }

                    // Special case for black
                    if (normalizedColor.equals("black") &&
                            (productColor.contains("black") || productColor.contains("đen"))) {
                        return true;
                    }

                    // Contains check for flexibility
                    return productColor.contains(normalizedColor) || normalizedColor.contains(productColor);
                })
                .collect(Collectors.toList());
    }

    /**
     * Apply special search criteria (most expensive, cheapest)
     */
    private List<GlassDTO> applySpecialSearch(List<GlassDTO> products, SearchCriteria criteria) {
        if (products.isEmpty()) {
            return products;
        }

        // Handle most expensive search
        if (criteria.isWantsMostExpensive()) {
            log.info("Looking for most expensive products from {} filtered products", products.size());

            if (criteria.isWantsBoth()) {
                // Find most expensive from both categories
                List<GlassDTO> result = new ArrayList<>();

                // Find most expensive eyeglasses
                List<GlassDTO> eyeglasses = products.stream()
                        .filter(glass -> glass.getCategoryId() != null && glass.getCategoryId().equals(EYEGLASSES_CATEGORY_ID))
                        .collect(Collectors.toList());

                if (!eyeglasses.isEmpty()) {
                    GlassDTO mostExpensiveEyeglasses = eyeglasses.stream()
                            .max(Comparator.comparing(GlassDTO::getPrice))
                            .orElse(null);

                    if (mostExpensiveEyeglasses != null) {
                        result.add(mostExpensiveEyeglasses);
                        log.info("Most expensive eyeglasses: {} - ${}",
                                mostExpensiveEyeglasses.getName(), mostExpensiveEyeglasses.getPrice());
                    }
                }

                // Find most expensive sunglasses
                List<GlassDTO> sunglasses = products.stream()
                        .filter(glass -> glass.getCategoryId() != null && glass.getCategoryId().equals(SUNGLASSES_CATEGORY_ID))
                        .collect(Collectors.toList());

                if (!sunglasses.isEmpty()) {
                    GlassDTO mostExpensiveSunglasses = sunglasses.stream()
                            .max(Comparator.comparing(GlassDTO::getPrice))
                            .orElse(null);

                    if (mostExpensiveSunglasses != null) {
                        result.add(mostExpensiveSunglasses);
                        log.info("Most expensive sunglasses: {} - ${}",
                                mostExpensiveSunglasses.getName(), mostExpensiveSunglasses.getPrice());
                    }
                }

                return result;
            } else {
                // Find single most expensive product
                GlassDTO mostExpensive = products.stream()
                        .max(Comparator.comparing(GlassDTO::getPrice))
                        .orElse(null);

                if (mostExpensive != null) {
                    log.info("Most expensive product: {} - ${}", mostExpensive.getName(), mostExpensive.getPrice());
                    return Collections.singletonList(mostExpensive);
                }
            }
        }
        // Handle cheapest search
        else if (criteria.isWantsCheapest()) {
            log.info("Looking for cheapest products from {} filtered products", products.size());

            GlassDTO cheapest = products.stream()
                    .min(Comparator.comparing(GlassDTO::getPrice))
                    .orElse(null);

            if (cheapest != null) {
                log.info("Cheapest product: {} - ${}", cheapest.getName(), cheapest.getPrice());
                return Collections.singletonList(cheapest);
            }
        }

        // Default: return all filtered products
        return products;
    }

    /**
     * Convert GlassDTO list to ProductSuggestion array for the chatbot
     */
    public ProductSuggestion[] convertToSuggestions(List<GlassDTO> products) {
        if (products == null || products.isEmpty()) {
            return new ProductSuggestion[0];
        }

        log.info("Converting {} products to suggestions", products.size());

        // Limit to 5 suggestions maximum
        List<GlassDTO> limitedProducts = products.size() > 5 ? products.subList(0, 5) : products;

        return limitedProducts.stream()
                .map(this::createProductSuggestion)
                .toArray(ProductSuggestion[]::new);
    }

    /**
     * Create a ProductSuggestion from a GlassDTO
     */
    private ProductSuggestion createProductSuggestion(GlassDTO product) {
        String imageUrl = product.getImageFrontUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
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
     * Normalize color string for consistent searching
     */
    private String normalizeColor(String color) {
        if (color == null || color.isEmpty()) {
            return null;
        }

        String lowercaseColor = color.toLowerCase().trim();

        // Basic color normalization
        Map<String, String> colorMap = Map.ofEntries(
                // Green variations
                Map.entry("xanh lá", "green"),
                Map.entry("xanh lá cây", "green"),
                Map.entry("xanh rêu", "green"),
                Map.entry("green", "green"),

                // Blue variations
                Map.entry("xanh dương", "blue"),
                Map.entry("xanh biển", "blue"),
                Map.entry("xanh da trời", "blue"),
                Map.entry("navy", "blue"),
                Map.entry("blue", "blue"),

                // Brown variations
                Map.entry("nâu", "brown"),
                Map.entry("brown", "brown"),
                Map.entry("coffee", "brown"),
                Map.entry("tortoise", "brown"),

                // Black variations
                Map.entry("đen", "black"),
                Map.entry("black", "black"),

                // Silver/gray variations
                Map.entry("bạc", "silver"),
                Map.entry("xám", "silver"),
                Map.entry("silver", "silver"),
                Map.entry("gray", "silver"),
                Map.entry("grey", "silver"),

                // Gold variations
                Map.entry("vàng", "gold"),
                Map.entry("gold", "gold"),

                // Pink variations
                Map.entry("hồng", "pink"),
                Map.entry("pink", "pink"),

                // Special: gold rose
                Map.entry("vàng hồng", "rose gold"),
                Map.entry("rose gold", "rose gold"),
                Map.entry("gold rose", "rose gold")
        );

        // Look for exact matches first
        if (colorMap.containsKey(lowercaseColor)) {
            return colorMap.get(lowercaseColor);
        }

        // Handle the generic "xanh" case (ambiguous in Vietnamese)
        if (lowercaseColor.equals("xanh")) {
            return "blue"; // Default to blue for ambiguous "xanh"
        }

        // If no exact match, look for partial matches
        for (Map.Entry<String, String> entry : colorMap.entrySet()) {
            if (lowercaseColor.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Return original if no mapping found
        return lowercaseColor;
    }

    /**
     * Normalize material string for consistent searching
     */
    private String normalizeMaterial(String material) {
        if (material == null || material.isEmpty()) {
            return null;
        }

        String lowercaseMaterial = material.toLowerCase().trim();

        // Basic material normalization
        Map<String, String> materialMap = Map.ofEntries(
                // Plastic variations
                Map.entry("nhựa", "plastic"),
                Map.entry("plastic", "plastic"),
                Map.entry("acetate", "plastic"),

                // Metal variations
                Map.entry("kim loại", "metal"),
                Map.entry("metal", "metal"),
                Map.entry("titanium", "metal"),

                // Stainless steel variations
                Map.entry("thép không gỉ", "stainless steel"),
                Map.entry("stainless steel", "stainless steel"),
                Map.entry("steel", "stainless steel"),

                // Carbon fiber variations
                Map.entry("carbon fiber", "carbon fiber"),
                Map.entry("sợi carbon", "carbon fiber"),
                Map.entry("carbon", "carbon fiber"),

                // Mixed/composite variations
                Map.entry("hỗn hợp", "mixed"),
                Map.entry("kết hợp", "mixed"),
                Map.entry("mixed", "mixed"),
                Map.entry("composite", "mixed")
        );

        // Look for exact matches first
        if (materialMap.containsKey(lowercaseMaterial)) {
            return materialMap.get(lowercaseMaterial);
        }

        // Handle special case for carbon fiber (contains both words)
        if (lowercaseMaterial.contains("carbon") && lowercaseMaterial.contains("fiber")) {
            return "carbon fiber";
        }

        // If no exact match, look for partial matches
        for (Map.Entry<String, String> entry : materialMap.entrySet()) {
            if (lowercaseMaterial.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Return original if no mapping found
        return lowercaseMaterial;
    }
}