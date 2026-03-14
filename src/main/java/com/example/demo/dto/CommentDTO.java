package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommentDTO {
    private Long id;
    private Long boardId;

    @NotBlank(message = "공백 또는 입력하지 않은 부분이 있습니다.")
    private String commentContents;

    // 비로그인 규칙: 2~10자 (한글, 영문, 숫자)
//    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,10}$", message = "작성자는 2~10자의 영문, 한글, 숫자만 가능합니다.")
    @NotBlank(message = "작성자를 입력해주세요.")
    private String commentWriter;

    // 비로그인 규칙: 최소 4자
//    @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다.")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String commentPass;

    private String memberEmail; // 회원 식별용
    private String commentCreatedAt;
}