package gecko2.gui;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import gecko2.GeckoInstance;
import gecko2.GeckoInstance.ResultFilter;
import gecko2.io.ResultWriter.ExportType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ResultExportDialog extends JDialog {

	/**
	 * Random generated serial version uid
	 */
	private static final long serialVersionUID = -5557686856082270849L;
	
	private final JTextField textField;

	/**
	 * Create the dialog.
	 */
	public ResultExportDialog(Frame parent) {
        super(parent);
		setTitle("Export Results");
		this.setIconImage(Gui.createImageIcon("images/gecko2_a_small.png").getImage());
		setBounds(100, 100, 450, 300);
		
		getContentPane().setLayout(new BorderLayout());
		
		DefaultFormBuilder contentBuilder = new DefaultFormBuilder(new FormLayout("p, 4dlu, p:g, 4dlu, p"));
		contentBuilder.border(Borders.DIALOG);

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

        contentBuilder.append(new JLabel("Choose Export Type"), exportTypeCompoBox);
        contentBuilder.nextLine();
        contentBuilder.append(new JLabel("Choose Filtering"), resultFilterComboBox);
        contentBuilder.nextLine();
		contentBuilder.append(new JLabel("Choose File"), textField, btnBrowse);

		
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
		
		JPanel contentPanel = contentBuilder.getPanel();
		JPanel buttonPanel = buttonBuilder.getPanel();
		buttonPanel.setBorder(Borders.DIALOG);
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
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
