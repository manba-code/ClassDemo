package com.classdemo.network.controller;

import com.classdemo.network.common.ApiResponse;
import com.classdemo.network.dto.LayerInfoDto;
import com.classdemo.network.dto.LayerUpdateRequest;
import com.classdemo.network.entity.LayerInfo;
import com.classdemo.network.service.LayerInfoConverter;
import com.classdemo.network.service.LayerInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分层元数据 API（PRD §7.6.2～§7.6.4、阶段 C）。
 */
@RestController
@RequestMapping("/layers")
public class LayerController {

    private final LayerInfoService layerInfoService;
    private final LayerInfoConverter layerInfoConverter;

    public LayerController(LayerInfoService layerInfoService, LayerInfoConverter layerInfoConverter) {
        this.layerInfoService = layerInfoService;
        this.layerInfoConverter = layerInfoConverter;
    }

    @GetMapping
    public ApiResponse<List<LayerInfoDto>> listAll() {
        List<LayerInfoDto> layers = layerInfoService.listAll().stream()
                .map(layerInfoConverter::toDto)
                .toList();
        return ApiResponse.ok(layers);
    }

    @GetMapping("/{layerKey}")
    public ApiResponse<LayerInfoDto> getOne(@PathVariable String layerKey) {
        LayerInfo info = layerInfoService.getByLayerKey(layerKey);
        return ApiResponse.ok(layerInfoConverter.toDto(info));
    }

    @PutMapping("/{layerKey}")
    public ApiResponse<LayerInfoDto> update(
            @PathVariable String layerKey,
            @RequestBody LayerUpdateRequest request) {
        LayerInfo updated = layerInfoService.update(layerKey, request);
        return ApiResponse.ok(layerInfoConverter.toDto(updated));
    }
}
