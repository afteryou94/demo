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

    // 2. 이 메서드를 반드시 오버라이드해야 시큐리티가 로그인을 처리합니다.
    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String memberId)
            throws org.springframework.security.core.userdetails.UsernameNotFoundException {

        // DB에서 아이디로 사용자 조회
        MemberEntity memberEntity = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("해당 아이디를 찾을 수 없습니다: " + memberId));

        // 시큐리티 전용 User 객체를 만들어 반환
        return org.springframework.security.core.userdetails.User.builder()
                .username(memberEntity.getMemberId())
                .password(memberEntity.getMemberPassword()) // 암호화된 비밀번호
                .roles(memberEntity.getRole().name())       // ROLE_USER 등
                .build();
    }
    // 1. 회원가입 저장
    public void save(MemberDTO memberDTO) {
        // 저장 전 비밀번호 암호화 필수!
        String encodedPassword = passwordEncoder.encode(memberDTO.getMemberPassword());
        memberDTO.setMemberPassword(encodedPassword);

        MemberEntity memberEntity = MemberEntity.toMemberEntity(memberDTO);

        // 일반 회원가입이므로 Role을 ROLE_USER로 명시해주는 것이 안전합니다.
        memberEntity.setRole(Role.USER);

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
    // 3. 로그인 로직
    public MemberDTO login(MemberDTO memberDTO) {
        Optional<MemberEntity> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        if (byMemberId.isPresent()) {
            MemberEntity memberEntity = byMemberId.get();
            // matches 메서드를 사용해서 (사용자 입력값, DB 암호화값)을 비교해야 합니다.
            if (passwordEncoder.matches(memberDTO.getMemberPassword(), memberEntity.getMemberPassword())) {
                return MemberDTO.toMemberDTO(memberEntity);
            }
        }
        return null;
    }

    //레포지토리에서 가져온 Entity를 컨트롤러가 쓰기 편한 DTO로 변환해서 반환
    public MemberDTO findByEmail(String loginEmail) {
        // 1. DB에서 이메일로 엔티티 조회
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findByMemberEmail(loginEmail);

        if (optionalMemberEntity.isPresent()) {
            // 2. 엔티티가 있으면 DTO로 변환하여 반환
            return MemberDTO.toMemberDTO(optionalMemberEntity.get());
        } else {
            // 3. 없으면 null 반환 (또는 예외 처리)
            return null;
        }
    }

    // MemberService.java에 추가
    public MemberDTO findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .map(MemberDTO::toMemberDTO)
                .orElse(null);
    }

    // 회원정보 수정
    @Transactional
    public void update(MemberUpdateDTO updateDTO) {
        MemberEntity memberEntity = memberRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        // 1. 닉네임 업데이트 (공통)
        memberEntity.setMemberNickname(updateDTO.getMemberNickname());

        // 2. 비밀번호 업데이트 (일반 사용자이며, 비밀번호를 입력했을 경우에만)
        // DB에 비밀번호가 있고(일반사용자), 입력폼이 비어있지 않다면 실행
        if (memberEntity.getMemberPassword() != null && !updateDTO.getMemberPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(updateDTO.getMemberPassword());
            memberEntity.setMemberPassword(encryptedPassword);
        }

        // @Transactional이 있으므로 별도의 repository.save() 호출 없이도 자동 저장됩니다.
    }

    // MemberService.java

    @Transactional // 데이터 수정을 위해 필수!
    public void updateNickname(String loginId, String memberNickname) {
        // memberId(또는 Email)로 기존 회원 찾기
        MemberEntity memberEntity = memberRepository.findByMemberEmail(loginId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        // 닉네임 변경 (Entity에 @Setter가 있거나 별도의 update 메서드가 있어야 함)
        memberEntity.setMemberNickname(memberNickname);

        // @Transactional이 있으면 save를 명시적으로 안 해도 감지하여 업데이트되지만, 안전하게 적어줌
        memberRepository.save(memberEntity);
    }

    public String nicknameCheck(String memberNickname) {
        Optional<MemberEntity> byMemberNickname = memberRepository.findByMemberNickname(memberNickname);
        if (byMemberNickname.isEmpty()) {
            return "ok"; // 사용 가능
        } else {
            return "no"; // 중복됨
        }
    }
}