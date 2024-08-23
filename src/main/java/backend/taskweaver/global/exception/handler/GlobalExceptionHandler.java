package backend.taskweaver.global.exception.handler;

import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.code.ErrorResponse;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final HttpStatus HTTP_STATUS_OK = HttpStatus.OK;

    /**
     * [Exception] API 호출 시 @RequestParam, @PathVariable의 유효성 검사(Validation)에 실패할 때
     *
     * @param ex ConstraintViolationException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("handleConstraintViolationException", ex);
        StringBuilder stringBuilder = new StringBuilder();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            stringBuilder.append(violation.getInvalidValue()).append(": ");
            stringBuilder.append(violation.getMessage());
            if(violations.size() != 1) {
                stringBuilder.append(", ");
            }
        }
        final ErrorResponse response = ErrorResponse.of(ErrorCode.NOT_VALID_ERROR, String.valueOf(stringBuilder));
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.NOT_VALID_ERROR.getStatus()));
    }

    /**
     * [Exception] API 호출 시 @RequestBody의 유효성 검사(Validation)에 실패할 때
     *
     * @param ex MethodArgumentNotValidException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("handleMethodArgumentNotValidException", ex);
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder stringBuilder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            stringBuilder.append(fieldError.getField()).append(":");
            stringBuilder.append(fieldError.getDefaultMessage());
            stringBuilder.append(", ");
        }
        final ErrorResponse response = ErrorResponse.of(ErrorCode.NOT_VALID_ERROR, String.valueOf(stringBuilder));
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.NOT_VALID_ERROR.getStatus()));
    }

    /**
     * [Exception] API 호출 시 'Header' 내에 데이터 값이 유효하지 않은 경우
     *
     * @param ex MissingRequestHeaderException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    protected ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        log.error("MissingRequestHeaderException", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.REQUEST_BODY_MISSING_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.REQUEST_BODY_MISSING_ERROR.getStatus()));
    }

    /**
     * [Exception] 클라이언트에서 Body로 '객체' 데이터가 넘어오지 않았을 경우
     *
     * @param ex HttpMessageNotReadableException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.REQUEST_BODY_MISSING_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.REQUEST_BODY_MISSING_ERROR.getStatus()));
    }

    /**
     * [Exception] 클라이언트에서 request로 '파라미터로' 데이터가 넘어오지 않았을 경우
     *
     * @param ex MissingServletRequestParameterException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingRequestHeaderExceptionException(
            MissingServletRequestParameterException ex) {
        log.error("handleMissingServletRequestParameterException", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.MISSING_REQUEST_PARAMETER_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.MISSING_REQUEST_PARAMETER_ERROR.getStatus()));
    }


    /**
     * [Exception] 잘못된 서버 요청일 경우 발생한 경우
     *
     * @param e HttpClientErrorException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(HttpClientErrorException.BadRequest.class)
    protected ResponseEntity<ErrorResponse> handleBadRequestException(HttpClientErrorException e) {
        log.error("HttpClientErrorException.BadRequest", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.BAD_REQUEST_ERROR, e.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.BAD_REQUEST_ERROR.getStatus()));
    }


    /**
     * [Exception] 잘못된 주소로 요청 한 경우
     *
     * @param e NoHandlerFoundException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNoHandlerFoundExceptionException(NoHandlerFoundException e) {
        log.error("handleNoHandlerFoundExceptionException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.NOT_FOUND_ERROR, e.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.NOT_FOUND_ERROR.getStatus()));
    }

    /**
     * [Exception] NULL 값이 발생한 경우
     *
     * @param e NullPointerException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
        log.error("handleNullPointerException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.NULL_POINT_ERROR, e.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.NULL_POINT_ERROR.getStatus()));
    }

    /**
     * Input / Output 내에서 발생한 경우
     *
     * @param ex IOException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(IOException.class)
    protected ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        log.error("handleIOException", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.IO_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.IO_ERROR.getStatus()));
    }


    /**
     * com.google.gson 내에 Exception 발생하는 경우
     *
     * @param ex JsonParseException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(JsonParseException.class)
    protected ResponseEntity<ErrorResponse> handleJsonParseExceptionException(JsonParseException ex) {
        log.error("handleJsonParseExceptionException", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.JSON_PARSE_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.JSON_PARSE_ERROR.getStatus()));
    }

    /**
     * com.fasterxml.jackson.core 내에 Exception 발생하는 경우
     *
     * @param ex JsonProcessingException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(JsonProcessingException.class)
    protected ResponseEntity<ErrorResponse> handleJsonProcessingException(JsonProcessingException ex) {
        log.error("handleJsonProcessingException", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.REQUEST_BODY_MISSING_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.REQUEST_BODY_MISSING_ERROR.getStatus()));
    }

    /**
     * io.jsonwebtoken 내의 ExpiredJwtException이 발생하는 경우
     * 토큰의 유효 기간이 만료된 경우에 발생
     * Exception 발생시 UTC 기준을 KST(한국 시간) 기준으로 바꿔 오류 메세지 출력하게 함
     *
     * @param ex ExpiredJwtException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(ExpiredJwtException.class)
    protected ResponseEntity<ErrorResponse> handleExpiredJwtException(ExpiredJwtException ex) {
        log.error("handleExpiredJwtException", ex);

        // 원래의 에러 메시지
        String originalMessage = ex.getMessage();

        // 에러 메시지에서 시간을 추출
        String expiredAtUtc = originalMessage.substring(originalMessage.indexOf("at") + 3, originalMessage.indexOf("Z.") + 1);
        String currentAtUtc = originalMessage.substring(originalMessage.indexOf("time:") + 6, originalMessage.indexOf("Z,", originalMessage.indexOf("time:")) + 1);

        // 한국 시간대로 변환 (UTC+9)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX", Locale.US);
        ZonedDateTime expiredAtKst = ZonedDateTime.parse(expiredAtUtc, formatter).plusHours(9);
        ZonedDateTime currentAtKst = ZonedDateTime.parse(currentAtUtc, formatter).plusHours(9);

        // 변환된 시간을 사용하여 새로운 에러 메시지 생성
        String newMessage = originalMessage
                .replace(expiredAtUtc, expiredAtKst.format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX")))
                .replace(currentAtUtc, currentAtKst.format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX")));

        // 새로운 에러 메시지로 ErrorResponse 생성
        final ErrorResponse response = ErrorResponse.of(ErrorCode.EXPIRED_JWT_ERROR, newMessage);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.EXPIRED_JWT_ERROR.getStatus()));
    }

    /**
     * io.jsonwebtoken.security 내의 SignatureException이 발생하는 경우
     *
     * @param ex SignatureException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(SignatureException.class)
    protected ResponseEntity<ErrorResponse> handleSignatureException(SignatureException ex) {
        log.error("handleJsonProcessingException", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_JWT_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.INVALID_JWT_ERROR.getStatus()));
    }

    /**
     * io.jsonwebtoken 내의 MalformedJwtException이 발생하는 경우
     *
     * @param ex MalformedJwtException
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(MalformedJwtException.class)
    protected ResponseEntity<ErrorResponse> handleMalformedJwtException(MalformedJwtException ex) {
        log.error("handleJsonProcessingException", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_JWT_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.INVALID_JWT_ERROR.getStatus()));
    }

    // ==================================================================================================================

    /**
     * [Exception] 모든 Exception 경우 발생
     *
     * @param ex Exception
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(Exception.class)
    protected final ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        log.error("Exception", ex);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()));
    }

    // ==================================================================================================================

    /**
     * BusinessException 발생
     *
     */

    @ExceptionHandler(BusinessExceptionHandler.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessExceptionHandler ex) {
        log.error("BusinessExceptionHandler", ex);
        ErrorCode errorCode = ex.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(errorCode, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(errorCode.getStatus()));
    }
}