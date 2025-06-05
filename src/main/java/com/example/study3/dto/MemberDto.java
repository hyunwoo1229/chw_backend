package com.example.study3.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class MemberDto {
    private String loginId;
    private String password;
    private String name;
    private Integer age;
    private String gender;
    private String country;
}
