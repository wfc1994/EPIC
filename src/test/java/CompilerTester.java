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

import cn.edu.zjut.myong.epic.model.ICCalculator;

public class CompilerTester {

    public static void main(String[] args) throws Exception {

        ICCalculator.build(
                "RESALLL",
                "public double rValue(int Na, int Nb) { return Na + Nb + 0.1; }",
                "public double dValue(int Na, int Nb) { return Na - Nb - 0.1; }");
    }
}
