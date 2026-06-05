package com.classdemo.network.controller;

import com.classdemo.network.dto.KnowledgeGraphDto;
import com.classdemo.network.dto.KnowledgeGraphKnowledgeNodeDto;
import com.classdemo.network.dto.KnowledgeGraphLayerNodeDto;
import com.classdemo.network.service.KnowledgeGraphService;
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

@DisplayName("知识图谱控制器单元测试")
@ExtendWith(MockitoExtension.class)
class KnowledgeGraphControllerTest {

    @Mock
    private KnowledgeGraphService knowledgeGraphService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new KnowledgeGraphController(knowledgeGraphService))
                .build();
    }

    @Test
    @DisplayName("GET /knowledge-graph - 聚合成功")
    void testAggregate_Success() throws Exception {
        KnowledgeGraphDto graph = new KnowledgeGraphDto(
                "计算机网络",
                List.of(new KnowledgeGraphLayerNodeDto(
                        "应用层", "application",
                        List.of(new KnowledgeGraphKnowledgeNodeDto(
                                1L, "TCP", "knowledge", "application", "传输控制协议"
                        ))
                ))
        );
        when(knowledgeGraphService.aggregate()).thenReturn(graph);

        mockMvc.perform(get("/knowledge-graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("计算机网络"))
                .andExpect(jsonPath("$.data.children[0].layerKey").value("application"))
                .andExpect(jsonPath("$.data.children[0].children[0].name").value("TCP"));
    }
}
