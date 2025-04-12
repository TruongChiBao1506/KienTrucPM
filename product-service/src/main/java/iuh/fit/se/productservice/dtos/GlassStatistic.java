package iuh.fit.se.productservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlassStatistic {
    private Long id;
    private String name;
    private String imageSideUrl;
    private Double price;
    private int totalSold;
}