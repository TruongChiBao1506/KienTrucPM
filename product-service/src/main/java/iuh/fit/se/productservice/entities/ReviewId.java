package iuh.fit.se.productservice.entities;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;

@Embeddable
public class ReviewId implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JoinColumn(name = "user_id")
	private Long userId;
	
	@JoinColumn(name = "product_id")
	private Long productId;

	public ReviewId(Long userId, Long productId) {
		super();
		this.userId = userId;
		this.productId = productId;
	}

	public ReviewId() {
		super();
	}

	
	
	@Override
	public int hashCode() {
		return Objects.hash(productId, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ReviewId))
			return false;
		ReviewId other = (ReviewId) obj;
		return Objects.equals(productId, other.productId) && Objects.equals(userId, other.userId);
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}
	
	
}
