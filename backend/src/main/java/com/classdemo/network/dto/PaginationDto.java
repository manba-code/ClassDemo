package com.classdemo.network.dto;

/**
 * 分页元数据（PRD §7.2、§7.6.5）。
 */
public record PaginationDto(
        int page,
        int pageSize,
        long total,
        int totalPages
) {
}
