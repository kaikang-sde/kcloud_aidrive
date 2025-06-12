package com.kang.kcloud_aidrive.controller;

import com.kang.kcloud_aidrive.service.StreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Chat Stream Controller
 *
 * @author Kai Kang
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class StreamController {
    private final StreamService streamService;

    /**
     * Chat Stream Interface
     * <p>
     * curl -N -X POST \
     * -H "Content-Type: application/json" \
     * -H "token: **" \
     * -d "questions" \
     * http://localhost:8080/api/chat/stream
     */
    @PostMapping(value = "/stream")
    public Flux<String> chatStream(
            @RequestHeader("token") String token,
            @RequestBody String message
    ) {
        return streamService.handleChatStream(token, message);
    }


}
