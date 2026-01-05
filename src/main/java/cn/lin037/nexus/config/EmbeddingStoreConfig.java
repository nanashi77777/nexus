package cn.lin037.nexus.config;

import cn.lin037.nexus.infrastructure.common.ai.model.dto.PgVectorConfig;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 嵌入存储配置
 *
 * @author LinSanQi
 */
@Configuration
@RequiredArgsConstructor
public class EmbeddingStoreConfig {
    private final PgVectorConfig pgVectorConfig;

    @Bean
    public Map<Integer, EmbeddingStore<TextSegment>> embeddingStores() {
        Map<Integer, EmbeddingStore<TextSegment>> stores = new HashMap<>();

        if (pgVectorConfig.getTables() == null) {
            return stores;
        }

        pgVectorConfig.getTables().forEach((dimension, tableConfig) -> {
            EmbeddingStore<TextSegment> store = PgVectorEmbeddingStore.builder()
                    .host(pgVectorConfig.getHost())
                    .port(pgVectorConfig.getPort())
                    .user(pgVectorConfig.getUsername())
                    .password(pgVectorConfig.getPassword())
                    .database(pgVectorConfig.getDatabase())
                    .table(tableConfig.getName())
                    .dimension(dimension)
                    .useIndex(tableConfig.isUseIndex())
                    .indexListSize(tableConfig.getIndexListSize())
                    .createTable(tableConfig.isCreateTable())
                    .dropTableFirst(tableConfig.isDropTableFirst())
                    .build();
            stores.put(dimension, store);
        });

        return stores;
    }
} 