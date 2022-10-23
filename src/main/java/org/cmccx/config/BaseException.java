package org.cmccx.config;

import lombok.Getter;

@Getter
public class BaseException extends Exception {
    private BaseResponseStatus status;
    private String message;

    public BaseException(BaseResponseStatus status){
        this.status = status;
    }

    public BaseException(BaseResponseStatus status, String message){
        this.status = status;
        this.message = message;
    }
}
