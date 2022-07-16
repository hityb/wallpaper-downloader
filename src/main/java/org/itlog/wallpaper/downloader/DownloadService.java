package org.itlog.wallpaper.downloader;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;
import cn.hutool.setting.dialect.Props;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DownloadService {
    private static final Log log = Log.get();
    private final JTextArea logArea;
    private final JProgressBar progressBar1;
    private final JScrollPane logScroll;

    Props headerProp;
    Props parameterProp;

    public DownloadService(JTextArea logArea, JProgressBar progressBar1, JScrollPane logScroll) {
        this.logArea = logArea;
        this.progressBar1 = progressBar1;
        this.logScroll = logScroll;
    }

    HashMap<String, Integer> size_index_map = new HashMap<>();

    {
        size_index_map.put("1024x768", 2);
        size_index_map.put("1280x720", 3);
        size_index_map.put("1280x1024", 4);
        size_index_map.put("1440x900", 5);
        size_index_map.put("1920x1080", 6);
        size_index_map.put("1920x1200", 7);
        size_index_map.put("1920x1440", 8);
    }


    private void initProps() {
        headerProp = new Props("header.properties");
        String temp = "\n##########http 请求参数 header############";
        logLn(temp);
        log.debug(temp);
        for (Map.Entry<Object, Object> entry : headerProp.entrySet()) {
            logLn(entry.getKey() + "=" + entry.getValue());
            log.debug(entry.getKey() + "=" + entry.getValue());
        }
        parameterProp = new Props("parameter.properties");
        temp = "\n##########http 请求参数 parameter############";
        logLn(temp);
        log.debug(temp);

        for (Map.Entry<Object, Object> entry : parameterProp.entrySet()) {
            logLn(entry.getKey() + "=" + entry.getValue());
            log.debug(entry.getKey() + "=" + entry.getValue());
        }
    }

    public void download(final String size, String fileDir) {
        initProps();
        String url = "https://apps.game.qq.com/cgi-bin/ams/module/ishow/V1.0/query/workList_inc.cgi";
        logLn("开始从 %s 下载数据", url);
        log.info("开始从 {} 下载数据", url);

        Map<String, Object> parameters = new HashMap<>();
        for (Map.Entry<Object, Object> entry : parameterProp.entrySet()) {
            parameters.put(entry.getKey().toString(), entry.getValue().toString());
        }
        parameters.put("_", System.currentTimeMillis());

        Map<String, String> header = new HashMap<>();
        for (Map.Entry<Object, Object> entry : headerProp.entrySet()) {
            header.put(entry.getKey().toString(), entry.getValue().toString());
        }
        parameters.put("page", 0);
        parameters.put("iListNum", 20);

        HttpResponse response = HttpRequest.get(url)
                .form(parameters)
                .headerMap(header, true)
                .execute();
        Assert.notNull(response, "请求数据失败");
        String body = response.body();
        String decode = URLDecoder.decode(body, StandardCharsets.UTF_8);
        JSONObject jsonObject = JSON.parseObject(decode);
        String totalPic = jsonObject.getString("iTotalLines");
        Assert.notBlank(totalPic, "获取不到完整数据[iTotalLines]");
        logLn("总计%s张图片", totalPic);
        log.info("总计{}张图片", totalPic);
        Integer iTotalPages = jsonObject.getInteger("iTotalPages");
        Assert.notBlank(totalPic, "获取不到完整数据[iTotalPages]");

        progressBar1.setString("0/" + totalPic);
        progressBar1.setMaximum(Integer.parseInt(totalPic));

        int successCount = 0;
        int sizeIndex = size_index_map.get(size);

        for (int i = 0; i < iTotalPages; i++) {
            parameters.put("page", i);
            response = HttpRequest.get(url)
                    .form(parameters)
                    .headerMap(header, true)
                    .execute();
            body = response.body();
            decode = URLDecoder.decode(body, StandardCharsets.UTF_8);
            jsonObject = JSON.parseObject(decode);
            JSONArray list = jsonObject.getJSONArray("List");
            if (list.isEmpty()) {
                break;
            }

            for (Object o : list) {

                JSONObject jo = (JSONObject) o;
                String prodId = jo.getString("iProdId");
                String sProdName = jo.getString("sProdName");
                sProdName = sProdName.replaceAll("[\\s\\\\/:*?\"<>|]", "_");

                String imgUrl = jo.getString("sProdImgNo_" + sizeIndex).replace("/200", "/0");
                logLn("\n下载[%s], %s", sProdName, imgUrl);
                log.info("\n下载[{}], {}", sProdName, imgUrl);
                String filePath = fileDir + "/" + prodId + "_" + sProdName + ".jpg";
                downloadPic(imgUrl, filePath);
                successCount++;
                process(successCount, Integer.parseInt(totalPic));
            }
        }
        logLn("全部下载完成");

    }

    public void downloadPic(String url, String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            logLn("%s已经存在", filePath);
            log.info("{}已经存在", filePath);
            return;
        }
        long size = HttpUtil.downloadFile(url, filePath);
        logLn("保存文件%s，size=%s", filePath, size);
        log.info("保存文件{}，size={}", filePath, size);

    }


    private void logLn(String str, Object... args) {
        logArea.append(String.format(str, args));
        logArea.append("\n");
        logScroll.getViewport().setViewPosition(new Point(0, logScroll.getVerticalScrollBar().getMaximum()));
    }

    private void process(int successCount, int totalPic) {
        progressBar1.setString(successCount + "/" + totalPic);

        progressBar1.setValue(successCount);

    }
}
