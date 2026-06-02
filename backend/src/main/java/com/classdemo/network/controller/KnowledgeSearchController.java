package com.classdemo.network.controller;

import com.classdemo.network.common.ApiResponse;
import com.classdemo.network.dto.KnowledgeSearchResponse;
import com.classdemo.network.service.KnowledgeSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 跨层知识检索 API（PRD §7.6.11、EXT-04）。
 */
@RestController
@RequestMapping("/knowledge")
public class KnowledgeSearchController {

    private final KnowledgeSearchService knowledgeSearchService;

    public KnowledgeSearchController(KnowledgeSearchService knowledgeSearchService) {
        this.knowledgeSearchService = knowledgeSearchService;
    }

    @GetMapping("/search")
    public ApiResponse<KnowledgeSearchResponse> search(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.ok(knowledgeSearchService.search(keyword, page, pageSize));
    }
}
