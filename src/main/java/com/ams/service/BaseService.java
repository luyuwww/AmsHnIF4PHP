package com.ams.service;

import ch.qos.logback.classic.Logger;
import com.ams.dao.BaseDao;
import com.ams.dao.JdbcDao;
import com.ams.dao.i.SUserMapper;
import com.ams.pojo.PTable;
import com.ams.util.CommonUtil;
import com.ams.util.DateUtil;
import com.ams.util.GlobalFinalAttr.DatabaseType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class BaseService {
    public static String ARCID;//档案类型的id
    public static Map<String, String> ARCfieldtype;//数据表的字段类型
    public static String dfileTableName ;//数据表名
    public static List<PTable> dPtabList= null;//数据表的字段值对应表配置表信息
    public static Map<String,String> fieldMap = new HashMap<String, String>();

    /**
     * 初始化方法
     */
    public void init() {
        dfileTableName = "f" + phpQzh + "_" + phpTabNum + "_document";
        List<PTable> tabList = baseDao.getEtabList(oaDfileMappingTable);
        List<Map<String, String>> maps = baseDao.getfieldtype(dfileTableName);
        List<String> efieldTemp = null;
        dPtabList = baseDao.getDtabList(oaDfileMappingTable);
        ARCID = baseDao.getArcId(phpTabNum);
        if (StringUtils.isBlank(ARCID)) {
            throw new RuntimeException("档案门类配置错误！");
        }
        if (!maps.isEmpty()) {
            ARCfieldtype = new HashMap<String, String>();
            for (Map<String, String> map : maps) {
                ARCfieldtype.put(MapUtils.getString(map, "code"), MapUtils.getString(map, "type"));
            }
        } else {
            throw new RuntimeException("表信息配置错误！");
        }
        for (PTable pTable : tabList) {
            fieldMap.put(pTable.getF1(),pTable.getF2());
        }
    }

    /**
     * 得到数据库信息 databaseType 和 databaseTime
     */
    protected Map<String, Object> getDBInfo() throws RuntimeSqlException {
        Date dataTime = null;
        Map<String, Object> infos = new LinkedHashMap<String, Object>();
        TimeZone.setDefault(TimeZone.getTimeZone("ETC/GMT-8")); // 设置时区 中国/北京/香港
        String typeStr = getDBTyeStr();
        if (StringUtils.isNotEmpty(typeStr)) {
            if (typeStr != null && typeStr.equals("Microsoft SQL Server")) {
                dataTime = sUserMapper.selectDateTimeForMSSQL();
            } else if (typeStr != null && typeStr.equals("Oracle")) {
                dataTime = sUserMapper.selectDateTimeForOra();
            } else if (typeStr != null && typeStr.equals("Db2")) {
                dataTime = sUserMapper.selectDateTimeForDB2();
            } else if (typeStr != null && typeStr.equals("MySQL")) {
                dataTime = sUserMapper.selectDateTimeForMySQL();
            } else if (typeStr != null && typeStr.equals("H2")) {
                dataTime = sUserMapper.selectDateTimeForH2();
            } else {
                dataTime = new Date();
                log.error("DB Type not funder!");
            }
        } else {
            dataTime = new Date();
            log.error("get database time is error!");
        }
        infos.put("databaseType", typeStr);
        infos.put("databaseTime", dataTime);
        return infos;
    }

    protected String generateTimeToSQLDate(Object date) {
        String datevalue = null;
        String typeStr = getDBTyeStr();
        TimeZone.setDefault(TimeZone.getTimeZone("ETC/GMT-8")); // 设置时区 中国/北京/香港
        if (date instanceof Date) {
            datevalue = DateUtil.getDateTimeFormat().format(date);
        } else if (date instanceof String) {
            datevalue = (String) date;
        }
        if (StringUtils.isNotEmpty(typeStr)) {
            if (typeStr != null && typeStr.equals("Microsoft SQL Server")) {
                datevalue = "cast('" + datevalue + "' as datetime)";
            } else if (typeStr != null && typeStr.equals("Oracle")) {
                if (datevalue.indexOf(".") > -1) {// 防止出现 2056-12-25 00:00:00.0
                    // 而无法导入
                    datevalue = datevalue.substring(0,
                            datevalue.lastIndexOf("."));
                }
                datevalue = "TO_DATE('" + datevalue
                        + "', 'yyyy-MM-dd HH24:mi:ss')";
            } else if (typeStr != null && typeStr.equals("Db2")) {
                datevalue = "TIMESTAMP('" + datevalue + "' )";
            } else if (typeStr != null && typeStr.equals("MySQL")) {
                datevalue = "DATE_FORMAT('" + datevalue
                        + "', '%Y-%m-%d %H:%i:%s')";
            } else if (typeStr != null && typeStr.equals("H2")) {
                datevalue = "PARSEDATETIME('" + datevalue
                        + "'，'dd-MM-yyyy hh:mm:ss.SS' )";
            } else {
                datevalue = "";
                log.error("DB Type not funder!");
            }
        } else {
            datevalue = "";
            log.error("get database time is error!");
        }
        return datevalue;
    }

    /**
     * 得到数据库的时间 如果错误返回new的时间
     */
    protected Date getDBDateTime() throws RuntimeSqlException {
        Date dbDate = null;
        TimeZone.setDefault(TimeZone.getTimeZone("ETC/GMT-8")); // 设置时区 中国/北京/香港
        String typeStr = getDBTyeStr();
        if (StringUtils.isNotEmpty(typeStr)) {
            if (typeStr.equals("Microsoft SQL Server")) {
                dbDate = sUserMapper.selectDateTimeForMSSQL();
            } else if (typeStr.equals("Oracle")) {
                dbDate = sUserMapper.selectDateTimeForOra();
            } else if (typeStr.equals("Db2")) {
                dbDate = sUserMapper.selectDateTimeForDB2();
            } else if (typeStr.equals("MySQL")) {
                dbDate = sUserMapper.selectDateTimeForMySQL();
            } else if (typeStr.equals("H2")) {
                dbDate = sUserMapper.selectDateTimeForH2();
            } else {
                dbDate = new Date();
                log.error("DB is no look!");
            }
        } else {
            dbDate = new Date();
            log.error("get database time is error!");
        }
        return dbDate;
    }

    /**
     * 得到数据库的类型str
     */
    protected String getDBTyeStr() throws RuntimeSqlException {
        String typeStr = null;
        TimeZone.setDefault(TimeZone.getTimeZone("ETC/GMT-8")); // 设置时区 中国/北京/香港
        Connection conn = null;
        DatabaseMetaData dbmd = null;
        try {
            conn = jdbcDao.getConn();
            dbmd = conn.getMetaData();
            typeStr = dbmd.getDatabaseProductName();
        } catch (Exception e) {
            log.error("get database type is error!", e);
        } finally {
            try {
                dbmd = null;
                conn.close();
            } catch (SQLException exx) {
                log.error(exx.getMessage());
            }
        }
        return typeStr;
    }

    /**
     * 得到数据库类型的 DatabaseType
     */
    protected DatabaseType getDatabaseType() {
        DatabaseType databaseType = null;
        try {
            databaseType = DatabaseType.getDatabaseType(getDBTyeStr());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return databaseType;
    }

    protected Map<String, Object> queryForMap(String sql) {
        return jdbcDao.queryForMap(sql);
    }

    protected List<Map<String, Object>> quertListMap(String sql) {
        return jdbcDao.quertListMap(sql);
    }

    protected String queryForString(String sql) {
        return jdbcDao.query4String(sql);
    }

    /**
     * 查新表2列 第一列是key第二列是value的一个map
     */
    protected Map<String, String> quert2Colum4Map(String sql, String col1,
                                                  String col2) {
        return jdbcDao.quert2Colum4Map(sql, col1, col2);
    }
    protected void execSql(String sql) {
        jdbcDao.excute(sql);
    }
    /**
     * 数据表添加方法
     *
     * @param map          xml中的name
     * @param fieldMapping
     * @param tableName
     * @param oaid
     * @return
     */
    protected String insertDfile4Map(Map<String, String> map,
                                     Map<String, String> fieldMapping, String tableName, String oaid,String formName) {
        String archKey = ""; // 档案字段
        String archVal = ""; // 档案字段对应的值
        String returnDid = "" + -1;
        StringBuffer fields = new StringBuffer();
        StringBuffer values = new StringBuffer();
        String did = CommonUtil.getpfpID();
        if (null != map && null != map.keySet() && map.keySet().size() > 0) {
            try {
                Set<String> fieldSet = map.keySet();
                for (String outSysField : fieldSet) {
                    archKey = fieldMapping.get(outSysField);
                    archVal = map.get(outSysField);
                    if (StringUtils.isNotBlank(archVal)
                            && StringUtils.isNotBlank(archKey)) {
                        archVal = (StringUtils.isBlank(archVal) ? "" : archVal);
                        archVal = (archVal.contains("'") ? archVal.replace("'",
                                "''") : archVal);// 兼容单引号
                        String fieldtype = MapUtils.getString(ARCfieldtype, archKey);
                        fields.append(archKey).append(",");
                        fieldtype = fieldtype.toLowerCase();
                        if (fieldtype.equals("datetime")) {
                            if (archVal.equals("")) {
                                values.append("sysdate,");
                            } else {
                                values.append(generateTimeToSQLDate(archVal))
                                        .append(",");
                            }

                        } else if (fieldtype.equals("date")) {
                            if (archVal.equals("")) {
                                values.append("curdate(),");
                            } else {
                                values.append(generateTimeToSQLDate(archVal))
                                        .append(",");
                            }

                        } else if (fieldtype.equals("varchar")) {
                            values.append("'").append(archVal).append("',");

                        } else if (fieldtype.equals("int")) {
                            if (StringUtils.isBlank(archVal)) {
                                values.append("null ,");
                            } else {
                                values.append(Integer.parseInt(archVal))
                                        .append(",");
                            }

                        } else if (fieldtype.equals("numdate")) {
                            if (StringUtils.isBlank(archVal)) {
                                values.append("null ,");
                            } else {
                                if (archVal.contains("-")) {
                                    values.append(archVal.replaceAll("-", "").substring(0, 8))
                                            .append(",");
                                }
                            }

                        } else {
                            values.append("'").append(archVal).append("',");

                        }
                    }
                }
                fields.append("id,createtime,fondsid,rid," + phpDfileKey+","+oaFormSaveField);
                values.append(did + ",sysdate()," + phpQzh + ",").append("'" + oaid + "',")
                        .append(phpDfileValue).append(",'"+formName+"'");
                String SQL = "insert into " + tableName + " ("
                        + fields.toString() + ") values ( " + values.toString()
                        + " )";
                execSql(SQL);
                returnDid = did;
                System.out.println("插入一条数据成功.fileReciveTxt: " + SQL);
                log.info("插入一条数据成功.fileReciveTxt: " + SQL);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("插入一条数据失败.fileReciveTxt: " + e.getMessage());
            }
        } else {
            returnDid = -1 + "";
        }
        fields.setLength(0);
        values.setLength(0);
        return returnDid;
    }

    /**
     * 新增附件信息方法
     *
     * @param efile
     * @param pid
     * @param efileName
     */
    protected void insertEfile(File efile, String pid, String efileName) {
        String eFileTableName = "e_record";
        String ext = FilenameUtils.getExtension(efile.getName());
        efileName = efileName+ "." + ext;
        String eid = CommonUtil.getpfpID();
        String realFileName = eid + "." + ext;
        String nowDate = DateUtil.getCurrentDateStr();
        long filesize = efile.length();
        String newFilepathstr = arcftpCatalogue + "/uploads/company1/fonds" + phpQzh +
                "/" + ARCID + "/" + nowDate.substring(0, 4) + "/" + nowDate.replaceAll("-", "") + "/";
        File newFilePath = new File(newFilepathstr);
        File newFile = new File(newFilepathstr + realFileName);
        if (!newFilePath.isDirectory()) {
            newFilePath.mkdirs();
        }
//		File newFile = new File(efilepath + File.separator + efile.getName());
        try {
            FileUtils.copyFile(efile, newFile);//复制电子文件
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        StringBuffer fields = new StringBuffer();
        StringBuffer values = new StringBuffer();
        try {
            fields.append("id, fondsid,archtypeid,tableid,archid,title,extension,savefilename,filepath," +
                    "hangingtime,filesize," + phpEfileKey);
            values.append("'" + eid + "',").append(phpQzh).append("," + ARCID).append(",'" + dfileTableName + "',");
            values.append(pid).append(",'").append(efileName).append("','").append(ext + "','");
            values.append(realFileName).append("','").append(newFilepathstr.replace(arcftpCatalogue, "")).append("',sysdate(),");
            values.append(filesize + ",");
            values.append(phpEfileValue);
            String SQL = "insert into " + eFileTableName + " ("
                    + fields.toString() + ") values ( " + values.toString()
                    + " )";
            execSql(SQL);
            System.out.println("插入一条电子文件成功.efileReciveTxt: " + SQL);
            log.info("插入一条电子文件成功.efileReciveTxt: " + SQL);
        } catch (Exception e) {
            log.error("插入一条电子文件失败.efileReciveTxt: " + e.getMessage());
            fields.setLength(0);
            values.setLength(0);
        }
    }

    /**
     * 根据数据字典表获得oa和档案系统字段值的对应关系
     */
//    protected Map<String, String> getGwMappingArc(String wjlx) {
//        Map<String, String> oaGwMappingArc = new HashMap<String, String>();
//        for (PTable pTable : dPtabList) {
//            if(wjlx.equals(pTable.getF3())){
//                oaGwMappingArc.put(pTable.getF1(),pTable.getF2());
//            }
//        }
//        return oaGwMappingArc;
//    }

    /**
     * 获取数据库参数 数据库类型名称,时间
     */
    protected String getSysdate() {
        if (sysdate != null) {
            return sysdate;
        }
        sysdate = "SYSDATE";
        return sysdate;
    }

    /**
     * 获得文件夹名作oa主键
     *
     * @param path
     * @return
     */
    protected String getOAid(String path) {
        String[] parts = path.split("/");
        String fileName = parts[parts.length - 1];
        if(fileName.contains("_")){
            fileName = fileName.split("_")[0];
        }
        return fileName;
    }

    @Autowired
    protected JdbcDao jdbcDao;
    @Autowired
    protected BaseDao baseDao;
    @Autowired
    protected SUserMapper sUserMapper;
    @Autowired
    @Value("${sqlserverSchemaName}")
    protected String sqlserverSchemaName;
    @Autowired
    @Value("${oa.dfile.mapping}")
    protected String oaDfileMappingTable;//oa Dfile mapping
    @Autowired
    @Value("${arc.ftp.catalogue}")
    protected String arcftpCatalogue;//oa Efile mapping
    @Autowired
    @Value("${php.qzh}")
    protected String phpQzh;
    @Autowired
    @Value("${php.tabname.Tabnam}")
    protected String phpTabNum;
    @Autowired
    @Value("${php.dfile.key}")
    protected String phpDfileKey;
    @Autowired
    @Value("${php.dfile.value}")
    protected String phpDfileValue;
    @Autowired
    @Value("${php.efile.key}")
    protected String phpEfileKey;
    @Autowired
    @Value("${php.efile.value}")
    protected String phpEfileValue;
    @Autowired
    @Value("${move.rootPath}")
    protected String movePath;
    @Autowired
    @Value("${oa.xml.infostr}")
    protected String oaXmlInfostr;
    @Autowired
    @Value("${oa.ftp.path}")
    protected String oaFtpPath;
    @Autowired
    @Value("${oa.form.saveField}")
    protected String oaFormSaveField;
    @Autowired
    @Value("${oa.ftp.efilepath}")
    protected String oaFtpEfilepath;

    private String sysdate = null;
    private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());
}
