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

package cn.edu.zjut.myong.epic.db;

import com.sleepycat.je.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过database对象直接操作
 */
public class BerkeleyDBGeneral<T> implements DB<T> {

    public Environment env = null;
    public Database database = null;

    public void openEnvironment(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        EnvironmentConfig envConf = new EnvironmentConfig();
        envConf.setAllowCreate(true);
        envConf.setTransactional(true);
        this.env = new Environment(file, envConf);
    }

    public List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();
        try {
            Cursor cursor = database.openCursor(null, null);
            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry data = new DatabaseEntry();
            while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                keys.add(new String(key.getData()));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keys;
    }

    @Override
    public void openConnection(String filePath, String databaseName) {
        if (env == null) {
            File file = new File(filePath);
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            envConfig.setTransactional(true);
            env = new Environment(file, envConfig);
        }

        if (database != null) {
            database.close();
        }

        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setAllowCreate(true);
        databaseConfig.setTransactional(true);
        database = env.openDatabase(null, databaseName, databaseConfig);
    }

    @Override
    public void closeConnection() {
        if (database != null) {
            database.close();
            if (env != null) {
                env.cleanLog();
                env.close();
            }
        }
    }

    @Override
    public void delete(String key) {
        DatabaseEntry keyEntry = new DatabaseEntry();
        keyEntry.setData(key.getBytes());
        database.delete(null, keyEntry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get(String key) {
        T t = null;
        DatabaseEntry keyEntry = new DatabaseEntry();
        DatabaseEntry valueEntry = new DatabaseEntry();
        keyEntry.setData(key.getBytes());
        if (database.get(null, keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            ByteArrayInputStream bais = new ByteArrayInputStream(valueEntry.getData());
            try {
                ObjectInputStream ois = new ObjectInputStream(bais);
                t = (T) ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return t;
    }

    @Override
    public void save(String key, T t) {
        DatabaseEntry keyEntry = new DatabaseEntry();
        DatabaseEntry valueEntry = new DatabaseEntry();
        keyEntry.setData(key.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(t);
        } catch (IOException e) {
            e.printStackTrace();
        }
        valueEntry.setData(baos.toByteArray());
        database.put(null, keyEntry, valueEntry);
    }

    @Override
    public void update(String key, T t) {
        save(key, t);
    }
}
