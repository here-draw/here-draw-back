package org.cmccx.config;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@ControllerAdvice
@ResponseBody
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    public void handelException(Exception e){
        e.printStackTrace();
    }


    @ExceptionHandler(BaseException.class)
    public BaseResponse handleBaseException(BaseException e){
        return new BaseResponse<>(e.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse handleValidException(MethodArgumentNotValidException e){
        // @Valid 에러 목록
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        // 첫번째 에러 메세지
        String message = errors.get(0).getDefaultMessage();

        return new BaseResponse(BaseResponseStatus.REQUEST_ERROR, message);
    }


    @ExceptionHandler(BindException.class)
    public BaseResponse handleValidException(BindException e){
        // @Valid 에러 목록
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        // 첫번째 에러 메세지
        String message = errors.get(0).getDefaultMessage();

        return new BaseResponse(BaseResponseStatus.REQUEST_ERROR, message);
    }

}
