package forge.game.player;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicate;

import forge.card.ColorSet;
import forge.deck.Deck;
import forge.game.Game;
import forge.game.GameEntity;
import forge.game.GameObject;
import forge.game.GameType;
import forge.game.card.Card;
import forge.game.card.CounterType;
import forge.game.combat.Combat;
import forge.game.cost.Cost;
import forge.game.cost.CostPart;
import forge.game.mana.Mana;
import forge.game.phase.PhaseType;
import forge.game.replacement.ReplacementEffect;
import forge.game.spellability.AbilitySub;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.SpellAbilityStackInstance;
import forge.game.spellability.TargetChoices;
import forge.game.trigger.Trigger;
import forge.game.trigger.WrappedAbility;
import forge.game.zone.ZoneType;
import forge.item.PaperCard;


/** 
 * A prototype for player controller class
 * 
 * Handles phase skips for now.
 */
public abstract class PlayerController {

    public static enum ManaPaymentPurpose {
        DeclareAttacker,
        DeclareBlocker,
        Echo,
        Multikicker,
        Replicate,
        CumulativeUpkeep;
    }

    public static enum BinaryChoiceType {
        HeadsOrTails, // coin
        TapOrUntap,
        PlayOrDraw,
        OddsOrEvens
    }

    protected final Game game;

    private PhaseType autoPassUntil = null;
    protected final Player player;
    protected final LobbyPlayer lobbyPlayer;

