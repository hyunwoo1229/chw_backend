package com.example.study3.controller;

import com.example.study3.domain.MusicEntity;
import com.example.study3.service.SunoResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suno")
@RequiredArgsConstructor
public class SunoController {
    private final SunoResultService sunoResultService;

    @PostMapping("/callback")
    public ResponseEntity<String> RecieveCallback(@RequestParam String taskId, @RequestBody Map<String, Object> callbackData) {
        sunoResultService.handleSunoCallback(taskId, callbackData);
        return ResponseEntity.ok("곡 저장 완료");
    }

    @GetMapping("music-list")
    public ResponseEntity<List<MusicEntity>> getMusicByTask(@RequestParam String taskId) {
        List<MusicEntity> musicList = sunoResultService.findByTaskId(taskId);
        return ResponseEntity.ok(musicList);
    }

}

