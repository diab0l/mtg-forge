package forge.gui.input;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import forge.game.GameEntity;
import forge.game.card.Card;
import forge.game.card.CounterType;
import forge.game.player.Player;
import forge.gui.GuiChoose;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public final class InputProliferate extends InputSelectManyBase<GameEntity> {
    private static final long serialVersionUID = -1779224307654698954L;
    private Map<GameEntity, CounterType> chosenCounters = new HashMap<GameEntity, CounterType>();

    public InputProliferate() {
        super(1, Integer.MAX_VALUE);
        allowUnselect = true;
    }

    
    protected String getMessage() {
        StringBuilder sb = new StringBuilder("Choose permanents and/or players with counters on them to add one more counter of that type.");
        sb.append("\n\nYou've selected so far:\n");
        if (chosenCounters.isEmpty()) {
            sb.append("(none)");
        }
        else {
            for (Entry<GameEntity, CounterType> ge : chosenCounters.entrySet()) {
                if (ge.getKey() instanceof Player) {
                    sb.append("* A poison counter to player ").append(ge.getKey()).append("\n");
                }
                else {
                    sb.append("* ").append(ge.getKey()).append(" -> ").append(ge.getValue()).append("counter\n");
                }
            }
        }

        return sb.toString();
    }

    @Override
    protected void onCardSelected(final Card card, final MouseEvent triggerEvent) {
        if (!card.hasCounters()) {
            return;
        }
        
        boolean entityWasSelected = chosenCounters.containsKey(card);
        if (entityWasSelected) {
            this.chosenCounters.remove(card);
        }
        else {
            final List<CounterType> choices = new ArrayList<CounterType>();
            for (final CounterType ct : CounterType.values()) {
                if (card.getCounters(ct) > 0) {
                    choices.add(ct);
                }
            }

            CounterType toAdd = choices.size() == 1 ? choices.get(0) : GuiChoose.one("Select counter type", choices);
            chosenCounters.put(card, toAdd);
        }

        refresh();
    }

    @Override
    protected final void onPlayerSelected(Player player, final MouseEvent triggerEvent) {
        if (player.getPoisonCounters() == 0 || player.hasKeyword("You can't get poison counters")) {
            return;
        }
        
        boolean entityWasSelected = chosenCounters.containsKey(player);
        if (entityWasSelected) {
            this.chosenCounters.remove(player);
        } else
            this.chosenCounters.put(player, null /* POISON counter is meant */);
        
        refresh();
    }

    public Map<GameEntity, CounterType> getProliferationMap() {
        return chosenCounters;
    }


    @Override
    protected boolean hasEnoughTargets() { return true; }

    @Override
    protected boolean hasAllTargets() { return false; }


    @Override
    public Collection<GameEntity> getSelected() {
        // TODO Auto-generated method stub
        return chosenCounters.keySet();
    }
}