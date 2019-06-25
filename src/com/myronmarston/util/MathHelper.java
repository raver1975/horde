/*
 * Copyright 2008, Myron Marston <myron DOT marston AT gmail DOT com>
 *
 * This file is part of Fractal Composer.
 *
 * Fractal Composer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option any later version.
 *
 * Fractal Composer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Fractal Composer.  If not, see <http://www.gnu.org/licenses/>. 
 */

package com.myronmarston.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class containing static helper methods for some math functions used to
 * generate fractal music.
 * 
 * @author Myron
 */
public class MathHelper {
    
    /**
     * Calculates the greatest common divisor of two numbers, using Euclid's 
     * algorithm.
     * 
     * @param m a number
     * @param n another number
     * @return the greatest common divisor of these two numbers
     */
    public static long greatestCommonDivisor(final long m, final long n) {
        return n == 0 ? m : greatestCommonDivisor(n, m % n);        
    }
    
    /**
     * Calculates the least common multiple of two numbers.
     * 
     * @param m a number
     * @param n another number
     * @return the least common multiple of these two numbers
     */
    public static long leastCommonMultiple(final long m, final long n) {
        return  m * (n / greatestCommonDivisor(m, n));
    }
    
    /**
     * Calculates the least common multiple of a list of numbers.
     * 
     * @param integers a list of integers
     * @return the least common multiple of this list
     */
    public static long leastCommonMultiple(List<Long> integers) {
        if (integers.size() == 0) throw new IllegalArgumentException("You passed an empty list.  The list must contain at least one value.");
        
        // if we only have one number, that's our LCM...
        if (integers.size() == 1) return integers.get(0);
        
        // get the LCM for the first two numbers...
        long firstTwoNumLCM = leastCommonMultiple(integers.get(0), integers.get(1));
                
        // if this is our whole list, we can return this...
        if (integers.size() == 2) return firstTwoNumLCM;
                
        // otherwise, construct a list, replacing the first two items with their LCM
        ArrayList<Long> restOfList = new ArrayList<Long>(integers.size() - 1);
        restOfList.add(firstTwoNumLCM);
        restOfList.addAll(integers.subList(2, integers.size()));
        
        // get the LCM of this smaller list...
        return leastCommonMultiple(restOfList);
    }        
    
    /**
     * Calculates the base two logarithm of the given number.
     * 
     * @param a number to take the log of     
     * @return the base 2 logarithm
     */
    public static double log2(double a) {
        return Math.log(a) / Math.log(2);        
    }   
    
    /**
     * Checks if the given number is a power of 2.
     * 
     * @param num the number to check
     * @return true if it is a power of 2
     */
    public static boolean numIsPowerOf2(long num) {
        // taken from the wikipedia article http://en.wikipedia.org/wiki/Power_of_two
        if (num == 0) return false;
        return (num & (num - 1)) == 0;
    }    

    /**
     * Gets the largest power of 2 that is less than the given number.  For 
     * example, given 7, 4 will be returned.  Given 18, 16 will be returned.
     * 
     * @param num the given number
     * @return the largest power of 2 less than num
     */
    public static long getLargestPowerOf2LessThanGivenNumber(long num) {
        if (num <= 1) {
            throw new UnsupportedOperationException("This method is not designed to handle the given number.  It only deals with numbers that are greater than 1.");
        }
        
        //TODO: is there a more efficient algorithm for this?
        while (true) {
            num -= 1;
            if (numIsPowerOf2(num)) return num;
            
            // given our asserted assumptions above, we should never wind up negative...
            assert num > 0 : num;
        }        
    }
    
    /**
     * Normalizes a value into a certain range. This is like a modulus function,
     * only this properly handles negative values.
     * 
     * @param value the value to normalize
     * @param mod the modulus number
     * @return the normalized value
     */
    public static int getNormalizedValue(int value, int mod) {        
        while (value < 0) value += mod;        
        int returnVal = value % mod;
        assert returnVal >= 0 && returnVal < mod : returnVal;
        
        return returnVal;
    }
    
    /**
     * Gets the median value from a list of numbers.  This is the value that has
     * the same number of values above it and below it when the list has
     * been sorted.  For a list that has an even number of entries, this method
     * will return the average of the middle two values.
     * 
     * @param nums the list of values
     * @return the median
     */
    public static int getMedianValue(List<Integer> nums) {
        List<Integer> copy = new ArrayList<Integer>(nums);
        Collections.sort(copy);
        
        int numberOfValues = copy.size();
        if (numberOfValues % 2 == 0) {
            // we have an even number of values
            // example: for 6 values, we want to return the average of indices 2 and 3
            // (0, 1, 2, 3, 4, 5)
            int val1 = copy.get(numberOfValues / 2);
            int val2 = copy.get((numberOfValues - 2) / 2);
            return (val1 + val2) / 2;
        } else {
            // we have an odd number of values
            // example: for 5 values, we want to return the number at index 2 (0, 1, 2, 3, 4)
            return copy.get((numberOfValues - 1) / 2);
        }        
    }
}
