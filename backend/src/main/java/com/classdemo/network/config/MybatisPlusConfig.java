package com.classdemo.network.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.classdemo.network.common.KnowledgeTableContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：五层知识点动态分表路由（BE-06）。
 */
@Configuration
public class MybatisPlusConfig {

    private static final String KNOWLEDGE_LOGICAL_TABLE = "knowledge_point";

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        DynamicTableNameInnerInterceptor dynamicTable = new DynamicTableNameInnerInterceptor();
        dynamicTable.setTableNameHandler((sql, tableName) -> {
            if (KNOWLEDGE_LOGICAL_TABLE.equals(tableName)) {
                String actual = KnowledgeTableContext.get();
                return actual != null ? actual : tableName;
            }
            return tableName;
        });
        interceptor.addInnerInterceptor(dynamicTable);
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());

        return interceptor;
    }
}
