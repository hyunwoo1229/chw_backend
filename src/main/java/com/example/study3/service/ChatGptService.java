package com.example.study3.service;

import com.example.study3.dto.MessageDto;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;


@Service
public class ChatGptService {

    private final WebClient webClient;

    public ChatGptService(@Value("${openai.api.key}") String apiKey){
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String getChatGptReply(List<MessageDto> messages) {

        boolean hasSystemMessages = messages.stream()
                .anyMatch(msg -> "system".equals(msg.getRole()));

        if (!hasSystemMessages) {
            messages.add(0, new MessageDto("system",  "너는 작곡가야. 사용자와 대화하며 곡의 분위기, 장르, 스타일, 가사를 함께 정하고 사용자가 원하는 노래 스타일과 가사를 잘 만들어줘. 가사는 2분 분량 정도로 만들어야 돼."));
        }


        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", messages
        );

        try {
            Map response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");

        } catch (WebClientResponseException.TooManyRequests e) {
            return "잠시 후 다시 시도해주세요. (요청 제한)";
        } catch (WebClientResponseException e) {
            return "OpenAI 요청 중 오류가 발생했어요.";
        } catch (Exception e) {
            return "서버 내부 오류가 발생했어요.";
        }


    }

    public String extractSongInfoFromMessages(List<Map<String, String>> messages) {
        // 요약을 요청하는 메시지를 추가
        messages.add(Map.of(
                "role", "user",
                "content", "지금까지 대화로 만든 가사와 음악 스타일을 알려줘. 형식:\n[가사]\n[스타일]"
        ));

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", messages
        );

        try {
            Map response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");

        } catch (WebClientResponseException.TooManyRequests e) {
            return "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
        } catch (WebClientResponseException e) {
            return "OpenAI 요청 중 오류가 발생했어요.";
        } catch (Exception e) {
            return "서버 내부 오류가 발생했어요.";
        }
    }
}