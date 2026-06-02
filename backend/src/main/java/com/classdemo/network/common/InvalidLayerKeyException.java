package com.classdemo.network.common;

/**
 * 无效分层 key（40402）。
 */
public class InvalidLayerKeyException extends BusinessException {

    public InvalidLayerKeyException(String layerKey) {
        super(ErrorCode.INVALID_LAYER_KEY, "无效的分层 key: " + layerKey);
    }
}
