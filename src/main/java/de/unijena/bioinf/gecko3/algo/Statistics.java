package de.unijena.bioinf.gecko3.algo;

import cern.jet.random.Binomial;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import de.unijena.bioinf.gecko3.algo.status.AlgorithmProgressListener;
import de.unijena.bioinf.gecko3.algo.status.AlgorithmProgressProvider;
import de.unijena.bioinf.gecko3.algo.status.AlgorithmStatusEvent;
import org.apache.commons.math3.util.Precision;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CancellationException;

class Statistics implements AlgorithmProgressProvider {
	private final GenomeList genomes;
	private final List<ReferenceCluster> refClusterList;
	private final int delta;
	private final boolean singleReference;
	private final Map<Integer, Integer> genomeGroupMapping;
	private final int nrOfGenomeGroups;
	private final boolean useGenomeGrouping;
	
	private BigDecimal testedIntervals;	
	private final RandomEngine random;

	private final List<AlgorithmProgressListener> progressListeners;
	private final int maxProgressValue;
	private int progressValue;
	
	private Statistics(GenomeList genomes, List<ReferenceCluster> refClusterList, int delta, boolean singleReference, int nrOfGenomeGroups, Map<Integer, Integer> genomeGroupMapping) {
		this.genomes = genomes;
		this.refClusterList = refClusterList;
		this.delta = delta;
		this.singleReference = singleReference;
		this.testedIntervals = null;
		this.nrOfGenomeGroups = nrOfGenomeGroups;
		this.genomeGroupMapping = genomeGroupMapping;
		this.useGenomeGrouping = nrOfGenomeGroups != genomes.size();
		
		this.random = new MersenneTwister();

		progressListeners = new ArrayList<>();
		maxProgressValue = genomes.size()* refClusterList.size() + refClusterList.size();
		progressValue = 0;
	}
	
	public static void computeReferenceStatistics(GenomeList genomes, List<ReferenceCluster> refCluster, int delta, boolean singleReference, int nrOfGenomeGroups, Map<Integer, Integer> genomeGroupMapping, List<AlgorithmProgressListener> listeners) {
		Statistics statistics = new Statistics(genomes, refCluster, delta, singleReference, nrOfGenomeGroups, genomeGroupMapping);
		for (AlgorithmProgressListener listener : listeners)
			statistics.addListener(listener);
		
		statistics.computeStatistics();
	}

	private void computeStatistics() {
		int maxClusterSize = getMaxRefClusterSize() + delta;
		
		for (int k=0; k<genomes.size(); k++){
			computeSinglePValuesForGenome(k, maxClusterSize);
		}

		for (ReferenceCluster cluster : refClusterList) {
			double[] best_pValue = determineBestReferenceOccurrence(cluster);
			cluster.setBestCombined_pValue(combine_pValuesWithQuorum(best_pValue, cluster.getCoveredGenomeGroups()));
			//cluster.setBestCombined_pValueCorrected(bonferroniCorrection(cluster));
		}

		fdrCorrection(refClusterList);
	}

	private double[] determineBestReferenceOccurrence(ReferenceCluster cluster){
		fireProgressUpdateEvent(new AlgorithmStatusEvent(progressValue++, AlgorithmStatusEvent.Task.ComputingStatistics));
		double[] best_pValue = new double[nrOfGenomeGroups];  // init with 0.0

		double bestRefLoc_pValue = 0.0;
		DeltaLocation bestRefLoc = null;

		for (int k=0; k<genomes.size(); k++){
			Iterator<DeltaLocation> dLocIt = cluster.getDeltaLocations(k).iterator();
			int genomeGroup = useGenomeGrouping ? genomeGroupMapping.get(k) : k;
			while(dLocIt.hasNext()){
				DeltaLocation dLoc = dLocIt.next();

				double pValue = dLoc.getpValue();
				if (pValue != 0.0 && (best_pValue[genomeGroup] == 0.0 || pValue < best_pValue[genomeGroup]))
					best_pValue[genomeGroup] = pValue;

				if (dLoc.getChrNr() == -1){
					assert(!dLocIt.hasNext());
					dLocIt.remove();
				} else {
					if (dLoc.getDistance() == 0 && pValue > bestRefLoc_pValue){
						if (!singleReference || k==0) {  // only use k==0 as reference if using single reference
							bestRefLoc_pValue = pValue;
							bestRefLoc = dLoc;
						}
					}
				}
			}
		}
		bestRefLoc.setpValue(1.0);
		if (useGenomeGrouping)
			best_pValue[genomeGroupMapping.get(bestRefLoc.getGenomeNr())] = 1.0;
		else
			best_pValue[bestRefLoc.getGenomeNr()] = 1.0;

		cluster.changeReferenceOccurrence(bestRefLoc);

		return best_pValue;
	}
	
