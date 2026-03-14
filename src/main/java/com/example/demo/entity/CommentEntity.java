package com.example.demo.entity;

import com.example.demo.dto.CommentDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "comment_table")
public class CommentEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String commentContents;


    @Column
    private String commentWriter;


    @Column
    private String commentPass;


    @Column
    private String memberEmail;

    /* 핵심 설정: 게시글과의 연관관계 */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private BoardEntity boardEntity;


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