package iuh.fit.se.chatbotservice.dto;

import iuh.fit.se.chatbotservice.service.ProductIntentAnalyzer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    private boolean wantsEyeglasses = false;
    private boolean wantsSunglasses = false;
    private boolean wantsBoth = false;

    private boolean forMen = false;
    private boolean forWomen = false;

    private String brand;
    private String shape;
    private String material;
    private String color;
    private Double minPrice;
    private Double maxPrice;

    private boolean wantsMostExpensive = false;
    private boolean wantsCheapest = false;

    private Double minWidth;
    private Double maxWidth;
    private Double minHeight;
    private Double maxHeight;
    private Double minBridgeWidth;
    private Double maxBridgeWidth;

    // Convert from ProductIntent for backward compatibility
    public static SearchCriteria fromProductIntent(ProductIntentAnalyzer.ProductIntent intent) {
        return SearchCriteria.builder()
                .wantsEyeglasses(intent.isWantsEyeglasses())
                .wantsSunglasses(intent.isWantsSunglasses())
                .wantsBoth(intent.isWantsBoth())
                .forMen(intent.isForMen())
                .forWomen(intent.isForWomen())
                .brand(intent.getBrand())
                .shape(intent.getShape())
                .material(intent.getMaterial())
                .color(intent.getColor())
                .minPrice(intent.getMinPrice())
                .maxPrice(intent.getMaxPrice())
                .wantsMostExpensive(intent.isWantsMostExpensive())
                .wantsCheapest(intent.isWantsCheapest())
                .build();
    }
}