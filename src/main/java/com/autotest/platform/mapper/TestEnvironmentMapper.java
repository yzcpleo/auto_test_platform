package com.autotest.platform.mapper;

import com.autotest.platform.domain.project.TestEnvironment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试环境Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface TestEnvironmentMapper extends BaseMapper<TestEnvironment> {

    /**
     * 查询测试环境
     *
     * @param envId 测试环境主键
     * @return 测试环境
     */
    TestEnvironment selectTestEnvironmentByEnvId(Long envId);

    /**
     * 查询测试环境列表
     *
     * @param testEnvironment 测试环境
     * @return 测试环境集合
     */
    List<TestEnvironment> selectTestEnvironmentList(TestEnvironment testEnvironment);

    /**
     * 根据项目ID查询环境列表
     *
     * @param projectId 项目ID
     * @return 环境列表
     */
    List<TestEnvironment> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 新增测试环境
     *
     * @param testEnvironment 测试环境
     * @return 结果
     */
    int insertTestEnvironment(TestEnvironment testEnvironment);

    /**
     * 修改测试环境
     *
     * @param testEnvironment 测试环境
     * @return 结果
     */
    int updateTestEnvironment(TestEnvironment testEnvironment);

    /**
     * 删除测试环境
     *
     * @param envId 测试环境主键
     * @return 结果
     */
    int deleteTestEnvironmentByEnvId(Long envId);

    /**
     * 批量删除测试环境
     *
     * @param envIds 需要删除的数据主键集合
     * @return 结果
     */
    int deleteTestEnvironmentByEnvIds(Long[] envIds);

    /**
     * 获取项目默认环境
     *
     * @param projectId 项目ID
     * @return 默认环境
     */
    TestEnvironment selectDefaultEnvironment(@Param("projectId") Long projectId);

    /**
     * 设置项目默认环境
     *
     * @param projectId 项目ID
     * @param envId 环境ID
     * @return 结果
     */
    int setDefaultEnvironment(@Param("projectId") Long projectId, @Param("envId") Long envId);
}