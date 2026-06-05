package com.classdemo.network.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("分层枚举单元测试")
class LayerKeyTest {

    @Test
    @DisplayName("from - 有效layerKey解析成功")
    void testFrom_ValidKey() {
        assertEquals(LayerKey.APPLICATION, LayerKey.from("application"));
        assertEquals(LayerKey.TRANSPORT, LayerKey.from("transport"));
        assertEquals(LayerKey.NETWORK, LayerKey.from("network"));
        assertEquals(LayerKey.DATALINK, LayerKey.from("datalink"));
        assertEquals(LayerKey.PHYSICAL, LayerKey.from("physical"));
    }

    @Test
    @DisplayName("from - 带空格的有效key")
    void testFrom_TrimmedKey() {
        assertEquals(LayerKey.APPLICATION, LayerKey.from("  application  "));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "invalid", "APP"})
    @DisplayName("from - 无效layerKey抛出异常")
    void testFrom_InvalidKey(String layerKey) {
        assertThrows(InvalidLayerKeyException.class, () -> LayerKey.from(layerKey));
    }

    @Test
    @DisplayName("枚举属性 - 表名与显示名正确")
    void testEnumProperties() {
        assertEquals("knowledge_application", LayerKey.APPLICATION.getTableName());
        assertEquals("应用层", LayerKey.APPLICATION.getDisplayName());
        assertEquals("application", LayerKey.APPLICATION.getKey());
    }
}
