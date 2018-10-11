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

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.Serializable;
import java.util.Map;

/**
 * The discrete time, round-based social-ecological system model
 */
public interface GameModel extends Serializable {

    /**
     * Setting the parameters (from configuration file) to the model.
     * @param params
     */
    public void setParameters(Map<String, String> params) throws Exception;

    /**
     * Get all parameters in json format
     * @return
     */
    public JsonObjectBuilder getJsonParameters();

    /**
     * Return the initial state of the model.
     * @return
     */
    public GamePattern getInitial();

    /**
     * Return the initial state in json format.
     * @return
     */
    public JsonArrayBuilder getJsonInitial();

    /**
     * parsing the upload action (json) from players' client to the GameAction object.
     * @param json
     * @return
     */
    public GameAction parseAction(String json);

    /**
     * Calculating the change of the system from previous state with given actions of all players to the new state.
     * @param group
     * @param state0
     * @param action
     * @return
     */
    public GamePattern simulate(Game group, GamePattern state0, Map<String, GameAction> action);

    public GamePattern redistribute(Game group, GamePattern state0, Map<String, GameAction> action);
}
