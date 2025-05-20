package iuh.fit.se.notificationservice.services;

import iuh.fit.se.notificationservice.entities.Notification;

import java.util.List;

public interface NotificationService {
    public Notification save(Notification notification);
    public Notification findById(Long id);
    public List<Notification> findByIsReadFalse();

}
