package gecko2.io;

import gecko2.GeckoInstance;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.Genome;
import gecko2.gui.Gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import javax.swing.JOptionPane;

/**
 * The class implements a reader for .gck files (session files).
 * The code is exported from GeckoInstance.java
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.03
 */
public class GckFileReader 
{
	/**
	 * Storing place for the colorMap
	 */
	private HashMap<Integer, Color> colorMap;
	
	/**
	 * Storing place for the geneLabelMap 
	 */
	private HashMap<Integer, String[]> geneLabelMap;
	
	/**
	 * Storing place for the genomes.
	 */
	private Genome[] genomes;
	
	/**
	 * Storing place for the gene clusters.
	 */
	private GeneCluster[] clusters;
	
	/**
	 * Storing place for the length of the longest id.
	 */
	private int maxIdLength;
	
	/**
	 * The method is a getter for the geneLabelMap which contains the relation between external ID
	 * and internal ID from the gene names
	 * 
	 * @return the geneLabelMap (HashMap)
	 */
	public HashMap<Integer, String[]> getGeneLabelMap()
	{
		return this.geneLabelMap;
	}
	
	/**
	 * Reads a gecko session from a file
	 * @param f The file to read from
	 */
	public void loadSessionFromFile(File f)  
	{
		GeckoInstance.getInstance().setLastOpendFile(f);
		GeckoInstance.getInstance().getGui().changeMode(Gui.Mode.READING_GENOMES);
		new SessionLoadingThread(f);
	}
	
	/**
	 * Implementation of the Runnable for the SessionLoadingThread
	 */
	class SessionLoadingThread implements Runnable 
	{
		private File f;
		
		/**
		 * The constructor sets the file where to  store the data and starts the thread.
		 * 
		 * @param f file object which refers to the file where the data will be stored
		 */
		public SessionLoadingThread(File f) 
		{
			this.f = f;
			new Thread(this).start();
		}
			
		@SuppressWarnings("unchecked")
		public void run() 
		{
			FileInputStream fis = null;
			ObjectInputStream o = null;
			try 
			{
				fis = new FileInputStream(f);
				o = new ObjectInputStream(fis);
				genomes = (Genome[]) o.readObject();
				geneLabelMap = (HashMap<Integer, String[]>) o.readObject();
				colorMap = (HashMap<Integer, Color>) o.readObject();
				clusters = (GeneCluster[]) o.readObject();
				maxIdLength = (Integer) o.readObject();
				
				if (clusters != null)
				{
					for (GeneCluster c : clusters)
					{
						c.setMatch(true);
					}
				}
					
				EventQueue.invokeLater(new Runnable() 
				{	
					public void run() 
					{		
						/* Give the GeckoInstance the readed data */
						GeckoInstance.getInstance().setClusters(clusters);
						GeckoInstance.getInstance().setGeneLabelMap(geneLabelMap);
						GeckoInstance.getInstance().setColorMap(colorMap);
						GeckoInstance.getInstance().setGenomes(genomes);
						GeckoInstance.getInstance().setMaxIdLength(maxIdLength);
						
						// update the gui
						GeckoInstance.getInstance().setReducedList(null);
						GeckoInstance.getInstance().getGui().updateViewscreen();
						GeckoInstance.getInstance().getGui().updategcSelector();
						GeckoInstance.getInstance().fireDataChanged();
						GeckoInstance.getInstance().getGui().changeMode(Gui.Mode.SESSION_IDLE);
					}
				});
			} 
			catch (IOException e) 
			{
				EventQueue.invokeLater(new Runnable() 
				{
					public void run() 
					{
						JOptionPane.showMessageDialog(GeckoInstance.getInstance().getGui().getMainframe(), 
								"An error occured while reading the file!",
								"Error", 
								JOptionPane.ERROR_MESSAGE);
					}
				});
				
				handleFailedSessionLoad();
			} 
			catch (ClassNotFoundException e) 
			{
				EventQueue.invokeLater(new Runnable() 
				{
					public void run() 
					{
						JOptionPane.showMessageDialog(GeckoInstance.getInstance().getGui().getMainframe(),
								"The input file is not in the right format!",
								"Wrong format",
								JOptionPane.ERROR_MESSAGE);
					}
				});
				
				handleFailedSessionLoad();
			}
			catch (ClassCastException e){
				EventQueue.invokeLater(new Runnable() 
				{
					public void run() 
					{
						JOptionPane.showMessageDialog(GeckoInstance.getInstance().getGui().getMainframe(),
								"The input file is not in the right format!",
								"Wrong format",
								JOptionPane.ERROR_MESSAGE);
					}
				});
				
				handleFailedSessionLoad();
			}
		}
	}
		
	/**
	 * Method for handling errors while the file is read.
	 */
	private void handleFailedSessionLoad() 
	{
		genomes = null;
		geneLabelMap = null;
		colorMap = null;
		clusters = null;
		maxIdLength = 0;
		GeckoInstance.getInstance().getGui().changeMode(Gui.Mode.SESSION_IDLE);		
	}
}
