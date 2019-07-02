package com.kayak.algorithmtest.mapper;

import com.kayak.algorithmtest.entity.Exercise;
import com.kayak.core.projectbase.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author mxl
 * @title: ExerciseMapper
 * @projectName algorithm_test1.0
 * @description: TODO
 * @date 2019/6/30 18:42
 */
@Repository
public interface ExerciseMapper extends BaseMapper<Exercise> {


    List<Exercise> getExerciseListByBondId(String bondId);

}
