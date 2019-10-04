package com.safeway.app.ps01.repository;

import java.sql.Date;
import java.util.List;

import com.safeway.app.ps01.domain.CycleChangeRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleChangeRequestRepository extends JpaRepository<CycleChangeRequest, Long> {
    
    public List<CycleChangeRequest> findByDivIdAndRunDateBetweenOrderByRunDateAsc(String divId, Date startRunDate, Date endRunDate);

}