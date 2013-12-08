package forge.gui.input;

import java.awt.event.MouseEvent;

import forge.game.card.Card;

public abstract class InputSelectCards extends InputSelectManyBase<Card> {
    private static final long serialVersionUID = -6609493252672573139L;

    protected InputSelectCards(int min, int max) {
        super(min, max);
    }

    @Override
    protected void onCardSelected(final Card c, final MouseEvent triggerEvent) {
        if (!selectEntity(c)) {
            return;
        }

        refresh();
    }
}
