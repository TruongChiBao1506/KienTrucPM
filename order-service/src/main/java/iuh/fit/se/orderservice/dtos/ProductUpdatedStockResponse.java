package iuh.fit.se.orderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdatedStockResponse {
    private String name;
    private String colorName;
    private Double price;
}
