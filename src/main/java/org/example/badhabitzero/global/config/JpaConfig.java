package org.example.badhabitzero.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing  // JPA Auditing 기능 활성화 (createdAt, updatedAt 자동 입력)
public class JpaConfig {
}