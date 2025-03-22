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
	@JsonIgnore
	public GlassesDTO(Long id, String name, String brand, Double price, String colorName, String colorCode,
			String imageFrontUrl, String imageSideUrl, boolean gender, int stock, String description,
			Specifications specifications, FrameSize frameSize, Category category) {
		super();
		this.id = id;
		this.name = name;
		this.brand = brand;
		this.price = price;
		this.colorName = colorName;
		this.colorCode = colorCode;
		this.imageFrontUrl = imageFrontUrl;
		this.imageSideUrl = imageSideUrl;
		this.gender = gender;
		this.stock = stock;
		this.description = description;
		this.specifications = specifications;
		this.frameSize = frameSize;
		this.category = category;
	}

//	@JsonIgnore
	@Valid
	private Specifications specifications;
//	@JsonIgnore
	@Valid
	private FrameSize frameSize;
//	@JsonIgnore
	private Category category;
	public GlassesDTO() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public String getColorName() {
		return colorName;
	}
	public void setColorName(String colorName) {
		this.colorName = colorName;
	}
	public String getColorCode() {
		return colorCode;
	}
	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
	}
	public String getImageFrontUrl() {
		return imageFrontUrl;
	}
	public void setImageFrontUrl(String imageFrontUrl) {
		this.imageFrontUrl = imageFrontUrl;
	}
	public String getImageSideUrl() {
		return imageSideUrl;
	}
	public void setImageSideUrl(String imageSideUrl) {
		this.imageSideUrl = imageSideUrl;
	}
	public boolean isGender() {
		return gender;
	}
	public void setGender(boolean gender) {
		this.gender = gender;
	}
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Specifications getSpecifications() {
		return specifications;
	}
	public void setSpecifications(Specifications specifications) {
		this.specifications = specifications;
	}
	public FrameSize getFrameSize() {
		return frameSize;
	}
	public void setFrameSize(FrameSize frameSize) {
		this.frameSize = frameSize;
	}
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "GlassesDTO{" +
				"id=" + id +
				", name='" + name + '\'' +
				", brand='" + brand + '\'' +
				", price=" + price +
				", colorName='" + colorName + '\'' +
				", colorCode='" + colorCode + '\'' +
				", imageFrontUrl='" + imageFrontUrl + '\'' +
				", imageSideUrl='" + imageSideUrl + '\'' +
				", gender=" + gender +
				", stock=" + stock +
				", description='" + description + '\'' +
				", specifications=" + specifications +
				", frameSize=" + frameSize +
				", category=" + category +
				'}';
	}
}
