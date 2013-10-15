package forge.game.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import forge.Card;
import forge.CounterType;
import forge.GameEntity;
import forge.ITargetable;
import forge.Singletons;
import forge.card.ability.effects.CharmEffect;
import forge.card.cost.Cost;
import forge.card.mana.Mana;
import forge.card.replacement.ReplacementEffect;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellAbilityStackInstance;
import forge.card.spellability.TargetSelection;
import forge.card.spellability.TargetChoices;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.game.Game;
import forge.game.GameType;
import forge.game.combat.Combat;
import forge.game.phase.PhaseType;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.GuiDialog;
import forge.gui.GuiUtils;
import forge.gui.input.InputAttack;
import forge.gui.input.InputBlock;
import forge.gui.input.InputConfirmMulligan;
import forge.gui.input.InputPassPriority;
import forge.gui.input.InputPlayOrDraw;
import forge.gui.input.InputSelectCards;
import forge.gui.input.InputSelectCardsFromList;
import forge.gui.match.CMatchUI;
import forge.item.PaperCard;
import forge.properties.ForgePreferences.FPref;
import forge.util.Lang;
import forge.util.TextUtil;



/** 
 * A prototype for player controller class
 * 
 * Handles phase skips for now.
 */
public class PlayerControllerHuman extends PlayerController {


    public PlayerControllerHuman(Game game0, Player p, LobbyPlayer lp) {
        super(game0, p, lp);
    }


    public boolean isUiSetToSkipPhase(final Player turn, final PhaseType phase) {
        return !CMatchUI.SINGLETON_INSTANCE.stopAtPhase(turn, phase);
    }
    

    /**
     * Uses GUI to learn which spell the player (human in our case) would like to play
     */
    public SpellAbility getAbilityToPlay(List<SpellAbility> abilities) {
        if (abilities.isEmpty()) {
            return null;
        } else if (abilities.size() == 1) {
            return abilities.get(0);
        } else {
            return GuiChoose.oneOrNone("Choose ability to play", abilities);
        }
    }

    /**
     * TODO: Write javadoc for this method.
     * @param c
     */
    /**public void playFromSuspend(Card c) {
        c.setSuspendCast(true);
        HumanPlay.playCardWithoutPayingManaCost(player, c);
    }**/


    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#mayPlaySpellAbilityForFree(forge.card.spellability.SpellAbility)
     */
    @Override
    public void playSpellAbilityForFree(SpellAbility copySA, boolean mayChoseNewTargets) {
        HumanPlay.playSaWithoutPayingManaCost(player.getGame(), copySA, mayChoseNewTargets);
    }

