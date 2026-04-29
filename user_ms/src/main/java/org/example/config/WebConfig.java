package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapam ruta URL "/uploads/**" catre folderul fizic "uploads/" de pe discul aplicatiei
        registry.addResourceHandler("/users/uploads/**")
                .addResourceLocations("file:/app/uploads/");
    }
}