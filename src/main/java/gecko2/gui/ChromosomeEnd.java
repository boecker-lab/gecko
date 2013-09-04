package gecko2.gui;

import gecko2.GeckoInstance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;


public class ChromosomeEnd extends JPanel implements Adjustable {

	private static final long serialVersionUID = 3167399537083845840L;

	public static final short LEFT = 1;
	public static final short RIGHT = 2;
	private final short orientation;
	
	private static final GeckoInstance gecko = GeckoInstance.getInstance();
	
	public ChromosomeEnd(Color c, short orientation) {
		this.setBackground(c);
		this.adjustSize();
		this.orientation = orientation;
	}

	
	public void adjustSize() {
		this.setPreferredSize(computeDimension());
		this.setSize(this.getPreferredSize());
	}
	
	public static Dimension computeDimension() {
		int w = 4+gecko.getGeneElementHight();
		return new Dimension(w,w);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.fillRect(2+gecko.getGeneElementHight()/2, 2, 2, gecko.getGeneElementHight());
		if (this.orientation==LEFT) 
			g.fillRect(gecko.getGeneElementHight()/2+4, gecko.getGeneElementHight()/2+1, gecko.getGeneElementHight()/2-2, 2);
		else
			g.fillRect(4, gecko.getGeneElementHight()/2+1, gecko.getGeneElementHight()/2-2, 2);
	}

}
