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

/**
 * Calling this servlet when the player is prepraring to enter the next round of the game.
 * Address: /servlet/waiting4next
 * Parameters:
 * [group] : the player's group id.
 * [code] : the player's code.
 * No response
 */
@SuppressWarnings("ALL")
public class ServletFinished extends HttpServlet {

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
            // 获得调查答案
            List<String> results = new ArrayList<>();
            JsonParser parser = Json.createParser(new StringReader(request.getParameter("result")));
            while (parser.hasNext()) {
                JsonParser.Event e = parser.next();
                if (e == JsonParser.Event.VALUE_STRING) {
                    results.add(parser.getString());
                }
            }
            my.result = results.toArray(new String[]{});
            //
            grp.waitAtStateus(code, Experiment.STAGE.finished);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.getWriter().print(json.build().toString());
    }
}
