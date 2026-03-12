package com.sun.lifetrack.repository;

import com.sun.lifetrack.entity.SleepRecord;
import com.sun.lifetrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SleepRecordRepository extends JpaRepository<SleepRecord, Long> {

    // 查询某个用户的所有记录，按日期倒序
    List<SleepRecord> findByUserOrderByDateDescStartTimeDesc(User user);

    // 查询某个用户某天的记录
    Optional<SleepRecord> findByUserAndDate(User user, LocalDate date);
    List<SleepRecord> findAllByUserAndDate(User user, LocalDate date);

    // 查询某个用户最近7天的记录（用于图表）
    List<SleepRecord> findByUserAndDateBetweenOrderByDateAsc(User user, LocalDate startDate, LocalDate endDate);

}