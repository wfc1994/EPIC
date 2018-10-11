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

package cn.edu.zjut.myong.epic.model;

import cn.edu.zjut.myong.epic.ChatMessage;
import cn.edu.zjut.myong.epic.Game;
import cn.edu.zjut.myong.epic.GamePlayer;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Exporting data of Game into given file format.
 */
public class ICGameExporter {

    private static final SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Exporting all data to a excel file via a OutputStream.
     * @param group group id
     * @param output
     * @throws Exception
     */
    public static void export2xlsx(Game group, OutputStream output) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // 玩家列表
        XSSFSheet pSheet = workbook.createSheet("Players");
        List<String> pList = new ArrayList<>();
        pList.addAll(group.players.keySet());
        Collections.sort(pList);
        for (int i = 0; i < pList.size(); i++) {
            GamePlayer player = group.players.get(pList.get(i));
            Row row = pSheet.createRow(i + 1);
            int column = 0;

            row.createCell(column++).setCellValue(i + 1);
            row.createCell(column++).setCellValue(pList.get(i));
            row.createCell(column++).setCellValue(player.role.name());

            for (int j = 0; j < player.answer.length; j++) {
                row.createCell(column++).setCellValue(player.answer[j]);
            }

            if (player.result != null)
                for (int j = 0; j < player.result.length; j++) {
                    row.createCell(column++).setCellValue(player.result[j]);
                }
        }

        // 状态变化
        ICParameters modelParameters = (ICParameters) group.modelParameters;
        int stepNum = 4;
        XSSFSheet ptnSheet = workbook.createSheet("Patterns");
        int row = 0;
        for (int i = -1; i <= group.getCurrent(); i++) {
            ICPattern ptn;
            if (i == -1) {
                ptn = (ICPattern) group.initialPattern;
            } else {
                ptn = (ICPattern) group.patterns[i];
            }

            for (int j = 0; j < ICModel.SIMSTEP.values().length; j++) {
                if (ICModel.SIMSTEP.values()[j] == ICModel.SIMSTEP.mov
                        && ((ICParameters) group.modelParameters).spatial == ICConfiguration.SPATIAL)
                    continue;
                int c = 0;
                StringBuilder places = new StringBuilder();
                for (Grid2DIndex idx : ptn.patternA.get(ICModel.SIMSTEP.values()[j])) {
                    c++;
                    places.append(idx.toString());
                }
                Row line = ptnSheet.createRow(row++);
                line.createCell(0).setCellValue(i + 1);
                line.createCell(1).setCellValue(ICModel.SIMSTEP.values()[j].name());
                line.createCell(2).setCellValue("A");
                line.createCell(3).setCellValue(c);
                line.createCell(4).setCellValue(places.toString());
            }

            for (int j = 0; j < ICModel.SIMSTEP.values().length; j++) {
                if (ICModel.SIMSTEP.values()[j] == ICModel.SIMSTEP.mov
                        && ((ICParameters) group.modelParameters).spatial == ICConfiguration.SPATIAL)
                    continue;
                int c = 0;
                StringBuilder places = new StringBuilder();
                for (Grid2DIndex idx : ptn.patternB.get(ICModel.SIMSTEP.values()[j])) {
                    c++;
                    places.append(idx.toString());
                }
                Row line = ptnSheet.createRow(row++);
                line.createCell(0).setCellValue(i + 1);
                line.createCell(1).setCellValue(ICModel.SIMSTEP.values()[j].name());
                line.createCell(2).setCellValue("B");
                line.createCell(3).setCellValue(c);
                line.createCell(4).setCellValue(places.toString());
            }
        }

        // 策略变化
        XSSFSheet aSheet = workbook.createSheet("Actions");
        row = 0;
        for (int i = 0; i <= group.getCurrent(); i++) {
            ICPattern ptn;
            if (i == -1) {
                ptn = (ICPattern) group.initialPattern;
            } else {
                ptn = (ICPattern) group.patterns[i];
            }

            for (int k = 0; k < pList.size(); k++) {
                Row line = aSheet.createRow(row++);
                line.createCell(0).setCellValue(i + 1);
                line.createCell(1).setCellValue(pList.get(k));
                ICAction action = (ICAction) group.actions[i].get(pList.get(k));
                List<Grid2DIndex> pos = action.print2positions();
                StringBuilder r = new StringBuilder();
                for (int x = 0; x < pos.size(); x++) {
                    r.append(pos.get(x).toString());
                }
                line.createCell(2).setCellValue(pos.size());
                line.createCell(3).setCellValue(r.toString());

                ICRedistributableBenefit benefit = ptn.benefit.get(pList.get(k));
                line.createCell(4).setCellValue(benefit.income);
                line.createCell(5).setCellValue(benefit.redist);

                for (int h = 0; h < pList.size(); h++) {
                    int iv = 0;
                    if (action.redist.containsKey(pList.get(h)))
                        iv = action.redist.get(pList.get(h)).intValue();
                    line.createCell(6 + h).setCellValue(iv);
                }
            }
        }

