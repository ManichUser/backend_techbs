package com.techbs.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort);
        localServer.setDescription("Serveur de développement local");

        Contact contact = new Contact();
        contact.setName("TechBS Team");
        contact.setEmail("contact@techbs.com");
        contact.setUrl("https://techbs.com");

        License license = new License()
            .name("Apache 2.0")
            .url("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
            .title("TechBS API - Formations & Publications")
            .version("1.0.0")
            .description("API REST pour la gestion des formations et publications avec upload de fichiers (images, PDFs, audios, vidéos)")
            .contact(contact)
            .license(license);

        return new OpenAPI()
            .info(info)
            .servers(List.of(localServer));
    }
}