    public PlayerController(Game game0, Player p, LobbyPlayer lp) {
        game = game0;
        player = p;
        lobbyPlayer = lp;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param cleanup
     */
    public void autoPassTo(PhaseType cleanup) {
        autoPassUntil = cleanup;
    }
    public void autoPassCancel() {
        autoPassUntil = null;
    }

    public boolean mayAutoPass(PhaseType phase) {
        return phase.isBefore(autoPassUntil);
    }


    // Triggers preliminary choice: ask, decline or play
    private Map<Integer, Boolean> triggersAlwaysAccept = new HashMap<Integer, Boolean>();

    public final  boolean shouldAlwaysAcceptTrigger(Integer trigger) { return Boolean.TRUE.equals(triggersAlwaysAccept.get(trigger)); }
    public final boolean shouldAlwaysDeclineTrigger(Integer trigger) { return Boolean.FALSE.equals(triggersAlwaysAccept.get(trigger)); }

    public final void setShouldAlwaysAcceptTrigger(Integer trigger) { triggersAlwaysAccept.put(trigger, true); }
    public final void setShouldAlwaysDeclineTrigger(Integer trigger) { triggersAlwaysAccept.put(trigger, false); }
    public final void setShouldAlwaysAskTrigger(Integer trigger) { triggersAlwaysAccept.remove(trigger); }

    // End of Triggers preliminary choice

    public LobbyPlayer getLobbyPlayer() { return lobbyPlayer; }

    /**
     * Uses GUI to learn which spell the player (human in our case) would like to play
     */
    public final SpellAbility getAbilityToPlay(List<SpellAbility> abilities) {
    	return getAbilityToPlay(abilities, null);
    }

    /**
     * Uses GUI to learn which spell the player (human in our case) would like to play
     */
    public abstract SpellAbility getAbilityToPlay(List<SpellAbility> abilities, MouseEvent triggerEvent);

    /**
     * TODO: Write javadoc for this method.
     * @param c
     */
    //public abstract void playFromSuspend(Card c);
    public abstract void playSpellAbilityForFree(SpellAbility copySA, boolean mayChoseNewTargets);
    public abstract void playSpellAbilityNoStack(SpellAbility effectSA, boolean mayChoseNewTargets);

    public abstract Deck sideboard(final Deck deck, GameType gameType);

    public abstract Map<Card, Integer> assignCombatDamage(Card attacker, List<Card> blockers, int damageDealt, GameEntity defender, boolean overrideOrder);

    public abstract Integer announceRequirements(SpellAbility ability, String announce, boolean allowZero);
    public abstract List<Card> choosePermanentsToSacrifice(SpellAbility sa, int min, int max, List<Card> validTargets, String message);
    public abstract List<Card> choosePermanentsToDestroy(SpellAbility sa, int min, int max, List<Card> validTargets, String message);
    public abstract TargetChoices chooseNewTargetsFor(SpellAbility ability);

    // Specify a target of a spell (Spellskite)
    public abstract Pair<SpellAbilityStackInstance, GameObject> chooseTarget(SpellAbility sa, List<Pair<SpellAbilityStackInstance, GameObject>> allTargets);

    public abstract List<Card> chooseCardsForEffect(List<Card> sourceList, SpellAbility sa, String title, int amount, boolean isOptional);
    public final Card chooseSingleCardForEffect(Collection<Card> sourceList, SpellAbility sa, String title) { return chooseSingleCardForEffect(sourceList, sa, title, false, null); }
    public final Card chooseSingleCardForEffect(Collection<Card> sourceList, SpellAbility sa, String title, boolean isOptional) { return chooseSingleCardForEffect(sourceList, sa, title, isOptional, null); } 
    public abstract Card chooseSingleCardForEffect(Collection<Card> sourceList, SpellAbility sa, String title, boolean isOptional, Player relatedPlayer);
    public abstract Player chooseSinglePlayerForEffect(List<Player> options, SpellAbility sa, String title);
    public abstract SpellAbility chooseSingleSpellForEffect(List<SpellAbility> spells, SpellAbility sa, String title);

    public abstract boolean confirmAction(SpellAbility sa, PlayerActionConfirmMode mode, String message);
    public abstract boolean confirmStaticApplication(Card hostCard, GameEntity affected, String logic, String message);
    public abstract boolean confirmTrigger(SpellAbility sa, Trigger regtrig, Map<String, String> triggerParams, boolean isMandatory);
    public abstract boolean getWillPlayOnFirstTurn(boolean isFirstGame);

    public abstract List<Card> orderBlockers(Card attacker, List<Card> blockers);
    public abstract List<Card> orderAttackers(Card blocker, List<Card> attackers);

    /** Shows the card to this player*/
    public abstract void reveal(String string, Collection<Card> cards, ZoneType zone, Player owner);
    /** Shows message to player to reveal chosen cardName, creatureType, number etc. AI must analyze API to understand what that is */
    public abstract void notifyOfValue(SpellAbility saSource, GameObject realtedTarget, String value);
    public abstract ImmutablePair<List<Card>, List<Card>> arrangeForScry(List<Card> topN);
    public abstract boolean willPutCardOnTop(Card c);
    public abstract List<Card> orderMoveToZoneList(List<Card> cards, ZoneType destinationZone);

    /** p = target player, validCards - possible discards, min cards to discard */
    public abstract List<Card> chooseCardsToDiscardFrom(Player playerDiscard, SpellAbility sa, List<Card> validCards, int min, int max);
    public abstract Card chooseCardToDredge(List<Card> dredgers);

    public abstract void playMiracle(SpellAbility miracle, Card card);
    public abstract List<Card> chooseCardsToDelve(int colorLessAmount, List<Card> grave);
    public abstract List<Card> chooseCardsToRevealFromHand(int min, int max, List<Card> valid);
    public abstract List<Card> chooseCardsToDiscardUnlessType(int min, List<Card> hand, String param, SpellAbility sa);
    public abstract List<SpellAbility> chooseSaToActivateFromOpeningHand(List<SpellAbility> usableFromOpeningHand);
    public abstract Mana chooseManaFromPool(List<Mana> manaChoices);

    public final String chooseSomeType(String kindOfType, SpellAbility sa, List<String> validTypes, List<String> invalidTypes) {
        return chooseSomeType(kindOfType, sa, validTypes, invalidTypes, false);
    }
    public abstract String chooseSomeType(String kindOfType, SpellAbility sa, List<String> validTypes, List<String> invalidTypes, boolean isOptional);
    public abstract Pair<CounterType,String> chooseAndRemoveOrPutCounter(Card cardWithCounter);
    public abstract boolean confirmReplacementEffect(ReplacementEffect replacementEffect, SpellAbility effectSA, String question);
    public abstract List<Card> getCardsToMulligan(boolean isCommander, Player firstPlayer);

    public abstract void declareAttackers(Player attacker, Combat combat);
    public abstract void declareBlockers(Player defender, Combat combat);
    public abstract void takePriority();

    public abstract List<Card> chooseCardsToDiscardToMaximumHandSize(int numDiscard);
    public abstract boolean payManaOptional(Card card, Cost cost, SpellAbility sa, String prompt, ManaPaymentPurpose purpose);

    public abstract int chooseNumber(SpellAbility sa, String title, int min, int max);

    public abstract boolean chooseBinary(SpellAbility sa, String question, BinaryChoiceType kindOfChoice);
    public abstract boolean chooseFlipResult(SpellAbility sa, Player flipper, boolean[] results, boolean call);
    public abstract Card chooseProtectionShield(GameEntity entityBeingDamaged, List<String> options, Map<String, Card> choiceMap);

    public abstract List<AbilitySub> chooseModeForAbility(SpellAbility sa, int min, int num);

    public abstract byte chooseColor(String message, SpellAbility sa, ColorSet colors);

    public abstract PaperCard chooseSinglePaperCard(SpellAbility sa, String message, Predicate<PaperCard> cpp, String name);
    public abstract List<String> chooseColors(String message, SpellAbility sa, int min, int max, List<String> options);
    public abstract CounterType chooseCounterType(Collection<CounterType> options, SpellAbility sa, String prompt);

    public abstract boolean confirmPayment(CostPart costPart, String string);
    public abstract ReplacementEffect chooseSingleReplacementEffect(String prompt, List<ReplacementEffect> possibleReplacers, HashMap<String, Object> runParams);
    public abstract String chooseProtectionType(String string, SpellAbility sa, List<String> choices);

    // these 4 need some refining.
    public abstract boolean payCostToPreventEffect(Cost cost, SpellAbility sa, boolean alreadyPaid, List<Player> allPayers);
    public abstract void orderAndPlaySimultaneousSa(List<SpellAbility> activePlayerSAs);
    public abstract void playTrigger(Card host, WrappedAbility wrapperAbility, boolean isMandatory);

    public abstract boolean playSaFromPlayEffect(SpellAbility tgtSA);
}
