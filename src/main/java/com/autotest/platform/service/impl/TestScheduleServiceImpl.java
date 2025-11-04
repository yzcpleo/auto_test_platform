package com.autotest.platform.service.impl;

import com.autotest.platform.domain.testcase.TestSchedule;
import com.autotest.platform.mapper.TestScheduleMapper;
import com.autotest.platform.service.ITestScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 测试调度Service业务层处理
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Service
public class TestScheduleServiceImpl extends ServiceImpl<TestScheduleMapper, TestSchedule> implements ITestScheduleService {

    @Autowired
    private TestScheduleMapper testScheduleMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 查询测试调度
     *
     * @param scheduleId 测试调度主键
     * @return 测试调度
     */
    @Override
    public TestSchedule selectTestScheduleByScheduleId(Long scheduleId) {
        return testScheduleMapper.selectTestScheduleByScheduleId(scheduleId);
    }

    /**
     * 查询测试调度列表
     *
     * @param testSchedule 测试调度
     * @return 测试调度
     */
    @Override
    public List<TestSchedule> selectTestScheduleList(TestSchedule testSchedule) {
        return testScheduleMapper.selectTestScheduleList(testSchedule);
    }

    /**
     * 新增测试调度
     *
     * @param testSchedule 测试调度
     * @return 结果
     */
    @Override
    @Transactional
    public int insertTestSchedule(TestSchedule testSchedule) {
        // 设置默认值
        testSchedule.setExecuteCount(0);
        testSchedule.setSuccessCount(0);
        testSchedule.setFailureCount(0);
        testSchedule.setStatus("ENABLED");
        testSchedule.setCreateTime(new Date());

        // 计算下次执行时间
        String nextExecuteTime = calculateNextExecuteTime(testSchedule);
        if (nextExecuteTime != null) {
            testSchedule.setNextExecuteTime(nextExecuteTime);
        }

        return testScheduleMapper.insertTestSchedule(testSchedule);
    }

    /**
     * 修改测试调度
     *
     * @param testSchedule 测试调度
     * @return 结果
     */
    @Override
    @Transactional
    public int updateTestSchedule(TestSchedule testSchedule) {
        testSchedule.setUpdateTime(new Date());

        // 重新计算下次执行时间
        String nextExecuteTime = calculateNextExecuteTime(testSchedule);
        if (nextExecuteTime != null) {
            testSchedule.setNextExecuteTime(nextExecuteTime);
        }

        return testScheduleMapper.updateTestSchedule(testSchedule);
    }

