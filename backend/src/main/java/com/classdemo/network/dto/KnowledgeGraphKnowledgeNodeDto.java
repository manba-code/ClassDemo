package com.classdemo.network.dto;

/**
 * 知识图谱知识点叶子节点（PRD §7.6.10、阶段 E）。
 */
public record KnowledgeGraphKnowledgeNodeDto(
        Long id,
        String name,
        String type,
        String layerKey,
        String content
) {
}
