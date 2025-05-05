package iuh.fit.se.chatbotservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSuggestion {
    private String productId;
    private String name;
    private String imageUrl;
    private Double price;
    private String category;
    private String detailUrl;  // Thêm trường mới cho URL chi tiết sản phẩm
}