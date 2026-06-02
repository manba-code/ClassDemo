package com.classdemo.network.service;

import com.classdemo.network.dto.LayerInfoDto;
import com.classdemo.network.entity.LayerInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

/**
 * layer_info 实体与 API DTO 转换；protocols 在 DB 中以 JSON 数组字符串存储（PRD §6.2、C-05）。
 */
@Component
public class LayerInfoConverter {

    private static final TypeReference<List<String>> PROTOCOL_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public LayerInfoConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LayerInfoDto toDto(LayerInfo entity) {
        return new LayerInfoDto(
                entity.getId(),
                entity.getLayerKey(),
                entity.getLayerName(),
                entity.getMainFunction(),
                parseProtocols(entity.getProtocols()),
                entity.getDevicesUnits(),
                toInstant(entity.getUpdatedAt())
        );
    }

    public String serializeProtocols(List<String> protocols) {
        if (protocols == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(protocols);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("protocols 格式无效");
        }
    }

    List<String> parseProtocols(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[")) {
            try {
                return objectMapper.readValue(trimmed, PROTOCOL_LIST_TYPE);
            } catch (JsonProcessingException ex) {
                throw new IllegalArgumentException("protocols 数据损坏: " + raw);
            }
        }
        return List.of(trimmed.split("\\s*,\\s*"));
    }

    private Instant toInstant(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
