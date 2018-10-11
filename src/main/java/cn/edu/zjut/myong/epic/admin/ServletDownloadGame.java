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

import cn.edu.zjut.myong.epic.model.ICGameExporter;

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
public class ServletDownloadGame extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        JsonObjectBuilder json = Json.createObjectBuilder();
        try {
            String db = request.getParameter("db").trim();
            String game = request.getParameter("game").trim();
            int type = Integer.parseInt(request.getParameter("type").trim());

            String fileName = db + "_" + game;
            switch (type) {
                case 0:
                    fileName = fileName + ".xlsx";
                    break;
                case 1:
                    fileName = fileName + ".json";
                    break;
                default:
                    fileName = fileName + ".zip";
                    break;
            }
            String mimeType = getServletContext().getMimeType(fileName);
            response.setContentType(mimeType);
            response.setHeader("content-disposition", "attachment;filename=" + fileName);

            switch (type) {
                case 0:
                    ICGameExporter.export2xlsx(AdminStation.getGame(db, game), response.getOutputStream());
                    break;
                case 1:
                    ICGameExporter.export2json(AdminStation.getGame(db, game), response.getOutputStream());
                    break;
                default:
                    ICGameExporter.export2csv(AdminStation.getGame(db, game), response.getOutputStream());
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
