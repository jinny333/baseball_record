package com.baseball.record.global.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void BusinessException은_ErrorCode에_정의된_상태코드와_메시지로_응답한다() {
        BusinessException exception = new BusinessException(ErrorCode.MEMBER_NOT_FOUND);

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("MEMBER_NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void Validation_실패시_400과_첫번째_필드_에러_메시지를_응답한다() throws NoSuchMethodException {
        MethodArgumentNotValidException exception = createValidationException("nickname", "닉네임은 필수입니다.");

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INVALID_REQUEST.name());
        assertThat(response.getBody().message()).isEqualTo("닉네임은 필수입니다.");
    }

    @Test
    void Validation_실패_메시지가_없으면_기본_메시지를_사용한다() throws NoSuchMethodException {
        MethodArgumentNotValidException exception = createValidationException("nickname", null);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.INVALID_REQUEST.getMessage());
    }

    @Test
    void 타입_불일치시_400과_파라미터명을_포함한_메시지를_응답한다() throws NoSuchMethodException {
        MethodParameter parameter = new MethodParameter(SampleController.class.getMethod("sample", Long.class), 0);
        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException("abc", Long.class, "id", parameter, new NumberFormatException());

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatchException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INVALID_REQUEST.name());
        assertThat(response.getBody().message()).contains("id");
    }

    @Test
    void 필수_파라미터_누락시_400과_파라미터명을_포함한_메시지를_응답한다() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("limit", "Integer");

        ResponseEntity<ErrorResponse> response = handler.handleMissingParameterException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INVALID_REQUEST.name());
        assertThat(response.getBody().message()).contains("limit");
    }

    @Test
    void 요청_본문을_읽을_수_없으면_400을_응답한다() {
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("parse error", inputMessage);

        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadableException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INVALID_REQUEST.name());
    }

    @Test
    void 처리되지_않은_예외는_500과_공통_에러_메시지를_응답한다() {
        ResponseEntity<ErrorResponse> response = handler.handleException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name());
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    @Test
    void 모든_에러_응답은_code와_message_필드를_가진_통일된_포맷을_따른다() {
        ResponseEntity<ErrorResponse> businessResponse =
                handler.handleBusinessException(new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        ResponseEntity<ErrorResponse> serverResponse = handler.handleException(new RuntimeException());

        assertThat(businessResponse.getBody()).isInstanceOf(ErrorResponse.class);
        assertThat(serverResponse.getBody()).isInstanceOf(ErrorResponse.class);
        assertThat(businessResponse.getBody().code()).isNotBlank();
        assertThat(businessResponse.getBody().message()).isNotBlank();
        assertThat(serverResponse.getBody().code()).isNotBlank();
        assertThat(serverResponse.getBody().message()).isNotBlank();
    }

    private MethodArgumentNotValidException createValidationException(String field, String defaultMessage)
            throws NoSuchMethodException {
        MethodParameter parameter = new MethodParameter(SampleController.class.getMethod("sample", Long.class), 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", field, defaultMessage));
        return new MethodArgumentNotValidException(parameter, bindingResult);
    }

    static class SampleController {
        public void sample(Long id) {
        }
    }
}
