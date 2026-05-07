package com.conferenchub.discoveryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "spring.cloud.inetutils.use-only-site-local-interfaces=true",
        "spring.cloud.inetutils.default-hostname=localhost",
        "spring.cloud.inetutils.default-ip-address=127.0.0.1"
})
@ActiveProfiles("test")
class DiscoveryServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
