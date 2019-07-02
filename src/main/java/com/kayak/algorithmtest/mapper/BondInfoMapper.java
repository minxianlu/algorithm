package com.kayak.algorithmtest.mapper;

import com.kayak.algorithmtest.entity.BondInfo;
import com.kayak.core.projectbase.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author mxl
 * @title: BondInfoMapper
 * @projectName algorithm_test
 * @description: TODO
 * @date 2019/6/27 16:25
 */
@Repository
public interface BondInfoMapper extends BaseMapper<BondInfo> {


    public List<BondInfo> getListByBondIds(Set<String> set);

    public BondInfo getBondInfoByBondId(String bondId);


}
