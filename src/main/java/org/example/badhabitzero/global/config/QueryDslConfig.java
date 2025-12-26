package org.example.badhabitzero.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryDslConfig {

    @PersistenceContext  // EntityManager 주입
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        // QueryDSL의 핵심 객체
        // 모든 Repository에서 주입받아 사용
        return new JPAQueryFactory(entityManager);
    }
}