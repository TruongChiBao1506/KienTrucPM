package iuh.fit.se.productservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "specifications")
public class Specifications {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotBlank(message = "Khoảng PD không được để trống")
	@Pattern(regexp = "^[0-9]+(-[0-9]+)?$", message = "Khoảng PD phải ở dạng số hoặc khoảng số, ví dụ: 60-70")
	private String pdRange;

	@NotBlank(message = "Khoảng độ kính không được để trống")
	@Pattern(
		    regexp = "^[+-]?\\d{1,2}(\\.\\d{2})?\\s*~\\s*[+-]?\\d{1,2}(\\.\\d{2})?$",
		    message = "Khoảng độ kính phải ở dạng số, ví dụ: -20.00 ~ +12.00"
		)	private String prescriptionRange;

	@NotNull(message = "Trường có sẵn kính đa tròng không được để trống")
	private Boolean availableAsProgressiveBifocal;

	@NotNull(message = "Trường kính đọc sách không được để trống")
	private Boolean readers;

	@NotBlank(message = "Mô tả kích thước gọng không được để trống")
	@Size(max = 200, message = "Mô tả kích thước gọng không được vượt quá 200 ký tự")
	private String frameSizeDescription;

	@NotBlank(message = "Loại gọng không được để trống")
	@Size(max = 50, message = "Loại gọng không được vượt quá 50 ký tự")
	private String rim;

	@NotBlank(message = "Kiểu dáng không được để trống")
	@Size(max = 50, message = "Kiểu dáng không được vượt quá 50 ký tự")
	private String shape;

	@NotBlank(message = "Chất liệu không được để trống")
	@Size(max = 50, message = "Chất liệu không được vượt quá 50 ký tự")
	private String material;

	@NotBlank(message = "Tính năng không được để trống")
	@Size(max = 100, message = "Tính năng không được vượt quá 100 ký tự")
	private String feature;

	@OneToOne(mappedBy = "specifications")
	@JsonIgnore
	private Glass glass;

	public Specifications(Long id, String pdRange, String prescriptionRange, Boolean availableAsProgressiveBifocal,
			Boolean readers, String frameSizeDescription, String rim, String shape, String material, String feature,
			Glass glass) {
		super();
		this.id = id;
		this.pdRange = pdRange;
		this.prescriptionRange = prescriptionRange;
		this.availableAsProgressiveBifocal = availableAsProgressiveBifocal;
		this.readers = readers;
		this.frameSizeDescription = frameSizeDescription;
		this.rim = rim;
		this.shape = shape;
		this.material = material;
		this.feature = feature;
		this.glass = glass;
	}

	public Specifications() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPdRange() {
		return pdRange;
	}

	public void setPdRange(String pdRange) {
		this.pdRange = pdRange;
	}

	public String getPrescriptionRange() {
		return prescriptionRange;
	}

	public void setPrescriptionRange(String prescriptionRange) {
		this.prescriptionRange = prescriptionRange;
	}

	public Boolean getAvailableAsProgressiveBifocal() {
		return availableAsProgressiveBifocal;
	}

	public void setAvailableAsProgressiveBifocal(Boolean availableAsProgressiveBifocal) {
		this.availableAsProgressiveBifocal = availableAsProgressiveBifocal;
	}

	public Boolean getReaders() {
		return readers;
	}

	public void setReaders(Boolean readers) {
		this.readers = readers;
	}

	public String getFrameSizeDescription() {
		return frameSizeDescription;
	}

	public void setFrameSizeDescription(String frameSizeDescription) {
		this.frameSizeDescription = frameSizeDescription;
	}

	public String getRim() {
		return rim;
	}

	public void setRim(String rim) {
		this.rim = rim;
	}

	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}

	public Glass getGlass() {
		return glass;
	}

	public void setGlass(Glass glass) {
		this.glass = glass;
	}

	@Override
	public String toString() {
		return "Specifications [id=" + id + ", pdRange=" + pdRange + ", prescriptionRange=" + prescriptionRange
				+ ", availableAsProgressiveBifocal=" + availableAsProgressiveBifocal + ", readers=" + readers
				+ ", frameSizeDescription=" + frameSizeDescription + ", rim=" + rim + ", shape=" + shape + ", material="
				+ material + ", feature=" + feature + "]";
	}

}
