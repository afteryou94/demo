package com.example.demo.controller;

import com.example.demo.dto.MemberDTO;
import com.example.demo.dto.MemberUpdateDTO;
import com.example.demo.service.MailService;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.tomcat.util.modeler.BaseAttributeFilter;
import org.springframework.ui.Model; // 이 부분이 빠져서 에러가 날 확률이 높습니다!
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member") // 모든 주소가 /member로 시작함 (예: /member/save)
public class MemberController {
    private final MemberService memberService;
    private final MailService mailService;

    // 1. 회원가입 페이지 출력
    @GetMapping("/save")
    public String saveForm() {
        return "join"; // join.html을 보여줌
    }

    // 2. 회원가입 실행
    @PostMapping("/save")
    public String save(@ModelAttribute MemberDTO memberDTO) {
        // 사용자가 입력한 정보를 담은 DTO를 서비스로 넘겨서 DB에 저장
        memberService.save(memberDTO);
        return "login"; // 가입 완료 후 로그인 페이지로 이동
    }

    // 3. 로그인 페이지 출력
    @GetMapping("/login")
    public String loginForm() {
        return "login"; // login.html을 보여줌
    }

    // 4. 로그인 실행 - Spring Security가 대체
//    @PostMapping("/login")
//    public String login(@ModelAttribute MemberDTO memberDTO, HttpSession session, Model model) {
//        // 서비스에서 아이디/비번 일치 여부 확인 후 결과를 DTO로 받음
//        MemberDTO loginResult = memberService.login(memberDTO);
//        if (loginResult != null) {
//            // 로그인 성공 시 세션에 이메일과 닉네임 정보를 저장 (로그인 유지)
//            session.setAttribute("loginEmail", loginResult.getMemberEmail());
//            session.setAttribute("loginId", loginResult.getMemberId());
//            session.setAttribute("loginNickname", loginResult.getMemberNickname());
//            return "redirect:/board/"; // 로그인 후 게시판 목록으로 이동
//        } else {
//            // 로그인 실패 시 다시 로그인 페이지
//            model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호가 일치하지 않습니다.");
//            return "login";
//        }
//    }
    //5. 수정 화면 요청
    @GetMapping("/update")
    public String updateForm(HttpSession session, Model model) {
        // 5-1. 세션에서 로그인한 사용자의 식별자(아이디 또는 이메일)를 가져옵니다.
        // 시큐리티 설정에 따라 loginEmail이라는 이름에 memberId가 들어있을 수 있습니다.
        String loginId = (String) session.getAttribute("loginEmail");

        // 5-2. 로그인 상태가 아니라면 수정 페이지에 접근할 수 없도록 로그인 페이지로 보냅니다. (방어적 설계)
        if (loginId == null) {
            return "redirect:/member/login";
        }

        // 5-3. 서비스 계층을 통해 DB에서 현재 사용자의 상세 정보를 가져옵니다.
        // 포트폴리오 포인트: "사용자의 최신 정보를 안전하게 조회하기 위해 식별자(ID)를 사용했습니다."
        MemberDTO memberDTO = memberService.findByMemberId(loginId);

        if (memberDTO != null) {
            // 5-4. 가져온 데이터를 'updateMember'라는 이름으로 모델에 담아 HTML(memberUpdate.html)에 전달합니다.
            model.addAttribute("updateMember", memberDTO);
            return "memberUpdate";
        } else {
            // 만약 어떤 이유로 데이터를 못 찾았다면 게시판 메인으로 튕겨냅니다.
            return "redirect:/board/";
        }
    }
    //6.실제 수정 처리
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("updateMember") MemberUpdateDTO updateDTO,
                         BindingResult bindingResult, HttpSession session) {

        // 6-1. @Valid와 BindingResult를 사용해 입력값의 유효성(공백, 길이 등)을 검사합니다.
        // 포트폴리오 포인트: "서버 측 검증을 통해 잘못된 데이터가 DB에 저장되는 것을 방지했습니다."
        if (bindingResult.hasErrors()) {
            return "memberUpdate"; // 에러가 있다면 수정한 내용을 유지한 채 다시 수정 페이지로 보냅니다.
        }

        // 6-2. 서비스 계층의 update 메서드를 호출하여 실제 DB의 회원 정보를 변경합니다.
        memberService.update(updateDTO);

        // 6-3. [매우 중요] 세션 정보 동기화
        // DB만 바꾼다고 해서 상단 헤더에 표시되는 'OOO님 환영합니다'가 바로 바뀌지 않습니다.
        // 현재 세션에 저장된 loginNickname을 사용자가 새로 입력한 닉네임으로 갱신해줍니다.
        session.setAttribute("loginNickname", updateDTO.getMemberNickname());

        // 6-4. 모든 처리가 끝나면 게시판 메인으로 이동합니다.
        return "redirect:/board/";
    }

    // 6-5. 아이디 중복 체크 (Ajax 요청 처리)
    @PostMapping("/id-check")
    // @ResponseBody: HTML 전체가 아닌 "ok" 또는 "no" 같은 문자열 데이터만 보냄
    public @ResponseBody String idCheck(@RequestParam("memberId") String memberId) {
        String checkResult = memberService.idCheck(memberId);
        return checkResult; // 중복 여부에 따라 "ok" 또는 "no" 반환
    }

    // 7. 닉네임 중복 체크 (Ajax 요청 처리)
    @PostMapping("/nickname-check")
    public @ResponseBody String nicknameCheck(@RequestParam("memberNickname") String memberNickname) {
        // 회원가입 창과 소셜 로그인 설정 창 모두에서 사용됨
        String checkResult = memberService.nicknameCheck(memberNickname);
        return checkResult;
    }

    // 8. 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 정보를 무효화(삭제)하여 로그아웃 처리
        return "redirect:/"; // 메인 페이지로 이동
    }

    // 9. [소셜 로그인 전용] 닉네임 설정 페이지 요청
    @GetMapping("/set-nickname")
    public String setNicknameForm(HttpSession session, Model model) {
        // 소셜 로그인 성공 후 세션에 임시 저장된 이메일 정보를 꺼냄
        String email = (String) session.getAttribute("loginEmail");
        model.addAttribute("memberEmail", email);
        return "set-nickname"; // 닉네임 설정 화면 출력
    }

    // 10. [소셜 로그인 전용] 닉네임 저장 실행
    @PostMapping("/set-nickname")
    public String setNickname(@RequestParam("memberEmail") String memberEmail,
                              @RequestParam("memberNickname") String memberNickname,
                              HttpSession session) {
        // 소셜 계정 정보에 사용자가 입력한 닉네임을 업데이트
        memberService.updateNickname(memberEmail, memberNickname);
        // 업데이트된 닉네임을 세션에 새로 저장 (게시글 작성 시 활용)
        session.setAttribute("loginNickname", memberNickname);
        return "redirect:/board/";
    }

    // 11.이메일 인증번호 발송
    @PostMapping("/mail-auth")
    public @ResponseBody int mailAuth(@RequestParam("memberEmail") String memberEmail) {
        int code = mailService.sendMail(memberEmail);
        return code; // 생성된 6자리 번호를 프론트로 전달 (보안상 실제 서비스에선 세션/Redis 권장)
    }
}