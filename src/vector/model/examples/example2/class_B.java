/*
 * Copyright 2005-2017 Sixth and Red River Software, Bas Leijdekkers
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package vector.model.examples.example2;

/**
 * Created by Kivi on 05.05.2017.
 */
class class_B
{
    static void methodB1()
    {
        class_A.attributeA1=0;
        class_A.attributeA2=0;
        class_A.methodA1();
    }

    static void methodB2()
    {
        attributeB1=0;
        attributeB2=0;
    }

    static void methodB3()
    {
        attributeB1=0;
        methodB1();
        methodB2();
    }

    static int attributeB1;
    static int attributeB2;
}
