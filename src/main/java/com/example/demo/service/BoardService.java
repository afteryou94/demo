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
    //1. 클래스 설정 및 저장 (Save)
    @Service // 이 클래스를 비즈니스 로직을 수행하는 서비스 빈으로 등록합니다.
    @RequiredArgsConstructor // final이 붙은 Repository 필드를 생성자 주입 방식으로 연결합니다.
    public class BoardService {
        private final BoardRepository boardRepository;

        public void save(BoardDTO boardDTO, String loginEmail) {
            // 1-1. 컨트롤러에서 전달받은 로그인 이메일을 DTO에 저장합니다.
            boardDTO.setMemberEmail(loginEmail);

            // 1-2. DB 저장을 위해 DTO 객체를 Entity 객체로 변환합니다. (엔티티 보호 목적)
            BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);

            // 1-3. 변환된 엔티티를 리포지토리를 통해 DB에 저장합니다.
            boardRepository.save(boardEntity);
        }

    //2. 목록 조회 및 변환 (Find All)
        //엔티티(Entity) 리스트를 DTO 리스트로 변환하는 이유는 보안과 유지보수를 위해 DB 구조를 외부에 직접 노출하지 않기 위함
    public List<BoardDTO> findAll() {
        // 2-1. DB에서 모든 데이터를 엔티티 형태로 가져옵니다.
        List<BoardEntity> boardEntityList = boardRepository.findAll();

        // 2-2. 화면으로 전달할 DTO 리스트 바구니를 만듭니다.
        List<BoardDTO> boardDTOList = new ArrayList<>();

        // 2-3. 반복문을 돌며 각 엔티티를 DTO로 변환하여 바구니에 담습니다.
        for (BoardEntity boardEntity: boardEntityList){
            boardDTOList.add(BoardDTO.toBoardDTO(boardEntity));
        }
        return boardDTOList;
    }

    //3. 조회수 및 상세 조회 (Hits & FindById)
    @Transactional // DB 값을 변경(Update)하므로 트랜잭션 처리가 필수입니다.
    public void updateHits(Long id) {
        // 리포지토리에 정의된 커스텀 쿼리를 호출하여 조회수를 1 증가시킵니다.
        boardRepository.updateHits(id);
    }

        public BoardDTO findById(Long id) {
            // ID로 게시글을 찾되, 결과가 없을 수 있으므로 Optional을 사용합니다.
            Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
            if (optionalBoardEntity.isPresent()) {
                // 데이터가 존재하면 DTO로 변환하여 반환합니다.
                return BoardDTO.toBoardDTO(optionalBoardEntity.get());
            } else {
                return null;
            }
        }

        //4. 권한 기반 삭제 및 수정 검증 (Delete & Update)
        @Transactional
        public void delete(Long id, String loginEmail, String inputPass) {
            // 4-1. 삭제할 글이 있는지 먼저 확인하고, 없으면 예외를 발생시킵니다.
            BoardEntity boardEntity = boardRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

            // 4-2. 로그인 글인지 비로그인 글인지 판단하여 검증을 진행합니다.
            if (boardEntity.getMemberEmail() != null && !boardEntity.getMemberEmail().isEmpty()) {
                // 로그인 유저: 세션의 이메일과 DB의 작성자 이메일이 다르면 예외 발생
                if (!boardEntity.getMemberEmail().equals(loginEmail)) {
                    throw new RuntimeException("본인의 글만 삭제할 수 있습니다.");
                }
            } else {
                // 비로그인 유저: 입력한 비밀번호와 DB의 비밀번호가 다르면 예외 발생
                if (!boardEntity.getBoardPass().equals(inputPass)) {
                    throw new RuntimeException("비밀번호가 일치하지 않습니다.");
                }
            }
            // 4-3. 모든 검증을 통과하면 비로소 삭제를 실행합니다.
            boardRepository.deleteById(id);
        }

        //5. 업데이트
        @Transactional
        public void update(BoardDTO boardDTO, String loginEmail, String inputPass) {
            BoardEntity boardEntity = boardRepository.findById(boardDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

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

        //6. 동적 페이징 및 검색 (Paging & Search)
        public Page<BoardDTO> paging(Pageable pageable, String type, String keyword) {
            // 6-1. JPA는 페이지 번호를 0부터 시작하므로, 사용자 요청(1)에서 1을 뺍니다.
            int page = pageable.getPageNumber() - 1;
            int pageLimit = 10; // 한 페이지에 보여줄 글 개수
            Page<BoardEntity> boardEntities;

            // 6-2. 검색어(keyword) 존재 여부에 따라 분기 처리합니다.
            if (keyword != null && !keyword.isEmpty()) {
                // 검색 조건(제목, 내용, 작성자)에 맞는 쿼리를 호출합니다. (ID 내림차순 정렬 포함)
                if ("title".equals(type)) {
                    boardEntities = boardRepository.findByBoardTitleContaining(keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
                } else if ("contents".equals(type)) {
                    boardEntities = boardRepository.findByBoardContentsContaining(keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
                } else if ("writer".equals(type)) {
                    boardEntities = boardRepository.findByBoardWriterContaining(keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
                } else { // "all" (제목+내용 합친 검색)
                    boardEntities = boardRepository.findByBoardTitleContainingOrBoardContentsContaining(keyword, keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
                }
            } else {
                // 검색어가 없으면 전체 목록을 페이징하여 가져옵니다.
                boardEntities = boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
            }

            // 6-3. 가져온 Page<BoardEntity>를 Page<BoardDTO>로 변환(Map)하여 반환합니다.
            return boardEntities.map(board -> new BoardDTO(
                    board.getId(), board.getBoardWriter(), board.getBoardTitle(), board.getBoardHits(), board.getCreatedAt()
            ));
        }


}
