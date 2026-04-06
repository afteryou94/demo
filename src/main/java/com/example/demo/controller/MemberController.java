package com.example.demo.controller;

import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.MemberUpdateDTO;
import com.example.demo.service.MailService;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.tomcat.util.modeler.BaseAttributeFilter;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final MailService mailService;


    @GetMapping("/save")
    public String saveForm(Model model) {

        model.addAttribute("memberDTO", new MemberDTO());
        return "join";
    }


    @PostMapping("/save")
    public String save(@Valid @ModelAttribute MemberDTO memberDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {

            System.out.println("검증 에러 발생: " + bindingResult.getAllErrors());
            return "join";
        }
        memberService.save(memberDTO);
        return "login";
    }


    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "exception", required = false) String exception, Model model) {

        if (error != null) {

            model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        return "login";
    }


    @GetMapping("/update")
    public String updateForm(HttpSession session, Model model) {


        String loginId = (String) session.getAttribute("loginEmail");


        if (loginId == null) {
            return "redirect:/member/login";
        }


        MemberDTO memberDTO = memberService.findByMemberId(loginId);

        if (memberDTO != null) {

            model.addAttribute("updateMember", memberDTO);
            return "memberUpdate";
        } else {

            return "redirect:/board/";
        }
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("updateMember") MemberUpdateDTO updateDTO, BindingResult bindingResult, HttpSession session) {


        if (bindingResult.hasErrors()) {
            return "memberUpdate";
        }


        memberService.update(updateDTO);


        session.setAttribute("loginNickname", updateDTO.getMemberNickname());


        return "redirect:/board/";
    }


    @PostMapping("/id-check")

    public @ResponseBody String idCheck(@RequestParam("memberId") String memberId) {
        String checkResult = memberService.idCheck(memberId);
        return checkResult;
    }


    @PostMapping("/nickname-check")
    public @ResponseBody String nicknameCheck(@RequestParam("memberNickname") String memberNickname) {

        String checkResult = memberService.nicknameCheck(memberNickname);
        return checkResult;
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }


    @GetMapping("/set-nickname")
    public String setNicknameForm(HttpSession session, Model model) {

        String email = (String) session.getAttribute("loginEmail");
        model.addAttribute("memberEmail", email);
        return "set-nickname";
    }


    @PostMapping("/set-nickname")
    public String setNickname(@RequestParam("memberEmail") String memberEmail, @RequestParam("memberNickname") String memberNickname, HttpSession session) {

        memberService.updateNickname(memberEmail, memberNickname);

        session.setAttribute("loginNickname", memberNickname);
        return "redirect:/board/";
    }


    @PostMapping("/mail-auth")
    public @ResponseBody String mailAuth(@RequestParam("memberEmail") String memberEmail, HttpSession session) {

        if (memberService.existsByEmail(memberEmail)) {
            return "duplicate";
        }


        int authCode = mailService.sendMail(memberEmail);
        // [변경] 브라우저에 번호를 주는 대신, 서버 세션에 저장
        session.setAttribute("serverAuthCode", String.valueOf(authCode));
        // [중요] 현재 시간을 밀리초(ms) 단위로 저장
        session.setAttribute("authCodeTime", System.currentTimeMillis());

        return "ok"; // 번호 대신 성공 메시지만 리턴
    }

    @PostMapping("/verify-code")
    public @ResponseBody String verifyCode(@RequestParam("userCode") String userCode, HttpSession session) {
        String serverCode = (String) session.getAttribute("serverAuthCode");
        Long createTime = (Long) session.getAttribute("authCodeTime");

        if (serverCode == null || createTime == null) {
            return "expired"; // 세션이 이미 만료됨
        }

        // 현재 시간과 생성 시간의 차이 계산 (1000ms * 60s * 3m = 180,000ms)
        long currentTime = System.currentTimeMillis();
        if (currentTime - createTime > 3 * 60 * 1000) {
            session.removeAttribute("serverAuthCode");
            session.removeAttribute("authCodeTime");
            return "timeout"; // 3분 지남
        }

        if (userCode.equals(serverCode)) {
            session.removeAttribute("serverAuthCode");
            return "success";
        }
        return "fail";
    }


}