package com.example.demo.entity;

import com.example.demo.dto.MemberDTO;
import jakarta.persistence.*;
import lombok.*;
import com.example.demo.domain.Role;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member_table")
public class MemberEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100)
    private String memberId;

    @Column(nullable = true)
    private String memberPassword;

    @Column(unique = true, nullable = false, length = 50)
    private String memberNickname;

    @Column(unique = true, nullable = false, length = 100)
    private String memberEmail;

    @Column
    private String memberName;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    public MemberEntity update(String name) {
        this.memberName = name;
        return this;
    }


    public String getRoleKey() {
        return this.role.getKey();
    }


    public static MemberEntity toMemberEntity(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setMemberId(memberDTO.getMemberId());
        memberEntity.setMemberPassword(memberDTO.getMemberPassword());
        memberEntity.setMemberNickname(memberDTO.getMemberNickname());
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        return memberEntity;
    }
}