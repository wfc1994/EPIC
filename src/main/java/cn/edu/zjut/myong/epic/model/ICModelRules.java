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

import javax.json.JsonArray;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ICModelRules implements Serializable {

    private static final long serialVersionUID = -4804695984602007288L;

    private List<ICModelRule> rules;

    public static ICModelRules parseRuleList(JsonArray array) {
        ICModelRules rules = new ICModelRules();
        rules.rules = new ArrayList<>();
        for (int r = 0; r < array.size(); r++) {
            JsonArray eles = (JsonArray) array.get(r);
            ICModelRule rule = new ICModelRule();
            rule.lbA = eles.getInt(0);
            rule.ubA = eles.getInt(1);
            rule.lbB = eles.getInt(2);
            rule.ubB = eles.getInt(3);
            rule.expr = eles.getString(4);
            rule.expr = rule.expr.replace("log", "Math.log");
            rule.expr = rule.expr.replace("exp", "Math.exp");
            rule.expr = rule.expr.replace("pow", "Math.pow");
            rule.expr = rule.expr.replace("abs", "Math.abs");
            rule.expr = rule.expr.replace("sqrt", "Math.sqrt");
            rule.expr = rule.expr.replace("sin", "Math.sin");
            rule.expr = rule.expr.replace("cos", "Math.cos");
            rule.expr = rule.expr.replace("tan", "Math.tan");
            rule.expr = rule.expr.replace("ceil", "Math.ceil");
            rule.expr = rule.expr.replace("floor", "Math.floor");
            rule.expr = rule.expr.replace("round", "Math.round");
            rule.expr = rule.expr.replace("min", "Math.min");
            rule.expr = rule.expr.replace("max", "Math.max");
            rules.rules.add(rule);
        }
        return rules;
    }

    public double getValue(int Na, int Nb) {
        for (int i = 0; i < rules.size(); i++) {
            if (Na >= rules.get(i).lbA && Na <= rules.get(i).ubA
                    && Nb >= rules.get(i).lbB && Nb <= rules.get(i).ubB) {
                try {
                    return Double.parseDouble(
                            JavaScriptEngine.engine.eval("a=" + Na + ";b=" + Nb + ";" + rules.get(i).expr).toString());
                } catch (Exception e) {
                    return 0;
                }
            }
        }
        return 0;
    }
}

class ICModelRule implements Serializable {

    private static final long serialVersionUID = 6981719603972639348L;

    public int lbA = 0;
    public int ubA = 0;
    public int lbB = 0;
    public int ubB = 0;
    public String expr = "0";
}