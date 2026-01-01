# radonTransform4J
Java library for calculating the Radon transform of an image. It allows multi-threading for the calculation.


## Requirements

Java 8

The library is self-contained

Note: For being able to execute the example, you will need to execute it with, at least, java 11 jvm (for being able to load the tiff image)

## Background

You can see some details of Radon transform at the Wikipedia

https://en.wikipedia.org/wiki/Radon_transform


## Description of the library

The radon transformation is calculated by integrating every line in the image, and then calculating
their (rho, theta) pair from the image center, and setting them into a bidimensional array.

You can use multithreading by passing a Consumer< Runnable > to the Radon transform calculator.

Once it is computed, you can get the N highest values.

You can avoid duplicates (close (Rho, Theta) pairs, which might refer to the same line) by setting the max delta for considering distinct (rho, theta) pairs to be the same line.

You can also filter by (rho, theta) conditions, as for instance, for filtering a specific angle range via a lambda,
which you can pass to the highest values getter.

## Example

(**RadonTransformCalculatorTest** class)

In the example test, the radon transform is computed over a canny detection of the target image, which is a leaning chess board.

Once it is finished to be computed, we will look for the four highest values.

A max delta for considering the same line is set to 2 for Rho, and 1º for Theta.

(Rho, Theta) pairs are then filtered for being less than 10º from the horizontal or the vertical.

Once we have the highest four (Rho, Theta) pairs, we will calculate the four vertices (those four line intersections).

Then we will put them in the order for building a convex polygon (a square, in this case).

## Making it work

The entry point to the library is the RadonTransformExecutor class.

You have to create an instance of that class.

The constructor takes a parameter, the way to execute the internal tasks.

You can set it so that several threads do the task. (See the example RadonTransformCalculatorTest)

Once you have created the executor, you can invoke the Radon transform calculation.

There are several **calculateRadonTransform** functions, which return a Future<RadonTransformCalculator>

The future is used for dealing with the concurrency, and for not make the caller stop mandatory until the calculation is done.

## Browsing the results

If the calculation has been successfully done, the future will yield a RadonTransformCalculator.

The RadonTransformCalculator, has a function getResult, which returns a **RadonTransformResult**

You can get the Radon transform top values and choose the way those values are obtained (See the example RadonTransformCalculatorTest)

### Results you can browse

With that result (RadonTransformResult), you will be able to browse the values of the Radon transformation.

* result.getRadonTransform(). Radon transform container
* result.getStandardizedRadonTransform(). Radon transform but applying a factor so that the maximum is 1.0d
* result.getNormalizedRadonTransform(). Variant of radon transform, but instead of the accumulated value, it holds the average of the luminance for every pair (Rho, Theta).

They are instances of **My2dContainer**

### Init value

All values of those containers are initialized to -1.0d, in order to identify the values that have not been calculated.

Some values of the containers have never been set, as the nature of (Rho, Theta) related to Cartesian coordinates, makes they not be uniformly sampled during the calculation, and there are some combinations that have never been calculated because they have not any line in the image that coincides with that (Rho, Theta) value.

(I mean every line segment with their limits inside the image)

So if you browse the results and find a -1.0d value, it means that that value has never been calculated.

### How to browse each My2dContainer

Those containers can be browsed via the browseValue function, which takes a visitor as parameter.

That visitor is invoked at each element, with the indices of Rho and Theta in the container, and its value (which is a double)

```
    Interface My2dContainer

    void browseValue(TriConsumer<Integer, Integer, Double> visitor);
```

You can also get particular values (with the get function), which takes a Rho index and a Theta index.

```
    Interface My2dContainer

    Double getValue(int xx, int yy);
```

Those indices can be translated into Rho and Theta real values, by using the function:

```
    Class RadonTransformResult

    public Point2d indexToRhoTheta(int rhoIndex, int thetaIndex)
```

Each such Point2d is a (Rho, Theta) pair

* Rho. (associated with the x coordinate in the library). Is the distance to the center (it is signed) (negative y deltas, result in a negative Rho).
* Theta. (associated with the y coordinate in the library). Is the angle (orthogonal to the line it represents)

(See the background for more details)



In order to build a line from its (Rho, Theta) pair, you will need to know the center from which that (Rho, Theta) pair has been calculated.

That center, is the center of the image, and you can get it this way:

```
    Class RadonTransformCalculator

    protected Point2d getImageCenter()
```

