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

        public List<CycleChangeRequest> findByRunDateBetweenOrderByRunDateAscRunNumberAsc(Date startDate, Date endDate);

        public List<CycleChangeRequest> findByDivIdAndRunDateBetweenOrderByRunDateAsc(String divisionId,
                        Date startRunDate, Date endRunDate);

        public List<CycleChangeRequest> findByIdIn(List<Long> ids);

        // This query can be improved using dynamic JPQL
        @Query("SELECT c FROM CycleChangeRequest c WHERE c.divId = :divisionId AND c.expiryTimestamp >= :expiryTs AND c.runDate BETWEEN :startDate AND :endDate ORDER BY c.runDate, c.effectiveDate ASC") public List<CycleChangeRequest> findActiveByDivIdAndBetweenRunDatesAsc(
                        String divisionId, Date startDate, Date endDate, Timestamp expiryTs);

        @Query("SELECT c FROM CycleChangeRequest c WHERE c.divId = :divisionId AND c.expiryTimestamp >= :expiryTs AND c.runDate BETWEEN :startDate AND :endDate ORDER BY c.runDate DESC, c.effectiveDate DESC")
        public List<CycleChangeRequest> findActiveByDivIdAndBetweenRunDatesDesc(String divisionId, Date startDate,
                        Date endDate, Timestamp expiryTs);

        @Query("SELECT c FROM CycleChangeRequest c WHERE c.divId = :divisionId AND c.runDate = :runDate AND c.expiryTimestamp >= :expiryTs ORDER BY c.runDate ASC")
        public List<CycleChangeRequest> findByDivIdAndRunDateAndNotExpired(String divisionId, Date runDate,
                        Timestamp expiryTs);

}