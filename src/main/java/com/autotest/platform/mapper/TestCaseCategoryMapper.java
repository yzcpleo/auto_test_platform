package com.autotest.platform.mapper;

import com.autotest.platform.domain.testcase.TestCaseCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试用例分类Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface TestCaseCategoryMapper extends BaseMapper<TestCaseCategory> {

    /**
     * 查询测试用例分类
     *
     * @param categoryId 测试用例分类主键
     * @return 测试用例分类
     */
    TestCaseCategory selectTestCaseCategoryByCategoryId(Long categoryId);

    /**
     * 查询测试用例分类列表
     *
     * @param testCaseCategory 测试用例分类
     * @return 测试用例分类集合
     */
    List<TestCaseCategory> selectTestCaseCategoryList(TestCaseCategory testCaseCategory);

    /**
     * 根据项目ID查询分类树
     *
     * @param projectId 项目ID
     * @return 分类树
     */
    List<TestCaseCategory> selectCategoryTreeByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<TestCaseCategory> selectCategoriesByParentId(@Param("parentId") Long parentId);

    /**
     * 新增测试用例分类
     *
     * @param testCaseCategory 测试用例分类
     * @return 结果
     */
    int insertTestCaseCategory(TestCaseCategory testCaseCategory);

    /**
     * 修改测试用例分类
     *
     * @param testCaseCategory 测试用例分类
     * @return 结果
     */
    int updateTestCaseCategory(TestCaseCategory testCaseCategory);

    /**
     * 删除测试用例分类
     *
     * @param categoryId 测试用例分类主键
     * @return 结果
     */
    int deleteTestCaseCategoryByCategoryId(Long categoryId);

    /**
     * 批量删除测试用例分类
     *
     * @param categoryIds 需要删除的数据主键集合
     * @return 结果
     */
    int deleteTestCaseCategoryByCategoryIds(Long[] categoryIds);

    /**
     * 检查分类名称是否存在
     *
     * @param categoryName 分类名称
     * @param parentId 父分类ID
     * @param projectId 项目ID
     * @return 数量
     */
    int checkCategoryNameExists(@Param("categoryName") String categoryName,
                                @Param("parentId") Long parentId,
                                @Param("projectId") Long projectId);

    /**
     * 检查是否存在子分类
     *
     * @param categoryId 分类ID
     * @return 数量
     */
    int hasChildren(@Param("categoryId") Long categoryId);

    /**
     * 检查分类下是否有用例
     *
     * @param categoryId 分类ID
     * @return 数量
     */
    int hasTestCases(@Param("categoryId") Long categoryId);
}