package iuh.fit.se.orderservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.orderservice.dtos.*;
import iuh.fit.se.orderservice.entities.Order;
import iuh.fit.se.orderservice.entities.OrderItem;
import iuh.fit.se.orderservice.events.publisher.OrderEventPublisher;
import iuh.fit.se.orderservice.feign.AuthServiceClient;
import iuh.fit.se.orderservice.feign.NotificationServiceClient;
import iuh.fit.se.orderservice.feign.ProductServiceClient;
import iuh.fit.se.orderservice.feign.UserServiceClient;
import iuh.fit.se.orderservice.repositories.OrderItemRepository;
import iuh.fit.se.orderservice.repositories.OrderRepository;
import iuh.fit.se.orderservice.services.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private NotificationServiceClient notificationServiceClient;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void createOrder(@RequestBody OrderRequest orderRequest) {
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setOrderNumber(createOrderNumber());
        order.setOrderDate(orderRequest.getOrderDate());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setTotalAmount(calculateTotal(orderRequest.getOrderItems()));
        order.setStatus(orderRequest.getStatus());

        Order savedOrder = orderRepository.save(order);
        StringBuilder productTable = new StringBuilder();
        for (OrderItemRequest item : orderRequest.getOrderItems()){
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(item.getPrice());
            orderItem.setTotalPrice(item.getQuantity() * item.getPrice());
            System.out.println("Gọi đến service sản phẩm");
            ResponseEntity<Map<String, Object>> responseProducts = productServiceClient.updateStockProduct(item.getProductId(), item.getQuantity());
            if(responseProducts.getStatusCode().is2xxSuccessful()){
                orderItemRepository.save(orderItem);
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
                        item.getColorName(),
                        item.getQuantity(),
                        formatCurrency(item.getPrice())
                ));
            } else {
                throw new RuntimeException("Failed to update stock for product ID: " + item.getProductId());
            }
        }
        Notification notification = new Notification();
        notification.setOrderId(savedOrder.getId());
        notification.setMessage("New Order: #" + order.getOrderNumber());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        orderEventPublisher.sendNotification(notification);
