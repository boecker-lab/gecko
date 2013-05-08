package gecko2.algo;

import java.util.*;

import cern.jet.random.Binomial;
import cern.jet.random.engine.RandomEngine;

class PTable {
	private int numberOfChars;
	private final int maxDifferentCharsGenerated;
	private final double[] charProbs;
	private final List<List<double[]>> table;
	private int maxPossiblePositionsCalculated;
	private final RandomEngine random;

	public PTable(int differentChars, int initialNumberGeneratedChars, int differentGeneratedChars,
			double[] localCharProb, RandomEngine random) {
		this.random = random;
		
		maxDifferentCharsGenerated = differentGeneratedChars;
		
		numberOfChars = 0;
		for (int c=1; c<=differentChars; c++)
			if (localCharProb[c] != 0.0)
				numberOfChars++;
		
		//sparse char probs
		charProbs = new double[numberOfChars+1];
		int offset = 0;
		for (int c=1; c<=differentChars; c++){
			if (localCharProb[c] != 0.0)
				charProbs[c-offset] = localCharProb[c];
			else
				offset++;
		}
		
		table = new ArrayList<List<double[]>>(numberOfChars+1);
		for (int i=0; i<=numberOfChars; i++){
			table.add(new ArrayList<double[]>());  // TODO perhaps use max size here. Improves performance, how much extra memory is needed?
			table.get(i).add(new double[maxDifferentCharsGenerated + 1]);
			table.get(i).get(0)[0] = 1.0;
		}
		maxPossiblePositionsCalculated = 0;
		
		getValue(initialNumberGeneratedChars, differentGeneratedChars);
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
					if (!(charProbs[c] == 1.0 || charProbs[c] == 0.0))
						binomial = new Binomial(l, charProbs[c], random);
					if (charProbs[c] == 0.0)
						table.get(c).get(l)[h] += table.get(c-1).get(l)[h];
					else {
						if (charProbs[c] != 1.0)
							table.get(c).get(l)[h] = binomial.pdf(0) * table.get(c-1).get(l)[h];
						else 								
							table.get(c).get(l)[h] = 0.0;
						for (int k=1; k<=l; k++){
							if (charProbs[c] != 1.0)
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
