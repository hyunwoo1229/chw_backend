package com.example.study3.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ChatResponse {
    private String reply;

    public ChatResponse(String reply) {
        this.reply = reply;
    }
}
