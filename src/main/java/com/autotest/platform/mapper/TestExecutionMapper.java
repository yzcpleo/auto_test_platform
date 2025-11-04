package com.autotest.platform.mapper;

import com.autotest.platform.domain.testcase.TestExecution;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 测试执行Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface TestExecutionMapper {

    /**
     * 查询测试执行
     *
     * @param executionId 测试执行主键
     * @return 测试执行
     */
    public TestExecution selectTestExecutionByExecutionId(Long executionId);

    /**
     * 查询测试执行列表
     *
     * @param testExecution 测试执行
     * @return 测试执行集合
     */
    public List<TestExecution> selectTestExecutionList(TestExecution testExecution);

    /**
     * 新增测试执行
     *
     * @param testExecution 测试执行
     * @return 结果
     */
    public int insertTestExecution(TestExecution testExecution);

    /**
     * 修改测试执行
     *
     * @param testExecution 测试执行
     * @return 结果
     */
    public int updateTestExecution(TestExecution testExecution);

    /**
     * 删除测试执行
     *
     * @param executionId 测试执行主键
     * @return 结果
     */
    public int deleteTestExecutionByExecutionId(Long executionId);

    /**
     * 批量删除测试执行
     *
     * @param executionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTestExecutionByExecutionIds(Long[] executionIds);

    /**
     * 根据项目ID查询执行列表
     *
     * @param projectId 项目ID
     * @return 执行列表
     */
    public List<TestExecution> selectByProjectId(Long projectId);

    /**
     * 根据状态查询执行列表
     *
     * @param status 状态
     * @param limit 限制数量
     * @return 执行列表
     */
    public List<TestExecution> selectByStatus(@Param("status") String status, @Param("limit") Integer limit);

    /**
     * 更新执行状态
     *
     * @param executionId 执行ID
     * @param status 新状态
     * @return 结果
     */
    public int updateStatus(@Param("executionId") Long executionId, @Param("status") String status);

    /**
     * 更新执行进度
     *
     * @param executionId 执行ID
     * @param progress 进度
     * @param successCases 成功用例数
     * @param failedCases 失败用例数
     * @param skippedCases 跳过用例数
     * @return 结果
     */
    public int updateProgress(@Param("executionId") Long executionId,
                            @Param("progress") Integer progress,
                            @Param("successCases") Integer successCases,
                            @Param("failedCases") Integer failedCases,
                            @Param("skippedCases") Integer skippedCases);

    /**
     * 统计执行数据
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 统计数据
     */
    public Map<String, Object> statisticsByProject(@Param("projectId") Long projectId,
                                                  @Param("timeRange") String timeRange);

    /**
     * 查询执行历史统计
     *
     * @param projectId 项目ID
     * @param days 天数
     * @return 历史统计
     */
    public List<Map<String, Object>> selectExecutionHistory(@Param("projectId") Long projectId,
                                                           @Param("days") Integer days);
}