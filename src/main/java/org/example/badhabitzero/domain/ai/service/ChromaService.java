package org.example.badhabitzero.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.badhabitzero.domain.ai.config.ChromaProperties;
import org.example.badhabitzero.domain.ai.config.GeminiProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChromaService {
    // 설정값 주입
    private final ChromaProperties chromaProperties;  // Chroma 서버 주소
    private final GeminiService geminiService;        // 임베딩 생성용
    private final ObjectMapper objectMapper;          // JSON 파싱용
    private final GeminiProperties geminiProperties;

    // HTTP 클라이언트 (Chroma 서버와 통신)
    private WebClient webClient;

    // 컬렉션 이름 (테이블 이름 같은 것)
    private static final String COLLECTION_NAME = "habit_facts";

    /**
     * 서비스 시작 시 WebClient 초기화
     * @PostConstruct: 빈 생성 후 자동 실행
     */
    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(chromaProperties.getHost())  // http://localhost:8000(로컬)
                .build();
        log.info("ChromaService 초기화 완료. 서버: {}", chromaProperties.getHost());
    }

    /**
     * 컬렉션 생성 (테이블 만드는 것과 비슷)
     *
     * 컬렉션이 없으면 새로 만들고,
     * 이미 있으면 무시합니다.
     */
    public void createCollection() {
        try {
            // Chroma API에 보낼 데이터
            Map<String, Object> body = Map.of(
                    "name", COLLECTION_NAME,  // 컬렉션 이름: "habit_facts"
                    "metadata", Map.of("description", "악습 관련 사실 데이터")
            );

            // POST 요청으로 컬렉션 생성
            // URL: http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections(로컬)
            webClient.post()
                    .uri("/api/v2/tenants/default_tenant/databases/default_database/collections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();  // 동기 방식으로 대기

            log.info("컬렉션 생성 완료: {}", COLLECTION_NAME);

        } catch (Exception e) {
            log.info("컬렉션이 이미 존재하거나 생성 실패: {}", e.getMessage());
        }
    }

    /**
     * 컬렉션 ID 조회
     *
     * Chroma에서 문서를 추가하거나 검색할 때
     * 컬렉션 이름이 아닌 ID가 필요합니다.
     */
    private String getCollectionId() {
        try {
            // GET 요청으로 컬렉션 정보 조회
            // URL: http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections/habit_facts
            String response = webClient.get()
                    .uri("/api/v2/tenants/default_tenant/databases/default_database/collections/{name}", COLLECTION_NAME)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // JSON 응답에서 id 추출
            // 응답 예시: {"id": "abc-123", "name": "habit_facts", ...}
            JsonNode root = objectMapper.readTree(response);
            return root.path("id").asText();

        } catch (Exception e) {
            log.error("컬렉션 ID 조회 실패: {}", e.getMessage());
            throw new RuntimeException("컬렉션을 찾을 수 없습니다.");
        }
    }

    /**
     * Gemini Embedding API로 임베딩 생성
     *
     * 텍스트를 숫자 배열(벡터)로 변환합니다.
     * 예: "담배는 건강에 해롭다" → [0.12, -0.45, 0.78, ...]
     *
     * @param text 변환할 텍스트
     * @return 임베딩 벡터 (숫자 리스트)
     */
    private List<Float> generateEmbedding(String text) {
        // Gemini Embedding API URL
        // text-embedding-004 모델 사용
        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=%s",
                geminiService.getApiKey()
        );

        // API 요청 본문
        Map<String, Object> body = Map.of(
                "model", "models/text-embedding-004",
                "content", Map.of(
                        "parts", List.of(Map.of("text", text))
                )
        );

        try {
            // Gemini API 호출
            String response = WebClient.create()
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 응답에서 임베딩 값 추출
            // 응답 예시:
            // {
            //   "embedding": {
            //     "values": [0.12, -0.45, 0.78, ...]
            //   }
            // }
            JsonNode root = objectMapper.readTree(response);
            JsonNode values = root.path("embedding").path("values");

            // JsonNode를 List<Float>로 변환
            List<Float> embedding = new ArrayList<>();
            for (JsonNode value : values) {
                embedding.add(value.floatValue());
            }

            log.debug("임베딩 생성 완료. 차원: {}", embedding.size());
            return embedding;

        } catch (Exception e) {
            log.error("임베딩 생성 실패: {}", e.getMessage());
            throw new RuntimeException("임베딩 생성에 실패했습니다.");
        }
    }

    /**
     * 문서 추가 (임베딩과 함께)
     *
     * 1. 텍스트를 임베딩으로 변환 (Gemini)
     * 2. 임베딩과 메타데이터를 Chroma에 저장
     *
     * @param id 문서 고유 ID (예: "smoking_001")
     * @param content 문서 내용 (예: "담배 1갑 가격은 4,500원이다")
     * @param category 카테고리 (예: "SMOKING")
     * @param source 출처 (예: "기획재정부, 2024")
     * @param costType 비용 유형 (예: "direct", "health", "opportunity")
     */
    public void addDocument(String id, String content, String category, String source, String costType) {
        // 1. 컬렉션 ID 가져오기
        String collectionId = getCollectionId();

        // 2. Gemini로 임베딩 생성
        //    "담배 1갑 가격은 4,500원이다" → [0.12, -0.45, ...]
        List<Float> embedding = generateEmbedding(content);

        // 3. Chroma에 저장할 데이터 구성
        Map<String, Object> body = Map.of(
                "ids", List.of(id),                    // 문서 ID 리스트
                "documents", List.of(content),         // 원본 텍스트 리스트
                "metadatas", List.of(Map.of(           // 메타데이터 리스트
                        "category", category,
                        "source", source,
                        "costType", costType
                )),
                "embeddings", List.of(embedding)       // 임베딩 리스트
        );

        try {
            // 4. Chroma에 문서 추가
            // URL: http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections/{collectionId}/add
            webClient.post()
                    .uri("/api/v2/tenants/default_tenant/databases/default_database/collections/{collectionId}/add", collectionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("문서 추가 완료: {} - {}", id, content.substring(0, Math.min(30, content.length())));

        } catch (Exception e) {
            log.error("문서 추가 실패: {}", e.getMessage());
        }
    }

    /**
     * 유사 문서 검색
     *
     * 1. 검색어를 임베딩으로 변환 (Gemini)
     * 2. Chroma에서 비슷한 임베딩을 가진 문서 찾기
     * 3. 유사도 높은 순서대로 반환
     *
     * @param query 검색어 (예: "담배 끊고 싶어요")
     * @param topK 가져올 문서 개수 (예: 3)
     * @return 검색 결과 리스트
     */
    public List<Map<String, Object>> search(String query, int topK) {
        // 1. 컬렉션 ID 가져오기
        String collectionId = getCollectionId();

        // 2. 검색어를 임베딩으로 변환
        //    "담배 끊고 싶어요" → [0.11, -0.43, 0.76, ...]
        List<Float> queryEmbedding = generateEmbedding(query);

        // 3. Chroma 검색 요청 데이터
        Map<String, Object> body = Map.of(
                "query_embeddings", List.of(queryEmbedding),  // 검색할 임베딩
                "n_results", topK,                             // 가져올 개수
                "include", List.of("documents", "metadatas", "distances")  // 포함할 정보
        );

        try {
            // 4. Chroma에 검색 요청
            // URL: http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections/{collectionId}/query
            String response = webClient.post()
                    .uri("/api/v2/tenants/default_tenant/databases/default_database/collections/{collectionId}/query", collectionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 5. 검색 결과 파싱
            return parseSearchResults(response);

        } catch (Exception e) {
            log.error("검색 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 카테고리로 필터링하여 검색
     *
     * 특정 카테고리 내에서만 검색합니다.
     * 예: "SMOKING" 카테고리에서만 검색
     *
     * @param query 검색어
     * @param category 카테고리 (예: "SMOKING")
     * @param topK 가져올 문서 개수
     * @return 검색 결과 리스트
     */
    public List<Map<String, Object>> searchByCategory(String query, String category, int topK) {
        String collectionId = getCollectionId();
        List<Float> queryEmbedding = generateEmbedding(query);

        // where 조건 추가: category가 일치하는 문서만 검색
        Map<String, Object> body = Map.of(
                "query_embeddings", List.of(queryEmbedding),
                "n_results", topK,
                "include", List.of("documents", "metadatas", "distances"),
                "where", Map.of("category", category)  // 필터 조건!
        );

        try {
            String response = webClient.post()
                    .uri("/api/v2/tenants/default_tenant/databases/default_database/collections/{collectionId}/query", collectionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseSearchResults(response);

        } catch (Exception e) {
            log.error("카테고리 검색 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 검색 결과 파싱
     *
     * Chroma 응답을 우리가 사용하기 쉬운 형태로 변환합니다.
     */
    private List<Map<String, Object>> parseSearchResults(String response) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);

            // Chroma 응답 구조:
            // {
            //   "documents": [["문서1", "문서2", "문서3"]],
            //   "metadatas": [[{...}, {...}, {...}]],
            //   "distances": [[0.1, 0.2, 0.3]]
            // }
            // 첫 번째 [0]은 첫 번째 쿼리 결과 (우리는 쿼리 1개만 보냄)

            JsonNode documents = root.path("documents").get(0);
            JsonNode metadatas = root.path("metadatas").get(0);
            JsonNode distances = root.path("distances").get(0);

            // 각 결과를 Map으로 변환
            for (int i = 0; i < documents.size(); i++) {
                Map<String, Object> result = new HashMap<>();
                result.put("content", documents.get(i).asText());
                result.put("metadata", objectMapper.convertValue(metadatas.get(i), Map.class));
                result.put("distance", distances.get(i).floatValue());

                // 유사도 계산 (distance가 작을수록 유사함)
                // 유사도 = 1 - distance (대략적인 변환)
                float similarity = 1 - distances.get(i).floatValue();
                result.put("similarity", similarity);

                results.add(result);
            }

            log.info("검색 결과: {}개 문서 찾음", results.size());

        } catch (Exception e) {
            log.error("검색 결과 파싱 실패: {}", e.getMessage());
        }

        return results;
    }

}
