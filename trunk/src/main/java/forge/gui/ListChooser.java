/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package forge.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import forge.FThreads;

/**
 * A simple class that shows a list of choices in a dialog. Two properties
 * influence the behavior of a list chooser: minSelection and maxSelection.
 * These two give the allowed number of selected items for the dialog to be
 * closed.
 * <ul>
 * <li>If minSelection is 0, there will be a Cancel button.</li>
 * <li>If minSelection is 0 or 1, double-clicking a choice will also close the
 * dialog.</li>
 * <li>If the number of selections is out of bounds, the "OK" button is
 * disabled.</li>
 * <li>The dialog was "committed" if "OK" was clicked or a choice was double
 * clicked.</li>
 * <li>The dialog was "canceled" if "Cancel" or "X" was clicked.</li>
 * <li>If the dialog was canceled, the selection will be empty.</li>
 * <li>
 * </ul>
 * 
 * @param <T>
 *            the generic type
 * @author Forge
 * @version $Id$
 */
public class ListChooser<T> {

    // Data and number of choices for the list
    private List<T> list;
    private int minChoices, maxChoices;

    // Decoration
    private String title;

    // Flag: was the dialog already shown?
    private boolean called;
    // initialized before; listeners may be added to it
    private JList<T> jList;
    // Temporarily stored for event handlers during show
    private JDialog dialog;
    private JOptionPane optionPane;
    private Action ok, cancel;

