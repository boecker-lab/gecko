package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.GeneCluster;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;


public class GeneClusterSelector extends JPanel {

	private static final long serialVersionUID = -4860132931042035952L;
	private GeneClusterSelectorModel model;
	private JTable table;
	
	public static final short COL_ID = 0;
	public static final short COL_NGENES = 1;
	public static final short COL_NGENOMES = 2;
	public static final short COL_SCORE = 3;
	public static final short COL_GENES = 4;
	
	//TODO remove
	public JTable getTable() {
		return table;
	}
	
	public GeneClusterSelector() {
		this.setLayout(new GridLayout(1,1));
		this.setPreferredSize(new Dimension(50,50));
		this.setBackground(Color.WHITE);
		table = new JTable();
		table.setBackground(this.getBackground());
		model = new GeneClusterSelectorModel();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setModel(model);
		TableRowSorter<GeneClusterSelectorModel> sorter = new TableRowSorter<GeneClusterSelectorModel>(model);
		sorter.setSortable(COL_GENES, false);
		table.setRowSorter(sorter);
		final TableColumnModel cm = table.getColumnModel();
		cm.getColumn(0).setPreferredWidth(50); // ID
		cm.getColumn(1).setPreferredWidth(50); // #Genes
		cm.getColumn(2).setPreferredWidth(70); // #Genomes
		cm.getColumn(3).setPreferredWidth(60); // pValue
		cm.getColumn(4).setPreferredWidth(200); // Genes
		for (MouseListener m : table.getMouseListeners()) 
			table.removeMouseListener(m);
		for (MouseMotionListener m : table.getMouseMotionListeners())
			table.removeMouseMotionListener(m);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TableMouseListener m = new TableMouseListener();
		table.addMouseListener(m);
		table.addMouseMotionListener(m);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(this.getBackground());
		this.add(scrollPane);
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					GeckoInstance.getInstance().highLightCluster((Integer) table.getValueAt(table.getSelectedRow(), 0));
					e.consume();
				}
			}
		});
	}
	
	
	
	class TableMouseListener extends MouseAdapter implements MouseMotionListener  {
		
		private JTable t;
		
		public TableMouseListener() {
			t = GeneClusterSelector.this.table;
		}
		
		
		public void mouseDragged(MouseEvent e) {
			mousePressed(e);
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			int row = table.rowAtPoint(e.getPoint());
			if (row!=-1)
				table.setRowSelectionInterval(row, row);
			table.requestFocus();
			
		}
		
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount()==2)
				GeckoInstance.getInstance().highLightCluster((Integer) t.getValueAt(t.getSelectedRow(), 0));
		}

		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub	
		}
		
	}
	
	public void refresh() {
		model.refreshMachingClusters();
		model.fireTableDataChanged();
		TableCellRenderer r = table.getDefaultRenderer(String.class);
		int maxWidth = 0;
		for (int i=0; i<model.getRowCount(); i++) {
			int width = (int) r.getTableCellRendererComponent(table, model.getValueAt(i, 4), false, true, i, 4).getPreferredSize().getWidth();
			if (width>maxWidth) maxWidth=width;
		}
		table.getColumnModel().getColumn(4).setPreferredWidth(maxWidth+5);
	}

	
	class GeneClusterSelectorModel extends AbstractTableModel {

		private static final long serialVersionUID = -8389126835229250539L;
		private final Class<?>[] columns = {Integer.class, Integer.class, Integer.class, String.class, String.class};
		private final String[] columnNames = {"ID","#Genes","#Genomes","Score","Genes"};
		private GeckoInstance instance;
		
		private ArrayList<GeneCluster> matchingClusters;
		
		public void refreshMachingClusters() {
			matchingClusters.clear();
			if (instance.getClusters()!=null)
				for (GeneCluster c : instance.getClusters()) 
					if (c.isMatch()) matchingClusters.add(c);
		}
		
		public ArrayList<GeneCluster> getMatchingClusters() {
			return matchingClusters;
		}
		
		
		public GeneClusterSelectorModel() {
			this.instance = GeckoInstance.getInstance();
			matchingClusters = new ArrayList<GeneCluster>();
		}
		
		public Class<?> getColumnClass(int columnIndex) {
			return this.columns[columnIndex];
		}

		public int getColumnCount() {
			return this.columns.length;
		}

		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}

		public int getRowCount() {
			return matchingClusters.size();
		}


		public Object getValueAt(int rowIndex, int columnIndex) {
			switch(columnIndex) {
			case 0:
				return matchingClusters.get(rowIndex).getId();
			case 1:
				return matchingClusters.get(rowIndex).getGenes().length;
			case 2:
				return matchingClusters.get(rowIndex).getSize();
			case 3:
				DecimalFormat twoPlaces = new DecimalFormat("#.##");
				return twoPlaces.format(-Math.log(matchingClusters.get(rowIndex).getpValue()));
			case 4:
				if (instance.getGenLabelMap()!=null) {
					int[] genes = matchingClusters.get(rowIndex).getGenes();
					ArrayList<Integer> knownGenes = new ArrayList<Integer>();
					for (int g : genes) if (instance.getGenLabelMap()[g]!=0) knownGenes.add(instance.getGenLabelMap()[g]);
					return Arrays.toString(knownGenes.toArray(new Integer[knownGenes.size()]));
				} else return "";

			default:
				return null;
			}
		}

		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// Read-only table
			return false;
		}
		
	}
	
}
