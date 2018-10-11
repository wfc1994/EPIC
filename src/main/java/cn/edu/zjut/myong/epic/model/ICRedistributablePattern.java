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

import cn.edu.zjut.myong.epic.GamePattern;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by myong on 2017/8/17.
 */
public abstract class ICRedistributablePattern implements GamePattern {

    private static final long serialVersionUID = -4666296747931234479L;
    public Map<String, ICRedistributableBenefit> benefit = new HashMap<>();

    public JsonObjectBuilder getJson() {
        JsonObjectBuilder ply = Json.createObjectBuilder();
        for (String code : benefit.keySet()) {
            JsonObjectBuilder obj = Json.createObjectBuilder();
            obj.add("income", benefit.get(code).income);
            obj.add("cost", benefit.get(code).cost);
            obj.add("redist", benefit.get(code).redist);
            ply.add(code, obj);
        }
        return ply;
    }
}
