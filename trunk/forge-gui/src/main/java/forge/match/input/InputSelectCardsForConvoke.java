package forge.match.input;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;

import forge.card.mana.ManaCost;
import forge.card.mana.ManaCostShard;
import forge.game.card.Card;
import forge.game.card.CardCollectionView;
import forge.game.card.CardUtil;
import forge.game.mana.ManaCostBeingPaid;
import forge.game.player.Player;
import forge.player.PlayerControllerHuman;
import forge.util.ITriggerEvent;


public final class InputSelectCardsForConvoke extends InputSelectManyBase<Card> {
    private static final long serialVersionUID = -1779224307654698954L;
    private final Map<Card, ImmutablePair<Byte, ManaCostShard>> chosenCards = new HashMap<Card, ImmutablePair<Byte, ManaCostShard>>();
    private final ManaCostBeingPaid remainingCost;
    private final Player player;
    private final CardCollectionView availableCreatures;
    
    public InputSelectCardsForConvoke(final PlayerControllerHuman controller, final Player p, final ManaCost cost, final CardCollectionView untapped) { 
        super(controller, 0, Math.min(cost.getCMC(), untapped.size()));
        remainingCost = new ManaCostBeingPaid(cost);
        player = p;
        availableCreatures = untapped;
    }

    protected String getMessage() {
        return "Choose creatures to tap for convoke.\nRemaining mana cost is " + remainingCost.toString();
    }

    @Override
    protected boolean onCardSelected(final Card card, final List<Card> otherCardsToSelect, final ITriggerEvent triggerEvent) {
        if (!availableCreatures.contains(card)) {
            // Not in untapped creatures list provided. Not a legal Convoke selection.
            return false;
        }

        boolean entityWasSelected = chosenCards.containsKey(card);
        if (entityWasSelected) {
            ImmutablePair<Byte, ManaCostShard> color = this.chosenCards.remove(card);
            remainingCost.increaseShard(color.right, 1);
            onSelectStateChanged(card, false);
        }
        else {
            byte chosenColor = player.getController().chooseColorAllowColorless("Convoke " + card.toString() + "  for which color?", card, CardUtil.getColors(card));
            
            if (remainingCost.getColorlessManaAmount() > 0 && (chosenColor == 0 || !remainingCost.needsColor(chosenColor, player.getManaPool()))) {
                registerConvoked(card, ManaCostShard.COLORLESS, chosenColor);
            }
            else {
                for (ManaCostShard shard : remainingCost.getDistinctShards()) {
                    if (shard.canBePaidWithManaOfColor(chosenColor)) {
                        registerConvoked(card, shard, chosenColor);
                        refresh();
                        return true;
                    }
                }
                showMessage("The colors provided by " + card.toString() + " you've chosen cannot be used to decrease the manacost of " + remainingCost.toString());
                return false;
            }
        }

        refresh();
        return true;
    }

    private void registerConvoked(Card card, ManaCostShard shard, byte chosenColor) {
        remainingCost.decreaseShard(shard, 1);
        chosenCards.put(card, ImmutablePair.of(chosenColor, shard));
        onSelectStateChanged(card, true);
    }


    @Override
    protected final void onPlayerSelected(Player player, final ITriggerEvent triggerEvent) {
    }

    public Map<Card, ManaCostShard> getConvokeMap() {
        Map<Card, ManaCostShard> result = new HashMap<Card, ManaCostShard>();
        if( !hasCancelled() )
            for(Entry<Card, ImmutablePair<Byte, ManaCostShard>> c : chosenCards.entrySet())
                result.put(c.getKey(), c.getValue().right);
        return result;
    }


    @Override
    protected boolean hasEnoughTargets() { return true; }

    @Override
    protected boolean hasAllTargets() { return false; }


    @Override
    public Collection<Card> getSelected() {
        // TODO Auto-generated method stub
        return chosenCards.keySet();
    }
}