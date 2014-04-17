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

package forge.toolbox;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.google.common.base.Function;

import forge.FThreads;
import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinColor.Colors;
import forge.toolbox.FList;
import forge.toolbox.FOptionPane;
import forge.util.Callback;
import forge.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simple class that shows a list of choices in a dialog. Two properties
 * influence the behavior of a list chooser: minSelection and maxSelection.
 * These two give the allowed number of selected items for the dialog to be
 * closed. A negative value for minSelection suggests that the list is revealed
 * and the choice doesn't matter.
 * <ul>
 * <li>If minSelection is 0, there will be a Cancel button.</li>
 * <li>If minSelection is -1, 0 or 1, double-clicking a choice will also close the
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
 * @version $Id: ListChooser.java 25183 2014-03-14 23:09:45Z drdev $
 */
public class ListChooser<T> {
    private static final FSkinColor BACK_COLOR = FSkinColor.get(Colors.CLR_ZEBRA);
    private static final FSkinColor ALT_ITEM_COLOR = BACK_COLOR.getContrastColor(-20);
    private static final FSkinColor SEL_COLOR = FSkinColor.get(Colors.CLR_ACTIVE);
    private static final FSkinColor BORDER_COLOR = FSkinColor.get(Colors.CLR_BORDERS);
    private static final float ITEM_HEIGHT = Utils.AVG_FINGER_HEIGHT * 0.8f;

    // Data and number of choices for the list
    private int minChoices, maxChoices;

    // Flag: was the dialog already shown?
    private boolean called;

    // initialized before; listeners may be added to it
    private ChoiceList lstChoices;
    private FOptionPane optionPane;
    private final Callback<List<T>> callback;

    public ListChooser(final String title, final int minChoices0, final int maxChoices0, final Collection<T> list, final Function<T, String> display, final Callback<List<T>> callback0) {
        FThreads.assertExecutedByEdt(true);
        minChoices = minChoices0;
        maxChoices = maxChoices0;
        lstChoices = new ChoiceList(list);
        callback = callback0;

        String[] options;
        if (minChoices == 0) {
            options = new String[] {"OK","Cancel"};
        }
        else {
            options = new String[] {"OK"};
        }

        if (maxChoices == 1 || minChoices == -1) {
            lstChoices.allowMultipleSelections = false;
        }
        else {
            lstChoices.allowMultipleSelections = true;
        }
        lstChoices.setHeight(ITEM_HEIGHT * Math.min(list.size(), 8)); //make tall enough to show 8 items without scrolling

        optionPane = new FOptionPane(null, title, null, lstChoices, options, minChoices < 0 ? 0 : -1, new Callback<Integer>() {
            @Override
            public void run(Integer result) {
                called = false;
                if (result == 0) {
                    List<T> choices = new ArrayList<T>();
                    for (int i : lstChoices.selectedIndices) {
                        choices.add(lstChoices.getItemValueAt(i));
                    }
                    callback.run(choices);
                }
                else if (minChoices > 0) {
                    show(); //show if user tries to cancel when input is mandatory
                }
                else {
                    callback.run(new ArrayList<T>());
                }
            }
        });
    }

    public void show() {
        show(null);
    }

    /**
     * Shows the dialog and returns after the dialog was closed.
     * 
     * @param index0 index to select when shown
     * @return a boolean.
     */
    public void show(final T item) {
        if (called) {
            throw new IllegalStateException("Already shown");
        }
        called = true;
        lstChoices.selectedIndices.clear();
        if (item == null) {
            if (maxChoices > 0) {
                lstChoices.selectedIndices.add(0);
            }
        }
        else {
            lstChoices.selectedIndices.add(lstChoices.getIndexOf(item));
        }
        onSelectionChange();
        optionPane.show();
    }

    private void onSelectionChange() {
        final int num = lstChoices.selectedIndices.size();
        optionPane.setButtonEnabled(0, (num >= minChoices) && (num <= maxChoices || maxChoices == -1));
    }

    public final class ChoiceList extends FList<T> {
        private boolean allowMultipleSelections;
        private List<Integer> selectedIndices = new ArrayList<Integer>();

        private ChoiceList(Collection<T> items) {
            super(items);

            setListItemRenderer(new ListItemRenderer<T>() {
                @Override
                public float getItemHeight() {
                    return ITEM_HEIGHT;
                }

                @Override
                public boolean tap(T value, float x, float y, int count) {
                    int index = lstChoices.getIndexOf(value);
                    if (allowMultipleSelections) {
                        if (selectedIndices.contains(index)) {
                            selectedIndices.remove(index);
                        }
                        else {
                            selectedIndices.add(index);
                        }
                    }
                    else {
                        selectedIndices.clear();
                        selectedIndices.add(index);
                    }
                    onSelectionChange();
                    if (count == 2 && optionPane.isButtonEnabled(0)) {
                        optionPane.setResult(0);
                    }
                    return true;
                }

                @Override
                public void drawValue(Graphics g, T value, FSkinFont font, FSkinColor foreColor, boolean pressed, float width, float height) {
                    float x = width * INSETS_FACTOR;
                    g.drawText(value.toString(), font, foreColor, x, 0, width - 2 * x, height, false, HAlignment.LEFT, true);
                }
            });
            setFontSize(12);
        }

        @Override
        protected void drawBackground(Graphics g) {
            g.fillRect(BACK_COLOR, 0, 0, getWidth(), getHeight());
        }

        @Override
        public void drawOverlay(Graphics g) {
            g.drawRect(1.5f, BORDER_COLOR, 0, 0, getWidth(), getHeight());
        }

        @Override
        protected FSkinColor getItemFillColor(ListItem item) {
            int index = Math.round(item.getTop() / ITEM_HEIGHT); //more efficient indexing strategy
            if (selectedIndices.contains(index)) {
                return SEL_COLOR;
            }
            if (index % 2 == 1) {
                return ALT_ITEM_COLOR;
            }
            return null;
        }

        @Override
        protected boolean drawLineSeparators() {
            return false;
        }
    }
}
