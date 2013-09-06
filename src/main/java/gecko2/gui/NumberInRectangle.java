package gecko2.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;

public class NumberInRectangle extends JPanel {

	private static final long serialVersionUID = -5779445240934635898L;
	private final int number;
	
	/**
	 * Here we save the number of genomes to calculate the size and place of the rectangles.
	 */
	private final int dim;
	
	public NumberInRectangle(int number, Color bg, MouseListener ml, int dim) {
		this.setBackground(bg);
		this.number = number;
		this.dim = dim;
		
		if (this.dim == 1)
		{
			this.setPreferredSize(new Dimension(17,17));
		}
		else
		{
			this.setPreferredSize(new Dimension(8 + (9 * (this.dim - 1)),17));
		}
		
		this.setMaximumSize(new Dimension(8 + (9 * (this.dim - 1)),17));
		if (ml!=null)
			this.addMouseListener(ml);
	}
	
	public NumberInRectangle(int number, Color bg, int dim) {
		this(number, bg, null, dim);
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.BLUE);
		
		if (this.dim == 1)
		{
			g.drawRect(0, 0, 16, 16);
		}
		else
		{
			g.drawRect(0, 0, 8 + (9 * (this.dim - 1)) - 1, 16);
		}
		
		g.setColor(Color.black);
		g.setFont(g.getFont().deriveFont(10.5F));
		if (number<10)
		{
			g.drawString(Integer.toString(number), 5, 12);
		}
		else
		{
			g.drawString(Integer.toString(number), 1, 12);
		}
	}
	
}