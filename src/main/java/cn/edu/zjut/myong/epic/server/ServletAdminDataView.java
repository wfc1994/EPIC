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
import cn.edu.zjut.myong.epic.GameExporter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Address: /servlet/admin/view
 * Parameters:
 * [group] : targeted group id
 * [type] : the format of replyed data: "xlsx" for downloading excel file of all data about a group at the end of experiments, and "json" for dynamical view of data in the course of experiments.
 * [parc] : if the type is "json", the parameter indicates which kinds of data is responsed, including: "patterns", "actions", "revenues", and "costs".
 * Response: for "json", a 2D json array of strings; for "xlsx", a excel file.
 */
public class ServletAdminDataView extends HttpServlet {

    public static final String GET = "GET";
    public static final String ADD = "ADD";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int grpId = Integer.parseInt(request.getParameter("group").trim());
            Game grp = Experiment.groups.get(grpId);
            String type = request.getParameter("type").trim();
            String parc = request.getParameter("parc").trim();

            if (type.equals("xlsx")) {
                String fn = "Group_" + grpId + ".xlsx";
                response.setStatus(HttpServletResponse.SC_OK);
                // 设置下载的类型 告诉浏览器 需要以下载的方式操作
                response.setContentType("application/force=download");
                // 下载头设置
                response.setHeader("content-disposition", "attachment;fileName="
                        + java.net.URLEncoder.encode(fn, "UTF-8"));
                ServletOutputStream servletOutputStream = response.getOutputStream();
                GameExporter.export2xlsx(grp, servletOutputStream);
                servletOutputStream.flush();
                servletOutputStream.close();

            } else if (type.equals("json")) {
                response.setContentType("text/html");
                response.setStatus(HttpServletResponse.SC_OK);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print(GameExporter.export2Json(grp, parc).build().toString());

            } else {
                throw new Exception("Invalid type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
