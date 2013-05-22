package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.Gene;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.Genome;
import gecko2.algorithm.Subsequence;
import gecko2.event.ClusterSelectionEvent;
import gecko2.event.ClusterSelectionListener;
import gecko2.event.LocationSelectionEvent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class GeneClusterDisplay extends JScrollPane implements ClusterSelectionListener {

	private static final long serialVersionUID = -2156280340296694286L;
	private JPanel masterPanel;
	private JPanel flowpanel;
	private HashMap<Integer, Gene[]> annotations;
	private GeneCluster cluster;
	private GeneClusterOccurrence gOcc;
	private int[] subselections;
	
	private static final String VALUES_TITLE = "Global cluster information:";

	private static final String GENES_TITLE = "Genes in this Cluster:";
	
	public GeneClusterDisplay() {
		flowpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		masterPanel = new JPanel();		
		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.PAGE_AXIS));
		masterPanel.setBackground(Color.WHITE);
		flowpanel.setBackground(masterPanel.getBackground());
		flowpanel.add(masterPanel);
		this.setViewportView(flowpanel);
		update();
	}

	@Override
	public void selectionChanged(ClusterSelectionEvent e) {
		if (! (e instanceof LocationSelectionEvent))
			return;
		LocationSelectionEvent l = (LocationSelectionEvent) e;
		this.cluster = l.getSelection();
		if (this.cluster!=null) {
			this.annotations = GeckoInstance.getInstance().generateAnnotations(l.getSelection(), 
					l.getgOcc(), 
					l.getsubselection());
			this.subselections = l.getsubselection();
			this.gOcc = l.getgOcc();
		} else {
			this.annotations = null;
			this.subselections = null;
		}
		update();
	}
	
	
	private void update() {
		masterPanel.removeAll();
		if (annotations!=null) {

			/*
			 * Get the mouselisteners
			 */
			GeckoInstance instance = GeckoInstance.getInstance();
			ArrayList<MouseListener> mlisteners = new ArrayList<MouseListener>();

			for (int i=0;i<gOcc.getSubsequences().length;i++) {
				if (subselections[i]==GeneClusterOccurrence.GENOME_NOT_INCLUDED) {
					mlisteners.add(i,null);
					continue;
				}
				Subsequence s = gOcc.getSubsequences()[i][subselections[i]];
				if (s.isValid())
					mlisteners.add(i, instance.getGenomes()[i].getChromosomes().get(s.getChromosome()).getChromosomeMouseListener());
				else
					mlisteners.add(i, null);
			}
				
			
			Font monoFont = new Font("Monospaced",Font.PLAIN,new JLabel().getFont().getSize()-1);
			Font boldFont = new JLabel().getFont().deriveFont(Font.BOLD);

			/*
			 * Involed Chromosomes
			 */
			{
		
			JPanel involvedChrTitle = new JPanel();
			involvedChrTitle.setBackground(masterPanel.getBackground());
			involvedChrTitle.setLayout(new BoxLayout(involvedChrTitle,BoxLayout.X_AXIS));
			JLabel invChrTitle = new JLabel("Involved chromosomes");
			invChrTitle.setFont(boldFont);
			involvedChrTitle.add(invChrTitle);
			involvedChrTitle.add(Box.createHorizontalGlue());
			masterPanel.add(involvedChrTitle);
			masterPanel.add(Box.createVerticalStrut(5));
			
			for (int i = 0; i < gOcc.getSubsequences().length; i++) {
				
				if (subselections[i] == GeneClusterOccurrence.GENOME_NOT_INCLUDED)
					continue;
				
				Subsequence s = gOcc.getSubsequences()[i][subselections[i]];
				
				if (!s.isValid()) 
					continue;
				
				JPanel cpanel = new JPanel();
				FlowLayout f = new FlowLayout(FlowLayout.LEFT);
				f.setVgap(1);
				cpanel.setLayout(f);
				cpanel.setBackground(masterPanel.getBackground());
				
				Chromosome c = GeckoInstance.getInstance().getGenomes()[i].getChromosomes().get(s.getChromosome());
				Genome g = GeckoInstance.getInstance().getGenomes()[i];
				cpanel.add(new NumberInRectangle(i + 1, getBackground(), mlisteners.get(i), Integer.toString(i + 1).length()));
				
				cpanel.add(new TextLabel(c.getFullName()));
				
				masterPanel.add(cpanel);
			}
			masterPanel.add(Box.createVerticalStrut(5));
			}

			
			/*
			 * Global data
			 */
			
			
			JPanel title1 = new JPanel();
			title1.setBackground(masterPanel.getBackground());
			title1.setLayout(new BoxLayout(title1,BoxLayout.X_AXIS));
			JLabel valTitle = new JLabel(VALUES_TITLE);
			valTitle.setFont(boldFont);
			title1.add(valTitle);
			title1.add(Box.createHorizontalGlue());
			masterPanel.add(title1);
			
			String[] titles = {"Best total distance: ", 
					           "         Best score: "};
			Object[] values = { gOcc.getTotalDist(), 
								gOcc.getBestScore() };
			masterPanel.add(Box.createVerticalStrut(5));
			for (int i=0; i<titles.length; i++) {
				JPanel cpanel = new JPanel();
				FlowLayout f = new FlowLayout(FlowLayout.LEFT);
				f.setVgap(1);
				cpanel.setLayout(f);
				cpanel.setBackground(masterPanel.getBackground());
				TextLabel textLabel = new TextLabel(titles[i]+values[i]);
				textLabel.setFont(monoFont);
				cpanel.add(textLabel);
				masterPanel.add(cpanel);
			}
			masterPanel.add(Box.createVerticalStrut(5));

			/*
			 * Local distances to median/center
			 */
			
			JPanel title3 = new JPanel();
			title3.setBackground(masterPanel.getBackground());
			title3.setLayout(new BoxLayout(title3,BoxLayout.X_AXIS));

			{
				JLabel label = new JLabel();
				if (cluster.getType() == 'm')
					label.setText("Distance to median per dataset:");
				else if (cluster.getType()=='c')
					label.setText("Distance to center set per dataset:");
				else
					label.setText("Distance to reference gene set per dataset:");
				label.setFont(boldFont);
				title3.add(label);
			}
			title3.add(Box.createHorizontalGlue());
			masterPanel.add(title3);
			
			masterPanel.add(Box.createVerticalStrut(5));
			JPanel cpanel = new JPanel();
			FlowLayout f = new FlowLayout(FlowLayout.LEFT);
			f.setVgap(1);
			cpanel.setLayout(f);
			cpanel.setBackground(masterPanel.getBackground());

			for (int i=0; i<gOcc.getSubsequences().length; i++) {
				if (subselections[i]==GeneClusterOccurrence.GENOME_NOT_INCLUDED) 
					continue;
				Subsequence s = gOcc.getSubsequences()[i][subselections[i]];
				if (s.getStart()>s.getStop()) 
					continue;
				cpanel.add(new NumberInRectangle(i+1, getBackground(), mlisteners.get(i), Integer.toString(i+1).length()));
				TextLabel textLabel = new TextLabel(Integer.toString(s.getDist()));
				textLabel.setFont(monoFont);
				cpanel.add(textLabel);
				cpanel.add(Box.createHorizontalStrut(5));
			}
			masterPanel.add(cpanel);
			masterPanel.add(Box.createVerticalStrut(5));
			
			/*
			 * List of genes
			 */
			
			JPanel title2 = new JPanel();
			title2.setBackground(masterPanel.getBackground());
			title2.setLayout(new BoxLayout(title2,BoxLayout.X_AXIS));

			{
				TextLabel label = new TextLabel(GENES_TITLE);
				label.setFont(boldFont);
				title2.add(label);
			}
			
			title2.add(Box.createHorizontalGlue());
			
			masterPanel.add(title2);
			masterPanel.add(Box.createVerticalStrut(10));
			for (int genid : annotations.keySet()) {
				GeneElement e = new GeneElement(new Gene(null, genid), false);
				Gene[] genes = annotations.get(genid);
				e.setToolTipText(null);
				e.setOrientation(GeneElement.ORIENTATION_NONE);
				JPanel p = new JPanel();
				p.setBackground(masterPanel.getBackground());
				p.setLayout(new FlowLayout(FlowLayout.LEFT));
				((FlowLayout)p.getLayout()).setVgap(0);
				((FlowLayout)p.getLayout()).setHgap(0);
				p.add(e);
				JPanel refPanel = p;
				for (int i=0;i<genes.length;i++) {
					if (genes[i]!=null) {
						boolean first = false;
						if (refPanel == null) { // if we are not using the first panel (shared with GeneElement)
							first = true;
							refPanel = new JPanel();
							refPanel.setBackground(masterPanel.getBackground());
							refPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
							((FlowLayout)refPanel.getLayout()).setVgap(0);
							((FlowLayout)refPanel.getLayout()).setHgap(0);
							refPanel.add(Box.createRigidArea(new Dimension((int)e.getPreferredSize().getWidth(), 0)));
						}
						TextLabel l = new TextLabel(genes[i].getSummary());
						l.setFont(monoFont);
						NumberInRectangle n = new NumberInRectangle(i+1,refPanel.getBackground(), mlisteners.get(i), Integer.toString(i+1).length());
						refPanel.add(n);
						refPanel.add(Box.createRigidArea(new Dimension(4, 0)));
						refPanel.add(l);
						masterPanel.add(refPanel);
						if (i<genes.length && first) masterPanel.add(Box.createRigidArea(new Dimension(0,4)));
						refPanel = null;
					}
				}
				if (!masterPanel.isAncestorOf(p))
					masterPanel.add(p);
			}
			masterPanel.add(Box.createHorizontalGlue());
			masterPanel.repaint();
			flowpanel.repaint();
		
			
		}
		this.repaint();
		this.revalidate();
		this.getVerticalScrollBar().setValue(0);
	}
	
	private class TextLabel extends JLabel {	
		/**
		 * Random generated serialization UID
		 */
		private static final long serialVersionUID = -741356610359230027L;

		public TextLabel(String s) {
			super(s);
			setBorder(null);
			setBackground(Color.WHITE);
		}
		
	}



}
