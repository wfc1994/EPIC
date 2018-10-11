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

import cn.edu.zjut.myong.epic.Experiment;
import cn.edu.zjut.myong.epic.Game;
import cn.edu.zjut.myong.epic.GamePlayer;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Address: /servlet/waiting4decision
 * Parameters:
 * [group] : the player's group id.
 * [code] : the player's code.
 * Response: the (initial) game parameters in json format:
 * {
 *     current : current round,
 *     content : the html content for displaying decision interface,
 *     e_content : the html content for displaying the final statistical information of the game,
 *     deadline : the ending time of the stage
 * }
 */
@SuppressWarnings("ALL")
public class ServletHarvest extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/x-json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        JsonObjectBuilder json = Json.createObjectBuilder();
        try {
            // 获得用户的code和答案
            int grpId = Integer.parseInt(request.getParameter("group").trim());
            String code = request.getParameter("code").trim();
            Game grp = Experiment.groups.get(grpId);
            GamePlayer my = grp.players.get(code);
            //
            if (grp.getStage() == Experiment.STAGE.signin) {
                return;
            }
            //
            if (request.getParameterMap().containsKey("x") && request.getParameterMap().containsKey("y")) {
                int x = Integer.parseInt(request.getParameter("x"));
                int y = Integer.parseInt(request.getParameter("y"));
                boolean succ;
                if (my.role == GamePlayer.ROLE.A)
                    succ = Harvest.dViewers.get(grpId).harvestA(code, x, y);
                else
                    succ = Harvest.dViewers.get(grpId).harvestB(code, x, y);
                json.add("harvest", succ);
            }
            //
            ICParameters params = (ICParameters) Experiment.conf.getModelParameters();
            if (params.viewable == 1 || my.role == GamePlayer.ROLE.A)
                json.add("actionAs", Harvest.dViewers.get(grpId).getHarvestedA());
            else
                json.add("actionAs", Json.createArrayBuilder());
            if (params.viewable == 1 || my.role == GamePlayer.ROLE.B)
                json.add("actionBs", Harvest.dViewers.get(grpId).getHarvestedB());
            else
                json.add("actionBs", Json.createArrayBuilder());
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.getWriter().print(json.build().toString());
    }
}
