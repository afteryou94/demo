package com.example.demo.service;

import com.example.demo.domain.Role;
import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.MemberUpdateDTO;
import com.example.demo.entity.MemberEntity;
import com.example.demo.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @InjectMocks MemberService memberService;

    private MemberEntity member;

    @BeforeEach
    void setUp() {
        member = MemberEntity.builder()
                .id(1L)
                .memberId("test123")
                .memberPassword("encodedPassword")
                .memberNickname("테스터")
                .memberEmail("test@example.com")
                .memberName("홍길동")
                .role(Role.USER)
                .build();
    }

    @Test
    void 회원가입_비밀번호를_암호화해서_저장한다() {
        MemberDTO dto = new MemberDTO(null, "test123", "RawPass1!", "테스터", "test@example.com", "홍길동");
        when(passwordEncoder.encode("RawPass1!")).thenReturn("encodedPassword");

        memberService.save(dto);

        assertEquals("encodedPassword", dto.getMemberPassword());
        verify(passwordEncoder).encode("RawPass1!");
        verify(memberRepository).save(argThat(saved ->
                "test123".equals(saved.getMemberId()) &&
                "encodedPassword".equals(saved.getMemberPassword()) &&
                Role.USER == saved.getRole()));
    }

    @Test
    void 아이디가_존재하면_idCheck는_null을_반환한다() {
        when(memberRepository.findByMemberId("test123")).thenReturn(Optional.of(member));
        assertNull(memberService.idCheck("test123"));
    }

    @Test
    void 아이디가_없으면_idCheck는_ok를_반환한다() {
        when(memberRepository.findByMemberId("new123")).thenReturn(Optional.empty());
        assertEquals("ok", memberService.idCheck("new123"));
    }

    @Test
    void 올바른_비밀번호면_로그인_회원정보를_반환한다() {
        MemberDTO login = new MemberDTO();
        login.setMemberId("test123");
        login.setMemberPassword("RawPass1!");
        when(memberRepository.findByMemberId("test123")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("RawPass1!", "encodedPassword")).thenReturn(true);

        MemberDTO result = memberService.login(login);

        assertNotNull(result);
        assertEquals("test123", result.getMemberId());
        assertEquals("test@example.com", result.getMemberEmail());
    }

    @Test
    void 틀린_비밀번호면_로그인은_null이다() {
        MemberDTO login = new MemberDTO();
        login.setMemberId("test123");
        login.setMemberPassword("WrongPass1!");
        when(memberRepository.findByMemberId("test123")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("WrongPass1!", "encodedPassword")).thenReturn(false);

        assertNull(memberService.login(login));
    }

    @Test
    void 존재하지_않는_아이디로_로그인하면_null이다() {
        MemberDTO login = new MemberDTO();
        login.setMemberId("unknown");
        login.setMemberPassword("RawPass1!");
        when(memberRepository.findByMemberId("unknown")).thenReturn(Optional.empty());

        assertNull(memberService.login(login));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void 회원정보_수정시_닉네임과_새_비밀번호를_변경한다() {
        MemberUpdateDTO dto = new MemberUpdateDTO();
        dto.setId(1L);
        dto.setMemberNickname("새닉네임");
        dto.setMemberPassword("NewPass1!");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.encode("NewPass1!")).thenReturn("newEncoded");

        memberService.update(dto);

        assertEquals("새닉네임", member.getMemberNickname());
        assertEquals("newEncoded", member.getMemberPassword());
    }

    @Test
    void 회원정보_수정시_비밀번호가_비어있으면_기존_비밀번호를_유지한다() {
        MemberUpdateDTO dto = new MemberUpdateDTO();
        dto.setId(1L);
        dto.setMemberNickname("새닉네임");
        dto.setMemberPassword("");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        memberService.update(dto);

        assertEquals("새닉네임", member.getMemberNickname());
        assertEquals("encodedPassword", member.getMemberPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void 존재하지_않는_회원정보_수정은_예외가_발생한다() {
        MemberUpdateDTO dto = new MemberUpdateDTO();
        dto.setId(999L);
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> memberService.update(dto));
    }

    @Test
    void 닉네임이_없으면_nicknameCheck는_ok다() {
        when(memberRepository.findByMemberNickname("새닉네임")).thenReturn(Optional.empty());
        assertEquals("ok", memberService.nicknameCheck("새닉네임"));
    }

    @Test
    void 닉네임이_존재하면_nicknameCheck는_no다() {
        when(memberRepository.findByMemberNickname("테스터")).thenReturn(Optional.of(member));
        assertEquals("no", memberService.nicknameCheck("테스터"));
    }

    @Test
    void 이메일이_존재하는지_확인한다() {
        when(memberRepository.existsByMemberEmail("test@example.com")).thenReturn(true);
        assertTrue(memberService.existsByEmail("test@example.com"));
        verify(memberRepository).existsByMemberEmail("test@example.com");
    }

    @Test
    void 아이디로_회원정보를_조회한다() {
        when(memberRepository.findByMemberId("test123")).thenReturn(Optional.of(member));
        MemberDTO result = memberService.findByMemberId("test123");
        assertNotNull(result);
        assertEquals("테스터", result.getMemberNickname());
    }
}
