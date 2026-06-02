package com.classdemo.network.common;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 五层分层枚举与知识点表路由（PRD §7.4）。
 */
public enum LayerKey {

    APPLICATION("application", "knowledge_application", "应用层"),
    TRANSPORT("transport", "knowledge_transport", "传输层"),
    NETWORK("network", "knowledge_network", "网络层"),
    DATALINK("datalink", "knowledge_datalink", "数据链路层"),
    PHYSICAL("physical", "knowledge_physical", "物理层");

    private static final Map<String, LayerKey> BY_KEY = Arrays.stream(values())
            .collect(Collectors.toMap(LayerKey::getKey, Function.identity()));

    private final String key;
    private final String tableName;
    private final String displayName;

    LayerKey(String key, String tableName, String displayName) {
        this.key = key;
        this.tableName = tableName;
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public String getTableName() {
        return tableName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static LayerKey from(String layerKey) {
        if (layerKey == null || layerKey.isBlank()) {
            throw new InvalidLayerKeyException(String.valueOf(layerKey));
        }
        LayerKey resolved = BY_KEY.get(layerKey.trim());
        if (resolved == null) {
            throw new InvalidLayerKeyException(layerKey);
        }
        return resolved;
    }
}
