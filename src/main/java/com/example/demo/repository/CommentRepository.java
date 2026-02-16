package com.example.demo.repository;

import com.example.demo.entity.CommentEntity;
import com.example.demo.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    // 특정 게시글의 댓글 목록을 가져오는 메서드 (작성순 정렬)
    List<CommentEntity> findAllByBoardEntityOrderByIdDesc(BoardEntity boardEntity);
}