	private BigDecimal bonferroniCorrection(ReferenceCluster cluster) {
		if (testedIntervals == null){
			int tmpTestedIntervals = 0;
			int genomesToTest = genomes.size();
			if (singleReference)
				genomesToTest = 1;
			for (int k=0; k<genomesToTest; k++){
				for (int intervalLength=1; intervalLength<=genomes.get(k).getLength(); intervalLength++){
					tmpTestedIntervals += genomes.get(k).getLength() - intervalLength + 1;
				}
			}
			testedIntervals = new BigDecimal(tmpTestedIntervals);
		}
		return cluster.getBestCombined_pValue().multiply(testedIntervals);
	}
	
	private void fdrCorrection(List<ReferenceCluster> clusters) {
		List<ReferenceCluster> sortedList = new ArrayList<>(clusters);
		Collections.sort(sortedList, new Comparator<ReferenceCluster>() {
			@Override
			public int compare(ReferenceCluster o1, ReferenceCluster o2) {
				return o1.getBestCombined_pValue().compareTo(o2.getBestCombined_pValue());
			}
		});
		
		if (testedIntervals == null){
			int tmpTestedIntervals = 0;
			int genomesToTest = genomes.size();
			if (singleReference)
				genomesToTest = 1;
			for (int k=0; k<genomesToTest; k++){
				for (int intervalLength=1; intervalLength<=genomes.get(k).getLength(); intervalLength++){
					tmpTestedIntervals += genomes.get(k).getLength() - intervalLength + 1;
				}
			}
			testedIntervals = new BigDecimal(tmpTestedIntervals);
		}
		
		BigDecimal lastValue = null;
		for (int i=0; i<sortedList.size(); i++) {
			BigDecimal index = new BigDecimal(i+1);
			BigDecimal correction = testedIntervals.divide(index,5, BigDecimal.ROUND_HALF_UP);
			ReferenceCluster cluster = sortedList.get(i);
			BigDecimal correctedValue = cluster.getBestCombined_pValue().multiply(correction);
			if (lastValue != null)
				correctedValue = correctedValue.max(lastValue);
			lastValue = correctedValue;
			cluster.setBestCombined_pValueCorrected(correctedValue);
		}
	}
	
	private BigDecimal combine_pValuesWithQuorumBD(double[] pValue,
			int K, int Q) {
		BigDecimal[] bd = new BigDecimal[pValue.length];
		for (int i=0; i<pValue.length; i++)
			bd[i] = new BigDecimal(pValue[i]);
		return combine_pValuesWithQuorumBD(bd, K, Q);
	}

