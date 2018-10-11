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
package cn.edu.zjut.myong.epic.server;

import cn.edu.zjut.myong.epic.*;
import cn.edu.zjut.myong.epic.model.ICConfiguration;


import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
//import bulie.*;

/**
 * Address: /servlet/waiting4instruction
 * Parameters:
 * [group] : the player's group id.
 * [code] : the player's code.
 * Response: the instruction slides or video in json format:
 * {
 *     slide : [file1, file2, ...],
 *     deadline : the ending time of this stage
 * }
 */
public class ServletWaiting4Main extends HttpServlet {
    public static String[] pattern = new String[100];
    public static String[] action = new String[100];
    public static String[] action_all = new String[100];
    public static int[] status = new int[100];
    public static int[] durtime = new int[100];

//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("text/x-json");
//        response.setCharacterEncoding("UTF-8");
//        response.setStatus(HttpServletResponse.SC_OK);
//        JsonObjectBuilder json = Json.createObjectBuilder();
//        // 获得用户的code
//        int grpId = Integer.parseInt(request.getParameter("group").trim());
//        String code = request.getParameter("code").trim();
//        int grpStage = Integer.parseInt(request.getParameter("stage").trim());
//        Game grp = Experiment.groups.get(grpId);
//        GamePlayer my = grp.players.get(code);
//
//        if(grpStage==17)
//        {
//            pattern = request.getParameter("pattern").trim();
//        }
//        json.add("d_pattern",pattern);
//    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/x-json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        JsonObjectBuilder json = Json.createObjectBuilder();
        // 获得用户的code
        int grpId = Integer.parseInt(request.getParameter("group").trim());
        String code = request.getParameter("code").trim();
        int grpStage = Integer.parseInt(request.getParameter("stage").trim());
        Game grp = Experiment.groups.get(grpId);
        GamePlayer my = grp.players.get(code);
        int i = Integer.parseInt(code.substring(code.length() - 1));



