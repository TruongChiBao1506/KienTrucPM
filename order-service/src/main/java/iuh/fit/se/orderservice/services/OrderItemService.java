package iuh.fit.se.orderservice.services;

import iuh.fit.se.orderservice.dtos.OrderItemDTO;
import iuh.fit.se.orderservice.entities.OrderItem;

import java.util.List;

public interface OrderItemService {
    public List<OrderItemDTO> findByOrderItemIdOrderId(Long orderId);
    public OrderItem save(OrderItem orderItem);

}
