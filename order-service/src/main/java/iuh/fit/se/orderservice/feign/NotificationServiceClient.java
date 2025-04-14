package iuh.fit.se.orderservice.feign;

import iuh.fit.se.orderservice.configs.FeignHeaderInterceptor;
import iuh.fit.se.orderservice.configs.FeignConfig;
import iuh.fit.se.orderservice.dtos.Notification;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service", path = "/api/notifications", configuration = {FeignHeaderInterceptor.class, FeignConfig.class})
public interface NotificationServiceClient {
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveNotification(@RequestBody Notification notification);
}