//        ResponseEntity<Map<String, Object>> responseNotification = notificationServiceClient.saveNotification(notification);
//        if (responseNotification.getStatusCode().is2xxSuccessful()){
//            System.out.println("Notification sent successfully");
//        } else {
//            throw new RuntimeException("Failed to send notification");
//        }
        ResponseEntity<Map<String, Object>> responseAuth = authServiceClient.getAuthUserEmailById(savedOrder.getUserId());
        if (responseAuth.getStatusCode().is2xxSuccessful()){
            String email = (String) responseAuth.getBody().get("data");
            orderEventPublisher.sendEmail(savedOrder, email, productTable);
        }
        else {
            throw new RuntimeException("Failed to send email");
        }
    }
    public String formatCurrency(double amount) {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return currencyFormat.format(amount) + "đ";
    }
    private String createOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        String number = now.getYear() + "" + now.getMonthValue() + "" + now.getDayOfMonth() + "" + now.getHour() + "" + now.getMinute() + "" + now.getSecond();
        return number;
    }
    private Double calculateTotal(List<OrderItemRequest> items) {
        return items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();
    }

    @Override
    public Order findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public boolean deleteById(Long id) {
        if(this.findById(id)!= null) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public List<Order> filterOrders(String keyword, String status, String sort) {
        Specification<Order> spec = Specification.where(null);

        // Thêm điều kiện tìm kiếm theo keyword nếu có
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(containsKeyword(keyword));
        }

        // Thêm điều kiện lọc theo trạng thái nếu có
        if (status != null && !status.isEmpty()) {
            spec = spec.and(hasStatus(status));
        }

        // Xác định thứ tự sắp xếp
        Sort sortOption = Sort.unsorted();
        if ("asc".equals(sort)) {
            sortOption = Sort.by("totalAmount").ascending(); // Sắp xếp giá tăng dần
        } else if ("desc".equals(sort)) {
            sortOption = Sort.by("totalAmount").descending(); // Sắp xếp giá giảm dần
        }

        // Trả về danh sách kết quả sau khi lọc
        return orderRepository.findAll(spec, sortOption);
    }
    // Tìm kiếm theo từ khóa trong các trường cụ thể
    public static Specification<Order> containsKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("id").as(String.class)), likePattern), // Tìm theo ID
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("orderNumber")), likePattern),         // Tìm theo Order Number
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("user").get("fullname")), likePattern),  // Tìm theo tên User
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("shippingAddress")), likePattern),    // Tìm theo địa chỉ giao hàng
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("paymentMethod")), likePattern)       // Tìm theo phương thức thanh toán
            );
        };
    }
    // Lọc theo trạng thái
    public static Specification<Order> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    @Override
    public List<OrderStatistic> getPurchasedOrder() {
        List<Object[]> list = orderRepository.getPurchasedOrder();
        return list.stream()
                .map(item -> new OrderStatistic(
                        (Integer) item[0], // MONTH(order_date) là Integer
                        ((Long) item[1]).intValue() // COUNT(*) là Long, chuyển thành Integer
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderStatistic> getSalesOrder() {
        List<Object[]> list = orderRepository.getSalesOrder();
        return list.stream()
                .map(item -> new OrderStatistic(
                        (Integer) item[0], // MONTH(order_date) là Integer
                        ((Long) item[1]).intValue() // COUNT(*) là Long, chuyển thành Integer
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getMonthlyRevenue(int year) {
        List<Object[]> results = orderRepository.findMonthlyRevenue(year);
        Map<Integer, Double> revenueMap = new HashMap<>();

        // Gán dữ liệu query vào map (month -> revenue)
        for (Object[] result : results) {
            int month = (int) result[0];
            double totalRevenue = (double) result[2];
            revenueMap.put(month, totalRevenue);
        }

        // Tạo danh sách đầy đủ 12 tháng
        List<Map<String, Object>> monthlyRevenue = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("month", i);
            data.put("year", year);
            data.put("totalRevenue", revenueMap.getOrDefault(i, 0.0)); // Doanh thu = 0 nếu không có
            monthlyRevenue.add(data);
        }

        return monthlyRevenue;
    }

    @Override
    public List<Map<String, Object>> getDailyRevenueInMonth(int year, int month) {
        List<Object[]> results = orderRepository.findDailyRevenueInMonth(year, month);
        Map<Integer, Double> revenueMap = new HashMap<>();

        // Gán dữ liệu query vào map (day -> revenue)
        for (Object[] result : results) {
            int day = (int) result[0];
            double totalRevenue = (double) result[1];
            revenueMap.put(day, totalRevenue);
        }

        // Xác định số ngày trong tháng
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        List<Map<String, Object>> dailyRevenue = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("day", i);
            data.put("totalRevenue", revenueMap.getOrDefault(i, 0.0)); // Doanh thu = 0 nếu không có
            dailyRevenue.add(data);
        }

        return dailyRevenue;
    }

    @Override
    public Map<String, Double> getStatusPercentageByMonth(int year, int month) {
        List<Object[]> results = orderRepository.countOrdersByStatusInMonth(year, month);
        return calculatePercentage(results);
    }

    @Override
    public Map<String, Double> getStatusPercentageByYear(int year) {
        List<Object[]> results = orderRepository.countOrdersByStatusInYear(year);
        return calculatePercentage(results);
    }
    private Map<String, Double> calculatePercentage(List<Object[]> results) {
        Map<String, Long> statusCountMap = new HashMap<>();
        long total = 0;

        for (Object[] result : results) {
            String status = (String) result[0];
            Long count = (Long) result[1];
            statusCountMap.put(status, count);
            total += count;
        }

        Map<String, Double> percentageMap = new HashMap<>();
        for (Map.Entry<String, Long> entry : statusCountMap.entrySet()) {
            String status = entry.getKey();
            Long count = entry.getValue();
            percentageMap.put(status, (double) count / total * 100);
        }

        return percentageMap;
    }
    @Override
    public List<Order> getOrdersByYearAndMonth(int year, Integer month) {
        List<Order> orders;
        if (month != null) {
            orders = orderRepository.findByYearAndMonth(year, month);
        } else {
            orders = orderRepository.findByYear(year);
        }
        return orders;
    }

    @Override
    public void exportOrderData(List<Order> orders, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Orders");

        // Tạo tiêu đề cho file Excel
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Order Report");

        // Định dạng tiêu đề
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16); // Kích thước chữ lớn hơn
        titleStyle.setAlignment(HorizontalAlignment.CENTER); // Căn giữa ngang
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa dọc
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        // Hợp nhất các cột cho tiêu đề
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        int currentRowIndex = 2;

        // Tạo Header
        Row headerRow = sheet.createRow(currentRowIndex++);
        String[] headers = {"Order Number", "Customer", "Order Date", "Shipping Address", "Payment Method", "Status", "Total Amount"};
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Điền dữ liệu
        double totalRevenue = 0;
        for (Order order : orders) {
            Row row = sheet.createRow(currentRowIndex++);

            row.createCell(0).setCellValue(order.getOrderNumber());
            row.createCell(1).setCellValue(order.getUserId()); // User giả định có trường "name"
            row.createCell(2).setCellValue(order.getOrderDate().toString());
            row.createCell(3).setCellValue(order.getShippingAddress());
            row.createCell(4).setCellValue(order.getPaymentMethod());
            row.createCell(5).setCellValue(order.getStatus());
            row.createCell(6).setCellValue(order.getTotalAmount());

            // Tính tổng doanh thu
            totalRevenue += order.getTotalAmount();
        }
    }

    @Override
    public List<Order> findOrdersByUserId(Long userId) {
        return orderRepository.findOrdersByUserId(userId);
    }


}
