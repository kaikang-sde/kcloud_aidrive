package com.kang.kcloud_aidrive.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Swagger3.0：http://localhost:8080/swagger-ui/index.html
 * Doc: https://springdoc.org/
 * Author: Kai Kang
 */

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KCloud AIDrive APIs")
                        .version("1.0")
                        .description("KCloud AIDrive APIs")
                        .termsOfService("https://com.kang")
                        .license(new License().name("Apache 2.0").url("https://com.kang"))
                        .contact(new Contact()
                                .name("Kai Kang")
                                .email("kaikangsde@gmail.com")
                                .url("https://github.com/kaikang-sde")
                        )
                );
    }
}

