package forge.gui.toolbox.itemmanager.filters;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.common.base.Predicate;

import forge.gui.framework.ILocalRepaint;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSkin.Colors;
import forge.gui.toolbox.FSkin.JComponentSkin;
import forge.gui.toolbox.FSkin.JLabelSkin;
import forge.gui.toolbox.FSkin.SkinColor;
import forge.gui.toolbox.FTextField;
import forge.gui.toolbox.LayoutHelper;
import forge.gui.toolbox.itemmanager.ItemManager;
import forge.item.InventoryItem;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public abstract class ItemFilter<T extends InventoryItem> {
    public final static int PANEL_HEIGHT = 28;

    public static void layoutCheckbox(JCheckBox cb) {
        JComponentSkin<JCheckBox> skin = FSkin.get(cb);
        skin.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        skin.setFont(FSkin.getFont(12));
        cb.setOpaque(false);
        cb.setFocusable(false);
    }

    protected final ItemManager<? super T> itemManager;
    private FilterPanel panel;
    private Widget widget;
    private final JCheckBox chkEnable = new JCheckBox();
    private RemoveButton btnRemove;

    protected ItemFilter(ItemManager<? super T> itemManager0) {
        this.itemManager = itemManager0;
        this.chkEnable.setSelected(true); //enable by default
    }

    public JPanel getPanel() {
        if (this.panel == null) {
            this.panel = new FilterPanel();

            layoutCheckbox(this.chkEnable);
            this.chkEnable.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent arg0) {
                    updateEnabled();
                    applyChange();
                }
            });
            this.panel.add(this.chkEnable);

            getWidget(); //initialize widget
            if (!isEnabled()) {
                updateEnabled();
            }
            this.panel.add(this.widget);

            this.btnRemove = new RemoveButton();
            this.panel.add(this.btnRemove);
        }
        return this.panel;
    }

    public JPanel getWidget() {
        if (this.widget == null) {
            this.widget = new Widget();
            this.buildWidget(this.widget);
        }
        return this.widget;
    }

    public void refreshWidget() {
        if (this.widget == null) { return; }
        this.widget.removeAll();
        this.buildWidget(this.widget);
    }

    public Component getMainComponent() {
        return getWidget();
    }

    public void setNumber(int number) {
        this.chkEnable.setText("(" + number + ")");
    }

    public boolean isEnabled() {
        return this.chkEnable.isSelected();
    }

    public void setEnabled(boolean enabled0) {
        this.chkEnable.setSelected(enabled0);
    }

    public void updateEnabled() {
        boolean enabled = this.isEnabled();
        for (Component comp : this.widget.getComponents()) {
            comp.setEnabled(enabled);
        }
    }

    protected void applyChange() {
        this.itemManager.applyFilters();
    }

    public final <U extends InventoryItem> Predicate<U> buildPredicate(Class<U> genericType) {
        final Predicate<T> predicate = this.buildPredicate();
        return new Predicate<U>() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean apply(U item) {
                try {
                    return predicate.apply((T)item);
                }
                catch (Exception ex) {
                    return showUnsupportedItem(item); //if can't cast U to T, filter item out unless derived class can handle it
                }
            }
        };
    }

    protected <U extends InventoryItem> boolean showUnsupportedItem(U item) {
        return false; //don't show unsupported items by default
    }

    public abstract ItemFilter<T> createCopy();
    public abstract boolean isEmpty();
    public abstract void reset();
    public void afterFiltersApplied() {
    }

    /**
     * Merge the given filter with this filter if possible
     * @param filter
     * @return true if filter merged in or to suppress adding a new filter, false to allow adding new filter
     */
    public abstract boolean merge(ItemFilter<?> filter);

    protected abstract void buildWidget(JPanel widget);
    protected abstract void doWidgetLayout(LayoutHelper helper);
    protected abstract Predicate<T> buildPredicate();

    @SuppressWarnings("serial")
    private class FilterPanel extends JPanel {
        private FilterPanel() {
            setLayout(null);
            setOpaque(false);
            FSkin.get(this).setMatteBorder(0, 0, 1, 0, FSkin.getColor(Colors.CLR_TEXT));
        }

        @Override
        public void doLayout() {
            LayoutHelper helper = new LayoutHelper(this);
            int removeButtonSize = 17;
            helper.include(chkEnable, 43, FTextField.HEIGHT);
            helper.offset(-3, 0); //avoid extra padding between checkbox and widget
            helper.fillLine(widget, PANEL_HEIGHT, removeButtonSize); //leave room for remove button
            helper.offset(-3, (PANEL_HEIGHT - removeButtonSize) / 2 - 1); //shift position of remove button
            helper.include(btnRemove, removeButtonSize, removeButtonSize);
        }
    }

    @SuppressWarnings("serial")
    private class Widget extends JPanel {
        private Widget() {
            setLayout(null);
            setOpaque(false);
        }

        @Override
        public void doLayout() {
            LayoutHelper helper = new LayoutHelper(this);
            ItemFilter.this.doWidgetLayout(helper);
        }
    }

    @SuppressWarnings("serial")
    private class RemoveButton extends JLabel implements ILocalRepaint {
        protected JLabelSkin<RemoveButton> skin = FSkin.get(this);
        private final SkinColor iconColor = FSkin.getColor(Colors.CLR_TEXT);
        private boolean pressed, hovered;

        private RemoveButton() {
            setToolTipText("Remove filter");
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!RemoveButton.this.isEnabled()) { return; }
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        pressed = true;
                        repaintSelf();
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (pressed && SwingUtilities.isLeftMouseButton(e)) {
                        pressed = false;
                        if (hovered) { //only handle click if mouse released over button
                            repaintSelf();
                            itemManager.focus();
                            itemManager.removeFilter(ItemFilter.this);
                        }
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!RemoveButton.this.isEnabled()) { return; }
                    hovered = true;
                    repaintSelf();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (hovered) {
                        hovered = false;
                        repaintSelf();
                    }
                }
            });
        }

        @Override
        public void setEnabled(boolean enabled0) {
            if (!enabled0 && hovered) {
                hovered = false; //ensure hovered reset if disabled
            }
            super.setEnabled(enabled0);
        }

        @Override
        public void repaintSelf() {
            final Dimension d = this.getSize();
            repaint(0, 0, d.width, d.height);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            int thickness = 2;
            int offset = 4;
            int x1 = offset;
            int y1 = offset;
            int x2 = getWidth() - offset - 1;
            int y2 = getHeight() - offset - 1;

            Graphics2D g2d = (Graphics2D) g;
            if (hovered) {
                if (pressed) {
                    g.translate(1, 1); //translate icon to give pressed button look
                }
                skin.setGraphicsColor(g2d, iconColor);
            }
            else {
                skin.setGraphicsColor(g2d, iconColor.alphaColor(150));
            }
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(thickness));
            g2d.drawLine(x1, y1, x2, y2);
            g2d.drawLine(x2, y1, x1, y2);
        }
    }
}
