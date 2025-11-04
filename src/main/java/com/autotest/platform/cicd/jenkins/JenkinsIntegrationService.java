package com.autotest.platform.cicd.jenkins;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jenkins集成服务
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Service
public class JenkinsIntegrationService {

    @Autowired
    private ObjectMapper objectMapper;

    private String jenkinsUrl;
    private String jenkinsUsername;
    private String jenkinsApiToken;

    /**
     * 初始化Jenkins配置
     */
    public void initialize(String jenkinsUrl, String username, String apiToken) {
        this.jenkinsUrl = jenkinsUrl;
        this.jenkinsUsername = username;
        this.jenkinsApiToken = apiToken;
        log.info("Jenkins integration initialized - URL: {}", jenkinsUrl);
    }

    /**
     * 触发Jenkins构建
     */
    public JenkinsBuildResult triggerBuild(String jobName, Map<String, Object> parameters) {
        try {
            log.info("Triggering Jenkins build for job: {}", jobName);

            // 构建Jenkins API URL
            String apiUrl = jenkinsUrl + "/job/" + jobName + "/build";

            // 准备请求参数
            Map<String, Object> requestParams = new HashMap<>();
            if (parameters != null && !parameters.isEmpty()) {
                requestParams.putAll(parameters);
            }

            // 发送构建请求
            String response = sendJenkinsRequest("POST", apiUrl, requestParams);

            // 解析响应
            JenkinsBuildResult result = parseBuildResponse(response);

            log.info("Jenkins build triggered successfully - Job: {}, Build #: {}",
                    jobName, result.getBuildNumber());

            return result;

        } catch (Exception e) {
            log.error("Failed to trigger Jenkins build for job: " + jobName, e);
            return JenkinsBuildResult.error("Failed to trigger build: " + e.getMessage());
        }
    }

    /**
     * 获取构建状态
     */
    public JenkinsBuildStatus getBuildStatus(String jobName, Integer buildNumber) {
        try {
            log.debug("Getting Jenkins build status - Job: {}, Build: {}", jobName, buildNumber);

            String apiUrl = jenkinsUrl + "/job/" + jobName + "/" + buildNumber + "/api/json";

            String response = sendJenkinsRequest("GET", apiUrl, null);

            return parseBuildStatus(response);

        } catch (Exception e) {
            log.error("Failed to get Jenkins build status", e);
            return JenkinsBuildStatus.error("Failed to get build status: " + e.getMessage());
        }
    }

    /**
     * 获取构建日志
     */
    public String getBuildLog(String jobName, Integer buildNumber) {
        try {
            log.debug("Getting Jenkins build log - Job: {}, Build: {}", jobName, buildNumber);

            String apiUrl = jenkinsUrl + "/job/" + jobName + "/" + buildNumber + "/consoleText";

            return sendJenkinsRequest("GET", apiUrl, null);

        } catch (Exception e) {
            log.error("Failed to get Jenkins build log", e);
            return "Failed to get build log: " + e.getMessage();
        }
    }

    /**
     * 停止构建
     */
    public boolean stopBuild(String jobName, Integer buildNumber) {
        try {
            log.info("Stopping Jenkins build - Job: {}, Build: {}", jobName, buildNumber);

            String apiUrl = jenkinsUrl + "/job/" + jobName + "/" + buildNumber + "/stop";

            String response = sendJenkinsRequest("POST", apiUrl, null);

            return response != null && !response.isEmpty();

        } catch (Exception e) {
            log.error("Failed to stop Jenkins build", e);
            return false;
        }
    }

    /**
     * 创建Jenkins Job
     */
    public boolean createJob(String jobName, String jobConfigXml) {
        try {
            log.info("Creating Jenkins job: {}", jobName);

            String apiUrl = jenkinsUrl + "/createItem?name=" + jobName;

            Map<String, Object> params = new HashMap<>();
            params.put("name", jobName);
            params.put("mode", "hudson.model.FreeStyleProject");
            params.put("from", "");
            params.put("Submit", "OK");
            params.put("json", "{\"name\":\"" + jobName + "\",\"mode\":\"hudson.model.FreeStyleProject\"}");

            String response = sendJenkinsRequestWithXml("POST", apiUrl, jobConfigXml, params);

            return response != null && response.contains("success");

        } catch (Exception e) {
            log.error("Failed to create Jenkins job: " + jobName, e);
            return false;
        }
    }

