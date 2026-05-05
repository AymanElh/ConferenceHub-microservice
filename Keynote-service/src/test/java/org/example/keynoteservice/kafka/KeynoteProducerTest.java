package org.example.keynoteservice.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class KeynoteProducerTest {

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks KeynoteProducer keynoteProducer;

    @Test
    void shouldSendWelcomeEvent_ShouldSendToKafka() {
        KeynoteWelcomeEvent event = KeynoteWelcomeEvent.builder()
                .keynoteId(1L)
                .nom("Doe")
                .prenom("john")
                .email("john@doe.com")
                .fonction("speaker")
                .welcomeMessage("Welcome!")
                .build();

        when(kafkaTemplate.send(any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        keynoteProducer.sendWelcomeEvent(event);

        verify(kafkaTemplate).send(any(Message.class));
    }

    @Test
    void sendWelcomeEvent_ShouldLog_WhenKafkaFails() {
        when(kafkaTemplate.send(any(Message.class)))
                .thenThrow(new RuntimeException("Kafka down"));

        assertThatThrownBy(() -> keynoteProducer.sendWelcomeEvent(
                KeynoteWelcomeEvent.builder().keynoteId(1L)
                        .email("x@x.com").build()))
                .isInstanceOf(RuntimeException.class);
    }
}
