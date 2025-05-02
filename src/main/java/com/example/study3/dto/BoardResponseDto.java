package com.example.study3.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardResponseDto {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private String createdAt;
    private String musicTitle;
    private String audioUrl;
    private String imageUrl;
    private boolean author;
}
