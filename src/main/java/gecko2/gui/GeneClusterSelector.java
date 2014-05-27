package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.GeckoInstance.ResultFilter;
import gecko2.algorithm.GeneCluster;
import gecko2.algorithm.GeneClusterOccurrence;
import gecko2.algorithm.GeneFamily;
import gecko2.algorithm.Parameter;
import gecko2.event.ClusterSelectionEvent;
import gecko2.event.ClusterSelectionListener;
import gecko2.event.LocationSelectionEvent;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class GeneClusterSelector extends JPanel implements ClipboardOwner {

	private static final long serialVersionUID = -4860132931042035952L;
	private final GeneClusterSelectorModel model;
	private final JCheckBox showSuboptimalCheckBox;
	private final JTable table;
	private JPopupMenu popUp;

    private final EventListenerList eventListener = new EventListenerList();
	
	public static final short COL_ID = 0;
	public static final short COL_NGENES = 1;
	public static final short COL_NGENOMES = 2;
	public static final short COL_SCORE = 3;
	public static final short COL_SCORE_CORRECTED = 4;
	public static final short COL_GENES = 5;

	
	public GeneClusterSelector() {
		this.setLayout(new BorderLayout());
		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		showSuboptimalCheckBox = new JCheckBox("show suboptimal hits");
		showSuboptimalCheckBox.setVisible(false);
		showSuboptimalCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireSelectionEvent(true);
            }
        });

        JComboBox<ResultFilter> selectionComboBox = new JComboBox<>(ResultFilter.values());
		selectionComboBox.setVisible(true);
		
		selectionComboBox.addActionListener(new ActionListener() {			
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ResultFilter selection = (ResultFilter)((JComboBox)e.getSource()).getSelectedItem();
				GeckoInstance.getInstance().filterBy(selection);
			}
		});
		checkBoxPanel.add(selectionComboBox);
		checkBoxPanel.add(showSuboptimalCheckBox);
		this.add(checkBoxPanel, BorderLayout.PAGE_END);
		this.setPreferredSize(new Dimension(50, 200));
		table = new JTable();
		table.setBackground(Color.WHITE);

		model = new GeneClusterSelectorModel();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setModel(model);
		TableRowSorter<GeneClusterSelectorModel> sorter = new TableRowSorter<GeneClusterSelectorModel>(model);
		sorter.setSortable(COL_GENES, false);

		table.setRowSorter(sorter);
		final TableColumnModel cm = table.getColumnModel();
		cm.getColumn(COL_ID).setPreferredWidth(50); // ID
		cm.getColumn(COL_NGENES).setPreferredWidth(50); // #Genes
		cm.getColumn(COL_NGENOMES).setPreferredWidth(70); // #Genomes
		cm.getColumn(COL_SCORE).setPreferredWidth(60); // pValue
		cm.getColumn(COL_SCORE_CORRECTED).setPreferredWidth(60); // corrected pValue
		cm.getColumn(COL_GENES).setPreferredWidth(200); // Genes
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(Color.WHITE);

        // Add content to Panel
		this.add(scrollPane, BorderLayout.CENTER);

        // Listeners
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = table.getSelectedRow();
                if (row < 0)
                    return;

                GeneCluster gc = GeckoInstance.getInstance().getClusters().get((Integer) table.getValueAt(row, 0));
                if (gc != null && !(gc.getType() == Parameter.OperationMode.reference)) {
                    fireSelectionEvent(false);
                }
            }
        });
		
		for (MouseListener l : table.getMouseListeners()) {
			table.removeMouseListener(l);
		}
		
		for (MouseMotionListener l : table.getMouseMotionListeners()) {
			table.removeMouseMotionListener(l);
		}
		
		table.addMouseListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2)	{
                    fireSelectionEvent(true);
                }
                else {
                    if (e.getClickCount() == 1) {
                        table.requestFocus();
                        int row = table.rowAtPoint(e.getPoint());

                        if (row >= 0) {
                            table.setRowSelectionInterval(row, row);
                        }

                        if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                            popUp.show(e.getComponent(), e.getX(), e.getY());
                        }

                        e.consume();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    popUp.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

		table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    fireSelectionEvent(true);
                    e.consume();
                }
            }
        });
		ActionMap am =  table.getActionMap();
		am.put("copy", new AbstractAction() {
            /**
             * Random generated serialization UID
             */
            private static final long serialVersionUID = 8912874714540056321L;

            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();

                if (row < 0) return;

                GeneCluster gc = GeckoInstance.getInstance().getClusters().get((Integer) table.getValueAt(row, 0));
                StringBuilder geneIDs = new StringBuilder();

                for (GeneFamily geneFamily : gc.getGeneFamilies()) {
                    geneIDs.append(geneFamily.getAlgorithmId()).append(" ");
                }

                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(geneIDs.toString()), GeneClusterSelector.this);
            }
        });
		table.setDefaultRenderer(Double.class, new DoubleCellRenderer());
		
		table.getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(3, SortOrder.DESCENDING)));

		// Build popup menu
		popUp = new JPopupMenu();
		
		JMenuItem menuItem = new JMenuItem("Add to selection");
		menuItem.addActionListener(new ActionListener()	{
			@Override
			public void actionPerformed(ActionEvent e) {
				GeckoInstance.getInstance().addToClusterSelection((Integer)table.getValueAt(table.getSelectedRow(), 0));				
			}
		});
		
		popUp.add(menuItem);
		menuItem = new JMenuItem("Clear selection");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int confirmDialog = JOptionPane.showConfirmDialog(GeneClusterSelector.this, "Really clear selection?", "Clear selection", JOptionPane.YES_NO_OPTION);
				if (confirmDialog == JOptionPane.YES_OPTION) {
					GeckoInstance.getInstance().clearClusterSelection();				
				}
			}
		});
		
		popUp.add(menuItem);

		menuItem = new JMenuItem("Export gene cluster");
		menuItem.addActionListener(new ActionListener() {			
			
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractMultipleGenomeBrowser mgb = GeckoInstance.getInstance().getGui().getMgb();
				if (mgb.getSelectedCluster() == null || !GeckoInstance.getInstance().getClusters().get((Integer) table.getValueAt(table.getSelectedRow(), COL_ID)).equals(mgb.getSelectedCluster()))
					fireSelectionEvent(true);
				GeneClusterExportDialog d = new GeneClusterExportDialog(GeckoInstance.getInstance().getGui().getMainframe(), mgb.getSelectedCluster(), mgb.getSubselection());
				d.setVisible(true);
			}
		});
		
		popUp.add(menuItem);
		
		popUp.addSeparator();
		
		menuItem = new JMenuItem("Show similiar clusters");
		menuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				String filterString = table.getValueAt(table.getSelectedRow(), 4).toString();
				
				if (filterString.length() > 2) {
					filterString = filterString.substring(1, filterString.length() - 1);
				}
				
				GeckoInstance.getInstance().setFilterString(filterString);
			}
		});
		popUp.add(menuItem);
	}

    /**
     * A new cell renderer for double values, used for the p-values
     */
	public static class DoubleCellRenderer extends  DefaultTableCellRenderer.UIResource {

		/**
		 * Random generated serialization UID
		 */
		private static final long serialVersionUID = 3185195801465019965L;

		public DoubleCellRenderer() {
			super();
		    setHorizontalAlignment(JLabel.RIGHT);
		}
		
		@Override
		protected void setValue(Object value) {
			if (value == null || (! (value instanceof Double))) {
				setText("");
			} 
			else {
				setText(String.format("%1.3f", (Double)value));
			}
		}
	}
	
	private void fireSelectionEvent(boolean instant) {
		int row = table.getSelectedRow();
		
		if (row < 0) return;
		
		GeneCluster gc = GeckoInstance.getInstance().getClusters().get((Integer) table.getValueAt(row, 0));
		
		if (gc.getType() == Parameter.OperationMode.center || gc.getType() == Parameter.OperationMode.median) {
			showSuboptimalCheckBox.setVisible(false);
			fireSelectionEvent(new ClusterSelectionEvent(GeneClusterSelector.this, 
					gc,
					instant));
		} 
		else {
			showSuboptimalCheckBox.setVisible(true);

            GeneClusterOccurrence gOcc;
			if (showSuboptimalCheckBox.isSelected())
				gOcc = gc.getAllOccurrences()[0];
			else
				gOcc = gc.getOccurrences()[0];
			
			int[] subselections = gc.getDefaultSubSelection(showSuboptimalCheckBox.isSelected());
			
			fireSelectionEvent(new LocationSelectionEvent(GeneClusterSelector.this,
					gc,
					gOcc,
					subselections,
					instant));
		}
	}
		
	public void refresh() {
		table.clearSelection();
		model.refreshMatchingClusters();
		model.fireTableDataChanged();
		fireSelectionEvent(new LocationSelectionEvent(this, null, null, null));
		TableCellRenderer r = table.getDefaultRenderer(String.class);
		int maxWidth = 0;

		for (int i = 0; i < model.getRowCount(); i++) {
			int width = (int) r.getTableCellRendererComponent(table, model.getValueAt(i, 4), false, true, i, 4).getPreferredSize().getWidth();

			if (width > maxWidth) {
				maxWidth = width;
			}
		}

		table.getColumnModel().getColumn(4).setPreferredWidth(maxWidth + 5);
	}

	/**
	 * This method makes the method removeGenomeFromSelection from the class
	 * GeneClusterSelectorModel visible for the user.
	 * 
	 * @param genomeToReset genome to remove from the filtered view
	 */
	public void resetGenome(int genomeToReset) {
		table.clearSelection();
		model.removeGenomeFromSelection(genomeToReset);
		model.fireTableDataChanged();
		fireSelectionEvent(new LocationSelectionEvent(this, null, null, null));
		TableCellRenderer r = table.getDefaultRenderer(String.class);
		int maxWidth = 0;

		for (int i = 0; i < model.getRowCount(); i++) {
			int width = (int) r.getTableCellRendererComponent(table, model.getValueAt(i, 4), false, true, i, 4).getPreferredSize().getWidth();

			if (width > maxWidth) {
				maxWidth = width;
			}
		}

		table.getColumnModel().getColumn(4).setPreferredWidth(maxWidth + 5);
	}

	/**
	 * This method makes the method showClustersWithSelectedGenome from the class
	 * GeneClusterSelectorModel visible for the user.
	 * 
	 * @param genomeToAddToFilter the index of the genome that is added to the filter
	 */
	public void showOnlyClusterWithSelectedGenome(int genomeToAddToFilter) {
		table.clearSelection();
		model.showClustersWithSelectedGenome(genomeToAddToFilter);
		model.fireTableDataChanged();
		fireSelectionEvent(new LocationSelectionEvent(this, null, null, null));
		TableCellRenderer r = table.getDefaultRenderer(String.class);
		int maxWidth = 0;

		for (int i = 0; i < model.getRowCount(); i++) {
			int width = (int) r.getTableCellRendererComponent(table, model.getValueAt(i, 4), false, true, i, 4).getPreferredSize().getWidth();

			if (width > maxWidth) {
				maxWidth = width;
			}
		}

		table.getColumnModel().getColumn(4).setPreferredWidth(maxWidth + 5);
	}

	/**
	 * This method make the method showClustersWithoutSelectedGenome from the class 
	 * GeneClusterSelectorModel visible for the user.
	 * 
	 * @param genomeToRemove genome to add to the filter
	 */
	public void dontShowClusterWithSelectedGenome(int genomeToRemove) {
		table.clearSelection();
		model.showClustersWithoutSelectedGenome(genomeToRemove);
		model.fireTableDataChanged();
		fireSelectionEvent(new LocationSelectionEvent(this, null, null, null));
		TableCellRenderer r = table.getDefaultRenderer(String.class);
		int maxWidth = 0;

		for (int i = 0; i < model.getRowCount(); i++) {
			int width = (int) r.getTableCellRendererComponent(table, model.getValueAt(i, 4), false, true, i, 4).getPreferredSize().getWidth();

			if (width > maxWidth) {
				maxWidth = width;
			}
		}

		table.getColumnModel().getColumn(4).setPreferredWidth(maxWidth + 5);
	}

	
	static class GeneClusterSelectorModel extends AbstractTableModel {

		private static final long serialVersionUID = -8389126835229250539L;
		private final Class<?>[] columns = {Integer.class, Integer.class, Integer.class, Double.class, Double.class, String.class};
		private final String[] columnNames = {"ID", "#Genes", "#Genomes", "Score", "C-Score", "Genes"};
		private final GeckoInstance instance;
		
		private java.util.List<GeneCluster> matchingClusters;
		private final Set<Integer> exclude = new HashSet<>();
		private final Set<Integer> include = new HashSet<>();
		
		public void refreshMatchingClusters() {
			matchingClusters.clear();

			if (instance.getClusters() != null)	{
				for (GeneCluster c : instance.getClusters()) {
					if (c.isMatch()) {
						matchingClusters.add(c);
					}
				}
			}
		}
		
		public java.util.List<GeneCluster> getMatchingClusters()	{
			return matchingClusters;
		}
		
		
		public GeneClusterSelectorModel() {
			this.instance = GeckoInstance.getInstance();
			matchingClusters = new ArrayList<>();
		}

        @Override
		public Class<?> getColumnClass(int columnIndex)	{
			return this.columns[columnIndex];
		}

        @Override
		public int getColumnCount()	{
			return this.columns.length;
		}

        @Override
		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}

        @Override
		public int getRowCount() {
			return matchingClusters.size();
		}

        @Override
		public Object getValueAt(int rowIndex, int columnIndex)	{
			
			switch(columnIndex)	{
				case COL_ID:
					return matchingClusters.get(rowIndex).getId();
				case COL_NGENES:
					return matchingClusters.get(rowIndex).getGeneFamilies().size();
				case COL_NGENOMES:
					return matchingClusters.get(rowIndex).getSize();
				case COL_SCORE:
					return matchingClusters.get(rowIndex).getBestScore();
				case COL_SCORE_CORRECTED:
					return matchingClusters.get(rowIndex).getBestCorrectedScore();
				case COL_GENES:
                    Set<GeneFamily> genes = matchingClusters.get(rowIndex).getGeneFamilies();
                    ArrayList<String> knownGenes = new ArrayList<>();

                    for (GeneFamily g : genes)	{
                        if (!g.isSingleGeneFamily()) {
                            knownGenes.add(g.getExternalId());
                        }
                    }

                    return Arrays.toString(knownGenes.toArray(new String[knownGenes.size()]));

				default:
					return null;
			}
		}

        @Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// Read-only table
			return false;
		}
		
		/**
		 * This method removes a genome from the criteria list of genomes
		 * which are in-/excluded from the clusters.
		 * 
		 * @param toRemove id of the genome to remove
		 */
		protected void removeGenomeFromSelection(int toRemove) {
			this.refreshMatchingClusters();
			
			if (this.include.contains(toRemove)) {
				include.remove(toRemove);
			}
			
			if (this.exclude.contains(toRemove)) {
				exclude.remove(toRemove);
			}
			
			Object[] includeHelper = this.include.toArray();
			Object[] excludeHelper = this.exclude.toArray();
			
			this.exclude.clear();
			this.include.clear();
			
			// If the array lists are not empty we have to include/exclude the rest of
			// the contained genomes
			for (int i = 0; i < includeHelper.length && includeHelper.length > 0; i++) {
				this.showClustersWithSelectedGenome((Integer) includeHelper[i]);
			}
			
			for (int i = 0; i < excludeHelper.length && excludeHelper.length > 0; i++) {
				this.showClustersWithoutSelectedGenome((Integer) excludeHelper[i]);
			}
		}
		
		
		/**
		 * This method filters the matching clusters array list. It sorts out all
		 * clusters which do not contain the given genome.
		 * 
		 * @param genomeToAddToFilter id of the genome to keep in clusters
		 */
		protected void showClustersWithSelectedGenome(int genomeToAddToFilter) {
			ArrayList<GeneCluster> tmp = new ArrayList<>();
			
			if (this.exclude.contains(genomeToAddToFilter)) {
				this.removeGenomeFromSelection(genomeToAddToFilter);
			}
			
			for (GeneCluster g : this.matchingClusters) {
				for (GeneClusterOccurrence k : g.getAllOccurrences()) {
					for (int i = 0; i < k.getSubsequences()[genomeToAddToFilter].length; i++) {
						// If a genome is not in the cluster start is greater then stop
						// keep this in mind
						if (k.getSubsequences()[genomeToAddToFilter][i].getStart() < k.getSubsequences()[genomeToAddToFilter][i].getStop()) {
							tmp.add(g);
							break;
						}
					}
				}
			}
				
			this.matchingClusters = tmp;
			this.include.add(genomeToAddToFilter);	
		} 
		
		/**
		 * This method filters the matching clusters array list. It sorts out all
		 * clusters which do contain the given genome.
		 * 
		 * @param genomeToRemoveFromClusterSelection id of the genome to sort out (number)
		 */
		protected void showClustersWithoutSelectedGenome(int genomeToRemoveFromClusterSelection) {
			ArrayList<GeneCluster> tmp = new ArrayList<>();
			
			if (this.include.contains(genomeToRemoveFromClusterSelection)) {
				this.removeGenomeFromSelection(genomeToRemoveFromClusterSelection);
			}
			
			for (GeneCluster g : this.matchingClusters) {
				for (GeneClusterOccurrence k : g.getAllOccurrences()) {
					for (int i = 0; i < k.getSubsequences()[genomeToRemoveFromClusterSelection].length; i++) {
						// If a genome is not in the cluster start is greater then stop
						if (k.getSubsequences()[genomeToRemoveFromClusterSelection][i].getStart() < k.getSubsequences()[genomeToRemoveFromClusterSelection][i].getStop()) {
							tmp.add(g);
							break;
						}
					}
				}
			}
			
			this.matchingClusters.removeAll(tmp);
			this.exclude.add(genomeToRemoveFromClusterSelection);
		}
	}
	
	public void addSelectionListener(ClusterSelectionListener s) {
		eventListener.add(ClusterSelectionListener.class, s);
	}
	
	public void removeSelectionListener(ClusterSelectionListener s) {
		eventListener.remove(ClusterSelectionListener.class, s);
	}
	
	public void fireSelectionEvent(ClusterSelectionEvent e)	{
		for (ClusterSelectionListener l : eventListener.getListeners(ClusterSelectionListener.class)) {
			l.selectionChanged(e);
		}
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// Do nothing		
	}
	
}

