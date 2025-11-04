package com.autotest.platform.mapper;

import com.autotest.platform.domain.project.ProjectMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目成员Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {

    /**
     * 查询项目成员
     *
     * @param memberId 项目成员主键
     * @return 项目成员
     */
    ProjectMember selectProjectMemberByMemberId(Long memberId);

    /**
     * 查询项目成员列表
     *
     * @param projectMember 项目成员
     * @return 项目成员集合
     */
    List<ProjectMember> selectProjectMemberList(ProjectMember projectMember);

    /**
     * 根据项目ID查询成员列表
     *
     * @param projectId 项目ID
     * @return 成员列表
     */
    List<ProjectMember> selectMembersByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据用户ID查询参与的项目列表
     *
     * @param userId 用户ID
     * @return 项目ID列表
     */
    List<Long> selectProjectIdsByUserId(@Param("userId") Long userId);

    /**
     * 新增项目成员
     *
     * @param projectMember 项目成员
     * @return 结果
     */
    int insertProjectMember(ProjectMember projectMember);

    /**
     * 修改项目成员
     *
     * @param projectMember 项目成员
     * @return 结果
     */
    int updateProjectMember(ProjectMember projectMember);

    /**
     * 删除项目成员
     *
     * @param memberId 项目成员主键
     * @return 结果
     */
    int deleteProjectMemberByMemberId(Long memberId);

    /**
     * 批量删除项目成员
     *
     * @param memberIds 需要删除的数据主键集合
     * @return 结果
     */
    int deleteProjectMemberByMemberIds(Long[] memberIds);

    /**
     * 检查用户是否已是项目成员
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 数量
     */
    int checkMemberExists(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 检查用户是否有项目权限
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param permission 权限标识
     * @return 数量
     */
    int checkUserPermission(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("permission") String permission);
}