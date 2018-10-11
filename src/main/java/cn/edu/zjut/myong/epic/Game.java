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
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The main class to encapsulating the game of a group of players. In order to keep the synchronization of all players, major parameters of the game is stored in the class.
 * 封装一组玩家的游戏的主要类。 为了保持所有玩家的同步，游戏的主要参数存储在类中。
 */
public class Game implements Serializable {

    private static final long serialVersionUID = 6637633930493640965L;

    private Lock clockLock = new ReentrantLock();
    private Lock calcLock = new ReentrantLock();
    private Lock roundLock = new ReentrantLock();
    private Lock waitLock = new ReentrantLock();

    public String playerString = "";

    /**
     * The id for the group (i.e. a game)
     * 某组的id
     */
    public int id = -1;

    /**
     * The model class name, replicated in Experiment class.
     * 模型类名称，在Experiment类中复制。
     */
    public String modelClass = "";

    /**
     * The model parameters, replicated in Experiment class.
     * 模型的参数，在Experiment中复制
     */
    public Map<String, String> modelParameters;

    /**
     * The model initial state.
     * 模型的初始状态。
     */
    public GamePattern initialPattern;

    /**
     * All players in the group.
     * 某组中的所有玩家。
     */
    public Map<String, GamePlayer> players = new HashMap<>();

    /**
     * All players' code list.
     * 所有玩家的代码列表。
     */
    public String[] playerCodes;

    public List<String> getRoleCodes(GamePlayer.ROLE role) {
        List<String> codes = new ArrayList<>();
        for (String code : playerCodes) {
            if (players.get(code).role == role)
                codes.add(code);
        }
        return codes;
    }

    public void buildPlayerString() {
        List<String> collA = getRoleCodes(GamePlayer.ROLE.A);
        List<String> collB = getRoleCodes(GamePlayer.ROLE.B);
        Collections.sort(collA);
        Collections.sort(collB);
        StringBuilder builder = new StringBuilder();
        builder.append("");
        for (int i = 0; i < collA.size(); i++) {
            builder.append(collA.get(i));
            if (i < collA.size() - 1)
                builder.append("\n");
        }
        builder.append("&");
        for (int i = 0; i < collB.size(); i++) {
            builder.append(collB.get(i));
            if (i < collB.size() - 1)
                builder.append("\n");
        }
        this.playerString = builder.toString();
    }

    /**
     * curent round
     * 本轮
     */
    private int current = 0;

    /**
     * System dynamics
     * 系统动态
     */
    public GamePattern[] patterns;

    /**
     * Actions of all round and all players.
     * 所有round和所有player的行动。
     */
    public Map<String, GameAction>[] actions;

    /**
     * Chat within the group.
     * 在组内聊天。
     */
    public Chat chat = new Chat();

    private Map<Experiment.STAGE, Date> deadline = new HashMap<>(4);

    // private boolean goneNext = false;

    private boolean simulated = false;

    private boolean redistributed = false;

    private Experiment.STAGE stage = Experiment.STAGE.signin;
    // private Set<String>[] waitCount = new Set[Experiment.STAGE.values().length];
    private Map<Experiment.STAGE, Set<String>> waitCount= new HashMap<>();

    public Game() {
        deadline.put(Experiment.STAGE.instruct, null);
        deadline.put(Experiment.STAGE.survey4test, null);
        deadline.put(Experiment.STAGE.decision, null);
        deadline.put(Experiment.STAGE.redistribution, null);
        deadline.put(Experiment.STAGE.process, null);
        deadline.put(Experiment.STAGE.survey4questionnaire, null);

        for (int i = 0; i < Experiment.STAGE.values().length; i++) {
            // waitCount[i] = new HashSet<>(players.size());
            waitCount.put(Experiment.STAGE.values()[i], new HashSet<String>(players.size()));
        }
    }

