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

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The Berkeley DB accessor using SortedMap implementation.
 */
public class BerkeleyDBSortedMap<T> implements DB<T> {

    protected Environment env = null;
    protected Database db = null;
    protected StoredMap<String, T> storedMap = null;
    protected Class<T> persistentClass = null;

    protected void openEnvironment(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        EnvironmentConfig envConf = new EnvironmentConfig();
        envConf.setAllowCreate(true);
        envConf.setTransactional(true);
        this.env = new Environment(file, envConf);
    }

    protected List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();
        try {
            Cursor cursor = db.openCursor(null, null);
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

    /**
     *
     * @param persistentClass
     */
    public BerkeleyDBSortedMap(Class<T> persistentClass){
        this.persistentClass = persistentClass;
    }

    @Override
    public void openConnection(String filePath, String dbName) {
        try {
            if (env == null) {
                openEnvironment(filePath);
            }

            if (db != null) {
                db.close();
            }

            DatabaseConfig dbConf = new DatabaseConfig();
            dbConf.setAllowCreate(true);
            dbConf.setTransactional(true);
            this.db = this.env.openDatabase(null, dbName, dbConf);

            // data for serialization
            // Database classdb = this.env.openDatabase(null, "java_class_catalog", dbConf);
            StoredClassCatalog catalog = new StoredClassCatalog(db);

            EntryBinding<String> keyBinding = new SerialBinding<String>(catalog, String.class);
            SerialBinding<T> valueBinding = new SerialBinding<T>(catalog, this.persistentClass);
            this.storedMap = new StoredMap<String, T>(db, keyBinding, valueBinding, true);
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public void closeConnection() {
        if (db != null) {
            db.close();
        }
        if (env != null) {
            env.cleanLog(); //在关闭环境前，清理日志，用以释放更多的磁盘空间
            env.close();
        }
    }

    @Override
    public void save(String key, T value) {
        storedMap.put(key, value);
    }

    @Override
    public void delete(String key) {
        storedMap.remove(key);
    }

    @Override
    public void update(String key, T value) {
        save(key, value);
    }

    @Override
    public T get(String key) {
        return storedMap.get(key);
    }
}
