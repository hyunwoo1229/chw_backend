package com.example.study3.repository;

import com.example.study3.domain.BoardEntity;
import com.example.study3.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    List<BoardEntity> findByMember(Member member);



}
