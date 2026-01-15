package org.example.badhabitzero.domain.habit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.badhabitzero.domain.habit.dto.HabitRequestDto;
import org.example.badhabitzero.domain.habit.dto.HabitResponseDto;
import org.example.badhabitzero.domain.habit.entity.Habit;
import org.example.badhabitzero.domain.habit.repository.HabitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HabitService {
    private final HabitRepository habitRepository;

    /**
     * 악습 등록
     */
    @Transactional
    public HabitResponseDto createHabit(Long userId, HabitRequestDto request) {
        // 순서 설정 (기존 악습 개수 + 1)
        int currentCount = habitRepository.countByUserIdAndIsActiveTrue(userId);

        Habit habit = request.toEntity(userId);
        habit.updateDisplayOrder(currentCount + 1);

        Habit savedHabit = habitRepository.save(habit);
        log.info("악습 등록 완료: userId={}, habitId={}, name={}", userId, savedHabit.getId(), savedHabit.getName());

        return HabitResponseDto.from(savedHabit);
    }

    /**
     * 내 악습 목록 조회
     */
    public List<HabitResponseDto> getMyHabits(Long userId) {
        List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrueOrderByDisplayOrderAsc(userId);
        return habits.stream()
                .map(HabitResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 악습 상세 조회
     */
    public HabitResponseDto getHabit(Long userId, Long habitId) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new IllegalArgumentException("악습을 찾을 수 없습니다."));
        return HabitResponseDto.from(habit);
    }

    /**
     * 악습 수정
     */
    @Transactional
    public HabitResponseDto updateHabit(Long userId, Long habitId, HabitRequestDto request) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new IllegalArgumentException("악습을 찾을 수 없습니다."));

        habit.update(
                request.getName(),
                Habit.Category.valueOf(request.getCategory()),
                request.getReason(),
                request.getIcon(),
                request.getBaseValue()
        );

        // AI 값도 업데이트 (있으면)
        if (request.getAiValue() != null) {
            habit.updateAiValue(request.getAiValue(), request.getAiDescription());
        }

        log.info("악습 수정 완료: userId={}, habitId={}", userId, habitId);
        return HabitResponseDto.from(habit);
    }

    /**
     * 악습 삭제 (비활성화)
     */
    @Transactional
    public void deleteHabit(Long userId, Long habitId) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new IllegalArgumentException("악습을 찾을 수 없습니다."));

        habit.updateActive(false);
        log.info("악습 삭제(비활성화) 완료: userId={}, habitId={}", userId, habitId);
    }

    /**
     * AI 가치 업데이트
     */
    @Transactional
    public HabitResponseDto updateAiValue(Long userId, Long habitId, Integer aiValue, String aiDescription) {
        Habit habit = habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new IllegalArgumentException("악습을 찾을 수 없습니다."));

        habit.updateAiValue(aiValue, aiDescription);
        log.info("AI 가치 업데이트: userId={}, habitId={}, aiValue={}", userId, habitId, aiValue);

        return HabitResponseDto.from(habit);
    }


}
