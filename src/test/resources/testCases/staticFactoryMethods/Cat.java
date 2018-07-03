/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package staticFactoryMethods;

import java.util.ArrayList;

class Cat {
    private final Color color;
    private int weight;

    public Cat(Color color, int weight) {
        this.color = color;
        this.weight = weight;
    }

    public ArrayList<Cat> bearChildren() {
        ArrayList<Cat> children = new ArrayList<>();
        children.add(new Cat(Color.makeFromHex(0), 10));
        children.add(new Cat(Color.makeFromRGB("101010"), 20));
        children.add(new Cat(Color.makeFromPalette(20, 20, 20), 30));
        children.forEach(this::feedChild);
        return children;
    }

    public void feedChild(Cat cat) {
        cat.weight++;
    }
}
