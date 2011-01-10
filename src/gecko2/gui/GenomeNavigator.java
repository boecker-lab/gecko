package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.GeckoInstance.DataEvent;
import gecko2.GeckoInstance.DataListener;
import gecko2.gui.MultipleGenomesBrowser.BrowserContentEvent;
import gecko2.gui.MultipleGenomesBrowser.BrowserContentListener;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

public class GenomeNavigator extends JPanel implements DataListener,BrowserContentListener {
	
	private static final long serialVersionUID = -3454613481139655426L;
	private MultipleGenomesBrowser mgb;
	private int virtualWidth=0;
	private int barWidth = 0;
	private float windowCoverage;
	private int maxBarMax;
	public static final int PADDING = 5;
	public static final int GENOME_DIST = 10;
	public static final int GENOME_LINE_WIDTH = 6;
	
	public GenomeNavigator() {
		this.addComponentListener(this.componentAdapter);
		this.mgb = GeckoInstance.getInstance().getGui().getMgb();
	}
	
	@Override
	public void paint(Graphics g1) {
		super.paint(g1);
//		updateVirtualWidth();
		if (mgb==null) return;
		
		Graphics2D g = (Graphics2D) g1;
		
		float scale = this.getWidth()/(virtualWidth*1.0F);
		
		int y = PADDING;
		
		for (int i=0; i<mgb.getGenomeBrowsers().size();i++) {
			GenomeBrowser gb = mgb.getGenomeBrowsers().get(i);
			JScrollBar bar = gb.getHorizontalScrollBar();
			
			int lineLength = bar.getMaximum() - (2*(gb.getWidth()+2));
			// Scale the linelength to our window width
			lineLength = (int) Math.floor(lineLength*scale);
//			System.err.println(i+": BARMAX="+bar.getMaximum()+" GBWIDTH="+gb.getWidth()+" VIRTUALWIDTH="+virtualWidth+" LINELENGTH="+lineLength);
			
			int lineStart = maxBarMax-barWidth-2; // rightmost position
			float substract = bar.getValue();// * (bar.getMaximum()/(1.0F*maxBarMax)); // substract value (scaled to longest bar)
			lineStart-= (int)(Math.floor(substract));	
			lineStart = (int) Math.floor(lineStart*scale);
			
			lineStart+=2;
			lineLength-=4;
			
			Shape t = new RoundRectangle2D.Float(lineStart,
					y,
					lineLength,
					GENOME_LINE_WIDTH,
					4,
					4);
			
//			g.drawLine(lineStart,
//					y,
//					lineStart+lineLength,
//					y);
			g.setStroke(new BasicStroke(1));
			if (gb.isFlipped())
				g.setColor(new Color(1F,1F,0.75F));
			else
				g.setColor(new Color(0.7F,0.7F,1.0F));
			g.fill(t);
			g.setColor(Color.BLACK);
			g.draw(t);
			y+= GENOME_DIST;

		}
		// Draw the window frame
		Shape s = new RoundRectangle2D.Float((int) Math.floor(this.getWidth()/2.0F-(0.5*windowCoverage*this.getWidth())), 
				1, 
				(int) Math.floor(windowCoverage*this.getWidth()), 
				this.getHeight()-2, 
				2, 
				2);
		g.setStroke(new BasicStroke(1));
		g.setColor(new Color(0.7F,0.7F,0.7F,0.5F));
		g.fill(s);
		g.setColor(Color.BLACK);
//		g.draw(s);

	}

	
	public void refresh() {
		Runnable toRun = new Runnable() {
			
			@Override
			public void run() {
				GenomeNavigator.this.repaint();
				
			}
		};
		if (SwingUtilities.isEventDispatchThread())
			toRun.run();
		else
			SwingUtilities.invokeLater(toRun);
	}
	
	private void updateVirtualWidth() {
		maxBarMax=0;
		for (GenomeBrowser gb : mgb.getGenomeBrowsers()) {
			JScrollBar bar = gb.getHorizontalScrollBar();
			barWidth = gb.getWidth();
			
			if (bar.getMaximum()>maxBarMax)
				maxBarMax = bar.getMaximum();
		}
		int maxGenomeWidth = maxBarMax - (2*(barWidth+2)); 
		virtualWidth = 2*maxGenomeWidth + barWidth;
		windowCoverage = barWidth/(1.0F*virtualWidth);
	}
	
	private ComponentAdapter componentAdapter = new ComponentAdapter() {
		public void componentResized(java.awt.event.ComponentEvent e) {
			updateVirtualWidth();
			refresh();
		};
	};

	@Override
	public void dataChanged(DataEvent e) {
		if (mgb.getGenomeBrowsers().size()==0) {
			this.setPreferredSize(new Dimension(0,0));
		} else {
			this.setPreferredSize(new Dimension(0,mgb.getGenomeBrowsers().size()*(GENOME_DIST)+2*PADDING));
			updateVirtualWidth();
		}
		this.getParent().doLayout();
		this.refresh();
	}

	@Override
	public void browserContentChanged(BrowserContentEvent e) {
		if (e.getEventType()==BrowserContentEvent.ZOOM_FACTOR_CHANGED) {
			updateVirtualWidth();
		}
		this.refresh();		
	}
	
}
