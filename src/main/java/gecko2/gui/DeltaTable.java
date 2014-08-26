package gecko2.gui;

import gecko2.gui.util.JTableSelectAll;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class DeltaTable extends JPanel{
    DeltaTableTableModel model;

    public DeltaTable() {
        super(new GridLayout(1,0));
        JTable deltaTable = new JTableSelectAll();
        deltaTable.setBackground(Color.WHITE);
        model = new DeltaTableTableModel();
        deltaTable.setModel(model);
        deltaTable.setRowSelectionAllowed(false);
        deltaTable.setCellSelectionEnabled(true);
        deltaTable.setDefaultRenderer(Integer.class, new DeltaTableCellRenderer());
        deltaTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        deltaTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(deltaTable);

        add(scrollPane);
    }

    /**
     * The model for the delta table. Will automatically grow and shrink,
     * to always have at least one at a maximum of two invalid rows at the end,
     */
    private static class DeltaTableTableModel extends AbstractTableModel {
        private static final short COL_D_LOSS = 0;
        private static final short COL_D_ADD = 1;
        private static final short COL_D_SUM = 2;
        private static final short COL_SIZE = 3;

        private final String[] columnNames = {"D_LOSS", "D_ADD", "D_SUM", "Size"};
        private java.util.List<int[]> deltaValues;

        public DeltaTableTableModel() {
            this(null);
        }

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
            if (columnIndex == COL_SIZE)
                return isLastRow(rowIndex) ? "-" : rowIndex;
            return deltaValues.get(rowIndex)[columnIndex];
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            deltaValues.get(rowIndex)[columnIndex] = (int)value;
            if (rowIndex == deltaValues.size()-1 && isValidRow(rowIndex)){
                addEmptyDeltaValues();
                fireTableRowsInserted(rowIndex+1, rowIndex+1);
            } else if (deltaValues.size() > 1 && rowIndex == deltaValues.size()-2 && isInValidRow(rowIndex) && isInValidRow(rowIndex + 1)) {
                    deltaValues.remove(deltaValues.size()-1);
                    fireTableRowsDeleted(rowIndex, rowIndex);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == COL_SIZE)
                return false;
            else
                return true;
        }

        public boolean isValidRow(int row) {
            return isValidCell(row, COL_D_ADD) && isValidCell(row, COL_D_LOSS) && isValidCell(row, COL_D_SUM);
        }

        private boolean isInValidRow(int row) {
            return !isValidCell(row, COL_D_ADD) && !isValidCell(row, COL_D_LOSS) && !isValidCell(row, COL_D_SUM);
        }

        public boolean isValidCell(int row, int column) {
            if (column == COL_SIZE)
                return true;
            if (deltaValues.get(row)[column] < 0)
                return false;
            boolean valid = true;
            if (row >= 1) {
                valid &= deltaValues.get(row-1)[column] <= deltaValues.get(row)[column];
            }
            if (column == COL_D_SUM) {
                valid &= deltaValues.get(row)[column] >= deltaValues.get(row)[COL_D_ADD];
                valid &= deltaValues.get(row)[column] >= deltaValues.get(row)[COL_D_LOSS];
            } else {
                valid &= deltaValues.get(row)[column] <= deltaValues.get(row)[COL_D_SUM];
            }
            return valid;
        }

        public int[][] getDeltaTable() {
            int[][] deltaTable = new int[deltaValues.size()-1][];
            for (int i=0; i<deltaTable.length; i++) {
                deltaTable[i] = new int[3];
                for (int j=0; j<deltaTable[i].length; j++)
                    deltaTable[i][j] = deltaValues.get(i)[j];
            }
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
                for (int[] deltas : deltaTable) {
                    if (deltas.length != 3)
                        throw new IllegalArgumentException("Invalid array, needs to contain exactly 3 elements!");
                    deltaValues.add(Arrays.copyOf(deltas, deltas.length));
                }
            }
            if (deltaTable==null || deltaTable.length==0 || isValidRow(deltaValues.size()-1))
                addEmptyDeltaValues();
        }

        private void addEmptyDeltaValues() {
            deltaValues.add(new int[]{-1, -1, -1});
        }

        private boolean isLastRow(int row) {
            return row == deltaValues.size()-1;
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

    public void setDeltaTable(int[][] deltaTable) {
        model.setDeltaTable(deltaTable);
    }


    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("SimpleTableDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        DeltaTable deltaTable1 = new DeltaTable();
        frame.setContentPane(deltaTable1);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}
