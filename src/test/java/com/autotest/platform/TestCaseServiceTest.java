package com.autotest.platform;

import com.autotest.platform.domain.testcase.TestCase;
import com.autotest.platform.domain.testcase.TestCaseCategory;
import com.autotest.platform.service.ITestCaseService;
import com.autotest.platform.service.ITestCaseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 测试用例服务测试
 */
@SpringBootTest
public class TestCaseServiceTest {

    @Autowired
    private ITestCaseService testCaseService;

    @Autowired
    private ITestCaseCategoryService testCaseCategoryService;

    @Test
    public void testTestCaseCategoryService() {
        System.out.println("测试用例分类服务测试");

        // 创建测试分类
        TestCaseCategory category = new TestCaseCategory();
        category.setProjectId(1L);
        category.setCategoryName("测试分类");
        category.setParentId(0L);
        category.setOrderNum(1);
        category.setLeader("测试负责人");

        int result = testCaseCategoryService.insertTestCaseCategory(category);
        System.out.println("创建分类结果: " + result);

        // 查询分类列表
        List<TestCaseCategory> categories = testCaseCategoryService.selectTestCaseCategoryList(category);
        System.out.println("查询分类数量: " + categories.size());
    }

    @Test
    public void testTestCaseService() {
        System.out.println("测试用例服务测试");

        // 创建测试用例
        TestCase testCase = new TestCase();
        testCase.setProjectId(1L);
        testCase.setCategoryId(1L);
        testCase.setCaseTitle("登录功能测试");
        testCase.setCaseType("WEB_UI");
        testCase.setPriority("HIGH");
        testCase.setPreconditions("用户已注册");
        testCase.setExpectedResult("登录成功，跳转到首页");

        // 添加测试步骤
        List<TestCase.TestStep> steps = List.of(
            createTestStep(1, "打开登录页面", "url", "/login", "navigate", null),
            createTestStep(2, "输入用户名", "input", "#username", "sendKeys", "testuser"),
            createTestStep(3, "输入密码", "input", "#password", "sendKeys", "password123"),
            createTestStep(4, "点击登录按钮", "button", "#login-btn", "click", null)
        );
        testCase.setTestStepList(steps);

        // 添加标签
        testCase.setTagList(List.of("登录", "冒烟测试", "核心功能"));

        int result = testCaseService.insertTestCase(testCase);
        System.out.println("创建用例结果: " + result);
        System.out.println("用例编码: " + testCase.getCaseCode());

        // 查询用例列表
        TestCase queryCase = new TestCase();
        queryCase.setProjectId(1L);
        List<TestCase> cases = testCaseService.selectTestCaseList(queryCase);
        System.out.println("查询用例数量: " + cases.size());
    }

    private TestCase.TestStep createTestStep(int stepNumber, String action, String elementType,
                                            String locator, String operation, Object value) {
        TestCase.TestStep step = new TestCase.TestStep();
        step.setStepNumber(stepNumber);
        step.setAction(action);
        step.setElementType(elementType);
        step.setLocator(locator);
        step.setOperation(operation);
        step.setValue(value);
        return step;
    }
}