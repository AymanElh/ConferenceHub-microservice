package com.conferenchub.configservice;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ConfigServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(ConfigServiceApplication.class);

    @Value("${spring.cloud.config.server.git.uri:}")
    private String gitUri;

    @Value("${spring.cloud.config.server.git.username:}")
    private String gitUser;

    public static void main(String[] args) {
        SpringApplication.run(ConfigServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {
        log.debug("GIT URI: {}", gitUri);
        log.debug("GIT USER: {}", gitUser);
    }

}
