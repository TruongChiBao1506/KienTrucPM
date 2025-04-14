package iuh.fit.se.productservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlassesDTOForReviewResonponse {
    private Long id;
    private String name;
    private String description;
    private double price;
}
