package iuh.fit.se.productservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlassesUpdatedStockResponse {
    private String name;
    private String colorName;
    private Double price;
}
