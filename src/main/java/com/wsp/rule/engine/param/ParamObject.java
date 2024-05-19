package com.wsp.rule.engine.param;

import lombok.Data;

import java.util.List;

@Data
public class ParamObject {

    private List<Integer> numbers;

    private Integer addValue;

    private Integer cityId;
}
