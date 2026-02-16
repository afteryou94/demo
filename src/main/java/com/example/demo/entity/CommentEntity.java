package com.example.demo.entity;

import com.example.demo.dto.CommentDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "comment_table")
public class CommentEntity extends BaseTimeEntity { // BaseTimeEntity 상속으로 작성시간 자동 관리
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String commentContents;

    // 비로그인 작성자 (2~10자)
    @Column
    private String commentWriter;

    // 비로그인 비밀번호 (최소 4자)
    @Column
    private String commentPass;

    // 회원 이메일 (로그인 유저인 경우 저장)
    @Column
    private String memberEmail;

    /* 핵심 설정: 게시글과의 연관관계 */
    // optional = false: 게시글 없는 댓글은 존재할 수 없음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private BoardEntity boardEntity;

    // DTO -> Entity 변환 메서드
    public static CommentEntity toSaveEntity(CommentDTO commentDTO, BoardEntity boardEntity) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setCommentContents(commentDTO.getCommentContents());
        commentEntity.setCommentWriter(commentDTO.getCommentWriter());
        commentEntity.setCommentPass(commentDTO.getCommentPass());
        commentEntity.setMemberEmail(commentDTO.getMemberEmail());
        commentEntity.setBoardEntity(boardEntity);
        return commentEntity;
    }
}