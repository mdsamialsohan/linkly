package com.samialsohan.linkly.repository;

import com.samialsohan.linkly.entity.Click;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClickRepository extends JpaRepository<Click, Long> {

    long countByShortCode(String shortCode);
}