package com.sun.lifetrack.controller;

import com.sun.lifetrack.dto.request.SleepRecordRequest;
import com.sun.lifetrack.dto.response.SleepRecordResponse;
import com.sun.lifetrack.service.SleepRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "睡眠记录管理", description = "睡眠记录的增删改查及周趋势")
@Slf4j
@RestController
@RequestMapping("/sleep-records")
@RequiredArgsConstructor
public class SleepRecordController {

    private final SleepRecordService recordService;

    @Operation(summary = "创建睡眠记录")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRecord(@Valid @RequestBody SleepRecordRequest request) {
        SleepRecordResponse response = recordService.createRecord(request);
        return buildResponse("创建成功", response, HttpStatus.CREATED);
    }

    @Operation(summary = "修改睡眠记录")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRecord(@PathVariable Long id,
                                                            @Valid @RequestBody SleepRecordRequest request) {
        SleepRecordResponse response = recordService.updateRecord(id, request);
        return buildResponse("更新成功", response, HttpStatus.OK);
    }

    @Operation(summary = "获取单条记录")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRecord(@PathVariable Long id) {
        SleepRecordResponse response = recordService.getRecord(id);
        return buildResponse("查询成功", response, HttpStatus.OK);
    }

    @Operation(summary = "删除记录")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return buildResponse("删除成功", null, HttpStatus.OK);
    }

    @Operation(summary = "获取当前用户所有记录")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMyRecords() {
        List<SleepRecordResponse> records = recordService.getAllMyRecords();
        return buildResponse("查询成功", records, HttpStatus.OK);
    }

    @Operation(summary = "获取最近7天睡眠记录", description = "用于图表展示，数据已缓存")
    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyRecords() {
        List<SleepRecordResponse> records = recordService.getWeeklyRecords();
        return buildResponse("查询成功", records, HttpStatus.OK);
    }


    // 统一响应格式
    private ResponseEntity<Map<String, Object>> buildResponse(String message, Object data, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", status.value());
        response.put("message", message);
        response.put("data", data);
        return new ResponseEntity<>(response, status);
    }
}