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

import cn.edu.zjut.myong.epic.db.BerkeleyDBGeneral;
import cn.edu.zjut.myong.epic.db.DB;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * The center class of once experiment, which contains all information about a experiment.
 * 一次实验的中心课程，其中包含有关实验的所有信息。
 */
public class Experiment {

    /**
     * All stages
     * 所有阶段
     */
    public enum STAGE {
        signin,
        instruct,
        survey4test,
        communication,
        decision,
        redistribution,
        process,
        next,
        survey4questionnaire,
        finished,
        waiting
    }

    /**
     * Experiment id, unique for each experiment, and also used to define the database to record the experimental data.
     * 实验id，每个实验都是唯一的，也用于定义数据库以记录实验数据。
     */
    public static String eid;

    /**
     * Version for running a experiment.
     * 运行实验的版本。
     */
    public static long version = 0;

    /**
     * Configuration file.
     * 配置文件。
     */
    public static Configuration conf;

    /**
     * Game properties: inherit, round numberm, ...
     * 游戏属性：继承，整数，...
     */
    public static GameProperties gameProperties;

    /**
     * All gaming groups.
     * 所有的游戏组。
     */
    public static Map<Integer, Game> groups;

    public static Map<String, GamePlayer> allPlayers;

    /**
     * Slides or videa for instruction.
     * 幻灯片或视频的指示。
     */
    public static String[] slides;

    /**
     * Quesions for survey4test.
     * 调查测试的问题。
     */
    public static GameSurveyQuestion[] questions;

    /**
     * Total scores of all questions.
     * 所有问题的总分数。
     */
    public static int surveyTotalScores = 0;

    /**
     * Threshold for passing the survey4test, no use now.
     */
    public static int surveyThreshold = 0;

    /**
     * Model for simulating social-ecological systems.
     * 模拟社会生态系统的模型。
     */
    public static GameModel model;

    /**
     * Database for persisting the data.
     */
    public static DB<Game> db = null;

    /**
     * Generating the version of the experiment.
     * 生成实验的版本。
     */
    public static void init() throws Exception {
        Configuration cf = null;
        try {
            cf = new XMLConfiguration(
                    new FileInputStream("./configuration.xml"),
                    ClassLoader.getSystemResourceAsStream("configuration.xsd"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        init(cf);
    }

    public static void init(Configuration cf) throws Exception {
        conf = cf;
        conf.buildConfiguration();

        eid = conf.getExperimentId();

        db = new BerkeleyDBGeneral<>();
        db.openConnection("./db", eid);

        gameProperties = conf.getGameProgperties();

        model = (GameModel) Class.forName(conf.getModelClassName()).newInstance();
        model.setParameters(conf.getModelParameters());

        slides = conf.getSlides();

        questions = conf.getQuestons();
        surveyThreshold = conf.getSurveyThreshold();
        for (GameSurveyQuestion q : questions) {
            surveyTotalScores += q.getScore();
        }

        allPlayers = new HashMap<>();
        groups = conf.getGroups();
        for (Game grp : groups.values()) {
            grp.actions = new Map[gameProperties.nRound];
            for (int i = 0; i < gameProperties.nRound; i++)
                grp.actions[i] = new HashMap<>();
            grp.patterns = new GamePattern[gameProperties.nRound];

            Map<String, GamePlayer> players = conf.getPlayers(grp.id);
            allPlayers.putAll(players);
            grp.players = players;
            List<String> lst = new ArrayList<>();
            lst.addAll(players.keySet());
            Collections.sort(lst);
            grp.playerCodes = lst.toArray(new String[lst.size()]);
            for (GamePlayer player : players.values()) {
                String[] as = new String[questions.length];
                for (int i = 0; i < as.length; i++)
                    as[i] = new String("");
                player.answer = as;
                // player.stage = STAGE.signin;
            }

            grp.modelClass = model.getClass().getName();
            grp.initialPattern = model.getInitial();
            grp.buildPlayerString();
        }

        version = new Random(new Date().getTime()).nextLong();
    }

    /**
     * Translating the stage duration to the ending time in Date format.
     * 以日期格式转换阶段持续时间到结束时间。
     * @param status
     * @return
     */
    public static Date calcDeadline(STAGE status) {
        long duration = conf.getStageDuration(status);
        Date deadline = new Date();
        deadline.setTime(deadline.getTime() + duration);
        return deadline;
    }

    public static long calcDuration(STAGE status) {
        long duration = conf.getStageDuration(status);
        return duration;
    }
}
