package com.example.study3.controller;

import com.example.study3.dto.ChatRequestDto;
import com.example.study3.dto.ChatResponse;
import com.example.study3.dto.SuccessResponse;
import com.example.study3.dto.SunoRequest;
import com.example.study3.service.ChatGptService;
import com.example.study3.service.MusicService;
import com.example.study3.service.SunoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatGptController {
    private final ChatGptService chatGptService;

    public ChatGptController(ChatGptService chatGptService) {
        this.chatGptService = chatGptService;
    }

    @PostMapping("/chat")
    public ChatResponse getchat(@RequestBody ChatRequestDto request) {
        return chatGptService.getChatGptReply(request.getMessages());
    }

    @PostMapping("/chat/summarize")
    public ResponseEntity<?> summarizeAndGenerateMusic(@RequestBody ChatRequestDto request) {
        String taskId = chatGptService.generateSunoInfoFromChat(request.getMessages());
        return ResponseEntity.ok(taskId);
    }
}

