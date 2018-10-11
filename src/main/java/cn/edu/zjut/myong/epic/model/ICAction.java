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

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ICAction extends ICRedistributableAction implements Serializable {

    private static final long serialVersionUID = -8473963604430107194L;

    public static final int NONE = 0;
    public static final int EXTRACT = 1;

    protected int[][] action;

    @Override
    public JsonArrayBuilder toJson() {
        JsonArrayBuilder row = Json.createArrayBuilder();
        for (int i = 0; i < action.length; i++) {
            JsonArrayBuilder column = Json.createArrayBuilder();
            for (int j = 0; j < action[i].length; j++) {
                column.add(action[i][j]);
            }
            row.add(column);
        }

        JsonArrayBuilder json = Json.createArrayBuilder();
        json.add(row);
        json.add(super.getJson());
        return json;
    }

    @Override
    public String toText() {
        return toJson().build().toString();
    }

    public void print() {
        for (int i = 0; i < action.length; i++) {
            for (int j = 0; j < action[i].length; j++) {
                if (action[i][j] == EXTRACT)
                    System.out.print("(" + i + " " + j + ")");
            }
        }
        System.out.println("");
    }

    public List<Grid2DIndex> print2positions() {
        List<Grid2DIndex> pos = new ArrayList<>();
        for (int i = 0; i < action.length; i++) {
            for (int j = 0; j < action[i].length; j++) {
                if (action[i][j] == EXTRACT)
                    pos.add(new Grid2DIndex(i, j));
            }
        }
        return pos;
    }
}
