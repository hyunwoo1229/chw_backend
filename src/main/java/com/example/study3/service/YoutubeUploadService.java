package com.example.study3.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import org.springframework.stereotype.Service;


import java.io.*;
import java.net.URL;

@Service
public class YoutubeUploadService {

    private File downloadFromUrl(String url, String fileName) throws IOException{
        URL resourceUrl = new URL(url);
        File file = new File("/tmp/" + fileName);

        try (InputStream in = resourceUrl.openStream();
             OutputStream out = new FileOutputStream(file)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        return file;
    }

    private File generateMp4FromImageAndAudio(File imageFile, File audioFile, String outputFileName) throws IOException, InterruptedException {
        File output = new File("/tmp/" + outputFileName);
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-loop", "1",
                "-i", imageFile.getAbsolutePath(),
                "-i", audioFile.getAbsolutePath(),
                "-vf", "scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2",
                "-c:v", "libx264",
                "-c:a", "aac",
                "-shortest",
                "-y", output.getAbsolutePath()
        );

        pb.redirectErrorStream(true); // stderr → stdout 합치기
        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("ffmpeg failed with exit code " + exitCode);
        }

        return output;
    }

    public String uploadToYoutube(String accessToken, String imageUrl, String audioUrl,
                                  String title, String description) throws Exception {
        try {
            // 🔽 기존 mp4 생성 + YouTube API 요청
            File imageFile = downloadFromUrl(imageUrl, "image.jpg");
            File audioFile = downloadFromUrl(audioUrl, "audio.mp3");
            File mp4File = generateMp4FromImageAndAudio(imageFile, audioFile, "output.mp4");

            HttpRequestInitializer requestInitializer = request -> {
                request.getHeaders().setAuthorization("Bearer " + accessToken);
            };

            YouTube youtube = new YouTube.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    requestInitializer
            ).setApplicationName("your-app-name").build();

            // ✨ 영상 정보 설정
            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle(title);
            snippet.setDescription(description);
            snippet.setCategoryId("10"); // Music

            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("public");

            Video videoMetadata = new Video();
            videoMetadata.setSnippet(snippet);
            videoMetadata.setStatus(status);

            InputStreamContent mediaContent = new InputStreamContent(
                    "video/*", new BufferedInputStream(new FileInputStream(mp4File)));
            mediaContent.setLength(mp4File.length());

            YouTube.Videos.Insert videoInsert = youtube.videos()
                    .insert("snippet,status", videoMetadata, mediaContent);
            Video uploadedVideo = videoInsert.execute();

            return "https://www.youtube.com/watch?v=" + uploadedVideo.getId();

        } catch (GoogleJsonResponseException e) {
            // 🔥 유튜브 accessToken 만료 또는 권한 없음
            if (e.getStatusCode() == 401 || e.getStatusCode() == 403) {
                throw new RuntimeException("유튜브 인증이 만료되었거나 권한이 없습니다. 다시 인증해주세요.");
            }
            throw e;
        }
    }





}


