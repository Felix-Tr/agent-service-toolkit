package de.trafficvalidator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI 3.0 documentation (Swagger)
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trafficValidatorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Traffic Light Validator API")
                        .description("API for validating traffic light configurations against safety rules")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Traffic Validator Team")
                                .email("support@trafficvalidator.de"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Default Server URL")
                ));
    }
} 