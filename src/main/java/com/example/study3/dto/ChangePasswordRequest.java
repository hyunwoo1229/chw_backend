package com.example.study3.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}
