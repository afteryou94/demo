package com.example.demo.controller;

import com.example.demo.dto.BoardDTO;
import com.example.demo.dto.CommentDTO;
import com.example.demo.service.CommentService;
import com.example.demo.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession; // HttpSession을 위해 필요
import jakarta.validation.Valid; // 추가
import org.springframework.validation.BindingResult; // 추가

import java.util.List;

    //1. 클래스 설정 및 주입
@Controller // 이 클래스가 웹 요청을 처리하는 컨트롤러임을 스프링에 알림
@RequiredArgsConstructor // final이 붙은 필드(boardService)를 매개변수로 하는 생성자를 자동으로 생성 (Lombok)
@RequestMapping("/board") // 이 클래스 내 모든 메서드의 주소 앞에 /board가 기본으로 붙음
public class BoardController {
    private final BoardService boardService; // 비즈니스 로직을 처리하는 서비스 객체를 주입받음
        private final CommentService commentService;
@GetMapping("/save")
public String saveForm(Model model) {
    model.addAttribute("boardDTO", new BoardDTO()); // 빈 객체를 넘겨줍니다.
    return "save";
}


//        }
@PostMapping("/save")
public String save(@Valid @ModelAttribute BoardDTO boardDTO, BindingResult bindingResult, HttpSession session) {
    // 1. 유효성 검사 결과 에러가 있다면 (비번 1자리 등)
    if (bindingResult.hasErrors()) {
        // 에러가 발생한 페이지(save.html)로 다시 돌려보냅니다.
        return "save";
    }

    String loginNickname = (String) session.getAttribute("loginNickname");
    String loginId = (String) session.getAttribute("loginEmail");

    if (loginId != null) {
        boardDTO.setBoardWriter(loginNickname);
        boardDTO.setMemberEmail(loginId);
    }

    boardService.save(boardDTO, loginId);
    return "redirect:/board/";
}

    //3. 목록 및 상세 조회 (Read)
    @GetMapping("/") //주소: GET /board/
    public String findall(Model model) {
        // DB에서 모든 게시글 데이터를 가져와서 리스트에 담음
        List<BoardDTO> boardDTOList = boardService.findAll();
        // 화면(HTML)으로 데이터를 전달하기 위해 모델에 담음 (이름: boardList)
        model.addAttribute("boardList", boardDTOList);
        return "list"; // templates/list.html 파일을 보여줌
    }
    //4. 수정 및 삭제 (Update & Delete)
        // BoardController.java
        @GetMapping("/{id}")
        public String findById(@PathVariable Long id, Model model) {
            boardService.updateHits(id);
            BoardDTO boardDTO = boardService.findById(id);

            // [추가] 해당 게시글의 댓글 목록 가져오기
            List<CommentDTO> commentDTOList = commentService.findAll(id);
            model.addAttribute("board", boardDTO);
            model.addAttribute("commentList", commentDTOList); // HTML의 th:each="comment: ${commentList}"와 연결됨

            return "detail";
        }

        @GetMapping("/delete/{id}")
        public String delete(@PathVariable Long id, HttpSession session) {
            String loginEmail = (String) session.getAttribute("loginEmail");
            BoardDTO boardDTO = boardService.findById(id);

            // 로그인 유저가 자기 글을 삭제하는 경우
            if (boardDTO.getMemberEmail() != null && boardDTO.getMemberEmail().equals(loginEmail)) {
                boardService.delete(id, loginEmail, null);
                return "redirect:/board/";
            }

            // 그 외(비로그인 유저 글이거나 타인의 글인 경우) -> 비밀번호 확인 페이지로 이동
            return "redirect:/board/delete-check/" + id;
        }

        // BoardController.java

        // 삭제 비밀번호 입력 화면 요청
        @GetMapping("/delete-check/{id}")
        public String deleteCheckForm(@PathVariable Long id, Model model) {
            model.addAttribute("id", id);
            return "delete-check";
        }



        // BoardController.java 수정
// 수정 화면 요청
        @GetMapping("/update/{id}")
        public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
            BoardDTO boardDTO = boardService.findById(id);
            String loginEmail = (String) session.getAttribute("loginEmail");

            // 1. 로그인 유저가 쓴 글인 경우
            if (boardDTO.getMemberEmail() != null) {
                if (!boardDTO.getMemberEmail().equals(loginEmail)) {
                    return "redirect:/board/?error=unauthorized";
                }
                model.addAttribute("boardUpdate", boardDTO);
                return "update"; // 바로 수정 페이지로
            }

            // 2. 비로그인 유저가 쓴 글인 경우 (memberEmail이 null인 경우)
            // 바로 수정페이지로 보내되, 수정 완료 시 비밀번호를 체크하게 합니다.
            model.addAttribute("boardUpdate", boardDTO);
            return "update";
        }

        // 실제 수정 처리
        @PostMapping("/update")
        public String update(@ModelAttribute BoardDTO boardDTO, HttpSession session) {
            String loginEmail = (String) session.getAttribute("loginEmail");

            try {
                // DTO에 담긴 boardPass를 서비스의 3번째 인자로 전달합니다.
                boardService.update(boardDTO, loginEmail, boardDTO.getBoardPass());
                return "redirect:/board/" + boardDTO.getId();
            } catch (RuntimeException e) {
                return "redirect:/board/?error=auth";
            }
        }
        // 비밀번호 입력 후 삭제 버튼을 눌렀을 때 호출되는 메서드
        // BoardController.java

        // 비밀번호 입력 후 삭제 버튼을 눌렀을 때 호출되는 메서드
        @PostMapping("/delete") // 클래스 상단의 /board와 합쳐져서 실제 주소는 /board/delete가 됨
        public String delete(@RequestParam("id") Long id,
                             @RequestParam("boardPass") String boardPass,
                             HttpSession session) {

            String loginEmail = (String) session.getAttribute("loginEmail");

            try {
                boardService.delete(id, loginEmail, boardPass);
                return "redirect:/board/";
            } catch (RuntimeException e) {
                // [중요] 여기서 튕길 때 /board/가 아니라 /member/login으로 가고 있지 않은지 확인!
                return "redirect:/board/" + id + "?error=auth";
            }
        }


}
