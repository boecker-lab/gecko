package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.GenomeOccurence;
import gecko2.algorithm.GeneCluster;
import gecko2.io.ClusterAnnotationReader;
import gecko2.io.CogFileReader;
import gecko2.io.GckFileReader;
import gecko2.io.SessionWriter;
import gecko2.util.FileUtils;
import gecko2.util.PrintUtils;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

public class Gui {
	
	private GeckoInstance gecko;

	private JFrame mainframe;
	private JMenuBar menubar;
	
	private JSplitPane horiSplit;
	private JSplitPane vertSplit;
	
	private JMenu menuFile, menuView, menuAbout;
	
	private AbstractMultipleGenomeBrowser mgb;
	private GenomeNavigator navigator;
	private GeneClusterSelector gcSelector;
	private GeneClusterDisplay gcDisplay;

	private JLabel statusbartext;
	private JLabel statusbaricon;
	private JProgressBar progressbar;
	
	private JLabel infobar;
	private ImageIcon waitingAnimation;
	private JCheckBox mgbViewSwitcher = new JCheckBox();
	private final JTextField searchField;

		
	public JFrame getMainframe() {
		return mainframe;
	}
	
	public GeneClusterSelector getGcSelector() {
		return gcSelector;
	}
	
	public GeneClusterDisplay getGcDisplay() {
		
		return gcDisplay;
	}
	
	public Gui() {
		this.gecko = GeckoInstance.getInstance();
		this.gecko.setGui(this);
		
		initActions(); 
		
		this.statusbaricon = new JLabel();
		statusbaricon.setEnabled(true);
		this.statusbartext = new JLabel();
		this.progressbar = new JProgressBar();
		progressbar.setMaximumSize(new Dimension(100, 30));
		progressbar.setValue(12);
		this.waitingAnimation = createImageIcon("images/ghost.png");
		
		this.gcDisplay = new GeneClusterDisplay();
		
		this.gcSelector = new GeneClusterSelector();
		Dimension startDimension = new Dimension(1024, 768);
		
		// Basic frame settings
		
		mainframe = new JFrame("Gecko\u00B2");
		mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainframe.setPreferredSize(startDimension);
		mainframe.setLayout(new BorderLayout());
		
		// The Splitpane that contains the two selectors (GeneClusterSelector
		// and OccurrenceSelector)
		JSplitPane selectorSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		selectorSplitPane.setTopComponent(this.gcSelector);
		OccurrenceSelector occurrenceSelector = new OccurrenceSelector(this.gcDisplay);
		selectorSplitPane.setBottomComponent(occurrenceSelector);
		this.gcSelector.addSelectionListener(occurrenceSelector);
				
		// SplitPane arrangements
		// splits the gui in the vertical half
		horiSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);	
		horiSplit.setResizeWeight(0.5);
		
		// splits the gui in horizontal half
		vertSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		vertSplit.setResizeWeight(0.5);
		this.gecko.setClusters(null);
		vertSplit.setTopComponent(selectorSplitPane);
		
		vertSplit.setBottomComponent(gcDisplay);

		// Menu arrangements
		menuFile = new JMenu("File");
		menuView = new JMenu("View");
		menuAbout = new JMenu("Help");
		menubar = new JMenuBar();
		menubar.add(menuFile);
		menubar.add(menuView);
		menubar.add(menuAbout);
		
				
		mgb = new MultipleGenomesBrowser();
		
		
		// Lowest component in the upper half of the window		
	
		final JScrollPane upscoll = new JScrollPane();
		upscoll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		upscoll.setViewportView(mgb);
		
		navigator = new GenomeNavigator();
		gecko.addDataListener(navigator);
		
		mgb.addBrowserContentListener(navigator);
		occurrenceSelector.addSelectionListener(mgb);
		
		JScrollPane navigatorScroll = new JScrollPane(navigator);
		
