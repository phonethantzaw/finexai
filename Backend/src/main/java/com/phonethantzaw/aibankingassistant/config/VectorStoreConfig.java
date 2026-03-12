package com.phonethantzaw.aibankingassistant.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

public class VectorStoreConfig {

    @Value("${spring.ai.vectorstore.pgvector.dimensions:768}")
    private int dimensions;

    @Value("${spring.ai.vectorstore.pgvector.distance-type:COSINE_DISTANCE}")
    private String distanceType;

    @Value("${spring.ai.vectorstore.pgvector.index-type:HNSW}")
    private String indexType;

    @Value("${spring.ai.vectorstore.pgvector.remove-existing-vector-store-table:false}")
    private boolean removeExistingVectorStoreTable;

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return org.springframework.ai.vectorstore.pgvector.PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(dimensions)
                .distanceType(PgVectorStore.PgDistanceType.valueOf(distanceType))
                .indexType(PgVectorStore.PgIndexType.valueOf(indexType))
                .removeExistingVectorStoreTable(removeExistingVectorStoreTable)
                .initializeSchema(true)
                .build();
    }
}

