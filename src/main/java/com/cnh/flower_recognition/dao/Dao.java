package com.cnh.flower_recognition.dao;

import com.cnh.flower_recognition.domain.FloData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface Dao {

    @Results(value={
            @Result(property = "id", column = "id", id = true),
            @Result(property = "floImg", column = "flo_img"),
            @Result(property = "floName", column = "flo_name"),
            @Result(property = "floEnName", column = "flo_enname"),
            @Result(property = "floDetails", column = "flo_details"),
            @Result(property = "floLink",column = "flo_link")
    })
    @Select("select * from flo_data where id = #{id}")
    FloData getFloData(Integer id);
}
