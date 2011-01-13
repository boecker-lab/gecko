package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.event.ClusterSelectionEvent;
import gecko2.event.ClusterSelectionListener;
import gecko2.event.LocationSelectionEvent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;


public class GeneClusterSelector extends JPanel {

	private static final long serialVersionUID = -4860132931042035952L;
	private GeneClusterSelectorModel model;
	private JCheckBox checkBox;
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
		this.setLayout(new BorderLayout());
		checkBox = new JCheckBox("show suboptimal hits");
		checkBox.setVisible(false);
		checkBox.addActionListener(actionListener);
		this.add(checkBox, BorderLayout.PAGE_END);
		this.setPreferredSize(new Dimension(50,200));
		table = new JTable();
		table.setBackground(Color.WHITE);
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
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(Color.WHITE);
		this.add(scrollPane, BorderLayout.CENTER);
		table.getSelectionModel().addListSelectionListener(listSelectionListener);
		for (MouseListener l : table.getMouseListeners())
			table.removeMouseListener(l);
		for (MouseMotionListener l : table.getMouseMotionListeners())
			table.removeMouseMotionListener(l);
		table.addMouseListener(mouseListener);
		table.addKeyListener(keyListener);
	}
	
	private ListSelectionListener listSelectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			int row = table.getSelectedRow();
			if (row<0) return;
			GeneCluster gc = GeckoInstance.getInstance().getClusters()[(Integer) table.getValueAt(row, 0)];
			if (gc!=null && !(gc.getType()==GeneCluster.TYPE_REFERENCE))
				fireSelectionEvent(false);
		}
	};
	
	private KeyListener keyListener = new KeyAdapter() {
		
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_ENTER) {
				fireSelectionEvent(true);
				e.consume();
			}
		};
	};
	
	private ActionListener actionListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			fireSelectionEvent(true);
		}
	};
	
	private void fireSelectionEvent(boolean instant) {
		int row = table.getSelectedRow();
		if (row<0) return;
		GeneCluster gc = GeckoInstance.getInstance().getClusters()[(Integer) table.getValueAt(row, 0)];
		if (gc.getType()==GeneCluster.TYPE_CENTER || gc.getType()==GeneCluster.TYPE_MEDIAN) {
			checkBox.setVisible(false);
			fireSelectionEvent(new ClusterSelectionEvent(GeneClusterSelector.this, 
					gc,
					instant));
		} else {
			checkBox.setVisible(true);
			int[] subselections = new int[GeckoInstance.getInstance().getGenomes().length];
			Arrays.fill(subselections, 0);
			GeneClusterOccurrence gOcc;
			if (checkBox.isSelected())
				gOcc = gc.getAllOccurrences()[0];
			else
				gOcc = gc.getOccurrences()[0];
			fireSelectionEvent(new LocationSelectionEvent(GeneClusterSelector.this,
					gc,
					gOcc,
					subselections,
					instant));
		}
			
	}
	
	private MouseListener mouseListener = new MouseAdapter() {
		
		public void mouseDragged(MouseEvent e) {
			e.consume();
		};
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount()==1) {
				table.requestFocus();
				int row = table.rowAtPoint(e.getPoint());
				if (row>=0) table.setRowSelectionInterval(row, row);
				e.consume();
			} else if (e.getClickCount()==2) 
				fireSelectionEvent(true);
		};
	};	
		
	public void refresh() {
		table.clearSelection();
		model.refreshMachingClusters();
		model.fireTableDataChanged();
		fireSelectionEvent(new LocationSelectionEvent(this, null, null, null));
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
		private final Class<?>[] columns = {Integer.class, Integer.class, Integer.class, Double.class, String.class};
		private final String[] columnNames = {"ID","#Genes","#Genomes","P-value","Genes"};
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
//				DecimalFormat twoPlaces = new DecimalFormat("#.##");
//TODO UNCOMMENT
//				return twoPlaces.format(-Math.log(matchingClusters.get(rowIndex).getpValue()));
//				return 1;
				return matchingClusters.get(rowIndex).getBestPValue();
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
	
	private EventListenerList eventListener = new EventListenerList();
	
	public void addSelectionListener(ClusterSelectionListener s) {
		eventListener.add(ClusterSelectionListener.class, s);
	}
	public void removeSelectionListener(ClusterSelectionListener s) {
		eventListener.remove(ClusterSelectionListener.class, s);
	}
	public void fireSelectionEvent(ClusterSelectionEvent e) {
		for (ClusterSelectionListener l : eventListener.getListeners(ClusterSelectionListener.class))
			l.selectionChanged(e);
	}
	
}

