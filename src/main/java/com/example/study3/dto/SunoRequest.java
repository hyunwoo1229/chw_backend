package com.example.study3.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SunoRequest {

    private String prompt;
    private String style;
    private String title;
    private boolean customMode;
    private boolean instrumental;
    private String model;
    private String negativeTags;
    private String callBackUrl;
}
