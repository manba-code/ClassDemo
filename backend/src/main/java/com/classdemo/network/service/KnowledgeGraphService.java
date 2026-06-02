package com.classdemo.network.service;

import com.classdemo.network.dto.KnowledgeGraphDto;
import com.classdemo.network.dto.KnowledgeGraphKnowledgeNodeDto;
import com.classdemo.network.dto.KnowledgeGraphLayerNodeDto;
import com.classdemo.network.dto.KnowledgePointDto;
import com.classdemo.network.entity.LayerInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识图谱聚合服务（PRD §7.6.10、阶段 E）。
 */
@Service
public class KnowledgeGraphService {

    private static final String ROOT_NAME = "计算机网络";
    private static final String KNOWLEDGE_TYPE = "knowledge";

    private final LayerInfoService layerInfoService;
    private final KnowledgePointService knowledgePointService;

    public KnowledgeGraphService(LayerInfoService layerInfoService,
                                 KnowledgePointService knowledgePointService) {
        this.layerInfoService = layerInfoService;
        this.knowledgePointService = knowledgePointService;
    }

    public KnowledgeGraphDto aggregate() {
        List<KnowledgeGraphLayerNodeDto> layers = layerInfoService.listAll().stream()
                .map(this::buildLayerNode)
                .toList();
        return new KnowledgeGraphDto(ROOT_NAME, layers);
    }

    private KnowledgeGraphLayerNodeDto buildLayerNode(LayerInfo layer) {
        List<KnowledgeGraphKnowledgeNodeDto> knowledgeNodes = knowledgePointService
                .listAll(layer.getLayerKey()).stream()
                .map(dto -> toKnowledgeNode(dto, layer.getLayerKey()))
                .toList();
        return new KnowledgeGraphLayerNodeDto(layer.getLayerName(), layer.getLayerKey(), knowledgeNodes);
    }

    private KnowledgeGraphKnowledgeNodeDto toKnowledgeNode(KnowledgePointDto dto, String layerKey) {
        return new KnowledgeGraphKnowledgeNodeDto(
                dto.id(),
                dto.title(),
                KNOWLEDGE_TYPE,
                layerKey,
                dto.content()
        );
    }
}
