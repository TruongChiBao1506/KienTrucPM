package iuh.fit.se.productservice.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "glasses")
public class Glass {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String brand;
	private Double price;
    private String colorName;
    private String colorCode;
    private String imageFrontUrl;
    private String imageSideUrl;
	private boolean gender;
	private int stock;
	private String description;
	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "specifications_id")
//	@JsonIgnore
	private Specifications specifications;
	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "frame_size_id")
//	@JsonIgnore
	private FrameSize frameSize;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id")
//	@JsonIgnore
	private Category category;
	



	public Glass(Long id, String name, String brand, Double price, String colorName, String colorCode,
			String imageFrontUrl, String imageSideUrl, boolean gender, int stock, String description) {
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
	}



	public Glass() {
		super();
	}
	
	

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Glass))
			return false;
		Glass other = (Glass) obj;
		return Objects.equals(id, other.id);
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

	public boolean isGender() {
		return gender;
	}

	public void setGender(boolean gender) {
		this.gender = gender;
	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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



	@Override
	public String toString() {
		return "Glass [id=" + id + ", name=" + name + ", brand=" + brand + ", price=" + price + ", colorName="
				+ colorName + ", colorCode=" + colorCode + ", imageFrontUrl=" + imageFrontUrl + ", imageSideUrl="
				+ imageSideUrl + ", gender=" + gender + ", stock=" + stock + ", description=" + description + "]";
	}



	

	


	
}
