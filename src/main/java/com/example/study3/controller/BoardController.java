package com.example.study3.controller;

import com.example.study3.domain.BoardEntity;
import com.example.study3.domain.Member;
import com.example.study3.domain.MusicEntity;
import com.example.study3.dto.*;
import com.example.study3.repository.BoardRepository;
import com.example.study3.repository.MemberRepository;
import com.example.study3.repository.MusicRepository;
import com.example.study3.security.jwt.JwtTokenProvider;
import com.example.study3.service.BoardService;
import com.example.study3.service.YoutubeUploadService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/board")
@RequiredArgsConstructor

public class BoardController {

    private final BoardService boardService;
    private final BoardRepository boardRepository;
    private final YoutubeUploadService youtubeUploadService;
    private final MemberRepository memberRepository;

    //게시물 생성
    @PostMapping
    public ResponseEntity<SuccessResponse> createboard(@RequestBody BoardRequestDto dto, Authentication authentication) {
        boardService.createBoard(dto, authentication);
        return ResponseEntity.ok(new SuccessResponse("게시글 등록 완료"));
    }

    //전체 게시물 조회
    @GetMapping
    public ResponseEntity<BoardCategoriesResponseDto> getBoardsByCategories(
            Authentication authentication
    ) {
        Integer age = null;
        String country = null;
        String gender = null;

        // 1) 인증된 사용자(authentication) 정보가 넘어왔으면
        //    authentication.getPrincipal()은 Spring Security가 세션(JWT)에서 꺼낸 loginId (String)이다.
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            String loginId = (String) authentication.getPrincipal();

            // 2) DB(Member 테이블)에서 해당 loginId의 회원을 꺼내 age/country/gender를 읽어 온다.
            Member member = memberRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

            age     = member.getAge();      // DB에 저장된 실제 age
            country = member.getCountry();  // DB에 저장된 실제 country
            gender  = member.getGender();   // DB에 저장된 실제 gender
        }

        // 3) 서비스에서 다섯가지 필터링 로직을 실행, 나머지는 null→빈 리스트로 처리
        BoardCategoriesResponseDto responseDto =
                boardService.getBoardsByCategories(age, country, gender);

        return ResponseEntity.ok(responseDto);
    }
    //하나의 게시물 자세히 보기
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDto> getBoardDetail(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(boardService.getBoardDetail(id, authentication));
    }

    //게시물 수정
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> updateBoard(@PathVariable Long id, @RequestBody BoardRequestDto dto, Authentication authentication) {
        boardService.updateBoard(id, dto, authentication);
        return ResponseEntity.ok(new SuccessResponse("게시글 수정 완료"));
    }

    //게시물 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deleteBoard(@PathVariable Long id, Authentication authentication) {
        boardService.deleteBoard(id, authentication);
        return ResponseEntity.ok(new SuccessResponse("게시글 삭제 완료"));
    }

    //마이페이지
    @GetMapping("/my")
    public ResponseEntity<List<BoardResponseDto>> getMyBoards(Authentication authentication) {
        return ResponseEntity.ok(boardService.getBoardsByLoginId(authentication));
    }

    //검색
    @GetMapping("/search")
    public ResponseEntity<List<BoardResponseDto>> searchBoardsByTitle(
            @RequestParam("query") String query
    ) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<BoardResponseDto> result = boardService.searchByTitle(query.trim());
        return ResponseEntity.ok(result);
    }



}


