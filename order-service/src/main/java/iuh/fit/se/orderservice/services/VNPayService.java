package iuh.fit.se.orderservice.services;

import iuh.fit.se.orderservice.dtos.OrderRequest;

public interface VNPayService {
    public String createVNPayUrlAndCreateOrder(OrderRequest orderRequest);
}
