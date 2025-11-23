package com.software.dev.repository;

import com.software.dev.domain.UrlRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrlRequestRepository extends JpaRepository<UrlRequest, String> {
    @Query(value = """
            SELECT t.*, tri.TRIGGER_STATE as triggerState, tri.NEXT_FIRE_TIME as nextFireTime 
            FROM url_request t 
            LEFT JOIN qrtz_triggers tri ON t.request_id::text = tri.JOB_NAME 
            WHERE 1=1 
            AND (:search IS NULL OR t.request_name LIKE CONCAT('%',:search,'%') OR t.request_group LIKE CONCAT('%',:search,'%'))
            ORDER BY t.request_id DESC 
            LIMIT :pageSize OFFSET :pageStart
            """, nativeQuery = true)
    List<UrlRequest> listUrl(@Param("pageStart") Integer pageStart, 
                            @Param("pageSize") Integer pageSize, 
                            @Param("search") String search);
    
    long count();
    
    List<UrlRequest> findAllByOrderByRequestIdAsc();
}