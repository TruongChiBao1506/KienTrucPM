package iuh.fit.se.notificationservice.controllers;

import iuh.fit.se.notificationservice.entities.Notification;
import iuh.fit.se.notificationservice.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class notificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello from notification service";
    }

    // Lấy danh sách thông báo chưa đọc
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications() {
        System.out.println("getUnreadNotifications");
        Map<String, Object> response = new LinkedHashMap<>();
        List<Notification> notifications = notificationService.findByIsReadFalse();
        response.put("status", HttpStatus.OK.value());
        response.put("data", notifications);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Đánh dấu thông báo là đã đọc
    @PostMapping("/{id}/mark-as-read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        System.out.println("markAsRead");
        Notification notification = notificationService.findById(id);
        notification.setRead(true);
        notificationService.save(notification);
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Notification has been marked as read");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveNotification(@RequestBody Notification notification) {
        Map<String, Object> response = new LinkedHashMap<>();
        notificationService.save(notification);
        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "Notification has been created");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
