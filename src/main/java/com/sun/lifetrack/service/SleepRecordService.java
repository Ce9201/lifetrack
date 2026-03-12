package com.sun.lifetrack.service;

import com.sun.lifetrack.dto.request.SleepRecordRequest;
import com.sun.lifetrack.dto.response.SleepRecordResponse;
import java.util.List;

public interface SleepRecordService {
    SleepRecordResponse createRecord(SleepRecordRequest request);
    SleepRecordResponse updateRecord(Long id, SleepRecordRequest request);
    void deleteRecord(Long id);
    SleepRecordResponse getRecord(Long id);

    List<SleepRecordResponse> getAllMyRecords();
    List<SleepRecordResponse> getWeeklyRecords(); // 最近7天
}