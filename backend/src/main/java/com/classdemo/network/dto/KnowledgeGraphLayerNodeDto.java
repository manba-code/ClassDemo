package com.classdemo.network.dto;

import java.util.List;

/**
 * 知识图谱分层节点（PRD §7.6.10、阶段 E）。
 */
public record KnowledgeGraphLayerNodeDto(
        String name,
        String layerKey,
        List<KnowledgeGraphKnowledgeNodeDto> children
) {
}
