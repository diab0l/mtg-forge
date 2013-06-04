package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import forge.Card;
import forge.CardUtil;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.game.player.Player;
import forge.util.Lang;

public class DamageDealEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#getStackDescription(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected String getStackDescription(SpellAbility sa) {
        // when damageStackDescription is called, just build exactly what is happening
        final StringBuilder sb = new StringBuilder();
        final String damage = sa.getParam("NumDmg");
        final int dmg = AbilityUtils.calculateAmount(sa.getSourceCard(), damage, sa);


        List<Object> tgts = getTargetObjects(sa);
        if (tgts.isEmpty()) 
            return "";

        final List<Card> definedSources = AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("DamageSource"), sa);
        Card source = new Card();
        if (!definedSources.isEmpty()) {
            source = definedSources.get(0);
        }

        if (source != sa.getSourceCard()) {
            sb.append(source.toString()).append(" deals");
        } else {
            sb.append("Deals");
        }

        sb.append(" ").append(dmg).append(" damage ");

        if (sa.hasParam("DivideEvenly")) {
            sb.append("divided evenly (rounded down) ");
        } else if (sa.hasParam("DividedAsYouChoose")) {
            sb.append("divided as you choose ");
        }
        sb.append("to ").append(Lang.joinHomogenous(tgts));

        if (sa.hasParam("Radiance")) {
            sb.append(" and each other ").append(sa.getParam("ValidTgts"))
                    .append(" that shares a color with ");
            if (tgts.size() > 1) {
                sb.append("them");
            } else {
                sb.append("it");
            }
        }

        sb.append(". ");
        return sb.toString();
    }

    @Override
    public void resolve(SpellAbility sa) {
        final String damage = sa.getParam("NumDmg");
        int dmg = AbilityUtils.calculateAmount(sa.getSourceCard(), damage, sa);

        final boolean noPrevention = sa.hasParam("NoPrevention");
        final boolean combatDmg = sa.hasParam("CombatDamage");
        final boolean removeDamage = sa.hasParam("Remove");

        ArrayList<Object> tgts;
        if (sa.getTarget() == null) {
            tgts = AbilityUtils.getDefinedObjects(sa.getSourceCard(), sa.getParam("Defined"), sa) ;
        } else {
            tgts = sa.getTarget().getTargets();
        }

        // Right now for Fireball, maybe later for other stuff
        if (sa.hasParam("DivideEvenly")) {
            String evenly = sa.getParam("DivideEvenly");
            if (evenly.equals("RoundedDown")) {
                dmg = tgts.isEmpty() ? 0 : dmg / tgts.size();
            }
        }

        final boolean targeted = (sa.getTarget() != null);

        if (sa.hasParam("Radiance") && targeted) {
            Card origin = null;
            for (int i = 0; i < tgts.size(); i++) {
                if (tgts.get(i) instanceof Card) {
                    origin = (Card) tgts.get(i);
                    break;
                }
            }
            // Can't radiate from a player
            if (origin != null) {
                for (final Card c : CardUtil.getRadiance(sa.getSourceCard(), origin,
                        sa.getParam("ValidTgts").split(","))) {
                    tgts.add(c);
                }
            }
        }

        final boolean remember = sa.hasParam("RememberDamaged");

        final List<Card> definedSources = AbilityUtils.getDefinedCards(sa.getSourceCard(), sa.getParam("DamageSource"), sa);
        if (definedSources == null) {
            return;
        }
        final Card source = definedSources.get(0);

        for (final Object o : tgts) {
            dmg = (sa.getTarget() != null && sa.hasParam("DividedAsYouChoose")) ? sa.getTarget().getDividedValue(o) : dmg;
            if (o instanceof Card) {
                final Card c = (Card) o;
                if (c.isInPlay() && (!targeted || c.canBeTargetedBy(sa))) {
                    if (removeDamage) {
                        c.setDamage(0);
                        c.clearAssignedDamage();
                    }
                    else if (noPrevention) {
                        if (c.addDamageWithoutPrevention(dmg, source) && remember) {
                            source.addRemembered(c);
                        }
                    } else if (combatDmg) {
                        HashMap<Card, Integer> combatmap = new HashMap<Card, Integer>();
                        combatmap.put(source, dmg);
                        c.addCombatDamage(combatmap);
                        if (remember) {
                            source.addRemembered(c);
                        }
                    } else {
                        if (c.addDamage(dmg, source) && remember) {
                            source.addRemembered(c);
                        }
                    }
                }

            } else if (o instanceof Player) {
                final Player p = (Player) o;
                if (!targeted || p.canBeTargetedBy(sa)) {
                    if (noPrevention) {
                        if (p.addDamageWithoutPrevention(dmg, source) && remember) {
                            source.addRemembered(p);
                        }
                    } else if (combatDmg) {
                        p.addCombatDamage(dmg, source);
                        if (remember) {
                            source.addRemembered(p);
                        }
                    } else {
                        if (p.addDamage(dmg, source) && remember) {
                            source.addRemembered(p);
                        }
                    }
                }
            }
        }
    }

}
