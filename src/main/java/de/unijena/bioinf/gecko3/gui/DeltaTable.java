package de.unijena.bioinf.gecko3.gui;

import de.unijena.bioinf.gecko3.datastructures.Parameter;
import de.unijena.bioinf.gecko3.gui.util.JTableSelectAll;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class DeltaTable extends JPanel{
    private final DeltaTableTableModel model;
    private final JSpinner sizeSpinner;

    public DeltaTable(Dimension dimension) {
        this(dimension, Parameter.DeltaTable.getDefault().getDeltaTable(), Parameter.DeltaTable.getDefault().getMinimumSize());
    }

    public DeltaTable(Dimension dimension, int[][] deltas, int initialMinimumSize) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setPreferredSize(dimension);
        final JTable deltaTable = new JTableSelectAll();
        JTableHeader header = deltaTable.getTableHeader();
        header.setPreferredSize(new Dimension((int)dimension.getWidth(), 20));
        deltaTable.setBackground(Color.WHITE);
        model = new DeltaTableTableModel(deltas);
        deltaTable.setModel(model);
        deltaTable.setRowSelectionAllowed(false);
        deltaTable.setCellSelectionEnabled(true);
        deltaTable.setDefaultRenderer(Integer.class, new DeltaTableCellRenderer());
        deltaTable.setPreferredScrollableViewportSize(new Dimension((int)dimension.getWidth(), (int)dimension.getHeight() - StartComputationDialog.COMBO_HEIGHT - StartComputationDialog.V_GAP));
        deltaTable.setFillsViewportHeight(true);

        // add popup menu to table
        final JPopupMenu popUp = new JPopupMenu();
        final JMenuItem addRow = new JMenuItem(new AbstractAction("Add row"){
            private static final long serialVersionUID = -5276986817187027648L;

            /**
             * Invoked when an action occurs.
             *
             * @param e the event
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                model.addEmptyDeltaValues(deltaTable.getSelectedRow());

            }
        });
        final JMenuItem deleteRow = new JMenuItem(new AbstractAction("Delete Row") {
            private static final long serialVersionUID = -4664247776129030317L;

            @Override
            public void actionPerformed(ActionEvent e) {
                model.removeRow(deltaTable.getSelectedRow());
            }
        });
        final JMenuItem reset = new JMenuItem(new AbstractAction("Reset") {
            private static final long serialVersionUID = 7855959578095508292L;

            @Override
            public void actionPerformed(ActionEvent e) {
                model.setDeltaTable(Parameter.DeltaTable.getDefault().getDeltaTable());
                sizeSpinner.setValue(Parameter.DeltaTable.getDefault().getMinimumSize());
            }
        });

        popUp.add(addRow);
        popUp.add(deleteRow);
        popUp.addSeparator();
        popUp.add(reset);

        // mouse listener for popup menu
        deltaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e){
                if (e.isPopupTrigger()) {
                    int row = deltaTable.rowAtPoint(e.getPoint());
                    int column = deltaTable.columnAtPoint(e.getPoint());
                    deltaTable.changeSelection(row, column, false, false);
                    popUp.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(deltaTable);
        add(scrollPane);

        add(Box.createVerticalStrut(StartComputationDialog.V_GAP));

        sizeSpinner = new JSpinner(new SpinnerNumberModel(initialMinimumSize, 0, Integer.MAX_VALUE, 1));
        sizeSpinner.setPreferredSize(new Dimension(StartComputationDialog.COMBO_WIDTH, StartComputationDialog.COMBO_HEIGHT));
        JLabel sizeLabel = new JLabel("Minimum cluster size: ", JLabel.LEFT);

        final JPanel sizePanel = new JPanel();
        sizePanel.setPreferredSize(new Dimension(dimension.width, StartComputationDialog.COMBO_HEIGHT+StartComputationDialog.V_GAP));
        GridLayout gridLayout = new GridLayout(1, 2, StartComputationDialog.H_GAP, StartComputationDialog.V_GAP);
        sizePanel.setLayout(gridLayout);

        sizePanel.add(sizeLabel);
        sizePanel.add(sizeSpinner);

        add(sizePanel);
    }

    /**
     * The model for the delta table. Will automatically grow and shrink,
     * to always have at least one at a maximum of two invalid rows at the end,
     */
    private static class DeltaTableTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 769826863674743708L;

        private static final short COL_D_ADD = 0;
        private static final short COL_D_LOSS = 1;
        private static final short COL_D_SUM = 2;
        private static final short COL_SIZE = 3;

        private final String[] columnNames = {"D_ADD","D_LOSS", "D_SUM", "Size"};
        private java.util.List<int[]> deltaValues;

        public DeltaTableTableModel(int[][] values) {
            setDeltaTable(values);
        }

        @Override
        public int getRowCount() {
            return deltaValues.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class getColumnClass(int c) {
            return Integer.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return deltaValues.get(rowIndex)[columnIndex];
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            deltaValues.get(rowIndex)[columnIndex] = (int)value;
            checkAndAddOrRemoveLastRows();
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }


        /*
         * Additional table logic
         */

        public boolean isValidRow(int row) {
            return isValidCell(row, COL_D_ADD) && isValidCell(row, COL_D_LOSS) && isValidCell(row, COL_D_SUM) && isValidCell(row, COL_SIZE);
        }

        public boolean isValidCell(int row, int column) {
            if (deltaValues.get(row)[column] < 0)
                return false;

            if (column == COL_SIZE) {
                return row > 0 ? deltaValues.get(row - 1)[column] < deltaValues.get(row)[column] : true;
            }

            boolean valid = true;
            if (row >= 1) {
                valid = deltaValues.get(row - 1)[column] <= deltaValues.get(row)[column];
            }
            if (column == COL_D_SUM) {
                valid &= deltaValues.get(row)[column] >= deltaValues.get(row)[COL_D_ADD];
                valid &= deltaValues.get(row)[column] >= deltaValues.get(row)[COL_D_LOSS];
            } else if (column == COL_D_LOSS || column == COL_D_ADD){
                valid &= deltaValues.get(row)[column] <= deltaValues.get(row)[COL_D_SUM];
            }
            return valid;
        }

        public int[][] getDeltaTable() {
            int[][] deltaTable = new int[deltaValues.get(deltaValues.size()-2)[COL_SIZE]+1][];
            int row = 0;
            int[] currentDeltaValues = new int[]{0, 0 ,0};
            for (int i=0; i<deltaValues.size()-1; i++) {
                int deltaValueRow = deltaValues.get(i)[COL_SIZE];
                while (row < deltaValueRow)
                    deltaTable[row++] = Arrays.copyOf(currentDeltaValues, currentDeltaValues.length);

                currentDeltaValues = new int[3];
                for (int j=0; j<3; j++)
                    currentDeltaValues[j] = deltaValues.get(i)[j];

                deltaTable[row++] = Arrays.copyOf(currentDeltaValues, currentDeltaValues.length);
            }
            while (row < deltaTable.length)
                deltaTable[row++] = Arrays.copyOf(currentDeltaValues, currentDeltaValues.length);

            return deltaTable;
        }

        public boolean isValidDeltaTable() {
            for (int i=0; i<deltaValues.size()-1; i++)
                if (!isValidRow(i))
                    return false;
            return true;
        }

        public void setDeltaTable(int[][] deltaTable) {
            this.deltaValues = new ArrayList<>(deltaTable!=null ? deltaTable.length+1 : 1);
            if (deltaTable!= null){
                for (int i=0; i<deltaTable.length; i++) {
                    if (deltaTable[i].length != 3)
                        throw new IllegalArgumentException("Invalid array, needs to contain exactly 3 elements!");
                    if (isZeroRow(deltaTable[i]))
                        continue;
                    int[] newValues = Arrays.copyOf(deltaTable[i], deltaTable[i].length+1);
                    newValues[COL_SIZE] = i;
                    deltaValues.add(newValues);
                }
            }
            if (deltaTable==null || deltaTable.length==0 || isValidRow(deltaValues.size()-1))
                addEmptyDeltaValues();
        }

        private boolean isZeroRow(int[] values) {
            for (int value : values) {
                if (value != 0)
                    return false;
            }
            return true;
        }

        private boolean isEmptyRow(int row) {
            if (deltaValues.get(row)[COL_D_SUM] > 0)
                return false;
            if (deltaValues.get(row)[COL_D_LOSS] > 0)
                return false;
            if (deltaValues.get(row)[COL_D_ADD] > 0)
                return false;
            return true;
        }

        private void checkAndAddOrRemoveLastRows() {
            if (isValidRow(deltaValues.size()-1)) {
                addEmptyDeltaValues();
            } else if (isEmptyRow(deltaValues.size()-1)) {
                for (int row = deltaValues.size()-2; row >= 0; row--){
                    if (isEmptyRow(row)) {
                        deltaValues.remove(deltaValues.size() - 1);
                        fireTableRowsDeleted(row, row);
                    } else
                        break;
                }
            }
        }

        /**
         * Adds an empty row at the end of the delta table
         */
        void addEmptyDeltaValues() {
            addEmptyDeltaValues(deltaValues.size());
        }

        void addEmptyDeltaValues(int index) {
            deltaValues.add(index, new int[]{-1, -1, -1, (deltaValues.isEmpty()) ? 0 : deltaValues.get(index-1)[COL_SIZE]+1 });
            fireTableRowsInserted(index, index);
        }

        public void removeRow(int selectedRow) {
            if (deltaValues.size()-1 != selectedRow)
                deltaValues.remove(selectedRow);
            fireTableRowsDeleted(selectedRow, selectedRow);
        }
    }

    class DeltaTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            TableModel model = table.getModel();
            if (model instanceof DeltaTableTableModel){
                DeltaTableTableModel myModel = (DeltaTableTableModel) model;
                setBorder(BorderFactory.createLineBorder(!myModel.isValidCell(row, column) ? Color.RED : Color.BLACK));
            }
            return this;
        }
    }

    public boolean isValidDeltaTable() {
        return model.isValidDeltaTable();
    }

    public int[][] getDeltaTable() {
        return model.getDeltaTable();
    }

    public int getMinimumClusterSize(){
        return (Integer)sizeSpinner.getValue();
    }
}