    @Override
    public void playSpellAbilityNoStack(SpellAbility effectSA, boolean canSetupTargets) {
        HumanPlay.playSpellAbilityNoStack(player, effectSA, !canSetupTargets);
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#sideboard(forge.deck.Deck)
     */
    @Override
    public Deck sideboard(Deck deck, GameType gameType) {
        CardPool sideboard = deck.get(DeckSection.Sideboard);
        if (sideboard == null) {
            // Use an empty cardpool instead of null for 75/0 sideboarding scenario.
            sideboard = new CardPool();
        }

        CardPool main = deck.get(DeckSection.Main);

        boolean conform = Singletons.getModel().getPreferences().getPrefBoolean(FPref.ENFORCE_DECK_LEGALITY);
        int mainSize = main.countAll();
        int sbSize = sideboard.countAll();
        int combinedDeckSize = mainSize + sbSize;

        int deckMinSize = Math.min(mainSize, gameType.getDecksFormat().getMainRange().getMinimumInteger());
        IntRange sbRange = gameType.getDecksFormat().getSideRange();
        // Limited doesn't have a sideboard max, so let the Main min take care of things.
        int sbMax = sbRange == null ? combinedDeckSize : sbRange.getMaximumInteger();

        CardPool newSb = new CardPool();
        List<PaperCard> newMain = null;
        
        if (sbSize == 0 && mainSize == deckMinSize) {
            // Skip sideboard loop if there are no sideboarding opportunities
            newMain = main.toFlatList();
        } else {
            do {
                if (newMain != null) {
                    if (newMain.size() < deckMinSize) {
                        String errMsg = String.format("Too few cards in your main deck (minimum %d), please make modifications to your deck again.", deckMinSize);
                        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), errMsg, "Invalid deck", JOptionPane.ERROR_MESSAGE);
                    } else {
                        String errMsg = String.format("Too many cards in your sideboard (maximum %d), please make modifications to your deck again.", sbMax);
                        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), errMsg, "Invalid deck", JOptionPane.ERROR_MESSAGE);
                    }
                }
                // Sideboard rules have changed for M14, just need to consider min maindeck and max sideboard sizes
                // No longer need 1:1 sideboarding in non-limited formats
                newMain = GuiChoose.sideboard(sideboard.toFlatList(), main.toFlatList());
            } while (conform && (newMain.size() < deckMinSize || combinedDeckSize - newMain.size() > sbMax));
        }
        newSb.clear();
        newSb.addAll(main);
        newSb.addAll(sideboard);
        for(PaperCard c : newMain) {
            newSb.remove(c);
        }
    
        Deck res = (Deck)deck.copyTo(deck.getName());
        res.getMain().clear();
        res.getMain().add(newMain);
        CardPool resSb = res.getOrCreate(DeckSection.Sideboard);
        resSb.clear();
        resSb.addAll(newSb);
        return res;
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#assignCombatDamage()
     */
    @Override
    public Map<Card, Integer> assignCombatDamage(Card attacker, List<Card> blockers, int damageDealt, GameEntity defender, boolean overrideOrder) {
        // Attacker is a poor name here, since the creature assigning damage
        // could just as easily be the blocker. 
        Map<Card, Integer> map;
        if (defender != null && assignDamageAsIfNotBlocked(attacker)) {
            map = new HashMap<Card, Integer>();
            map.put(null, damageDealt);
        } else {
            if ((attacker.hasKeyword("Trample") && defender != null) || (blockers.size() > 1)) {
                map = CMatchUI.SINGLETON_INSTANCE.getDamageToAssign(attacker, blockers, damageDealt, defender, overrideOrder);
            } else {
                map = new HashMap<Card, Integer>();
                map.put(blockers.get(0), damageDealt);
            }
        }
        return map;
    }
    
    private final boolean assignDamageAsIfNotBlocked(Card attacker) {
        return attacker.hasKeyword("CARDNAME assigns its combat damage as though it weren't blocked.")
                || (attacker.hasKeyword("You may have CARDNAME assign its combat damage as though it weren't blocked.")
                && GuiDialog.confirm(attacker, "Do you want to assign its combat damage as though it weren't blocked?"));
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#announceRequirements(java.lang.String)
     */
    @Override
    public Integer announceRequirements(SpellAbility ability, String announce, boolean canChooseZero) {
        List<Object> options = new ArrayList<Object>();
        for(int i = canChooseZero ? 0 : 1; i < 10; i++)
            options.add(Integer.valueOf(i));
        options.add("Other amount");
        
        
        Object chosen = GuiChoose.oneOrNone("Choose " + announce + " for " + ability.getSourceCard().getName(), options);
        if (chosen instanceof Integer || chosen == null)
            return (Integer)chosen;

        String message = String.format("How much will you announce for %s?%s", announce, canChooseZero ? "" : " (X cannot be 0)");
        while(true){
            String str = JOptionPane.showInputDialog(JOptionPane.getRootFrame(), message, ability.getSourceCard().getName(), JOptionPane.QUESTION_MESSAGE);
            if (null == str) return null; // that is 'cancel'
            
            if(StringUtils.isNumeric(str)) {
                Integer val = Integer.valueOf(str);
                if (val == 0 && canChooseZero || val > 0) 
                    return val;
            }
            GuiDialog.message("You have to enter a valid number", "Announce value");
        }
    }


    @Override
    public List<Card> choosePermanentsToSacrifice(SpellAbility sa, int min, int max, List<Card> valid, String message) {
        String outerMessage = "Select %d " + message + "(s) to sacrifice";
        return choosePermanentsTo(min, max, valid, outerMessage); 
    }

    @Override
    public List<Card> choosePermanentsToDestroy(SpellAbility sa, int min, int max, List<Card> valid, String message) {
        String outerMessage = "Select %d " + message + "(s) to be destroyed";
        return choosePermanentsTo(min, max, valid, outerMessage); 
    }

    private List<Card> choosePermanentsTo(int min, int max, List<Card> valid, String outerMessage) {
        max = Math.min(max, valid.size());
        if (max <= 0)
            return new ArrayList<Card>();

        InputSelectCards inp = new InputSelectCardsFromList(min == 0 ? 1 : min, max, valid);
        inp.setMessage(outerMessage);
        inp.setCancelAllowed(min == 0);
        Singletons.getControl().getInputQueue().setInputAndWait(inp);
        return inp.hasCancelled() ? Lists.<Card>newArrayList() : inp.getSelected();
    }


    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseCardsForEffect(java.util.Collection, forge.card.spellability.SpellAbility, java.lang.String, int, boolean)
     */
    @Override
    public List<Card> chooseCardsForEffect(List<Card> sourceList, SpellAbility sa, String title, int amount,
            boolean isOptional) {
        // If only one card to choose, use a dialog box. 
        // Otherwise, use the order dialog to be able to grab multiple cards in one shot 
        if (amount == 1) {
            return Lists.newArrayList(chooseSingleCardForEffect(sourceList, sa, title, isOptional));
        }
        
        GuiUtils.setPanelSelection(sa.getSourceCard());
        int remaining = isOptional ? -1 : Math.max(sourceList.size() - amount, 0);
        return GuiChoose.order(title, "Chosen Cards", remaining, sourceList, null, sa.getSourceCard());
    }
    
    @Override
    public Card chooseSingleCardForEffect(Collection<Card> options, SpellAbility sa, String title, boolean isOptional) {
        // Human is supposed to read the message and understand from it what to choose
        if (options.isEmpty())
            return null;
        if ( !isOptional && options.size() == 1 )
            return Iterables.getFirst(options, null);
        
        boolean canUseSelectCardsInput = true;
        for(Card c : options) {
            Zone cz = c.getZone();
            // can point at cards in own hand and anyone's battlefield
            boolean canUiPointAtCards = cz != null && ( cz.is(ZoneType.Hand) && cz.getPlayer() == player || cz.is(ZoneType.Battlefield));
            if ( !canUiPointAtCards ) {
                canUseSelectCardsInput = false;
                break;
            }
        }

        if ( canUseSelectCardsInput ) {
            InputSelectCardsFromList input = new InputSelectCardsFromList(isOptional ? 0 : 1, 1, options);
            input.setCancelAllowed(isOptional);
            input.setMessage(title);
            Singletons.getControl().getInputQueue().setInputAndWait(input);
            return Iterables.getFirst(input.getSelected(), null);
        }
        
        return isOptional ? GuiChoose.oneOrNone(title, options) : GuiChoose.one(title, options);
    }

    @Override
    public int chooseNumber(SpellAbility sa, String title, int min, int max) {
        final Integer[] choices = new Integer[max + 1];
        for (int i = min; i <= max; i++) {
            choices[i] = Integer.valueOf(i);
        }
        return GuiChoose.one(title, choices).intValue();
    }

    @Override
    public Player chooseSinglePlayerForEffect(List<Player> options, SpellAbility sa, String title) {
        // Human is supposed to read the message and understand from it what to choose
        return options.size() < 2 ? options.get(0) : GuiChoose.one(title, options);
    }

    @Override
    public SpellAbility chooseSingleSpellForEffect(java.util.List<SpellAbility> spells, SpellAbility sa, String title) {
        // Human is supposed to read the message and understand from it what to choose
        return spells.size() < 2 ? spells.get(0) : GuiChoose.one(title, spells);
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#confirmAction(forge.card.spellability.SpellAbility, java.lang.String, java.lang.String)
     */
    @Override
    public boolean confirmAction(SpellAbility sa, PlayerActionConfirmMode mode, String message) {
        return GuiDialog.confirm(sa.getSourceCard(), message);
    }

    @Override
    public boolean confirmStaticApplication(Card hostCard, GameEntity affected, String logic, String message) {
        return GuiDialog.confirm(hostCard, message);
    }

    @Override
    public boolean getWillPlayOnFirstTurn(boolean isFirstGame) {
        InputPlayOrDraw inp = new InputPlayOrDraw(player, isFirstGame);
        Singletons.getControl().getInputQueue().setInputAndWait(inp);
        return inp.isPlayingFirst();
    }

    @Override
    public List<Card> orderBlockers(Card attacker, List<Card> blockers) {
        GuiUtils.setPanelSelection(attacker);
        return GuiChoose.order("Choose Damage Order for " + attacker, "Damaged First", 0, blockers, null, attacker);
    }

    @Override
    public List<Card> orderAttackers(Card blocker, List<Card> attackers) {
        GuiUtils.setPanelSelection(blocker);
        return GuiChoose.order("Choose Damage Order for " + blocker, "Damaged First", 0, attackers, null, blocker);
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#reveal(java.lang.String, java.util.List, forge.game.zone.ZoneType, forge.game.player.Player)
     */
    @Override
    public void reveal(String string, Collection<Card> cards, ZoneType zone, Player owner) {
        String message = string;
        if ( StringUtils.isBlank(message) ) 
            message = String.format("Looking at %s's %s", owner, zone);
        GuiChoose.oneOrNone(message, cards);
    }

    @Override
    public ImmutablePair<List<Card>, List<Card>> arrangeForScry(List<Card> topN) {
        List<Card> toBottom = null;
        List<Card> toTop = null;
        
        if (topN.size() == 1) {
            if (willPutCardOnTop(topN.get(0)))
                toTop = topN;
            else 
                toBottom = topN;
        } else { 
            toBottom = GuiChoose.order("Select cards to be put on the bottom of your library", "Cards to put on the bottom", -1, topN, null, null);
            topN.removeAll(toBottom);
            if ( topN.isEmpty() )
                toTop = null;
            else if ( topN.size() == 1 )
                toTop = topN;
            else
                toTop = GuiChoose.order("Arrange cards to be put on top of your library", "Cards arranged", 0, topN, null, null);
        }
        return ImmutablePair.of(toTop, toBottom);
    }


    @Override
    public boolean willPutCardOnTop(Card c) {
        return GuiDialog.confirm(c, "Where will you put " + c.getName() + " in your library", new String[]{"Top", "Bottom"} );
    }

    @Override
    public List<Card> orderMoveToZoneList(List<Card> cards, ZoneType destinationZone) {
        switch (destinationZone) {
            case Library:
                return GuiChoose.order("Choose order of cards to put into the library", "Closest to top", 0, cards, null, null);
            case Battlefield:
                return GuiChoose.order("Choose order of cards to put onto the battlefield", "Put first", 0, cards, null, null);
            case Graveyard:
                return GuiChoose.order("Choose order of cards to put into the graveyard", "Closest to bottom", 0, cards, null, null);
            case PlanarDeck:
                return GuiChoose.order("Choose order of cards to put into the planar deck", "Closest to top", 0, cards, null, null);
            case SchemeDeck:
                return GuiChoose.order("Choose order of cards to put into the scheme deck", "Closest to top", 0, cards, null, null);
            default:
                System.out.println("ZoneType " + destinationZone + " - Not Ordered");
                break;
        }
        return cards;
    }

    @Override
    public List<Card> chooseCardsToDiscardFrom(Player p, SpellAbility sa, List<Card> valid, int min, int max) {
        if ( p != player ) {
            int cntToKeepInHand =  min == 0 ? -1 : valid.size() - min;
            return GuiChoose.order("Choose cards to Discard", "Discarded", cntToKeepInHand, valid, null, null);
        }

        InputSelectCards inp = new InputSelectCardsFromList(min, max, valid);
        inp.setMessage(sa.hasParam("AnyNumber") ? "Discard up to %d card(s)" : "Discard %d card(s)");
        Singletons.getControl().getInputQueue().setInputAndWait(inp);
        return inp.getSelected();
    }

    @Override
    public Card chooseCardToDredge(List<Card> dredgers) {
        if (GuiDialog.confirm(null, "Do you want to dredge?", false)) {
            return GuiChoose.oneOrNone("Select card to dredge", dredgers);
        }
        return null;
    }

    @Override
    public void playMiracle(SpellAbility miracle, Card card) {
        if (GuiDialog.confirm(card, card + " - Drawn. Play for Miracle Cost?")) {
            HumanPlay.playSpellAbility(player, miracle);
        }
    }

    @Override
    public List<Card> chooseCardsToDelve(int colorLessAmount, List<Card> grave) {
        List<Card> toExile = new ArrayList<Card>();
        int cardsInGrave = grave.size();
        final Integer[] cntChoice = new Integer[cardsInGrave + 1];
        for (int i = 0; i <= cardsInGrave; i++) {
            cntChoice[i] = Integer.valueOf(i);
        }

        final Integer chosenAmount = GuiChoose.one("Exile how many cards?", cntChoice);
        System.out.println("Delve for " + chosenAmount);

        for (int i = 0; i < chosenAmount; i++) {
            final Card nowChosen = GuiChoose.oneOrNone("Exile which card?", grave);

            if (nowChosen == null) {
                // User canceled,abort delving.
                toExile.clear();
                break;
            }

            grave.remove(nowChosen);
            toExile.add(nowChosen);
        }
        return toExile;
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseTargets(forge.card.spellability.SpellAbility, forge.card.spellability.SpellAbilityStackInstance)
     */
    @Override
    public TargetChoices chooseNewTargetsFor(SpellAbility ability) {
        if (ability.getTargetRestrictions() == null) {
            return null;
        }
        TargetChoices oldTarget = ability.getTargets();
        TargetSelection select = new TargetSelection(ability);
        ability.resetTargets();
        if (select.chooseTargets(oldTarget.getNumTargeted())) {
            return ability.getTargets();
        } else {
            // Return old target, since we had to reset them above
            return oldTarget;
        }
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseCardsToDiscardUnlessType(int, java.lang.String, forge.card.spellability.SpellAbility)
     */
    @Override
    public List<Card> chooseCardsToDiscardUnlessType(int num, List<Card> hand, final String uType, SpellAbility sa) {
        final InputSelectCards target = new InputSelectCardsFromList(num, num, hand) {
            private static final long serialVersionUID = -5774108410928795591L;

            @Override
            protected boolean hasAllTargets() {
                for(Card c : selected) {
                    if (c.isType(uType))
                        return true;
                }
                return super.hasAllTargets();
            }
        };
        target.setMessage("Select %d card(s) to discard, unless you discard a " + uType + ".");
        Singletons.getControl().getInputQueue().setInputAndWait(target);
        return target.getSelected();
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseManaFromPool(java.util.List)
     */
    @Override
    public Mana chooseManaFromPool(List<Mana> manaChoices) {
        List<String> options = new ArrayList<String>();
        for(int i = 0; i < manaChoices.size(); i++) {
            Mana m = manaChoices.get(i);
            options.add(String.format("%d. %s mana from %s", 1+i, m.getColor(), m.getSourceCard() ));
        }
        String chosen = GuiChoose.one("Pay Mana from Mana Pool", options);
        String idx = TextUtil.split(chosen, '.')[0];
        return manaChoices.get(Integer.parseInt(idx)-1);
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseSomeType(java.lang.String, java.lang.String, java.util.List, java.util.List, java.lang.String)
     */
    @Override
    public String chooseSomeType(String kindOfType, String aiLogic, List<String> validTypes, List<String> invalidTypes) {
        return GuiChoose.one("Choose a " + kindOfType.toLowerCase() + " type", validTypes);
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#confirmReplacementEffect(forge.card.replacement.ReplacementEffect, forge.card.spellability.SpellAbility, java.lang.String)
     */
    @Override
    public boolean confirmReplacementEffect(ReplacementEffect replacementEffect, SpellAbility effectSA, String question) {
        return GuiDialog.confirm(replacementEffect.getHostCard(), question);
    }


    @Override
    public List<Card> getCardsToMulligan(boolean isCommander, Player firstPlayer) {
        final InputConfirmMulligan inp = new InputConfirmMulligan(player, firstPlayer, isCommander);
        Singletons.getControl().getInputQueue().setInputAndWait(inp);
        return inp.isKeepHand() ? null : isCommander ? inp.getSelectedCards() : player.getCardsIn(ZoneType.Hand);
    }

    @Override
    public void declareAttackers(Player attacker, Combat combat) {
        // This input should not modify combat object itself, but should return user choice
        InputAttack inpAttack = new InputAttack(attacker, player, combat);
        Singletons.getControl().getInputQueue().setInputAndWait(inpAttack);
    }

    @Override
    public void declareBlockers(Player defender, Combat combat) {
        // This input should not modify combat object itself, but should return user choice
        InputBlock inpBlock = new InputBlock(player, defender, combat);
        Singletons.getControl().getInputQueue().setInputAndWait(inpBlock);
    }

    @Override
    public void takePriority() {
        PhaseType phase = game.getPhaseHandler().getPhase();
        boolean maySkipPriority = mayAutoPass(phase) || isUiSetToSkipPhase(game.getPhaseHandler().getPlayerTurn(), phase);
        if (game.getStack().isEmpty() && maySkipPriority) {
            return;
        } else
            autoPassCancel(); // probably cancel, since something has happened
        
        SpellAbility chosenSa = null;
        do {
            if (chosenSa != null) {
                HumanPlay.playSpellAbility(player, chosenSa);
            }
            InputPassPriority defaultInput = new InputPassPriority(player);
            Singletons.getControl().getInputQueue().setInputAndWait(defaultInput);
            chosenSa = defaultInput.getChosenSa();
        } while( chosenSa != null );
    }

    @Override
    public List<Card> chooseCardsToDiscardToMaximumHandSize(int nDiscard) {
        final int n = player.getZone(ZoneType.Hand).size();
        final int max = player.getMaxHandSize();

        InputSelectCardsFromList inp = new InputSelectCardsFromList(nDiscard, nDiscard, player.getZone(ZoneType.Hand).getCards());
        String msgFmt = "Cleanup Phase: You can only have a maximum of %d cards, you currently have %d  cards in your hand - select %d card(s) to discard";
        String message = String.format(msgFmt, max, n, nDiscard);
        inp.setMessage(message);
        inp.setCancelAllowed(false);
        Singletons.getControl().getInputQueue().setInputAndWait(inp);
        return inp.getSelected();
    }


    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseCardsToRevealFromHand(int, int, java.util.List)
     */
    @Override
    public List<Card> chooseCardsToRevealFromHand(int min, int max, List<Card> valid) {
        max = Math.min(max, valid.size());
        min = Math.min(min, max);
        InputSelectCardsFromList inp = new InputSelectCardsFromList(min, max, valid);
        inp.setMessage("Choose Which Cards to Reveal");
        Singletons.getControl().getInputQueue().setInputAndWait(inp);
        return inp.getSelected();
    }


    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#payManaOptional(forge.Card, forge.card.cost.Cost)
     */
    @Override
    public boolean payManaOptional(Card c, Cost cost, SpellAbility sa, String prompt, ManaPaymentPurpose purpose) {
        return HumanPlay.payCostDuringAbilityResolve(player, c, cost, sa, prompt);
    }


    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseSaToActivateFromOpeningHand(java.util.List)
     */
    @Override
    public List<SpellAbility> chooseSaToActivateFromOpeningHand(List<SpellAbility> usableFromOpeningHand) {
        List<Card> srcCards = new ArrayList<Card>();
        for(SpellAbility sa : usableFromOpeningHand) {
            srcCards.add(sa.getSourceCard());
        }
        List<SpellAbility> result = new ArrayList<SpellAbility>();
        if (srcCards.isEmpty()) {
            return result;
        }
        List<Card> chosen = GuiChoose.order("Choose cards to activate from opening hand", "Activate first", -1, srcCards, null, null);
        for(Card c : chosen) {
            for(SpellAbility sa : usableFromOpeningHand) {
                if ( sa.getSourceCard() == c ) {
                    result.add(sa);
                    break;
                }
            }
        }
        return result;
    }

    // end of not related candidates for move.
    
    
    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseBinary(java.lang.String, boolean)
     */
    @Override
    public boolean chooseBinary(SpellAbility sa, String question, boolean isCoin) {
        String[] labels = isCoin ? new String[]{"Heads", "Tails"} : new String[]{"Odds", "Evens"};
        return GuiDialog.confirm(sa.getSourceCard(), question, labels);
    }


    @Override
    public boolean chooseFlipResult(SpellAbility sa, Player flipper, boolean[] results, boolean call) {
        String[] labelsSrc = call ? new String[]{"heads", "tails"} : new String[]{"win the flip", "lose the flip"};
        String[] strResults = new String[results.length];
        for(int i = 0; i < results.length; i++) {
            strResults[i] = labelsSrc[results[i] ? 0 : 1];
        }
        return GuiChoose.one(sa.getSourceCard().getName() + " - Choose a result", strResults) == labelsSrc[0];
    }

    @Override
    public Card chooseProtectionShield(GameEntity entityBeingDamaged, List<String> options, Map<String, Card> choiceMap) {
        String title = entityBeingDamaged + " - select which prevention shield to use";
        return choiceMap.get(GuiChoose.one(title, options));
    }

    @Override
    public Pair<CounterType,String> chooseAndRemoveOrPutCounter(Card cardWithCounter) {
        if (!cardWithCounter.hasCounters()) {
            System.out.println("chooseCounterType was reached with a card with no counters on it. Consider filtering this card out earlier");
            return null;
        }

        String counterChoiceTitle = "Choose a counter type on " + cardWithCounter;
        final CounterType chosen = GuiChoose.one(counterChoiceTitle, cardWithCounter.getCounters().keySet());

        String putOrRemoveTitle = "Do what with counter " + chosen.getName() + "counter ";
        final String putString = "Put another " + chosen.getName() + " counter on " + cardWithCounter;
        final String removeString = "Remove a " + chosen.getName() + " counter from " + cardWithCounter;
        final String addOrRemove = GuiChoose.one(putOrRemoveTitle, new String[]{putString,removeString});

        return new ImmutablePair<CounterType,String>(chosen,addOrRemove);
    }


    @Override
    public Pair<SpellAbilityStackInstance, ITargetable> chooseTarget(SpellAbility saSpellskite, List<Pair<SpellAbilityStackInstance, ITargetable>> allTargets) {
        if( allTargets.size() < 2)
            return Iterables.getFirst(allTargets, null);
        
        final Function<Pair<SpellAbilityStackInstance, ITargetable>, String> fnToString = new Function<Pair<SpellAbilityStackInstance, ITargetable>, String>() {
            @Override
            public String apply(Pair<SpellAbilityStackInstance, ITargetable> targ) {
                return targ.getRight().toString() + " - " + targ.getLeft().getStackDescription();
            }
        };
        
        List<Pair<SpellAbilityStackInstance, ITargetable>> chosen = GuiChoose.getChoices(saSpellskite.getSourceCard().getName(), 1, 1, allTargets, null, fnToString);
        return Iterables.getFirst(chosen, null);
    }

    @Override
    public void notifyOfValue(SpellAbility sa, ITargetable realtedTarget, String value) {
        String message = formatNotificationMessage(sa, realtedTarget, value); 
        GuiDialog.message(message, sa.getSourceCard().getName());
    }


    // These are not much related to PlayerController
    private String formatNotificationMessage(SpellAbility sa, ITargetable target, String value) {
        if (sa.getApi() == null) {
            return ("Result: " + value);
        }
        switch(sa.getApi()) {
            case ChooseNumber:
                final boolean random = sa.hasParam("Random");
                return String.format(random ? "Randomly chosen number for %s is %s" : "%s choses number: %s", mayBeYou(target, player), value);
            case FlipACoin:
                String flipper = StringUtils.capitalize(mayBeYou(target, player));
                return sa.hasParam("NoCall") 
                        ? String.format("%s flip comes up %s", Lang.getPossesive(flipper), value) 
                        : String.format("%s %s the flip", flipper, Lang.joinVerb(flipper, value));
            case Protection:
                String choser = StringUtils.capitalize(mayBeYou(target, player));
                return String.format("%s %s protection from %s", choser, Lang.joinVerb(choser, "choose"), value);
            default:
                return String.format("%s effect's value for %s is %s", sa.getSourceCard().getName(), mayBeYou(target, player), value);
        }
    }
    
    private String mayBeYou(ITargetable what, Player you) {
        return what == you ? "you" : what.toString();
    }
    // end of not related candidates for move.


    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#chooseModeForAbility(forge.card.spellability.SpellAbility, java.util.List, int, int)
     */
    @Override
    public List<AbilitySub> chooseModeForAbility(SpellAbility sa, int min, int num) {
        List<AbilitySub> choices = CharmEffect.makePossibleOptions(sa);
        String modeTitle = String.format("%s activated %s - Choose a mode", sa.getActivatingPlayer(), sa.getSourceCard());
        List<AbilitySub> chosen = new ArrayList<AbilitySub>();
        for (int i = 0; i < num; i++) {
            AbilitySub a;
            if (i < min) {
                a = GuiChoose.one(modeTitle, choices);
            } else {
                a = GuiChoose.oneOrNone(modeTitle, choices);
            }
            if (null == a) {
                break;
            }

            choices.remove(a);
            chosen.add(a);
        }
        return chosen;
    }

    @Override
    public String chooseSingleColor(ImmutableList<String> names) {
        return GuiChoose.one("Choose a color:", names);
    }

    @Override
    public String chooseHybridMana(String s) {
        return GuiChoose.one("Choose a type", s.split("/"));
    }
}
