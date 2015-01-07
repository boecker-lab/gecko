/*
 * Copyright 2014 Sascha Winter
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

package de.unijena.bioinf.gecko3.testUtils;

/**
* @author Sascha Winter (sascha.winter@uni-jena.de)
*/
public class ExpectedDeltaLocationValues {
    private final int chrNr;
    private final int l;
    private final int r;
    private final int distance;
    private final double pValue;

    public ExpectedDeltaLocationValues(int chrNr, int l, int r, int distance) {
        this.chrNr = chrNr;
        this.l = l;
        this.r = r;
        this.distance = distance;
        this.pValue = -1.0;
    }

    public ExpectedDeltaLocationValues(int chrNr, int l, int r, int distance, double pValue) {
        this.chrNr = chrNr;
        this.l = l;
        this.r = r;
        this.distance = distance;
        this.pValue = pValue;
    }

    public int getChrNr() {
        return chrNr;
    }

    public int getL() {
        return l;
    }

    public int getR() {
        return r;
    }

    public int getDistance() {
        return distance;
    }

    public double getpValue() {
        return pValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpectedDeltaLocationValues that = (ExpectedDeltaLocationValues) o;

        if (chrNr != that.chrNr) return false;
        if (distance != that.distance) return false;
        if (l != that.l) return false;
        if (Double.compare(that.pValue, pValue) != 0) return false;
        if (r != that.r) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = chrNr;
        result = 31 * result + l;
        result = 31 * result + r;
        result = 31 * result + distance;
        temp = Double.doubleToLongBits(pValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ExpectedDeltaLocationValues{" +
                "chrNr=" + chrNr +
                ", l=" + l +
                ", r=" + r +
                ", distance=" + distance +
                ", pValue=" + pValue +
                '}';
    }
}
