package com.autotest.platform.service.impl;

import com.autotest.platform.domain.project.TestProject;
import com.autotest.platform.mapper.TestProjectMapper;
import com.autotest.platform.service.ITestProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 测试项目服务测试
 *
 * @author autotest
 * @date 2024-01-01
 */
class TestProjectServiceImplTest {

    @Mock
    private TestProjectMapper testProjectMapper;

    @InjectMocks
    private TestProjectServiceImpl testProjectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectTestProjectByProjectId() {
        // Given
        Long projectId = 1L;
        TestProject expectedProject = new TestProject();
        expectedProject.setProjectId(projectId);
        expectedProject.setProjectName("Test Project");

        when(testProjectMapper.selectById(projectId)).thenReturn(expectedProject);

        // When
        TestProject result = testProjectService.selectTestProjectByProjectId(projectId);

        // Then
        assertNotNull(result);
        assertEquals(projectId, result.getProjectId());
        assertEquals("Test Project", result.getProjectName());
        verify(testProjectMapper, times(1)).selectById(projectId);
        System.out.println("✅ selectTestProjectByProjectId test passed");
    }

    @Test
    void testInsertTestProject() {
        // Given
        TestProject project = new TestProject();
        project.setProjectName("New Project");
        project.setProjectDesc("New project description");

        when(testProjectMapper.insert(any(TestProject.class))).thenReturn(1);

        // When
        int result = testProjectService.insertTestProject(project);

        // Then
        assertEquals(1, result);
        assertEquals(0, project.getDelFlag());
        verify(testProjectMapper, times(1)).insert(project);
        System.out.println("✅ insertTestProject test passed");
    }

    @Test
    void testUpdateTestProject() {
        // Given
        TestProject project = new TestProject();
        project.setProjectId(1L);
        project.setProjectName("Updated Project");

        when(testProjectMapper.updateById(any(TestProject.class))).thenReturn(1);

        // When
        int result = testProjectService.updateTestProject(project);

        // Then
        assertEquals(1, result);
        verify(testProjectMapper, times(1)).updateById(project);
        System.out.println("✅ updateTestProject test passed");
    }

    @Test
    void testDeleteTestProjectByProjectId() {
        // Given
        Long projectId = 1L;
        when(testProjectMapper.deleteById(projectId)).thenReturn(1);

        // When
        int result = testProjectService.deleteTestProjectByProjectId(projectId);

        // Then
        assertEquals(1, result);
        verify(testProjectMapper, times(1)).deleteById(projectId);
        System.out.println("✅ deleteTestProjectByProjectId test passed");
    }

    @Test
    void testSelectTestProjectList() {
        // Given
        TestProject searchProject = new TestProject();
        searchProject.setProjectName("Test");
        searchProject.setStatus("0");

        List<TestProject> expectedList = Arrays.asList(
            createTestProject(1L, "Test Project 1", "0"),
            createTestProject(2L, "Test Project 2", "0")
        );

        when(testProjectMapper.selectList(any())).thenReturn(expectedList);

        // When
        List<TestProject> result = testProjectService.selectTestProjectList(searchProject);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Project 1", result.get(0).getProjectName());
        assertEquals("Test Project 2", result.get(1).getProjectName());
        verify(testProjectMapper, times(1)).selectList(any());
        System.out.println("✅ selectTestProjectList test passed");
    }

    @Test
    void testDeleteTestProjectByProjectIds() {
        // Given
        Long[] projectIds = {1L, 2L};
        when(testProjectMapper.delete(any())).thenReturn(2);

        // When
        int result = testProjectService.deleteTestProjectByProjectIds(projectIds);

        // Then
        assertEquals(2, result);
        verify(testProjectMapper, times(1)).delete(any());
        System.out.println("✅ deleteTestProjectByProjectIds test passed");
    }

    private TestProject createTestProject(Long id, String name, String status) {
        TestProject project = new TestProject();
        project.setProjectId(id);
        project.setProjectName(name);
        project.setStatus(status);
        return project;
    }
}