package com.example.demo.entity;

import com.example.demo.dto.MemberDTO;
import jakarta.persistence.*;
import lombok.*;
import com.example.demo.domain.Role;   // Role이 domain 패키지에 있다면

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // 빌더 패턴 추가
@Table(name = "member_table")
public class MemberEntity extends BaseTimeEntity { // 가입 시간 기록을 위해 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100)
    private String memberId; // 영어 대소문자, 숫자 6~10자

    @Column(nullable = true) // 소셜 로그인 사용자는 비밀번호가 없으므로 null 허용
    private String memberPassword;

    @Column(unique = false, nullable = false, length = 50) // 중복 허용 및 길이 확대
    private String memberNickname;

    @Column(unique = true, nullable = false, length = 100)
    private String memberEmail; // 인증받은 이메일

    @Column
    private String memberName;

    // 권한을 DB에 저장할 때 숫자가 아닌 문자열(ROLE_USER 등)로 저장함
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 소셜 정보를 업데이트하기 위한 메서드
    public MemberEntity update(String name) {
        this.memberName = name;
        return this;
    }

    // Role의 Key값을 가져오는 메서드
    public String getRoleKey() {
        return this.role.getKey();
    }

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