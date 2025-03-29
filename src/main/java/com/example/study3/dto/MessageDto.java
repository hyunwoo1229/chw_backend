package com.example.study3.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDto {
    private String role;
    private String content;

    public MessageDto() {}
    public MessageDto(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
