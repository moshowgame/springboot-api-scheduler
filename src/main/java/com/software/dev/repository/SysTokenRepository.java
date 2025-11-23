package com.software.dev.repository;

import com.software.dev.domain.SysToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SysTokenRepository extends JpaRepository<SysToken, Integer> {
    
    @Query("SELECT COUNT(s) FROM SysToken s WHERE s.tokenValue = :tokenValue")
    long countByTokenValue(@Param("tokenValue") String tokenValue);
}