        // 聊天
        XSSFSheet cSheet = workbook.createSheet("Message");
        List<ChatMessage> msgs = group.chat.getMsg(null);
        for (int i = 0; i < msgs.size(); i++) {
            Row line = cSheet.createRow(i);
            line.createCell(0).setCellValue(dFormat.format(msgs.get(i).time));
            line.createCell(1).setCellValue(msgs.get(i).sender);
            String v = "NONE";
            switch (msgs.get(i).audience) {
                case ChatMessage.EVERYONE:
                    v = "EVERYONE";
                    break;
                case ChatMessage.ALLIANCE:
                    v = "ALLIANCE";
                    break;
                case ChatMessage.OPPONENT:
                    v = "OPPONENT";
                    break;
            }
            line.createCell(2).setCellValue(v);
            line.createCell(3).setCellValue(msgs.get(i).content);
        }

        workbook.write(output);
    }

    public static void export2json(Game group, OutputStream output) throws Exception {
        JsonObjectBuilder json = Json.createObjectBuilder();

        List<String> pList = new ArrayList<>();
        pList.addAll(group.players.keySet());
        Collections.sort(pList);

        // 玩家列表
        JsonArrayBuilder jab0 = Json.createArrayBuilder();
        for (String player : pList)
            jab0.add(group.players.get(player).toJon());
        json.add("player", jab0);

        // 状态变化
        JsonArrayBuilder jab1 = Json.createArrayBuilder();
        for (int j = 0; j < group.getCurrent(); j++) {
            if (j == 0) {
                jab1.add(group.initialPattern.toJson());
            }
            jab1.add(group.patterns[j].toJson());
        }
        json.add("pattern", jab1);

        // 行为
        JsonArrayBuilder jab2 = Json.createArrayBuilder();
        for (int j = 0; j < group.getCurrent(); j++) {
            JsonObjectBuilder obj = Json.createObjectBuilder();
            for (String player : pList)
                obj.add(player, group.actions[j].get(player).toJson());
            jab2.add(obj);
        }
        json.add("action", jab2);

        // 聊天
        JsonArrayBuilder jab3 = Json.createArrayBuilder();
        List<ChatMessage> msgs = group.chat.getMsg(null);
        for (int i = 0; i < msgs.size(); i++)
            jab3.add(msgs.get(i).toJson());
        json.add("message", jab3);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output,"UTF-8"));
        writer.write(json.build().toString());
        writer.close();
    }

    public static void export2csv(Game group, OutputStream output) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();

        List<String> pList = new ArrayList<>();
        pList.addAll(group.players.keySet());
        Collections.sort(pList);

        ZipOutputStream out = new ZipOutputStream(output);

        // 玩家列表
        StringBuilder strp = new StringBuilder();
        for (int i = 0; i < pList.size(); i++) {
            GamePlayer player = group.players.get(pList.get(i));
            strp.append((i + 1) + ",");
            strp.append(pList.get(i) + ",");
            strp.append(player.role.name() + ",");
            for (int j = 0; j < player.answer.length; j++) {
                strp.append(player.answer[j] + ",");
            }
            if (player.result != null) {
                for (int j = 0; j < player.result.length; j++) {
                    strp.append(player.result[j] + ",");
                }
            }
            strp.deleteCharAt(strp.length() - 1);
            strp.append("\n");
        }
        ZipEntry entry0 = new ZipEntry("player.csv");
        out.putNextEntry(entry0);
        out.write(strp.toString().getBytes("UTF-8"));


        // 状态变化
        ICParameters modelParameters = (ICParameters) group.modelParameters;
        StringBuilder strn = new StringBuilder();
        for (int i = -1; i <= group.getCurrent(); i++) {
            ICPattern ptn;
            if (i == -1) {
                ptn = (ICPattern) group.initialPattern;
            } else {
                ptn = (ICPattern) group.patterns[i];
            }

            for (int j = 0; j < ICModel.SIMSTEP.values().length; j++) {
                if (ICModel.SIMSTEP.values()[j] == ICModel.SIMSTEP.mov
                        && ((ICParameters) group.modelParameters).spatial == ICConfiguration.SPATIAL)
                    continue;
                int c = 0;
                StringBuilder places = new StringBuilder();
                for (Grid2DIndex idx : ptn.patternA.get(ICModel.SIMSTEP.values()[j])) {
                    c++;
                    places.append(idx.toString());
                }
                strn.append((i+1) + ",");
                strn.append(ICModel.SIMSTEP.values()[j].name() + ",");
                strn.append("A,");
                strn.append(c + ",");
                strn.append(places.toString());
                strn.append("\n");
            }

            for (int j = 0; j < ICModel.SIMSTEP.values().length; j++) {
                if (ICModel.SIMSTEP.values()[j] == ICModel.SIMSTEP.mov
                        && ((ICParameters) group.modelParameters).spatial == ICConfiguration.SPATIAL)
                    continue;
                int c = 0;
                StringBuilder places = new StringBuilder();
                for (Grid2DIndex idx : ptn.patternB.get(ICModel.SIMSTEP.values()[j])) {
                    c++;
                    places.append(idx.toString());
                }
                strn.append((i+1) + ",");
                strn.append(ICModel.SIMSTEP.values()[j].name() + ",");
                strn.append("B,");
                strn.append(c + ",");
                strn.append(places.toString());
                strn.append("\n");
            }
        }
        ZipEntry entry1 = new ZipEntry("pattern.csv");
        out.putNextEntry(entry1);
        out.write(strn.toString().getBytes("UTF-8"));

        // 策略变化
        StringBuilder stra = new StringBuilder();
        for (int i = 0; i <= group.getCurrent(); i++) {
            ICPattern ptn;
            if (i == -1) {
                ptn = (ICPattern) group.initialPattern;
            } else {
                ptn = (ICPattern) group.patterns[i];
            }

            for (int k = 0; k < pList.size(); k++) {
                stra.append((i+1) + ",");
                stra.append(pList.get(k) + ",");
                ICAction action = (ICAction) group.actions[i].get(pList.get(k));
                List<Grid2DIndex> pos = action.print2positions();
                StringBuilder r = new StringBuilder();
                for (int x = 0; x < pos.size(); x++) {
                    r.append(pos.get(x).toString());
                }
                stra.append(pos.size() + ",");
                stra.append(r.toString() + ",");

                ICRedistributableBenefit benefit = ptn.benefit.get(pList.get(k));
                stra.append(benefit.income + ",");
                stra.append(benefit.redist + ",");

                for (int h = 0; h < pList.size(); h++) {
                    int iv = 0;
                    if (action.redist.containsKey(pList.get(h)))
                        iv = action.redist.get(pList.get(h)).intValue();
                    stra.append(benefit.redist + ",");
                }
                strp.deleteCharAt(strp.length() - 1);
                stra.append("\n");
            }
        }
        ZipEntry entry2 = new ZipEntry("action.csv");
        out.putNextEntry(entry2);
        out.write(stra.toString().getBytes("UTF-8"));

        // 聊天
        StringBuilder strc = new StringBuilder();
        List<ChatMessage> msgs = group.chat.getMsg(null);
        for (int i = 0; i < msgs.size(); i++) {
            strc.append(dFormat.format(msgs.get(i).time) + ",");
            strc.append(msgs.get(i).sender + ",");
            String v = "NONE";
            switch (msgs.get(i).audience) {
                case ChatMessage.EVERYONE:
                    v = "EVERYONE";
                    break;
                case ChatMessage.ALLIANCE:
                    v = "ALLIANCE";
                    break;
                case ChatMessage.OPPONENT:
                    v = "OPPONENT";
                    break;
            }
            strc.append(v + ",");
            strc.append(msgs.get(i).content);
            strc.append("\n");
        }
        ZipEntry entry3 = new ZipEntry("message.csv");
        out.putNextEntry(entry3);
        out.write(strc.toString().getBytes("UTF-8"));

        workbook.write(output);
    }
}
