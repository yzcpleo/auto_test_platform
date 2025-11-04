package com.autotest.platform.service;

import com.autotest.platform.domain.testcase.TestSchedule;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 测试调度Service接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface ITestScheduleService extends IService<TestSchedule> {

    /**
     * 查询测试调度
     *
     * @param scheduleId 测试调度主键
     * @return 测试调度
     */
    TestSchedule selectTestScheduleByScheduleId(Long scheduleId);

    /**
     * 查询测试调度列表
     *
     * @param testSchedule 测试调度
     * @return 测试调度集合
     */
    List<TestSchedule> selectTestScheduleList(TestSchedule testSchedule);

    /**
     * 新增测试调度
     *
     * @param testSchedule 测试调度
     * @return 结果
     */
    int insertTestSchedule(TestSchedule testSchedule);

    /**
     * 修改测试调度
     *
     * @param testSchedule 测试调度
     * @return 结果
     */
    int updateTestSchedule(TestSchedule testSchedule);

    /**
     * 批量删除测试调度
     *
     * @param scheduleIds 需要删除的测试调度主键集合
     * @return 结果
     */
    int deleteTestScheduleByScheduleIds(Long[] scheduleIds);

    /**
     * 删除测试调度信息
     *
     * @param scheduleId 测试调度主键
     * @return 结果
     */
    int deleteTestScheduleByScheduleId(Long scheduleId);

    /**
     * 检查调度名称是否唯一
     *
     * @param testSchedule 测试调度信息
     * @return 结果
     */
    boolean checkScheduleNameUnique(TestSchedule testSchedule);

    /**
     * 启用调度
     *
     * @param scheduleIds 调度ID列表
     * @return 结果
     */
    int enableSchedule(Long[] scheduleIds);

    /**
     * 禁用调度
     *
     * @param scheduleIds 调度ID列表
     * @return 结果
     */
    int disableSchedule(Long[] scheduleIds);

    /**
     * 手动触发调度
     *
     * @param scheduleId 调度ID
     * @param triggerBy 触发人
     * @return 执行结果
     */
    Map<String, Object> triggerSchedule(Long scheduleId, String triggerBy);

    /**
     * 查询即将执行的调度
     *
     * @return 调度列表
     */
    List<TestSchedule> selectPendingSchedules();

    /**
     * 更新调度执行统计
     *
     * @param scheduleId 调度ID
     * @param success 是否成功
     * @return 结果
     */
    int updateScheduleStats(Long scheduleId, boolean success);

    /**
     * 计算下次执行时间
     *
     * @param schedule 调度信息
     * @return 下次执行时间
     */
    String calculateNextExecuteTime(TestSchedule schedule);

    /**
     * 验证调度配置
     *
     * @param testSchedule 调度信息
     * @return 验证结果
     */
    Map<String, Object> validateScheduleConfig(TestSchedule testSchedule);

    /**
     * 获取调度执行历史
     *
     * @param scheduleId 调度ID
     * @param days 天数
     * @return 执行历史
     */
    List<Map<String, Object>> getScheduleHistory(Long scheduleId, Integer days);

    /**
     * 暂停调度
     *
     * @param scheduleId 调度ID
     * @return 结果
     */
    int pauseSchedule(Long scheduleId);

    /**
     * 恢复调度
     *
     * @param scheduleId 调度ID
     * @return 结果
     */
    int resumeSchedule(Long scheduleId);
}