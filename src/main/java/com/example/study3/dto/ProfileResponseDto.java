package com.example.study3.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResponseDto {
    private String loginId;
    private String name;
    private Integer age;
    private String gender;
    private String country;
    private String provider;
}
