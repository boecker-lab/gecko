package de.unijena.bioinf.gecko3.gui;

import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.algo.status.AlgorithmStatusEvent;
import de.unijena.bioinf.gecko3.datastructures.DataSet;
import de.unijena.bioinf.gecko3.datastructures.GeneCluster;
import de.unijena.bioinf.gecko3.io.ClusterAnnotationReader;
import de.unijena.bioinf.gecko3.io.CogFileReader;
import de.unijena.bioinf.gecko3.io.DataSetWriter;
import de.unijena.bioinf.gecko3.io.GckFileReader;
import de.unijena.bioinf.gecko3.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Gui {
    private static final Logger logger = LoggerFactory.getLogger(Gui.class);
    private static final String MAX_WIDTH = "Max Width";
	
	private final GeckoInstance gecko;

	private final JFrame mainframe;

	private final MultipleGenomesBrowser mgb;
	private final GeneClusterSelector gcSelector;
	private final GeneClusterDisplay gcDisplay;

	private final JLabel statusbartext;
	private final JLabel statusbaricon;
	private final JProgressBar progressbar;
    private boolean progressActive;

	private final JLabel infobar;
	private ImageIcon waitingAnimation;
	private final JCheckBox mgbViewSwitcher = new JCheckBox();
	private final JTextField searchField;

    private static final String[] GECKO_ICONS = {
            "images/geckoIcons/Gecko3.178.png",
            "images/geckoIcons/Gecko3.164.png",
            "images/geckoIcons/Gecko3.128.png",
            "images/geckoIcons/Gecko3.96.png",
            "images/geckoIcons/Gecko3.64.png",
            "images/geckoIcons/Gecko3.48.png",
            "images/geckoIcons/Gecko3.32.png",
            "images/geckoIcons/Gecko3.16.png"
    };

    /*
 * The users possibilities to interact with the ui depending on the current
 * state of the application are handled here
 */
    public enum Mode {
        /**
         * The application is currently performing the gene cluster computation
         */
        COMPUTING,DOING_STATISTICS
        /**
         * The application is idle, a session is currently open
         */
        ,SESSION_IDLE
        /**
         * The application is idle, no session is open
         */
        ,NO_SESSION
        /**
         * The application is currently reading the genomes selected by the user
         */
        ,READING_GENOMES
        ,PREPARING_COMPUTATION
        ,FINISHING_COMPUTATION}
    private Mode mode;

    public JFrame getMainframe() {
		return mainframe;
	}
	
	public GeneClusterSelector getGcSelector() {
		return gcSelector;
	}
	
	private Gui() {
		this.gecko = GeckoInstance.getInstance();
		this.gecko.setGui(this);
		
		initActions(); 
		
		this.statusbaricon = new JLabel();
		statusbaricon.setEnabled(true);
		this.statusbartext = new JLabel();
		this.progressbar = new JProgressBar();
		progressbar.setMaximumSize(new Dimension(100, 30));
        this.waitingAnimation = createImageIcon("images/loading.gif");
		
		this.gcDisplay = new GeneClusterDisplay();
        searchField = new JTextField("");
		this.gcSelector = new GeneClusterSelector(searchField);
        gecko.addDataListener(gcSelector);
		
		// Basic frame settings
		
		mainframe = new JFrame("Gecko3");
		mainframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainframe.setPreferredSize(new Dimension(1024, 768));
		mainframe.setLayout(new BorderLayout());

		// SplitPane arrangements
		// splits the gui in the vertical half
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		horizontalSplit.setResizeWeight(0.5);
		
		// splits the gui in horizontal half
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		verticalSplit.setResizeWeight(0.5);
		verticalSplit.setTopComponent(gcSelector);
		verticalSplit.setBottomComponent(gcDisplay);

		// Menu arrangements
        JMenu menuFile = new JMenu("File");
        JMenu menuView = new JMenu("View");
        JMenu menuAbout = new JMenu("Help");
        JMenuBar menubar = new JMenuBar();
		menubar.add(menuFile);
		menubar.add(menuView);
		menubar.add(menuAbout);

		mgb = new MultipleGenomesBrowser();

		// Lowest component in the upper half of the window
		final JScrollPane genomeBrowserScrollPane = new JScrollPane(mgb);
        genomeBrowserScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        genomeBrowserScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		
		//Toolbar
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.LINE_AXIS));
		toolbar.add(importGenomesAction);
		toolbar.add(saveSessionAction);
		
		toolbar.add(new JToolBar.Separator());
		menuFile.add(importGenomesAction);
		menuFile.add(saveSessionAction);
		menuFile.add(exportResultsAction);
		menuFile.add(loadClusterAnnotationsAction);
		menuFile.addSeparator();
		menuFile.add(exitAction);
		
		toolbar.add(clearSelectionAction);
		toolbar.add(startComputation);
		toolbar.add(stopComputationAction);
		
		toolbar.add(new JToolBar.Separator());
		toolbar.add(zoomIn);
		toolbar.add(zoomOut);

        JMenu nameWidthChooser = new JMenu("Gene Width");
        ButtonGroup group = new ButtonGroup();
        boolean useMaxLength = true;
        for (int i=4; i<=12; i+=2) {
            JRadioButtonMenuItem radioButtonMenuItem = new JRadioButtonMenuItem(Integer.toString(i));
            radioButtonMenuItem.addActionListener(changeGeneWidth);
            if (i==gecko.DEFAULT_MAX_GENE_NAME_LENGTH) {
                radioButtonMenuItem.setSelected(true);
                useMaxLength = false;
            }
            group.add(radioButtonMenuItem);
            nameWidthChooser.add(radioButtonMenuItem);
        }
        JRadioButtonMenuItem radioButtonMenuItem = new JRadioButtonMenuItem(MAX_WIDTH);
        radioButtonMenuItem.addActionListener(changeGeneWidth);
        radioButtonMenuItem.setSelected(useMaxLength);
        group.add(radioButtonMenuItem);
        nameWidthChooser.add(radioButtonMenuItem);
        menuView.add(nameWidthChooser);

        JMenu nameChooser = new JMenu("Gene Display Type");
        group = new ButtonGroup();
        boolean first = true;
        for (GenomePainting.NameType nameType : GenomePainting.NameType.values()) {
            radioButtonMenuItem = new JRadioButtonMenuItem(nameType.toString());
            radioButtonMenuItem.addActionListener(changeNameType);
            group.add(radioButtonMenuItem);
            nameChooser.add(radioButtonMenuItem);
            radioButtonMenuItem.setSelected(first);
            first = false;
        }
        menuView.add(nameChooser);
        menuView.addSeparator();
		menuView.add(zoomIn);
		menuView.add(zoomOut);
		
		menuAbout.add(aboutAction);
		menuAbout.add(showHomePage);
				
		JToggleButton animationButton = new JToggleButton("Animation");
		animationButton.setSelected(gecko.isAnimationEnabled());
		toolbar.add(new JToolBar.Separator());
		toolbar.add(animationButton);
		
		toolbar.add(new JToolBar.Separator());
		
		mgbViewSwitcher.setText("Hide unclustered genomes");
		mgbViewSwitcher.setToolTipText("Hides all genomes which are not in the currently selected cluster.");
		
		mgbViewSwitcher.addItemListener(new ItemListener()
		{

			@Override
			public void itemStateChanged(ItemEvent arg0) 
			{	
				int status = arg0.getStateChange();
	            JCheckBox mgbViewSwitcher2 = (JCheckBox) arg0.getItemSelectable();
		    
	            if(mgbViewSwitcher2 == Gui.this.mgbViewSwitcher) {
	            	if (status == ItemEvent.DESELECTED)
	            		mgb.hideNonClusteredGenomes(false);
	            	else
	            		mgb.hideNonClusteredGenomes(true);
	            }
			}
			
		});
		
		toolbar.add(mgbViewSwitcher);
		toolbar.add(Box.createHorizontalGlue());
		toolbar.add(new JLabel("Search "));
		
		animationButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				GeckoInstance.getInstance().setAnimationEnabled(((JToggleButton) e.getSource()).isSelected());
			}
		});
	
		JPanel p = new JPanel(new BorderLayout());
		searchField.setPreferredSize(new Dimension(150, toolbar.getHeight()));
		
		p.setMaximumSize(new Dimension(150, (int) toolbar.getPreferredSize().getHeight()));
		p.add(searchField, BorderLayout.CENTER);
		toolbar.add(p);
		
		// Add components to Frame
		JPanel northpanel = new JPanel();
		northpanel.setLayout(new GridLayout(1, 1));
		mainframe.setJMenuBar(menubar);
		northpanel.add(toolbar);
		mainframe.add(northpanel, BorderLayout.NORTH);
		
		horizontalSplit.setTopComponent(genomeBrowserScrollPane);
		horizontalSplit.setBottomComponent(verticalSplit);
		mainframe.add(horizontalSplit, BorderLayout.CENTER);
		JPanel southpanel = new JPanel();
		southpanel.setLayout(new BoxLayout(southpanel, BoxLayout.LINE_AXIS));
		
		JPanel statusbar = new JPanel();
		BoxLayout box = new BoxLayout(statusbar,BoxLayout.X_AXIS);
		statusbar.setLayout(box);
		statusbar.add(Box.createRigidArea(new Dimension(5, 0)));
		statusbar.add(statusbaricon);
		statusbar.add(progressbar);
		statusbar.add(Box.createRigidArea(new Dimension(5, 0)));
		statusbar.add(statusbartext);
		southpanel.add(statusbar);
		
		JPanel infobarPanel = new JPanel();
		infobarPanel.setLayout(new BoxLayout(infobarPanel, BoxLayout.LINE_AXIS));
		infobar = new JLabel();
		
		infobarPanel.add(Box.createGlue());
		infobarPanel.add(infobar);
		infobarPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		
		southpanel.add(Box.createRigidArea(new Dimension(5, 0)));
		southpanel.add(infobarPanel);
		mainframe.add(southpanel,BorderLayout.SOUTH);
		changeMode(Mode.NO_SESSION);

		
		// Listener stuff
		gcSelector.addSelectionListener(gcDisplay);
		gcSelector.addSelectionListener(mgb);
		mgb.addSelectionListener(gcDisplay);

		
		// Show JFrame
		mainframe.pack();
		mainframe.setLocationRelativeTo(null);
		mainframe.setIconImages(createGeckoImages());
		mainframe.setVisible(true);
        horizontalSplit.setDividerLocation(0.5);
        verticalSplit.setDividerLocation(0.5);
        // Update data
        gecko.setGeckoInstanceData();
	}

    public void setInfobarText(String text) {
		infobar.setText(text);
	}
	
	public static void startUp() {
		new Gui();
	}
	
	private void selectAndImportGenomes(CogFileReader reader) {
		new GenomeSelector(reader, this.getMainframe());
	}

    public void setProgressStatus(final int value, final AlgorithmStatusEvent.Task task) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (task) {
                    case Init:
                        initProgressbar(value);
                        break;
                    case ComputingClusters:
                        if (setProgressValue(value))
                            changeMode(Gui.Mode.COMPUTING);
                        break;
                    case ComputingStatistics:
                        if (setProgressValue(value))
                            changeMode(Gui.Mode.DOING_STATISTICS);
                        break;
                    case Done:
                        disableProgressBar();
                        changeMode(Gui.Mode.FINISHING_COMPUTATION);
                        break;
                }
            }
        });
    }

    private void initProgressbar(int maxValue){
        progressActive = true;
        progressbar.setMaximum(maxValue);
        progressbar.setValue(0);
    }

    private boolean setProgressValue(int value) {
        if (!progressActive)
            return false;
        progressbar.setValue(value);
        return true;
    }

    public void disableProgressBar(){
        progressActive = false;
    }

	/**public JProgressBar getProgressbar() {
		return progressbar;
	}*/
	
	public void updateViewscreen() {
        if (mgb != null) {
            mgb.setGenomes(gecko.getGenomes());
        }
	}
	
	public MultipleGenomesBrowser getMgb() {
		return mgb;
	}
	
	/**
	 * Simple helper method which makes it easier to create ImageIcons from Resource
     * @param path the path
     * @return Returns a ImageIcon if path describes an image, null otherwise
     */
	private static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL =  ClassLoader.getSystemResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			logger.warn("Couldn't find file: {}", path);
			return null;
		}
	}

    /**
     * Helper method that creates a list of Images
     * @return
     */
    private static List<? extends Image> createGeckoImages() {
        List<Image> images = new ArrayList<>();
        for (String path : GECKO_ICONS){
            ImageIcon icon = createImageIcon(path);
            if (icon != null)
                images.add(icon.getImage());
        }
        return images;
    }

    public void clearSelection() {
        getGcSelector().clearSelection();
    }
	
	private void changeGuiMode(final String text,
                               final boolean icon,
                               final boolean stopAndProgress,
                               final boolean importGenomes,
                               final boolean clusterBrowserActive){
		if (icon)
		{
			statusbaricon.setIcon(waitingAnimation);
		}
		statusbaricon.setVisible(icon);
		statusbartext.setText(text);
		
		progressbar.setVisible(stopAndProgress);
		progressbar.setVisible(stopAndProgress);
		stopComputationAction.setEnabled(stopAndProgress);
				
		importGenomesAction.setEnabled(importGenomes);
		
        startComputation.setEnabled(clusterBrowserActive);
		clearSelectionAction.setEnabled(clusterBrowserActive);
		saveSessionAction.setEnabled(clusterBrowserActive);
		if (clusterBrowserActive) {
			zoomIn.setEnabled(mgb.canZoomIn());
			zoomOut.setEnabled(mgb.canZoomOut());
		} else {
			zoomIn.setEnabled(false);
			zoomOut.setEnabled(false);
		}
		mgbViewSwitcher.setEnabled(clusterBrowserActive);
		searchField.setEnabled(clusterBrowserActive);
		exportResultsAction.setEnabled(clusterBrowserActive);
		loadClusterAnnotationsAction.setEnabled(clusterBrowserActive);
	}
	
	public void changeMode(Mode mode) {
        if (!mode.equals(this.mode)){
            switch (mode) {
                case COMPUTING:
                    changeGuiMode("Computing gene clusters...", false, true, false, false);
                    break;
                case DOING_STATISTICS:
                    changeGuiMode("Computing cluster statistics...", false, true, false, false);
                    break;
                case SESSION_IDLE:
                    changeGuiMode("Ready", false, false, true, true);
                    break;
                case NO_SESSION:
                    changeGuiMode("Ready", false, false, true, false);
                    break;
                case READING_GENOMES:
                    changeGuiMode("Reading genomes...", true, false, false, false);
                    break;
                case PREPARING_COMPUTATION:
                    changeGuiMode("Preparing data...", true, false, false, false);
                    break;
                case FINISHING_COMPUTATION:
                    changeGuiMode("Finishing...", true, false, false, false);
                    break;
            }
            this.mode = mode;
        }
	}

	public void closeCurrentSession() {
        gecko.setGeckoInstanceData(DataSet.getEmptyDataSet());
		mgb.clear();
	}
	
	/*
	 * The following section contains the actions that the user can trigger
	 */

	private final Action stopComputationAction = new AbstractAction() {

		private static final long serialVersionUID = -6567239762573695048L;

		public void actionPerformed(ActionEvent e) {
			gecko.stopComputation();
		}
	};

	private final Action importGenomesAction = new AbstractAction() {
		private static final long serialVersionUID = -7418023194238092616L;
		
		public void actionPerformed(ActionEvent e) {
			final JFileChooser fc = new JFileChooser(gecko.getCurrentWorkingDirectoryOrFile());
            fc.setAcceptAllFileFilterUsed(false);
			fc.addChoosableFileFilter(new FileUtils.GenericFilter("cog;gck"));

			int state = fc.showOpenDialog( null );
			if (state == JFileChooser.APPROVE_OPTION) {
				try {
					// Close the current session
					closeCurrentSession();
					// Check what type of file we are opening
					if (FileUtils.getExtension(fc.getSelectedFile()).equals("cog")) {
						gecko.setCurrentWorkingDirectoryOrFile(fc.getSelectedFile());
                        GeckoInstance.getInstance().getGui().changeMode(Gui.Mode.READING_GENOMES);

						CogFileReader reader = new CogFileReader(fc.getSelectedFile());
						reader.importGenomesOccs();
						selectAndImportGenomes(reader);
					} else {
						if (FileUtils.getExtension(fc.getSelectedFile()).equals("gck")) {
                            Gui.this.gecko.setCurrentWorkingDirectoryOrFile(fc.getSelectedFile());
                            GeckoInstance.getInstance().getGui().changeMode(Gui.Mode.READING_GENOMES);

                            SwingWorker worker = new SwingWorker<Void, Void>() {
                                GckFileReader reader = new GckFileReader(fc.getSelectedFile());
                                DataSet data;
                                @Override
                                protected Void doInBackground() {
                                    try{
                                        data = reader.readData();
                                    } catch (final IOException e) {
                                        EventQueue.invokeLater(new Runnable() {
                                            public void run() {
                                            e.printStackTrace();
                                            JOptionPane.showMessageDialog(GeckoInstance.getInstance().getGui().getMainframe(),
                                                    "An error occured while reading the file!",
                                                    "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                            }
                                        });
                                    } catch (final ParseException e) {
                                        EventQueue.invokeLater(new Runnable() {
                                            public void run() {
                                                e.printStackTrace();
                                                JOptionPane.showMessageDialog(GeckoInstance.getInstance().getGui().getMainframe(),
                                                    "The input file is not in the right format!",
                                                    "Wrong format",
                                                    JOptionPane.ERROR_MESSAGE);
                                            }
                                        });
                                    }
                                    return null;
                                }

                                @Override
                                public void done() {
                                    try {
                                        get();
                                    } catch (InterruptedException | ExecutionException e) {
                                        JOptionPane.showMessageDialog(mainframe, e.getMessage(), "Exception Occurred", JOptionPane.ERROR_MESSAGE);
                                        logger.error("Reader error", e);
                                    }
                                    GeckoInstance.getInstance().setGeckoInstanceData(data);
                                }
                            };

                            worker.execute();
						}
					}
				} catch (FileNotFoundException ex) {
					JOptionPane.showMessageDialog(mainframe,"File not found", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			mainframe.requestFocus();
		}	
	};
	
	private final Action clearSelectionAction = new AbstractAction() {
		private static final long serialVersionUID = 6179596178200200696L;
		public void actionPerformed(ActionEvent e) {
			mgb.clearSelection();
		}
		
	};

    private final Action changeGeneWidth = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getActionCommand().equals(MAX_WIDTH))
                gecko.setMaxGeneNameLength(Integer.MAX_VALUE);
            else
                gecko.setMaxGeneNameLength(Integer.parseInt(actionEvent.getActionCommand()));
            mgb.updateGeneWidth();
        }
    };

    private final Action changeNameType = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            mgb.changeNameType(GenomePainting.NameType.getNameTypeFromString(actionEvent.getActionCommand()));
        }
    };
	
	private final Action startComputation = new AbstractAction() {
		private static final long serialVersionUID = -1530838105061978403L;
		public void actionPerformed(ActionEvent e) {
			StartComputationDialog d = gecko.getStartComputationDialog();
			d.setLocationRelativeTo(Gui.this.mainframe);
			d.setVisible(true);
			
			// Set the check box enabled
			Gui.this.mgbViewSwitcher.setEnabled(true);
		}
	};
	
	private final Action exitAction = new AbstractAction() {

		/**
		 * Random generated serial version uid
		 */
		private static final long serialVersionUID = 196167012152483868L;

		@Override
		public void actionPerformed(ActionEvent arg0) {

			System.exit(0);
		}
	};
	
	private final Action aboutAction = new AbstractAction() {

		/**
		 * Random generated serial version uid
		 */
		private static final long serialVersionUID = -5982961195947652321L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JDialog about = new JDialog();
			about.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			about.getRootPane().setBackground(about.getContentPane().getBackground());
			about.setIconImages(Gui.this.getMainframe().getIconImages());
			about.setTitle("About Gecko3");
			about.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			about.setLayout(new BorderLayout());
			
			JLabel iconLabel = new JLabel();
			iconLabel.setIcon(createImageIcon("images/geckoIcons/Gecko3.178.png"));
			JPanel iconPanel = new JPanel();
			iconPanel.add(iconLabel);
			about.add(iconPanel, BorderLayout.NORTH);
			
			JPanel authorPane = new JPanel();
			JEditorPane text = new JEditorPane();
			text.setContentType("text/html");
			//text.addHyperlinkListener(new HyperlinkListener());
			text.setText(
			"<html>" +
				"<body>" +
					"<center>" +
						"Gecko 3" +
						"<br>" +
						"Tool for searching gene clusters" +
						"<br>" +
						"Version 1.0" +
						"<br>" +
						"2014, Sascha Winter, Hans-Martin Haase and Tobias Mann" +
						"<br>" +
						"Chair of Bioinformatics, Friedrich-Schiller-Universität Jena" +
						"<br>" +
						"http://bio.informatik.uni-jena.de" +
						"<br><br>" +
						"This program is based on Gecko2 by <br>" +
						"Katharina Jahn and Leon Kuchenbecker" +
					"</center>" +
				"</body>" +
			"</html>");
	
			text.setEditable(false);
			text.setBackground(about.getContentPane().getBackground());
			authorPane.add(text, BorderLayout.CENTER);
			about.add(authorPane);
			about.pack();
			about.setVisible(true);
		}
	};
	
	private final Action showHomePage = new AbstractAction() {

		/**
		 * Random generated serial version uid
		 */
		private static final long serialVersionUID = 3693048160852637628L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			// fix the url for gecko just linking to the bioinformatic page
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				
				try {
					desktop.browse(new URI("http://bio.informatik.uni-jena.de/software/gecko3"));
				} 
				catch (IOException e) {
					new JOptionPane("This option requires a internet connection.", JOptionPane.ERROR_MESSAGE);	
				} 
				catch (URISyntaxException e) {
					new JOptionPane("Wrong URI syntax.", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		}
	};
	
	private final Action zoomIn = new AbstractAction() {
		private static final long serialVersionUID = 6370914610738020426L;
		public void actionPerformed(ActionEvent e) {
			Gui.this.mgb.changeGeneElementHeight(+2);
			this.setEnabled(mgb.canZoomIn());
			zoomOut.setEnabled(mgb.canZoomOut());
		}
	};
	
	private final Action zoomOut = new AbstractAction() {
		private static final long serialVersionUID = -5867464557496189529L;
		public void actionPerformed(ActionEvent e) {
			Gui.this.mgb.changeGeneElementHeight(-2);
			this.setEnabled(mgb.canZoomOut());
			zoomIn.setEnabled(mgb.canZoomIn());
		}
	};

	private final Action exportResultsAction = new AbstractAction() {

		private static final long serialVersionUID = 3693048160852637628L;

		public void actionPerformed(ActionEvent e) {
			ResultExportDialog resultExportDialog = new ResultExportDialog(Gui.this.getMainframe());
			resultExportDialog.setVisible(true);
		}
	};
	
	private final Action saveSessionAction = new AbstractAction() {
		private static final long serialVersionUID = -1530838105061978403L;
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser(gecko.getCurrentWorkingDirectoryOrFile());
			
			for (FileFilter f : fc.getChoosableFileFilters())
				fc.removeChoosableFileFilter(f);
			
			fc.addChoosableFileFilter(new FileUtils.GenericFilter("gck"));
				
			for (;;) {
				int state = fc.showSaveDialog(null);
				if (state == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					
					if (! fc.getFileFilter().accept(fc.getSelectedFile())) {
                        f = new File(f.getAbsolutePath() + ".gck");
                        gecko.setCurrentWorkingDirectoryOrFile(f);
                    }
					
					if (f.exists()) {
						if (f.isDirectory()) {
							JOptionPane.showMessageDialog(mainframe, "You cannot choose a directory", "Error", JOptionPane.ERROR_MESSAGE);
							continue;
						}
						
						int x = JOptionPane.showConfirmDialog(mainframe, 
								"The chosen file already exists. Overwrite?", 
								"Overwrite existing file?", 
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
						
						if (x == JOptionPane.NO_OPTION) 
							continue;
					}
					
					if (! DataSetWriter.saveDataSetToFile(gecko.getData(), f))
						JOptionPane.showMessageDialog(mainframe, "An error occured while writing the file!", "Error", JOptionPane.ERROR_MESSAGE);
					
					break;
				} else break;
			}
			mainframe.requestFocus();
		}
	};
	
	private final Action loadClusterAnnotationsAction = new AbstractAction()
	{
		
		private static final long serialVersionUID = 1148103871109191664L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser(gecko.getCurrentWorkingDirectoryOrFile());
			
			for (;;) {	
				int state = fc.showOpenDialog(null);
				
				if (state == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
                    gecko.setCurrentWorkingDirectoryOrFile(f);
					
					if (f.exists()) {	
						if (f.isDirectory()) {
							JOptionPane.showMessageDialog(mainframe, "You cannot choose a directory", "Error", JOptionPane.ERROR_MESSAGE);
							continue;
						}

						GeckoInstance geckoInstance = GeckoInstance.getInstance();
						List<GeneCluster> newCluster = ClusterAnnotationReader.readClusterAnnotations(f, geckoInstance.getData());
						
						if (newCluster == null)
							JOptionPane.showMessageDialog(mainframe, "An error occured while reading the annotations!", "Error", JOptionPane.ERROR_MESSAGE);
						else {
                            List<GeneCluster> clusterWithPValue = geckoInstance.computeReferenceStatistics(newCluster);
							geckoInstance.mergeClusters(clusterWithPValue, null);
						}									
						break;
					}
				}
				else 
				{
					break;
				}
			}
		}
	};

	
	private void initActions() {
		
		stopComputationAction.putValue(Action.NAME, "Stop current computation...");
		stopComputationAction.putValue(Action.SHORT_DESCRIPTION, "Stop");
		stopComputationAction.putValue(Action.SMALL_ICON, createImageIcon("images/player_stop.png"));
		stopComputationAction.setEnabled(false);
		
		importGenomesAction.putValue(Action.NAME, "Open session or genome file...");
		importGenomesAction.putValue(Action.SHORT_DESCRIPTION, "Open...");	
		importGenomesAction.putValue(Action.SMALL_ICON, createImageIcon("images/fileopen.png"));
		importGenomesAction.putValue(Action.LARGE_ICON_KEY, createImageIcon("images/fileopen_large.png"));
		importGenomesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		startComputation.putValue(Action.NAME, "Start computation...");
		startComputation.putValue(Action.SHORT_DESCRIPTION, "Start computation...");
		startComputation.putValue(Action.SMALL_ICON, createImageIcon("images/player_play.png"));
		startComputation.putValue(Action.LARGE_ICON_KEY, createImageIcon("images/player_play_large.png"));
		startComputation.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		startComputation.setEnabled(false);
		
		saveSessionAction.putValue(Action.NAME, "Save session...");
		saveSessionAction.putValue(Action.SHORT_DESCRIPTION, "Save session...");
		saveSessionAction.putValue(Action.SMALL_ICON, createImageIcon("images/filesave.png"));
		saveSessionAction.putValue(Action.LARGE_ICON_KEY, createImageIcon("images/filesave_large.png"));
		saveSessionAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		saveSessionAction.setEnabled(false);
		
		exportResultsAction.putValue(Action.NAME, "Export results...");
		exportResultsAction.putValue(Action.SHORT_DESCRIPTION, "Export results...");
		exportResultsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		saveSessionAction.setEnabled(false);
		
		loadClusterAnnotationsAction.putValue(Action.NAME, "Load clusters...");
		loadClusterAnnotationsAction.putValue(Action.SHORT_DESCRIPTION, "Load clusters...");
		loadClusterAnnotationsAction.setEnabled(false);
		
		clearSelectionAction.putValue(Action.SHORT_DESCRIPTION, "Clear selection");
		clearSelectionAction.putValue(Action.SMALL_ICON, createImageIcon("images/cancel.png"));
		clearSelectionAction.putValue(Action.LARGE_ICON_KEY, createImageIcon("images/cancel_large.png"));
		zoomIn.putValue(Action.NAME, "Zoom in");
		zoomIn.putValue(Action.SHORT_DESCRIPTION, "Zoom in");
		zoomIn.putValue(Action.SMALL_ICON, createImageIcon("images/viewmag+.png"));
		zoomIn.putValue(Action.LARGE_ICON_KEY, createImageIcon("images/viewmag+_large.png"));
		zoomIn.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		zoomOut.putValue(Action.NAME, "Zoom out");
		zoomOut.putValue(Action.SHORT_DESCRIPTION, "Zoom out");
		zoomOut.putValue(Action.SMALL_ICON, createImageIcon("images/viewmag-.png"));
		zoomOut.putValue(Action.LARGE_ICON_KEY, createImageIcon("images/viewmag-_large.png"));
		zoomOut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_MINUS,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		
		exitAction.putValue(Action.NAME, "Exit");
		exitAction.putValue(Action.SHORT_DESCRIPTION, "Leave the program");
		
		showHomePage.putValue(Action.NAME, "Gecko3 Website");
		showHomePage.putValue(Action.SHORT_DESCRIPTION, "Opens the Gecko3 website in the browser");
		
		aboutAction.putValue(Action.NAME, "About Gecko3...");
		
	}
}
