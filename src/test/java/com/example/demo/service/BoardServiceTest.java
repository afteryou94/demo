package com.example.demo.service;

import com.example.demo.dto.BoardDTO;
import com.example.demo.entity.BoardEntity;
import com.example.demo.repository.BoardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock BoardRepository boardRepository;
    @InjectMocks BoardService boardService;

    private BoardEntity board(String email, String pass) {
        BoardDTO dto = new BoardDTO();
        dto.setBoardWriter("홍길동");
        dto.setMemberEmail(email);
        dto.setBoardPass(pass);
        dto.setBoardTitle("테스트 제목");
        dto.setBoardContents("테스트 내용");
        return BoardEntity.toSaveEntity(dto);
    }

    @Test
    void 회원_게시글_저장시_로그인_이메일이_게시글에_저장된다() {
        BoardDTO dto = new BoardDTO();
        dto.setBoardWriter("홍길동");
        dto.setBoardTitle("제목");
        dto.setBoardContents("내용");
        dto.setBoardPass("1234");

        boardService.save(dto, "user@example.com");

        assertEquals("user@example.com", dto.getMemberEmail());
        verify(boardRepository).save(argThat(saved ->
                "user@example.com".equals(saved.getMemberEmail()) &&
                "제목".equals(saved.getBoardTitle()) &&
                saved.getBoardHits() == 0));
    }

    @Test
    void 존재하는_게시글을_조회하면_DTO를_반환한다() {
        BoardEntity entity = board("user@example.com", "1234");
        when(boardRepository.findById(1L)).thenReturn(Optional.of(entity));

        BoardDTO result = boardService.findById(1L);

        assertNotNull(result);
        assertEquals("테스트 제목", result.getBoardTitle());
        assertEquals("user@example.com", result.getMemberEmail());
    }

    @Test
    void 존재하지_않는_게시글을_조회하면_null을_반환한다() {
        when(boardRepository.findById(999L)).thenReturn(Optional.empty());
        assertNull(boardService.findById(999L));
    }

    @Test
    void 회원_게시글은_작성자_본인만_삭제할_수_있다() {
        BoardEntity entity = board("owner@example.com", "1234");
        when(boardRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThrows(RuntimeException.class,
                () -> boardService.delete(1L, "other@example.com", null));
        verify(boardRepository, never()).deleteById(anyLong());
    }

    @Test
    void 회원_게시글은_작성자_본인이면_삭제할_수_있다() {
        BoardEntity entity = board("owner@example.com", "1234");
        when(boardRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> boardService.delete(1L, "owner@example.com", null));
        verify(boardRepository).deleteById(1L);
    }

    @Test
    void 비회원_게시글은_올바른_비밀번호로_삭제할_수_있다() {
        BoardEntity entity = board(null, "1234");
        when(boardRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> boardService.delete(1L, null, "1234"));
        verify(boardRepository).deleteById(1L);
    }

    @Test
    void 비회원_게시글은_틀린_비밀번호로_삭제할_수_없다() {
        BoardEntity entity = board(null, "1234");
        when(boardRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThrows(RuntimeException.class,
                () -> boardService.delete(1L, null, "9999"));
        verify(boardRepository, never()).deleteById(anyLong());
    }

    @Test
    void 존재하지_않는_게시글_삭제는_예외가_발생한다() {
        when(boardRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> boardService.delete(999L, "owner@example.com", null));
    }

    @Test
    void 비회원_게시글은_올바른_비밀번호로_수정할_수_있다() {
        BoardEntity entity = board(null, "1234");
        when(boardRepository.findById(1L)).thenReturn(Optional.of(entity));

        BoardDTO update = new BoardDTO();
        update.setId(1L);
        update.setBoardTitle("수정 제목");
        update.setBoardContents("수정 내용");

        assertDoesNotThrow(() -> boardService.update(update, null, "1234"));
        assertEquals("수정 제목", entity.getBoardTitle());
        assertEquals("수정 내용", entity.getBoardContents());
    }

    @Test
    void 비회원_게시글은_틀린_비밀번호로_수정할_수_없다() {
        BoardEntity entity = board(null, "1234");
        when(boardRepository.findById(1L)).thenReturn(Optional.of(entity));

        BoardDTO update = new BoardDTO();
        update.setId(1L);
        update.setBoardTitle("수정 제목");
        update.setBoardContents("수정 내용");

        assertThrows(RuntimeException.class,
                () -> boardService.update(update, null, "9999"));
        assertEquals("테스트 제목", entity.getBoardTitle());
    }

    @Test
    void 조회수_증가_요청이_Repository에_전달된다() {
        boardService.updateHits(10L);
        verify(boardRepository).updateHits(10L);
    }

    @Test
    void 검색어가_없으면_최신순_10개_페이징을_요청한다() {
        Page<BoardEntity> page = new PageImpl<>(List.of(board("a@example.com", "1234")));
        when(boardRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<BoardDTO> result = boardService.paging(PageRequest.of(1, 10), null, "");

        assertEquals(1, result.getTotalElements());
        verify(boardRepository).findAll(any(PageRequest.class));
    }

    @Test
    void 제목_검색은_제목Containing_Repository를_사용한다() {
        Page<BoardEntity> page = new PageImpl<>(List.of(board("a@example.com", "1234")));
        when(boardRepository.findByBoardTitleContaining(eq("스프링"), any(PageRequest.class))).thenReturn(page);

        Page<BoardDTO> result = boardService.paging(PageRequest.of(1, 10), "title", "스프링");

        assertEquals(1, result.getTotalElements());
        verify(boardRepository).findByBoardTitleContaining(eq("스프링"), any(PageRequest.class));
        verify(boardRepository, never()).findByBoardContentsContaining(anyString(), any(PageRequest.class));
    }
}
