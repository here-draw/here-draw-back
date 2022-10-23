package org.cmccx.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.cmccx.config.BaseResponseStatus.BAD_REQUEST;
import static org.cmccx.config.BaseResponseStatus.SERVER_ERROR;



@ControllerAdvice
@ResponseBody
public class ExceptionController {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(BaseException.class)
    protected BaseResponse handleBaseException(BaseException e){
        return new BaseResponse<>(e.getStatus());
    }

    // @Valid 또는 @Validate 바인딩 에러 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected BaseResponse handleValidException(MethodArgumentNotValidException e){
        // @Valid 에러 목록
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        // 첫번째 에러 메세지
        String message = errors.get(0).getDefaultMessage();

        return new BaseResponse(BaseResponseStatus.VALIDATION_ERROR, message);
    }

    // @ModelAttribute 바인딩 에러 처리
    @ExceptionHandler(BindException.class)
    protected BaseResponse handleValidException(BindException e){
        // @Valid 에러 목록
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        // 첫번째 에러 메세지
        String message = errors.get(0).getDefaultMessage();

        return new BaseResponse(BaseResponseStatus.VALIDATION_ERROR, message);
    }

    // 데이터 타입 불일치로 인한 바인딩 에러 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected BaseResponse handleValidException(MethodArgumentTypeMismatchException e){
        logger.error(e.getMessage());
        return new BaseResponse(BAD_REQUEST);
    }

    // 지원하지 않는 HTTP 메소드 호출 시 에러 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected BaseResponse handleValidException(HttpRequestMethodNotSupportedException e) {
        logger.error(e.getMessage());
        return new BaseResponse(BAD_REQUEST);
    }

    //Authentication 권한이 없는 경우 발생 시 에러 처리(security에서 발생시킴)
    @ExceptionHandler(AccessDeniedException.class)
    protected BaseResponse handleValidException(AccessDeniedException e){
        logger.error(e.getMessage());
        return new BaseResponse(BAD_REQUEST);
    }

    // 접근할 수 없는 호출 발생 시 에러 처리
    @ExceptionHandler(IllegalAccessException.class)
    protected BaseResponse handleValidException(IllegalAccessException e){
        logger.error(e.getMessage());
        return new BaseResponse(BAD_REQUEST);
    }

    // 그 외 에러 처리
    @ExceptionHandler(Exception.class)
    protected BaseResponse handelException(Exception e){
        logger.error("Error has occurred", e);
        return new BaseResponse(SERVER_ERROR);
    }

}
