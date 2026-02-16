package com.example.demo.entity;

import com.example.demo.dto.BoardDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

//1. 주요 어노테이션 (설계 규칙)
@Entity //"이 클래스는 이제부터 자바 객체가 아니라 DB 테이블이다!"라고 스프링(JPA)에게 선언하는 것입니다.
@Getter
    //JPA는 내부적으로 기본 생성자가 꼭 필요합니다.
    //하지만 외부에서 new BoardEntity()를 함부로 호출하지 못하도록 **PROTECTED**로 막아둔 아주 좋은 습관의 코드입니다. (무분별한 객체 생성을 방지합니다.)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "board_table_2025") //DB에 생성될 테이블의 이름을 직접 지정합니다. 이 설정 덕분에 MySQL에 board_table_2025라는 이름의 테이블이 만들어집니다.
public class BoardEntity extends BaseTimeEntity { //작성 시간, 수정 시간 기능을 상속받습니다. 이 덕분에 모든 게시글에 자동으로 시간이 기록됩니다.
    // 2. 필드 구성 (테이블 컬럼)
    @Id // 이 필드가 테이블의 기본키(PK)임을 선언
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL의 auto_increment (번호 자동 증가)
    private Long id;

    @Column // 별도 설정이 없으면 필드명이 컬럼명이 됨
    private String boardWriter;

    @Column
    private String memberEmail;

    @Column
    private String boardPass;

    @Column
    private String boardTitle;

    @Column
    private String boardContents;

    @Column
    private int boardHits;

// BoardEntity.java 내부에 추가

        @OneToMany(mappedBy = "boardEntity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
        private List<CommentEntity> commentEntityList = new ArrayList<>();

//3. 정적 메서드: toSaveEntity (저장용 변환기) : 사용자가 쓴 글(DTO)을 DB에 처음 저장하기 위해 Entity로 바꾸는 과정
    public static BoardEntity toSaveEntity(BoardDTO boardDTO) {
        BoardEntity boardEntity = new BoardEntity(); // 1. 새 엔티티 객체 생성
        boardEntity.boardWriter = boardDTO.getBoardWriter(); // 2. DTO 값 복사
        // DTO에 담긴 이메일을 엔티티에 복사
        boardEntity.memberEmail = boardDTO.getMemberEmail();
        boardEntity.boardPass = boardDTO.getBoardPass();
        boardEntity.boardTitle = boardDTO.getBoardTitle();
        boardEntity.boardContents = boardDTO.getBoardContents();
        boardEntity.boardHits = 0; // 3. 처음 글을 쓸 때 조회수는 무조건 0으로 초기화
        return boardEntity;
    }
//4. 핵심 로직: update (수정 기능)
        //Service 클래스에서 이 update 메서드를 실행 -> 엔터티 내용 바뀜 -> 메서드가 끝날 때 자동으로 DB에 UPDATE 쿼리반영
    public void update(BoardDTO boardDTO) {
        this.boardTitle = boardDTO.getBoardTitle();
        this.boardContents = boardDTO.getBoardContents();
    }
}
