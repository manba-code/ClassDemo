package com.classdemo.network.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdemo.network.common.LayerKey;
import com.classdemo.network.common.ResourceNotFoundException;
import com.classdemo.network.dto.LayerUpdateRequest;
import com.classdemo.network.entity.LayerInfo;
import com.classdemo.network.mapper.LayerInfoMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LayerInfoService {

    private final LayerInfoMapper layerInfoMapper;
    private final LayerInfoConverter layerInfoConverter;

    public LayerInfoService(LayerInfoMapper layerInfoMapper, LayerInfoConverter layerInfoConverter) {
        this.layerInfoMapper = layerInfoMapper;
        this.layerInfoConverter = layerInfoConverter;
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

    public LayerInfo update(String layerKey, LayerUpdateRequest request) {
        LayerKey.from(layerKey);
        if (request == null || !request.hasAnyField()) {
            throw new IllegalArgumentException("至少需提供 mainFunction、protocols、devicesUnits 中的一项");
        }
        LayerInfo info = getByLayerKey(layerKey);
        if (request.mainFunction() != null) {
            info.setMainFunction(request.mainFunction());
        }
        if (request.protocols() != null) {
            info.setProtocols(layerInfoConverter.serializeProtocols(request.protocols()));
        }
        if (request.devicesUnits() != null) {
            info.setDevicesUnits(request.devicesUnits());
        }
        info.setUpdatedAt(LocalDateTime.now());
        layerInfoMapper.updateById(info);
        return info;
    }
}
