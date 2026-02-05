package com.example.demo.service;

import com.example.demo.dto.BoardDTO;
import com.example.demo.entity.BoardEntity;
import com.example.demo.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

    //1. 클래스 설정
@Service // 이 클래스가 서비스 레이어임을 스프링에 등록
@RequiredArgsConstructor // final 붙은 boardRepository를 자동으로 주입(연결)해줌
public class BoardService {
    private final BoardRepository boardRepository; // DB와 연결되는 통로
        // 2. 글 저장 (Save)
    public void save(BoardDTO boardDTO) {
        // 2-1. 화면에서 받아온 DTO를 DB에 저장하기 위한 Entity 형식으로 변환함
        BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);
        // 2-2. 리포지토리를 통해 실제 DB에 저장함
        boardRepository.save(boardEntity);
    }

    //3. 전체 목록 가져오기 (Find All)
    public List<BoardDTO> findAll() {
        // 3-1. DB에서 모든 글(Entity)을 가져와서 리스트에 담음
        List<BoardEntity> boardEntityList = boardRepository.findAll();
        // 3-2. 컨트롤러(화면)에 줄 DTO 리스트를 새로 만듦=View(HTML)에는 DTO만 전달해야 되니까
        List<BoardDTO> boardDTOList = new ArrayList<>();
        // 3-3. 반복문을 돌며 Entity를 하나하나 DTO로 변환해서 리스트에 채움
        for (BoardEntity boardEntity: boardEntityList){
            boardDTOList.add(BoardDTO.toBoardDTO(boardEntity));
        }
        return boardDTOList; // 3-4. 꽉 찬 DTO 리스트를 반환
    }

    //4. 조회수 증가 및 상세 조회 (Hits & FindById)
    @Transactional // DB 상태를 변경할 때 안전하게 처리하기 위한 어노테이션
    public void updateHits(Long id) {
        boardRepository.updateHits(id); // 리포지토리에 정의된 쿼리로 조회수 +1
    }

    public BoardDTO findById(Long id) {
        // Optional: 데이터가 있을 수도, 없을 수도 있다는 것을 표현하는 자바 박스
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        if (optionalBoardEntity.isPresent()) { // 만약 데이터가 박스 안에 있다면
            BoardEntity boardEntity = optionalBoardEntity.get(); // 꺼내서
            return BoardDTO.toBoardDTO(boardEntity); // DTO로 변환해서 반환
        } else {
            return null; // 없으면 null 반환
        }
    }

    public void delete(Long id) {
        boardRepository.deleteById(id);
    }

    //5. 글 수정 (Update) - 핵심 로직
    @Transactional
    public void update(BoardDTO boardDTO) {
        // 5-1. 기존 게시글을 DB에서 가져옴. 없으면 에러(Exception)를 던짐,
        BoardEntity boardEntity = boardRepository.findById(boardDTO.getId()).orElseThrow(() ->
                //↑ boardDTO.getId()의 의미: "사용자가 지금 몇 번 글을 수정하겠다고 요청했지?" 하고 가방에서 이름표(ID)를 확인하는 것
                //↑ boardRepository.findById(...)의 의미: "그 이름표(ID)를 가진 **진짜 데이터(Entity)**를 DB 창고에서 가져와!"라는 뜻
                new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        // 5-2. DB에 저장된 비번(getBoardPass)과 사용자가 수정을 시도하며 입력한 비번(getUpdatePass)을 비교
        if (boardEntity.getBoardPass().equals(boardDTO.getUpdatePass())){
            // 5-3. 일치하면 Entity의 내용을 업데이트함 (Dirty Checking 방식)
            boardEntity.update(boardDTO);
        } else {
            // 5-4. 일치하지 않으면 에러를 발생시켜서 작업을 중단함
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }
}
