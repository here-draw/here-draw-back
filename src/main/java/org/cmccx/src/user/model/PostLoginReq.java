package org.cmccx.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
@AllArgsConstructor
public class PostLoginReq {
    private int flag;

    @NotBlank(message = "Access Token을 입력하세요.")
    private String accessToken;
}