	private BigDecimal combine_pValuesWithQuorumBD(BigDecimal[] pValue,
			int K, int Q) {
		if (Q == K){
			BigDecimal combined = BigDecimal.ONE;
			for (int k=0; k<K; k++)
				combined = combined.multiply(pValue[k]);
			return combined;
		}
		
		BigDecimal[] pArray = new BigDecimal[K+1];
		Arrays.fill(pArray, BigDecimal.ZERO);
		
		int i=0;
		int offset = 0;
		while(pValue[i].equals( BigDecimal.ONE)){
			offset++;
			i++;
		}
		if (i >= Q)
			return BigDecimal.ONE;
		
		BigDecimal q0 = pValue[i];
		pArray[0] = BigDecimal.ONE.subtract(pValue[i]);
		pArray[1] = pValue[i];
		
		i++;
		
		for (; i<K; i++){
			if (pValue[i].equals(BigDecimal.ONE)){
				offset++;
				continue;
			}
			for (int j=Math.min(i+1, K)-offset; j>=0; j--){
				if (j > 1){
					BigDecimal mul1 = pArray[j].multiply(pValue[i]);
					BigDecimal mul2 = pArray[j-1].multiply(pValue[i]);
					BigDecimal sub = pArray[j].subtract(mul1);
					pArray[j] = sub.add(mul2);
				} else if (j == 1){
					BigDecimal mul1 = pArray[j].multiply(pValue[i]);
					BigDecimal sub1 = pArray[j].subtract(mul1);
					BigDecimal add = sub1.add(pValue[i]);
					BigDecimal mul2 = pArray[i].multiply(q0);
					pArray[j] = add.subtract(mul2);
				} else {
					BigDecimal add = pValue[i].add(q0);
					BigDecimal mul = pValue[i].multiply(q0);
					q0 = add.subtract(mul);
				}
			}
		}
		
		assert(Q-offset > 0); // will not work if Q or rather (Q-offset) is 0, but that does not make sense for our gene clusters!
		
		BigDecimal sum = BigDecimal.ZERO;
		for (int j=Q-offset; j<=K-offset; j++)
			sum = sum.add(pArray[j]);
			
		return sum;
	}
	
	private BigDecimal combine_pValuesWithQuorum(double[] pValue, int Q) {
		if (Q == pValue.length || Q == genomes.size()){
			Probability combined = Probability.ONE;
			for (double aPValue : pValue)
				combined = combined.multiply(aPValue);
			return combined.toBigDecimal();
		}
		
		Probability[] pArray = new Probability[pValue.length+1];
		Arrays.fill(pArray, Probability.ZERO);
		
		int i=0;
		int offset = 0;
		while(pValue[i] == 1.0){
			offset++;
			i++;
		}
		if (i >= Q)
			return BigDecimal.ONE;
		
		double q0 = pValue[i];
		pArray[0] = Probability.ONE.subtract(pValue[i]);
		pArray[1] = new Probability(pValue[i]);
		
		i++;
		
		for (; i<pValue.length; i++){
			if (pValue[i] == 1.0){
				offset++;
				continue;
			}
			for (int j=Math.min(i+1, pValue.length)-offset; j>=0; j--){
				if (j > 1){
					Probability mul1 = pArray[j].multiply(pValue[i]);
					Probability mul2 = pArray[j-1].multiply(pValue[i]);
					Probability sub = pArray[j].subtract(mul1);
					pArray[j] = sub.add(mul2);
				} else if (j == 1){
					Probability mul1 = pArray[j].multiply(pValue[i]);
					Probability sub1 = pArray[j].subtract(mul1);
					Probability add = sub1.add(pValue[i]);
					Probability mul2 = pArray[i].multiply(q0);
					pArray[j] = add.subtract(mul2);
				} else {
					q0 = pValue[i] + q0 - pValue[i] * q0;
				}
			}
		}
		
		assert(Q-offset > 0); // will not work if Q or rather (Q-offset) is 0, but that does not make sense for our gene clusters!
		
		Probability sum = Probability.ZERO;
		for (int j=Q-offset; j<=pValue.length-offset; j++)
			sum = sum.add(pArray[j]);
			
		return sum.toBigDecimal();
	}

