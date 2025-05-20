package iuh.fit.se.productservice.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NotNull
public class GlassDTO {
	private Long id;
	private String imageSideUrl;
	private String imageFrontUrl;
	private String colorCode;
	private String name;
	private String brand;
	private Double price;
}
