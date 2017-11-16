package com.ams.service.impl;

import ch.qos.logback.classic.Logger;
import com.ams.service.BaseService;
import com.ams.service.i.OaDataRcvService;
import com.ams.util.DateUtil;
import com.ams.util.XmlObjUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("oaDataRcvService")
public class OaDataRcvServiceImpl extends BaseService implements
        OaDataRcvService {

	/**
	 * INTEGRATION
	 */
	public void dataReceive() {
		// 获取发文目录下所有xml文件
		List<File> fwFiles = getFiles(OAfwCatalogue);
		if(fwFiles != null && fwFiles.size() >0){
			for (File file : fwFiles) {
				boolean flag = parseXml(file , "发文" , OAfwFileCatalogue);
				if(flag){
					moveFile(file , "发文");
				}
			}
		}
		listFiles = new ArrayList();
		// 获取收文目录下所有xml文件
		List<File> swFiles = getFiles(OAswCatalogue);
		if(swFiles != null && swFiles.size() >0){
			for (File file : swFiles) {
				boolean flag = parseXml(file , "收文" , OAswFileCatalogue);
				if(flag) {
					moveFile(file , "收文");
				}
			}
		}
		listFiles = new ArrayList();
		// 获取签报目录下所有xml文件
		List<File> qbFiles = getFiles(OAqbCatalogue);
		if(qbFiles != null && qbFiles.size() >0){
			for (File file : qbFiles) {
				boolean flag = parseXml(file , "签报" , OAqbFileCatalogue);
				if(flag){
					moveFile(file , "签报");
				}
			}
		}
	}

	/**
	 * PRASE
	 */
	private boolean parseXml(File file , String wjlx , String fjcatalague){
		String dfileTableName = "D_FILE" + wsCode;
		String xmlPath = file.getAbsolutePath();
		boolean flag = false;
		Integer maxDid = -1;
		try {
			Document document = XmlObjUtil.xmlFile2Document(xmlPath);
			Element root = document.getRootElement();
			List<Element> elements = root.getChild("ItemInfo").getChildren().get(0).getChildren();
			String oaName = "";
			String oaValue = "";
			String oaFwFieldsSql = "select F1 from " + oaDfileMappingTable + " where F3 = '"+wjlx+"'";
			List<String> oaFields = jdbcDao.quert4List(oaFwFieldsSql);
			Map<String, String> Map = new HashMap();
			for (String oaField : oaFields) {
                for(Element ele : elements){
                    if(ele.getName().contains("EFile")){
                        continue;
                    }else{
                        oaName = ele.getAttributeValue("Name");
                        if(oaField.equals(oaName)){
                            oaValue = ele.getAttributeValue("Value");
                            if(StringUtils.isNotBlank(oaValue)){
                                Map.put(oaField, oaValue);
                            }
                        }
                    }
                }
            }
			maxDid = insertDfile4Map(Map, getFwGwMappingArc(wjlx), dfileTableName);
			if(!maxDid.equals("-1")){
				for(Element ele : elements){
					if(ele.getName().contains("Field")){
						continue;
					}else{
						String eFileName = ele.getAttributeValue("EFileName");
						String fileType = ele.getAttributeValue("FileType");
						String cFileName = ele.getAttributeValue("CFileName");
						File efile = new File(fjcatalague +File.separator+ eFileName);
						if(efile.exists()){
							insertEfile(efile, wsCode, maxDid, fileType+"："+cFileName,"");
						}else{
							log.error(maxDid+":"+eFileName+"不存在！");
						}
					}
				}
				flag = true;
			}else{
				return flag;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			if(!maxDid.equals("-1")){
				jdbcDao.excute("delete from "+dfileTableName+" where did = "+maxDid+"");
				System.out.println("异常情况，DID："+maxDid+"的文件被删除！");
			}
		}
		return flag;
	}

	/**
	 * MOVE
	 * @param file
	 */
	public void moveFile(File file , String wjlx){
		String movePath = oaXmlLocalPath + File.separator
				+ DateUtil.getCurrentDateStr() + File.separator
				+ wjlx+System.currentTimeMillis();
			try {
				FileUtils.moveFileToDirectory(file, new File(movePath), true);
			} catch (IOException e) {
				e.printStackTrace();
				log.error(e.getMessage());
			}
		}


	/**
	 * attain xml
	 * @param rootPath
	 * @return
	 */
	private List<File> getFiles(String rootPath) {
		String path = rootPath;
		File[] listFile = new File(path).listFiles();
		if(listFile == null || listFile.length == 0){
			return null;
		}
		for (File file : listFile) {
			if (file != null) {
				if (file.isDirectory()) {
					String runPath = file.getAbsolutePath();
					getFiles(runPath);
				} else {
					listFiles.add(file);
				}
			}
		}
		return listFiles;

	}
	public static void main(String[] args) {
	}
	private List<File> listFiles = new ArrayList();
	private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());
}
