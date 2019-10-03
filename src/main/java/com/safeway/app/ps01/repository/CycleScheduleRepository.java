package com.safeway.app.ps01.repository;

import java.util.List;

import com.safeway.app.ps01.domain.CycleSchedule;
import com.safeway.app.ps01.domain.CycleScheduleId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleScheduleRepository extends JpaRepository<CycleSchedule, CycleScheduleId> {

    public List<CycleSchedule> findByDivIdOrderByDayNumAsc(String divId);

}