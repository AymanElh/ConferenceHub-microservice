package com.conferenchub.configservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "CONFIG_GIT_URI=dummy",
        "CONFIG_GIT_USER=dummy",
        "CONFIG_GIT_PASSWORD=dummy"
})
@ActiveProfiles("native")
class ConfigServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
