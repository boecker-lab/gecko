package gecko2.io;

import gecko2.algorithm.Chromosome;
import gecko2.algorithm.ExternalGeneId;
import gecko2.algorithm.Gene;
import gecko2.algorithm.Genome;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		
		// using Integer String[] HashMap for geneLabelMap to have multiple id possibility in later 
		// releases
		Map<Integer, ExternalGeneId> geneLabelMap = new HashMap<>();
		Genome[] refGenomes = new Genome[2];
		
		int geneID1 = 1;
		int geneID2 = 2;
		int geneID3 = 3;
		int geneID4 = 4;

        geneLabelMap.put(0, ExternalGeneId.getUnknownGeneID(0));
		geneLabelMap.put(1, new ExternalGeneId("25", 1));
		geneLabelMap.put(2, new ExternalGeneId("21", 1));
		geneLabelMap.put(3, new ExternalGeneId("7", 1));
		geneLabelMap.put(4, new ExternalGeneId("4", 1));
		
		
		List<Gene> genes1 = new ArrayList<>();
		List<Gene> genes2 = new ArrayList<>();
		Gene gen1 = new Gene("fusA", geneID1, "elongation factor EF-G");
		Gene gen2 = new Gene("tufA1", geneID2, "elongation factor EF-Tu");
		Gene gen3 = new Gene("fusA", geneID3, "elongation factor EF-G");
		Gene gen4 = new Gene("tufA1", geneID4, "elongation factor EF-Tu");
		
		genes1.add(gen1);
		genes1.add(gen2);
		genes2.add(gen3);
		genes2.add(gen4);
		
		refGenomes[0] = new Genome("Shorty 1");
		refGenomes[1] = new Genome("Shorty 2");
		refGenomes[0].addChromosome(new Chromosome("", genes1, refGenomes[0]));
		refGenomes[1].addChromosome(new Chromosome("", genes2, refGenomes[1]));

        File inputFile = new File(getClass().getResource("/c2.cog").getFile());

        CogFileReader reader = new CogFileReader(inputFile);
			
        reader.readData();

        testReader(reader, geneLabelMap, refGenomes);
	}
	
	@Test
	public void readFileContentTest2() throws  IOException, ParseException {
		// define the result we want to get
		
		// using Integer String[] HashMap for geneLabelMap to have multiple id possibility in later 
		// releases
		Map<Integer, ExternalGeneId> geneLabelMap = new HashMap<>();
		
		Genome[] refGenomes = new Genome[4];
		
		// setup int ids
		int geneID1 = 1;
		int geneID2 = 2;
		int geneID3 = 3;
		int geneID4 = 4;
		int geneID5 = 5;
		int geneID6 = 6;
		int geneID7 = 7;
		int geneID8 = 8;
		int geneID9 = 9;
		int geneID10 = 10;
		int geneID11 = 11;
		int geneID12 = 12;

		// setup genelabelmap
        geneLabelMap.put(0, ExternalGeneId.getUnknownGeneID(0));
		geneLabelMap.put(1, new ExternalGeneId("1", 8));
		geneLabelMap.put(2, new ExternalGeneId("2", 3));
		geneLabelMap.put(3, new ExternalGeneId("3", 6));
		geneLabelMap.put(4, new ExternalGeneId("4", 9));
		geneLabelMap.put(5, new ExternalGeneId("5", 6));
		geneLabelMap.put(6, new ExternalGeneId("6", 3));
		geneLabelMap.put(7, new ExternalGeneId("7", 4));
		geneLabelMap.put(8, new ExternalGeneId("8", 2));
		geneLabelMap.put(9, new ExternalGeneId("9", 2));
		geneLabelMap.put(10, new ExternalGeneId("10", 1));
		geneLabelMap.put(11, new ExternalGeneId("11", 2));
		geneLabelMap.put(12, new ExternalGeneId("12", 3));
		
		// build genes
		// Shorty1
		Gene gen11 = new Gene("fusA", geneID1, "elongation factor EF-G");
		Gene gen21 = new Gene("tufA1", geneID2, "elongation factor EF-Tu");
		Gene gen31 = new Gene("rpsJ", geneID3, "ribosomal protein S10");
		Gene gen41 = new Gene("rplC", geneID4, "ribosomal protein L03");
		Gene gen51 = new Gene("rplD", geneID5, "ribosomal protein L04");
		Gene gen61 = new Gene("rplW", geneID6, "ribosomal protein L23");
		Gene gen71 = new Gene("rpsJ", geneID3, "ribosomal protein S10");
		Gene gen81 = new Gene("fusA", geneID1, "elongation factor EF-G");
		
		// Shorty2
		Gene gen12 = new Gene("fusA", geneID7, "elongation factor EF-G");
		Gene gen22 = new Gene("tufA1", geneID4, "elongation factor EF-Tu");
		Gene gen32 = new Gene("rpsJ", geneID3, "ribosomal protein S10");
		Gene gen42 = new Gene("rplC", geneID1, "ribosomal protein L03");
		Gene gen52 = new Gene("rplD", geneID8, "ribosomal protein L04");
		Gene gen62 = new Gene("rplW", geneID4, "ribosomal protein L23");
		Gene gen72 = new Gene("rplB", geneID5, "ribosomal protein L02");
		Gene gen82 = new Gene("rpsS", geneID4, "ribosomal protein S19");
		Gene gen92 = new Gene("rplV", geneID1, "ribosomal protein L22");
		Gene gen102 = new Gene("rpsC", geneID6, "ribosomal protein S03");
		Gene gen112 = new Gene("rplP", geneID9, "ribosomal protein L16");
		Gene gen122 = new Gene("rplP", geneID7, "ribosomal protein L16");
		
		// Shorty3
		Gene gen13 = new Gene("smb ", -geneID10, "small protein B");
		Gene gen23 = new Gene("----", -geneID3, "hypothetical protein");
		Gene gen33 = new Gene("----", geneID5, "hypothetical protein");
		Gene gen43 = new Gene("----", geneID1, "putative protein");
		Gene gen53 = new Gene("nsd ", -geneID4, "nucleotide sugar dehydrogenase");
		Gene gen63 = new Gene("----", geneID11, "hypothetical protein");
		Gene gen73 = new Gene("----", geneID11, "hypothetical protein");
		Gene gen83 = new Gene("abcT8", geneID2, "ABC transporter");
		Gene gen93 = new Gene("serS", -geneID4, "seryl-tRNA synthetase");
		Gene gen103 = new Gene("glmS", geneID3, "glucosamine-fructose-6-phosphate aminotransferase");
		Gene gen113 = new Gene("argD", -geneID6, "N-acetylornithine aminotransferase");
		Gene gen123 = new Gene("nsd ", -geneID1, "nucleotide sugar dehydrogenase");
		Gene gen133 = new Gene("rodA", -geneID5, "rod shape determining protein RodA");
		Gene gen143 = new Gene("rodA", -geneID12, "rod shape determining protein RodA");
		
		// Shorty4 C1
		Gene gen14 = new Gene("smb ", -geneID7, "small protein B");
		Gene gen24 = new Gene("smb ", -geneID7, "small protein B");
		Gene gen34 = new Gene("----", -geneID4, "hypothetical protein");
		Gene gen44 = new Gene("----", geneID4, "hypothetical protein");
		Gene gen54 = new Gene("----", geneID2, "putative protein");
		Gene gen64 = new Gene("nsd ", -geneID3, "nucleotide sugar dehydrogenase");
		Gene gen74 = new Gene("----", geneID1, "hypothetical protein");
		Gene gen84 = new Gene("abcT8", geneID12, "ABC transporter");
		
		// Shorty4 C2
		Gene gen15 = new Gene("smb ", -geneID9, "small protein B");
		Gene gen25 = new Gene("----", -geneID1, "hypothetical protein");
		Gene gen35 = new Gene("----", geneID4, "hypothetical protein");
		Gene gen45 = new Gene("----", geneID8, "putative protein");
		Gene gen55 = new Gene("nsd ", -geneID5, "nucleotide sugar dehydrogenase");
		Gene gen65 = new Gene("----", geneID12, "hypothetical protein");
		Gene gen75 = new Gene("abcT8", geneID5, "ABC transporter");
		
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

        reader.readData();

        testReader(reader, geneLabelMap, refGenomes);
	}

    /**
     * Simple test for file with two genomes which contain one gene, with two ids.
     */
    @Test
    public void readFileContentMultiIdTest() throws  IOException, ParseException {
        // define the result we want to get

        // using Integer String[] HashMap for geneLabelMap to have multiple id possibility in later
        // releases
        Map<Integer, ExternalGeneId> geneLabelMap = new HashMap<>();
        Genome[] refGenomes = new Genome[2];

        int geneID1 = 1;
        int geneID2 = 2;
        int geneID3 = 3;
        int geneID4 = 4;

        geneLabelMap.put(0, ExternalGeneId.getUnknownGeneID(0));
        geneLabelMap.put(1, new ExternalGeneId("25", 1));
        geneLabelMap.put(2, new ExternalGeneId("21", 1));
        geneLabelMap.put(3, new ExternalGeneId("7", 1));
        geneLabelMap.put(4, new ExternalGeneId("4", 1));


        List<Gene> genes1 = new ArrayList<>();
        List<Gene> genes2 = new ArrayList<>();
        Gene gen1 = new Gene("fusA", geneID1, "elongation factor EF-A");
        Gene gen2 = new Gene("fusA", geneID2, "elongation factor EF-A");
        Gene gen3 = new Gene("fusB", geneID3, "elongation factor EF-B");
        Gene gen4 = new Gene("fusB", geneID4, "elongation factor EF-B");

        genes1.add(gen1);
        genes1.add(gen2);
        genes2.add(gen3);
        genes2.add(gen4);

        refGenomes[0] = new Genome("Shorty 1");
        refGenomes[1] = new Genome("Shorty 2");
        refGenomes[0].addChromosome(new Chromosome("", genes1, refGenomes[0]));
        refGenomes[1].addChromosome(new Chromosome("", genes2, refGenomes[1]));

        File inputFile = new File(getClass().getResource("/multiIdGenes.cog").getFile());

        CogFileReader reader = new CogFileReader(inputFile);

        reader.readData();

        testReader(reader, geneLabelMap, refGenomes);
    }

    @Test
    public void readUnHomologueGenesTest() throws  IOException, ParseException {
        File inputFile = new File(getClass().getResource("/unHomologueGenes.cog").getFile());

        CogFileReader reader = new CogFileReader(inputFile);

        reader.readData();

        assertEquals(reader.getGenomes().length, 2);
        assertEquals(reader.getGenomes()[0].getChromosomes().size(), 1);
        assertEquals(reader.getGenomes()[0].getChromosomes().get(0).getGenes().size(), 10);

        assertEquals(reader.getGenomes()[1].getChromosomes().size(), 1);
        assertEquals(reader.getGenomes()[1].getChromosomes().get(0).getGenes().size(), 3);

        Map<Integer, ExternalGeneId> geneLabelMap = reader.getGeneLabelMap();
        assertEquals(geneLabelMap.size(), 6);
        for (ExternalGeneId entry : geneLabelMap.values()) {
            if (entry.getId().equals("1"))
                assertTrue(entry.isSingleGeneFamily());
            else if (entry.getId().equals("gF1"))
                assertTrue(entry.isSingleGeneFamily());
            else if (entry.getId().equals("4"))
                assertFalse(entry.isSingleGeneFamily());
            else if (entry.getId().equals("5"))
                assertFalse(entry.isSingleGeneFamily());
            else if (entry.getId().equals("gF2"))
                assertFalse(entry.isSingleGeneFamily());
            else if (entry.getId().equals(Gene.UNKNOWN_GENE_ID)) {
                assertEquals(entry.getFamilySize(), 5);
                assertTrue(entry.isSingleGeneFamily());
            }
            else
                fail("unexpected map entry: " + entry.toString());
        }
    }


    private static void testReader(CogFileReader reader, Map<Integer, ExternalGeneId> geneLabelMap, Genome[] refGenomes) {
        assertEquals(reader.getGeneLabelMap().size(), geneLabelMap.size());

        for( int i = 1; i < reader.getGeneLabelMap().size() + 1; i++) {
            // Check keys and values for the geneLabelMap
            for(int z = 0; z < reader.getGeneLabelMap().keySet().toArray().length; z++)
                assertEquals(geneLabelMap.keySet().toArray()[z], reader.getGeneLabelMap().keySet().toArray()[z]);

            for( int j = 1; j <= reader.getGeneLabelMap().size(); j++)
                assertEquals(geneLabelMap.get(j), reader.getGeneLabelMap().get(j));

            // check the genome array
            for( int j = 0; j < reader.getGenomes().length; j++) {
                for (int k = 0; k < refGenomes[j].getChromosomes().size(); k++) {
                    assertEquals(refGenomes[j].getChromosomes().get(k).getName(), reader.getGenomes()[j].getChromosomes().get(k).getName());

                    int p = 0;

                    for (Gene gene: refGenomes[j].getChromosomes().get(k).getGenes()) {
                        assertEquals(gene.getAnnotation(), reader.getGenomes()[j].getChromosomes().get(k).getGenes().get(p).getAnnotation());
                        assertEquals(gene.getId(), reader.getGenomes()[j].getChromosomes().get(k).getGenes().get(p).getId());
                        assertEquals(gene.getName(), reader.getGenomes()[j].getChromosomes().get(k).getGenes().get(p).getName());
                        assertEquals(gene.getSummary(), reader.getGenomes()[j].getChromosomes().get(k).getGenes().get(p).getSummary());
                        p++;
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
                {{0,1,2,3,4,5,6,3,1,0}},
                {{0,7,4,3,1,8,4,5,4,1,6,9,7,0}},
                {{0,10,3,5,1,4,11,11,2,4,3,6,1,5,12,0}},
                {{0,7,7,4,4,2,3,1,12,0},{0,9,1,4,8,5,12,5,0}}
        };

        // When
        CogFileReader reader = new CogFileReader(inputFile);
        reader.readData();
        Gene.setGeneLabelMap(reader.getGeneLabelMap());
        int[][][] genomes = Genome.toIntArray(reader.getGenomes());

        Genome.printIntArray(genomes);

        // Then
        assertArrayEquals(expected, genomes);
    }

    @Test
    public void testFileReaderToGenomeIntArrayWithUnHomologous() throws IOException, ParseException{
        // Given
        File inputFile = new File(getClass().getResource("/unHomologueGenes.cog").getFile());
        int[][][] expected = new int[][][] {
                {{0,1,2,3,4,5,6,7,8,8,9,0}},
                {{0,10,7,9,0}}
        };

        // When
        CogFileReader reader = new CogFileReader(inputFile);
        reader.readData();
        Gene.setGeneLabelMap(reader.getGeneLabelMap());
        int[][][] genomes = Genome.toIntArray(reader.getGenomes());

        // Then
        assertArrayEquals(expected, genomes);
    }

    @Test
    public void testFileReaderToReducedGenomeIntArrayWithUnHomologous() throws IOException, ParseException{
        // Given
        File inputFile = new File(getClass().getResource("/unHomologueGenes.cog").getFile());
        int[][][] expected = new int[][][] {
                {{0,-1,-1,-1,-1,-1,-1,1,2,2,3,0}},
                {{0,-1,1,3,0}}
        };

        // When
        CogFileReader reader = new CogFileReader(inputFile);
        reader.readData();
        Gene.setGeneLabelMap(reader.getGeneLabelMap());
        int[][][] genomes = Genome.toReducedIntArray(reader.getGenomes());

        Genome.printIntArray(genomes);

        // Then
        assertArrayEquals(expected, genomes);
    }
}
