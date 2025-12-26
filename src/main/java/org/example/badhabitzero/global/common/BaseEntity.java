package org.example.badhabitzero.global.common;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass  // 이 클래스를 상속받는 Entity에 필드가 컬럼으로 추가됨
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 기본 생성자 (JPA 스펙)
@EntityListeners(AuditingEntityListener.class)  // Auditing 이벤트 리스너 등록
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // MySQL AUTO_INCREMENT
    private Long id;

    @CreatedDate  // 엔티티 생성 시 자동으로 현재 시간 입력
    @Column(name = "created_at", nullable = false, updatable = false)  // 수정 불가
    private LocalDateTime createdAt;

    @LastModifiedDate  // 엔티티 수정 시 자동으로 현재 시간 입력
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
