package com.autotest.platform.cicd.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Git Webhook处理器
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Component
public class GitWebhookHandler {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 处理GitHub Webhook事件
     */
    public WebhookEventResult handleGitHubWebhook(HttpServletRequest request, String payload) {
        try {
            // 获取GitHub事件类型
            String eventType = request.getHeader("X-GitHub-Event");
            String deliveryId = request.getHeader("X-GitHub-Delivery");
            String signature = request.getHeader("X-Hub-Signature-256");

            log.info("Received GitHub webhook - Event: {}, Delivery: {}", eventType, deliveryId);

            // 验证签名
            if (signature != null && !validateSignature(payload, signature)) {
                return WebhookEventResult.error("Invalid signature");
            }

            // 解析事件数据
            Map<String, Object> eventData = objectMapper.readValue(payload, Map.class);

            // 根据事件类型处理
            switch (eventType) {
                case "push":
                    return handleGitHubPushEvent(eventData, deliveryId);
                case "pull_request":
                    return handleGitHubPullRequestEvent(eventData, deliveryId);
                case "release":
                    return handleGitHubReleaseEvent(eventData, deliveryId);
                default:
                    return WebhookEventResult.ignored("Unsupported event type: " + eventType);
            }

        } catch (Exception e) {
            log.error("Failed to handle GitHub webhook", e);
            return WebhookEventResult.error("Failed to process webhook: " + e.getMessage());
        }
    }

    /**
     * 处理GitLab Webhook事件
     */
    public WebhookEventResult handleGitLabWebhook(HttpServletRequest request, String payload) {
        try {
            // 获取GitLab事件类型
            String eventType = request.getHeader("X-Gitlab-Event-System-Hook-UUID");
            String token = request.getHeader("X-Gitlab-Token");

            log.info("Received GitLab webhook - Event: {}", eventType);

            // 验证token
            if (token != null && !validateGitLabToken(token)) {
                return WebhookEventResult.error("Invalid token");
            }

            // 解析事件数据
            Map<String, Object> eventData = objectMapper.readValue(payload, Map.class);

            // 根据对象类型处理
            String objectKind = (String) eventData.get("object_kind");
            switch (objectKind) {
                case "push":
                    return handleGitLabPushEvent(eventData);
                case "merge_request":
                    return handleGitLabMergeRequestEvent(eventData);
                case "tag_push":
                    return handleGitLabTagPushEvent(eventData);
                default:
                    return WebhookEventResult.ignored("Unsupported object kind: " + objectKind);
            }

        } catch (Exception e) {
            log.error("Failed to handle GitLab webhook", e);
            return WebhookEventResult.error("Failed to process webhook: " + e.getMessage());
        }
    }

    /**
     * 处理Gitee Webhook事件
     */
    public WebhookEventResult handleGiteeWebhook(HttpServletRequest request, String payload) {
        try {
            // 获取Gitee事件类型
            String eventType = request.getHeader("X-Gitee-Event");
            String timestamp = request.getHeader("X-Gitee-Timestamp");
            String token = request.getHeader("X-Gitee-Token");

            log.info("Received Gitee webhook - Event: {}, Timestamp: {}", eventType, timestamp);

            // 验证token
            if (token != null && !validateGiteeToken(token)) {
                return WebhookEventResult.error("Invalid token");
            }

            // 解析事件数据
            Map<String, Object> eventData = objectMapper.readValue(payload, Map.class);

            // 根据事件类型处理
            switch (eventType) {
                case "Push Hook":
                    return handleGiteePushEvent(eventData);
                case "Merge Request Hook":
                    return handleGiteeMergeRequestEvent(eventData);
                case "Tag Push Hook":
                    return handleGiteeTagPushEvent(eventData);
                default:
                    return WebhookEventResult.ignored("Unsupported event type: " + eventType);
            }

        } catch (Exception e) {
            log.error("Failed to handle Gitee webhook", e);
            return WebhookEventResult.error("Failed to process webhook: " + e.getMessage());
        }
    }

