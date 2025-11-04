package com.autotest.platform.common.core.page;

import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 *
 * @author autotest
 */
public class TableDataInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 列表数据 */
    private List<?> rows;

    /** 消息状态码 */
    private int code;

    /** 消息内容 */
    private String msg;

    /**
     * 表格数据对象
     */
    public TableDataInfo() {
    }

    /**
     * 分页
     *
     * @param total  总记录数
     * @param rows   列表数据
     * @param code   状态码
     * @param msg    消息
     */
    public TableDataInfo(long total, List<?> rows, int code, String msg) {
        this.total = total;
        this.rows = rows;
        this.code = code;
        this.msg = msg;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}