    /**
     * 批量删除测试调度
     *
     * @param scheduleIds 需要删除的测试调度主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteTestScheduleByScheduleIds(Long[] scheduleIds) {
        return testScheduleMapper.deleteTestScheduleByScheduleIds(scheduleIds);
    }

    /**
     * 删除测试调度信息
     *
     * @param scheduleId 测试调度主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteTestScheduleByScheduleId(Long scheduleId) {
        return testScheduleMapper.deleteTestScheduleByScheduleId(scheduleId);
    }

    /**
     * 检查调度名称是否唯一
     */
    @Override
    public boolean checkScheduleNameUnique(TestSchedule testSchedule) {
        Long scheduleId = testSchedule.getScheduleId() == null ? -1L : testSchedule.getScheduleId();
        TestSchedule info = testScheduleMapper.selectTestScheduleByScheduleId(scheduleId);
        if (info != null && info.getScheduleId().longValue() != scheduleId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 启用调度
     */
    @Override
    @Transactional
    public int enableSchedule(Long[] scheduleIds) {
        return testScheduleMapper.batchUpdateStatus(Arrays.asList(scheduleIds), "ENABLED");
    }

    /**
     * 禁用调度
     */
    @Override
    @Transactional
    public int disableSchedule(Long[] scheduleIds) {
        return testScheduleMapper.batchUpdateStatus(Arrays.asList(scheduleIds), "DISABLED");
    }

    /**
     * 手动触发调度
     */
    @Override
    @Transactional
    public Map<String, Object> triggerSchedule(Long scheduleId, String triggerBy) {
        Map<String, Object> result = new HashMap<>();

        try {
            TestSchedule schedule = testScheduleMapper.selectTestScheduleByScheduleId(scheduleId);
            if (schedule == null) {
                result.put("success", false);
                result.put("message", "Schedule not found");
                return result;
            }

            if (!"ENABLED".equals(schedule.getStatus())) {
                result.put("success", false);
                result.put("message", "Schedule is not enabled");
                return result;
            }

            // TODO: 这里应该调用测试执行服务来执行测试
            // 暂时只更新调度统计信息

            // 更新执行统计
            updateScheduleStats(scheduleId, true);

            result.put("success", true);
            result.put("message", "Schedule triggered successfully");
            result.put("scheduleId", scheduleId);
            result.put("executionId", "EXEC-" + System.currentTimeMillis());

            log.info("Schedule triggered manually: {} by {}", schedule.getScheduleName(), triggerBy);

        } catch (Exception e) {
            log.error("Failed to trigger schedule: " + scheduleId, e);
            result.put("success", false);
            result.put("message", "Failed to trigger schedule: " + e.getMessage());
        }

        return result;
    }

    /**
     * 查询即将执行的调度
     */
    @Override
    public List<TestSchedule> selectPendingSchedules() {
        String currentTime = DATE_FORMAT.format(new Date());
        return testScheduleMapper.selectPendingSchedules(currentTime);
    }

    /**
     * 更新调度执行统计
     */
    @Override
    @Transactional
    public int updateScheduleStats(Long scheduleId, boolean success) {
        try {
            TestSchedule schedule = testScheduleMapper.selectTestScheduleByScheduleId(scheduleId);
            if (schedule == null) {
                return 0;
            }

            int executeCount = schedule.getExecuteCount() + 1;
            int successCount = success ? schedule.getSuccessCount() + 1 : schedule.getSuccessCount();
            int failureCount = success ? schedule.getFailureCount() : schedule.getFailureCount() + 1;

            String lastExecuteTime = DATE_FORMAT.format(new Date());
            String nextExecuteTime = calculateNextExecuteTime(schedule);

            return testScheduleMapper.updateExecutionStats(
                scheduleId, executeCount, successCount, failureCount,
                lastExecuteTime, nextExecuteTime
            );

        } catch (Exception e) {
            log.error("Failed to update schedule stats: " + scheduleId, e);
            return 0;
        }
    }

    /**
     * 计算下次执行时间
     */
    @Override
    public String calculateNextExecuteTime(TestSchedule schedule) {
        try {
            if ("CRON".equals(schedule.getScheduleType()) && schedule.getCronExpression() != null) {
                return calculateNextCronTime(schedule.getCronExpression());
            } else if ("FIXED_RATE".equals(schedule.getScheduleType()) && schedule.getFixedRate() != null) {
                return calculateNextFixedRateTime(schedule.getLastExecuteTime(), schedule.getFixedRate());
            } else if ("FIXED_DELAY".equals(schedule.getScheduleType()) && schedule.getFixedDelay() != null) {
                return calculateNextFixedDelayTime(schedule.getLastExecuteTime(), schedule.getFixedDelay());
            }
        } catch (Exception e) {
            log.error("Failed to calculate next execute time for schedule: " + schedule.getScheduleId(), e);
        }
        return null;
    }

    /**
     * 验证调度配置
     */
    @Override
    public Map<String, Object> validateScheduleConfig(TestSchedule testSchedule) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        try {
            // 验证调度名称
            if (testSchedule.getScheduleName() == null || testSchedule.getScheduleName().trim().isEmpty()) {
                errors.add("Schedule name is required");
            }

            // 验证调度类型
            if (testSchedule.getScheduleType() == null || testSchedule.getScheduleType().trim().isEmpty()) {
                errors.add("Schedule type is required");
            } else {
                String scheduleType = testSchedule.getScheduleType();
                if (!Arrays.asList("CRON", "FIXED_RATE", "FIXED_DELAY").contains(scheduleType)) {
                    errors.add("Invalid schedule type: " + scheduleType);
                }

                // 验证对应类型的配置
                if ("CRON".equals(scheduleType)) {
                    if (testSchedule.getCronExpression() == null || testSchedule.getCronExpression().trim().isEmpty()) {
                        errors.add("Cron expression is required for CRON type");
                    } else if (!isValidCronExpression(testSchedule.getCronExpression())) {
                        errors.add("Invalid cron expression: " + testSchedule.getCronExpression());
                    }
                } else if ("FIXED_RATE".equals(scheduleType)) {
                    if (testSchedule.getFixedRate() == null || testSchedule.getFixedRate() <= 0) {
                        errors.add("Fixed rate must be greater than 0");
                    }
                } else if ("FIXED_DELAY".equals(scheduleType)) {
                    if (testSchedule.getFixedDelay() == null || testSchedule.getFixedDelay() <= 0) {
                        errors.add("Fixed delay must be greater than 0");
                    }
                }
            }

            // 验证用例或分类
            if ((testSchedule.getCaseIds() == null || testSchedule.getCaseIds().trim().isEmpty()) &&
                (testSchedule.getCategoryIds() == null || testSchedule.getCategoryIds().trim().isEmpty())) {
                errors.add("Either case IDs or category IDs must be specified");
            }

            // 验证时间范围
            if (testSchedule.getStartTime() != null && testSchedule.getEndTime() != null) {
                if (testSchedule.getStartTime().after(testSchedule.getEndTime())) {
                    errors.add("Start time must be before end time");
                }
            }

            result.put("valid", errors.isEmpty());
            result.put("errors", errors);
            result.put("message", errors.isEmpty() ? "Schedule configuration is valid" : "Schedule configuration has errors");

        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "Validation error: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取调度执行历史
     */
    @Override
    public List<Map<String, Object>> getScheduleHistory(Long scheduleId, Integer days) {
        // TODO: 实现调度执行历史查询
        // 这里需要从执行记录表中查询相关数据
        return new ArrayList<>();
    }

    /**
     * 暂停调度
     */
    @Override
    @Transactional
    public int pauseSchedule(Long scheduleId) {
        return testScheduleMapper.updateStatus(scheduleId, "PAUSED");
    }

    /**
     * 恢复调度
     */
    @Override
    @Transactional
    public int resumeSchedule(Long scheduleId) {
        TestSchedule schedule = testScheduleMapper.selectTestScheduleByScheduleId(scheduleId);
        if (schedule != null) {
            // 重新计算下次执行时间
            String nextExecuteTime = calculateNextExecuteTime(schedule);
            if (nextExecuteTime != null) {
                schedule.setNextExecuteTime(nextExecuteTime);
                testScheduleMapper.updateTestSchedule(schedule);
            }
        }
        return testScheduleMapper.updateStatus(scheduleId, "ENABLED");
    }

    /**
     * 计算下次CRON执行时间
     */
    private String calculateNextCronTime(String cronExpression) {
        try {
            CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
            CronParser parser = new CronParser(cronDefinition);
            Cron cron = parser.parse(cronExpression);

            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime nextTime = cron.nextExecution(now);

            return DATE_FORMAT.format(Date.from(nextTime.toInstant()));
        } catch (Exception e) {
            log.error("Failed to calculate next cron time: " + cronExpression, e);
            return null;
        }
    }

    /**
     * 计算下次固定间隔执行时间
     */
    private String calculateNextFixedRateTime(Date lastExecuteTime, Long fixedRate) {
        if (lastExecuteTime == null) {
            return DATE_FORMAT.format(new Date());
        }

        long nextTime = lastExecuteTime.getTime() + fixedRate;
        return DATE_FORMAT.format(new Date(nextTime));
    }

    /**
     * 计算下次固定延迟执行时间
     */
    private String calculateNextFixedDelayTime(Date lastExecuteTime, Long fixedDelay) {
        if (lastExecuteTime == null) {
            return DATE_FORMAT.format(new Date());
        }

        long nextTime = lastExecuteTime.getTime() + fixedDelay;
        return DATE_FORMAT.format(new Date(nextTime));
    }

    /**
     * 验证CRON表达式
     */
    private boolean isValidCronExpression(String cronExpression) {
        try {
            CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
            CronParser parser = new CronParser(cronDefinition);
            parser.parse(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}