package org.cmccx.src.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SocketChatRoom {
    private long userId;
    private long roomId;
}
