package forge.ai.ability;

import forge.ai.*;
import forge.game.Game;
import forge.game.GameObject;
import forge.game.ability.AbilityFactory;
import forge.game.ability.AbilityUtils;
import forge.game.ability.ApiType;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates.Presets;
import forge.game.cost.Cost;
import forge.game.cost.CostPart;
import forge.game.cost.CostTapType;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.player.PlayerActionConfirmMode;
import forge.game.spellability.AbilitySub;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.SpellAbilityRestriction;
import forge.game.spellability.TargetRestrictions;
import forge.game.trigger.Trigger;
import forge.game.trigger.TriggerType;
import forge.game.zone.ZoneType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PumpAi extends PumpAiBase {

    private static boolean hasTapCost(final Cost cost, final Card source) {
        if (cost == null) {
            return true;
        }
        for (final CostPart part : cost.getCostParts()) {
            if (part instanceof CostTapType) {
                return true;
            }
        }
        return false;
    }

    
    /* (non-Javadoc)
         * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
         */
    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        SpellAbility futureSpell = ((PlayerControllerAi)ai.getController()).getAi().predictSpellToCastInMain2(ApiType.Pump);
        if (futureSpell != null && futureSpell.getHostCard() != null) {
            ((PlayerControllerAi)ai.getController()).getAi().reserveManaSourcesForMain2(futureSpell);
        }

        final Cost cost = sa.getPayCosts();
        final Game game = ai.getGame();
        final PhaseHandler ph = game.getPhaseHandler();
        final List<String> keywords = sa.hasParam("KW") ? Arrays.asList(sa.getParam("KW").split(" & ")) : new ArrayList<String>();
        final String numDefense = sa.hasParam("NumDef") ? sa.getParam("NumDef") : "";
        final String numAttack = sa.hasParam("NumAtt") ? sa.getParam("NumAtt") : "";

        if (!ComputerUtilCost.checkLifeCost(ai, cost, sa.getHostCard(), 4, null)) {
            return false;
        }

        if (!ComputerUtilCost.checkDiscardCost(ai, cost, sa.getHostCard())) {
            return false;
        }

        if (!ComputerUtilCost.checkCreatureSacrificeCost(ai, cost, sa.getHostCard())) {
            return false;
        }

        if (!ComputerUtilCost.checkRemoveCounterCost(cost, sa.getHostCard())) {
            return false;
        }

        if (!ComputerUtilCost.checkTapTypeCost(ai, cost, sa.getHostCard())) {
            return false;
        }

        if (game.getStack().isEmpty() && hasTapCost(cost, sa.getHostCard())) {
                if (ph.getPhase().isBefore(PhaseType.COMBAT_DECLARE_ATTACKERS) && ph.isPlayerTurn(ai)) {
                    return false;
                }
                if (ph.getPhase().isBefore(PhaseType.COMBAT_DECLARE_BLOCKERS) && ph.isPlayerTurn(ai.getOpponent())) {
                    return false;
                }
        }
        
        if (sa.hasParam("AILogic")) {
            if (sa.getParam("AILogic").equals("Never")) {
            	return false;
            }
        }
        
        if (ComputerUtil.preventRunAwayActivations(sa)) {
            return false;
        }

        // Phase Restrictions
        if (game.getStack().isEmpty() && ph.getPhase().isBefore(PhaseType.COMBAT_BEGIN)) {
            // Instant-speed pumps should not be cast outside of combat when the
            // stack is empty
            if (!sa.isCurse() && !SpellAbilityAi.isSorcerySpeed(sa)) {
                return false;
            }
        } else if (!game.getStack().isEmpty() && !sa.isCurse()) {
            return pumpAgainstRemoval(ai, sa);
        }

        if (sa.hasParam("ActivationNumberSacrifice")) {
            final SpellAbilityRestriction restrict = sa.getRestrictions();
            final int sacActivations = Integer.parseInt(sa.getParam("ActivationNumberSacrifice").substring(2));
            final int activations = restrict.getNumberTurnActivations();
            // don't risk sacrificing a creature just to pump it
            if (activations >= sacActivations - 1) {
                return false;
            }
        }

        final Card source = sa.getHostCard();
        if (source.getSVar("X").equals("Count$xPaid")) {
            source.setSVar("PayX", "");
        }

        int defense;
        if (numDefense.contains("X") && source.getSVar("X").equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            final int xPay = ComputerUtilMana.determineLeftoverMana(sa, ai);
            source.setSVar("PayX", Integer.toString(xPay));
            defense = xPay;
            if (numDefense.equals("-X")) {
                defense = -xPay;
            }
        } else {
            defense = AbilityUtils.calculateAmount(sa.getHostCard(), numDefense, sa);
        }

        int attack;
        if (numAttack.contains("X") && source.getSVar("X").equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            final String toPay = source.getSVar("PayX");

            if (toPay.equals("")) {
                final int xPay = ComputerUtilMana.determineLeftoverMana(sa, ai);
                source.setSVar("PayX", Integer.toString(xPay));
                attack = xPay;
            } else {
                attack = Integer.parseInt(toPay);
            }
        } else {
            attack = AbilityUtils.calculateAmount(sa.getHostCard(), numAttack, sa);
        }

        if ((numDefense.contains("X") && defense == 0)
                || (numAttack.contains("X") && attack == 0)) {
            return false;
        }

        //Untargeted
        if ((sa.getTargetRestrictions() == null) || !sa.getTargetRestrictions().doesTarget()) {
            final List<Card> cards = AbilityUtils.getDefinedCards(sa.getHostCard(),
                    sa.getParam("Defined"), sa);

            if (cards.size() == 0) {
                return false;
            }

            // when this happens we need to expand AI to consider if its ok for
            // everything?
            for (final Card card : cards) {
                if (sa.isCurse()) {
                    if (!card.getController().isOpponentOf(ai)) {
                        return false;
                    }

                    if (!containsUsefulKeyword(ai, keywords, card, sa, attack)) {
                        continue;
                    }

                    return true;
                }
                if (!card.getController().isOpponentOf(ai) && shouldPumpCard(ai, sa, card, defense, attack, keywords)) {
                    return true;
                }
            }
            return false;
        }
        //Targeted
        if (!this.pumpTgtAI(ai, sa, defense, attack, false)) {
            return false;
        }

        return true;
    } // pumpPlayAI()

    private boolean pumpTgtAI(final Player ai, final SpellAbility sa, final int defense, final int attack, final boolean mandatory) {
        final List<String> keywords = sa.hasParam("KW") ? Arrays.asList(sa.getParam("KW").split(" & ")) : new ArrayList<String>();
        final Game game = ai.getGame();
        final Card source = sa.getHostCard();

        if (!mandatory
                && !sa.isTrigger()
                && game.getPhaseHandler().getPhase().isAfter(PhaseType.COMBAT_DECLARE_BLOCKERS)
                && !(sa.isCurse() && defense < 0)
                && !this.containsNonCombatKeyword(keywords)
                && !sa.hasParam("UntilYourNextTurn")) {
            return false;
        }

        final Player opp = ai.getOpponent();
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        sa.resetTargets();
        if (sa.hasParam("TargetingPlayer") && sa.getActivatingPlayer().equals(ai) && !sa.isTrigger()) {
            Player targetingPlayer = AbilityUtils.getDefinedPlayers(source, sa.getParam("TargetingPlayer"), sa).get(0);
            sa.setTargetingPlayer(targetingPlayer);
            return targetingPlayer.getController().chooseTargetsFor(sa);
        }

        List<Card> list = new ArrayList<Card>();
        if (sa.hasParam("AILogic")) {
            if (sa.getParam("AILogic").equals("HighestPower")) {
                list = CardLists.getValidCards(CardLists.filter(game.getCardsIn(ZoneType.Battlefield), Presets.CREATURES), tgt.getValidTgts(), ai, source);
                list = CardLists.getTargetableCards(list, sa);
                CardLists.sortByPowerDesc(list);
                if (!list.isEmpty()) {
                    sa.getTargets().add(list.get(0));
                    return true;
                } else {
                    return false;
                }
            }
            if (sa.getParam("AILogic").equals("Fight") || sa.getParam("AILogic").equals("PowerDmg")) {
            	final AbilitySub tgtFight = sa.getSubAbility();
                List<Card> aiCreatures = ai.getCreaturesInPlay();
                aiCreatures = CardLists.getTargetableCards(aiCreatures, sa);
                aiCreatures =  ComputerUtil.getSafeTargets(ai, sa, aiCreatures);
                ComputerUtilCard.sortByEvaluateCreature(aiCreatures);
                //sort is suboptimal due to conflicting needs depending on game state:
                //  -deathtouch for deal damage
                //  -max power for damage to player
                //  -survivability for generic "fight"
                //  -no support for "heroic"

                List<Card> humCreatures = ai.getOpponent().getCreaturesInPlay();
                humCreatures = CardLists.getTargetableCards(humCreatures, tgtFight);
                ComputerUtilCard.sortByEvaluateCreature(humCreatures);
                if (humCreatures.isEmpty() || aiCreatures.isEmpty()) {
                	return false;
                }
                int buffedAtk = attack, buffedDef = defense;
                for (Card humanCreature : humCreatures) {
                	for (Card aiCreature : aiCreatures) {
                	    if (sa.isSpell()) {   //heroic triggers adding counters
                            for (Trigger t : aiCreature.getTriggers()) {
                                if (t.getMode() == TriggerType.SpellCast) {
                                    final Map<String, String> params = t.getMapParams();
                                    if ("Card.Self".equals(params.get("TargetsValid")) && "You".equals(params.get("ValidActivatingPlayer")) 
                                    		&& params.containsKey("Execute")) {
                                        SpellAbility heroic = AbilityFactory.getAbility(aiCreature.getSVar(params.get("Execute")),aiCreature);
                                        if ("Self".equals(heroic.getParam("Defined")) && "P1P1".equals(heroic.getParam("CounterType"))) {
                                            int amount = AbilityUtils.calculateAmount(aiCreature, heroic.getParam("CounterNum"), heroic);
                                            buffedAtk += amount;
                                            buffedDef += amount;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                		if (sa.getParam("AILogic").equals("PowerDmg")) {
                			if (FightAi.canKill(aiCreature, humanCreature, buffedAtk)) {
	                			sa.getTargets().add(aiCreature);
	                			tgtFight.getTargets().add(humanCreature);
	                			return true;
	                		}
                		} else {
	                		if (FightAi.shouldFight(aiCreature, humanCreature, buffedAtk, buffedDef)) {
	                			sa.getTargets().add(aiCreature);
	                			tgtFight.getTargets().add(humanCreature);
	                			return true;
	                		}
                		}
                	}
                }
                return false;
            }
        }
        	
        if (sa.isCurse()) {
            if (sa.canTarget(opp)) {
                sa.getTargets().add(opp);
                return true;
            }
            list = this.getCurseCreatures(ai, sa, defense, attack, keywords);
        } else {
            if (!tgt.canTgtCreature()) {
                ZoneType zone = tgt.getZone().get(0);
                list = game.getCardsIn(zone);
            } else {
                list = this.getPumpCreatures(ai, sa, defense, attack, keywords);
            }
            if (sa.canTarget(ai)) {
                sa.getTargets().add(ai);
                return true;
            }
        }

        list = CardLists.getValidCards(list, tgt.getValidTgts(), ai, source);
        if (game.getStack().isEmpty()) {
            // If the cost is tapping, don't activate before declare
            // attack/block
            if (sa.getPayCosts() != null && sa.getPayCosts().hasTapCost()) {
                if (game.getPhaseHandler().getPhase().isBefore(PhaseType.COMBAT_DECLARE_ATTACKERS)
                        && game.getPhaseHandler().isPlayerTurn(ai)) {
                    list.remove(sa.getHostCard());
                }
                if (game.getPhaseHandler().getPhase().isBefore(PhaseType.COMBAT_DECLARE_BLOCKERS)
                        && game.getPhaseHandler().isPlayerTurn(opp)) {
                    list.remove(sa.getHostCard());
                }
            }
        }

        if (list.isEmpty()) {
            return mandatory && this.pumpMandatoryTarget(ai, sa, mandatory);
        }

        if (!sa.isCurse()) {
            // Don't target cards that will die.
            list = ComputerUtil.getSafeTargets(ai, sa, list);
        }

        while (sa.getTargets().getNumTargeted() < tgt.getMaxTargets(source, sa)) {
            Card t = null;
            // boolean goodt = false;

            if (list.isEmpty()) {
                if ((sa.getTargets().getNumTargeted() < tgt.getMinTargets(source, sa)) || (sa.getTargets().getNumTargeted() == 0)) {
                    if (mandatory) {
                        return this.pumpMandatoryTarget(ai, sa, mandatory);
                    }

                    sa.resetTargets();
                    return false;
                } else {
                    // TODO is this good enough? for up to amounts?
                    break;
                }
            }

            t = ComputerUtilCard.getBestAI(list);
            //option to hold removal instead only applies for single targeted removal
            if (!sa.isTrigger() && tgt.getMaxTargets(source, sa) == 1 && sa.isCurse()) {
                if (!ComputerUtilCard.useRemovalNow(sa, t, -defense, ZoneType.Graveyard)) {
                    return false;
                }
            }
            sa.getTargets().add(t);
            list.remove(t);
        }

        return true;
    } // pumpTgtAI()

    private boolean pumpMandatoryTarget(final Player ai, final SpellAbility sa, final boolean mandatory) {
        final Game game = ai.getGame();
        List<Card> list = game.getCardsIn(ZoneType.Battlefield);
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        final Player opp = ai.getOpponent();
        list = CardLists.getValidCards(list, tgt.getValidTgts(), sa.getActivatingPlayer(), sa.getHostCard());
        list = CardLists.getTargetableCards(list, sa);

        if (list.size() < tgt.getMinTargets(sa.getHostCard(), sa)) {
            sa.resetTargets();
            return false;
        }

        // Remove anything that's already been targeted
        for (final Card c : sa.getTargets().getTargetCards()) {
            list.remove(c);
        }

        List<Card> pref;
        List<Card> forced;
        final Card source = sa.getHostCard();

        if (sa.isCurse()) {
            pref = CardLists.filterControlledBy(list, opp);
            forced = CardLists.filterControlledBy(list, ai);
        } else {
            pref = CardLists.filterControlledBy(list, ai);
            forced = CardLists.filterControlledBy(list, opp);
        }

        while (sa.getTargets().getNumTargeted() < tgt.getMaxTargets(source, sa)) {
            if (pref.isEmpty()) {
                break;
            }

            Card c;
            if (CardLists.getNotType(pref, "Creature").isEmpty()) {
                c = ComputerUtilCard.getBestCreatureAI(pref);
            } else {
                c = ComputerUtilCard.getMostExpensivePermanentAI(pref, sa, true);
            }

            pref.remove(c);

            sa.getTargets().add(c);
        }

        while (sa.getTargets().getNumTargeted() < tgt.getMinTargets(source, sa)) {
            if (forced.isEmpty()) {
                break;
            }

            Card c;
            if (CardLists.getNotType(forced, "Creature").isEmpty()) {
                c = ComputerUtilCard.getWorstCreatureAI(forced);
            } else {
                c = ComputerUtilCard.getCheapestPermanentAI(forced, sa, true);
            }

            forced.remove(c);

            sa.getTargets().add(c);
        }

        if (sa.getTargets().getNumTargeted() < tgt.getMinTargets(source, sa)) {
            sa.resetTargets();
            return false;
        }

        return true;
    } // pumpMandatoryTarget()

    @Override
    protected boolean doTriggerAINoCost(Player ai, SpellAbility sa, boolean mandatory) {
        final Card source = sa.getHostCard();
        final String numDefense = sa.hasParam("NumDef") ? sa.getParam("NumDef") : "";
        final String numAttack = sa.hasParam("NumAtt") ? sa.getParam("NumAtt") : "";

        int defense;
        if (numDefense.contains("X") && source.getSVar("X").equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            final int xPay = ComputerUtilMana.determineLeftoverMana(sa, ai);
            source.setSVar("PayX", Integer.toString(xPay));
            defense = xPay;
        } else {
            defense = AbilityUtils.calculateAmount(sa.getHostCard(), numDefense, sa);
        }

        int attack;
        if (numAttack.contains("X") && source.getSVar("X").equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            final String toPay = source.getSVar("PayX");

            if (toPay.equals("")) {
                final int xPay = ComputerUtilMana.determineLeftoverMana(sa, ai);
                source.setSVar("PayX", Integer.toString(xPay));
                attack = xPay;
            } else {
                attack = Integer.parseInt(toPay);
            }
        } else {
            attack = AbilityUtils.calculateAmount(sa.getHostCard(), numAttack, sa);
        }

        if (sa.getTargetRestrictions() == null) {
            if (mandatory) {
                return true;
            }
        } else {
            return this.pumpTgtAI(ai, sa, defense, attack, mandatory);
        }

        return true;
    } // pumpTriggerAI

    @Override
    public boolean chkAIDrawback(SpellAbility sa, Player ai) {

        final Card source = sa.getHostCard();

        final String numDefense = sa.hasParam("NumDef") ? sa.getParam("NumDef") : "";
        final String numAttack = sa.hasParam("NumAtt") ? sa.getParam("NumAtt") : "";

        int defense;
        if (numDefense.contains("X") && source.getSVar("X").equals("Count$xPaid")) {
            defense = Integer.parseInt(source.getSVar("PayX"));
        } else {
            defense = AbilityUtils.calculateAmount(sa.getHostCard(), numDefense, sa);
        }

        int attack;
        if (numAttack.contains("X") && source.getSVar("X").equals("Count$xPaid")) {
            if (source.getSVar("PayX").equals("")) {
                // X is not set yet
                final int xPay = ComputerUtilMana.determineLeftoverMana(sa.getRootAbility(), ai);
                source.setSVar("PayX", Integer.toString(xPay));
                attack = xPay;
            } else {
                attack = Integer.parseInt(source.getSVar("PayX"));
            }
        } else {
            attack = AbilityUtils.calculateAmount(sa.getHostCard(), numAttack, sa);
        }

        if ((sa.getTargetRestrictions() == null) || !sa.getTargetRestrictions().doesTarget()) {
            if (source.isCreature()) {
                if (!source.hasKeyword("Indestructible")
                        && ((source.getNetDefense() + defense) <= source.getDamage())) {
                    return false;
                }
                if ((source.getNetDefense() + defense) <= 0) {
                    return false;
                }
            }
        } else {
            //Targeted
            if (!this.pumpTgtAI(ai, sa, defense, attack, false)) {
                return false;
            }
        }

        return true;
    } // pumpDrawbackAI()
    


    @Override
    public boolean confirmAction(Player player, SpellAbility sa, PlayerActionConfirmMode mode, String message) {
        //TODO Add logic here if necessary but I think the AI won't cast
        //the spell in the first place if it would curse its own creature
        //and the pump isn't mandatory
        return true;
    }
    
    boolean pumpAgainstRemoval(Player ai, SpellAbility sa) {
        final List<GameObject> objects = ComputerUtil.predictThreatenedObjects(sa.getActivatingPlayer(), sa);
        final List<Card> threatenedTargets = new ArrayList<Card>();
        final TargetRestrictions tgt = sa.getTargetRestrictions();

        if (tgt == null) {
            // For pumps without targeting restrictions, just return immediately until this is fleshed out.
            return false;
        }

        List<Card> targetables = CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), tgt.getValidTgts(), ai, sa.getHostCard());
        targetables = CardLists.getTargetableCards(targetables, sa);
        targetables = ComputerUtil.getSafeTargets(ai, sa, targetables);
        for (final Card c : targetables) {
            if (objects.contains(c)) {
                threatenedTargets.add(c);
            }
        }
        if (!threatenedTargets.isEmpty()) {
            ComputerUtilCard.sortByEvaluateCreature(threatenedTargets);
            for (Card c : threatenedTargets) {
                sa.getTargets().add(c);
                if (sa.getTargets().getNumTargeted() >= tgt.getMaxTargets(sa.getHostCard(), sa)) {
                    break;
                }
            }
            if (sa.getTargets().getNumTargeted() > tgt.getMaxTargets(sa.getHostCard(), sa)
                    || sa.getTargets().getNumTargeted() < tgt.getMinTargets(sa.getHostCard(), sa)) {
                sa.resetTargets();
                return false;
            }
            return true;
        }
        return false;
    }
}
