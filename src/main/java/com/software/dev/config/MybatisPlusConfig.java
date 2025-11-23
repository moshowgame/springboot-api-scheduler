package com.software.dev.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Configuration
@Import(MybatisPlusAutoConfiguration.class)
public class MybatisPlusConfig {
    
    /**
     * 为Quartz提供名为'dataSource'的数据源Bean
     * 解决 Quartz 报错: There is no DataSource named 'dataSource'
     */
    @Bean("dataSource")
    public DataSource quartzDataSource(DataSource dataSource) {
        return dataSource;
    }
}