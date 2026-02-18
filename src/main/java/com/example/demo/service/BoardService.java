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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
    //1. 클래스 설정
@Service // 이 클래스가 서비스 레이어임을 스프링에 등록
@RequiredArgsConstructor // final 붙은 boardRepository를 자동으로 주입(연결)해줌
public class BoardService {
    private final BoardRepository boardRepository; // DB와 연결되는 통로
        // 2. 글 저장 (Save)
//    public void save(BoardDTO boardDTO) {
//        // 2-1. 화면에서 받아온 DTO를 DB에 저장하기 위한 Entity 형식으로 변환함
//        BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);
//        // 2-2. 리포지토리를 통해 실제 DB에 저장함
//        boardRepository.save(boardEntity);
//    }

     public void save(BoardDTO boardDTO, String loginEmail) {
         // 세션에서 가져온 로그인 이메일을 DTO에 담음
         boardDTO.setMemberEmail(loginEmail);

         BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);
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

        // 상세 조회 시 작성자 확인 (수정 화면 진입 전 검증용)
        public BoardDTO findById(Long id) {
            Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
            if (optionalBoardEntity.isPresent()) {
                return BoardDTO.toBoardDTO(optionalBoardEntity.get());
            } else {
                return null;
            }
        }

        // 삭제 처리 전 검증
        @Transactional
        public void delete(Long id, String loginEmail, String inputPass) {
            BoardEntity boardEntity = boardRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

            if (boardEntity.getMemberEmail() != null) {
                // 1. 로그인 유저가 쓴 글인 경우: 이메일 비교
                if (!boardEntity.getMemberEmail().equals(loginEmail)) {
                    throw new RuntimeException("본인의 글만 삭제할 수 있습니다.");
                }
            } else {
                // 2. 비로그인 유저가 쓴 글인 경우: 비밀번호 비교
                if (!boardEntity.getBoardPass().equals(inputPass)) {
                    throw new RuntimeException("비밀번호가 일치하지 않습니다.");
                }
            }
            boardRepository.deleteById(id);
        }

        // 수정 처리 전 검증
        @Transactional
        public void update(BoardDTO boardDTO, String loginEmail, String inputPass) {
            BoardEntity boardEntity = boardRepository.findById(boardDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

//            if (boardEntity.getMemberEmail() != null) {
//                // 1. 로그인 유저가 쓴 글인 경우: 이메일 비교
//                if (!boardEntity.getMemberEmail().equals(loginEmail)) {
//                    throw new RuntimeException("본인의 글만 수정할 수 있습니다.");
//                }
//            } else {
//                // 2. 비로그인 유저가 쓴 글인 경우: 비밀번호 비교
//                if (!boardEntity.getBoardPass().equals(inputPass)) {
//                    throw new RuntimeException("비밀번호가 일치하지 않습니다.");
//                }
//            }
            // BoardService.java의 삭제/수정 로직 권장 형태
            if (boardEntity.getMemberEmail() != null && !boardEntity.getMemberEmail().isEmpty()) {
                // 로그인 글 비교
            } else {
                // 비로그인 글 비밀번호 비교
                if (!boardEntity.getBoardPass().equals(inputPass)) {
                    throw new RuntimeException("비밀번호 불일치");
                }
            }

            boardEntity.update(boardDTO); // 기존 update 메서드 호출
        }

// BoardService.java 에 추가/수정

        public Page<BoardDTO> paging(Pageable pageable, String type, String keyword) {
            int page = pageable.getPageNumber() - 1;
            int pageLimit = 10;
            Page<BoardEntity> boardEntities;

            // 검색 조건이 있는 경우
            if (keyword != null && !keyword.isEmpty()) {
                if ("title".equals(type)) {
                    boardEntities = boardRepository.findByBoardTitleContaining(keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
                } else if ("contents".equals(type)) {
                    boardEntities = boardRepository.findByBoardContentsContaining(keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
                } else if ("writer".equals(type)) {
                    boardEntities = boardRepository.findByBoardWriterContaining(keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
                } else { // "all" (제목+내용)
                    boardEntities = boardRepository.findByBoardTitleContainingOrBoardContentsContaining(keyword, keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
                }
            } else {
                // 검색 조건이 없는 경우 기존 페이징 로직
                boardEntities = boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
            }

            return boardEntities.map(board -> new BoardDTO(
                    board.getId(), board.getBoardWriter(), board.getBoardTitle(), board.getBoardHits(), board.getCreatedAt()
            ));
        }


}
