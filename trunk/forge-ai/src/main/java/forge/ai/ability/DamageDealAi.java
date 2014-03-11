package forge.ai.ability;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import forge.ai.*;
import forge.game.Game;
import forge.game.GameObject;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.cost.Cost;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.AbilitySub;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetChoices;
import forge.game.spellability.TargetRestrictions;
import forge.game.zone.ZoneType;
import forge.util.Aggregates;
import forge.util.MyRandom;

import java.util.List;
import java.util.Random;

public class DamageDealAi extends DamageAiBase {
    @Override
    public boolean chkAIDrawback(SpellAbility sa, Player ai) {
        final String damage = sa.getParam("NumDmg");
        int dmg = AbilityUtils.calculateAmount(sa.getHostCard(), damage, sa);

        final Card source = sa.getHostCard();

        if (damage.equals("X") && sa.getSVar(damage).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            dmg = ComputerUtilMana.determineLeftoverMana(sa, ai);
            source.setSVar("PayX", Integer.toString(dmg));
        }
        if (!this.damageTargetAI(ai, sa, dmg)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {

        final Cost abCost = sa.getPayCosts();
        final Card source = sa.getHostCard();

        final String damage = sa.getParam("NumDmg");
        int dmg = AbilityUtils.calculateAmount(sa.getHostCard(), damage, sa);

        if (damage.equals("X") && sa.getSVar(damage).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            dmg = ComputerUtilMana.determineLeftoverMana(sa, ai);
            source.setSVar("PayX", Integer.toString(dmg));
        }
        String logic = sa.getParam("AILogic");
        
        if ("DiscardLands".equals(logic)) {
            dmg = 2;
        } else if ("WildHunt".equals(logic)) {
            // This dummy ability will just deal 0 damage, but holds the logic for the AI for Master of Wild Hunt
            List<Card> wolves = CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), "Creature.Wolf+untapped+YouCtrl+Other", ai, source);
            dmg = Aggregates.sum(wolves, CardPredicates.Accessors.fnGetNetAttack);
        }

        if (dmg <= 0) {
            return false;
        }

        // temporarily disabled until better AI
        if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 4, null)) {
            return false;
        }

        if (!ComputerUtilCost.checkSacrificeCost(ai, abCost, source)) {
            return false;
        }

        if (!ComputerUtilCost.checkRemoveCounterCost(abCost, source)) {
            return false;
        }
        
        if ("DiscardLands".equals(sa.getParam("AILogic")) && !ComputerUtilCost.checkDiscardCost(ai, abCost, source)) {
            return false;
        }

        if (sa.isAbility()) {
            final Random r = MyRandom.getRandom(); // prevent run-away
                                                   // activations
            if (r.nextFloat() > Math.pow(.9, sa.getActivationsThisTurn())) {
                return false;
            }
        }

        if (!this.damageTargetAI(ai, sa, dmg)) {
            return false;
        }

        if (damage.equals("X") && source.getSVar(damage).equals("Count$xPaid")) {
            // If I can kill my target by paying less mana, do it
            if (sa.usesTargeting() && !sa.getTargets().isTargetingAnyPlayer() && !sa.hasParam("DividedAsYouChoose")) {
                int actualPay = 0;
                final boolean noPrevention = sa.hasParam("NoPrevention");
                for (final Card c : sa.getTargets().getTargetCards()) {
                    final int adjDamage = ComputerUtilCombat.getEnoughDamageToKill(c, dmg, source, false, noPrevention);
                    if ((adjDamage > actualPay) && (adjDamage <= dmg)) {
                        actualPay = adjDamage;
                    }
                }
                source.setSVar("PayX", Integer.toString(actualPay));
            }
        }
        return true;
    }

    /**
     * <p>
     * dealDamageChooseTgtC.
     * </p>
     * 
     * @param d
     *            a int.
     * @param noPrevention
     *            a boolean.
     * @param pl
     *            a {@link forge.game.player.Player} object.
     * @param mandatory
     *            a boolean.
     * @return a {@link forge.game.card.Card} object.
     */
    private Card dealDamageChooseTgtC(final Player ai, final SpellAbility sa, final int d, final boolean noPrevention,
            final Player pl, final boolean mandatory) {

        // wait until stack is empty (prevents duplicate kills)
        if (!sa.isTrigger() && !ai.getGame().getStack().isEmpty()) {
            return null;
        }
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        final Card source = sa.getHostCard();
        List<Card> hPlay = CardLists.getValidCards(pl.getCardsIn(ZoneType.Battlefield), tgt.getValidTgts(), ai, source);

        final List<GameObject> objects = Lists.newArrayList(sa.getTargets().getTargets());
        if (sa.hasParam("TargetUnique")) {
            objects.addAll(sa.getUniqueTargets());
        }
        for (final Object o : objects) {
            if (o instanceof Card) {
                final Card c = (Card) o;
                if (hPlay.contains(c)) {
                    hPlay.remove(c);
                }
            }
        }
        hPlay = CardLists.getTargetableCards(hPlay, sa);

        final List<Card> killables = CardLists.filter(hPlay, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return (ComputerUtilCombat.getEnoughDamageToKill(c, d, source, false, noPrevention) <= d) && !ComputerUtil.canRegenerate(ai, c)
                        && !(c.getSVar("SacMe").length() > 0);
            }
        });

        Card targetCard;
        if (pl.isOpponentOf(ai) && !killables.isEmpty()) {
            targetCard = ComputerUtilCard.getBestCreatureAI(killables);

            return targetCard;
        }

        if (!mandatory) {
            return null;
        }

        if (!hPlay.isEmpty()) {
            if (pl.isOpponentOf(ai)) {
                targetCard = ComputerUtilCard.getBestCreatureAI(hPlay);
            } else {
                targetCard = ComputerUtilCard.getWorstCreatureAI(hPlay);
            }

            return targetCard;
        }

        return null;
    }

    /**
     * <p>
     * damageTargetAI.
     * </p>
     * 
     * @param saMe
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param dmg
     *            a int.
     * @return a boolean.
     */
    private boolean damageTargetAI(final Player ai, final SpellAbility saMe, final int dmg) {
        final TargetRestrictions tgt = saMe.getTargetRestrictions();

        if (tgt == null) {
            return this.damageChooseNontargeted(ai, saMe, dmg);
        }

        if (tgt.isRandomTarget()) {
            return false;
        }

        return this.damageChoosingTargets(ai, saMe, tgt, dmg, false, false);
    }

    /**
     * <p>
     * damageChoosingTargets.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param tgt
     *            a {@link forge.game.spellability.TargetRestrictions} object.
     * @param dmg
     *            a int.
     * @param mandatory
     *            a boolean.
     * @return a boolean.
     */
    private boolean damageChoosingTargets(final Player ai, final SpellAbility sa, final TargetRestrictions tgt, int dmg,
            final boolean isTrigger, final boolean mandatory) {
        final Card source = sa.getHostCard();
        final boolean noPrevention = sa.hasParam("NoPrevention");
        final Game game = source.getGame();
        final PhaseHandler phase = game.getPhaseHandler();
        final boolean divided = sa.hasParam("DividedAsYouChoose");
        final boolean oppTargetsChoice = sa.hasParam("TargetingPlayer");

        // target loop
        sa.resetTargets();
        TargetChoices tcs = sa.getTargets();
        Player enemy = ai.getOpponent();
        
        if (tgt.getMaxTargets(source, sa) <= 0) {
            return false;
        }

        while (tcs.getNumTargeted() < tgt.getMaxTargets(source, sa)) {
            if (oppTargetsChoice && sa.getActivatingPlayer().equals(ai)) {
                // canPlayAI (sa activated by ai)
                Player targetingPlayer = AbilityUtils.getDefinedPlayers(source, sa.getParam("TargetingPlayer"), sa).get(0);
                sa.setTargetingPlayer(targetingPlayer);
                return targetingPlayer.getController().chooseTargetsFor(sa);
            }

            if (tgt.canTgtCreatureAndPlayer()) {

                if (this.shouldTgtP(ai, sa, dmg, noPrevention)) {
                    tcs.add(enemy);
                    if (divided) {
                        tgt.addDividedAllocation(enemy, dmg);
                        break;
                    }
                    continue;
                }

                final Card c = this.dealDamageChooseTgtC(ai, sa, dmg, noPrevention, enemy, false);
                if (c != null) {
                    tcs.add(c);
                    if (divided) {
                        final int assignedDamage = ComputerUtilCombat.getEnoughDamageToKill(c, dmg, source, false, noPrevention);
                        if (assignedDamage <= dmg) {
                            tgt.addDividedAllocation(c, assignedDamage);
                        }
                        dmg = dmg - assignedDamage;
                        if (dmg <= 0) {
                            break;
                        }
                    }
                    continue;
                }

                // When giving priority to targeting Creatures for mandatory
                // triggers
                // feel free to add the Human after we run out of good targets

                // TODO: add check here if card is about to die from something
                // on the stack
                // or from taking combat damage
                boolean freePing = isTrigger || sa.getPayCosts() == null || sa.getTargets().getNumTargeted() > 0;

                if (phase.is(PhaseType.END_OF_TURN) && sa.isAbility()) {
                    if (phase.getNextTurn().equals(ai))
                        freePing = true;
                }

                if (phase.is(PhaseType.MAIN2) && sa.isAbility()) {
                    if (sa.getRestrictions().getPlaneswalker() || source.hasKeyword("At the beginning of the end step, exile CARDNAME.")
                            || source.hasKeyword("At the beginning of the end step, sacrifice CARDNAME."))
                        freePing = true;
                }

                if (freePing && sa.canTarget(enemy)) {
                    tcs.add(enemy);
                    if (divided) {
                        tgt.addDividedAllocation(enemy, dmg);
                        break;
                    }
                }
            } else if (tgt.canTgtCreature()) {
                final Card c = this.dealDamageChooseTgtC(ai, sa, dmg, noPrevention, enemy, mandatory);
                if (c != null) {
                    tcs.add(c);
                    if (divided) {
                        final int assignedDamage = ComputerUtilCombat.getEnoughDamageToKill(c, dmg, source, false, noPrevention);
                        if (assignedDamage <= dmg) {
                            tgt.addDividedAllocation(c, assignedDamage);
                        } else {
                            tgt.addDividedAllocation(c, dmg);
                        }
                        dmg = dmg - assignedDamage;
                        if (dmg <= 0) {
                            break;
                        }
                    }
                    continue;
                }
            }

            // TODO: Improve Damage, we shouldn't just target the player just
            // because we can
            else if (sa.canTarget(enemy)) {
                if ((phase.is(PhaseType.END_OF_TURN) && phase.getNextTurn().equals(ai))
                        || (SpellAbilityAi.isSorcerySpeed(sa) && phase.is(PhaseType.MAIN2))
                        || sa.getPayCosts() == null || isTrigger
                        || this.shouldTgtP(ai, sa, dmg, noPrevention)) {
                    sa.getTargets().add(enemy);
                    if (divided) {
                        tgt.addDividedAllocation(enemy, dmg);
                        break;
                    }
                    continue;
                }
            }
            // fell through all the choices, no targets left?
            if (sa.getTargets().getNumTargeted() < tgt.getMinTargets(source, sa) || sa.getTargets().getNumTargeted() == 0) {
                if (!mandatory) {
                    sa.resetTargets();
                    return false;
                } else {
                    // If the trigger is mandatory, gotta choose my own stuff now
                    return this.damageChooseRequiredTargets(ai, sa, tgt, dmg, mandatory);
                }
            } else {
                // TODO is this good enough? for up to amounts?
                break;
            }
        }
        return true;
    }

    /**
     * <p>
     * damageChooseNontargeted.
     * </p>
     * @param ai 
     * 
     * @param saMe
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param dmg
     *            a int.
     * @return a boolean.
     */
    private boolean damageChooseNontargeted(Player ai, final SpellAbility saMe, final int dmg) {
        // TODO: Improve circumstances where the Defined Damage is unwanted
        final List<GameObject> objects = AbilityUtils.getDefinedObjects(saMe.getHostCard(), saMe.getParam("Defined"), saMe);
        boolean urgent = false; // can it wait?
        boolean positive = false;

        for (final Object o : objects) {
            if (o instanceof Card) {
                Card c = (Card) o;
                final int restDamage = ComputerUtilCombat.predictDamageTo(c, dmg, saMe.getHostCard(), false);
                if (!c.hasKeyword("Indestructible") && ComputerUtilCombat.getDamageToKill(c) <= restDamage) {
                    if (c.getController().equals(ai)) {
                        return false;
                    } else {
                        urgent = true;
                    }
                }
                if (c.getController().isOpponentOf(ai) ^ c.getName().equals("Stuffy Doll")) {
                    positive = true;
                }
            } else if (o instanceof Player) {
                final Player p = (Player) o;
                final int restDamage = ComputerUtilCombat.predictDamageTo(p, dmg, saMe.getHostCard(), false);
                if (!p.isOpponentOf(ai) && p.canLoseLife() && restDamage + 3 >= p.getLife() && restDamage > 0) {
                    // from this spell will kill me
                    return false;
                }
                if (p.isOpponentOf(ai) && p.canLoseLife()) {
                    positive = true;
                    if (p.getLife() + 3 <= restDamage) {
                        urgent = true;
                    }
                }
            }
        }
        if (!positive && !(saMe instanceof AbilitySub)) {
            return false;
        }
        if (!urgent && !SpellAbilityAi.playReusable(ai, saMe)) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * damageChooseRequiredTargets.
     * </p>
     * 
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param tgt
     *            a {@link forge.game.spellability.TargetRestrictions} object.
     * @param dmg
     *            a int.
     * @param mandatory
     *            a boolean.
     * @return a boolean.
     */
    private boolean damageChooseRequiredTargets(final Player ai, final SpellAbility sa, final TargetRestrictions tgt, final int dmg,
            final boolean mandatory) {
        // this is for Triggered targets that are mandatory
        final boolean noPrevention = sa.hasParam("NoPrevention");
        final boolean divided = sa.hasParam("DividedAsYouChoose");

        while (sa.getTargets().getNumTargeted() < tgt.getMinTargets(sa.getHostCard(), sa)) {
            // TODO: Consider targeting the planeswalker
            if (tgt.canTgtCreature()) {
                final Card c = this.dealDamageChooseTgtC(ai, sa, dmg, noPrevention, ai, mandatory);
                if (c != null) {
                    sa.getTargets().add(c);
                    if (divided) {
                        tgt.addDividedAllocation(c, dmg);
                        break;
                    }
                    continue;
                }
            }

            if (sa.canTarget(ai)) {
                if (sa.getTargets().add(ai)) {
                    if (divided) {
                        tgt.addDividedAllocation(ai, dmg);
                        break;
                    }
                    continue;
                }
            }

            // if we get here then there isn't enough targets, this is the only
            // time we can return false
            return false;
        }
        return true;
    }

    @Override
    protected boolean doTriggerAINoCost(Player ai, SpellAbility sa, boolean mandatory) {

        final Card source = sa.getHostCard();
        final String damage = sa.getParam("NumDmg");
        int dmg = AbilityUtils.calculateAmount(source, damage, sa);

        // Remove all damage
        if (sa.hasParam("Remove")) {
            return true;
        }

        if (damage.equals("X") && sa.getSVar(damage).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            dmg = ComputerUtilMana.determineLeftoverMana(sa, ai);
            source.setSVar("PayX", Integer.toString(dmg));
        }

        final TargetRestrictions tgt = sa.getTargetRestrictions();
        if (tgt == null) {
            // If it's not mandatory check a few things
            if (!mandatory && !this.damageChooseNontargeted(ai, sa, dmg)) {
                return false;
            }
        } else {
            if (!this.damageChoosingTargets(ai, sa, tgt, dmg, true, mandatory) && !mandatory) {
                return false;
            }

            if (damage.equals("X") && source.getSVar(damage).equals("Count$xPaid") && !sa.hasParam("DividedAsYouChoose")) {
                // If I can kill my target by paying less mana, do it
                int actualPay = 0;
                final boolean noPrevention = sa.hasParam("NoPrevention");
                
                //target is a player
                if (!sa.getTargets().isTargetingAnyCard()) {
                    actualPay = dmg;
                }
                for (final Card c : sa.getTargets().getTargetCards()) {
                    final int adjDamage = ComputerUtilCombat.getEnoughDamageToKill(c, dmg, source, false, noPrevention);
                    if (adjDamage > actualPay) {
                        actualPay = adjDamage;
                    }
                }

                source.setSVar("PayX", Integer.toString(actualPay));
            }
        }

        return true;
    }
}
