package com.kang.kcloud_aidrive.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class CommonUtilTest {

    @Test
    void testGetFileSuffix_ValidFileName() {
        assertEquals("txt", CommonUtil.getFileSuffix("document.txt"));
        assertEquals("jpg", CommonUtil.getFileSuffix("image.jpg"));
        assertEquals("png", CommonUtil.getFileSuffix("photo.png"));
    }

    @Test
    void testGetFileSuffix_MultipleDots() {
        assertEquals("log", CommonUtil.getFileSuffix("server.log.backup.log"));
        assertEquals("gz", CommonUtil.getFileSuffix("archive.tar.gz"));
    }

    @Test
    void testGetFileSuffix_NoExtension() {
        // File names without an extension
        assertEquals("", CommonUtil.getFileSuffix("filename"));
        assertEquals("", CommonUtil.getFileSuffix("file."));
    }

    @Test
    void testGetFilePath_ValidFileName() {
        String fileName = "example.JPG";
        String result = CommonUtil.getFilePath(fileName);

        LocalDate mockDate = LocalDate.of(2025, 1, 13);
        String expectedDate = mockDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/";

        assertTrue(result.startsWith(expectedDate), "The file path should start with the correct date.");
        assertTrue(result.endsWith(".JPG"), "The file path should end with the correct suffix.");

        String uuidPart = result.substring(expectedDate.length(), result.lastIndexOf('.'));
        assertTrue(isValidUUID(uuidPart), "The UUID part of the file path should be valid.");
    }

    private boolean isValidUUID(String uuid) {
        String uuidRegex = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";
        return Pattern.matches(uuidRegex, uuid);
    }

    @Test
    public void testFlux() {
        Flux<String> flux1 = Flux.just("A", "B", "C");       // 固定元素
        Flux<Integer> flux2 = Flux.range(1, 5);              // 数字序列
        Flux<Long> flux3 = Flux.interval(Duration.ofSeconds(1)); // 定时生成

        Flux<Integer> flux = Flux.range(1, 10);
        //.filter(n -> n % 2 == 0)
        //.map(n -> n * 10);

        flux.subscribe(System.out::println);
    }

    @Test
    public void testMono() {
        Mono<String> mono1 = Mono.just("Hello");             // 单值
        Mono<Throwable> mono3 = Mono.error(new RuntimeException("Error")); // 错误信号
    }

    @Test
    public void testFluxFromListOrArray() {
        // 从List集合创建Flux并订阅消费
        List<String> list = Arrays.asList("Java", "Python");
        Flux<String> fluxFromList = Flux.fromIterable(list);
        fluxFromList.subscribe(System.out::println);

        // 从数组创建Flux并订阅消费
        String[] array = {"Apple", "Banana"};
        Flux<String> fluxFromArray = Flux.fromArray(array);
        fluxFromArray.subscribe(System.out::println);
    }

    @Test
    public void testWebClient() {
        Map<String, Object> body = new HashMap<>();
        body.put("url", "https://kaikang-sde.github.io/CoT-And-ReAct-In-LLM-Agents/");
        body.put("summary_type", "summary within 200 words");
        body.put("language", "English");

        String requestBodyJson = JsonUtil.obj2Json(body);
        System.out.println("Request body: " + requestBodyJson);

        // 创建WebClient
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8000")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build();

        // 发送POST请求并处理流式响应 POST request
        Flux<String> response = webClient.post()
                // 设置目标API端点
                .uri("/api/document/stream")
                // 设置JSON格式请求体（自动序列化）
                .bodyValue(requestBodyJson)
                // 发送请求并获取响应
                .retrieve()
                // 将响应体转换为持续接收字符串的流式对象
                .bodyToFlux(String.class);

        // Handle the streaming response
        response.subscribe(
                chunk -> {
                    System.out.println("Received chunk: {}" + chunk);
                },
                error -> {
                    System.out.println("Error: {}" + error.getMessage());
                },
                () -> {
                    System.out.println("Completed");
                }
        );

        // Wait for the response to complete
        response.blockLast(Duration.ofSeconds(60));

    }

}
