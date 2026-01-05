package cn.lin037.nexus.infrastructure.common.ai.model.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * PGVector 嵌入存储的配置属性
 *
 * @author LinSanQi
 */
@Data
@Component
@ConfigurationProperties(prefix = "langchain4j.pgvector")
public class PgVectorConfig {

    /**
     * PostgreSQL 数据库主机地址
     */
    private String host;

    /**
     * PostgreSQL 数据库端口
     */
    private Integer port;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    /**
     * 数据库名称
     */
    private String database;

    /**
     * 维度到表配置的映射
     * Key: 向量维度 (e.g., 1536)
     * Value: 该维度对应的表配置
     */
    private Map<Integer, TableConfig> tables;

    @Data
    public static class TableConfig {
        /**
         * 向量存储的表名
         */
        private String name;

        /**
         * 是否使用 IVFFlat 索引以加速搜索
         */
        private boolean useIndex = false;

        /**
         * IVFFlat 索引的列表大小
         */
        private int indexListSize = 1000;

        /**
         * 是否在启动时自动创建表（如果不存在）
         */
        private boolean createTable = true;

        /**
         * 是否在启动时先删除表（如果已存在）
         */
        private boolean dropTableFirst = false;
    }
} 