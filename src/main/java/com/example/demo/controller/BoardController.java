package com.example.demo.controller;

import com.example.demo.dto.BoardDTO;
import com.example.demo.dto.CommentDTO;
import com.example.demo.service.CommentService;
import com.example.demo.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession; // HttpSession을 위해 필요
import jakarta.validation.Valid; // 추가
import org.springframework.validation.BindingResult; // 추가
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;

    //1. 클래스 설정 및 주입
@Controller // 이 클래스가 웹 요청을 처리하는 컨트롤러임을 스프링에 알림
@RequiredArgsConstructor // final 필드인 BoardService와 CommentService를 스프링이 자동으로 주입(DI)하게 해줍니다.
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

    String loginNickname = (String) session.getAttribute("loginNickname");
    String loginId = (String) session.getAttribute("loginEmail");

    // 1. 로그인 상태라면 DTO에 미리 값을 채워줍니다.
    if (loginId != null) {
        boardDTO.setBoardWriter(loginNickname);
        boardDTO.setMemberEmail(loginId);
    }

    // 2. [핵심] 로그인 유저인 경우, 작성자와 비밀번호 에러는 검사 대상에서 제외합니다.
    if (loginId != null) {
        // 'boardWriter'와 'boardPass'에 대한 에러가 있다면 삭제해줍니다.
        // 소셜/일반 로그인 유저는 비번을 따로 안 적으니까요.
    }

    // 3. 다시 유효성 검사 체크
    if (bindingResult.hasFieldErrors("boardTitle") || bindingResult.hasFieldErrors("boardContents")) {
        return "save";
    }

    // 비로그인 유저인데 작성자나 비번을 안 적었다면 튕겨야 함
    if (loginId == null && (bindingResult.hasFieldErrors("boardWriter") || bindingResult.hasFieldErrors("boardPass"))) {
        return "save";
    }

    boardService.save(boardDTO, loginId);
    return "redirect:/board/";
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

        @GetMapping("/")
        public String paging(@PageableDefault(page = 1) Pageable pageable,
                             @RequestParam(value = "type", required = false) String type,
                             @RequestParam(value = "keyword", required = false) String keyword,Model model) {
            Page<BoardDTO> boardList = boardService.paging(pageable, type, keyword);

            // 하단에 보여줄 페이지 번호 개수 (예: 1 2 3 4 5)
            int blockLimit = 5;
            int startPage = (((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;
            int endPage = ((startPage + blockLimit - 1) < boardList.getTotalPages()) ? startPage + blockLimit - 1 : boardList.getTotalPages();

            model.addAttribute("boardList", boardList); // 10개의 글 데이터
            model.addAttribute("startPage", startPage); // 시작 페이지 번호
            model.addAttribute("endPage", endPage);     // 마지막 페이지 번호
            model.addAttribute("type", type);       // 검색 후에도 입력한 검색 타입을 유지하기 위해
            model.addAttribute("keyword", keyword); // 검색 후에도 검색어를 유지하기 위해

            return "list"; // paging.html 생성 필요
        }


}
