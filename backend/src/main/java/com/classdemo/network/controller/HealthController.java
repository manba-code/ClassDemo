package com.classdemo.network.controller;

import com.classdemo.network.common.ApiResponse;
import com.classdemo.network.dto.HealthDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * 健康检查（BE-09、PRD §7.6.1）。
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<HealthDto> check() {
        return ApiResponse.ok(new HealthDto("up", Instant.now()));
    }
}
