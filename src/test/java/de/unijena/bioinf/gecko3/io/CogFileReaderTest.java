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

package de.unijena.bioinf.gecko3.io;

import de.unijena.bioinf.gecko3.datastructures.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * JUnit test for the new class InputFileReader.
 * 
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.10
 */
public class CogFileReaderTest {

	/**
	 * Simple test for file with two genomes which contain two genes.
	 */
	@Test
	public void readFileContentTest() throws  IOException, ParseException {
		// define the result we want to get

        GeneFamily unknownGeneFamily = GeneFamily.getUnknownTestGeneFamily(0);

        GeneFamily[] geneFamilies = GeneFamilyTest.getTestGeneFamilies(
                new String[]{"25", "21", "7", "4"},
                new int[]{1, 1, 1, 1});

        Set<GeneFamily> geneFamilySet = new HashSet<>(Arrays.asList(geneFamilies));

		List<Gene> genes1 = new ArrayList<>();
		List<Gene> genes2 = new ArrayList<>();
		Gene gen1 = new Gene("fusA", geneFamilies[0], Gene.GeneOrientation.POSITIVE, "elongation factor EF-G");
		Gene gen2 = new Gene("tufA1", geneFamilies[1], Gene.GeneOrientation.POSITIVE, "elongation factor EF-Tu");
		Gene gen3 = new Gene("fusA", geneFamilies[2], Gene.GeneOrientation.POSITIVE, "elongation factor EF-G");
		Gene gen4 = new Gene("tufA1", geneFamilies[3], Gene.GeneOrientation.POSITIVE, "elongation factor EF-Tu");
		
		genes1.add(gen1);
		genes1.add(gen2);
		genes2.add(gen3);
		genes2.add(gen4);

        Genome[] refGenomes = new Genome[2];
		refGenomes[0] = new Genome("Shorty 1");
		refGenomes[1] = new Genome("Shorty 2");
		refGenomes[0].addChromosome(new Chromosome("", genes1, refGenomes[0]));
		refGenomes[1].addChromosome(new Chromosome("", genes2, refGenomes[1]));

        File inputFile = new File(getClass().getResource("/c2.cog").getFile());

        CogFileReader reader = new CogFileReader(inputFile);
			
        DataSet data = reader.readData();

        testReader(data, geneFamilySet, unknownGeneFamily, refGenomes);
	}
	
