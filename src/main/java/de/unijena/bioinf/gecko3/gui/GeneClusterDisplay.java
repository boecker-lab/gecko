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


import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.*;
import de.unijena.bioinf.gecko3.event.ClusterSelectionEvent;
import de.unijena.bioinf.gecko3.event.ClusterSelectionListener;
import de.unijena.bioinf.gecko3.event.LocationSelectionEvent;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.*;
import java.util.List;


public class GeneClusterDisplay extends JScrollPane implements ClusterSelectionListener {

	private static final long serialVersionUID = -2156280340296694286L;

    private static final Font monoFont = new Font("Monospaced",Font.PLAIN,new JLabel().getFont().getSize()-1);
    private static final Font boldFont = new JLabel().getFont().deriveFont(Font.BOLD);

	private final JPanel masterPanel;

	private GeneCluster cluster;
    private Parameter parameters;
	private boolean includeSubOptimalOccurrences;
    private int[] subselections;

    private String maxLengthString;
    private int geneWidth;

    private List<Subsequence> subsequences;
    private List<Chromosome> chromosomes;
    private List<Integer> genomeIndexMapping;
    private Map<Integer, Integer> genomeIndexBackmap;

    private List<Gene> geneList;
    private List<Integer> genomeIndexInGeneList;
    private Map<Integer, GeneFamily> geneIdAtTablePosition;
    private List<ReferenceGeneInfo> referenceGeneList;

    // local?
    private final JTable chromosomeNameTable;
    private final JTable referenceGeneTable;
    private final JTable annotationTable;
    // end local?
	
