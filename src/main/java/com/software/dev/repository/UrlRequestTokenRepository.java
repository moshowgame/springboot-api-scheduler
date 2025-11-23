package com.software.dev.repository;

import com.software.dev.domain.UrlRequestToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRequestTokenRepository extends JpaRepository<UrlRequestToken, String> {
    
    Page<UrlRequestToken> findByStatusOrderByRequestIdDesc(Integer status, Pageable pageable);
    
    Page<UrlRequestToken> findByStatusAndTokenUrlContainingOrderByRequestIdDesc(Integer status, String tokenUrl, Pageable pageable);
}