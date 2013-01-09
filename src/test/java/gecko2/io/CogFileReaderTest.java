package gecko2.io;

import static org.junit.Assert.*;

import gecko2.GeckoInstance;
import gecko2.GenomeOccurence;
import gecko2.LinePassedException;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.Gene;
import gecko2.algorithm.Genome;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

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
	public void readFileContentTest() 
	{
		// define the result we want to get
		
		// using Integer String[] HashMap for geneLabelMap to have multiple id possibility in later 
		// releases
		HashMap<Integer, String[]> geneLabelMap = new HashMap<Integer, String[]>();
		Genome[] refGenomes = new Genome[2];
		
		int geneID1 = 1;
		int geneID2 = 2;
		int geneID3 = 3;
		int geneID4 = 4;
		
		String[] geneString1 = {"25"};
		String[] geneString2 = {"21"};
		String[] geneString3 = {"7"};
		String[] geneString4 = {"4"};
	
		geneLabelMap.put(1, geneString1);
		geneLabelMap.put(2, geneString2);
		geneLabelMap.put(3, geneString3);
		geneLabelMap.put(4, geneString4);
		
		
		ArrayList<Gene> genes1 = new ArrayList<Gene>();
		ArrayList<Gene> genes2 = new ArrayList<Gene>();
		Gene gen1 = new Gene("fusA", geneID1, "elongation factor EF-G", false);
		Gene gen2 = new Gene("tufA1", geneID2, "elongation factor EF-Tu", false);
		Gene gen3 = new Gene("fusA", geneID3, "elongation factor EF-G", false);
		Gene gen4 = new Gene("tufA1", geneID4, "elongation factor EF-Tu", false);
		
		genes1.add(gen1);
		genes1.add(gen2);
		genes2.add(gen3);
		genes2.add(gen4);
		
		refGenomes[0] = new Genome("Shorty 1");
		refGenomes[1] = new Genome("Shorty 2");
		refGenomes[0].addChromosome(new Chromosome("", genes1, refGenomes[0]));
		refGenomes[1].addChromosome(new Chromosome("", genes2, refGenomes[1]));
		
		ArrayList<GenomeOccurence> genOcc = null;
		
		CogFileReader reader = new CogFileReader();
		
		try 
		{
			File inputFile = new File(CogFileReaderTest.class.getResource("/c2.cog").toURI());
			GeckoInstance.getInstance().setCurrentInputFile(inputFile);
			
			genOcc = reader.importGenomes(inputFile);
		
			reader.readFileContent(genOcc);
		} 
		catch (EOFException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (LinePassedException e) 
		{
			e.printStackTrace();
		} 
		catch (URISyntaxException e) 
		{
			e.printStackTrace();
		}
		
		
		assertEquals(reader.getGeneLabelMap().size(), geneLabelMap.size());
		
		for( int i = 1; i < reader.getGeneLabelMap().size() + 1; i++)
		{	
			// Check keys and values for the geneLabelMap
			for(int z = 0; z < reader.getGeneLabelMap().keySet().toArray().length; z++)
			{
				//int z;
				assertEquals((Integer) geneLabelMap.keySet().toArray()[z], (Integer) reader.getGeneLabelMap().keySet().toArray()[z]);
			}
			
			for( int j = 1; j <= reader.getGeneLabelMap().size(); j++)
			{
				assertArrayEquals(geneLabelMap.get(j), reader.getGeneLabelMap().get(j));
			}
			
			// check the genome array
			for( int j = 0; j < reader.getGenomes().length; j++)
			{
				for (int k = 0; k < refGenomes[j].getChromosomes().size(); k++)
				{
					assertEquals(refGenomes[j].getChromosomes().get(k).getName(), reader.getGenomes()[j].getChromosomes().get(k).getName());
					
					int p = 0;
					
					for (Gene gene: refGenomes[j].getChromosomes().get(k).getGenes())
					{
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
	public void readFileContentTest2() 
	{
		// define the result we want to get
		
		// using Integer String[] HashMap for geneLabelMap to have multiple id possibility in later 
		// releases
		HashMap<Integer, String[]> geneLabelMap = new HashMap<Integer, String[]>();
		
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
		
		// setup string IDs
		String[] geneString1 = {"1"};
		String[] geneString2 = {"2"};
		String[] geneString3 = {"3"};
		String[] geneString4 = {"4"};
		String[] geneString5 = {"5"};
		String[] geneString6 = {"6"};
		String[] geneString7 = {"7"};
		String[] geneString8 = {"8"};
		String[] geneString9 = {"9"};
		String[] geneString10 = {"10"};
		String[] geneString11 = {"11"};
		String[] geneString12 = {"12"};
	
		// setup genelabelmap
		geneLabelMap.put(1, geneString1);
		geneLabelMap.put(2, geneString2);
		geneLabelMap.put(3, geneString3);
		geneLabelMap.put(4, geneString4);
		geneLabelMap.put(5, geneString5);
		geneLabelMap.put(6, geneString6);
		geneLabelMap.put(7, geneString7);
		geneLabelMap.put(8, geneString8);
		geneLabelMap.put(9, geneString9);
		geneLabelMap.put(10, geneString10);
		geneLabelMap.put(11, geneString11);
		geneLabelMap.put(12, geneString12);		
		
		// build genes
		// Shorty1
		Gene gen11 = new Gene("fusA", geneID1, "elongation factor EF-G", false);
		Gene gen21 = new Gene("tufA1", geneID2, "elongation factor EF-Tu", false);
		Gene gen31 = new Gene("rpsJ", geneID3, "ribosomal protein S10", false);
		Gene gen41 = new Gene("rplC", geneID4, "ribosomal protein L03", false);
		Gene gen51 = new Gene("rplD", geneID5, "ribosomal protein L04", false);
		Gene gen61 = new Gene("rplW", geneID6, "ribosomal protein L23", false);
		Gene gen71 = new Gene("rpsJ", geneID3, "ribosomal protein S10", false);
		Gene gen81 = new Gene("fusA", geneID1, "elongation factor EF-G", false);
		
		// Shorty2
		Gene gen12 = new Gene("fusA", geneID7, "elongation factor EF-G", false);
		Gene gen22 = new Gene("tufA1", geneID4, "elongation factor EF-Tu", false);
		Gene gen32 = new Gene("rpsJ", geneID3, "ribosomal protein S10", false);
		Gene gen42 = new Gene("rplC", geneID1, "ribosomal protein L03", false);
		Gene gen52 = new Gene("rplD", geneID8, "ribosomal protein L04", false);
		Gene gen62 = new Gene("rplW", geneID4, "ribosomal protein L23", false);
		Gene gen72 = new Gene("rplB", geneID5, "ribosomal protein L02", false);
		Gene gen82 = new Gene("rpsS", geneID4, "ribosomal protein S19", false);
		Gene gen92 = new Gene("rplV", geneID1, "ribosomal protein L22", false);
		Gene gen102 = new Gene("rpsC", geneID6, "ribosomal protein S03", false);
		Gene gen112 = new Gene("rplP", geneID9, "ribosomal protein L16", false);
		Gene gen122 = new Gene("rplP", geneID7, "ribosomal protein L16", false);
		
		// Shorty3
		Gene gen13 = new Gene("smb ", -geneID10, "small protein B", false);
		Gene gen23 = new Gene("----", -geneID3, "hypothetical protein", false);
		Gene gen33 = new Gene("----", geneID5, "hypothetical protein", false);
		Gene gen43 = new Gene("----", geneID1, "putative protein", false);
		Gene gen53 = new Gene("nsd ", -geneID4, "nucleotide sugar dehydrogenase", false);
		Gene gen63 = new Gene("----", geneID11, "hypothetical protein", false);
		Gene gen73 = new Gene("----", geneID11, "hypothetical protein", false);
		Gene gen83 = new Gene("abcT8", geneID2, "ABC transporter", false);
		Gene gen93 = new Gene("serS", -geneID4, "seryl-tRNA synthetase", false);
		Gene gen103 = new Gene("glmS", geneID3, "glucosamine-fructose-6-phosphate aminotransferase", false);
		Gene gen113 = new Gene("argD", -geneID6, "N-acetylornithine aminotransferase", false);
		Gene gen123 = new Gene("nsd ", -geneID1, "nucleotide sugar dehydrogenase", false);
		Gene gen133 = new Gene("rodA", -geneID5, "rod shape determining protein RodA", false);
		Gene gen143 = new Gene("rodA", -geneID12, "rod shape determining protein RodA", false);
		
		// Shorty4 C1
		Gene gen14 = new Gene("smb ", -geneID7, "small protein B", false);
		Gene gen24 = new Gene("smb ", -geneID7, "small protein B", false);
		Gene gen34 = new Gene("----", -geneID4, "hypothetical protein", false);
		Gene gen44 = new Gene("----", geneID4, "hypothetical protein", false);
		Gene gen54 = new Gene("----", geneID2, "putative protein", false);
		Gene gen64 = new Gene("nsd ", -geneID3, "nucleotide sugar dehydrogenase", false);
		Gene gen74 = new Gene("----", geneID1, "hypothetical protein", false);
		Gene gen84 = new Gene("abcT8", geneID12, "ABC transporter", false);
		
		// Shorty4 C2
		Gene gen15 = new Gene("smb ", -geneID9, "small protein B", false);
		Gene gen25 = new Gene("----", -geneID1, "hypothetical protein", false);
		Gene gen35 = new Gene("----", geneID4, "hypothetical protein", false);
		Gene gen45 = new Gene("----", geneID8, "putative protein", false);
		Gene gen55 = new Gene("nsd ", -geneID5, "nucleotide sugar dehydrogenase", false);
		Gene gen65 = new Gene("----", geneID12, "hypothetical protein", false);
		Gene gen75 = new Gene("abcT8", geneID5, "ABC transporter", false);
		
		// build chromosomes		
		//Shorty1
		ArrayList<Gene> genes1 = new ArrayList<Gene>();
			
		//Shorty2
		ArrayList<Gene> genes2 = new ArrayList<Gene>();
		
		//Shorty3
		ArrayList<Gene> genes3 = new ArrayList<Gene>();
		
		//Shorty4 C1
		ArrayList<Gene> genes4 = new ArrayList<Gene>();
			
		//Shorty4 C2
		ArrayList<Gene> genes42 = new ArrayList<Gene>();

		
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
		
		ArrayList<GenomeOccurence> genOcc = null;
		
		CogFileReader reader = new CogFileReader();
		
		try 
		{
			File inputFile = new File(CogFileReaderTest.class.getResource("/c.cog").toURI());
			GeckoInstance.getInstance().setCurrentInputFile(inputFile);
			
			genOcc = reader.importGenomes(inputFile);
		
			reader.readFileContent(genOcc);
		} 
		catch (EOFException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (LinePassedException e) 
		{
			e.printStackTrace();
		} 
		catch (URISyntaxException e) 
		{
			e.printStackTrace();
		}
		
		assertEquals(reader.getGeneLabelMap().size(), geneLabelMap.size());
		
		//assertEquals(GeckoInstance.getInstance().getColormap(), colorMap);
		for( int i = 1; i <= reader.getGeneLabelMap().size(); i++)
		{	
			// Check keys and values for the geneLabelMap
			for(int z = 0; z < reader.getGeneLabelMap().keySet().toArray().length; z++)
			{
				//int z;
				assertEquals((Integer) geneLabelMap.keySet().toArray()[z], (Integer) reader.getGeneLabelMap().keySet().toArray()[z]);
			}
			
			for( int j = 1; j <= reader.getGeneLabelMap().size(); j++)
			{
				assertArrayEquals(geneLabelMap.get(j), reader.getGeneLabelMap().get(j));
			}
			
			// check the genome array
			for( int j = 0; j < reader.getGenomes().length; j++)
			{
				for (int k = 0; k < refGenomes[j].getChromosomes().size(); k++)
				{
					assertEquals(refGenomes[j].getChromosomes().get(k).getName(), reader.getGenomes()[j].getChromosomes().get(k).getName());
					
					int p = 0;
					
					for (Gene gene: refGenomes[j].getChromosomes().get(k).getGenes())
					{
						// System.out.println(p);
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
}
