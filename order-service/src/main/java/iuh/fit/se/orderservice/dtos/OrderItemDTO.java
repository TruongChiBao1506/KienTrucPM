package iuh.fit.se.orderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private String name;
    private String image_side_url;
    private String brand;
    private String color_name;
    private String color_code;
    private int quantity;
    private Double unit_price;
    private Double total_price;
}
