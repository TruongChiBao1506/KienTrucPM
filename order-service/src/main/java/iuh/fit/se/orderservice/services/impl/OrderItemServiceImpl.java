package iuh.fit.se.orderservice.services.impl;

import iuh.fit.se.orderservice.dtos.OrderItemDTO;
import iuh.fit.se.orderservice.dtos.OrderItemFromProductDTO;
import iuh.fit.se.orderservice.entities.OrderItem;
import iuh.fit.se.orderservice.feign.ProductServiceClient;
import iuh.fit.se.orderservice.repositories.OrderItemRepository;
import iuh.fit.se.orderservice.services.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderItemServiceImpl implements OrderItemService {
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductServiceClient productServiceClient;

    @Override
    public List<OrderItemDTO> findByOrderItemIdOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return orderItems.stream().map(orderItem -> {
            OrderItemFromProductDTO product = productServiceClient.getProductById(orderItem.getProductId());
            OrderItemDTO orderItemDTO = new OrderItemDTO();
            orderItemDTO.setName(product.getName());
            orderItemDTO.setImage_side_url(product.getImage_side_url());
            orderItemDTO.setBrand(product.getBrand());
            orderItemDTO.setColor_name(product.getColor_name());
            orderItemDTO.setColor_code(product.getColor_code());
            orderItemDTO.setQuantity(orderItem.getQuantity());
            orderItemDTO.setUnit_price(orderItem.getUnitPrice());
            orderItemDTO.setTotal_price(orderItem.getTotalPrice());
            return orderItemDTO;
        }).toList();
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }
}
