package com.example.study3.repository;

import com.example.study3.domain.BoardEntity;
import com.example.study3.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    List<BoardEntity> findByMember(Member member);

    // 조회순으로 정렬 (views 내림차순)
    List<BoardEntity> findAllByOrderByViewsDesc();

    // 최신순으로 정렬 (createdAt 내림차순으로 가정; 실제 컬럼명에 맞게 수정)
    List<BoardEntity> findAllByOrderByCreatedAtDesc();

    // 나이 범위 검색 (예: age between startAge and endAge)
    List<BoardEntity> findByMember_AgeBetween(Integer startAge, Integer endAge);

    // 같은 국가 필터링
    List<BoardEntity> findByMember_Country(String country);

    // 같은 성별 필터링
    List<BoardEntity> findByMember_Gender(String gender);

    // 검색: 제목에 keyword가 포함된 게시물
    List<BoardEntity> findByTitleContainingIgnoreCase(String keyword);

}
