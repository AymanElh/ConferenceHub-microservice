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
    @Autowired
    private org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void testSendKafkaMessage() throws InterruptedException {
        String jsonEvent = "{\"eventType\": \"KEYNOTE_CREATED\", \"timestamp\": \"2026-03-10T09:00:00\", \"keynoteId\": 101, \"nom\": \"Alice\", \"prenom\": \"Wonder\", \"email\": \"alicekafka@test.com\", \"fonction\": \"Speaker\"}";
                
        kafkaTemplate.send("keynote-events", jsonEvent);
        System.out.println(">>> Message sent to Kafka");
        Thread.sleep(5000); // Wait for consumer to process
    }
}
