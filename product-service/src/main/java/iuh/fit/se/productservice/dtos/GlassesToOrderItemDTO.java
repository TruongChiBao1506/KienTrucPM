package iuh.fit.se.productservice.dtos;

import jakarta.annotation.security.DenyAll;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlassesToOrderItemDTO {
    private String name;
    private String image_side_url;
    private String brand;
    private String color_name;
    private String color_code;
}
