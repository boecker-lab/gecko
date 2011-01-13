package gecko2.gui;

import gecko2.GeckoInstance;
import gecko2.GenomeOccurence;
import gecko2.algorithm.Genome;
import gecko2.util.FileUtils;
import gecko2.util.PrintUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
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
	
	private JMenu menuFile, menuView;
	
	private MultipleGenomesBrowser mgb;
	private GeneClusterSelector gcSelector;
	private GeneClusterDisplay gcDisplay;

	private JLabel statusbartext;
	private JLabel statusbaricon;
	private JProgressBar progressbar;
	
	private JLabel infobar;
	private ImageIcon waitingAnimation;
	private final JTextField searchField;
	private GenomeNavigator navigator;
		
	public JFrame getMainframe() {
		return mainframe;
	}
	
	public GeneClusterSelector getGcSelector() {
		return gcSelector;
	}
	
	public Gui() {
		this.gecko = GeckoInstance.getInstance();
		this.gecko.setGui(this);
		
		initActions(); 
		
		this.statusbaricon = new JLabel();
		this.statusbartext = new JLabel();
		this.progressbar = new JProgressBar();
		progressbar.setMaximumSize(new Dimension(100,30));
		progressbar.setValue(12);
		this.waitingAnimation = new ImageIcon("images/loading.gif");
		
		this.gcDisplay = new GeneClusterDisplay();
		
		this.gcSelector = new GeneClusterSelector();
		Dimension startDimension = new Dimension(800,600);
		
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
		horiSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);	
		horiSplit.setResizeWeight(0.5);
		
		vertSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		vertSplit.setResizeWeight(0.5);
		this.gecko.setClusters(null);
		vertSplit.setTopComponent(selectorSplitPane);
		
		vertSplit.setBottomComponent(gcDisplay);

		// Menu arrangements
		menuFile = new JMenu("File");
		menuView = new JMenu("View");
		menubar = new JMenuBar();
		menubar.add(menuFile);
		menubar.add(menuView);
		
				
		mgb = new MultipleGenomesBrowser();
		
		
		// Lowest component in the upper half of the window
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout());
		
		
		JScrollPane upscoll = new JScrollPane();
		upscoll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		upscoll.setViewportView(mgb);
		
		upperPanel.add(upscoll, BorderLayout.CENTER);
		
		navigator = new GenomeNavigator();
		gecko.addDataListener(navigator);
		
		mgb.addBrowserContentListener(navigator);
		occurrenceSelector.addSelectionListener(mgb);
		
		
		upperPanel.add(navigator, BorderLayout.SOUTH);
		
		//Toolbar
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.LINE_AXIS));
		toolbar.add(importGenomesAction);
		toolbar.add(saveSessionAction);
		toolbar.add(new JToolBar.Separator());
		menuFile.add(importGenomesAction);
		menuFile.add(saveSessionAction);
		
		toolbar.add(clearSelectionAction);
		toolbar.add(startComputation);
		toolbar.add(stopComputationAction);
		
		toolbar.add(new JToolBar.Separator());
		toolbar.add(zoomIn);
		toolbar.add(zoomOut);
		menuView.add(zoomIn);
		menuView.add(zoomOut);
				
		JToggleButton testButton = new JToggleButton("Animation");
		testButton.setSelected(gecko.isAnimationEnabled());
		toolbar.add(new JToolBar.Separator());
		toolbar.add(testButton);
		toolbar.add(Box.createHorizontalGlue());
		
		toolbar.add(new JLabel("Search "));
		testButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				GeckoInstance.getInstance().setAnimationEnabled(((JToggleButton) e.getSource()).isSelected());
			}
		});
		
		JPanel p = new JPanel(new BorderLayout());
		
		searchField = new JTextField("");
		searchField.setPreferredSize(new Dimension(150, toolbar.getHeight()));
		searchField.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				gecko.filterResults(searchField.getText());
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
		northpanel.setLayout(new GridLayout(1,1));
		mainframe.setJMenuBar(menubar);
		northpanel.add(toolbar);
		mainframe.add(northpanel,BorderLayout.NORTH);
		horiSplit.setTopComponent(upperPanel);
		horiSplit.setBottomComponent(vertSplit);
		mainframe.add(horiSplit, BorderLayout.CENTER);
		JPanel southpanel = new JPanel();
		southpanel.setLayout(new BoxLayout(southpanel, BoxLayout.LINE_AXIS));
		
		JPanel statusbar = new JPanel();
		BoxLayout box = new BoxLayout(statusbar,BoxLayout.X_AXIS);
		statusbar.setLayout(box);
		statusbar.add(Box.createRigidArea(new Dimension(5,0)));
		statusbar.add(statusbaricon);
		statusbar.add(progressbar);
		statusbar.add(Box.createRigidArea(new Dimension(5,0)));
		statusbar.add(statusbartext);
		southpanel.add(statusbar);
		
		JPanel infobarPanel = new JPanel();
		infobarPanel.setLayout(new BoxLayout(infobarPanel, BoxLayout.LINE_AXIS));
		
		infobar = new JLabel();
		
		infobarPanel.add(Box.createGlue());
		infobarPanel.add(infobar);
		infobarPanel.add(Box.createRigidArea(new Dimension(10,0)));
		southpanel.add(Box.createRigidArea(new Dimension(5,0)));
		southpanel.add(infobarPanel);
		mainframe.add(southpanel,BorderLayout.SOUTH);
		changeMode(MODE_NO_SESSION);

		
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
		mainframe.setVisible(true);
	
		
	}
	
	public void setInfobarText(String text) {
		infobar.setText(text);
	}
	
	public static void startUp() {
		new Gui();
	}
	
	private void selectGenomesForImport(ArrayList<GenomeOccurence> occs) {
		new GenomeSelector(occs, this);
	}
	
	public void readSelectedGenomes(GenomeSelector g, ArrayList<GenomeOccurence> occs) {
		g.setVisible(false);
		if (occs == null) {
			return;
		}
		this.gecko.readGenomes(occs);
	}
	
	public JProgressBar getProgressbar() {
		return progressbar;
	}
	
	public void updateViewscreen() {
		this.mgb.clear();
		if (gecko.getGenomes()!=null)
			for (Genome g: this.gecko.getGenomes())
				this.mgb.addGenome(g);
	}
	