	@Test
	public void readFileContentTest2() throws  IOException, ParseException {
		// define the result we want to get

        // define gene families
        GeneFamily unknownGeneFamily = GeneFamily.getUnknownTestGeneFamily(0);

        GeneFamily[] geneFamilies = GeneFamilyTest.getTestGeneFamilies(
                new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"},
                new int[]{8, 3, 6, 9, 6, 3, 4, 2, 2, 1, 2, 3, });

        Set<GeneFamily> geneFamilySet = new HashSet<>(Arrays.asList(geneFamilies));
		
		// build genes
		// Shorty1
		Gene gen11 = new Gene("fusA", geneFamilies[0], Gene.GeneOrientation.POSITIVE, "elongation factor EF-G");
		Gene gen21 = new Gene("tufA1", geneFamilies[1], Gene.GeneOrientation.POSITIVE, "elongation factor EF-Tu");
		Gene gen31 = new Gene("rpsJ", geneFamilies[2], Gene.GeneOrientation.POSITIVE, "ribosomal protein S10");
		Gene gen41 = new Gene("rplC", geneFamilies[3], Gene.GeneOrientation.POSITIVE, "ribosomal protein L03");
		Gene gen51 = new Gene("rplD", geneFamilies[4], Gene.GeneOrientation.POSITIVE, "ribosomal protein L04");
		Gene gen61 = new Gene("rplW", geneFamilies[5], Gene.GeneOrientation.POSITIVE, "ribosomal protein L23");
		Gene gen71 = new Gene("rpsJ", geneFamilies[2], Gene.GeneOrientation.POSITIVE, "ribosomal protein S10");
		Gene gen81 = new Gene("fusA", geneFamilies[0], Gene.GeneOrientation.POSITIVE, "elongation factor EF-G");
		
		// Shorty2
		Gene gen12 = new Gene("fusA", geneFamilies[6], Gene.GeneOrientation.POSITIVE, "elongation factor EF-G");
		Gene gen22 = new Gene("tufA1", geneFamilies[3], Gene.GeneOrientation.POSITIVE, "elongation factor EF-Tu");
		Gene gen32 = new Gene("rpsJ", geneFamilies[2], Gene.GeneOrientation.POSITIVE, "ribosomal protein S10");
		Gene gen42 = new Gene("rplC", geneFamilies[0], Gene.GeneOrientation.POSITIVE, "ribosomal protein L03");
		Gene gen52 = new Gene("rplD", geneFamilies[7], Gene.GeneOrientation.POSITIVE, "ribosomal protein L04");
		Gene gen62 = new Gene("rplW", geneFamilies[3], Gene.GeneOrientation.POSITIVE, "ribosomal protein L23");
		Gene gen72 = new Gene("rplB", geneFamilies[4], Gene.GeneOrientation.POSITIVE, "ribosomal protein L02");
		Gene gen82 = new Gene("rpsS", geneFamilies[3], Gene.GeneOrientation.POSITIVE, "ribosomal protein S19");
		Gene gen92 = new Gene("rplV", geneFamilies[0], Gene.GeneOrientation.POSITIVE, "ribosomal protein L22");
		Gene gen102 = new Gene("rpsC", geneFamilies[5], Gene.GeneOrientation.POSITIVE, "ribosomal protein S03");
		Gene gen112 = new Gene("rplP", geneFamilies[8], Gene.GeneOrientation.POSITIVE, "ribosomal protein L16");
		Gene gen122 = new Gene("rplP", geneFamilies[6], Gene.GeneOrientation.POSITIVE, "ribosomal protein L16");
		
		// Shorty3
		Gene gen13 = new Gene("smb", geneFamilies[9], Gene.GeneOrientation.NEGATIVE, "small protein B");
		Gene gen23 = new Gene("----", geneFamilies[2], Gene.GeneOrientation.NEGATIVE, "hypothetical protein");
		Gene gen33 = new Gene("----", geneFamilies[4], Gene.GeneOrientation.POSITIVE, "hypothetical protein");
		Gene gen43 = new Gene("----", geneFamilies[0], Gene.GeneOrientation.POSITIVE, "putative protein");
		Gene gen53 = new Gene("nsd", geneFamilies[3], Gene.GeneOrientation.NEGATIVE, "nucleotide sugar dehydrogenase");
		Gene gen63 = new Gene("----", geneFamilies[10], Gene.GeneOrientation.POSITIVE, "hypothetical protein");
		Gene gen73 = new Gene("----", geneFamilies[10], Gene.GeneOrientation.POSITIVE, "hypothetical protein");
		Gene gen83 = new Gene("abcT8", geneFamilies[1], Gene.GeneOrientation.POSITIVE, "ABC transporter");
		Gene gen93 = new Gene("serS", geneFamilies[3], Gene.GeneOrientation.NEGATIVE, "seryl-tRNA synthetase");
		Gene gen103 = new Gene("glmS", geneFamilies[2], Gene.GeneOrientation.POSITIVE, "glucosamine-fructose-6-phosphate aminotransferase");
		Gene gen113 = new Gene("argD", geneFamilies[5], Gene.GeneOrientation.NEGATIVE, "N-acetylornithine aminotransferase");
		Gene gen123 = new Gene("nsd", geneFamilies[0], Gene.GeneOrientation.NEGATIVE, "nucleotide sugar dehydrogenase");
		Gene gen133 = new Gene("rodA", geneFamilies[4], Gene.GeneOrientation.NEGATIVE, "rod shape determining protein RodA");
		Gene gen143 = new Gene("rodA", geneFamilies[11], Gene.GeneOrientation.NEGATIVE, "rod shape determining protein RodA");
		
		// Shorty4 C1
		Gene gen14 = new Gene("smb", geneFamilies[6], Gene.GeneOrientation.NEGATIVE, "small protein B");
		Gene gen24 = new Gene("smb", geneFamilies[6], Gene.GeneOrientation.NEGATIVE, "small protein B");
		Gene gen34 = new Gene("----", geneFamilies[3], Gene.GeneOrientation.NEGATIVE, "hypothetical protein");
		Gene gen44 = new Gene("----", geneFamilies[3], Gene.GeneOrientation.POSITIVE, "hypothetical protein");
		Gene gen54 = new Gene("----", geneFamilies[1], Gene.GeneOrientation.POSITIVE, "putative protein");
		Gene gen64 = new Gene("nsd", geneFamilies[2], Gene.GeneOrientation.NEGATIVE, "nucleotide sugar dehydrogenase");
		Gene gen74 = new Gene("----", geneFamilies[0], Gene.GeneOrientation.POSITIVE, "hypothetical protein");
		Gene gen84 = new Gene("abcT8", geneFamilies[11], Gene.GeneOrientation.POSITIVE, "ABC transporter");
		
		// Shorty4 C2
		Gene gen15 = new Gene("smb", geneFamilies[8], Gene.GeneOrientation.NEGATIVE, "small protein B");
		Gene gen25 = new Gene("----", geneFamilies[0], Gene.GeneOrientation.NEGATIVE, "hypothetical protein");
		Gene gen35 = new Gene("----", geneFamilies[3], Gene.GeneOrientation.POSITIVE, "hypothetical protein");
		Gene gen45 = new Gene("----", geneFamilies[7], Gene.GeneOrientation.POSITIVE, "putative protein");
		Gene gen55 = new Gene("nsd", geneFamilies[4], Gene.GeneOrientation.NEGATIVE, "nucleotide sugar dehydrogenase");
		Gene gen65 = new Gene("----", geneFamilies[11], Gene.GeneOrientation.POSITIVE, "hypothetical protein");
		Gene gen75 = new Gene("abcT8", geneFamilies[4], Gene.GeneOrientation.POSITIVE, "ABC transporter");
		
		// build chromosomes		
		//Shorty1
		List<Gene> genes1 = new ArrayList<>();
			
		//Shorty2
		List<Gene> genes2 = new ArrayList<>();
		
		//Shorty3
		List<Gene> genes3 = new ArrayList<>();
		
		//Shorty4 C1
		List<Gene> genes4 = new ArrayList<>();
			
		//Shorty4 C2
		List<Gene> genes42 = new ArrayList<>();

		
		genes1.add(gen11);
		genes1.add(gen21);
		genes1.add(gen31);
		genes1.add(gen41);
		genes1.add(gen51);
		genes1.add(gen61);
		genes1.add(gen71);
		genes1.add(gen81);
		genes2.add(gen12);
		
		genes2.add(gen22);
		genes2.add(gen32);
		genes2.add(gen42);
		genes2.add(gen52);
		genes2.add(gen62);
		genes2.add(gen72);
		genes2.add(gen82);
		genes2.add(gen92);
		genes2.add(gen102);
		genes2.add(gen112);
		genes2.add(gen122);
		
		genes3.add(gen13);
		genes3.add(gen23);
		genes3.add(gen33);
		genes3.add(gen43);
		genes3.add(gen53);
		genes3.add(gen63);
		genes3.add(gen73);
		genes3.add(gen83);
		genes3.add(gen93);
		genes3.add(gen103);
		genes3.add(gen113);
		genes3.add(gen123);
		genes3.add(gen133);
		genes3.add(gen143);
		
		genes4.add(gen14);
		genes4.add(gen24);
		genes4.add(gen34);
		genes4.add(gen44);
		genes4.add(gen54);
		genes4.add(gen64);
		genes4.add(gen74);
		genes4.add(gen84);
		
		genes42.add(gen15);
		genes42.add(gen25);
		genes42.add(gen35);
		genes42.add(gen45);
		genes42.add(gen55);
		genes42.add(gen65);
		genes42.add(gen75);
		
		// build genomes
        Genome[] refGenomes = new Genome[4];
		refGenomes[0] = new Genome("Shorty 1");
		refGenomes[1] = new Genome("Shorty 2");
		refGenomes[2] = new Genome("Shorty 3");
		refGenomes[3] = new Genome("Shorty 4");
		refGenomes[0].addChromosome(new Chromosome("", genes1, refGenomes[0]));
		refGenomes[1].addChromosome(new Chromosome("", genes2, refGenomes[1]));
		refGenomes[2].addChromosome(new Chromosome("", genes3, refGenomes[2]));
		refGenomes[3].addChromosome(new Chromosome("chromosome I", genes4, refGenomes[3]));
		refGenomes[3].addChromosome(new Chromosome("chromosome II", genes42, refGenomes[3]));

        File inputFile = new File(getClass().getResource("/c.cog").getFile());

        CogFileReader reader = new CogFileReader(inputFile);

        DataSet data = reader.readData();

        testReader(data, geneFamilySet, unknownGeneFamily, refGenomes);
	}

