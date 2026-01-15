package org.example.badhabitzero.domain.habit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.badhabitzero.domain.habit.dto.HabitRequestDto;
import org.example.badhabitzero.domain.habit.dto.HabitResponseDto;
import org.example.badhabitzero.domain.habit.service.HabitService;
import org.example.badhabitzero.global.common.ApiResponse;
import org.example.badhabitzero.global.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Habit", description = "악습 관리 API")
@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    @Operation(summary = "악습 등록", description = "새로운 악습을 등록합니다")
    @PostMapping
    public ResponseEntity<ApiResponse<HabitResponseDto>> createHabit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody HabitRequestDto request) {

        HabitResponseDto response = habitService.createHabit(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 악습 목록", description = "내 악습 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<List<HabitResponseDto>>> getMyHabits(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<HabitResponseDto> response = habitService.getMyHabits(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "악습 상세 조회", description = "악습 상세 정보를 조회합니다")
    @GetMapping("/{habitId}")
    public ResponseEntity<ApiResponse<HabitResponseDto>> getHabit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long habitId) {

        HabitResponseDto response = habitService.getHabit(userDetails.getUserId(), habitId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "악습 수정", description = "악습 정보를 수정합니다")
    @PutMapping("/{habitId}")
    public ResponseEntity<ApiResponse<HabitResponseDto>> updateHabit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long habitId,
            @RequestBody HabitRequestDto request) {

        HabitResponseDto response = habitService.updateHabit(userDetails.getUserId(), habitId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "악습 삭제", description = "악습을 삭제(비활성화)합니다")
    @DeleteMapping("/{habitId}")
    public ResponseEntity<ApiResponse<String>> deleteHabit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long habitId) {

        habitService.deleteHabit(userDetails.getUserId(), habitId);
        return ResponseEntity.ok(ApiResponse.success("악습이 삭제되었습니다."));
    }
}
