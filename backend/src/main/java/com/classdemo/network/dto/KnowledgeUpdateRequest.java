package com.classdemo.network.dto;

/**
 * 更新知识点请求体（PRD §7.6.8、D-04）。
 */
public record KnowledgeUpdateRequest(
        String title,
        String content,
        String tags,
        Integer sortOrder
) {

    public boolean hasAnyField() {
        return title != null || content != null || tags != null || sortOrder != null;
    }
}
