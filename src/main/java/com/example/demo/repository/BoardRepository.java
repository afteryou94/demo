package com.example.demo.repository;

import com.example.demo.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface BoardRepository extends JpaRepository<BoardEntity, Long> {


    @Modifying
    @Query(value = "update BoardEntity b set b.boardHits = b.boardHits + 1 where b.id = :id")
    void updateHits(@Param("id") Long id);


    Page<BoardEntity> findByBoardTitleContaining(String keyword, Pageable pageable);


    Page<BoardEntity> findByBoardContentsContaining(String keyword, Pageable pageable);


    Page<BoardEntity> findByBoardWriterContaining(String keyword, Pageable pageable);


    Page<BoardEntity> findByBoardTitleContainingOrBoardContentsContaining(String title, String contents, Pageable pageable);
}