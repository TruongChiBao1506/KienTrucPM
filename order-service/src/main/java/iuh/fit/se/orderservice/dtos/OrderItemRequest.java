package iuh.fit.se.orderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {
    private Long productId;
    private int quantity;
    private Double price;
    private String colorName;
    private String colorCode;
}
