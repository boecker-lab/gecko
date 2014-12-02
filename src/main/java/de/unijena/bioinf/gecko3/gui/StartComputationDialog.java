package de.unijena.bioinf.gecko3.gui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.*;
import de.unijena.bioinf.gecko3.util.PrintUtils;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class StartComputationDialog extends JDialog {
	private static final long serialVersionUID = -5635614016950101153L;

    static final int COMBO_HEIGHT = 30;
    static final int COMBO_WIDTH = 180;
    static final int V_GAP = 1;
    static final int H_GAP = 1;

	private int quorum;
	private Parameter.OperationMode opMode;
    private Parameter.ReferenceType refType;
	private final JComboBox<Parameter.ReferenceType> refCombo;
	private final JCheckBox mergeResults;
    private final JSpinner distanceSpinner;
    private final JSpinner sizeSpinner;

	public StartComputationDialog() {
        final GeckoInstance gecko = GeckoInstance.getInstance();
		this.setModal(true);
		this.setResizable(false);
		this.setIconImage(Gui.createImageIcon("images/gecko3_a_small.png").getImage());
		this.setTitle("Configure computation");

        this.opMode = Parameter.OperationMode.reference;
        this.refType = Parameter.ReferenceType.allAgainstAll;
        this.distanceSpinner = new JSpinner(new SpinnerNumberModel(3, 0, Integer.MAX_VALUE, 1));
        this.sizeSpinner = new JSpinner(new SpinnerNumberModel(7, 0, Integer.MAX_VALUE, 1));

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panel.setPreferredSize(new Dimension(430,400));

        final JTabbedPane tabbedDistancePane = new JTabbedPane();
        tabbedDistancePane.addTab("Single Distance", getDistancePanel(new Dimension(410, 6*COMBO_HEIGHT)));
        tabbedDistancePane.add("Distance Table", new DeltaTable(new Dimension(410, 6*COMBO_HEIGHT)));
        panel.add(tabbedDistancePane);

		final JPanel gridPanel = new JPanel();
		gridPanel.setPreferredSize(new Dimension(410, 5*(COMBO_HEIGHT+V_GAP)));
        GridLayout gridLayout = new GridLayout(5, 2, H_GAP, V_GAP);
		gridPanel.setLayout(gridLayout);

        sizeSpinner.setPreferredSize(new Dimension(COMBO_WIDTH, COMBO_HEIGHT));

        //final JSpinner groupSpinner = new JSpinner(new SpinnerNumberModel(1.1, 0.0, 1.1, 0.1));
        //groupSpinner.setPreferredSize(new Dimension(COMBO_WIDTH, COMBO_HEIGHT));

        final JComboBox<Parameter.OperationMode> modeCombo = new JComboBox<>(Parameter.OperationMode.getSupported());
        modeCombo.setPreferredSize(new Dimension(COMBO_WIDTH, COMBO_HEIGHT));
        modeCombo.setSelectedIndex(0);

		final String[] qValues = new String[gecko.getGenomes().length-1];
		qValues[Math.max(qValues.length-1, 0)] = "all";
		for (int i=2;i<gecko.getGenomes().length;i++)
			qValues[i-2] = Integer.toString(i);
		final JComboBox<String> qCombo = new JComboBox<>(qValues);
		qCombo.setSelectedIndex(qValues.length-1);
		qCombo.setPreferredSize(new Dimension(COMBO_WIDTH, COMBO_HEIGHT));

        refCombo = new JComboBox<>(Parameter.ReferenceType.getSupported());
        refCombo.setPreferredSize(new Dimension(COMBO_WIDTH, COMBO_HEIGHT));

        mergeResults = new JCheckBox("Merge Results");
        mergeResults.setPreferredSize(new Dimension(100, COMBO_HEIGHT));
        mergeResults.setSelected(false);

		JLabel minimumNumberOfGenomesLabel = new JLabel("Minimum # of genomes: ", JLabel.LEFT);
		gridPanel.add(minimumNumberOfGenomesLabel);
		gridPanel.add(qCombo);

		JLabel searchModeLabel = new JLabel("Search mode: ", JLabel.LEFT);
        gridPanel.add(searchModeLabel);
		gridPanel.add(modeCombo);


        JLabel refLabel = new JLabel("Reference:", JLabel.LEFT);
        gridPanel.add(refLabel);
		gridPanel.add(refCombo);

		final JTextField refClusterField = new JTextField() {

			/**
			 * Random generated serilization UID
			 */
			private static final long serialVersionUID = 8768959243069148651L;

			@Override
			public Point getToolTipLocation(MouseEvent event) {
				Point p = this.getLocation();
				p.y = p.y + getHeight();
				return p;
			}

		};

		EventList<Genome> genomeEventList = new BasicEventList<>();
		genomeEventList.addAll(Arrays.asList(GeckoInstance.getInstance().getGenomes()));

		final JComboBox refGenomeCombo = new JComboBox();
		AutoCompleteSupport.install(refGenomeCombo, genomeEventList);
		refGenomeCombo.setSelectedIndex(0);

		refGenomeCombo.setPreferredSize(new Dimension(COMBO_WIDTH, COMBO_HEIGHT));
		refClusterField.setPreferredSize(new Dimension(COMBO_WIDTH, COMBO_HEIGHT));

		gridPanel.add(mergeResults);
		final JPanel additionalRefClusterSettings = new JPanel(new CardLayout());
        JPanel emptyCard = new JPanel();
        additionalRefClusterSettings.add(emptyCard, Parameter.ReferenceType.allAgainstAll.toString());
        additionalRefClusterSettings.add(refGenomeCombo, Parameter.ReferenceType.genome.toString());
        additionalRefClusterSettings.add(refClusterField, Parameter.ReferenceType.cluster.toString());
		gridPanel.add(additionalRefClusterSettings);

        JLabel genomeGroupingLabel = new JLabel("Genome Grouping Factor: ", JLabel.LEFT);
        gridPanel.add(genomeGroupingLabel);
        //gridPanel.add(groupSpinner);

		panel.add(gridPanel);

        // Actions
        qCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (qCombo.getSelectedIndex()==qValues.length-1)
                    quorum = 0;
                else
                    quorum = qCombo.getSelectedIndex()+2;
            }
        });

        modeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                opMode = (Parameter.OperationMode)modeCombo.getSelectedItem();
            }
        });

		refCombo.setEnabled(true);
		mergeResults.setEnabled(true);
		ActionListener modeActionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (modeCombo.getSelectedIndex()==0) {
					refCombo.setEnabled(true);
					mergeResults.setEnabled(true);
				} else {
					refCombo.setEnabled(false);
					mergeResults.setEnabled(false);
				}
			}
		};
		modeCombo.addActionListener(modeActionListener);

		refClusterField.setToolTipText("<html>Enter a sequence of gene IDs here, separated by<br>" +
				"spaces.<br><br>" +
				"<B>Hint</B>:<br>Instead of entering a sequence of genes by<br>" +
				"hand you can also select a gene cluster from<br>" +
				"the result list and copy it!</html>");
		
		Document refClusterFieldDocument = new PlainDocument(){
			
			/**
			 * Random generated serialization UID
			 */
			private static final long serialVersionUID = -1398119324023579537L;

			@Override
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException {
				if (!str.matches("^[ \\d]*$")) return;
				super.insertString(offs, str, a);
			}
			
		};
		
		refClusterField.setDocument(refClusterFieldDocument);

		refCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refType = (Parameter.ReferenceType) refCombo.getSelectedItem();
                CardLayout layout = (CardLayout)(additionalRefClusterSettings.getLayout());
                layout.show(additionalRefClusterSettings, refType.toString());
                if (refType.equals(Parameter.ReferenceType.cluster)){
                    ToolTipManager.sharedInstance().mouseMoved(
                            new MouseEvent(refClusterField, 0, 0, 0,
                                    (int) refClusterField.getLocation().getX(), // X-Y of the mouse for the tool tip
                                    (int) refClusterField.getLocation().getY(),
                                    0, false));
                }
            }
        });
		
		JPanel lowerpanel = new JPanel();
		lowerpanel.setLayout(new BoxLayout(lowerpanel,BoxLayout.X_AXIS));
        lowerpanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lowerpanel.setPreferredSize(new Dimension(300,50));
		Action okAction = new AbstractAction("OK") {
		
			private static final long serialVersionUID = 6197096728152587585L;
			public void actionPerformed(ActionEvent e) {
                Parameter parameter;
                if (tabbedDistancePane.getSelectedIndex() == 0) {
                    parameter = new Parameter(
                            (Integer) distanceSpinner.getValue(),
                            (Integer) sizeSpinner.getValue(),
                            quorum,
                            opMode,
                            refType);
                } else {
                    DeltaTable deltaTable = (de.unijena.bioinf.gecko3.gui.DeltaTable)tabbedDistancePane.getSelectedComponent();
                    if (!deltaTable.isValidDeltaTable()) {
                        JOptionPane.showMessageDialog(StartComputationDialog.this, "Invalid Distance Table!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        parameter = new Parameter(
                                deltaTable.getDeltaTable(),
                                deltaTable.getMinimumClusterSize(),
                                quorum,
                                opMode,
                                refType);
                    }
                }

				StartComputationDialog.this.setVisible(false);
				// Reorder the genomes if necessary
				if (opMode==Parameter.OperationMode.reference && refType==Parameter.ReferenceType.genome && refGenomeCombo.getSelectedIndex()!=0) {
					PrintUtils.printDebug("swapping genomes");
                    gecko.reorderGenomes(refGenomeCombo.getSelectedIndex());
				} else if (opMode==Parameter.OperationMode.reference && refType==Parameter.ReferenceType.cluster) {
					Genome cluster = new Genome();
					ArrayList<Gene> genes = new ArrayList<>();
					Map<String, GeneFamily> revIDMap = gecko.getGeneLabelMap();
					for (String id : refClusterField.getText().split(" "))
						if (id!=null && (!(id.equals("")))) {
							GeneFamily geneFamily = revIDMap.get(id);
							if (geneFamily!=null)
								genes.add(new Gene(geneFamily));
						}
					cluster.getChromosomes().add(new Chromosome("Reference cluster", genes, cluster));
					gecko.addReferenceGenome(cluster);
				}
				boolean mergeResultsEnabled = false;
				if (opMode==Parameter.OperationMode.reference && mergeResults.isSelected())
					mergeResultsEnabled = true;

				gecko.performClusterDetection(
                        parameter,
                        mergeResultsEnabled,
                        1.1);
			}
			
		};
		JButton okButton = new JButton(okAction);
		Action cancelAction = new AbstractAction() {
			private static final long serialVersionUID = 2057638030083370800L;
			public void actionPerformed(ActionEvent e) {
				StartComputationDialog.this.setVisible(false);
			}
			
		};
		
		panel.getActionMap().put("okAction", okAction);
		panel.getActionMap().put("cancelAction", cancelAction);
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "okAction");
		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelAction");

		cancelAction.putValue(Action.NAME, "Cancel");
		JButton cancelButton = new JButton(cancelAction);
		lowerpanel.add(Box.createHorizontalGlue());
		lowerpanel.add(okButton);
		lowerpanel.add(cancelButton);
		
		panel.add(lowerpanel);
		this.add(panel);
	
		this.pack();
	}

    private JPanel getDistancePanel(Dimension dimension) {
        JPanel distancePanel = new JPanel(new GridLayout(2, 2, H_GAP, V_GAP));
        distancePanel.setPreferredSize(new Dimension((int)dimension.getWidth(), 2*(COMBO_HEIGHT+V_GAP)));
        distanceSpinner.setPreferredSize(new Dimension(COMBO_WIDTH, COMBO_HEIGHT));
        JLabel distanceLabel = new JLabel("Maximum distance: ", JLabel.LEFT);
        distancePanel.add(distanceLabel);
        distancePanel.add(distanceSpinner);

        JLabel sizeLabel = new JLabel("Minimum cluster size: ", JLabel.LEFT);
        distancePanel.add(sizeLabel);
        distancePanel.add(sizeSpinner);

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(distancePanel, constraints);
        panel.add(distancePanel);

        return panel;
    }

}
