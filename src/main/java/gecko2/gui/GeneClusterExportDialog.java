package gecko2.gui;

import com.lowagie.text.DocumentException;
import gecko2.GeckoInstance;
import gecko2.datastructures.GeneCluster;
import gecko2.datastructures.GeneFamily;
import gecko2.gui.GenomePainting.NameType;
import gecko2.io.GeneClusterToPDFWriter;
import gecko2.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * This class implements an dialog for setting option to export a GeneCluster
 * (picture in MultipleGenomesBrowser) into a pdf file and several image formats.
 * 
 * @author Hans-Martin Haase <hans-martin.haase at uni-jena dot de>
 * @version 0.15
 */
public class GeneClusterExportDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(GeneClusterExportDialog.class);

	/**
	 * Random serial version UID
	 */
	private static final long serialVersionUID = 1468873732499397039L;
	
	/**
	 * Text filed which contains the path to the pdf file we want to store.
	 */
	private final JTextField storingLocation = new JTextField();
	
	/**
	 * Text field which contains the users name.
	 */
	private final JTextField authorName = new JTextField(System.getProperty("user.name"));
	
	/**
	 * Predefined file name for the pdf file.
	 */
	private final String FILENAME = "ClusterExport";

	/**
	 * Here we store whether extra data from the GeneClusterDisplay shall be
	 * added to the PDF.
	 */
	private boolean eData = false;
	
	/**
	 * True if we have to use the optional output format png.
	 */
	private boolean png = false;
	
	/**
	 * True if we have to use the optional output format jpg.
	 */
	private boolean jpg = false;
	
	/**
	 * Default output format
	 */
	private boolean pdf = true;
	
	/**
	 * The object which contains/creates the image content
	 */
	private GeneClusterPicture clusterPics;
	
	/**
	 * The preview area on the gui
	 */
	private final Preview prev;

    /**
     * Constructor sets the elements of the dialog.
     *
     * @param parent the parent frame
     * @param clusterSelection the cluster selection
     */
	public GeneClusterExportDialog (final Frame parent, GeneClusterLocationSelection clusterSelection) {

		// Setup the dialog window
		super(parent,"Export gene cluster");
		super.setModal(true);
		this.rootPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(1010, 700));
		this.setIconImage(Gui.createImageIcon("images/gecko2_a_small.png").getImage());

        JLabel ovlapStatus1 = new JLabel();
		ovlapStatus1.setForeground(Color.RED);
        JLabel ovlapStatus2 = new JLabel();
		ovlapStatus2.setForeground(Color.RED);
	
		// two main panels
		JPanel mainPanel1 = new JPanel();
		mainPanel1.setLayout(new GridLayout(23, 1, 5, 5));
		JPanel mainPanel2 = new JPanel();
		mainPanel2.setLayout(new BorderLayout());
		
		// description label for the text field
		JLabel storLocLabel = new JLabel("File name:  ");
		this.storingLocation.setPreferredSize(new Dimension(280, storingLocation.getHeight()));
		this.storingLocation.setText(System.getProperty("user.dir") + File.separatorChar + FILENAME + ".pdf");
		
		
		// file chooser button including action if pressed
		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// set up file chooser
				JFileChooser fc = new JFileChooser(GeckoInstance.getInstance().getCurrentWorkingDirectoryOrFile());
				fc.setName(FILENAME);
				fc.addChoosableFileFilter(new FileUtils.GenericFilter("pdf;png;jpg"));
				fc.setDialogTitle("Select the location to save the file...");

                int state = fc.showSaveDialog(GeneClusterExportDialog.this);
                if (state == JFileChooser.APPROVE_OPTION) {
                    if (fc.getSelectedFile() != null)
                        GeneClusterExportDialog.this.storingLocation.setText(fc.getSelectedFile().getAbsolutePath());
                }
			}
		});
		
		// check box for using genome names instead of the internal mapped number
		JCheckBox useGenomeNamesCheckBox = new JCheckBox();
		useGenomeNamesCheckBox.setText("Use genome names instead of numbers.");
		
		useGenomeNamesCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                int status = e.getStateChange();

                // update preview on select/deselect
                if (status == ItemEvent.DESELECTED)
                    clusterPics.setGnames(false);
                else
                    clusterPics.setGnames(true);
                updateImage();
            }
        });

		
		final JComboBox geneNamingComboBox = new JComboBox(GenomePainting.NameType.values());
		geneNamingComboBox.setSelectedIndex(0);
		geneNamingComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				clusterPics.setNameType((NameType)geneNamingComboBox.getSelectedItem());
				updateImage();
			}
		});
		
		// check box for the extra data from the gene cluster display
		JCheckBox extraData = new JCheckBox();
		extraData.setText("Add extra information about the cluster. (Just for .pdf export)");
		
		extraData.addItemListener(new ItemListener() {

			// just save the status we use this for the pdf only
			@Override
			public void itemStateChanged(ItemEvent arg0) {				
				int status = arg0.getStateChange();

                GeneClusterExportDialog.this.eData = status != ItemEvent.DESELECTED;
			}
			
		});		
		
		// disable because it is not implemented
		//extraData.setEnabled(false);
		
		// color chooser for preventing random colors in the non clustered parts
		// of the genome
		// TODO implement actionListener
		/*
		 * commented because currently not implemented
		 */
		//JButton colorChooser = new JButton("Custom cluster color...");
		//colorChooser.setEnabled(false);
		
		// Button to abort the export including the actionListener
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				GeneClusterExportDialog.this.setVisible(false);
			}
			
		});
		
        /**
         * Action which is launched by pressing the export button.
         * Causes the export of the cluster to a pdf file.
         */
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                File file = validateSettings();
                if (file != null) {
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        if (GeneClusterExportDialog.this.png) {
                            ImageIO.write(GeneClusterExportDialog.this.clusterPics.createImage(), "png", out);
                            GeneClusterExportDialog.this.setVisible(false);
                        }

                        if (GeneClusterExportDialog.this.jpg) {
                            ImageIO.write(GeneClusterExportDialog.this.clusterPics.createImage(), "jpg", out);
                            GeneClusterExportDialog.this.setVisible(false);
                        }

                        if (GeneClusterExportDialog.this.pdf) {
                            GeneClusterToPDFWriter gcw = new GeneClusterToPDFWriter(out);
                            gcw.write(GeneClusterExportDialog.this.clusterPics);
                            GeneClusterExportDialog.this.setVisible(false);
                        }
                    }  catch (IOException | DocumentException e) {
                        if (GeckoInstance.getInstance().getGui() != null)
                            JOptionPane.showMessageDialog(GeneClusterExportDialog.this, "Error in output", "Error", JOptionPane.ERROR_MESSAGE);
                        logger.warn("Unable to write picture", e);
                    }
                }
            }
        });
		
		JLabel author = new JLabel("Author:  ");
		
		// add single components to the two main panels
		mainPanel1.add(storLocLabel);
		JPanel storPanel = new JPanel();
		storPanel.setLayout(new BorderLayout());
		storPanel.add(storingLocation, BorderLayout.WEST);
		storPanel.add(browseButton, BorderLayout.CENTER);
		mainPanel1.add(storPanel);
		//mainPanel1.add(storingLocation);
		mainPanel1.add(useGenomeNamesCheckBox);
		mainPanel1.add(geneNamingComboBox);
		//mainPanel1.add(extraData);
		mainPanel1.add(ovlapStatus1);
		mainPanel1.add(ovlapStatus2);
		JPanel authorPane = new JPanel();
		authorPane.setLayout(new BorderLayout());
		authorPane.add(author, BorderLayout.WEST);
		authorPane.add(this.authorName, BorderLayout.CENTER);
		mainPanel1.add(authorPane);
		
		
		// output format chooser
		JLabel exportFormats = new JLabel("Export formats: ");
		JRadioButton pngExport = new JRadioButton();
		pngExport.setText(".png");
		
		pngExport.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				int status = e.getStateChange();
				
				if (status == ItemEvent.SELECTED) {

					pdf = false;
					png = true;
					jpg = false;

                    matchFileEndingToFiletype();
				}
			}
		});
		
		JRadioButton jpgExport = new JRadioButton();
		jpgExport.setText(".jpg");
		
		jpgExport.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				int status = e.getStateChange();
				
				if (status == ItemEvent.SELECTED) {

					pdf = false;
					png = false;
					jpg = true;

                    matchFileEndingToFiletype();
				}
			}
		});

		JRadioButton pdfExport = new JRadioButton();
		pdfExport.setText(".pdf");
		pdfExport.setSelected(true);
		
		pdfExport.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				int status = e.getStateChange();
				
				if (status == ItemEvent.SELECTED) {

					pdf = true;
					png = false;
					jpg = false;

                    matchFileEndingToFiletype();
				}
			}
		});
		
		ButtonGroup formatOptions = new ButtonGroup();
		formatOptions.add(pdfExport);
		formatOptions.add(jpgExport);
		formatOptions.add(pngExport);
		
		mainPanel1.add(exportFormats);
		mainPanel1.add(pngExport);
		mainPanel1.add(jpgExport);
		mainPanel1.add(pdfExport);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(cancelButton, BorderLayout.WEST);
		buttonPanel.add(exportButton, BorderLayout.EAST);
		mainPanel1.add(buttonPanel);
		
		// add main panels to the window
		this.add(mainPanel1, BorderLayout.WEST); 
		
		// create a scrollPanel with the cluster image
		clusterPics = new GeneClusterPicture(clusterSelection, (NameType)geneNamingComboBox.getSelectedItem(), useGenomeNamesCheckBox.isSelected());
		prev = new Preview(clusterPics.createImage());
		JScrollPane previewScroll = new JScrollPane(prev);
		previewScroll.setEnabled(true);
		previewScroll.setBackground(Color.white);
		previewScroll.setDoubleBuffered(true);
		previewScroll.setBorder(BorderFactory.createEmptyBorder());
		
		//previewScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		mainPanel2.add(previewScroll);
		this.add(mainPanel2, BorderLayout.CENTER);
		this.pack();
		
	}

    /**
     * Validate all settings to check if a result can be exported
     * @return the File the results will be written to, or null if not valid
     */
    private File validateSettings() {
        if (storingLocation.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(GeneClusterExportDialog.this, "You have to select a file!");
            return null;
        }
        File file = new File(storingLocation.getText());
        if (file.exists()) {
            int x = JOptionPane.showConfirmDialog(GeneClusterExportDialog.this,
                    "The chosen file already exists. Overwrite?",
                    "Overwrite existing file?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (x==JOptionPane.NO_OPTION) return null;
        }

        return file;
    }

    private void matchFileEndingToFiletype() {
        int dot = GeneClusterExportDialog.this.storingLocation.getText().lastIndexOf(".");
        String newFilename = GeneClusterExportDialog.this.storingLocation.getText().substring(0, dot);
        if (pdf)
            storingLocation.setText(newFilename + ".pdf");
        if (png)
            storingLocation.setText(newFilename + ".png");
        if (jpg)
            storingLocation.setText(newFilename + ".jpg");
    }
	
	private void updateImage() {
		GeneClusterExportDialog.this.prev.updatePreview(GeneClusterExportDialog.this.clusterPics.createImage());
		
		GeneClusterExportDialog.this.validate();
		GeneClusterExportDialog.this.repaint();
	}
	
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
			
			JOptionPane.showMessageDialog(null, "The file" + fileToTest.getAbsoluteFile() + " already exists.", "Error", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		
		return false;
	}
	
	/**
	 * The method checks whether the given file (given as string) has the correct file extension.
	 * 
	 * @param absoluteFile String with the absolute file path
	 * @return the fixed file path as String
	 */
	private String checkAndFixFileExtension(String absoluteFile) {
		
		// get the correct file extension
		String suffix = ".pdf";

		if (jpg) {
			
			suffix = ".jpg";
		}
		
		if (png) {
			
			suffix = ".png";
		}
		
		String absoluteFileFixed = absoluteFile;
		
		// fix file extension if the user entered a other extension than the selected one
		if ((absoluteFile.contains(".png") || absoluteFile.contains(".jpg")) && this.pdf) {
			
			absoluteFileFixed = absoluteFile.replaceAll("\\.png|\\.jpg", ".pdf");
		}
		
		if ((absoluteFile.contains(".jpg") || absoluteFile.contains(".pdf")) && this.png) {
			
			absoluteFileFixed = absoluteFile.replaceAll("\\.pdf|\\.jpg", ".png");
		}
		
		if ((absoluteFile.contains(".png") || absoluteFile.contains(".pdf")) && this.jpg) {
			
			absoluteFileFixed = absoluteFile.replaceAll("\\.pdf|\\.png", ".jpg");
		}
		
		if (!absoluteFile.contains(".jpg") && !absoluteFile.contains(".png") && !absoluteFile.contains(".pdf")) {
			
			absoluteFileFixed = absoluteFile + suffix;
		}
		
		return absoluteFileFixed;
	}

    /**
     * The class implements a simple preview for the pdf file created with the
     * GeneClusterToPDFWriter.
     *
     * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
     *
     */
    private static class Preview extends JPanel {
        /**
         * Random generated serial version UID
         */
        private static final long serialVersionUID = -1392501449288707281L;

        /**
         * Stores the original given picture which was set in the constructor.
         */
        private BufferedImage clusterPicture;

        /**
         * The constructor sets the global variable clusterPicture and sets a basic layout.
         *
         * @param toShow BufferdImage from GeneClusterToPDFWriter
         */
        public Preview (BufferedImage toShow) {
            this.setImage(toShow);
            this.setBackground(Color.WHITE);
        }

        private void setImage(BufferedImage image) {
            this.clusterPicture = image;
            this.setPreferredSize(new Dimension(clusterPicture.getWidth(), clusterPicture.getHeight()));
        }

        /**
         * Overwrites the paint method so the the cluster picture is on the Frame.
         */
        @Override
        public void paint(Graphics g) {
            g.drawImage(this.clusterPicture, 15, 15, null);
        }

        public void updatePreview(BufferedImage newImage) {
            setImage(newImage);
            this.getGraphics().dispose();
            this.getGraphics().drawImage(this.clusterPicture, 15, 15, null);
            this.validate();
            this.repaint();
        }
    }
}
