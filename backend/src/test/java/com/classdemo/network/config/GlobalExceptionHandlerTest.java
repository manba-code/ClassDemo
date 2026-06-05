package com.classdemo.network.config;

import com.classdemo.network.common.ApiResponse;
import com.classdemo.network.common.BusinessException;
import com.classdemo.network.common.ErrorCode;
import com.classdemo.network.common.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("全局异常处理器单元测试")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("处理IllegalArgumentException - 返回40001")
    void testHandleIllegalArgument() {
        // When
        ApiResponse<Void> response = handler.handleIllegalArgument(
                new IllegalArgumentException("参数无效"));

        // Then
        assertAll(
                () -> assertEquals(ErrorCode.BAD_REQUEST, response.code()),
                () -> assertEquals("参数无效", response.message())
        );
    }

    @Test
    @DisplayName("处理BusinessException - 返回业务错误码")
    void testHandleBusiness() {
        // When
        ApiResponse<Void> response = handler.handleBusiness(
                new ResourceNotFoundException("资源不存在"));

        // Then
        assertAll(
                () -> assertEquals(ErrorCode.NOT_FOUND, response.code()),
                () -> assertEquals("资源不存在", response.message())
        );
    }

    @Test
    @DisplayName("处理未知异常 - 返回50000")
    void testHandleException() {
        // When
        ApiResponse<Void> response = handler.handleException(new RuntimeException("boom"));

        // Then
        assertAll(
                () -> assertEquals(ErrorCode.INTERNAL_ERROR, response.code()),
                () -> assertEquals("服务器内部错误", response.message())
        );
    }

    @Test
    @DisplayName("处理参数校验异常 - 返回字段错误信息")
    void testHandleValidation() throws NoSuchMethodException {
        // Given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "title", "不能为空"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ApiResponse<Void> response = handler.handleValidation(ex);

        // Then
        assertAll(
                () -> assertEquals(ErrorCode.BAD_REQUEST, response.code()),
                () -> assertEquals("参数错误：title 不能为空", response.message())
        );
    }

    @Test
    @DisplayName("处理BusinessException - 自定义错误码")
    void testHandleBusiness_CustomCode() {
        // When
        ApiResponse<Void> response = handler.handleBusiness(
                new BusinessException(99999, "自定义错误"));

        // Then
        assertEquals(99999, response.code());
    }

    @Test
    @DisplayName("处理BindException - 返回字段错误信息")
    void testHandleBind() {
        // Given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "不能为空"));
        BindException ex = new BindException(bindingResult);

        // When
        ApiResponse<Void> response = handler.handleBind(ex);

        // Then
        assertAll(
                () -> assertEquals(ErrorCode.BAD_REQUEST, response.code()),
                () -> assertEquals("参数错误：name 不能为空", response.message())
        );
    }

    @Test
    @DisplayName("处理参数校验异常 - 无字段错误时使用默认消息")
    void testHandleValidation_NoFieldErrors() throws NoSuchMethodException {
        // Given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ApiResponse<Void> response = handler.handleValidation(ex);

        // Then
        assertAll(
                () -> assertEquals(ErrorCode.BAD_REQUEST, response.code()),
                () -> assertEquals("请求参数错误", response.message())
        );
    }

    @Test
    @DisplayName("处理BindException - 无字段错误时使用默认消息")
    void testHandleBind_NoFieldErrors() {
        // Given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        BindException ex = new BindException(bindingResult);

        // When
        ApiResponse<Void> response = handler.handleBind(ex);

        // Then
        assertEquals("请求参数错误", response.message());
    }
}
