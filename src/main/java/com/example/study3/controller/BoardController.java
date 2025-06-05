package com.example.study3.controller;

import com.example.study3.domain.BoardEntity;
import com.example.study3.domain.Member;
import com.example.study3.domain.MusicEntity;
import com.example.study3.dto.BoardRequestDto;
import com.example.study3.dto.BoardResponseDto;
import com.example.study3.dto.ErrorResponse;
import com.example.study3.dto.SuccessResponse;
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
    public ResponseEntity<List<BoardResponseDto>> getAllBoards() {
        return ResponseEntity.ok(boardService.getAllBoards());
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

}


