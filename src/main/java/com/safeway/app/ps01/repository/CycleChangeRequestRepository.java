package com.safeway.app.ps01.repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import com.safeway.app.ps01.domain.CycleChangeRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleChangeRequestRepository extends JpaRepository<CycleChangeRequest, Long> {
    
    public List<CycleChangeRequest> findByDivIdAndRunDateBetweenOrderByRunDateAsc(String divisionId, Date startRunDate, Date endRunDate);

    @Query("SELECT c FROM CycleChangeRequest c WHERE c.divId = :divisionId AND c.runDate = :runDate AND c.expiryTimestamp >= :expiryTs")
    public List<CycleChangeRequest> findByDivIdAndRunDateAndNotExpired(String divisionId, Date runDate, Timestamp expiryTs);
}