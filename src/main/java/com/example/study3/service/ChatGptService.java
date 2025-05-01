package com.example.study3.service;

import com.example.study3.dto.ChatResponse;
import com.example.study3.dto.MessageDto;
import com.example.study3.dto.SunoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class ChatGptService {

    private final SunoService sunoService;
    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final WebClient webClient;

    public ChatGptService(@Value("${openai.api.key}") String apiKey, SunoService sunoService){
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.sunoService = sunoService;
    }

    public ChatResponse getChatGptReply(List<MessageDto> messages) {

        boolean hasSystemMessages = messages.stream()
                .anyMatch(msg -> "system".equals(msg.getRole()));

        if (!hasSystemMessages) {
            messages.add(0, new MessageDto("system",  "너는 작곡가야. 사용자와 대화하며 곡의 분위기, 장르, 스타일, 가사를 함께 정하고 사용자가 원하는 노래 스타일과 가사, 장르를 잘 만들어줘. 가사는 300자 이하 2분 분량 정도로 만들어야 돼. "));
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

            String reply = (String) message.get("content");
            return new ChatResponse(reply);

        } catch (WebClientResponseException.TooManyRequests e) {
            return new ChatResponse("잠시 후 다시 시도해주세요. (요청 제한)");
        } catch (WebClientResponseException e) {
            return new ChatResponse("OpenAI 요청 중 오류가 발생했어요.");
        } catch (Exception e) {
            return new ChatResponse("서버 내부 오류가 발생했어요.");
        }


    }


    public String generateSunoInfoFromChat(List<MessageDto> messages) {
        System.out.println("=== ChatGptService generateSunoInfoFromChat() 호출 ===");


        List<Map<String, String>> openAiMessages = messages.stream()
                .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
                .collect(Collectors.toList());
        openAiMessages.add(Map.of(
                "role", "user",
                "content", "이제 대화는 끝났어. 지금까지 대화 내용을 바탕으로 아래 형식과 정확히 동일한 순수 JSON을 생성해줘. ``` 등의 코드블럭 없이 순수한 JSON 텍스트만 생성해. prompt 부분에는 반드시 300자 이하로 2분 정도 분량의 가사를 넣어야 해." +
                        "다른 부가 설명이나 코드 블록 없이 오직 JSON만 출력해줘:\n" +
                        "{\n" +
                        "  \"prompt\": \"<300자 이하의 2분 분량의 가사>\",\n" +
                        "  \"style\": \"<음악 스타일, 예: Classical, Jazz 등>\",\n" +
                        "  \"title\": \"<노래 제목>\",\n" +
                        "  \"customMode\": true,\n" +
                        "  \"instrumental\": true,\n" +
                        "  \"model\": \"V3_5\",\n" +
                        "  \"negativeTags\": \"<제외할 태그, 예: Heavy Metal, Upbeat Drums>\",\n" +
                        "  \"callBackUrl\": \"<https://8e24-121-165-35-251.ngrok-free.app/api/suno/callback?taskid=~~>\"\n" +
                        "}"
        ));

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", openAiMessages
        );

        try {
            Map response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            String content = (String) message.get("content");
            System.out.println("ChatGPT 응답 내용: " + content);

            Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                content = matcher.group();
            } else {
                System.out.println("JSON 형식 추출 실패");
                return "JSON형식 추출 실패";
            }
            System.out.println("추출된 JSON: " + content);

            ObjectMapper mapper = new ObjectMapper();
            SunoRequest sunoRequest = mapper.readValue(content, SunoRequest.class);
            System.out.println("SunoRequest: prompt=" + sunoRequest.getPrompt() +
                    ", style=" + sunoRequest.getStyle() +
                    ", title=" + sunoRequest.getTitle() +
                    ", customMode=" + sunoRequest.isCustomMode() +
                    ", instrumental=" + sunoRequest.isInstrumental() +
                    ", model=" + sunoRequest.getModel() +
                    ", negativeTags=" + sunoRequest.getNegativeTags() +
                    ", callBackUrl=" + sunoRequest.getCallBackUrl());

            try{
                String taskId = sunoService.generateMusic(sunoRequest);
                return taskId;
            } catch (Exception e) {
                e.printStackTrace();
                return "응답 중 오류 발생";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "응답 중 오류 발생";
        }

    }
}