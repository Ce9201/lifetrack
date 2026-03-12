package com.sun.lifetrack.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.lifetrack.dto.request.SleepRecordRequest;
import com.sun.lifetrack.dto.response.SleepRecordResponse;
import com.sun.lifetrack.entity.SleepRecord;
import com.sun.lifetrack.entity.User;
import com.sun.lifetrack.exception.BusinessException;
import com.sun.lifetrack.exception.ErrorCode;
import com.sun.lifetrack.repository.SleepRecordRepository;
import com.sun.lifetrack.repository.UserRepository;
import com.sun.lifetrack.service.SleepRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SleepRecordServiceImpl implements SleepRecordService {

    private final SleepRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final DefaultRedisScript<Long> releaseLockScript;

    private static final String CACHE_KEY_FMT = "sleep:recent:%d";
    private static final String LOCK_KEY_FMT = "lock:sleep:recent:%d";

    @Override
    @Transactional
    public SleepRecordResponse createRecord(SleepRecordRequest request) {
        User currentUser = getCurrentUser();

        // 校验时间合理性
        validateTimeRange(request.getStartTime(), request.getEndTime(), request.getDate());
        checkTimeOverlap(currentUser, request.getDate(), request.getStartTime(), request.getEndTime(), null);

        SleepRecord record = new SleepRecord();
        record.setUser(currentUser);
        record.setDate(request.getDate());
        record.setStartTime(request.getStartTime());
        record.setEndTime(request.getEndTime());
        record.setNotes(request.getNotes());

        SleepRecord saved = recordRepository.save(record);
        log.info("用户 {} 创建睡眠记录 ID: {}", currentUser.getUsername(), saved.getId());
        // 保存完成之后
        String cacheKey = String.format(CACHE_KEY_FMT, currentUser.getId());
        try {
            stringRedisTemplate.delete(cacheKey);
            log.info("createRecord: deleted cache {}", cacheKey);
        } catch (Exception e) {
            log.warn("createRecord: delete cache failed {}, err={}", cacheKey, e.getMessage());
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public SleepRecordResponse updateRecord(Long id, SleepRecordRequest request) {
        User currentUser = getCurrentUser();
        SleepRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORD_NOT_FOUND));

        // 权限校验
        if (!record.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        // 校验时间合理性
        validateTimeRange(request.getStartTime(), request.getEndTime(), request.getDate());
        checkTimeOverlap(currentUser, request.getDate(), request.getStartTime(), request.getEndTime(), id);

        record.setDate(request.getDate());
        record.setStartTime(request.getStartTime());
        record.setEndTime(request.getEndTime());
        record.setNotes(request.getNotes());

        SleepRecord updated = recordRepository.save(record);
        log.info("用户 {} 更新睡眠记录 ID: {}", currentUser.getUsername(), id);
        // 保存完成之后
        String cacheKey = String.format(CACHE_KEY_FMT, currentUser.getId());
        try {
            stringRedisTemplate.delete(cacheKey);
            log.info("updateRecord: deleted cache {}", cacheKey);
        } catch (Exception e) {
            log.warn("updateRecord: delete cache failed {}, err={}", cacheKey, e.getMessage());
        }

        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRecord(Long id) {
        User currentUser = getCurrentUser();
        SleepRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORD_NOT_FOUND));

        if (!record.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        recordRepository.delete(record);
        log.info("用户 {} 删除睡眠记录 ID: {}", currentUser.getUsername(), id);
        // 保存完成之后
        String cacheKey = String.format(CACHE_KEY_FMT, currentUser.getId());
        try {
            stringRedisTemplate.delete(cacheKey);
            log.info("deleteRecord: deleted cache {}", cacheKey);
        } catch (Exception e) {
            log.warn("deleteRecord: delete cache failed {}, err={}", cacheKey, e.getMessage());
        }

    }

    @Override
    public SleepRecordResponse getRecord(Long id) {
        User currentUser = getCurrentUser();
        SleepRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORD_NOT_FOUND));

        if (!record.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        return toResponse(record);
    }

    @Override
    public List<SleepRecordResponse> getAllMyRecords() {
        User currentUser = getCurrentUser();
        return recordRepository.findByUserOrderByDateDescStartTimeDesc(currentUser)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SleepRecordResponse> getWeeklyRecords() {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();
        String cacheKey = String.format(CACHE_KEY_FMT, userId);
        String lockKey = String.format(LOCK_KEY_FMT, userId);

        // 1. 尝试从缓存读取
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("weeklyRecords: cache hit for user {}", userId);
                // 反序列化
                var listType = objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, SleepRecordResponse.class);
                return objectMapper.readValue(cached, listType);
            } else {
                log.info("weeklyRecords: cache miss for user {}", userId);
            }
        } catch (Exception e) {
            log.warn("weeklyRecords: read cache failed, will fallback to DB. user={}, err={}", userId, e.getMessage());
        }

        // 2. 尝试抢锁，只有抢到锁的线程去查库并写入缓存
        String lockVal = java.util.UUID.randomUUID().toString();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockVal, java.time.Duration.ofSeconds(10)); // 锁超时 10s

        if (Boolean.TRUE.equals(acquired)) {
            try {
                log.info("weeklyRecords: acquired lock, loading from DB for user {}", userId);
                // 从 DB 查询（最近 7 天）
                LocalDate today = LocalDate.now();
                LocalDate sevenDaysAgo = today.minusDays(6);
                List<SleepRecordResponse> records = recordRepository
                        .findByUserAndDateBetweenOrderByDateAsc(currentUser, sevenDaysAgo, today)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());

                // 写入缓存（1 小时）
                try {
                    String json = objectMapper.writeValueAsString(records);
                    stringRedisTemplate.opsForValue().set(cacheKey, json, java.time.Duration.ofHours(1));
                    log.info("weeklyRecords: cache written for user {}", userId);
                } catch (Exception e) {
                    log.warn("weeklyRecords: write cache failed user={}, err={}", userId, e.getMessage());
                }

                return records;
            } finally {
                // 释放锁（只有持有者能释放）
                try {
                    stringRedisTemplate.execute(releaseLockScript, java.util.Collections.singletonList(lockKey), lockVal);
                } catch (Exception e) {
                    log.warn("weeklyRecords: release lock failed for user {}, err={}", userId, e.getMessage());
                }
            }
        } else {
            // 3. 未抢到锁：短轮询等待缓存重建（防止所有线程同时去 DB）
            log.info("weeklyRecords: waiting for cache to be built by owner, user={}", userId);
            int attempts = 0;
            int maxAttempts = 50; // 最多等待 50 次（100ms * 50 = 5s）
            while (attempts++ < maxAttempts) {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                String cached = stringRedisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    try {
                        var listType = objectMapper.getTypeFactory()
                                .constructCollectionType(List.class, SleepRecordResponse.class);
                        log.info("weeklyRecords: got cache after wait for user {}", userId);
                        return objectMapper.readValue(cached, listType);
                    } catch (Exception e) {
                        log.warn("weeklyRecords: parse cache failed user={}, err={}", userId, e.getMessage());
                        break;
                    }
                }
            }

            // 4. 等待超时，回退到直接查库（保证可用性）
            log.warn("weeklyRecords: wait timeout, fallback to DB for user {}", userId);
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(6);
            return recordRepository.findByUserAndDateBetweenOrderByDateAsc(currentUser, sevenDaysAgo, today)
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
    }


    //睡眠时间合理性验证
    private void validateTimeRange(LocalDateTime start, LocalDateTime end,LocalDate date) {
        if (start == null || end == null) {
            throw new BusinessException(ErrorCode.TIME_CANNOT_BE_NULL);
        }
        if (!start.toLocalDate().equals(date)) {
            throw new BusinessException(ErrorCode.DATE_MISMATCH);
        }
        // 计算时长（分钟）
        long minutes = ChronoUnit.MINUTES.between(start, end);
        if (minutes <= 0) {
            throw new BusinessException(ErrorCode.INVALID_DURATION);
        }
        // 最大睡眠时长限制（例如 18 小时 = 1080 分钟）
        if (minutes > 1080) {
            throw new BusinessException(ErrorCode.DURATION_TOO_LONG);
        }
    }
    private void checkTimeOverlap(User user, LocalDate date, LocalDateTime start, LocalDateTime end, Long excludeId) {
        List<SleepRecord> existingRecords = recordRepository.findAllByUserAndDate(user, date);
        for (SleepRecord record : existingRecords) {
            if (excludeId != null && record.getId().equals(excludeId)) {
                continue; // 更新时排除自身
            }
            // 区间重叠判断：[start, end) 与 [record.startTime, record.endTime) 有交集
            if (start.isBefore(record.getEndTime()) && end.isAfter(record.getStartTime())) {
                throw new BusinessException(ErrorCode.RECORD_TIME_OVERLAP, "与已有睡眠记录时间重叠");
            }
        }
    }


    //从 SecurityContext 获取当前用户名，然后查询 User 实体
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USERNAME_NOT_FOUND));
    }
    //实体转换为 Response
    private SleepRecordResponse toResponse(SleepRecord record) {
        return SleepRecordResponse.builder()
                .id(record.getId())
                .date(record.getDate())
                .startTime(record.getStartTime())
                .endTime(record.getEndTime())
                .durationMinutes(record.getDurationMinutes())
                .notes(record.getNotes())
                .build();
    }
}