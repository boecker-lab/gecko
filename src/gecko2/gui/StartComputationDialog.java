package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Chromosome;
import gecko2.algorithm.Gene;
import gecko2.algorithm.Genome;
import gecko2.algorithm.Parameter;
import gecko2.util.PrintUtils;
import gecko2.util.SortUtils;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


public class StartComputationDialog extends JDialog {

	private static final long serialVersionUID = -5635614016950101153L;
	private int quorum;
	private char opMode,refType;
	private JComboBox refCombo;
	private JLabel refLabel;
	private GeckoInstance gecko = GeckoInstance.getInstance();

	public StartComputationDialog(int ngenomes) {
		this.setModal(true);
		this.setResizable(false);
		JPanel panel = new JPanel(new FlowLayout());
		panel.setPreferredSize(new Dimension(430,280));
		final JPanel gridpanel = new JPanel();
		gridpanel.setPreferredSize(new Dimension(410,220));
		gridpanel.setLayout(new GridLayout(6,2));
		
		final JSpinner dSpinner = new JSpinner(new SpinnerNumberModel(3, 0, Integer.MAX_VALUE, 1));
		dSpinner.setPreferredSize(new Dimension(150,30));
		
		final JSpinner sSpinner = new JSpinner(new SpinnerNumberModel(7, 0, Integer.MAX_VALUE, 1));
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

		
		final String[] modes = {"median", "center", "reference"};
		final JComboBox modeCombo = new JComboBox(modes);

		modeCombo.setPreferredSize(new Dimension(190,30));

		this.opMode = 'm';
		modeCombo.setSelectedIndex(0);
		
		final String[] refModes = {"all against all", "fixed genome", "manual cluster"};
		refLabel = new JLabel("Reference:");
		refCombo = new JComboBox(refModes);
		refCombo.setPreferredSize(new Dimension(190, 30));
		
		modeCombo.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				switch (modeCombo.getSelectedIndex()) {
				case 0:
					opMode = 'm';
					PrintUtils.printDebug("Using MEDIAN mode!");
					break;
				case 1:
					opMode = 'c';
					PrintUtils.printDebug("Using CENTER mode!");
					break;
				default:
					opMode = 'r';
					PrintUtils.printDebug("Using REFERENCE mode!");				
				}
			}
		});
		
		
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
		
		JLabel refValueLabel = new JLabel();
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
		
		Genome[] genomes = GeckoInstance.getInstance().getGenomes();
		String[] revGenomes = new String[genomes.length];
		for (int i=0;i<revGenomes.length;i++) {
			revGenomes[i] = genomes[i].getChromosomes().get(0).getName();
			if (genomes[i].getChromosomes().size()>1)
				revGenomes[i] = revGenomes[i]+" [and more...]";
		}
		
		final JComboBox refGenomeCombo = new JComboBox(revGenomes);
		refGenomeCombo.setPreferredSize(new Dimension(190, 30));
		refClusterField.setPreferredSize(new Dimension(190, 30));
		
		
		JPanel p6a = new JPanel(new FlowLayout(FlowLayout.LEFT));
		((FlowLayout) p6a.getLayout()).setVgap(12);
		p6a.add(refValueLabel);
		gridpanel.add(p6a);
		final JPanel p6b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		gridpanel.add(p6b);
		
		panel.add(gridpanel);
		
		refCombo.setEnabled(false);
		ActionListener modeActionListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (modeCombo.getSelectedIndex()==2) { 
					refCombo.setEnabled(true);
				} else { 
					refCombo.setEnabled(false);
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
				switch (refCombo.getSelectedIndex()) {
				case 1:
					p6b.remove(refClusterField);
					p6b.add(refGenomeCombo);
					refType = 'g';
					break;
				case 2:
					p6b.add(refClusterField);
					ToolTipManager.sharedInstance().mouseMoved(
					        new MouseEvent(refClusterField, 0, 0, 0,
					                (int) refClusterField.getLocation().getX(), // X-Y of the mouse for the tool tip
					                (int) refClusterField.getLocation().getY(),
					                0, false));
					refType = 'c';
					p6b.remove(refGenomeCombo);
					break;
				default:
					p6b.remove(refClusterField);
					p6b.remove(refGenomeCombo);		
					refType = 'd';
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
				if (opMode=='r' && refType=='g' && refGenomeCombo.getSelectedIndex()!=0) {
					PrintUtils.printDebug("swapping genomes");
					Genome[] genomes = GeckoInstance.getInstance().getGenomes();
					Genome first = genomes[0];
					genomes[0] = genomes[refGenomeCombo.getSelectedIndex()];
					genomes[refGenomeCombo.getSelectedIndex()] = first;
					
					gecko.getGui().closeCurrentSession();
					gecko.setGenomes(genomes);
					//TODO improve
				} else if (opMode=='r' && refType=='c') {
					Genome[] oldGenomes = gecko.getGenomes();
					Genome[] genomes = new Genome[oldGenomes.length+1];
					Genome cluster = new Genome();
					ArrayList<Gene> genes = new ArrayList<Gene>();
					Map<Integer, Integer> revIDMap = SortUtils.invertIntArray(gecko.getGenLabelMap());
					for (String id : refClusterField.getText().split(" "))
						if (id!=null && (!(id.equals("")))) {
							Integer iid = revIDMap.get(Integer.parseInt(id));
							if (iid!=null)
								genes.add(new Gene("", iid));
						}
					cluster.getChromosomes().add(new Chromosome("Reference cluster", genes));
					genomes[0] = cluster;
					for (int i=0;i<oldGenomes.length;i++) {
						genomes[i+1] = oldGenomes[i];
					}
					gecko.getGui().closeCurrentSession();
					gecko.setGenomes(genomes);
				}
				GeckoInstance.getInstance().performClusterDetection(new Parameter((Integer) dSpinner.getValue(), 
						(Integer) sSpinner.getValue(),
						quorum,Parameter.QUORUM_NO_COST, 
						opMode,
						refType));
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