	public GeneClusterDisplay() {
		masterPanel = new JPanel();		
		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.PAGE_AXIS));
        masterPanel.setAlignmentX(LEFT_ALIGNMENT);
		masterPanel.setBackground(Color.WHITE);
		this.setViewportView(masterPanel);

        chromosomeNameTable = new JTable(new ChromosomeNameTableModel());
        chromosomeNameTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        chromosomeNameTable.setShowGrid(false);
        chromosomeNameTable.setAlignmentX(Component.LEFT_ALIGNMENT);
        chromosomeNameTable.setDefaultRenderer(NumberInRectangle.NumberIcon.class, new NumberIconRenderer());
        final TableColumnModel chromosomeNameTableColumnModel = chromosomeNameTable.getColumnModel();
        chromosomeNameTableColumnModel.getColumn(0).setPreferredWidth(50); // Index
        chromosomeNameTableColumnModel.getColumn(0).setMaxWidth(50); // Index
        chromosomeNameTableColumnModel.getColumn(1).setPreferredWidth(200);

        referenceGeneTable = new JTable(new ReferenceGeneTableModel());
        referenceGeneTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        referenceGeneTable.setShowGrid(false);
        referenceGeneTable.setAlignmentX(Component.LEFT_ALIGNMENT);
        referenceGeneTable.setDefaultRenderer(GeneFamily.class, new GeneRenderer());
        referenceGeneTable.getTableHeader().setReorderingAllowed(false);
        referenceGeneTable.getTableHeader().setFont(referenceGeneTable.getTableHeader().getFont().deriveFont(10.0f));
        final TableColumnModel referenceTableColumnModel = referenceGeneTable.getColumnModel();
        referenceTableColumnModel.getColumn(1).setPreferredWidth(65); // Index
        referenceTableColumnModel.getColumn(1).setMaxWidth(65); // Index
        referenceTableColumnModel.getColumn(2).setPreferredWidth(200);

        annotationTable = new JTable(new GeneAnnotationTableModel());
        annotationTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        annotationTable.setShowGrid(false);
        annotationTable.setAlignmentX(Component.LEFT_ALIGNMENT);
        annotationTable.setDefaultRenderer(NumberInRectangle.NumberIcon.class, new NumberIconRenderer());
        annotationTable.setDefaultRenderer(GeneFamily.class, new GeneRenderer());
        final TableColumnModel annotationTableColumnModel = annotationTable.getColumnModel();
        annotationTableColumnModel.getColumn(1).setPreferredWidth(50); // Index
        annotationTableColumnModel.getColumn(1).setMaxWidth(50); // Index
        annotationTableColumnModel.getColumn(2).setPreferredWidth(200);

        reset();

		update();
	}

    private void reset() {
        subsequences = new ArrayList<>();
        chromosomes = new ArrayList<>();
        genomeIndexMapping = new ArrayList<>();
        genomeIndexBackmap = new HashMap<>();

        geneList = new ArrayList<>();
        genomeIndexInGeneList = new ArrayList<>();
        geneIdAtTablePosition = new HashMap<>();
        includeSubOptimalOccurrences = false;
        subselections = null;
        referenceGeneList = new ArrayList<>();
    }

	@Override
	public void selectionChanged(ClusterSelectionEvent e) {
		if (! (e instanceof LocationSelectionEvent))
			return;
		LocationSelectionEvent l = (LocationSelectionEvent) e;

        GeckoInstance gecko = GeckoInstance.getInstance();

		cluster = l.getSelection();
        parameters = gecko.getParameters();

        reset();

		if (cluster!=null) {
			subselections = l.getSubselection();
            includeSubOptimalOccurrences = l.includeSubOptimalOccurrences();

            this.setGeneData();

            for (int i=0; i<subselections.length; i++){
                if (subselections[i] != GeneClusterOccurrence.GENOME_NOT_INCLUDED && cluster.getOccurrences(includeSubOptimalOccurrences).getSubsequences()[i][subselections[i]].isValid()) {
                    Subsequence subseq =  cluster.getOccurrences(includeSubOptimalOccurrences).getSubsequences()[i][subselections[i]];
                    subsequences.add(subseq);
                    chromosomes.add(gecko.getGenomes()[i].getChromosomes().get(subseq.getChromosome()));
                    genomeIndexBackmap.put(i, genomeIndexMapping.size());
                    genomeIndexMapping.add(i);
                }
            }
		}
		update();
	}

    private void setMaxLengthStringWidth(int idLength){
        if (maxLengthString == null || maxLengthString.length() != idLength){
            maxLengthString = GenomePainting.buildMaxLengthString(idLength);
        }
        geneWidth = GenomePainting.getGeneWidth(masterPanel.getGraphics(), maxLengthString, MultipleGenomesBrowser.DEFAULT_GENE_HEIGHT);
    }

	private void update() {
		masterPanel.removeAll();
		if (geneList.size() != 0) {

            /*
			 * Global data
			 */
            JLabel valTitle = getBoldLabel("Global cluster information:");
            masterPanel.add(valTitle);

            JPanel totalDistancePanel = generateGeneralGenomeInformationPanel("Best total distance: " + cluster.getOccurrences(includeSubOptimalOccurrences).getTotalDist());
            totalDistancePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            masterPanel.add(totalDistancePanel);

            JPanel bestScorePanel = generateGeneralGenomeInformationPanel("         Best score: " + cluster.getOccurrences(includeSubOptimalOccurrences).getBestScore());
            bestScorePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            masterPanel.add(bestScorePanel);

            masterPanel.add(Box.createVerticalStrut(5));

            /*
             * Used parameters
             */
            JLabel parameterLabel = getBoldLabel("Parameters used for computation:");
            masterPanel.add(parameterLabel);
            if (parameters == null)
                masterPanel.add(new JLabel("No Parameters available!"));
            else {
                JPanel parameterPanel = getParameterPanel(parameters);
                parameterPanel.setAlignmentX(LEFT_ALIGNMENT);
                masterPanel.add(parameterPanel);
            }
            masterPanel.add(Box.createVerticalStrut(5));

            /*
			 * Local distances to median/center
			 */
            JLabel distanceLabel;
            if (cluster.getType() == Parameter.OperationMode.median)
                distanceLabel = getBoldLabel("Distance to median per dataset:");
            else if (cluster.getType() == Parameter.OperationMode.center)
                distanceLabel = getBoldLabel("Distance to center set per dataset:");
            else
                distanceLabel = getBoldLabel("Distance to reference gene set per dataset:");
            masterPanel.add(distanceLabel);
            masterPanel.add(Box.createVerticalStrut(5));

            JPanel cpanel = generateChromosomeDistancePanel();
            cpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            masterPanel.add(cpanel);
            masterPanel.add(Box.createVerticalStrut(5));

            /*
             * Reference Genes
             */
            JLabel referenceGenes = getBoldLabel("Reference Genes:");
            masterPanel.add(referenceGenes);
            masterPanel.add(Box.createVerticalStrut(5));
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(referenceGeneTable.getTableHeader(), BorderLayout.NORTH);
            panel.add(referenceGeneTable, BorderLayout.CENTER);
            panel.setAlignmentX(LEFT_ALIGNMENT);
            masterPanel.add(panel);
            masterPanel.add(Box.createVerticalStrut(5));
			
			/*
			 * List of genes
			 */
            JLabel genesInClusterLabel = getBoldLabel("Genes in this Cluster:");
			masterPanel.add(genesInClusterLabel);
			masterPanel.add(Box.createVerticalStrut(10));
            masterPanel.add(annotationTable);
            masterPanel.add(Box.createVerticalStrut(5));

            /*
			 * Involved Chromosomes
			 */
            JLabel invChrTitle = getBoldLabel("Involved chromosomes");
            invChrTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            masterPanel.add(invChrTitle);

            masterPanel.add(Box.createVerticalStrut(5));

            masterPanel.add(chromosomeNameTable);
		}
		this.repaint();
		this.revalidate();
		this.getVerticalScrollBar().setValue(0);
        this.getHorizontalScrollBar().setValue(0);
	}

    private static JLabel getBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(boldFont);
        return label;
    }

    private static JLabel getMonoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(monoFont);
        return label;
    }

    private JPanel getParameterPanel(Parameter parameters) {
        if (parameters.useDeltaTable()) {
            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("d"));
            builder.append(getMonoLabel("Reference mode: " + parameters.getRefType()));
            if (parameters.searchRefInRef())
                builder.append("Ref. in Ref.");
            builder.append(getMonoLabel("Quorum: " + parameters.getQ()));
            builder.append(getMonoLabel("Distance Table:"));
            JTable deltaTable = new JTable(new DeltaTableModel(parameters.getDeltaTable(), parameters.getMinClusterSize()));
            builder.append(deltaTable.getTableHeader());
            builder.append(deltaTable);

            return builder.build();
        } else {
            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p"));
            builder.append(getMonoLabel("Reference mode: " + parameters.getRefType()));
            if (parameters.searchRefInRef())
                builder.append("Ref. in Ref.");
            builder.nextLine();
            builder.append(getMonoLabel("Max. Distance: " + parameters.getDelta()));
            builder.append(getMonoLabel("Min. Size: " + parameters.getMinClusterSize()));
            builder.append(getMonoLabel("Quorum: " + parameters.getQ()));
            return builder.build();
        }
    }

    private JPanel generateGeneralGenomeInformationPanel(String text) {
        JPanel cpanel = new JPanel();
        FlowLayout f = new FlowLayout(FlowLayout.LEFT);
        f.setVgap(1);
        cpanel.setLayout(f);
        cpanel.setBackground(masterPanel.getBackground());
        JLabel textLabel = getMonoLabel(text);
        cpanel.add(textLabel);
        return cpanel;
    }

    private JPanel generateChromosomeDistancePanel() {  //TODO smarter Object?
        JPanel cpanel = new JPanel();
        FlowLayout f = new FlowLayout(FlowLayout.LEFT);
        f.setVgap(1);
        cpanel.setLayout(f);
        cpanel.setBackground(masterPanel.getBackground());

        for (int i=0; i<subsequences.size(); i++) {
            cpanel.add(new NumberInRectangle(genomeIndexMapping.get(i)+1, getBackground(), chromosomes.get(i).getChromosomeMouseListener()));
            JLabel textLabel = getMonoLabel(Integer.toString(subsequences.get(i).getDist()));
            cpanel.add(textLabel);
            cpanel.add(Box.createHorizontalStrut(5));
        }
        return cpanel;
    }


    private void setGeneData() {
        Map<GeneFamily, Gene[]> annotations = cluster.generateAnnotations(includeSubOptimalOccurrences,
                subselections);
        setMaxLengthStringWidth(GeneCluster.getMaximumIdLength(annotations));

        this.setAnnotationTable(annotations);
        this.setReferenceTable(annotations);
    }

    private void setReferenceTable(Map<GeneFamily, Gene[]> annotations) {
        final TableColumnModel tableColumnModel = referenceGeneTable.getColumnModel();
        tableColumnModel.getColumn(0).setPreferredWidth(geneWidth);
        tableColumnModel.getColumn(0).setMaxWidth(geneWidth);

        List<Gene> genes = cluster.getGenes(includeSubOptimalOccurrences, cluster.getRefSeqIndex(), subselections[cluster.getRefSeqIndex()]);
        for (Gene gene : genes) {
            ReferenceGeneInfo info;
            if (gene.getGeneFamily().isSingleGeneFamily()){
                info = new ReferenceGeneInfo(gene.getGeneFamily(), 1, gene);
            } else {
                Gene[] geneArray = annotations.get(gene.getGeneFamily());
                int occsInGenomes = 0;
                if (geneArray != null) {
                    for (int i=0;i<geneArray.length;i++) {
                        if (geneArray[i] != null)
                            occsInGenomes++;
                    }
                } else {
                    occsInGenomes = 1;
                }
                info = new ReferenceGeneInfo(gene.getGeneFamily(), occsInGenomes, gene);
            }
            referenceGeneList.add(info);
        }
    }
    
    public static class ReferenceGeneInfo{
        public final GeneFamily geneFamily;
        public final int occsInGenomes;
        public final Gene refGene;

        private ReferenceGeneInfo(GeneFamily geneFamily, int occsInGenomes, Gene refGene) {
            this.geneFamily = geneFamily;
            this.occsInGenomes = occsInGenomes;
            this.refGene = refGene;
        }
    }

    private void setAnnotationTable(Map<GeneFamily, Gene[]> annotations) {
        final TableColumnModel annotationTableColumnModel = annotationTable.getColumnModel();
        annotationTableColumnModel.getColumn(0).setPreferredWidth(geneWidth);
        annotationTableColumnModel.getColumn(0).setMaxWidth(geneWidth);

        for (Map.Entry<GeneFamily, Gene[]> entry : annotations.entrySet()) {
            geneIdAtTablePosition.put(geneList.size(), entry.getKey());
            Gene[] genes = entry.getValue();

            for (int i=0;i<genes.length;i++) {
                if (genes[i] != null) {
                    geneList.add(genes[i]);
                    genomeIndexInGeneList.add(i);
                }
            }
        }
    }

    private class ChromosomeNameTableModel extends AbstractTableModel {
        private static final long serialVersionUID = -3306238610287868813L;

        private final Class<?>[] columns = {NumberInRectangle.NumberIcon.class, String.class};

        @Override
        public int getRowCount() {
            return chromosomes.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)	{
            return this.columns[columnIndex];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return new NumberInRectangle.NumberIcon(genomeIndexMapping.get(rowIndex)+1);
                case 1:
                    return chromosomes.get(rowIndex).getFullName();
                default:
                    return null;
            }
        }
    }

    private class ReferenceGeneTableModel extends AbstractTableModel {
        private final Class<?>[] columns = {GeneFamily.class, Integer.class, String.class};
        private final String[] tableHeaders = {"Gene", "#Genomes", "Annotation"};

        @Override
        public int getRowCount() {
            return referenceGeneList.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)	{
            return this.columns[columnIndex];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return referenceGeneList.get(rowIndex).geneFamily;
                case 1:
                    return referenceGeneList.get(rowIndex).occsInGenomes;
                case 2:
                    return referenceGeneList.get(rowIndex).refGene.getSummary();
                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int columnIndex) {
            return tableHeaders[columnIndex];
        }
    }

    private class GeneAnnotationTableModel extends AbstractTableModel {
        private static final long serialVersionUID = -3306238610287868813L;

        private final Class<?>[] columns = {GeneFamily.class, NumberInRectangle.NumberIcon.class, String.class};

        @Override
        public int getRowCount() {
            return geneList.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)	{
            return this.columns[columnIndex];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return geneIdAtTablePosition.get(rowIndex);
                case 1:
                    return new NumberInRectangle.NumberIcon(genomeIndexInGeneList.get(rowIndex)+1);
                case 2:
                    return geneList.get(rowIndex).getSummary();
                default:
                    return null;
            }
        }
    }

    private class DeltaTableModel extends AbstractTableModel {
        private final Class<?>[] columns = {Integer.class, Integer.class, Integer.class, Integer.class};
        private final String[] columnTitles = {"D_ADD","D_LOSS", "D_SUM", "Size"};

        List<int[]> deltaValues;

        public DeltaTableModel(int[][] values, int minSize) {
            super();
            deltaValues = new ArrayList<>();
            for (int i=minSize; i<values.length; i++) {
                if (values[i][0] != -1){
                    deltaValues.add(new int[]{values[i][0], values[i][1], values[i][2], i});
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public int getRowCount() {
            return deltaValues.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return deltaValues.get(rowIndex)[columnIndex];
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnTitles[columnIndex];
        }
    }

    private class NumberIconRenderer extends DefaultTableCellRenderer.UIResource {
        public NumberIconRenderer() {
            super();
            setHorizontalAlignment(JLabel.LEFT);
        }

        @Override
        public void setValue(Object value) {
            if (value instanceof NumberInRectangle.NumberIcon) {
                NumberInRectangle.NumberIcon numberIcon = (NumberInRectangle.NumberIcon)value;
                setIcon(numberIcon);
                setToolTipText(chromosomes.get(genomeIndexBackmap.get(numberIcon.getNumber() - 1)).getFullName());
            } else
                setIcon(null);
        }
    }

    private class GeneRenderer extends DefaultTableCellRenderer.UIResource {
        public GeneRenderer() {
            super();
            setHorizontalAlignment(JLabel.LEFT);
        }

        @Override
        public void setValue(Object value) {
            if (value instanceof GeneFamily) {
                GenomePainting.GeneIcon icon = new GenomePainting.GeneIcon((GeneFamily)value, geneWidth, MultipleGenomesBrowser.DEFAULT_GENE_HEIGHT);
                setIcon(icon);
            } else
                setIcon(null);
        }
    }
}
