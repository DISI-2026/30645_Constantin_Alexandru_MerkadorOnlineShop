package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapam ruta URL "users/uploads/**" catre folderul fizic "app/uploads/" din containerul acestui microserviciu
        registry.addResourceHandler("/users/uploads/**")
                .addResourceLocations("file:/upload_data/");
    }
}