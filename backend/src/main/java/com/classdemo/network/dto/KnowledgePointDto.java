package com.classdemo.network.dto;

import java.time.Instant;

/**
 * 知识点响应（PRD §7.6.5～§7.6.6）。
 */
public record KnowledgePointDto(
        Long id,
        String title,
        String content,
        String tags,
        Integer sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
