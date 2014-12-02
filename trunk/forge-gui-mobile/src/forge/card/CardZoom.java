package forge.card;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.math.Rectangle;

import forge.Graphics;
import forge.ImageKeys;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinColor.Colors;
import forge.assets.FSkinImage;
import forge.game.card.CardView;
import forge.item.IPaperCard;
import forge.item.InventoryItem;
import forge.screens.FScreen;
import forge.toolbox.FCardPanel;
import forge.toolbox.FOverlay;
import forge.util.FCollectionView;

public class CardZoom extends FOverlay {
    private static final FSkinFont MSG_FONT = FSkinFont.get(12);
    private static final FSkinColor MSG_FORE_COLOR = FSkinColor.get(Colors.CLR_TEXT).alphaColor(0.9f);
    private static final FSkinColor MSG_BACK_COLOR = FScreen.Header.BACK_COLOR.alphaColor(0.75f);

    private static final CardZoom cardZoom = new CardZoom();
    private static List<?> items;
    private static int currentIndex;
    private static CardView currentCard, prevCard, nextCard;
    private static boolean zoomMode = true;
    private static ActivateHandler activateHandler;
    private static String currentActivateAction;
    private static Rectangle flipIconBounds;
    private static boolean showAltState;

    public static void show(Object item) {
        List<Object> items0 = new ArrayList<Object>();
        items0.add(item);
        show(items0, 0, null);
    }
    public static void show(FCollectionView<?> items0, int currentIndex0, ActivateHandler activateHandler0) {
        show((List<?>)items0, currentIndex0, activateHandler0);
    }
    public static void show(final List<?> items0, int currentIndex0, ActivateHandler activateHandler0) {
        items = items0;
        activateHandler = activateHandler0;
        currentIndex = currentIndex0;
        currentCard = getCardView(items.get(currentIndex));
        prevCard = currentIndex > 0 ? getCardView(items.get(currentIndex - 1)) : null;
        nextCard = currentIndex < items.size() - 1 ? getCardView(items.get(currentIndex + 1)) : null;
        onCardChanged();
        cardZoom.show();
    }

    public static boolean isOpen() {
        return cardZoom.isVisible();
    }

    public static void hideZoom() {
        cardZoom.hide();
    }

    private CardZoom() {
    }

    private static void incrementCard(int dir) {
        if (dir > 0) {
            if (currentIndex == items.size() - 1) { return; }
            currentIndex++;

            prevCard = currentCard;
            currentCard = nextCard;
            nextCard = currentIndex < items.size() - 1 ? getCardView(items.get(currentIndex + 1)) : null;
        }
        else {
            if (currentIndex == 0) { return; }
            currentIndex--;

            nextCard = currentCard;
            currentCard = prevCard;
            prevCard = currentIndex > 0 ? getCardView(items.get(currentIndex - 1)) : null;
        }
        onCardChanged();
    }

    private static void onCardChanged() {
        if (activateHandler != null) {
            currentActivateAction = activateHandler.getActivateAction(currentIndex);
        }
        flipIconBounds = null;
        showAltState = false;
    }

    private static CardView getCardView(Object item) {
        if (item instanceof Entry) {
            item = ((Entry<?, ?>)item).getKey();
        }
        if (item instanceof CardView) {
            return (CardView)item;
        }
        if (item instanceof IPaperCard) {
            return CardView.getCardForUi((IPaperCard)item);
        }
        if (item instanceof InventoryItem) {
            InventoryItem ii = (InventoryItem)item;
            return new CardView(-1, ii.getName(), null, ImageKeys.getImageKey(ii, false));
        }
        return new CardView(-1, item.toString());
    }

    @Override
    public boolean tap(float x, float y, int count) {
        if (flipIconBounds != null && flipIconBounds.contains(x, y)) {
            showAltState = !showAltState;
            return true;
        }
        hide();
        return true;
    }

    @Override
    public boolean fling(float velocityX, float velocityY) {
        //toggle between Zoom and Details with a quick horizontal fling action
        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            incrementCard(velocityX > 0 ? -1 : 1);
            return true;
        }
        if (velocityY > 0) {
            zoomMode = !zoomMode;
            return true;
        }
        if (currentActivateAction != null) {
            hide();
            activateHandler.activate(currentIndex);
            return true;
        }
        return false;
    }

    @Override
    public void drawOverlay(Graphics g) {
        float w = getWidth();
        float h = getHeight();

        float cardWidth = w * 0.5f;
        float cardHeight = FCardPanel.ASPECT_RATIO * cardWidth;
        float y = (h - cardHeight) / 2;
        if (prevCard != null) {
            CardImageRenderer.drawZoom(g, prevCard, false, 0, y, cardWidth, cardHeight);
        }
        if (nextCard != null) {
            CardImageRenderer.drawZoom(g, nextCard, false, w - cardWidth, y, cardWidth, cardHeight);
        }

        cardWidth = w * 0.7f;
        cardHeight = FCardPanel.ASPECT_RATIO * cardWidth;
        float x = (w - cardWidth) / 2;
        y = (h - cardHeight) / 2;
        if (zoomMode) {
            CardImageRenderer.drawZoom(g, currentCard, showAltState, x, y, cardWidth, cardHeight);
        }
        else {
            CardImageRenderer.drawDetails(g, currentCard, showAltState, x, y, cardWidth, cardHeight);
        }

        if (currentCard.hasAlternateState()) {
            float imageWidth = cardWidth / 2;
            float imageHeight = imageWidth * FSkinImage.FLIPCARD.getHeight() / FSkinImage.FLIPCARD.getWidth();
            flipIconBounds = new Rectangle(x + (cardWidth - imageWidth) / 2, y + (cardHeight - imageHeight) / 2, imageWidth, imageHeight);
            g.drawImage(FSkinImage.FLIPCARD, flipIconBounds.x, flipIconBounds.y, flipIconBounds.width, flipIconBounds.height);
        }
        else {
            flipIconBounds = null;
        }

        float messageHeight = MSG_FONT.getCapHeight() * 2.5f;
        if (currentActivateAction != null) {
            g.fillRect(MSG_BACK_COLOR, 0, 0, w, messageHeight);
            g.drawText("Swipe up to " + currentActivateAction, MSG_FONT, MSG_FORE_COLOR, 0, 0, w, messageHeight, false, HAlignment.CENTER, true);
        }
        g.fillRect(MSG_BACK_COLOR, 0, h - messageHeight, w, messageHeight);
        g.drawText("Swipe down to switch to " + (zoomMode ? "detail" : "picture") + " view", MSG_FONT, MSG_FORE_COLOR, 0, h - messageHeight, w, messageHeight, false, HAlignment.CENTER, true);
    }

    @Override
    protected void doLayout(float width, float height) {
    }

    public static interface ActivateHandler {
        String getActivateAction(int index);
        void activate(int index);
    }
}
