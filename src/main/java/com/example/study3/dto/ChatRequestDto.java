package com.example.study3.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class ChatRequestDto {
    private List<MessageDto> messages;

    public ChatRequestDto() {}
    public ChatRequestDto(List<MessageDto> messages) {
        this.messages = messages;
    }
}