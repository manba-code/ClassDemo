package com.classdemo.network.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("统一API响应单元测试")
class ApiResponseTest {

    @Test
    @DisplayName("ok - 带数据返回成功响应")
    void testOk_WithData() {
        // When
        ApiResponse<String> response = ApiResponse.ok("data");

        // Then
        assertAll(
                () -> assertEquals(ErrorCode.SUCCESS, response.code()),
                () -> assertEquals("ok", response.message()),
                () -> assertEquals("data", response.data())
        );
    }

    @Test
    @DisplayName("ok - 无数据返回成功响应")
    void testOk_NoData() {
        // When
        ApiResponse<Void> response = ApiResponse.ok();

        // Then
        assertAll(
                () -> assertEquals(ErrorCode.SUCCESS, response.code()),
                () -> assertNull(response.data())
        );
    }

    @Test
    @DisplayName("fail - 返回失败响应")
    void testFail() {
        // When
        ApiResponse<Void> response = ApiResponse.fail(ErrorCode.BAD_REQUEST, "错误");

        // Then
        assertAll(
                () -> assertEquals(ErrorCode.BAD_REQUEST, response.code()),
                () -> assertEquals("错误", response.message()),
                () -> assertNull(response.data())
        );
    }
}
