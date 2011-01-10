package gecko2.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

public class NumberInRectangle extends JPanel {

	private static final long serialVersionUID = -5779445240934635898L;
	private int number;
	
	public NumberInRectangle(int number, Color bg, MouseListener ml) {
		this.setBackground(bg);
		this.number = number;
		this.setPreferredSize(new Dimension(17,17));
		this.setMaximumSize(new Dimension(17,17));
		if (ml!=null)
			this.addMouseListener(ml);
	}
	
	public NumberInRectangle(int number, Color bg) {
		this(number, bg, null);
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.BLUE);
		g.drawRect(0, 0, 16, 16);
		g.setColor(Color.black);
		g.setFont(g.getFont().deriveFont(10.5F));
		if (number<10)
			g.drawString(Integer.toString(number), 5, 12);
		else if (number<100)
			g.drawString(Integer.toString(number), 1, 12);
		else
			g.drawString("XX", 1, 12);
	}
	
}