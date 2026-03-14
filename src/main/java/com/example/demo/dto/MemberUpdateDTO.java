package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateDTO {
    private Long id;

    private String memberId; 

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다.")
    @Pattern(regexp = "^\\S*$", message = "닉네임에 공백을 포함할 수 없습니다.")
    private String memberNickname;

    
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$|^$",
            message = "비밀번호는 영문 대 소문자, 숫자, 특수문자를 포함하여 8~16자여야 합니다."
    )
    private String memberPassword;

    private boolean socialUser; 
}