    /**
     * 处理GitHub Push事件
     */
    private WebhookEventResult handleGitHubPushEvent(Map<String, Object> eventData, String deliveryId) {
        try {
            String ref = (String) eventData.get("ref");
            String repositoryUrl = getRepositoryUrl(eventData);

            // 提取分支名
            String branch = extractBranchName(ref);

            // 获取提交信息
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> commits = (List<Map<String, Object>>) eventData.get("commits");
            if (commits == null || commits.isEmpty()) {
                return WebhookEventResult.ignored("No commits in push event");
            }

            Map<String, Object> latestCommit = commits.get(commits.size() - 1);
            String commitSha = (String) latestCommit.get("id");
            String commitMessage = (String) latestCommit.get("message");
            String commitAuthor = getCommitAuthor(latestCommit);

            // 创建事件结果
            WebhookEventResult result = new WebhookEventResult();
            result.setEventType("PUSH");
            result.setBranch(branch);
            result.setRepositoryUrl(repositoryUrl);
            result.setCommitSha(commitSha);
            result.setCommitMessage(commitMessage);
            result.setCommitAuthor(commitAuthor);
            result.setDeliveryId(deliveryId);
            result.setEventData(eventData);
            result.setStatus(WebhookEventResult.Status.PROCESSED);

            log.info("Processed GitHub push event - Branch: {}, Commit: {}", branch, commitSha.substring(0, 7));

            return result;

        } catch (Exception e) {
            log.error("Failed to process GitHub push event", e);
            return WebhookEventResult.error("Failed to process push event: " + e.getMessage());
        }
    }

    /**
     * 处理GitHub Pull Request事件
     */
    private WebhookEventResult handleGitHubPullRequestEvent(Map<String, Object> eventData, String deliveryId) {
        try {
            String action = (String) eventData.get("action");
            @SuppressWarnings("unchecked")
            Map<String, Object> pullRequest = (Map<String, Object>) eventData.get("pull_request");

            Integer number = (Integer) pullRequest.get("number");
            String title = (String) pullRequest.get("title");
            String state = (String) pullRequest.get("state");

            @SuppressWarnings("unchecked")
            Map<String, Object> base = (Map<String, Object>) pullRequest.get("base");
            @SuppressWarnings("unchecked")
            Map<String, Object> head = (Map<String, Object>) pullRequest.get("head");

            String baseBranch = (String) base.get("ref");
            String headBranch = (String) head.get("ref");
            String repositoryUrl = getRepositoryUrl(eventData);

            WebhookEventResult result = new WebhookEventResult();
            result.setEventType("MERGE_REQUEST");
            result.setAction(action);
            result.setBranch(baseBranch);
            result.setRepositoryUrl(repositoryUrl);
            result.setPullRequestNumber(number);
            result.setPullRequestTitle(title);
            result.setPullRequestState(state);
            result.setSourceBranch(headBranch);
            result.setTargetBranch(baseBranch);
            result.setDeliveryId(deliveryId);
            result.setEventData(eventData);
            result.setStatus(WebhookEventResult.Status.PROCESSED);

            log.info("Processed GitHub pull request event - Action: {}, PR: #{} - {}", action, number, title);

            return result;

        } catch (Exception e) {
            log.error("Failed to process GitHub pull request event", e);
            return WebhookEventResult.error("Failed to process pull request event: " + e.getMessage());
        }
    }

    /**
     * 处理GitHub Release事件
     */
    private WebhookEventResult handleGitHubReleaseEvent(Map<String, Object> eventData, String deliveryId) {
        try {
            String action = (String) eventData.get("action");
            @SuppressWarnings("unchecked")
            Map<String, Object> release = (Map<String, Object>) eventData.get("release");

            if (release == null) {
                return WebhookEventResult.ignored("No release data in event");
            }

            String tagName = (String) release.get("tag_name");
            String name = (String) release.get("name");
            Boolean prerelease = (Boolean) release.get("prerelease");
            String repositoryUrl = getRepositoryUrl(eventData);

            WebhookEventResult result = new WebhookEventResult();
            result.setEventType("RELEASE");
            result.setAction(action);
            result.setTag(tagName);
            result.setReleaseName(name);
            result.setPrerelease(prerelease);
            result.setRepositoryUrl(repositoryUrl);
            result.setDeliveryId(deliveryId);
            result.setEventData(eventData);
            result.setStatus(WebhookEventResult.Status.PROCESSED);

            log.info("Processed GitHub release event - Action: {}, Tag: {}, Release: {}", action, tagName, name);

            return result;

        } catch (Exception e) {
            log.error("Failed to process GitHub release event", e);
            return WebhookEventResult.error("Failed to process release event: " + e.getMessage());
        }
    }

