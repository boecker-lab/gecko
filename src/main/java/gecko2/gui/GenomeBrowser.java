package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Gene;
import gecko2.algorithm.Genome;
import gecko2.event.BrowserContentEvent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;



public class GenomeBrowser extends JScrollPane implements Adjustable {
	
	private final static Color BG_COLOR_FLIPPED = Color.ORANGE;
	
	private FlowLayout flowlayout;
	
	private static final long serialVersionUID = 7086043901343368118L;
	private Genome genome;
	private ArrayList<GeneElement> genElements[];
	private JPanel contentPanel;
	private GenomeBrowserMouseDrag genomebrowsermousedrag;
	private HashMap<GeneElement, Integer[]> backmap;
	private 
	GeckoInstance gecko;
	
	private boolean flipped = false;
	
	private JPanel leftspace, rightspace;
	
	private boolean init = true;
	
	public boolean isFlipped() {
		return flipped;
	}

	
	/**
	 * Computes the first index within the GeneElement list that belongs to
	 * a particular chromosome
	 * @param chromosome The chromosome whose first element is looked for
	 * @return The index of the first GeneElement
	 */
	public int getFirstElementId(int chromosome) {
		if (chromosome>=genome.getChromosomes().size())
			throw new IndexOutOfBoundsException("Invalid chromosome index");
		int index = 0;
		for (int i=0;i<chromosome;i++)
			index += genome.getChromosomes().get(i).getGenes().size();
		return index;
	}
	

	/**
	 * Call the adjustSize() method for all GeneElements handled by this
	 * GenomeBrowser
	 */
	private void adjustAllSizes() {
		for (Component c : contentPanel.getComponents())
			if (c instanceof Adjustable)
				((Adjustable) c).adjustSize();
		this.revalidate();
		this.repaint();
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
	}

	
	public int getNumberOfElements(int chromosome) {
		return genElements[chromosome].size();
	}
	
	public void setRangeToGrey(int chromosome, int start, int stop, boolean grey) {
		if (stop==-1) stop = genElements[chromosome].size()-1;
		if (genElements[chromosome].size()!=0)
			for (int i=start;i<=stop;i++)
				this.genElements[chromosome].get(i).setGrey(grey);
		this.repaint();
	}
	
	/**
	 * Highlights a range within a chromosome
	 * @param chromosome The id of the chromosome
	 * @param left The first gene to highlight
	 * @param right The last gene to highlight
	 * @param highlightColor the color to use for the highlight
	 */
	public void markClusterBorder(int chromosome, int left, int right, Color highlightColor) {
		for (int i=0;i<genElements.length;i++)
			this.setHighlightRange(i, 0, genElements[i].size()-1, null);
		this.setHighlightRange(chromosome, left, right, highlightColor);
	}
	
	/**
	 * Highlights or unhighlights a range within a chromosome
	 * @param chromosome The id of the chromosome
	 * @param start The first gene to be affected
	 * @param stop The last gene to be affected
	 * @param highlight {@code true} for highlight {@code false} for unhighlight
	 */
	public void setHighlightRange(int chromosome, int start, int stop, Color highlight) {
		if (stop==-1) stop = genElements[chromosome].size()-1;
		if (genElements[chromosome].size()!=0)
			for (int i=start; i<=stop; i++)
				genElements[chromosome].get(i).setHighlighted(highlight);
	}
	
	public int getLeftSpacerWidth() {
		return leftspace.getWidth();
	}
	
	private void arrangeGeneElements(boolean flipped) {
		boolean changeOrientation = false;
		if (this.flipped!=flipped) {
			getHorizontalScrollBar().setValue(getHorizontalScrollBar().getMaximum()-getHorizontalScrollBar().getValue()-leftspace.getWidth()+2);
			// TODO Understand dirty +2 hack - probably the border width
			changeOrientation = true;
		}
		this.flipped = flipped;
		contentPanel.removeAll();
		contentPanel.add(leftspace);
		if (flipped) {
			contentPanel.setBackground(BG_COLOR_FLIPPED);
			for (int j=genElements.length-1;j>=0;j--) {
				contentPanel.add(new ChromosomeEnd(contentPanel.getBackground(), ChromosomeEnd.LEFT));
				for (int i=genElements[j].size()-1; i>=0; i--) {
					genElements[j].get(i).setBackground(contentPanel.getBackground());
					contentPanel.add(genElements[j].get(i));
					if (changeOrientation) genElements[j].get(i).flipOrientation();
				}
				contentPanel.add(new ChromosomeEnd(contentPanel.getBackground(), ChromosomeEnd.RIGHT));

			}
		} else  {
			contentPanel.setBackground(Color.WHITE);
			for (ArrayList<GeneElement> chromo : genElements) {
				contentPanel.add(new ChromosomeEnd(contentPanel.getBackground(), ChromosomeEnd.LEFT));
				for (GeneElement e : chromo) {
					contentPanel.add(e);
					e.setBackground(contentPanel.getBackground());
					if (changeOrientation) e.flipOrientation();
				}
				contentPanel.add(new ChromosomeEnd(contentPanel.getBackground(), ChromosomeEnd.RIGHT));
			}
		}
		contentPanel.add(rightspace);
		contentPanel.revalidate();
		this.gecko.getGui().getMgb().fireBrowserContentChanged(BrowserContentEvent.SCROLL_VALUE_CHANGED);
	}
	
