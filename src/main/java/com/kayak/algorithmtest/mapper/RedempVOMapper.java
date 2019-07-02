package com.kayak.algorithmtest.mapper;

import com.kayak.algorithmtest.entity.Cashflow;
import com.kayak.algorithmtest.entity.RedempVO;
import com.kayak.core.projectbase.BaseMapper;

import java.util.List;

/**
 * @author mxl
 * @title: RedempVOMapper
 * @projectName algorithm_test1.0
 * @description: TODO
 * @date 2019/7/2 10:39
 */
public interface RedempVOMapper extends BaseMapper<RedempVO> {


    List<RedempVO> getRedempVOListByBondId(String bondId);

}
