package gecko2.gui;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import gecko2.GeckoInstance;
import gecko2.GeckoInstance.ResultFilter;
import gecko2.algorithm.*;
import gecko2.event.*;

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
import java.util.*;


public class GeneClusterSelector extends JPanel implements ClipboardOwner, DataListener {
	private static final long serialVersionUID = -4860132931042035952L;

	private final GeneClusterSelectorModel model;
    private java.util.List<GeneCluster> clusters;
	private final JCheckBox showSuboptimalCheckBox;
	private final JTable table;
	private JPopupMenu popUp;

    // Filter options
    private ResultFilter filterSelection;
    private String[] filterStrings;

    private final EventListenerList eventListener = new EventListenerList();
	
	public static final short COL_ID = 0;
	public static final short COL_NGENES = 1;
	public static final short COL_NGENOMES = 2;
	public static final short COL_SCORE = 3;
	public static final short COL_SCORE_CORRECTED = 4;
	public static final short COL_GENES = 5;

	
	public GeneClusterSelector() {
        filterSelection = ResultFilter.showAll;
        filterStrings = new String[0];

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
                JComboBox<ResultFilter> cb = (JComboBox<ResultFilter>)e.getSource();
                ResultFilter newFilter = cb.getItemAt(cb.getSelectedIndex());
                if (filterSelection != newFilter) {
                    filterSelection = (ResultFilter) ((JComboBox) e.getSource()).getSelectedItem();
                    updateData();
                }
            }
        });

		checkBoxPanel.add(selectionComboBox);
		checkBoxPanel.add(showSuboptimalCheckBox);
		this.add(checkBoxPanel, BorderLayout.PAGE_END);
		this.setPreferredSize(new Dimension(50, 200));
		table = new JTable();
		table.setBackground(Color.WHITE);
        this.clusters = new ArrayList<>();
		model = new GeneClusterSelectorModel();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setModel(model);
		TableRowSorter<GeneClusterSelectorModel> sorter = new TableRowSorter<>(model);
		sorter.setSortable(COL_GENES, false);

        java.util.List<RowFilter<GeneClusterSelectorModel, Integer>> filters = new ArrayList<>(3);
        filters.add(new GeneClusterTextFilter());

        sorter.setRowFilter(RowFilter.andFilter(filters));

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
                if (e.getClickCount() == 2) {
                    fireSelectionEvent(true);
                } else {
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
				GeckoInstance.getInstance().addToClusterSelection(GeckoInstance.getInstance().getClusters().get((Integer) table.getValueAt(table.getSelectedRow(), 0)));
                if (filterSelection.equals(ResultFilter.showSelected))
                    updateData();
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
                    if (filterSelection.equals(ResultFilter.showSelected))
                        updateData();
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
				setFilterString(filterString);
			}
		});
		popUp.add(menuItem);
	}

    private class GeneClusterTextFilter extends RowFilter<GeneClusterSelectorModel, Integer> {
        /**
         * Returns true if the specified entry should be shown;
         * returns false if the entry should be hidden.
         * <p/>
         * The <code>entry</code> argument is valid only for the duration of
         * the invocation.  Using <code>entry</code> after the call returns
         * results in undefined behavior.
         *
         * @param entry a non-<code>null</code> object that wraps the underlying
         *              object from the model
         * @return true if the entry should be shown
         */
        @Override
        public boolean include(Entry<? extends GeneClusterSelectorModel, ? extends Integer> entry) {
            if (0 == filterStrings.length)
                return true;
            GeneCluster cluster = entry.getModel().getGeneCluster(entry.getIdentifier());

            for (GeneClusterOccurrence gOcc : cluster.getAllOccurrences()) {
                for (int genome = 0; genome < gOcc.getSubsequences().length; genome++) {
                    Subsequence[] subseqs = gOcc.getSubsequences()[genome];
                    for (Subsequence s : subseqs) {
                        //TODO GeneCluster should know their genomes?
                        for (Gene g : GeckoInstance.getInstance().getGenomes()[genome].getSubsequence(s)) {
                            for (String pattern : filterStrings) {
                                if (g.getSummary().toLowerCase().contains(pattern)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    private static class GeneClusterIncludeExcludeMatcherEditor extends AbstractMatcherEditor<GeneCluster> implements ActionListener {
        private Set<Integer> include;
        private Set<Integer> exclude;
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox<MultipleGenomesBrowser.GenomeFilterMode> cb = (JComboBox) e.getSource();
            int genomeIndex = Integer.parseInt(cb.getName());
            MultipleGenomesBrowser.GenomeFilterMode filterMode = cb.getItemAt(cb.getSelectedIndex());

            boolean relaxed = false;
            boolean constrained = false;

            switch (filterMode) {
                case None:
                    relaxed = (exclude.remove(genomeIndex) ||include.remove(genomeIndex));
                    break;
                case Exclude:
                    relaxed = include.remove(genomeIndex);
                    constrained = exclude.add(genomeIndex);
                    break;
                case Include:
                    relaxed = exclude.remove(genomeIndex);
                    constrained = include.remove(genomeIndex);
            }

            if (relaxed && !constrained) {
                if (include.isEmpty() && exclude.isEmpty())
                    this.fireMatchAll();
                else
                    this.fireRelaxed(new GeneClusterIncludeExcludeMatcher(include, exclude));
            } else if (constrained && ! relaxed) {
                this.fireConstrained(new GeneClusterIncludeExcludeMatcher(include, exclude));

            } else if (constrained && relaxed) {
                this.fireChanged(new GeneClusterIncludeExcludeMatcher(include, exclude));
            }

            this.fireMatchAll();
            this.fireChanged(new GeneClusterIncludeExcludeMatcher(include, exclude));
        }

        private static class GeneClusterIncludeExcludeMatcher implements Matcher {
            private Set<Integer> include;
            private Set<Integer> exclude;

            public GeneClusterIncludeExcludeMatcher(Set<Integer> include, Set<Integer> exclude){
                this.include = new HashSet<>(include);
                this.exclude = new HashSet<>(exclude);
            }
            /**
             * Return true if an item matches a filter.
             *
             * @param item The item possibly being filtered.
             */
            @Override
            public boolean matches(Object item) {
                final GeneCluster cluster = (GeneCluster) item;
                return (include.isEmpty() || cluster.hasOccurrenceInGenome(include)) &&
                        (exclude.isEmpty() || cluster.hasOccurrenceInGenome(exclude));
            }
        }
    }

    private void updateData() {
        table.clearSelection();
        //TODO remember selected cluster?
        clusters = GeckoInstance.getInstance().getClusterList(filterSelection);
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

    public void setFilterString(String filterString) {
        filterStrings = filterString.trim().split(" ");
        for (int i=0; i<filterStrings.length; i++)
            filterStrings[i] = filterStrings[i].toLowerCase();
        if (1 == filterStrings.length && filterStrings.equals(""))
            filterStrings = new String[0];

        model.fireTableDataChanged();
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

    /**
     * This method is called when the data, i.e. the genomes or clusters currently
     * observed in this session are changed.
     *
     * @param e The data event that references the {@link gecko2.GeckoInstance} object
     *          handled the data update
     */
    @Override
    public void dataChanged(DataEvent e) {
        updateData();
    }

    private class GeneClusterSelectorModel extends AbstractTableModel {
		private static final long serialVersionUID = -8389126835229250539L;

		private final Class<?>[] columns = {Integer.class, Integer.class, Integer.class, Double.class, Double.class, String.class};
		private final String[] columnNames = {"ID", "#Genes", "#Genomes", "Score", "C-Score", "Genes"};

        public GeneCluster getGeneCluster(int index) {
            return clusters.get(index);
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
			return clusters.size();
		}

        @Override
		public Object getValueAt(int rowIndex, int columnIndex)	{
			switch(columnIndex)	{
				case COL_ID:
					return clusters.get(rowIndex).getId();
				case COL_NGENES:
					return clusters.get(rowIndex).getGeneFamilies().size();
				case COL_NGENOMES:
					return clusters.get(rowIndex).getSize();
				case COL_SCORE:
					return clusters.get(rowIndex).getBestScore();
				case COL_SCORE_CORRECTED:
					return clusters.get(rowIndex).getBestCorrectedScore();
				case COL_GENES:
                    Set<GeneFamily> genes = clusters.get(rowIndex).getGeneFamilies();
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

    /**
     * A new cell renderer for double values, used for the p-values
     */
    static class DoubleCellRenderer extends  DefaultTableCellRenderer.UIResource {

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
}

