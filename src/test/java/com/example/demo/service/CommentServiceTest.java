package com.example.demo.service;

import com.example.demo.dto.CommentDTO;
import com.example.demo.entity.BoardEntity;
import com.example.demo.entity.CommentEntity;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock BoardRepository boardRepository;
    @InjectMocks CommentService commentService;

    private BoardEntity board() {
        BoardDTOBuilder builder = new BoardDTOBuilder();
        return builder.build();
    }

    private CommentEntity comment(BoardEntity board, String email, String pass) {
        CommentEntity entity = new CommentEntity();
        entity.setId(10L);
        entity.setCommentWriter("댓글작성자");
        entity.setCommentContents("댓글 내용");
        entity.setCommentPass(pass);
        entity.setMemberEmail(email);
        entity.setBoardEntity(board);
        return entity;
    }

    @Test
    void 댓글_저장시_존재하는_게시글과_연결된다() {
        BoardEntity board = board();
        CommentDTO dto = new CommentDTO();
        dto.setBoardId(1L);
        dto.setCommentWriter("홍길동");
        dto.setCommentContents("안녕하세요");
        dto.setCommentPass("1234");
        dto.setMemberEmail("user@example.com");

        CommentEntity saved = comment(board, dto.getMemberEmail(), dto.getCommentPass());
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(saved);

        Long id = commentService.save(dto);

        assertEquals(10L, id);
        verify(commentRepository).save(argThat(entity ->
                entity.getBoardEntity() == board &&
                "안녕하세요".equals(entity.getCommentContents())));
    }

    @Test
    void 존재하지_않는_게시글에는_댓글을_저장하지_않는다() {
        CommentDTO dto = new CommentDTO();
        dto.setBoardId(999L);
        when(boardRepository.findById(999L)).thenReturn(Optional.empty());

        assertNull(commentService.save(dto));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void 회원_댓글은_작성자_본인만_삭제할_수_있다() {
        BoardEntity board = board();
        CommentEntity entity = comment(board, "owner@example.com", "1234");
        when(commentRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertThrows(RuntimeException.class,
                () -> commentService.delete(10L, "other@example.com", null));
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    void 회원_댓글은_작성자_본인이면_삭제할_수_있다() {
        BoardEntity board = board();
        CommentEntity entity = comment(board, "owner@example.com", "1234");
        when(commentRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> commentService.delete(10L, "owner@example.com", null));
        verify(commentRepository).deleteById(10L);
    }

    @Test
    void 비회원_댓글은_올바른_비밀번호로_삭제할_수_있다() {
        BoardEntity board = board();
        CommentEntity entity = comment(board, null, "1234");
        when(commentRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> commentService.delete(10L, null, "1234"));
        verify(commentRepository).deleteById(10L);
    }

    @Test
    void 비회원_댓글은_틀린_비밀번호로_삭제할_수_없다() {
        BoardEntity board = board();
        CommentEntity entity = comment(board, null, "1234");
        when(commentRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertThrows(RuntimeException.class,
                () -> commentService.delete(10L, null, "9999"));
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    void 회원_댓글은_작성자_본인만_수정할_수_있다() {
        BoardEntity board = board();
        CommentEntity entity = comment(board, "owner@example.com", "1234");
        when(commentRepository.findById(10L)).thenReturn(Optional.of(entity));

        CommentDTO dto = new CommentDTO();
        dto.setId(10L);
        dto.setCommentContents("악의적인 수정");

        assertThrows(RuntimeException.class,
                () -> commentService.update(dto, "other@example.com"));
        assertEquals("댓글 내용", entity.getCommentContents());
    }

    @Test
    void 회원_댓글은_본인이면_수정할_수_있다() {
        BoardEntity board = board();
        CommentEntity entity = comment(board, "owner@example.com", "1234");
        when(commentRepository.findById(10L)).thenReturn(Optional.of(entity));

        CommentDTO dto = new CommentDTO();
        dto.setId(10L);
        dto.setCommentContents("수정된 댓글");

        commentService.update(dto, "owner@example.com");
        assertEquals("수정된 댓글", entity.getCommentContents());
    }

    @Test
    void 비회원_댓글은_올바른_비밀번호로_수정할_수_있다() {
        BoardEntity board = board();
        CommentEntity entity = comment(board, null, "1234");
        when(commentRepository.findById(10L)).thenReturn(Optional.of(entity));

        CommentDTO dto = new CommentDTO();
        dto.setId(10L);
        dto.setCommentContents("수정된 댓글");
        dto.setCommentPass("1234");

        commentService.update(dto, null);
        assertEquals("수정된 댓글", entity.getCommentContents());
    }

    @Test
    void 비회원_댓글은_틀린_비밀번호로_수정할_수_없다() {
        BoardEntity board = board();
        CommentEntity entity = comment(board, null, "1234");
        when(commentRepository.findById(10L)).thenReturn(Optional.of(entity));

        CommentDTO dto = new CommentDTO();
        dto.setId(10L);
        dto.setCommentContents("수정된 댓글");
        dto.setCommentPass("9999");

        assertThrows(RuntimeException.class, () -> commentService.update(dto, null));
        assertEquals("댓글 내용", entity.getCommentContents());
    }

    @Test
    void 로그인한_사용자는_익명_댓글을_수정할_수_없다() {
        BoardEntity board = board();
        CommentEntity entity = comment(board, null, "1234");
        when(commentRepository.findById(10L)).thenReturn(Optional.of(entity));

        CommentDTO dto = new CommentDTO();
        dto.setId(10L);
        dto.setCommentContents("수정 시도");
        dto.setCommentPass("1234");

        assertThrows(RuntimeException.class,
                () -> commentService.update(dto, "logged@example.com"));
    }

    private static class BoardDTOBuilder {
        BoardEntity build() {
            com.example.demo.dto.BoardDTO dto = new com.example.demo.dto.BoardDTO();
            dto.setBoardWriter("게시글작성자");
            dto.setBoardTitle("게시글");
            dto.setBoardContents("내용");
            dto.setBoardPass("1234");
            return BoardEntity.toSaveEntity(dto);
        }
    }
}
