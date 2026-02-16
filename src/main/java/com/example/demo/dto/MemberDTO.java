package com.example.demo.dto;

import com.example.demo.entity.MemberEntity;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberDTO {
    private Long id;

    @Size(min = 6, max = 16, message = "아이디는 6~16자 사이여야 합니다.")
    // ID: 영어 대소문자, 숫자 포함 6~16자
    private String memberId;

    // PW: 6~16자, 영문 대소문자, 숫자, 특수문자 최소 1회 포함
    private String memberPassword;

    // 닉네임: 2~6자 한글, 영어, 숫자
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