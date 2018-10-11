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

import cn.edu.zjut.myong.epic.GameAction;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by myong on 2017/8/17.
 */
public abstract class ICRedistributableAction implements GameAction, Serializable {

    private static final long serialVersionUID = 2615592991721247072L;
    protected Map<String, Double> redist = new HashMap<>();

    public JsonObjectBuilder getJson() {
        JsonObjectBuilder obj = Json.createObjectBuilder();
        for (String code : redist.keySet()) {
            obj.add(code, redist.get(code));
        }
        return obj;
    }
}