    /**
     * 处理GitLab Push事件
     */
    private WebhookEventResult handleGitLabPushEvent(Map<String, Object> eventData) {
        try {
            String ref = (String) eventData.get("ref");
            String repositoryUrl = getGitLabRepositoryUrl(eventData);

            String branch = extractBranchName(ref);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> commits = (List<Map<String, Object>>) eventData.get("commits");
            if (commits == null || commits.isEmpty()) {
                return WebhookEventResult.ignored("No commits in push event");
            }

            Map<String, Object> latestCommit = commits.get(commits.size() - 1);
            String commitId = (String) latestCommit.get("id");
            String commitMessage = (String) latestCommit.get("message");
            String commitAuthor = getGitLabCommitAuthor(latestCommit);

            WebhookEventResult result = new WebhookEventResult();
            result.setEventType("PUSH");
            result.setBranch(branch);
            result.setRepositoryUrl(repositoryUrl);
            result.setCommitSha(commitId);
            result.setCommitMessage(commitMessage);
            result.setCommitAuthor(commitAuthor);
            result.setEventData(eventData);
            result.setStatus(WebhookEventResult.Status.PROCESSED);

            log.info("Processed GitLab push event - Branch: {}, Commit: {}", branch, commitId.substring(0, 7));

            return result;

        } catch (Exception e) {
            log.error("Failed to process GitLab push event", e);
            return WebhookEventResult.error("Failed to process push event: " + e.getMessage());
        }
    }