    /**
     * Simple test for file with two genomes which contain one gene, with two ids.
     */
    @Test
    public void readFileContentMultiIdTest() throws  IOException, ParseException {
        // define the result we want to get
        GeneFamily unknownGeneFamily = GeneFamily.getUnknownTestGeneFamily(1);

        GeneFamily[] geneFamilies = GeneFamilyTest.getTestGeneFamilies(
                new String[]{"25", "21", "COG07", "NOG08", "7", "4", "COG08", "NIG07"},
                new int[]{3, 1, 3, 4, 1, 1, 1, 1});

        Set<GeneFamily> geneFamilySet = new HashSet<>(Arrays.asList(geneFamilies));

        List<Gene> genes1 = new ArrayList<>();
        genes1.add(new Gene("int1", geneFamilies[0], Gene.GeneOrientation.POSITIVE, "int gene"));
        genes1.add(new Gene("int1", geneFamilies[1], Gene.GeneOrientation.POSITIVE, "int gene"));
        genes1.add(new Gene("str1", geneFamilies[2], Gene.GeneOrientation.POSITIVE, "string gene"));
        genes1.add(new Gene("str1", geneFamilies[3], Gene.GeneOrientation.POSITIVE, "string gene"));
        genes1.add(new Gene("mix1", geneFamilies[0], Gene.GeneOrientation.POSITIVE, "mixed gene"));
        genes1.add(new Gene("mix1", geneFamilies[3], Gene.GeneOrientation.POSITIVE, "mixed gene"));
        genes1.add(new Gene("nh", geneFamilies[2], Gene.GeneOrientation.POSITIVE, "gene with 0 homologe"));
        genes1.add(new Gene("nh", unknownGeneFamily, Gene.GeneOrientation.POSITIVE, "gene with 0 homologe"));

        List<Gene> genes2 = new ArrayList<>();
        genes2.add(new Gene("int2", geneFamilies[4], Gene.GeneOrientation.POSITIVE, "int gene 2"));
        genes2.add(new Gene("int2", geneFamilies[5], Gene.GeneOrientation.POSITIVE, "int gene 2"));
        genes2.add(new Gene("str1.2", geneFamilies[2], Gene.GeneOrientation.POSITIVE, "string gene"));
        genes2.add(new Gene("str1.2", geneFamilies[3], Gene.GeneOrientation.POSITIVE, "string gene"));
        genes2.add(new Gene("mix1.2", geneFamilies[0], Gene.GeneOrientation.POSITIVE, "mixed gene"));
        genes2.add(new Gene("mix1.2", geneFamilies[3], Gene.GeneOrientation.POSITIVE, "mixed gene"));
        genes2.add(new Gene("str2", geneFamilies[6], Gene.GeneOrientation.POSITIVE, "string gene 2"));
        genes2.add(new Gene("str2", geneFamilies[7], Gene.GeneOrientation.POSITIVE, "string gene 2"));

        Genome[] refGenomes = new Genome[2];
        refGenomes[0] = new Genome("Shorty 1");
        refGenomes[1] = new Genome("Shorty 2");
        refGenomes[0].addChromosome(new Chromosome("", genes1, refGenomes[0]));
        refGenomes[1].addChromosome(new Chromosome("", genes2, refGenomes[1]));

        File inputFile = new File(getClass().getResource("/multiIdGenes.cog").getFile());

        CogFileReader reader = new CogFileReader(inputFile);

        DataSet data = reader.readData();

        testReader(data, geneFamilySet, unknownGeneFamily, refGenomes);
    }

