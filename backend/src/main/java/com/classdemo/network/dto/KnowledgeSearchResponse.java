package com.classdemo.network.dto;

import java.util.List;

/**
 * 跨层知识检索响应（PRD §7.6.11、EXT-04）。
 */
public record KnowledgeSearchResponse(
        String keyword,
        List<KnowledgeSearchItemDto> list,
        PaginationDto pagination
) {
}
