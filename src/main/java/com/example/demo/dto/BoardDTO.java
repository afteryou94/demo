package com.example.demo.dto;

import com.example.demo.entity.BoardEntity;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@ToString

public class BoardDTO {
    private Long id;
    private String memberEmail;
    private String updatePass;
    @NotBlank(message = "제목을 입력해주십시오.")
    private String boardTitle;
    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "내용을 입력해주십시오.")
    private String boardContents;
    private int boardHits;
    private String boardCreatedAt;
    @NotBlank(message = "작성자는 필수입니다.")
    @Size(min = 2, max = 10, message = "이름은 2~10자 사이로 입력해주세요.")
    private String boardWriter;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, message = "비밀번호는 최소 4자 이상 입력해주세요.")
    private String boardPass;


    private String dateFormat(LocalDateTime date) {
        if (date == null) return null;


        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }


    public static BoardDTO toBoardDTO(BoardEntity boardEntity) {
        BoardDTO boardDTO = new BoardDTO();


        boardDTO.setId(boardEntity.getId());
        boardDTO.setBoardWriter(boardEntity.getBoardWriter());
        boardDTO.setBoardTitle(boardEntity.getBoardTitle());
        boardDTO.setBoardPass(boardEntity.getBoardPass());
        boardDTO.setBoardContents(boardEntity.getBoardContents());
        boardDTO.setBoardHits(boardEntity.getBoardHits());


        boardDTO.setMemberEmail(boardEntity.getMemberEmail());


        boardDTO.setBoardCreatedAt(boardDTO.dateFormat(boardEntity.getCreatedAt()));

        return boardDTO;
    }


    public BoardDTO() {
    }


    public BoardDTO(Long id, String boardWriter, String boardTitle, int boardHits, LocalDateTime createdAt) {
        this.id = id;
        this.boardWriter = boardWriter;
        this.boardTitle = boardTitle;
        this.boardHits = boardHits;

        this.boardCreatedAt = dateFormat(createdAt);
    }


}
