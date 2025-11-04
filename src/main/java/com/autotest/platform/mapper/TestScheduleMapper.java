package com.autotest.platform.mapper;

import com.autotest.platform.domain.testcase.TestSchedule;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 测试调度Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface TestScheduleMapper {

    /**
     * 查询测试调度
     *
     * @param scheduleId 测试调度主键
     * @return 测试调度
     */
    public TestSchedule selectTestScheduleByScheduleId(Long scheduleId);

    /**
     * 查询测试调度列表
     *
     * @param testSchedule 测试调度
     * @return 测试调度集合
     */
    public List<TestSchedule> selectTestScheduleList(TestSchedule testSchedule);

    /**
     * 新增测试调度
     *
     * @param testSchedule 测试调度
     * @return 结果
     */
    public int insertTestSchedule(TestSchedule testSchedule);

    /**
     * 修改测试调度
     *
     * @param testSchedule 测试调度
     * @return 结果
     */
    public int updateTestSchedule(TestSchedule testSchedule);

    /**
     * 删除测试调度
     *
     * @param scheduleId 测试调度主键
     * @return 结果
     */
    public int deleteTestScheduleByScheduleId(Long scheduleId);

    /**
     * 批量删除测试调度
     *
     * @param scheduleIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTestScheduleByScheduleIds(Long[] scheduleIds);

    /**
     * 根据项目ID查询调度列表
     *
     * @param projectId 项目ID
     * @return 调度列表
     */
    public List<TestSchedule> selectByProjectId(Long projectId);

    /**
     * 查询启用的调度列表
     *
     * @return 启用的调度列表
     */
    public List<TestSchedule> selectEnabledSchedules();

    /**
     * 查询需要执行的调度
     *
     * @param currentTime 当前时间
     * @return 调度列表
     */
    public List<TestSchedule> selectPendingSchedules(@Param("currentTime") String currentTime);

    /**
     * 更新调度状态
     *
     * @param scheduleId 调度ID
     * @param status 状态
     * @return 结果
     */
    public int updateStatus(@Param("scheduleId") Long scheduleId, @Param("status") String status);

    /**
     * 更新执行统计
     *
     * @param scheduleId 调度ID
     * @param executeCount 执行次数
     * @param successCount 成功次数
     * @param failureCount 失败次数
     * @param lastExecuteTime 上次执行时间
     * @param nextExecuteTime 下次执行时间
     * @return 结果
     */
    public int updateExecutionStats(@Param("scheduleId") Long scheduleId,
                                  @Param("executeCount") Integer executeCount,
                                  @Param("successCount") Integer successCount,
                                  @Param("failureCount") Integer failureCount,
                                  @Param("lastExecuteTime") String lastExecuteTime,
                                  @Param("nextExecuteTime") String nextExecuteTime);

    /**
     * 检查调度名称是否唯一
     *
     * @param projectId 项目ID
     * @param scheduleName 调度名称
     * @param scheduleId 调度ID(排除自己)
     * @return 数量
     */
    public int checkScheduleNameUnique(@Param("projectId") Long projectId,
                                     @Param("scheduleName") String scheduleName,
                                     @Param("scheduleId") Long scheduleId);

    /**
     * 批量启用/禁用调度
     *
     * @param scheduleIds 调度ID列表
     * @param status 状态
     * @return 结果
     */
    public int batchUpdateStatus(@Param("scheduleIds") List<Long> scheduleIds,
                               @Param("status") String status);
}