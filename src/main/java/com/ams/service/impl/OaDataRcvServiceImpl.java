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
        // 获取发文目录下所有xml文件
        List<File> fwFiles = catchXnls(OAfwCatalogue);
        boolean flag = false;
        if (fwFiles != null && fwFiles.size() > 0) {
            for (File file : fwFiles) {
                flag = parseXml(file, "公司发文");
            }
        }
        listFiles = new ArrayList();
        // 获取函件发文目录下所有xml文件
        List<File> hjfwFiles = catchXnls(OAhjfwCatalogue);
        boolean flag1 = false;
        if (hjfwFiles != null && hjfwFiles.size() > 0) {
            for (File file : hjfwFiles) {
                flag1 = parseXml(file, "函件发文");
            }
        }
        listFiles = new ArrayList();
        // 获取收文目录下所有xml文件
        List<File> swFiles = catchXnls(OAswCatalogue);
        boolean flag2 = false;
        if (swFiles != null && swFiles.size() > 0) {
            for (File file : swFiles) {
                flag2 = parseXml(file, "公司收文");
            }
        }
        listFiles = new ArrayList();
        // 获取签报目录下所有xml文件
        List<File> qbFiles = catchXnls(OAqbCatalogue);
        boolean flag3 = false;
        if (qbFiles != null && qbFiles.size() > 0) {
            for (File file : qbFiles) {
                flag3 = parseXml(file, "公司签报");
            }
        }
        listFiles = new ArrayList();
        // 获取会议纪要目录下所有xml文件
        List<File> hyjyFiles = catchXnls(OAhyjyCatalogue);
        boolean flag4 = false;
        if (hyjyFiles != null && hyjyFiles.size() > 0) {
            for (File file : hyjyFiles) {
                flag4 = parseXml(file, "会议纪要");
            }
        }
//        if(flag1&flag2&flag3&flag4){
//            moveFile(movePath);//移动文件到存储的路径下
//        }
    }

    /**
     * 根据配置表解析xml信息
     */
    private boolean parseXml(File file, String wjlx) {
        String xmlPath = file.getAbsolutePath();//获得xml的绝对路径
        String absolutionPath = xmlPath.substring(0, xmlPath.lastIndexOf("\\") + 1);
        String oapath = "";
        oapath = FilenameUtils.normalize(xmlPath);
        oapath = oapath.replaceAll("\\\\", "/");//统一路径中的“\” 为“/”
        String oaid = wjlx + "_" + super.getOAid(oapath);//为了日后可以纠错查找将文件的上级文件夹名做为标识放入系统中
        boolean flag = false;
        String did = "-1";
        List<String> oaFields = new ArrayList<String>();
        try {
            //处理xml
            Document document = XmlObjUtil.xmlFile2Document(xmlPath);
            Element root = document.getRootElement();
            List<Element> elements = root.getChild(oaXmlInfostr).getChildren().get(0).getChildren();
            String oaName = "";
            String oaValue = "";
            //遍历数据表信息获得OA的字段
//            String oaFwFieldsSql = "select F1 from " + oaDfileMappingTable + " where F3 = '" + wjlx + "'";
            for (PTable pTable : dPtabList) {
                if(wjlx.equals(pTable.getF3())){
                    oaFields.add(pTable.getF1());
                }
            }
//             = jdbcDao.quert4List(oaFwFieldsSql);
            //组装数据表map
            Map<String, String> map = new HashMap();
            for (String oaField : oaFields) {
                for (Element ele : elements) {
                    oaName = ele.getAttributeValue("Name");
                    if (oaField.equals(oaName)) {
                        oaValue = ele.getAttributeValue("Value");
                        if (StringUtils.isNotBlank(oaValue)) {
                            map.put(oaField, oaValue);
                        }
                    }
                }
            }
            //添加数据表
            did = insertDfile4Map(map, getGwMappingArc(wjlx), dfileTableName, wjlx, oaid);
            if (!did.equals("-1")) {
                int efilesize = 0;
//                String oaSFwEFieldsSql = "select F1 from " + oaESFwMappingTable + " where F3 = '" +  + "'";
                List<String> oaSFwEFields = oaEFields.get(wjlx);
                String oaEName = "";
                String oaEValue = "";
                for (String oaSFwEField : oaSFwEFields) {
                    for (Element ele : elements) {
                        oaEName = ele.getAttributeValue("Name");
                        if (oaSFwEField.equals(oaEName)) {
                            oaEValue = ele.getAttributeValue("Value");
                            if (StringUtils.isNotBlank(oaEValue)) {
                                String[] efileNames = oaEValue.split(";");
                                for (String efileName : efileNames) {
                                    File efile = new File(absolutionPath + efileName);
                                    insertEfile(efile, did, efileName);
                                    efilesize++;
                                }
                            }
                        }
                    }
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
        String movePath = oaXmlLocalPath + File.separator
                + DateUtil.getCurrentDateStr() + File.separator + System.currentTimeMillis();
        try {
            File[] listFile = new File(catalogue).listFiles();
            for (File file : listFile) {
                FileUtils.moveDirectoryToDirectory(file, new File(movePath), true);
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
