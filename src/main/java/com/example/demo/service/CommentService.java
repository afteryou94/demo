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
        // 1-1. 댓글이 달릴 부모 게시글이 존재하는지 먼저 확인합니다.
        BoardEntity boardEntity = boardRepository.findById(commentDTO.getBoardId()).orElse(null);

        if (boardEntity != null) {
            // 1-2. DTO와 찾은 게시글 엔티티를 함께 넘겨 댓글 엔티티를 생성합니다. (연관 관계 매핑)
            CommentEntity commentEntity = CommentEntity.toSaveEntity(commentDTO, boardEntity);
            // 1-3. 저장 후 생성된 댓글의 ID를 반환합니다.
            return commentRepository.save(commentEntity).getId();
        }
        return null;
    }

    /**
     * 2. 특정 게시글의 모든 댓글 목록 조회
     */
    @Transactional
    public List<CommentDTO> findAll(Long boardId) {
        // 2-1. 해당 게시글이 있는지 확인합니다.
        BoardEntity boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다."));

        // 2-2. 리포지토리의 커스텀 쿼리를 이용해 해당 게시글의 댓글을 최신순(ID 내림차순)으로 가져옵니다.
        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardEntityOrderByIdDesc(boardEntity);

        // 2-3. 화면에 보여주기 위해 Entity 리스트를 DTO 리스트로 변환합니다.
        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (CommentEntity commentEntity : commentEntityList) {
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setId(commentEntity.getId());
            commentDTO.setCommentWriter(commentEntity.getCommentWriter());
            commentDTO.setCommentContents(commentEntity.getCommentContents());

            // 2-4. 날짜 데이터 가공: BaseEntity에서 관리하는 생성 시간을 문자열로 변환하여 저장합니다.
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
        // 3-1. 삭제할 댓글을 조회합니다.
        CommentEntity commentEntity = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        // 3-2. 권한 검사 분기 (회원 vs 비회원)
        if (commentEntity.getMemberEmail() != null) {
            // [회원] 세션에 저장된 이메일과 댓글 작성자 이메일이 다르면 예외 발생
            if (!commentEntity.getMemberEmail().equals(loginEmail)) {
                throw new RuntimeException("본인의 댓글만 삭제할 수 있습니다.");
            }
        } else {
            // [비회원] 입력한 비밀번호와 DB에 저장된 비밀번호가 다르면 예외 발생
            if (!commentEntity.getCommentPass().equals(commentPass)) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }
        // 3-3. 검증 통과 시 삭제 실행
        commentRepository.deleteById(id);
    }

    /**
     * 4. 댓글 수정 (권한 검증 포함)
     */
    @Transactional
    public void update(CommentDTO commentDTO, String loginEmail) {
        // 4-1. 수정할 댓글을 조회합니다.
        CommentEntity commentEntity = commentRepository.findById(commentDTO.getId())
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        // 4-2. 삭제와 동일한 로직으로 수정 권한을 확인합니다.
        if (commentEntity.getMemberEmail() != null) {
            if (!commentEntity.getMemberEmail().equals(loginEmail)) {
                throw new RuntimeException("본인의 댓글만 수정할 수 있습니다.");
            }
        } else {
            if (!commentEntity.getCommentPass().equals(commentDTO.getCommentPass())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }

        // 4-3. 더티 체킹(Dirty Checking) 활용: 엔티티의 필드값만 바꾸면 트랜잭션 종료 시 자동 반영됩니다.
        commentEntity.setCommentContents(commentDTO.getCommentContents());
    }}
}