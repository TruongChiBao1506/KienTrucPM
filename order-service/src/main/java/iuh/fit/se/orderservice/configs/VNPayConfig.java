package iuh.fit.se.orderservice.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VNPayConfig {
    @Value("${vnpay.url}")
    private String vnpUrl;

    @Value("${vnpay.tmn_code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash_secret}")
    private String vnpHashSecret;

    @Value("${vnpay.return_url}")
    private String returnUrl;

    @Value("${vnpay.ip_addr}")
    private String vnpIpAddr;


    public VNPayConfig() {
        super();
    }

    public String getVnpUrl() {
        return vnpUrl;
    }

    public String getVnpTmnCode() {
        return vnpTmnCode;
    }

    public String getVnpHashSecret() {
        return vnpHashSecret;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getVnpIpAddr() {
        return vnpIpAddr;
    }

}
