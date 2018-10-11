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

import cn.edu.zjut.myong.epic.Experiment;
import cn.edu.zjut.myong.epic.Game;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Address: /servlet/admin/signin
 * Parameters:
 * [code] : the administrator's code defined in the configuration file.
 * [password] : the administrator's password defined in the configuration file.
 * Response: ture for successful, false for failure.
 */
public class ServletAdminICMonitor extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        JsonArrayBuilder json = Json.createArrayBuilder();
        try {
            if (Experiment.groups != null) {
                for (int gid : Experiment.groups.keySet()) {
                    JsonArrayBuilder gson = Json.createArrayBuilder();
                    Game grp = Experiment.groups.get(gid);
                    gson.add(gid);//获取Groupid分类;删了之后[["player1","",1,"signin"],["player4","",1,"signin"],["player3","",1,"signin"],["player2","",1,"signin"]]

                    //用户信息
                    String s = grp.playerString.replace("\n", "<br>");
                    String[] ss = s.split("&");
                    if (ss.length >= 1)
                        gson.add(ss[0]);//获取sectorA;删了之后[[1,"",1,"signin"],[2,"",1,"signin"],[3,"",1,"signin"],[4,"",1,"signin"]]
                    else
                        gson.add("");
                    if (ss.length >= 2)
                        gson.add(ss[1]);//获取sectorB;删了之后[[1,"player1","",1,"signin"],[2,"player2","",1,"signin"],[3,"player3","",1,"signin"],[4,"player4","",1,"signin"]]
                    else
                        gson.add("");

                    gson.add(grp.getCurrent() + 1);
                    /*
                    int ord = Experiment.STAGE.values().length;
                    for (GamePlayer player : grp.players.values()) {
                        if (player.stage.ordinal() < ord)
                            ord = player.stage.ordinal();
                    }
                    gson.add(Experiment.STAGE.values()[ord].name());
                    */
                    gson.add(grp.getStage().name());//获取登录状态;删了之后[[1,"player1","",1],[2,"player2","",1],[3,"player3","",1],[4,"player4","",1]]
                    //gson.add(grp.getDeadline(Experiment.STAGE.signin));//1518322656912
                    //gson.add(grp.getDeadline(Experiment.STAGE.waiting));//1518321630927
                    //gson.add(grp.getDeadline(Experiment.STAGE.finished));
                    //gson.add(grp.getDuration(Experiment.STAGE));
                    json.add(gson);//初始显示[[1,"player1","",1,"signin"],[2,"player4","",1,"signin"],[3,"player2","",1,"signin"],[4,"player3","",1,"signin"]]
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.getWriter().print(json.build().toString());
        }
    }
}