	private void computeSinglePValuesForGenome(int genomeNr, int maxClusterSize){
		int[] charFrequencies = genomes.get(genomeNr).getCharFrequency(genomes.getAlphabetSize());
		double[] globalProbabilityForDifferentCharHits = computeGlobalProbabilityForDifferentCharHits(charFrequencies);
		PTable pPlusTable = new PTable(globalProbabilityForDifferentCharHits, maxClusterSize, delta, random);

		for (ReferenceCluster cluster : refClusterList){
			fireProgressUpdateEvent(new AlgorithmStatusEvent(progressValue++, AlgorithmStatusEvent.Task.ComputingStatistics));
			if (cluster.getDeltaLocations(genomeNr).isEmpty()){
				DeltaLocation artificial_dLoc = DeltaLocation.getArtificialDeltaLocation(genomeNr, cluster.getMaxDistance());
				cluster.getDeltaLocations(genomeNr).add(artificial_dLoc);					
			}
			for (DeltaLocation dLoc : cluster.getDeltaLocations(genomeNr)) {
				if (cluster.isOnlyPossibleReferenceOccurrence(dLoc))
					dLoc.setpValue(1.0);                                         // does not need p-value
				else {
					// For individual distance bound
					dLoc.setpValue(prob_C_has_approxOccInGenome(dLoc.getDistance(), genomes.get(genomeNr), cluster.getGeneContent(), pPlusTable, charFrequencies));
					// For global distance bound
					//dLoc.setpValue(prob_C_has_approxOccInGenome(cluster.getMaxDistance(), genomes.get(genomeNr).getLength(), genomes.getAlphabetSize(), cluster.getGeneContent(), pPlusTable, charFrequencies));
				}
			}
		}
	}
	
	private double prob_C_has_approxOccInGenome(int delta, Genome genome,
			List<Integer> geneContent, PTable pPlusTable, int[] charFrequencies) {

		if (noDLocPossible(geneContent, delta, charFrequencies))
			return 0.0;
		
		double[] localCharProb = computeLocalCharProb(geneContent, charFrequencies);
		double probOfC = elementOfC_Prob(geneContent, charFrequencies, genome.getLength());
		
		PTable pTable = new PTable(localCharProb, geneContent.size(), geneContent.size(), random);
		
		double log = 0.0;
		int L = Math.max(1, geneContent.size()-delta);
		boolean notEqual = true;
		while (notEqual){
            if (genome.getNrOfPossibleIntervals(L) == 0)
                break;
			double newLog = log + genome.getNrOfPossibleIntervals(L)*Math.log1p(-1.0*q_L_delta(delta, L, probOfC, geneContent.size(), pTable, pPlusTable));
			
			if (L >= geneContent.size())
				notEqual = !Precision.equalsWithRelativeTolerance(log, newLog, 0.0000001);
			
			if (Double.isNaN(newLog))
				notEqual = false;
			
			log = newLog;
			L++;
		}
		
		if (Double.isNaN(log)) // we get NaN in log, if we try to do log1p(-1)=log(0). In that case we would get pValue = 1.0
			return 1.0;
		else
			return -1.0 * Math.expm1(log);
	}

	private double q_L_delta(int delta, int L, double probOfC, int sizeOfC, PTable pMinusTable,
			PTable pPlusTable) {
		double sum = 0.0;
		for (int d=0; d<=delta; d++)
			sum += p_L_d(d, L, probOfC, sizeOfC, pMinusTable, pPlusTable);
		
		return sum;
	}

	private double p_L_d(int d, int L, double probOfC,
			int sizeOfC, PTable pMinusTable, PTable pPlusTable) {
		double prob = 0.0;
		Binomial binomial = null;
		if (probOfC < 1.0) //!=
			binomial = new Binomial(L, probOfC, random);
			//Illegal argument (5 1,2 random) <= 0.0

		for (int d_plus = Math.max(0, d-(sizeOfC-2)); d_plus<=d; d_plus++){
			if (binomial != null) {
				for (int l=0; l<=L; l++){
					double p_0 = binomial.pdf(l);
					double p_minus = pMinusTable.getValue(l, sizeOfC - (d - d_plus));
					double p_plus = Math.pow(1.0 - probOfC, d_plus) * pPlusTable.getValue(L-l, d_plus);
					
					prob += p_0 * p_minus * p_plus;
				}
			}
			else {   // if probOfC = 1.0, p_0=binomial.pdf(l) == 0, if l != L
				prob += pMinusTable.getValue(L, sizeOfC - (d - d_plus)) * pPlusTable.getValue(0, d_plus);
			}

		}
		return prob;
	}

