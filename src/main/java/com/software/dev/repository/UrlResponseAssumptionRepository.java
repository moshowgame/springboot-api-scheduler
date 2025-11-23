package com.software.dev.repository;

import com.software.dev.domain.UrlResponseAssumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlResponseAssumptionRepository extends JpaRepository<UrlResponseAssumption, String> {
}