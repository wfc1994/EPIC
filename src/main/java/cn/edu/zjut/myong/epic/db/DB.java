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

/**
 * The interface to access the database.
 */
public interface DB<T> {
    /**
     * open database
     * @param filePath store path
     * @param dbName database name
     * */
    public void openConnection(String filePath, String dbName);

    /**
     * close database
     * */
    public void closeConnection();

    /**
     * insert
     * */
    public void save(String key, T value);

    /**
     * delete
     * */
    public void delete(String key);

    /**
     * update
     * */
    public void update(String key, T value);

    /**
     * select one
     * */
    public T get(String key);
}
