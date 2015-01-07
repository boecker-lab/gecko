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
import de.unijena.bioinf.gecko3.datastructures.Parameter;
import de.unijena.bioinf.gecko3.gui.util.ColorUtils;
import de.unijena.bioinf.gecko3.gui.util.JTableSelectAll;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
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
public class DeltaTable extends JPanel {
    private final DeltaTableTableModel model;
    private final JTable deltaTable;

    public DeltaTable() {
        this(Parameter.DeltaTable.getDefault().getDeltaTable(), Parameter.DeltaTable.getDefault().getMinimumSize());
    }

    public DeltaTable(int[][] deltas, int initialMinimumSize) {
        /**
         * Setup the table
         */
        deltaTable = new JTableSelectAll();
        deltaTable.setBackground(Color.WHITE);
        model = new DeltaTableTableModel(deltas, initialMinimumSize);
        deltaTable.setModel(model);
        deltaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deltaTable.setRowSelectionAllowed(false);
        deltaTable.setCellSelectionEnabled(true);
        deltaTable.setDefaultRenderer(Integer.class, new DeltaTableCellRenderer());
        deltaTable.putClientProperty("terminateEditOnFocusLost", true);
        deltaTable.setFillsViewportHeight(true);

        // layout of window
        FormLayout layout = new FormLayout("min(155dlu;default)", "min(100dlu;default)");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
        builder.append(new JScrollPane(deltaTable));

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
                if (deltaTable.isEditing())
                    deltaTable.getCellEditor().stopCellEditing();
                model.addEmptyDeltaValuesBefore(deltaTable.getSelectedRow());

            }
        });
        final JMenuItem deleteRow = new JMenuItem(new AbstractAction("Delete Row") {
            private static final long serialVersionUID = -4664247776129030317L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (deltaTable.isEditing())
                    deltaTable.getCellEditor().stopCellEditing();
                model.removeRow(deltaTable.getSelectedRow());
            }
        });

        final JMenu reset = new JMenu("Reset");
        for (Parameter.DeltaTable value : Parameter.DeltaTable.getSupported()) {
            final JMenuItem item = new JMenuItem(new SetDeltaTableAction(value));
            reset.add(item);
        }

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

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = deltaTable.rowAtPoint(e.getPoint());
                    int column = deltaTable.columnAtPoint(e.getPoint());
                    deltaTable.changeSelection(row, column, false, false);
                    popUp.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    public int getClusterSize() {
        return model.getMinimumClusterSize();
    }

    public boolean isValidDeltaTable() {
        return model.isValidDeltaTable();
    }

    public int[][] getDeltaTable() {
        return model.getDeltaTable();
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

        public DeltaTableTableModel(int[][] values, int minimumSize) {
            setDeltaTable(values, minimumSize);
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

        public void setDeltaTable(Parameter.DeltaTable deltaValues) {
            setDeltaTable(deltaValues.getDeltaTable(), deltaValues.getMinimumSize());
        }

        public void setDeltaTable(int[][] deltaTable, int minimumSize) {
            this.deltaValues = new ArrayList<>(deltaTable!=null ? deltaTable.length+1 : 1);
            if (deltaTable!= null){
                for (int i=0; i<deltaTable.length; i++) {
                    if (deltaTable[i].length != 3)
                        throw new IllegalArgumentException("Invalid array, needs to contain exactly 3 elements!");
                    if (i < minimumSize && !isZeroRow(deltaTable[i]))
                        throw new IllegalArgumentException("Invalid array, non zero row in table before minimum size");
                    if (i >= minimumSize){
                        int[] newValues = Arrays.copyOf(deltaTable[i], deltaTable[i].length+1);
                        newValues[COL_SIZE] = i;
                        deltaValues.add(newValues);
                    }
                }
            }
            if (deltaTable==null || deltaTable.length==0 || isValidRow(deltaValues.size()-1))
                addEmptyDeltaValuesAtEnd();
        }

        private boolean isZeroRow(int[] values) {
            for (int value : values) {
                if (value != 0)
                    return false;
            }
            return true;
        }

        private boolean isEmptyRow(int row) {
            if (deltaValues.get(row)[COL_D_SUM] >= 0)
                return false;
            if (deltaValues.get(row)[COL_D_LOSS] >= 0)
                return false;
            if (deltaValues.get(row)[COL_D_ADD] >= 0)
                return false;
            return true;
        }

        private void checkAndAddOrRemoveLastRows() {
            if (!isEmptyRow(deltaValues.size()-1)) {
                addEmptyDeltaValuesAtEnd();
            } else {
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
        void addEmptyDeltaValuesAtEnd() {
            addEmptyDeltaValues(deltaValues.size(), (deltaValues.isEmpty()) ? 0 : deltaValues.get(deltaValues.size()-1)[COL_SIZE]+1);
        }

        /**
         * Adds a new empty delta value line before the selected one
         * @param index
         */
        void addEmptyDeltaValuesBefore(int index) {
            addEmptyDeltaValues(index, (deltaValues.isEmpty()) ? 0 : deltaValues.get(index)[COL_SIZE]-1);
        }

        void addEmptyDeltaValues(int index, int size) {
            deltaValues.add(index, new int[]{-1, -1, -1, size});
            fireTableRowsInserted(index, index);
        }

        public void removeRow(int selectedRow) {
            if (deltaValues.size()-1 != selectedRow)
                deltaValues.remove(selectedRow);
            fireTableRowsDeleted(selectedRow, selectedRow);
        }

        public int getMinimumClusterSize() {
            return deltaValues.get(0)[COL_SIZE];
        }
    }

    class DeltaTableCellRenderer extends DefaultTableCellRenderer {
        private final Color defaultForeground;
        private final Color lastRowForeground;

        public DeltaTableCellRenderer() {
            super();
            this.defaultForeground = UIManager.getColor("Table.foreground");
            this.lastRowForeground = ColorUtils.getTranslucentColor(defaultForeground, 0.2f);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component rendererComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            TableModel model = table.getModel();
            if (model instanceof DeltaTableTableModel){
                DeltaTableTableModel myModel = (DeltaTableTableModel) model;
                if (row == myModel.getRowCount()-1) {
                    rendererComp.setForeground(lastRowForeground);
                    setBorder(BorderFactory.createLineBorder(ColorUtils.getTranslucentColor(Color.BLACK, 0.2f)));
                } else {
                    rendererComp.setForeground(defaultForeground);
                    setBorder(BorderFactory.createLineBorder(!myModel.isValidCell(row, column) ? Color.RED : Color.BLACK));
                }
            }
            return this;
        }
    }

    class SetDeltaTableAction extends AbstractAction {
        private static final long serialVersionUID = 7855959578095508292L;
        private Parameter.DeltaTable value;

        public SetDeltaTableAction(Parameter.DeltaTable value) {
            super(value.toString());
            this.value = value;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (deltaTable.isEditing())
                deltaTable.getCellEditor().stopCellEditing();
            model.setDeltaTable(value);
        }
    }

    public static void main(String[] argv){
        JFrame frame = new JFrame();
        final DeltaTable t = new DeltaTable();
        frame.add(t);
        frame.pack();
        frame.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println(t.getPreferredSize());
            }
        });
    }
}
