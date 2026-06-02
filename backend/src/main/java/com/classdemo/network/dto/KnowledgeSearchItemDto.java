package com.classdemo.network.dto;

/**
 * 跨层知识检索单条结果（PRD §7.6.11、EXT-04）。
 */
public record KnowledgeSearchItemDto(
        String layerKey,
        String layerName,
        Long id,
        String title,
        String snippet,
        String tags,
        Integer sortOrder
) {
}
