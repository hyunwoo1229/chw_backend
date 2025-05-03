package com.example.study3.controller;

import com.example.study3.dto.BoardRequestDto;
import com.example.study3.dto.BoardResponseDto;
import com.example.study3.dto.SuccessResponse;
import com.example.study3.repository.BoardRepository;
import com.example.study3.repository.MemberRepository;
import com.example.study3.repository.MusicRepository;
import com.example.study3.security.jwt.JwtTokenProvider;
import com.example.study3.service.BoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/board")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> createboard(@RequestBody BoardRequestDto dto, Authentication authentication) {
        boardService.createBoard(dto, authentication);
        return ResponseEntity.ok(new SuccessResponse("게시글 등록 완료"));
    }

    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> getAllBoards() {
        return ResponseEntity.ok(boardService.getAllBoards());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDto> getBoardDetail(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(boardService.getBoardDetail(id, authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> updateBoard(@PathVariable Long id, @RequestBody BoardRequestDto dto, Authentication authentication) {
        boardService.updateBoard(id, dto, authentication);
        return ResponseEntity.ok(new SuccessResponse("게시글 수정 완료"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> deleteBoard(@PathVariable Long id, Authentication authentication) {
        boardService.deleteBoard(id, authentication);
        return ResponseEntity.ok(new SuccessResponse("게시글 삭제 완료"));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BoardResponseDto>> getMyBoards(Authentication authentication) {
        return ResponseEntity.ok(boardService.getBoardsByLoginId(authentication));
    }
}
