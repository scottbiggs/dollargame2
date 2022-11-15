package com.sleepfuriously.dollargame2.model

import android.util.Log
import kotlin.random.Random


class SetsOfIntsUtil {

    //----------------------------------
    //  data
    //----------------------------------

    /** if false, blocks debug prints to Log.d */
    private var debug = false

    /** used for the Gaussian random function */
    private var nextNextGaussian: Float = 0f

    /** used for the Gaussian random function */
    private var haveNextNextGaussian: Boolean = false

    //----------------------------------
    // functions
    //----------------------------------


    /**
     * Finds a single random set of numInts integers which sum to the specified sum.
     * Integers are chosen from the range floor..ceiling, inclusive. Duplicates are allowed.
     * Returns a nullable array of those ints -- which will be null iff it's impossible to
     * find a set from the given pool.
     *
     * @param sum      The number that the items in the list will add up to.
     * @param numInts  The number of ints in the return list.
     * @param floor    The lowest possible value of any number in the list.
     * @param ceiling  The highest possible value. Must be >= floor.
     *
     * @return  An array of ints that add up to the given sum. If no possible
     *          list exists, then null will be returned.
     */
    fun findRandomSetOfIntsWithGivenSum(
        sum : Int,
        numInts : Int,
        floor : Int,
        ceiling : Int) : Array<Int>? {

        val resultArray = findRandomSetOfIntsWithGivenSumRecurse(sum, numInts, floor, ceiling, "")

//        debugOn()

        debugPrint("### RESULT: ### array size = " + resultArray.size.toString())
        for (i in resultArray.indices) {
            debugPrint( i.toString() + ": " + resultArray.get(i).toString() )
        }
        debugPrint("RESULT sum = " + resultArray.sum())

        // Return a nullable array -- either the array returned from the recursive routing,
        // or null if that routine returns an empty list, meaning it was given an impossible task.
        var resultArrayNullable : Array<Int>? = null
        if(resultArray.isNotEmpty()) {
            resultArrayNullable = resultArray
        }
        return resultArrayNullable
    }



    /**
     * Same as the public non-recursive non-recursive function -- except that this
     * returns an empty list rather than null if no set of ints can satisfy the conditions.
     */
    private fun findRandomSetOfIntsWithGivenSumRecurse(
        sum : Int, numInts: Int, floor: Int, ceiling: Int, indent : String): Array<Int> {

        debugPrint("M=" + sum.toString() + " N=" + numInts.toString() +
                " R=" + floor.toString() + " S=" + ceiling.toString(), indent )
        lateinit var resultArray : Array<Int>

        if (numInts < 1) {
            debugPrint("ERROR numInts < 1", indent)
            resultArray = arrayOf()   // we'll return empty array
        } else if ( floor > ceiling ) {
            debugPrint("floor > ceiling", indent)
            resultArray = arrayOf()   // we'll return empty array
        } else if (sum < numInts * floor || sum > numInts * ceiling) {
            // If we're here, there's no possible set of of numInts ints in floor..ceiling that add up to sum.
            debugPrint("No possible set of ints satisfy the conditions.", indent)
            resultArray = arrayOf()   // we'll return empty array
        } else if (numInts == 1) {
            debugPrint("base case -- sum = $sum", indent)
            resultArray = Array(1) { i -> sum }   // we'll return empty array
        } else {
            // Split numInts into two N's .. the first being 1 higher iff numInts is odd
            val secondNumInts = numInts / 2
            val firstNumInts = numInts - secondNumInts  // always >= secondNumInts

            debugPrint("N1=$firstNumInts N2=$secondNumInts", indent )
            val secondFloor = floor * secondNumInts
            val secondCeiling = ceiling * secondNumInts
            val firstFloor = maxOf(floor * firstNumInts, sum - secondCeiling)
            val firstCeiling = minOf(ceiling * firstNumInts, sum - secondFloor)
            val firstRange = firstFloor..firstCeiling
            val secondRange = secondFloor..secondCeiling
            debugPrint("firstRange $firstRange", indent)
            debugPrint("secondRange $secondRange", indent)

            val firstSum = weightedRandom(firstRange)
            val secondSum = sum - firstSum
            debugPrint("Loop iter M1=$firstSum M2=$secondSum", indent)

            val firstArray = findRandomSetOfIntsWithGivenSumRecurse(
                firstSum, firstNumInts, floor, ceiling, "$indent  ")

            val secondArray = findRandomSetOfIntsWithGivenSumRecurse(
                secondSum, secondNumInts, floor, ceiling, "$indent  ")

            resultArray = firstArray + secondArray
        }
        return resultArray
    }  // end recursive function


