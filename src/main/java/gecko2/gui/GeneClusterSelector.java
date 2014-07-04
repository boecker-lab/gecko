package gecko2.gui;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.*;
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
import java.util.List;


public class GeneClusterSelector extends JPanel implements ClipboardOwner, DataListener {
	private static final long serialVersionUID = -4860132931042035952L;

    //private java.util.List<GeneCluster> clusters;
	private final JCheckBox showSuboptimalCheckBox;
	private final JTable table;
    private DefaultEventSelectionModel<GeneCluster> tableModel;
	private JPopupMenu popUp;

    // Filter options
    private final TextComponentMatcherEditor<GeneCluster> textMatcherEditor;
    private final GeneClusterIncludeExcludeMatcherEditor includeExcludeMatcherEditor;
    private ResultFilter filterSelection;

    private final EventListenerList eventListener = new EventListenerList();
	
	private static final short COL_ID = 0;
    private static final short COL_NGENES = 1;
    private static final short COL_NGENOMES = 2;
    private static final short COL_SCORE = 3;
    private static final short COL_SCORE_CORRECTED = 4;
    private static final short COL_GENES = 5;

	
	public GeneClusterSelector(final JTextField filterField) {
        filterSelection = ResultFilter.showAll;
        textMatcherEditor = new TextComponentMatcherEditor<>(filterField, new GeneClusterTextFilterator());
        textMatcherEditor.setLive(false);
        textMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
        includeExcludeMatcherEditor = new GeneClusterIncludeExcludeMatcherEditor();

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
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(Color.WHITE);

        // Add content to Panel
		this.add(scrollPane, BorderLayout.CENTER);

        // Listeners
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                List<GeneCluster> gcs = tableModel.getSelected();
                if (gcs.isEmpty())
                    return;
                GeneCluster gc = gcs.get(0);
                if (!(gc.getType() == Parameter.OperationMode.reference)) {
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
                List<GeneCluster> gcs = tableModel.getSelected();
                if (gcs.isEmpty())
                    return;
                GeneCluster gc = gcs.get(0);
                StringBuilder geneIDs = new StringBuilder();

                for (GeneFamily geneFamily : gc.getGeneFamilies()) {
                    geneIDs.append(geneFamily.getAlgorithmId()).append(" ");
                }

                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(geneIDs.toString()), GeneClusterSelector.this);
            }
        });
		table.setDefaultRenderer(Double.class, new DoubleCellRenderer());

		// Build popup menu
		popUp = new JPopupMenu();
		
		JMenuItem menuItem = new JMenuItem("Add to selection");
		menuItem.addActionListener(new ActionListener()	{
			@Override
			public void actionPerformed(ActionEvent e) {
                List<GeneCluster> gcs = tableModel.getSelected();
                if (gcs.isEmpty())
                    return;
                GeneCluster gc = gcs.get(0);
				GeckoInstance.getInstance().addToClusterSelection(gc);
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
                List<GeneCluster> gcs = tableModel.getSelected();
                if (gcs.isEmpty())
                    return;
                GeneCluster gc = gcs.get(0);
				if (mgb.getSelectedCluster() == null || !gc.equals(mgb.getSelectedCluster()))
					fireSelectionEvent(true);
				GeneClusterExportDialog d = new GeneClusterExportDialog(GeckoInstance.getInstance().getGui().getMainframe(), mgb.getSelectedCluster(), mgb.getSubselection());
				d.setVisible(true);
			}
		});
		
		popUp.add(menuItem);
		
		popUp.addSeparator();
		
		menuItem = new JMenuItem("Show similar clusters");
		menuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				String filterString = table.getValueAt(table.getSelectedRow(), 4).toString();
				if (filterString.length() > 2) {
					filterString = filterString.substring(1, filterString.length() - 1);
				}
				filterField.setText(filterString);
			}
		});
		popUp.add(menuItem);
	}

    private void updateData() {
        table.clearSelection();
        List<GeneCluster> clusters = GeckoInstance.getInstance().getClusterList(filterSelection);
        EventList<GeneCluster> geneClusterEventList = GlazedLists.eventList(clusters);
        SortedList<GeneCluster> sortedList = new SortedList<>(geneClusterEventList);
        FilterList<GeneCluster> includeExcludeFilteredList = new FilterList<>(sortedList, includeExcludeMatcherEditor);
        FilterList<GeneCluster> textFilteredList = new FilterList<>(includeExcludeFilteredList, textMatcherEditor);
        AdvancedTableModel<GeneCluster> geneClusterTableModel = GlazedListsSwing.eventTableModelWithThreadProxyList(textFilteredList, new GeneClusterTableFormat());
        tableModel = new DefaultEventSelectionModel<>(textFilteredList);
        table.setModel(geneClusterTableModel);
        table.setSelectionModel(tableModel);
        final TableColumnModel cm = table.getColumnModel();
        cm.getColumn(COL_ID).setPreferredWidth(50); // ID
        cm.getColumn(COL_NGENES).setPreferredWidth(50); // #Genes
        cm.getColumn(COL_NGENOMES).setPreferredWidth(70); // #Genomes
        cm.getColumn(COL_SCORE).setPreferredWidth(60); // pValue
        cm.getColumn(COL_SCORE_CORRECTED).setPreferredWidth(60); // corrected pValue
        cm.getColumn(COL_GENES).setPreferredWidth(200); // Genes

        fireSelectionEvent(new LocationSelectionEvent(this, null, null, null));
    }

    public void addIncludeExcludeFilterComboBox(JComboBox box) {
        box.addActionListener(includeExcludeMatcherEditor);
    }

    private static class GeneClusterTextFilterator implements TextFilterator<GeneCluster> {
        /**
         * Gets the specified object as a list of Strings. These Strings
         * should contain all object information so that it can be compared
         * to the filter set.
         *
         * @param baseList a list that the implementor shall add their filter
         *                 strings to via <code>baseList.add()</code>. This may be a non-empty
         *                 List and it is an error to call any method other than add().
         * @param cluster  the object to extract the filter strings from.
         */
        @Override
        public void getFilterStrings(List<String> baseList, GeneCluster cluster) {
            for (GeneClusterOccurrence gOcc : cluster.getAllOccurrences()) {
                for (int genome = 0; genome < gOcc.getSubsequences().length; genome++) {
                    Subsequence[] subseqs = gOcc.getSubsequences()[genome];
                    for (Subsequence s : subseqs) {
                        //TODO GeneCluster should know their genomes?
                        for (Gene g : GeckoInstance.getInstance().getGenomes()[genome].getSubsequence(s)) {
                            baseList.add(g.getSummary());
                        }
                    }
                }
            }
        }
    }

    static class GeneClusterIncludeExcludeMatcherEditor extends AbstractMatcherEditor<GeneCluster> implements ActionListener {
        private final Set<Integer> include;
        private final Set<Integer> exclude;

        public GeneClusterIncludeExcludeMatcherEditor() {
            include = new HashSet<>();
            exclude = new HashSet<>();
        }

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
                    constrained = include.add(genomeIndex);
            }

            if (relaxed && !constrained) {
                if (include.isEmpty() && exclude.isEmpty())
                    this.fireMatchAll();
                else
                    this.fireRelaxed(new GeneClusterIncludeExcludeMatcher(include, exclude));
            } else if (constrained && !relaxed) {
                this.fireConstrained(new GeneClusterIncludeExcludeMatcher(include, exclude));

            } else {
                this.fireChanged(new GeneClusterIncludeExcludeMatcher(include, exclude));
            }
        }

        private static class GeneClusterIncludeExcludeMatcher implements Matcher {
            private final Set<Integer> include;
            private final Set<Integer> exclude;

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
                return (include.isEmpty() || cluster.hasOccurrenceInAllGenomes(include)) &&
                        (exclude.isEmpty() || cluster.hasNoOccurrenceInGenomes(exclude));
            }
        }
    }
	
	private void fireSelectionEvent(boolean instant) {
		int row = table.getSelectedRow();
		
		if (row < 0) return;

        List<GeneCluster> gcs = tableModel.getSelected();
        if (gcs.isEmpty())
            return;
        GeneCluster gc = gcs.get(0);
		
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
			
			int[] subSelections = gc.getDefaultSubSelection(showSuboptimalCheckBox.isSelected());
			
			fireSelectionEvent(new LocationSelectionEvent(GeneClusterSelector.this,
					gc,
					gOcc,
					subSelections,
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

    private class GeneClusterTableFormat implements TableFormat<GeneCluster> {
        private final String[] columnNames = {"ID", "#Genes", "#Genomes", "Score", "C-Score", "Genes"};
        /**
         * The number of columns to display.
         */
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        /**
         * Gets the title of the specified column.
         *
         * @param column
         */
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        /**
         * Gets the value of the specified field for the specified object. This
         * is the value that will be passed to the editor and renderer for the
         * column. If you have defined a custom renderer, you may choose to return
         * simply the baseObject.
         *
         * @param cluster
         * @param column
         */
        @Override
        public Object getColumnValue(GeneCluster cluster, int column) {
            switch(column)	{
                case COL_ID:
                    return cluster.getId();
                case COL_NGENES:
                    return cluster.getGeneFamilies().size();
                case COL_NGENOMES:
                    return cluster.getSize();
                case COL_SCORE:
                    return cluster.getBestScore();
                case COL_SCORE_CORRECTED:
                    return cluster.getBestCorrectedScore();
                case COL_GENES:
                    Set<GeneFamily> genes = cluster.getGeneFamilies();
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
    }
	
	public void addSelectionListener(ClusterSelectionListener s) {
		eventListener.add(ClusterSelectionListener.class, s);
	}
	
	public void removeSelectionListener(ClusterSelectionListener s) {
		eventListener.remove(ClusterSelectionListener.class, s);
	}
	
	void fireSelectionEvent(ClusterSelectionEvent e)	{
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