    @Test
    public void readUnHomologueGenesTest() throws  IOException, ParseException {
        File inputFile = new File(getClass().getResource("/unHomologueGenes.cog").getFile());

        CogFileReader reader = new CogFileReader(inputFile);

        DataSet data = reader.readData();

        assertEquals(2, data.getGenomes().length);
        assertEquals(1, data.getGenomes()[0].getChromosomes().size());
        assertEquals(10, data.getGenomes()[0].getChromosomes().get(0).getGenes().size());

        assertEquals(1, data.getGenomes()[1].getChromosomes().size());
        assertEquals(3, data.getGenomes()[1].getChromosomes().get(0).getGenes().size());

        Set<GeneFamily> geneFamilySet = data.getGeneFamilySet();
        GeneFamily unknownGeneFamily = data.getUnknownGeneFamily();

        assertEquals(5, unknownGeneFamily.getFamilySize());
        assertEquals(5, geneFamilySet.size());
        for (GeneFamily entry : geneFamilySet) {
            if (entry.getExternalId().equals("1"))
                assertTrue(entry.isSingleGeneFamily());
            else if (entry.getExternalId().equals("gF1"))
                assertTrue(entry.isSingleGeneFamily());
            else if (entry.getExternalId().equals("4"))
                assertFalse(entry.isSingleGeneFamily());
            else if (entry.getExternalId().equals("5"))
                assertFalse(entry.isSingleGeneFamily());
            else if (entry.getExternalId().equals("gF2"))
                assertFalse(entry.isSingleGeneFamily());
            else
                fail("unexpected map entry: " + entry.toString());
        }
    }