    /**
     * 更新Jenkins Job配置
     */
    public boolean updateJobConfig(String jobName, String jobConfigXml) {
        try {
            log.info("Updating Jenkins job config: {}", jobName);

            String apiUrl = jenkinsUrl + "/job/" + jobName + "/config.xml";

            String response = sendJenkinsRequestWithXml("POST", apiUrl, jobConfigXml, null);

            return response != null && response.isEmpty();

        } catch (Exception e) {
            log.error("Failed to update Jenkins job config: " + jobName, e);
            return false;
        }
    }

    /**
     * 获取Job列表
     */
    public List<JenkinsJob> getJobList() {
        try {
            log.debug("Getting Jenkins job list");

            String apiUrl = jenkinsUrl + "/api/json?tree=jobs[name,url,color,lastBuild[number,result,url,timestamp]";

            String response = sendJenkinsRequest("GET", apiUrl, null);

            return parseJobList(response);

        } catch (Exception e) {
            log.error("Failed to get Jenkins job list", e);
            return List.of();
        }
    }

    /**
     * 获取Job信息
     */
    public JenkinsJob getJobInfo(String jobName) {
        try {
            log.debug("Getting Jenkins job info: {}", jobName);

            String apiUrl = jenkinsUrl + "/job/" + jobName + "/api/json";

            String response = sendJenkinsRequest("GET", apiUrl, null);

            return parseJobInfo(response);

        } catch (Exception e) {
            log.error("Failed to get Jenkins job info: " + jobName, e);
            return null;
        }
    }

    /**
     * 删除Jenkins Job
     */
    public boolean deleteJob(String jobName) {
        try {
            log.info("Deleting Jenkins job: {}", jobName);

            String apiUrl = jenkinsUrl + "/job/" + jobName + "/doDelete";

            String response = sendJenkinsRequest("POST", apiUrl, null);

            return response != null && response.isEmpty();

        } catch (Exception e) {
            log.error("Failed to delete Jenkins job: " + jobName, e);
            return false;
        }
    }

    /**
     * 测试Jenkins连接
     */
    public JenkinsConnectionStatus testConnection() {
        try {
            log.info("Testing Jenkins connection to: {}", jenkinsUrl);

            String apiUrl = jenkinsUrl + "/api/json";

            String response = sendJenkinsRequest("GET", apiUrl, null);

            if (response != null && response.contains("hudson")) {
                return JenkinsConnectionStatus.connected();
            } else {
                return JenkinsConnectionStatus.error("Invalid response from Jenkins");
            }

        } catch (Exception e) {
            log.error("Failed to test Jenkins connection", e);
            return JenkinsConnectionStatus.error("Connection failed: " + e.getMessage());
        }
    }

    /**
     * 发送Jenkins API请求
     */
    private String sendJenkinsRequest(String method, String url, Map<String, Object> params) {
        // TODO: 实现HTTP请求逻辑
        // 这里应该使用HttpClient或其他HTTP客户端发送请求
        // 临时返回空字符串
        return "";
    }

    /**
     * 发送带XML的Jenkins请求
     */
    private String sendJenkinsRequestWithXml(String method, String url, String xmlContent, Map<String, Object> params) {
        // TODO: 实现带XML内容的HTTP请求
        return "";
    }

    /**
     * 解析构建响应
     */
    private JenkinsBuildResult parseBuildResponse(String response) {
        try {
            JenkinsBuildResult result = new JenkinsBuildResult();

            // 从响应中提取队列ID和构建URL
            if (response.contains("QueueTask")) {
                // 解析队列ID
                int queueIdStart = response.indexOf("id\":") + 4;
                int queueIdEnd = response.indexOf(",", queueIdStart);
                if (queueIdEnd > queueIdStart) {
                    String queueId = response.substring(queueIdStart, queueIdEnd).trim();
                    result.setQueueId(Integer.parseInt(queueId));
                }
            }

            // 构建构建URL
            String buildUrl = jenkinsUrl + "/job/";
            result.setBuildUrl(buildUrl);
            result.setStatus("QUEUED");

            return result;

        } catch (Exception e) {
            log.error("Failed to parse build response", e);
            return JenkinsBuildResult.error("Failed to parse response: " + e.getMessage());
        }
    }

