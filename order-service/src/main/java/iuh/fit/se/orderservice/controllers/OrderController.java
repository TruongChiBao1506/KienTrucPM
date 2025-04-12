package iuh.fit.se.orderservice.controllers;

import iuh.fit.se.orderservice.dtos.OrderDataStatistic;
import iuh.fit.se.orderservice.dtos.OrderRequest;
import iuh.fit.se.orderservice.dtos.OrderStatistic;
import iuh.fit.se.orderservice.entities.Order;
import iuh.fit.se.orderservice.feign.ProductServiceClient;
import iuh.fit.se.orderservice.feign.UserServiceClient;
import iuh.fit.se.orderservice.services.OrderItemService;
import iuh.fit.se.orderservice.services.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private  OrderService orderService;
    @Autowired
    private  OrderItemService orderItemService;
    @Autowired
    private  UserServiceClient userServiceClient;
    @Autowired
    private  ProductServiceClient productServiceClient;


//    @GetMapping("/{id}")
//    public Order getOrder(@PathVariable Long id) {
//        return orderService.findById(id);
//    }
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody OrderRequest orderRequest) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            orderService.createOrder(orderRequest);
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Order created successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        List<Order> orders = orderService.findAll();
        orders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        response.put("status", HttpStatus.OK.value());
        response.put("data", orders);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/order")
    public ResponseEntity<Map<String, Object>> getOrderItemByOrderId(@RequestParam Long id) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", orderItemService.findByOrderItemIdOrderId(id));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findOrderById(@PathVariable Long id) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", orderService.findById(id));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrderById(@PathVariable Long id) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", orderService.deleteById(id));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/update-status/{id}")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long id,
                                                                 @RequestParam String status) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        System.out.println(id + " | " + status);
        Order order = orderService.findById(id);
        order.setStatus(status);
        orderService.save(order);
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Order status updated successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterOrders(@RequestParam(value = "keyword", required = false) String keyword,
                                                            @RequestParam(value = "status", required = false) String status,
                                                            @RequestParam(value = "sort", required = false) String sort) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", orderService.filterOrders(keyword, status, sort));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/orders-statistic")
    public ResponseEntity<Map<String, Object>> getOrderStatistic() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        OrderDataStatistic data = new OrderDataStatistic();
        List<OrderStatistic> purchasedOrder = orderService.getPurchasedOrder();
        List<OrderStatistic> salesOrder = orderService.getSalesOrder();
        Integer[] monthlyPurchase = new Integer[12];
        Arrays.fill(monthlyPurchase, 0);
        Integer[] monthlySales = new Integer[12];
        Arrays.fill(monthlySales, 0);
        for (OrderStatistic order : purchasedOrder) {
            monthlyPurchase[order.getMonth() - 1] = order.getCount();
        }
        for (OrderStatistic order : salesOrder) {
            monthlySales[order.getMonth() - 1] = order.getCount();
        }
        data.setPurchasedOrder(Arrays.asList(monthlyPurchase));
        data.setSalesOrder(Arrays.asList(monthlySales));
        data.setMonth(List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));

        response.put("status", HttpStatus.OK.value());
        response.put("data", data);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyRevenue(@RequestParam int year) {
        return ResponseEntity.ok(orderService.getMonthlyRevenue(year));
    }
    // API thống kê doanh thu theo ngày trong tháng
    @GetMapping("/revenue/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyRevenueInMonth(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(orderService.getDailyRevenueInMonth(year, month));
    }
    @GetMapping("/status-percentage/monthly")
    public ResponseEntity<Map<String, Double>> getStatusPercentageByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(orderService.getStatusPercentageByMonth(year, month));
    }

    @GetMapping("/status-percentage/yearly")
    public ResponseEntity<Map<String, Double>> getStatusPercentageByYear(@RequestParam int year) {
        return ResponseEntity.ok(orderService.getStatusPercentageByYear(year));
    }
    @GetMapping("/list")
    public ResponseEntity<List<Order>> findByYearAndMonth(@RequestParam int year, @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(orderService.getOrdersByYearAndMonth(year, month));
    }
    @GetMapping("/orders-export")
    public void exportOrders(@RequestParam int year, @RequestParam(required = false) Integer month, HttpServletResponse response) throws IOException {
        System.out.println("Exporting orders");
        List<Order> orders = orderService.getOrdersByYearAndMonth(year, month);
        orderService.exportOrderData(orders, response);
    }
    @GetMapping("/orders-history")
    public ResponseEntity<Map<String, Object>> getOrderByUserName(@RequestParam Long userId) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", orderService.findOrdersByUserId(userId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
