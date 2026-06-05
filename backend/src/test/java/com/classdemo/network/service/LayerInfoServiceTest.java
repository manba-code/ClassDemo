package com.classdemo.network.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdemo.network.common.ResourceNotFoundException;
import com.classdemo.network.dto.LayerUpdateRequest;
import com.classdemo.network.entity.LayerInfo;
import com.classdemo.network.mapper.LayerInfoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("分层元数据服务单元测试")
@ExtendWith(MockitoExtension.class)
class LayerInfoServiceTest {

    @Mock
    private LayerInfoMapper layerInfoMapper;

    @InjectMocks
    private LayerInfoService layerInfoService;

    private LayerInfoConverter layerInfoConverter;

    @BeforeEach
    void setUp() {
        layerInfoConverter = new LayerInfoConverter(new ObjectMapper());
        layerInfoService = new LayerInfoService(layerInfoMapper, layerInfoConverter);
    }

    private LayerInfo createLayerInfo() {
        LayerInfo info = new LayerInfo();
        info.setId(1);
        info.setLayerKey("application");
        info.setLayerName("应用层");
        info.setMainFunction("为用户提供服务");
        info.setProtocols("[\"HTTP\",\"DNS\"]");
        info.setDevicesUnits("主机");
        info.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return info;
    }

    @Test
    @DisplayName("查询全部分层 - 成功")
    void testListAll_Success() {
        // Given
        when(layerInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(createLayerInfo()));

        // When
        List<LayerInfo> result = layerInfoService.listAll();

        // Then
        assertEquals(1, result.size());
        assertEquals("application", result.get(0).getLayerKey());
    }

    @Nested
    @DisplayName("根据layerKey查询")
    class GetByLayerKeyTest {

        @Test
        @DisplayName("根据layerKey查询 - 存在")
        void testGetByLayerKey_Exists() {
            // Given
            when(layerInfoMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(createLayerInfo());

            // When
            LayerInfo result = layerInfoService.getByLayerKey("application");

            // Then
            assertEquals("应用层", result.getLayerName());
        }

        @Test
        @DisplayName("根据layerKey查询 - 不存在抛出异常")
        void testGetByLayerKey_NotExists() {
            // Given
            when(layerInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // When & Then
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> layerInfoService.getByLayerKey("application")
            );
            assertTrue(exception.getMessage().contains("分层元数据不存在"));
        }
    }

    @Nested
    @DisplayName("更新分层元数据")
    class UpdateTest {

        @Test
        @DisplayName("更新分层元数据 - 成功")
        void testUpdate_Success() {
            // Given
            LayerInfo existing = createLayerInfo();
            when(layerInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
            when(layerInfoMapper.updateById(any(LayerInfo.class))).thenReturn(1);
            LayerUpdateRequest request = new LayerUpdateRequest(
                    "新的主要功能",
                    List.of("HTTP", "FTP"),
                    "新设备"
            );

            // When
            LayerInfo result = layerInfoService.update("application", request);

            // Then
            ArgumentCaptor<LayerInfo> captor = ArgumentCaptor.forClass(LayerInfo.class);
            verify(layerInfoMapper).updateById(captor.capture());
            LayerInfo updated = captor.getValue();
            assertAll(
                    () -> assertEquals("新的主要功能", result.getMainFunction()),
                    () -> assertEquals("新设备", result.getDevicesUnits()),
                    () -> assertTrue(updated.getProtocols().contains("HTTP")),
                    () -> assertNotNull(updated.getUpdatedAt())
            );
        }

        @Test
        @DisplayName("更新分层元数据 - 请求为空抛出异常")
        void testUpdate_EmptyRequest() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> layerInfoService.update("application",
                            new LayerUpdateRequest(null, null, null))
            );
            assertTrue(exception.getMessage().contains("至少需提供"));
            verify(layerInfoMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("更新分层元数据 - 分层不存在抛出异常")
        void testUpdate_LayerNotExists() {
            // Given
            when(layerInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            LayerUpdateRequest request = new LayerUpdateRequest("功能", null, null);

            // When & Then
            assertThrows(ResourceNotFoundException.class,
                    () -> layerInfoService.update("application", request));
        }

        @Test
        @DisplayName("更新分层元数据 - request为null抛出异常")
        void testUpdate_NullRequest() {
            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> layerInfoService.update("application", null));
            verify(layerInfoMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("更新分层元数据 - 仅更新mainFunction")
        void testUpdate_OnlyMainFunction() {
            // Given
            LayerInfo existing = createLayerInfo();
            when(layerInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
            when(layerInfoMapper.updateById(any(LayerInfo.class))).thenReturn(1);

            // When
            LayerInfo result = layerInfoService.update("application",
                    new LayerUpdateRequest("仅更新功能", null, null));

            // Then
            assertEquals("仅更新功能", result.getMainFunction());
            assertEquals("主机", result.getDevicesUnits());
        }

        @Test
        @DisplayName("更新分层元数据 - 仅更新protocols")
        void testUpdate_OnlyProtocols() {
            // Given
            LayerInfo existing = createLayerInfo();
            when(layerInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
            when(layerInfoMapper.updateById(any(LayerInfo.class))).thenReturn(1);

            // When
            LayerInfo result = layerInfoService.update("application",
                    new LayerUpdateRequest(null, List.of("SMTP"), null));

            // Then
            assertTrue(result.getProtocols().contains("SMTP"));
        }

        @Test
        @DisplayName("更新分层元数据 - 仅更新devicesUnits")
        void testUpdate_OnlyDevicesUnits() {
            // Given
            LayerInfo existing = createLayerInfo();
            when(layerInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
            when(layerInfoMapper.updateById(any(LayerInfo.class))).thenReturn(1);

            // When
            LayerInfo result = layerInfoService.update("application",
                    new LayerUpdateRequest(null, null, "路由器"));

            // Then
            assertEquals("路由器", result.getDevicesUnits());
        }
    }
}
