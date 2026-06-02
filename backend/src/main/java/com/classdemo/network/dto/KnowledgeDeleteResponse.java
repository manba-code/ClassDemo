package com.classdemo.network.dto;

/**
 * 删除知识点响应（PRD §7.6.9、D-05）。
 */
public record KnowledgeDeleteResponse(
        Long id,
        boolean deleted
) {
}
