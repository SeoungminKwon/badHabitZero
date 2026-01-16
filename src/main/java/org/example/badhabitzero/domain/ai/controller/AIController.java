package org.example.badhabitzero.domain.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.badhabitzero.domain.ai.dto.*;

import org.example.badhabitzero.domain.ai.service.ChromaService;
import org.example.badhabitzero.domain.ai.service.GeminiService;
import org.example.badhabitzero.domain.ai.service.ValueAnalysisService;
import org.example.badhabitzero.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Tag(name = "AI", description = "AI 관련 API")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final GeminiService geminiService;
    private final ChromaService chromaService;
    private final ValueAnalysisService valueAnalysisService;

    @Operation(summary = "악습 분석", description = "악습을 분석하고 추가 질문 생성")
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AnalyzeResponse>> analyze(@RequestBody AnalyzeRequest request) {
        AnalyzeResponse response = valueAnalysisService.analyze(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "가치 산정", description = "사용자 답변을 바탕으로 최종 가치 산정")
    @PostMapping("/analyze/complete")
    public ResponseEntity<ApiResponse<ValueResult>> analyzeComplete(@RequestBody AnalyzeCompleteRequest request) {
        ValueResult result = valueAnalysisService.calculateValue(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "AI 테스트", description = "Gemini API 연동 테스트")
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<String>> testAI(@RequestBody String prompt) {
        String response = geminiService.generate(prompt);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Chroma 연결 테스트", description = "Chroma DB 연결 상태 확인")
    @GetMapping("/chroma/test")
    public ResponseEntity<ApiResponse<String>> testChroma() {
        try {
            WebClient webClient = WebClient.create();
            String response = webClient.get()
                    .uri("http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(ApiResponse.success("연결 성공: " + response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success("연결 실패: " + e.getMessage()));
        }
    }

    @Operation(summary = "RAG 검색", description = "Vector DB에서 유사 문서 검색")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int topK) {

        List<Map<String, Object>> results = chromaService.search(query, topK);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @Operation(summary = "카테고리별 RAG 검색", description = "특정 카테고리에서 유사 문서 검색")
    @GetMapping("/search/category")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchByCategory(
            @RequestParam String query,
            @RequestParam String category,
            @RequestParam(defaultValue = "3") int topK) {

        List<Map<String, Object>> results = chromaService.searchByCategory(query, category, topK);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    // ============================================
    // 챗봇 기반 가치 산정 API
    // ============================================

    @Operation(summary = "챗봇 세션 시작", description = "가치 산정 챗봇 대화 세션 시작")
    @PostMapping("/chat/start")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> startChat(@RequestBody ChatSessionRequest request) {
        ChatSessionResponse response = valueAnalysisService.startChatSession(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "챗봇 메시지 전송", description = "사용자 답변 전송 및 다음 질문 수신")
    @PostMapping("/chat/message")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(@RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = valueAnalysisService.handleChatMessage(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}