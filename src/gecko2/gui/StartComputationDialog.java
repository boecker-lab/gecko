package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.algorithm.Parameter;
import gecko2.util.PrintUtils;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

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
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;


public class StartComputationDialog extends JDialog {

	private static final long serialVersionUID = -5635614016950101153L;
	private int quorum;
	private char opMode;

	public StartComputationDialog(int ngenomes) {
		this.setModal(true);
		this.setResizable(false);
		JPanel panel = new JPanel(new FlowLayout());
		panel.setPreferredSize(new Dimension(430,220));
		JPanel gridpanel = new JPanel();
		gridpanel.setPreferredSize(new Dimension(410,160));
		gridpanel.setLayout(new GridLayout(4,2));
		
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
		qCombo.setPreferredSize(new Dimension(150,30));
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

		modeCombo.setPreferredSize(new Dimension(120,30));

		this.opMode = 'm';
		modeCombo.setSelectedIndex(0);
		
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
		
		panel.add(gridpanel);
		
		JPanel lowerpanel = new JPanel();
		lowerpanel.setLayout(new BoxLayout(lowerpanel,BoxLayout.X_AXIS));
		lowerpanel.setPreferredSize(new Dimension(300,50));
		Action okAction = new AbstractAction() {
		
			private static final long serialVersionUID = 6197096728152587585L;
			public void actionPerformed(ActionEvent e) {
				StartComputationDialog.this.setVisible(false);
				GeckoInstance.getInstance().performClusterDetection(new Parameter((Integer) dSpinner.getValue(), (Integer) sSpinner.getValue(),quorum,Parameter.QUORUM_NO_COST, opMode));
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
