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


        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getFieldError().getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }


        String loginEmail = (String) session.getAttribute("loginEmail");
        String loginNickname = (String) session.getAttribute("loginNickname");


        if (loginEmail == null) {
            if (!commentDTO.getCommentWriter().matches("^[a-zA-Z0-9가-힣]{2,10}$")) {
                return new ResponseEntity<>("작성자는 2~10자의 영문, 한글, 숫자만 가능합니다.", HttpStatus.BAD_REQUEST);
            }
            if (commentDTO.getCommentPass() == null || commentDTO.getCommentPass().length() < 4) {
                return new ResponseEntity<>("비밀번호는 4자 이상이어야 합니다.", HttpStatus.BAD_REQUEST);
            }
        } else {

            commentDTO.setMemberEmail(loginEmail);
            commentDTO.setCommentWriter(loginNickname);
        }


        try {
            Long saveResult = commentService.save(commentDTO);
            System.out.println("저장된 댓글 번호: " + saveResult);

            if (saveResult != null) {
                List<CommentDTO> commentDTOList = commentService.findAll(commentDTO.getBoardId());
                System.out.println("가져온 댓글 개수: " + commentDTOList.size());
                return new ResponseEntity<>(commentDTOList, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("해당 게시글이 존재하지 않습니다.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("서버 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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

            commentService.delete(id, loginEmail, commentPass);
            return new ResponseEntity<>("삭제 성공", HttpStatus.OK);
        } catch (RuntimeException e) {

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