    /**
     * 处理GitLab Merge Request事件
     */
    private WebhookEventResult handleGitLabMergeRequestEvent(Map<String, Object> eventData) {
        try {
            String action = (String) eventData.get("object_attributes");
            if (action == null) {
                action = (String) ((Map<String, Object>) eventData.get("object_attributes")).get("action");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> mergeRequest = (Map<String, Object>) eventData.get("object_attributes");

            Integer iid = (Integer) mergeRequest.get("iid");
            String title = (String) mergeRequest.get("title");
            String state = (String) mergeRequest.get("state");

            String sourceBranch = (String) mergeRequest.get("source_branch");
            String targetBranch = (String) mergeRequest.get("target_branch");
            String repositoryUrl = getGitLabRepositoryUrl(eventData);

            WebhookEventResult result = new WebhookEventResult();
            result.setEventType("MERGE_REQUEST");
            result.setAction(action);
            result.setBranch(targetBranch);
            result.setRepositoryUrl(repositoryUrl);
            result.setPullRequestNumber(iid);
            result.setPullRequestTitle(title);
            result.setPullRequestState(state);
            result.setSourceBranch(sourceBranch);
            result.setTargetBranch(targetBranch);
            result.setEventData(eventData);
            result.setStatus(WebhookEventResult.Status.PROCESSED);

            log.info("Processed GitLab merge request event - Action: {}, MR: !{} - {}", action, iid, title);

            return result;

        } catch (Exception e) {
            log.error("Failed to process GitLab merge request event", e);
            return WebhookEventResult.error("Failed to process merge request event: " + e.getMessage());
        }
    }

    /**
     * 处理GitLab Tag Push事件
     */
    private WebhookEventResult handleGitLabTagPushEvent(Map<String, Object> eventData) {
        try {
            String ref = (String) eventData.get("ref");
            String repositoryUrl = getGitLabRepositoryUrl(eventData);

            String tag = extractTagName(ref);

            @SuppressWarnings("unchecked")
            Map<String, Object> checkoutSha = (Map<String, Object>) eventData.get("checkout_sha");
            String commitSha = (String) checkoutSha.get("id");

            WebhookEventResult result = new WebhookEventResult();
            result.setEventType("RELEASE");
            result.setTag(tag);
            result.setCommitSha(commitSha);
            result.setRepositoryUrl(repositoryUrl);
            result.setEventData(eventData);
            result.setStatus(WebhookEventResult.Status.PROCESSED);

            log.info("Processed GitLab tag push event - Tag: {}, Commit: {}", tag, commitSha.substring(0, 7));

            return result;

        } catch (Exception e) {
            log.error("Failed to process GitLab tag push event", e);
            return WebhookEventResult.error("Failed to process tag push event: " + e.getMessage());
        }
    }

    /**
     * 处理Gitee Push事件
     */
    private WebhookEventResult handleGiteePushEvent(Map<String, Object> eventData) {
        try {
            String ref = (String) eventData.get("ref");
            String repositoryUrl = getGiteeRepositoryUrl(eventData);

            String branch = extractBranchName(ref);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> commits = (List<Map<String, Object>>) eventData.get("commits");
            if (commits == null || commits.isEmpty()) {
                return WebhookEventResult.ignored("No commits in push event");
            }

            Map<String, Object> latestCommit = commits.get(commits.size() - 1);
            String commitId = (String) latestCommit.get("id");
            String commitMessage = (String) latestCommit.get("message");
            String commitAuthor = getGiteeCommitAuthor(latestCommit);

            WebhookEventResult result = new WebhookEventResult();
            result.setEventType("PUSH");
            result.setBranch(branch);
            result.setRepositoryUrl(repositoryUrl);
            result.setCommitSha(commitId);
            result.setCommitMessage(commitMessage);
            result.setCommitAuthor(commitAuthor);
            result.setEventData(eventData);
            result.setStatus(WebhookEventResult.Status.PROCESSED);

            log.info("Processed Gitee push event - Branch: {}, Commit: {}", branch, commitId.substring(0, 7));

            return result;

        } catch (Exception e) {
            log.error("Failed to process Gitee push event", e);
            return WebhookEventResult.error("Failed to process push event: " + e.getMessage());
        }
    }

    /**
     * 处理Gitee Merge Request事件
     */
    private WebhookEventResult handleGiteeMergeRequestEvent(Map<String, Object> eventData) {
        try {
            String action = (String) eventData.get("action");
            @SuppressWarnings("unchecked")
            Map<String, Object> pullRequest = (Map<String, Object>) eventData.get("pull_request");

            Integer number = (Integer) pullRequest.get("number");
            String title = (String) pullRequest.get("title");
            String state = (String) pullRequest.get("state");

            String baseBranch = (String) pullRequest.get("base");
            String headBranch = (String) pullRequest.get("head");
            String repositoryUrl = getGiteeRepositoryUrl(eventData);

            WebhookEventResult result = new WebhookEventResult();
            result.setEventType("MERGE_REQUEST");
            result.setAction(action);
            result.setBranch(baseBranch);
            result.setRepositoryUrl(repositoryUrl);
            result.setPullRequestNumber(number);
            result.setPullRequestTitle(title);
            result.setPullRequestState(state);
            result.setSourceBranch(headBranch);
            result.setTargetBranch(baseBranch);
            result.setEventData(eventData);
            result.setStatus(WebhookEventResult.Status.PROCESSED);

            log.info("Processed Gitee merge request event - Action: {}, PR: #{} - {}", action, number, title);

            return result;

        } catch (Exception e) {
            log.error("Failed to process Gitee merge request event", e);
            return WebhookEventResult.error("Failed to process merge request event: " + e.getMessage());
        }
    }

    /**
     * 处理Gitee Tag Push事件
     */
    private WebhookEventResult handleGiteeTagPushEvent(Map<String, Object> eventData) {
        try {
            String ref = (String) eventData.get("ref");
            String repositoryUrl = getGiteeRepositoryUrl(eventData);

            String tag = extractTagName(ref);

            @SuppressWarnings("unchecked")
            Map<String, Object> pushData = (Map<String, Object>) eventData.get("push_data");
            String commitSha = (String) pushData.get("commit");

            WebhookEventResult result = new WebhookEventResult();
            result.setEventType("RELEASE");
            result.setTag(tag);
            result.setCommitSha(commitSha);
            result.setRepositoryUrl(repositoryUrl);
            result.setEventData(eventData);
            result.setStatus(WebhookEventResult.Status.PROCESSED);

            log.info("Processed Gitee tag push event - Tag: {}, Commit: {}", tag, commitSha.substring(0, 7));

            return result;

        } catch (Exception e) {
            log.error("Failed to process Gitee tag push event", e);
            return WebhookEventResult.error("Failed to process tag push event: " + e.getMessage());
        }
    }

    /**
     * 验证GitHub签名
     */
    private boolean validateSignature(String payload, String signature) {
        // TODO: 实现GitHub签名验证逻辑
        return true; // 临时返回true
    }

    /**
     * 验证GitLab Token
     */
    private boolean validateGitLabToken(String token) {
        // TODO: 实现GitLab Token验证逻辑
        return true; // 临时返回true
    }

    /**
     * 验证Gitee Token
     */
    private boolean validateGiteeToken(String token) {
        // TODO: 实现Gitee Token验证逻辑
        return true; // 临时返回true
    }

    /**
     * 提取分支名
     */
    private String extractBranchName(String ref) {
        if (ref.startsWith("refs/heads/")) {
            return ref.substring(11);
        }
        return ref;
    }

    /**
     * 提取标签名
     */
    private String extractTagName(String ref) {
        if (ref.startsWith("refs/tags/")) {
            return ref.substring(10);
        }
        return ref;
    }

    /**
     * 获取仓库URL
     */
    private String getRepositoryUrl(Map<String, Object> eventData) {
        @SuppressWarnings("unchecked")
        Map<String, Object> repository = (Map<String, Object>) eventData.get("repository");
        if (repository != null) {
            return (String) repository.get("html_url");
        }
        return null;
    }

    /**
     * 获取GitLab仓库URL
     */
    private String getGitLabRepositoryUrl(Map<String, Object> eventData) {
        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) eventData.get("project");
        if (project != null) {
            return (String) project.get("web_url");
        }
        return null;
    }

