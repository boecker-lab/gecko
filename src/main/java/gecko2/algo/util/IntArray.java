package gecko2.algo.util;

import java.util.Arrays;
import java.util.List;

/**
 * An array of int values.
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */

public class IntArray{
    /**
     * Constructs an IntArray that holds the values from an List<Integer>.
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
     * Constructs an IntArray that holds the values from an List<Integer>.
     * @param list the list of Integer who's values the IntArray will store.
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
     * Factory method, that returns an new instance of IntArray that holds the values from an List<Integer>. The IntArray starts and ends with a terminal character 0.
     * @param list the list of Integer who's values the IntArray will store.
     * @return the new instance of IntArray.
     */
    public static int[] newZeroTerminatedInstance(List<Integer> list) {
        return newIntArray(list, true);
    }
    
    /**
     * Factory method, that returns an new instance of IntArray that holds the values from an List<Integer>. The IntArray starts and ends with a terminal character 0.
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