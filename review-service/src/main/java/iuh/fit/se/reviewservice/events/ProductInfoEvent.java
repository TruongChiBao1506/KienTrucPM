package iuh.fit.se.reviewservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoEvent {
    private Long id;
    private String name;
    private String description;
    private Double price;
}