//	public void refreshClusterList() {
//		this.gcSelector.refresh();
//	}
	
	public MultipleGenomesBrowser getMgb() {
		return mgb;
	}
	
	/** Simple helper methods which makes it easier to create ImageIcons from Resource
	
    @param String path 
    @return Returns a imageicon if path describes an image, null otherwise 
*/
	private static ImageIcon createImageIcon(String path) {
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
	public static final short MODE_COMPUTING = 1;
	
	/**
	 * The application is idle, a session is currently open
	 */
	public static final short MODE_SESSION_IDLE = 2;
	
	/**
	 * The application is idle, no session is open
	 */
	public static final short MODE_NO_SESSION = 3;
	
	/**
	 * The application is currently reading the genomes selected by the user
	 */
	public static final short MODE_READING_GENOMES = 4;
	public static final short MODE_PREPARING_COMPUTATION = 5;
	public static final short MODE_FINISHING_COMPUTATION = 6;

	private void changeMode(boolean icon, 
			boolean pbar, 
			String text,
			boolean importGenomes,
			boolean startComp,
			boolean clearSelect,
			boolean saveSession,
			boolean zoom,
			boolean search,
			boolean stop) {
		if (icon) statusbaricon.setIcon(waitingAnimation);
		statusbaricon.setVisible(icon);
		progressbar.setVisible(pbar);
		if (pbar) progressbar.grabFocus();
		statusbartext.setText(text);
		importGenomesAction.setEnabled(importGenomes);
		startComputation.setEnabled(startComp);
		clearSelectionAction.setEnabled(clearSelect);
		saveSessionAction.setEnabled(saveSession);
		if (zoom) {
			zoomIn.setEnabled(gecko.canZoomIn());
			zoomOut.setEnabled(gecko.canZoomOut());
		} else {
			zoomIn.setEnabled(false);
			zoomOut.setEnabled(false);
		}
		searchField.setEnabled(search);
		stopComputationAction.setEnabled(stop);
	}
	
	public void changeMode(short mode) {
		switch (mode) {
		case MODE_COMPUTING:
			changeMode(false, true, "Computing gene clusters...", false, false, false, false, false, false,true);
			break;
		case MODE_SESSION_IDLE:
			changeMode(false, false, "Ready", true, true, true, true, true, true,false);
			break;
		case MODE_NO_SESSION:
			changeMode(false, false, "Ready", true, false, false, false, false, false,false);
			break;
		case MODE_READING_GENOMES:
			changeMode(true, false, "Reading genomes...", false, false, false, false, false, false,false);
			break;
		case MODE_PREPARING_COMPUTATION:
			changeMode(true, false, "Preparing data...", false, false, false, false, false, false,true);
			break;
		case MODE_FINISHING_COMPUTATION:
			changeMode(true, false, "Finishing...", false, false, false, false, false, false,false);
		}
	}
	
	public void handleFileError(short error) {
		closeCurrentSession();
		changeMode(Gui.MODE_NO_SESSION);		
		JOptionPane.showMessageDialog(mainframe, "The input file is not a valid COG file", "Wrong file format", JOptionPane.ERROR_MESSAGE);
	}

	private void closeCurrentSession() {
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
						ArrayList<GenomeOccurence> list = Gui.this.gecko.importGenomes(fc.getSelectedFile());
						selectGenomesForImport(list);
					} else if (FileUtils.getExtension(fc.getSelectedFile()).equals("gck")) {
						gecko.loadSessionFromFile(fc.getSelectedFile());
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
			mgb.clearHighlight();
			mgb.clearGrey();
			mgb.unflipAll();
		}
		
	};
	
	private Action startComputation = new AbstractAction() {
		private static final long serialVersionUID = -1530838105061978403L;
		public void actionPerformed(ActionEvent e) {
			StartComputationDialog d = gecko.getStartComputationDialog();
			d.setLocationRelativeTo(Gui.this.mainframe);
			d.setVisible(true);
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
					if (!fc.getFileFilter().accept(fc.getSelectedFile()))
						f = new File(f.getAbsolutePath()+".gck");
					PrintUtils.printDebug("Choosen file to save to: "+f);
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
						if (x==JOptionPane.NO_OPTION) continue;
					}
					if (!gecko.saveSessionToFile(f))
						JOptionPane.showMessageDialog(mainframe, "An error occured while writing the file!", "Error", JOptionPane.ERROR_MESSAGE);
					break;
				} else break;
			}
			mainframe.requestFocus();

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
		zoomOut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_MINUS,  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );		
	}

}
