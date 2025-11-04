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
import com.autotest.platform.domain.testcase.TestCase;
import com.autotest.platform.service.ITestCaseService;
import com.autotest.platform.common.utils.poi.ExcelUtil;
import com.autotest.platform.common.core.page.TableDataInfo;

/**
 * 测试用例Controller
 *
 * @author autotest
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/test/case")
public class TestCaseController extends BaseController {
    @Autowired
    private ITestCaseService testCaseService;

    /**
     * 查询测试用例列表
     */
    @PreAuthorize("@ss.hasPermi('test:case:list')")
    @GetMapping("/list")
    public TableDataInfo list(TestCase testCase) {
        startPage();
        List<TestCase> list = testCaseService.selectTestCaseList(testCase);
        return getDataTable(list);
    }

    /**
     * 查询测试用例列表（不分页）
     */
    @PreAuthorize("@ss.hasPermi('test:case:list')")
    @GetMapping("/all")
    public AjaxResult all(TestCase testCase) {
        List<TestCase> list = testCaseService.selectTestCaseList(testCase);
        return AjaxResult.success(list);
    }

    /**
     * 导出测试用例列表
     */
    @PreAuthorize("@ss.hasPermi('test:case:export')")
    @Log(title = "测试用例", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TestCase testCase) {
        List<TestCase> list = testCaseService.selectTestCaseList(testCase);
        ExcelUtil<TestCase> util = new ExcelUtil<>(TestCase.class);
        util.exportExcel(response, list, "测试用例数据");
    }

    /**
     * 获取测试用例详细信息
     */
    @PreAuthorize("@ss.hasPermi('test:case:query')")
    @GetMapping(value = "/{caseId}")
    public AjaxResult getInfo(@PathVariable("caseId") Long caseId) {
        return AjaxResult.success(testCaseService.selectTestCaseByCaseId(caseId));
    }

    /**
     * 新增测试用例
     */
    @PreAuthorize("@ss.hasPermi('test:case:add')")
    @Log(title = "测试用例", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TestCase testCase) {
        if (!testCaseService.checkCaseCodeUnique(testCase)) {
            return AjaxResult.error("新增用例编码'" + testCase.getCaseCode() + "'失败，用例编码已存在");
        }
        return toAjax(testCaseService.insertTestCase(testCase));
    }

    /**
     * 修改测试用例
     */
    @PreAuthorize("@ss.hasPermi('test:case:edit')")
    @Log(title = "测试用例", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TestCase testCase) {
        testCaseService.checkCaseCodeUnique(testCase);
        return toAjax(testCaseService.updateTestCase(testCase));
    }

    /**
     * 删除测试用例
     */
    @PreAuthorize("@ss.hasPermi('test:case:remove')")
    @Log(title = "测试用例", businessType = BusinessType.DELETE)
    @DeleteMapping("/{caseIds}")
    public AjaxResult remove(@PathVariable Long[] caseIds) {
        return toAjax(testCaseService.deleteTestCaseByCaseIds(caseIds));
    }

    /**
     * 更新用例状态
     */
    @PreAuthorize("@ss.hasPermi('test:case:edit')")
    @Log(title = "测试用例", businessType = BusinessType.UPDATE)
    @PutMapping("/status/{caseId}/{status}")
    public AjaxResult updateStatus(@PathVariable Long caseId, @PathVariable String status) {
        return toAjax(testCaseService.updateCaseStatus(caseId, status));
    }

    /**
     * 批量更新用例状态
     */
    @PreAuthorize("@ss.hasPermi('test:case:edit')")
    @Log(title = "测试用例", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult batchUpdateStatus(@RequestBody BatchStatusUpdateRequest request) {
        return toAjax(testCaseService.batchUpdateCaseStatus(request.getCaseIds(), request.getStatus()));
    }

    /**
     * 复制测试用例
     */
    @PreAuthorize("@ss.hasPermi('test:case:add')")
    @Log(title = "测试用例", businessType = BusinessType.INSERT)
    @PostMapping("/copy/{caseId}")
    public AjaxResult copy(@PathVariable Long caseId, @RequestBody CopyTestCaseRequest request) {
        return toAjax(testCaseService.copyTestCase(caseId, request.getCategoryId(), request.getCaseTitle()));
    }

    /**
     * 移动测试用例
     */
    @PreAuthorize("@ss.hasPermi('test:case:edit')")
    @Log(title = "测试用例", businessType = BusinessType.UPDATE)
    @PutMapping("/move")
    public AjaxResult move(@RequestBody MoveTestCaseRequest request) {
        return toAjax(testCaseService.moveTestCases(request.getCaseIds(), request.getTargetCategoryId()));
    }

    /**
     * 批量状态更新请求对象
     */
    public static class BatchStatusUpdateRequest {
        private Long[] caseIds;
        private String status;

        public Long[] getCaseIds() {
            return caseIds;
        }

        public void setCaseIds(Long[] caseIds) {
            this.caseIds = caseIds;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * 复制用例请求对象
     */
    public static class CopyTestCaseRequest {
        private Long categoryId;
        private String caseTitle;

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getCaseTitle() {
            return caseTitle;
        }

        public void setCaseTitle(String caseTitle) {
            this.caseTitle = caseTitle;
        }
    }

    /**
     * 移动用例请求对象
     */
    public static class MoveTestCaseRequest {
        private Long[] caseIds;
        private Long targetCategoryId;

        public Long[] getCaseIds() {
            return caseIds;
        }

        public void setCaseIds(Long[] caseIds) {
            this.caseIds = caseIds;
        }

        public Long getTargetCategoryId() {
            return targetCategoryId;
        }

        public void setTargetCategoryId(Long targetCategoryId) {
            this.targetCategoryId = targetCategoryId;
        }
    }
}