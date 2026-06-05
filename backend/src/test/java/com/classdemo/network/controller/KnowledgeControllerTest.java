package com.classdemo.network.controller;

import com.classdemo.network.common.ErrorCode;
import com.classdemo.network.config.GlobalExceptionHandler;
import com.classdemo.network.dto.KnowledgeCreateRequest;
import com.classdemo.network.dto.KnowledgeDeleteResponse;
import com.classdemo.network.dto.KnowledgeListResponse;
import com.classdemo.network.dto.KnowledgePointDto;
import com.classdemo.network.dto.KnowledgeUpdateRequest;
import com.classdemo.network.dto.PaginationDto;
import com.classdemo.network.service.KnowledgePointService;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("知识点控制器单元测试")
@ExtendWith(MockitoExtension.class)
class KnowledgeControllerTest {

    private static final String LAYER_KEY = "application";

    @Mock
    private KnowledgePointService knowledgePointService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new KnowledgeController(knowledgePointService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private KnowledgePointDto sampleDto() {
        return new KnowledgePointDto(
                1L, "TCP", "传输控制协议", "网络", 1,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );
    }

    @Test
    @DisplayName("GET /layers/{layerKey}/knowledge - 分页列表成功")
    void testList_Success() throws Exception {
        when(knowledgePointService.list(eq(LAYER_KEY), eq(1), eq(10), eq(null)))
                .thenReturn(new KnowledgeListResponse(
                        List.of(sampleDto()),
                        new PaginationDto(1, 10, 1, 1)
                ));

        mockMvc.perform(get("/layers/{layerKey}/knowledge", LAYER_KEY)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS))
                .andExpect(jsonPath("$.data.list[0].title").value("TCP"));
    }

    @Test
    @DisplayName("GET /layers/{layerKey}/knowledge/{id} - 查询详情成功")
    void testGetById_Success() throws Exception {
        when(knowledgePointService.getById(LAYER_KEY, 1L)).thenReturn(sampleDto());

        mockMvc.perform(get("/layers/{layerKey}/knowledge/{id}", LAYER_KEY, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("TCP"));
    }

    @Test
    @DisplayName("POST /layers/{layerKey}/knowledge - 创建成功")
    void testCreate_Success() throws Exception {
        KnowledgeCreateRequest request = new KnowledgeCreateRequest("DNS", "域名解析", "网络", 1);
        when(knowledgePointService.create(eq(LAYER_KEY), any(KnowledgeCreateRequest.class)))
                .thenReturn(sampleDto());

        mockMvc.perform(post("/layers/{layerKey}/knowledge", LAYER_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("TCP"));
        verify(knowledgePointService).create(eq(LAYER_KEY), any(KnowledgeCreateRequest.class));
    }

    @Test
    @DisplayName("POST /layers/{layerKey}/knowledge - 参数校验失败")
    void testCreate_ValidationFailed() throws Exception {
        KnowledgeCreateRequest request = new KnowledgeCreateRequest("", "内容", null, null);

        mockMvc.perform(post("/layers/{layerKey}/knowledge", LAYER_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST));
    }

    @Test
    @DisplayName("PUT /layers/{layerKey}/knowledge/{id} - 更新成功")
    void testUpdate_Success() throws Exception {
        KnowledgeUpdateRequest request = new KnowledgeUpdateRequest("新标题", null, null, null);
        when(knowledgePointService.update(eq(LAYER_KEY), eq(1L), any(KnowledgeUpdateRequest.class)))
                .thenReturn(sampleDto());

        mockMvc.perform(put("/layers/{layerKey}/knowledge/{id}", LAYER_KEY, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("DELETE /layers/{layerKey}/knowledge/{id} - 删除成功")
    void testDelete_Success() throws Exception {
        when(knowledgePointService.delete(LAYER_KEY, 1L))
                .thenReturn(new KnowledgeDeleteResponse(1L, true));

        mockMvc.perform(delete("/layers/{layerKey}/knowledge/{id}", LAYER_KEY, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deleted").value(true));
    }
}
