package com.ShopMaster.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * AppConfig — registra RestTemplate como bean de Spring.
 * Necesario para que ChatBotService pueda @Autowired RestTemplate.
 *
 * Coloca este archivo en: src/main/java/com/ShopMaster/Config/AppConfig.java
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
