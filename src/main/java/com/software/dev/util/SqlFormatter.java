package com.software.dev.util;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL格式化工具类
 */
public class SqlFormatter {

    private static final Pattern pattern = Pattern.compile("\\?");

    /**
     * 格式化SQL语句，将参数替换到SQL中
     */
    public static String formatSql(String sql, Object parameterObject) {
        if (parameterObject == null) {
            return sql;
        }

        // 获取参数的MetaObject
        MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
        
        // 处理不同类型的参数
        if (parameterObject instanceof MapperMethod.ParamMap) {
            // MyBatis参数Map
            return formatSqlWithParamMap(sql, (MapperMethod.ParamMap<?>) parameterObject);
        } else if (parameterObject instanceof Map) {
            // 普通Map
            return formatSqlWithMap(sql, (Map<?, ?>) parameterObject);
        } else {
            // 单个参数
            return formatSqlWithSingleParam(sql, parameterObject);
        }
    }

    /**
     * 处理MyBatis ParamMap参数
     */
    private static String formatSqlWithParamMap(String sql, MapperMethod.ParamMap<?> paramMap) {
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        int paramIndex = 0;
        
        while (matcher.find()) {
            Object value = paramMap.get("param" + (++paramIndex));
            String paramValue = formatValue(value);
            matcher.appendReplacement(sb, paramValue);
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    /**
     * 处理普通Map参数
     */
    private static String formatSqlWithMap(String sql, Map<?, ?> paramMap) {
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        
        // 对于Map参数，我们无法确定参数顺序，所以显示参数信息
        matcher.appendTail(sb);
        
        String result = sb.toString();
        String paramInfo = "参数: " + paramMap.toString();
        
        return result + " | " + paramInfo;
    }

    /**
     * 处理单个参数
     */
    private static String formatSqlWithSingleParam(String sql, Object parameter) {
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String paramValue = formatValue(parameter);
            matcher.appendReplacement(sb, paramValue);
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    /**
     * 格式化参数值
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'";
        } else if (value instanceof java.util.Date) {
            return "'" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value) + "'";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return value.toString();
        } else {
            return "'" + value.toString().replace("'", "''") + "'";
        }
    }
}