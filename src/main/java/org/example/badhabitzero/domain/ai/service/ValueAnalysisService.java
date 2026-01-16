package org.example.badhabitzero.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.badhabitzero.domain.ai.dto.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValueAnalysisService {

    private final GeminiService geminiService;
    private final ChromaService chromaService;
    private final ChatSessionManager chatSessionManager;
    private final ObjectMapper objectMapper;

    /**
     * 1단계: 악습 분석 및 추가 질문 생성
     */
    public AnalyzeResponse analyze(AnalyzeRequest request) {
        // RAG 검색
        List<Map<String, Object>> ragResults = chromaService.searchByCategory(
                request.getHabitName(),
                request.getCategory(),
                3
        );

        // AI에게 추가 질문 생성 요청
        String prompt = buildQuestionPrompt(request, ragResults);
        String aiResponse = geminiService.generate(prompt);

        // 디버깅용 로그
        log.info("질문 생성 AI 응답: {}", aiResponse);

        // AI 응답 파싱
        return parseQuestionResponse(aiResponse);
    }

    /**
     * 2단계: 최종 가치 산정
     */
    public ValueResult calculateValue(AnalyzeCompleteRequest request) {
        // RAG 검색
        List<Map<String, Object>> ragResults = chromaService.searchByCategory(
                request.getHabitName(),
                request.getCategory(),
                5
        );

        // AI에게 가치 산정 요청
        String prompt = buildValuePrompt(request, ragResults);
        String aiResponse = geminiService.generate(prompt);

        // 디버깅용 로그
        log.info("가치 산정 AI 응답: {}", aiResponse);

        // AI 응답 파싱
        return parseValueResponse(aiResponse);
    }

    /**
     * 추가 질문 생성 프롬프트
     */
    private String buildQuestionPrompt(AnalyzeRequest request, List<Map<String, Object>> ragResults) {
        StringBuilder ragContext = new StringBuilder();
        for (Map<String, Object> result : ragResults) {
            ragContext.append("- ").append(result.get("content")).append("\n");
        }

        return String.format("""
            당신은 악습의 가치를 산정하기 위해 사용자에게 추가 질문을 하는 전문가입니다.
            
            [사용자 입력]
            - 악습: %s
            - 카테고리: %s
            - 고치고 싶은 이유: %s
            
            [관련 데이터]
            %s
            
            [규칙]
            1. 가치 산정에 필요한 추가 정보를 질문 형태로 3~5개 생성하세요.
            2. 직접 비용, 빈도, 시간대, 양 등을 파악할 수 있는 질문을 만드세요.
            3. 반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트 없이 JSON만 출력하세요.
            
            [응답 형식]
            {
              "needMoreInfo": true,
              "questions": [
                {"id": "price", "question": "주로 지출하는 금액은 얼마인가요?", "type": "number", "options": null},
                {"id": "frequency", "question": "일주일에 몇 번 정도 하시나요?", "type": "number", "options": null},
                {"id": "time", "question": "주로 어떤 시간대에 하시나요?", "type": "choice", "options": ["아침", "점심", "저녁", "밤"]}
              ]
            }
            """,
                request.getHabitName(),
                request.getCategory(),
                request.getReason() != null ? request.getReason() : "없음",
                ragContext.toString()
        );
    }

    /**
     * 가치 산정 프롬프트
     */
    private String buildValuePrompt(AnalyzeCompleteRequest request, List<Map<String, Object>> ragResults) {
        StringBuilder ragContext = new StringBuilder();
        List<String> sources = new ArrayList<>();

        for (Map<String, Object> result : ragResults) {
            ragContext.append("- ").append(result.get("content")).append("\n");
            Map<String, Object> metadata = (Map<String, Object>) result.get("metadata");
            if (metadata != null && metadata.get("source") != null) {
                sources.add((String) metadata.get("source"));
            }
        }

        StringBuilder answersStr = new StringBuilder();
        if (request.getAnswers() != null) {
            for (Map.Entry<String, Object> entry : request.getAnswers().entrySet()) {
                answersStr.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }

        return String.format("""
            당신은 악습의 경제적 가치를 산정하는 전문가입니다.
            
            [사용자 정보]
            - 악습: %s
            - 카테고리: %s
            - 고치고 싶은 이유: %s
            
            [사용자 추가 정보]
            %s
            
            [참고 데이터]
            %s
            
            [가치 산정 기준]
            1. 직접 비용 (directCost): 실제 지출 금액
            2. 건강 비용 (healthCost): 건강 악화로 인한 비용 환산
            3. 기회 비용 (opportunityCost): 시간 낭비를 시급으로 환산 (평균 시급 25,000원 기준)
            4. 심리 비용 (psychologicalCost): 스트레스, 죄책감 등 (1,000~5,000원 범위)
            
            [규칙]
            1. 참고 데이터가 있으면 활용하고, 없으면 일반 지식으로 추정하세요.
            2. 1회당 비용을 계산하세요.
            3. 반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트 없이 JSON만 출력하세요.
            
            [응답 형식]
            {
              "value": 15000,
              "breakdown": {
                "directCost": 10000,
                "healthCost": 2000,
                "opportunityCost": 2000,
                "psychologicalCost": 1000
              },
              "explanation": "배달음식 1회 평균 15,000원 지출에 건강 및 심리 비용을 합산했습니다.",
              "sources": ["통계청, 2023", "AI 추정"]
            }
            """,
                request.getHabitName(),
                request.getCategory(),
                request.getReason() != null ? request.getReason() : "없음",
                answersStr.toString(),
                ragContext.toString()
        );
    }

    /**
     * 질문 응답 파싱 - 개선 버전
     */
    private AnalyzeResponse parseQuestionResponse(String aiResponse) {
        try {
            String json = extractJson(aiResponse);

            if (json.equals("{}")) {
                log.warn("JSON 추출 실패, 기본 질문 반환");
                return getDefaultQuestions();
            }

            JsonNode root = objectMapper.readTree(json);

            List<AnalyzeResponse.Question> questions = new ArrayList<>();
            JsonNode questionsNode = root.path("questions");

            if (!questionsNode.isArray() || questionsNode.isEmpty()) {
                log.warn("questions 배열 없음, 기본 질문 반환");
                return getDefaultQuestions();
            }

            for (JsonNode q : questionsNode) {
                List<String> options = null;
                if (q.has("options") && q.get("options").isArray()) {
                    options = new ArrayList<>();
                    for (JsonNode opt : q.get("options")) {
                        options.add(opt.asText());
                    }
                }

                questions.add(AnalyzeResponse.Question.builder()
                        .id(q.path("id").asText("unknown"))
                        .question(q.path("question").asText("질문을 불러오지 못했습니다."))
                        .type(q.path("type").asText("text"))
                        .options(options)
                        .build());
            }

            return AnalyzeResponse.builder()
                    .needMoreInfo(true)
                    .questions(questions)
                    .build();

        } catch (Exception e) {
            log.error("질문 응답 파싱 실패: {}", e.getMessage());
            log.error("원본 응답: {}", aiResponse);
            return getDefaultQuestions();
        }
    }

    /**
     * 가치 응답 파싱 - 개선 버전
     */
    private ValueResult parseValueResponse(String aiResponse) {
        try {
            String json = extractJson(aiResponse);

            if (json.equals("{}")) {
                log.warn("JSON 추출 실패, 기본값 반환");
                return getDefaultValueResult();
            }

            JsonNode root = objectMapper.readTree(json);

            // 필수 필드 체크
            if (!root.has("value")) {
                log.warn("value 필드 없음, 기본값 반환");
                return getDefaultValueResult();
            }

            List<String> sources = new ArrayList<>();
            JsonNode sourcesNode = root.path("sources");
            if (sourcesNode.isArray()) {
                for (JsonNode s : sourcesNode) {
                    sources.add(s.asText());
                }
            }

            JsonNode breakdown = root.path("breakdown");

            return ValueResult.builder()
                    .value(root.path("value").asInt())
                    .breakdown(ValueResult.Breakdown.builder()
                            .directCost(breakdown.path("directCost").asInt(0))
                            .healthCost(breakdown.path("healthCost").asInt(0))
                            .opportunityCost(breakdown.path("opportunityCost").asInt(0))
                            .psychologicalCost(breakdown.path("psychologicalCost").asInt(0))
                            .build())
                    .explanation(root.path("explanation").asText("AI가 산정한 결과입니다."))
                    .sources(sources.isEmpty() ? List.of("AI 추정") : sources)
                    .build();

        } catch (Exception e) {
            log.error("가치 응답 파싱 실패: {}", e.getMessage());
            log.error("원본 응답: {}", aiResponse);
            return getDefaultValueResult();
        }
    }

    /**
     * JSON 추출 (AI 응답에서 JSON 부분만) - 개선 버전
     */
    private String extractJson(String response) {
        if (response == null || response.isEmpty()) {
            log.warn("AI 응답이 비어있음");
            return "{}";
        }

        // 1. 마크다운 코드블록 제거 (```json ... ```)
        response = response.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        // 2. JSON 객체 추출 { ... }
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");

        if (start != -1 && end != -1 && end > start) {
            String json = response.substring(start, end + 1);
            log.debug("추출된 JSON: {}", json);
            return json;
        }

        log.warn("JSON 추출 실패. 원본: {}", response);
        return "{}";
    }

    /**
     * 기본 질문 (파싱 실패 시)
     */
    private AnalyzeResponse getDefaultQuestions() {
        return AnalyzeResponse.builder()
                .needMoreInfo(true)
                .questions(List.of(
                        AnalyzeResponse.Question.builder()
                                .id("cost")
                                .question("1회당 대략 얼마를 지출하시나요?")
                                .type("number")
                                .build(),
                        AnalyzeResponse.Question.builder()
                                .id("frequency")
                                .question("일주일에 몇 번 정도 하시나요?")
                                .type("number")
                                .build(),
                        AnalyzeResponse.Question.builder()
                                .id("duration")
                                .question("1회당 몇 시간 정도 하시나요?")
                                .type("number")
                                .build()
                ))
                .build();
    }

    /**
     * 기본 가치 결과 (파싱 실패 시)
     */
    private ValueResult getDefaultValueResult() {
        return ValueResult.builder()
                .value(10000)
                .breakdown(ValueResult.Breakdown.builder()
                        .directCost(7000)
                        .healthCost(1000)
                        .opportunityCost(1000)
                        .psychologicalCost(1000)
                        .build())
                .explanation("기본값으로 산정되었습니다. 다시 시도해주세요.")
                .sources(List.of("기본값"))
                .build();
    }

    // ============================================
    // 챗봇 기반 가치 산정 메서드
    // ============================================

    /**
     * 챗봇 세션 시작 - 자연어 질문 생성
     */
    public ChatSessionResponse startChatSession(ChatSessionRequest request) {
        // RAG 검색
        List<Map<String, Object>> ragResults = chromaService.searchByCategory(
                request.getHabitName(),
                request.getCategory(),
                3
        );

        // AI에게 자연어 질문 목록 생성 요청
        String prompt = buildConversationalQuestionsPrompt(request, ragResults);
        String aiResponse = geminiService.generate(prompt);

        log.info("챗봇 질문 생성 AI 응답: {}", aiResponse);

        // 질문 목록 파싱
        List<String> questions = parseQuestionList(aiResponse);

        if (questions.isEmpty()) {
            questions = getDefaultConversationalQuestions();
        }

        // 세션 생성
        ChatSessionManager.ChatSession session = chatSessionManager.createSession(
                request.getHabitName(),
                request.getCategory(),
                request.getReason(),
                questions
        );

        // 첫 번째 질문 반환
        return ChatSessionResponse.builder()
                .sessionId(session.getSessionId())
                .message(questions.get(0))
                .questionNumber(1)
                .totalQuestions(questions.size())
                .isComplete(false)
                .build();
    }

    /**
     * 사용자 메시지 처리 - 다음 질문 또는 결과 반환
     */
    public ChatMessageResponse handleChatMessage(ChatMessageRequest request) {
        ChatSessionManager.ChatSession session = chatSessionManager.getSession(request.getSessionId());

        if (session == null) {
            return ChatMessageResponse.builder()
                    .message("세션이 만료되었습니다. 다시 시작해주세요.")
                    .questionNumber(0)
                    .totalQuestions(0)
                    .isComplete(true)
                    .valueResult(null)
                    .build();
        }

        // 답변 저장
        chatSessionManager.addAnswer(request.getSessionId(), request.getMessage());

        // 모든 질문 완료 체크
        if (chatSessionManager.isComplete(request.getSessionId())) {
            // 가치 산정
            ValueResult valueResult = calculateValueFromConversation(session);
            chatSessionManager.removeSession(request.getSessionId());

            return ChatMessageResponse.builder()
                    .message("분석이 완료되었습니다!")
                    .questionNumber(session.getQuestions().size())
                    .totalQuestions(session.getQuestions().size())
                    .isComplete(true)
                    .valueResult(valueResult)
                    .build();
        }

        // 다음 질문 반환
        String nextQuestion = chatSessionManager.getNextQuestion(request.getSessionId());

        return ChatMessageResponse.builder()
                .message(nextQuestion)
                .questionNumber(session.getCurrentQuestionIndex() + 1)
                .totalQuestions(session.getQuestions().size())
                .isComplete(false)
                .valueResult(null)
                .build();
    }

    /**
     * 대화 기반 가치 산정
     */
    private ValueResult calculateValueFromConversation(ChatSessionManager.ChatSession session) {
        // RAG 검색
        List<Map<String, Object>> ragResults = chromaService.searchByCategory(
                session.getHabitName(),
                session.getCategory(),
                5
        );

        // AI에게 가치 산정 요청
        String prompt = buildValueFromConversationPrompt(session, ragResults);
        String aiResponse = geminiService.generate(prompt);

        log.info("챗봇 가치 산정 AI 응답: {}", aiResponse);

        return parseValueResponse(aiResponse);
    }

    /**
     * 자연어 질문 생성 프롬프트
     */
    private String buildConversationalQuestionsPrompt(ChatSessionRequest request, List<Map<String, Object>> ragResults) {
        StringBuilder ragContext = new StringBuilder();
        for (Map<String, Object> result : ragResults) {
            ragContext.append("- ").append(result.get("content")).append("\n");
        }

        return String.format("""
            당신은 사용자의 악습에 대해 친근하게 대화하며 정보를 수집하는 상담사입니다.

            [사용자 정보]
            - 악습: %s
            - 카테고리: %s
            - 고치고 싶은 이유: %s

            [관련 데이터]
            %s

            [규칙]
            1. 가치 산정에 필요한 정보를 수집하기 위한 질문 3~5개를 생성하세요.
            2. 질문은 자연스러운 대화체로 작성하세요 (친근하고 공감하는 톤).
            3. 사용자가 자유롭게 답변할 수 있도록 개방형 질문으로 작성하세요.
            4. 다음 정보를 파악할 수 있는 질문을 포함하세요:
               - 1회당 비용 (금액)
               - 빈도 (얼마나 자주)
               - 소요 시간
               - 건강/심리적 영향
            5. 반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트 없이 JSON만 출력하세요.

            [응답 형식]
            {
              "questions": [
                "안녕하세요! 우선 한 번 할 때 보통 얼마 정도 쓰시나요? 대략적으로 알려주셔도 괜찮아요.",
                "일주일에 몇 번 정도 하시는 것 같아요?",
                "한 번 하면 보통 얼마나 시간을 보내시나요?",
                "하고 나면 어떤 기분이 드세요? 후회가 되거나 몸이 안 좋아지는 느낌이 있으신가요?"
              ]
            }
            """,
                request.getHabitName(),
                request.getCategory(),
                request.getReason() != null ? request.getReason() : "없음",
                ragContext.toString()
        );
    }

    /**
     * 대화 기반 가치 산정 프롬프트
     */
    private String buildValueFromConversationPrompt(ChatSessionManager.ChatSession session, List<Map<String, Object>> ragResults) {
        StringBuilder ragContext = new StringBuilder();
        for (Map<String, Object> result : ragResults) {
            ragContext.append("- ").append(result.get("content")).append("\n");
        }

        StringBuilder conversationStr = new StringBuilder();
        List<String> questions = session.getQuestions();
        List<String> answers = session.getAnswers();

        for (int i = 0; i < questions.size(); i++) {
            conversationStr.append("Q: ").append(questions.get(i)).append("\n");
            if (i < answers.size()) {
                conversationStr.append("A: ").append(answers.get(i)).append("\n");
            }
            conversationStr.append("\n");
        }

        return String.format("""
            당신은 악습의 경제적 가치를 산정하는 전문가입니다.
            사용자와의 자연어 대화를 분석하여 가치를 산정해주세요.

            [사용자 정보]
            - 악습: %s
            - 카테고리: %s
            - 고치고 싶은 이유: %s

            [대화 내용]
            %s

            [참고 데이터]
            %s

            [가치 산정 기준]
            1. 직접 비용 (directCost): 대화에서 파악된 실제 지출 금액
            2. 건강 비용 (healthCost): 건강 악화로 인한 비용 환산
            3. 기회 비용 (opportunityCost): 시간 낭비를 시급으로 환산 (평균 시급 25,000원 기준)
            4. 심리 비용 (psychologicalCost): 스트레스, 죄책감 등 (1,000~5,000원 범위)

            [규칙]
            1. 대화 내용에서 금액, 빈도, 시간 등의 정보를 추출하세요.
            2. 자연어로 된 답변을 해석하세요 (예: "만원 정도" → 10000, "일주일에 두세번" → 2.5)
            3. 명확하지 않은 경우 합리적으로 추정하세요.
            4. 1회당 비용을 계산하세요.
            5. 반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트 없이 JSON만 출력하세요.

            [응답 형식]
            {
              "value": 15000,
              "breakdown": {
                "directCost": 10000,
                "healthCost": 2000,
                "opportunityCost": 2000,
                "psychologicalCost": 1000
              },
              "explanation": "대화 분석 결과, 1회 평균 10,000원 지출에 약 1시간 소요됩니다. 건강 및 심리 비용을 합산하여 총 15,000원으로 산정했습니다.",
              "sources": ["AI 분석"]
            }
            """,
                session.getHabitName(),
                session.getCategory(),
                session.getReason() != null ? session.getReason() : "없음",
                conversationStr.toString(),
                ragContext.toString()
        );
    }

    /**
     * 자연어 질문 목록 파싱
     */
    private List<String> parseQuestionList(String aiResponse) {
        try {
            String json = extractJson(aiResponse);

            if (json.equals("{}")) {
                return new ArrayList<>();
            }

            JsonNode root = objectMapper.readTree(json);
            JsonNode questionsNode = root.path("questions");

            if (!questionsNode.isArray() || questionsNode.isEmpty()) {
                return new ArrayList<>();
            }

            List<String> questions = new ArrayList<>();
            for (JsonNode q : questionsNode) {
                questions.add(q.asText());
            }

            return questions;

        } catch (Exception e) {
            log.error("질문 목록 파싱 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 기본 대화형 질문 목록 (파싱 실패 시)
     */
    private List<String> getDefaultConversationalQuestions() {
        return List.of(
                "안녕하세요! 우선 한 번 할 때 보통 얼마 정도 쓰시나요?",
                "일주일에 몇 번 정도 하시는 것 같아요?",
                "한 번 하면 보통 얼마나 시간을 보내시나요?",
                "하고 나면 어떤 기분이 드세요?"
        );
    }
}