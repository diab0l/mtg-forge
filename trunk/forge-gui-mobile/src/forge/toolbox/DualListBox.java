package forge.toolbox;

import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.screens.match.views.VPrompt;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FEvent.FEventType;
import forge.util.Callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

// An input box for handling the order of choices.
// Left box has the original choices
// Right box has the final order
// Top string will be like Top of the Stack or Top of the Library
// Bottom string will be like Bottom of the Stack or Bottom of the Library
// Single Arrows in between left box and right box for ordering
// Multi Arrows for moving everything in order
// Up/down arrows on the right of the right box for swapping
// Single ok button, disabled until left box has specified number of items remaining
public class DualListBox<T> extends FDialog {
    private final ChoiceList sourceList;
    private final ChoiceList destList;

    private final FButton addButton;
    private final FButton addAllButton;
    private final FButton removeButton;
    private final FButton removeAllButton;
    private final FButton okButton;
    private final FButton autoButton;

    private final FLabel orderedLabel;
    private final FLabel selectOrder;

    private final int targetRemainingSourcesMin;
    private final int targetRemainingSourcesMax;

    private boolean sideboardingMode = false;

    public DualListBox(String title, int remainingSources, List<T> sourceElements, List<T> destElements, final Callback<List<T>> callback) {
        this(title, remainingSources, remainingSources, sourceElements, destElements, callback);
    }
    
