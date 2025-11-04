package com.autotest.platform.domain.project;

import com.autotest.platform.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 测试项目对象 test_project
 *
 * @author autotest
 * @date 2024-01-01
 */
@TableName("test_project")
public class TestProject extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 项目名称 */
    private String projectName;

    /** 项目描述 */
    private String projectDesc;

    /** 项目状态（0-正常 1-停用） */
    private String status;

    /** 租户ID */
    private Long tenantId;

    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDesc() {
        return projectDesc;
    }

    public void setProjectDesc(String projectDesc) {
        this.projectDesc = projectDesc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}