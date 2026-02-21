package com.example.demo.repository;

import com.example.demo.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    // 아이디로 회원 정보 조회 (중복 확인 및 로그인 시 사용)
    Optional<MemberEntity> findByMemberId(String memberId);

    // 이메일로 회원 정보 조회 (이메일 중복 가입 방지 시 사용)
    Optional<MemberEntity> findByMemberEmail(String memberEmail);

    Optional<MemberEntity> findByMemberNickname(String memberNickname);
}