package com.techbs.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir);
        String uploadPathUri = uploadPath.toUri().toString();
        
        // Servir les fichiers statiques depuis le répertoire uploads
        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPathUri + "images/");
        
        registry.addResourceHandler("/pdfs/**")
                .addResourceLocations(uploadPathUri + "pdfs/");
        
        registry.addResourceHandler("/audios/**")
                .addResourceLocations(uploadPathUri + "audios/");
        
        registry.addResourceHandler("/videos/**")
                .addResourceLocations(uploadPathUri + "videos/");
    }
}