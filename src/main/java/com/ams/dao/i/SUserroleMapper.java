package com.ams.dao.i;

import java.util.List;

import com.ams.dao.BaseDao;
import org.apache.ibatis.annotations.Param;

import com.ams.pojo.SUserrole;
import com.ams.pojo.SUserroleExample;

public interface SUserroleMapper extends BaseDao {
    int countByExample(SUserroleExample example);
    int deleteByExample(SUserroleExample example);
    int deleteByPrimaryKey(Integer did);

    int insert(SUserrole record);

    int insertSelective(SUserrole record);
    List<SUserrole> selectByExample(SUserroleExample example);

    SUserrole selectByPrimaryKey(Integer did);

    int updateByExampleSelective(@Param("record") SUserrole record, @Param("example") SUserroleExample example);
    int updateByExample(@Param("record") SUserrole record, @Param("example") SUserroleExample example);

    int updateByPrimaryKeySelective(SUserrole record);
    int updateByPrimaryKey(SUserrole record);
}