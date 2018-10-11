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

package cn.edu.zjut.myong.epic;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exporting data of Game into given file format.
 */
public class GameExporter {

    /**
     * Generating 2D json array of selected data type (parc)
     * @param group group id
     * @param parc "players", "patterns", "actions", "revenues", or "costs"
     * @return
     * @throws Exception
     */
    public static JsonArrayBuilder export2Json(Game group, String parc) throws Exception {
        List<String> pList = new ArrayList<>();
        pList.addAll(group.players.keySet());
        Collections.sort(pList);

        JsonArrayBuilder jArray = Json.createArrayBuilder();
        switch (parc) {
            case "players" : {
                for (String player : pList)
                    jArray.add(player);
            }
            break;
            case "model" : {
                for (String pk : group.modelParameters.keySet()) {
                    JsonObjectBuilder obj = Json.createObjectBuilder();
                    obj.add(pk, group.modelParameters.get(pk));
                    jArray.add(obj);
                }
            }
            break;
            case "patterns": {
                for (int i = 0, j = 0; j < group.getCurrent(); j++) {
                    if (j == 0) {
                        JsonArrayBuilder row = Json.createArrayBuilder();
                        row.add(0);
                        row.add(group.initialPattern.toText());
                        jArray.add(row);
                    }
                    JsonArrayBuilder row = Json.createArrayBuilder();
                    row.add(j + 1);
                    row.add(group.patterns[j].toText());
                    jArray.add(row);
                }
            }
            break;
            case "actions": {
                for (int j = 0; j < group.getCurrent(); j++) {
                    JsonArrayBuilder row = Json.createArrayBuilder();
                    row.add(j + 1);
                    for (int k = 0; k < pList.size(); k++) {
                        row.add(group.actions[j].get(pList.get(k)).toText());
                    }
                    jArray.add(row);
                }
            }
            break;
            default: {
                // Nothing
            }
        }

        return jArray;
    }

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
            Row row = pSheet.createRow(i + 1);
            row.createCell(1).setCellValue(i + 1);
            row.createCell(2).setCellValue(pList.get(i));
            row.createCell(3).setCellValue(group.players.get(pList.get(i)).role.name());
        }

        // 模型参数
        XSSFSheet mSheet = workbook.createSheet("Model");
        {
            int i = 0;
            mSheet.createRow(++i).createCell(1).setCellValue(group.modelClass);
            for (String pn : group.modelParameters.keySet()) {
                Row row = mSheet.createRow(++i);
                row.createCell(1).setCellValue(pn);
                row.createCell(2).setCellValue(group.modelParameters.get(pn));
            }
        }

        // 状态变化
        XSSFSheet ptnSheet = workbook.createSheet("Patterns");
        for (int i = 0, j = 0; j < group.getCurrent(); j++) {
            Row row = ptnSheet.createRow(++i);
            if (j == 0) {
                row.createCell(1).setCellValue(0);
                row.createCell(2).setCellValue(group.initialPattern.toText());
                row = ptnSheet.createRow(++i);
            }
            row.createCell(1).setCellValue(j + 1);
            row.createCell(2).setCellValue(group.patterns[j].toText());
        }

        // 策略变化
        XSSFSheet aSheet = workbook.createSheet("Actions");
        for (int j = 0; j < group.getCurrent(); j++) {
            Row row = aSheet.createRow(j + 1);
            row.createCell(1).setCellValue(j + 1);
            for (int k = 0; k < pList.size(); k++) {
                row.createCell(k + 2).setCellValue(group.actions[j].get(pList.get(k)).toText());
            }
        }

        workbook.write(output);
    }

    public static void export2json(Game group, OutputStream output) throws Exception {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("players", export2Json(group, "players"));
        json.add("model", export2Json(group, "model"));
        json.add("patterns", export2Json(group, "patterns"));
        json.add("actions", export2Json(group, "actions"));
        json.add("revenues", export2Json(group, "revenues"));
        json.add("costs", export2Json(group, "costs"));

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output,"UTF-8"));
        writer.write(json.build().toString());
        writer.close();
    }
}
