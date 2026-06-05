package com.classdemo.network.service;

import com.classdemo.network.dto.KnowledgeGraphDto;
import com.classdemo.network.dto.KnowledgePointDto;
import com.classdemo.network.entity.LayerInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("知识图谱聚合服务单元测试")
@ExtendWith(MockitoExtension.class)
class KnowledgeGraphServiceTest {

    @Mock
    private LayerInfoService layerInfoService;

    @Mock
    private KnowledgePointService knowledgePointService;

    @InjectMocks
    private KnowledgeGraphService knowledgeGraphService;

    @Test
    @DisplayName("聚合知识图谱 - 成功")
    void testAggregate_Success() {
        // Given
        LayerInfo layer = new LayerInfo();
        layer.setLayerKey("application");
        layer.setLayerName("应用层");

        KnowledgePointDto point = new KnowledgePointDto(
                1L, "HTTP", "超文本传输协议", "web", 1,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );

        when(layerInfoService.listAll()).thenReturn(List.of(layer));
        when(knowledgePointService.listAll("application")).thenReturn(List.of(point));

        // When
        KnowledgeGraphDto result = knowledgeGraphService.aggregate();

        // Then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("计算机网络", result.name()),
                () -> assertEquals(1, result.children().size()),
                () -> assertEquals("应用层", result.children().get(0).name()),
                () -> assertEquals("application", result.children().get(0).layerKey()),
                () -> assertEquals(1, result.children().get(0).children().size()),
                () -> assertEquals("HTTP", result.children().get(0).children().get(0).name()),
                () -> assertEquals("knowledge", result.children().get(0).children().get(0).type()),
                () -> assertEquals("application", result.children().get(0).children().get(0).layerKey())
        );
        verify(layerInfoService).listAll();
        verify(knowledgePointService).listAll("application");
    }

    @Test
    @DisplayName("聚合知识图谱 - 无知识点时返回空知识节点")
    void testAggregate_EmptyKnowledge() {
        // Given
        LayerInfo layer = new LayerInfo();
        layer.setLayerKey("physical");
        layer.setLayerName("物理层");
        when(layerInfoService.listAll()).thenReturn(List.of(layer));
        when(knowledgePointService.listAll("physical")).thenReturn(List.of());

        // When
        KnowledgeGraphDto result = knowledgeGraphService.aggregate();

        // Then
        assertEquals(0, result.children().get(0).children().size());
    }
}
