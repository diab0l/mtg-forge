package forge.toolbox;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinColor.Colors;
import forge.game.card.Card;
import forge.screens.match.views.VPrompt;
import forge.toolbox.FEvent;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FList.ListItemRenderer;

public class FCardZoom extends FOverlay {
    private static final FSkinColor BACK_COLOR = FSkinColor.get(Colors.CLR_THEME).alphaColor(0.9f);
    private static final float LIST_OPTION_HEIGHT = VPrompt.HEIGHT;
    private static final float CARD_DETAILS_HEIGHT = VPrompt.HEIGHT * 2f;
    private static final FCardZoom cardZoom = new FCardZoom();

    public static abstract class ZoomController<T> {
        public abstract List<T> getOptions(final Card card);
        public abstract boolean selectOption(final Card card, final T option);
    }

    public static <T> void show(String message0, final Card card0, final List<Card> orderedCards0, ZoomController<?> controller0) {
        if (cardZoom.isVisible()) { return; } //don't support showing if already shown

        if (orderedCards0.isEmpty()) {
            return;
        }

        cardZoom.prompt.setMessage(message0);
        cardZoom.orderedCards = orderedCards0;
        cardZoom.controller = controller0;

        int index = orderedCards0.indexOf(card0);
        if (index == -1) {
            index = 0;
        }
        cardZoom.setSelectedIndex(index);

        if (showTask.isScheduled()) { //select first option without showing zoom if called a second time in quick succession
            showTask.cancel();
            cardZoom.selectFirstOption();
        }
        else { //delay showing briefly to give time for a double-tap to auto-select the first ability
            Timer.schedule(showTask, FGestureAdapter.DOUBLE_TAP_INTERVAL);
        }
    }

    private static final Task showTask = new Task() {
        @Override
        public void run () {
            cardZoom.show();
        }
    };

    public static boolean isOpen() {
        return cardZoom.isVisible();
    }

    public static String getMessage() {
        return cardZoom.getPrompt().getMessage();
    }

    public static void setMessage(String message0) {
        cardZoom.getPrompt().setMessage(message0);
    }

    public static void hideZoom() {
        cardZoom.hide();
    }

    private final CardSlider slider;
    private final FList<Object> optionList;
    private boolean optionListExpanded;
    private final VPrompt prompt;
    private List<Card> orderedCards;
    private int selectedIndex = -1;

    @SuppressWarnings("rawtypes")
    private ZoomController controller;

