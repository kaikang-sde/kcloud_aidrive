package com.kang.kcloud_aidrive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuration class for Cross-Origin Resource Sharing (CORS) settings.
 * This configuration allows cross-origin requests to the API endpoints.
 *
 * @author Kai Kang
 */
@Configuration
public class MyCorsFilter {

    /**
     * Creates and configures a CORS filter bean.
     * This filter allows cross-origin requests with the following settings:
     * - Allows requests from any origin (in production, consider restricting this to specific domains)
     * - Allows credentials (cookies, HTTP authentication) to be included in requests
     * - Allows all HTTP methods (GET, POST, PUT, DELETE, etc.)
     * - Allows all headers in the request
     * - Exposes all response headers to the client
     *
     * @return Configured CorsFilter instance
     */
    @Bean
    public CorsFilter corsFilter() {
        // Create CORS configuration
        CorsConfiguration config = new CorsConfiguration();

        // Allow requests from any origin (replace "*" with specific origins in production)
        config.addAllowedOriginPattern("*");

        // Allow credentials (cookies, HTTP authentication) to be included in requests
        config.setAllowCredentials(true);

        // Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
        config.addAllowedMethod("*");

        // Allow all headers in the request
        config.addAllowedHeader("*");

        // Expose all response headers to the client
        config.addExposedHeader("*");

        // Create a URL-based CORS configuration source
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();

        // Register CORS configuration for all paths
        corsConfigurationSource.registerCorsConfiguration(
                "/**",  // Apply to all paths
                config
        );

        // Create and return the CORS filter with the configuration
        return new CorsFilter(corsConfigurationSource);
    }
}