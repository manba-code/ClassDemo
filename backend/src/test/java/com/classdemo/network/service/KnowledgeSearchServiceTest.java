package com.classdemo.network.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdemo.network.dto.KnowledgeSearchResponse;
import com.classdemo.network.entity.KnowledgePoint;
import com.classdemo.network.mapper.KnowledgePointMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@DisplayName("知识点联合检索服务单元测试")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KnowledgeSearchServiceTest {

    @Mock
    private KnowledgePointRouter router;

    @Mock
    private KnowledgePointMapper knowledgePointMapper;

    @InjectMocks
    private KnowledgeSearchService knowledgeSearchService;

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            Function<KnowledgePointMapper, ?> action = invocation.getArgument(1);
            return action.apply(knowledgePointMapper);
        }).when(router).execute(anyString(), any());
    }

    private KnowledgePoint createPoint(Long id, String title, String content, int sortOrder) {
        KnowledgePoint point = new KnowledgePoint();
        point.setId(id);
        point.setTitle(title);
        point.setContent(content);
        point.setTags("网络");
        point.setSortOrder(sortOrder);
        return point;
    }

    @Test
    @DisplayName("联合检索 - 成功返回匹配结果")
    void testSearch_Success() {
        // Given
        when(knowledgePointMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(createPoint(1L, "TCP协议", "传输控制协议详解", 1)))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList());

        // When
        KnowledgeSearchResponse result = knowledgeSearchService.search("TCP", 1, 10);

        // Then
        assertAll(
                () -> assertEquals("TCP", result.keyword()),
                () -> assertEquals(1, result.list().size()),
                () -> assertEquals("application", result.list().get(0).layerKey()),
                () -> assertEquals("TCP协议", result.list().get(0).title()),
                () -> assertTrue(result.list().get(0).snippet().contains("传输控制协议")),
                () -> assertEquals(1L, result.pagination().total())
        );
    }

    @Test
    @DisplayName("联合检索 - 超长内容截断为摘要")
    void testSearch_LongContentSnippet() {
        // Given
        String longContent = "A".repeat(250);
        when(knowledgePointMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(createPoint(1L, "标题", longContent, 1)))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList());

        // When
        KnowledgeSearchResponse result = knowledgeSearchService.search("标题", 1, 10);

        // Then
        assertEquals(201, result.list().get(0).snippet().length());
        assertTrue(result.list().get(0).snippet().endsWith("…"));
    }

    @Test
    @DisplayName("联合检索 - content为null时摘要为空")
    void testSearch_NullContentSnippet() {
        // Given
        KnowledgePoint point = createPoint(1L, "标题", null, 1);
        point.setTags(null);
        when(knowledgePointMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(point))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList());

        // When
        KnowledgeSearchResponse result = knowledgeSearchService.search("标题", 1, 10);

        // Then
        assertAll(
                () -> assertEquals("", result.list().get(0).snippet()),
                () -> assertEquals("", result.list().get(0).tags())
        );
    }

    @Test
    @DisplayName("联合检索 - content为空白时摘要为空")
    void testSearch_BlankContentSnippet() {
        // Given
        when(knowledgePointMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(createPoint(1L, "标题", "   ", 1)))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList());

        // When
        KnowledgeSearchResponse result = knowledgeSearchService.search("标题", 1, 10);

        // Then
        assertEquals("", result.list().get(0).snippet());
    }

    @Test
    @DisplayName("联合检索 - page为负数时使用默认值1")
    void testSearch_NegativePage() {
        // Given
        when(knowledgePointMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // When
        KnowledgeSearchResponse result = knowledgeSearchService.search("test", -1, 10);

        // Then
        assertEquals(1, result.pagination().page());
    }

    @Test
    @DisplayName("联合检索 - 超出页码返回空列表")
    void testSearch_PageOutOfRange() {
        // Given
        when(knowledgePointMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(createPoint(1L, "TCP", "内容", 1)))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList());

        // When
        KnowledgeSearchResponse result = knowledgeSearchService.search("TCP", 99, 10);

        // Then
        assertTrue(result.list().isEmpty());
        assertEquals(1L, result.pagination().total());
    }

    @Nested
    @DisplayName("参数校验")
    class ValidationTest {

        @Test
        @DisplayName("联合检索 - keyword为空抛出异常")
        void testSearch_EmptyKeyword() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> knowledgeSearchService.search("", 1, 10)
            );
            assertTrue(exception.getMessage().contains("keyword 不能为空"));
        }

        @Test
        @DisplayName("联合检索 - keyword为null抛出异常")
        void testSearch_NullKeyword() {
            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> knowledgeSearchService.search(null, 1, 10));
        }

        @Test
        @DisplayName("联合检索 - 默认分页参数")
        void testSearch_DefaultPagination() {
            // Given
            when(knowledgePointMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            // When
            KnowledgeSearchResponse result = knowledgeSearchService.search("test", null, null);

            // Then
            assertEquals(1, result.pagination().page());
            assertEquals(10, result.pagination().pageSize());
        }

        @Test
        @DisplayName("联合检索 - pageSize超过上限截断为20")
        void testSearch_MaxPageSize() {
            // Given
            when(knowledgePointMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            // When
            KnowledgeSearchResponse result = knowledgeSearchService.search("test", 1, 50);

            // Then
            assertEquals(20, result.pagination().pageSize());
        }
    }
}
