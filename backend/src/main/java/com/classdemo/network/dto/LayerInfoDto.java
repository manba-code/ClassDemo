package com.classdemo.network.dto;

import java.time.Instant;
import java.util.List;

/**
 * 分层元数据响应（PRD §7.6.2、§7.6.3）。
 */
public record LayerInfoDto(
        Integer id,
        String layerKey,
        String layerName,
        String mainFunction,
        List<String> protocols,
        String devicesUnits,
        Instant updatedAt
) {
}
