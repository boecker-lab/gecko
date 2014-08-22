package gecko2.gui;

import gecko2.gui.util.JTableSelectAll;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class DeltaTable extends JPanel{
    public DeltaTable() {
        super(new GridLayout(1,0));
        JTable deltaTable = new JTableSelectAll();
        deltaTable.setBackground(Color.WHITE);
        deltaTable.setModel(new DeltaTableTableModel());
        deltaTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        deltaTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(deltaTable);

        add(scrollPane);
    }

    private static class DeltaTableTableModel extends AbstractTableModel {
        private static final short COL_D_LOSS = 0;
        private static final short COL_D_ADD = 1;
        private static final short COL_D_SUM = 2;
        private static final short COL_SIZE = 3;

        private final String[] columnNames = {"D_LOSS", "D_ADD", "D_SUM", "Size"};
        private java.util.List<int[]> deltaValues;

        public DeltaTableTableModel() {
            this(new int[][]{{-1, -1, -1}});
        }

        public DeltaTableTableModel(int[][] values) {
            deltaValues = new ArrayList<>(values.length);
            for (int[] val : values) {
                deltaValues.add(val);
            }
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
                return rowIndex;
            return deltaValues.get(rowIndex)[columnIndex];
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            deltaValues.get(rowIndex)[columnIndex] = (int)value;
            if (rowIndex == deltaValues.size()-1 && rowFull(rowIndex)){
                deltaValues.add(new int[] {-1, -1, -1});
                fireTableRowsInserted(rowIndex+1, rowIndex+1);
            } else if (deltaValues.size() > 1 && rowIndex == deltaValues.size()-2 && rowEmpty(rowIndex) && rowEmpty(rowIndex+1)) {
                    deltaValues.remove(deltaValues.size()-1);
                    fireTableRowsDeleted(rowIndex, rowIndex);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        private boolean rowEmpty(int rowIndex) {
            return deltaValues.get(rowIndex)[COL_D_SUM] <= 0
                    && deltaValues.get(rowIndex)[COL_D_SUM] <= 0
                    && deltaValues.get(rowIndex)[COL_D_SUM] <= 0;
        }

        private boolean rowFull(int rowIndex) {
            return deltaValues.get(rowIndex)[COL_D_SUM] >= 0
                    && deltaValues.get(rowIndex)[COL_D_SUM] >= 0
                    && deltaValues.get(rowIndex)[COL_D_SUM] >= 0;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == COL_SIZE)
                return false;
            else
                return true;
        }
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
