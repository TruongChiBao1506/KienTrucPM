package iuh.fit.se.orderservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.orderservice.dtos.*;
import iuh.fit.se.orderservice.entities.Order;
import iuh.fit.se.orderservice.events.publisher.OrderEventPublisher;
import iuh.fit.se.orderservice.feign.AuthServiceClient;
import iuh.fit.se.orderservice.feign.ProductServiceClient;
import iuh.fit.se.orderservice.feign.UserServiceClient;
import iuh.fit.se.orderservice.services.OrderItemService;
import iuh.fit.se.orderservice.services.OrderService;
import iuh.fit.se.orderservice.services.VNPayService;
import iuh.fit.se.orderservice.utils.VNPayUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

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
    private VNPayService vnpayService;

    @Autowired
    private  ProductServiceClient productServiceClient;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${vnpay.hash_secret}")
    private String VNP_HASH_SECRET;
    @Value("${vnpay.url}")
    private String VNP_URL;

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody OrderRequest orderRequest) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        try {
            if(orderRequest.getPaymentMethod().equalsIgnoreCase("VNPAY")) {
                String vnpUrl = vnpayService.createVNPayUrlAndCreateOrder(orderRequest);
                response.put("status", HttpStatus.OK.value());
                response.put("message", "Redirect to VNPAY");
                response.put("paymenUrl", vnpUrl);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }else {
                orderService.createOrder(orderRequest);
                response.put("status", HttpStatus.OK.value());
                response.put("message", "Order created successfully");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        } catch (Exception e) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<Map<String, Object>> handleVNPayCallback(@RequestParam Map<String, String> allParams, HttpServletResponse responseServlet) throws IOException {
        Map<String, Object> responseRequest = new LinkedHashMap<String, Object>();
        String txnRef = allParams.get("vnp_TxnRef"); // Lấy mã giao dịch
        String responseCode = allParams.get("vnp_ResponseCode");
        // Kiểm tra chữ ký
        String secureHash = allParams.remove("vnp_SecureHash");
        String calculatedHash = VNPayUtils.buildQuery(allParams, VNP_HASH_SECRET, VNP_URL);
        Map<String, String> params = extractParams(calculatedHash);
        String hash = params.get("vnp_SecureHash");
        System.out.println("Secure Hash: " + secureHash);
        System.out.println("Calculated Hash: " + hash);
        if (!secureHash.equals(hash)) {
            System.out.println("Invalid signature!");
            responseRequest.put("status", HttpStatus.BAD_REQUEST.value());
            responseRequest.put("message", "Invalid signature!");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseRequest);

//            responseServlet.sendRedirect("http://localhost:8081/payment-result?status=failed&txnRef=" + txnRef);
        }
        // Tìm đơn hàng dựa vào txnRef
        Order order = orderService.findByOrderNumber(txnRef);
        StringBuilder productTable = new StringBuilder();
        if ("00".equals(responseCode)) {
            // Thanh toán thành công, cập nhật trạng thái đơn hàng
            order.setStatus("PAID");
            orderService.save(order);


            order.getOrderItems().forEach(item -> {
                ResponseEntity<Map<String, Object>> responseProducts = productServiceClient.updateStockProduct(item.getProductId(), item.getQuantity());
                if (responseProducts.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Order saved successfully");
                    ProductUpdatedStockResponse response = objectMapper.convertValue(responseProducts.getBody().get("data"), ProductUpdatedStockResponse.class);
                    productTable.append(String.format("""
                                        <tr>
                                            <td style="padding: 10px; border-top: 1px solid #ddd;">
                                                <div style="display: flex; align-items: center;">
                                                    <div>
                                                        <div style="font-weight: bold;">%s</div>
                                                    </div>
                                                </div>
                                            </td>
                                            <td style="padding: 10px; border-top: 1px solid #ddd; text-align: right;">%s</td>
                                            <td style="padding: 10px; border-top: 1px solid #ddd; text-align: right;">%d</td>
                                            <td style="padding: 10px; border-top: 1px solid #ddd; text-align: right;">%s</td>
                                        </tr>
                                    """,
                            response.getName(),
                            response.getColorName(),
                            item.getQuantity(),
                            formatCurrency(response.getPrice())
                    ));
                } else {
                    throw new RuntimeException("Failed to update stock for product ID: " + item.getProductId());
                }
            });
            Notification notification = new Notification();
            notification.setOrderId(order.getId());
            notification.setMessage("New Order: #" + order.getOrderNumber());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);
            orderEventPublisher.sendNotification(notification);
            ResponseEntity<Map<String, Object>> responseAuth = authServiceClient.getAuthUserEmailById(order.getUserId());
            String email = (String) responseAuth.getBody().get("data");
            orderEventPublisher.sendEmail(order, email, productTable);
            responseRequest.put("status", HttpStatus.OK.value());
            responseRequest.put("message", "Payment successful!");
            return ResponseEntity.ok(responseRequest);
        }else{
            order.setStatus("FAILED");
            orderService.save(order);
            Notification notification = new Notification();
            notification.setOrderId(order.getId());
            notification.setMessage("New Order: #" + order.getOrderNumber());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);
            orderEventPublisher.sendNotification(notification);
//            responseServlet.sendRedirect("http://localhost:8081/payment-result?status=failed&txnRef=" + txnRef);
            responseRequest.put("status", HttpStatus.BAD_REQUEST.value());
            responseRequest.put("message", "Payment failed!");
            return ResponseEntity.ok(responseRequest);
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
    public static Map<String, String> extractParams(String url) {
        Map<String, String> params = new HashMap<>();
        try {
            // Tách phần query string (phần sau dấu ?)
            String queryString = url.split("\\?")[1];

            // Phân tách các cặp key=value
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : "";
                params.put(key, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi trích xuất tham số từ URL", e);
        }
        return params;
    }
    public String formatCurrency(double amount) {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return currencyFormat.format(amount) + "đ";
    }
}
