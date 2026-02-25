package com.example.demo.repository;

import com.example.demo.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 1. JpaRepository를 상속받음으로써 기본적인 CRUD(save, findAll, deleteById 등) 메서드를 자동으로 사용하게 됩니다.
// <BoardEntity, Long>은 이 리포지토리가 다루는 엔티티 클래스와 그 엔티티의 PK(기본키) 타입을 의미합니다.
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    // 2. 조회수 증가를 위한 커스텀 쿼리
    // @Modifying: 조회가 아닌 데이터 수정(Update, Delete) 쿼리임을 나타냅니다.
    // @Query: 직접 SQL과 유사한 JPQL을 작성합니다. (nativeQuery=true를 주면 실제 SQL 사용 가능)
    // :id 는 @Param("id")와 매칭되는 파라미터 변수입니다.
    @Modifying
    @Query(value = "update BoardEntity b set b.boardHits = b.boardHits + 1 where b.id = :id")
    void updateHits(@Param("id") Long id);

    // 3. 검색 및 페이징 처리를 위한 메서드 (메서드 이름 규칙 활용)
    // Containing: SQL의 LIKE %keyword% 기능을 수행합니다.
    // Pageable: 페이징 정보(현재페이지, 정렬방식 등)를 파라미터로 받습니다.
    // Page<BoardEntity>: 페이징 결과 데이터뿐만 아니라 전체 페이지 수, 현재 페이지 정보 등을 포함한 객체를 반환합니다.

    // 제목으로 검색
    Page<BoardEntity> findByBoardTitleContaining(String keyword, Pageable pageable);

    // 내용으로 검색
    Page<BoardEntity> findByBoardContentsContaining(String keyword, Pageable pageable);

    // 작성자로 검색
    Page<BoardEntity> findByBoardWriterContaining(String keyword, Pageable pageable);

    // 제목 또는 내용으로 검색 (두 필드를 결합하여 검색할 때 사용)
    Page<BoardEntity> findByBoardTitleContainingOrBoardContentsContaining(String title, String contents, Pageable pageable);
}