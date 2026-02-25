package com.example.demo.controller;

import com.example.demo.dto.CommentDTO;
import com.example.demo.service.CommentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;

    /**
     * 1. 댓글 저장 (Ajax)
     * ResponseEntity를 사용하여 데이터와 함께 HTTP 상태 코드를 세밀하게 반환합니다.
     */
    @PostMapping("/save")
    public ResponseEntity save(@Valid @ModelAttribute CommentDTO commentDTO,
                               BindingResult bindingResult,
                               HttpSession session) {

        // 1-1. 유효성 검사: 댓글 내용이 없거나 형식이 틀리면 에러 메시지 반환
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getFieldError().getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }

        // 1-2. 세션에서 로그인 정보 추출
        String loginEmail = (String) session.getAttribute("loginEmail");
        String loginNickname = (String) session.getAttribute("loginNickname");

        // 1-3. 로그인 상태라면 세션의 검증된 닉네임과 이메일을 강제로 주입 (보안 강화)
        if (loginEmail != null) {
            commentDTO.setMemberEmail(loginEmail);
            commentDTO.setCommentWriter(loginNickname);
        }

        try {
            Long saveResult = commentService.save(commentDTO);
            if (saveResult != null) {
                // 저장 성공 시 해당 게시글의 전체 댓글 목록을 다시 가져와서 반환
                List<CommentDTO> commentDTOList = commentService.findAll(commentDTO.getBoardId());
                return new ResponseEntity<>(commentDTOList, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("해당 게시글이 존재하지 않습니다.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 2. 댓글 삭제 (Ajax)
     */
    @PostMapping("/delete")
    public ResponseEntity delete(@RequestParam Long id,
                                 @RequestParam(required = false) String commentPass,
                                 HttpSession session) {
        String loginEmail = (String) session.getAttribute("loginEmail");
        try {
            // 2-1. 서비스에서 권한 검증(본인 확인 또는 비번 확인) 후 삭제 진행
            commentService.delete(id, loginEmail, commentPass);
            return new ResponseEntity<>("삭제 성공", HttpStatus.OK);
        } catch (RuntimeException e) {
            // 2-2. 권한이 없거나 비번이 틀리면 에러 메시지 반환
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 3. 댓글 수정 (Ajax)
     */
    @PostMapping("/update")
    public ResponseEntity update(@Valid @ModelAttribute CommentDTO commentDTO,
                                 BindingResult bindingResult,
                                 HttpSession session) {

        // 3-1. 수정한 내용이 유효한지 검사
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getFieldError().getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }

        String loginEmail = (String) session.getAttribute("loginEmail");

        try {
            // 3-2. 수정 권한 확인 후 업데이트
            commentService.update(commentDTO, loginEmail);
            return new ResponseEntity<>("수정 성공", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}