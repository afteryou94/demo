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

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public void save(BoardDTO boardDTO, String loginEmail) {

        boardDTO.setMemberEmail(loginEmail);


        BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);


        boardRepository.save(boardEntity);
    }


    public List<BoardDTO> findAll() {

        List<BoardEntity> boardEntityList = boardRepository.findAll();


        List<BoardDTO> boardDTOList = new ArrayList<>();


        for (BoardEntity boardEntity : boardEntityList) {
            boardDTOList.add(BoardDTO.toBoardDTO(boardEntity));
        }
        return boardDTOList;
    }


    @Transactional
    public void updateHits(Long id) {

        boardRepository.updateHits(id);
    }

    public BoardDTO findById(Long id) {

        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        if (optionalBoardEntity.isPresent()) {

            return BoardDTO.toBoardDTO(optionalBoardEntity.get());
        } else {
            return null;
        }
    }


    @Transactional
    public void delete(Long id, String loginEmail, String inputPass) {

        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));


        if (boardEntity.getMemberEmail() != null && !boardEntity.getMemberEmail().isEmpty()) {

            if (!boardEntity.getMemberEmail().equals(loginEmail)) {
                throw new RuntimeException("본인의 글만 삭제할 수 있습니다.");
            }
        } else {

            if (!boardEntity.getBoardPass().equals(inputPass)) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }

        boardRepository.deleteById(id);
    }


    @Transactional
    public void update(BoardDTO boardDTO, String loginEmail, String inputPass) {
        BoardEntity boardEntity = boardRepository.findById(boardDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));


        if (boardEntity.getMemberEmail() != null && !boardEntity.getMemberEmail().isEmpty()) {

        } else {

            if (!boardEntity.getBoardPass().equals(inputPass)) {
                throw new RuntimeException("비밀번호 불일치");
            }
        }

        boardEntity.update(boardDTO);
    }


    public Page<BoardDTO> paging(Pageable pageable, String type, String keyword) {

        int page = pageable.getPageNumber() - 1;
        int pageLimit = 10;
        Page<BoardEntity> boardEntities;


        if (keyword != null && !keyword.isEmpty()) {

            if ("title".equals(type)) {
                boardEntities = boardRepository.findByBoardTitleContaining(keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
            } else if ("contents".equals(type)) {
                boardEntities = boardRepository.findByBoardContentsContaining(keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
            } else if ("writer".equals(type)) {
                boardEntities = boardRepository.findByBoardWriterContaining(keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
            } else {
                boardEntities = boardRepository.findByBoardTitleContainingOrBoardContentsContaining(keyword, keyword, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
            }
        } else {

            boardEntities = boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
        }


        return boardEntities.map(board -> new BoardDTO(
                board.getId(), board.getBoardWriter(), board.getBoardTitle(), board.getBoardHits(), board.getCreatedAt()
        ));
    }


}
