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

    @PostMapping("/save")
    public ResponseEntity save(@Valid @ModelAttribute CommentDTO commentDTO,
                               BindingResult bindingResult,
                               HttpSession session) {

        // 1. 유효성 검사 (공백, 글자수 등)
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getFieldError().getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }

        String loginEmail = (String) session.getAttribute("loginEmail");
        String loginNickname = (String) session.getAttribute("loginNickname");

        if (loginEmail != null) {
            // 로그인 상태: 세션 정보 우선
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

    @PostMapping("/delete")
    public ResponseEntity delete(@RequestParam Long id,
                                 @RequestParam(required = false) String commentPass,
                                 HttpSession session) {
        String loginEmail = (String) session.getAttribute("loginEmail");
        try {
            commentService.delete(id, loginEmail, commentPass);
            return new ResponseEntity<>("삭제 성공", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/update")
    public ResponseEntity update(@Valid @ModelAttribute CommentDTO commentDTO,
                                 BindingResult bindingResult,
                                 HttpSession session) {

        // 1. 공백 및 유효성 검사
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getFieldError().getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }

        String loginEmail = (String) session.getAttribute("loginEmail");

        try {
            commentService.update(commentDTO, loginEmail);
            return new ResponseEntity<>("수정 성공", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}