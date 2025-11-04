package com.autotest.platform.service;

import com.autotest.platform.domain.testcase.DataSource;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 测试数据源Service接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface IDataSourceService extends IService<DataSource> {

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
     * 批量删除测试数据源
     *
     * @param dataIds 需要删除的测试数据源主键集合
     * @return 结果
     */
    int deleteDataSourceByDataIds(Long[] dataIds);

    /**
     * 删除测试数据源信息
     *
     * @param dataId 测试数据源主键
     * @return 结果
     */
    int deleteDataSourceByDataId(Long dataId);

    /**
     * 检查数据源名称是否唯一
     *
     * @param dataSource 测试数据源信息
     * @return 结果
     */
    boolean checkDataSourceNameUnique(DataSource dataSource);

    /**
     * 上传文件类型数据源
     *
     * @param dataSource 数据源信息
     * @param filePath 文件路径
     * @param fileName 文件名
     * @return 结果
     */
    int uploadFileDataSource(DataSource dataSource, String filePath, String fileName);

    /**
     * 验证数据库连接
     *
     * @param dbConfig 数据库配置
     * @return 验证结果
     */
    boolean validateDatabaseConnection(Map<String, Object> dbConfig);

    /**
     * 测试API数据源
     *
     * @param apiConfig API配置
     * @return 测试结果
     */
    Map<String, Object> testApiDataSource(Map<String, Object> apiConfig);

    /**
     * 预览数据源内容
     *
     * @param dataId 数据源ID
     * @param limit 限制条数
     * @return 数据内容
     */
    List<Map<String, Object>> previewDataSource(Long dataId, Integer limit);

    /**
     * 获取数据源记录数
     *
     * @param dataId 数据源ID
     * @return 记录数
     */
    Integer getDataSourceRecordCount(Long dataId);
}