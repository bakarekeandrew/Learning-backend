package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:3000",
                "https://*.vercel.app",     // Allow all Vercel subdomains
                "https://learning-backend-production-65eb.up.railway.app"  // Your specific Vercel domain
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("*")  // Add this to expose any custom headers
            .allowCredentials(true)
            .maxAge(3600);  // Cache preflight requests for 1 hour
    }
}