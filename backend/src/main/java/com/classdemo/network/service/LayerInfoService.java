package com.classdemo.network.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdemo.network.common.LayerKey;
import com.classdemo.network.common.ResourceNotFoundException;
import com.classdemo.network.entity.LayerInfo;
import com.classdemo.network.mapper.LayerInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LayerInfoService {

    private final LayerInfoMapper layerInfoMapper;

    public LayerInfoService(LayerInfoMapper layerInfoMapper) {
        this.layerInfoMapper = layerInfoMapper;
    }

    public List<LayerInfo> listAll() {
        return layerInfoMapper.selectList(
                new LambdaQueryWrapper<LayerInfo>().orderByAsc(LayerInfo::getId));
    }

    public LayerInfo getByLayerKey(String layerKey) {
        LayerKey key = LayerKey.from(layerKey);
        LayerInfo info = layerInfoMapper.selectOne(
                new LambdaQueryWrapper<LayerInfo>().eq(LayerInfo::getLayerKey, key.getKey()));
        if (info == null) {
            throw new ResourceNotFoundException("分层元数据不存在: " + layerKey);
        }
        return info;
    }
}
