package com.xik.aibookkeeping.aiagent.rag;



import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 自定义错误处理逻辑
 */
public class ContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter createInstance() {
        PromptTemplate promptTemplate = new PromptTemplate("""
                你应该输出以下内容：
                抱歉，我只能处理记账和理财相关的功能，其他问题不能回答你哟~
                有问题可以联系作者：panxikai0513@163.com
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(promptTemplate)
                .build();
    }


}
