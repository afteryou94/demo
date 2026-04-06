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
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;

    @GetMapping("/save")
    public String saveForm(Model model) {
        model.addAttribute("boardDTO", new BoardDTO());
        return "save";
    }


    @PostMapping("/save")
    public String save(@Valid @ModelAttribute BoardDTO boardDTO, BindingResult bindingResult,
                       HttpSession session) {

        String loginNickname = (String) session.getAttribute("loginNickname");
        String loginId = (String) session.getAttribute("loginEmail");


        if (loginId != null) {
            boardDTO.setBoardWriter(loginNickname);
            boardDTO.setMemberEmail(loginId);
        }


        if (bindingResult.hasFieldErrors("boardTitle") || bindingResult.hasFieldErrors("boardContents")) {
            return "save";
        }


        if (loginId == null && (bindingResult.hasFieldErrors("boardWriter") || bindingResult.hasFieldErrors("boardPass"))) {
            return "save";
        }


        boardService.save(boardDTO, loginId);
        return "redirect:/board/";
    }


    @GetMapping("/{id}")
    public String findById(@PathVariable Long id, Model model) {

        boardService.updateHits(id);


        BoardDTO boardDTO = boardService.findById(id);


        List<CommentDTO> commentDTOList = commentService.findAll(id);


        model.addAttribute("board", boardDTO);
        model.addAttribute("commentList", commentDTOList);

        return "detail";
    }


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {

        String loginEmail = (String) session.getAttribute("loginEmail");

        BoardDTO boardDTO = boardService.findById(id);


        if (boardDTO.getMemberEmail() != null && boardDTO.getMemberEmail().equals(loginEmail)) {

            boardService.delete(id, loginEmail, null);
            return "redirect:/board/";
        }


        return "redirect:/board/delete-check/" + id;
    }


    @GetMapping("/delete-check/{id}")
    public String deleteCheckForm(@PathVariable Long id, Model model) {

        model.addAttribute("id", id);
        return "delete-check";
    }


    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id,
                         @RequestParam(value = "boardPass", required = false) String boardPass,
                         HttpSession session, Model model) {


        String loginEmail = (String) session.getAttribute("loginEmail");

        try {

            boardService.delete(id, loginEmail, boardPass);
            return "redirect:/board/";
        } catch (RuntimeException e) {

            model.addAttribute("id", id);
            model.addAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");

            return "delete-check";
        }
    }


    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {

        BoardDTO boardDTO = boardService.findById(id);

        String loginEmail = (String) session.getAttribute("loginEmail");


        if (boardDTO.getMemberEmail() != null) {
            if (!boardDTO.getMemberEmail().equals(loginEmail)) {

                return "redirect:/board/?error=unauthorized";
            }

            model.addAttribute("boardUpdate", boardDTO);
            return "update";
        }


        model.addAttribute("boardUpdate", boardDTO);
        return "update";
    }


    @PostMapping("/update")
    public String update(@ModelAttribute BoardDTO boardDTO, HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String loginEmail = (String) session.getAttribute("loginEmail");

        try {

            boardService.update(boardDTO, loginEmail, boardDTO.getBoardPass());

            redirectAttributes.addAttribute("id", boardDTO.getId());
            return "redirect:/board/{id}";
        } catch (RuntimeException e) {
            // 수정 실패 시 다시 update.html로 보내고 에러 메시지를 담는다.
            model.addAttribute("boardUpdate", boardDTO); // ⬅️ 입력했던 데이터 유지
            model.addAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
            return "update"; // ⬅️ 다시 수정 폼으로 보냄!
        }
    }


    @GetMapping("/")
    public String paging(@PageableDefault(page = 1) Pageable pageable,
                         @RequestParam(value = "type", required = false) String type,
                         @RequestParam(value = "keyword", required = false) String keyword,
                         Model model) {


        Page<BoardDTO> boardList = boardService.paging(pageable, type, keyword);


        int blockLimit = 5;


        int startPage = (((int) (Math.ceil((double) pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;


        int endPage = ((startPage + blockLimit - 1) < boardList.getTotalPages()) ? startPage + blockLimit - 1 : boardList.getTotalPages();


        model.addAttribute("boardList", boardList);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);

        return "list";
    }


}
