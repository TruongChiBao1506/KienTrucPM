package iuh.fit.se.notificationservice.services.impl;

import iuh.fit.se.notificationservice.entities.Notification;
import iuh.fit.se.notificationservice.repositories.NotificationRepository;
import iuh.fit.se.notificationservice.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    NotificationRepository notificationRepository;

    @Override
    public List<Notification> findByIsReadFalse() {
        return notificationRepository.findByIsReadFalse();
    }

    @Override
    public Notification findById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }
}
