package com.example.study3.service;

import com.example.study3.domain.BoardEntity;
import com.example.study3.domain.Member;
import com.example.study3.domain.MusicEntity;
import com.example.study3.dto.BoardRequestDto;
import com.example.study3.dto.BoardResponseDto;
import com.example.study3.repository.BoardRepository;
import com.example.study3.repository.MemberRepository;
import com.example.study3.repository.MusicRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final MusicRepository musicRepository;

    public BoardService(BoardRepository boardRepository, MemberRepository memberRepository, MusicRepository musicRepository) {
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
        this.musicRepository = musicRepository;
    }

    public void createBoard(BoardRequestDto dto, Authentication auth) {
        String loginId = (String) auth.getPrincipal();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        MusicEntity music = musicRepository.findById(dto.getMusicId())
                .orElseThrow(() -> new IllegalArgumentException("유요하지 않은 음악 ID입니다."));

        BoardEntity board = new BoardEntity();
        board.setTitle(dto.getTitle());
        board.setContent(dto.getContent());
        board.setMember(member);
        board.setMusic(music);
        boardRepository.save(board);
    }

    //게시물 목록
    public List<BoardResponseDto> getAllBoards() {
        return boardRepository.findAll().stream().map(boardEntity -> {
            BoardResponseDto dto = new BoardResponseDto();
            dto.setId(boardEntity.getId());
            dto.setTitle(boardEntity.getTitle());
            dto.setContent(null);
            dto.setAuthorName(boardEntity.getMember().getName());
            dto.setCreatedAt(boardEntity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            dto.setAudioUrl(boardEntity.getMusic().getAudioUrl());
            dto.setImageUrl(boardEntity.getMusic().getImageUrl());
            dto.setAuthor(false);
            dto.setViews(boardEntity.getViews());
            return dto;
        }).collect(Collectors.toList());
    }

    //게시물 하나 들어가서 보기
    public BoardResponseDto getBoardDetail(Long id, Authentication auth) {
        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        board.setViews(board.getViews() + 1);
        boardRepository.save(board);

        String loginId = null;
        boolean isAuthor = false;
        if(auth != null && auth.isAuthenticated() && "anonymousUser".equals(auth.getPrincipal())) {
            loginId = (String) auth.getPrincipal();
            isAuthor = board.getMember().getLoginId().equals(loginId);
        }


        BoardResponseDto dto = new BoardResponseDto();
        dto.setId(board.getId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setAuthorName(board.getMember().getName());
        dto.setCreatedAt(board.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        dto.setAudioUrl(board.getMusic().getAudioUrl());
        dto.setImageUrl(board.getMusic().getImageUrl());
        dto.setAuthor(isAuthor);
        dto.setViews(board.getViews());
        return dto;
    }


    //게시글 수정
    public void updateBoard(Long id, BoardRequestDto dto, Authentication auth) {
        String loginId = (String) auth.getPrincipal();

        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다"));

        if(!board.getMember().getLoginId().equals(loginId)) {
            throw new SecurityException("작성자만 수정할 수 있습니다.");
        }

        board.setTitle(dto.getTitle());
        board.setContent(dto.getContent());
        boardRepository.save(board);
    }

    //게시글 삭제
    public void deleteBoard(Long id, Authentication auth) {
        String loginId = (String) auth.getPrincipal();

        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if(!board.getMember().getLoginId().equals(loginId)) {
            throw new SecurityException("작성자만 삭제할 수 있습니다.");
        }

        boardRepository.delete(board);
    }
}
