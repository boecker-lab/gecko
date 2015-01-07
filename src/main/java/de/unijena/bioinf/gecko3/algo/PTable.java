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

package de.unijena.bioinf.gecko3.algo;

import cern.jet.random.Binomial;
import cern.jet.random.engine.RandomEngine;

import java.util.ArrayList;
import java.util.List;

class PTable {
	private int numberOfChars;
	private final int maxDifferentCharsGenerated;
	private final double[] probabilityForDifferentCharHits;
	private final List<List<double[]>> table;
	private int maxPossiblePositionsCalculated;
	private final RandomEngine random;

	public PTable(double[] probabilityForDifferentCharHits,
                  int initialNumberGeneratedChars,
                  int maxDifferentGeneratedChars,
                  RandomEngine random) {
		this.random = random;
		this.maxDifferentCharsGenerated = maxDifferentGeneratedChars;
		
		numberOfChars = 0;
		for (int c=1; c<probabilityForDifferentCharHits.length; c++)
			if (probabilityForDifferentCharHits[c] != 0.0)
				numberOfChars++;
		
		//sparse char probabilities
		this.probabilityForDifferentCharHits = new double[numberOfChars+1];
		int offset = 0;
		for (int c=1; c<probabilityForDifferentCharHits.length; c++){
			if (probabilityForDifferentCharHits[c] != 0.0)
				this.probabilityForDifferentCharHits[c-offset] = probabilityForDifferentCharHits[c];
			else
				offset++;
		}
		
		table = new ArrayList<>(numberOfChars+1);
		for (int i=0; i<=numberOfChars; i++){
			table.add(new ArrayList<double[]>());
			table.get(i).add(new double[this.maxDifferentCharsGenerated + 1]);
			table.get(i).get(0)[0] = 1.0;
		}
		maxPossiblePositionsCalculated = 0;
		
		getValue(initialNumberGeneratedChars, this.maxDifferentCharsGenerated);
	}

	double getValue(int numberGeneratedChars,
                    int differentCharsGenerated) {
		if (maxPossiblePositionsCalculated < numberGeneratedChars){		
			updatePTable(numberGeneratedChars, differentCharsGenerated);
		}
		return table.get(numberOfChars).get(numberGeneratedChars)[differentCharsGenerated];
	}
	
	private void updatePTable(int numberGeneratedChars,
            int differentCharsGenerated) {
		
		for (int i=0; i<=numberOfChars; i++){
			for (int j=maxPossiblePositionsCalculated+1; j <= numberGeneratedChars; j++){
				table.get(i).add(new double[maxDifferentCharsGenerated + 1]); 
			}
		}
		
		for (int c=1; c<=numberOfChars; c++){
			for (int h=Math.min(c, differentCharsGenerated); h>=1; h--){
				for (int l=numberGeneratedChars; l>= h && l>=maxPossiblePositionsCalculated+1; l--){
					Binomial binomial = null;
					if (!(probabilityForDifferentCharHits[c] == 1.0 || probabilityForDifferentCharHits[c] == 0.0))
						binomial = new Binomial(l, probabilityForDifferentCharHits[c], random);
					if (probabilityForDifferentCharHits[c] == 0.0)
						table.get(c).get(l)[h] += table.get(c-1).get(l)[h];
					else {
						if (probabilityForDifferentCharHits[c] != 1.0)
							table.get(c).get(l)[h] = binomial.pdf(0) * table.get(c-1).get(l)[h];
						else 								
							table.get(c).get(l)[h] = 0.0;
						for (int k=1; k<=l; k++){
							if (probabilityForDifferentCharHits[c] != 1.0)
								table.get(c).get(l)[h] += binomial.pdf(k) * table.get(c-1).get(l-k)[h-1];
							else
								table.get(c).get(l)[h] += table.get(c-1).get(l-k)[h-1];
						}	
					}
				}
			}
		}
		maxPossiblePositionsCalculated = numberGeneratedChars;
	}
}
