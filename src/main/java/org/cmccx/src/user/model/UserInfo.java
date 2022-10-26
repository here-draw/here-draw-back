package org.cmccx.src.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserInfo {
    private long userId;
    private String nickname;
    private String status;
}
