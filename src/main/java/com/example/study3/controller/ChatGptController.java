package com.example.study3.controller;

import com.example.study3.dto.ChatRequestDto;
import com.example.study3.dto.ChatResponse;
import com.example.study3.dto.MessageDto;
import com.example.study3.service.ChatGptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")

public class ChatGptController {
    private final ChatGptService chatGptService;

    public ChatGptController(ChatGptService chatGptService) {
        this.chatGptService = chatGptService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequestDto requestDto) {
        //프론트에서 messages라는 키로 전체 대화 배열을 넘김
        List<MessageDto> messages = requestDto.getMessages();
        String reply = chatGptService.getChatGptReply(messages);
        return ResponseEntity.ok(new ChatResponse(reply));
    }
}

