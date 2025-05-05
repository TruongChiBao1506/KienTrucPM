package iuh.fit.se.chatbotservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlassDTO {
    private Long id;
    private String name;
    private String brand;
    private Double price;
    private String colorName;
    private String colorCode;
    private String imageFrontUrl;
    private String imageSideUrl;
    private boolean gender;  // true: nam, false: nữ
    private int stock;
    private String description;

    // Specifications
    private String shape;      // Hình dạng gọng kính
    private String material;   // Chất liệu

    // Frame Size
    private Double frameWidth;
    private Double templeLength;
    private Double lensHeight;
    private Double lensWidth;
    private Double bridgeWidth;

    // Category
    private Long categoryId;
    private String categoryName;
}