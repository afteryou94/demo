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
    public @ResponseBody String mailAuth(@RequestParam("memberEmail") String memberEmail) {

        if (memberService.existsByEmail(memberEmail)) {
            return "duplicate";
        }


        int authCodeInt = mailService.sendMail(memberEmail);
        String authCode = String.valueOf(authCodeInt);
        return authCode;
    }


}