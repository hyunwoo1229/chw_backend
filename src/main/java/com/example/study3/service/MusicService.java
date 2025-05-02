package com.example.study3.service;
import com.example.study3.domain.MusicEntity;
import com.example.study3.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MusicService {

    private final MusicRepository musicRepository;

    public MusicService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }


    public void handleSunoCallback(String taskId, Map<String, Object> callbackData) {
        System.out.println("🎶 [MusicService] 콜백 처리 시작");

        Map<String, Object> data = (Map<String, Object>) callbackData.get("data");
        List<Map<String, Object>> trackList = (List<Map<String, Object>>) data.get("data");


        for (Map<String, Object> track : trackList) {
            String audioUrl = (String) track.get("audio_url");
            String id = (String) track.get("id");

            if(audioUrl == null || audioUrl.isBlank()){
                System.out.println("🚫 audio_url 없음 → 저장하지 않음");
                continue;
            }

            if(musicRepository.existsByAudioUrl(audioUrl)){
                System.out.println("🚫 중복된 audio_url → 저장하지 않음: " + audioUrl);
                continue;
            }


            MusicEntity music = new MusicEntity(
                    id,
                    (String) track.get("title"),
                    audioUrl,
                    (String) track.get("image_url")
            );
            music.setTaskId(taskId);
            musicRepository.save(music);
            System.out.println("💾 저장 완료 (taskId: " + taskId + "): " + music.getTitle());
        }

         System.out.println("저장 종료");
    }

    public List<MusicEntity> findByTaksId(String taksId) {
        return musicRepository.findByTaskIdAndAudioUrlIsNotNull(taksId);
    }
}
