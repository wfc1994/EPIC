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

import java.io.Serializable;

public class Grid2DIndex implements Serializable {

    private static final long serialVersionUID = 4633417321111532828L;

    public int x;
    public int y;

    public Grid2DIndex(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        Grid2DIndex other = (Grid2DIndex) obj;
        if (x == other.x && y == other.y)
            return true;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return x * 1000 + y;
    }

    @Override
    public String toString() {
        return "(" + x + " " + y + ")";
    }
}
