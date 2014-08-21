package gecko2.gui;


import gecko2.GeckoInstance;
import gecko2.algorithm.*;
import gecko2.event.ClusterSelectionEvent;
import gecko2.event.ClusterSelectionListener;
import gecko2.event.LocationSelectionEvent;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GeneClusterDisplay extends JScrollPane implements ClusterSelectionListener {

	private static final long serialVersionUID = -2156280340296694286L;

    private static final Font monoFont = new Font("Monospaced",Font.PLAIN,new JLabel().getFont().getSize()-1);
    private static final Font boldFont = new JLabel().getFont().deriveFont(Font.BOLD);

	private final JPanel masterPanel;
	private final JPanel flowPanel;
	private GeneCluster cluster;
	private GeneClusterOccurrence gOcc;

    private String maxLengthString;
    private int geneWidth;

    private List<Subsequence> subsequences;
    private List<Chromosome> chromosomes;
    private List<Integer> genomeIndexMapping;
    private Map<Integer, Integer> genomeIndexBackmap;

    private List<Gene> geneList;
    private List<Integer> genomeIndexInGeneList;
    private Map<Integer, GeneFamily> geneIdAtTablePosition;

    // local?
    private final JTable chromosomeNameTable;
    private final JTable annotationTable;
    // end local?
	
	private static final String VALUES_TITLE = "Global cluster information:";

	private static final String GENES_TITLE = "Genes in this Cluster:";
	
	public GeneClusterDisplay() {
		flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		masterPanel = new JPanel();		
		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.PAGE_AXIS));
		masterPanel.setBackground(Color.WHITE);
		flowPanel.setBackground(masterPanel.getBackground());
		flowPanel.add(masterPanel);
		this.setViewportView(flowPanel);

        chromosomeNameTable = new JTable(new ChromosomeNameTableModel());
        chromosomeNameTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        chromosomeNameTable.setShowGrid(false);
        chromosomeNameTable.setAlignmentX(Component.LEFT_ALIGNMENT);
        chromosomeNameTable.setDefaultRenderer(NumberInRectangle.NumberIcon.class, new NumberIconRenderer());
        final TableColumnModel chromosomeNameTableColumnModel = chromosomeNameTable.getColumnModel();
        chromosomeNameTableColumnModel.getColumn(0).setPreferredWidth(50); // Index
        chromosomeNameTableColumnModel.getColumn(0).setMaxWidth(50); // Index
        chromosomeNameTableColumnModel.getColumn(1).setPreferredWidth(200);

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
    }

	@Override
	public void selectionChanged(ClusterSelectionEvent e) {
		if (! (e instanceof LocationSelectionEvent))
			return;
		LocationSelectionEvent l = (LocationSelectionEvent) e;

        GeckoInstance gecko = GeckoInstance.getInstance();

		this.cluster = l.getSelection();

        reset();

		if (this.cluster!=null) {
			int[] subselections = l.getsubselection();
            this.gOcc = l.getgOcc();

            Map<GeneFamily, Gene[]> annotations = l.getSelection().generateAnnotations(l.getgOcc(),
                    l.getsubselection());

            setMaxLengthStringWidth(GeneCluster.getMaximumIdLength(annotations));
            final TableColumnModel annotationTableColumnModel = annotationTable.getColumnModel();
            annotationTableColumnModel.getColumn(0).setPreferredWidth(geneWidth); // Index
            annotationTableColumnModel.getColumn(0).setMaxWidth(geneWidth); // Index

            this.setAnnotationData(annotations);

            for (int i=0; i<subselections.length; i++){
                if (subselections[i] != GeneClusterOccurrence.GENOME_NOT_INCLUDED && gOcc.getSubsequences()[i][subselections[i]].isValid()) {
                    Subsequence subseq =  gOcc.getSubsequences()[i][subselections[i]];
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
        geneWidth = GenomePainting.getGeneWidth(masterPanel.getGraphics(), maxLengthString, GeckoInstance.DEFAULT_GENE_HIGHT);
    }

	private void update() {
		masterPanel.removeAll();
		if (geneList.size() != 0) {

			/*
			 * Involed Chromosomes
			 */
			JLabel invChrTitle = new JLabel("Involved chromosomes");
			invChrTitle.setFont(boldFont);
            invChrTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
			masterPanel.add(invChrTitle);

            masterPanel.add(Box.createVerticalStrut(5));

            masterPanel.add(chromosomeNameTable);
			masterPanel.add(Box.createVerticalStrut(5));
			
			/*
			 * Global data
			 */
			JPanel title1 = new JPanel();
			title1.setBackground(masterPanel.getBackground());
			title1.setLayout(new BoxLayout(title1,BoxLayout.X_AXIS));
			JLabel valTitle = new JLabel(VALUES_TITLE);
			valTitle.setFont(boldFont);
			title1.add(valTitle);
			title1.add(Box.createHorizontalGlue());
            title1.setAlignmentX(Component.LEFT_ALIGNMENT);
			masterPanel.add(title1);

            JPanel totalDistancePanel = generateGeneralGenomeInformationPanel("Best total distance: " + gOcc.getTotalDist());
            totalDistancePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            masterPanel.add(totalDistancePanel);

            JPanel bestScorePanel = generateGeneralGenomeInformationPanel("         Best score: " + gOcc.getBestScore());
            bestScorePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            masterPanel.add(bestScorePanel);

			masterPanel.add(Box.createVerticalStrut(5));

			/*
			 * Local distances to median/center
			 */
			JPanel title3 = new JPanel();
			title3.setBackground(masterPanel.getBackground());
			title3.setLayout(new BoxLayout(title3,BoxLayout.X_AXIS));

            JLabel distanceLabel = new JLabel();
            if (cluster.getType() == Parameter.OperationMode.median)
                distanceLabel.setText("Distance to median per dataset:");
            else if (cluster.getType() == Parameter.OperationMode.center)
                distanceLabel.setText("Distance to center set per dataset:");
            else
                distanceLabel.setText("Distance to reference gene set per dataset:");
            distanceLabel.setFont(boldFont);
            title3.add(distanceLabel);

			title3.add(Box.createHorizontalGlue());
            title3.setAlignmentX(Component.LEFT_ALIGNMENT);
			masterPanel.add(title3);
			masterPanel.add(Box.createVerticalStrut(5));

			JPanel cpanel = generateChromosomeDistancePanel();
            cpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            masterPanel.add(cpanel);
			masterPanel.add(Box.createVerticalStrut(5));
			
			/*
			 * List of genes
			 */
			JPanel title2 = new JPanel();
			title2.setBackground(masterPanel.getBackground());
			title2.setLayout(new BoxLayout(title2,BoxLayout.X_AXIS));

            TextLabel label = new TextLabel(GENES_TITLE);
            label.setFont(boldFont);
            title2.add(label);
			
			title2.add(Box.createHorizontalGlue());

            title2.setAlignmentX(Component.LEFT_ALIGNMENT);
			masterPanel.add(title2);
			masterPanel.add(Box.createVerticalStrut(10));

            masterPanel.add(annotationTable);

			masterPanel.add(Box.createHorizontalGlue());
		}
		this.repaint();
		this.revalidate();
		this.getVerticalScrollBar().setValue(0);
        this.getHorizontalScrollBar().setValue(0);
	}

    private JPanel generateGeneralGenomeInformationPanel(String text) {
        JPanel cpanel = new JPanel();
        FlowLayout f = new FlowLayout(FlowLayout.LEFT);
        f.setVgap(1);
        cpanel.setLayout(f);
        cpanel.setBackground(masterPanel.getBackground());
        TextLabel textLabel = new TextLabel(text);
        textLabel.setFont(monoFont);
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
            TextLabel textLabel = new TextLabel(Integer.toString(subsequences.get(i).getDist()));
            textLabel.setFont(monoFont);
            cpanel.add(textLabel);
            cpanel.add(Box.createHorizontalStrut(5));
        }
        return cpanel;
    }

    private void setAnnotationData(Map<GeneFamily, Gene[]> annotations) {
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
	
	private class TextLabel extends JLabel {
		/**
		 * Random generated serialization UID
		 */
		private static final long serialVersionUID = -741356610359230027L;

		public TextLabel(String s) {
			super(s);
			setBorder(null);
			setBackground(Color.WHITE);
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
                GenomePainting.GeneIcon icon = new GenomePainting.GeneIcon((GeneFamily)value, geneWidth, GeckoInstance.DEFAULT_GENE_HIGHT);
                setIcon(icon);
            } else
                setIcon(null);
        }
    }
}
