package iuh.fit.se.productservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequest {
    private String brands;
    private String shapes;
    private String materials;
    private String colors;
    private String minPrice;
    private String maxPrice;
}