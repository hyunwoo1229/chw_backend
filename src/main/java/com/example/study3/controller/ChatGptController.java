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

    @PostMapping
    public ChatResponse getchat(@RequestBody ChatRequestDto request) {
        return chatGptService.getChatGptReply(request.getMessages());
    }

    @PostMapping("/summarize")
    public ResponseEntity<?> summarizeAndGenerateMusic(@RequestBody ChatRequestDto request) {
        String taskId = chatGptService.generateSunoInfoFromChat(request.getMessages());
        return ResponseEntity.ok(taskId);
    }
}

