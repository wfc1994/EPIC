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

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Address: /servlet/admin/monitor
 * No parameters
 * Response: a json object: {id : group id, round : current round, players : [{code : player code, stage : current stage} ...]}
 */
@SuppressWarnings("ALL")
public class ServletAdminMonitor extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/x-json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        JsonObjectBuilder json = Json.createObjectBuilder();
        try {
            JsonArrayBuilder jgrps = Json.createArrayBuilder();
            for (Game grp : Experiment.groups.values()) {
                JsonObjectBuilder jg = Json.createObjectBuilder();
                jg.add("id", grp.id);
                jg.add("round", grp.getCurrent() + 1);
                JsonArrayBuilder ps = Json.createArrayBuilder();
                for (GamePlayer player : grp.players.values()) {
                    JsonObjectBuilder jp = Json.createObjectBuilder();
                    jp.add("code", player.code);
                    jp.add("stage", grp.getStage().name());
                    ps.add(jp);
                }
                jg.add("players", ps);
                jgrps.add(jg);
            }
            json.add("groups", jgrps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.getWriter().print(json.build().toString());
    }
}
