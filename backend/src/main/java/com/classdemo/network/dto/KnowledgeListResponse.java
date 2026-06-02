package com.classdemo.network.dto;

import java.util.List;

/**
 * 知识点分页列表响应（PRD §7.6.5、D-06）。
 */
public record KnowledgeListResponse(
        List<KnowledgePointDto> list,
        PaginationDto pagination
) {
}
