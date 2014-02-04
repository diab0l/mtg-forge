package forge.gui.toolbox.itemmanager.views;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSkin.SkinColor;
import forge.gui.toolbox.FSkin.SkinImage;
import forge.gui.toolbox.ToolTipListener;
import forge.gui.toolbox.itemmanager.ItemManager;
import forge.gui.toolbox.itemmanager.ItemManagerModel;
import forge.item.InventoryItem;

public abstract class ItemView<T extends InventoryItem> {
    private static final SkinColor BORDER_COLOR = FSkin.getColor(FSkin.Colors.CLR_TEXT);

    protected final ItemManager<T> itemManager;
    protected final ItemManagerModel<T> model;
    private final FScrollPane scroller;
    private final FLabel button;
    private int heightBackup;
    private boolean isIncrementalSearchActive = false;

    protected ItemView(ItemManager<T> itemManager0, ItemManagerModel<T> model0) {
        this.itemManager = itemManager0;
        this.model = model0;
        this.scroller = new FScrollPane(false);
        this.scroller.setBorder(new FSkin.LineSkinBorder(BORDER_COLOR));
        this.button = new FLabel.Builder().hoverable().selectable(true)
            .icon(getIcon()).iconScaleAuto(false)
            .tooltip(getCaption()).build();
    }

