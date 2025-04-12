package iuh.fit.se.orderservice.services;

import iuh.fit.se.orderservice.dtos.OrderRequest;
import iuh.fit.se.orderservice.dtos.OrderStatistic;
import iuh.fit.se.orderservice.entities.Order;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface OrderService {
    public void createOrder(OrderRequest orderRequest);

    public Order findByOrderNumber(String orderNumber);

    public List<Order> findAll();

    public Order findById(Long id);

    public boolean deleteById(Long id);

    public Order save(Order order);

    public List<Order> filterOrders(String keyword, String status, String sort);

    public List<OrderStatistic> getPurchasedOrder();

    public List<OrderStatistic> getSalesOrder();

    public List<Map<String, Object>> getMonthlyRevenue(int year);

    public List<Map<String, Object>> getDailyRevenueInMonth(int year, int month);

    public Map<String, Double> getStatusPercentageByMonth(int year, int month);

    public Map<String, Double> getStatusPercentageByYear(int year);

    public List<Order> getOrdersByYearAndMonth(int year, Integer month);

    public void exportOrderData(List<Order> orders, HttpServletResponse response) throws IOException;

    public List<Order> findOrdersByUserId(Long userId);
}
