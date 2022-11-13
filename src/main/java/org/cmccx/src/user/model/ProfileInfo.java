package org.cmccx.src.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
public class ProfileInfo {
    private String profileImage;
    @NotNull
    @Size(min = 1, max = 10, message = "닉네임은 최소 1글자, 최대 10글자까지 입력 가능합니다.")
    private String nickname;

    @Size(max = 70, message = "간단 소개는 최대 70글자까지 입력 가능합니다.")
    private String description;
}