    private static void testReader(DataSet readDataSet, Set<GeneFamily> geneFamilySet, GeneFamily unknownGeneFamily, Genome[] refGenomes) {
        assertEquals(geneFamilySet, readDataSet.getGeneFamilySet());
        assertEquals(unknownGeneFamily, readDataSet.getUnknownGeneFamily());
        Map<GeneFamily, Integer> algorithmIdMap = new HashMap<>();

        for( int j = 0; j < readDataSet.getGenomes().length; j++) {
            assertEquals(refGenomes[j].getName(), readDataSet.getGenomes()[j].getName());
            assertEquals(refGenomes[j].getChromosomes().size(), readDataSet.getGenomes()[j].getChromosomes().size());
            for (int k = 0; k < refGenomes[j].getChromosomes().size(); k++) {
                assertEquals(refGenomes[j].getChromosomes().get(k).getName(), readDataSet.getGenomes()[j].getChromosomes().get(k).getName());
                assertEquals(refGenomes[j].getChromosomes().get(k).getGenes().size(), readDataSet.getGenomes()[j].getChromosomes().get(k).getGenes().size());

                for (int i=0; i<refGenomes[j].getChromosomes().get(k).getGenes().size(); i++){
                    Gene expectedGene = refGenomes[j].getChromosomes().get(k).getGenes().get(i);
                    Gene actualGene = readDataSet.getGenomes()[j].getChromosomes().get(k).getGenes().get(i);

                    assertEquals(expectedGene.getAnnotation(), actualGene.getAnnotation());
                    assertEquals(expectedGene.getExternalId(), actualGene.getExternalId());
                    assertEquals(expectedGene.getFamilySize(), actualGene.getFamilySize());
                    assertEquals(expectedGene.getName(), actualGene.getName());
                    assertEquals(expectedGene.getSummary(), actualGene.getSummary());
                    if (!algorithmIdMap.containsKey(actualGene.getGeneFamily())) {
                        algorithmIdMap.put(actualGene.getGeneFamily(), expectedGene.getAlgorithmId());
                        if (expectedGene.getAlgorithmId() == -1)
                            assertEquals(expectedGene.getAlgorithmId(), actualGene.getAlgorithmId());
                    }
                    else {
                        assertEquals(expectedGene.getAlgorithmId(), (int)algorithmIdMap.get(actualGene.getGeneFamily()));
                        assertFalse(actualGene.getAlgorithmId() == -1);
                    }
                }
            }
        }
    }

