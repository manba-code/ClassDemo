package com.classdemo.network.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdemo.network.common.ResourceNotFoundException;
import com.classdemo.network.entity.KnowledgePoint;
import com.classdemo.network.mapper.KnowledgePointMapper;
import org.springframework.stereotype.Service;

/**
 * 知识点服务：通过 {@link KnowledgePointRouter} 访问对应层的物理表。
 */
@Service
public class KnowledgePointService {

    private final KnowledgePointRouter router;

    public KnowledgePointService(KnowledgePointRouter router) {
        this.router = router;
    }

    public KnowledgePoint getById(String layerKey, Long id) {
        return router.execute(layerKey, mapper -> {
            KnowledgePoint point = mapper.selectById(id);
            if (point == null) {
                throw new ResourceNotFoundException("知识点不存在: id=" + id);
            }
            return point;
        });
    }

    public long countByLayer(String layerKey) {
        return router.execute(layerKey, mapper ->
                mapper.selectCount(new LambdaQueryWrapper<KnowledgePoint>()));
    }
}
