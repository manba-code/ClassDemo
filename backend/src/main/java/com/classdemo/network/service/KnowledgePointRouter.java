package com.classdemo.network.service;

import com.classdemo.network.common.KnowledgeTableContext;
import com.classdemo.network.common.LayerKey;
import com.classdemo.network.mapper.KnowledgePointMapper;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * 五层知识点分表路由：按 layerKey 切换物理表后执行 Mapper 操作（BE-06）。
 */
@Component
public class KnowledgePointRouter {

    private final KnowledgePointMapper knowledgePointMapper;

    public KnowledgePointRouter(KnowledgePointMapper knowledgePointMapper) {
        this.knowledgePointMapper = knowledgePointMapper;
    }

    public LayerKey resolve(String layerKey) {
        return LayerKey.from(layerKey);
    }

    public KnowledgePointMapper mapper() {
        return knowledgePointMapper;
    }

    public <T> T execute(String layerKey, Function<KnowledgePointMapper, T> action) {
        LayerKey layer = resolve(layerKey);
        try {
            KnowledgeTableContext.set(layer.getTableName());
            return action.apply(knowledgePointMapper);
        } finally {
            KnowledgeTableContext.clear();
        }
    }

    public void run(String layerKey, java.util.function.Consumer<KnowledgePointMapper> action) {
        execute(layerKey, mapper -> {
            action.accept(mapper);
            return null;
        });
    }
}
