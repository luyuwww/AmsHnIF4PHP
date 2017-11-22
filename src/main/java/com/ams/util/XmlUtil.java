package com.ams.util;

import ch.qos.logback.classic.Logger;
import com.ams.pojo.jaxb.Table;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileNotFoundException;
import java.io.FileReader;

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
    private Boolean isIgnore(String fieldName) {
        Boolean isIgnore = false;
        for (String ig : ignoreFieldName) {
            if (ig.equals(fieldName)) {
                isIgnore = true;
                break;
            }
        }
        return isIgnore;
    }

    /**
     * 忽略不需要的字段
     */
    private String[] ignoreFieldName = {"EFILEID", "XLH", "BBH", "SWT", "BBH", "STATUS", "ATTR", "ATTREX"
            , "CREATETIME", "EDITOR", "EDITTIME", "DELTOR", "DELTIME", "DHYY", "DID", "PID", "RECEIVER"};

    private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());
    public static String basePath = GlobalFinalAttr.class.getClassLoader().getResource("").getPath() + "/config/xml/";
}
