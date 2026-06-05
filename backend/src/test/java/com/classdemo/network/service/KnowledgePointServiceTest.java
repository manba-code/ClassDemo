package com.classdemo.network.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classdemo.network.common.ResourceNotFoundException;
import com.classdemo.network.dto.KnowledgeCreateRequest;
import com.classdemo.network.dto.KnowledgeDeleteResponse;
import com.classdemo.network.dto.KnowledgeListResponse;
import com.classdemo.network.dto.KnowledgePointDto;
import com.classdemo.network.dto.KnowledgeUpdateRequest;
import com.classdemo.network.entity.KnowledgePoint;
import com.classdemo.network.mapper.KnowledgePointMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("知识点服务单元测试")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KnowledgePointServiceTest {

    private static final String LAYER_KEY = "application";

    @Mock
    private KnowledgePointRouter router;

    @Mock
    private KnowledgePointMapper knowledgePointMapper;

    @InjectMocks
    private KnowledgePointService knowledgePointService;

    private final KnowledgePointConverter converter = new KnowledgePointConverter();

    @BeforeEach
    void setUp() {
        knowledgePointService = new KnowledgePointService(router, converter);
        stubRouter();
    }

    private void stubRouter() {
        doAnswer(invocation -> {
            Function<KnowledgePointMapper, ?> action = invocation.getArgument(1);
            return action.apply(knowledgePointMapper);
        }).when(router).execute(anyString(), any());

        doAnswer(invocation -> {
            Consumer<KnowledgePointMapper> action = invocation.getArgument(1);
            action.accept(knowledgePointMapper);
            return null;
        }).when(router).run(anyString(), any());
    }

    private KnowledgePoint createPoint(Long id, String title) {
        KnowledgePoint point = new KnowledgePoint();
        point.setId(id);
        point.setTitle(title);
        point.setContent("内容-" + title);
        point.setTags("tag1");
        point.setSortOrder(1);
        point.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        point.setUpdatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        return point;
    }

    @Nested
    @DisplayName("分页列表查询")
    class ListTest {

        @Test
        @DisplayName("分页列表 - 成功返回数据")
        void testList_Success() {
            // Given
            KnowledgePoint point = createPoint(1L, "TCP");
            Page<KnowledgePoint> page = new Page<>(1, 10);
            page.setRecords(List.of(point));
            page.setTotal(1);
            when(knowledgePointMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(page);

            // When
            KnowledgeListResponse result = knowledgePointService.list(LAYER_KEY, 1, 10, null);

            // Then
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(1, result.list().size()),
                    () -> assertEquals("TCP", result.list().get(0).title()),
                    () -> assertEquals(1, result.pagination().page()),
                    () -> assertEquals(10, result.pagination().pageSize()),
                    () -> assertEquals(1L, result.pagination().total()),
                    () -> assertEquals(1, result.pagination().totalPages())
            );
            verify(router).execute(eq(LAYER_KEY), any());
        }

        @Test
        @DisplayName("分页列表 - 默认分页参数")
        void testList_DefaultPagination() {
            // Given
            Page<KnowledgePoint> page = new Page<>(1, 10);
            page.setRecords(List.of());
            page.setTotal(0);
            when(knowledgePointMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(page);

            // When
            KnowledgeListResponse result = knowledgePointService.list(LAYER_KEY, null, null, "关键词");

            // Then
            assertEquals(1, result.pagination().page());
            assertEquals(10, result.pagination().pageSize());
        }

        @Test
        @DisplayName("分页列表 - pageSize 超过上限时截断为50")
        void testList_MaxPageSize() {
            // Given
            Page<KnowledgePoint> page = new Page<>(1, 50);
            page.setRecords(List.of());
            page.setTotal(0);
            when(knowledgePointMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(page);

            // When
            KnowledgeListResponse result = knowledgePointService.list(LAYER_KEY, 1, 100, null);

            // Then
            assertEquals(50, result.pagination().pageSize());
        }

        @Test
        @DisplayName("分页列表 - page为负数时使用默认值1")
        void testList_NegativePage() {
            // Given
            Page<KnowledgePoint> page = new Page<>(1, 10);
            page.setRecords(List.of());
            page.setTotal(0);
            when(knowledgePointMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(page);

            // When
            KnowledgeListResponse result = knowledgePointService.list(LAYER_KEY, -1, 10, null);

            // Then
            assertEquals(1, result.pagination().page());
        }

        @Test
        @DisplayName("分页列表 - pageSize为负数时使用默认值10")
        void testList_NegativePageSize() {
            // Given
            Page<KnowledgePoint> page = new Page<>(1, 10);
            page.setRecords(List.of());
            page.setTotal(0);
            when(knowledgePointMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(page);

            // When
            KnowledgeListResponse result = knowledgePointService.list(LAYER_KEY, 1, -5, null);

            // Then
            assertEquals(10, result.pagination().pageSize());
        }
    }

    @Nested
    @DisplayName("根据ID查询")
    class GetByIdTest {

        @Test
        @DisplayName("根据ID查询 - 存在")
        void testGetById_Exists() {
            // Given
            KnowledgePoint point = createPoint(1L, "HTTP");
            when(knowledgePointMapper.selectById(1L)).thenReturn(point);

            // When
            KnowledgePointDto result = knowledgePointService.getById(LAYER_KEY, 1L);

            // Then
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(1L, result.id()),
                    () -> assertEquals("HTTP", result.title())
            );
        }

        @Test
        @DisplayName("根据ID查询 - 不存在抛出异常")
        void testGetById_NotExists() {
            // Given
            when(knowledgePointMapper.selectById(999L)).thenReturn(null);

            // When & Then
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> knowledgePointService.getById(LAYER_KEY, 999L)
            );
            assertTrue(exception.getMessage().contains("知识点不存在"));
        }
    }

    @Nested
    @DisplayName("创建知识点")
    class CreateTest {

        @Test
        @DisplayName("创建知识点 - 成功")
        void testCreate_Success() {
            // Given
            KnowledgeCreateRequest request = new KnowledgeCreateRequest("  DNS  ", "  域名解析  ", "网络", 2);
            when(knowledgePointMapper.insert(any(KnowledgePoint.class))).thenAnswer(invocation -> {
                KnowledgePoint point = invocation.getArgument(0);
                point.setId(10L);
                return 1;
            });

            // When
            KnowledgePointDto result = knowledgePointService.create(LAYER_KEY, request);

            // Then
            ArgumentCaptor<KnowledgePoint> captor = ArgumentCaptor.forClass(KnowledgePoint.class);
            verify(knowledgePointMapper).insert(captor.capture());
            KnowledgePoint saved = captor.getValue();
            assertAll(
                    () -> assertEquals("DNS", saved.getTitle()),
                    () -> assertEquals("域名解析", saved.getContent()),
                    () -> assertEquals("网络", saved.getTags()),
                    () -> assertEquals(2, saved.getSortOrder()),
                    () -> assertNotNull(saved.getCreatedAt()),
                    () -> assertNotNull(result),
                    () -> assertEquals(10L, result.id())
            );
        }

        @Test
        @DisplayName("创建知识点 - tags和sortOrder为null时使用默认值")
        void testCreate_NullOptionalFields() {
            // Given
            KnowledgeCreateRequest request = new KnowledgeCreateRequest("UDP", "无连接传输", null, null);
            when(knowledgePointMapper.insert(any(KnowledgePoint.class))).thenReturn(1);

            // When
            knowledgePointService.create(LAYER_KEY, request);

            // Then
            ArgumentCaptor<KnowledgePoint> captor = ArgumentCaptor.forClass(KnowledgePoint.class);
            verify(knowledgePointMapper).insert(captor.capture());
            assertEquals("", captor.getValue().getTags());
            assertEquals(0, captor.getValue().getSortOrder());
        }
    }

    @Nested
    @DisplayName("更新知识点")
    class UpdateTest {

        @Test
        @DisplayName("更新知识点 - 成功更新标题")
        void testUpdate_Success() {
            // Given
            KnowledgePoint existing = createPoint(1L, "旧标题");
            when(knowledgePointMapper.selectById(1L)).thenReturn(existing);
            when(knowledgePointMapper.updateById(any(KnowledgePoint.class))).thenReturn(1);
            KnowledgeUpdateRequest request = new KnowledgeUpdateRequest("新标题", null, null, null);

            // When
            KnowledgePointDto result = knowledgePointService.update(LAYER_KEY, 1L, request);

            // Then
            assertEquals("新标题", result.title());
            verify(knowledgePointMapper).updateById(any(KnowledgePoint.class));
        }

        @Test
        @DisplayName("更新知识点 - 请求为空抛出异常")
        void testUpdate_EmptyRequest() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> knowledgePointService.update(LAYER_KEY, 1L, new KnowledgeUpdateRequest(null, null, null, null))
            );
            assertTrue(exception.getMessage().contains("至少需提供"));
            verify(knowledgePointMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("更新知识点 - 标题为空字符串抛出异常")
        void testUpdate_BlankTitle() {
            // Given
            when(knowledgePointMapper.selectById(1L)).thenReturn(createPoint(1L, "旧标题"));
            KnowledgeUpdateRequest request = new KnowledgeUpdateRequest("  ", null, null, null);

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> knowledgePointService.update(LAYER_KEY, 1L, request));
        }

        @Test
        @DisplayName("更新知识点 - 不存在抛出异常")
        void testUpdate_NotExists() {
            // Given
            when(knowledgePointMapper.selectById(99L)).thenReturn(null);
            KnowledgeUpdateRequest request = new KnowledgeUpdateRequest("标题", null, null, null);

            // When & Then
            assertThrows(ResourceNotFoundException.class,
                    () -> knowledgePointService.update(LAYER_KEY, 99L, request));
        }

        @Test
        @DisplayName("更新知识点 - request为null抛出异常")
        void testUpdate_NullRequest() {
            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> knowledgePointService.update(LAYER_KEY, 1L, null));
            verify(knowledgePointMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("更新知识点 - content为空字符串抛出异常")
        void testUpdate_BlankContent() {
            // Given
            when(knowledgePointMapper.selectById(1L)).thenReturn(createPoint(1L, "标题"));
            KnowledgeUpdateRequest request = new KnowledgeUpdateRequest(null, "  ", null, null);

            // When & Then
            assertThrows(IllegalArgumentException.class,
                    () -> knowledgePointService.update(LAYER_KEY, 1L, request));
        }

        @Test
        @DisplayName("更新知识点 - 成功更新content")
        void testUpdate_ContentSuccess() {
            // Given
            KnowledgePoint existing = createPoint(1L, "标题");
            when(knowledgePointMapper.selectById(1L)).thenReturn(existing);
            when(knowledgePointMapper.updateById(any(KnowledgePoint.class))).thenReturn(1);
            KnowledgeUpdateRequest request = new KnowledgeUpdateRequest(null, "  新内容  ", null, null);

            // When
            KnowledgePointDto result = knowledgePointService.update(LAYER_KEY, 1L, request);

            // Then
            assertEquals("新内容", result.content());
        }

        @Test
        @DisplayName("更新知识点 - 成功更新tags和sortOrder")
        void testUpdate_TagsAndSortOrder() {
            // Given
            KnowledgePoint existing = createPoint(1L, "标题");
            when(knowledgePointMapper.selectById(1L)).thenReturn(existing);
            when(knowledgePointMapper.updateById(any(KnowledgePoint.class))).thenReturn(1);
            KnowledgeUpdateRequest request = new KnowledgeUpdateRequest(null, null, "新标签", 99);

            // When
            KnowledgePointDto result = knowledgePointService.update(LAYER_KEY, 1L, request);

            // Then
            assertAll(
                    () -> assertEquals("新标签", result.tags()),
                    () -> assertEquals(99, result.sortOrder())
            );
        }
    }

    @Nested
    @DisplayName("删除知识点")
    class DeleteTest {

        @Test
        @DisplayName("删除知识点 - 成功")
        void testDelete_Success() {
            // Given
            when(knowledgePointMapper.selectById(1L)).thenReturn(createPoint(1L, "待删除"));
            when(knowledgePointMapper.deleteById(1L)).thenReturn(1);

            // When
            KnowledgeDeleteResponse result = knowledgePointService.delete(LAYER_KEY, 1L);

            // Then
            assertAll(
                    () -> assertEquals(1L, result.id()),
                    () -> assertTrue(result.deleted())
            );
            verify(knowledgePointMapper).deleteById(1L);
        }

        @Test
        @DisplayName("删除知识点 - 不存在抛出异常")
        void testDelete_NotExists() {
            // Given
            when(knowledgePointMapper.selectById(1L)).thenReturn(null);

            // When & Then
            assertThrows(ResourceNotFoundException.class,
                    () -> knowledgePointService.delete(LAYER_KEY, 1L));
            verify(knowledgePointMapper, never()).deleteById(any(Long.class));
        }
    }

    @Nested
    @DisplayName("统计与全量查询")
    class CountAndListAllTest {

        @Test
        @DisplayName("统计层内知识点数量 - 成功")
        void testCountByLayer_Success() {
            // Given
            when(knowledgePointMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            // When
            long count = knowledgePointService.countByLayer(LAYER_KEY);

            // Then
            assertEquals(5L, count);
        }

        @Test
        @DisplayName("查询层内全部知识点 - 成功")
        void testListAll_Success() {
            // Given
            when(knowledgePointMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(createPoint(1L, "A"), createPoint(2L, "B")));

            // When
            List<KnowledgePointDto> result = knowledgePointService.listAll(LAYER_KEY);

            // Then
            assertEquals(2, result.size());
            assertEquals("A", result.get(0).title());
        }
    }
}
