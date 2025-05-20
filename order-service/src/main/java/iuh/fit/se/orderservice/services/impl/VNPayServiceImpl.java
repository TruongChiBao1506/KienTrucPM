package iuh.fit.se.orderservice.services.impl;

import iuh.fit.se.orderservice.configs.VNPayConfig;
import iuh.fit.se.orderservice.dtos.OrderItemRequest;
import iuh.fit.se.orderservice.dtos.OrderRequest;
import iuh.fit.se.orderservice.entities.Order;
import iuh.fit.se.orderservice.entities.OrderItem;
import iuh.fit.se.orderservice.repositories.OrderItemRepository;
import iuh.fit.se.orderservice.repositories.OrderRepository;
import iuh.fit.se.orderservice.services.VNPayService;
import iuh.fit.se.orderservice.utils.VNPayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VNPayServiceImpl implements VNPayService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private VNPayConfig vnpayConfig;

    @Override
    @Transactional
    public String createVNPayUrlAndCreateOrder(OrderRequest orderRequest) {
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
            orderItemRepository.save(orderItem);
        }

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpayConfig.getVnpTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf((long) (order.getTotalAmount() * 100))); // Số tiền phải *
        vnpParams.put("vnp_BankCode", "NCB");																							// 100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", order.getOrderNumber()); // Mã giao dịch duy nhất
        vnpParams.put("vnp_OrderInfo", "Thanhtoandonhang" + order.getOrderNumber());
        vnpParams.put("vnp_OrderType", "billpayment");
        vnpParams.put("vnp_IpAddr", vnpayConfig.getVnpIpAddr());
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnpParams.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);


        // Tạo query string
        String query = VNPayUtils.buildQuery(vnpParams, vnpayConfig.getVnpHashSecret(), vnpayConfig.getVnpUrl());
        return query;
    }
    private Double calculateTotal(List<OrderItemRequest> items) {
        return items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getPrice())
                .sum();
    }
    private String createOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        String number = now.getYear() + "" + now.getMonthValue() + "" + now.getDayOfMonth() + "" + now.getHour() + ""
                + now.getMinute() + "" + now.getSecond();
        return number;
    }
}
