package com.autotest.platform.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.autotest.platform.common.annotation.Log;
import com.autotest.platform.common.core.controller.BaseController;
import com.autotest.platform.common.core.domain.AjaxResult;
import com.autotest.platform.common.enums.BusinessType;
import com.autotest.platform.domain.testcase.TestCaseCategory;
import com.autotest.platform.service.ITestCaseCategoryService;
import com.autotest.platform.common.utils.poi.ExcelUtil;
import com.autotest.platform.common.core.page.TableDataInfo;

/**
 * 测试用例分类Controller
 *
 * @author autotest
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/test/category")
public class TestCaseCategoryController extends BaseController {
    @Autowired
    private ITestCaseCategoryService testCaseCategoryService;

    /**
     * 查询测试用例分类列表
     */
    @PreAuthorize("@ss.hasPermi('test:category:list')")
    @GetMapping("/list")
    public AjaxResult list(TestCaseCategory testCaseCategory) {
        List<TestCaseCategory> list = testCaseCategoryService.selectTestCaseCategoryList(testCaseCategory);
        return AjaxResult.success(list);
    }

    /**
     * 查询测试用例分类树结构
     */
    @PreAuthorize("@ss.hasPermi('test:category:list')")
    @GetMapping("/tree/{projectId}")
    public AjaxResult tree(@PathVariable Long projectId) {
        TestCaseCategory category = new TestCaseCategory();
        category.setProjectId(projectId);
        List<TestCaseCategory> categories = testCaseCategoryService.selectTestCaseCategoryList(category);
        List<TestCaseCategory> tree = testCaseCategoryService.buildCategoryTree(categories);
        return AjaxResult.success(tree);
    }

    /**
     * 导出测试用例分类列表
     */
    @PreAuthorize("@ss.hasPermi('test:category:export')")
    @Log(title = "测试用例分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TestCaseCategory testCaseCategory) {
        List<TestCaseCategory> list = testCaseCategoryService.selectTestCaseCategoryList(testCaseCategory);
        ExcelUtil<TestCaseCategory> util = new ExcelUtil<>(TestCaseCategory.class);
        util.exportExcel(response, list, "测试用例分类数据");
    }

    /**
     * 获取测试用例分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('test:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId) {
        return AjaxResult.success(testCaseCategoryService.selectTestCaseCategoryByCategoryId(categoryId));
    }

    /**
     * 新增测试用例分类
     */
    @PreAuthorize("@ss.hasPermi('test:category:add')")
    @Log(title = "测试用例分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TestCaseCategory testCaseCategory) {
        if (!testCaseCategoryService.checkCategoryNameUnique(testCaseCategory)) {
            return AjaxResult.error("新增分类'" + testCaseCategory.getCategoryName() + "'失败，分类名称已存在");
        }
        return toAjax(testCaseCategoryService.insertTestCaseCategory(testCaseCategory));
    }

    /**
     * 修改测试用例分类
     */
    @PreAuthorize("@ss.hasPermi('test:category:edit')")
    @Log(title = "测试用例分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TestCaseCategory testCaseCategory) {
        if (!testCaseCategoryService.checkCategoryNameUnique(testCaseCategory)) {
            return AjaxResult.error("修改分类'" + testCaseCategory.getCategoryName() + "'失败，分类名称已存在");
        }
        return toAjax(testCaseCategoryService.updateTestCaseCategory(testCaseCategory));
    }

    /**
     * 删除测试用例分类
     */
    @PreAuthorize("@ss.hasPermi('test:category:remove')")
    @Log(title = "测试用例分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds) {
        return toAjax(testCaseCategoryService.deleteTestCaseCategoryByCategoryIds(categoryIds));
    }
}