/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.algo.util;

import java.util.Arrays;
import java.util.List;

/**
 * An array of int values.
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */

public class IntArray{
    /**
     * Constructs an IntArray that holds the values from an List.
     * @param list the list of Integer who's values the IntArray will store.
     * @param zeroTerminate if the list should be zero terminated.
     */
    private static int[] newIntArray(List<Integer> list, boolean zeroTerminate) {
    	int[] values;
        if (list == null)
            values = new int[0];
        else {
            if (!zeroTerminate) {
                values = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    values[i] = list.get(i);
                }
            }
            else {          // values[0] and values[list.size()+1] are by default filled with 0.
                values = new int[list.size()+2];
                for (int i = 1; i < list.size()+1; i++) {
                    values[i] = list.get(i-1);
                }
            }
        }
        return values;
    }
    
    /**
     * Constructs an int array.
     * @param list the array who's values the array will store.
     * @param zeroTerminate if the list should be zero terminated.
     */
    public static int[] newIntArray(int[] list, boolean zeroTerminate) {
    	int[] values;
        if (list == null)
            values = new int[0];
        else {
            if (!zeroTerminate) {
                values = new int[list.length];
                System.arraycopy(list, 0, values, 0, list.length);
            }
            else {          // values[0] and values[list.size()+1] are by default filled with 0.
                values = new int[list.length+2];
                System.arraycopy(list, 0, values, 1, list.length + 1 - 1);
            }
        }
        return values;
    }

    /**
     * Factory method that creates a new instance of IntArray that holds n-times the int value.
     * @param n the number of times the Element is included in the array.
     * @param value the value the array is initialized to.
     * @return the new instance of IntArray.
     */
    public static int[] newIntArray(int n, int value) {
    	int[] values = new int[n];
        if (value!=0)       // new int[] are filled with 0 by default.
        	Arrays.fill(values, value);
        return values;
    }

    /**
     * Factory method, that returns an new instance of IntArray that holds the values from an List. The IntArray starts and ends with a terminal character 0.
     * @param list the list of Integer who's values the IntArray will store.
     * @return the new instance of IntArray.
     */
    public static int[] newZeroTerminatedInstance(List<Integer> list) {
        return newIntArray(list, true);
    }
    
    /**
     * Factory method, that returns an new instance of IntArray that holds the values from an List. The IntArray starts and ends with a terminal character 0.
     * @param list the list of Integer who's values the IntArray will store.
     * @return the new instance of IntArray.
     */
    public static int[] newZeroTerminatedInstance(int[] list) {
        return newIntArray(list, true);
    }

    /**
     * Resets the values of the IntArray to value.
     * @param value The value the IntArray is reset to.
     */
    public static void reset(int[] values, int value) {
        Arrays.fill(values, value);
    }

    /**
     * Increments all values in the IntArray.
     */
    public static void increaseAll(int[] array) {
        for (int i=0; i<array.length; i++) {
            array[i]++;   
        }
    }
}