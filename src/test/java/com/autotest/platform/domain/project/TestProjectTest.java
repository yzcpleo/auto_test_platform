package com.autotest.platform.domain.project;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试项目实体类测试
 *
 * @author autotest
 * @date 2024-01-01
 */
class TestProjectTest {

    @Test
    void testTestProjectCreation() {
        // Given
        TestProject project = new TestProject();

        // When
        project.setProjectId(1L);
        project.setProjectName("Test Project");
        project.setProjectDesc("This is a test project");
        project.setStatus("0");
        project.setTenantId(100L);

        // Then
        assertNotNull(project);
        assertEquals(1L, project.getProjectId());
        assertEquals("Test Project", project.getProjectName());
        assertEquals("This is a test project", project.getProjectDesc());
        assertEquals("0", project.getStatus());
        assertEquals(100L, project.getTenantId());
        System.out.println("✅ TestProject creation test passed");
    }

    @Test
    void testTestProjectInheritance() {
        // Given & When
        TestProject project = new TestProject();

        // Then
        assertNotNull(project);
        assertTrue(project instanceof com.autotest.platform.domain.BaseEntity);
        System.out.println("✅ TestProject inheritance test passed");
    }

    @Test
    void testTestProjectSettersAndGetters() {
        // Given
        TestProject project = new TestProject();

        // When
        project.setProjectName("Updated Project");
        project.setProjectDesc("Updated description");
        project.setStatus("1");

        // Then
        assertEquals("Updated Project", project.getProjectName());
        assertEquals("Updated description", project.getProjectDesc());
        assertEquals("1", project.getStatus());
        System.out.println("✅ TestProject setters and getters test passed");
    }
}