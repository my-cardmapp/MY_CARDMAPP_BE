package com.thc.my_cardmapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Card-Map API")
                        .version("1.0")
                        .description("복지카드 가맹점 찾기 서비스 API")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@example.com")));
    }
}