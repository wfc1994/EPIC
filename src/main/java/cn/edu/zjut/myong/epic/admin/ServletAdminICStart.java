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

import cn.edu.zjut.myong.epic.model.ICConfiguration;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Address: /servlet/admin/signin
 * Parameters:
 * [code] : the administrator's code defined in the configuration file.
 * [password] : the administrator's password defined in the configuration file.
 * Response: ture for successful, false for failure.
 */
public class ServletAdminICStart extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        JsonObjectBuilder json = Json.createObjectBuilder();
        try {
            String code = request.getParameter("code");
            String cjson = request.getParameter("conf");

            ICConfiguration config = new ICConfiguration(cjson);

            boolean s = AdminStation.startExp(code, config);
            json.add("started", s);
            if (s)
                json.add("response", "Experiment is started!");
            else
                json.add("response", "Experiment cannot be started!");
        } catch (Exception e) {
            e.printStackTrace();
            json.add("started", false);
            json.add("response", "Configuration is error!");
        }
        response.getWriter().print(json.build().toString());
    }
}
