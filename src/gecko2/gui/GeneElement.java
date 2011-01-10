package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Gene;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;


public class GeneElement extends JPanel implements Adjustable {

	public static final short ORIENTATION_FORWARD = -1;
	public static final short ORIENTATION_BACKWARDS = 1;
	public static final short ORIENTATION_NONE = 0;
	
	public static final short BORDER_LEFT = 1;
	public static final short BORDER_NO = 2;
	public static final short BORDER_RIGHT = 3;
	
	public static final Color COLOR_HIGHLIGHT_DEFAULT = new Color(120,120,254);
	public static final Color COLOR_HIGHLIGHT_REFCLUST = Color.RED;
	
	private static final int Y_OFFSET = 2;

	private static final long serialVersionUID = -636870731963818339L;
	
	private Color geneColor;
	private short orientation = ORIENTATION_FORWARD;
	private boolean grey = false;
	private boolean unknown = false;
	private boolean fixedSize = false;
	private Gene gene;
	
	private Color highlightColor;
	
	private final GeckoInstance gecko = GeckoInstance.getInstance();

	
	public boolean isUnknown() {
		return unknown;
	}
	
	public void setUnknown(boolean unknown) {
		this.unknown = unknown;
	}
	
	public GeneElement(Gene g) {
		this(g, false);
	}
	
	public GeneElement(Gene g, boolean fixedSize) {
		this.fixedSize = fixedSize;
		this.adjustSize();
		this.gene = g;
		this.setBackground(Color.WHITE);
		updateElement();
	}
	
	private int computeHeight() {
		if (fixedSize)
			return GeckoInstance.DISPLAY_GENEELEMENT_HIGHT;
		else
			return gecko.getGeneElementHight();
	}
	
	private int computeWidth() {
		if (fixedSize)
			return (int) Math.round(GeckoInstance.DISPLAY_GENEELEMENT_HIGHT*1.75);
		else
			return gecko.getGeneElementWidth();
	}

	/**
	 * Adjusts the preferred dimension of this GeneElement according to the element
	 * dimensions currently set
	 */
	public void adjustSize() {
		this.setPreferredSize(new Dimension(6 // 2 x borders a 3 px
				+ (computeWidth()+((int) Math.ceil(computeHeight()/2.0))) // the triangle height
				,computeHeight()+4));
		this.setSize(this.getPreferredSize());
	}
	
	private void updateElement() {
		this.geneColor = gecko.getColormap().get(Math.abs(this.gene.getId()));
		// Update Orientation
		if (gene.getId()<0)
			this.orientation = ORIENTATION_BACKWARDS;
		else
			this.orientation = ORIENTATION_FORWARD;
		// Update the tooltip text
		if (gene.getName()!=null) {
			if (gene.getAnnotation()!=null)
				this.setToolTipText(gene.getName()+" - "+gene.getAnnotation());
			else
				this.setToolTipText(gene.getName());
		} else
			if (gene.getAnnotation()!=null)
				this.setToolTipText("---- -"+gene.getAnnotation());
			else
				this.setToolTipText("----");
	}
	
	public void setHighlighted(Color highlightColor) {
		this.highlightColor = highlightColor;
		this.repaint();
	}
	
	public boolean isHighlighted() {
		if (this.highlightColor!=null)
			return true;
		return false;
	}
		
	public void setGrey(boolean grey) {
		this.grey = grey;
		this.repaint();
	}
	

		
	@Override 
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int ELEMHIGHT = computeHeight();
		int ELEMENTWIDTH = computeWidth();
		Color c;
		if (this.highlightColor != null) {
//			c = new Color(120,120,254);
			g.setColor(this.highlightColor);
			g.fillRect(0, 0, (int) getPreferredSize().getWidth(), getPreferredHeight());
		}
		if (this.unknown)
			c = Color.GRAY;
	 	else if (this.grey) {
			int gv = (int) Math.floor((this.geneColor.getBlue()+this.geneColor.getRed()+this.geneColor.getGreen())/3);
			c = new Color(gv,gv,gv);
	 	} else
			c = this.geneColor;
		g.setColor(c);
		int fontoffset = 0;
		if (this.orientation == ORIENTATION_FORWARD) {
			g.fillRect(3, Y_OFFSET+0, ELEMENTWIDTH, ELEMHIGHT);
			int xPoints[] = {3+ELEMENTWIDTH,
					3+ELEMENTWIDTH+((int) Math.ceil(ELEMHIGHT/2.0)),
					3+ELEMENTWIDTH};
			int yPoints[] = {Y_OFFSET+0,Y_OFFSET+ELEMHIGHT/2,Y_OFFSET+ELEMHIGHT};
			g.fillPolygon(xPoints,yPoints, 3);
		} else if (orientation == ORIENTATION_BACKWARDS) {
			int triangleHeight = (int) Math.ceil(ELEMHIGHT/2.0);
			g.fillRect(3+triangleHeight, Y_OFFSET+0, ELEMENTWIDTH, ELEMHIGHT);
			int xPoints[] = {3+triangleHeight,3,3+triangleHeight};
			int yPoints[] = {Y_OFFSET+0,(int) Math.ceil(Y_OFFSET+ELEMHIGHT/2.0),Y_OFFSET+ELEMHIGHT};
			g.fillPolygon(xPoints,yPoints, 3);
			fontoffset = triangleHeight;
		} else {
			g.fillRect(8, Y_OFFSET+0, ELEMENTWIDTH, ELEMHIGHT);
			fontoffset = 5;
		}
		if (!this.unknown) {
			if (geneColor == null) {
				System.err.println("Color?!");
				geneColor = Color.blue;
			} 
			if (geneColor.getRed() + geneColor.getGreen() + geneColor.getBlue() > 450) {
				g.setColor(Color.BLACK);
			} else {
				g.setColor(Color.WHITE);
			}
			g.setFont(g.getFont().deriveFont((float) ELEMHIGHT-((float) ELEMHIGHT/2.4F)));
			g.drawString(Integer.toString(gecko.getGenLabelMap()[Math.abs(gene.getId())]), 5+fontoffset, ELEMHIGHT/2+Y_OFFSET+(int) Math.round(g.getFont().getSize()/2));
		}
	}
	
	public void setOrientation(short orientation) {
		this.orientation = orientation;
	}
	
	public short getOrientation() {
		return orientation;
	}
	
	public int getPreferredHeight() {
		return (int) getPreferredSize().getHeight();
	}
	
	public Gene getGene() {
		return gene;
	}

	public void flipOrientation() {
		orientation = (short) ((-1) * orientation);
		
	}
}
