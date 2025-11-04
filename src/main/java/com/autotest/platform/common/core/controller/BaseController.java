package com.autotest.platform.common.core.controller;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import com.github.pagehelper.PageInfo;
import com.autotest.platform.common.core.domain.AjaxResult;
import com.autotest.platform.common.core.page.TableDataInfo;
import com.autotest.platform.common.utils.DateUtils;
import com.autotest.platform.common.utils.StringUtils;
import com.autotest.platform.common.utils.security.SecurityUtils;
import com.autotest.platform.common.utils.sql.SqlUtil;

/**
 * web层通用数据处理
 *
 * @author autotest
 */
public class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 将前台传递过来的日期格式的字符串，自动转化为Date类型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * 设置请求分页数据
     */
    protected void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if (StringUtils.isNotNull(pageNum) && StringUtils.isNotNull(pageSize)) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.startPage(pageNum, pageSize, orderBy);
        }
    }

    /**
     * 设置请求排序数据
     */
    protected void startOrderBy() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        if (StringUtils.isNotEmpty(orderBy)) {
            String orderBySql = " order by " + orderBy;
            PageHelper.orderBy(orderBySql);
        }
    }

    /**
     * 响应请求分页数据
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TableDataInfo getDataTable(List<?> list) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

    /**
     * 返回成功
     */
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * 返回成功消息
     */
    public AjaxResult success(String message) {
        return AjaxResult.success(message);
    }

    /**
     * 返回成功消息
     */
    public AjaxResult success(Object data) {
        return AjaxResult.success(data);
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error(String message) {
        return AjaxResult.error(message);
    }

    /**
     * 返回警告消息
     */
    public AjaxResult warn(String message) {
        return AjaxResult.warn(message);
    }

    /**
     * 响应返回结果
     */
    public AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 响应返回结果
     */
    public AjaxResult toAjax(int rows, String msg) {
        return rows > 0 ? AjaxResult.success(msg) : AjaxResult.error(msg);
    }

    /**
     * 响应返回结果
     */
    public AjaxResult toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * 响应返回结果
     */
    public AjaxResult toAjax(boolean result, String msg) {
        return result ? success(msg) : error(msg);
    }

    /**
     * 页面跳转
     */
    public String redirect(String url) {
        return StringUtils.format("redirect:{}", url);
    }

    /**
     * 获取用户cache信息
     */
    public LoginUser getLoginUser() {
        return SecurityUtils.getLoginUser();
    }

    /**
     * 获取登录用户id
     */
    public Long getUserId() {
        return getLoginUser().getUserId();
    }

    /**
     * 获取登录用户名
     */
    public String getUsername() {
        return getLoginUser().getUsername();
    }

    /**
     * 获取登录用户部门
     */
    public Long getDeptId() {
        return getLoginUser().getDeptId();
    }
}