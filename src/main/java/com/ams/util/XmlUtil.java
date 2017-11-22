package com.ams.util;

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

import com.ams.pojo.jaxb.Field;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Logger;
import com.ams.pojo.FDTable;
import com.ams.pojo.SDalx;
import com.ams.pojo.WWjkgl;
import com.ams.pojo.jaxb.Table;

@Component("createXmlUtil")
public class XmlUtil {
	
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
	
	/** 忽略不需要的字段 */
	private String[] ignoreFieldName = {"EFILEID","XLH","BBH","SWT","BBH","STATUS","ATTR","ATTREX"
			,"CREATETIME","EDITOR","EDITTIME","DELTOR","DELTIME","DHYY","DID","PID", "RECEIVER"};

	private Logger log =  (Logger) LoggerFactory.getLogger(this.getClass());
	public static  String basePath = GlobalFinalAttr.class.getClassLoader().getResource("").getPath() + "/config/xml/";	
}