	private void arrangeGeneElements() {
		arrangeGeneElements(false);
	}
	
	@SuppressWarnings("unchecked")
	private void createGeneElements() {
		int id = 0;
		genElements = new ArrayList[genome.getChromosomes().size()];
		for (int i=0;i<genome.getChromosomes().size();i++) {
			MouseListener ml = genome.getChromosomes().get(i).getChromosomeMouseListener();
			genElements[i] = new ArrayList<GeneElement>();
			for (Gene g : this.genome.getChromosomes().get(i).getGenes()) {
				GeneElement element = new GeneElement(g);
				if (g.getId()<0) element.setOrientation(GeneElement.ORIENTATION_BACKWARDS);
				element.addMouseMotionListener(genomebrowsermousedrag);
				element.addMouseListener(genomebrowsermousedrag);
				element.addMouseListener(ml);
				if (g.isUnknown()) element.setUnknown(true);
				this.genElements[i].add(element);
				Integer[] back = {i,id++};
				backmap.put(element,back);
			}
		}
		arrangeGeneElements();
 	}
	
	public void adjustSize() {
		this.adjustAllSizes();
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE,getGBHeight()));
		this.setMinimumSize(new Dimension(20,getGBHeight()));
		this.setPreferredSize(new Dimension(20,getGBHeight()));
//		this.setSize(this.getPreferredSize());
		this.revalidate();
		this.repaint();
	}
	
	public GenomeBrowser(Genome g) {
		gecko = GeckoInstance.getInstance();
		backmap = new HashMap<GeneElement, Integer[]>();
		genomebrowsermousedrag = new GenomeBrowserMouseDrag();
		this.setBorder(null);
		this.contentPanel = new JPanel();
		this.flowlayout = new FlowLayout(FlowLayout.LEFT);
		this.flowlayout.setHgap(0);
		this.flowlayout.setVgap(0);
		this.contentPanel.setLayout(this.flowlayout);
		this.genome = g;
		this.setBackground(Color.WHITE);
		this.contentPanel.setBackground(Color.WHITE);
		this.adjustSize();
		leftspace = new JPanel();
		rightspace = new JPanel();
		this.setViewportBorder(null);
		this.setViewportView(this.contentPanel);
		this.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
		this.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
		this.addComponentListener(new GenomeBrowserListener());
		createGeneElements();
	}
	
	
	private class GenomeBrowserListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			super.componentResized(e);
			int brwidth = (int)GenomeBrowser.this.getSize().getWidth();
			leftspace.setPreferredSize(new Dimension(brwidth,0));
			rightspace.setPreferredSize(new Dimension(brwidth,0));
			leftspace.revalidate();
			rightspace.revalidate();
			if (init) {
				getHorizontalScrollBar().setValue(brwidth);
				init = false;
			}
		}
	}
	
	public int getGenWidth() {
		if (this.genElements.length==0 || this.genElements[0].size()==0)
			return 0;
		return this.genElements[0].get(0).getWidth()+this.flowlayout.getHgap();
	}
	
	public Genome getGenome() {
		return genome;
	}
	
	public int getGBHeight() {
		return 4+gecko.getGeneElementHight()+this.flowlayout.getVgap();
	}
	
	public GeneElement getGeneElementAt(int chromosome, int position) {
		return genElements[chromosome].get(position);
	}
	
	class GenomeBrowserMouseDrag extends MouseAdapter implements MouseMotionListener {
		
		private int clickXPos;
		
		@Override
		public void mousePressed(MouseEvent e) {
			this.clickXPos = e.getX();
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount()%2==0 && e.getSource() instanceof GeneElement) {
				if (!e.isShiftDown()) {
					GeneElement el = (GeneElement) e.getSource();
					if (el.isHighlighted()) {
						gecko.getGui().getMgb().centerCurrentClusterAt(Math.abs(el.getGene().getId()));
					}
				} else
					arrangeGeneElements(!flipped);
				}
		}
		
		public void mouseDragged(MouseEvent e) {
			int diff = e.getX()-this.clickXPos;
			JScrollBar bar = GenomeBrowser.this.getHorizontalScrollBar();
			bar.setValue(bar.getValue()-diff);
		}
		
		public void mouseMoved(MouseEvent e) {}

	}
	
	public void flip() {
		arrangeGeneElements(!flipped);
	}
	
	public GenomeBrowserMouseDrag getGenomebrowsermousedrag() {
		return genomebrowsermousedrag;
	}

	
}

