package forge.game.ability.effects;

import java.util.Iterator;
import java.util.List;

import forge.Command;
import forge.game.Game;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;

public class RegenerateEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#getStackDescription(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
        final List<Card> tgtCards = getTargetCards(sa);

        if (tgtCards.size() > 0) {
            sb.append("Regenerate ");

            final Iterator<Card> it = tgtCards.iterator();
            while (it.hasNext()) {
                final Card tgtC = it.next();
                if (tgtC.isFaceDown()) {
                    sb.append("Morph");
                } else {
                    sb.append(tgtC);
                }

                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        sb.append(".");

        return sb.toString();
    }

    @Override
    public void resolve(SpellAbility sa) {
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        final Game game = sa.getActivatingPlayer().getGame();

        for (final Card tgtC : getTargetCards(sa)) {
            final Command untilEOT = new Command() {
                private static final long serialVersionUID = 1922050611313909200L;

                @Override
                public void run() {
                    tgtC.resetShield();
                }
            };

            if (tgtC.isInPlay() && ((tgt == null) || tgtC.canBeTargetedBy(sa))) {
                tgtC.addShield();
                game.getEndOfTurn().addUntil(untilEOT);
            }
        }
    } // regenerateResolve

}
