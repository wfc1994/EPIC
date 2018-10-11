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
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.FileFilter;
import java.io.StringReader;
import java.util.*;

public class ICConfiguration implements Configuration {

    public static final int BOUNDARY_FIXED = 0;
    public static final int BOUNDARY_CYCLIC = 1;

    public static final int REPRODUCING_NUMBER = 0;
    public static final int REPRODUCING_RATE = 1;

    public static final int COEXIST_YES = 1;
    public static final int COEXIST_NO = 0;

    public static final int S_TEST = 0;
    public static final int S_QUESTIONNAIRE = 1;

    public static final String MP_MOV = "MOV";
    public static final String MP_REP = "REP";
    public static final String MP_DIE = "DIE";

    public static final String[] UNITS = new String[]{"$", "¥", "€"};

    public static final int SPATIAL = 1;
    public static final int NON_SPATIAL = 0;

    public static void clearClasses() {
        File[] classes = new File("./").listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".class"))
                    return true;
                else
                    return false;
            }
        });
        for (File cls : classes) {
            cls.delete();
        }
    }

    public static final int PUNISH_NONE = 0;
    public static final int PUNISH_PUNISH = 1;
    public static final int PUNISH_REWEARD = 2;
    public static final int PUNISH_ALL = 3;

    enum VARNAME {
        conf_players,
        conf_eid,
        conf_round,
        conf_K,
        conf_boundary,
        conf_ini_A,
        conf_ini_B,
        conf_viewable,
        conf_sec_A,
        conf_sec_B,
        conf_chat_emoji,
        conf_chat_text,
        conf_chat_range,
        conf_chat_limit,
        conf_instruction_full,
        conf_instruction_concise,
        conf_survey_questions,
        conf_instruction_duration,
        conf_test_duration,
        conf_com_duration,
        conf_decision_duration,
        conf_punish_duration,
        conf_disp_duration,
        conf_ques_duration,
        conf_dyn_A_neighbor,
        conf_dyn_B_neighbor,
        /*
        conf_dyn_A_rep_area,
        conf_dyn_B_rep_area,
        conf_dyn_A_rep_type,
        conf_dyn_B_rep_type,
        conf_dyn_A_rep_fcn,
        conf_dyn_A_death_fcn,
        conf_dyn_B_rep_fcn,
        conf_dyn_B_death_fcn,
        */
        conf_dyn_rules,
        conf_dyn_update,
        conf_dyn_cluster,
        conf_dyn_coexist,
        conf_dyn_A_win,
        conf_payoff_unit,
        conf_payoff_appearance,
        conf_payoff_a_price,
        conf_payoff_b_price,
        conf_punish_type,
        conf_punish_rate_AA,
        conf_punish_rate_AB,
        conf_punish_rate_BA,
        conf_punish_rate_BB
    };

    private JsonObject cJsonObject;
    private GameProperties gameProperties = new GameProperties();
    private Map<String, GamePlayer> players = new HashMap<>();
    private Map<Integer, Game> groups = new HashMap<>();
    private Map<Integer, Map<String, GamePlayer>> groupPlayers = new HashMap<>();

    private ICParameters params = new ICParameters();

    /**
     * 解析传送来的参数
     * @param json
     */
    public ICConfiguration(String json) throws Exception {
        JsonReader jsonReader = Json.createReader(new StringReader(json));
        cJsonObject = jsonReader.readObject();
    }

    @Override
    public String getExperimentId() {
        return cJsonObject.getString(VARNAME.conf_eid.name());
    }

    @Override
    public String getModelClassName() {
        return ICModel.class.getName();
    }

    @Override
    public Map<String, String> getModelParameters() {
        return params;
    }

    @Override
    public Map<String, GamePlayer> getPlayers(int groupId) {
        return groupPlayers.get(groupId);
    }

    @Override
    public Map<Integer, Game> getGroups() {
        return groups;
    }

    @Override
    public String[] getSlides() {
        String[] instructions = new String[2];
        instructions[0] = cJsonObject.getString(VARNAME.conf_instruction_concise.name());
        instructions[1] = cJsonObject.getString(VARNAME.conf_instruction_full.name());
        return instructions;
    }

    @Override
    public GameSurveyQuestion[] getQuestons() {
        JsonArray Qs = cJsonObject.getJsonArray(VARNAME.conf_survey_questions.name());
        GameSurveyQuestion[] questions = new GameSurveyQuestion[Qs.size()];
        for (int i = 0; i < Qs.size(); i++) {
            GameSurveyQuestion question = new GameSurveyQuestion();
            question.setType(Qs.getJsonObject(i).getInt("type"));
            question.setSubject(Qs.getJsonObject(i).getString("subject"));
            JsonArray opts = Qs.getJsonObject(i).getJsonArray("options");
            String[] options = new String[opts.size()];
            for (int j = 0; j < opts.size(); j++) {
                options[j] = opts.getString(j);
            }
            question.setOptions(options);
            int ans = Qs.getJsonObject(i).getInt("answer");
            if (ans < 0 || ans >= options.length)
                question.setAnswer("");
            else
                question.setAnswer(options[Qs.getJsonObject(i).getInt("answer")]);
            question.setScore(0);
            questions[i] = question;
        }
        return questions;
    }

    @Override
    public GameProperties getGameProgperties() {
        return gameProperties;
    }

    @Override
    public int getSurveyThreshold() {
        return 0;
    }

    @Override
    public long getStageDuration(Experiment.STAGE status) {
        int d = -1;
        switch (status) {
            case instruct:
                d = cJsonObject.getInt(VARNAME.conf_instruction_duration.name());
                break;
            case survey4test:
                d = cJsonObject.getInt(VARNAME.conf_test_duration.name());
                break;
            case communication:
                d = cJsonObject.getInt(VARNAME.conf_com_duration.name());
                break;
            case decision:
                d = cJsonObject.getInt(VARNAME.conf_decision_duration.name());
                break;
            case redistribution:
                d = cJsonObject.getInt(VARNAME.conf_punish_duration.name());
                break;
            case process:
                d = cJsonObject.getInt(VARNAME.conf_disp_duration.name());
                break;
            case survey4questionnaire:
                d = cJsonObject.getInt(VARNAME.conf_ques_duration.name());
                break;
        }
        if (d > 0) {
            return d * 1000;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isCommunicable() {
        if (getStageDuration(Experiment.STAGE.communication) <= 0)
            return false;
        else
            return true;
    }

    @Override
    public boolean isRedistributable() {
        if (getStageDuration(Experiment.STAGE.redistribution) <= 0
                || cJsonObject.getInt(VARNAME.conf_punish_type.name()) == PUNISH_NONE)
            return false;
        else
            return true;
    }

    @Override
    public String getDecisionContent() {
        return "4.decision_dyn.html";
    }

    @Override
    public String getRedistributionContent() {
        return "4.redistribution.html";
    }

    @Override
    public String getProcessContent() {
        return "5.process.html";
    }

    @Override
    public String getFinalContent() {
        return "6.final.html";
    }

    @Override
    public String getAdminCode() {
        return null;
    }

    @Override
    public String getAdminPassword() {
        return null;
    }

    @Override
    public boolean buildConfiguration() {
        try {
            // 基本参数
            gameProperties.nRound = cJsonObject.getInt(VARNAME.conf_round.name());
            gameProperties.inherit = true;

            // 构建Players
            JsonArray plst = cJsonObject.getJsonArray(VARNAME.conf_players.name());
            for (int i = 0; i < plst.size(); i++) {
                GamePlayer player = new GamePlayer();
                player.code = plst.getJsonArray(i).getString(0);
                player.password = plst.getJsonArray(i).getString(1);
                players.put(player.code, player);
            }

            // IC模型参数
            params.K = cJsonObject.getInt(VARNAME.conf_K.name());
            params.boundary = cJsonObject.getInt(VARNAME.conf_boundary.name());
            params.iniA = cJsonObject.getInt(VARNAME.conf_ini_A.name());
            params.iniB = cJsonObject.getInt(VARNAME.conf_ini_B.name());
            params.viewable = cJsonObject.getInt(VARNAME.conf_viewable.name());
            params.secA = cJsonObject.getInt(VARNAME.conf_sec_A.name());
            params.secB = cJsonObject.getInt(VARNAME.conf_sec_B.name());
            params.chatEmoji = cJsonObject.getInt(VARNAME.conf_chat_emoji.name());
            params.chatText = cJsonObject.getInt(VARNAME.conf_chat_text.name());
            params.chatRange = cJsonObject.getInt(VARNAME.conf_chat_range.name());

            JsonArray larray = cJsonObject.getJsonArray(VARNAME.conf_chat_limit.name());
            String[] limits = new String[larray.size()];
            for (int i = 0; i < larray.size(); i++)
                limits[i] = larray.getString(i);
            params.chatLimit = limits;

            String[] nghA = cJsonObject.getString(VARNAME.conf_dyn_A_neighbor.name()).split("-");
            params.nghA0 = Integer.parseInt(nghA[0]);
            params.nghA1 = Integer.parseInt(nghA[1]);
            String[] nghB = cJsonObject.getString(VARNAME.conf_dyn_B_neighbor.name()).split("-");
            params.nghB0 = Integer.parseInt(nghB[0]);
            params.nghB1 = Integer.parseInt(nghB[1]);
            /*
            String[] areaA = cJsonObject.getString(VARNAME.conf_dyn_A_rep_area.name()).split("-");
            params.areaA0 = Integer.parseInt(areaA[0]);
            params.areaA1 = Integer.parseInt(areaA[1]);
            String[] areaB = cJsonObject.getString(VARNAME.conf_dyn_B_rep_area.name()).split("-");
            params.areaB0 = Integer.parseInt(areaB[0]);
            params.areaB1 = Integer.parseInt(areaB[1]);
            params.rtypA = cJsonObject.getInt(VARNAME.conf_dyn_A_rep_type.name());
            params.rtypB = cJsonObject.getInt(VARNAME.conf_dyn_B_rep_type.name());
            */
            JsonArray ruleArray = cJsonObject.getJsonArray(VARNAME.conf_dyn_rules.name());
            params.rRulesA = ICModelRules.parseRuleList(ruleArray.getJsonArray(0));
            params.dRulesA = ICModelRules.parseRuleList(ruleArray.getJsonArray(1));
            params.rRulesB = ICModelRules.parseRuleList(ruleArray.getJsonArray(2));
            params.dRulesB = ICModelRules.parseRuleList(ruleArray.getJsonArray(3));
            /*
            params.calculatorA = ICCalculator.build(
                    randomName(20),
                    cJsonObject.getString(VARNAME.conf_dyn_A_rep_fcn.name()),
                    cJsonObject.getString(VARNAME.conf_dyn_A_death_fcn.name()));
            if (params.calculatorA == null)
                throw new Exception("A's reproducing and dying functions are invalid!");
            params.calculatorB = ICCalculator.build(
                    randomName(20),
                    cJsonObject.getString(VARNAME.conf_dyn_B_rep_fcn.name()),
                    cJsonObject.getString(VARNAME.conf_dyn_B_death_fcn.name()));
            if (params.calculatorB == null)
                throw new Exception("B's reproducing and dying functions are invalid!");
            */
            params.spatial = cJsonObject.getInt(VARNAME.conf_dyn_update.name());
            params.coexist = cJsonObject.getInt(VARNAME.conf_dyn_coexist.name());
            // params.cluster = cJsonObject.getJsonNumber(VARNAME.conf_dyn_cluster.name()).doubleValue();
            params.winA = cJsonObject.getJsonNumber(VARNAME.conf_dyn_A_win.name()).doubleValue();

            // 惩罚奖励参数
            params.put("punishment_type", cJsonObject.getInt(VARNAME.conf_punish_type.name()) + "");
            params.put("punishment_rate_AA",
                    cJsonObject.getJsonNumber(VARNAME.conf_punish_rate_AA.name()).doubleValue() + "");
            params.put("punishment_rate_AB",
                    cJsonObject.getJsonNumber(VARNAME.conf_punish_rate_AB.name()).doubleValue() + "");
            params.put("punishment_rate_BA",
                    cJsonObject.getJsonNumber(VARNAME.conf_punish_rate_BA.name()).doubleValue() + "");
            params.put("punishment_rate_BB",
                    cJsonObject.getJsonNumber(VARNAME.conf_punish_rate_BB.name()).doubleValue() + "");

            // 支付参数
            params.unit = cJsonObject.getString(VARNAME.conf_payoff_unit.name());
            params.feeAppear = cJsonObject.getJsonNumber(VARNAME.conf_payoff_appearance.name()).doubleValue();
            params.priceA = cJsonObject.getJsonNumber(VARNAME.conf_payoff_a_price.name()).doubleValue();
            params.priceB = cJsonObject.getJsonNumber(VARNAME.conf_payoff_b_price.name()).doubleValue();

            // 随机构建groups
            List<String> pks = new ArrayList<>();
            pks.addAll(players.keySet());
            Collections.shuffle(pks);
            for (int i = 0, grpId = 1; i < pks.size(); i = i + params.secA + params.secB, grpId++) {
                Game grp = new Game();
                grp.id = grpId;
                grp.modelParameters = params;
                Map<String, GamePlayer> gps = new HashMap<>(2);
                for (int j = i; j < i + params.secA; j++) {
                    GamePlayer pa = players.get(pks.get(j));
                    pa.role = GamePlayer.ROLE.A;
                    gps.put(pks.get(j), pa);
                }
                for (int j = i + params.secA; j < i + params.secA + params.secB; j++) {
                    GamePlayer pb = players.get(pks.get(j));
                    pb.role = GamePlayer.ROLE.B;
                    gps.put(pks.get(j), pb);
                }
                groups.put(grpId, grp);
                groupPlayers.put(grpId, gps);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
