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

    //게시글 저장 로직
        //세션(Session)**과 **유효성 검사(Validation)**를 결합한 로직
        @PostMapping("/save")
        public String save(@Valid @ModelAttribute BoardDTO boardDTO, BindingResult bindingResult,
                           HttpSession session) {
            // 1. 현재 세션에서 로그인한 사용자의 닉네임과 이메일(ID)을 가져옵니다.
            String loginNickname = (String) session.getAttribute("loginNickname");
            String loginId = (String) session.getAttribute("loginEmail");

            // 2. 로그인 상태(loginId가 존재)라면, 사용자 입력을 대신해 세션 정보를 DTO에 자동으로 채워줍니다.
            if (loginId != null) {
                boardDTO.setBoardWriter(loginNickname); // 작성자를 로그인 유저 닉네임으로 고정
                boardDTO.setMemberEmail(loginId);        // 추후 본인 확인을 위한 이메일 정보 저장
            }

            // 3. 유효성 검사 단계: 제목(Title)이나 내용(Contents)이 비어있으면 다시 작성 페이지로 보냅니다.
            if (bindingResult.hasFieldErrors("boardTitle") || bindingResult.hasFieldErrors("boardContents")) {
                return "save";
            }

            // 4. 비로그인 유저 보호 로직:
            // 로그인하지 않은 사용자가 '작성자'나 '비밀번호'를 입력하지 않았다면 등록을 막습니다.
            if (loginId == null && (bindingResult.hasFieldErrors("boardWriter") || bindingResult.hasFieldErrors("boardPass"))) {
                return "save";
            }

            // 5. 모든 검증을 통과하면 서비스를 호출하여 DB에 저장하고 목록 페이지로 리다이렉트합니다.
            boardService.save(boardDTO, loginId);
            return "redirect:/board/";
        }

    //상세 조회 (GET /board/{id}), 게시글 내용과 댓글을 동시에 불러오는 조회의 핵심
        // 상세 페이지 요청: /board/10 과 같이 게시글의 고유 ID를 주소로 받음, 게시글의 내용과 댓글을 동시에 불러오는 '조회'의 핵심
        @GetMapping("/{id}")
        public String findById(@PathVariable Long id, Model model) {
            // 1. 해당 게시글의 조회수(Hits)를 1 올리는 서비스 로직 호출
            boardService.updateHits(id);

            // 2. ID를 이용해 게시글 데이터를 DB에서 가져와 DTO 객체에 담음
            BoardDTO boardDTO = boardService.findById(id);

            // 3. 해당 게시글(id)에 작성된 모든 댓글 리스트를 가져옴
            List<CommentDTO> commentDTOList = commentService.findAll(id);

            // 4. 게시글 데이터와 댓글 리스트를 모두 Model에 담아 HTML로 전달
            model.addAttribute("board", boardDTO);
            model.addAttribute("commentList", commentDTOList); // detail.html에서 댓글 목록 출력용

            return "detail"; // templates/detail.html 실행
        }

    //권한 기반 삭제 로직 (GET /board/delete/{id})
        //사용자의 상태에 따라 삭제 절차를 다르게 가져가는 로직
        @GetMapping("/delete/{id}")
        public String delete(@PathVariable Long id, HttpSession session) {
            // 1. 현재 로그인한 사람의 정보를 가져옵니다.
            String loginEmail = (String) session.getAttribute("loginEmail");
            // 2. 삭제하려는 게시글의 상세 정보를 가져옵니다.
            BoardDTO boardDTO = boardService.findById(id);

            // 3. 본인 확인 로직:
            // 게시글에 저장된 이메일과 현재 세션의 이메일이 일치하면 '본인'으로 간주합니다.
            if (boardDTO.getMemberEmail() != null && boardDTO.getMemberEmail().equals(loginEmail)) {
                // 본인이면 비밀번호 확인 없이 즉시 삭제를 진행합니다.
                boardService.delete(id, loginEmail, null);
                return "redirect:/board/";
            }

            // 4. 본인이 아니거나 비로그인 유저의 글이라면:
            // 바로 삭제하지 않고 비밀번호 입력 페이지(/delete-check)로 리다이렉트 시킵니다.
            return "redirect:/board/delete-check/" + id;
        }

        // BoardController.java

   //삭제 화면 요청 및 처리, 삭제 확인 페이지를 거쳐 비밀번호를 검증하는 단계
        // 3-1. 삭제 비밀번호 입력 화면 요청 (비로그인 유저 또는 타인 확인용)
        @GetMapping("/delete-check/{id}")
        public String deleteCheckForm(@PathVariable Long id, Model model) {
            // 어떤 글을 삭제할지 ID 값만 화면(delete-check.html)으로 전달
            model.addAttribute("id", id);
            return "delete-check";
        }

        // 3-2. 실제 삭제 처리 (비밀번호 입력 후 삭제 버튼 클릭 시)
        @PostMapping("/delete")
        public String delete(@RequestParam("id") Long id,
                             @RequestParam(value = "boardPass", required = false) String boardPass, // required=false 추가
                             HttpSession session, Model model){

            // 세션에서 로그인 정보를 가져옴
            String loginEmail = (String) session.getAttribute("loginEmail");

            try {
                // 서비스 계층에 ID, 세션이메일, 입력한비밀번호를 넘겨 검증 후 삭제 진행
                boardService.delete(id, loginEmail, boardPass);
                return "redirect:/board/"; // 삭제 성공 시 목록으로 이동
            } catch (RuntimeException e) {
                // 비밀번호가 틀린 경우 다시 입력 페이지로
                model.addAttribute("id", id);
                model.addAttribute("errorMessage", e.getMessage());
                // 비밀번호가 틀려 서비스에서 예외(Exception)를 던진 경우, 에러 메시지와 함께 상세페이지 복귀
                return "redirect:/board/" + id + "?error=auth";
            }
        }


    //수정 화면 요청 및 처리 (GET/POST /board/update), 수정은 **"본인 확인"**과 **"기존 데이터 로드"**가 핵심
        // 2-1. 수정 화면 요청 (수정 페이지를 보여줌)
        @GetMapping("/update/{id}")
        public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
            // 1. 수정하려는 기존 게시글 데이터를 가져옴
            BoardDTO boardDTO = boardService.findById(id);
            // 2. 세션에서 현재 로그인한 유저의 이메일을 가져옴
            String loginEmail = (String) session.getAttribute("loginEmail");

            // 3. [권한 검증] 게시글에 저장된 이메일이 있고, 로그인한 이메일과 다르면 수정 불가
            if (boardDTO.getMemberEmail() != null) {
                if (!boardDTO.getMemberEmail().equals(loginEmail)) {
                    // 본인 글이 아니면 에러 메시지와 함께 목록으로 튕겨냄
                    return "redirect:/board/?error=unauthorized";
                }
                // 본인이면 수정 페이지로 이동하며 기존 데이터를 넘겨줌
                model.addAttribute("boardUpdate", boardDTO);
                return "update";
            }

            // 4. 비로그인 유저의 글인 경우 (memberEmail이 null) 일단 수정 페이지로 보냄
            // (이후 실제 처리 POST 단계에서 비밀번호로 검증하게 됨)
            model.addAttribute("boardUpdate", boardDTO);
            return "update";
        }

        // 2-2. 실제 수정 처리 (수정 완료 버튼 클릭 시)
        @PostMapping("/update")
        public String update(@ModelAttribute BoardDTO boardDTO, HttpSession session, RedirectAttributes redirectAttributes) {
            String loginEmail = (String) session.getAttribute("loginEmail");

            try {
                // 서비스 단에서 본인(이메일 비교) 또는 비로그인(비밀번호 비교) 검증 후 업데이트 수행
                boardService.update(boardDTO, loginEmail, boardDTO.getBoardPass());
                // redirect 경로의 {id} 자리에 boardDTO.getId() 값을 자동으로 채워줍니다.
                redirectAttributes.addAttribute("id", boardDTO.getId());
                return "redirect:/board/{id}";
            } catch (RuntimeException e) {
                // 검증 실패(비밀번호 틀림 등) 시 에러 파라미터를 들고 목록으로 이동
                return "redirect:/board/?error=auth";
            }
        }


    //페이징 및 검색 로직 (GET /board/)
        //동적 쿼리 결과의 페이징 가공
        @GetMapping("/")
        public String paging(@PageableDefault(page = 1) Pageable pageable, // 기본 페이지를 1로 설정
                             @RequestParam(value = "type", required = false) String type, // 검색 조건(제목, 작성자 등)
                             @RequestParam(value = "keyword", required = false) String keyword, // 검색어
                             Model model) {

            // 1. 서비스에 페이징 정보와 검색 조건을 넘겨 해당 페이지의 게시글 목록(Page 객체)을 가져옵니다.
            Page<BoardDTO> boardList = boardService.paging(pageable, type, keyword);

            // 2. 하단 페이지 번호 노출 개수 설정 (예: [1][2][3][4][5])
            int blockLimit = 5;

            // 3. 시작 페이지 계산: 현재 페이지가 3이라면 (3/5 -> 0.6 -> 올림하면 1) -> (1-1)*5 + 1 = 1페이지부터 시작
            int startPage = (((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;

            // 4. 마지막 페이지 계산: 시작페이지 + 블록제한 - 1 이 전체 페이지수보다 크면 전체 페이지수를 마지막으로 설정
            int endPage = ((startPage + blockLimit - 1) < boardList.getTotalPages()) ? startPage + blockLimit - 1 : boardList.getTotalPages();

            // 5. 계산된 모든 데이터를 Model에 담아 list.html로 전달합니다.
            model.addAttribute("boardList", boardList); // 게시글 목록
            model.addAttribute("startPage", startPage); // 하단 시작 번호
            model.addAttribute("endPage", endPage);     // 하단 끝 번호
            model.addAttribute("type", type);           // 검색 상태 유지용
            model.addAttribute("keyword", keyword);     // 검색어 유지용

            return "list";
        }


}
