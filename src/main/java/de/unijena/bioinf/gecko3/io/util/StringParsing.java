package de.unijena.bioinf.gecko3.io.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Some static helpers for string parsing
 * Created by swinter on 09.12.2014.
 */
public class StringParsing {
    private static final Logger logger = LoggerFactory.getLogger(StringParsing.class);

    /**
     * Parses the deepToString of an int[][]
     * @param full
     * @return
     */
    public static int[][] parseDeltaTable(String full) throws ParseException {
        String[] delimitedStrings = full.split("\\]");
        int[][] table = new int[delimitedStrings.length][];
        int i = 0;
        for (String delimitedString : delimitedStrings) {
            String cleanedString = delimitedString.substring(delimitedString.lastIndexOf("[") + 1);
            String[] singleValues = cleanedString.split(",");
            if (singleValues.length != 3) {
                ParseException e = new ParseException("Expected exactly 3 values in a single array, got: " + singleValues.length, 0);
                logger.warn("Malformed parameters at {}", singleValues, e);
                throw e;
            } try {
                int[] d = new int[3];
                d[0] = Integer.parseInt(singleValues[0].trim());
                d[1] = Integer.parseInt(singleValues[1].trim());
                d[2] = Integer.parseInt(singleValues[2].trim());
                table[i] = d;
            } catch (NumberFormatException e){
                logger.warn("Not a number in {}", singleValues, e);
                throw new ParseException(e.getMessage(), 0);
            }
            i++;
        }
        return table;
    }
}