    /**
     * All players' actions in a round.
     * @param round
     * @return player code and its action in json.
     * 在一轮中所有玩家的行动
     * 返回json--------------------------------------------------
     */
    public JsonObjectBuilder getAllActions(int round) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        for (String code : actions[round].keySet()) {
            json.add(code, actions[round].get(code).toJson());
        }
        return json;
    }

    /**
     * Current round.
     * @return
     */
    public int getCurrent() {
        return current;
    }

    /**
     * Checking whether all players are located in the targeted stage. The method is used to keep synchronization of all players. All players should enter a stage concurrently.
     * @param status
     * @return
     * 检查所有玩家是否位于目标阶段。 该方法用于保持所有玩家的同步。 所有球员应该同时进入一个阶段。
     */
    /*
    public boolean allAtStatus(Experiment.STAGE status) {
        for (GamePlayer player : players.values()) {
            if (player.stage != status)
                return false;
        }
        // 当所有人都进入decision过程的时候代表大家都在已经进入新的一轮，且都没有进入下一轮
        if (status == Experiment.STAGE.decision) {
            simulated = false;
            redistributed = false;
            goneNext = false;
        }
        return true;
    }
    */

    public void waitAtStateus(String code, Experiment.STAGE status) {
        try {
            waitLock.lock();
            // System.out.println(code + "TO" + status.name());
            if (!waitCount.get(status).contains(code))
                waitCount.get(status).add(code);
            if (waitCount.get(status).size() == players.size()) {
                this.stage = status;
                waitCount.get(status).clear();

                // 当所有人都进入decision过程的时候代表大家都在已经进入新的一轮，且都没有进入下一轮
                if (this.stage == Experiment.STAGE.decision) {
                    simulated = false;
                    redistributed = false;
                    // goneNext = false;
                }

                if (this.stage == Experiment.STAGE.next) {
                    deadline.put(Experiment.STAGE.communication, null);
                    deadline.put(Experiment.STAGE.decision, null);
                    deadline.put(Experiment.STAGE.redistribution, null);
                    deadline.put(Experiment.STAGE.process, null);
                    // chat.addMsg("administrator", "Welcome to the round" + (current + 1), ChatMessage.EVERYONE);
                    // 记录数据
                    Experiment.db.save("Group" + id, this);
                    current++;
                    /*
                    if (!goneNext) {
                        // goneNext = true;
                        current++;
                        deadline.put(Experiment.STAGE.communication, null);
                        deadline.put(Experiment.STAGE.decision, null);
                        deadline.put(Experiment.STAGE.redistribution, null);
                        deadline.put(Experiment.STAGE.process, null);
                        // chat.addMsg("administrator", "Welcome to the round" + (current + 1), ChatMessage.EVERYONE);
                        // 记录数据
                        Experiment.db.save("Group" + id, this);
                        // 结束
                        if (current >= Experiment.gameProperties.nRound) {
                            for (GamePlayer player : players.values()) {
                                player.stage = Experiment.STAGE.finish;
                            }
                        }
                    }
                    */
                }

                if (this.stage == Experiment.STAGE.finished) {
                    current = Experiment.gameProperties.nRound - 1;
                    Experiment.db.save("Group" + id, this);
                }
            }
        } finally {
            waitLock.unlock();
        }
    }

    public Experiment.STAGE getStage() {
        return stage;
    }


    /**
     * Synchronized method, all player call this method for enter the next round. The system state is updated when the first calling the method.
     */
    /*
    public void go2NextRound() {
        try {
            roundLock.lock();
            // 当大家都还没有进入下一轮，可以正式开始下一轮数据的更新，只要有一个player已经进入下一轮，那么数据已经更新，不需要重复的更新。
            if (!goneNext) {
                goneNext = true;
                current++;
                deadline.put(Experiment.STAGE.communication, null);
                deadline.put(Experiment.STAGE.decision, null);
                deadline.put(Experiment.STAGE.redistribution, null);
                deadline.put(Experiment.STAGE.process, null);
                // chat.addMsg("administrator", "Welcome to the round" + (current + 1), ChatMessage.EVERYONE);
                // 记录数据
                Experiment.db.save("Group" + id, this);
                // 结束
                if (current >= Experiment.gameProperties.nRound) {
                    for (GamePlayer player : players.values()) {
                        player.stage = Experiment.STAGE.finish;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            roundLock.unlock();
        }
    }
    */

    /**
     * Synchronized method, all player call this method for get deadline of current stage. The deadline is updated when the first calling the method.
     * @param status
     * @return Date in long.
     * 同步方法，所有玩家调用此方法获取当前阶段的截止时间。 第一次调用方法时更新截止日期。
     */
    public long getDeadline(Experiment.STAGE status) {
        try {
            clockLock.lock();//线程锁操作
            if (deadline.get(status) == null) {
                deadline.put(status, Experiment.calcDeadline(status));
            }
            return deadline.get(status).getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            clockLock.unlock();
        }
    }

    public long getDuration(Experiment.STAGE status) {
        try {
            clockLock.lock();//线程锁操作
            long duration = Experiment.calcDuration(status);
            return duration;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            clockLock.unlock();
        }
    }


    /**
     * Running the model and get results. The model only run when first calling the method.
     */
    public void simulate() {
        try {
            calcLock.lock();
            if (patterns[current] == null || !simulated) {
                List<GameAction> acts = new ArrayList<>();
                GamePattern preState = current == 0 ? initialPattern : patterns[current - 1];
                GamePattern nxt = Experiment.model.simulate(this, preState, actions[current]);
                patterns[current] = nxt;
                simulated = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            calcLock.unlock();
        }
    }

    public void redistribute() {
        try {
            calcLock.lock();
            if (patterns[current] == null || !redistributed) {
                List<GameAction> acts = new ArrayList<>();
                GamePattern nxt = Experiment.model.redistribute(this, patterns[current], actions[current]);
                patterns[current] = nxt;
                redistributed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            calcLock.unlock();
        }
    }
}
