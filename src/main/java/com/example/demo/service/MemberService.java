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
// UserDetailsService: 스프링 시큐리티가 로그인을 처리할 때 사용하는 인터페이스를 구현합니다.
public class MemberService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder; // 비밀번호 암호화를 위한 객체

    /**
     * [스프링 시큐리티 전용] 사용자 인증 메서드
     * 시큐리티가 로그인을 시도할 때 DB에서 사용자 정보를 가져오는 역할을 합니다.
     */
    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String memberId)
            throws org.springframework.security.core.userdetails.UsernameNotFoundException {

        // 1. DB에서 아이디로 사용자를 조회합니다.
        MemberEntity memberEntity = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("해당 아이디를 찾을 수 없습니다: " + memberId));

        // 2. 시큐리티가 이해할 수 있는 UserDetails 객체(User)를 생성하여 반환합니다.
        // 이때 비밀번호는 암호화된 상태여야 시큐리티가 비교할 수 있습니다.
        return org.springframework.security.core.userdetails.User.builder()
                .username(memberEntity.getMemberId())
                .password(memberEntity.getMemberPassword()) // 암호화된 비밀번호
                .roles(memberEntity.getRole().name())       // 권한 부여 (USER, ADMIN 등)
                .build();
    }
    /**
     * 1. 회원가입 저장
     */
    public void save(MemberDTO memberDTO) {
        // 1-1. 저장 전 비밀번호 암호화는 필수입니다! (BCrypt 사용)
        String encodedPassword = passwordEncoder.encode(memberDTO.getMemberPassword());
        memberDTO.setMemberPassword(encodedPassword);

        // 1-2. DTO를 DB에 저장할 엔티티로 변환합니다.
        MemberEntity memberEntity = MemberEntity.toMemberEntity(memberDTO);

        // 1-3. 일반 회원가입 사용자에게 기본 권한(ROLE_USER)을 부여합니다.
        memberEntity.setRole(Role.USER);

        // 1-4. DB 저장
        memberRepository.save(memberEntity);
    }

    /**
     * 2. 아이디 중복 확인 (Ajax 전용)
     */
    public String idCheck(String memberId) {
        Optional<MemberEntity> byMemberId = memberRepository.findByMemberId(memberId);
        if (byMemberId.isPresent()) {
            return null; // 이미 있으면 사용 불가
        } else {
            return "ok"; // 없으면 사용 가능
        }
    }
    /**
     * 3. 로그인 로직 (시큐리티 미사용 시 수동 로그인용)
     */
    public MemberDTO login(MemberDTO memberDTO) {
        Optional<MemberEntity> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        if (byMemberId.isPresent()) {
            MemberEntity memberEntity = byMemberId.get();
            // passwordEncoder.matches: (평문 비밀번호, 암호화된 비밀번호)를 대조해줍니다.
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
        // 4-1. DB에서 이메일로 엔티티 조회
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findByMemberEmail(loginEmail);

        if (optionalMemberEntity.isPresent()) {
            // 4-2. 엔티티가 있으면 DTO로 변환하여 반환
            return MemberDTO.toMemberDTO(optionalMemberEntity.get());
        } else {
            // 4-3. 없으면 null 반환 (또는 예외 처리)
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
    @Transactional // 더티 체킹(변경 감지)을 활용한 자동 업데이트
    public void update(MemberUpdateDTO updateDTO) {
        // 6-1. 수정할 회원을 찾아오기
        MemberEntity memberEntity = memberRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        // 6-2. 닉네임 업데이트
        memberEntity.setMemberNickname(updateDTO.getMemberNickname());

        // 6-3. 비밀번호 업데이트 (비밀번호를 입력한 경우에만 암호화해서 변경)
        if (memberEntity.getMemberPassword() != null && !updateDTO.getMemberPassword().isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(updateDTO.getMemberPassword());
            memberEntity.setMemberPassword(encryptedPassword);
        }
        // @Transactional이 걸려있으므로, 트랜잭션이 끝날 때 변경된 사항이 DB에 자동으로 반영됩니다.
    }

    /**
     * 7. [소셜 로그인] 닉네임 설정 및 업데이트
     */
    @Transactional
    public void updateNickname(String loginId, String memberNickname) {
        MemberEntity memberEntity = memberRepository.findByMemberEmail(loginId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        memberEntity.setMemberNickname(memberNickname);
        // 수동으로 save를 호출해줘도 무방합니다.
        memberRepository.save(memberEntity);
    }

    /**
     * 8. 닉네임 중복 확인 (Ajax 전용)
     */
    public String nicknameCheck(String memberNickname) {
        Optional<MemberEntity> byMemberNickname = memberRepository.findByMemberNickname(memberNickname);
        if (byMemberNickname.isEmpty()) {
            return "ok"; // 중복되지 않음
        } else {
            return "no"; // 중복됨
        }
    }

//    //이메일 중복 검사
//    public String emailCheck(String memberEmail) {
//        // repository에 findByMemberEmail이 있다면 활용, 없다면 existsByMemberEmail 추가
//        Optional<MemberEntity> byMemberEmail = memberRepository.findByMemberEmail(memberEmail);
//        if (byMemberEmail.isPresent()) {
//            return "no"; // 이미 존재함
//        } else {
//            return "ok"; // 가입 가능
//        }
//    }

    public boolean existsByEmail(String email) {
        // repository에서 해당 이메일로 가입된 정보가 있는지 확인 (true/false 반환)
        return memberRepository.existsByMemberEmail(email);
    }
}