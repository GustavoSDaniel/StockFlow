package com.gustavosdaniel.stock_flow_api.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring configuration for OpenAPI 3 (Swagger) documentation.
 * <p>
 * Defines API metadata (title, version, contact, license), server environments
 * (local and production), resource tags for each domain aggregate, and the
 * Bearer JWT security scheme used by the Keycloak-secured endpoints.
 * </p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Builds the OpenAPI specification for the StockFlow API.
     *
     * @return a fully configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI customOpenAPI(){

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("StockFlows")
                        .version("1.0")
                        .description("API reativa de gerenciamento de estoques. " +
                                "Autentique-se via Keycloak e insira o token JWT no botão 'Authorize'.")
                        .contact(new Contact()
                                .name("Gustavo SIlva Daniel")
                                .email("gustavosdaniel@hotmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:6060")
                                .description("Ambiente local"),
                        new Server()
                                .url("https://api.stockflow.com")
                                .description("Ambiente de produção")
                ))
                .tags(List.of(
                        new Tag().name("Users")         .description("Gerenciamento de usuários"),
                        new Tag().name("Products")      .description("Gerenciamento de produtos"),
                        new Tag().name("Stocks")        .description("Controle de estoque"),
                        new Tag().name("Suppliers")     .description("Gerenciamento de fornecedores"),
                        new Tag().name("Categories")    .description("Gerenciamento de categorias"),
                        new Tag().name("Movements")     .description("Movimentações de estoque"),
                        new Tag().name("Notifications") .description("Alertas e notificações"),
                        new Tag().name("Dashboard")     .description("Métricas e relatórios"),
                        new Tag().name("Errors")        .description("Documentação de erros")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName)
                )
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insira o token JWT gerado pelo Keycloak " +
                                        "(sem a palavra 'Bearer')")
                        )
                );
    }
}
