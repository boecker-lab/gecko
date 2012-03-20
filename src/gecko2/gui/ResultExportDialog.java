package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.GeckoInstance.ResultFilter;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

public class ResultExportDialog extends JDialog {

	private JTextField textField;

	/**
	 * Create the dialog.
	 */
	public ResultExportDialog(Frame parent) {
		setTitle("Export Results");
		setBounds(100, 100, 450, 300);
		
		getContentPane().setLayout(new BorderLayout());
		
		DefaultFormBuilder contentBuilder = new DefaultFormBuilder(new FormLayout("p, 4dlu, p:g, 4dlu, p"));
		contentBuilder.setDefaultDialogBorder();
		
		textField = new JTextField();
		JButton btnBrowse = new JButton("Browse...");
		btnBrowse.addActionListener(new ActionListener() {				
			@Override
			public void actionPerformed(ActionEvent e) {
				GeckoInstance gecko = GeckoInstance.getInstance();
				JFileChooser fc = new JFileChooser();
				if (gecko.getLastExportedFile()!=null)
					fc.setSelectedFile(gecko.getLastExportedFile());
				int state = fc.showSaveDialog(ResultExportDialog.this);
				if (state == JFileChooser.APPROVE_OPTION) {
					if (fc.getSelectedFile() != null)
						textField.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		final JComboBox selectionComboBox = new JComboBox(ResultFilter.values());
		
		contentBuilder.append(new JLabel("Choose File"), textField, btnBrowse);
		contentBuilder.append(new JLabel("Choose Filtering"), selectionComboBox);
		
		ButtonBarBuilder2 buttonBuilder = new ButtonBarBuilder2();
		
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = validateSettings();
				
				if (file != null) {
					if (!GeckoInstance.getInstance().exportResultsToFile(file, (ResultFilter)selectionComboBox.getSelectedItem()))
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
		buttonPanel.setBorder(Borders.DIALOG_BORDER);
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Validate all settings to check if a result can be exported
	 * @return the File the results will be written to, or null if not valid 
	 */
	private File validateSettings() {
		if (textField.getText().trim() == "") {
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
