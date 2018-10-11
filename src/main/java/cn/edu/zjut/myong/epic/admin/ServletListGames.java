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
package cn.edu.zjut.myong.epic.admin;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
public class ServletListGames extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/x-json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        JsonObjectBuilder json = Json.createObjectBuilder();
        try {
            String db = request.getParameter("db").trim();
            List<String> games = AdminStation.listGames(db);
            Collections.sort(games);
            JsonArrayBuilder array = Json.createArrayBuilder();
            for (int i = 0; i < games.size(); i++) {
                array.add(games.get(i));
            }
            json.add("games", array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.getWriter().print(json.build().toString());
    }
}
