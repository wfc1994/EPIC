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

package cn.edu.zjut.myong.epic.db;

import cn.edu.zjut.myong.epic.Experiment;
import cn.edu.zjut.myong.epic.Game;
import cn.edu.zjut.myong.epic.GameExporter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by myong on 2017/8/13.
 */
public class BerkeleyDBGUI {
    private JPanel mainPane;
    private JComboBox<String> expBox;
    private JComboBox<String> grpBox;
    private JComboBox<String> fmtBox;
    private JButton saveButton;

    private BerkeleyDBGeneral<Game> berkeley;
    private final JFileChooser fc = new JFileChooser();

    public BerkeleyDBGUI() {

        try {
            if (Experiment.db == null) {
                berkeley = new BerkeleyDBGeneral();
                berkeley.openEnvironment("./db");
            } else {
                berkeley = (BerkeleyDBGeneral<Game>) Experiment.db;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> dNames = berkeley.env.getDatabaseNames();
        expBox.addItem("");
        for (String name : dNames)
            expBox.addItem(name);

        fmtBox.addItem("Excel");
        fmtBox.addItem("JSON");
        expBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String item = (String) expBox.getSelectedItem();
                if (item.isEmpty())
                    return;
                berkeley.openConnection("./db", item);
                List<String> dbs = berkeley.getAllKeys();

                grpBox.removeAllItems();
                grpBox.addItem("");
                for (String db : dbs)
                    grpBox.addItem(db);
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String gKey = (String) grpBox.getSelectedItem();
                String fmt = (String) fmtBox.getSelectedItem();
                if (gKey == null || gKey.isEmpty()) {
                    JOptionPane.showMessageDialog(mainPane, "Please select Experiment & Game!");
                    return;
                }
                Game grp = berkeley.get(gKey);

                int returnVal = fc.showOpenDialog(mainPane);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        if (!file.exists())
                            file.createNewFile();
                        if (fmt.equals("JSON")) {
                            GameExporter.export2json(grp, new FileOutputStream(file));
                        } else {
                            GameExporter.export2xlsx(grp, new FileOutputStream(file));
                        }
                        JOptionPane.showMessageDialog(mainPane, "Saved!");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(mainPane, "Cannot saved!");
                    }
                }
            }
        });
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("Berkeley DB Exporter");
        BerkeleyDBGUI home = new BerkeleyDBGUI();
        frame.setContentPane(home.mainPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 300);
        frame.setVisible(true);
        frame.setResizable(true);
    }
}
