package com.classdemo.network.controller;

import com.classdemo.network.dto.KnowledgeSearchItemDto;
import com.classdemo.network.dto.KnowledgeSearchResponse;
import com.classdemo.network.dto.PaginationDto;
import com.classdemo.network.service.KnowledgeSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("知识检索控制器单元测试")
@ExtendWith(MockitoExtension.class)
class KnowledgeSearchControllerTest {

    @Mock
    private KnowledgeSearchService knowledgeSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new KnowledgeSearchController(knowledgeSearchService))
                .build();
    }

    @Test
    @DisplayName("GET /knowledge/search - 检索成功")
    void testSearch_Success() throws Exception {
        KnowledgeSearchResponse response = new KnowledgeSearchResponse(
                "TCP",
                List.of(new KnowledgeSearchItemDto(
                        "application", "应用层", 1L, "TCP协议", "摘要", "网络", 1
                )),
                new PaginationDto(1, 10, 1, 1)
        );
        when(knowledgeSearchService.search("TCP", 1, 10)).thenReturn(response);

        mockMvc.perform(get("/knowledge/search")
                        .param("keyword", "TCP")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyword").value("TCP"))
                .andExpect(jsonPath("$.data.list[0].title").value("TCP协议"));
    }
}
