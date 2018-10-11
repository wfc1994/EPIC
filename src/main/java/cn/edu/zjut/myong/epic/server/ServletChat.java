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

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Communicating function in the game within a group.
 * Address: /servlet/chat
 * Parameters:
 * [group] : group id
 * [code] : player code
 * [action] : "GET" for receiving the new messages from given deadline, and "ADD" for sending a new message.
 * [time] : the temporal boundary for the last received message.
 * Response: For "GET", a json array, and for "ADD", no response.
 */
public class ServletChat extends HttpServlet {

    public static final String GET = "GET";
    public static final String ADD = "ADD";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        JsonObjectBuilder json = Json.createObjectBuilder();
        try {
            int grpId = Integer.parseInt(request.getParameter("group").trim());
            String code = request.getParameter("code").trim();
            String cmd = request.getParameter("action").trim();
            Game grp = Experiment.groups.get(grpId);
            GamePlayer my = grp.players.get(code);

            Chat chat = Experiment.groups.get(grpId).chat;

            if (cmd.equals(GET)) {
                // 获得消息
                String dtime = request.getParameter("time");
                if (dtime == null) {
                    json.add("msg", Json.createArrayBuilder());
                }
                Date deadline = new Date();
                deadline.setTime(Long.parseLong(dtime));
                // json.add("msg", chat.getJsonMsg(deadline));
                List<ChatMessage> messages = chat.getMsg(deadline);
                JsonArrayBuilder mson = Json.createArrayBuilder();
                for (int i = 0; i < messages.size(); i++) {
                    ChatMessage message = messages.get(i);
                    if (message.sender.equals(code) ||
                            message.audience == ChatMessage.EVERYONE ||
                            (message.audience == ChatMessage.ALLIANCE && my.role == grp.players.get(message.sender).role) ||
                            (message.audience == ChatMessage.OPPONENT && my.role != grp.players.get(message.sender).role)) {
                        mson.add(message.toJson());
                    }
                }
                json.add("msg", mson);

            } else if (cmd.equals(ADD)) {
                String msg = request.getParameter("msg");
                int towho = Integer.parseInt(request.getParameter("to"));
                chat.addMsg(code, msg, towho);
                // chat.addMsg(code, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.getWriter().print(json.build().toString());
    }
}