    private FCardZoom() {
        slider = add(new CardSlider());
        optionList = add(new FList<Object>() {
            @Override
            protected void drawBackground(Graphics g) {
                g.fillRect(BACK_COLOR, 0, 0, getWidth(), getHeight());
            }

            @Override
            protected void drawOverlay(Graphics g) { //draw top border
                g.drawLine(1, FList.LINE_COLOR, 0, 0, getWidth(), 0);
            }
        });
        optionList.setListItemRenderer(new ListItemRenderer<Object>() {
            @Override
            public boolean tap(Object value, float x, float y, int count) {
                selectOption(value);
                return true;
            }

            @Override
            public float getItemHeight() {
                return LIST_OPTION_HEIGHT;
            }

            @Override
            public void drawValue(Graphics g, Object value, FSkinFont font, FSkinColor foreColor, boolean pressed, float x, float y, float w, float h) {
                g.drawText(value.toString(), font, foreColor, x, y, w, h, true, HAlignment.CENTER, true);
            }
        });
        prompt = add(new VPrompt("Hide", "More",
                new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        hide();
                    }
                },
                new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        optionListExpanded = !optionListExpanded;
                        prompt.getBtnCancel().setText(optionListExpanded ? "Less" : "More");
                        revalidate();
                    }
                }));
        prompt.getBtnCancel().setEnabled(false);
    }

    @Override
    public void hide() {
        orderedCards = null; //reset fields when hidden
        controller = null;
        selectedIndex = -1;
        optionList.clear();
        optionListExpanded = false;
        prompt.setMessage(null);
        prompt.getBtnCancel().setText("More");
        prompt.getBtnCancel().setEnabled(false);
        super.hide();
    }

    private void selectFirstOption() {
        selectOption(optionList.getItemAt(0));
    }

    @SuppressWarnings("unchecked")
    private void selectOption(Object option) {
        if (option == null ||
                controller.selectOption(orderedCards.get(selectedIndex), option) ||
                !isVisible() ||
                selectedIndex == orderedCards.size() - 1) {
            hide();
        }
        else {
            //select next card instead of hiding if input wants to keep zoom open
            //after selecting option and there's another card to select
            setSelectedIndex(selectedIndex + 1);
        }
    }

    private void setSelectedIndex(int selectedIndex0) {
        if (selectedIndex0 < 0) {
            selectedIndex0 = 0;
        }
        else if (selectedIndex0 >= orderedCards.size()) {
            selectedIndex0 = orderedCards.size() - 1;
        }

        if (selectedIndex == selectedIndex0) { return; }
        selectedIndex = selectedIndex0;

        Card selectedCard = orderedCards.get(selectedIndex);
        slider.frontPanel.setCard(selectedCard);
        slider.leftPanel.setCard(selectedIndex > 0 ? orderedCards.get(selectedIndex - 1) : null);
        slider.rightPanel.setCard(selectedIndex < orderedCards.size() - 1 ? orderedCards.get(selectedIndex + 1) : null);
        slider.leftPanel.setVisible(slider.leftPanel.getCard() != null);
        slider.rightPanel.setVisible(slider.rightPanel.getCard() != null);

        optionList.clear();
        List<?> options = controller.getOptions(selectedCard);
        if (options == null || options.isEmpty()) {
            optionList.setVisible(false);
        }
        else {
            for (Object option : options) {
                optionList.addItem(option);
            }
            optionList.setVisible(true);
        }
        optionList.revalidate();
        optionListExpanded = false;
        prompt.getBtnCancel().setText("More");
        prompt.getBtnCancel().setEnabled(optionList.getCount() > 1);
    }

    public VPrompt getPrompt() {
        return prompt;
    }

    @Override
    public boolean tap(float x, float y, int count) {
        hide(); //hide if uncovered area tapped
        return true;
    }

    @Override
    protected void doLayout(float width, float height) {
        float y = height - VPrompt.HEIGHT;
        prompt.setBounds(0, y, width, VPrompt.HEIGHT);
        
        float optionListHeight = LIST_OPTION_HEIGHT * (optionListExpanded ? optionList.getCount() : 1);
        if (optionListHeight > y) {
            optionListHeight = y;
        }
        y -= optionListHeight;
        optionList.setBounds(0, y, width, optionListHeight);

        slider.setBounds(0, CARD_DETAILS_HEIGHT, width, height - VPrompt.HEIGHT - LIST_OPTION_HEIGHT - CARD_DETAILS_HEIGHT);
    }

    private class CardSlider extends FContainer {
        private final FCardPanel frontPanel, leftPanel, rightPanel;

        private CardSlider() {
            leftPanel = add(new FCardPanel() {
                @Override
                public boolean tap(float x, float y, int count) {
                    setSelectedIndex(selectedIndex - 1);
                    return true;
                }
            });
            rightPanel = add(new FCardPanel() {
                @Override
                public boolean tap(float x, float y, int count) {
                    setSelectedIndex(selectedIndex + 1);
                    return true;
                }
            });
            frontPanel = add(new FCardPanel() {
                @Override
                public boolean tap(float x, float y, int count) {
                    selectFirstOption();
                    return true;
                }
            });
        }

        @Override
        protected void doLayout(float width, float height) {
            float backPanelHeight = height * 0.8f;
            float backPanelWidth = backPanelHeight / FCardPanel.ASPECT_RATIO;
            float y = (height - backPanelHeight) / 2;
            leftPanel.setBounds(0, y, backPanelWidth, backPanelHeight);
            rightPanel.setBounds(width - backPanelWidth, y, backPanelWidth, backPanelHeight);

            float frontPanelWidth = height / FCardPanel.ASPECT_RATIO;
            frontPanel.setBounds((width - frontPanelWidth) / 2, 0, frontPanelWidth, height);
        }
    }
}
