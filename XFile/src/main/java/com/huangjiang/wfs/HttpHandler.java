package com.huangjiang.wfs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class HttpHandler implements HttpRequestHandler {

    private String webRoot;
    private String apkPath;

    public HttpHandler(final String webRoot, final String apkPath) {
        this.webRoot = webRoot;
        this.apkPath = apkPath;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response,
                       HttpContext context) throws HttpException, IOException {
        String target = URLDecoder.decode(request.getRequestLine().getUri(), "UTF-8");
        final File file = new File(this.webRoot, target);
        response.setStatusCode(HttpStatus.SC_OK);
        if ("/XFile.apk".equals(target)) {
            // 下载本地安装程序
            final File apkFile = new File(apkPath);
            String contentType = URLConnection.guessContentTypeFromName(apkFile.getAbsolutePath());
            contentType = null == contentType ? "charset=UTF-8"
                    : contentType + "; charset=UTF-8";
            FileEntity entity = new FileEntity(apkFile, contentType);
            response.setHeader("Content-Type", contentType);
            response.setEntity(entity);
        } else if (file.isDirectory()) {
            // 访问文件夹
            StringEntity entity = createWelcomeHtml();
            response.setHeader("Content-Type", "text/html");
            response.setEntity(entity);
        } else if (file.exists() && file.canRead()) {
            // 访问文件
            String contentType = URLConnection.guessContentTypeFromName(file.getAbsolutePath());
            contentType = null == contentType ? "charset=UTF-8"
                    : contentType + "; charset=UTF-8";
            FileEntity entity = new FileEntity(file, contentType);
            response.setHeader("Content-Type", contentType);
            response.setEntity(entity);
        } else {
            // 出错提示
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            StringEntity entity = new StringEntity("<html><body><h1>Error 403</h1></body></html>", "UTF-8");
            response.setHeader("Content-Type", "text/html");
            response.setEntity(entity);
        }
    }

    private StringEntity createWelcomeHtml() throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        sb.append("<head>");
        sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
        sb.append("<meta http-equiv=\"Cache-Control\" content=\"no-cache\"/>");
        sb.append("<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; minimum-scale=1.0; maximum-scale=2.0\"/>");
        sb.append("<meta name=\"MobileOptimized\" content=\"260\"/>");
        sb.append("<title>xfile download</title>");
        sb.append("<style type=\"text/css\" id=\"internalStyle\">");
        sb.append("html, body {word-wrap:break-word;}");
        sb.append("body {background:#464646;margin:0px;}");
        sb.append(".tip {background-color:#DDDDDD;height:60px;text-align:center;}");
        sb.append(".detail {margin:10px 0px 20px;padding-bottom:20px;padding-left:15px;padding-right:2px;text-align:center}");
        sb.append(".detail li {color:#cbcbcb;list-style-type: none;line-height: 2;font-family: SimHei;font-size: 16px}");
        sb.append("a:visited{ color:#ffffff;text-decoration:none; }");
        sb.append(".main{text-align:center; margin-top:20px;}");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<div class=\"tip\"><img style=\"width:40px;height:40px;margin-top:12px;\" src=\"/mnt/sdcard/XFile/.wfs/logo.png\" ><br/></div>");
        sb.append("<div class=\"main\"> <a href=\"/XFile.apk\"><img style=\"width:260px;\" src=\"/mnt/sdcard/XFile/.wfs/download.png\"/></a>");
        sb.append("<ul class=\"detail\">");
        sb.append("<li>轻型的手机文件传输工具!</li>");
        sb.append("<li>30M文件，只需15秒 </li>");
        sb.append("<li>无需WLAN环境，无需2G/3G</li>");
        sb.append("<li>任意文件格式,任意文件大小</li>");
        sb.append("</ul>");
        sb.append("</div>");
        sb.append("</body>");
        sb.append("</html>");
        return new StringEntity(sb.toString(), "UTF-8");
    }


}
