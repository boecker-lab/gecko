/*
 * Copyright 2014 Sascha Winter
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

package de.unijena.bioinf.gecko3.gui.util;

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
