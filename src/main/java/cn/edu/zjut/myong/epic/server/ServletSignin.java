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

import cn.edu.zjut.myong.epic.Experiment;
import cn.edu.zjut.myong.epic.Game;
import cn.edu.zjut.myong.epic.GamePlayer;
import cn.edu.zjut.myong.epic.admin.AdminStation;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Address: /servlet/signin
 * Parameters:
 * [code] : the player's code defined in the configuration file.
 * [password] : the player's password defined in the configuration file.
 * Response: if successful, the response is a json object:
 * {
 *     group : group id,
 *     code : player code,
 *     role : player role in the game,
 *     login : successful or failure,
 *     deadline : no use now
 * }.
 */
public class ServletSignin extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        JsonObjectBuilder json = Json.createObjectBuilder();
        try {
            String code = request.getParameter("code").trim();
            String password = request.getParameter("password").trim();
            int grpId = 0;
            for (Game grp : Experiment.groups.values()) {
                if (grp.players.containsKey(code) && grp.players.get(code).password.equals(password) && grp.getStage() != Experiment.STAGE.finished && AdminStation.expStatus == AdminStation.ESTABLISHED) {
                    grpId = grp.id;
                    break;
                }
            }
            if (grpId > 0) {
                GamePlayer player = Experiment.groups.get(grpId).players.get(code);
                // player.stage = Experiment.STAGE.signin;
                json.add("group", grpId);
                json.add("code", code);
                json.add("role", player.role.toString());
                json.add("deadline", Experiment.groups.get(grpId).getDeadline(Experiment.STAGE.signin));
            }
            json.add("login", (grpId > 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.getWriter().print(json.build().toString());
    }
}
