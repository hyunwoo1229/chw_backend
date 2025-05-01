package com.example.study3.controller;

import com.example.study3.domain.MusicEntity;
import com.example.study3.dto.SunoRequest;
import com.example.study3.service.ChatGptService;
import com.example.study3.service.MusicService;
import com.example.study3.service.SunoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/suno")
@CrossOrigin(origins = "http://localhost:5173")

public class SunoController {
    private final ChatGptService chatGptService;
    private final SunoService sunoService;
    private final MusicService musicService;

    public SunoController(ChatGptService chatGptService, SunoService sunoService, MusicService musicService) {
        this.chatGptService = chatGptService;
        this.sunoService = sunoService;
        this.musicService = musicService;
    }


    @PostMapping("/callback")
    public ResponseEntity<String> RecieveCallback(@RequestParam String taskId,
                                                  @RequestBody Map<String, Object> callbackData) {
        System.out.println("🎯 [Suno 콜백 도착] => " + callbackData);
        musicService.handleSunoCallback(taskId, callbackData);
        return ResponseEntity.ok("곡 저장 완료");
    }

    @GetMapping("music-list")
    public ResponseEntity<List<MusicEntity>> getMusicByTask(@RequestParam String taskId) {
        List<MusicEntity> musicList = musicService.findByTaksId(taskId);
        return ResponseEntity.ok(musicList);
    }

    @PostMapping("/choose")
    public ResponseEntity<String> chooseMusic(@RequestBody Map<String, String> selectedMusic) {
        String title = selectedMusic.get("title");
        System.out.println("🎵 선택된 곡: " + title);
        return ResponseEntity.ok("선택 완료");
    }

}

