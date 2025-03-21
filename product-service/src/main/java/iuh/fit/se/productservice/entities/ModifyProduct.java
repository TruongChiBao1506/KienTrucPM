package iuh.fit.se.productservice.entities;

import java.time.LocalDateTime;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "modify_product")
public class ModifyProduct {
	
	@EmbeddedId
	private ModifyProductId modifyProductId;
	
	@MapsId("glass")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "glass_id")
	private Glass glass;
	
	@MapsId("modifiedBy")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "modified_by")
	private User modifiedBy;
	
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date",  updatable = false)
	private Date createdDate;
	
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_date")
	private Date updatedDate;

	public ModifyProduct(ModifyProductId modifyProductId, Glass glass, User modifiedBy, Date createdDate,
			Date updatedDate) {
		super();
		this.modifyProductId = modifyProductId;
		this.glass = glass;
		this.modifiedBy = modifiedBy;
		this.createdDate = createdDate;
		this.updatedDate = updatedDate;
	}



	public ModifyProduct() {
		super();
	}



	public ModifyProductId getModifyProductId() {
		return modifyProductId;
	}

	public void setModifyProductId(ModifyProductId modifyProductId) {
		this.modifyProductId = modifyProductId;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	
	public Glass getGlass() {
		return glass;
	}

	public void setGlass(Glass glass) {
		this.glass = glass;
	}

	public User getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(User modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	@Override
	public String toString() {
		return "ModifyProduct [modifyProductId=" + modifyProductId + ", createdDate=" + createdDate + ", updatedDate="
				+ updatedDate + "]";
	}

	
	
	
	
}
