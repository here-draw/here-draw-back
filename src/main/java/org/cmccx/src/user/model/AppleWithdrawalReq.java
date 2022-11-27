package org.cmccx.src.user.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class AppleWithdrawalReq {
    private int flag;

    @NotBlank(message = "Authorization Code를 입력하세요.")
    private String authorizationCode;
}
