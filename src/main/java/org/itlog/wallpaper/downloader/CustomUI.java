package org.itlog.wallpaper.downloader;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomUI extends MainUI {
    private final static Log log = Log.get();

    {
        picSizeInit();
        fileDirText.setText(URLDecoder.decode(getPath(), StandardCharsets.UTF_8));
        downloadBtn.addActionListener(e -> {
            progressBar1.setValue(0);
            if (!validateDir()) return;
            enable(false);
            try {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> {
                    DownloadService downloadService = new DownloadService(logArea, progressBar1, logScroll);
                    downloadService.download((String) picSizeCombo.getSelectedItem(), fileDirText.getText());
                    enable(true);
                });

            } catch (Exception ex) {
                logLn("出现异常:" + ex.getMessage());
                log.error(ex);
            }

        });
        dirBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setFileSelectionMode(1);
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                fileDirText.setText(file.getAbsolutePath());
            }
        });
    }

    public static String getPath() {
        String path = "";
        //jar 中没有目录的概念
        URL location = CustomUI.class.getProtectionDomain().getCodeSource().getLocation();//获得当前的URL
        File file = new File(location.getPath());//构建指向当前URL的文件描述符
        if (file.isDirectory()) {//如果是目录,指向的是包所在路径，而不是文件所在路径
            path = file.getAbsolutePath();//直接返回绝对路径
        } else {//如果是文件,这个文件指定的是jar所在的路径(注意如果是作为依赖包，这个路径是jvm启动加载的jar文件名)
            path = file.getParent();//返回jar所在的父路径
        }

        return path;
    }

    private void enable(boolean b) {
        downloadBtn.setEnabled(b);
        picSizeCombo.setEnabled(b);
        fileDirText.setEnabled(b);
    }

    private boolean validateDir() {
        String dir = fileDirText.getText();
        if (StrUtil.isBlank(dir)) {
            logLn("保存位置为空");
            return false;
        }
        try {
            FileUtil.mkdir(dir);
            return true;
        } catch (Exception ex) {
            logLn("保存位置不能生成文件夹");
        }
        return false;
    }


    private void logLn(String str) {
        log.info(str);
        logArea.append(str);
        logArea.append("\n");
        logScroll.getViewport().setViewPosition(new Point(0, logScroll.getVerticalScrollBar().getMaximum()));
    }


    private void picSizeInit() {
        picSizeCombo.addItem("1024x768");
        picSizeCombo.addItem("1280x720");
        picSizeCombo.addItem("1280x1024");
        picSizeCombo.addItem("1440x900");
        picSizeCombo.addItem("1920x1080");
        picSizeCombo.addItem("1920x1200");
        picSizeCombo.addItem("1920x1440");
        picSizeCombo.setSelectedIndex(4);
    }
}
