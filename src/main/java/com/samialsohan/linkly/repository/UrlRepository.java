package com.samialsohan.linkly.repository;

import com.samialsohan.linkly.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    @Query(value = "SELECT nextval('url_code_seq')", nativeQuery = true)
    long nextCodeSequenceValue();
}
