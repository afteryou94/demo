package com.example.demo.repository;

import com.example.demo.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
//1. JpaRepository 상속 (마법의 통로)
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    //2. 조회수 증가 로직 (updateHits)
    @Modifying // 1. 데이터에 변화가 생기는 쿼리(Update, Delete)임을 선언
    @Query(value = "update BoardEntity b set b.boardHits = b.boardHits + 1 where b.id = :id") // 2. 직접 쿼리 작성
    void updateHits(@Param("id") Long id); // 3. 파라미터 :id를 자바 변수 id와 매핑
}