		// Panel under 
		final JSplitPane upperPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upscoll, navigatorScroll);
		// setResizeWight to give both components the same size
		upperPanel.setResizeWeight(0.5);
		
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
		    
	            if(mgbViewSwitcher2 == Gui.this.mgbViewSwitcher)
	            {
	            	if (status == ItemEvent.DESELECTED)
	            	{
	            		mgb.hideNonClusteredGenomes(false);
	            	}
	            	else 
	            	{	
	            		mgb.hideNonClusteredGenomes(true);
	            	}
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
		
		searchField = new JTextField("");
		searchField.setPreferredSize(new Dimension(150, toolbar.getHeight()));
		searchField.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				gecko.setFilterString(searchField.getText());
				searchField.setSelectionStart(0);
				searchField.setSelectionEnd(searchField.getText().length());
			}
		});
		
		p.setMaximumSize(new Dimension(150, (int) toolbar.getPreferredSize().getHeight()));
		p.add(searchField, BorderLayout.CENTER);
		toolbar.add(p);
		
		// END TEST
		
		// Add components to Frame
		JPanel northpanel = new JPanel();
		northpanel.setLayout(new GridLayout(1, 1));
		mainframe.setJMenuBar(menubar);
		northpanel.add(toolbar);
		mainframe.add(northpanel, BorderLayout.NORTH);
		
		horiSplit.setTopComponent(upperPanel);
		horiSplit.setBottomComponent(vertSplit);
		mainframe.add(horiSplit, BorderLayout.CENTER);
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
		occurrenceSelector.addSelectionListener(gcDisplay);
		gcSelector.addSelectionListener(gcDisplay);
		gcSelector.addSelectionListener(navigator);
		gcSelector.addSelectionListener(mgb);
		occurrenceSelector.addSelectionListener(navigator);
		mgb.addSelectionListener(gcDisplay);

		
		// Show JFrame
		mainframe.pack();
		mainframe.setLocationRelativeTo(null);
		mainframe.addKeyListener(mgb.getWheelListener());
		mainframe.setIconImage(createImageIcon("images/gecko2_a_small.png").getImage());
		mainframe.setVisible(true);
	}
	
	public void setInfobarText(String text) {
		infobar.setText(text);
	}
	
	public static void startUp() {
		new Gui();
	}
	
	private void selectGenomesForImport(ArrayList<GenomeOccurence> occs) {
		new GenomeSelector(occs, this.getMainframe());
	}
	
	public JProgressBar getProgressbar() {
		return progressbar;
	}
	
	public void updateViewscreen() {
		this.mgb.clear();
		if (gecko.getGenomes()!=null)
			this.mgb.addGenomes(gecko.getGenomes());
	}
	
