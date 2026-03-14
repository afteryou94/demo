package com.example.demo.entity;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

//1. 주요 어노테이션 (공통 설정)
@Getter
//이 클래스는 실제 DB 테이블로 만들어지지 않습니다. 대신, 다른 엔티티(예: BoardEntity)가 이 클래스를 상속받으면, 여기에 선언된 필드(createdAt, updatedAt)를 상속받는 테이블의 컬럼으로 포함시키라는 뜻입니다.
@MappedSuperclass
//이 클래스에 'Auditing(감시)' 기능을 부여합니다. 데이터가 생성되거나 수정되는 순간을 스프링이 지켜보고 있다가 자동으로 시간을 채워줍니다.
@EntityListeners(AuditingEntityListener.class)
//2. 필드 구성 (시간 데이터)
public class BaseTimeEntity {
    @CreatedDate // 데이터가 처음 생성되어 DB에 저장될 때의 시간을 자동으로 기록
    @Column(nullable = false, updatable = false) // 생성 시간은 수정되면 안 되므로, UPDATE 쿼리에서 제외함
    private LocalDateTime createdAt;

    @LastModifiedDate// 데이터가 수정될 때마다 그 시점의 시간을 자동으로 기록
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
