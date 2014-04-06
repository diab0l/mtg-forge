package forge.itemmanager.filters;

import forge.assets.FImage;
import forge.item.InventoryItem;
import forge.itemmanager.ItemManager;
import forge.toolbox.FEvent;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FEvent.FEventType;
import forge.toolbox.FLabel;
import forge.utils.LayoutHelper;

import java.util.ArrayList;


public abstract class ToggleButtonsFilter<T extends InventoryItem> extends ItemFilter<T> {
    protected boolean lockFiltering;
    private final ArrayList<FLabel> buttons = new ArrayList<FLabel>();

    protected ToggleButtonsFilter(ItemManager<? super T> itemManager0) {
        super(itemManager0);
    }

    protected ToggleButton addToggleButton(Widget widget, FImage icon) {
        final ToggleButton button = new ToggleButton(icon);

        this.buttons.add(button);
        widget.add(button);
        return button;
    }

    @Override
    protected void doWidgetLayout(LayoutHelper helper) {
        float availableWidth = helper.getParentWidth() - (buttons.size() - 1) * (helper.getGapX() - 1); //account for gaps
        float buttonWidth = availableWidth / buttons.size();
        float buttonHeight = helper.getParentHeight() - 2 * helper.getGapY();

        for (FLabel btn : buttons) {
            helper.include(btn, buttonWidth, buttonHeight);
            helper.offset(-1, 0); //keep buttons tighter together
        }
    }

    @Override
    public final boolean isEmpty() {
        for (FLabel button : buttons) { //consider filter empty if any button isn't selected
            if (!button.isSelected()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void reset() {
        for (FLabel button : buttons) {
            button.setSelected(true);
        }
    }
    
    /**
     * Merge the given filter with this filter if possible
     * @param filter
     * @return true if filter merged in or to suppress adding a new filter, false to allow adding new filter
     */
    @Override
    public boolean merge(ItemFilter<?> filter) {
        return true;
    }

    public class ToggleButton extends FLabel {
        private FEventHandler longPressHandler;

        private ToggleButton(FImage icon) {
            super(new FLabel.Builder()
                .icon(icon).iconScaleAuto(false)
                .fontSize(11)
                .selectable(true).selected(true)
                .command(new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        if (lockFiltering) { return; }
                        applyChange();
                    }
                }));
        }

        public void setLongPressHandler(FEventHandler longPressHandler0) {
            longPressHandler = longPressHandler0;
        }

        @Override
        public boolean longPress(float x, float y) {
            if (longPressHandler != null) {
                longPressHandler.handleEvent(new FEvent(this, FEventType.LONG_PRESS));
                return true;
            }
            return false;
        }
    }
}