	private double elementOfC_Prob(List<Integer> geneContent, int[] charFrequencies,
			int totalLength) {
		int totalCharFreq = 0;
		for (Integer gene : geneContent)
			if (gene > 0)
				totalCharFreq += charFrequencies[gene];
		
		return ((double)totalCharFreq)/totalLength;
	}

	private double[] computeLocalCharProb(List<Integer> geneContent,
			int[] charFrequencies) {
		
		double[] localCharProb = new double[geneContent.size()+1];
		int totalFreq = 0;
		int i=1;
		for (Integer gene : geneContent) {
			if (gene >= 0) {
				totalFreq += charFrequencies[gene];
				if (charFrequencies[gene] <= 0)
					localCharProb[i] = 0.0;
				else
					localCharProb[i] = ((double)charFrequencies[gene])/totalFreq;
			}
			i++;
		}

		return localCharProb;
	}

	private boolean noDLocPossible(List<Integer> geneContent, int maxDelta,
								   int[] charFrequencies) {
		int nonAppearingGenes = 0;
		for (Integer gene : geneContent){
			if (gene < 0)
				nonAppearingGenes++;
			else if (charFrequencies[gene] == 0)
				nonAppearingGenes++;
			if (nonAppearingGenes > maxDelta)
				return true;
		}
		return nonAppearingGenes == geneContent.size();
	}

	private int getMaxRefClusterSize() {
		int maxSize = 0;
		
		for (ReferenceCluster cluster : refClusterList)
			if (cluster.getSize() > maxSize)
				maxSize = cluster.getSize();
		
		return maxSize;
	}

	private double[] computeGlobalProbabilityForDifferentCharHits(int[] charFrequencies) {
		double[] charProb = new double[charFrequencies.length + charFrequencies[0]];
		int totalFreq = 0;
		for (int i=0; i<charFrequencies[0]; i++){
			totalFreq++;
			charProb[i+1] = 1.0 / totalFreq;
		}
		for (int i=1; i<charFrequencies.length; i++){
			totalFreq += charFrequencies[i];
			if (charFrequencies[i] <= 0.0)
				charProb[i+charFrequencies[0]] = 0.0;
			else{
				charProb[i+charFrequencies[0]] = ((double)charFrequencies[i] / totalFreq);
			}
		}
		return charProb;
	}

	@Override
	public void addListener(AlgorithmProgressListener listener) {
		if (listener != null) {
			progressListeners.add(listener);
			listener.algorithmProgressUpdate(new AlgorithmStatusEvent(maxProgressValue, AlgorithmStatusEvent.Task.Init));
			listener.algorithmProgressUpdate(new AlgorithmStatusEvent(progressValue, AlgorithmStatusEvent.Task.ComputingStatistics));
		}
	}

	@Override
	public void removeListener(AlgorithmProgressListener listener) {
		if (listener != null)
			progressListeners.remove(listener);
	}

    /**
     * Fires a AlgorithmStatusEvent to all listeners. If the Thread is interrupted, throws a CancellationException to
     * stop the computation
     * @param statusEvent
     * @throws java.util.concurrent.CancellationException if the thread was interrupted
     */
	private void fireProgressUpdateEvent(AlgorithmStatusEvent statusEvent){
        if (Thread.currentThread().isInterrupted()){
            throw new CancellationException();
        }
		for (AlgorithmProgressListener listener : progressListeners)
			listener.algorithmProgressUpdate(statusEvent);
	}
}
