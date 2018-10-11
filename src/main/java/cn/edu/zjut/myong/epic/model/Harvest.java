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

package cn.edu.zjut.myong.epic.model;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Harvest {

    public static HashMap<Integer, Harvest> dViewers = new HashMap<>();

    public static void init(Set<Integer> games) {
        for (int gid : games) {
            dViewers.put(gid, new Harvest(gid));
        }
    }

    private Lock viewLockA = new ReentrantLock();
    private Lock viewLockB = new ReentrantLock();

    private HashMap<Grid2DIndex, String> harvestedA = new HashMap<>();
    private HashMap<Grid2DIndex, String> harvestedB = new HashMap<>();

    private int gameId;

    private Harvest(int gameId) {
        this.gameId = gameId;
    }

    public boolean harvestA(String code, int x, int y) {
        try {
            viewLockA.lock();
            Grid2DIndex pos = new Grid2DIndex(x, y);
            if (harvestedA.containsKey(pos)) {
                return false;
            } else {
                harvestedA.put(pos, code);
                return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            viewLockA.unlock();
        }
    }

    public JsonArrayBuilder getHarvestedA() {
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Grid2DIndex pos : harvestedA.keySet()) {
            JsonArrayBuilder jpos = Json.createArrayBuilder();
            jpos.add(pos.x);
            jpos.add(pos.y);
            jpos.add(harvestedA.get(pos));
            json.add(jpos);
        }
        return json;
    }

    public boolean harvestB(String code, int x, int y) {
        try {
            viewLockB.lock();
            Grid2DIndex pos = new Grid2DIndex(x, y);
            if (harvestedB.containsKey(pos)) {
                return false;
            } else {
                harvestedB.put(pos, code);
                return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            viewLockB.unlock();
        }
    }

    public JsonArrayBuilder getHarvestedB() {
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Grid2DIndex pos : harvestedB.keySet()) {
            JsonArrayBuilder jpos = Json.createArrayBuilder();
            jpos.add(pos.x);
            jpos.add(pos.y);
            jpos.add(harvestedB.get(pos));
            json.add(jpos);
        }
        return json;
    }

    public void renew() {
        harvestedA = new HashMap<>();
        harvestedB = new HashMap<>();
    }
}
