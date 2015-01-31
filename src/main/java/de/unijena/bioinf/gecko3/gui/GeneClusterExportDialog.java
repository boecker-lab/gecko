/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.gui;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.lowagie.text.DocumentException;
import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.gui.GenomePainting.NameType;
import de.unijena.bioinf.gecko3.io.GeneClusterToPDFWriter;
import de.unijena.bioinf.gecko3.util.FileUtils;
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

    private enum PictureFormat{
        PDF,
        JPG,
        PNG
    }
	
	/**
	 * Text filed which contains the path to the pdf file we want to store.
	 */
	private final JTextField storingLocation;
	
	/**
	 * Text field which contains the users name.
	 */
	private final JTextField authorName;

    private final JComboBox<PictureFormat> exportFormatComboBox;
	
	/**
	 * Predefined file name for the pdf file.
	 */
	private final String FILENAME = "ClusterExport";
	
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
		super(parent,"Export gene cluster", true);
        authorName = new JTextField(System.getProperty("user.name"));
        storingLocation = new JTextField();
		this.rootPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		this.setLayout(new BorderLayout());
		this.setIconImages(parent.getIconImages());

		// description label for the text field
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
		useGenomeNamesCheckBox.setText("Print genome names");
		
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
                        switch ((PictureFormat)exportFormatComboBox.getSelectedItem()) {
                            case PNG:
                                ImageIO.write(GeneClusterExportDialog.this.clusterPics.createImage(), "png", out);
                                break;
                            case JPG:
                                ImageIO.write(GeneClusterExportDialog.this.clusterPics.createImage(), "jpg", out);
                                break;
                            case PDF:
                                GeneClusterToPDFWriter gcw = new GeneClusterToPDFWriter(out);
                                gcw.write(GeneClusterExportDialog.this.clusterPics);
                        }
                        GeneClusterExportDialog.this.setVisible(false);
                    }  catch (IOException | DocumentException e) {
                        if (GeckoInstance.getInstance().getGui() != null)
                            JOptionPane.showMessageDialog(GeneClusterExportDialog.this, "Error in output", "Error", JOptionPane.ERROR_MESSAGE);
                        logger.warn("Unable to write picture", e);
                    }
                }
            }
        });

        exportFormatComboBox = new JComboBox<>(PictureFormat.values());
        exportFormatComboBox.setEditable(false);
        exportFormatComboBox.setMaximumRowCount(PictureFormat.values().length);
        exportFormatComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    matchFileEndingToFiletype();
                }
            }
        });

        final JCheckBox clusterHeaderCheckbox = new JCheckBox();
        clusterHeaderCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                clusterPics.setClusterHeader(clusterHeaderCheckbox.isSelected());
                updateImage();
            }
        });
        clusterHeaderCheckbox.setSelected(true);

        DefaultFormBuilder leftPanelBuilder = new DefaultFormBuilder(new FormLayout("p, 4dlu, p"));
        leftPanelBuilder.append(storingLocation, browseButton);
        leftPanelBuilder.append("Print genome names", useGenomeNamesCheckBox);
        leftPanelBuilder.append("Print Gene using:", geneNamingComboBox);
        leftPanelBuilder.append("Author:", authorName);
        leftPanelBuilder.append("Export format:", exportFormatComboBox);
        leftPanelBuilder.append("Add cluster header:", clusterHeaderCheckbox);
        leftPanelBuilder.append(exportButton, cancelButton);
        JPanel leftPanel = leftPanelBuilder.getPanel();
        leftPanel.setBackground(this.getBackground());
		
		// add main panels to the window
		this.add(leftPanel, BorderLayout.WEST);
		
		// create a scrollPanel with the cluster image
		clusterPics = new GeneClusterPicture(clusterSelection, (NameType)geneNamingComboBox.getSelectedItem(), clusterHeaderCheckbox.isSelected(), useGenomeNamesCheckBox.isSelected());
		prev = new Preview(clusterPics.createImage());
		JScrollPane previewScroll = new JScrollPane(prev);
		previewScroll.setEnabled(true);
		previewScroll.setBackground(Color.white);
		previewScroll.setDoubleBuffered(true);
		previewScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel mainPanel2 = new JPanel();
        mainPanel2.setLayout(new BorderLayout());
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
        switch ((PictureFormat)exportFormatComboBox.getSelectedItem()) {
            case JPG:
                storingLocation.setText(newFilename + ".jpg");
                break;
            case PNG:
                storingLocation.setText(newFilename + ".png");
                break;
            case PDF:
                storingLocation.setText(newFilename + ".pdf");
                break;
        }
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
