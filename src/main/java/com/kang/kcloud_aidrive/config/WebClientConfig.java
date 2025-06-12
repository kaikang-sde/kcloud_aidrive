/**
 * Configuration class for setting up WebClient with connection pooling and timeout configurations.
 * This class provides a pre-configured WebClient instance for making HTTP requests
 * with optimized connection pooling and timeout settings.
 */
package com.kang.kcloud_aidrive.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


/**
 * Configuration properties for the WebClient with connection pooling capabilities.
 * All properties are configurable via application.yml/application.properties with 'stream' prefix.
 * @author Kai Kang
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "stream")
public class WebClientConfig {

    /**
     * Base URL for all requests made by the WebClient
     * Example: https://api.example.com
     */
    private String baseUrl;

    /**
     * Path for chat streaming endpoint
     * This will be appended to the baseUrl for streaming requests
     */
    private String chatStreamPath;

    /**
     * Timeout Configuration
     */
    private Timeout timeout = new Timeout();

    /**
     * Pool Configuration
     */
    private Pool pool = new Pool();

    /**
     * Nested configuration class for timeout settings
     * All durations should be specified in the application configuration
     */
    @Data
    public static class Timeout {
        private Duration connect;
        private Duration response;
        private Duration read;
        private Duration write;
    }

    /**
     * Nested configuration class for connection pool settings
     * These settings control the behavior of the underlying connection pool
     */
    @Data
    public static class Pool {
        private int maxConnections;
        private Duration maxIdleTime;
        private Duration maxLifeTime;
        private Duration pendingAcquireTimeout;
        private Duration evictInBackground;
    }

    /**
     * Creates and configures a WebClient instance with connection pooling and timeout settings.
     * The WebClient is configured for both regular and streaming responses.
     *
     * @return Configured WebClient instance ready for use
     */
    @Bean
    public WebClient webClient() {
        // Configure connection pool with settings from application properties
        ConnectionProvider provider = ConnectionProvider.builder("stream-connection-pool")
                .maxConnections(pool.getMaxConnections())
                .maxIdleTime(pool.getMaxIdleTime())
                .maxLifeTime(pool.getMaxLifeTime())
                .pendingAcquireTimeout(pool.getPendingAcquireTimeout())
                .evictInBackground(pool.getEvictInBackground())
                .build();

        // Configure HttpClient with timeouts and connection settings
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeout.getConnect().toMillis())
                .responseTimeout(timeout.getResponse())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeout.getRead().getSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout.getWrite().getSeconds(), TimeUnit.SECONDS))
                );

        // Build and return WebClient with all configurations
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
