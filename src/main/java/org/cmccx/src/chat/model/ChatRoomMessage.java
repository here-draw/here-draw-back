package org.cmccx.src.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomMessage {
    private long messageId;
    private boolean sender;
    private String message;
    private String date;
}
