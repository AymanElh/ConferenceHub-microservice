package org.example.keynoteservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.inetutils.use-only-site-local-interfaces=true",
        "spring.cloud.inetutils.default-hostname=localhost",
        "spring.cloud.inetutils.default-ip-address=127.0.0.1"
})
@ActiveProfiles("test")
class KeynoteServiceApplicationTests {

    @MockitoBean
    KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void contextLoads() {
    }

}
