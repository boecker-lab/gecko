package gecko2.gui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import gecko2.GeckoInstance;
import gecko2.algorithm.*;
import gecko2.util.PrintUtils;
import gecko2.util.SortUtils;

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
	private Parameter.OperationMode opMode;
    private Parameter.ReferenceType refType;
	private final JComboBox refCombo;
	private final JCheckBox mergeResults;
	private final GeckoInstance gecko = GeckoInstance.getInstance();

	public StartComputationDialog(int ngenomes) {
		this.setModal(true);
		this.setResizable(false);
		this.setIconImage(Gui.createImageIcon("images/gecko2_a_small.png").getImage());
		this.setTitle("Configure computation");
		
		JPanel panel = new JPanel(new FlowLayout());
		panel.setPreferredSize(new Dimension(430,310));
		final JPanel gridpanel = new JPanel();
		gridpanel.setPreferredSize(new Dimension(410,250));
		gridpanel.setLayout(new GridLayout(7,2));
		
		final JSpinner dSpinner = new JSpinner(new SpinnerNumberModel(3, 0, Integer.MAX_VALUE, 1));
		dSpinner.setPreferredSize(new Dimension(150,30));
		
		final JSpinner sSpinner = new JSpinner(new SpinnerNumberModel(7, 0, Integer.MAX_VALUE, 1));
		sSpinner.setPreferredSize(new Dimension(150,30));

        final JSpinner groupSpinner = new JSpinner(new SpinnerNumberModel(1.1, 0.0, 1.1, 0.1));
        sSpinner.setPreferredSize(new Dimension(150,30));
		
		final String[] qValues = new String[ngenomes-1];
		qValues[qValues.length-1] = "all";
		for (int i=2;i<ngenomes;i++)
			qValues[i-2] = Integer.toString(i);
		final JComboBox qCombo = new JComboBox(qValues);
		qCombo.setSelectedIndex(qValues.length-1);
		qCombo.setPreferredSize(new Dimension(190,30));
		qCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (qCombo.getSelectedIndex()==qValues.length-1)
					quorum = 0;
				else
					quorum = qCombo.getSelectedIndex()+2;
			}
		});

		final JComboBox modeCombo = new JComboBox(Parameter.OperationMode.values());

		modeCombo.setPreferredSize(new Dimension(190,30));

		this.opMode = Parameter.OperationMode.reference;
		this.refType = Parameter.ReferenceType.allAgainstAll;
		modeCombo.setSelectedIndex(0);

        JLabel refLabel = new JLabel("Reference:");
		refCombo = new JComboBox(Parameter.ReferenceType.values());
		refCombo.setPreferredSize(new Dimension(190, 30));
		
		modeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                opMode = (Parameter.OperationMode)modeCombo.getSelectedItem();
			}
		});
		
		mergeResults = new JCheckBox("Merge Results");
		mergeResults.setSelected(false);
		
		JPanel p1a = new JPanel(new FlowLayout(FlowLayout.LEFT));
		((FlowLayout) p1a.getLayout()).setVgap(12);
		JLabel l1 = new JLabel("Maximum distance: ");
		p1a.add(l1);
		gridpanel.add(p1a);
		JPanel p1b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p1b.add(dSpinner);
		gridpanel.add(p1b);
		
		
		JPanel p2a = new JPanel(new FlowLayout(FlowLayout.LEFT));
		((FlowLayout) p2a.getLayout()).setVgap(12);
		p2a.add(new JLabel("Minimum cluster size: "));
		gridpanel.add(p2a);
		JPanel p2b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p2b.add(sSpinner);
		gridpanel.add(p2b);
		
		JPanel p3a = new JPanel(new FlowLayout(FlowLayout.LEFT));
		((FlowLayout) p3a.getLayout()).setVgap(12);
		p3a.add(new JLabel("Minimum number of genomes: "));
		gridpanel.add(p3a);
		JPanel p3b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p3b.add(qCombo);
		gridpanel.add(p3b);
		
		JPanel p4a = new JPanel(new FlowLayout(FlowLayout.LEFT));
		((FlowLayout) p4a.getLayout()).setVgap(12);
		p4a.add(new JLabel("Search mode: "));
		gridpanel.add(p4a);
		JPanel p4b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p4b.add(modeCombo);
		gridpanel.add(p4b);
		
		JPanel p5a = new JPanel(new FlowLayout(FlowLayout.LEFT));
		((FlowLayout) p5a.getLayout()).setVgap(12);
		p5a.add(refLabel);
		gridpanel.add(p5a);
		JPanel p5b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p5b.add(refCombo);
		gridpanel.add(p5b);
		
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
		
		EventList<Genome> genomeEventList = new BasicEventList<Genome>();
		genomeEventList.addAll(Arrays.asList(GeckoInstance.getInstance().getGenomes()));
		
		final JComboBox refGenomeCombo = new JComboBox();
		AutoCompleteSupport.install(refGenomeCombo, genomeEventList);
		refGenomeCombo.setSelectedIndex(0);
		
		
		refGenomeCombo.setPreferredSize(new Dimension(190, 30));
		refClusterField.setPreferredSize(new Dimension(190, 30));
		
		
		JPanel p6a = new JPanel(new FlowLayout(FlowLayout.LEFT));
		((FlowLayout) p6a.getLayout()).setVgap(12);
		p6a.add(mergeResults);
		gridpanel.add(p6a);
		final JPanel p6b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		gridpanel.add(p6b);

        final JPanel p7a = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ((FlowLayout) p6a.getLayout()).setVgap(12);
        p7a.add(new JLabel("Genome Grouping Factor: "));
        gridpanel.add(p7a);
        final JPanel p7b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p7b.add(groupSpinner);
        gridpanel.add(p7b);
		
		panel.add(gridpanel);
		
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
	
		
		ActionListener refGenomeComboListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
                refType = (Parameter.ReferenceType) refCombo.getSelectedItem();
                switch (refType) {
                    case genome:
                        p6b.remove(refClusterField);
					    p6b.add(refGenomeCombo);
                        break;
                    case cluster:
                        p6b.remove(refGenomeCombo);
                        p6b.add(refClusterField);
					    ToolTipManager.sharedInstance().mouseMoved(
					        new MouseEvent(refClusterField, 0, 0, 0,
					                (int) refClusterField.getLocation().getX(), // X-Y of the mouse for the tool tip
					                (int) refClusterField.getLocation().getY(),
					                0, false));
                        break;
                    default:
                        p6b.remove(refClusterField);
					    p6b.remove(refGenomeCombo);
                }
				p6b.validate();
				gridpanel.validate();
				
				StartComputationDialog.this.repaint();
			}
		};
		refCombo.addActionListener(refGenomeComboListener);
		
		JPanel lowerpanel = new JPanel();
		lowerpanel.setLayout(new BoxLayout(lowerpanel,BoxLayout.X_AXIS));
		lowerpanel.setPreferredSize(new Dimension(300,50));
		Action okAction = new AbstractAction() {
		
			private static final long serialVersionUID = 6197096728152587585L;
			public void actionPerformed(ActionEvent e) {
				StartComputationDialog.this.setVisible(false);
				// Reorder the genomes if necessary
				if (opMode==Parameter.OperationMode.reference && refType==Parameter.ReferenceType.genome && refGenomeCombo.getSelectedIndex()!=0) {
					PrintUtils.printDebug("swapping genomes");
					Genome[] genomes = GeckoInstance.getInstance().getGenomes();
					Genome first = genomes[0];
					genomes[0] = genomes[refGenomeCombo.getSelectedIndex()];
					genomes[refGenomeCombo.getSelectedIndex()] = first;
					
					gecko.getGui().closeCurrentSession();
					gecko.setGenomes(genomes);
					//TODO improve
				} else if (opMode==Parameter.OperationMode.reference && refType==Parameter.ReferenceType.cluster) {
					Genome[] oldGenomes = gecko.getGenomes();
					Genome[] genomes = new Genome[oldGenomes.length+1];
					Genome cluster = new Genome();
					ArrayList<Gene> genes = new ArrayList<Gene>();
					Map<ExternalGeneId, Integer> revIDMap = SortUtils.invertIntArray(gecko.getGenLabelMap());
					for (String id : refClusterField.getText().split(" "))
						if (id!=null && (!(id.equals("")))) {
							Integer iid = revIDMap.get(Integer.parseInt(id)); //TODO contains strings, should not work!
							if (iid!=null)
								genes.add(new Gene("", iid));
						}
					cluster.getChromosomes().add(new Chromosome("Reference cluster", genes, cluster));
					genomes[0] = cluster;
                    System.arraycopy(oldGenomes, 0, genomes, 1, oldGenomes.length);
					gecko.getGui().closeCurrentSession();
					gecko.setGenomes(genomes);
				}
				boolean mergeResultsEnabled = false;
				if (opMode==Parameter.OperationMode.reference && mergeResults.isSelected())
					mergeResultsEnabled = true;
				GeckoInstance.getInstance().performClusterDetection(new Parameter((Integer) dSpinner.getValue(), 
						(Integer) sSpinner.getValue(),
						quorum,
						Parameter.QUORUM_NO_COST, 
						opMode,
						refType),
						mergeResultsEnabled,
                        (Double)groupSpinner.getValue());
			}
			
		};
		okAction.putValue(Action.NAME, "OK");
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
		lowerpanel.add(cancelButton);
		lowerpanel.add(okButton);
		
		panel.add(lowerpanel);
		this.add(panel);
	
		this.pack();
	}

}
