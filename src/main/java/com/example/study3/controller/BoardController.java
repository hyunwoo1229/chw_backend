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
public class BoardController {

    private final BoardService boardService;
    private final BoardRepository boardRepository;
    private final YoutubeUploadService youtubeUploadService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final MemberRepository memberRepository;

    public BoardController(BoardService boardService, BoardRepository boardRepository, YoutubeUploadService youtubeUploadService,
                           OAuth2AuthorizedClientService authorizedClientService, MemberRepository memberRepository) {
        this.boardService = boardService;
        this.boardRepository = boardRepository;
        this.youtubeUploadService = youtubeUploadService;
        this.authorizedClientService = authorizedClientService;
        this.memberRepository = memberRepository;
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

    @PostMapping("/{id}/youtube")
    public ResponseEntity<?> uploadToYoutube(@PathVariable Long id, Authentication authentication) {
        Member member = memberRepository.findByLoginId(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요"));

        BoardEntity board = boardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글 없음"));

        MusicEntity music = board.getMusic();
        if (music == null || music.getAudioUrl() == null || music.getImageUrl() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "음악 정보가 없습니다");
        }

        // ✅ 필요한 정보 선언
        String accessToken = member.getYoutubeAccessToken();  // ✅ DB에 저장된 YouTube 토큰
        String imageUrl = music.getImageUrl();
        String audioUrl = music.getAudioUrl();
        String title = board.getTitle();
        String description = board.getContent() != null ? board.getContent() : "";

        try {
            // ✅ 유튜브 업로드 호출
            String youtubeUrl = youtubeUploadService.uploadToYoutube(accessToken, imageUrl, audioUrl, title, description);
            System.out.println("✅ 유튜브 업로드 성공: " + youtubeUrl);
            return ResponseEntity.ok(new SuccessResponse(youtubeUrl));
        } catch (RuntimeException e) {
            System.out.println("❌ 유튜브 업로드 실패 (권한 문제): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("유튜브 업로드 실패: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("❌ 유튜브 업로드 실패 (기타 예외): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("업로드 중 알 수 없는 오류가 발생했습니다."));
        }
    }
}


