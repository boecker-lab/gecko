package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.datastructures.Gene;
import gecko2.util.PrintUtils;

import javax.swing.*;
import java.awt.*;


public class GeneElement extends JPanel implements Adjustable {

	public static final short ORIENTATION_FORWARD = -1;
	public static final short ORIENTATION_BACKWARDS = 1;
	public static final short ORIENTATION_NONE = 0;
	
	public static final Color COLOR_HIGHLIGHT_DEFAULT = new Color(120,120,254);
	public static final Color COLOR_HIGHLIGHT_REFCLUST = Color.RED;
	
	private static final int Y_OFFSET = 2;

	private static final long serialVersionUID = -636870731963818339L;
	
	private Color geneColor;
	private short orientation = ORIENTATION_FORWARD;
	private boolean grey = false;
	private boolean unknown = false;
	private boolean fixedSize = false;
	private final Gene gene;

	private final GeckoInstance gecko;
	
	public GeneElement(Gene g, boolean fixedSize) {
		this.gecko = GeckoInstance.getInstance();
		this.fixedSize = fixedSize;
		this.gene = g;
		this.adjustSize();
		this.setBackground(Color.WHITE);
		updateElement();
	}
	
	private int computeHeight() {
		if (fixedSize)
			return GeckoInstance.DEFAULT_GENE_HIGHT;
		else
			return gecko.getGeneElementHight();
	}
	
	private int computeWidth() {
		if (fixedSize)
			return gecko.getGeneElementWidth();
		else
			return 8 * gecko.getMaxLength(GenomePainting.NameType.ID) + 3;
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
		this.geneColor = gene.getGeneColor();
		// Update Orientation
		if (gene.getOrientation().getSign()<0)
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
		
	@Override 
	protected void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
		
		int ELEMHIGHT = computeHeight();
		int ELEMENTWIDTH = computeWidth();
		Color c;
		
		if (this.unknown)
			c = Color.GRAY;
	 	else  {
	 		if (this.grey) {
	 			int gv = (this.geneColor.getBlue() + this.geneColor.getRed() + this.geneColor.getGreen()) / 3;
	 			c = new Color(gv, gv, gv);
	 		} 
	 		else {
	 			c = this.geneColor;
	 		}
	 	}
		
		g.setColor(c);
		int fontoffset = 0;
		if (this.orientation == ORIENTATION_FORWARD) {
			g.fillRect(3, Y_OFFSET, ELEMENTWIDTH, ELEMHIGHT);
			int xPoints[] = {3 + ELEMENTWIDTH,
					3 + ELEMENTWIDTH + ((int) Math.ceil(ELEMHIGHT / 2.0)),
					3 + ELEMENTWIDTH};
			int yPoints[] = {Y_OFFSET, Y_OFFSET + ELEMHIGHT / 2, Y_OFFSET + ELEMHIGHT};
			g.fillPolygon(xPoints, yPoints, 3);
		} 
		else {
			if (orientation == ORIENTATION_BACKWARDS) {
				int triangleHeight = (int) Math.ceil(ELEMHIGHT / 2.0);
				g.fillRect(3 + triangleHeight, Y_OFFSET, ELEMENTWIDTH, ELEMHIGHT);
				int xPoints[] = {3 + triangleHeight, 3, 3 + triangleHeight};
				int yPoints[] = {Y_OFFSET, (int) Math.ceil(Y_OFFSET + ELEMHIGHT / 2.0), Y_OFFSET + ELEMHIGHT};
				g.fillPolygon(xPoints, yPoints, 3);
				fontoffset = triangleHeight;
			} 
			else {
				g.fillRect(8, Y_OFFSET, ELEMENTWIDTH, ELEMHIGHT);
				fontoffset = 5;
			}
		}
		
		if (!this.unknown) {
			if (geneColor == null) {
				PrintUtils.printDebug("COLOR ERROR");
				geneColor = Color.blue;
			} 
			
			if (geneColor.getRed() + geneColor.getGreen() + geneColor.getBlue() > 450) {
				g.setColor(Color.BLACK);
			} else {
				g.setColor(Color.WHITE);
			}
			g.setFont(new Font("Monospaced", Font.PLAIN, Math.round((float) ELEMHIGHT - ((float) ELEMHIGHT / 2.4F))));
			
			/* Draw only the first index of the mapped String array */
			String geneId = gene.getExternalId();
			int fontY_Position = ELEMHIGHT / 2 + Y_OFFSET + (g.getFont().getSize() / 2);
			
			if (geneId.length() < gecko.getMaxLength(GenomePainting.NameType.ID))
				g.drawString(geneId , ((8 * gecko.getMaxLength(GenomePainting.NameType.ID) + 3) / 2) - ((8 * geneId.length()) / 2) + 5, fontY_Position);
			else
				g.drawString(geneId , 5 + fontoffset, fontY_Position);
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