    public ListChooser(final String title, final int minChoices, final int maxChoices, final Collection<T> list, final Function<T, String> display) {
        FThreads.assertExecutedByEdt(true);
        this.title = title;
        this.minChoices = minChoices;
        this.maxChoices = maxChoices;
        this.list = list.getClass().isInstance(List.class) ? (List<T>)list : Lists.newArrayList(list);
        this.jList = new JList<T>(new ChooserListModel());
        this.ok = new CloseAction(JOptionPane.OK_OPTION, "OK");
        this.ok.setEnabled(minChoices == 0);
        this.cancel = new CloseAction(JOptionPane.CANCEL_OPTION, "Cancel");

        Object[] options;
        if (minChoices == 0) {
            options = new Object[] { new JButton(this.ok), new JButton(this.cancel) };
        } else {
            options = new Object[] { new JButton(this.ok) };
        }
        if (maxChoices == 1) {
            this.jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        
        if( null != display )
            this.jList.setCellRenderer(new TransformedCellRenderer(display));

        this.optionPane = new JOptionPane(new JScrollPane(this.jList), JOptionPane.QUESTION_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, options, options[0]);
        this.jList.getSelectionModel().addListSelectionListener(new SelListener());
        this.jList.addMouseListener(new DblListener());
    }

    /**
     * Returns the JList used in the list chooser. this is useful for
     * registering listeners before showing the dialog.
     * 
     * @return a {@link javax.swing.JList} object.
     */
    public JList<T> getJList() {
        return this.jList;
    }

    /** @return boolean */
    public boolean show() {
        return show(list.get(0));
    }

    /**
     * Shows the dialog and returns after the dialog was closed.
     * 
     * @param index0 index to select when shown
     * @return a boolean.
     */
    public boolean show(T item) {
        if (this.called) {
            throw new IllegalStateException("Already shown");
        }
        Integer value;
        do {
            this.dialog = this.optionPane.createDialog(this.optionPane.getParent(), this.title);
            if (this.minChoices != 0) {
                this.dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            }

            if (list.contains(item)) {
                jList.setSelectedValue(item, true);
            } else {
                jList.setSelectedIndex(0);
            }

            this.dialog.addWindowFocusListener(new WindowFocusListener() {
                @Override
                public void windowGainedFocus(final WindowEvent e) {
                    ListChooser.this.jList.grabFocus();
                }

                @Override
                public void windowLostFocus(final WindowEvent e) {
                }
            });
            this.dialog.setVisible(true);
            this.dialog.dispose();
            value = (Integer) this.optionPane.getValue();
            if ((value == null) || (value != JOptionPane.OK_OPTION)) {
                this.jList.clearSelection();
                // can't stop closing by ESC, so repeat if cancelled
            }
        } while ((this.minChoices != 0) && (value != JOptionPane.OK_OPTION));
        // this assert checks if we really don't return on a cancel if input is
        // mandatory
        assert (this.minChoices == 0) || (value == JOptionPane.OK_OPTION);
        this.called = true;
        return (value != null) && (value == JOptionPane.OK_OPTION);
    }

    /**
     * Returns if the dialog was closed by pressing "OK" or double clicking an
     * option the last time.
     * 
     * @return a boolean.
     */
    public boolean isCommitted() {
        if (!this.called) {
            throw new IllegalStateException("not yet shown");
        }
        return (Integer) this.optionPane.getValue() == JOptionPane.OK_OPTION;
    }

    /**
     * Returns the selected indices as a list of integers.
     * 
     * @return a {@link java.util.List} object.
     */
    public int[] getSelectedIndices() {
        if (!this.called) {
            throw new IllegalStateException("not yet shown");
        }
        return this.jList.getSelectedIndices();
    }

    /**
     * Returns the selected values as a list of objects. no casts are necessary
     * when retrieving the objects.
     * 
     * @return a {@link java.util.List} object.
     */
    public List<T> getSelectedValues() {
        if (!this.called) {
            throw new IllegalStateException("not yet shown");
        }
        return this.jList.getSelectedValuesList();
    }

    /**
     * Returns the (minimum) selected index, or -1.
     * 
     * @return a int.
     */
    public int getSelectedIndex() {
        if (!this.called) {
            throw new IllegalStateException("not yet shown");
        }
        return this.jList.getSelectedIndex();
    }

    /**
     * Returns the (first) selected value, or null.
     * 
     * @return a T object.
     */
    public T getSelectedValue() {
        if (!this.called) {
            throw new IllegalStateException("not yet shown");
        }
        return (T) this.jList.getSelectedValue();
    }

    /**
     * <p>
     * commit.
     * </p>
     */
    private void commit() {
        if (this.ok.isEnabled()) {
            this.optionPane.setValue(JOptionPane.OK_OPTION);
        }
    }

    private class ChooserListModel extends AbstractListModel<T> {

        private static final long serialVersionUID = 3871965346333840556L;

        @Override
        public int getSize() {
            return ListChooser.this.list.size();
        }

        @Override
        public T getElementAt(final int index) {
            return ListChooser.this.list.get(index);
        }
    }

    private class CloseAction extends AbstractAction {

        private static final long serialVersionUID = -8426767786083886936L;
        private final int value;

        public CloseAction(final int value, final String label) {
            super(label);
            this.value = value;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            ListChooser.this.optionPane.setValue(this.value);
        }
    }

    private class SelListener implements ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            final int num = ListChooser.this.jList.getSelectedIndices().length;
            ListChooser.this.ok
                    .setEnabled((num >= ListChooser.this.minChoices) && (num <= ListChooser.this.maxChoices));
        }
    }

    private class DblListener extends MouseAdapter {
        @Override
        public void mouseClicked(final MouseEvent e) {
            if (e.getClickCount() == 2) {
                ListChooser.this.commit();
            }
        }
    }
    
    private class TransformedCellRenderer implements ListCellRenderer<T> {
        public final Function<T, String> transformer;
        public final DefaultListCellRenderer defRenderer;
        
        /**
         * TODO: Write javadoc for Constructor.
         */
        public TransformedCellRenderer(final Function<T, String> t1) {
            transformer = t1;
            defRenderer = new DefaultListCellRenderer();
        }

        /* (non-Javadoc)
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        @Override
        public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected,
                boolean cellHasFocus) {
            // TODO Auto-generated method stub
            return defRenderer.getListCellRendererComponent(list, transformer.apply(value), index, isSelected, cellHasFocus);
        }
        


    }
}
