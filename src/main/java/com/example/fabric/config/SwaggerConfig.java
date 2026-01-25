package com.example.fabric.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Fabric Production Management System")
                        .version("1.0.0")
                        .description("API documentation"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public OperationCustomizer customizeOperation() {
        List<String> publicPaths = List.of("/user/login");

        return (operation, handlerMethod) -> {
            RequestMapping classMapping = handlerMethod.getBeanType().getAnnotation(RequestMapping.class);
            if (classMapping != null) {
                String[] paths = classMapping.value();
                for (String path : paths) {
                    for (String publicPath : publicPaths) {
                        if (path.startsWith(publicPath)) {
                            operation.setSecurity(List.of());
                            return operation;
                        }
                    }
                }
            }
            return operation;
        };
    }
}
