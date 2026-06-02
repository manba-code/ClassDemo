package com.classdemo.network.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 新增知识点请求体（PRD §7.6.7、D-03）。
 */
public record KnowledgeCreateRequest(
        @NotBlank(message = "不能为空")
        String title,
        @NotBlank(message = "不能为空")
        String content,
        String tags,
        Integer sortOrder
) {
}
