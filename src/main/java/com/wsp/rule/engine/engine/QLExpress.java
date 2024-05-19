package com.wsp.rule.engine.engine;


import cn.hutool.json.JSONUtil;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.instruction.op.OperatorBase;
import com.ql.util.express.instruction.op.OperatorSelfDefineServiceFunction;

import com.wsp.rule.engine.config.RuleConfig;
import com.wsp.rule.engine.operator.ContainsOperator;
import com.wsp.rule.engine.param.ParamObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QLExpress implements InitializingBean {
    private static ExpressRunner runner = new ExpressRunner();
    private static Map<String, Field> fieldMap    = new HashMap<>();
    private static ThreadLocal<DefaultContext<String, Object>> contextValueThreadLocal = new ThreadLocal();
    private  Map<Integer,List<RuleConfig>> ruleConfigMap = new HashMap<>();
    @Override
    public void afterPropertiesSet() throws Exception {
        //参数字段解析
        Field[] fields = ParamObject.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            fieldMap.put(field.getName(), field);
        }

        //初始化自定义操作符
        runner.addOperator("contains", new ContainsOperator());

        //假设初始化规则配置
        RuleConfig ruleConfig = new RuleConfig();
        ruleConfig.setId(1L);
        ruleConfig.setRuleName("下单规则");
        ruleConfig.setBizType(1001); //1001 订单中心
        ruleConfig.setCityId(1);

        List<RuleConfig.RuleAction> ruleActionList = new ArrayList<>();
        //规则详情
        RuleConfig.RuleAction ruleAction1 = new RuleConfig.RuleAction();
        ruleAction1.setEventCodeList(Arrays.asList("2001", "2002"));
        ruleAction1.setMatchActionList(buildMatchActionList());
        ruleAction1.setRuleExpress("(avg#{this}*3+#{addValue}) contains 9.5");
        ruleActionList.add(ruleAction1);

        ruleConfig.setRuleActionList(ruleActionList);

        ruleConfigMap.put(ruleConfig.getCityId(), Collections.singletonList(ruleConfig));

    }

    private List<RuleConfig.Action> buildMatchActionList() {
        List<RuleConfig.Action> actionList1 = new ArrayList<>();
        //也可通过Spring映射加载类信息
        RuleConfig.Action action = new RuleConfig.Action();
        action.setServiceName("com.wsp.rule.engine.action.OrderAction");
        action.setMethodName("createOrderAction");
        action.setParams("{\"name\":123}");
        action.setOrder(1);
        actionList1.add(action);
        return actionList1;
    }

    /**
     * 初始化预加载自定义函数
     */
    public static void loadFunction(Object bean, Method method, String functionId) {
        try {
            if (method == null) {
                return;
            }


            OperatorBase operatorBase = new OperatorSelfDefineServiceFunction(functionId, bean, method.getName(),
                    method.getParameterTypes(), (String[]) null, (String[]) null, null);
            if (runner.getFunciton(functionId) != null) {
                runner.replaceOperator(functionId, operatorBase);
            } else {
                runner.addFunction(functionId, operatorBase);
            }
        } catch (Exception e) {
           log.error("QLExecutor#loadFunction error for function {}" , functionId);
        }
    }

    public Object execExpress(String express, DefaultContext<String, Object> context, ParamObject paramObject) {
        Object result = null;
        try {
            //从规则中抽取字段并组装
            List<String> fieldList = getFieldList(express);
            for (String fieldName : fieldList) {
                if("this".equals(fieldName)){
                    context.put("this", paramObject);
                }else {
                    Field field = fieldMap.get(fieldName);
                    if(!Objects.isNull(field)){
                        context.put(fieldName, field.get(paramObject));
                    }
                }

            }
            //替换express特殊字符#{}
            express = express.replaceAll("#\\{","(").replace("}",")");
            result = runner.execute(express, context, null, false, true);
            log.info("执行函数返回详情:{}",getContextValue());
        } catch (Exception e) {
            log.info("执行异常:{}", e.getMessage());
            throw new RuntimeException(e);
        }
        return result;
    }


    private List<String> getFieldList(String express) {
        List<String> fieldList = new ArrayList<>();
        if(StringUtils.isEmpty(express)){
            return fieldList;
        }
        //定义正则表达式进行抽取
        String regx = "#\\{(.*?)\\}";
        Pattern compile = Pattern.compile(regx);
        Matcher matcher = compile.matcher(express);
        while (matcher.find()){
            fieldList.add(matcher.group(1));
        }
        return fieldList;
    }

    public Object execExpressByEvent(String eventCode, ParamObject paramObject) {
        //通过事件码找到匹配的规则配置
        List<RuleConfig> ruleConfigs = ruleConfigMap.get(paramObject.getCityId());
        if(CollectionUtils.isEmpty(ruleConfigs)){
            log.error("未找到该城市的规则配置");
            return null;
        }
        List<RuleConfig> eventCodeMatchRuleConfigList = ruleConfigs.stream().filter((ruleConfig -> {
            List<RuleConfig.RuleAction> ruleActionList = ruleConfig.getRuleActionList();
            for (RuleConfig.RuleAction ruleAction : ruleActionList) {
                return ruleAction.getEventCodeList().contains(eventCode);
            }
            return false;
        })).collect(Collectors.toList());

        Map<String, Object> resultMap = new HashMap<>();
        for (RuleConfig ruleConfig : eventCodeMatchRuleConfigList) {
            List<RuleConfig.RuleAction> ruleActionList = ruleConfig.getRuleActionList();
            for (RuleConfig.RuleAction ruleAction : ruleActionList) {
                String ruleExpress = ruleAction.getRuleExpress();
                DefaultContext<String, Object> context = new DefaultContext<>();
                //执行规则匹配
                Boolean match = (Boolean) execExpress(ruleExpress, context, paramObject);
                if(match){
                    List<RuleConfig.Action> matchActionList = ruleAction.getMatchActionList();
                    //反射执行action(可提前加载到spring中执行)
                    for (RuleConfig.Action action : matchActionList) {
                        try {
                            String serviceName = action.getServiceName();
                            Class<?> clazz = Class.forName(serviceName);
                            Method method = clazz.getMethod(action.getMethodName(), ParamObject.class, Map.class, Map.class);
                            method.setAccessible(true);
                            method.invoke(clazz.newInstance(), paramObject,
                                    JSONUtil.parseObj(action.getParams()),
                                    resultMap);

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }else{
                    List<RuleConfig.Action> noMatchActionList = ruleAction.getNoMatchActionList();

                }

            }
        }

        return resultMap;
    }

    public void setContextValue(String key, Object value) {
        DefaultContext<String, Object> contextValue = contextValueThreadLocal.get();
        if (null == contextValue || contextValue.isEmpty()) {
            contextValue = new DefaultContext<String, Object>();
        }
        contextValue.put(key, value);
        contextValueThreadLocal.set(contextValue);
    }

    public static DefaultContext<String, Object>  getContextValue() {
        return contextValueThreadLocal.get();
    }

}
