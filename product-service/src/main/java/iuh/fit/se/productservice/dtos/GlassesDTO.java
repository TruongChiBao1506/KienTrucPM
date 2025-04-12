package iuh.fit.se.productservice.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import iuh.fit.se.productservice.entities.Category;
import iuh.fit.se.productservice.entities.FrameSize;
import iuh.fit.se.productservice.entities.Specifications;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GlassesDTO {
	private Long id;
	@NotBlank(message = "Tên sản phẩm không được để trống")
	@Size(max = 100, message = "Tên sản phẩm không được vượt quá 100 ký tự")
	private String name;
	@NotBlank(message = "Thương hiệu không được để trống")
	@Size(max = 50, message = "Thương hiệu không được vượt quá 50 ký tự")
	private String brand;
	@NotNull(message = "Giá sản phẩm không được để trống")
	@DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
	private Double price;
    @NotBlank(message = "Tên màu không được để trống")
    private String colorName;
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Mã màu không hợp lệ (phải ở dạng HEX, ví dụ: #FFFFFF)")
    private String colorCode;
    @NotBlank(message = "Ảnh mặt trước không được để trống")
    @Pattern(regexp = ".*\\.(png|jpg|jpeg|avif)$", message = "Ảnh mặt trước chỉ được phép có định dạng png, jpg, hoặc jpeg")
    private String imageFrontUrl;
    @NotBlank(message = "Ảnh mặt bên không được để trống")
    @Pattern(regexp = ".*\\.(png|jpg|jpeg|avif)$", message = "Ảnh mặt bên chỉ được phép có định dạng png, jpg, hoặc jpeg")
    private String imageSideUrl;
    
	private boolean gender;
    @Min(value = 0, message = "Số lượng trong kho phải lớn hơn hoặc bằng 0")
	private int stock;
    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
	private String description;
	//	@JsonIgnore
	@Valid
	private Specifications specifications;
	//	@JsonIgnore
	@Valid
	private FrameSize frameSize;
	//	@JsonIgnore
	private Category category;

}
