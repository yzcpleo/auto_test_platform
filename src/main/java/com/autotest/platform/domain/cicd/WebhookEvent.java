package com.autotest.platform.domain.cicd;

import com.autotest.platform.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * Webhook事件对象 webhook_event
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@TableName("webhook_event")
public class WebhookEvent extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long eventId;

    /** 事件ID */
    private String eventUuid;

    /** 流水线ID */
    private Long pipelineId;

    /** 项目ID */
    private Long projectId;

    /** 事件类型 (PUSH/MERGE_REQUEST/RELEASE/TAG) */
    private String eventType;

    /** 事件来源 (GITHUB/GITLAB/GITEE/CUSTOM) */
    private String eventSource;

    /** 仓库URL */
    private String repositoryUrl;

    /** 分支名称 */
    private String branch;

    /** 标签名称 */
    private String tag;

    /** 提交SHA */
    private String commitSha;

    /** 提交信息 */
    private String commitMessage;

    /** 提交作者 */
    private String commitAuthor;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date commitTime;

    /** 事件数据(JSON格式) */
    private String eventData;

    /** 处理状态 (PENDING/PROCESSING/SUCCESS/FAILED/IGNORED) */
    private String processStatus;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date processTime;

    /** 触发的执行ID */
    private Long triggeredExecutionId;

    /** 错误信息 */
    private String errorMessage;

    /** 重试次数 */
    private Integer retryCount = 0;

    /** 签名验证结果 */
    private Boolean signatureValid;

    /** 请求头 */
    private String requestHeaders;

    /** 请求体 */
    private String requestBody;

    /** 响应内容 */
    private String responseContent;

    /** 响应状态码 */
    private Integer responseStatusCode;

    /** 项目名称 */
    @TableField(exist = false)
    private String projectName;

    /** 流水线名称 */
    @TableField(exist = false)
    private String pipelineName;

    /** 事件数据对象 */
    @TableField(exist = false)
    private Map<String, Object> eventDataMap;

    /**
     * Git Push事件数据
     */
    @Data
    public static class PushEvent {
        private String ref;
        private String before;
        private String after;
        private String repository;
        private Pusher pusher;
        private List<Commit> commits;

        @Data
        public static class Commit {
            private String id;
            private String message;
            private String author;
            private String url;
            private Boolean distinct;
            private Added added;
            private Removed removed;
            private Modified modified;
            private Timestamp timestamp;
        }

        @Data
        public static class Pusher {
            private String name;
            private String email;
        }

        @Data
        public static class Repository {
            private String id;
            private String name;
            private String fullName;
            private String htmlUrl;
            private Boolean private;
            private Owner owner;

            @Data
            public static class Owner {
                private String name;
                String email;
            }
        }
    }

    /**
     * Merge Request事件数据
     */
    @Data
    public static class MergeRequestEvent {
        private String action;
        private Integer number;
        private MergeRequest mergeRequest;
        private Repository repository;

        @Data
        public static class MergeRequest {
            private Integer id;
            private String title;
            private String description;
            private String state;
            private String sourceBranch;
            private String targetBranch;
            private Author author;
            private List<Commit> commits;

            @Data
            public static class Author {
                private String id;
                private String name;
                private String username;
                String email;
            }
        }
    }

    /**
     * Release事件数据
     */
    @Data
    public static class ReleaseEvent {
        private String action;
        private Release release;
        private Repository repository;

        @Data
        public static class Release {
            private String tag;
            private String name;
            private Boolean draft;
            private Boolean prerelease;
            private Author author;
            private PublishedAt publishedAt;
            private Assets assets;

            @Data
            public static class Author {
                private String id;
                private String login;
                String name;
                String email;
            }

            @Data
            public static class Assets {
                private List<Asset> assets;

                @Data
                public static class Asset {
                    private String name;
                    private String contentType;
                    private Integer size;
                    private String browserDownloadUrl;
                }
            }
        }
    }
}