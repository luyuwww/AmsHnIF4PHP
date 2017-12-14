package com.ams.service.impl;

import ch.qos.logback.classic.Logger;
import com.ams.pojo.PTable;
import com.ams.service.BaseService;
import com.ams.service.i.OaDataRcvService;
import com.ams.util.DateUtil;
import com.ams.util.XmlObjUtil;
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
        if(StringUtils.isBlank(dfileTableName)){//初始化接口信息
            super.init();
        }
        // 获取目录下所有xml文件
        List<File> Files = catchXnls(oaFtpPath);
        boolean flag = false;
        if (Files != null && Files.size() > 0) {
            for (File file : Files) {
                flag = parseXml (file);
            }
        }
        moveFile(oaFtpPath);
    }

    /**
     * 根据配置表解析xml信息
     */
    private boolean parseXml(File file) {
        String xmlPath = file.getAbsolutePath();//获得xml的绝对路径
        String oapath = "";
        String filepath = "";
        String fileName = "";
        String formName = "";
        oapath = FilenameUtils.normalize(xmlPath);
        oapath = oapath.replaceAll("\\\\", "/");//统一路径中的“\” 为“/”
        String oaid = super.getOAid(oapath);//为了日后可以纠错查找将文件的上级文件夹名做为标识放入系统中
        boolean flag = false;
        List<Element> dElements = null;
        List<Element> eElements = null;
        String did = "-1";
        try {
            //处理xml
            Document document = XmlObjUtil.xmlFile2Document(xmlPath);
            Element root = document.getRootElement();
            dElements = root.getChild("ams_form").getChild("formdata").getChildren("field");
            String oaName = "";
            String oaValue = "";
            //组装数据表map
            Map<String, String> map = new HashMap();
            for (String oaField : fieldMap.keySet()) {
                for (Element ele : dElements) {
                    oaName = ele.getChild("fieldName").getValue();
                    if (StringUtils.isNotBlank(oaName) && oaField.equals(oaName)) {
                        oaValue = ele.getChild("fieldValue").getValue();
                        if (StringUtils.isNotBlank(oaValue)) {
                            map.put(oaField, oaValue);
                        }
                    }
                }
            }
            //表单名称存储
            formName = root.getChild("ams_form").getChild("formname").getValue();
            //添加数据表
            did = insertDfile4Map(map, fieldMap, dfileTableName, oaid,formName);
            //处理电子文件信息
            if (!did.equals("-1")) {
//            if (!did.equals("")) {
                int efilesize = 0;
                //遍历file_from里所有附件信息
                eElements = root.getChild("ams_form").getChild("filelist_form").getChildren("file_form");
                for (Element ele : eElements) {
                    filepath = ele.getChild("path").getValue();
                    fileName = ele.getChild("filename_form").getValue();
                    filepath = filepath.replaceAll("\\\\\\\\", "/");
                    File efile = new File(oaFtpEfilepath + filepath);
                    insertEfile(efile, did, fileName);
                    efilesize++;
                }
                eElements = null;
                eElements = root.getChild("ams_form").getChild("filelist_formattach").getChildren("file_formattach");
                for (Element ele : eElements) {
                    filepath = ele.getChild("attachpath").getValue();
                    fileName = ele.getChild("filename_formattach").getValue();
                    filepath = filepath.replaceAll("\\\\\\\\", "/");
                    File efile = new File(oaFtpEfilepath + filepath);
                    insertEfile(efile, did, fileName);
                    efilesize++;
                }
                //会写电子附件个数
                String updateefilestr = "update " + dfileTableName + " set filesnum=" + efilesize + " where id = " + did;
                jdbcDao.excute(updateefilestr);
                flag = true;
            } else {
                return flag;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            if (!did.equals("-1")) {
                jdbcDao.excute("delete from " + dfileTableName + " where id = " + did + "");
                System.out.println("异常情况，ID：" + did + "的文件被删除！");
            }
        }
        return flag;
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
