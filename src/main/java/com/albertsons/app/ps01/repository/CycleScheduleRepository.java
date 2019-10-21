package com.albertsons.app.ps01.repository;

import java.util.List;

import com.albertsons.app.ps01.domain.CycleSchedule;
import com.albertsons.app.ps01.domain.CycleScheduleId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleScheduleRepository extends JpaRepository<CycleSchedule, CycleScheduleId> {

    public List<CycleSchedule> findByDivIdOrderByDayNumAsc(String divId);

    @Query("SELECT DISTINCT divId FROM CycleSchedule")
    public List<String> findAllDistinctDivId();

    @Query("SELECT DISTINCT divId FROM CycleSchedule WHERE divId = :divId")
    public String findDistinctDivIdByDivId(String divId);
}