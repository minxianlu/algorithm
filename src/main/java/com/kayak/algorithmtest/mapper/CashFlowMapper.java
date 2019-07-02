package com.kayak.algorithmtest.mapper;

import com.kayak.algorithmtest.entity.Cashflow;
import com.kayak.core.projectbase.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author mxl
 * @title: CashFlowMapper
 * @projectName algorithm_test1.0
 * @description: TODO
 * @date 2019/6/30 14:35
 */
@Repository
public interface CashFlowMapper extends BaseMapper<Cashflow> {

    List<Cashflow> getCashFlowListByBondId(String bondId);

}
