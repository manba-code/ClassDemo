package com.classdemo.network.controller;

import com.classdemo.network.common.ApiResponse;
import com.classdemo.network.dto.KnowledgeCreateRequest;
import com.classdemo.network.dto.KnowledgeDeleteResponse;
import com.classdemo.network.dto.KnowledgeListResponse;
import com.classdemo.network.dto.KnowledgePointDto;
import com.classdemo.network.dto.KnowledgeUpdateRequest;
import com.classdemo.network.service.KnowledgePointService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识点 CRUD API（PRD §7.6.5～§7.6.9、阶段 D）。
 */
@RestController
@RequestMapping("/layers/{layerKey}/knowledge")
public class KnowledgeController {

    private final KnowledgePointService knowledgePointService;

    public KnowledgeController(KnowledgePointService knowledgePointService) {
        this.knowledgePointService = knowledgePointService;
    }

    @GetMapping
    public ApiResponse<KnowledgeListResponse> list(
            @PathVariable String layerKey,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(knowledgePointService.list(layerKey, page, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgePointDto> getById(
            @PathVariable String layerKey,
            @PathVariable Long id) {
        return ApiResponse.ok(knowledgePointService.getById(layerKey, id));
    }

    @PostMapping
    public ApiResponse<KnowledgePointDto> create(
            @PathVariable String layerKey,
            @Valid @RequestBody KnowledgeCreateRequest request) {
        return ApiResponse.ok(knowledgePointService.create(layerKey, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgePointDto> update(
            @PathVariable String layerKey,
            @PathVariable Long id,
            @RequestBody KnowledgeUpdateRequest request) {
        return ApiResponse.ok(knowledgePointService.update(layerKey, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<KnowledgeDeleteResponse> delete(
            @PathVariable String layerKey,
            @PathVariable Long id) {
        return ApiResponse.ok(knowledgePointService.delete(layerKey, id));
    }
}
