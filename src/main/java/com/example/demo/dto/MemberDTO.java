package com.example.demo.dto;

import com.example.demo.entity.MemberEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberDTO {
    private Long id;

    // 1. 아이디: 6~16자 영문 대소문자, 숫자 (공백 불가)
    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,16}$",
            message = "아이디는 영문 대소문자와 숫자만 사용하여 6~16자로 입력해주세요.")
    private String memberId;

    // 2. 비밀번호: 영문 대/소문자, 숫자, 특수문자 각 1회 이상 포함, 8~16자 (공백 불가)
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
            message = "비밀번호는 영문 대문자, 소문자, 숫자, 특수문자를 포함하여 8~16자로 입력해주세요.")
    private String memberPassword;

    // 3. 닉네임: 영문 대소문자, 한글, 숫자 포함하여 2~10자 (공백 불가)
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,10}$",
            message = "닉네임은 특수문자와 공백 없이 2~10자로 입력해주세요.")
    private String memberNickname;

    // 이메일: 점유 인증을 거칠 주소
    private String memberEmail;
    private String memberName;

    // Entity를 DTO로 변환 (조회용)
    public static MemberDTO toMemberDTO(MemberEntity memberEntity) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId(memberEntity.getId());
        memberDTO.setMemberId(memberEntity.getMemberId());
        memberDTO.setMemberPassword(memberEntity.getMemberPassword());
        memberDTO.setMemberNickname(memberEntity.getMemberNickname());
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        memberDTO.setMemberName(memberEntity.getMemberName());
        return memberDTO;
    }
}