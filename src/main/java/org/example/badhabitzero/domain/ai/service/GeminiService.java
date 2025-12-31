package org.example.badhabitzero.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.badhabitzero.domain.ai.config.GeminiProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    /**
     * Gemini API 호출
     */
    public String generate(String prompt) {
        String url = String.format(GEMINI_API_URL,
                geminiProperties.getModel(),
                geminiProperties.getApiKey());

        WebClient webClient = WebClient.create();

        // 요청 본문 구성
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 2048
                )
        );

        try {
            String response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 응답에서 텍스트 추출
            return extractTextFromResponse(response);

        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("AI 서비스 호출에 실패했습니다.", e);
        }
    }

    /**
     * Gemini 응답에서 텍스트 추출
     */
    private String extractTextFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            return root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            log.error("응답 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("AI 응답 처리에 실패했습니다.", e);
        }
    }

    public String getApiKey() {
        return geminiProperties.getApiKey();
    }
}