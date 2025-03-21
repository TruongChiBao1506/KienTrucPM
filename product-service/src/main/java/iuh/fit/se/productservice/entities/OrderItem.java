package iuh.fit.se.productservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {
	@EmbeddedId
    private OrderItemId orderItemId;
	
	@MapsId("order")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "order_id")
	@JsonIgnore
	private Order order;
	
	@MapsId("product")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id")
	private Glass product;
	
	private int quantity;
	private Double unitPrice;
	private Double totalPrice;
	
	public OrderItem(OrderItemId orderItemId, Order order, Glass product, int quantity, Double unitPrice,
			Double totalPrice) {
		super();
		this.orderItemId = orderItemId;
		this.order = order;
		this.product = product;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.totalPrice = totalPrice;
	}


	public OrderItem() {
		super();
	}
	
	
	public OrderItemId getOrderItemId() {
		return orderItemId;
	}
	public void setOrderItemId(OrderItemId orderItemId) {
		this.orderItemId = orderItemId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public Double getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}
	public Double getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}
	
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}


	public Glass getProduct() {
		return product;
	}


	public void setProduct(Glass product) {
		this.product = product;
	}


	@Override
	public String toString() {
		return "OrderItem [orderItemId=" + orderItemId + ", quantity=" + quantity + ", unitPrice=" + unitPrice
				+ ", totalPrice=" + totalPrice + "]";
	}
	
	
	
}
