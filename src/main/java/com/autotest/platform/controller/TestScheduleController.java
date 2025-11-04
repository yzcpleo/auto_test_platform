package com.autotest.platform.controller;

import com.autotest.platform.common.core.controller.BaseController;
import com.autotest.platform.common.core.domain.AjaxResult;
import com.autotest.platform.common.core.page.TableDataInfo;
import com.autotest.platform.domain.testcase.TestSchedule;
import com.autotest.platform.service.ITestScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 测试调度Controller
 *
 * @author autotest
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/test/schedule")
public class TestScheduleController extends BaseController {

    @Autowired
    private ITestScheduleService testScheduleService;

    /**
     * 查询测试调度列表
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:list')")
    @GetMapping("/list")
    public TableDataInfo list(TestSchedule testSchedule) {
        startPage();
        List<TestSchedule> list = testScheduleService.selectTestScheduleList(testSchedule);
        return getDataTable(list);
    }

    /**
     * 获取测试调度详细信息
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:query')")
    @GetMapping(value = "/{scheduleId}")
    public AjaxResult getInfo(@PathVariable("scheduleId") Long scheduleId) {
        return success(testScheduleService.selectTestScheduleByScheduleId(scheduleId));
    }

    /**
     * 新增测试调度
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:add')")
    @PostMapping
    public AjaxResult add(@RequestBody TestSchedule testSchedule) {
        // 检查调度名称是否唯一
        if (!testScheduleService.checkScheduleNameUnique(testSchedule)) {
            return error("Schedule name already exists");
        }

        testSchedule.setCreateBy(getUsername());
        return toAjax(testScheduleService.insertTestSchedule(testSchedule));
    }

    /**
     * 修改测试调度
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody TestSchedule testSchedule) {
        // 检查调度名称是否唯一
        if (!testScheduleService.checkScheduleNameUnique(testSchedule)) {
            return error("Schedule name already exists");
        }

        testSchedule.setUpdateBy(getUsername());
        return toAjax(testScheduleService.updateTestSchedule(testSchedule));
    }

    /**
     * 删除测试调度
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:remove')")
    @DeleteMapping("/{scheduleIds}")
    public AjaxResult remove(@PathVariable Long[] scheduleIds) {
        return toAjax(testScheduleService.deleteTestScheduleByScheduleIds(scheduleIds));
    }

    /**
     * 启用调度
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:enable')")
    @PostMapping("/enable")
    public AjaxResult enableSchedule(@RequestBody Long[] scheduleIds) {
        return toAjax(testScheduleService.enableSchedule(scheduleIds));
    }

    /**
     * 禁用调度
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:disable')")
    @PostMapping("/disable")
    public AjaxResult disableSchedule(@RequestBody Long[] scheduleIds) {
        return toAjax(testScheduleService.disableSchedule(scheduleIds));
    }

    /**
     * 手动触发调度
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:trigger')")
    @PostMapping("/{scheduleId}/trigger")
    public AjaxResult triggerSchedule(@PathVariable Long scheduleId) {
        Map<String, Object> result = testScheduleService.triggerSchedule(scheduleId, getUsername());
        if ((Boolean) result.get("success")) {
            return success(result.get("message"), result);
        } else {
            return error(result.get("message").toString());
        }
    }

    /**
     * 暂停调度
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:pause')")
    @PostMapping("/{scheduleId}/pause")
    public AjaxResult pauseSchedule(@PathVariable Long scheduleId) {
        return toAjax(testScheduleService.pauseSchedule(scheduleId));
    }

    /**
     * 恢复调度
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:resume')")
    @PostMapping("/{scheduleId}/resume")
    public AjaxResult resumeSchedule(@PathVariable Long scheduleId) {
        return toAjax(testScheduleService.resumeSchedule(scheduleId));
    }

    /**
     * 获取即将执行的调度
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:pending')")
    @GetMapping("/pending")
    public AjaxResult getPendingSchedules() {
        List<TestSchedule> schedules = testScheduleService.selectPendingSchedules();
        return success(schedules);
    }

    /**
     * 验证调度配置
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:validate')")
    @PostMapping("/validate")
    public AjaxResult validateSchedule(@RequestBody TestSchedule testSchedule) {
        Map<String, Object> result = testScheduleService.validateScheduleConfig(testSchedule);
        return success(result);
    }

    /**
     * 获取调度执行历史
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:history')")
    @GetMapping("/history/{scheduleId}")
    public AjaxResult getScheduleHistory(@PathVariable Long scheduleId,
                                        @RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> history = testScheduleService.getScheduleHistory(scheduleId, days);
        return success(history);
    }

    /**
     * 计算下次执行时间
     */
    @PreAuthorize("@ss.hasPermi('test:schedule:calculate')")
    @PostMapping("/calculate")
    public AjaxResult calculateNextTime(@RequestBody TestSchedule testSchedule) {
        String nextTime = testScheduleService.calculateNextExecuteTime(testSchedule);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("nextExecuteTime", nextTime);
        return success(result);
    }
}