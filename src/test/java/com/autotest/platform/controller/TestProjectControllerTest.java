package com.autotest.platform.controller;

import com.autotest.platform.domain.project.TestProject;
import com.autotest.platform.service.ITestProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试项目控制器测试
 *
 * @author autotest
 * @date 2024-01-01
 */
class TestProjectControllerTest {

    @Mock
    private ITestProjectService testProjectService;

    @InjectMocks
    private TestProjectController testProjectController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(testProjectController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testListProjects() throws Exception {
        // Given
        List<TestProject> projects = Arrays.asList(
            createTestProject(1L, "Project 1", "0"),
            createTestProject(2L, "Project 2", "0")
        );
        when(testProjectService.selectTestProjectList(any(TestProject.class))).thenReturn(projects);

        // When & Then
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[0].projectName").value("Project 1"))
                .andExpect(jsonPath("$[1].projectId").value(2))
                .andExpect(jsonPath("$[1].projectName").value("Project 2"));

        verify(testProjectService, times(1)).selectTestProjectList(any(TestProject.class));
        System.out.println("✅ testListProjects passed");
    }

    @Test
    void testGetProjectInfo() throws Exception {
        // Given
        Long projectId = 1L;
        TestProject project = createTestProject(projectId, "Test Project", "0");
        when(testProjectService.selectTestProjectByProjectId(projectId)).thenReturn(project);

        // When & Then
        mockMvc.perform(get("/api/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.projectName").value("Test Project"));

        verify(testProjectService, times(1)).selectTestProjectByProjectId(projectId);
        System.out.println("✅ testGetProjectInfo passed");
    }

    @Test
    void testAddProject() throws Exception {
        // Given
        TestProject project = createTestProject(null, "New Project", "0");
        when(testProjectService.insertTestProject(any(TestProject.class))).thenReturn(1);

        // When & Then
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(testProjectService, times(1)).insertTestProject(any(TestProject.class));
        System.out.println("✅ testAddProject passed");
    }

    @Test
    void testEditProject() throws Exception {
        // Given
        TestProject project = createTestProject(1L, "Updated Project", "0");
        when(testProjectService.updateTestProject(any(TestProject.class))).thenReturn(1);

        // When & Then
        mockMvc.perform(put("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(testProjectService, times(1)).updateTestProject(any(TestProject.class));
        System.out.println("✅ testEditProject passed");
    }

    @Test
    void testRemoveProjects() throws Exception {
        // Given
        Long[] projectIds = {1L, 2L};
        when(testProjectService.deleteTestProjectByProjectIds(projectIds)).thenReturn(2);

        // When & Then
        mockMvc.perform(delete("/api/projects/{projectIds}", "1,2"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        verify(testProjectService, times(1)).deleteTestProjectByProjectIds(projectIds);
        System.out.println("✅ testRemoveProjects passed");
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