    @Test
    public void testFileReaderToGenomeIntArrayWithoutUnHomologous() throws IOException, ParseException{
        // Given
        File inputFile = new File(getClass().getResource("/c.cog").getFile());
        int[][][] expected = new int[][][] {
                {{0,2,8,1,3,4,5,1,2,0}},
                {{0,6,3,1,2,11,3,4,3,2,5,10,6,0}},
                {{0,12,1,4,2,3,7,7,8,3,1,5,2,4,9,0}},
                {{0,6,6,3,3,8,1,2,9,0},{0,10,2,3,11,4,9,4,0}}
        };

        // When
        CogFileReader reader = new CogFileReader(inputFile);
        DataSet data = reader.readData();

        int[][][] genomes = data.toIntArray();

        // Then
        assertArrayEquals(expected, genomes);
    }

    @Test
    public void testFileReaderToGenomeIntArrayWithUnHomologous() throws IOException, ParseException{
        // Given
        File inputFile = new File(getClass().getResource("/unHomologueGenes.cog").getFile());
        int[][][] expected = new int[][][] {
                {{0,4,5,6,7,8,9,2,1,1,3,0}},
                {{0,10,2,3,0}}
        };

        // When
        CogFileReader reader = new CogFileReader(inputFile);
        DataSet data = reader.readData();

        int[][][] genomes = data.toIntArray();

        // Then
        assertArrayEquals(expected, genomes);
    }
    
    @Test
    public void testFileReaderToReducedGenomeGroupIntArrayWithUnHomologous() throws IOException, ParseException{
        // Given
        File inputFile = new File(getClass().getResource("/unHomologueGenes.cog").getFile());
        int[][][] expected = new int[][][] {
                {{0,-6,2,1,1,3,0}},
                {{0,-1,2,3,0}}
        };

        // When
        CogFileReader reader = new CogFileReader(inputFile);
        DataSet data = reader.readData();

        int[][][] genomes = data.toReducedIntArray();

        // Then
        assertArrayEquals(expected, genomes);
    }
    
    @Test
    public void testFileReaderToReducedGenomeIntArrayWithUnHomologous() throws IOException, ParseException{
        // Given
        File inputFile = new File(getClass().getResource("/unHomologueGenes.cog").getFile());
        int[][][] expected = new int[][][] {
                {{0,-6,2,1,1,3,0}},
                {{0,-1,2,3,0}}
        };

        // When
        CogFileReader reader = new CogFileReader(inputFile);
        DataSet data = reader.readData();

        int[][][] genomes = data.toReducedIntArray();

        // Then
        assertArrayEquals(expected, genomes);
    }
}
