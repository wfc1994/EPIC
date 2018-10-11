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

import cn.edu.zjut.myong.epic.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by myong on 2017/8/17.
 */
public abstract class ICRedistributableModel implements GameModel {

    private static final long serialVersionUID = 2207510802123994579L;
    protected int type = ICConfiguration.PUNISH_NONE;
    protected double rateAA = 0.0;
    protected double rateAB = 0.0;
    protected double rateBA = 0.0;
    protected double rateBB = 0.0;

    public Map<String, Double> parseRedistributingAction(JsonObject robj) {
        Map<String, Double> acts = new HashMap<>();
        for (String code : robj.keySet()) {
            acts.put(code, robj.getJsonNumber(code).doubleValue());
        }
        return acts;
    }

    @Override
    public void setParameters(Map<String, String> params) throws Exception {
        if (params.containsKey("punishment_type")) {
            this.type = Integer.parseInt(params.get("punishment_type"));
        }
        if (params.containsKey("punishment_rate_AA")) {
            this.rateAA = Double.parseDouble(params.get("punishment_rate_AA"));
        }
        if (params.containsKey("punishment_rate_AB")) {
            this.rateAB = Double.parseDouble(params.get("punishment_rate_AB"));
        }
        if (params.containsKey("punishment_rate_BA")) {
            this.rateBA = Double.parseDouble(params.get("punishment_rate_BA"));
        }
        if (params.containsKey("punishment_rate_BB")) {
            this.rateBB = Double.parseDouble(params.get("punishment_rate_BB"));
        }
    }

    @Override
    public JsonObjectBuilder getJsonParameters() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("punishment_type", type);
        json.add("punishment_rate_AA", rateAA);
        json.add("punishment_rate_AB", rateAB);
        json.add("punishment_rate_BA", rateBA);
        json.add("punishment_rate_BB", rateBB);
        return json;
    }

    @Override
    public GamePattern redistribute(Game group, GamePattern state0, Map<String, GameAction> action) {
        ICRedistributablePattern ptn = (ICRedistributablePattern) state0;

        System.out.println("In-Redistribution");
        ((ICPattern) state0).print();

        for (int i = 0; i < group.playerCodes.length; i++) {
            for (int j = 0; j < group.playerCodes.length; j++) {
                if (i == j)
                    continue;
                // 计算i对j的惩罚或者奖励
                String icode = group.playerCodes[i];
                String jcode = group.playerCodes[j];
                ICRedistributableAction iAct = (ICRedistributableAction) action.get(group.playerCodes[i]);
                ptn.benefit.get(icode).redist = Arith.sub(ptn.benefit.get(icode).redist, Math.abs(iAct.redist.get
                        (jcode)));
                double r = 0;
                GamePlayer.ROLE irole = group.players.get(icode).role;
                GamePlayer.ROLE jrole = group.players.get(jcode).role;
                if (irole == GamePlayer.ROLE.A && jrole == GamePlayer.ROLE.A)
                    r = rateAA;
                else if (irole == GamePlayer.ROLE.A && jrole == GamePlayer.ROLE.B)
                    r = rateAB;
                else if (irole == GamePlayer.ROLE.B && jrole == GamePlayer.ROLE.A)
                    r = rateBA;
                else if (irole == GamePlayer.ROLE.B && jrole == GamePlayer.ROLE.B)
                    r = rateBB;
                /*
                if (group.players.get(jcode).role == GamePlayer.ROLE.A)
                    r = rateA;
                else
                    r = rateB;
                */
                ptn.benefit.get(jcode).redist = Arith.add(
                        ptn.benefit.get(jcode).redist,
                        Arith.mul(iAct.redist.get(jcode), r));
            }
        }

        System.out.println("Out-Redistribution");
        ((ICPattern) state0).print();

        return ptn;
    }
}
