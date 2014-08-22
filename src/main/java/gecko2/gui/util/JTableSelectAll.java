package gecko2.gui.util;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * A JTable, that will select the whole text in a cell, when it is selected
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class JTableSelectAll extends JTable {
    /*
 *  Override to provide Select All editing functionality
 */
    public boolean editCellAt(int row, int column, EventObject e)
    {
        boolean result = super.editCellAt(row, column, e);
            selectAll(e);

        return result;
    }

    /*
	 * Select the text when editing on a text related cell is started
	 */
    private void selectAll(EventObject e)
    {
        final Component editor = getEditorComponent();

        if (editor == null
                || ! (editor instanceof JTextComponent))
            return;

        if (e == null)
        {
            ((JTextComponent)editor).selectAll();
            return;
        }

        //  Typing in the cell was used to activate the editor

        if (e instanceof KeyEvent)
        {
            ((JTextComponent)editor).selectAll();
            return;
        }

        //  F2 was used to activate the editor

        if (e instanceof ActionEvent)
        {
            ((JTextComponent)editor).selectAll();
            return;
        }

        //  A mouse click was used to activate the editor.
        //  Generally this is a double click and the second mouse click is
        //  passed to the editor which would remove the text selection unless
        //  we use the invokeLater()

        if (e instanceof MouseEvent)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    ((JTextComponent)editor).selectAll();
                }
            });
        }
    }
}