//	public void refreshClusterList() {
//		this.gcSelector.refresh();
//	}
	
	public AbstractMultipleGenomeBrowser getMgb() {
		return mgb;
	}
	
	/** Simple helper methods which makes it easier to create ImageIcons from Resource
	 *
     * @param String path 
     * @return Returns a imageicon if path describes an image, null otherwise 
     */
	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL =  ClassLoader.getSystemResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	
	/*
	 * The users possibilities to interact with the ui depending on the current
	 * state of the application are handled here
	 */
	
	/**
	 * The application is currently performing the gene cluster computation
	 */
	public enum Mode {COMPUTING
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
	
	private void changeMode(final String text,
			final boolean icon,
			final boolean stopAndProgress,
			final boolean importGenomes,
			final boolean clusterBrowserActive){
		if (icon)
		{
			this.waitingAnimation = createImageIcon("images/loading.gif");
			statusbaricon.setIcon(waitingAnimation);
		}
		statusbaricon.setVisible(icon);
		statusbartext.setText(text);
		
		progressbar.setVisible(stopAndProgress);
		progressbar.setVisible(stopAndProgress);
		stopComputationAction.setEnabled(stopAndProgress);
				
		importGenomesAction.setEnabled(importGenomes);
		
		if (gecko.isLibgeckoLoaded())
			startComputation.setEnabled(clusterBrowserActive);
		clearSelectionAction.setEnabled(clusterBrowserActive);
		saveSessionAction.setEnabled(clusterBrowserActive);
		if (clusterBrowserActive) {
			zoomIn.setEnabled(gecko.canZoomIn());
			zoomOut.setEnabled(gecko.canZoomOut());
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
		switch (mode) {
			case COMPUTING:
				changeMode("Computing gene clusters...", false, true, false, false);
				break;
			case SESSION_IDLE:
				changeMode("Ready", false, false, true, true);
				break;
			case NO_SESSION:
				changeMode("Ready", false, false, true, false);
				break;
			case READING_GENOMES:
				changeMode("Reading genomes...", true, false, false, false);
				break;
			case PREPARING_COMPUTATION:
				changeMode("Preparing data...", true, false, false, false);
				break;
			case FINISHING_COMPUTATION:
				changeMode("Finishing...", true, false, false, false);
				break;
		}
	}
	
	public void handleFileError(short error) {
		closeCurrentSession();
		changeMode(Mode.NO_SESSION);		
		JOptionPane.showMessageDialog(mainframe, "The input file is not a valid COG file", "Wrong file format", JOptionPane.ERROR_MESSAGE);
	}

	public void closeCurrentSession() {
		gecko.setClusters(null);
		gecko.setGenomes(null);
		gcSelector.refresh();
		mgb.clear();
	}
	
	public void updategcSelector() {
		gcSelector.refresh();
	}
	
	/*
	 * The following section contains the actions that the user can trigger
	 */
	
	private Action stopComputationAction = new AbstractAction() {

		private static final long serialVersionUID = -6567239762573695048L;

		public void actionPerformed(ActionEvent e) {
			gecko.getLastParameter().setRun(false);
		}	
	};
	
	private Action importGenomesAction = new AbstractAction() {
		private static final long serialVersionUID = -7418023194238092616L;
		
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.addChoosableFileFilter(new FileUtils.GenericFilter("cog;gck"));
			if (gecko.getLastOpenedFile()!=null)
				fc.setSelectedFile(gecko.getLastOpenedFile());

			int state = fc.showOpenDialog( null );
			if (state == JFileChooser.APPROVE_OPTION) {
				try {
					// Close the current session
					closeCurrentSession();
					// Check what type of file we are opening
					if (FileUtils.getExtension(fc.getSelectedFile()).equals("cog")) {
						Gui.this.gecko.setCurrentInputFile(fc.getSelectedFile());
						CogFileReader reader = new CogFileReader((byte) 0);
						ArrayList<GenomeOccurence> list = reader.importGenomes(fc.getSelectedFile());
						selectGenomesForImport(list);
					} else {
						if (FileUtils.getExtension(fc.getSelectedFile()).equals("gck")) {
							GckFileReader reader = new GckFileReader();
							
							reader.loadSessionFromFile(fc.getSelectedFile());
							gecko.setGeneLabelMap(reader.getGeneLabelMap());
						}
					}
				} catch (FileNotFoundException ex) {
					JOptionPane.showMessageDialog(mainframe,"File not found", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			mainframe.requestFocus();
		}	
	};
	
	private Action clearSelectionAction = new AbstractAction() {
		private static final long serialVersionUID = 6179596178200200696L;
		public void actionPerformed(ActionEvent e) {
			mgb.clearSelection();
		}
		
	};
	
	private Action startComputation = new AbstractAction() {
		private static final long serialVersionUID = -1530838105061978403L;
		public void actionPerformed(ActionEvent e) {
			StartComputationDialog d = gecko.getStartComputationDialog();
			d.setLocationRelativeTo(Gui.this.mainframe);
			d.setVisible(true);
			
			// Set the check box enabled
			Gui.this.mgbViewSwitcher.setEnabled(true);
		}
	};
	
	private Action exitAction = new AbstractAction() {

		/**
		 * Random generated serial version uid
		 */
		private static final long serialVersionUID = 196167012152483868L;

		@Override
		public void actionPerformed(ActionEvent arg0) {

			System.exit(0);
		}
	};
	
	private Action aboutAction = new AbstractAction() {

		/**
		 * Random generated serial version uid
		 */
		private static final long serialVersionUID = -5982961195947652321L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			JDialog about = new JDialog();
			about.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			about.getRootPane().setBackground(about.getContentPane().getBackground());
			about.setIconImage(createImageIcon("images/gecko2_a_small.png").getImage());
			about.setTitle("About Gecko²");
			about.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			about.setPreferredSize(new Dimension(350, 475));
			about.setSize(new Dimension(350, 475));
			about.setLayout(new BorderLayout());
			
			JLabel iconLabel = new JLabel();
			iconLabel.setIcon((Icon) createImageIcon("images/gecko2_a_small.png"));
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
						"Gecko²" +
						"<br>" +
						"Tool for searching gene clusters" +
						"<br>" +
						"Version ???" +
						"<br>" +
						"2013, Sascha Winter, Hans-Martin Haase" +
						"<br>" +
						"Chair of Bioinformatics, University of Jena." +
						"<br>" +
						"http://bio.informatik.uni-jena.de" +
						"<br> <br>" +
						"Uses the iText library" +
						"<br>" +
						"<i>http://sourceforge.net/projects/itext/</i>" + 
						"<br><br>" +
						"JGoodies Forms library" +
						"<br>" +
						"<i>www.jgoodies.com/freeware/libraries/forms/</i>" +
						"<br<br>" +
						"colt library" +
						"<br>" +
						"<i>http://acs.lbl.gov/software/colt/index.html</i>" +
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
			about.setVisible(true);
		}
	};
	
	private Action showHomePage = new AbstractAction() {

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
					
					desktop.browse(new URI("http://bio.informatik.uni-jena.de/"));
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
	
	private Action zoomIn = new AbstractAction() {
		private static final long serialVersionUID = 6370914610738020426L;
		public void actionPerformed(ActionEvent e) {
			Gui.this.mgb.changeGeneElementHight(+2);
			this.setEnabled(gecko.canZoomIn());
			zoomOut.setEnabled(gecko.canZoomOut());
		}
	};
	
	private Action zoomOut = new AbstractAction() {
		private static final long serialVersionUID = -5867464557496189529L;
		public void actionPerformed(ActionEvent e) {
			Gui.this.mgb.changeGeneElementHight(-2);
			this.setEnabled(gecko.canZoomOut());
			zoomIn.setEnabled(gecko.canZoomIn());
		}
	};

	private Action exportResultsAction = new AbstractAction() {

		private static final long serialVersionUID = 3693048160852637628L;

		public void actionPerformed(ActionEvent e) {
			ResultExportDialog resultExportDialog = new ResultExportDialog(Gui.this.getMainframe());
			resultExportDialog.setVisible(true);
		}
	};
	
	private Action saveSessionAction = new AbstractAction() {
		private static final long serialVersionUID = -1530838105061978403L;
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			
			for (FileFilter f : fc.getChoosableFileFilters())
				fc.removeChoosableFileFilter(f);
			
			fc.addChoosableFileFilter(new FileUtils.GenericFilter("gck"));
			if (gecko.getLastSavedFile()!=null)
				fc.setSelectedFile(gecko.getLastSavedFile());
				
			for (;;) {
				int state = fc.showSaveDialog(null);
				if (state == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					
					if (! fc.getFileFilter().accept(fc.getSelectedFile()))
						f = new File(f.getAbsolutePath() + ".gck");
					
					PrintUtils.printDebug("Choosen file to save to: " + f);
					
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
					
					if (! SessionWriter.saveSessionToFile(f))
						JOptionPane.showMessageDialog(mainframe, "An error occured while writing the file!", "Error", JOptionPane.ERROR_MESSAGE);
					
					break;
				} else break;
			}
			mainframe.requestFocus();
		}
	};
	
	private Action loadClusterAnnotationsAction = new AbstractAction() 
	{
		
		private static final long serialVersionUID = 1148103871109191664L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			
			for (;;) {	
				int state = fc.showOpenDialog(null);
				
				if (state == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					
					if (f.exists()) {	
						if (f.isDirectory()) {
							JOptionPane.showMessageDialog(mainframe, "You cannot choose a directory", "Error", JOptionPane.ERROR_MESSAGE);
							continue;
						}
						
						GeckoInstance geckoInstance = GeckoInstance.getInstance();
						List<GeneCluster> newCluster = ClusterAnnotationReader.readClusterAnnotations(f, geckoInstance.getGenomes());
						
						if (newCluster == null)
							JOptionPane.showMessageDialog(mainframe, "An error occured while reading the annotations!", "Error", JOptionPane.ERROR_MESSAGE);
						else {
							GeneCluster[] clusterWithPValue = geckoInstance.computeReferenceStatistics(newCluster.toArray(new GeneCluster[newCluster.size()]));
							geckoInstance.setClusters(GeneCluster.mergeResults(geckoInstance.getClusters(), clusterWithPValue));
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
		importGenomesAction.putValue(Action.SMALL_ICON, createImageIcon("images/fileopen_large.png"));
		importGenomesAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		startComputation.putValue(Action.NAME, "Start computation...");
		startComputation.putValue(Action.SHORT_DESCRIPTION, "Start computation...");
		startComputation.putValue(Action.SMALL_ICON, createImageIcon("images/player_play.png"));
		startComputation.putValue(Action.SMALL_ICON, createImageIcon("images/player_play_large.png"));
		startComputation.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		startComputation.setEnabled(false);
		
		saveSessionAction.putValue(Action.NAME, "Save session...");
		saveSessionAction.putValue(Action.SHORT_DESCRIPTION, "Save session...");
		saveSessionAction.putValue(Action.SMALL_ICON, createImageIcon("images/filesave.png"));
		saveSessionAction.putValue(Action.SMALL_ICON, createImageIcon("images/filesave_large.png"));
		saveSessionAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		saveSessionAction.setEnabled(false);
		
		exportResultsAction.putValue(Action.NAME, "Export results...");
		exportResultsAction.putValue(Action.SHORT_DESCRIPTION, "Export results...");
		exportResultsAction.putValue(Action.SMALL_ICON, createImageIcon("images/fileexport_large.png"));
		exportResultsAction.putValue(Action.SMALL_ICON, createImageIcon("images/fileexport_large.png"));
		exportResultsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		saveSessionAction.setEnabled(false);
		
		loadClusterAnnotationsAction.putValue(Action.NAME, "Load cluster annotations...");
		loadClusterAnnotationsAction.putValue(Action.NAME, "Load clusters...");
		loadClusterAnnotationsAction.setEnabled(false);
		
		clearSelectionAction.putValue(Action.SHORT_DESCRIPTION, "Clear selection");
		clearSelectionAction.putValue(Action.SMALL_ICON, createImageIcon("images/cancel.png"));
		clearSelectionAction.putValue(Action.SMALL_ICON, createImageIcon("images/cancel_large.png"));
		zoomIn.putValue(Action.NAME, "Zoom in");
		zoomIn.putValue(Action.SHORT_DESCRIPTION, "Zoom in");
		zoomIn.putValue(Action.SMALL_ICON, createImageIcon("images/viewmag+.png"));
		zoomIn.putValue(Action.SMALL_ICON, createImageIcon("images/viewmag+_large.png"));
		zoomIn.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		zoomOut.putValue(Action.NAME, "Zoom out");
		zoomOut.putValue(Action.SHORT_DESCRIPTION, "Zoom out");
		zoomOut.putValue(Action.SMALL_ICON, createImageIcon("images/viewmag-.png"));
		zoomOut.putValue(Action.SMALL_ICON, createImageIcon("images/viewmag-_large.png"));
		zoomOut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_MINUS,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		
		exitAction.putValue(Action.NAME, "Exit");
		exitAction.putValue(Action.SHORT_DESCRIPTION, "Leave the program");
		
		showHomePage.putValue(Action.NAME, "Gecko² Website");
		showHomePage.putValue(Action.SHORT_DESCRIPTION, "Opens the Gecko² website in the browser");
		
		aboutAction.putValue(Action.NAME, "About Gecko²...");
		
	}
}
