package com.ams.service.impl;

import ch.qos.logback.classic.Logger;
import com.ams.pojo.PTable;
import com.ams.service.BaseService;
import com.ams.service.i.OaDataRcvService;
import com.ams.util.DateUtil;
import com.ams.util.XmlObjUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    public void initIf(){
        super.init();
    }

    /**
     * 进入归档方法
     * 循环遍历数据文件夹
     */
    public void dataReceive() {
        listFiles = new ArrayList();
        if(MapUtils.isEmpty(ARCfieldtypes)){//初始化接口信息
            super.init();
        }
        try{
            readXml(oaZBpath,"ZB");
            readXml(oaNMpath,"NM");
            readXml(oaSYpath,"SY");
            readXml(oaYCpath,"YC");
            moveFile(oaFtpPath);
        }catch (Exception e){
            log.error("本次抓取错误"+e.getMessage());
        }
    }

    /**
     * 分别读取每个公司的文件
     * @param path
     */
    private void readXml(String path,String company){
        listFiles = new ArrayList();
        // 获取目录下所有xml文件
        List<File> Files = catchXnls(path);
        String message = "";
        boolean flag = false;
        if (Files != null && Files.size() > 0) {
            for (File file : Files) {
                message = parseXml (file, company);
                if(StringUtils.isNotBlank(message)) log.error(message);
            }
        }
    }

    /**
     * 根据配置表解析xml信息
     */
    private String parseXml(File file,String company) {
        String xmlPath = file.getAbsolutePath();//获得xml的绝对路径
        String oapath = "";
        oapath = FilenameUtils.normalize(xmlPath);
        oapath = oapath.replaceAll("\\\\", "/");//统一路径中的“\” 为“/”
        String oaid = super.getOAid(oapath);//为了日后可以纠错查找将文件的上级文件夹名做为标识放入系统中
        String result = "";
        String did = "-1";
        String type= "";
        String dfileTableName = "";
        String mapKey ="";//取缓存中的相应信息的key;
        Map<String,List<String>> efileMessage= null;
        List<String> filePaths = null;
        List<String> fileNames = null;
        String fileName = "";
        try {
            //处理xml
            Document document = XmlObjUtil.xmlFile2Document(xmlPath);
            Element root = document.getRootElement();
            //区分合同和文书的类型
            type = root.getChild("ams_form") != null? "HT":"WS";
            mapKey=  company+"_"+type;
            dfileTableName = tabNameMap.get(mapKey);
            //添加数据表
            did = insertDfile4Map(this.getdMessageMap(root,company,type), fieldMaps.get(mapKey), dfileTableName, oaid,company,type);
            //处理电子文件信息
            if (!did.equals("-1")) {
//            if (!did.equals("")) {
                int efilesize = 0;
                efileMessage = this.dealEfileMessage(root,type);
                filePaths = efileMessage.get("filePaths");
                fileNames = efileMessage.get("fileNames");
                for (int i = 0; i < filePaths.size(); i++) {
                    fileName =  fileNames.get(i).contains(".")?fileNames.get(i).split("\\.")[0]:fileNames.get(i);
                    File efile = new File(oaFtpEfilepath + filePaths.get(i));
                    insertEfile(efile, did, fileName,dfileTableName);
                    efilesize++;
                }
                //会写电子附件个数
                String updateefilestr = "update " + dfileTableName + " set filesnum=" + efilesize + " where id = " + did;
                jdbcDao.excute(updateefilestr);
            } else {
                result = "xml名为："+oaid+"的数据插入失败";
            }
        } catch (Exception e) {
            if (!did.equals("-1")) {
                jdbcDao.excute("delete from " + dfileTableName + " where id = " + did + "");
                System.out.println("异常情况，ID：" + did + "的文件被删除！");
            }
            throw new RuntimeException("数据处理异常！"+e.getMessage());
        }
        return result;
    }

    /**
     * 获得oa字段的对应值
     * @param root
     * @param company
     * @param type
     * @return
     */
    private Map<String,String> getdMessageMap(Element root,String company,String type){
        List<Element> dElements = null;
        if("HT".equalsIgnoreCase(type)){
            dElements = root.getChild("ams_form").getChild("formdata").getChildren("field");
        }else {
            dElements = root.getChild("ams").getChildren();
        }

        String oaName = "";
        String oaValue = "";
        //组装数据表map
        Map<String, String> map = new HashMap();
        Map<String,String> fieldMap = fieldMaps.get(company+"_"+type);
        for (String oaField : fieldMap.keySet()) {
            if("HT".equalsIgnoreCase(type)){//如果是合同档案
                for (Element ele : dElements) {
                    oaName = ele.getChild("fieldName").getValue();
                    if (StringUtils.isNotBlank(oaName) && oaField.equals(oaName)) {
                        oaValue = ele.getChild("fieldValue").getValue();
                        if (StringUtils.isNotBlank(oaValue)) {
                            map.put(oaField, oaValue);
                        }
                    }
                }
            }else{
                for (Element ele : dElements) {
                    oaName = ele.getName();
                    if (StringUtils.isNotBlank(oaName) && oaField.equalsIgnoreCase(oaName)) {
                        oaValue = ele.getValue();
                        if (StringUtils.isNotBlank(oaValue)) {
                            map.put(oaField, oaValue);
                        }
                    }
                }
            }
        }
        return  map;
    }
    private Map<String,List<String>> dealEfileMessage(Element root,String type){
        List<Element> eElements = null;
        List<String> filePaths = new ArrayList<String>();
        List<String> fileNames = new ArrayList<String>();
        Map<String,List<String>> result = new HashMap<String, List<String>>();
        String filepath = "";
        String fileName = "";
        if("HT".equalsIgnoreCase(type)){
            eElements = root.getChild("ams_form").getChild("filelist_form").getChildren("file_form");
            for (Element ele : eElements) {
                filepath = ele.getChild("path").getValue();
                fileName = ele.getChild("filename_form").getValue();
                filepath = filepath.replaceAll("\\\\\\\\", "/");
                filePaths.add(filepath);
                fileNames.add(fileName);
            }
            eElements = root.getChild("ams_form").getChild("filelist_formattach").getChildren("file_formattach");
            for (Element ele : eElements) {
                filepath = ele.getChild("attachpath").getValue();
                fileName = ele.getChild("filename_formattach").getValue();
                filepath = filepath.replaceAll("\\\\\\\\", "/");
                filePaths.add(filepath);
                fileNames.add(fileName);
            }
        }else{
            eElements = root.getChild("ams").getChildren("file_edoc");
            for (Element ele : eElements) {
                filepath = ele.getChild("path").getValue();
                fileName = ele.getChild("filename_edoc").getValue();
                filepath = filepath.replaceAll("\\\\\\\\", "/");
                filePaths.add(filepath);
                fileNames.add(fileName);
            }
            eElements = root.getChild("ams").getChildren("filesystemname_edocbody");
            for (Element ele : eElements) {
                filepath = ele.getChild("bodypath").getValue();
                fileName = ele.getChild("filename_edocbody").getValue();
                filepath = filepath.replaceAll("\\\\\\\\", "/");
                filePaths.add(filepath);
                fileNames.add(fileName);
            }
            eElements = root.getChild("ams").getChild("filelist_edocattach")
                    .getChildren("file_edocattach");
            for (Element ele : eElements) {
                filepath = ele.getChild("attachpath").getValue();
                fileName = ele.getChild("filename_edocattach").getValue();
                filepath = filepath.replaceAll("\\\\\\\\", "/");
                filePaths.add(filepath);
                fileNames.add(fileName);
            }
        }
        result.put("filePaths", filePaths);
        result.put("fileNames", fileNames);
        return result;
    }

    /**
     * MOVE
     */
    public void moveFile(String catalogue) {
        String movePathNew = movePath + DateUtil.getCurrentDateStr() + "/" + System.currentTimeMillis();
        try {
            File[] listFile = new File(catalogue).listFiles();
            for (File file : listFile) {
                FileUtils.moveDirectoryToDirectory(file, new File(movePathNew), true);
            }
            new File(catalogue).mkdir();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }


    /**
     * attain xml 获得执行路径下的文件
     *
     * @return
     */
    private List<File> catchXnls(String root) {
        File file = new File(root);
        File[] subFile = file.listFiles();
        String xmlPathName = "";
        if (subFile != null) {
            for (int i = 0; i < subFile.length; i++) {
                if (subFile[i].isDirectory()) {
                    if(subFile[i].getName().equalsIgnoreCase("File")){
                        continue;
                    }
                    catchXnls(subFile[i].getAbsolutePath());
                } else {
                    if (subFile[i].getName().endsWith(".xml")) {
                        xmlPathName = subFile[i].getAbsolutePath();
                        File xmlFile = new File(xmlPathName);
                        listFiles.add(xmlFile);
                    }
                }
            }
        }
        return listFiles;
    }
    private List<File> listFiles = new ArrayList();
    private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());
}
