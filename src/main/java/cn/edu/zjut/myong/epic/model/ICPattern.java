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
import javax.json.JsonObjectBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ICPattern extends ICRedistributablePattern {
    private static final long serialVersionUID = 697486660984087398L;

    public static final int EMPTY = 0;
    public static final int EXIST = 1;

    // protected Map<Grid2DIndex, int[]> patternA = new HashMap<>();
    // protected Map<Grid2DIndex, int[]> patternB = new HashMap<>();

    protected Map<ICModel.SIMSTEP, Set<Grid2DIndex>> patternA = new HashMap<>(4);
    protected Map<ICModel.SIMSTEP, Set<Grid2DIndex>> patternB = new HashMap<>(4);

    public void print() {
        for (ICModel.SIMSTEP stp : ICModel.SIMSTEP.values()) {
            if (patternA.containsKey(stp)) {
                System.out.println(stp + " {" + patternA.get(stp).size() + "}: " + patternA.get(stp));
            }
            if (patternB.containsKey(stp)) {
                System.out.println(stp + " {" + patternB.get(stp).size() + "}: " + patternB.get(stp));
            }
        }
    }

    @Override
    public JsonArrayBuilder toJson() {
        JsonObjectBuilder aObj = Json.createObjectBuilder();
        for (ICModel.SIMSTEP stp : patternA.keySet()) {
            JsonArrayBuilder nodes = Json.createArrayBuilder();
            for (Grid2DIndex point : patternA.get(stp)) {
                JsonArrayBuilder v = Json.createArrayBuilder();
                v.add(point.x);
                v.add(point.y);
                nodes.add(v);
            }
            aObj.add(stp.name(), nodes);
        }

        JsonObjectBuilder bObj = Json.createObjectBuilder();
        for (ICModel.SIMSTEP stp : patternB.keySet()) {
            JsonArrayBuilder nodes = Json.createArrayBuilder();
            for (Grid2DIndex point : patternB.get(stp)) {
                JsonArrayBuilder v = Json.createArrayBuilder();
                v.add(point.x);
                v.add(point.y);
                nodes.add(v);
            }
            bObj.add(stp.name(), nodes);
        }

        JsonArrayBuilder json = Json.createArrayBuilder();
        json.add(aObj);
        json.add(bObj);
        json.add(super.getJson());
        return json;
    }

    @Override
    public String toText() {
        return toJson().build().toString();
    }
}