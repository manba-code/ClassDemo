package com.classdemo.network.common;

/**
 * 业务错误码（PRD §7.3）。
 */
public final class ErrorCode {

    public static final int SUCCESS = 0;
    public static final int BAD_REQUEST = 40001;
    public static final int NOT_FOUND = 40401;
    public static final int INVALID_LAYER_KEY = 40402;
    public static final int INTERNAL_ERROR = 50000;

    private ErrorCode() {
    }
}