        try {
            if(grpStage==1)
            {
                // 等待所有用户登录完毕
                grp.waitAtStateus(code, Experiment.STAGE.instruct);
                while (grp.getStage() != Experiment.STAGE.instruct) {
                    Thread.sleep(100);
                }
                // 幻灯片
                JsonArrayBuilder slides = Json.createArrayBuilder();
                for (String s : Experiment.slides) {
                    slides.add(s);
                }
                json.add("slide", slides);
                // 截止时间
                json.add("duration_instruct", grp.getDuration(Experiment.STAGE.instruct));
            }

            if(grpStage==2)
            {
                JsonArrayBuilder slides = Json.createArrayBuilder();
                for (String s : Experiment.slides) {
                    slides.add(s);
                }
                json.add("slide", slides);
            }

            if(grpStage==3 || grpStage==14)
            {
                if (grp.getStage() == Experiment.STAGE.signin) {
                    return;
                }
                // 需要何种问题
                int type = Integer.parseInt(request.getParameter("type").trim());
                // 目标状态
                Experiment.STAGE target;
                if (type == ICConfiguration.S_TEST)
                    target = Experiment.STAGE.survey4test;
                else
                    target = Experiment.STAGE.survey4questionnaire;
                // 等待所有用户完毕
                grp.waitAtStateus(code, target);
                while (grp.getStage() != target) {
                    Thread.sleep(100);
                }
                // 题目
                JsonArrayBuilder question = Json.createArrayBuilder();
                for (GameSurveyQuestion s : Experiment.questions) {
                    if (s.getType() != type)
                        continue;
                    JsonObjectBuilder q = Json.createObjectBuilder();
                    q.add("subject", s.getSubject());
                    q.add("score", s.getScore());
                    JsonArrayBuilder opts = Json.createArrayBuilder();
                    if (s.getOptions() != null && s.getOptions().length > 0) {
                        for (String o : s.getOptions())
                            opts.add(o);
                    }
                    q.add("options", opts);
                    question.add(q);
                }
                json.add("question", question);
                json.add("duration_target", grp.getDuration(target));
            }

            if(grpStage==5)
            {
                if (grp.getStage() == Experiment.STAGE.signin) {
                    return;
                }
                // 获得调查答案
                if (grp.getCurrent() <= 0) {
                    List<String> answers = new ArrayList<>();
                    JsonParser parser = Json.createParser(new StringReader(request.getParameter("answer")));
                    while (parser.hasNext()) {
                        JsonParser.Event e = parser.next();
                        if (e == JsonParser.Event.VALUE_STRING) {
                            answers.add(parser.getString());
                        }
                    }
                    my.answer = answers.toArray(new String[]{});
                }
                // 等待所有用户进入聊天环节
                grp.waitAtStateus(code, Experiment.STAGE.communication);
                while (grp.getStage() != Experiment.STAGE.communication) {
                    Thread.sleep(100);
                }

                // 游戏参数
                json.add("current", grp.getCurrent());
                if (grp.getCurrent() == 0) {
                    json.add("round", Experiment.gameProperties.nRound);
                    json.add("inherit", Experiment.gameProperties.inherit);
                    json.add("initial", Experiment.model.getJsonInitial());
                    // 模型参数也加入
                    json.add("model", Experiment.model.getJsonParameters());
                    // 博弈者序列
                    JsonArrayBuilder plst = Json.createArrayBuilder();
                    for (String c : grp.playerCodes) {
                        JsonObjectBuilder ply = Json.createObjectBuilder();
                        ply.add("code", c);
                        ply.add("role", grp.players.get(c).role.name());
                        plst.add(ply);
                    }
                    json.add("all_players", plst);
                    // 后各项的页面
                    json.add("d_content", Experiment.conf.getDecisionContent());
                    if (Experiment.conf.getRedistributionContent() != null)
                        json.add("r_content", Experiment.conf.getRedistributionContent());
                    json.add("p_content", Experiment.conf.getProcessContent());
                    json.add("e_content", Experiment.conf.getFinalContent());
                }
                // 截止时间
                json.add("communicable", Experiment.conf.isCommunicable());
                json.add("duration_communication", Experiment.groups.get(grpId).getDuration(Experiment.STAGE.communication));
            }

            if(grpStage==6)
            {
                json.add("current", grp.getCurrent());
                json.add("round", Experiment.gameProperties.nRound);
                json.add("model", Experiment.model.getJsonParameters());
                // 博弈者序列
                JsonArrayBuilder plst = Json.createArrayBuilder();
                for (String c : grp.playerCodes) {
                    JsonObjectBuilder ply = Json.createObjectBuilder();
                    ply.add("code", c);
                    ply.add("role", grp.players.get(c).role.name());
                    plst.add(ply);
                }
                json.add("all_players", plst);
            }

            if(grpStage==7)
            {
                if (grp.getStage() == Experiment.STAGE.signin) {
                    return;
                }
                // 等待所有用户考试完毕
                grp.waitAtStateus(code, Experiment.STAGE.decision);
                while (grp.getStage() != Experiment.STAGE.decision) {
                    Thread.sleep(100);
                }
                JsonArrayBuilder plst = Json.createArrayBuilder();
                for (String c : grp.playerCodes) {
                    JsonObjectBuilder ply = Json.createObjectBuilder();
                    ply.add("code", c);
                    ply.add("role", grp.players.get(c).role.name());
                    plst.add(ply);
                }
                json.add("all_players", plst);
                json.add("model", Experiment.model.getJsonParameters());
                json.add("current_decision", grp.getCurrent());
                //json.add("deadline", grp.getDeadline(Experiment.STAGE.decision));
                json.add("duration_decision", grp.getDuration(Experiment.STAGE.decision));
            }

            if(grpStage==8)
            {
                json.add("current", grp.getCurrent());
                json.add("round", Experiment.gameProperties.nRound);
                json.add("initial", Experiment.model.getJsonInitial());
                json.add("model", Experiment.model.getJsonParameters());
                json.add("d_content", Experiment.conf.getDecisionContent());

                // 博弈者序列
                JsonArrayBuilder plst = Json.createArrayBuilder();
                for (String c : grp.playerCodes) {
                    JsonObjectBuilder ply = Json.createObjectBuilder();
                    ply.add("code", c);
                    ply.add("role", grp.players.get(c).role.name());
                    plst.add(ply);
                }
                json.add("all_players", plst);

                JsonArrayBuilder slides = Json.createArrayBuilder();
                for (String s : Experiment.slides) {
                    slides.add(s);
                }
                json.add("slide", slides);
            }

            if(grpStage==9)
            {
                if (grp.getStage() == Experiment.STAGE.signin) {
                    return;
                }
                // 用户行为
                GameAction action = Experiment.model.parseAction(request.getParameter("action"));
                grp.actions[grp.getCurrent()].put(code, action);
                // 等待所有用户决策完毕
                grp.waitAtStateus(code, Experiment.STAGE.redistribution);
                while (grp.getStage() != Experiment.STAGE.redistribution) {
                    Thread.sleep(100);
                }
                // 运行模型
                grp.simulate();
                json.add("current", grp.getCurrent());
                json.add("model", Experiment.model.getJsonParameters());
                // 返回结果，这两个是直接返回json格式了
                json.add("all_redistribution", grp.getAllActions(grp.getCurrent()));
                json.add("last_redistribution", grp.patterns[grp.getCurrent()].toJson());
                //json.add("deadline", grp.getDeadline(Experiment.STAGE.redistribution));
                json.add("duration_redistribution", grp.getDuration(Experiment.STAGE.redistribution));
            }

            if(grpStage==10)
            {
                json.add("current", grp.getCurrent());
                json.add("round", Experiment.gameProperties.nRound);
                json.add("model", Experiment.model.getJsonParameters());
                if (Experiment.conf.getRedistributionContent() != null)
                    json.add("r_content", Experiment.conf.getRedistributionContent());
                // 博弈者序列
                JsonArrayBuilder plst = Json.createArrayBuilder();
                for (String c : grp.playerCodes) {
                    JsonObjectBuilder ply = Json.createObjectBuilder();
                    ply.add("code", c);
                    ply.add("role", grp.players.get(c).role.name());
                    plst.add(ply);
                }
                json.add("all_players", plst);
            }

            if(grpStage==11)
            {
                if (grp.getStage() == Experiment.STAGE.signin) {
                    return;
                }
                // 用户行为
                GameAction action = Experiment.model.parseAction(request.getParameter("action"));
                grp.actions[grp.getCurrent()].put(code, action);
                // 等待所有用户决策完毕
                grp.waitAtStateus(code, Experiment.STAGE.process);
                while (grp.getStage() != Experiment.STAGE.process) {
                    Thread.sleep(100);
                }
                // 运行模型
                if (Experiment.conf.isRedistributable()) {
                    grp.redistribute();
                } else {
                    grp.simulate();
                }

                // 返回结果
                json.add("all_process", grp.getAllActions(grp.getCurrent()));
                json.add("last_process", grp.patterns[grp.getCurrent()].toJson());
                //json.add("deadline", grp.getDeadline(Experiment.STAGE.process));
                json.add("duration_process", grp.getDuration(Experiment.STAGE.process));
            }

            if(grpStage==12)
            {
                json.add("current", grp.getCurrent());
                json.add("round", Experiment.gameProperties.nRound);

                json.add("model", Experiment.model.getJsonParameters());
                json.add("p_content", Experiment.conf.getProcessContent());
            }

//            if(grpStage==120)
//            {
//                Bulie theBulie = null;
//                try {
//                    theBulie = new Bulie();
//                    theBulie.waitForFigures();
//                    theBulie.bulie();
//                    System.out.println("bulie生成");
//                } catch (Exception e) {
//                    System.out.println("Exception: " + e.toString());
//                }
//            }

            if(grpStage==13)
            {
                json.add("current", grp.getCurrent());
                json.add("round", Experiment.gameProperties.nRound);
                if (grp.getStage() == Experiment.STAGE.signin) {
                    return;
                }
                // 等待所有用户决策完毕
                grp.waitAtStateus(code, Experiment.STAGE.next);
                while (grp.getStage() != Experiment.STAGE.next) {
                    Thread.sleep(100);
                }
            }

            if(grpStage==16)
            {
                json.add("round", Experiment.gameProperties.nRound);
                json.add("model", Experiment.model.getJsonParameters());
                // 博弈者序列
                JsonArrayBuilder plst = Json.createArrayBuilder();
                for (String c : grp.playerCodes) {
                    JsonObjectBuilder ply = Json.createObjectBuilder();
                    ply.add("code", c);
                    ply.add("role", grp.players.get(c).role.name());
                    plst.add(ply);
                }
                json.add("all_players", plst);
                json.add("e_content", Experiment.conf.getFinalContent());
            }

            if(grpStage==17)
            {
                pattern[i] = request.getParameter("pattern").trim();
            }
            if(pattern[i]!=null)
            {
                json.add("d_pattern",pattern[i]);
            }

            if(grpStage==18)
            {
                action[i] = request.getParameter("action").trim();
            }
            if(action[i]!=null)
            {
                json.add("d_action",action[i]);
            }

            if(grpStage==19)
            {
                action_all[i] = request.getParameter("action_all").trim();
            }
            if(action_all[i]!=null)
            {
                json.add("d_action_all",action_all[i]);
            }

            if(grpStage==20)
            {
                status[i] = Integer.parseInt(request.getParameter("status").trim());
            }
            if(status[i]<100)
            {
                json.add("d_status",status[i]);
            }

            if(grpStage==21)
            {
                durtime[i] = Integer.parseInt(request.getParameter("dur").trim());
            }
            if(durtime[i]>=0)
            {
                json.add("d_time",durtime[i]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        response.getWriter().print(json.build().toString());
    }
}
