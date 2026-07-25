package com.badal.moneybot.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI moneyBotOpenAPI() {

        return new OpenAPI()

                .info(new Info()

                        .title("MoneyBot API")

                        .description("Automated Crypto Trading Bot using Spring Boot")

                        .version("1.0")

                        .contact(new Contact()

                                .name("Badal Solanki")

                                .email("badal@example.com")))

                .externalDocs(new ExternalDocumentation()

                        .description("MoneyBot Documentation"));

    }

}
