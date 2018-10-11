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

import javax.json.Json;
import javax.json.stream.JsonParser;
import java.io.StringReader;

/**
 * Created by myong on 2017/7/6.
 */
public class TestJsonParser {

    public static void main(String[] args) throws Exception {
        JsonParser parser = Json.createParser(new StringReader("[\"20\",\"222\"]"));
        while (parser.hasNext()) {
            JsonParser.Event e = parser.next();
            if (e == JsonParser.Event.VALUE_STRING)
                System.out.println(parser.getString());
        }
    }
}
