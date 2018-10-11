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

import java.io.Serializable;
import java.util.HashMap;

public class ICParameters extends HashMap<String, String> implements Serializable {

    private static final long serialVersionUID = -7140850803229592378L;

    public int K, boundary, iniA, iniB, nghA0, nghA1, nghB0, nghB1, spatial, coexist, viewable, secA, secB, chatEmoji, chatText, chatRange;
    public String unit;
    public double winA, feeAppear, priceA, priceB;
    public String[] chatLimit;
    public ICModelRules rRulesA, dRulesA, rRulesB, dRulesB;

    // public int areaA0, areaA1, areaB0, areaB1, rtypA, rtypB;
    // public String fcnRA, fcnRB, fcnDA, fcnDB;
}
