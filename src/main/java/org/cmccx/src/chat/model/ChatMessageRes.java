package org.cmccx.src.chat.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRes {
    private Long messageId;
    private String message;
    private String date;
}
