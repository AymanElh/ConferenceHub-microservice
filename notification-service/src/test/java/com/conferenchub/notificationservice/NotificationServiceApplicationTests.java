package com.conferenchub.notificationservice;

import com.conferenchub.notificationservice.application.service.NotificationService;
import com.conferenchub.notificationservice.domain.model.EventType;
import com.conferenchub.notificationservice.domain.model.Notification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.conferenchub.notificationservice.domain.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.inetutils.use-only-site-local-interfaces=true",
        "spring.cloud.inetutils.default-hostname=localhost",
        "spring.cloud.inetutils.default-ip-address=127.0.0.1",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration," +
                "org.springframework.boot.mongodb.autoconfigure.MongoReactiveAutoConfiguration," +
                "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration," +
                "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration," +
                "org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveAutoConfiguration," +
                "org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveRepositoriesAutoConfiguration," +
                "org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration"
})
@ActiveProfiles("test")
class NotificationServiceApplicationTests {

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) {
                notification.setId("test-notification-id");
            }
            return notification;
        });
    }


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