    /**
     * 解析构建状态
     */
    private JenkinsBuildStatus parseBuildStatus(String response) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonData = objectMapper.readValue(response, Map.class);

            JenkinsBuildStatus status = new JenkinsBuildStatus();
            status.setNumber((Integer) jsonData.get("number"));
            status.setResult((String) jsonData.get("result"));
            status.setBuilding((Boolean) jsonData.get("building"));
            status.setTimestamp((Long) jsonData.get("timestamp"));
            status.setDuration((Integer) jsonData.get("duration"));

            // 解析构建结果
            String result = status.getResult();
            if (result != null) {
                switch (result) {
                    case "SUCCESS":
                        status.setStatus("SUCCESS");
                        break;
                    case "FAILURE":
                        status.setStatus("FAILED");
                        break;
                    case "ABORTED":
                        status.setStatus("ABORTED");
                        break;
                    case "UNSTABLE":
                        status.setStatus("UNSTABLE");
                        break;
                    default:
                        status.setStatus("UNKNOWN");
                }
            }

            return status;

        } catch (Exception e) {
            log.error("Failed to parse build status", e);
            return JenkinsBuildStatus.error("Failed to parse status: " + e.getMessage());
        }
    }

    /**
     * 解析Job列表
     */
    private List<JenkinsJob> parseJobList(String response) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonData = objectMapper.readValue(response, Map.class);

            List<JenkinsJob> jobs = new java.util.ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> jobData = (List<Map<String, Object>>) jsonData.get("jobs");

            for (Map<String, Object> job : jobData) {
                JenkinsJob jenkinsJob = new JenkinsJob();
                jenkinsJob.setName((String) job.get("name"));
                jenkinsJob.setUrl((String) job.get("url"));
                jenkinsJob.setColor((String) job.get("color"));

                // 解析最后构建信息
                @SuppressWarnings("unchecked")
                Map<String, Object> lastBuild = (Map<String, Object>) job.get("lastBuild");
                if (lastBuild != null) {
                    jenkinsJob.setLastBuildNumber((Integer) lastBuild.get("number"));
                    jenkinsJob.setLastBuildResult((String) lastBuild.get("result"));
                    jenkinsJob.setLastBuildUrl((String) lastBuild.get("url"));
                    jenkinsJob.setLastBuildTimestamp((Long) lastBuild.get("timestamp"));
                }

                jobs.add(jenkinsJob);
            }

            return jobs;

        } catch (Exception e) {
            log.error("Failed to parse job list", e);
            return List.of();
        }
    }

    /**
     * 解析Job信息
     */
    private JenkinsJob parseJobInfo(String response) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonData = objectMapper.readValue(response, Map.class);

            JenkinsJob job = new JenkinsJob();
            job.setName((String) jsonData.get("name"));
            job.setUrl((String) jsonData.get("url"));
            job.setDescription((String) jsonData.get("description"));
            job.setDisplayName((String) jsonData.get("displayName"));

            // 解析最后构建信息
            @SuppressWarnings("unchecked")
            Map<String, Object> lastBuild = (Map<String, Object>) jsonData.get("lastBuild");
            if (lastBuild != null) {
                job.setLastBuildNumber((Integer) lastBuild.get("number"));
                job.setLastBuildResult((String) lastBuild.get("result"));
                job.setLastBuildUrl((String) lastBuild.get("url"));
                job.setLastBuildTimestamp((Long) lastBuild.get("timestamp"));
            }

            return job;

        } catch (Exception e) {
            log.error("Failed to parse job info", e);
            return null;
        }
    }

    /**
     * Jenkins构建结果
     */
    public static class JenkinsBuildResult {
        private String status;
        private Integer queueId;
        private Integer buildNumber;
        private String buildUrl;
        private String errorMessage;

        public static JenkinsBuildResult queued() {
            JenkinsBuildResult result = new JenkinsBuildResult();
            result.setStatus("QUEUED");
            return result;
        }

        public static JenkinsBuildResult started() {
            JenkinsBuildResult result = new JenkinsBuildResult();
            result.setStatus("STARTED");
            return result;
        }

        public static JenkinsBuildResult completed() {
            JenkinsBuildResult result = new JenkinsBuildResult();
            result.setStatus("COMPLETED");
            return result;
        }

        public static JenkinsBuildResult error(String errorMessage) {
            JenkinsBuildResult result = new JenkinsBuildResult();
            result.setStatus("ERROR");
            result.setErrorMessage(errorMessage);
            return result;
        }

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getQueueId() { return queueId; }
        public void setQueueId(Integer queueId) { this.queueId = queueId; }
        public Integer getBuildNumber() { return buildNumber; }
        public void setBuildNumber(Integer buildNumber) { this.buildNumber = buildNumber; }
        public String getBuildUrl() { return buildUrl; }
        public void setBuildUrl(String buildUrl) { this.buildUrl = buildUrl; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * Jenkins构建状态
     */
    public static class JenkinsBuildStatus {
        private String status;
        private Integer number;
        private String result;
        private Boolean building;
        private Long timestamp;
        private Integer duration;
        private String errorMessage;

        public static JenkinsBuildStatus success() {
            JenkinsBuildStatus status = new JenkinsBuildStatus();
            status.setStatus("SUCCESS");
            return status;
        }

        public static JenkinsBuildStatus failed() {
            JenkinsBuildStatus status = new JenkinsBuildStatus();
            status.setStatus("FAILED");
            return status;
        }

        public static JenkinsBuildStatus building() {
            JenkinsBuildStatus status = new JenkinsBuildStatus();
            status.setStatus("BUILDING");
            return status;
        }

        public static JenkinsBuildStatus error(String errorMessage) {
            JenkinsBuildStatus status = new JenkinsBuildStatus();
            status.setStatus("ERROR");
            status.setErrorMessage(errorMessage);
            return status;
        }

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getNumber() { return number; }
        public void setNumber(Integer number) { this.number = number; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public Boolean getBuilding() { return building; }
        public void setBuilding(Boolean building) { this.building = building; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * Jenkins Job信息
     */
    public static class JenkinsJob {
        private String name;
        private String displayName;
        private String url;
        private String description;
        private String color;
        private Integer lastBuildNumber;
        private String lastBuildResult;
        private String lastBuildUrl;
        private Long lastBuildTimestamp;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public Integer getLastBuildNumber() { return lastBuildNumber; }
        public void setLastBuildNumber(Integer lastBuildNumber) { this.lastBuildNumber = lastBuildNumber; }
        public String getLastBuildResult() { return lastBuildResult; }
        public void setLastBuildResult(String lastBuildResult) { this.lastBuildResult = lastBuildResult; }
        public String getLastBuildUrl() { return lastBuildUrl; }
        public void setLastBuildUrl(String lastBuildUrl) { this.lastBuildUrl = lastBuildUrl; }
        public Long getLastBuildTimestamp() { return lastBuildTimestamp; }
        public void setLastBuildTimestamp(Long lastBuildTimestamp) { this.lastBuildTimestamp = lastBuildTimestamp; }
    }

    /**
     * Jenkins连接状态
     */
    public static class JenkinsConnectionStatus {
        private boolean connected;
        private String message;
        private String version;

        public static JenkinsConnectionStatus connected() {
            JenkinsConnectionStatus status = new JenkinsConnectionStatus();
            status.setConnected(true);
            status.setMessage("Connection successful");
            return status;
        }

        public static JenkinsConnectionStatus error(String message) {
            JenkinsConnectionStatus status = new JenkinsConnectionStatus();
            status.setConnected(false);
            status.setMessage(message);
            return status;
        }

        // Getters and setters
        public boolean isConnected() { return connected; }
        public void setConnected(boolean connected) { this.connected = connected; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }
}