package iuh.fit.se.productservice.entities;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews")
public class Review {

	@EmbeddedId
	private ReviewId reviewId;
	
	@MapsId("userId")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	private User user;
	
	@MapsId("productId")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id")
	private Glass product;
	
	private String content;
	
	private int rating;
	
	private LocalDateTime createdAt;

	public Review(ReviewId reviewId, User user, Glass product, String content, int rating, LocalDateTime createdAt) {
		super();
		this.reviewId = reviewId;
		this.user = user;
		this.product = product;
		this.content = content;
		this.rating = rating;
		this.createdAt = createdAt;
	}

	public Review() {
		super();
	}

	public ReviewId getReviewId() {
		return reviewId;
	}

	public void setReviewId(ReviewId reviewId) {
		this.reviewId = reviewId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Glass getProduct() {
		return product;
	}

	public void setProduct(Glass product) {
		this.product = product;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "Review [reviewId=" + reviewId + ", content=" + content + ", rating=" + rating + ", createdAt="
				+ createdAt + "]";
	}
	
	
}
