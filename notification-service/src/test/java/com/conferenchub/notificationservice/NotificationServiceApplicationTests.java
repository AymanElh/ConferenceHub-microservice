package com.conferenchub.notificationservice;

import com.conferenchub.notificationservice.application.service.NotificationService;
import com.conferenchub.notificationservice.domain.model.EventType;
import com.conferenchub.notificationservice.domain.model.Notification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class NotificationServiceApplicationTests {

    @Autowired
    private NotificationService notificationService;

    @Test
    void testSendEmail() {
        Notification notification = Notification.builder()
                .destinataire("test@example.com")
                .sujet("Test Mailpit Integration")
                .contenu("Hello, this is a test email sent to Mailpit.")
                .typeEvenement(EventType.KEYNOTE_CREATED)
                .build();
        
        notificationService.processAndSaveNotification(notification);
        
        assertNotNull(notification.getId());
    }

}
