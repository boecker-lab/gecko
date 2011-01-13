package gecko2.gui;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

/**
 * The {@link OccurrenceSelector} lets the user choose between the occurrences
 * of a selected {@link GeneCluster} (that was choosen with the {@link GeneClusterSelector}. 
 * 
 * @author Leon Kuchenbecker <lkuchenb@inf.fu-berlin.de>
 *
 */
public class OccurrenceSelector extends JPanel implements ClusterSelectionListener {
	
	/**
	 * Random generated serialization UID
	 */
	private static final long serialVersionUID = 1527543261671085862L;
	
	/*
	 * Gui elements
	 */
	private JTable table;
	private JCheckBox checkBox;
	
	/*
	 * Data
	 */
	private GeneCluster selection;

	/**
	 * Creates a new {@link OccurrenceSelector}.
	 * @param gd The {@link GeneClusterDisplay} that should be used to display
	 * the details of a selected occurrence.
	 */
	public OccurrenceSelector(GeneClusterDisplay gd) {
		// Create the gui elements
		this.setPreferredSize(new Dimension(50,10));
		table = new JTable();
		table.setModel(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TableColumnModel cm = table.getColumnModel();
		cm.getColumn(0).setPreferredWidth(20);
		cm.getColumn(1).setPreferredWidth(20);
		cm.getColumn(2).setPreferredWidth(20);
//		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		JScrollPane scrollPane = new JScrollPane(table);
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		checkBox = new JCheckBox("show suboptimal hits");
		checkBox.addActionListener(checkBoxListener);
		this.add(checkBox, BorderLayout.PAGE_END);
		table.addMouseListener(mouseListener);
		table.setBackground(Color.WHITE);
		scrollPane.getViewport().setBackground(Color.WHITE);
		scrollPane.setBackground(Color.WHITE);
	}
	
	private ActionListener checkBoxListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			model.fireTableDataChanged();			
		}
	};
	
	private void selectOccurrence(int row) {
		// Get the selected occurrence
		GeneClusterOccurrence gOcc = getOccurrences(selection)[(Integer) table.getValueAt(row, 0)];
		int[] subselections = new int[gOcc.getSubsequences().length];
		// The OccurrenceSelector is handling only center and median
		// clusters, so we expect only 1-element arrays for each genome
		for (int i=0; i<subselections.length; i++)
			if (gOcc.getSubsequences()[i].length==0)
				subselections[i]=GeneClusterOccurrence.GENOME_NOT_INCLUDED;
			else
				subselections[i]=0;
		fireSelectionEvent(new LocationSelectionEvent(OccurrenceSelector.this, 
				selection,
				gOcc,
				subselections));
	}
	
	private MouseListener mouseListener = new MouseAdapter() {
		
		public void mouseClicked(java.awt.event.MouseEvent e) {
			if (e.getClickCount()==2 && table.getSelectedRow()>=0) {
					selectOccurrence(table.getSelectedRow());
			}
		};
		
	};
	
	private GeneClusterOccurrence[] getOccurrences(GeneCluster gc) {
		if (gc==null)
			return null;
		if (checkBox.isSelected()) {
			return gc.getAllOccurrences();
		} else
			return gc.getOccurrences();
	}
	
	private int lastSplitPos;
	@Override
	public void selectionChanged(ClusterSelectionEvent e) {
		GeneCluster gc = e.getSelection();
		if (gc==null) {
			this.selection=null;
			model.fireTableDataChanged();
			lastSplitPos=-1;
			setVisible(false);
			return;
		}
		// If we are handling median or center gene clusters we
		// make sure the selector is visible and show the data
		if (gc.getType()==GeneCluster.TYPE_CENTER ||
				gc.getType()==GeneCluster.TYPE_MEDIAN) {
			this.selection = gc;
			model.fireTableDataChanged();
			if (!isVisible()) {
				setVisible(true);
				if (getParent() instanceof JSplitPane) {
					((JSplitPane) getParent()).setDividerLocation(lastSplitPos);
				}
			}
			if (e.isInstantDisplayEnabled() && table.getRowCount()>0) {
				table.getSelectionModel().setSelectionInterval(0, 0);
				selectOccurrence(0);
			}
		// Otherwise we check if the selector is visible, if it is
		// we turn the selector invisible
		} else if (isVisible()) {
			this.selection = null;
			if (getParent() instanceof JSplitPane) {
				lastSplitPos = ((JSplitPane) getParent()).getDividerLocation();
			}
			setVisible(false);
		}
	}
	
	private AbstractTableModel model = new AbstractTableModel() {
		
		/**
		 * Random generated serialization UID
		 */
		private static final long serialVersionUID = 4474685987973352693L;
		private final Class<?>[] columns = {Integer.class,Integer.class, Double.class, Integer.class};
		private final String[] columnNames = {"ID", "#Genomes", "P-value","Distance"};
		private final static int COL_ID = 0;
		private final static int COL_NGENOMES = 1;
		private final static int COL_PVALUE = 2;
		private final static int COL_DIST = 3;
			
		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		};
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case COL_ID:
				return getOccurrences(selection)[rowIndex].getId();
			case COL_NGENOMES:
				return getOccurrences(selection)[rowIndex].getSupport();
			case COL_PVALUE:
				return getOccurrences(selection)[rowIndex].getpValue();
			case COL_DIST:
				return getOccurrences(selection)[rowIndex].getTotalDist();
			default:
				return null;
			}			
		}
		
		@Override
		public int getRowCount() {
			if (selection==null)
				return 0;
			return getOccurrences(selection).length;
		}
		
		@Override
		public java.lang.Class<?> getColumnClass(int columnIndex) {
			return columns[columnIndex];
		};
		
		@Override
		public int getColumnCount() {
			return columns.length;
		}
	};
	
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
