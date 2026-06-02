package com.classdemo.network.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classdemo.network.common.ResourceNotFoundException;
import com.classdemo.network.dto.KnowledgeCreateRequest;
import com.classdemo.network.dto.KnowledgeDeleteResponse;
import com.classdemo.network.dto.KnowledgeListResponse;
import com.classdemo.network.dto.KnowledgePointDto;
import com.classdemo.network.dto.KnowledgeUpdateRequest;
import com.classdemo.network.dto.PaginationDto;
import com.classdemo.network.entity.KnowledgePoint;
import com.classdemo.network.mapper.KnowledgePointMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识点 CRUD 服务（PRD §7.6.5～§7.6.9、阶段 D）。
 */
@Service
public class KnowledgePointService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final KnowledgePointRouter router;
    private final KnowledgePointConverter converter;

    public KnowledgePointService(KnowledgePointRouter router, KnowledgePointConverter converter) {
        this.router = router;
        this.converter = converter;
    }

    public KnowledgeListResponse list(String layerKey, Integer page, Integer pageSize, String keyword) {
        int resolvedPage = normalizePage(page);
        int resolvedPageSize = normalizePageSize(pageSize);

        return router.execute(layerKey, mapper -> {
            LambdaQueryWrapper<KnowledgePoint> wrapper = buildKeywordWrapper(keyword);
            wrapper.orderByAsc(KnowledgePoint::getSortOrder)
                    .orderByDesc(KnowledgePoint::getId);

            Page<KnowledgePoint> pageRequest = new Page<>(resolvedPage, resolvedPageSize);
            Page<KnowledgePoint> result = mapper.selectPage(pageRequest, wrapper);

            List<KnowledgePointDto> list = result.getRecords().stream()
                    .map(converter::toDto)
                    .toList();

            int totalPages = resolvedPageSize == 0
                    ? 0
                    : (int) Math.ceil((double) result.getTotal() / resolvedPageSize);

            PaginationDto pagination = new PaginationDto(
                    resolvedPage,
                    resolvedPageSize,
                    result.getTotal(),
                    totalPages
            );

            return new KnowledgeListResponse(list, pagination);
        });
    }

    public KnowledgePointDto getById(String layerKey, Long id) {
        KnowledgePoint point = router.execute(layerKey, mapper -> {
            KnowledgePoint found = mapper.selectById(id);
            if (found == null) {
                throw new ResourceNotFoundException("知识点不存在: id=" + id);
            }
            return found;
        });
        return converter.toDto(point);
    }

    public KnowledgePointDto create(String layerKey, KnowledgeCreateRequest request) {
        KnowledgePoint created = router.execute(layerKey, mapper -> {
            LocalDateTime now = LocalDateTime.now();
            KnowledgePoint point = new KnowledgePoint();
            point.setTitle(request.title().trim());
            point.setContent(request.content().trim());
            point.setTags(request.tags() != null ? request.tags() : "");
            point.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
            point.setCreatedAt(now);
            point.setUpdatedAt(now);
            mapper.insert(point);
            return point;
        });
        return converter.toDto(created);
    }

    public KnowledgePointDto update(String layerKey, Long id, KnowledgeUpdateRequest request) {
        if (request == null || !request.hasAnyField()) {
            throw new IllegalArgumentException("至少需提供 title、content、tags、sortOrder 中的一项");
        }

        KnowledgePoint updated = router.execute(layerKey, mapper -> {
            KnowledgePoint point = mapper.selectById(id);
            if (point == null) {
                throw new ResourceNotFoundException("知识点不存在: id=" + id);
            }
            if (request.title() != null) {
                if (request.title().isBlank()) {
                    throw new IllegalArgumentException("title 不能为空");
                }
                point.setTitle(request.title().trim());
            }
            if (request.content() != null) {
                if (request.content().isBlank()) {
                    throw new IllegalArgumentException("content 不能为空");
                }
                point.setContent(request.content().trim());
            }
            if (request.tags() != null) {
                point.setTags(request.tags());
            }
            if (request.sortOrder() != null) {
                point.setSortOrder(request.sortOrder());
            }
            point.setUpdatedAt(LocalDateTime.now());
            mapper.updateById(point);
            return point;
        });
        return converter.toDto(updated);
    }

    public KnowledgeDeleteResponse delete(String layerKey, Long id) {
        router.run(layerKey, mapper -> {
            KnowledgePoint point = mapper.selectById(id);
            if (point == null) {
                throw new ResourceNotFoundException("知识点不存在: id=" + id);
            }
            mapper.deleteById(id);
        });
        return new KnowledgeDeleteResponse(id, true);
    }

    public long countByLayer(String layerKey) {
        return router.execute(layerKey, mapper ->
                mapper.selectCount(new LambdaQueryWrapper<KnowledgePoint>()));
    }

    private LambdaQueryWrapper<KnowledgePoint> buildKeywordWrapper(String keyword) {
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w
                    .like(KnowledgePoint::getTitle, kw)
                    .or().like(KnowledgePoint::getContent, kw)
                    .or().like(KnowledgePoint::getTags, kw));
        }
        return wrapper;
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
