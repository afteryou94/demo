package com.example.demo.repository;

import com.example.demo.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 1. JpaRepository 상속
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    // 2. 조회수 증가 로직
    @Modifying
    @Query(value = "update BoardEntity b set b.boardHits = b.boardHits + 1 where b.id = :id")
    void updateHits(@Param("id") Long id);

    // 3. 검색 쿼리 메서드 (반드시 org.springframework.data.domain.Page를 사용해야 함)

    // 제목 검색
    Page<BoardEntity> findByBoardTitleContaining(String keyword, Pageable pageable);

    // 내용 검색
    Page<BoardEntity> findByBoardContentsContaining(String keyword, Pageable pageable);

    // 작성자 검색
    Page<BoardEntity> findByBoardWriterContaining(String keyword, Pageable pageable);

    // 제목 또는 내용 검색
    Page<BoardEntity> findByBoardTitleContainingOrBoardContentsContaining(String title, String contents, Pageable pageable);
}