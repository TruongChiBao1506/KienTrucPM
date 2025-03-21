package iuh.fit.se.productservice.entities;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
public class OrderItemId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    @JoinColumn(name = "order_id")
    private Long order;

    @JoinColumn(name = "product_id")
    private Long product;

    // Constructors
    public OrderItemId() {}
    
    public OrderItemId(Long order, Long product) {
        this.order = order;
        this.product = product;
    }

	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}

	public Long getProduct() {
		return product;
	}

	public void setProduct(Long product) {
		this.product = product;
	}
   
}
