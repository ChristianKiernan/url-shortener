package com.christiankiernan.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .description("A RESTful API for creating and managing shortened URLs.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Christian Kiernan")
                                .url("https://github.com/ChristianKiernan/url-shortener")));
    }
}
