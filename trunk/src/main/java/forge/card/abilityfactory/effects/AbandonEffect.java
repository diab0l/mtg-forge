package forge.card.abilityfactory.effects;


import forge.Card;
import forge.Singletons;
import forge.card.abilityfactory.SpellEffect;
import forge.card.spellability.SpellAbility;
import forge.card.trigger.TriggerType;
import forge.game.GameState;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class AbandonEffect extends SpellEffect {

    private GameState game = Singletons.getModel().getGame();

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(SpellAbility sa) {
        Card source = sa.getSourceCard();
        Player controller = source.getController();

        game.getTriggerHandler().suppressMode(TriggerType.ChangesZone);
        controller.getZone(ZoneType.Command).remove(source);
        game.getTriggerHandler().clearSuppression(TriggerType.ChangesZone);

        controller.getSchemeDeck().add(source);
    }

}
