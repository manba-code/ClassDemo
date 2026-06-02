package com.classdemo.network.dto;

import java.util.List;

/**
 * 更新分层元数据请求体（PRD §7.6.4）。
 */
public record LayerUpdateRequest(
        String mainFunction,
        List<String> protocols,
        String devicesUnits
) {

    public boolean hasAnyField() {
        return mainFunction != null || protocols != null || devicesUnits != null;
    }
}
