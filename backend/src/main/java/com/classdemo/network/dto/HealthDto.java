package com.classdemo.network.dto;

import java.time.Instant;

/**
 * 健康检查响应数据（PRD §7.6.1）。
 */
public record HealthDto(String status, Instant timestamp) {
}
