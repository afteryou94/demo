package com.example.demo.dto;

import com.example.demo.entity.BoardEntity;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
    //1. 어노테이션 (Lombok)
@Getter //클래스의 필드(id, boardWriter 등)에 접근하고 값을 넣을 수 있는 getId(), setBoardWriter() 같은 메서드를 자동으로 만듭니다.
@Setter
@ToString //객체를 출력할 때 예쁘게 보여줌System.out.println(boardDTO)를 했을 때 주소값이 아닌 {id=1, boardWriter='홍길동'...} 처럼 필드값을 예쁘게 출력해줍니다. 디버깅할 때 필수입니다.
    //2. 필드
public class BoardDTO { // (데이터 저장 공간)
    private Long id;
    private String memberEmail; // 추가
    private String updatePass;
    private String boardTitle;
    private String boardContents;
    private int boardHits;
    private String boardCreatedAt;
        @NotBlank(message = "작성자는 필수입니다.")
        @Size(min = 2, max = 10, message = "이름은 2~10자 사이로 입력해주세요.")
        private String boardWriter;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 4, message = "비밀번호는 최소 4자 이상 입력해주세요.")
        private String boardPass;

    //3. 날짜 가공 메서드 (dateFormat)
    private String dateFormat(LocalDateTime date){
        if (date == null) return null; // 날짜 데이터가 없으면 빈 값 반환

        // 날짜를 "년-월-일 시:분" 형태의 글자(String)로 바꿈
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    //4. 핵심 로직: toBoardDTO (변환기)
    // static: 객체를 만들지 않고도 BoardDTO.toBoardDTO()로 바로 호출 가능
    public static BoardDTO toBoardDTO(BoardEntity boardEntity) {
        BoardDTO boardDTO = new BoardDTO(); // 4-1. 빈 DTO 가방을 하나 만든다.

        // 4-2. 엔티티(DB 데이터)에서 꺼내서 DTO 가방에 하나씩 옮겨 담는다.
        boardDTO.setId(boardEntity.getId());
        boardDTO.setBoardWriter(boardEntity.getBoardWriter());
        boardDTO.setBoardTitle(boardEntity.getBoardTitle());
        boardDTO.setBoardPass(boardEntity.getBoardPass());
        boardDTO.setBoardContents(boardEntity.getBoardContents());
        boardDTO.setBoardHits(boardEntity.getBoardHits());
        // [중요] 이 줄이 반드시 있어야 합니다!
        // 엔티티의 식별자(afteryou 등)를 DTO에 담아서 화면으로 전달해야 함
        boardDTO.setMemberEmail(boardEntity.getMemberEmail());

        // 4-3. 엔티티의 날짜를 가져와서 위에서 만든 dateFormat으로 예쁘게 깎아서 담는다.
        boardDTO.setBoardCreatedAt(boardDTO.dateFormat(boardEntity.getCreatedAt()));

        return boardDTO; // 4. 데이터가 꽉 찬 가방을 반환한다.
    }


    }
