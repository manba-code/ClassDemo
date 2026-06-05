package com.classdemo.network.controller;

import com.classdemo.network.dto.LayerInfoDto;
import com.classdemo.network.dto.LayerUpdateRequest;
import com.classdemo.network.entity.LayerInfo;
import com.classdemo.network.service.LayerInfoConverter;
import com.classdemo.network.service.LayerInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("分层元数据控制器单元测试")
@ExtendWith(MockitoExtension.class)
class LayerControllerTest {

    @Mock
    private LayerInfoService layerInfoService;

    @Mock
    private LayerInfoConverter layerInfoConverter;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LayerInfo sampleEntity;
    private LayerInfoDto sampleDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new LayerController(layerInfoService, layerInfoConverter))
                .build();

        sampleEntity = new LayerInfo();
        sampleEntity.setId(1);
        sampleEntity.setLayerKey("application");
        sampleEntity.setLayerName("应用层");
        sampleEntity.setMainFunction("为用户提供服务");
        sampleEntity.setProtocols("[\"HTTP\"]");
        sampleEntity.setDevicesUnits("主机");
        sampleEntity.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));

        sampleDto = new LayerInfoDto(
                1, "application", "应用层", "为用户提供服务",
                List.of("HTTP"), "主机",
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    @Test
    @DisplayName("GET /layers - 查询全部分层")
    void testListAll_Success() throws Exception {
        when(layerInfoService.listAll()).thenReturn(List.of(sampleEntity));
        when(layerInfoConverter.toDto(sampleEntity)).thenReturn(sampleDto);

        mockMvc.perform(get("/layers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].layerKey").value("application"));
    }

    @Test
    @DisplayName("GET /layers/{layerKey} - 查询单个分层")
    void testGetOne_Success() throws Exception {
        when(layerInfoService.getByLayerKey("application")).thenReturn(sampleEntity);
        when(layerInfoConverter.toDto(sampleEntity)).thenReturn(sampleDto);

        mockMvc.perform(get("/layers/{layerKey}", "application"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.layerName").value("应用层"));
    }

    @Test
    @DisplayName("PUT /layers/{layerKey} - 更新分层元数据")
    void testUpdate_Success() throws Exception {
        LayerUpdateRequest request = new LayerUpdateRequest("新功能", List.of("HTTP"), "主机");
        when(layerInfoService.update(eq("application"), any(LayerUpdateRequest.class)))
                .thenReturn(sampleEntity);
        when(layerInfoConverter.toDto(sampleEntity)).thenReturn(sampleDto);

        mockMvc.perform(put("/layers/{layerKey}", "application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.layerKey").value("application"));
    }
}
