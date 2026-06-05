package com.classdemo.network.service;

import com.classdemo.network.common.InvalidLayerKeyException;
import com.classdemo.network.common.KnowledgeTableContext;
import com.classdemo.network.common.LayerKey;
import com.classdemo.network.mapper.KnowledgePointMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("知识点分表路由单元测试")
@ExtendWith(MockitoExtension.class)
class KnowledgePointRouterTest {

    @Mock
    private KnowledgePointMapper knowledgePointMapper;

    @InjectMocks
    private KnowledgePointRouter knowledgePointRouter;

    @AfterEach
    void tearDown() {
        KnowledgeTableContext.clear();
    }

    @Test
    @DisplayName("解析layerKey - 成功")
    void testResolve_Success() {
        LayerKey layer = knowledgePointRouter.resolve("network");
        assertEquals(LayerKey.NETWORK, layer);
    }

    @Test
    @DisplayName("解析layerKey - 无效key抛出异常")
    void testResolve_InvalidKey() {
        assertThrows(InvalidLayerKeyException.class,
                () -> knowledgePointRouter.resolve("invalid"));
    }

    @Test
    @DisplayName("execute - 设置并清理表上下文")
    void testExecute_SetsAndClearsContext() {
        // Given
        when(knowledgePointMapper.selectCount(null)).thenReturn(3L);

        // When
        Long result = knowledgePointRouter.execute("datalink", mapper -> mapper.selectCount(null));

        // Then
        assertEquals(3L, result);
        assertNull(KnowledgeTableContext.get());
        verify(knowledgePointMapper).selectCount(null);
    }

    @Test
    @DisplayName("mapper - 返回注入的Mapper实例")
    void testMapper_ReturnsInjectedInstance() {
        // When
        KnowledgePointMapper result = knowledgePointRouter.mapper();

        // Then
        assertEquals(knowledgePointMapper, result);
    }

    @Test
    @DisplayName("run - 执行无返回值操作")
    void testRun_Success() {
        // When
        knowledgePointRouter.run("physical", mapper -> mapper.deleteById(1L));

        // Then
        verify(knowledgePointMapper).deleteById(1L);
        assertNull(KnowledgeTableContext.get());
    }
}
