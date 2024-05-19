package com.wsp.rule.engine;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.DynamicParamsUtil;
import com.wsp.rule.engine.engine.QLExpress;
import com.wsp.rule.engine.param.ParamObject;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class RuleEngineApplicationTests {

    @Resource
    QLExpress qlExpress;

    @Test
    void contextLoads() {
        //1.定义一个QL_Express类，并初始化加载定义的函数
        //2.执行
//		testDefineQLExec();

        //testSimplyQLExec();

        //设置表达式参数
        testQLExecByEventCode();
    }

    private void testQLExecByEventCode() {
        DefaultContext<String, Object> context = new DefaultContext();
        ParamObject paramObject = new ParamObject();
        paramObject.setNumbers(Lists.newArrayList(1, 2));
        paramObject.setAddValue(5);
        paramObject.setCityId(1);
        Object o = qlExpress.execExpressByEvent("2001", paramObject);
        System.out.println("返回结果为: "+o);
    }


    private void testSimplyQLExec() {
        String express = "(avg2(2,3)*3+#{addValue})";
        DefaultContext<String, Object> context = new DefaultContext();
        ParamObject paramObject = new ParamObject();
        paramObject.setNumbers(Lists.newArrayList(1, 2));
        paramObject.setAddValue(5);
        context.put("this", paramObject);
        //支持动态参数调用
        DynamicParamsUtil.supportDynamicParams = true;
        //反射获取字段
        Object o = qlExpress.execExpress(express, context, paramObject);
        System.out.println("返回结果为: "+o);
    }

}
