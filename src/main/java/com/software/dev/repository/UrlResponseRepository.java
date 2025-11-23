package com.software.dev.repository;

import com.software.dev.domain.UrlResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlResponseRepository extends JpaRepository<UrlResponse, String> {
    
    Page<UrlResponse> findByRequestIdAndResponseTextContainingOrderByResponseTimeDesc(String requestId, String searchText, Pageable pageable);
    
    Page<UrlResponse> findByResponseTextContainingOrderByResponseTimeDesc(String searchText, Pageable pageable);
    
    Page<UrlResponse> findByRequestIdOrderByResponseTimeDesc(String requestId, Pageable pageable);
    
    Page<UrlResponse> findAllByOrderByResponseTimeDesc(Pageable pageable);
}