package com.smartfit.getwayservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.inetutils.use-only-site-local-interfaces=true",
        "spring.cloud.inetutils.default-hostname=localhost",
        "spring.cloud.inetutils.default-ip-address=127.0.0.1"
})
class GetwayServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
