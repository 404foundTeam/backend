package com.found404.marketbee.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.file-upload-dir}")
    private String uploadDir;
    @Value("${app.public-url-prefix}")
    private String publicPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler(publicPrefix + "/**")
                .addResourceLocations("file:" + path + "/");
    }
}