    public DualListBox(String title, int remainingSourcesMin, int remainingSourcesMax, List<T> sourceElements, List<T> destElements, final Callback<List<T>> callback) {
        super(title);
        targetRemainingSourcesMin = remainingSourcesMin;
        targetRemainingSourcesMax = remainingSourcesMax;

        final FEventHandler onAdd = new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                if (!addButton.isEnabled()) { return; }

                List<T> selected = new ArrayList<T>();
                for (int index : sourceList.selectedIndices) {
                    selected.add(sourceList.getItemAt(index));
                }
                destList.selectedIndices.clear();
                for (T item : selected) {
                    sourceList.removeItem(item);
                    destList.selectedIndices.add(destList.getCount());
                    destList.addItem(item);
                }
                sourceList.cleanUpSelections();
                setButtonState();
            }
        };

        final FEventHandler onRemove = new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                if (!removeButton.isEnabled()) { return; }

                List<T> selected = new ArrayList<T>();
                for (int index : destList.selectedIndices) {
                    selected.add(destList.getItemAt(index));
                }
                sourceList.selectedIndices.clear();
                for (T item : selected) {
                    destList.removeItem(item);
                    sourceList.selectedIndices.add(sourceList.getCount());
                    sourceList.addItem(item);
                }
                destList.cleanUpSelections();
                setButtonState();
            }
        };

        sourceList = add(new ChoiceList(sourceElements, onAdd));
        destList = add(new ChoiceList(destElements, onRemove));
        if (sourceList.getCount() > 0) { //select first items by default if possible
            sourceList.selectedIndices.add(0);
        }
        if (destList.getCount() > 0) {
            destList.selectedIndices.add(0);
        }

        // Dual List control buttons
        addButton = add(new FButton(">", onAdd));
        addAllButton = add(new FButton(">>", new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                addAll();
            }
        }));
        removeButton = add(new FButton("<", onRemove));
        removeAllButton = add(new FButton("<<", new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                removeAll();
            }
        }));
        
        final FEventHandler onAccept = new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                hide();
                callback.run(destList.extractListData());
            }
        };

        // Dual List Complete Buttons
        okButton = add(new FButton("OK", onAccept));
        autoButton = add(new FButton("Auto", new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                addAll();
                onAccept.handleEvent(e);
            }
        }));

        selectOrder = add(new FLabel.Builder().align(HAlignment.CENTER).text("Select Order").build());
        orderedLabel = add(new FLabel.Builder().align(HAlignment.CENTER).build());

        setButtonState();
    }

    @Override
    protected float layoutAndGetHeight(float width, float maxHeight) {
        float x = FOptionPane.PADDING;
        float y = FOptionPane.PADDING / 2;
        width -= 2 * x;
        maxHeight -= 2 * (VPrompt.HEIGHT - FDialog.INSETS);

        float gapBetweenButtons = FOptionPane.PADDING / 2;
        float buttonHeight = FOptionPane.BUTTON_HEIGHT;
        float labelHeight = selectOrder.getAutoSizeBounds().height;
        float listHeight = (maxHeight - 2 * labelHeight - buttonHeight - FOptionPane.PADDING - 2 * FDialog.INSETS) / 2;
        float addButtonWidth = addAllButton.getAutoSizeBounds().width * 1.2f;
        float addButtonHeight = listHeight / 2 - gapBetweenButtons;
        float listWidth = width - addButtonWidth - gapBetweenButtons;

        selectOrder.setBounds(x, y, width, labelHeight);
        y += labelHeight;
        sourceList.setBounds(x, y, listWidth, listHeight);
        x += width - addButtonWidth;
        addButton.setBounds(x, y, addButtonWidth, addButtonHeight);
        addAllButton.setBounds(x, y + addButtonHeight + gapBetweenButtons, addButtonWidth, addButtonHeight);
        y += listHeight + FOptionPane.PADDING / 2;

        x = FOptionPane.PADDING;
        orderedLabel.setBounds(x, y, width, labelHeight);
        y += labelHeight;
        removeButton.setBounds(x, y, addButtonWidth, addButtonHeight);
        removeAllButton.setBounds(x, y + addButtonHeight + gapBetweenButtons, addButtonWidth, addButtonHeight);
        destList.setBounds(x + width - listWidth, y, listWidth, listHeight);
        y += listHeight + FOptionPane.PADDING;

        float buttonWidth = (width - gapBetweenButtons) / 2;
        okButton.setBounds(x, y, buttonWidth, buttonHeight);
        x += buttonWidth + gapBetweenButtons;
        autoButton.setBounds(x, y, buttonWidth, buttonHeight);

        return maxHeight;
    }

    public void setSecondColumnLabelText(String label) {
        orderedLabel.setText(label);
    }

    public void setSideboardMode(boolean isSideboardMode) {
        sideboardingMode = isSideboardMode;
        if (sideboardingMode) {
            addAllButton.setVisible(false);
            removeAllButton.setVisible(false);
            autoButton.setEnabled(false);
            selectOrder.setText(String.format("Sideboard (%d):", sourceList.getCount()));
            orderedLabel.setText(String.format("Main Deck (%d):", destList.getCount()));
        }
    }

    public List<T> getRemainingSourceList() {
        return sourceList.extractListData();
    }

    private void addAll() {
        destList.selectedIndices.clear();
        for (T item : sourceList) {
            destList.selectedIndices.add(destList.getCount());
            destList.addItem(item);
        }
        sourceList.clear();
        sourceList.selectedIndices.clear();
        setButtonState();
    }

    private void removeAll() {
        sourceList.selectedIndices.clear();
        for (T item : destList) {
            sourceList.selectedIndices.add(sourceList.getCount());
            sourceList.addItem(item);
        }
        destList.clear();
        destList.selectedIndices.clear();
        setButtonState();
    }

    private void setButtonState() {
        if (sideboardingMode) {
            removeAllButton.setVisible(false);
            addAllButton.setVisible(false);
            selectOrder.setText(String.format("Sideboard (%d):", sourceList.getCount()));
            orderedLabel.setText(String.format("Main Deck (%d):", destList.getCount()));
        }

        boolean anySize = targetRemainingSourcesMax < 0;
        boolean canAdd = sourceList.getCount() != 0 && (anySize || targetRemainingSourcesMin <= sourceList.getCount());
        boolean canRemove = destList.getCount() != 0;
        boolean targetReached = anySize || targetRemainingSourcesMin <= sourceList.getCount() && targetRemainingSourcesMax >= sourceList.getCount();

        autoButton.setEnabled(targetRemainingSourcesMax == 0 && !targetReached && !sideboardingMode);

        addButton.setEnabled(canAdd);
        addAllButton.setEnabled(canAdd);
        removeButton.setEnabled(canRemove);
        removeAllButton.setEnabled(canRemove);
        okButton.setEnabled(targetReached);
    }

    private class ChoiceList extends FList<T> {
        private List<Integer> selectedIndices = new ArrayList<Integer>();

        private ChoiceList(Collection<T> items, final FEventHandler dblTapCommand) {
            super(items != null ? items : new ArrayList<T>()); //handle null without crashing

            setListItemRenderer(new ListItemRenderer<T>() {
                @Override
                public float getItemHeight() {
                    return ListChooser.ITEM_HEIGHT;
                }

                @Override
                public boolean tap(T value, float x, float y, int count) {
                    Integer index = ChoiceList.this.getIndexOf(value);
                    selectedIndices.clear();
                    selectedIndices.add(index);
                    if (count == 2) {
                        dblTapCommand.handleEvent(new FEvent(ChoiceList.this, FEventType.ACTIVATE, index));
                    }
                    return true;
                }

                @Override
                public void drawValue(Graphics g, T value, FSkinFont font, FSkinColor foreColor, boolean pressed, float x, float y, float w, float h) {
                    g.drawText(value.toString(), font, foreColor, x, y, w, h, false, HAlignment.LEFT, true);
                }
            });
            setFontSize(12);
        }

        //remove any selected indices outside item range
        public void cleanUpSelections() {
            int count = getCount();
            for (int i = 0; i < selectedIndices.size(); i++) {
                if (selectedIndices.get(i) >= count) {
                    selectedIndices.remove(i);
                    i--;
                }
            }
            if (selectedIndices.isEmpty() && count > 0) {
                selectedIndices.add(count - 1); //select last item if nothing remains selected
            }
        }

        @Override
        protected void drawBackground(Graphics g) {
            //draw no background
        }

        @Override
        public void drawOverlay(Graphics g) {
            g.drawRect(1.5f, ListChooser.BORDER_COLOR, 0, 0, getWidth(), getHeight());
        }

        @Override
        protected FSkinColor getItemFillColor(int index) {
            if (selectedIndices.contains(index)) {
                return ListChooser.SEL_COLOR;
            }
            if (index % 2 == 1) {
                return ListChooser.ALT_ITEM_COLOR;
            }
            return ListChooser.ITEM_COLOR;
        }

        @Override
        protected boolean drawLineSeparators() {
            return false;
        }
    }
}
