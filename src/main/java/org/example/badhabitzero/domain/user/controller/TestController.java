package org.example.badhabitzero.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Test", description = "테스트 API")  // API 그룹 태그
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Operation(summary = "헬스 체크", description = "서버 상태를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "서버 정상"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "BadHabitZero API is running!"
        ));
    }
}