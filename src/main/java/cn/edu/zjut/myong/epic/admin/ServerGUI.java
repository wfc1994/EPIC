/*
 * Copyright 2017 Yong Min
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.edu.zjut.myong.epic.admin;

import cn.edu.zjut.myong.epic.Experiment;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

/**
 * Created by myong on 2017/8/12.
 */
public class ServerGUI {
    private JPanel mainPane;
    private JButton startBtn;
    private JButton stopBtn;
    private JLabel infoLabel;
    private JPanel centerPane;
    private JTextField textName;
    private JTextField textPassword;
    private JTextField textPort;

    public ServerGUI() {
        loadConfigurationFile();

        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveConfigurationFile();
                    new Thread() {
                        @Override
                        public void run() {
                            AdminStation.startServer();
                            try {
                                /*
                                Thread.sleep(1000);
                                //创建一个URI实例
                                java.net.URI uri = java.net.URI.create(" http://127.0.0.1:" + Integer.parseInt
                                        (textPort.getText().trim()) + "/admin/index.html");
                                //获取当前系统桌面扩展
                                java.awt.Desktop dp = java.awt.Desktop.getDesktop();
                                //判断系统桌面是否支持要执行的功能
                                if(dp.isSupported(java.awt.Desktop.Action.BROWSE)){
                                    //获取系统默认浏览器打开链接
                                    dp.browse(uri);
                                }
                                */
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    startBtn.setEnabled(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (Experiment.db != null)
                        Experiment.db.closeConnection();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    System.exit(1);
                }
            }
        });
    }

    private void saveConfigurationFile() {
        try {
            Properties prop = new Properties();
            prop.setProperty("name", textName.getText().trim());
            prop.setProperty("password", textPassword.getText().trim());
            prop.setProperty("port", Integer.parseInt(textPort.getText().trim()) + "");
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream("./admin.properties"), "UTF-8"));
            prop.store(writer, "");
            writer.close();
            // JOptionPane.showMessageDialog(mainPane, "Saved.");
            infoLabel.setText("Configuration file is saved");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainPane, "Cannot save file.");
        }
    }

    private String loadConfigurationFile() {
        StringBuffer conf = new StringBuffer();
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("./admin.properties"));
            textName.setText(prop.getProperty("name"));
            textPassword.setText(prop.getProperty("password"));
            textPort.setText(prop.getProperty("port"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conf.toString();
    }

    public static void main(String[] args) throws Exception {
        // UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        JFrame splashFrame = new JFrame();
        // Image img = splashFrame.getToolkit().getImage("./web/images/eglamour.png");

        // JLabel splash = new JLabel(new ImageIcon("./web/images/eglamour.png"));
        // splash.setText("<html><img src='./web/images/eglamour.png' /><html>");

        final Image image = ImageIO.read(new FileInputStream("./web/images/eglamour.png"));
        JPanel splash = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, image.getWidth(this), image.getHeight(this),
                        this);
                // super.paintComponent(g);
            }
        };
        splash.setPreferredSize(new Dimension(image.getWidth(splash), image.getHeight(splash)));
        splashFrame.getContentPane().add(splash, BorderLayout.CENTER);
        splashFrame.setUndecorated(true);
        splashFrame.setSize(image.getWidth(splash), image.getHeight(splash));
        splashFrame.setLocationRelativeTo(null);
        splashFrame.setVisible(true);
        splashFrame.setResizable(false);

        // com.jtattoo.plaf.aero.AeroLookAndFeel.setTheme("Green-Small-Font", "", "");
        // com.jtattoo.plaf.fast.FastLookAndFeel
        // com.jtattoo.plaf.mint.MintLookAndFeel
        // com.jtattoo.plaf.mcwin.McWinLookAndFeel
        // com.jtattoo.plaf.smart.SmartLookAndFeel
        // com.jtattoo.plaf.aero.AeroLookAndFeel
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        JFrame frame = new JFrame("Interacting Commons");
        ServerGUI home = new ServerGUI();
        ImageIcon img = new ImageIcon("./web/images/logo64.png");
        frame.setIconImage(img.getImage());
        frame.setContentPane(home.mainPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);
        Thread.sleep(5000);
        splashFrame.setVisible(false);
        splashFrame.dispose();
        frame.setVisible(true);
        frame.setResizable(true);
    }
}
