package com.ams.service;

import ch.qos.logback.classic.Logger;
import com.ams.dao.BaseDao;
import com.ams.dao.JdbcDao;
import com.ams.dao.i.SUserMapper;
import com.ams.pojo.FieldMappingTab;
import com.ams.pojo.PTable;
import com.ams.pojo.TabNameMapping;
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
    public static Map<String,String> ARCIDMap = new HashMap<String, String>();//档案类型的id
    public static Map<String,Map<String, String>> ARCfieldtypes = new HashMap<String, Map<String, String>>();//数据表的字段类型
    public static Map<String,Map<String,String>> fieldMaps =
            new HashMap<String, Map<String, String>>();//字段对应信息
    public static List<FieldMappingTab> FieldMappingList;//字段对应表
    public static List<TabNameMapping> TabMappingList;//表信息
    public static Map<String ,String> tabNameMap = new HashMap<String, String>();//表名称map
    public static String[] wsTypeKeys= null;//区分文书类型的字段
    public static String[] wsTypeCValuses= null;//对应的文书字段
    public static String[] wsTypeEValuses= null;//对应的文书字段

    /**
     * 初始化方法
     */
    public void init() {
        String tabName = "";
        String ARCID = "";
        Map<String,String> ARCfieldtype = null;
        Map<String,String> fieldMap = null;
        List<Map<String, String>> maps = null;
        FieldMappingList = baseDao.getFieldMappingList(oaDfileMappingTable);
        TabMappingList = baseDao.getTabNameMappingList(daTabnameMapping);
        for (TabNameMapping tabNameMapping : TabMappingList) {
            tabName = "f"+tabNameMapping.getQzh()+"_"+tabNameMapping.getTabnum()+"_document";
            tabNameMap.put(tabNameMapping.getCompany()+"_"+tabNameMapping.getType(),tabName);
            ARCfieldtype = new HashMap<String, String>();
            maps = baseDao.getfieldtype(tabName);
            ARCID = baseDao.getArcId(tabNameMapping.getTabnum(),tabNameMapping.getQzh());
            if(StringUtils.isBlank(ARCID)){
                throw new RuntimeException("表信息配置错误！");
            }
            ARCIDMap.put(tabNameMapping.getTabnum(),ARCID);
            if (!maps.isEmpty()) {
                ARCfieldtype = new HashMap<String, String>();
                for (Map<String, String> map : maps) {
                    ARCfieldtype.put(MapUtils.getString(map, "code"), MapUtils.getString(map, "type"));
                }
            } else {
                throw new RuntimeException("表信息配置错误！");
            }
            ARCfieldtypes.put(tabName,ARCfieldtype);
            //添加字段对应信息
            fieldMap = new HashMap<String, String>();
            for (FieldMappingTab fieldMappingTab : FieldMappingList) {
                if(tabNameMapping.getCompany().equalsIgnoreCase(fieldMappingTab.getCompany())&&
                        tabNameMapping.getType().equalsIgnoreCase(fieldMappingTab.getType()) ){
                    fieldMap.put(fieldMappingTab.getOafield(),fieldMappingTab.getDafield());
                }
            }
            fieldMaps.put(tabNameMapping.getCompany()+"_"+tabNameMapping.getType(),fieldMap);
        }
        wsTypeKeys = arcWsTypekey.split(",");
        wsTypeCValuses = arcWsTypeCValue.split(",");
        wsTypeEValuses = arcWsTypeEValue.split(",");
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
                                     Map<String, String> fieldMapping, String tableName, String oaid,String company,String type) {
        String archKey = ""; // 档案字段
        String archVal = ""; // 档案字段对应的值
        String returnDid = "" + -1;
        StringBuffer fields = new StringBuffer();
        StringBuffer values = new StringBuffer();
        String did = CommonUtil.getpfpID();
        Map<String,String> ARCfieldtype = ARCfieldtypes.get(tableName);
        String oaWStype = "";
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
                        if(StringUtils.isBlank(fieldtype)){
                            log.error("字段名为 "+archKey+ " 配置错误！");
                        }
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
                        if("ZB".equalsIgnoreCase(company)&&"WS".equalsIgnoreCase(type)&&arcWsTypeField.equalsIgnoreCase(archKey)){
                            //arcWsTypeSaveField
                            for (int i = 0; i < wsTypeKeys.length; i++) {
                                if(archVal.contains(wsTypeKeys[i])){
                                    fields.append("classfy").append(",");
                                    fields.append("classfyname").append(",");
                                    values.append("'"+wsTypeEValuses[i]+"',");
                                    values.append("'"+wsTypeCValuses[i]+"',");
                                    break;
                                }
                            }
                        }
                    }
                }
                if("WS".equalsIgnoreCase(type)){
                    fields.append("volid,");
                    values.append("0,");
                }
                fields.append("id,createtime,fondsid,rid," + phpDfileKey);
                values.append(did + ",sysdate()," + this.getQZH(tableName) + ",").append("'" + oaid + "',")
                        .append(phpDfileValue);
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
    protected void insertEfile(File efile, String pid, String efileName,String dfileTableName) {
        String eFileTableName = "e_record";
        String ext = FilenameUtils.getExtension(efile.getName());
        efileName = efileName+ "." + ext;
        String eid = CommonUtil.getpfpID();
        String realFileName = eid + "." + ext;
        String nowDate = DateUtil.getCurrentDateStr();
        String ARCID = ARCIDMap.get(this.getTabNum(dfileTableName));
        long filesize = efile.length();
        String newFilepathstr = arcftpCatalogue + "/uploads/company1/fonds" + this.getQZH(dfileTableName) +
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
            values.append("'" + eid + "',").append(this.getQZH(dfileTableName)).append("," + ARCID).append(",'" + dfileTableName + "',");
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

    /**
     * 获取全宗号
     * @param tabname
     * @return
     */
    protected String getQZH(String tabname){
        return tabname.split("_")[0].replace("f","");
    }

    /**
     * 获取表序号
     * @param tabname
     * @return
     */
    protected String getTabNum(String tabname){
        return tabname.split("_")[1];
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
    @Value("${oa.ftp.path}")
    protected String oaFtpPath;
    @Autowired
    @Value("${oa.ftp.efilepath}")
    protected String oaFtpEfilepath;
    @Autowired
    @Value("${da.tabname.mapping}")
    protected String daTabnameMapping;

    @Autowired
    @Value("${oa.zb.path}")
    protected String oaZBpath;
    @Autowired
    @Value("${oa.nm.path}")
    protected String oaNMpath;
    @Autowired
    @Value("${oa.sy.path}")
    protected String oaSYpath;
    @Autowired
    @Value("${oa.yc.path}")
    protected String oaYCpath;
    @Autowired
    @Value("${arc.ws.typeField}")
    protected String arcWsTypeField;

    @Autowired
    @Value("${arc.ws.typekey}")
    protected String arcWsTypekey;

    @Autowired
    @Value("${arc.ws.typeCValue}")
    protected String arcWsTypeCValue;
    @Autowired
    @Value("${arc.ws.typeEValue}")
    protected String arcWsTypeEValue;

    private String sysdate = null;
    private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());
}
