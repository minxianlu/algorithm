package com.kayak.core.projectbase;

import java.util.List;
import java.util.Map;

/**
 * @author mxl
 * @title: BaseMapper
 * @projectName algorithm_test
 * @description: TODO
 * @date 2019/6/27 16:29
 */
public interface BaseMapper<E> {

    public E getById(String id);

    public List<E> getListByE(E e);

    public void updateEById(String id);

//    public List<Map<String,String>> getListByIds(List<String> list);


}
