package org.itlog.wallpaper.downloader;

import cn.hutool.log.Log;

import javax.swing.*;
import java.net.URL;
import java.util.Objects;

public class Launcher {
    private final static Log log = Log.get();


    public static void main(String[] args) {
        CustomUI customUI = new CustomUI();
        CustomUI.frame = new JFrame("MainUI");
        JFrame frame = CustomUI.frame;
        frame.setContentPane(customUI.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setTitle("王者荣耀壁纸下载器");
        URL resource = CustomUI.class.getResource("/icon.ico");
        ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(resource).getPath());
        frame.setIconImage(imageIcon.getImage());

    }

}
