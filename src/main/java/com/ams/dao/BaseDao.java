package com.ams.dao;

import com.ams.pojo.FDTable;
import com.ams.pojo.PTable;
import com.ams.pojo.SDalx;
import com.ams.pojo.WWjkgl;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface BaseDao {
    /**
     * 得到最大的did
     */
    @Select("select max(did) from ${tableName}")
    Integer getMaxDid(@Param("tableName") String tableName);

    /**
     * sqlserver得到数据库时间
     */
    @Select("select getdate()")
    Date selectDateTimeForMSSQL();

    /**
     * oracle得到数据库时间
     */
    @Select("select SYSDATE from dual")
    Date selectDateTimeForOra();

    /**
     * db2得到数据库时间
     */
    @Select("select current timestamp from sysibm.sysdummy1")
    Date selectDateTimeForDB2();

    /**
     * MySql得到数据库时间
     */
    @Select("select now()")
    Date selectDateTimeForMySQL();

    /**
     * h2得到数据库时间
     */
    @Select("select CURRENT_TIMESTAMP")
    Date selectDateTimeForH2();

    /**
     * copy表
     *
     * @param sName 源表
     * @param tName 目标表
     */
    @Insert("create table ${tName} as  (select * from ${sName})")
    void copyTable(@Param("sName") String sName, @Param("tName") String tName);

    @Insert("drop table ${tableName}")
    void dropTable(@Param("tableName") String tableName);

    @Select("select * from ${tableName}")
    List<FDTable> getFtableList(@Param("tableName") String tableName);

    @Select("select * from s_dalx ")
    List<SDalx> getAllDalxList();

    @Select("select * from w_wjkgl ")
    List<WWjkgl> getAllWjkglList();

    /**
     * 查询字段类型
     *
     * @param tableName
     * @return
     */
    @Select("select code,type from s_archive_field where tableid='${tableName}'")
    List<Map<String, String>> getfieldtype(@Param("tableName") String tableName);

    /**
     * 查询档案类型id
     *
     * @param archindex
     * @return
     */
    @Select("select id from s_archive_type where archindex = '${archindex}' and status = 1")
    String getArcId(@Param("archindex") String archindex);

    /**
     * 查询字段对应表的信息
     * @return
     */
//    @Select("select F1,F2,F3 from ${tableName}")
    @Select("select F1,F2 from ${tableName}")
    List<PTable> getDtabList(@Param("tableName") String tableName);
    /**
     * 查询附件对应信息
     * @return
     */
    @Select("select F1,F2 from ${tableName}")
    List<PTable> getEtabList(@Param("tableName") String tableName);


}
