package com.example.demo.service;

import com.example.demo.dto.CommentDTO;
import com.example.demo.entity.BoardEntity;
import com.example.demo.entity.CommentEntity;
import com.example.demo.repository.BoardRepository;
import com.example.demo.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    public Long save(CommentDTO commentDTO) {
        BoardEntity boardEntity = boardRepository.findById(commentDTO.getBoardId()).orElse(null);
        if (boardEntity != null) {
            CommentEntity commentEntity = CommentEntity.toSaveEntity(commentDTO, boardEntity);
            return commentRepository.save(commentEntity).getId();
        }
        return null;
    }

// CommentService.java

    @Transactional
    public List<CommentDTO> findAll(Long boardId) {
        // 1. 게시글 엔티티 조회
        BoardEntity boardEntity = boardRepository.findById(boardId).orElseThrow(() -> new RuntimeException("게시글이 없습니다."));
        // 2. 해당 게시글에 달린 댓글 목록 조회 (작성일 기준 내림차순)
        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardEntityOrderByIdDesc(boardEntity);

        // 3. Entity 리스트를 DTO 리스트로 변환
        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (CommentEntity commentEntity : commentEntityList) {
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setId(commentEntity.getId());
            commentDTO.setCommentWriter(commentEntity.getCommentWriter());
            commentDTO.setCommentContents(commentEntity.getCommentContents());
            // BaseEntity의 createdAt을 String으로 변환 (날짜 가공)
            commentDTO.setCommentCreatedAt(commentEntity.getCreatedAt().toString());
            commentDTO.setMemberEmail(commentEntity.getMemberEmail());
            commentDTOList.add(commentDTO);
        }
        return commentDTOList;
    }

    @Transactional
    public void delete(Long id, String loginEmail, String commentPass) {
        CommentEntity commentEntity = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        if (commentEntity.getMemberEmail() != null) {
            // 회원 댓글
            if (!commentEntity.getMemberEmail().equals(loginEmail)) {
                throw new RuntimeException("본인의 댓글만 삭제할 수 있습니다.");
            }
        } else {
            // 비회원 댓글
            if (!commentEntity.getCommentPass().equals(commentPass)) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }
        commentRepository.deleteById(id);
    }

    @Transactional
    public void update(CommentDTO commentDTO, String loginEmail) {
        CommentEntity commentEntity = commentRepository.findById(commentDTO.getId())
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        if (commentEntity.getMemberEmail() != null) {
            // 회원 댓글: 세션 이메일과 작성자 이메일 비교
            if (!commentEntity.getMemberEmail().equals(loginEmail)) {
                throw new RuntimeException("본인의 댓글만 수정할 수 있습니다.");
            }
        } else {
            // 비회원 댓글: 입력한 비번과 DB 비번 비교
            if (!commentEntity.getCommentPass().equals(commentDTO.getCommentPass())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }

        // 내용 업데이트 (엔티티에 update 메서드를 만들거나 여기서 직접 세팅)
        commentEntity.setCommentContents(commentDTO.getCommentContents());
    }
}