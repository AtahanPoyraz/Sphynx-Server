package io.sphynx.server.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sphynx-Server API")
                        .version("v1.0.0")
                        .description("Sphynx Server API documentation. This API allows you to manage agents, users, authentication, and other core services.")
                        .termsOfService("https://yourcompany.com/terms")
                        .contact(new Contact()
                                .name("Atahan Poyraz")
                                .email("atahan.poyraz@example.com")
                                .url("https://yourcompany.com/contact"))
                        .license(new License()
                                .name("GNU General Public License v3.0")
                                .url("https://www.gnu.org/licenses/gpl-3.0.en.html"))
                );
    }
}
