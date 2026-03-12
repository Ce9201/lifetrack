package com.sun.lifetrack.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Entity
@Table(name = "sleep_record")
public class SleepRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 多对一关联，每条记录属于一个用户

    @Column(nullable = false)
    private LocalDate date;  // 睡眠日期（如 2024-03-01）

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;  // 入睡时间

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;    // 醒来时间

    @Column(name = "duration_minutes")
    private Integer durationMinutes;  // 睡眠时长（分钟），自动计算

    private String notes;  // 备注，可选

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateDuration();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateDuration();
    }

    private void calculateDuration() {
        if (startTime != null && endTime != null) {
            durationMinutes = Math.toIntExact(ChronoUnit.MINUTES.between(startTime, endTime));
        }
    }
}