package forge.game.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.Lists;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.GameEntity;
import forge.card.cost.Cost;
import forge.card.mana.Mana;
import forge.card.replacement.ReplacementEffect;
import forge.card.spellability.Ability;
import forge.card.spellability.AbilityStatic;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.deck.Deck;
import forge.game.Game;
import forge.game.GameType;
import forge.game.ai.AiController;
import forge.game.ai.ComputerUtil;
import forge.game.ai.ComputerUtilBlock;
import forge.game.ai.ComputerUtilCombat;
import forge.game.ai.ComputerUtilCost;
import forge.game.zone.ZoneType;
import forge.util.Aggregates;


/** 
 * A prototype for player controller class
 * 
 * Handles phase skips for now.
 */
public class PlayerControllerAi extends PlayerController {
    private final AiController brains;

    public PlayerControllerAi(Game game, Player p, LobbyPlayer lp) {
        super(game, p, lp);

        brains = new AiController(p, game); 
    }

    /**
     * Uses GUI to learn which spell the player (human in our case) would like to play
     */
    public SpellAbility getAbilityToPlay(List<SpellAbility> abilities) {
        if (abilities.size() == 0) {
            return null;
        } else 
            return abilities.get(0);
//        } else {
//            return GuiChoose.oneOrNone("Choose ability for AI to play", abilities); // some day network interaction will be here
//        }
    }

