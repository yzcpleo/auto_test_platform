package com.autotest.platform.execution.framework;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 框架适配器管理器
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Component
public class FrameworkAdapterManager {

    @Autowired
    private List<TestFrameworkAdapter> adapters;

    private Map<String, TestFrameworkAdapter> adapterMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (TestFrameworkAdapter adapter : adapters) {
            registerAdapter(adapter);
        }
        log.info("FrameworkAdapterManager initialized with {} adapters", adapterMap.size());
    }

    /**
     * 注册适配器
     */
    public void registerAdapter(TestFrameworkAdapter adapter) {
        adapterMap.put(adapter.getFrameworkType(), adapter);
        log.info("Registered framework adapter: {} - {}", adapter.getFrameworkType(), adapter.getFrameworkName());
    }

    /**
     * 获取适配器
     */
    public TestFrameworkAdapter getAdapter(String frameworkType) {
        return adapterMap.get(frameworkType);
    }

    /**
     * 根据用例类型获取适配器
     */
    public TestFrameworkAdapter getAdapterByCaseType(String caseType) {
        for (TestFrameworkAdapter adapter : adapterMap.values()) {
            for (String supportedType : adapter.getSupportedCaseTypes()) {
                if (supportedType.equals(caseType)) {
                    return adapter;
                }
            }
        }
        return null;
    }

    /**
     * 获取所有适配器
     */
    public Map<String, TestFrameworkAdapter> getAllAdapters() {
        return new HashMap<>(adapterMap);
    }

    /**
     * 初始化所有适配器
     */
    public boolean initializeAllAdapters(Map<String, String> configs) {
        boolean allSuccess = true;
        for (Map.Entry<String, TestFrameworkAdapter> entry : adapterMap.entrySet()) {
            String frameworkType = entry.getKey();
            TestFrameworkAdapter adapter = entry.getValue();

            String config = configs.get(frameworkType);
            if (config == null) {
                config = adapter.getConfigTemplate();
            }

            boolean success = adapter.initialize(config);
            if (!success) {
                allSuccess = false;
                log.error("Failed to initialize adapter: {}", frameworkType);
            } else {
                log.info("Successfully initialized adapter: {}", frameworkType);
            }
        }
        return allSuccess;
    }

    /**
     * 清理所有适配器
     */
    public void cleanupAllAdapters() {
        for (TestFrameworkAdapter adapter : adapterMap.values()) {
            try {
                adapter.cleanup();
            } catch (Exception e) {
                log.error("Error cleaning up adapter: " + adapter.getFrameworkType(), e);
            }
        }
    }

    /**
     * 检查用例类型是否支持
     */
    public boolean isCaseTypeSupported(String caseType) {
        return getAdapterByCaseType(caseType) != null;
    }

    /**
     * 获取支持的用例类型
     */
    public String[] getSupportedCaseTypes() {
        return adapterMap.values().stream()
                .flatMap(adapter -> java.util.Arrays.stream(adapter.getSupportedCaseTypes()))
                .distinct()
                .toArray(String[]::new);
    }
}