package cn.lams.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Logger;
import cn.lams.dao.i.SGroupMapper;
import cn.lams.pojo.FDTable;
import cn.lams.pojo.SDalx;
import cn.lams.pojo.WWjkgl;
import cn.lams.pojo.jaxb.Field;
import cn.lams.pojo.jaxb.Table;

@Component("createXmlUtil")
public class XmlUtil {
	
	/**
	 * 调用生成xml
	 */
	public void generatorXml() throws IOException {
		List<SDalx> dalxList = sGroupMapper.getAllDalxList();
		for (SDalx dalx : dalxList) {
			if(dalx.getStatus() == 0){
				if (dalx.getHasprj() == 1) {
					generatorXmlByTableName("D_PRJ" + dalx.getCode(),  dalx.getChname() + "-项目");
				}
				if (dalx.getHasvol() == 1) {
					generatorXmlByTableName("D_VOL" + dalx.getCode(),  dalx.getChname() + "-案卷");
				}
				generatorXmlByTableName("D_FILE" + dalx.getCode(),  dalx.getChname() + "-文件");
				generatorXmlByTableName("E_FILE" + dalx.getCode(),  dalx.getChname() + "-电子文件");
			}
		}
		List<WWjkgl> wjkglList = sGroupMapper.getAllWjkglList();
		for (WWjkgl wjkgl : wjkglList) {
			generatorXmlByTableName("W_QT" + wjkgl.getDid() ,  wjkgl.getWjkmc() + "-库");
			generatorXmlByTableName("E_FILEQT" + wjkgl.getDid() ,  wjkgl.getWjkmc() + "-电子文件库");
		}
	}
	
	public Table getTable(String xmlclasspath) {
		xmlclasspath = basePath + xmlclasspath;
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Table.class);
			Unmarshaller unmarshal = context.createUnmarshaller();
			FileReader reader = new FileReader(xmlclasspath);
			Table table = (Table) unmarshal.unmarshal(reader);
			return table;
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void generatorXmlByTableName(String tableName, String chName) throws IOException {
		File folder = new File(basePath);
		if(!folder.exists()){
			folder.mkdir();
		}
		File targetFile = new File(basePath + tableName + ".xml");
		if (targetFile.exists()) {
			return;
		}else{
			targetFile.createNewFile();
		}
		OutputStreamWriter writer = null;
		try {
			JAXBContext context = JAXBContext.newInstance(Table.class);
			Table table = new Table();
			table.setName(tableName);
			table.setFname("F_" + tableName);
			table.setChname(chName);
			List<Field> fields = new ArrayList<Field>();
			
			List<FDTable> fdtableList = sGroupMapper.getFtableList("F_"+tableName);
			for (FDTable tt : fdtableList) {
				if(isIgnore(tt.getFieldname().toUpperCase())){
					continue;
				}
				Field field = new Field();
				field.setChname(tt.getChname());
				field.setFieldname(tt.getFieldname());
				field.setFieldtype(tt.getFieldtype());
				field.setLength(tt.getLength());
				field.setNotnull(tt.getNotnull()==1);
				fields.add(field);
			}
			table.setFields(fields);
			Marshaller marshal = context.createMarshaller();
			marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			//marshal输出默认是UTF-8 所以这里需要指定 writer 是UTF-8的输出
			writer = new OutputStreamWriter(new FileOutputStream(targetFile),"UTF-8");
			marshal.marshal(table, writer);
		} catch (Exception e) {
			log.error(e.getMessage());
		}finally{
			writer.close();
		}
	}
	
	/**
	 * <p>Title: 传入字段的英文名称 如果是忽略列表返回ture </p>
	*/
	private Boolean isIgnore(String fieldName){
		Boolean isIgnore = false;
		for (String ig : ignoreFieldName) {
			if(ig.equals(fieldName)){
				isIgnore = true;
				break;
			}
		}
		return isIgnore;
	}
	
	@Autowired
	private SGroupMapper sGroupMapper;
	
	/** 忽略不需要的字段 */
	private String[] ignoreFieldName = {"EFILEID","XLH","BBH","SWT","BBH","STATUS","ATTR","ATTREX"
			,"CREATETIME","EDITOR","EDITTIME","DELTOR","DELTIME","DHYY","DID","PID", "RECEIVER"};

	private Logger log =  (Logger) LoggerFactory.getLogger(this.getClass());
	public static  String basePath = GlobalFinalAttr.class.getClassLoader().getResource("").getPath() + "/config/xml/";	
}
