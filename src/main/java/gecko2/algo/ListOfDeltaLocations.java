package gecko2.algo;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class ListOfDeltaLocations implements Iterable<DeltaLocation>{
	private SortedSet<DeltaLocation> deltaLocations;
	
	ListOfDeltaLocations(){
		deltaLocations = new TreeSet<DeltaLocation>(new DeltaLocationOrderComperator());
	}
	
	public ListOfDeltaLocations(ListOfDeltaLocations listOfDeltaLocations) {
		this();
		for (DeltaLocation dLoc : listOfDeltaLocations)
			deltaLocations.add(new DeltaLocation(dLoc));
	}

	public void emptyList(){
		deltaLocations.clear();
	}

	public void removeNonInheritableElements(GenomeList genomes, int c, int delta) {
		int chrNr = -1;
		int[] pos = null;
		Iterator<DeltaLocation> dLocIt = deltaLocations.iterator();
		while(dLocIt.hasNext()) {
			DeltaLocation dLoc = dLocIt.next();
			if (dLoc.getChrNr() != chrNr) {
				chrNr = dLoc.getChrNr();
				pos = genomes.get(dLoc.getGenomeNr()).get(chrNr).getPOS(c);
			}
			
			if (pos == null)
				continue;
				
			int i=0;
			for (; i<pos.length; i++) {
				if (dLoc.getL() <= pos[i])
					break;
			}
				
			if (i<pos.length && dLoc.getR() >= pos[i])
				dLoc.increaseHitCount();
			else {
				dLoc.increaseDistance();
				if (!dLoc.isInheritableWithoutC(genomes.get(dLoc.getGenomeNr()).get(dLoc.getChrNr()), delta, c)){
					dLocIt.remove();
				}
			}
		}
	}		


	public void insertDeltaLocation(DeltaLocation deltaLocation) {
		deltaLocations.add(deltaLocation);
	}

	public void mergeLists(ListOfDeltaLocations newList) {
		if (deltaLocations.isEmpty()) {
			deltaLocations = newList.deltaLocations;
			return;
		}
		newList.deltaLocations.addAll(deltaLocations);
		deltaLocations = newList.deltaLocations;
	}

	public boolean minHitsCovered() {
		for (DeltaLocation dLoc : deltaLocations){
			if (dLoc.isValid())
				return true;
		}
		return false;
	}

	@Override
	public Iterator<DeltaLocation> iterator() {
		return deltaLocations.iterator();
	}
	
	public int size() {
		return deltaLocations.size();
	}

	public boolean valid_dLocContainsCharacter(int c, GenomeList genomes) {
		for (DeltaLocation dLoc : deltaLocations){
			if (dLoc.isValid()){
				for (int l=dLoc.getL(); l<=dLoc.getR(); l++)
					if (c == genomes.get(dLoc.getGenomeNr()).get(dLoc.getChrNr()).getGene(l))
						return true;
			}
		}
		return false;
	}
	
	public ListOfDeltaLocations getOptimalCopy() {
		ListOfDeltaLocations newList = new ListOfDeltaLocations();
		for (DeltaLocation dLoc : deltaLocations)
			if (dLoc.isValid()) {
				boolean validLoc = true;
				Iterator<DeltaLocation> newLocIter = newList.iterator();
				while(newLocIter.hasNext()){
					DeltaLocation newLoc = newLocIter.next();
					if (dLoc.getDistance() >= newLoc.getDistance() && dLoc.isNested(newLoc)) {
						validLoc = false;
						break;
					} 
					if (newLoc.getDistance() >= dLoc.getDistance() && newLoc.isNested(dLoc)) {
						newLocIter.remove();
					}
				}
				if (validLoc)
					newList.deltaLocations.add(dLoc);
			}
		return newList;
	}

	public boolean isEmpty() {
		return 0 == deltaLocations.size();
	}
	
	public void removeRefDLocReferenceHit(Pattern pattern, int chrNr) {
		Iterator<DeltaLocation> dLocIt = deltaLocations.iterator();
		while (dLocIt.hasNext()) {
			DeltaLocation dLoc = dLocIt.next();
			if (dLoc.getChrNr() == chrNr &&
					dLoc.getL() <= pattern.getLeftBorder() &&
					dLoc.getR() >= pattern.getRightBorder()) {
				dLocIt.remove();
			}
		}
	}
	
	public void checkForValidDeltaTableLocations(AlgorithmParameters param,
			int clusterSize) {
		if (!param.useDeltaTable())
			return;
		for (DeltaLocation dLoc : deltaLocations) {
			dLoc.checkForDeltaTableValidity(param, clusterSize);
		}
		
	}
	
	/**
	 * For delta locations that are not ordered by the natural order but only by appearance on the chromosome.
	 * @author swinter
	 *
	 */
	private static class DeltaLocationOrderComperator implements Comparator<DeltaLocation> {
		@Override
		public int compare(DeltaLocation o1, DeltaLocation o2) {
			if (o1.getL() < o2.getL())
				return -1;
			if (o1.getL() == o2.getL() && o1.getR() < o2.getR())
				return -1;
			if (o1.getL() == o2.getL() && o1.getR() == o2.getR())
				return 0;
			return 1;
		}
		
	}
}
