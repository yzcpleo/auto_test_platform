package com.autotest.platform.service;

import com.autotest.platform.domain.testcase.TestCaseCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 测试用例分类Service接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface ITestCaseCategoryService extends IService<TestCaseCategory> {

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
     * 构建树形结构
     *
     * @param categories 分类列表
     * @return 树形结构
     */
    List<TestCaseCategory> buildCategoryTree(List<TestCaseCategory> categories);

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
     * 批量删除测试用例分类
     *
     * @param categoryIds 需要删除的测试用例分类主键集合
     * @return 结果
     */
    int deleteTestCaseCategoryByCategoryIds(Long[] categoryIds);

    /**
     * 删除测试用例分类信息
     *
     * @param categoryId 测试用例分类主键
     * @return 结果
     */
    int deleteTestCaseCategoryByCategoryId(Long categoryId);

    /**
     * 检查分类名称是否唯一
     *
     * @param testCaseCategory 测试用例分类信息
     * @return 结果
     */
    boolean checkCategoryNameUnique(TestCaseCategory testCaseCategory);

    /**
     * 检查是否存在子分类
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    boolean hasChildren(Long categoryId);

    /**
     * 检查分类下是否有用例
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    boolean hasTestCases(Long categoryId);
}