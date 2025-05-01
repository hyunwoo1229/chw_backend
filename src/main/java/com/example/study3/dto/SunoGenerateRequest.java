package com.example.study3.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter

public class SunoGenerateRequest {
    private List<Map<String, String>> messages;

}
