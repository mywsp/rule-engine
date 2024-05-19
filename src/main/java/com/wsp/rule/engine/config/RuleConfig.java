package com.wsp.rule.engine.config;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RuleConfig {
    /**
     * 规则id
     */
    private Long id;

    /**
     * 业务类型
     */
    private Integer bizType;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 城市id
     * 区分不同城市可以配置不同规则
     */
    private Integer cityId;

    /**
     * 规则执行列表
     */
    private List<RuleAction> ruleActionList;


    /**
     * 具体规则动作
     */
    @Data
    public static class RuleAction {
        /**
         * 事件码列表(事件驱动)
         */
        private List<String> eventCodeList;

        /**
         * 匹配成功执行动作列表
         */
        private List<Action> matchActionList;

        /**
         * 匹配失败执行动作列表
         */
        private List<Action> noMatchActionList;

        /**
         * 规则表达式
         */
        private String ruleExpress;
    }

    /**
     * 具体执行函数定义
     */
    @Data
    public static class Action implements Serializable,Comparable<Action> {
        /**
         * 服务接口名
         */
        private String serviceName;

        /**
         * 方法名
         */
        private String methodName;

        /**
         * 参数列表
         */
        private String params;

        /**
         * 优先级
         */
        private Integer order;

        @Override
        public int compareTo(Action o) {
            return this.getOrder() - o.getOrder();
        }
    }
}
