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

package cn.edu.zjut.myong.epic;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.Serializable;
import java.util.Date;

/**
 * A message within the chat of a group.
 */
public class ChatMessage implements Serializable {

    public static final int EVERYONE = 0;
    public static final int ALLIANCE = 1;
    public static final int OPPONENT = 2;

    private static final long serialVersionUID = -508406171306501059L;

    /**
     * the time for receiving the message.
     */
    public Date time;

    /**
     * the text content.
     */
    public String content;

    /**
     * the code of the player sending the message, or "administrator" for system message.
     */
    public String sender;

    public int audience;

    public ChatMessage() {};

    /**
     * create a new message.
     * @param sender code of player
     * @param content
     * @param time
     */
    public ChatMessage(String sender, String content, Date time) {
        this(sender, content, time, EVERYONE);
    }

    public ChatMessage(String sender, String content, Date time, int audience) {
        this.sender = sender;
        this.content = content;
        this.time = time;
        this.audience = audience;
    }

    /**
     * converting the message into json format.
     * @return {sender : player code, content : message text, time : date in long}
     */
    public JsonObjectBuilder toJson() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("sender", sender);
        json.add("role", Experiment.allPlayers.get(sender).role.name());
        json.add("content", content);
        json.add("time", time.getTime());
        json.add("audience", audience);
        return json;
    }
}
