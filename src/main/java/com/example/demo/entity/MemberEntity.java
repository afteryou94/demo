package com.example.demo.entity;

import com.example.demo.dto.MemberDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "member_table")
public class MemberEntity extends BaseTimeEntity { // 가입 시간 기록을 위해 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String memberId; // 영어 대소문자, 숫자 6~10자

    @Column(nullable = false)
    private String memberPassword; // 암호화된 비밀번호가 저장될 곳

    @Column(unique = true, nullable = false, length = 6)
    private String memberNickname; // 2~6자 닉네임

    @Column(unique = true, nullable = false)
    private String memberEmail; // 인증받은 이메일

    // DTO를 Entity로 변환하는 정적 메서드 (회원가입용)
    public static MemberEntity toMemberEntity(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setMemberId(memberDTO.getMemberId());
        memberEntity.setMemberPassword(memberDTO.getMemberPassword());
        memberEntity.setMemberNickname(memberDTO.getMemberNickname());
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        return memberEntity;
    }
}