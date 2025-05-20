package iuh.fit.se.reviewservice.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ReviewId implements Serializable {
    private static final long serialVersionUID = 1L;

    @JoinColumn(name = "user_id")
    private Long userId;

    @JoinColumn(name = "product_id")
    private Long productId;

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
}