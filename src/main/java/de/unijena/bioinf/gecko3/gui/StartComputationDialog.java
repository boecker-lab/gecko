package de.unijena.bioinf.gecko3.gui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
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

    private int quorum;
    private final JComboBox<String> qCombo;
	private Parameter.OperationMode opMode;
    private Parameter.ReferenceType refType;
	private final JComboBox<Parameter.ReferenceType> refCombo;
    private final JPanel additionalRefClusterSettings;

	private final JCheckBox mergeResults;
    private final JCheckBox refInRef;

    private final DeltaTable deltaTable;
    private final JSpinner distanceSpinner;
    private final JSpinner sizeSpinner;

	public StartComputationDialog(JFrame parent) {
        final GeckoInstance gecko = GeckoInstance.getInstance();
		this.setModalityType(DEFAULT_MODALITY_TYPE);
		this.setResizable(false);
        setIconImages(parent.getIconImages());
		this.setTitle("Configure computation");

        this.opMode = Parameter.OperationMode.reference;
        this.refType = Parameter.ReferenceType.allAgainstAll;

        this.sizeSpinner = new JSpinner(new SpinnerNumberModel(7, 0, Integer.MAX_VALUE, 1));
        this.distanceSpinner = new JSpinner(new SpinnerNumberModel(3, 0, Integer.MAX_VALUE, 1));
        this.deltaTable = new DeltaTable();

        /*
         * Tabbed distance and size pane
         */
        final JTabbedPane tabbedDistancePane = new JTabbedPane();
        tabbedDistancePane.addTab("Single Distance", getDistancePanel());
        tabbedDistancePane.add("Distance Table", deltaTable);

        /*
         * All other options
         */
		final String[] qValues = new String[gecko.getGenomes().length-1];
		qValues[Math.max(qValues.length-1, 0)] = "all";
		for (int i=2;i<gecko.getGenomes().length;i++)
			qValues[i-2] = Integer.toString(i);
		qCombo = new JComboBox<>(qValues);
		qCombo.setSelectedIndex(qValues.length-1);

        refCombo = new JComboBox<>(Parameter.ReferenceType.getSupported());

        mergeResults = new JCheckBox("Merge Results");
        mergeResults.setSelected(false);

        refInRef = new JCheckBox("Search Ref. in Ref.");
        refInRef.setSelected(false);

        /*
         * Ref cluster options
         */
        final JTextField refClusterField = new JTextField() {
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

        additionalRefClusterSettings = new JPanel(new CardLayout());
        additionalRefClusterSettings.add(new JPanel(), Parameter.ReferenceType.allAgainstAll.toString());
        additionalRefClusterSettings.add(refGenomeCombo, Parameter.ReferenceType.genome.toString());
        additionalRefClusterSettings.add(refClusterField, Parameter.ReferenceType.cluster.toString());

        /*
         * layout of other options
         */

        // Actions
        qCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (qCombo.getSelectedIndex()==qValues.length-1)
                    quorum = 0;
                else
                    quorum = qCombo.getSelectedIndex()+2;
            }
        });

		refCombo.setEnabled(true);
		mergeResults.setEnabled(true);

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
                    if (!deltaTable.isValidDeltaTable()) {
                        JOptionPane.showMessageDialog(StartComputationDialog.this, "Invalid Distance Table!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        parameter = new Parameter(
                                deltaTable.getDeltaTable(),
                                deltaTable.getClusterSize(),
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
		cancelAction.putValue(Action.NAME, "Cancel");
		JButton cancelButton = new JButton(cancelAction);
        JPanel buttonPanel = new ButtonBarBuilder().addButton(okButton, cancelButton).build();

        FormLayout layout = new FormLayout("default", "default:grow, default, default");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.append(tabbedDistancePane);
        builder.nextLine();
        builder.append(getBody());
        builder.nextLine();
        builder.append(buttonPanel);

        JPanel panel = builder.build();
        panel.getActionMap().put("okAction", okAction);
        panel.getActionMap().put("cancelAction", cancelAction);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "okAction");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelAction");

        this.setContentPane(panel);
		this.pack();
	}

    private JComponent getBody(){
        FormLayout layout = new FormLayout(
                "pref, 4dlu, pref",
                "p, 2dlu, p, 2dlu, p, 2dlu, p"
        );
        PanelBuilder builder  = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();

        builder.addLabel("Minimum # of genomes:", cc.xy(1, 1));
        builder.add(qCombo,                         cc.xy(3, 1));

        builder.addLabel("Reference type:",         cc.xy(1, 3));
        builder.add(refCombo,                       cc.xy(3, 3));

        builder.add(additionalRefClusterSettings,   cc.xyw(1, 5, 3));

        //builder.add(mergeResults, cc.xy(1, 7));
        builder.add(refInRef,                       cc.xy(1, 7));

        return builder.getPanel();
    }

    private JPanel getDistancePanel() {
        FormLayout layout = new FormLayout(
                "pref, 4dlu, pref",
                ""
        );
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);

        builder.append("Maximum distance:", distanceSpinner);
        builder.append("Minimum cluster size:", sizeSpinner);

        return builder.getPanel();
    }

}
