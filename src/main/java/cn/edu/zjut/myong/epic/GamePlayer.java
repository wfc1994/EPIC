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
import javax.json.JsonObjectBuilder;
import java.io.Serializable;

/**
 * A game player.
 */
public class GamePlayer implements Serializable {

    private static final long serialVersionUID = -791269826516971547L;

    //TODO 为不同的游戏设定更多的ROLE选项
    public enum ROLE {
        PLAYER, PREY, PREDATOR, COMPETITOR, SUPCOMPETITOR, SUBCOMPETITOR, A, B
    }

    /**
     * player code (i.e. id).
     */
    public String code;

    /**
     * player password.
     */
    public String password;

    /**
     * waiting for future development.
     */
    public boolean AI;

    /**
     * the role of the player in the game.
     */
    public ROLE role;

    /**
     * current stage of the player.
     */
    // public Experiment.STAGE stage;

    /**
     * the answers for all questions in survey4test stage.
     */
    public String[] answer;
    public String[] result;

    public JsonObjectBuilder toJon() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("code", code);
        json.add("role", role.name());
        JsonArrayBuilder array1 = Json.createArrayBuilder();
        for (int i = 0; i < answer.length; i++) {
            array1.add(answer[i]);
        }
        json.add("answer", array1);
        JsonArrayBuilder array2 = Json.createArrayBuilder();
        for (int i = 0; i < result.length; i++) {
            array2.add(result[i]);
        }
        json.add("result", array2);
        return json;
    }
}
