package forge.match.input;

import java.util.Collection;
import java.util.List;

import forge.game.GameEntity;
import forge.game.card.Card;
import forge.game.player.Player;
import forge.player.PlayerControllerHuman;
import forge.util.FCollection;
import forge.util.FCollectionView;
import forge.util.ITriggerEvent;

public class InputSelectEntitiesFromList<T extends GameEntity> extends InputSelectManyBase<T> {
    private static final long serialVersionUID = -6609493252672573139L;

    private final FCollectionView<T> validChoices;
    protected final FCollection<T> selected = new FCollection<T>();

    public InputSelectEntitiesFromList(final PlayerControllerHuman controller, final int min, final int max, final FCollectionView<T> validChoices) {
        super(controller, Math.min(min, validChoices.size()), Math.min(max, validChoices.size()));
        this.validChoices = validChoices;

        if (min > validChoices.size()) {
            System.out.println(String.format("Trying to choose at least %d cards from a list with only %d cards!", min, validChoices.size()));
        }
    }

    @Override
    protected boolean onCardSelected(final Card c, final List<Card> otherCardsToSelect, final ITriggerEvent triggerEvent) {
        if (!selectEntity(c)) {
            return false;
        }
        refresh();
        return true;
    }

    @Override
    protected void onPlayerSelected(final Player p, final ITriggerEvent triggerEvent) {
        if (!selectEntity(p)) {
            return;
        }
        refresh();
    }

    public final Collection<T> getSelected() {
        return selected;
    }

    @SuppressWarnings("unchecked")
    protected boolean selectEntity(GameEntity c) {
        if (!validChoices.contains(c)) {
            return false;
        }

        boolean entityWasSelected = selected.contains(c);
        if (entityWasSelected) {
            this.selected.remove(c);
        }
        else {
            this.selected.add((T)c);
        }
        onSelectStateChanged(c, !entityWasSelected);

        return true;
    }

    // might re-define later
    protected boolean hasEnoughTargets() { return selected.size() >= min; }
    protected boolean hasAllTargets() { return selected.size() >= max; }

    protected String getMessage() {
        return max == Integer.MAX_VALUE
                ? String.format(message, selected.size())
                : String.format(message, max - selected.size());
    }
}