    public void initialize(final int index) {
        final JComponent comp = this.getComponent();

        //hook incremental search functionality
        final IncrementalSearch incrementalSearch  = new IncrementalSearch();
        comp.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent arg0) {
                incrementalSearch.cancel();
            }
        });
        comp.addKeyListener(incrementalSearch);

        this.button.setCommand(new Runnable() {
            @Override
            public void run() {
                itemManager.setViewIndex(index);
            }
        });

        this.scroller.setViewportView(comp);
        this.scroller.getVerticalScrollBar().addAdjustmentListener(new ToolTipListener());
        this.scroller.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onResize();
                //scroll selection into view whenever view height changes
                int height = e.getComponent().getHeight();
                if (height != heightBackup) {
                    heightBackup = height;
                    scrollSelectionIntoView();
                }
            }
        });
    }

    public FLabel getButton() {
        return this.button;
    }

    public FScrollPane getScroller() {
        return this.scroller;
    }

    public boolean isIncrementalSearchActive() {
        return this.isIncrementalSearchActive;
    }

    public void refresh(final Iterable<T> itemsToSelect, final int backupIndexToSelect) {
        this.model.refreshSort();
        onRefresh();
        fixSelection(itemsToSelect, backupIndexToSelect);
    }
    protected abstract void onResize();
    protected abstract void onRefresh();
    private void fixSelection(final Iterable<T> itemsToSelect, final int backupIndexToSelect) {
        if (itemsToSelect == null) {
            setSelectedIndex(0, false); //select first item if no items to select
            getScroller().getVerticalScrollBar().setValue(0); //ensure scrolled to top
        }
        else {
            if (!setSelectedItems(itemsToSelect)) {
                setSelectedIndex(backupIndexToSelect);
            }
        }
    }

    public final T getSelectedItem() {
        int index = getSelectedIndex();
        return index >= 0 ? getItemAtIndex(index) : null;
    }

    public final Collection<T> getSelectedItems() {
        List<T> items = new ArrayList<T>();
        for (Integer i : getSelectedIndices()) {
            items.add(getItemAtIndex(i));
        }
        return items;
    }

    public final boolean setSelectedItem(T item) {
        return setSelectedItem(item, true);
    }
    public final boolean setSelectedItem(T item, boolean scrollIntoView) {
        int index = getIndexOfItem(item);
        if (index != -1) {
            setSelectedIndex(index, scrollIntoView);
            return true;
        }
        return false;
    }

    public final boolean setSelectedItems(Iterable<T> items) {
        return setSelectedItems(items, true);
    }
    public final boolean setSelectedItems(Iterable<T> items, boolean scrollIntoView) {
        List<Integer> indices = new ArrayList<Integer>();
        for (T item : items) {
            int index = getIndexOfItem(item);
            if (index != -1) {
                indices.add(index);
            }
        }
        if (indices.size() > 0) {
            onSetSelectedIndices(indices);
            if (scrollIntoView) {
                scrollSelectionIntoView();
            }
            return true;
        }
        return false;
    }

    public void setSelectedIndex(int index) {
        setSelectedIndex(index, true);
    }
    public void setSelectedIndex(int index, boolean scrollIntoView) {
        int count = getCount();
        if (count == 0) { return; }

        if (index < 0) {
            index = 0;
        }
        else if (index >= count) {
            index = count - 1;
        }

        onSetSelectedIndex(index);
        if (scrollIntoView) {
            scrollSelectionIntoView();
        }
    }

    public void setSelectedIndices(Iterable<Integer> indices) {
        setSelectedIndices(indices, true);
    }
    public void setSelectedIndices(Iterable<Integer> indices, boolean scrollIntoView) {
        int count = getCount();
        if (count == 0) { return; }

        List<Integer> indexList = new ArrayList<Integer>();
        for (Integer index : indices) {
            if (index >= 0 && index < count) {
                indexList.add(index);
            }
        }

        if (indexList.isEmpty()) { //if no index in range, set selected index based on first index
            for (Integer index : indices) {
                setSelectedIndex(index);
                return;
            }
            return;
        }

        onSetSelectedIndices(indexList);
        if (scrollIntoView) {
            scrollSelectionIntoView();
        }
    }

    protected void onSelectionChange() {
        final int index = getSelectedIndex();
        if (index != -1) {
            ListSelectionEvent event = new ListSelectionEvent(itemManager, index, index, false);
            for (ListSelectionListener listener : itemManager.getSelectionListeners()) {
                listener.valueChanged(event);
            }
        }
    }

    public void scrollSelectionIntoView() {
        Container parent = getComponent().getParent();
        if (parent instanceof JViewport) {
            onScrollSelectionIntoView((JViewport)parent);
        }
    }

    public void focus() {
        this.getComponent().requestFocusInWindow();
    }

    public boolean hasFocus() {
        return this.getComponent().hasFocus();
    }

    public Point getLocationOnScreen() {
        return this.getComponent().getParent().getLocationOnScreen(); //use parent scroller's location by default
    }

    @Override
    public String toString() {
        return this.getCaption(); //return caption as string for display in combo box
    }

    public abstract JComponent getComponent();
    public abstract void setAllowMultipleSelections(boolean allowMultipleSelections);
    public abstract T getItemAtIndex(int index);
    public abstract int getIndexOfItem(T item);
    public abstract int getSelectedIndex();
    public abstract Iterable<Integer> getSelectedIndices();
    public abstract void selectAll();
    public abstract int getCount();
    public abstract int getSelectionCount();
    public abstract int getIndexAtPoint(Point p);
    protected abstract SkinImage getIcon();
    protected abstract String getCaption();
    protected abstract void onSetSelectedIndex(int index);
    protected abstract void onSetSelectedIndices(Iterable<Integer> indices);
    protected abstract void onScrollSelectionIntoView(JViewport viewport);

    private class IncrementalSearch extends KeyAdapter {
        private StringBuilder str = new StringBuilder();
        private final FLabel popupLabel = new FLabel.Builder().fontAlign(SwingConstants.LEFT).opaque().build();
        private boolean popupShowing = false;
        private Popup popup;
        private Timer popupTimer;
        private static final int okModifiers = KeyEvent.SHIFT_MASK | KeyEvent.ALT_GRAPH_MASK;

        public IncrementalSearch() {
        }

        private void setPopupSize() {
            // resize popup to size of label (ensure there's room for the next character so the label
            // doesn't show '...' in the time between when we set the text and when we increase the size
            Dimension labelDimension = popupLabel.getPreferredSize();
            Dimension popupDimension = new Dimension(labelDimension.width + 12, labelDimension.height + 4);
            SwingUtilities.getRoot(popupLabel).setSize(popupDimension);
        }

        private void findNextMatch(int startIdx, boolean reverse) {
            int numItems = itemManager.getItemCount();
            if (0 == numItems) {
                cancel();
                return;
            }

            // find the next item that matches the string
            startIdx %= numItems;
            final int increment = reverse ? numItems - 1 : 1;
            int stopIdx = (startIdx + numItems - increment) % numItems;
            String searchStr = str.toString();
            boolean found = false;
            for (int idx = startIdx;; idx = (idx + increment) % numItems) {
                if (StringUtils.containsIgnoreCase(ItemView.this.getItemAtIndex(idx).getName(), searchStr)) {
                    ItemView.this.setSelectedIndex(idx);
                    found = true;
                    break;
                }

                if (idx == stopIdx) {
                    break;
                }
            }

            if (searchStr.isEmpty()) {
                cancel();
                return;
            }

            // show a popup with the current search string, highlighted in red if not found
            popupLabel.setText(searchStr + " (hit Enter for next match, Esc to cancel)");
            if (found) {
                popupLabel.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
            }
            else {
                popupLabel.setForeground(new Color(255, 0, 0));
            }

            if (popupShowing) {
                setPopupSize();
                popupTimer.restart();
            }
            else {
                PopupFactory factory = PopupFactory.getSharedInstance();
                Point tableLoc = ItemView.this.getLocationOnScreen();
                popup = factory.getPopup(null, popupLabel, tableLoc.x + 10, tableLoc.y + 10);
                FSkin.setTempBackground(SwingUtilities.getRoot(popupLabel), FSkin.getColor(FSkin.Colors.CLR_INACTIVE));

                popupTimer = new Timer(5000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cancel();
                    }
                });
                popupTimer.setRepeats(false);

                popup.show();
                setPopupSize();
                popupTimer.start();
                isIncrementalSearchActive = true;
                popupShowing = true;
            }
        }

        public void cancel() {
            str = new StringBuilder();
            popupShowing = false;
            if (null != popup) {
                popup.hide();
                popup = null;
            }
            if (null != popupTimer) {
                popupTimer.stop();
                popupTimer = null;
            }
            isIncrementalSearchActive = false;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (popupShowing) {
                if (KeyEvent.VK_ESCAPE == e.getKeyCode()) {
                    cancel();
                }
            }
            else {
                for (KeyListener keyListener : itemManager.getKeyListeners()) {
                    keyListener.keyPressed(e);
                    if (e.isConsumed()) { return; }
                }
                if (KeyEvent.VK_F == e.getKeyCode()) {
                    // let ctrl/cmd-F set focus to the text filter box
                    if (e.isControlDown() || e.isMetaDown()) {
                        itemManager.focusSearch();
                    }
                }
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if (!popupShowing) {
                for (KeyListener keyListener : itemManager.getKeyListeners()) {
                    keyListener.keyTyped(e);
                    if (e.isConsumed()) { return; }
                }
            }

            switch (e.getKeyChar()) {
            case KeyEvent.CHAR_UNDEFINED:
                return;

            case KeyEvent.VK_ENTER:
            case 13: // no KeyEvent constant for this, but this comes up on OSX for shift-enter
                if (!str.toString().isEmpty()) {
                    // no need to add (or subtract) 1 -- the table selection will already
                    // have been advanced by the (shift+) enter key
                    findNextMatch(ItemView.this.getSelectedIndex(), e.isShiftDown());
                }
                return;

            case KeyEvent.VK_BACK_SPACE:
                if (!str.toString().isEmpty()) {
                    str.deleteCharAt(str.toString().length() - 1);
                }
                break;

            case KeyEvent.VK_SPACE:
                // don't trigger if the first character is a space
                if (str.toString().isEmpty()) {
                    return;
                }
                // fall through

            default:
                // shift and/or alt-graph down is ok.  anything else is a hotkey (e.g. ctrl-f)
                if (okModifiers != (e.getModifiers() | okModifiers)
                 || !CharUtils.isAsciiPrintable(e.getKeyChar())) { // escape sneaks in here on Windows
                    return;
                }
                str.append(e.getKeyChar());
            }

            findNextMatch(Math.max(0, ItemView.this.getSelectedIndex()), false);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (!popupShowing) {
                for (KeyListener keyListener : itemManager.getKeyListeners()) {
                    keyListener.keyReleased(e);
                    if (e.isConsumed()) { return; }
                }
            }
        }
    }
}
