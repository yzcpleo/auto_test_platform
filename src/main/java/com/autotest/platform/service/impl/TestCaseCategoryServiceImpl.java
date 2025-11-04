package com.autotest.platform.service.impl;

import com.autotest.platform.common.utils.DateUtils;
import com.autotest.platform.common.utils.SecurityUtils;
import com.autotest.platform.common.utils.StringUtils;
import com.autotest.platform.domain.testcase.TestCaseCategory;
import com.autotest.platform.mapper.TestCaseCategoryMapper;
import com.autotest.platform.service.ITestCaseCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 测试用例分类Service业务层处理
 *
 * @author autotest
 */
@Service
public class TestCaseCategoryServiceImpl extends ServiceImpl<TestCaseCategoryMapper, TestCaseCategory> implements ITestCaseCategoryService {

    @Autowired
    private TestCaseCategoryMapper testCaseCategoryMapper;

    /**
     * 查询测试用例分类
     *
     * @param categoryId 测试用例分类主键
     * @return 测试用例分类
     */
    @Override
    public TestCaseCategory selectTestCaseCategoryByCategoryId(Long categoryId) {
        return testCaseCategoryMapper.selectTestCaseCategoryByCategoryId(categoryId);
    }

    /**
     * 查询测试用例分类列表
     *
     * @param testCaseCategory 测试用例分类
     * @return 测试用例分类
     */
    @Override
    public List<TestCaseCategory> selectTestCaseCategoryList(TestCaseCategory testCaseCategory) {
        return testCaseCategoryMapper.selectTestCaseCategoryList(testCaseCategory);
    }

    /**
     * 构建树形结构
     *
     * @param categories 分类列表
     * @return 树形结构
     */
    @Override
    public List<TestCaseCategory> buildCategoryTree(List<TestCaseCategory> categories) {
        List<TestCaseCategory> returnList = new ArrayList<>();
        List<Long> tempList = new ArrayList<>();
        for (TestCaseCategory category : categories) {
            tempList.add(category.getCategoryId());
        }
        for (Iterator<TestCaseCategory> iterator = categories.iterator(); iterator.hasNext();) {
            TestCaseCategory category = iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(category.getParentId())) {
                recursionFn(categories, category);
                returnList.add(category);
            }
        }
        if (returnList.isEmpty()) {
            returnList = categories;
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<TestCaseCategory> list, TestCaseCategory t) {
        // 得到子节点列表
        List<TestCaseCategory> childList = getChildList(list, t);
        t.setChildren(childList);
        for (TestCaseCategory tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<TestCaseCategory> getChildList(List<TestCaseCategory> list, TestCaseCategory t) {
        List<TestCaseCategory> tlist = new ArrayList<>();
        Iterator<TestCaseCategory> it = list.iterator();
        while (it.hasNext()) {
            TestCaseCategory n = it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getCategoryId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<TestCaseCategory> list, TestCaseCategory t) {
        return getChildList(list, t).size() > 0 ? true : false;
    }

    /**
     * 新增测试用例分类
     *
     * @param testCaseCategory 测试用例分类
     * @return 结果
     */
    @Override
    public int insertTestCaseCategory(TestCaseCategory testCaseCategory) {
        testCaseCategory.setCreateTime(DateUtils.getNowDate());
        testCaseCategory.setCreateBy(SecurityUtils.getUsername());

        // 处理祖级列表
        if (testCaseCategory.getParentId() != null && testCaseCategory.getParentId() != 0) {
            TestCaseCategory parent = selectTestCaseCategoryByCategoryId(testCaseCategory.getParentId());
            if (parent != null) {
                testCaseCategory.setAncestors(parent.getAncestors() + "," + parent.getCategoryId());
            }
        } else {
            testCaseCategory.setParentId(0L);
            testCaseCategory.setAncestors("0");
        }

        return testCaseCategoryMapper.insertTestCaseCategory(testCaseCategory);
    }

    /**
     * 修改测试用例分类
     *
     * @param testCaseCategory 测试用例分类
     * @return 结果
     */
    @Override
    public int updateTestCaseCategory(TestCaseCategory testCaseCategory) {
        testCaseCategory.setUpdateTime(DateUtils.getNowDate());
        testCaseCategory.setUpdateBy(SecurityUtils.getUsername());
        return testCaseCategoryMapper.updateTestCaseCategory(testCaseCategory);
    }

    /**
     * 批量删除测试用例分类
     *
     * @param categoryIds 需要删除的测试用例分类主键
     * @return 结果
     */
    @Override
    public int deleteTestCaseCategoryByCategoryIds(Long[] categoryIds) {
        return testCaseCategoryMapper.deleteTestCaseCategoryByCategoryIds(categoryIds);
    }

    /**
     * 删除测试用例分类信息
     *
     * @param categoryId 测试用例分类主键
     * @return 结果
     */
    @Override
    public int deleteTestCaseCategoryByCategoryId(Long categoryId) {
        return testCaseCategoryMapper.deleteTestCaseCategoryByCategoryId(categoryId);
    }

    /**
     * 检查分类名称是否唯一
     *
     * @param testCaseCategory 测试用例分类信息
     * @return 结果
     */
    @Override
    public boolean checkCategoryNameUnique(TestCaseCategory testCaseCategory) {
        Long categoryId = testCaseCategory.getCategoryId() == null ? -1L : testCaseCategory.getCategoryId();
        TestCaseCategory info = testCaseCategoryMapper.selectOne(new QueryWrapper<TestCaseCategory>()
                .eq("category_name", testCaseCategory.getCategoryName())
                .eq("parent_id", testCaseCategory.getParentId())
                .eq("project_id", testCaseCategory.getProjectId()));
        if (info != null && info.getCategoryId().longValue() != categoryId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 检查是否存在子分类
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    @Override
    public boolean hasChildren(Long categoryId) {
        int result = testCaseCategoryMapper.hasChildren(categoryId);
        return result > 0 ? true : false;
    }

    /**
     * 检查分类下是否有用例
     *
     * @param categoryId 分类ID
     * @return 结果
     */
    @Override
    public boolean hasTestCases(Long categoryId) {
        int result = testCaseCategoryMapper.hasTestCases(categoryId);
        return result > 0 ? true : false;
    }
}