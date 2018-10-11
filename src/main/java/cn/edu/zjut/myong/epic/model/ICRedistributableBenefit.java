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

/**
 * Created by myong on 2017/8/17.
 */
public class ICRedistributableBenefit implements Serializable {

    private static final long serialVersionUID = 7860968558271788967L;

    public double income = 0;

    public double cost = 0;

    public double redist = 0;
}
