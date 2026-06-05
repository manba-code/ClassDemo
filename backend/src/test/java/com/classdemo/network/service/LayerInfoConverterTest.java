package com.classdemo.network.service;

import com.classdemo.network.dto.LayerInfoDto;
import com.classdemo.network.entity.LayerInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("分层元数据转换器单元测试")
class LayerInfoConverterTest {

    private LayerInfoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new LayerInfoConverter(new ObjectMapper());
    }

    @Test
    @DisplayName("实体转DTO - JSON格式protocols")
    void testToDto_JsonProtocols() {
        // Given
        LayerInfo entity = new LayerInfo();
        entity.setId(1);
        entity.setLayerKey("transport");
        entity.setLayerName("传输层");
        entity.setMainFunction("端到端通信");
        entity.setProtocols("[\"TCP\",\"UDP\"]");
        entity.setDevicesUnits("主机");
        entity.setUpdatedAt(LocalDateTime.of(2026, 3, 1, 12, 0));

        // When
        LayerInfoDto dto = converter.toDto(entity);

        // Then
        assertAll(
                () -> assertEquals("transport", dto.layerKey()),
                () -> assertEquals(List.of("TCP", "UDP"), dto.protocols()),
                () -> assertNotNullInstant(dto.updatedAt())
        );
    }

    @Test
    @DisplayName("实体转DTO - 逗号分隔protocols")
    void testToDto_CommaSeparatedProtocols() {
        // Given
        LayerInfo entity = new LayerInfo();
        entity.setProtocols("HTTP, DNS , FTP");

        // When
        LayerInfoDto dto = converter.toDto(entity);

        // Then
        assertEquals(List.of("HTTP", "DNS", "FTP"), dto.protocols());
    }

    @Test
    @DisplayName("实体转DTO - protocols为空返回空列表")
    void testToDto_EmptyProtocols() {
        // Given
        LayerInfo entity = new LayerInfo();
        entity.setProtocols("");

        // When
        LayerInfoDto dto = converter.toDto(entity);

        // Then
        assertTrue(dto.protocols().isEmpty());
    }

    @Test
    @DisplayName("序列化protocols - 成功")
    void testSerializeProtocols_Success() {
        // When
        String json = converter.serializeProtocols(List.of("TCP", "UDP"));

        // Then
        assertTrue(json.contains("TCP"));
        assertTrue(json.contains("UDP"));
    }

    @Test
    @DisplayName("序列化protocols - null返回null")
    void testSerializeProtocols_Null() {
        assertEquals(null, converter.serializeProtocols(null));
    }

    @Test
    @DisplayName("解析protocols - 损坏JSON抛出异常")
    void testParseProtocols_InvalidJson() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.toDto(buildEntityWithProtocols("[invalid-json"))
        );
        assertTrue(exception.getMessage().contains("protocols 数据损坏"));
    }

    private LayerInfo buildEntityWithProtocols(String protocols) {
        LayerInfo entity = new LayerInfo();
        entity.setProtocols(protocols);
        return entity;
    }

    private void assertNotNullInstant(java.time.Instant instant) {
        org.junit.jupiter.api.Assertions.assertNotNull(instant);
    }
}
