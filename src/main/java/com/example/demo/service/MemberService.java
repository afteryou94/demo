package com.example.demo.service;

import com.example.demo.dto.MemberDTO;
import com.example.demo.entity.MemberEntity;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // 1. 회원가입 저장
    public void save(MemberDTO memberDTO) {
        // DTO -> Entity 변환
        MemberEntity memberEntity = MemberEntity.toMemberEntity(memberDTO);
        // Repository의 save 호출 (조건: id가 없으므로 insert 쿼리 수행)
        memberRepository.save(memberEntity);
    }

    // 2. 아이디 중복 확인
    public String idCheck(String memberId) {
        Optional<MemberEntity> byMemberId = memberRepository.findByMemberId(memberId);
        if (byMemberId.isPresent()) {
            // 이미 값이 있으면 사용할 수 없음
            return null;
        } else {
            // 값이 없으면 사용 가능
            return "ok";
        }
    }
}