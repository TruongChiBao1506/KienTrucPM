package iuh.fit.se.productservice.dtos;

public class GlassDTO {
	private Long id;
	private String imageSideUrl;
	private String imageFrontUrl;
	private String colorCode;
	private String name;
	private String brand;
	private Double price;
	public GlassDTO(Long id, String imageSideUrl, String imageFrontUrl, String colorCode, String name, String brand,
			Double price) {
		super();
		this.id = id;
		this.imageSideUrl = imageSideUrl;
		this.imageFrontUrl = imageFrontUrl;
		this.colorCode = colorCode;
		this.name = name;
		this.brand = brand;
		this.price = price;
	}
	public GlassDTO() {
		super();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getImageSideUrl() {
		return imageSideUrl;
	}
	public void setImageSideUrl(String imageSideUrl) {
		this.imageSideUrl = imageSideUrl;
	}
	public String getImageFrontUrl() {
		return imageFrontUrl;
	}
	public void setImageFrontUrl(String imageFrontUrl) {
		this.imageFrontUrl = imageFrontUrl;
	}
	public String getColorCode() {
		return colorCode;
	}
	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
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
	@Override
	public String toString() {
		return "GlassDTO [id=" + id + ", imageSideUrl=" + imageSideUrl + ", imageFrontUrl=" + imageFrontUrl
				+ ", colorCode=" + colorCode + ", name=" + name + ", brand=" + brand + ", price=" + price + "]";
	}
	
	
}