    /**
     * 获取Gitee仓库URL
     */
    private String getGiteeRepositoryUrl(Map<String, Object> eventData) {
        @SuppressWarnings("unchecked")
        Map<String, Object> repository = (Map<String, Object>) eventData.get("repository");
        if (repository != null) {
            return (String) repository.get("html_url");
        }
        return null;
    }

    /**
     * 获取提交作者
     */
    private String getCommitAuthor(Map<String, Object> commit) {
        @SuppressWarnings("unchecked")
        Map<String, Object> author = (Map<String, Object>) commit.get("author");
        if (author != null) {
            return (String) author.get("name");
        }
        return null;
    }

    /**
     * 获取GitLab提交作者
     */
    private String getGitLabCommitAuthor(Map<String, Object> commit) {
        @SuppressWarnings("unchecked")
        Map<String, Object> author = (Map<String, Object>) commit.get("author");
        if (author != null) {
            return (String) author.get("name");
        }
        return null;
    }

    /**
     * 获取Gitee提交作者
     */
    private String getGiteeCommitAuthor(Map<String, Object> commit) {
        @SuppressWarnings("unchecked")
        Map<String, Object> author = (Map<String, Object>) commit.get("author");
        if (author != null) {
            return (String) author.get("name");
        }
        return null;
    }

    /**
     * Webhook事件处理结果
     */
    public static class WebhookEventResult {
        private Status status;
        private String eventType;
        private String action;
        private String branch;
        private String tag;
        private String repositoryUrl;
        private String commitSha;
        private String commitMessage;
        private String commitAuthor;
        private String deliveryId;
        private Integer pullRequestNumber;
        private String pullRequestTitle;
        private String pullRequestState;
        private String sourceBranch;
        private String targetBranch;
        private String releaseName;
        private Boolean prerelease;
        private Map<String, Object> eventData;
        private String errorMessage;

        public static WebhookEventResult processed() {
            WebhookEventResult result = new WebhookEventResult();
            result.status = Status.PROCESSED;
            return result;
        }

        public static WebhookEventResult error(String errorMessage) {
            WebhookEventResult result = new WebhookEventResult();
            result.status = Status.ERROR;
            result.errorMessage = errorMessage;
            return result;
        }

        public static WebhookEventResult ignored(String message) {
            WebhookEventResult result = new WebhookEventResult();
            result.status = Status.IGNORED;
            result.errorMessage = message;
            return result;
        }

        // Getters and setters
        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
        public String getRepositoryUrl() { return repositoryUrl; }
        public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }
        public String getCommitSha() { return commitSha; }
        public void setCommitSha(String commitSha) { this.commitSha = commitSha; }
        public String getCommitMessage() { return commitMessage; }
        public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }
        public String getCommitAuthor() { return commitAuthor; }
        public void setCommitAuthor(String commitAuthor) { this.commitAuthor = commitAuthor; }
        public String getDeliveryId() { return deliveryId; }
        public void setDeliveryId(String deliveryId) { this.deliveryId = deliveryId; }
        public Integer getPullRequestNumber() { return pullRequestNumber; }
        public void setPullRequestNumber(Integer pullRequestNumber) { this.pullRequestNumber = pullRequestNumber; }
        public String getPullRequestTitle() { return pullRequestTitle; }
        public void setPullRequestTitle(String pullRequestTitle) { this.pullRequestTitle = pullRequestTitle; }
        public String getPullRequestState() { return pullRequestState; }
        public void setPullRequestState(String pullRequestState) { this.pullRequestState = pullRequestState; }
        public String getSourceBranch() { return sourceBranch; }
        public void setSourceBranch(String sourceBranch) { this.sourceBranch = sourceBranch; }
        public String getTargetBranch() { return targetBranch; }
        public void setTargetBranch(String targetBranch) { this.targetBranch = targetBranch; }
        public String getReleaseName() { return releaseName; }
        public void setReleaseName(String releaseName) { this.releaseName = releaseName; }
        public Boolean getPrerelease() { return prerelease; }
        public void setPrerelease(Boolean prerelease) { this.prerelease = prerelease; }
        public Map<String, Object> getEventData() { return eventData; }
        public void setEventData(Map<String, Object> eventData) { this.eventData = eventData; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public enum Status {
            PROCESSED,
            ERROR,
            IGNORED
        }
    }
}