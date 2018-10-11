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

import cn.edu.zjut.myong.epic.db.BerkeleyDBSortedMap;
import cn.edu.zjut.myong.epic.db.DB;

/**
 * Created by myong on 2017/8/10.
 */
public class BDBTester {

    public static void main(String[] args) throws Exception {
        DB<MSG> tdb = new BerkeleyDBSortedMap<>(MSG.class);

        tdb.openConnection("./db", "test");
        tdb.save("A", new MSG("1"));
        tdb.save("B", new MSG("2"));
        tdb.save("C", new MSG("3"));
        tdb.closeConnection();

        Thread.sleep(5000);

        tdb.openConnection("./db", "test");
        System.out.println(tdb.get("A").text);
        System.out.println(tdb.get("B").text);
        System.out.println(tdb.get("C").text);
        System.out.println(tdb.get("D").text);
        tdb.closeConnection();
    }
}
