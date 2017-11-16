package com.ams.service;

import ch.qos.logback.classic.Logger;
import com.ams.dao.JdbcDao;
import com.ams.dao.i.SGroupMapper;
import com.ams.dao.i.SQzhMapper;
import com.ams.dao.i.SUserMapper;
import com.ams.dao.i.SUserroleMapper;
import com.ams.pojo.FDTable;
import com.ams.util.CommonUtil;
import com.ams.util.DateUtil;
import com.ams.util.GlobalFinalAttr;
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
	 * 
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
	protected GlobalFinalAttr.DatabaseType getDatabaseType() {
		GlobalFinalAttr.DatabaseType databaseType = null;
		try {
			databaseType = GlobalFinalAttr.DatabaseType.getDatabaseType(getDBTyeStr());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return databaseType;
	}

	/**
	 * 根据表名判断数据表是否存在
	 */
	protected Boolean existTable(String tablename) {
		boolean result = false;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		ResultSet rs = null;
		try {
			conn = jdbcDao.getConn();
			dbmd = conn.getMetaData();
			String schemaName = getSchemaName(dbmd);
			rs = dbmd.getTables(null, schemaName, tablename,
					new String[] { "TABLE" });
			if (rs.next()) {
				result = true;
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
		} finally {
			try {
				dbmd = null;
				rs.close();
				conn.close();
			} catch (SQLException e) {
				log.error("获取ConnectionMetaData关闭链接错误!");
			}
		}
		return result;
	}

	/**
	 * 判断表的字段是否存在
	 */
	protected boolean existColumn(String tablename, String columnName) {
		return existColumnOrIndex(tablename, columnName, true);
	}

	/**
	 * 判断字段的索引是否存在
	 */
	protected boolean existIndex(String tablename, String indexName) {

		return existColumnOrIndex(tablename, indexName, false);
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

	/**
	 * 判断表的字段或者索引是否存在
	 * 
	 * @param tablename
	 *            表名
	 * @param columnOrIndexName
	 *            字段名, 或者索引名
	 * @param isColumn
	 *            true字段 false索引
	 * @return boolean true存在 false 不存在
	 */
	protected boolean existColumnOrIndex(String tablename,
			String columnOrIndexName, boolean isColumn) {
		boolean result = false;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		ResultSet rs = null;
		try {
			conn = jdbcDao.getConn();
			dbmd = conn.getMetaData();
			String schemaName = getSchemaName(dbmd);
			if (isColumn) {
				rs = dbmd.getColumns(null, schemaName, tablename,
						columnOrIndexName);
				if (rs.next()) {
					result = true;
				}
			} else {
				rs = dbmd.getIndexInfo(null, schemaName, tablename, false,
						false);
				while (rs.next()) {
					String indexName = rs.getString(6);
					if (indexName != null
							&& indexName.equals(columnOrIndexName)) {
						result = true;
						break;
					}
				}
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
		} finally {
			try {
				dbmd = null;
				rs.close();
				conn.close();
			} catch (SQLException e) {
				log.error("获取ConnectionMetaData关闭链接错误!");
			}
		}
		return result;
	}

	/**
	 * 根据表字段是否可以为空
	 */
	protected boolean validateColumnIsNULL(String tablename, String columnName) {
		boolean result = false;
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		ResultSet rs = null;
		try {
			conn = jdbcDao.getConn();
			dbmd = conn.getMetaData();
			String schemaName = getSchemaName(dbmd);
			rs = dbmd.getColumns(null, schemaName, tablename, columnName);
			if (rs.next()) {
				String notnull = rs.getString(11);
				result = notnull != null && notnull.equals("1");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
		} finally {
			try {
				dbmd = null;
				rs.close();
				conn.close();
			} catch (SQLException e) {
				log.error("获取ConnectionMetaData关闭链接错误!");
			}
		}
		return result;
	}

	/**
	 * 执行sql文件
	 */
	protected boolean runScript(Reader reader) {
		boolean result = false;
		Connection conn = null;
		try {
			conn = jdbcDao.getConn();
			ScriptRunner runner = new ScriptRunner(conn);
			runner.setErrorLogWriter(null);
			runner.setLogWriter(null);
			runner.runScript(reader);
			result = true;
		} catch (Exception ex) {
			log.error(ex.getMessage() + "执行sql文件错误", ex);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error(e.getMessage() + "获取ConnectionMetaData关闭链接错误!", e);
			}
		}
		return result;
	}

	/**
	 * 获取表模式 private
	 */
	private String getSchemaName(DatabaseMetaData dbmd) throws SQLException {
		String schemaName;
		switch (getDatabaseType().getValue()) {
		case 1:// mssql
			schemaName = sqlserverSchemaName;
			break;
		case 4:// h2
			schemaName = null;
			break;
		default:
			schemaName = dbmd.getUserName();
			break;
		}
		return schemaName;
	}

	protected void execSql(String sql) {
		jdbcDao.excute(sql);
	}
	/**
	 * 根据部门名称获取全宗号
	 * 
	 * @param qzmc
	 * @return
	 */
	protected String getQzh(String qzmc) {
		String sql = "select qzh from s_qzh where bz = '" + qzmc + "'";
		String qzh = jdbcDao.query4String(sql);
		return qzh;
	}

	/**
	 * 根据pid获取全宗号
	 * 
	 * @param pid
	 * @return
	 */
	protected String getQzhByPid(Integer pid) {
		String sql = "select qzh from s_qzh where did = " + pid;
		String qzh = jdbcDao.query4String(sql);
		return qzh;
	}

	protected String getQzhByKey(String key) {
		String sql = "select qzh from s_qzh where primarykey = " + key;
		String qzh = jdbcDao.query4String(sql);
		return qzh;
	}
	protected Integer getMaxDid(String tableName) {
		Integer returnMaxDid = sUserMapper.getMaxDid(tableName);
		if (returnMaxDid == null) {
			returnMaxDid = 1;
		} else {
			returnMaxDid = returnMaxDid + 1;
		}
		return returnMaxDid;

	}
	
	
	protected Integer insertDfile4Map(Map<String, String> map,
			Map<String, String> fieldMapping, String tableName) {
		String archKey = ""; // 档案字段
		String archVal = ""; // 档案字段对应的值
		String bmid = amsDefaultBmid;
		Integer returnDid = -1;
		FDTable fDtable = null;
		List<FDTable> fDTableList = null;// 相关档案类型的字段List
		StringBuffer fields = new StringBuffer();
		StringBuffer values = new StringBuffer();
		if (null != map && null != map.keySet() && map.keySet().size() > 0) {
			try {
				Integer maxdid = getMaxDid(tableName);
				fDTableList = sGroupMapper.getFtableList("F_" + tableName);
				Set<String> fieldSet = map.keySet();
				for (String outSysField : fieldSet) {
					archKey = fieldMapping.get(outSysField);
					archVal = map.get(outSysField);
					if (StringUtils.isNotBlank(archVal)
							&& StringUtils.isNotBlank(archKey)) {
						archVal = (StringUtils.isBlank(archVal) ? "" : archVal);
						archVal = (archVal.contains("'") ? archVal.replace("'",
								"''") : archVal);// 兼容单引号
						fDtable = CommonUtil.getFDtable(fDTableList,
								archKey);
						fields.append(fDtable.getFieldname()).append(",");
						switch (fDtable.getFieldtype()) {
						case 11:
							if (archVal.equals("")) {
								values.append("sysdate,");
							} else {
								values.append(generateTimeToSQLDate(archVal))
										.append(",");
							}
							break;
						case 1:
							values.append("'").append(archVal).append("',");
							break;
						case 3:
							if (StringUtils.isBlank(archVal)) {
								values.append("null ,");
							} else {
								values.append(Integer.parseInt(archVal))
										.append(",");
							}
							break;
						default:
							values.append("'").append(archVal).append("',");
							break;
						}
					}
				}
				fields.append("pid,createtime,status, attr,attrex,qzh,bmid,attached,did");
				values.append("-1,sysdate,0,").append(dfileAttr).append(",")
						.append(dfileAttrex).append(",'");
				values.append(amsDefaultQzh).append("','").append(bmid).append("',0,")
						.append(maxdid);
				String SQL = "insert into " + tableName + " ("
						+ fields.toString() + ") values ( " + values.toString()
						+ " )";
				execSql(SQL);
				returnDid = maxdid;
				System.out.println("插入一条数据成功.fileReciveTxt: " + SQL);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("插入一条数据失败.fileReciveTxt: " + e.getMessage());
			}
		} else {
			returnDid = -1;
		}
		fields.setLength(0);
		values.setLength(0);
		return returnDid;
	}
	
	
	protected void insertEfile(File efile, String libcode, Integer pid, String efileName , String sysName) {
		String eFileTableName = "E_FILE" + libcode;
		String ext = FilenameUtils.getExtension(efile.getName());
		String efilepath = eFileTableName + File.separator
				+ DateUtil.getCurrentDateStr() + File.separator;
		String realFileName = GlobalFinalAttr.getGuid()
				+ System.currentTimeMillis() + "." + ext;
//		String realFileName = System.currentTimeMillis() + "." + ext;
		File newFile = new File(arcftpCatalogue + File.separator + efilepath + File.separator + realFileName);
		File newFilePath = new File(arcftpCatalogue + File.separator + efilepath + File.separator);
		if(!newFilePath.isDirectory()){
			newFilePath.mkdirs();
		}
//		File newFile = new File(efilepath + File.separator + efile.getName());
		try {
			FileUtils.copyFile(efile, newFile);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		StringBuffer fields = new StringBuffer();
		StringBuffer values = new StringBuffer();
		Integer maxdid = getMaxDid(eFileTableName);
		try {
			fields.append("PID, PATHNAME,TITLE,PZM,EFILENAME,EXT,");
			values.append(pid).append(",'").append(efilepath).append("','");
			values.append(efileName)
					.append("','").append(pzm).append("','");
			values.append(realFileName).append("','").append(ext).append("',");
			fields.append("CREATETIME,STATUS,ATTR,ATTREX,CREATOR,DID");
			values.append("sysdate").append(",0,").append(dfileAttr)
					.append(",").append(dfileAttrex).append(",'");
			values.append(sysName).append("',").append(maxdid);
			String SQL = "insert into " + eFileTableName + " ("
					+ fields.toString() + ") values ( " + values.toString()
					+ " )";
			String upSql = "update d_file"+libcode+" set attached = 1 where did = "+pid+"";
			execSql(SQL);
			execSql(upSql);
			System.out.println("插入一条数据成功.fileReciveTxt: " + SQL);
		} catch (Exception e) {
			log.error("插入一条数据失败.fileReciveTxt: " + e.getMessage());
			fields.setLength(0);
			values.setLength(0);
		}
	}
	
	/**
	 * 得到oa Xt映射表
	 */
//	protected Map<String, String> getOaXtMappingArc() {
//		if (null == oaXtMappingArc) {
//			oaXtMappingArc = quert2Colum4Map("SELECT F1 , F2 FROM "
//					+ oaXtMappingTable, "F1", "F2");
//		}
//		return oaXtMappingArc;
//	}

	/**
	 * 得到oa Fw映射表
	 */
	protected Map<String, String> getFwGwMappingArc(String wjlx) {
		if (null == oaFwMappingArc) {
			oaFwMappingArc = quert2Colum4Map("SELECT F1 , F2 FROM "
					+ oaDfileMappingTable + " where F3 = '"+wjlx+"'", "F1", "F2");
		}
		return oaFwMappingArc;
	}
	
	/**
	 * 得到oa Sw映射表
	 */
	protected Map<String, String> getSwGwMappingArc(String wjlx) {
		if (null == oaSwMappingArc) {
			oaSwMappingArc = quert2Colum4Map("SELECT F1 , F2 FROM "
					+ oaDfileMappingTable + " where F3 = '"+wjlx+"'", "F1", "F2");
		}
		return oaSwMappingArc;
	}
	
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
	@Autowired
	protected JdbcDao jdbcDao;
	@Autowired
	protected SGroupMapper sGroupMapper;
	@Autowired
	protected SUserMapper sUserMapper;
	@Autowired
	protected SQzhMapper sQzhMapper;
	@Autowired
	protected SUserroleMapper sUserroleMapper;
	@Autowired
	@Value("${sqlserverSchemaName}")
	protected String sqlserverSchemaName;
	@Autowired
	@Value("${ams.pzm}")
	protected String pzm;
	@Autowired
	@Value("${ams.dfile.status}")
	protected String dfileStatus;// 状态
	@Autowired
	@Value("${ams.dfile.attr}")
	protected String dfileAttr;// 归档前后 1未归档 0已归档
	@Autowired
	@Value("${ams.dfile.attrex}")
	protected String dfileAttrex;// 移交
	@Autowired
	@Value("${ams.default.qzh}")
	protected String amsDefaultQzh;
	@Autowired
	@Value("${ams.ws.libcode}")
	protected String wsCode;
	@Autowired
	@Value("${oaFile.localPath}")
	protected String oaXmlLocalPath;//存放oa上传xml本地目录
	@Autowired
	@Value("${ams.default.bmid}")
	protected String amsDefaultBmid;
	@Autowired
	@Value("${oa.fwxml.catalogue}")
	protected String OAfwCatalogue;//oa fw mulu
	@Autowired
	@Value("${oa.swxml.catalogue}")
	protected String OAswCatalogue;//oa sw mulu
	@Autowired
	@Value("${oa.qbxml.catalogue}")
	protected String OAqbCatalogue;//oa qb mulu
	@Autowired
	@Value("${oa.fwfile.catalogue}")
	protected String OAfwFileCatalogue;//oa fwfile mulu
	@Autowired
	@Value("${oa.swfile.catalogue}")
	protected String OAswFileCatalogue;//oa swfile mulu
	@Autowired
	@Value("${oa.qbfile.catalogue}")
	protected String OAqbFileCatalogue;//oa qbfile mulu
	@Autowired
	@Value("${oa.dfile.mapping}")
	protected String oaDfileMappingTable;//oa Dfile mapping
	@Autowired
	@Value("${arc.ftp.catalogue}")
	protected String arcftpCatalogue;//oa Efile mapping
	
	
	
	private String sysdate = null;
	private static Map<String, String> oaSwMappingArc = null;
	private static Map<String, String> oaFwMappingArc = null;
	private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());
}
