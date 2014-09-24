package forge.screens.match.views;

import java.util.ArrayList;
import java.util.List;

import forge.FThreads;
import forge.GuiBase;
import forge.screens.match.MatchController;
import forge.screens.match.views.VCardDisplayArea.CardAreaPanel;
import forge.toolbox.FContainer;
import forge.view.CardView;
import forge.view.CardView.CardStateView;
import forge.view.PlayerView;

public class VField extends FContainer {
    private final PlayerView player;
    private final FieldRow row1, row2;
    private boolean flipped;
    private float commandZoneWidth;

    public VField(PlayerView player0) {
        player = player0;
        row1 = add(new FieldRow());
        row2 = add(new FieldRow());
    }

    public boolean isFlipped() {
        return flipped;
    }
    public void setFlipped(boolean flipped0) {
        flipped = flipped0;
    }

    public Iterable<CardAreaPanel> getCardPanels() {
        List<CardAreaPanel> cardPanels = new ArrayList<CardAreaPanel>();
        for (CardAreaPanel cardPanel : row1.getCardPanels()) {
            cardPanels.add(cardPanel);
        }
        for (CardAreaPanel cardPanel : row2.getCardPanels()) {
            cardPanels.add(cardPanel);
        }
        return cardPanels;
    }

    public void update() {
        FThreads.invokeInEdtNowOrLater(GuiBase.getInterface(), updateRoutine);
    }

    private final Runnable updateRoutine = new Runnable() {
        @Override
        public void run() {
            clear();

            List<CardView> model = player.getBfCards();
            for (CardView card : model) {
                updateCard(card);
            }

            List<CardView> creatures = new ArrayList<CardView>();
            List<CardView> lands = new ArrayList<CardView>();
            List<CardView> otherPermanents = new ArrayList<CardView>();

            for (CardView card : model) {
                CardAreaPanel cardPanel = CardAreaPanel.get(card);
                CardStateView details = MatchController.getCardDetails(card); //use details so creature/land check is accurate
                if (cardPanel.getAttachedToPanel() == null) { //skip attached panels
                    if (details.isCreature()) {
                        if (!tryStackCard(card, creatures)) {
                            creatures.add(card);
                        }
                    }
                    else if (details.isLand()) {
                        if (!tryStackCard(card, lands)) {
                            lands.add(card);
                        }
                    }
                    else {
                        if (!tryStackCard(card, otherPermanents)) {
                            otherPermanents.add(card);
                        }
                    }
                }
            }

            if (creatures.isEmpty()) {
                row1.refreshCardPanels(otherPermanents);
                row2.refreshCardPanels(lands);
            }
            else {
                row1.refreshCardPanels(creatures);
                lands.addAll(otherPermanents);
                row2.refreshCardPanels(lands);
            }
        }
    };

    private boolean tryStackCard(CardView card, List<CardView> cardsOfType) {
        if (card.isEnchanted() || card.isEquipped()) {
            return false; //can stack with enchanted or equipped card
        }
        if (card.getOriginal().isCreature() && !card.isToken()) {
            return false; //don't stack non-token creatures
        }
        final String cardName = card.getOriginal().getName();
        for (CardView c : cardsOfType) {
            if (!c.isEnchanted() && !c.isEquipped() &&
                    cardName.equals(c.getOriginal().getName()) &&
                    card.getCounters().equals(c.getCounters()) &&
                    card.isToken() == c.isToken()) { //don't stack tokens on top of non-tokens
                CardAreaPanel cPanel = CardAreaPanel.get(c);
                while (cPanel.getNextPanelInStack() != null) {
                    cPanel = cPanel.getNextPanelInStack();
                }
                CardAreaPanel cardPanel = CardAreaPanel.get(card);
                cPanel.setNextPanelInStack(cardPanel);
                cardPanel.setPrevPanelInStack(cPanel);
                return true;
            }
        }
        return false;
    }

    public void updateCard(final CardView card) {
        final CardAreaPanel toPanel = CardAreaPanel.get(card);
        if (toPanel == null) { return; }

        if (card.isTapped()) {
            toPanel.setTapped(true);
            toPanel.setTappedAngle(CardAreaPanel.TAPPED_ANGLE);
        }
        else {
            toPanel.setTapped(false);
            toPanel.setTappedAngle(0);
        }

        toPanel.getAttachedPanels().clear();
        if (card.isEnchanted()) {
            final Iterable<CardView> enchants = card.getEnchantedBy();
            for (final CardView e : enchants) {
                final CardAreaPanel cardE = CardAreaPanel.get(e);
                if (cardE != null) {
                    toPanel.getAttachedPanels().add(cardE);
                }
            }
        }
   
        if (card.isEquipped()) {
            final Iterable<CardView> enchants = card.getEquippedBy();
            for (final CardView e : enchants) {
                final CardAreaPanel cardE = CardAreaPanel.get(e);
                if (cardE != null) {
                    toPanel.getAttachedPanels().add(cardE);
                }
            }
        }

        if (card.isFortified()) {
            final Iterable<CardView> fortifications = card.getFortifiedBy();
            for (final CardView e : fortifications) {
                final CardAreaPanel cardE = CardAreaPanel.get(e);
                if (cardE != null) {
                    toPanel.getAttachedPanels().add(cardE);
                }
            }
        }

        if (card.getEnchantingCard() != null) {
            toPanel.setAttachedToPanel(CardAreaPanel.get(card.getEnchantingCard()));
        }
        else if (card.getEquipping() != null) {
            toPanel.setAttachedToPanel(CardAreaPanel.get(card.getEquipping()));
        }
        else if (card.getFortifying() != null) {
            toPanel.setAttachedToPanel(CardAreaPanel.get(card.getFortifying()));
        }
        else {
            toPanel.setAttachedToPanel(null);
        }

        toPanel.setCard(toPanel.getCard());
    }

    public FieldRow getRow1() {
        return row1;
    }

    public FieldRow getRow2() {
        return row2;
    }

    void setCommandZoneWidth(float commandZoneWidth0) {
        commandZoneWidth = commandZoneWidth0;
    }

    @Override
    public void clear() {
        row1.clear(); //clear rows instead of removing the rows
        row2.clear();
    }

    @Override
    protected void doLayout(float width, float height) {
        float cardSize = height / 2;
        float y1, y2;
        if (flipped) {
            y1 = cardSize;
            y2 = 0;
        }
        else {
            y1 = 0;
            y2 = cardSize;
        }
        row1.setBounds(0, y1, width, cardSize);
        row2.setBounds(0, y2, width - commandZoneWidth, cardSize);
    }

    public class FieldRow extends VCardDisplayArea {
        private FieldRow() {
            setVisible(true); //make visible by default unlike other display areas
        }

        @Override
        protected float getCardWidth(float cardHeight) {
            return cardHeight; //allow cards room to tap
        }

        @Override
        public void update() { //no logic needed
        }
    }
}
