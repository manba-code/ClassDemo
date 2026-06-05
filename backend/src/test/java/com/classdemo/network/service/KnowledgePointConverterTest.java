package com.classdemo.network.service;

import com.classdemo.network.dto.KnowledgePointDto;
import com.classdemo.network.entity.KnowledgePoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("知识点转换器单元测试")
class KnowledgePointConverterTest {

    private final KnowledgePointConverter converter = new KnowledgePointConverter();

    @Test
    @DisplayName("实体转DTO - 成功")
    void testToDto_Success() {
        // Given
        KnowledgePoint entity = new KnowledgePoint();
        entity.setId(1L);
        entity.setTitle("IP");
        entity.setContent("网际协议");
        entity.setTags("网络层");
        entity.setSortOrder(3);
        entity.setCreatedAt(LocalDateTime.of(2026, 1, 1, 8, 0));
        entity.setUpdatedAt(LocalDateTime.of(2026, 1, 2, 9, 0));

        // When
        KnowledgePointDto dto = converter.toDto(entity);

        // Then
        assertAll(
                () -> assertEquals(1L, dto.id()),
                () -> assertEquals("IP", dto.title()),
                () -> assertEquals("网际协议", dto.content()),
                () -> assertEquals("网络层", dto.tags()),
                () -> assertEquals(3, dto.sortOrder()),
                () -> assertEquals(LocalDateTime.of(2026, 1, 1, 8, 0)
                        .atZone(java.time.ZoneId.systemDefault()).toInstant(), dto.createdAt()),
                () -> assertEquals(LocalDateTime.of(2026, 1, 2, 9, 0)
                        .atZone(java.time.ZoneId.systemDefault()).toInstant(), dto.updatedAt())
        );
    }

    @Test
    @DisplayName("实体转DTO - 时间为null")
    void testToDto_NullDateTime() {
        // Given
        KnowledgePoint entity = new KnowledgePoint();
        entity.setId(2L);
        entity.setTitle("ARP");

        // When
        KnowledgePointDto dto = converter.toDto(entity);

        // Then
        assertAll(
                () -> assertNull(dto.createdAt()),
                () -> assertNull(dto.updatedAt())
        );
    }
}
