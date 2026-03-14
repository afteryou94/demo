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

    /**
     * 1. 댓글 저장
     * 게시글과 댓글의 연관 관계를 맺어주는 것이 핵심입니다.
     */


    public Long save(CommentDTO commentDTO) {
        System.out.println("Service save 호출됨: " + commentDTO);
        BoardEntity boardEntity = boardRepository.findById(commentDTO.getBoardId()).orElse(null);

        if (boardEntity != null) {
            System.out.println("게시글 찾음: " + boardEntity.getId());
            CommentEntity commentEntity = CommentEntity.toSaveEntity(commentDTO, boardEntity);

            CommentEntity savedEntity = commentRepository.save(commentEntity);
            System.out.println("저장 완료! 생성된 ID: " + savedEntity.getId());
            return savedEntity.getId();
        }
        System.out.println("게시글을 찾지 못함!");
        return null;
    }

    /**
     * 2. 특정 게시글의 모든 댓글 목록 조회
     */
    @Transactional
    public List<CommentDTO> findAll(Long boardId) {

        BoardEntity boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다."));


        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardEntityOrderByIdDesc(boardEntity);


        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (CommentEntity commentEntity : commentEntityList) {
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setId(commentEntity.getId());
            commentDTO.setCommentWriter(commentEntity.getCommentWriter());
            commentDTO.setCommentContents(commentEntity.getCommentContents());


            commentDTO.setCommentCreatedAt(commentEntity.getCreatedAt().toString());
            commentDTO.setMemberEmail(commentEntity.getMemberEmail());

            commentDTOList.add(commentDTO);
        }
        return commentDTOList;
    }

    /**
     * 3. 댓글 삭제 (권한 검증 포함)
     */
    @Transactional
    public void delete(Long id, String loginEmail, String commentPass) {

        CommentEntity commentEntity = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));


        if (commentEntity.getMemberEmail() != null) {

            if (!commentEntity.getMemberEmail().equals(loginEmail)) {
                throw new RuntimeException("본인의 댓글만 삭제할 수 있습니다.");
            }
        } else {

            if (!commentEntity.getCommentPass().equals(commentPass)) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }

        commentRepository.deleteById(id);
    }

    /**
     * 4. 댓글 수정 (권한 검증 포함)
     */
    @Transactional
    public void update(CommentDTO commentDTO, String loginEmail) {

        CommentEntity commentEntity = commentRepository.findById(commentDTO.getId())
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));


        if (commentEntity.getMemberEmail() != null) {
            if (!commentEntity.getMemberEmail().equals(loginEmail)) {
                throw new RuntimeException("본인의 댓글만 수정할 수 있습니다.");
            }
        } else {
            if (!commentEntity.getCommentPass().equals(commentDTO.getCommentPass())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }


        commentEntity.setCommentContents(commentDTO.getCommentContents());
    }
}
