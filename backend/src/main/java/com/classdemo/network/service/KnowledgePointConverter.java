package com.classdemo.network.service;

import com.classdemo.network.dto.KnowledgePointDto;
import com.classdemo.network.entity.KnowledgePoint;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 知识点实体与 API DTO 转换。
 */
@Component
public class KnowledgePointConverter {

    public KnowledgePointDto toDto(KnowledgePoint entity) {
        return new KnowledgePointDto(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getTags(),
                entity.getSortOrder(),
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getUpdatedAt())
        );
    }

    private Instant toInstant(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
