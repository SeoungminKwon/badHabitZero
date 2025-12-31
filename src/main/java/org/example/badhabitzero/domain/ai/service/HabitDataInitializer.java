package org.example.badhabitzero.domain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabitDataInitializer {

    private final ChromaService chromaService;

    /**
     * 앱 시작 시 자동 실행
     *
     * @EventListener(ApplicationReadyEvent.class)
     * = Spring Boot가 완전히 시작된 후 실행됨
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        log.info("========== 악습 데이터 초기화 시작 ==========");

        // 1. 컬렉션 생성 (테이블 만들기)
        chromaService.createCollection();

        // 2. 데이터 추가
        initSmokingData();      // 흡연
        initDrinkingData();     // 음주
        initEatingData();       // 과식/야식
        initSpendingData();     // 과소비
        initLazinessData();     // 게으름
        initDigitalData();      // 디지털
        initCaffeineData();     // 카페인
        initGamblingData();     // 도박

        log.info("========== 악습 데이터 초기화 완료! 총 40개 ==========");
    }

    // ============ SMOKING (흡연) ============
    private void initSmokingData() {
        log.info("SMOKING 데이터 추가 중...");

        addData("smoking_001",
                "담배 1갑 평균 가격은 4,500원이다",
                "SMOKING", "기획재정부, 2024", "direct");

        addData("smoking_002",
                "담배 1개비당 수명이 약 11분 단축된다",
                "SMOKING", "WHO, 2020", "health");

        addData("smoking_003",
                "흡연자는 비흡연자보다 연간 의료비가 약 50만원 더 높다",
                "SMOKING", "국민건강보험공단, 2023", "health");

        addData("smoking_004",
                "흡연은 폐암 발생률을 15배 높인다",
                "SMOKING", "대한폐암학회, 2022", "health");

        addData("smoking_005",
                "금연 시 연간 약 150만원을 절약할 수 있다",
                "SMOKING", "보건복지부, 2023", "direct");
    }

    // ============ DRINKING (음주) ============
    private void initDrinkingData() {
        log.info("DRINKING 데이터 추가 중...");

        addData("drinking_001",
                "소주 1병 평균 가격은 5,000원이다",
                "DRINKING", "통계청, 2024", "direct");

        addData("drinking_002",
                "음주 후 숙취로 인한 생산성 손실은 1회당 약 3만원이다",
                "DRINKING", "한국보건사회연구원, 2022", "opportunity");

        addData("drinking_003",
                "과음자는 연간 의료비가 약 40만원 더 높다",
                "DRINKING", "국민건강보험공단, 2023", "health");

        addData("drinking_004",
                "알코올 의존 치료비용은 연간 약 200만원이다",
                "DRINKING", "중독관리통합지원센터, 2023", "health");

        addData("drinking_005",
                "음주운전 적발 시 벌금은 최소 300만원이다",
                "DRINKING", "도로교통법, 2024", "direct");
    }

    // ============ EATING (과식/야식/배달) ============
    private void initEatingData() {
        log.info("EATING 데이터 추가 중...");

        addData("eating_001",
                "배달음식 1회 평균 주문 금액은 15,000원이다",
                "EATING", "통계청, 2023", "direct");

        addData("eating_002",
                "직접 조리 대비 배달/외식은 평균 10,000원 추가 지출이다",
                "EATING", "한국소비자원, 2023", "direct");

        addData("eating_003",
                "비만으로 인한 연간 추가 의료비는 약 30만원이다",
                "EATING", "국민건강보험공단, 2023", "health");

        addData("eating_004",
                "야식은 수면의 질을 30% 저하시킨다",
                "EATING", "대한수면학회, 2022", "health");

        addData("eating_005",
                "과식으로 인한 소화불량 치료비는 회당 약 2만원이다",
                "EATING", "건강보험심사평가원, 2023", "health");
    }

    // ============ SPENDING (과소비/충동구매) ============
    private void initSpendingData() {
        log.info("SPENDING 데이터 추가 중...");

        addData("spending_001",
                "한국인 월평균 충동구매 금액은 약 15만원이다",
                "SPENDING", "한국소비자원, 2023", "direct");

        addData("spending_002",
                "충동구매 후 후회 비율은 78%이다",
                "SPENDING", "대한상공회의소, 2023", "psychological");

        addData("spending_003",
                "불필요한 구독서비스 평균 지출은 월 3만원이다",
                "SPENDING", "금융감독원, 2023", "direct");

        addData("spending_004",
                "신용카드 연체 시 연이자율은 평균 15%이다",
                "SPENDING", "여신금융협회, 2024", "direct");

        addData("spending_005",
                "과소비자의 스트레스 지수는 평균보다 40% 높다",
                "SPENDING", "한국심리학회, 2022", "psychological");
    }

    // ============ LAZINESS (게으름/미루기) ============
    private void initLazinessData() {
        log.info("LAZINESS 데이터 추가 중...");

        addData("laziness_001",
                "2024년 최저시급은 9,860원이다",
                "LAZINESS", "고용노동부, 2024", "opportunity");

        addData("laziness_002",
                "한국 직장인 평균 시급은 약 25,000원이다",
                "LAZINESS", "통계청, 2023", "opportunity");

        addData("laziness_003",
                "미루기로 인한 생산성 손실은 연간 약 500만원이다",
                "LAZINESS", "한국생산성본부, 2023", "opportunity");

        addData("laziness_004",
                "지각 1회당 평균 손실 비용은 약 2만원이다",
                "LAZINESS", "한국경영자총협회, 2022", "opportunity");

        addData("laziness_005",
                "수면 부족으로 인한 집중력 저하는 업무효율을 25% 감소시킨다",
                "LAZINESS", "대한수면학회, 2022", "opportunity");
    }

    // ============ DIGITAL (SNS/유튜브/게임) ============
    private void initDigitalData() {
        log.info("DIGITAL 데이터 추가 중...");

        addData("digital_001",
                "한국인 하루 평균 스마트폰 사용시간은 4시간 23분이다",
                "DIGITAL", "과학기술정보통신부, 2023", "opportunity");

        addData("digital_002",
                "SNS 과다 사용자의 우울감은 평균보다 30% 높다",
                "DIGITAL", "한국정보화진흥원, 2023", "psychological");

        addData("digital_003",
                "게임 과금 월평균 금액은 약 5만원이다",
                "DIGITAL", "한국콘텐츠진흥원, 2023", "direct");

        addData("digital_004",
                "스마트폰 중독자의 수면 질은 평균보다 40% 낮다",
                "DIGITAL", "대한수면학회, 2022", "health");

        addData("digital_005",
                "디지털 디톡스 시 생산성이 평균 20% 향상된다",
                "DIGITAL", "한국생산성본부, 2023", "opportunity");
    }

    // ============ CAFFEINE (카페인) ============
    private void initCaffeineData() {
        log.info("CAFFEINE 데이터 추가 중...");

        addData("caffeine_001",
                "커피 1잔 평균 가격은 4,500원이다",
                "CAFFEINE", "한국소비자원, 2023", "direct");

        addData("caffeine_002",
                "에너지드링크 1캔 평균 가격은 2,500원이다",
                "CAFFEINE", "편의점 평균가, 2024", "direct");

        addData("caffeine_003",
                "카페인 과다섭취는 불안장애 위험을 25% 높인다",
                "CAFFEINE", "대한정신건강의학회, 2022", "health");

        addData("caffeine_004",
                "오후 카페인 섭취는 수면 질을 35% 저하시킨다",
                "CAFFEINE", "대한수면학회, 2022", "health");

        addData("caffeine_005",
                "카페인 의존 시 두통약 비용이 월 약 1만원 추가된다",
                "CAFFEINE", "건강보험심사평가원, 2023", "health");
    }

    // ============ GAMBLING (도박/투기) ============
    private void initGamblingData() {
        log.info("GAMBLING 데이터 추가 중...");

        addData("gambling_001",
                "도박 중독자 월평균 손실 금액은 약 200만원이다",
                "GAMBLING", "한국도박문제관리센터, 2023", "direct");

        addData("gambling_002",
                "도박 중독 치료비용은 연간 약 500만원이다",
                "GAMBLING", "중독관리통합지원센터, 2023", "health");

        addData("gambling_003",
                "주식 투기로 인한 개인 평균 손실은 연 300만원이다",
                "GAMBLING", "금융감독원, 2023", "direct");

        addData("gambling_004",
                "도박 중독자의 가정파탄 비율은 60%이다",
                "GAMBLING", "한국도박문제관리센터, 2023", "psychological");

        addData("gambling_005",
                "도박 충동 1회 참을 시 평균 5만원 절약 효과가 있다",
                "GAMBLING", "중독관리통합지원센터, 2023", "direct");
    }

    /**
     * 데이터 추가 헬퍼 메서드
     */
    private void addData(String id, String content, String category, String source, String costType) {
        try {
            chromaService.addDocument(id, content, category, source, costType);
        } catch (Exception e) {
            log.warn("데이터 추가 실패 (이미 존재할 수 있음): {} - {}", id, e.getMessage());
        }
    }
}