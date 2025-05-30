package iuh.fit.se.orderservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import iuh.fit.se.orderservice.dtos.*;
import iuh.fit.se.orderservice.entities.Order;
import iuh.fit.se.orderservice.entities.OrderItem;
import iuh.fit.se.orderservice.events.publisher.OrderEventPublisher;
import iuh.fit.se.orderservice.feign.*;
import iuh.fit.se.orderservice.repositories.OrderItemRepository;
import iuh.fit.se.orderservice.repositories.OrderRepository;
import iuh.fit.se.orderservice.services.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CartServiceClient cartServiceClient;


    @Override
    @Transactional
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
        cartServiceClient.clearCart(orderRequest.getUserId());
        orderEventPublisher.sendNotification(notification);
        messagingTemplate.convertAndSend("/topic/orders", notification);
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
    public List<OrderDTO> findAll() {
        List<Order> orders = orderRepository.findAll();
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO();
            // Lấy thông tin người dùng từ UserServiceClient
            ResponseEntity<Map<String, Object>> response = userServiceClient.getUserProfileById(order.getUserId());
            if (response.getStatusCode().is2xxSuccessful()) {
                List<OrderItemUserDTO> listOrderItem = new ArrayList<>();
                orderDTO.setOrderNumber(order.getOrderNumber());
                orderDTO.setOrderDate(order.getOrderDate());
                orderDTO.setId(order.getId());
                orderDTO.setStatus(order.getStatus());
                orderDTO.setPaymentMethod(order.getPaymentMethod());
                orderDTO.setShippingAddress(order.getShippingAddress());
                orderDTO.setTotalAmount(order.getTotalAmount());
                orderDTO.setUser(objectMapper.convertValue(response.getBody().get("data"), UserDTO.class));
                String email = (String) authServiceClient.getAuthUserEmailById(order.getUserId()).getBody().get("data");
                orderDTO.getUser().setEmail(email);
                for (OrderItem oi : order.getOrderItems()) {
                // Lấy thông tin sản phẩm từ ProductServiceClient
                    OrderItemFromProductDTO responseProducts = productServiceClient.getProductById(oi.getProductId());
                    OrderItemUserDTO orderItemUserDTO = new OrderItemUserDTO();
                    orderItemUserDTO.setOrderItemId(oi.getOrderItemId());
                    orderItemUserDTO.setQuantity(oi.getQuantity());
                    orderItemUserDTO.setUnitPrice(oi.getUnitPrice());
                    orderItemUserDTO.setTotalPrice(oi.getTotalPrice());
                    orderItemUserDTO.setProduct(responseProducts);
                    listOrderItem.add(orderItemUserDTO);
                }
            orderDTO.setOrderItems(listOrderItem);

            } else {
                System.out.println("Failed to fetch user data for order ID: " + order.getId());
            }
            orderDTOs.add(orderDTO);
        }
        return orderDTOs;
    }

    @Override
    @CircuitBreaker(name = "orderService", fallbackMethod = "findAllPaginatedFallback")
    public Page<OrderDTO> findAllPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findAll(pageable);
        
        List<OrderDTO> orderDTOList = new ArrayList<>();
        for (Order order : orderPage.getContent()) {
            OrderDTO orderDTO = new OrderDTO();
            // Lấy thông tin người dùng từ UserServiceClient
            ResponseEntity<Map<String, Object>> response = userServiceClient.getUserProfileById(order.getUserId());
            if (response.getStatusCode().is2xxSuccessful()) {
                List<OrderItemUserDTO> listOrderItem = new ArrayList<>();
                orderDTO.setOrderNumber(order.getOrderNumber());
                orderDTO.setOrderDate(order.getOrderDate());
                orderDTO.setId(order.getId());
                orderDTO.setStatus(order.getStatus());
                orderDTO.setPaymentMethod(order.getPaymentMethod());
                orderDTO.setShippingAddress(order.getShippingAddress());
                orderDTO.setTotalAmount(order.getTotalAmount());
                orderDTO.setUser(objectMapper.convertValue(response.getBody().get("data"), UserDTO.class));
                String email = (String) authServiceClient.getAuthUserEmailById(order.getUserId()).getBody().get("data");
                orderDTO.getUser().setEmail(email);
                for (OrderItem oi : order.getOrderItems()) {
                    // Lấy thông tin sản phẩm từ ProductServiceClient
                    OrderItemFromProductDTO responseProducts = productServiceClient.getProductById(oi.getProductId());
                    OrderItemUserDTO orderItemUserDTO = new OrderItemUserDTO();
                    orderItemUserDTO.setOrderItemId(oi.getOrderItemId());
                    orderItemUserDTO.setQuantity(oi.getQuantity());
                    orderItemUserDTO.setUnitPrice(oi.getUnitPrice());
                    orderItemUserDTO.setTotalPrice(oi.getTotalPrice());
                    orderItemUserDTO.setProduct(responseProducts);
                    listOrderItem.add(orderItemUserDTO);
                }
                orderDTO.setOrderItems(listOrderItem);
            } else {
                logger.error("Failed to fetch user data for order ID: {}", order.getId());
            }
            orderDTOList.add(orderDTO);
        }
        
        // Sắp xếp theo ngày đơn hàng giảm dần (mới nhất lên đầu)
        orderDTOList.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        
        return new PageImpl<>(orderDTOList, pageable, orderPage.getTotalElements());
    }
    
    // Phương thức fallback cho findAllPaginated
    public Page<OrderDTO> findAllPaginatedFallback(int page, int size, Throwable t) {
        logger.error("Circuit breaker triggered for findAllPaginated. Error: {}", t.getMessage(), t);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orderPage = orderRepository.findAll(pageable);
            
            List<OrderDTO> orderDTOList = new ArrayList<>();
            for (Order order : orderPage.getContent()) {
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setOrderNumber(order.getOrderNumber());
                orderDTO.setOrderDate(order.getOrderDate());
                orderDTO.setId(order.getId());
                orderDTO.setStatus(order.getStatus());
                orderDTO.setPaymentMethod(order.getPaymentMethod());
                orderDTO.setShippingAddress(order.getShippingAddress());
                orderDTO.setTotalAmount(order.getTotalAmount());
                // Trong trường hợp fallback, có thể không lấy được thông tin chi tiết về user hoặc sản phẩm
                orderDTOList.add(orderDTO);
            }
            
            orderDTOList.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
            
            return new PageImpl<>(orderDTOList, pageable, orderPage.getTotalElements());
        } catch (Exception ex) {
            logger.error("Error in fallback method: {}", ex.getMessage(), ex);
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
        }
    }

    @Override
    @CircuitBreaker(name = "orderService", fallbackMethod = "filterOrdersPaginatedFallback")
    public Page<OrderDTO> filterOrdersPaginated(String keyword, String status, String sort, int page, int size) {
        Specification<Order> spec = Specification.where(null);

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(containsKeyword(keyword));
        }

        if (status != null && !status.isEmpty()) {
            spec = spec.and(hasStatus(status));
        }

        Sort sortOption = Sort.unsorted();
        if ("asc".equalsIgnoreCase(sort)) {
            sortOption = Sort.by("totalAmount").ascending();
        } else if ("desc".equalsIgnoreCase(sort)) {
            sortOption = Sort.by("totalAmount").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sortOption);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        
        List<OrderDTO> orderDTOList = new ArrayList<>();

        for (Order order : orderPage.getContent()) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderNumber(order.getOrderNumber());
            orderDTO.setOrderDate(order.getOrderDate());
            orderDTO.setId(order.getId());
            orderDTO.setStatus(order.getStatus());
            orderDTO.setPaymentMethod(order.getPaymentMethod());
            orderDTO.setShippingAddress(order.getShippingAddress());
            orderDTO.setTotalAmount(order.getTotalAmount());

            // Lấy thông tin người dùng
            ResponseEntity<Map<String, Object>> response = userServiceClient.getUserProfileById(order.getUserId());
            if (response.getStatusCode().is2xxSuccessful()) {
                UserDTO userDTO = objectMapper.convertValue(response.getBody().get("data"), UserDTO.class);
                // Gọi Auth Service để lấy email
                ResponseEntity<Map<String, Object>> emailResponse = authServiceClient.getAuthUserEmailById(order.getUserId());
                if (emailResponse.getStatusCode().is2xxSuccessful()) {
                    String email = (String) emailResponse.getBody().get("data");
                    userDTO.setEmail(email);
                }
                orderDTO.setUser(userDTO);
            } else {
                logger.error("Failed to fetch user data for order ID: {}", order.getId());
            }

            // Lấy thông tin các OrderItem và sản phẩm tương ứng
            List<OrderItemUserDTO> listOrderItem = new ArrayList<>();
            for (OrderItem oi : order.getOrderItems()) {
                OrderItemUserDTO orderItemUserDTO = new OrderItemUserDTO();
                orderItemUserDTO.setOrderItemId(oi.getOrderItemId());
                orderItemUserDTO.setQuantity(oi.getQuantity());
                orderItemUserDTO.setUnitPrice(oi.getUnitPrice());
                orderItemUserDTO.setTotalPrice(oi.getTotalPrice());

                // Lấy sản phẩm từ ProductServiceClient
                OrderItemFromProductDTO responseProduct = productServiceClient.getProductById(oi.getProductId());
                orderItemUserDTO.setProduct(responseProduct);
                listOrderItem.add(orderItemUserDTO);
            }
            orderDTO.setOrderItems(listOrderItem);

            orderDTOList.add(orderDTO);
        }

        return new PageImpl<>(orderDTOList, pageable, orderPage.getTotalElements());
    }
    
    // Phương thức fallback cho filterOrdersPaginated
    public Page<OrderDTO> filterOrdersPaginatedFallback(String keyword, String status, String sort, int page, int size, Throwable t) {
        logger.error("Circuit breaker triggered for filterOrdersPaginated. Error: {}", t.getMessage(), t);
        
        try {
            Specification<Order> spec = Specification.where(null);

            if (keyword != null && !keyword.isEmpty()) {
                spec = spec.and(containsKeyword(keyword));
            }

            if (status != null && !status.isEmpty()) {
                spec = spec.and(hasStatus(status));
            }

            Sort sortOption = Sort.unsorted();
            if ("asc".equalsIgnoreCase(sort)) {
                sortOption = Sort.by("totalAmount").ascending();
            } else if ("desc".equalsIgnoreCase(sort)) {
                sortOption = Sort.by("totalAmount").descending();
            }

            Pageable pageable = PageRequest.of(page, size, sortOption);
            Page<Order> orderPage = orderRepository.findAll(spec, pageable);
            
            List<OrderDTO> orderDTOList = new ArrayList<>();

            for (Order order : orderPage.getContent()) {
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setOrderNumber(order.getOrderNumber());
                orderDTO.setOrderDate(order.getOrderDate());
                orderDTO.setId(order.getId());
                orderDTO.setStatus(order.getStatus());
                orderDTO.setPaymentMethod(order.getPaymentMethod());
                orderDTO.setShippingAddress(order.getShippingAddress());
                orderDTO.setTotalAmount(order.getTotalAmount());
                // Trong trường hợp fallback, có thể không lấy được thông tin chi tiết về user hoặc sản phẩm
                orderDTOList.add(orderDTO);
            }
            
            return new PageImpl<>(orderDTOList, pageable, orderPage.getTotalElements());
        } catch (Exception ex) {
            logger.error("Error in fallback method: {}", ex.getMessage(), ex);
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
        }
    }

    @Override
    public OrderDTO findOrderFullInfo(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return null;
        }

        OrderDTO orderDTO = new OrderDTO();

        // Lấy thông tin người dùng từ UserServiceClient
        ResponseEntity<Map<String, Object>> response = userServiceClient.getUserProfileById(order.getUserId());
        if (response.getStatusCode().is2xxSuccessful()) {
            orderDTO.setOrderNumber(order.getOrderNumber());
            orderDTO.setOrderDate(order.getOrderDate());
            orderDTO.setId(order.getId());
            orderDTO.setStatus(order.getStatus());
            orderDTO.setPaymentMethod(order.getPaymentMethod());
            orderDTO.setShippingAddress(order.getShippingAddress());
            orderDTO.setTotalAmount(order.getTotalAmount());

            // Mapping user
            UserDTO userDTO = objectMapper.convertValue(response.getBody().get("data"), UserDTO.class);

            // Lấy email từ AuthServiceClient
            String email = (String) authServiceClient.getAuthUserEmailById(order.getUserId()).getBody().get("data");
            userDTO.setEmail(email);
            orderDTO.setUser(userDTO);

            // Mapping orderItems
            List<OrderItemUserDTO> listOrderItem = new ArrayList<>();
            for (OrderItem oi : order.getOrderItems()) {
                OrderItemUserDTO orderItemUserDTO = new OrderItemUserDTO();
                orderItemUserDTO.setOrderItemId(oi.getOrderItemId());
                orderItemUserDTO.setQuantity(oi.getQuantity());
                orderItemUserDTO.setUnitPrice(oi.getUnitPrice());
                orderItemUserDTO.setTotalPrice(oi.getTotalPrice());

                // Lấy thông tin sản phẩm từ ProductServiceClient
                OrderItemFromProductDTO productDTO = productServiceClient.getProductById(oi.getProductId());
                orderItemUserDTO.setProduct(productDTO);

                listOrderItem.add(orderItemUserDTO);
            }
            orderDTO.setOrderItems(listOrderItem);
        } else {
            System.out.println("Failed to fetch user data for order ID: " + order.getId());
        }

        return orderDTO;
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public boolean deleteById(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if(order != null && order.getStatus().equals("FAILED")) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public List<OrderDTO> filterOrders(String keyword, String status, String sort) {
        Specification<Order> spec = Specification.where(null);

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(containsKeyword(keyword));
        }

        if (status != null && !status.isEmpty()) {
            spec = spec.and(hasStatus(status));
        }

        Sort sortOption = Sort.unsorted();
        if ("asc".equalsIgnoreCase(sort)) {
            sortOption = Sort.by("totalAmount").ascending();
        } else if ("desc".equalsIgnoreCase(sort)) {
            sortOption = Sort.by("totalAmount").descending();
        }

        List<Order> orders = orderRepository.findAll(spec, sortOption);
        List<OrderDTO> orderDTOs = new ArrayList<>();

        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderNumber(order.getOrderNumber());
            orderDTO.setOrderDate(order.getOrderDate());
            orderDTO.setId(order.getId());
            orderDTO.setStatus(order.getStatus());
            orderDTO.setPaymentMethod(order.getPaymentMethod());
            orderDTO.setShippingAddress(order.getShippingAddress());
            orderDTO.setTotalAmount(order.getTotalAmount());

            // Lấy thông tin người dùng
            ResponseEntity<Map<String, Object>> response = userServiceClient.getUserProfileById(order.getUserId());
            if (response.getStatusCode().is2xxSuccessful()) {
                UserDTO userDTO = objectMapper.convertValue(response.getBody().get("data"), UserDTO.class);
                // Gọi Auth Service để lấy email
                ResponseEntity<Map<String, Object>> emailResponse = authServiceClient.getAuthUserEmailById(order.getUserId());
                if (emailResponse.getStatusCode().is2xxSuccessful()) {
                    String email = (String) emailResponse.getBody().get("data");
                    userDTO.setEmail(email);
                }
                orderDTO.setUser(userDTO);
            } else {
                System.out.println("Failed to fetch user data for order ID: " + order.getId());
            }

            // Lấy thông tin các OrderItem và sản phẩm tương ứng
            List<OrderItemUserDTO> listOrderItem = new ArrayList<>();
            for (OrderItem oi : order.getOrderItems()) {
                OrderItemUserDTO orderItemUserDTO = new OrderItemUserDTO();
                orderItemUserDTO.setOrderItemId(oi.getOrderItemId());
                orderItemUserDTO.setQuantity(oi.getQuantity());
                orderItemUserDTO.setUnitPrice(oi.getUnitPrice());
                orderItemUserDTO.setTotalPrice(oi.getTotalPrice());

                // Lấy sản phẩm từ ProductServiceClient
                OrderItemFromProductDTO responseProduct = productServiceClient.getProductById(oi.getProductId());
                orderItemUserDTO.setProduct(responseProduct);
                listOrderItem.add(orderItemUserDTO);
            }
            orderDTO.setOrderItems(listOrderItem);

            orderDTOs.add(orderDTO);
        }

        return orderDTOs;
    }
    // Tìm kiếm theo từ khóa trong các trường cụ thể
    public static Specification<Order> containsKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("orderNumber")), likePattern),         // Tìm theo Order Number
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
    public List<OrderDTO> getOrdersByYearAndMonth(int year, Integer month) {
        List<OrderDTO> orderDTOs = new ArrayList<>();
        if (month != null) {
            List<Order> orders = orderRepository.findByYearAndMonth(year, month);
            for (Order order : orders) {
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setId(order.getId());
                orderDTO.setOrderNumber(order.getOrderNumber());
                orderDTO.setOrderDate(order.getOrderDate());
                orderDTO.setTotalAmount(order.getTotalAmount());
                orderDTO.setStatus(order.getStatus());
                orderDTO.setPaymentMethod(order.getPaymentMethod());
                orderDTO.setShippingAddress(order.getShippingAddress());
                UserDTO userDTO = objectMapper.convertValue(userServiceClient.getUserProfileById(order.getUserId()).getBody().get("data"), UserDTO.class);
                orderDTO.setUser(userDTO);
                orderDTOs.add(orderDTO);
            }

        } else {
            List<Order> orders = orderRepository.findByYear(year);
            for (Order order : orders) {
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setId(order.getId());
                orderDTO.setOrderNumber(order.getOrderNumber());
                orderDTO.setOrderDate(order.getOrderDate());
                orderDTO.setTotalAmount(order.getTotalAmount());
                orderDTO.setStatus(order.getStatus());
                orderDTO.setPaymentMethod(order.getPaymentMethod());
                orderDTO.setShippingAddress(order.getShippingAddress());
                UserDTO userDTO = objectMapper.convertValue(userServiceClient.getUserProfileById(order.getUserId()).getBody().get("data"), UserDTO.class);
                orderDTO.setUser(userDTO);
                orderDTOs.add(orderDTO);
            }
        }
        return orderDTOs;
    }

    @Override
    @CircuitBreaker(name = "orderService", fallbackMethod = "getOrdersByYearAndMonthPaginatedFallback")
    public Page<OrderDTO> getOrdersByYearAndMonthPaginated(int year, Integer month, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;
        
        if (month != null) {
            orderPage = orderRepository.findByYearAndMonthPaginated(year, month, pageable);
        } else {
            orderPage = orderRepository.findByYearPaginated(year, pageable);
        }
        
        List<OrderDTO> orderDTOList = new ArrayList<>();
        for (Order order : orderPage.getContent()) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setId(order.getId());
            orderDTO.setOrderNumber(order.getOrderNumber());
            orderDTO.setOrderDate(order.getOrderDate());
            orderDTO.setTotalAmount(order.getTotalAmount());
            orderDTO.setStatus(order.getStatus());
            orderDTO.setPaymentMethod(order.getPaymentMethod());
            orderDTO.setShippingAddress(order.getShippingAddress());
            
            try {
                UserDTO userDTO = objectMapper.convertValue(
                    userServiceClient.getUserProfileById(order.getUserId()).getBody().get("data"), 
                    UserDTO.class
                );
                orderDTO.setUser(userDTO);
            } catch (Exception e) {
                logger.error("Error fetching user data for order ID: {}", order.getId(), e);
            }
            
            orderDTOList.add(orderDTO);
        }
        
        return new PageImpl<>(orderDTOList, pageable, orderPage.getTotalElements());
    }
    
    // Phương thức fallback cho getOrdersByYearAndMonthPaginated
    public Page<OrderDTO> getOrdersByYearAndMonthPaginatedFallback(int year, Integer month, int page, int size, Throwable t) {
        logger.error("Circuit breaker triggered for getOrdersByYearAndMonthPaginated. Error: {}", t.getMessage(), t);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orderPage;
            
            if (month != null) {
                orderPage = orderRepository.findByYearAndMonthPaginated(year, month, pageable);
            } else {
                orderPage = orderRepository.findByYearPaginated(year, pageable);
            }
            
            List<OrderDTO> orderDTOList = new ArrayList<>();
            for (Order order : orderPage.getContent()) {
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setId(order.getId());
                orderDTO.setOrderNumber(order.getOrderNumber());
                orderDTO.setOrderDate(order.getOrderDate());
                orderDTO.setTotalAmount(order.getTotalAmount());
                orderDTO.setStatus(order.getStatus());
                orderDTO.setPaymentMethod(order.getPaymentMethod());
                orderDTO.setShippingAddress(order.getShippingAddress());
                // Trong fallback không thể lấy thông tin user đầy đủ
                orderDTOList.add(orderDTO);
            }
            
            return new PageImpl<>(orderDTOList, pageable, orderPage.getTotalElements());
        } catch (Exception ex) {
            logger.error("Error in fallback method: {}", ex.getMessage(), ex);
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);
        }
    }

    @Override
    public void exportOrderData(List<OrderDTO> orders, HttpServletResponse response) throws IOException {
        // Thiết lập header cho response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=BaoCaoDonHang.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Đơn hàng");

        // Tạo tiêu đề cho file Excel
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Báo Cáo Đơn Hàng");

        // Định dạng tiêu đề
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        // Hợp nhất các cột cho tiêu đề
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        int currentRowIndex = 2;

        // Tạo Header
        Row headerRow = sheet.createRow(currentRowIndex++);
        String[] headers = {"Mã đơn hàng", "Khách hàng", "Ngày đặt", "Địa chỉ giao hàng", "Phương thức thanh toán", "Trạng thái", "Tổng tiền"};
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
        double tongDoanhThu = 0;
        for (OrderDTO order : orders) {
            Row row = sheet.createRow(currentRowIndex++);

            row.createCell(0).setCellValue(order.getOrderNumber());
            row.createCell(1).setCellValue(order.getUser().getUsername());
            row.createCell(2).setCellValue(order.getOrderDate().toString());
            row.createCell(3).setCellValue(order.getShippingAddress());
            row.createCell(4).setCellValue(order.getPaymentMethod());
            row.createCell(5).setCellValue(order.getStatus());
            row.createCell(6).setCellValue(order.getTotalAmount());

            // Tính tổng doanh thu
            tongDoanhThu += order.getTotalAmount();
        }

        // Thêm dòng tổng doanh thu
        Row totalRow = sheet.createRow(currentRowIndex + 1);
        Cell totalLabelCell = totalRow.createCell(5);
        totalLabelCell.setCellValue("TỔNG DOANH THU:");

        CellStyle totalLabelStyle = workbook.createCellStyle();
        Font totalFont = workbook.createFont();
        totalFont.setBold(true);
        totalLabelStyle.setFont(totalFont);
        totalLabelCell.setCellStyle(totalLabelStyle);

        Cell totalValueCell = totalRow.createCell(6);
        totalValueCell.setCellValue(tongDoanhThu);

        // Tự động điều chỉnh độ rộng cột
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // DÒNG QUAN TRỌNG - Ghi workbook vào output stream của response
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Override
    public List<OrderDTO> findOrdersByUserId(Long userId) {

        List<Order> orders = orderRepository.findOrdersByUserId(userId);
        List<OrderDTO> orderDTOs = new ArrayList<>();

        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO();

            // Lấy thông tin người dùng từ UserServiceClient
            ResponseEntity<Map<String, Object>> response = userServiceClient.getUserProfileById(order.getUserId());
            if (response.getStatusCode().is2xxSuccessful()) {
                orderDTO.setOrderNumber(order.getOrderNumber());
                orderDTO.setOrderDate(order.getOrderDate());
                orderDTO.setId(order.getId());
                orderDTO.setStatus(order.getStatus());
                orderDTO.setPaymentMethod(order.getPaymentMethod());
                orderDTO.setShippingAddress(order.getShippingAddress());
                orderDTO.setTotalAmount(order.getTotalAmount());

                // Mapping user
                UserDTO userDTO = objectMapper.convertValue(response.getBody().get("data"), UserDTO.class);

                // Lấy email từ AuthServiceClient
                String email = (String) authServiceClient.getAuthUserEmailById(order.getUserId()).getBody().get("data");
                userDTO.setEmail(email);
                orderDTO.setUser(userDTO);

                // Mapping orderItems
                List<OrderItemUserDTO> listOrderItem = new ArrayList<>();
                for (OrderItem oi : order.getOrderItems()) {
                    OrderItemUserDTO orderItemUserDTO = new OrderItemUserDTO();
                    orderItemUserDTO.setOrderItemId(oi.getOrderItemId());
                    orderItemUserDTO.setQuantity(oi.getQuantity());
                    orderItemUserDTO.setUnitPrice(oi.getUnitPrice());
                    orderItemUserDTO.setTotalPrice(oi.getTotalPrice());

                    // Lấy thông tin sản phẩm từ ProductServiceClient
                    OrderItemFromProductDTO productDTO = productServiceClient.getProductById(oi.getProductId());
                    orderItemUserDTO.setProduct(productDTO);

                    listOrderItem.add(orderItemUserDTO);
                }
                orderDTO.setOrderItems(listOrderItem);
            } else {
                System.out.println("Failed to fetch user data for order ID: " + order.getId());
            }
            orderDTOs.add(orderDTO);
        }
        return orderDTOs;

    }


}
