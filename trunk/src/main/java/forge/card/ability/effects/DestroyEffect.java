package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import forge.Card;
import forge.CardUtil;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.TargetRestrictions;
import forge.game.Game;

public class DestroyEffect extends SpellAbilityEffect {
    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#getStackDescription(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected String getStackDescription(SpellAbility sa) {
        final boolean noRegen = sa.hasParam("NoRegen");
        final StringBuilder sb = new StringBuilder();

        final List<Card> tgtCards = getTargetCards(sa);

        if (sa.hasParam("Sacrifice")) {
            sb.append("Sacrifice ");
        } else {
            sb.append("Destroy ");
        }

        final Iterator<Card> it = tgtCards.iterator();
        while (it.hasNext()) {
            final Card tgtC = it.next();
            if (tgtC.isFaceDown()) {
                sb.append("Morph ").append("(").append(tgtC.getUniqueNumber()).append(")");
            } else {
                sb.append(tgtC);
            }

            if (it.hasNext()) {
                sb.append(", ");
            }
        }

        if (sa.hasParam("Radiance")) {
            sb.append(" and each other ").append(sa.getParam("ValidTgts"))
                    .append(" that shares a color with ");
            if (tgtCards.size() > 1) {
                sb.append("them");
            } else {
                sb.append("it");
            }
        }

        if (noRegen) {
            sb.append(". ");
            if (tgtCards.size() == 1) {
                sb.append("It");
            } else {
                sb.append("They");
            }
            sb.append(" can't be regenerated");
        }
        sb.append(".");

        return sb.toString();
    }

    @Override
    public void resolve(SpellAbility sa) {
        final Card card = sa.getSourceCard();
        final Game game = card.getGame();

        final boolean remDestroyed = sa.hasParam("RememberDestroyed");
        if (remDestroyed) {
            card.clearRemembered();
        }

        final boolean noRegen = sa.hasParam("NoRegen");
        final boolean sac = sa.hasParam("Sacrifice");

        final List<Card> tgtCards = getTargetCards(sa);
        final ArrayList<Card> untargetedCards = new ArrayList<Card>();

        final TargetRestrictions tgt = sa.getTargetRestrictions();

        if (sa.hasParam("Radiance")) {
            for (final Card c : CardUtil.getRadiance(card, tgtCards.get(0),
                    sa.getParam("ValidTgts").split(","))) {
                untargetedCards.add(c);
            }
        }

        for (final Card tgtC : tgtCards) {
            if (tgtC.isInPlay() && ((tgt == null) || tgtC.canBeTargetedBy(sa))) {
                boolean destroyed = false;
                if (sac) {
                    destroyed = game.getAction().sacrifice(tgtC, sa);
                } else if (noRegen) {
                    destroyed = game.getAction().destroyNoRegeneration(tgtC, sa);
                } else {
                    destroyed = game.getAction().destroy(tgtC, sa);
                } if (destroyed  && remDestroyed) {
                    card.addRemembered(tgtC);
                }
            }
        }

        for (final Card unTgtC : untargetedCards) {
            if (unTgtC.isInPlay()) {
                boolean destroyed = false;
                if (sac) {
                    destroyed = game.getAction().sacrifice(unTgtC, sa);
                } else if (noRegen) {
                    destroyed = game.getAction().destroyNoRegeneration(unTgtC, sa);
                } else {
                    destroyed = game.getAction().destroy(unTgtC, sa);
                } if (destroyed  && remDestroyed) {
                    card.addRemembered(unTgtC);
                }
            }
        }
    }

}
