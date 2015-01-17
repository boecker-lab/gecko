/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.gui;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.*;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.GeckoInstance.ResultFilter;
import de.unijena.bioinf.gecko3.datastructures.*;
import de.unijena.bioinf.gecko3.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.awt.geom.Arc2D;
import java.util.*;
import java.util.List;


public class GeneClusterSelector extends JPanel implements ClipboardOwner, DataListener {
	private static final long serialVersionUID = -4860132931042035952L;

    private static final Logger logger = LoggerFactory.getLogger(GeneClusterSelector.class);

	private final JCheckBox showSuboptimalCheckBox;
	private final JTable table;
    private EventList<GeneCluster> displayedTableData;
    private DefaultEventSelectionModel<GeneCluster> tableModel;
	private JPopupMenu popUp;

    // Filter options
    private final JTextField filterField;
    private TextComponentMatcherEditor<GeneCluster> textMatcherEditor;
    List<JComboBox> includeExcludeBoxes;
    private GeneClusterIncludeExcludeMatcherEditor includeExcludeMatcherEditor;
    private ResultFilter filterSelection;

    private final EventListenerList eventListener = new EventListenerList();
	
	private static final short COL_ID = 0;
    private static final short COL_NGENES = 1;
    private static final short COL_NGENOMES = 2;
    private static final short COL_SCORE = 3;
    private static final short COL_SCORE_CORRECTED = 4;
    private static final short COL_GENES = 5;

	
	public GeneClusterSelector(final JTextField filterField) {
        filterSelection = ResultFilter.showFiltered;
        this.filterField = filterField;
        this.includeExcludeBoxes = new ArrayList<>();

		this.setLayout(new BorderLayout());
		JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		showSuboptimalCheckBox = new JCheckBox("show suboptimal hits");
		showSuboptimalCheckBox.setVisible(false);
		showSuboptimalCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireSelectionEvent();
            }
        });

        JComboBox<ResultFilter> selectionComboBox = new JComboBox<>(ResultFilter.values());
		selectionComboBox.setVisible(true);

        // Build popup menu
        popUp = new JPopupMenu();
        final JMenuItem addToSelectionMenuItem = new JMenuItem("Add to selection");
        final JMenuItem addAllToSelectionMenuItem = new JMenuItem("Add all in list to selection");
        final JMenuItem clearSelectionMenuItem = new JMenuItem("Clear selection");
        final JMenuItem exportMenuItem = new JMenuItem("Export gene cluster");
        final JMenuItem showSimilarMenuItem = new JMenuItem("Show similar clusters");
        final JMenuItem copyGeneIdsMenuItem = new JMenuItem();
        popUp.add(addToSelectionMenuItem);
        popUp.add(addAllToSelectionMenuItem);
        popUp.add(clearSelectionMenuItem);
        popUp.add(exportMenuItem);
        popUp.addSeparator();
        popUp.add(showSimilarMenuItem);
        popUp.addSeparator();
        popUp.add(copyGeneIdsMenuItem);

		checkBoxPanel.add(selectionComboBox);
		checkBoxPanel.add(showSuboptimalCheckBox);
		this.add(checkBoxPanel, BorderLayout.PAGE_END);
		table = new JTable();
		table.setBackground(Color.WHITE);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Double.class, new DoubleCellRenderer());
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(Color.WHITE);

        // Add content to Panel
		this.add(scrollPane, BorderLayout.CENTER);

        // Listeners
        selectionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<ResultFilter> cb = (JComboBox<ResultFilter>)e.getSource();
                ResultFilter newFilter = cb.getItemAt(cb.getSelectedIndex());
                if (filterSelection != newFilter) {
                    filterSelection = (ResultFilter) ((JComboBox) e.getSource()).getSelectedItem();
                    if (filterSelection.equals(ResultFilter.showSelected)) {
                        addAllToSelectionMenuItem.setEnabled(false);
                    } else {
                        addAllToSelectionMenuItem.setEnabled(true);
                    }
                    updateData();
                }
            }
        });

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                List<GeneCluster> gcs = tableModel.getSelected();
                if (gcs.isEmpty())
                    return;
                GeneCluster gc = gcs.get(0);
                if (!(gc.getType() == Parameter.OperationMode.reference)) {
                    fireSelectionEvent();
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
                    fireSelectionEvent();
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
                    fireSelectionEvent();
                    e.consume();
                }
            }
        });
		ActionMap am =  table.getActionMap();
        Action copyAction = new AbstractAction("Copy gene ids") {
            private static final long serialVersionUID = 8912874714540056321L;

            @Override
            public void actionPerformed(ActionEvent e) {
                List<GeneCluster> gcs = tableModel.getSelected();
                if (gcs.isEmpty())
                    return;
                GeneCluster gc = gcs.get(0);
                StringBuilder geneIDs = new StringBuilder();

                for (GeneFamily geneFamily : gc.getGeneFamilies()) {
                    geneIDs.append(geneFamily.getExternalId()).append(" ");
                }

                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(geneIDs.toString()), GeneClusterSelector.this);
            }
        };
		am.put("copy", copyAction);
        copyGeneIdsMenuItem.setAction(copyAction);

		addToSelectionMenuItem.addActionListener(new ActionListener()	{
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

        addAllToSelectionMenuItem.addActionListener(new ActionListener()	{
            @Override
            public void actionPerformed(ActionEvent e) {
                if (displayedTableData.isEmpty())
                    return;
                for (GeneCluster geneCluster : displayedTableData) {
                    GeckoInstance.getInstance().addToClusterSelection(geneCluster);
                }
                if (filterSelection.equals(ResultFilter.showSelected))
                    updateData();
            }
        });

		clearSelectionMenuItem.addActionListener(new ActionListener() {
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

		exportMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				MultipleGenomesBrowser mgb = GeckoInstance.getInstance().getGui().getMgb();
                List<GeneCluster> gcs = tableModel.getSelected();
                if (gcs.isEmpty())
                    return;
                GeneCluster gc = gcs.get(0);
				if (mgb.getClusterSelection() == null || !gc.equals(mgb.getClusterSelection().getCluster()))
					fireSelectionEvent();
				GeneClusterExportDialog d = new GeneClusterExportDialog(GeckoInstance.getInstance().getGui().getMainframe(), mgb.getClusterSelection());
				d.setVisible(true);
			}
		});

		showSimilarMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String filterString = table.getValueAt(table.getSelectedRow(), 5).toString();
				if (filterString.length() > 2) {
					filterString = filterString.substring(1, filterString.length() - 1);
				}
				filterField.setText(filterString);
			}
		});
	}

    private void updateData() {
        table.clearSelection();
        List<GeneCluster> clusters = GeckoInstance.getInstance().getClusterList(filterSelection);

        //EventLists, sorting and filtering
        if (textMatcherEditor != null)
            textMatcherEditor.dispose();
        textMatcherEditor = new TextComponentMatcherEditor<>(filterField, new GeneClusterTextFilterator());
        textMatcherEditor.setLive(false);
        textMatcherEditor.setMode(TextMatcherEditor.CONTAINS);

        if (includeExcludeMatcherEditor != null){
            for (JComboBox box : includeExcludeBoxes)
                box.removeActionListener(includeExcludeMatcherEditor);
        }
        includeExcludeMatcherEditor = new GeneClusterIncludeExcludeMatcherEditor();
        for (JComboBox box : includeExcludeBoxes)
            box.addActionListener(includeExcludeMatcherEditor);

        EventList<GeneCluster> geneClusterEventList = GlazedLists.eventList(clusters);
        SortedList<GeneCluster> sortedList = new SortedList<>(geneClusterEventList);
        FilterList<GeneCluster> includeExcludeFilteredList = new FilterList<>(sortedList, includeExcludeMatcherEditor);
        FilterList<GeneCluster> textFilteredList = new FilterList<>(includeExcludeFilteredList, textMatcherEditor);

        displayedTableData = textFilteredList;
        AdvancedTableModel<GeneCluster> geneClusterTableModel = GlazedListsSwing.eventTableModelWithThreadProxyList(displayedTableData, new GeneClusterTableFormat());
        tableModel = new DefaultEventSelectionModel<>(textFilteredList);
        table.setModel(geneClusterTableModel);
        table.setSelectionModel(tableModel);
        TableComparatorChooser<GeneCluster> tableComparatorChooser = TableComparatorChooser.install(table, sortedList, TableComparatorChooser.SINGLE_COLUMN);
        tableComparatorChooser.getComparatorsForColumn(COL_GENES).clear();

        final TableColumnModel cm = table.getColumnModel();
        cm.getColumn(COL_ID).setPreferredWidth(30); // ID
        cm.getColumn(COL_NGENES).setPreferredWidth(50); // #Genes
        cm.getColumn(COL_NGENOMES).setPreferredWidth(70); // #Genomes
        cm.getColumn(COL_SCORE).setPreferredWidth(60); // pValue
        cm.getColumn(COL_SCORE_CORRECTED).setPreferredWidth(60); // corrected pValue
        cm.getColumn(COL_GENES).setPreferredWidth(200); // Genes

        fireSelectionEvent(new LocationSelectionEvent(this, null, false, null));
    }

    public void addIncludeExcludeFilterComboBox(JComboBox box) {
        includeExcludeBoxes.add(box);
        if (includeExcludeMatcherEditor != null)
            box.addActionListener(includeExcludeMatcherEditor);
    }

    public void clearSelection() {
        table.clearSelection();
        fireSelectionEvent(new LocationSelectionEvent(this, null, false, null));
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
            for (int genome = 0; genome < cluster.getOccurrences(true).getSubsequences().length; genome++) {
                Subsequence[] subseqs = cluster.getOccurrences(true).getSubsequences()[genome];
                for (Subsequence s : subseqs) {
                    //TODO GeneCluster should know their genomes?
                    for (Gene g : GeckoInstance.getInstance().getGenomes()[genome].getSubsequence(s)) {
                        baseList.add(g.getFilterString());
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
	
	private void fireSelectionEvent() {
		int row = table.getSelectedRow();
		
		if (row < 0) return;

        List<GeneCluster> gcs = tableModel.getSelected();
        if (gcs.isEmpty())
            return;
        GeneCluster gc = gcs.get(0);
		
		if (gc.getType() == Parameter.OperationMode.center || gc.getType() == Parameter.OperationMode.median) {
			showSuboptimalCheckBox.setVisible(false);
			fireSelectionEvent(new ClusterSelectionEvent(GeneClusterSelector.this, 
					gc));
		} 
		else {
			showSuboptimalCheckBox.setVisible(true);
			
			int[] subSelections = gc.getDefaultSubSelection(showSuboptimalCheckBox.isSelected());
			
			fireSelectionEvent(new LocationSelectionEvent(GeneClusterSelector.this,
					gc,
					showSuboptimalCheckBox.isSelected(),
					subSelections));
		}
	}

    /**
     * This method is called when the data, i.e. the genomes or clusters currently
     * observed in this session are changed.
     *
     * @param e The data event that references the {@link de.unijena.bioinf.gecko3.GeckoInstance} object
     *          handled the data update
     */
    @Override
    public void dataChanged(DataEvent e) {
        updateData();
    }

    private class GeneClusterTableFormat implements AdvancedTableFormat<GeneCluster> {
        private final String[] columnNames = {"ID", "#Genes", "#Genomes", "Score", "C-Score", "Genes"};
        private final Class<?>[] columns = {Integer.class, Integer.class, Integer.class, Double.class, Double.class, String.class};
        /**
         * The number of columns to display.
         */
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Class getColumnClass(int column) {
            return columns[column];
        }

        @Override
        public Comparator getColumnComparator(int column) {
            return GlazedLists.comparableComparator();
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

