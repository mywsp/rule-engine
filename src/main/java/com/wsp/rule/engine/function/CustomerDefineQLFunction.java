package com.wsp.rule.engine.function;

import com.wsp.rule.engine.aspect.QLFunc;
import com.wsp.rule.engine.param.ParamObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerDefineQLFunction {

    @QLFunc(key = "avg(this)")
    public Object avg(ParamObject paramObject){
        List<Integer> numbers = paramObject.getNumbers();
        Double sum = 0D;
        for (Integer number : numbers) {
            sum += number;
        }
        return sum/numbers.size();
    }

    @QLFunc(key = "avg2(this)")
    public Object avg2(Object[] objects){
        int length = objects.length;
        Double sum = 0D;
        for (Object object : objects) {
            double v = Double.parseDouble(object.toString());
            sum+=v;
        }
        return sum / length;
    }

}
