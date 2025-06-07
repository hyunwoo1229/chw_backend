package com.example.study3.service;

import com.example.study3.domain.BoardEntity;
import com.example.study3.domain.Member;
import com.example.study3.domain.MusicEntity;
import com.example.study3.dto.BoardCategoriesResponseDto;
import com.example.study3.dto.BoardRequestDto;
import com.example.study3.dto.BoardResponseDto;
import com.example.study3.repository.BoardRepository;
import com.example.study3.repository.MemberRepository;
import com.example.study3.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final MusicRepository musicRepository;

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
    public BoardCategoriesResponseDto getBoardsByCategories(Authentication auth) {
        Integer age     = null;
        String country  = null;
        String gender   = null;

        if (auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {

            String loginId = (String) auth.getPrincipal();
            Member member = memberRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

            age     = member.getAge();
            country = member.getCountry();
            gender  = member.getGender();
        }

        // 기존 메서드 재사용
        List<BoardResponseDto> popular      = getBoardsByViews();
        List<BoardResponseDto> recent       = getBoardsByRecent();
        List<BoardResponseDto> sameAge      = getBoardsBySameAgeRange(age);
        List<BoardResponseDto> sameCountry  = getBoardsBySameCountry(country);
        List<BoardResponseDto> sameGender   = getBoardsBySameGender(gender);
        List<BoardResponseDto> randomBoards = getRandomBoardsAll();

        return new BoardCategoriesResponseDto(popular, recent, sameAge, sameCountry, sameGender, randomBoards);
    }

    //게시물 하나 들어가서 보기
    public BoardResponseDto getBoardDetail(Long id, Authentication auth) {
        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        board.setViews(board.getViews() + 1);
        boardRepository.save(board);

        String loginId = null;
        boolean isAuthor = false;
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            loginId = (String) auth.getPrincipal();
            isAuthor = board.getMember().getLoginId().equals(loginId);
        }

        BoardResponseDto dto = toDto(board);
        dto.setContent(board.getContent());
        dto.setAuthor(isAuthor);
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

    //마이페이지
    public List<BoardResponseDto> getBoardsByLoginId(Authentication auth) {
        String loginId = (String) auth.getPrincipal();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return boardRepository.findByMember(member).stream()
                .map(board -> {
                    BoardResponseDto dto = toDto(board);
                    dto.setAuthor(true);           // 마이페이지이므로 항상 true
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //조회순 게시물
    public List<BoardResponseDto> getBoardsByViews() {
        return boardRepository.findAllByOrderByViewsDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    //최신순 게시물
    public List<BoardResponseDto> getBoardsByRecent() {
        return boardRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    //같은 나이대 게시물 조회
    public List<BoardResponseDto> getBoardsBySameAgeRange(Integer age) {
        if (age == null) return List.of();
        int start = (age / 10) * 10;
        int end   = start + 9;
        List<BoardResponseDto> list = boardRepository
                .findByMember_AgeBetween(start, end)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        Collections.shuffle(list);
        return list;
    }

    //같은 국가 게시물 조회
    public List<BoardResponseDto> getBoardsBySameCountry(String country) {
        if (country == null || country.isEmpty()) return List.of();
        List<BoardResponseDto> list = boardRepository
                .findByMember_Country(country)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        Collections.shuffle(list);
        return list;
    }

    //같은 성별 게시물 조회
    public List<BoardResponseDto> getBoardsBySameGender(String gender) {
        if (gender == null || gender.isEmpty()) return List.of();
        List<BoardResponseDto> list = boardRepository
                .findByMember_Gender(gender)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        Collections.shuffle(list);
        return list;
    }

    //카테고리별 게시물을 한 번에 묶어서 반환
    public BoardCategoriesResponseDto getBoardsByCategories(Integer age, String country, String gender) {
        List<BoardResponseDto> popular   = getBoardsByViews();
        List<BoardResponseDto> recent    = getBoardsByRecent();
        List<BoardResponseDto> sameAge   = getBoardsBySameAgeRange(age);
        List<BoardResponseDto> sameCountry = getBoardsBySameCountry(country);
        List<BoardResponseDto> sameGender  = getBoardsBySameGender(gender);
        List<BoardResponseDto> randomBoards = getRandomBoardsAll();

        return new BoardCategoriesResponseDto(popular, recent, sameAge, sameCountry, sameGender, randomBoards);
    }

    //검색
    public List<BoardResponseDto> searchByTitle(String keyword){
        return boardRepository.findByTitleContainingIgnoreCase(keyword).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    //랜덤 게시물
    public List<BoardResponseDto> getRandomBoardsAll() {
        List<BoardResponseDto> all = boardRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        Collections.shuffle(all);
        return all;
    }

    //BoardEntity를 BpardResponseDto로 매핑하는 공통 로직
    private BoardResponseDto toDto(BoardEntity boardEntity) {
        BoardResponseDto dto = new BoardResponseDto();
        dto.setId(boardEntity.getId());
        dto.setTitle(boardEntity.getTitle());
        dto.setContent(null); // 필요하다면 BoardEntity.getContent()를 넣을 수 있습니다.
        dto.setAuthorName(boardEntity.getMember().getName());
        dto.setCreatedAt(boardEntity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        dto.setAudioUrl(boardEntity.getMusic().getAudioUrl());
        dto.setImageUrl(boardEntity.getMusic().getImageUrl());
        dto.setAuthor(false); // 인증 정보가 필요한 경우, Controller에서 별도 처리
        dto.setViews(boardEntity.getViews());
        return dto;
    }
}
