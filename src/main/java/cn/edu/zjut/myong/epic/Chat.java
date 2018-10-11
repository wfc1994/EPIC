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
import javax.json.JsonArrayBuilder;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A communication within a group.
 */
public class Chat implements Serializable {

    private static final long serialVersionUID = 5087282650602080764L;

    private Lock chatLock = new ReentrantLock();

    private List<ChatMessage> MESSAGES = new LinkedList<>();

    public Chat() {
        // MESSAGES.add(new ChatMessage("administrator", "Welcome to our experiment", new Date()));
    }

    /**
     * sending a new message to the chat
     * @param sender
     * @param msg
     */
    public void addMsg(String sender, String msg, int towho) {
        try {
            chatLock.lock();
            //
            MESSAGES.add(new ChatMessage(sender, msg, new Date(), towho));
        } catch (Exception e) {
           e.printStackTrace();
        } finally {
            chatLock.unlock();
        }
    }

    /**
     * get all messages no later than the deadline.
     * @param deadline
     * @return
     */
    public List<ChatMessage> getMsg(Date deadline) {
        List<ChatMessage> rmsg = new ArrayList<>();
        try {
            chatLock.lock();
            //
            if (deadline == null) {
                rmsg.addAll(MESSAGES);
            } else {
                for (int i = MESSAGES.size() - 1; i >= 0; i--) {
                    if (MESSAGES.get(i).time.before(deadline))
                        break;
                    rmsg.add(MESSAGES.get(i));
                }
                Collections.reverse(rmsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            chatLock.unlock();
        }
        return rmsg;
    }

    /**
     * et all messages no later than the deadline in json format.
     * @param deadline
     * @return json data: [{msg1}, {msg2}, ...], the json format of message can be found in ChatMessage class.
     */
    public JsonArrayBuilder getJsonMsg(Date deadline) {
        List<ChatMessage> msg = getMsg(deadline);
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (int i = 0; i < msg.size(); i++) {
            json.add(msg.get(i).toJson());
        }
        return json;
    }
}
