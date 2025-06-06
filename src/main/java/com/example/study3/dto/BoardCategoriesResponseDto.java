package com.example.study3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 다섯 가지 카테고리별 게시물 목록을 묶어 한 번에 반환하기 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCategoriesResponseDto {
    private List<BoardResponseDto> popularBoards;     // 조회순
    private List<BoardResponseDto> recentBoards;      // 최신순
    private List<BoardResponseDto> sameAgeBoards;     // 같은 나이대 (10대/20대/…)
    private List<BoardResponseDto> sameCountryBoards; // 같은 국가
    private List<BoardResponseDto> sameGenderBoards;  // 같은 성별
}
