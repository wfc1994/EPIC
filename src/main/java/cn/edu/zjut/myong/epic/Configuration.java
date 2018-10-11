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

import java.util.Map;

public interface Configuration {
    /**
     * The id of the experiment, which is used in naming the database of the experiment, and should be unique for each experiment.
     * @return
     */
    public String getExperimentId();

    /**
     * The class of Model is used in the experiment.
     * @return class name.
     */
    public String getModelClassName();

    /**
     * The parameters of the model.
     * @return name and value of all parameters.
     */
    public Map<String, String> getModelParameters();

    /**
     * All players in a group.
     * @param groupId
     * @return code and corresponding player object.
     */
    public Map<String, GamePlayer> getPlayers(int groupId);

    /**
     * All groups in the experiment.
     * @return group id and corresponding group object.
     */
    public Map<Integer, Game> getGroups();

    /**
     * Related addresses of all slides or video for instruction stage.
     * @return array of addresses
     */
    public String[] getSlides();

    /**
     * All questions for survey4test stage.
     * @return array of questions.
     */
    public GameSurveyQuestion[] getQuestons();

    /**
     * Extracting game properties from the configuration file.
     * @return
     */
    public GameProperties getGameProgperties();

    /**
     * The threshold for passing survey4test stage.
     * @return
     */
    public int getSurveyThreshold();

    /**
     * Extracting the duration of each stage in the configuration file.
     * @param status the stage.
     * @return time in long.
     */
    public long getStageDuration(Experiment.STAGE status);

    /**
     * Whether the game does allow communication between players within a group.
     * @return
     */
    public boolean isCommunicable();

    public boolean isRedistributable();

    /**
     * The address of html content for decision stage.
     * @return
     */
    public String getDecisionContent();

    /**
     * The address of html content for redistribution stage.
     * @return
     */
    public String getRedistributionContent();

    /**
     * The address of html content for process stage.
     * @return
     */
    public String getProcessContent();

    /**
     * The address of html content for displaying in the end of games.
     * @return
     */
    public String getFinalContent();

    /**
     * Administrator's code for signin
     * @return
     */
    public String getAdminCode();

    /**
     * Administrator's password for signin
     * @return
     */
    public String getAdminPassword();

    /**
     * Validating and parsing the configuration file.
     * @return successful or failure
     */
    public boolean buildConfiguration();
}
