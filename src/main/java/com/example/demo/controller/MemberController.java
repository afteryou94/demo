package com.example.demo.controller;

import com.example.demo.dto.MemberDTO;
import com.example.demo.service.MailService;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model; // 이 부분이 빠져서 에러가 날 확률이 높습니다!
import org.springframework.web.bind.annotation.ModelAttribute;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MailService mailService;

    // 회원가입 페이지 출력
    @GetMapping("/save")
    public String saveForm() {
        return "join";
    }

    // 아이디 중복 확인 (Ajax)
    @PostMapping("/id-check")
    public @ResponseBody String idCheck(@RequestParam("memberId") String memberId) {
        String checkResult = memberService.idCheck(memberId);
        return checkResult; // "ok" 또는 null
    }

    // 이메일 인증번호 발송
    @PostMapping("/mail-auth")
    public @ResponseBody int mailAuth(@RequestParam("memberEmail") String memberEmail) {
        int code = mailService.sendMail(memberEmail);
        return code; // 생성된 6자리 번호를 프론트로 전달 (보안상 실제 서비스에선 세션/Redis 권장)
    }

    // 회원가입 처리
    @PostMapping("/save")
    public String save(@ModelAttribute MemberDTO memberDTO) {
        memberService.save(memberDTO);
        return "login"; // 가입 후 로그인 페이지로 이동
    }

    //로그인 로직
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute MemberDTO memberDTO, HttpSession session, Model model) {
        MemberDTO loginResult = memberService.login(memberDTO);
        if (loginResult != null) {
            // 로그인 성공: 세션에 사용자 정보 저장 (아이디와 닉네임)
            session.setAttribute("loginId", loginResult.getMemberId());
            session.setAttribute("loginNickname", loginResult.getMemberNickname());
            return "redirect:/board/"; // 리스트 화면으로 이동
        } else {
            // 로그인 실패
            model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "login";
        }
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 정보를 모두 무효화 (삭제)
        return "redirect:/board/";
    }
}