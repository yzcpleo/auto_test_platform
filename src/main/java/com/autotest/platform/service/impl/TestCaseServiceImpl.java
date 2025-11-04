package com.autotest.platform.service.impl;

import com.autotest.platform.common.utils.DateUtils;
import com.autotest.platform.common.utils.SecurityUtils;
import com.autotest.platform.common.utils.StringUtils;
import com.autotest.platform.domain.testcase.TestCase;
import com.autotest.platform.mapper.TestCaseMapper;
import com.autotest.platform.service.ITestCaseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 测试用例Service业务层处理
 *
 * @author autotest
 */
@Service
public class TestCaseServiceImpl extends ServiceImpl<TestCaseMapper, TestCase> implements ITestCaseService {

    @Autowired
    private TestCaseMapper testCaseMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 查询测试用例
     *
     * @param caseId 测试用例主键
     * @return 测试用例
     */
    @Override
    public TestCase selectTestCaseByCaseId(Long caseId) {
        TestCase testCase = testCaseMapper.selectTestCaseByCaseId(caseId);
        if (testCase != null && StringUtils.isNotEmpty(testCase.getTestSteps())) {
            try {
                // 解析测试步骤JSON
                List<TestCase.TestStep> steps = objectMapper.readValue(testCase.getTestSteps(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, TestCase.TestStep.class));
                testCase.setTestStepList(steps);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return testCase;
    }

    /**
     * 查询测试用例列表
     *
     * @param testCase 测试用例
     * @return 测试用例
     */
    @Override
    public List<TestCase> selectTestCaseList(TestCase testCase) {
        return testCaseMapper.selectTestCaseList(testCase);
    }

    /**
     * 新增测试用例
     *
     * @param testCase 测试用例
     * @return 结果
     */
    @Override
    @Transactional
    public int insertTestCase(TestCase testCase) {
        testCase.setCreateTime(DateUtils.getNowDate());
        testCase.setCreateBy(SecurityUtils.getUsername());
        testCase.setAuthorId(SecurityUtils.getUserId());
        testCase.setVersion(1);
        testCase.setStatus("DRAFT");
        testCase.setDelFlag("0");

        // 生成用例编码
        String caseCode = generateCaseCode(testCase.getProjectId(), testCase.getCategoryId());
        testCase.setCaseCode(caseCode);

        // 序列化测试步骤
        if (testCase.getTestStepList() != null && !testCase.getTestStepList().isEmpty()) {
            try {
                testCase.setTestSteps(objectMapper.writeValueAsString(testCase.getTestStepList()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        // 处理标签
        if (testCase.getTagList() != null && !testCase.getTagList().isEmpty()) {
            testCase.setTags(String.join(",", testCase.getTagList()));
        }

        return testCaseMapper.insertTestCase(testCase);
    }

    /**
     * 修改测试用例
     *
     * @param testCase 测试用例
     * @return 结果
     */
    @Override
    @Transactional
    public int updateTestCase(TestCase testCase) {
        testCase.setUpdateTime(DateUtils.getNowDate());
        testCase.setUpdateBy(SecurityUtils.getUsername());

        // 增加版本号
        TestCase oldCase = selectTestCaseByCaseId(testCase.getCaseId());
        if (oldCase != null) {
            testCase.setVersion(oldCase.getVersion() + 1);
        }

        // 序列化测试步骤
        if (testCase.getTestStepList() != null && !testCase.getTestStepList().isEmpty()) {
            try {
                testCase.setTestSteps(objectMapper.writeValueAsString(testCase.getTestStepList()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        // 处理标签
        if (testCase.getTagList() != null && !testCase.getTagList().isEmpty()) {
            testCase.setTags(String.join(",", testCase.getTagList()));
        }

        return testCaseMapper.updateTestCase(testCase);
    }

    /**
     * 批量删除测试用例
     *
     * @param caseIds 需要删除的测试用例主键
     * @return 结果
     */
    @Override
    public int deleteTestCaseByCaseIds(Long[] caseIds) {
        return testCaseMapper.deleteTestCaseByCaseIds(caseIds);
    }

    /**
     * 删除测试用例信息
     *
     * @param caseId 测试用例主键
     * @return 结果
     */
    @Override
    public int deleteTestCaseByCaseId(Long caseId) {
        return testCaseMapper.deleteTestCaseByCaseId(caseId);
    }

    /**
     * 生成用例编码
     *
     * @param projectId 项目ID
     * @param categoryId 分类ID
     * @return 用例编码
     */
    @Override
    public String generateCaseCode(Long projectId, Long categoryId) {
        return testCaseMapper.generateCaseCode(projectId, categoryId);
    }

    /**
     * 检查用例编码是否唯一
     *
     * @param testCase 测试用例信息
     * @return 结果
     */
    @Override
    public boolean checkCaseCodeUnique(TestCase testCase) {
        Long caseId = testCase.getCaseId() == null ? -1L : testCase.getCaseId();
        TestCase info = testCaseMapper.selectOne(new QueryWrapper<TestCase>()
                .eq("case_code", testCase.getCaseCode())
                .eq("project_id", testCase.getProjectId())
                .eq("del_flag", "0"));
        if (info != null && info.getCaseId().longValue() != caseId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 更新用例状态
     *
     * @param caseId 用例ID
     * @param status 状态
     * @return 结果
     */
    @Override
    public int updateCaseStatus(Long caseId, String status) {
        return testCaseMapper.updateCaseStatus(caseId, status);
    }

    /**
     * 批量更新用例状态
     *
     * @param caseIds 用例ID数组
     * @param status 状态
     * @return 结果
     */
    @Override
    @Transactional
    public int batchUpdateCaseStatus(Long[] caseIds, String status) {
        int result = 0;
        for (Long caseId : caseIds) {
            result += updateCaseStatus(caseId, status);
        }
        return result;
    }

    /**
     * 复制测试用例
     *
     * @param caseId 原用例ID
     * @param categoryId 目标分类ID
     * @param caseTitle 新用例标题
     * @return 结果
     */
    @Override
    @Transactional
    public int copyTestCase(Long caseId, Long categoryId, String caseTitle) {
        TestCase originalCase = selectTestCaseByCaseId(caseId);
        if (originalCase == null) {
            return 0;
        }

        TestCase newCase = new TestCase();
        newCase.setProjectId(originalCase.getProjectId());
        newCase.setCategoryId(categoryId);
        newCase.setCaseTitle(StringUtils.isNotEmpty(caseTitle) ? caseTitle : originalCase.getCaseTitle() + " - 副本");
        newCase.setCaseType(originalCase.getCaseType());
        newCase.setPriority(originalCase.getPriority());
        newCase.setPreconditions(originalCase.getPreconditions());
        newCase.setTestStepList(originalCase.getTestStepList());
        newCase.setExpectedResult(originalCase.getExpectedResult());
        newCase.setTestDataSource(originalCase.getTestDataSource());
        newCase.setTagList(originalCase.getTagList());
        newCase.setStatus("DRAFT");

        return insertTestCase(newCase);
    }

    /**
     * 移动测试用例到其他分类
     *
     * @param caseIds 用例ID数组
     * @param targetCategoryId 目标分类ID
     * @return 结果
     */
    @Override
    @Transactional
    public int moveTestCases(Long[] caseIds, Long targetCategoryId) {
        int result = 0;
        for (Long caseId : caseIds) {
            TestCase testCase = new TestCase();
            testCase.setCaseId(caseId);
            testCase.setCategoryId(targetCategoryId);
            testCase.setUpdateBy(SecurityUtils.getUsername());
            testCase.setUpdateTime(DateUtils.getNowDate());
            result += testCaseMapper.updateTestCase(testCase);
        }
        return result;
    }
}