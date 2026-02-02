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

@Controller
@RequiredArgsConstructor //생성자 주입
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService; //사용하는 객체 선언
    @GetMapping("/save")
    public String saveForm(){
        return "save";
    }

    @PostMapping("/save")
    public String save(BoardDTO boardDTO){
        System.out.println("boardDTO = " + boardDTO);
        boardService.save(boardDTO); //save 메서드 호출
        return "redirect:/board/"; //아래 Getmapping 메서드 호출
    }

    @GetMapping("/")
    public String findall(Model model) {
        List<BoardDTO> boardDTOList = boardService.findAll();
        model.addAttribute("boardList", boardDTOList);
        return "List";
    }

    @GetMapping("/{id}")
    public String findById(@PathVariable Long id, Model model){
        boardService.updateHits(id);
        BoardDTO boardDTO = boardService.findById(id);
        model.addAttribute("board", boardDTO);
        return  "detail";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id){
        boardService.delete(id);
        return "redirect:/board/";
    }

    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Long id, Model model){
        BoardDTO boardDTO = boardService.findById(id);
        model.addAttribute("board", boardDTO);
        return "update";
    }

    @PostMapping("/update")
    public String update(BoardDTO boardDTO, RedirectAttributes redirectAttributes){
        try{
            boardService.update(boardDTO);
            return "redirect:/board/" + boardDTO.getId();
        }   catch (IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/update/" + boardDTO.getId();
        }
    }
}
