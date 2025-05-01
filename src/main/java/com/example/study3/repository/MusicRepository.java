package com.example.study3.repository;

import com.example.study3.domain.MusicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MusicRepository extends JpaRepository<MusicEntity, String> {
    boolean existsByAudioUrl(String audioUrl);
    List<MusicEntity> findByTaskIdAndAudioUrlIsNotNull(String taskId);
}
