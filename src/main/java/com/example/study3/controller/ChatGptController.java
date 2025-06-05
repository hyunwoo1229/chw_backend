package com.example.study3.controller;

import com.example.study3.dto.ChatRequestDto;
import com.example.study3.dto.ChatResponse;
import com.example.study3.service.ChatGptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor

public class ChatGptController {
    private final ChatGptService chatGptService;

    //chatGpt와 대화
    @PostMapping
    public ChatResponse getchat(@RequestBody ChatRequestDto request) {
        return chatGptService.getChatGptReply(request.getMessages());
    }

    //노래 만들기 버튼 누르면 suno에게 정보 요약해서 전달
    @PostMapping("/summarize")
    public ResponseEntity<?> summarizeAndGenerateMusic(@RequestBody ChatRequestDto request) {
        String taskId = chatGptService.generateSunoInfoFromChat(request.getMessages());
        return ResponseEntity.ok(taskId);
    }
}

