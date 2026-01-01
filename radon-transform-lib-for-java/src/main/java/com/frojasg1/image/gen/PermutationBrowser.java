/*
 *
 * MIT License
 *
 * Copyright (c) 2026. Francisco Javier Rojas Garrido <frojasg1@hotmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

package com.frojasg1.image.gen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class PermutationBrowser<EE> {

    protected List<EE> elementsToPermute;

    public PermutationBrowser(List<EE> elementsToPermute) {
        this.elementsToPermute = elementsToPermute;
    }

    public void browsePermutations(Predicate<List<EE>> visitor) {
        Set<EE> presentElems = new HashSet<>();
        LinkedList<EE> currentIteration = new LinkedList<>();

        nextLevel(currentIteration, presentElems, visitor);
    }

    protected boolean nextLevel(LinkedList<EE> currentIteration, Set<EE> presentElems, Predicate<List<EE>> visitor) {
        Iterator<EE> it = elemIterator();

        EE current = null;
        boolean result = false;
        while (!result && it.hasNext()) {
            current = it.next();

            if (!presentElems.contains(current)) {
                currentIteration.addLast(current);
                presentElems.add(current);

                if (isComplete(currentIteration)) {
                    result = visit(visitor, currentIteration);
                } else {
                    result = nextLevel(currentIteration, presentElems, visitor);
                }

                currentIteration.removeLast();
                presentElems.remove(current);
            }
        }

        return result;
    }

    protected boolean isComplete(List<EE> list) {
        return list.size() == elementsToPermute.size();
    }

    protected Iterator<EE> elemIterator() {
        return elementsToPermute.iterator();
    }


    protected boolean visit(Predicate<List<EE>> visitor, List<EE> currentIteration) {
        List<EE> permutation = new ArrayList<>(currentIteration);

        return visitor.test(permutation);
    }
}
