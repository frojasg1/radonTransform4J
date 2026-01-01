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

package com.frojasg1.image.processing.radon.result;

import com.frojasg1.gen.ComparatorFunctions;
import com.frojasg1.gen.DoubleFunctions;
import com.frojasg1.gen.tuples.Pair;
import com.frojasg1.image.Point2d;
import com.frojasg1.image.normalized.My2dContainer;
import com.frojasg1.image.normalized.My2dContainerAbstract;
import com.frojasg1.image.normalized.impl.My2dContainerAtomicImpl;
import com.frojasg1.image.normalized.impl.MyNormalizedImageImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RadonTransformResult {

    protected double TWO_PI = 2.0d * Math.PI;
    protected double initValue = -1.0d;

    protected int imageWidth;
    protected int imageHeight;

    protected int numElementsForTheta;
    protected double thetaStep;
    protected int numElementsForRho;
    protected int minRho;

    protected boolean finished;


    protected My2dContainer normalizedRadonTransform;
    protected My2dContainer radonTransform;
    protected My2dContainer standardizedRadonTransform;

    protected Pair<Point2d, Double> max;

    public RadonTransformResult(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public RadonTransformResult init() {
        initNumElems();
        initContainers();

        return this;
    }

    protected double getInitValue() {
        return initValue;
    }

    protected void initContainers() {
        normalizedRadonTransform = createContainer();
        radonTransform = createContainer();
    }

    protected My2dContainerAbstract createContainer() {
        return new My2dContainerAtomicImpl(numElementsForRho, numElementsForTheta, getInitValue())
                .init();
    }

    protected void initNumElems() {
        numElementsForTheta = calculateNumElemsForTheta();
        thetaStep = calculateThetaStep(numElementsForTheta);
        numElementsForRho = calculateNumElemsForRho();
        minRho = calculateMinRho();
    }

    protected int calculateNumElemsForTheta() {
        return calculateNumHalfDiagonalSizeElems() * 2 - 1;
    }

    protected int calculateNumElemsForRho() {
        return calculateNumHalfDiagonalSizeElems() * 2 - 1;
    }

    protected int calculateMinRho() {
        return (1 - numElementsForRho) / 2;
    }

    protected int calculateNumHalfDiagonalSizeElems() {
        int result = (int) Math.ceil(calculateDiagonalSize() * 0.5d);
        result += 4;
        if ((result & 0x01) == 0) {
            result++;
        }
        return result;
    }

    protected double calculateDiagonalSize() {
        return Math.sqrt(imageHeight * imageHeight + imageWidth * imageWidth);
    }

    protected double calculateThetaStep(int numElementsForTheta) {
        return Math.PI / (numElementsForTheta - 1);
    }

    public double getTransform(double rho, double theta) {
        return get(radonTransform, rho, theta);
    }

    public double getStandardizedTransform(double rho, double theta) {
        return get(standardizedRadonTransform, rho, theta);
    }

    public double getNormalizedTransform(double rho, double theta) {
        return get(normalizedRadonTransform, rho, theta);
    }

    protected double get(My2dContainer container, double rho, double theta) {
        Integer rhoIndex = calculateRhoIndex(rho);
        int thetaIndex = calculateThetaIndex(theta);

        double result = 0.0d;
        if (rhoIndex != null) {
            result = container.getValue(rhoIndex, thetaIndex);
        }
        return result;
    }

    protected Integer calculateRhoIndex(double rho) {
        Integer result = null;
        double index = rho - getMinRho();
        if (index >= 0 && index < numElementsForRho) {
            result = (int) index;
        }

        return result;
    }

    protected int calculateThetaIndex(double theta) {
        int result = -1;

        double angle = theta;
        if (angle < 0.0d || angle >= TWO_PI) {
            double rounds = angle / TWO_PI;

            // module two pi (first round)
            angle = angle - Math.floor(rounds) * TWO_PI;
        }

        // if sinus is minor than zero ... we take the opposite direction (by symmetry of Radon transformation)
        if (angle >= Math.PI) {
            angle = angle - Math.PI;
        }

        return (int) Math.floor(angle / Math.PI * (numElementsForTheta - 1));
    }

    public void set(double rho, double theta, double accumulation, int count) {
        Integer rhoIndex = calculateRhoIndex(rho);
        int thetaIndex = calculateThetaIndex(theta);

        if (accumulation > 275.0d) {
            int kk = 0;
        }

        if (rhoIndex == 600 && thetaIndex == 360) {
//        if ((rhoIndex > 610 || rho > 246.0d) && Math.abs(theta) < 0.02d) {
            int kk = 0;
        }

        radonTransform.set(rhoIndex, thetaIndex, accumulation);
        if (count > 0) {
            normalizedRadonTransform.set(rhoIndex, thetaIndex, accumulation / count);
        }
    }

    public List<Pair<Point2d, Double>> getTopLines(int size) {
        return getTopLines(size, rhoTheta -> true, createPoint2d(1.0e-4d, 1.0e-4d));
    }

    public List<Pair<Point2d, Double>> getTopLines(int size, Predicate<Point2d> rhoThetaFilter,
                                                   Point2d maxRhoThetaDeltaForSameLine) {
        TopLinesVisitor visitor = new TopLinesVisitor(size, this::indexToRhoTheta, rhoThetaFilter, maxRhoThetaDeltaForSameLine);

        getRadonTransform().browseValue((rhoInd, thetaInd, value) -> visit(visitor, rhoInd, thetaInd, value));

        return visitor.getResult();
    }

    public Point2d indexToRhoTheta(int rhoIndex, int thetaIndex) {
        double rho = calculateRho(rhoIndex);
        double theta = calculateTheta(thetaIndex);

        Point2d rhoTheta = createPoint2d(rho, theta);
        return rhoTheta;
    }

    protected void visit(TopLinesVisitor visitor, int rhoInd, int thetaInd, Double value) {
        visitor.add(rhoInd, thetaInd, value);
    }

    protected double getThetaStep() {
        return thetaStep;
    }

    protected int getMinRho() {
        return minRho;
    }

    protected int calculateRho(int rhoInd) {
        return rhoInd + getMinRho();
    }

    protected double calculateTheta(int thetaInd) {
        return thetaInd * getThetaStep();
    }

    public My2dContainer getNormalizedRadonTransform() {
        return normalizedRadonTransform;
    }

    public My2dContainer getRadonTransform() {
        return radonTransform;
    }

    public My2dContainer getStandardizedRadonTransform() {
        return standardizedRadonTransform;
    }

    protected Point2d createPoint2d(double rho, double theta) {
        return Point2d.create(rho, theta);
    }

    protected <K, V> Pair<K, V> createPair(K key, V value) {
        return new Pair<>(key, value);
    }

    protected DoubleFunctions getDoubleFunctions() {
        return DoubleFunctions.instance();
    }

    protected boolean areClose(double value1, double value2, double tolerance) {
        return getDoubleFunctions().areClose(value1, value2, tolerance);
    }

    public void finished() {
        if (isFinished()) {
            throw new IllegalStateException("Already finished");
        }
        finished = true;

        doFinishedTasks();
    }

    public boolean isFinished() {
        return finished;
    }

    public Pair<Point2d, Double> getRhoThetaMaxValue() {
        return max;
    }

    protected void doFinishedTasks() {
        max = calculateMax();

        standardizedRadonTransform = calculateStandardizedRadonTransform(max);
    }

    protected My2dContainer calculateStandardizedRadonTransform(Pair<Point2d, Double> maxValue) {
        double factor = 1.0d;
        if (maxValue != null && maxValue.getValue() > 0.0d) {
            factor = 1.0d / maxValue.getValue();
        }

        double finalFactor = factor;
        My2dContainer result = new MyNormalizedImageImpl(radonTransform.getWidth(), radonTransform.getHeight())
                .init();
        radonTransform.browseValue((rhoInd, thetaInd, value) -> setWithFactor(result, rhoInd, thetaInd, value, finalFactor));

        return result;
    }

    protected void setWithFactor(My2dContainer container, int rhoInd, int thetaInd, Double value, double factor) {
        if (value != null) {
            double newValue = value;
            if (value >= 0.0d) {
                newValue = value * factor;
            }
            container.set(rhoInd, thetaInd, newValue);
        }
    }

    protected Pair<Point2d, Double> calculateMax() {
        List<Pair<Point2d, Double>> list = getTopLines(1);

        Pair<Point2d, Double> result = null;
        if (!list.isEmpty()) {
            result = list.get(0);
        }

        return result;
    }

    public Pair<Point2d, Double> getMax() {
        return max;
    }

    protected class TopLinesVisitor {
        protected List<Pair<Point2d, Double>> result = new ArrayList<>();

        protected int size;

        protected double minVal = 0.0d;

        protected BiFunction<Integer, Integer, Point2d> indexToRhoTheta;
        protected Predicate<Point2d> rhoThetaFilter;
        protected Point2d maxRhoThetaDeltaForSameLine;

        public TopLinesVisitor(int size, BiFunction<Integer, Integer, Point2d> indexToRhoTheta,
                               Predicate<Point2d> rhoThetaFilter,
                               Point2d maxRhoThetaDeltaForSameLine) {
            this.size = size;
            this.rhoThetaFilter = rhoThetaFilter;
            this.indexToRhoTheta = indexToRhoTheta;
            this.maxRhoThetaDeltaForSameLine = maxRhoThetaDeltaForSameLine;
        }

        public void add(int rhoIndex, int thetaIndex, double value) {
            if (hasToAdd(value)) {
                Point2d rhoTheta = indexToRhoTheta(rhoIndex, thetaIndex);
                if (passesFilter(rhoTheta)) {
                    addInternal(rhoTheta, value);
                }
            }
        }

        protected Point2d indexToRhoTheta(int rhoIndex, int thetaIndex) {
            return indexToRhoTheta.apply(rhoIndex, thetaIndex);
        }

        protected boolean hasToAdd(double value) {
            return value >= minVal;
        }

        protected boolean passesFilter(Point2d rhoTheta) {
            return rhoThetaFilter.test(rhoTheta);
        }

        protected List<Pair<Point2d, Double>> getCloseElements(Point2d rhoTheta) {
            return result.stream()
                    .filter(rhoThetaPair1 -> areClose(rhoThetaPair1.getKey(), rhoTheta))
                    .collect(Collectors.toList());
        }

        protected boolean areClose(Point2d rhoTheta1, Point2d rhoTheta2) {
            return RadonTransformResult.this.areClose(rhoTheta1.x, rhoTheta2.x, maxRhoThetaDeltaForSameLine.x) &&
                    RadonTransformResult.this.areClose(rhoTheta1.y, rhoTheta2.y, maxRhoThetaDeltaForSameLine.y);
        }

        protected double getMax(List<Pair<Point2d, Double>> elems) {
            return elems.stream()
                    .map(Pair::getValue)
                    .reduce(-1.0d, Math::max);
        }

        protected void addInternal(Point2d rhoTheta, double value) {
            List<Pair<Point2d, Double>> closeElems = getCloseElements(rhoTheta);
            if (!closeElems.isEmpty()) {
                double closeElemsMax = getMax(closeElems);
                if (value <= closeElemsMax) {
                    rhoTheta = null;
                } else {
                    result.removeAll(closeElems);
                }
            }

            if (rhoTheta != null) {
                result.add(createElem(rhoTheta, value));

                purge();
            }
        }

        protected Pair<Point2d, Double> createElem(Point2d rhoTheta, double value) {
            return new Pair<>(rhoTheta, value);
        }

        protected void purge() {
            result.sort(this::compare);

            if (result.size() >= size) {
                while (result.size() > size) {
                    result.remove(result.size() - 1);
                }

                minVal = calculateMinval();
            }
        }

        protected double calculateMinval() {
            return result.stream()
                    .map(Pair::getValue)
                    .reduce(java.lang.Double.MAX_VALUE, Math::min);
        }

        protected ComparatorFunctions getComparatorFunctions() {
            return ComparatorFunctions.instance();
        }

        protected Double getReverseValue(Pair<Point2d, Double> elem) {
            return -elem.getValue();
        }

        protected int getAbsRho(Pair<Point2d, Double> elem) {
            int rho = elem.getKey().getIntX();
            return Math.abs(rho);
        }

        protected int getTheta(Pair<Point2d, Double> elem) {
            int theta = elem.getKey().getIntY();
            return theta;
        }

        protected int compare(Pair<Point2d, Double> elem1, Pair<Point2d, Double> elem2) {
            return getComparatorFunctions()
                    .compareAttribs(elem1, elem2, true,
                            this::getReverseValue, this::getAbsRho, this::getTheta);
        }

        public List<Pair<Point2d, Double>> getResult() {
            return result;
        }
    }
}
