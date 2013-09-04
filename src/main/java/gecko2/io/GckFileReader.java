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
import java.util.Map;

import javax.swing.JOptionPane;

/**
 * The class implements a reader for .gck files (session files).
 * The code is exported from GeckoInstance.java and modified.
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.03
 */
public class GckFileReader 
{
	/**
	 * Storing place for the colorMap
	 */
	private Map<Integer, Color> colorMap;
	
	/**
	 * Storing place for the geneLabelMap 
	 */
	private Map<Integer, String[]> geneLabelMap;
	
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
	 * The session type which uses the class default is gui.
	 * 0 if used in a gui session.
	 * 1 if used in a cli session.
	 */
	private int sessionType = 0;
	
	/**
	 * Setter for the session type which accepts only 0 and 1 as input. If other numbers are entered we
	 * set the default value 0 again.
	 * 
	 * @param sType 0 for gui session 1 for cli session
	 */
	public void setSessionType(int sType) {
		
		if (sType == 0 || sType == 1)	
			this.sessionType = sType;
		else 
			this.sessionType = 0;
	}
	
	/**
	 * The method is a getter for the geneLabelMap which contains the relation between external ID
	 * and internal ID from the gene names
	 * 
	 * @return the geneLabelMap (HashMap)
	 */
	public Map<Integer, String[]> getGeneLabelMap() {
		return this.geneLabelMap;
	}
	
	
	/**
	 * Getter for the colorMap.
	 * 
	 * @return the colorMap of the input file
	 */
	public Map<Integer, Color> getColorMap() {
		return colorMap;
	}

	/**
	 * @return the genomes from the input file.
	 */
	public Genome[] getGenomes() {
		return genomes;
	}

	/**
	 * @return the maxIdLength from the input file
	 */
	public int getMaxIdLength() {
		return maxIdLength;
	}

	/**
	 * Reads a gecko session from a file
	 * @param f The file to read from
	 */
	public void loadSessionFromFile(File f)	{
		GeckoInstance.getInstance().setLastOpendFile(f);
		
		if (sessionType == 0) 
			GeckoInstance.getInstance().getGui().changeMode(Gui.Mode.READING_GENOMES);
		
		new SessionLoadingThread(f);
	}
	
	/**
	 * Implementation of the Runnable for the SessionLoadingThread
	 */
	class SessionLoadingThread implements Runnable {
		private final File f;
		
		/**
		 * The constructor sets the file where to  store the data and starts the thread.
		 * 
		 * @param f file object which refers to the file where the data will be stored
		 */
		public SessionLoadingThread(File f)	{
			this.f = f;
			new Thread(this).start();
		}
			
		@SuppressWarnings("unchecked")
		public void run() {
			ObjectInputStream o = null;
			
			try	{
				o = new ObjectInputStream(new FileInputStream(f));
				genomes = (Genome[]) o.readObject();
				geneLabelMap = (HashMap<Integer, String[]>) o.readObject();
				colorMap = (HashMap<Integer, Color>) o.readObject();
				clusters = (GeneCluster[]) o.readObject();
				maxIdLength = (Integer) o.readObject();
				
				if (clusters != null) {
					for (GeneCluster c : clusters) 
						c.setMatch(true);
				}
					
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						/* Give the GeckoInstance the readed data */
						GeckoInstance.getInstance().setClusters(clusters);
						GeckoInstance.getInstance().setGeneLabelMap(geneLabelMap);
						GeckoInstance.getInstance().setColorMap(colorMap);
						GeckoInstance.getInstance().setGenomes(genomes);
						GeckoInstance.getInstance().setMaxIdLength(maxIdLength);

						GeckoInstance.getInstance().fireDataChanged();
						
						// update the gui
						if (sessionType == 0) {
							GeckoInstance.getInstance().getGui().updateViewscreen();
							GeckoInstance.getInstance().getGui().updategcSelector();
							GeckoInstance.getInstance().getGui().changeMode(Gui.Mode.SESSION_IDLE);
						}
					}
				});
			} 
			catch (IOException e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (sessionType == 0) {
							JOptionPane.showMessageDialog(GeckoInstance.getInstance().getGui().getMainframe(), 
								"An error occured while reading the file!",
								"Error", 
								JOptionPane.ERROR_MESSAGE);
						}
						else {
							System.err.println("An error occured while reading the file!");
						}
					}
				});
				
				handleFailedSessionLoad();
			} 
			catch (ClassNotFoundException e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (sessionType == 0) {
							JOptionPane.showMessageDialog(GeckoInstance.getInstance().getGui().getMainframe(),
								"The input file is not in the right format!",
								"Wrong format",
								JOptionPane.ERROR_MESSAGE);
						}
						else {
							System.err.println("The input file is not in the right format!");
						}
					}
				});
				
				handleFailedSessionLoad();
			}
			catch (ClassCastException e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (sessionType == 0) {
							JOptionPane.showMessageDialog(GeckoInstance.getInstance().getGui().getMainframe(),
								"The input file is not in the right format!",
								"Wrong format",
								JOptionPane.ERROR_MESSAGE);
						} 
						else {
							System.err.println("The input file is not in the right format!");
						}
					}
				});
				
				handleFailedSessionLoad();
			}
            finally {
                if (o!=null) {
                    try {
                        o.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
		}
	}
		
	/**
	 * Method for handling errors while the file is read.
	 */
	private void handleFailedSessionLoad() {
		if (sessionType == 1) {
			System.exit(9);
		}
		else {
			genomes = null;
			geneLabelMap = null;
			colorMap = null;
			clusters = null;
			maxIdLength = 0;
			GeckoInstance.getInstance().getGui().changeMode(Gui.Mode.SESSION_IDLE);
		}
	}
}