    /**
     * TODO: Write javadoc for this method.
     * @param c
     */
    /**public void playFromSuspend(Card c) {
        final List<SpellAbility> choices = c.getBasicSpells();
        c.setSuspendCast(true);
        getAi().chooseAndPlaySa(choices, true, true);
    }**/

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#playCascade(java.util.List, forge.Card)
     */
    @Override
    public boolean playCascade(Card cascadedCard, Card source) {
        final List<SpellAbility> choices = cascadedCard.getBasicSpells();
        return null != getAi().chooseAndPlaySa(choices, false, true);
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public AiController getAi() {
        return brains;
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#mayPlaySpellAbilityForFree(forge.card.spellability.SpellAbility)
     */
    @Override
    public void playSpellAbilityForFree(SpellAbility copySA) {
        if (copySA instanceof Spell) {
            Spell spell = (Spell) copySA;
            if (spell.canPlayFromEffectAI(true, true)) {
                ComputerUtil.playStackFree(player, copySA);
            }
        } else {
            copySA.canPlayAI();
            ComputerUtil.playStackFree(player, copySA);
        }
    }

    @Override
    public Deck sideboard(Deck deck, GameType gameType) {
        // AI does not know how to sideboard
        return deck;
    }

    @Override
    public Map<Card, Integer> assignCombatDamage(Card attacker, List<Card> blockers, int damageDealt, GameEntity defender) {
        return ComputerUtilCombat.distributeAIDamage(attacker, blockers, damageDealt, defender);
    }

    @Override
    public Integer announceRequirements(SpellAbility ability, String announce, boolean allowZero) {
        // For now, these "announcements" are made within the AI classes of the appropriate SA effects
        return null; // return incorrect value to indicate that
    }

    @Override
    public List<Card> choosePermanentsToSacrifice(SpellAbility sa, int min, int max, List<Card> validTargets, String message) {
        return ComputerUtil.choosePermanentsToSacrifice(player, validTargets, max, sa, false, min == 0);
    }

    @Override
    public List<Card> choosePermanentsToDestroy(SpellAbility sa, int min, int max, List<Card> validTargets, String message) {
        return ComputerUtil.choosePermanentsToSacrifice(player, validTargets, max, sa, true, min == 0);
    }

    
    @Override
    public Card chooseSingleCardForEffect(List<Card> options, SpellAbility sa, String title, boolean isOptional) {
        return getAi().chooseSingleCardForEffect(options, sa, title, isOptional);
    }

    @Override
    public boolean confirmAction(SpellAbility sa, PlayerActionConfirmMode mode, String message) {
        return getAi().confirmAction(sa, mode, message);
    }

    @Override
    public boolean getWillPlayOnFirstTurn(boolean isFirstGame) {
        return true; // AI is brave :)
    }
    @Override
    public boolean confirmStaticApplication(Card hostCard, GameEntity affected, String logic, String message) {
        return getAi().confirmStaticApplication(hostCard, affected, logic, message);
    }

    @Override
    public List<Card> orderBlockers(Card attacker, List<Card> blockers) {
        return ComputerUtilBlock.orderBlockers(attacker, blockers);
    }

    @Override
    public List<Card> orderAttackers(Card blocker, List<Card> attackers) {
        return ComputerUtilBlock.orderAttackers(blocker, attackers);
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#reveal(java.lang.String, java.util.List)
     */
    @Override
    public void reveal(String string, Collection<Card> cards, ZoneType zone, Player owner) {
        // We don't know how to reveal cards to AI
    }

    @Override
    public ImmutablePair<List<Card>, List<Card>> arrangeForScry(List<Card> topN) {
        List<Card> toBottom = new ArrayList<Card>();
        List<Card> toTop = new ArrayList<Card>();

        for (Card c: topN) {
            if (ComputerUtil.scryWillMoveCardToBottomOfLibrary(player, c))
                toBottom.add(c);
            else 
                toTop.add(c); 
        }

        // put the rest on top in random order
        Collections.shuffle(toTop);
        return ImmutablePair.of(toTop, toBottom);
    }


    @Override
    public boolean willPutCardOnTop(Card c) {
        return true; // AI does not know what will happen next (another clash or that would become his topdeck)
    }

    @Override
    public List<Card> orderMoveToZoneList(List<Card> cards, ZoneType destinationZone) {
        //TODO Add logic for AI ordering here
        return cards;
    }

    @Override
    public List<Card> chooseCardsToDiscardFrom(Player p, SpellAbility sa, List<Card> validCards, int min, int max) {
        boolean isTargetFriendly = !p.isOpponentOf(player);
        
        return isTargetFriendly
               ? ComputerUtil.getCardsToDiscardFromFriend(player, p, sa, validCards, min, max)
               : ComputerUtil.getCardsToDiscardFromOpponent(player, p, sa, validCards, min, max);
    }

    @Override
    public Card chooseCardToDredge(List<Card> dredgers) {
        return getAi().chooseCardToDredge(dredgers);
    }

    @Override
    public void playMiracle(SpellAbility miracle, Card card) {
        getAi().chooseAndPlaySa(false, false, miracle);
    }


    @Override
    public void playMadness(SpellAbility madness) {
        getAi().chooseAndPlaySa(false, false, madness);
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseCardsToDelve(int, java.util.List)
     */
    @Override
    public List<Card> chooseCardsToDelve(int colorlessCost, List<Card> grave) {
        return getAi().chooseCardsToDelve(colorlessCost, grave);
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseTargets(forge.card.spellability.SpellAbility, forge.card.spellability.SpellAbilityStackInstance)
     */
    @Override
    public Target chooseTargets(SpellAbility ability) {
        // AI currently can't do this. But when it can it will need to be based on Ability API
        return null;
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseCardsToDiscardUnlessType(int, java.util.List, java.lang.String, forge.card.spellability.SpellAbility)
     */
    @Override
    public List<Card> chooseCardsToDiscardUnlessType(int num, List<Card> hand, String uType, SpellAbility sa) {
        final List<Card> cardsOfType = CardLists.getType(hand, uType);
        if (!cardsOfType.isEmpty()) {
            Card toDiscard = Aggregates.itemWithMin(cardsOfType, CardPredicates.Accessors.fnGetCmc);
            return Lists.newArrayList(toDiscard);
        }
        return getAi().getCardsToDiscard(num, (String[])null, sa);
    }


    @Override
    public Mana chooseManaFromPool(List<Mana> manaChoices) {
        return manaChoices.get(0); // no brains used
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#ChooseSomeType(java.lang.String, java.util.List, java.util.List)
     */
    @Override
    public String chooseSomeType(String kindOfType, String aiLogic, List<String> validTypes, List<String> invalidTypes) {
        return ComputerUtil.chooseSomeType(player, kindOfType, aiLogic, invalidTypes);
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#confirmReplacementEffect(forge.card.replacement.ReplacementEffect, forge.card.spellability.SpellAbility, java.lang.String)
     */
    @Override
    public boolean confirmReplacementEffect(ReplacementEffect replacementEffect, SpellAbility effectSA, String question) {
        return ReplacementEffect.aiShouldRun(replacementEffect, effectSA, player);
    }

    @Override
    public List<Card> getCardsToMulligan(boolean isCommander, Player firstPlayer)  { 
        if( !ComputerUtil.wantMulligan(player) )
            return null;

        if (!isCommander) 
            return player.getCardsIn(ZoneType.Hand);
        else
            return ComputerUtil.getPartialParisCandidates(player);
    }

    @Override
    public void takePriority() {
        if ( !game.isGameOver() )
            brains.onPriorityRecieved();
        // use separate thread for AI?
    }

    @Override
    public List<Card> chooseCardsToDiscardToMaximumHandSize(int numDiscard) {
        return brains.getCardsToDiscard(numDiscard, (String[])null, null);
    }

    @Override
    public List<Card> chooseCardsToRevealFromHand(int min, int max, List<Card> valid) {
        return max == 0 ? Lists.<Card>newArrayList() : valid.subList(0, max);
    }

    @Override
    public boolean payManaOptional(Card c, Cost cost, String prompt, ManaPaymentPurpose purpose) {
        final Ability ability = new AbilityStatic(c, cost, null) { @Override public void resolve() {} };
        ability.setActivatingPlayer(c.getController());

        if (ComputerUtilCost.canPayCost(ability, c.getController())) {
            ComputerUtil.playNoStack(c.getController(), ability, game);
            return true;
        }
        return false;
    }
}
