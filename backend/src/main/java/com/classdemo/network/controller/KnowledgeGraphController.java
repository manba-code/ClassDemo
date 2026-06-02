package com.classdemo.network.controller;

import com.classdemo.network.common.ApiResponse;
import com.classdemo.network.dto.KnowledgeGraphDto;
import com.classdemo.network.service.KnowledgeGraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识图谱聚合 API（PRD §7.6.10、阶段 E）。
 */
@RestController
@RequestMapping("/knowledge-graph")
public class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    public KnowledgeGraphController(KnowledgeGraphService knowledgeGraphService) {
        this.knowledgeGraphService = knowledgeGraphService;
    }

    @GetMapping
    public ApiResponse<KnowledgeGraphDto> aggregate() {
        return ApiResponse.ok(knowledgeGraphService.aggregate());
    }
}
