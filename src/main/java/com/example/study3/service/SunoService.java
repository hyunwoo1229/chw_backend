package com.example.study3.service;

import com.example.study3.dto.SunoRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class SunoService {
    private final ObjectMapper objectMapper;
    @Value("${suno.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public SunoService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generateMusic(SunoRequest request) {

        String taskId = UUID.randomUUID().toString();
        String callbackUrl = "https://4a8a-121-165-35-251.ngrok-free.app/api/suno/callback?taskId=" + taskId;

        try{
            String postUrl = "https://apibox.erweima.ai/api/v1/generate";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("prompt", request.getPrompt());
            body.put("title", request.getTitle());
            body.put("style", request.getStyle());
            body.put("customMode", true);
            body.put("instrumental", false);
            body.put("model", "V3_5");
            body.put("negativeTags", request.getNegativeTags());
            body.put("callBackUrl", callbackUrl);

            // 🔍 실제 Suno로 보내질 JSON 출력
            System.out.println("\n================ [Suno API 전송 내용] ================");
            System.out.println("POST URL: " + postUrl);
            System.out.println("Headers: " + headers);
            System.out.println("Body: " + body);
            System.out.println("====================================================\n");

            /*
            if (false) {
                System.out.println("🔧 [TEST_MODE] Suno API 호출 생략됨");
                System.out.println("📦 body = " + body);
                return taskId;
            }

             */

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(postUrl, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return taskId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }





    }
}