    /**
     * Returns a specially weighted number within the given range.
     * This number is weighted towards the center based on a normal
     * curve (it'll be pretty steep).
     *
     * @param range     The possible numbers that could be summed to.
     */
    private fun weightedRandom(range: IntRange): Int {

        // The function on n will be a steep normal curve

        // Returns a random float between -5 and 5 (normal curve).
        // So we'll have to convert it to the range that we want.
        val gaussianRandom = nextGaussianRandom()

        var r = gaussianRandom / (RANDOM_BASELINE * 2f)     // r is now in range [-0.5 .. 0.5]
        r += 0.5f       // now r should be [0 .. 1]
        if (r == 1f) {
            r -= Float.MIN_VALUE  // r [0 .. 1)
        }

        // convert the random to an integer within the given range.
        val intRandNum = (r * (range.count()).toFloat()).toInt() + range.first
        debugPrint("weightedRandom( $range ) ==> $intRandNum")
        return intRandNum
    }


    /**
     * Returns a random number with a Gaussian (normal) curve centered at 0 with standard
     * deviation of 1.
     *
     * The min value will be -5 and the max will be 5.  This routine makes sure of that, although
     * a much better approximation will be -4.6 and 4.6.
     *
     * Taken from:  https://docs.oracle.com/javase/8/docs/api/java/util/Random.html#nextGaussian--
     */
    private fun nextGaussianRandom(): Float {

        if (haveNextNextGaussian) {
            haveNextNextGaussian = false
            return nextNextGaussian
        }

        else {
            var v1: Float
            var v2: Float
            var s: Float

            do {
                v1 = 2f * Random.nextFloat() - 1f   // between -1.0 and 1.0
                v2 = 2f * Random.nextFloat() - 1f   // between -1.0 and 1.0
                s = v1 * v1 + v2 * v2
            } while (s >= 1f || s == 0f)

            val multiplier = StrictMath.sqrt(-2 * StrictMath.log(s.toDouble())/s)

            nextNextGaussian = v2 * multiplier.toFloat()
            haveNextNextGaussian = true

            var final: Float = v1 * multiplier.toFloat()

            // keep it within the promised bounds
            if (final < RANDOM_BASELINE * -1f) {
                final = RANDOM_BASELINE * -1f
            }
            else if (final > RANDOM_BASELINE) {
                final = RANDOM_BASELINE
            }
            return final
        }

    }


    /**
     * Quick test of the above function.
     */
    fun testNextGausianRandom() {

        var biggest = 0f
        var smallest = 0f

        var randNum: Float
        for (i in 0..9999999) {
            randNum = nextGaussianRandom()
            debugPrint("nextGaussian: $randNum")

            if (randNum > biggest) {
                biggest = randNum
            }
            if (randNum < smallest) {
                smallest = randNum
            }
        }
        debugPrint("smallest = $smallest, biggest = $biggest")
    }

    fun debugOn() {
        debug = true
    }

    fun debugOff() {
        debug = false
    }

    /**
     * If the class member var "debug" is true, prints text to Log.d, preceded by the class
     * member constant TAG and a space char. If debug is false, this does nothing.
     * @param text  The string to be printed.
     */
    private fun debugPrint (text : String) {
        if (debug) {
            Log.d(TAG, text)
        }
    }

    /**
     * If the class member var "debug" is true, prints text to Log.d, preceded by the class
     * member constant TAG, a space char, and indentChars. If debug is false, this fun does nothing.
     * @param text         The string to be printed.
     * @param indentChars  This string is printed in front of the main text.
     *                     The parameter indentChars is intended to be used as space characters to
     *                     indent the debug prints more and more as we dive into recursive functions.
     */
    private fun debugPrint (text : String, indentChars : String) {
        if (debug) {
            Log.d(TAG, " $indentChars$text")
        }
    }

    //----------------------------------
    //  constants
    //----------------------------------

    companion object {
        private const val TAG = "SetsOfIntsUtil"

        /** Used to limit the size of random numbers generated in the Gaussian function */
        private const val RANDOM_BASELINE = 4.5f

    }
}