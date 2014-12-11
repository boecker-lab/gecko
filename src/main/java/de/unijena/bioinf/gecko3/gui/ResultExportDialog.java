package de.unijena.bioinf.gecko3.gui;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.GeckoInstance.ResultFilter;
import de.unijena.bioinf.gecko3.io.ResultWriter.ExportType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

public class ResultExportDialog extends JDialog {

	/**
	 * Random generated serial version uid
	 */
	private static final long serialVersionUID = -5557686856082270849L;
	
	private final JTextField textField;
	private final JPanel advancedOptionsPanel;

	/**
	 * Create the dialog.
	 */
	public ResultExportDialog(Frame parent) {
        super(parent);
		setTitle("Export Results");
        setIconImages(parent.getIconImages());
		
		DefaultFormBuilder generalOptionsBuilder = new DefaultFormBuilder(new FormLayout("p, 4dlu, p:g, 4dlu, p"));
		generalOptionsBuilder.border(Borders.DIALOG);

		textField = new JTextField();
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.addActionListener(new ActionListener() {				
			@Override
			public void actionPerformed(ActionEvent e) {
				GeckoInstance gecko = GeckoInstance.getInstance();
				JFileChooser fc = new JFileChooser(gecko.getCurrentWorkingDirectoryOrFile());
				int state = fc.showSaveDialog(ResultExportDialog.this);
				if (state == JFileChooser.APPROVE_OPTION) {
					if (fc.getSelectedFile() != null) {
                        textField.setText(fc.getSelectedFile().getAbsolutePath());
                        gecko.setCurrentWorkingDirectoryOrFile(fc.getSelectedFile());
                    }
				}
			}
		});
		final JComboBox<ResultFilter> resultFilterComboBox = new JComboBox<>(ResultFilter.values());
		final JComboBox<ExportType> exportTypeCompoBox = new JComboBox<>(ExportType.getSupported());
		exportTypeCompoBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CardLayout cl = (CardLayout)(advancedOptionsPanel.getLayout());
					cl.show(advancedOptionsPanel, e.getItem().toString());
				}
			}
		});

        generalOptionsBuilder.append(new JLabel("Choose Export Type"), exportTypeCompoBox);
        generalOptionsBuilder.nextLine();
        generalOptionsBuilder.append(new JLabel("Choose Filtering"), resultFilterComboBox);
        generalOptionsBuilder.nextLine();
		generalOptionsBuilder.append(new JLabel("Choose File"), textField, btnBrowse);

		advancedOptionsPanel = new JPanel(new CardLayout());
		for (ExportType exportType : ExportType.getSupported()) {
			advancedOptionsPanel.add(exportType.getAdvancedOptionsPanel(), exportType.toString());
		}

		ButtonBarBuilder buttonBuilder = new ButtonBarBuilder();
		
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = validateSettings();
				
				if (file != null) {
					if (!GeckoInstance.getInstance().exportResultsToFile(file, (ResultFilter)resultFilterComboBox.getSelectedItem(), (ExportType)exportTypeCompoBox.getSelectedItem()))
						JOptionPane.showMessageDialog(ResultExportDialog.this, "Error writing the file!", "Write error", JOptionPane.ERROR_MESSAGE);
					ResultExportDialog.this.close();
				}						
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ResultExportDialog.this.close();	
			}
		});
		
		buttonBuilder.addGlue();
		buttonBuilder.addButton(okButton);
		buttonBuilder.addRelatedGap();
		buttonBuilder.addButton(cancelButton);
		
		JPanel generalOptionPanel = generalOptionsBuilder.getPanel();

		DefaultFormBuilder contentBuilder = new DefaultFormBuilder(new FormLayout("p"));
		contentBuilder.append(generalOptionPanel);
		contentBuilder.nextLine();
		contentBuilder.appendSeparator("Advanced Options");
		contentBuilder.append(advancedOptionsPanel);
		JPanel contentPanel = contentBuilder.getPanel();

		JPanel buttonPanel = buttonBuilder.getPanel();
		buttonPanel.setBorder(Borders.DIALOG);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}

	/**
	 * Validate all settings to check if a result can be exported
	 * @return the File the results will be written to, or null if not valid 
	 */
	private File validateSettings() {
		if (textField.getText().trim().equals("")) {
			JOptionPane.showMessageDialog(ResultExportDialog.this, "You have to select a file!");
			return null;
		}
		File file = new File(textField.getText());
		if (file.exists()) {
			int x = JOptionPane.showConfirmDialog(ResultExportDialog.this,
					"The chosen file already exists. Overwrite?",
					"Overwrite existing file?",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (x==JOptionPane.NO_OPTION) return null;
		}
		
		return file;
	}
	
	/**
	 * Close the dialog
	 */
	private void close() {
		this.setVisible(false);
	}
}
