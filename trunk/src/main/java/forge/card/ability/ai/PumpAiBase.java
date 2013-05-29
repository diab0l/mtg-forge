package forge.card.ability.ai;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.CardUtil;
import forge.card.ability.SpellAbilityAi;
import forge.card.spellability.SpellAbility;
import forge.game.Game;
import forge.game.ai.ComputerUtil;
import forge.game.ai.ComputerUtilCard;
import forge.game.ai.ComputerUtilCombat;
import forge.game.phase.Combat;
import forge.game.phase.CombatUtil;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.phase.Untap;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public abstract class PumpAiBase extends SpellAbilityAi {

    public boolean containsUsefulKeyword(final Player ai, final List<String> keywords, final Card card, final SpellAbility sa, final int attack) {
        for (final String keyword : keywords) {
            if (!sa.isCurse() && isUsefulPumpKeyword(ai, keyword, card, sa, attack)) {
                return true;
            }
            if (sa.isCurse() && isUsefulCurseKeyword(ai, keyword, card, sa)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks if is useful keyword.
     * 
     * @param keyword
     *            the keyword
     * @param card
     *            the card
     * @param sa SpellAbility
     * @return true, if is useful keyword
     */
    public boolean isUsefulCurseKeyword(final Player ai, final String keyword, final Card card, final SpellAbility sa) {
        final Game game = ai.getGame();
        final PhaseHandler ph = game.getPhaseHandler();
        final Player human = ai.getOpponent();
        //int attack = getNumAttack(sa);
        //int defense = getNumDefense(sa);
        if (!CardUtil.isStackingKeyword(keyword) && card.hasKeyword(keyword)) {
            return false;
        } else if (keyword.equals("Defender") || keyword.endsWith("CARDNAME can't attack.")) {
            if (ph.isPlayerTurn(ai) || !CombatUtil.canAttack(card, human)
                    || (card.getNetCombatDamage() <= 0)
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS)) {
                return false;
            }
        } else if (keyword.endsWith("CARDNAME can't attack or block.")) {
            if (sa.hasParam("UntilYourNextTurn")) {
                if (CombatUtil.canAttack(card, human) || CombatUtil.canBlock(card, true)) {
                    return true;
                }
                return false;
            }
            if (ph.isPlayerTurn(human)) {
                if (!CombatUtil.canAttack(card, human)
                        || (card.getNetCombatDamage() <= 0)
                        || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS)) {
                    return false;
                }
            } else {
                if (ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_BLOCKERS)
                        || ph.getPhase().isBefore(PhaseType.MAIN1)) {
                    return false;
                }

                List<Card> attackers = CardLists.filter(ai.getCardsIn(ZoneType.Battlefield), new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return (c.isCreature() && CombatUtil.canAttack(c, human));
                    }
                });
                if (!CombatUtil.canBlockAtLeastOne(card, attackers)) {
                    return false;
                }
            }
        } else if (keyword.endsWith("CARDNAME can't block.")) {
            if (ph.isPlayerTurn(human) || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_BLOCKERS)
                    || ph.getPhase().isBefore(PhaseType.MAIN1)) {
                return false;
            }

            List<Card> attackers = CardLists.filter(ai.getCardsIn(ZoneType.Battlefield), new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    return (c.isCreature() && CombatUtil.canAttack(c, human));
                }
            });
            if (!CombatUtil.canBlockAtLeastOne(card, attackers)) {
                return false;
            }
        } else if (keyword.endsWith("This card doesn't untap during your next untap step.")) {
            if (ph.getPhase().isBefore(PhaseType.MAIN2) || card.isUntapped() || ph.isPlayerTurn(human)
                    || !Untap.canUntap(card)) {
                return false;
            }
        } else if (keyword.endsWith("Prevent all combat damage that would be dealt by CARDNAME.")
                || keyword.endsWith("Prevent all damage that would be dealt by CARDNAME.")) {
            if (ph.isPlayerTurn(ai) && (!(CombatUtil.canBlock(card) || card.isBlocking())
                    || card.getNetCombatDamage() <= 0
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)
                    || ph.getPhase().isBefore(PhaseType.MAIN1)
                    || CardLists.getNotKeyword(ai.getCreaturesInPlay(), "Defender").isEmpty())) {
                return false;
            }
            if (ph.isPlayerTurn(human) && (!card.isAttacking()
                    || card.getNetCombatDamage() <= 0)) {
                return false;
            }
        } else if (keyword.endsWith("CARDNAME attacks each turn if able.")) {
            if (ph.isPlayerTurn(ai) || !CombatUtil.canAttack(card, human) || !CombatUtil.canBeBlocked(card)
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS)) {
                return false;
            }
        } else if (keyword.endsWith("CARDNAME can't be regenerated.")) {
            if (card.getShield() > 0) {
                return true;
            }
            if (card.hasKeyword("If CARDNAME would be destroyed, regenerate it.")
                    && (card.isBlocked() || card.isBlocking())) {
                return true;
            }
            return false;
        } else if (keyword.endsWith("CARDNAME's activated abilities can't be activated.")) {
            return false; //too complex
        }
        return true;
    }

    /**
     * Checks if is useful keyword.
     * 
     * @param keyword
     *            the keyword
     * @param card
     *            the card
     * @param sa SpellAbility
     * @return true, if is useful keyword
     */
    public boolean isUsefulPumpKeyword(final Player ai, final String keyword, final Card card, final SpellAbility sa, final int attack) {
        final Game game = ai.getGame();
        final PhaseHandler ph = game.getPhaseHandler();
        final Player opp = ai.getOpponent();
        //int defense = getNumDefense(sa);
        if (!CardUtil.isStackingKeyword(keyword) && card.hasKeyword(keyword)) {
            return false;
        }

        final boolean evasive = (keyword.endsWith("Unblockable") || keyword.endsWith("Fear")
                || keyword.endsWith("Intimidate") || keyword.endsWith("Shadow")
                || keyword.contains("CantBeBlockedBy") || keyword.endsWith("CARDNAME can't be blocked except by Walls."));
        final boolean combatRelevant = (keyword.endsWith("First Strike") || keyword.contains("Bushido"));
        // give evasive keywords to creatures that can or do attack
        if (evasive) {
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    || card.getNetCombatDamage() <= 0
                    || CardLists.filter(opp.getCreaturesInPlay(), CardPredicates.possibleBlockers(card)).isEmpty()) {
                return false;
            }
        } else if (keyword.endsWith("Flying")) {
            if (ph.isPlayerTurn(opp)
                    && ph.getPhase().equals(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    && !CardLists.getKeyword(game.getCombat().getAttackers(), "Flying").isEmpty()
                    && !card.hasKeyword("Reach")
                    && CombatUtil.canBlock(card)
                    && ComputerUtilCombat.lifeInDanger(ai, game.getCombat())) {
                return true;
            }
            Predicate<Card> flyingOrReach = Predicates.or(CardPredicates.hasKeyword("Flying"), CardPredicates.hasKeyword("Reach"));
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    || card.getNetCombatDamage() <= 0
                    || !Iterables.any(CardLists.filter(opp.getCreaturesInPlay(), CardPredicates.possibleBlockers(card)),
                            Predicates.not(flyingOrReach))) {
                return false;
            }
        } else if (keyword.endsWith("Horsemanship")) {
            if (ph.isPlayerTurn(opp)
                    && ph.getPhase().equals(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    && !CardLists.getKeyword(game.getCombat().getAttackers(), "Horsemanship").isEmpty()
                    && CombatUtil.canBlock(card)
                    && ComputerUtilCombat.lifeInDanger(ai, game.getCombat())) {
                return true;
            }
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    || card.getNetCombatDamage() <= 0
                    || CardLists.getNotKeyword(CardLists.filter(opp.getCreaturesInPlay(), CardPredicates.possibleBlockers(card)),
                            "Horsemanship").isEmpty()) {
                return false;
            }
        } else if (keyword.endsWith("Haste")) {
            if (!card.hasSickness() || ph.isPlayerTurn(opp) || card.isTapped()
                    || card.getNetCombatDamage() <= 0
                    || card.hasKeyword("CARDNAME can attack as though it had haste.")
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS)
                    || !CombatUtil.canAttackNextTurn(card)) {
                return false;
            }
        } else if (keyword.endsWith("Indestructible")) {
            return true;
        } else if (keyword.endsWith("Deathtouch")) {
            Combat combat = game.getCombat();
            if (ph.isPlayerTurn(opp) && ph.getPhase().equals(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)) {
                List<Card> attackers = combat.getAttackers();
                for (Card attacker : attackers) {
                    if (CombatUtil.canBlock(attacker, card, combat)
                            && !ComputerUtilCombat.canDestroyAttacker(ai, attacker, card, combat, false)) {
                        return true;
                    }
                }
            } else if (ph.isPlayerTurn(ai) && ph.getPhase().isBefore(PhaseType.COMBAT_DECLARE_ATTACKERS)
                    && CombatUtil.canAttack(card, opp)) {
                List<Card> blockers = opp.getCreaturesInPlay();
                for (Card blocker : blockers) {
                    if (CombatUtil.canBlock(card, blocker, combat)
                            && !ComputerUtilCombat.canDestroyBlocker(ai, blocker, card, combat, false)) {
                        return true;
                    }
                }
            }
            return false;
        } else if (combatRelevant) {
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)
                    || (opp.getCreaturesInPlay().size() < 1)
                    || CardLists.filter(opp.getCreaturesInPlay(), CardPredicates.possibleBlockers(card)).isEmpty()) {
                return false;
            }
        } else if (keyword.equals("Double Strike")) {
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || card.getNetCombatDamage() <= 0
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)) {
                return false;
            }
        } else if (keyword.startsWith("Rampage")) {
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    || CardLists.filter(opp.getCreaturesInPlay(), CardPredicates.possibleBlockers(card)).size() < 2) {
                return false;
            }
        } else if (keyword.startsWith("Flanking")) {
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    || CardLists.getNotKeyword(CardLists.filter(opp.getCreaturesInPlay(), CardPredicates.possibleBlockers(card)),
                            "Flanking").isEmpty()) {
                return false;
            }
        } else if (keyword.startsWith("Trample")) {
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || !CombatUtil.canBeBlocked(card)
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    || card.getNetCombatDamage() + attack <= 1
                    || CardLists.filter(opp.getCreaturesInPlay(), CardPredicates.possibleBlockers(card)).isEmpty()) {
                return false;
            }
        } else if (keyword.equals("Infect")) {
            if (card.getNetCombatDamage() <= 0) {
                return false;
            }
            if (card.isBlocking()) {
                return true;
            }
            if ((ph.isPlayerTurn(opp))
                    || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)) {
                return false;
            }
        } else if (keyword.endsWith("Wither")) {
            if (card.getNetCombatDamage() <= 0) {
                return false;
            }
            if (card.isBlocking()) {
                return true;
            }
            if (card.isAttacking() && card.isBlocked()) {
                return true;
            }
            return false;
        } else if (keyword.equals("Lifelink")) {
            if (card.getNetCombatDamage() <= 0) {
                return false;
            }
            if (!card.isBlocking() && !card.isAttacking()) {
                return false;
            }
        } else if (keyword.equals("Vigilance")) {
            if (ph.isPlayerTurn(opp) || !CombatUtil.canAttack(card, opp)
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS)
                    || CardLists.getNotKeyword(opp.getCreaturesInPlay(), "Defender").size() < 1) {
                return false;
            }
        } else if (keyword.equals("Reach")) {
            if (ph.isPlayerTurn(ai)
                    || !ph.getPhase().equals(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    || CardLists.getKeyword(game.getCombat().getAttackers(), "Flying").isEmpty()
                    || card.hasKeyword("Flying")
                    || !CombatUtil.canBlock(card)) {
                return false;
            }
        } else if (keyword.endsWith("CARDNAME can block an additional creature.")) {
            if (ph.isPlayerTurn(ai)
                    || !ph.getPhase().equals(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)) {
                return false;
            }
            int canBlockNum = 1 + card.getKeywordAmount("CARDNAME can block an additional creature.");
            int possibleBlockNum = 0;
            for (Card attacker : game.getCombat().getAttackers()) {
                if (CombatUtil.canBlock(attacker, card)) {
                    possibleBlockNum++;
                    if (possibleBlockNum > canBlockNum) {
                        break;
                    }
                }
            }
            if (possibleBlockNum <= canBlockNum) {
                return false;
            }
        } else if (keyword.equals("Shroud") || keyword.equals("Hexproof")) {
            if (!ComputerUtil.predictThreatenedObjects(sa.getActivatingPlayer(), sa).contains(card)) {
                return false;
            }
        } else if (keyword.equals("Islandwalk")) {
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    || card.getNetCombatDamage() <= 0
                    || CardLists.getType(opp.getLandsInPlay(), "Island").isEmpty()
                    || CardLists.filter(opp.getCreaturesInPlay(), CardPredicates.possibleBlockers(card)).isEmpty()) {
                return false;
            }
        } else if (keyword.equals("Swampwalk")) {
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    || card.getNetCombatDamage() <= 0
                    || CardLists.getType(opp.getLandsInPlay(), "Swamp").isEmpty()
                    || CardLists.filter(opp.getCreaturesInPlay(), CardPredicates.possibleBlockers(card)).isEmpty()) {
                return false;
            }
        } else if (keyword.equals("Mountainwalk")) {
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    || card.getNetCombatDamage() <= 0
                    || CardLists.getType(opp.getLandsInPlay(), "Mountain").isEmpty()
                    || CardLists.filter(opp.getCreaturesInPlay(), CardPredicates.possibleBlockers(card)).isEmpty()) {
                return false;
            }
        } else if (keyword.equals("Forestwalk")) {
            if (ph.isPlayerTurn(opp) || !(CombatUtil.canAttack(card, opp) || card.isAttacking())
                    || ph.getPhase().isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY)
                    || card.getNetCombatDamage() <= 0
                    || CardLists.getType(opp.getLandsInPlay(), "Forest").isEmpty()
                    || CardLists.filter(opp.getCreaturesInPlay(), CardPredicates.possibleBlockers(card)).isEmpty()) {
                return false;
            }
        } else if (keyword.endsWith("CARDNAME can attack as though it didn't have defender.")) {
            if (!ph.isPlayerTurn(ai) || !card.hasKeyword("Defender")
                    || ph.getPhase().isAfter(PhaseType.COMBAT_BEGIN)
                    || card.isTapped()) {
                return false;
            }
        }
        return true;
    }

    protected boolean shouldPumpCard(final Player ai, final SpellAbility sa, final Card c, final int defense, final int attack,
            final List<String> keywords) {
        final Game game = ai.getGame();
        PhaseHandler phase = game.getPhaseHandler();

        if (!c.canBeTargetedBy(sa)) {
            return false;
        }

        if ((c.getNetDefense() + defense) <= 0) {
            return false;
        }

        if (containsUsefulKeyword(ai, keywords, c, sa, attack)) {
            return true;
        }

        // will the creature attack (only relevant for sorcery speed)?
        if (phase.getPhase().isBefore(PhaseType.COMBAT_DECLARE_ATTACKERS)
                && phase.isPlayerTurn(ai)
                && SpellAbilityAi.isSorcerySpeed(sa)
                && attack > 0
                && ComputerUtilCard.doesCreatureAttackAI(ai, c)) {
            return true;
        }

        if (sa.isTrigger() && phase.getPhase().isBefore(PhaseType.COMBAT_DECLARE_ATTACKERS)) {
            if (phase.isPlayerTurn(ai)) {
                if (CombatUtil.canAttack(c)) {
                    return true;
                }
            } else {
                if (CombatUtil.canBlock(c)) {
                    return true;
                }
            }
        }

        // is the creature blocking and unable to destroy the attacker
        // or would be destroyed itself?
        if (phase.is(PhaseType.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY) && c.isBlocking()) {
            if (defense > 0 && ComputerUtilCombat.blockerWouldBeDestroyed(ai, c)) {
                return true;
            }
            List<Card> blockedBy = game.getCombat().getAttackersBlockedBy(c);
            // For now, Only care the first creature blocked by a card.
            // TODO Add in better BlockAdditional support
            if (!blockedBy.isEmpty() && attack > 0 && !ComputerUtilCombat.attackerWouldBeDestroyed(ai, blockedBy.get(0))) {
                return true;
            }
        }

        // is the creature unblocked and the spell will pump its power?
        if (phase.is(PhaseType.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)
                && game.getCombat().isAttacking(c) && game.getCombat().isUnblocked(c) && attack > 0) {
            return true;
        }

        // is the creature blocked and the blocker would survive
        if (phase.is(PhaseType.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY) && attack > 0
                && game.getCombat().isAttacking(c)
                && game.getCombat().isBlocked(c)
                && game.getCombat().getBlockers(c) != null
                && !game.getCombat().getBlockers(c).isEmpty()
                && !ComputerUtilCombat.blockerWouldBeDestroyed(ai, game.getCombat().getBlockers(c).get(0))) {
            return true;
        }

        // if the life of the computer is in danger, try to pump blockers blocking Tramplers
        List<Card> blockedBy = game.getCombat().getAttackersBlockedBy(c);
        boolean attackerHasTrample = false;
        for (Card b : blockedBy) {
            attackerHasTrample |= b.hasKeyword("Trample");
        }

        if (phase.getPhase().equals(PhaseType.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)
                && phase.isPlayerTurn(ai.getOpponent())
                && c.isBlocking()
                && defense > 0
                && attackerHasTrample
                && (sa.isAbility() || ComputerUtilCombat.lifeInDanger(ai, game.getCombat()))) {
            return true;
        }

        return false;
    }

    /**
     * <p>
     * getPumpCreatures.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    protected List<Card> getPumpCreatures(final Player ai, final SpellAbility sa, final int defense, final int attack, final List<String> keywords) {

        List<Card> list = ai.getCreaturesInPlay();
        list = CardLists.filter(list, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return shouldPumpCard(ai, sa, c, defense, attack, keywords);
            }
        });
        return list;
    } // getPumpCreatures()

    /**
     * <p>
     * getCurseCreatures.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param defense
     *            a int.
     * @param attack
     *            a int.
     * @return a {@link forge.CardList} object.
     */
    protected List<Card> getCurseCreatures(final Player ai, final SpellAbility sa, final int defense, final int attack, final List<String> keywords) {
        List<Card> list = ai.getOpponent().getCreaturesInPlay();
        final Game game = ai.getGame();
        list = CardLists.getTargetableCards(list, sa);
        
        if (list.isEmpty()) {
            return list;
        }

        if (defense < 0) { // with spells that give -X/-X, compi will try to destroy a creature
            list = CardLists.filter(list, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    if (c.getNetDefense() <= -defense) {
                        return true; // can kill indestructible creatures
                    }
                    return ((ComputerUtilCombat.getDamageToKill(c) <= -defense) && !c.hasKeyword("Indestructible"));
                }
            }); // leaves all creatures that will be destroyed
        } // -X/-X end
        else if (attack < 0 && !game.getPhaseHandler().isPreventCombatDamageThisTurn()) {
            // spells that give -X/0
            boolean isMyTurn = game.getPhaseHandler().isPlayerTurn(ai);
            if (isMyTurn) {
                if (game.getPhaseHandler().getPhase().isBefore(PhaseType.COMBAT_BEGIN)) {
                    // TODO: Curse creatures that will block AI's creatures, if AI is going to attack.
                    list = new ArrayList<Card>();
                } else {
                    list = new ArrayList<Card>();
                }
            } else {
                // Human active, only curse attacking creatures
                if (game.getPhaseHandler().getPhase().isBefore(PhaseType.COMBAT_DECLARE_BLOCKERS)) {
                    list = CardLists.filter(list, new Predicate<Card>() {
                        @Override
                        public boolean apply(final Card c) {
                            if (!c.isAttacking()) {
                                return false;
                            }
                            if (c.getNetAttack() > 0 && ai.getLife() < 5) {
                                return true;
                            }
                            //Don't waste a -7/-0 spell on a 1/1 creature
                            if (c.getNetAttack() + attack > -2 || c.getNetAttack() > 3) {
                                return true;
                            }
                            return false;
                        }
                    });
                } else {
                    list = new ArrayList<Card>();
                }
            }
        } // -X/0 end
        else {
            final boolean addsKeywords = keywords.size() > 0;
            if (addsKeywords) {
                list = CardLists.filter(list, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return containsUsefulKeyword(ai, keywords, c, sa, attack);
                    }
                });
            } else if (sa.hasParam("NumAtt") || sa.hasParam("NumDef")) { 
                // X is zero
                list = new ArrayList<Card>();
            }
        }

        return list;
    } // getCurseCreatures()

    protected boolean containsNonCombatKeyword(final List<String> keywords) {
        for (final String keyword : keywords) {
            // since most keywords are combat relevant check for those that are
            // not
            if (keyword.endsWith("This card doesn't untap during your next untap step.")
                    || keyword.endsWith("Shroud") || keyword.endsWith("Hexproof")) {
                return true;
            }
        }
        return false;
    }

}
