package com.example.demo.controller;

import com.example.demo.dto.BoardDTO;
import com.example.demo.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession; // HttpSession을 위해 필요

import java.util.List;

    //1. 클래스 설정 및 주입
@Controller // 이 클래스가 웹 요청을 처리하는 컨트롤러임을 스프링에 알림
@RequiredArgsConstructor // final이 붙은 필드(boardService)를 매개변수로 하는 생성자를 자동으로 생성 (Lombok)
@RequestMapping("/board") // 이 클래스 내 모든 메서드의 주소 앞에 /board가 기본으로 붙음
public class BoardController {
    private final BoardService boardService; // 비즈니스 로직을 처리하는 서비스 객체를 주입받음
    @GetMapping("/save") // 주소: GET /board/save
    public String saveForm(){
        return "save"; // templates/save.html 파일을 브라우저에 보여줌
    }

        //2. 게시글 작성
        @PostMapping("/save")
        public String save(@ModelAttribute BoardDTO boardDTO, HttpSession session) {
            // 세션에서 닉네임을 가져옴 (비로그인 시에는 null이 들어감)
            String loginNickname = (String) session.getAttribute("loginNickname");

            if (loginNickname != null) {
                // 로그인 상태라면 세션의 닉네임으로 작성자 강제 고정
                boardDTO.setBoardWriter(loginNickname);
            }
            // 비로그인 상태라면 HTML form에서 입력한 boardWriter가 그대로 저장됨

            boardService.save(boardDTO);
            return "redirect:/board/"; // 주소 끝에 슬래시(/)가 빠지지 않았는지 확인하세요!
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
    @GetMapping("/{id}") //주소: GET /board/1 (게시글 번호가 주소에 포함됨)
    public String findById(@PathVariable Long id, Model model){
        boardService.updateHits(id); // 해당 게시글의 조회수를 1 증가시킴
        BoardDTO boardDTO = boardService.findById(id); // id값으로 게시글 1건의 데이터를 가져옴
        model.addAttribute("board", boardDTO); // 상세 내용을 "board"라는 이름으로 HTML에 전달
        return "detail"; // templates/detail.html 파일을 보여줌
    }

    @GetMapping("/delete/{id}") // 주소: GET /board/delete/1
    public String delete(@PathVariable Long id){
        boardService.delete(id); // 서비스에 삭제 명령 전달
        return "redirect:/board/"; // 삭제 후 목록으로 이동
    }

        // BoardController.java

        // 삭제 비밀번호 입력 화면 요청
        @GetMapping("/delete-check/{id}")
        public String deleteCheckForm(@PathVariable Long id, Model model) {
            model.addAttribute("id", id);
            return "delete-check";
        }

        // 실제 삭제 처리
        @PostMapping("/delete")
        public String delete(@RequestParam("id") Long id,
                             @RequestParam("deletePass") String deletePass,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

            // 1. 세션에서 로그인 정보 확인 (로그인 사용자라면 비번 체크 생략 가능)
            String loginId = (String) session.getAttribute("loginId");

            // 2. 게시글 정보 가져오기
            BoardDTO boardDTO = boardService.findById(id);

            // 3. 권한 체크
            if (loginId != null || boardDTO.getBoardPass().equals(deletePass)) {
                // 로그인 중이거나, 입력한 비번이 DB 비번과 일치할 때
                boardService.delete(id);
                return "redirect:/board/";
            } else {
                // 비밀번호 불일치
                redirectAttributes.addFlashAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
                return "redirect:/board/delete-check/" + id;
            }
        }

        // BoardController.java 수정
        @GetMapping("/update/{id}")
        public String updateForm(@PathVariable Long id, Model model){
            BoardDTO boardDTO = boardService.findById(id);
            // 기존: model.addAttribute("board", boardDTO);
            model.addAttribute("boardUpdate", boardDTO); // html의 ${boardUpdate}와 이름을 맞춰야 함!
            return "update";
        }

        @PostMapping("/update")
        public String update(@ModelAttribute BoardDTO boardDTO, HttpSession session, RedirectAttributes redirectAttributes) {
            // 세션에서 로그인 정보 확인
            String loginId = (String) session.getAttribute("loginId");

            try {
                // 로그인 상태라면 비밀번호 체크를 통과하기 위해 기존 비밀번호를 updatePass에 넣어줌
                if (loginId != null) {
                    BoardDTO currentBoard = boardService.findById(boardDTO.getId());
                    boardDTO.setUpdatePass(currentBoard.getBoardPass());
                }

                boardService.update(boardDTO);
                return "redirect:/board/" + boardDTO.getId();
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/board/update/" + boardDTO.getId();
            }
        }

}
