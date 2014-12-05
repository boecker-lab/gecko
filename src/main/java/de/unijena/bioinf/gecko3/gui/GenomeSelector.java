package de.unijena.bioinf.gecko3.gui;

import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.DataSet;
import de.unijena.bioinf.gecko3.gui.util.ColorUtils;
import de.unijena.bioinf.gecko3.io.CogFileReader;
import de.unijena.bioinf.gecko3.io.GenomeOccurrence;
import de.unijena.bioinf.gecko3.util.SortUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class GenomeSelector extends JDialog {
	private static final long serialVersionUID = -8491964493540715101L;
    private static final Logger logger = LoggerFactory.getLogger(GenomeSelector.class);

    private final CogFileReader reader;
    private DataSet data;
	private final List<GenomeOccurrence> occs;
	private final short[] borders;
	private int highID =0;
	private final Random rand;
	private final JTable table;
	private AbstractAction importAction;
    private final AbstractAction groupAction;
    private final AbstractAction unGroupAction;
    private AbstractAction allOrNoneAction;
	
	public GenomeSelector(CogFileReader cogReader, Frame parent) {
		super(parent,"Select genomes to import...");
		super.setModal(true);
        this.reader = cogReader;
        this.occs = reader.getOccs();

        this.setIconImages(parent.getIconImages());
		
		// Resort the occurence list and recompute the group borders for
		// visualization
		SortUtils.resortGenomeOccurences(occs);
		borders = new short[occs.size()];
		recomputeBorders();
		// Create the color map for the initial singleton groups
		rand = new Random();
        HashMap<Integer, Color> colorMap = new HashMap<>();
		for (GenomeOccurrence occ : occs) {
			if (!colorMap.containsKey(occ.getGroup()))
				colorMap.put(occ.getGroup(), getRandomColor());
			if (occ.getGroup()>highID) highID = occ.getGroup();
		}
		
		this.setMinimumSize(new Dimension(50,50));
		JPanel mainpanel = new JPanel();
		mainpanel.setLayout(new BorderLayout());
		this.add(mainpanel);
		
		JPanel tablePanel = new JPanel(new GridLayout(1,1));
		table = new JTable();
		TableCellRenderer r = new GenomeTableCellRenderer();
		table.setDefaultRenderer(Character.class, r);
		table.setModel(new GenomeTableModel());
		table.addMouseListener(new GenomeTableMouseListener());
		table.getColumnModel().getColumn(2).setMaxWidth(20);
		table.getColumnModel().getColumn(1).setMaxWidth(20);
		table.setIntercellSpacing(new Dimension(0,0));

        JScrollPane scrollpane = new JScrollPane(table);
		tablePanel.add(scrollpane);
		mainpanel.add(tablePanel,BorderLayout.CENTER);
		
		table.setBackground(Color.WHITE);
		scrollpane.setBackground(Color.WHITE);
		scrollpane.getViewport().setBackground(Color.WHITE);
		
		JPanel lowerpanel = new JPanel();
		lowerpanel.setLayout(new BorderLayout(0,0));
		JButton allOrNoneButton = new JButton("Choose All");
		JButton okButton = new JButton();
		JButton cancelButton = new JButton("Cancel");
		lowerpanel.add(cancelButton, BorderLayout.WEST);
		lowerpanel.add(okButton, BorderLayout.EAST);
		lowerpanel.setPreferredSize(new Dimension(100,30));
		JPanel centerpanel = new JPanel();
		centerpanel.setLayout(new BoxLayout(centerpanel, BoxLayout.X_AXIS));
		centerpanel.add(Box.createHorizontalGlue());
		JButton groupButton = new JButton();
		JButton unGroupButton = new JButton();
		centerpanel.add(groupButton);
		centerpanel.add(unGroupButton);
		centerpanel.add(Box.createHorizontalGlue());
		centerpanel.add(allOrNoneButton);
		mainpanel.add(lowerpanel,BorderLayout.SOUTH);
		lowerpanel.add(centerpanel, BorderLayout.CENTER);
		
		allOrNoneAction = new AbstractAction() {
			
			/**
			 * Random generated serial version UID
			 */
			private static final long serialVersionUID = -2643293031674665454L;
			private boolean all = true; // toggle between all or none action.
			@Override
			public void actionPerformed(ActionEvent e) {
				if(all){
					checkAll();
					allOrNoneAction.putValue(Action.NAME, "choose None");
					all = false;
				} else {
					checkNone();
					allOrNoneAction.putValue(Action.NAME, "choose All");
					all = true;
				}
				
			}
		};
		allOrNoneAction.putValue(Action.NAME, "choose All");
		allOrNoneButton.setAction(allOrNoneAction);
		
		importAction = new AbstractAction() {
			
			private static final long serialVersionUID = -4809998022093313635L;

			public void actionPerformed(ActionEvent e) {
				// Better double check...
				checkSelectionCount();
				if (!importAction.isEnabled()) return;

				GenomeSelector.this.setVisible(false);
                GeckoInstance.getInstance().getGui().changeMode(Gui.Mode.READING_GENOMES);

                SwingWorker worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            data = reader.readFileContent();
                        } catch (IOException e) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    JOptionPane.showMessageDialog(GeckoInstance.getInstance().getGui().getMainframe(),
                                            "An error occured while reading the file!",
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        } catch (ParseException e) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
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
                            JOptionPane.showMessageDialog(GenomeSelector.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            logger.error("Unable to read genomes", e);
                        }
                        GeckoInstance.getInstance().setGeckoInstanceData(data);
                    }
                };
                worker.execute();
            }
		};
		importAction.setEnabled(false);
		importAction.putValue(Action.NAME, "OK");
		importAction.putValue(Action.ACCELERATOR_KEY, KeyEvent.VK_ENTER);
		
		okButton.setAction(importAction);
		
		AbstractAction cancelAction = new AbstractAction() {
			private static final long serialVersionUID = 3950317889981087655L;

			public void actionPerformed(ActionEvent e) {
				GenomeSelector.this.setVisible(false);
                GeckoInstance.getInstance().setGeckoInstanceData();
			}
		};
		cancelAction.putValue(Action.NAME, "Cancel");
		cancelAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
	
		cancelButton.setAction(cancelAction);
		mainpanel.getActionMap().put("cancelAction", cancelAction);
		mainpanel.getActionMap().put("importAction", importAction);
		mainpanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelAction");
		mainpanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "importAction");
		
		groupAction = new AbstractAction() {
			private static final long serialVersionUID = 3447536063832102826L;

			public void actionPerformed(ActionEvent e) {
				++highID;
				for (int i : table.getSelectedRows()) {
					occs.get(i).setGroup(highID);
					occs.get(i).setFlagged(true);
				}
				SortUtils.resortGenomeOccurences(occs);
				recomputeBorders();
				table.setRowSelectionInterval(0, table.getSelectedRowCount()-1);
				//scrollpane.getVerticalScrollBar().setValue(0);
				table.tableChanged(new TableModelEvent(table.getModel(), 0, occs.size()-1));
				table.repaint();
				checkSelectionCount();
			}
		};
		groupAction.putValue(Action.NAME, "Group");
		groupAction.setEnabled(false);
		groupButton.setAction(groupAction);
		
		unGroupAction = new AbstractAction() {
			private static final long serialVersionUID = -6309595381945331217L;

			public void actionPerformed(ActionEvent e) {
				Stack<Integer> touchedGroups = new Stack<>();
				for (int row : table.getSelectedRows()) {
					if (occs.get(row).getGroup()!=0) touchedGroups.push(occs.get(row).getGroup());
					occs.get(row).setGroup(0);
					occs.get(row).setFlagged(false);
				}
				//Check if a touched group became a singleton group
				while (!touchedGroups.isEmpty()) {
					int group = touchedGroups.pop();
					int count = 0;
					GenomeOccurrence last = null;
					for (GenomeOccurrence occ : occs)
						if (occ.getGroup()==group) {
							last = occ;
							count++;
						}
					if (count==1) {
						last.setGroup(0);
						last.setFlagged(false);
					}
				}
			
				SortUtils.resortGenomeOccurences(occs);
				recomputeBorders();
				table.clearSelection();
				table.tableChanged(new TableModelEvent(table.getModel(), 0, occs.size()-1));
				table.repaint();
				checkSelectedRowCount();
				checkSelectionCount();
			}
		};
		unGroupAction.putValue(Action.NAME, "Ungroup");
		unGroupAction.setEnabled(false);
		unGroupButton.setAction(unGroupAction);
		
		
		this.pack();
		this.setVisible(true);
	}
	
	private void recomputeBorders() {
		boolean inGroup = false;
		for (int i=0;i<occs.size()-1;i++) {
			if (occs.get(i).getGroup()==0) {
				borders[i] = GroupLabel.MODE_NOGRP;
				continue;
			}
			if (occs.get(i).getGroup()==occs.get(i+1).getGroup()) {
				if (inGroup)
					borders[i] = GroupLabel.MODE_MIDDLE;
				else {
					borders[i] = GroupLabel.MODE_BEGIN;
					inGroup = true;
				}
			} else {
				if (inGroup) {
					borders[i] = GroupLabel.MODE_END;
					inGroup = false;
				} else
					borders[i] = GroupLabel.MODE_NOGRP;
			}
		}
		if (inGroup)
			borders[borders.length-1] = GroupLabel.MODE_END;
		else
			borders[borders.length-1] = GroupLabel.MODE_NOGRP;
	}
	
	private Color getRandomColor() {
		return new Color(ColorUtils.HSVtoRGB(rand.nextFloat(), (205 + rand.nextInt(50)) / 255F, (205 + rand.nextInt(50)) / 255F));
	}
	
	private void checkAll() {
		for (GenomeOccurrence occ : occs)
			occ.setFlagged(true);
		table.repaint();
		checkSelectionCount();
	}
	
	private void checkNone() {
		for (GenomeOccurrence occ : occs)
			occ.setFlagged(false);
		table.repaint();
		importAction.setEnabled(false);
	}
	
	private void checkSelectionCount() {
		boolean atLeastOneChecked = false;
		for (GenomeOccurrence occ : occs)
			if (occ.isFlagged()) {
				atLeastOneChecked = true;
				break;
			}
		
		importAction.setEnabled(atLeastOneChecked);
			
	}
	
	
	private class GenomeTableMouseListener extends MouseAdapter {
		@Override
		public void mouseReleased(MouseEvent e) {
			checkSelectedRowCount();
		}
	}
	
	private void checkSelectedRowCount() {
		for (int row : table.getSelectedRows())
			if (occs.get(row).getGroup()!=0) {
				unGroupAction.setEnabled(true);
				break;
			} else unGroupAction.setEnabled(false);
		if (table.getSelectedRowCount()<2)
			groupAction.setEnabled(false);
		else
			groupAction.setEnabled(true);
	}
	
	static class GroupLabel extends JLabel {

		private static final long serialVersionUID = -678973051308024076L;
		public static final short MODE_NOGRP = 0;
		public static final short MODE_BEGIN = 1;
		public static final short MODE_END = 3;
		public static final short MODE_MIDDLE = 2;
		private final short mode;
		private final Dimension surface;
		
		public GroupLabel(short mode, int width, int height) {
			this.mode = mode;
			surface = new Dimension(width,height);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			switch (mode) {
			case MODE_BEGIN:
				g.fillRect(surface.width/2-1,surface.height/2, 2, surface.height/2);
				g.fillOval(surface.width/2-4, surface.height/2-4, 7,7);
				break;
			case MODE_MIDDLE:
				g.fillRect(surface.width/2-1,0, 2, surface.height);
				break;
			case MODE_END:
				g.fillRect(surface.width/2-1,0, 2, surface.height/2);
				g.fillOval(surface.width/2-4, surface.height/2-4, 7,7);
            default:
			}
		}
	}
	
	class GenomeTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = -9034363765045649103L;
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			return new GroupLabel(borders[row], 
					table.getColumnModel().getColumn(column).getWidth(),
					table.getRowHeight(row));
		}
		
		}
	
	class GenomeTableModel implements TableModel {
		
//		private boolean[] selected;
		private final int[] groups;

		public GenomeTableModel() {
//			this.selected = new boolean[occs.size()];
			this.groups = new int[occs.size()];
			Arrays.fill(groups, 0);
		}
		
		// We don't change the TableModel
		public void addTableModelListener(TableModelListener l) {}
		public void removeTableModelListener(TableModelListener l) {}

		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0:
					return String.class;
				case 1:
					return Character.class;
				default:
					return Boolean.class;
			}
		}

		public int getColumnCount() {
			return 3;
		}

		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
				case 0: 
					return "Name";
				default: 
					return "";
			}
		}

		public int getRowCount() {
			return occs.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0:
					return occs.get(rowIndex).getGenomeSelectorText();
				case 1:
					return '\0';
				default:
					return occs.get(rowIndex).isFlagged();					
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != 0;
        }

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex==2)  {
				int group = occs.get(rowIndex).getGroup();
				if (group==0) occs.get(rowIndex).setFlagged((Boolean) value);
				else {
					for (GenomeOccurrence occ : occs)
						if (occ.getGroup()==group) occ.setFlagged((Boolean) value);
					table.tableChanged(new TableModelEvent(this));
				}
				checkSelectionCount();
			}
		}
		
	}

}
