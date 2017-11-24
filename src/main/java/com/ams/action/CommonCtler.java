package com.ams.action;

import ch.qos.logback.classic.Logger;
import com.ams.service.i.OaDataRcvService;
import com.ams.util.GlobalFinalAttr;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
public class CommonCtler {
    /**
     * 主页跳转
     */
    @RequestMapping(value = {"/index.html", "/"})
    public String gotoIndex() {
        return "index.jsp";
    }

    /**
     * 列出所有日志- add
     */
    @RequestMapping(value = "/viewLogList")
    public String viewLogList(Model model) {
        try {
            File[] listFile = new File(logHomeAdd).listFiles();
            model.addAttribute("listFile", listFile);
            model.addAttribute("fileType", "log");
            return "listLog.jsp";
        } catch (Exception e) {
            log.error("获取日志列表错误.", e);
            return "index.jsp";
        }
    }

    /**
     * 列出所有XML
     */
    @RequestMapping(value = "/viewXMLList")
    public String viewXMLList(Model model) {
        try {
            File[] listFile = new File(GlobalFinalAttr.XML_PATH).listFiles();
            model.addAttribute("listFile", listFile);
            model.addAttribute("fileType", "xml");
            return "listLog.jsp";
        } catch (Exception e) {
            log.error("获取日志列表错误.", e);
            return "index.jsp";
        }
    }

    /**
     * 查看日志
     */
    @RequestMapping("/viewLog")
    public void viewLog(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            response.setContentType("text/html;charset=GBK ");
            out = response.getWriter();
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
            out.println("<HTML>");
            out.println("<BODY>");
            out.println("<XMP>");
            String filePath = request.getParameter("logFilePath");
            String fileType = request.getParameter("fileType");
            if (StringUtils.isEmpty(filePath)) {
                out.print("获取日志错误!");
            } else {
                filePath = new String(filePath.getBytes("ISO-8859-1"), "UTF-8");
                File tempFile = new File((fileType.equals("xml") ? GlobalFinalAttr.XML_PATH : logHomeAdd)
                        + File.separatorChar + filePath);
                if (null != tempFile) {
                    List<String> stList = FileUtils.readLines(tempFile);
                    for (String str : stList) {
//						out.println(str+"<br/>");
                        out.println(str);
                    }
                } else {
                    out.print("获取日志错误!");
                }
            }
            out.println("</XMP>");
            out.println("</BODY>");
            out.println("</HTML>");
        } catch (Exception e) {
            out.println("读取日志错误" + e.getMessage());
            log.error("读取日志错误" + e.getMessage());
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * 初始化接口
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value="/refreshIF")
    public String refreshIF(HttpServletRequest request, HttpServletResponse response) throws IOException {
        oaDataRcvService.initIf();
        return "index.jsp";
    }
    @RequestMapping(value = "/oadataReceive")
    public String oaDataReceive() {
        oaDataRcvService.dataReceive();
        return "index.jsp";
    }

    @Autowired
    @Value("${interface.log.home.address}")
    private String logHomeAdd;
    @Autowired
    private OaDataRcvService oaDataRcvService;
    private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());
}
