package com.wsp.rule.engine.operator;


import com.ql.util.express.Operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * list1包含某一个值,如 [1,2,3] 包含 2
 */
public class ContainsOperator extends Operator {
    @Override
    public Boolean executeInner(Object[] objects) throws Exception {
        int length = objects.length;
        if(length !=2){
            return false;
        }
        Object object1 = objects[0];
        Object object2 = objects[1];
        if(object1 instanceof List){
            List<Object> list = Collections.singletonList(object1);
            return list.contains(object2);
        }else {
            return object1.equals(object2);
        }
    }
}
