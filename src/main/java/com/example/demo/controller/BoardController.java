package com.example.demo.controller;

import com.example.demo.dto.BoardDTO;
import com.example.demo.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    //2. 게시글 작성 (Save)
    @PostMapping("/save") // 주소: POST /board/save (글쓰기 완료 버튼 클릭 시)
    public String save(BoardDTO boardDTO){
        // 브라우저에서 보낸 값(DTO)이 잘 들어왔는지 콘솔에 출력
        System.out.println("boardDTO = " + boardDTO);
        boardService.save(boardDTO); // 서비스의 저장 기능을 실행
        return "redirect:/board/"; // 글 작성이 끝나면 게시글 목록 주소(/board/)로 다시 강제 이동
    }

    //3. 목록 및 상세 조회 (Read)
    @GetMapping("/") //주소: GET /board/
    public String findall(Model model) {
        // DB에서 모든 게시글 데이터를 가져와서 리스트에 담음
        List<BoardDTO> boardDTOList = boardService.findAll();
        // 화면(HTML)으로 데이터를 전달하기 위해 모델에 담음 (이름: boardList)
        model.addAttribute("boardList", boardDTOList);
        return "List"; // templates/List.html 파일을 보여줌
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

    @GetMapping("/update/{id}") // 주소: GET /board/update/1 (수정 화면 요청)
    public String updateForm(@PathVariable Long id, Model model){
        // 수정할 때 기존 내용을 보여줘야 하므로 findById로 데이터를 가져옴
        BoardDTO boardDTO = boardService.findById(id);
        model.addAttribute("board", boardDTO);
        return "update"; // templates/update.html 파일을 보여줌
    }

    @PostMapping("/update") // 주소: POST /board/update (수정 완료 버튼 클릭 시)
    public String update(BoardDTO boardDTO, RedirectAttributes redirectAttributes){
        try {
            boardService.update(boardDTO); // 수정된 내용 반영
            // 수정 완료 후 해당 게시글의 상세 페이지로 다시 이동
            return "redirect:/board/" + boardDTO.getId();
        } catch (IllegalArgumentException e) {
            // 수정 중 에러(예: 비번 틀림 등) 발생 시 에러 메시지를 일회성으로 담아 보냄
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // 다시 수정 화면으로 돌려보냄
            return "redirect:/board/update/" + boardDTO.getId();
        }
    }
}
