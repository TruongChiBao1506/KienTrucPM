package iuh.fit.se.orderservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import iuh.fit.se.orderservice.entities.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemUserDTO {
    private Long orderItemId;
    private OrderItemFromProductDTO product;
    private int quantity;
    private Double unitPrice;
    private Double totalPrice;
}
