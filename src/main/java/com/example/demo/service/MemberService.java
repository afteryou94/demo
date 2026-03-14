package com.example.demo.service;

import com.example.demo.domain.Role;
import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.MemberUpdateDTO;
import com.example.demo.entity.MemberEntity;
import com.example.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor

public class MemberService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * [스프링 시큐리티 전용] 사용자 인증 메서드
     * 시큐리티가 로그인을 시도할 때 DB에서 사용자 정보를 가져오는 역할을 합니다.
     */
    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String memberId)
            throws org.springframework.security.core.userdetails.UsernameNotFoundException {


        MemberEntity memberEntity = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("해당 아이디를 찾을 수 없습니다: " + memberId));


        return org.springframework.security.core.userdetails.User.builder()
                .username(memberEntity.getMemberId())
                .password(memberEntity.getMemberPassword())
                .roles(memberEntity.getRole().name())
                .build();
    }

    /**
     * 1. 회원가입 저장
     */
    public void save(MemberDTO memberDTO) {

        String encodedPassword = passwordEncoder.encode(memberDTO.getMemberPassword());
        memberDTO.setMemberPassword(encodedPassword);


        MemberEntity memberEntity = MemberEntity.toMemberEntity(memberDTO);


        memberEntity.setRole(Role.USER);


        memberRepository.save(memberEntity);
    }

    /**
     * 2. 아이디 중복 확인 (Ajax 전용)
     */
    public String idCheck(String memberId) {
        Optional<MemberEntity> byMemberId = memberRepository.findByMemberId(memberId);
        if (byMemberId.isPresent()) {
            return null;
        } else {
            return "ok";
        }
    }

    /**
     * 3. 로그인 로직 (시큐리티 미사용 시 수동 로그인용)
     */
    public MemberDTO login(MemberDTO memberDTO) {
        Optional<MemberEntity> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        if (byMemberId.isPresent()) {
            MemberEntity memberEntity = byMemberId.get();

            if (passwordEncoder.matches(memberDTO.getMemberPassword(), memberEntity.getMemberPassword())) {
                return MemberDTO.toMemberDTO(memberEntity);
            }
        }
        return null;
    }

    /**
     * 4. 레포지토리에서 가져온 Entity를 컨트롤러가 쓰기 편한 DTO로 변환해서 반환
     */
    public MemberDTO findByEmail(String loginEmail) {

        Optional<MemberEntity> optionalMemberEntity = memberRepository.findByMemberEmail(loginEmail);

        if (optionalMemberEntity.isPresent()) {

            return MemberDTO.toMemberDTO(optionalMemberEntity.get());
        } else {

            return null;
        }
    }

    /**
     * 5. 아이디로 사용자 찾기 (회원 정보 수정 폼 데이터 로드용)
     */
    public MemberDTO findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .map(MemberDTO::toMemberDTO)
                .orElse(null);
    }

    /**
     * 6. 회원 정보 수정 로직
     */
    @Transactional
    public void update(MemberUpdateDTO updateDTO) {

        MemberEntity memberEntity = memberRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));


        memberEntity.setMemberNickname(updateDTO.getMemberNickname());


        if (memberEntity.getMemberPassword() != null && !updateDTO.getMemberPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(updateDTO.getMemberPassword());
            memberEntity.setMemberPassword(encryptedPassword);
        }

    }

    /**
     * 7. [소셜 로그인] 닉네임 설정 및 업데이트
     */
    @Transactional
    public void updateNickname(String loginId, String memberNickname) {
        MemberEntity memberEntity = memberRepository.findByMemberEmail(loginId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        memberEntity.setMemberNickname(memberNickname);

        memberRepository.save(memberEntity);
    }

    /**
     * 8. 닉네임 중복 확인 (Ajax 전용)
     */
    public String nicknameCheck(String memberNickname) {
        Optional<MemberEntity> byMemberNickname = memberRepository.findByMemberNickname(memberNickname);
        if (byMemberNickname.isEmpty()) {
            return "ok";
        } else {
            return "no";
        }
    }


    public boolean existsByEmail(String email) {

        return memberRepository.existsByMemberEmail(email);
    }
}