package gecko2.gui;

import gecko2.io.GeneClusterToPDFWriter;
import gecko2.util.FileUtils;
import gecko2.gui.PDFPreview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * This class implements an dialog for setting option to export an GeneCluster
 * (picture in MultipleGenomesBrowser) into a pdf file.
 * 
 * @author Hans-Martin Haase <hans-martin.haase at uni-jena dot de>
 *
 */
public class GeneClusterExportDialog extends JDialog {

	/**
	 * Random serial version UID
	 */
	private static final long serialVersionUID = 1468873732499397039L;
	
	/**
	 * Text filed which contains the path to the pdf file we want to store.
	 */
	private JTextField storingLocation = new JTextField();
	
	/**
	 * Users home directory for saving the pdf file (Default location).
	 */
	private final String HELP = System.getProperty("user.dir");
	
	/**
	 * Predefined file name for the pdf file.
	 */
	private final String FILENAME = "ClusterExport.pdf";
	
	/**
	 * Here we store whether extra data from the GeneClusterDisplay shall be
	 * added to the PDF.
	 */
	private boolean eData = false;
	
	/**
	 * Here we store whether we want to show genome names in the pdf or just
	 * the ids.
	 */
	private boolean gNames = false;
	
	/**
	 * Constructor sets the elements of the dialog.
	 * 
	 * @param parent the parent frame
	 * @param rowOfSelcCluster the row number from the table where we selected to export
	 */
	public GeneClusterExportDialog (final Frame parent) {
		
		// Setup the dialog window
		super(parent,"Export gene cluster to pdf");
		super.setModal(true);
		this.setLayout(new BorderLayout());
		this.setBounds(100, 100, 450, 300);
		
		// two main panels
		JPanel mainPanel1 = new JPanel();
		mainPanel1.setLayout(new GridLayout(6, 1, 5, 5));
		JPanel mainPanel2 = new JPanel();
		mainPanel2.setLayout(new GridLayout(6, 2, 5, 5));
		
		// description label for the text field
		JLabel storLocLabel = new JLabel("File name:");
		this.storingLocation.setText(HELP + File.separatorChar + FILENAME);
		
		// file chooser button including action if pressed
		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				// set up file chooser
				JFileChooser fileLocation = new JFileChooser();
				fileLocation.setName(FILENAME);
				fileLocation.addChoosableFileFilter(new FileUtils.GenericFilter("pdf;png"));
				fileLocation.setDialogTitle("Select the location to save the file...");
				int option;
				
				// ask for the path until we get a non existing file
				do {
					
					option = fileLocation.showDialog(parent, "Ok");
				}
				while (GeneClusterExportDialog.this.checkFileExistence(fileLocation.getSelectedFile()) || option != JFileChooser.CANCEL_OPTION);
		
				// set the path into the text field
				if (option == JFileChooser.APPROVE_OPTION) {
				
					GeneClusterExportDialog.this.storingLocation.setText(fileLocation.getSelectedFile().getAbsolutePath());	
				}
				else {
				
					GeneClusterExportDialog.this.storingLocation.setText(HELP + File.separatorChar + FILENAME);
				}
			}
		});
		
		// check box for using genome names instead of the internal mapped number
		JCheckBox gName = new JCheckBox();
		gName.setText("Use genome names instead of numbers.");
		
		gName.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				
				int status = e.getStateChange();
				
				if (status == ItemEvent.DESELECTED) {
					
            		GeneClusterExportDialog.this.gNames = false;
            	}
            	else {	
            		
            		GeneClusterExportDialog.this.gNames = true;
            	}
			}
		});
		
		// check box for the extra data from the gene cluster display
		JCheckBox extraData = new JCheckBox();
		extraData.setText("Add extra information about the cluster.");
		
		extraData.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				
				int status = arg0.getStateChange();
				
				if (status == ItemEvent.DESELECTED) {
					
            		GeneClusterExportDialog.this.eData = false;
            	}
            	else {	
            		
            		GeneClusterExportDialog.this.eData = true;
            	}
				
			}
			
		});		
		
		// disable because it is not implemented
		extraData.setEnabled(false);
		
		// color chooser for preventing random colors in the non clustered parts
		// of the genome
		// TODO implement actionListener
		JButton colorChooser = new JButton("Custom cluster color...");
		colorChooser.setEnabled(false);
		
		// Button to abort the export including the actionListener
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				GeneClusterExportDialog.this.setVisible(false);
			}
			
		});
		
		// Button for launching the export
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(export);
		
		JButton previewButton = new JButton("Preview");
		previewButton.addActionListener(preview);
		
		// add single components to the two main panels
		mainPanel1.add(storLocLabel);
		mainPanel1.add(storingLocation);
		mainPanel1.add(gName);
		mainPanel1.add(extraData);
		mainPanel1.add(colorChooser);
		
		mainPanel2.add(new JLabel(" "));
		mainPanel2.add(new JLabel(" "));
		mainPanel2.add(browseButton);
		mainPanel2.add(new JLabel(" "));
		mainPanel2.add(new JLabel(" "));
		
		mainPanel2.add(new JLabel(" "));
		mainPanel2.add(new JLabel(" "));
		mainPanel2.add(new JLabel(" "));
		mainPanel2.add(new JLabel(" "));
		mainPanel2.add(previewButton);
		
		mainPanel2.add(cancelButton);
		mainPanel2.add(exportButton);
		
		// add main panels to the window
		this.add(mainPanel1, BorderLayout.WEST);
		this.add(mainPanel2, BorderLayout.CENTER);
		
		this.pack();
		
	}
	
	/**
	 * Action which is launched by pressing the export button.
	 * Causes the export of the cluster to a pdf file.
	 */
	private Action export = new AbstractAction() {

		/**
		 * Random serial version UID
		 */
		private static final long serialVersionUID = 8546001404773999661L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			GeneClusterToPDFWriter gcw = new GeneClusterToPDFWriter(new File(GeneClusterExportDialog.this.storingLocation.getText()),
					HELP,
					GeneClusterExportDialog.this.eData, 
					GeneClusterExportDialog.this.gNames
					);
			
			gcw.createPDF();
			GeneClusterExportDialog.this.setVisible(false);
		}
	};
	
	/**
	 * This action generates a new frame with a preview of the pdf file
	 * we want to create.
	 */
	private Action preview = new AbstractAction() {

		/**
		 * Random generated serial Version UID
		 */
		private static final long serialVersionUID = 122903979629472599L;

		@Override
		public void actionPerformed(ActionEvent e) {
			
			GeneClusterToPDFWriter gcw = new GeneClusterToPDFWriter(null,
					HELP,
					GeneClusterExportDialog.this.eData, 
					GeneClusterExportDialog.this.gNames
					);
			
			// setup the new frame
			final JFrame prevFrame = new JFrame("Preview");
			prevFrame.setPreferredSize(new Dimension(500, 500));
			prevFrame.setSize(getPreferredSize());
			prevFrame.setLayout(new BorderLayout());
			prevFrame.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
			
			// create a scrollPanel with the cluster image
			PDFPreview pdfPrev = new PDFPreview(gcw.createPic());
			prevFrame.add(pdfPrev, BorderLayout.NORTH);
			
			// close button
			JButton close = new JButton("Close");
			prevFrame.add(close, BorderLayout.SOUTH);
			close.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					prevFrame.setVisible(false);
				}
				
				
			});
			
			// pack and make the frame visible
			prevFrame.pack();
			prevFrame.setVisible(true);
			
			
		}
	};
	
	/**
	 * The method test whether a given file object points to a existing file.
	 * 
	 * @param fileToTest file object to test
	 * @return true if the file object points to a existing file otherwise false
	 */
	private boolean checkFileExistence(File fileToTest) {
		
		if (fileToTest == null) {
			
			return false;
		}
		
		if (fileToTest.isFile()) {
			
			JOptionPane.showMessageDialog(null, "The file already exists.", "Error", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		
		return false;
	}
	
	
	
	// Just for layout testing
	public static void main(String[] args) {
		
		JFrame mainframe = new JFrame();
		mainframe = new JFrame("Gecko\u00B2");
		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension startDimension = new Dimension(800, 600);
		mainframe.setPreferredSize(startDimension);
		mainframe.setLayout(new BorderLayout());
		mainframe.pack();
		mainframe.setLocationRelativeTo(null);
		mainframe.setVisible(true);
		
		//GeneClusterExportDialog test = new GeneClusterExportDialog(mainframe);
		//test.setVisible(true);
	}
}
