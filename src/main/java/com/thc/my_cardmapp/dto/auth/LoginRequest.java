package com.thc.my_cardmapp.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {

    @Schema(description = "이메일", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "비밀번호", example = "password123", required = true)
    private String password;
}
