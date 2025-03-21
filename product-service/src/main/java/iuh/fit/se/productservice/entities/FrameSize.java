package iuh.fit.se.productservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "frame_size")
public class FrameSize {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	@NotNull(message = "Chiều rộng gọng không được để trống")
    @DecimalMin(value = "0.1", inclusive = true, message = "Chiều rộng gọng phải lớn hơn hoặc bằng 0.1")
    private Double frameWidth;

    @NotNull(message = "Chiều rộng kính không được để trống")
    @DecimalMin(value = "0.1", inclusive = true, message = "Chiều rộng kính phải lớn hơn hoặc bằng 0.1")
    private Double lensWidth;

    @NotNull(message = "Chiều dài cầu không được để trống")
    @DecimalMin(value = "0.1", inclusive = true, message = "Chiều dài cầu phải lớn hơn hoặc bằng 0.1")
    private Double bridge;

    @NotNull(message = "Chiều dài gọng không được để trống")
    @DecimalMin(value = "0.1", inclusive = true, message = "Chiều dài gọng phải lớn hơn hoặc bằng 0.1")
    private Double templeLength;

    @NotNull(message = "Chiều cao kính không được để trống")
    @DecimalMin(value = "0.1", inclusive = true, message = "Chiều cao kính phải lớn hơn hoặc bằng 0.1")
    private Double lensHeight;

    @NotNull(message = "Trọng lượng gọng không được để trống")
    @DecimalMin(value = "0.1", inclusive = true, message = "Trọng lượng gọng phải lớn hơn hoặc bằng 0.1")
    private Double frameWeight;
    
    @OneToOne(mappedBy = "frameSize")
    @JsonIgnore
    private Glass glass;

	public FrameSize(Long id, Double frameWidth, Double lensWidth, Double bridge, Double templeLength,
			Double lensHeight, Double frameWeight, Glass glass) {
		super();
		this.id = id;
		this.frameWidth = frameWidth;
		this.lensWidth = lensWidth;
		this.bridge = bridge;
		this.templeLength = templeLength;
		this.lensHeight = lensHeight;
		this.frameWeight = frameWeight;
		this.glass = glass;
	}

	public FrameSize() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getFrameWidth() {
		return frameWidth;
	}

	public void setFrameWidth(Double frameWidth) {
		this.frameWidth = frameWidth;
	}

	public Double getLensWidth() {
		return lensWidth;
	}

	public void setLensWidth(Double lensWidth) {
		this.lensWidth = lensWidth;
	}

	public Double getBridge() {
		return bridge;
	}

	public void setBridge(Double bridge) {
		this.bridge = bridge;
	}

	public Double getTempleLength() {
		return templeLength;
	}

	public void setTempleLength(Double templeLength) {
		this.templeLength = templeLength;
	}

	public Double getLensHeight() {
		return lensHeight;
	}

	public void setLensHeight(Double lensHeight) {
		this.lensHeight = lensHeight;
	}

	public Double getFrameWeight() {
		return frameWeight;
	}

	public void setFrameWeight(Double frameWeight) {
		this.frameWeight = frameWeight;
	}

	public Glass getGlass() {
		return glass;
	}

	public void setGlass(Glass glass) {
		this.glass = glass;
	}

	@Override
	public String toString() {
		return "FrameSize [id=" + id + ", frameWidth=" + frameWidth + ", lensWidth=" + lensWidth + ", bridge=" + bridge
				+ ", templeLength=" + templeLength + ", lensHeight=" + lensHeight + ", frameWeight=" + frameWeight
				+ "]";
	}
    
    
}
