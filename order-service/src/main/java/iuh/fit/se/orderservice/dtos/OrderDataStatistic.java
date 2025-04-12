package iuh.fit.se.orderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDataStatistic {
    private List<Integer> purchasedOrder;
    private List<Integer> salesOrder;
    private List<String> month;

}
