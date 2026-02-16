package com.example.demo.controller;

import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.MemberUpdateDTO;
import com.example.demo.service.MailService;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.ui.Model; // 이 부분이 빠져서 에러가 날 확률이 높습니다!
import org.springframework.validation.BindingResult;
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

//    @PostMapping("/login")
//    public String login(@ModelAttribute MemberDTO memberDTO, HttpSession session, Model model) {
//        MemberDTO loginResult = memberService.login(memberDTO);
//        if (loginResult != null) {
//            // 로그인 성공: 세션에 사용자 정보 저장 (아이디와 닉네임)
//            session.setAttribute("loginEmail", loginResult.getMemberEmail());
//            session.setAttribute("loginId", loginResult.getMemberId());
//            session.setAttribute("loginNickname", loginResult.getMemberNickname());
//            return "redirect:/board/"; // 리스트 화면으로 이동
//        } else {
//            // 로그인 실패
//            model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호가 일치하지 않습니다.");
//            return "login";
//        }
//    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 정보를 모두 무효화 (삭제)
        return "redirect:/board/";
    }

    //회원정보 수정
    // MemberController.java

    // 1. 수정 화면 요청
// MemberController.java
// MemberController.java의 updateForm 메서드를 아래 내용으로 교체하세요
    @GetMapping("/update")
    public String updateForm(HttpSession session, Model model) {
        // SecurityConfig의 successHandler에서 "loginEmail"이라는 이름으로 memberId를 담았습니다.
        String loginId = (String) session.getAttribute("loginEmail");

        if (loginId == null) {
            return "redirect:/member/login";
        }

        // [중요] 현재 DB 구조상 memberId로 사용자를 찾아야 합니다.
        // 만약 loginEmail 세션에 진짜 이메일 주소가 들어있다면 findByEmail을 쓰고,
        // memberId(아이디)가 들어있다면 findByMemberId를 써야 합니다.

        // 안전하게 가기 위해 memberService에 '아이디로 찾는 메서드'를 사용하도록 변경합니다.
        MemberDTO memberDTO = memberService.findByMemberId(loginId);

        if (memberDTO != null) {
            model.addAttribute("updateMember", memberDTO);
            return "memberUpdate";
        } else {
            // 데이터가 없으면 다시 게시판으로
            return "redirect:/board/";
        }
    }

    // 2. 실제 수정 처리
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("updateMember") MemberUpdateDTO updateDTO,
                         BindingResult bindingResult, HttpSession session) {

        if (bindingResult.hasErrors()) {
            return "memberUpdate"; // 유효성 검사 실패 시 다시 수정 페이지로
        }

        memberService.update(updateDTO);

        // 세션 닉네임 갱신 (헤더 등에 표시되는 이름 변경)
        session.setAttribute("loginNickname", updateDTO.getMemberNickname());

        return "redirect:/board/"; // 수정 완료 후 게시판 목록으로
    }
}