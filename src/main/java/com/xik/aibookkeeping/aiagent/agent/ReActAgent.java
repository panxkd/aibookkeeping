package com.xik.aibookkeeping.aiagent.agent;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ReAct (Reason推理 And Acting行动) 模式的代理抽象类
 */
@EqualsAndHashCode(callSuper = true)   //于自动生成 Java 类的 equals() 和 hashCode() 方法
@Data
public abstract class ReActAgent extends BaseAgent{

    /**
     * 处理当前状态并决定下一步行动
     * @return 是否需要执行行动 true需要执行 false不需要
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     * @return 行动执行的结果
     */
    public abstract String act();

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成 - 无需行动";
            }
            return act();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "步骤执行失败：" + ex.getMessage();
        }
    }
}
