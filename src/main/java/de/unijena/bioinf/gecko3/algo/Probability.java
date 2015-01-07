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

package de.unijena.bioinf.gecko3.algo;

import java.math.BigDecimal;

class Probability {
	private int exp;
	private double base;
	
	static final Probability ONE = new Probability(1.0);
	static final Probability ZERO = new Probability(0.0);
	
	Probability(double value) {
		if (value == 0.0) {
			exp = 0;
			base = 0.0;
		} else {
			boolean negative = false;
			if (value < 0) {
				value = -value;
				negative = true;
			}
			double log = Math.log10(value);

			exp = (int)log;  // equals floor(log)
			base = (value/Math.pow(10, exp));
			if (negative)
				base = -base;
		}
	}

	private Probability(double value, int exp) {
		if (value == 0.0) {
			this.exp = 0;
			this.base = 0.0;
		} else {
			boolean negative = false;
			if (value < 0) {
				value = -value;
				negative = true;
			}
			double log = Math.log10(value);

			this.exp = (int)log;
			this.base = (value/Math.pow(10, this.exp));
			this.exp += exp;
			if (negative)
				base = -base;
		}
	}

	boolean isZeroProbability(){
		return exp == 0 && base == 0.0;
	}

	Probability add(Probability prob2)
	{
		if (this.isZeroProbability())
			return prob2;
		if (prob2.isZeroProbability())
			return this;
		int expDiff = this.exp - prob2.exp;
		if (expDiff > 0) {
			return new Probability(this.base + prob2.base*(Math.pow(10, -expDiff)), this.exp);
		} else if (expDiff < 0) {
			return new Probability(prob2.base + this.base*(Math.pow(10, expDiff)), prob2.exp);
		} else	{
			return new Probability(this.base + prob2.base, this.exp);
		}
	}

	Probability add(double prob2)
	{
		Probability tmpProb = new Probability(prob2);
		return this.add(tmpProb);
	}

	Probability subtract(Probability prob2)
	{
		if (this.isZeroProbability())
			return prob2;
		if (prob2.isZeroProbability())
			return this;
		int expDiff = this.exp - prob2.exp;
		if (expDiff > 0) {
			return new Probability(this.base - prob2.base*(Math.pow(10, -expDiff)), this.exp);
		} else if (expDiff < 0) {
			return new Probability(prob2.base - this.base*(Math.pow(10, expDiff)), prob2.exp);
		} else	{
			return new Probability(this.base - prob2.base, this.exp);
		}
	}
	
	Probability subtract(double prob2) {
		Probability tmpProb = new Probability(prob2);
		return this.subtract(tmpProb);
	}

	Probability multiply(Probability prob2){
		double prod = this.base * prob2.base;
		if (prod == 0.0) {
			return new Probability(0.0);
		}
		int exp = this.exp + prob2.exp;
		return new Probability(prod, exp);
	}

	Probability multiply(double prob2){
		Probability tmpProb = new Probability(prob2);
		return this.multiply(tmpProb);
	}
	
	double toDouble() {
		return base*Math.pow(10, exp);
	}
	
	BigDecimal toBigDecimal() {
		return new BigDecimal(base).scaleByPowerOfTen(exp);
	}

	@Override
	public String toString() {
		return toBigDecimal().stripTrailingZeros().toString();
	}
}
