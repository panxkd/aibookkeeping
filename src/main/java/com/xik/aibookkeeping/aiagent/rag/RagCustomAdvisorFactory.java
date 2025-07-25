package com.xik.aibookkeeping.aiagent.rag;


import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建自定义的 RAG 检索增强顾问工厂
 */
public class RagCustomAdvisorFactory {

    /**
     * 创建自定义的 RAG 检索增强顾问
     * @param vectorStore
     * @param category
     * @return
     */
    public static Advisor createRagCustomAdvisor(VectorStore vectorStore, String category) {
        // 过滤特定状态的文档 可自定义
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("category", category)
                .build();
        VectorStoreDocumentRetriever vectorStoreDocumentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)   //过滤条件
                .similarityThreshold(0.5)       // 相似度阈值
                .topK(3)     // 返回文档数量
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(vectorStoreDocumentRetriever)
                .queryAugmenter(ContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
