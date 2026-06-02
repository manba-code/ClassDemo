package com.classdemo.network.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdemo.network.common.LayerKey;
import com.classdemo.network.dto.KnowledgeSearchItemDto;
import com.classdemo.network.dto.KnowledgeSearchResponse;
import com.classdemo.network.dto.PaginationDto;
import com.classdemo.network.entity.KnowledgePoint;
import com.classdemo.network.mapper.KnowledgePointMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 跨五层知识点联合检索（EXT-04）。
 */
@Service
public class KnowledgeSearchService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;
    private static final int SNIPPET_MAX = 200;

    private final KnowledgePointRouter router;
    private final KnowledgePointMapper knowledgePointMapper;

    public KnowledgeSearchService(KnowledgePointRouter router, KnowledgePointMapper knowledgePointMapper) {
        this.router = router;
        this.knowledgePointMapper = knowledgePointMapper;
    }

    public KnowledgeSearchResponse search(String keyword, Integer page, Integer pageSize) {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("keyword 不能为空");
        }

        String kw = keyword.trim();
        int resolvedPage = normalizePage(page);
        int resolvedPageSize = normalizePageSize(pageSize);

        List<KnowledgeSearchItemDto> allMatches = new ArrayList<>();

        for (LayerKey layer : LayerKey.values()) {
            List<KnowledgePoint> points = router.execute(layer.getKey(), mapper -> {
                LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
                wrapper.and(w -> w
                        .like(KnowledgePoint::getTitle, kw)
                        .or().like(KnowledgePoint::getContent, kw)
                        .or().like(KnowledgePoint::getTags, kw));
                wrapper.orderByAsc(KnowledgePoint::getSortOrder)
                        .orderByDesc(KnowledgePoint::getId);
                return mapper.selectList(wrapper);
            });

            for (KnowledgePoint point : points) {
                allMatches.add(toSearchItem(layer, point));
            }
        }

        allMatches.sort(Comparator
                .comparing(KnowledgeSearchItemDto::sortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(KnowledgeSearchItemDto::id, Comparator.reverseOrder()));

        long total = allMatches.size();
        int fromIndex = (resolvedPage - 1) * resolvedPageSize;
        int toIndex = Math.min(fromIndex + resolvedPageSize, allMatches.size());

        List<KnowledgeSearchItemDto> pageList = fromIndex >= allMatches.size()
                ? List.of()
                : allMatches.subList(fromIndex, toIndex);

        int totalPages = resolvedPageSize == 0
                ? 0
                : (int) Math.ceil((double) total / resolvedPageSize);

        PaginationDto pagination = new PaginationDto(
                resolvedPage,
                resolvedPageSize,
                total,
                totalPages
        );

        return new KnowledgeSearchResponse(kw, pageList, pagination);
    }

    private KnowledgeSearchItemDto toSearchItem(LayerKey layer, KnowledgePoint point) {
        return new KnowledgeSearchItemDto(
                layer.getKey(),
                layer.getDisplayName(),
                point.getId(),
                point.getTitle(),
                buildSnippet(point.getContent()),
                point.getTags() != null ? point.getTags() : "",
                point.getSortOrder()
        );
    }

    private String buildSnippet(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String trimmed = content.trim().replaceAll("\\s+", " ");
        if (trimmed.length() <= SNIPPET_MAX) {
            return trimmed;
        }
        return trimmed.substring(0, SNIPPET_MAX) + "…";
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
