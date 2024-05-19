package com.wsp.rule.engine.action;


import com.wsp.rule.engine.param.ParamObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class OrderAction {

    /**
     * 创建订单Action
     */
    public void createOrderAction(ParamObject paramObject, Map<String, Object> paramMap, Map<String, Object> resultMap){
        log.info("创建订单成功");
        resultMap.put("createOrderAction","success");
    }
}
