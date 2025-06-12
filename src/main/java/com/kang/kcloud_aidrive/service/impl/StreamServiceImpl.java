package com.kang.kcloud_aidrive.service.impl;

import com.kang.kcloud_aidrive.config.WebClientConfig;
import com.kang.kcloud_aidrive.service.StreamService;
import com.kang.kcloud_aidrive.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kai Kang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamServiceImpl implements StreamService {
    private final WebClient webClient;
    private final WebClientConfig webClientConfig;


    @Override
    public Flux<String> handleChatStream(String token, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);

        return sendRequest(webClientConfig.getChatStreamPath(), body, token);
    }

    /**
     * Sends an HTTP POST request to the specified path with the given body and optional authentication token.
     * The method handles the request asynchronously and returns a reactive stream of response strings.
     *
     * @param path  The API endpoint path to send the request to
     * @param body  A map containing the request payload to be sent as JSON
     * @param token Optional authentication token for authorization (can be null for unauthenticated requests)
     * @return A Flux of String containing the server's response stream
     * @throws RuntimeException If there's an error processing the request or serializing the body
     */
    private Flux<String> sendRequest(String path, Map<String, Object> body, String token) {
        try {
            // Convert the request body map to JSON string
            String requestBodyJson = JsonUtil.obj2Json(body);

            // Prepare the request specification with common settings
            WebClient.RequestBodySpec requestSpec = webClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON);

            // Add authentication header if token is provided
            if (token != null) {
                requestSpec.header("token", token);
            }

            // Execute the request and process the response
            return requestSpec.bodyValue(requestBodyJson)
                    .retrieve()
                    .bodyToFlux(String.class)
                    // Log any errors that occur during the stream
                    .doOnError(error -> log.error("Error in stream: {}", error.getMessage()))
                    // Log when the stream completes successfully
                    .doOnComplete(() -> log.info("Stream completed"));

        } catch (Exception e) {
            // Log the error and return an error message in the stream
            log.error("Error processing request: {}", e.getMessage());
            return Flux.just("Invalid request format");
        }
    }
}
