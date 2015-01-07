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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 *  A JComboBox that uses JCheckbox to display its items.
 *  The implementation is based on JList and JTextField, supported by glazedlists.
 */
public class JCheckList<E> extends JPanel implements ItemSelectable{
    EventList<CheckListItem<E>> basicEventList;
    JList<CheckListItem<E>> list;
    JTextField textField;
    List<ItemListener> itemListeners;

    public JCheckList(E[] values) {
        this(Arrays.asList(values));
    }

    public JCheckList(String label, E[] values) {
        this(label, Arrays.asList(values));
    }

    public JCheckList(Collection<E> collection) {
        this("", collection);
    }

    public JCheckList(String label, java.util.Collection<E> collection){
        itemListeners = new ArrayList<>();

        textField = new JTextField(25);

        basicEventList = new BasicEventList<>();
        for (E value : collection)
            basicEventList.add(new CheckListItem<>(value));

        TextMatcherEditor<CheckListItem<E>> textMatcherEditor = new TextComponentMatcherEditor<>(textField, new CheckListItemFilterator<E>());
        textMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        FilterList<CheckListItem<E>> filterList = new FilterList<>(basicEventList, textMatcherEditor);

        DefaultEventListModel<CheckListItem<E>> listModel = GlazedListsSwing.eventListModelWithThreadProxyList(filterList);

        list = new JList<>(listModel);
        list.setCellRenderer(new CheckListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(5);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                JList list = (JList) event.getSource();
                int index = list.locationToIndex(event.getPoint());
                CheckListItem item = (CheckListItem)list.getModel().getElementAt(index);
                item.setSelected(!item.isSelected());
                list.repaint(list.getCellBounds(index, index));
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);

        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("right:pref, 3dlu, fill:p:grow", "p, 5dlu, p"), this);
        builder.append(label, textField);
        builder.nextLine();
        builder.appendLineGapRow();
        builder.nextLine();
        builder.add(scrollPane, CC.xyw(builder.getColumn(), builder.getRow(), 3));
    }

    protected void fireItemStateChanged(ItemEvent e) {
        for (ItemListener listener : itemListeners)
                listener.itemStateChanged(e);
    }

    @Override
    public Object[] getSelectedObjects() {
        List<E> selected = new ArrayList<>();
        for (CheckListItem<E> item : basicEventList)
            if (item.isSelected())
                selected.add(item.value);
        return selected.toArray();
    }

    @Override
    public void addItemListener(ItemListener l) {
        itemListeners.add(l);
    }

    @Override
    public void removeItemListener(ItemListener l) {
        itemListeners.remove(l);
    }

    public void setSelectedIndex(int index) {
        if (index <= -1 || index >= basicEventList.size())
            throw new IllegalArgumentException("setSelectedIndex: " + index + " out of bounds");
        basicEventList.get(index).setSelected(true);
    }

    private class CheckListItem<T> {
        private T  value;
        private boolean isSelected = false;

        private CheckListItem(T value) {
            this.value = value;
        }

        private boolean isSelected() {
            return isSelected;
        }

        private void setSelected(boolean isSelected) {
            if (this.isSelected != isSelected) {
                this.isSelected = isSelected;
                fireItemStateChanged(new ItemEvent(JCheckList.this, ItemEvent.ITEM_STATE_CHANGED, value, isSelected() ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
            }
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    private class CheckListRenderer extends JCheckBox implements ListCellRenderer<CheckListItem<E>> {

        @Override
        public Component getListCellRendererComponent(JList<? extends CheckListItem<E>> list, CheckListItem<E> value, int index, boolean isSelected, boolean cellHasFocus) {
            setEnabled(list.isEnabled());
            setSelected(value.isSelected());
            setFont(list.getFont());
            setBackground(isSelected ? UIManager.getColor("ComboBox.selectionBackground") : UIManager.getColor("ComboBox.background"));
            setForeground(isSelected ? UIManager.getColor("ComboBox.selectionForeground") : UIManager.getColor("ComboBox.foreground"));
            setText(value.toString());
            return this;
        }
    }

    private class CheckListItemFilterator<T> implements TextFilterator<CheckListItem<T>> {
        @Override
        public void getFilterStrings(List<String> baseList, CheckListItem<T> element) {
            baseList.add(element.toString());
        }
    }

    public static void main(String args[]) {
        JFrame frame = new JFrame();
        String[] values = {"apple", "orange", "mango", "paw paw", "apple", "orange", "mango", "paw paw", "apple", "orange", "mango", "paw paw", "apple", "orange", "mango", "paw paw"};
        final JCheckList<String> l = new JCheckList<>(values);
        frame.add(l);
        frame.pack();
        frame.setVisible(true);
    }
}
