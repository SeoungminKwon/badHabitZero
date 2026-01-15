package org.example.badhabitzero.domain.habit.repository;

import org.example.badhabitzero.domain.habit.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    // 사용자의 활성화된 악습 목록 (순서대로)
    List<Habit> findByUserIdAndIsActiveTrueOrderByDisplayOrderAsc(Long userId);

    // 사용자의 모든 악습 목록
    List<Habit> findByUserIdOrderByDisplayOrderAsc(Long userId);

    // 사용자의 특정 악습 조회
    Optional<Habit> findByIdAndUserId(Long id, Long userId);

    // 사용자의 악습 개수
    int countByUserIdAndIsActiveTrue(Long userId);
}
