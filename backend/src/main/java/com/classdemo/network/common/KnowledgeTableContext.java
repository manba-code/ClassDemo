package com.classdemo.network.common;

/**
 * 动态知识点表名上下文（配合 MyBatis-Plus 分表路由）。
 */
public final class KnowledgeTableContext {

    private static final ThreadLocal<String> TABLE_NAME = new ThreadLocal<>();

    private KnowledgeTableContext() {
    }

    public static void set(String tableName) {
        TABLE_NAME.set(tableName);
    }

    public static String get() {
        return TABLE_NAME.get();
    }

    public static void clear() {
        TABLE_NAME.remove();
    }
}
