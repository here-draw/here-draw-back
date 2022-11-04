package org.cmccx.src.chat.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ChatMessageReq {
    @NotNull(message = "채팅방ID를 입력하세요.")
    private long roomId;

    @NotNull(message = "발신ID를 입력하세요.")
    private Long senderId;

    @NotNull(message = "수신ID를 입력하세요.")
    private Long receiverId;

    @NotBlank(message = "메세지를 입력하세요.")
    private String message;
}
