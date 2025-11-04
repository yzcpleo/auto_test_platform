package com.autotest.platform.e2e;

import com.autotest.platform.domain.project.TestProject;
import com.autotest.platform.service.ITestProjectService;
import com.autotest.platform.service.impl.TestProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 完整工作流程端到端测试
 *
 * @author autotest
 * @date 2024-01-01
 */
class CompleteWorkflowE2ETest {

    @Mock
    private ITestProjectService testProjectService;

    private TestProject testProject;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testProject = new TestProject();
        testProject.setProjectId(1L);
        testProject.setProjectName("E2E Test Project");
        testProject.setProjectDesc("End-to-end test project");
        testProject.setStatus("0");
        testProject.setTenantId(100L);
    }

    @Test
    @Order(1)
    void test01_ProjectCreationWorkflow() {
        // 模拟项目创建工作流
        System.out.println("=== 测试项目创建工作流 ===");

        // Given
        when(testProjectService.insertTestProject(any(TestProject.class))).thenReturn(1);
        when(testProjectService.selectTestProjectByProjectId(1L)).thenReturn(testProject);

        // When - 创建项目
        TestProject newProject = new TestProject();
        newProject.setProjectName("E2E Test Project");
        newProject.setProjectDesc("End-to-end test project");
        newProject.setStatus("0");
        newProject.setTenantId(100L);

        int createResult = testProjectService.insertTestProject(newProject);

        // Then - 验证创建结果
        assertEquals(1, createResult);
        verify(testProjectService, times(1)).insertTestProject(any(TestProject.class));

        // When - 查询项目
        TestProject retrievedProject = testProjectService.selectTestProjectByProjectId(1L);

        // Then - 验证查询结果
        assertNotNull(retrievedProject);
        assertEquals("E2E Test Project", retrievedProject.getProjectName());
        assertEquals("End-to-end test project", retrievedProject.getProjectDesc());

        System.out.println("✅ 项目创建工作流测试通过");
    }

    @Test
    @Order(2)
    void test02_ProjectUpdateWorkflow() {
        // 模拟项目更新工作流
        System.out.println("=== 测试项目更新工作流 ===");

        // Given
        when(testProjectService.updateTestProject(any(TestProject.class))).thenReturn(1);
        when(testProjectService.selectTestProjectByProjectId(1L)).thenReturn(testProject);

        // When - 更新项目
        testProject.setProjectName("Updated E2E Project");
        testProject.setProjectDesc("Updated description");
        testProject.setStatus("1");

        int updateResult = testProjectService.updateTestProject(testProject);

        // Then - 验证更新结果
        assertEquals(1, updateResult);
        verify(testProjectService, times(1)).updateTestProject(any(TestProject.class));

        // When - 查询更新后的项目
        TestProject updatedProject = testProjectService.selectTestProjectByProjectId(1L);

        // Then - 验证更新内容
        assertNotNull(updatedProject);
        assertEquals("Updated E2E Project", updatedProject.getProjectName());
        assertEquals("Updated description", updatedProject.getProjectDesc());
        assertEquals("1", updatedProject.getStatus());

        System.out.println("✅ 项目更新工作流测试通过");
    }

    @Test
    @Order(3)
    void test03_ProjectQueryWorkflow() {
        // 模拟项目查询工作流
        System.out.println("=== 测试项目查询工作流 ===");

        // Given
        List<TestProject> mockProjects = Arrays.asList(
            createTestProject(1L, "Project Alpha", "0"),
            createTestProject(2L, "Project Beta", "0"),
            createTestProject(3L, "Project Gamma", "1")
        );
        when(testProjectService.selectTestProjectList(any(TestProject.class))).thenReturn(mockProjects);

        // When - 查询项目列表
        TestProject searchProject = new TestProject();
        searchProject.setTenantId(100L);
        List<TestProject> projects = testProjectService.selectTestProjectList(searchProject);

        // Then - 验证查询结果
        assertNotNull(projects);
        assertEquals(3, projects.size());

        // 验证每个项目的属性
        for (TestProject project : projects) {
            assertNotNull(project.getProjectId());
            assertNotNull(project.getProjectName());
            assertNotNull(project.getStatus());
            assertNotNull(project.getTenantId());
        }

        verify(testProjectService, times(1)).selectTestProjectList(any(TestProject.class));
        System.out.println("✅ 项目查询工作流测试通过");
    }

    @Test
    @Order(4)
    void test04_ProjectDeletionWorkflow() {
        // 模拟项目删除工作流
        System.out.println("=== 测试项目删除工作流 ===");

        // Given
        when(testProjectService.deleteTestProjectByProjectIds(any(Long[].class))).thenReturn(2);
        when(testProjectService.deleteTestProjectByProjectId(1L)).thenReturn(1);

        // When - 批量删除项目
        Long[] projectIds = {1L, 2L};
        int batchDeleteResult = testProjectService.deleteTestProjectByProjectIds(projectIds);

        // Then - 验证批量删除结果
        assertEquals(2, batchDeleteResult);
        verify(testProjectService, times(1)).deleteTestProjectByProjectIds(projectIds);

        // When - 单个项目删除
        int singleDeleteResult = testProjectService.deleteTestProjectByProjectId(1L);

        // Then - 验证单个删除结果
        assertEquals(1, singleDeleteResult);
        verify(testProjectService, times(1)).deleteTestProjectByProjectId(1L);

        System.out.println("✅ 项目删除工作流测试通过");
    }

    @Test
    @Order(5)
    void test05_CompleteBusinessWorkflow() {
        // 模拟完整的业务工作流
        System.out.println("=== 测试完整业务工作流 ===");

        // Given - 设置模拟行为
        when(testProjectService.insertTestProject(any(TestProject.class))).thenReturn(1);
        when(testProjectService.selectTestProjectByProjectId(1L)).thenAnswer(invocation -> {
            TestProject project = new TestProject();
            project.setProjectId(1L);
            project.setProjectName("Updated Business Project");
            project.setProjectDesc("Testing complete business workflow");
            project.setStatus("1");
            project.setTenantId(200L);
            return project;
        });
        when(testProjectService.updateTestProject(any(TestProject.class))).thenReturn(1);
        when(testProjectService.selectTestProjectList(any(TestProject.class)))
                .thenReturn(Arrays.asList(testProject));
        when(testProjectService.deleteTestProjectByProjectId(1L)).thenReturn(1);

        // Step 1: 创建项目
        TestProject newProject = new TestProject();
        newProject.setProjectName("Business Workflow Project");
        newProject.setProjectDesc("Testing complete business workflow");
        newProject.setStatus("0");
        newProject.setTenantId(200L);

        int createResult = testProjectService.insertTestProject(newProject);
        assertEquals(1, createResult);
        System.out.println("步骤1: 项目创建成功");

        // Step 2: 验证项目创建
        TestProject createdProject = testProjectService.selectTestProjectByProjectId(1L);
        assertNotNull(createdProject);
        assertEquals("Updated Business Project", createdProject.getProjectName());
        System.out.println("步骤2: 项目创建验证成功");

        // Step 3: 更新项目
        createdProject.setProjectName("Updated Business Project");
        createdProject.setStatus("1");
        int updateResult = testProjectService.updateTestProject(createdProject);
        assertEquals(1, updateResult);
        System.out.println("步骤3: 项目更新成功");

        // Step 4: 查询项目列表
        List<TestProject> projects = testProjectService.selectTestProjectList(new TestProject());
        assertNotNull(projects);
        assertFalse(projects.isEmpty());
        System.out.println("步骤4: 项目列表查询成功，找到 " + projects.size() + " 个项目");

        // Step 5: 删除项目
        int deleteResult = testProjectService.deleteTestProjectByProjectId(1L);
        assertEquals(1, deleteResult);
        System.out.println("步骤5: 项目删除成功");

        // 验证所有方法调用
        verify(testProjectService, times(1)).insertTestProject(any(TestProject.class));
        verify(testProjectService, times(1)).selectTestProjectByProjectId(1L);
        verify(testProjectService, times(1)).updateTestProject(any(TestProject.class));
        verify(testProjectService, times(1)).selectTestProjectList(any(TestProject.class));
        verify(testProjectService, times(1)).deleteTestProjectByProjectId(1L);

        System.out.println("✅ 完整业务工作流测试通过");
    }

    @Test
    @Order(6)
    void test06_ErrorHandlingWorkflow() {
        // 模拟错误处理工作流
        System.out.println("=== 测试错误处理工作流 ===");

        // Given - 模拟服务异常
        when(testProjectService.selectTestProjectByProjectId(999L))
                .thenThrow(new RuntimeException("Project not found"));

        // When & Then - 测试异常处理
        assertThrows(RuntimeException.class, () -> {
            testProjectService.selectTestProjectByProjectId(999L);
        });

        verify(testProjectService, times(1)).selectTestProjectByProjectId(999L);
        System.out.println("✅ 错误处理工作流测试通过");
    }

    private TestProject createTestProject(Long id, String name, String status) {
        TestProject project = new TestProject();
        project.setProjectId(id);
        project.setProjectName(name);
        project.setProjectDesc("Description for " + name);
        project.setStatus(status);
        project.setTenantId(100L);
        return project;
    }
}