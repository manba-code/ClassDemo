package com.classdemo.network.dto;

import java.util.List;

/**
 * 知识图谱根节点（PRD §7.6.10、阶段 E）。
 */
public record KnowledgeGraphDto(
        String name,
        List<KnowledgeGraphLayerNodeDto> children
) {
}
