package com.autotest.platform.mapper;

import com.autotest.platform.domain.testcase.DataSource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试数据源Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface DataSourceMapper extends BaseMapper<DataSource> {

    /**
     * 查询测试数据源
     *
     * @param dataId 测试数据源主键
     * @return 测试数据源
     */
    DataSource selectDataSourceByDataId(Long dataId);

    /**
     * 查询测试数据源列表
     *
     * @param dataSource 测试数据源
     * @return 测试数据源集合
     */
    List<DataSource> selectDataSourceList(DataSource dataSource);

    /**
     * 根据项目ID查询数据源列表
     *
     * @param projectId 项目ID
     * @return 数据源列表
     */
    List<DataSource> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据数据类型查询数据源列表
     *
     * @param projectId 项目ID
     * @param dataType 数据类型
     * @return 数据源列表
     */
    List<DataSource> selectByProjectIdAndType(@Param("projectId") Long projectId,
                                               @Param("dataType") String dataType);

    /**
     * 新增测试数据源
     *
     * @param dataSource 测试数据源
     * @return 结果
     */
    int insertDataSource(DataSource dataSource);

    /**
     * 修改测试数据源
     *
     * @param dataSource 测试数据源
     * @return 结果
     */
    int updateDataSource(DataSource dataSource);

    /**
     * 删除测试数据源
     *
     * @param dataId 测试数据源主键
     * @return 结果
     */
    int deleteDataSourceByDataId(Long dataId);

    /**
     * 批量删除测试数据源
     *
     * @param dataIds 需要删除的数据主键集合
     * @return 结果
     */
    int deleteDataSourceByDataIds(Long[] dataIds);

    /**
     * 检查数据源名称是否存在
     *
     * @param dataName 数据源名称
     * @param projectId 项目ID
     * @return 数量
     */
    int checkDataSourceNameExists(@Param("dataName") String dataName,
                                  @Param("projectId") Long projectId);
}