package com.autotest.platform.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义导出Excel数据注解
 *
 * @author autotest
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Excel {
    /**
     * 导出到Excel中的名字
     */
    public String name() default "";

    /**
     * 日期格式, 如: yyyy-MM-dd
     */
    public String dateFormat() default "";

    /**
     * 读取内容转表达式 (如: 0=男,1=女,2=未知)
     */
    public String readConverterExp() default "";

    /**
     * 分隔符，读取字符串组内容
     */
    public String separator() default ",";

    /**
     * BigDecimal 精度 默认:-1(默认不开启BigDecimal格式化)
     */
    public int scale() default -1;

    /**
     * BigDecimal 舍入规则 默认:ROUND_HALF_EVEN
     */
    public int roundingMode() default 4;

    /**
     * 导出类型（0数字 1字符串）
     */
    public CellType cellType() default CellType.STRING;

    /**
     * 导出时在excel中每个列的高度 单位为字符
     */
    public double height() default 14;

    /**
     * 导出时在excel中每个列的宽 单位为字符
     */
    public double width() default 16;

    /**
     * 文字后缀,如% 90 变成90%
     */
    public String suffix() default "";

    /**
     * 当值为空时,字段的默认值
     */
    public String defaultValue() default "";

    /**
     * 提示信息
     */
    public String prompt() default "";

    /**
     * 设置只能选择不能输入的列内容
     */
    public String[] combo() default {};

    /**
     * 是否导出数据
     */
    public boolean isExport() default true;

    /**
     * 字段类型（0：导出导入；1：仅导出；2：仅导入）
     */
    public Type type() default Type.ALL;

    public enum Type {
        ALL(0), EXPORT(1), IMPORT(2);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    public enum CellType {
        NUMERIC(0), STRING(1), IMAGE(2), FORMULA(3);

        private final int value;

        CellType(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }
}