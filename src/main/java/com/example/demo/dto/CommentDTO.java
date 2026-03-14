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

    

    @NotBlank(message = "작성자를 입력해주세요.")
    private String commentWriter;

    

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String commentPass;

    private String memberEmail; 
    private String commentCreatedAt;
}