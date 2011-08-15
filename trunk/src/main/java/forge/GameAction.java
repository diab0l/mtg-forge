package forge;


import forge.card.abilityFactory.AbilityFactory;
import forge.card.abilityFactory.AbilityFactory_Attach;
import forge.card.cardFactory.CardFactoryInterface;
import forge.card.cardFactory.CardFactoryUtil;
import forge.card.mana.ManaCost;
import forge.card.mana.ManaPool;
import forge.card.spellability.*;
import forge.card.staticAbility.StaticAbility;
import forge.card.trigger.Trigger;
import forge.deck.Deck;
import forge.gui.GuiUtils;
import forge.gui.input.Input_Mulligan;
import forge.gui.input.Input_PayManaCost;
import forge.gui.input.Input_PayManaCost_Ability;
import forge.properties.ForgeProps;
import forge.properties.NewConstants.LANG.GameAction.GAMEACTION_TEXT;

import javax.swing.*;
import java.util.*;

/**
 * <p>GameAction class.</p>
 *
 * @author Forge
 * @version $Id$
 */
public class GameAction {

    /**
     * <p>resetActivationsPerTurn.</p>
     */
    public void resetActivationsPerTurn() {
        CardList all = AllZoneUtil.getCardsInGame();

        // Reset Activations per Turn
        for (Card card : all) {
            for (SpellAbility sa : card.getSpellAbility())
                sa.getRestrictions().resetTurnActivations();
        }
    }

    /**
     * <p>changeZone.</p>
     *
     * @param prev a {@link forge.PlayerZone} object.
     * @param zone a {@link forge.PlayerZone} object.
     * @param c    a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public static Card changeZone(PlayerZone prev, PlayerZone zone, Card c) {
        if (prev == null && !c.isToken()) {
            zone.add(c);
            return c;
        }

        boolean suppress;
        if (prev == null && !c.isToken()) {
            suppress = true;
        } else if (c.isToken()) {
            suppress = false;
        } else {
            suppress = prev.equals(zone);
        }

        Card copied = null;

        // Don't copy Tokens, Cards staying in same zone, or cards entering Battlefield
        if (c.isToken() || suppress || zone.is(Constant.Zone.Battlefield))
            copied = c;
        else {
            copied = AllZone.getCardFactory().copyCard(c);

            // todo: improve choices here
            // Certain attributes need to be copied from Hand->Stack and Stack->Battlefield
            if (c.wasSuspendCast())            // these probably can be moved back to SubtractCounters
                copied = addSuspendTriggers(c);
            copied.setUnearthed(c.isUnearthed());    // this might be unnecessary
        }

        // This is the fix for Isochron Scepter and friends, we need to test other situations
        // To make sure it doesn't break anything serious
        for (Trigger trigger : c.getTriggers())
            trigger.setHostCard(copied);

        if (suppress)
            AllZone.getTriggerHandler().suppressMode("ChangesZone");

        zone.add(copied);

        //Tokens outside the battlefield disappear immideately.
        if (copied.isToken() && !zone.is(Constant.Zone.Battlefield))
            zone.remove(copied);

        HashMap<String, Object> runParams = new HashMap<String, Object>();
        runParams.put("Card", copied);
        if (prev != null) {
            runParams.put("Origin", prev.getZoneName());
        } else {
            runParams.put("Origin", null);
        }
        runParams.put("Destination", zone.getZoneName());
        AllZone.getTriggerHandler().runTrigger("ChangesZone", runParams);
        //AllZone.getStack().chooseOrderOfSimultaneousStackEntryAll();

        if (suppress)
            AllZone.getTriggerHandler().clearSuppression("ChangesZone");

        if (prev != null) {
            if (prev.is(Constant.Zone.Battlefield) && c.isCreature())
                AllZone.getCombat().removeFromCombat(c);

            prev.remove(c);
        }
        
        //remove all counters from the card if destination is not the battlefield
        if (!zone.is(Constant.Zone.Battlefield))
            copied.clearCounters();
        
        copied.setTimestamp(AllZone.getNextTimestamp());

        return copied;
    }

    /**
     * <p>moveTo.</p>
     *
     * @param zone a {@link forge.PlayerZone} object.
     * @param c    a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public Card moveTo(PlayerZone zone, Card c) {
        // Ideally move to should never be called without a prevZone
        // Remove card from Current Zone, if it has one
        PlayerZone prev = AllZone.getZone(c);
        //String prevName = prev != null ? prev.getZoneName() : "";

        if (c.hasKeyword("If CARDNAME would leave the battlefield, exile it instead of putting it anywhere else.") &&
                !zone.is(Constant.Zone.Exile)) {
            PlayerZone removed = AllZone.getZone(Constant.Zone.Exile, c.getOwner());
            c.removeExtrinsicKeyword("If CARDNAME would leave the battlefield, exile it instead of putting it anywhere else.");
            return moveTo(removed, c);
        }

        //Card lastKnownInfo = c;

        c = changeZone(prev, zone, c);

        if (c.isAura() && zone.is(Constant.Zone.Battlefield) && (prev == null || !prev.is(Constant.Zone.Stack))) {
        	// TODO: Need a way to override this for Abilities that put Auras into play attached to things
        	AbilityFactory_Attach.attachAuraOnIndirectEnterBattlefield(c);
        }

        return c;
    }
    
    /**
     * <p>moveToPlayFromHand.</p>
     *
     * @param c    a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public Card moveToPlayFromHand(Card c) {
        //handles the case for Clone, etc where prev was null
    	
        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, c.getOwner());
        PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, c.getController());

        c = changeZone(hand, play, c);

        return c;
    }

     /*
    public void changeController(CardList list, Player oldController, Player newController) {
        if (oldController.equals(newController))
            return;

        // Consolidating this code for now. In the future I want moveTo to handle this garbage
        PlayerZone oldBattlefield = AllZone.getZone(Constant.Zone.Battlefield, oldController);
        PlayerZone newBattlefield = AllZone.getZone(Constant.Zone.Battlefield, newController);

        AllZone.getTriggerHandler().suppressMode("ChangesZone");
        ((PlayerZone_ComesIntoPlay) AllZone.getHumanBattlefield()).setTriggers(false);
        ((PlayerZone_ComesIntoPlay) AllZone.getComputerBattlefield()).setTriggers(false);
        //so "enters the battlefield" abilities don't trigger

        for (Card c : list) {
            int turnInZone = c.getTurnInZone();
            oldBattlefield.remove(c);
            c.setController(newController);
            newBattlefield.add(c);
            //set summoning sickness
            c.setSickness(true);
            c.setTurnInZone(turnInZone); // The number of turns in the zone should not change
            if (c.isCreature())
                AllZone.getCombat().removeFromCombat(c);
        }

        AllZone.getTriggerHandler().clearSuppression("ChangesZone");
        ((PlayerZone_ComesIntoPlay) AllZone.getHumanBattlefield()).setTriggers(true);
        ((PlayerZone_ComesIntoPlay) AllZone.getComputerBattlefield()).setTriggers(true);
    }*/

    /**
     *
     * @param c
     */
    public void controllerChange_ZoneCorrection(Card c)
    {
        System.out.println("Correcting zone for " + c.toString());
        PlayerZone oldBattlefield = AllZone.getZone(c);
        PlayerZone newBattlefield = AllZone.getZone("Battlefield",c.getController());

        if(oldBattlefield == null || newBattlefield == null)
        {
            return;
        }

        AllZone.getTriggerHandler().suppressMode("ChangesZone");
        ((PlayerZone_ComesIntoPlay) AllZone.getHumanBattlefield()).setTriggers(false);
        ((PlayerZone_ComesIntoPlay) AllZone.getComputerBattlefield()).setTriggers(false);

        int tiz = c.getTurnInZone();

        oldBattlefield.remove(c);
        newBattlefield.add(c);

        c.setTurnInZone(tiz);

        AllZone.getTriggerHandler().clearSuppression("ChangesZone");
        ((PlayerZone_ComesIntoPlay) AllZone.getHumanBattlefield()).setTriggers(true);
        ((PlayerZone_ComesIntoPlay) AllZone.getComputerBattlefield()).setTriggers(true);
    }

    /**
     * <p>moveToStack.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public Card moveToStack(Card c) {
        PlayerZone stack = AllZone.getZone(Constant.Zone.Stack, null);
        return moveTo(stack, c);
    }

    /**
     * <p>moveToGraveyard.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public Card moveToGraveyard(Card c) {
        final PlayerZone origZone = AllZone.getZone(c);
        final PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, c.getOwner());

        if (AllZoneUtil.isCardInPlay("Leyline of the Void", c.getOwner().getOpponent())) {
            return moveTo(AllZone.getZone(Constant.Zone.Exile, c.getOwner()), c);
        }

        if (c.getName().equals("Nissa's Chosen") && origZone.is(Constant.Zone.Battlefield)) {
            return moveToLibrary(c, -1);
        }

        if (c.hasKeyword("If CARDNAME would be put into a graveyard this turn, exile it instead.")) {
            return moveTo(AllZone.getZone(Constant.Zone.Exile, c.getOwner()), c);
        }

        if (c.hasKeyword("If CARDNAME is put into a graveyard this turn, its controller gets a poison counter.")) {
            c.getController().addPoisonCounters(1);
        }

        //must put card in OWNER's graveyard not controller's
        c = moveTo(grave, c);

        //Recover keyword
        if (c.isCreature()
                && origZone.is(Constant.Zone.Battlefield)) {
            for (final Card recoverable : AllZoneUtil.getPlayerGraveyard(c.getOwner())) {
                if (recoverable.hasStartOfKeyword("Recover")) {
                    SpellAbility abRecover = new Ability(recoverable, "0") {
                        @Override
                        public void resolve() {
                            AllZone.getGameAction().moveToHand(recoverable);
                        }

                        @Override
                        public String getStackDescription() {
                            StringBuilder SD = new StringBuilder(recoverable.getName());
                            SD.append(" - Recover.");

                            return SD.toString();
                        }
                    };

                    Command notPaid = new Command() {
                        private static final long serialVersionUID = 5812397026869965462L;

                        public void execute() {
                            AllZone.getGameAction().exile(recoverable);
                        }
                    };

                    abRecover.setCancelCommand(notPaid);
                    abRecover.setTrigger(true);

                    String recoverCost = recoverable.getKeyword().get(recoverable.getKeywordPosition("Recover")).split(":")[1];
                    Cost abCost = new Cost(recoverCost, recoverable.getName(), false);
                    abRecover.setPayCosts(abCost);

                    StringBuilder question = new StringBuilder("Recover ");
                    question.append(recoverable.getName());
                    question.append("(");
                    question.append(recoverable.getUniqueNumber());
                    question.append(")");
                    question.append("?");

                    boolean shouldRecoverForAI = false;
                    boolean shouldRecoverForHuman = false;

                    if (c.getOwner().isHuman()) {
                        shouldRecoverForHuman = GameActionUtil.showYesNoDialog(recoverable, question.toString());
                    } else if (c.getOwner().isComputer()) {
                        shouldRecoverForAI = ComputerUtil.canPayCost(abRecover);
                    }

                    if (shouldRecoverForHuman) {
                        AllZone.getStack().addSimultaneousStackEntry(abRecover);
                        //AllZone.getGameAction().playSpellAbility(abRecover);
                    } else if (shouldRecoverForAI) {
                        AllZone.getStack().addSimultaneousStackEntry(abRecover);
                        //ComputerUtil.playStack(abRecover);
                    }

                    if (!grave.hasChanged()) //If the controller declined Recovery or didn't pay the cost, exile the recoverable
                    {

                    }
                }
            }
        }
        return c;
    }

    /**
     * <p>moveToHand.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public Card moveToHand(Card c) {
        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, c.getOwner());
        return moveTo(hand, c);
    }

    /**
     * <p>moveToPlay.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public Card moveToPlay(Card c) {
        PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, c.getOwner());
        return moveTo(play, c);
    }

    /**
     * <p>moveToPlay.</p>
     *
     * @param c a {@link forge.Card} object.
     * @param p a {@link forge.Player} object.
     * @return a {@link forge.Card} object.
     */
    public Card moveToPlay(Card c, Player p) {
        // move to a specific player's Battlefield
        PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, p);
        return moveTo(play, c);
    }

    /**
     * <p>moveToBottomOfLibrary.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public Card moveToBottomOfLibrary(Card c) {
        return moveToLibrary(c, -1);
    }

    /**
     * <p>moveToLibrary.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public Card moveToLibrary(Card c) {
        return moveToLibrary(c, 0);
    }

    /**
     * <p>moveToLibrary.</p>
     *
     * @param c           a {@link forge.Card} object.
     * @param libPosition a int.
     * @return a {@link forge.Card} object.
     */
    public Card moveToLibrary(Card c, int libPosition) {
        PlayerZone p = AllZone.getZone(c);
        PlayerZone library = AllZone.getZone(Constant.Zone.Library, c.getOwner());

        if (c.hasKeyword("If CARDNAME would leave the battlefield, exile it instead of putting it anywhere else.")) {
            PlayerZone removed = AllZone.getZone(Constant.Zone.Exile, c.getOwner());
            c.removeExtrinsicKeyword("If CARDNAME would leave the battlefield, exile it instead of putting it anywhere else.");
            return moveTo(removed, c);
        }

        if (p != null) p.remove(c);


        if (c.isToken())
            return c;

        if (p != null && p.is(Constant.Zone.Battlefield))
            c = AllZone.getCardFactory().copyCard(c);

        c.clearCounters(); //remove all counters

        if (libPosition == -1 || libPosition > library.size())
            libPosition = library.size();

        library.add(c, libPosition);
        return c;
    }

    /**
     * <p>exile.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public Card exile(Card c) {
        if (AllZoneUtil.isCardExiled(c)) return c;

        PlayerZone removed = AllZone.getZone(Constant.Zone.Exile, c.getOwner());

        return AllZone.getGameAction().moveTo(removed, c);
    }

    /**
     * <p>moveTo.</p>
     *
     * @param name        a {@link java.lang.String} object.
     * @param c           a {@link forge.Card} object.
     * @param libPosition a int.
     * @return a {@link forge.Card} object.
     */
    public Card moveTo(String name, Card c, int libPosition) {
        // Call specific functions to set PlayerZone, then move onto moveTo
        if (name.equals(Constant.Zone.Hand))
            return moveToHand(c);
        else if (name.equals(Constant.Zone.Library))
            return moveToLibrary(c, libPosition);
        else if (name.equals(Constant.Zone.Battlefield))
            return moveToPlay(c);
        else if (name.equals(Constant.Zone.Graveyard))
            return moveToGraveyard(c);
        else if (name.equals(Constant.Zone.Exile))
            return exile(c);
        else //if (name.equals(Constant.Zone.Stack))
            return moveToStack(c);
    }

    /**
     * <p>AI_discardNumType.</p>
     *
     * @param numDiscard a int.
     * @param uTypes     an array of {@link java.lang.String} objects.
     * @param sa         a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public boolean AI_discardNumType(int numDiscard, String[] uTypes, SpellAbility sa) {
        CardList hand = AllZoneUtil.getPlayerHand(AllZone.getComputerPlayer());
        CardList tHand = hand.getValidCards(uTypes, sa.getActivatingPlayer(), sa.getSourceCard());

        if (tHand.size() >= numDiscard) {
            CardListUtil.sortCMC(tHand);
            tHand.reverse();
            for (int i = 0; i < numDiscard; i++)
                tHand.get(i).getController().discard(tHand.get(i), sa);
            return true;
        }
        return false;
    }

    /**
     * <p>discard_PutIntoPlayInstead.</p>
     *
     * @param c a {@link forge.Card} object.
     */
    public void discard_PutIntoPlayInstead(Card c) {
        moveToPlay(c);

        if (c.getName().equals("Dodecapod"))
            c.setCounter(Counters.P1P1, 2, false);
    }

    /**
     * <p>discard_madness.</p>
     *
     * @param c a {@link forge.Card} object.
     */
    public void discard_madness(Card c) {
        // Whenever a card with madness is discarded, you may cast it for it's madness cost
        if (!c.hasMadness())
            return;

        final Card madness = c;
        final Ability cast = new Ability(madness, madness.getMadnessCost()) {
            @Override
            public void resolve() {
                playCardNoCost(madness);
                System.out.println("Madness cost paid");
            }
        };

        StringBuilder sb = new StringBuilder();
        sb.append(madness.getName()).append(" - Cast via Madness");
        cast.setStackDescription(sb.toString());

        final Ability activate = new Ability(madness, "0") {
            @Override
            public void resolve() {
                // pay madness cost here.
                if (madness.getOwner().isHuman()) {
                    if (GameActionUtil.showYesNoDialog(madness, madness + " - Discarded. Pay Madness Cost?")) {
                        if (cast.getManaCost().equals("0"))
                            AllZone.getStack().add(cast);
                        else
                            AllZone.getInputControl().setInput(new Input_PayManaCost(cast));
                    }
                } else     // computer will ALWAYS pay a madness cost if he has the mana.
                    ComputerUtil.playStack(cast);
            }
        };

        StringBuilder sbAct = new StringBuilder();
        sbAct.append(madness.getName()).append(" - Discarded. Pay Madness Cost?");
        activate.setStackDescription(sbAct.toString());

        AllZone.getStack().add(activate);
    }

    /**
     * <p>checkEndGameSate.</p>
     *
     * @return a boolean.
     */
    public boolean checkEndGameSate() {
        // Win / Lose
        boolean humanWins = false;
        boolean computerWins = false;
        Player computer = AllZone.getComputerPlayer();
        Player human = AllZone.getHumanPlayer();

        int gameWon = Constant.Runtime.matchState.getWin();

        if (human.hasWon()) {    // Winning Conditions can be worth more than losing conditions
            // Human wins
            humanWins = true;
            if (human.getAltWin()) {
                Constant.Runtime.matchState.setWinMethod(gameWon, human.getWinCondition());
            }
            Constant.Runtime.matchState.addWin();
        } else if (computer.hasLost()) {
            // Human wins
            humanWins = true;

            if (computer.getAltLose()) {
                Constant.Runtime.matchState.setWinMethod(gameWon, computer.getLoseCondition());
            }
            Constant.Runtime.matchState.addWin();
        }


        if (computer.hasWon() || human.hasLost()) {
            if (humanWins) {
                // both players won/lost at the same time.
                // TODO: Handle a Draw here
            }

            // Computer wins
            computerWins = true;
            Constant.Runtime.matchState.addLose();
        }

        return humanWins || computerWins;
    }


    /**
     * <p>checkStateEffects.</p>
     */
    public void checkStateEffects() {
        // sol(10/29) added for Phase updates, state effects shouldn't be checked during Spell Resolution
        if (AllZone.getStack().getResolving())
            return;

        boolean refreeze = AllZone.getStack().isFrozen();
        AllZone.getStack().setFrozen(true);

        JFrame frame = (JFrame) AllZone.getDisplay();
        if (!frame.isDisplayable()) return;

        if (checkEndGameSate()) {
            AllZone.getDisplay().savePrefs();
            frame.dispose();
            new Gui_WinLose();
            return;
        }

        //do this twice, sometimes creatures/permanents will survive when they shouldn't
        for (int q = 0; q < 9; q++) {
        	
            boolean checkAgain = false;
        	
        	//remove old effects
        	AllZone.getStaticEffects().clearStaticEffects();
        	
        	//search for cards with static abilities
        	CardList allCards = AllZoneUtil.getCardsInGame();
        	CardList cardsWithStAbs = new CardList();
        	for (Card card : allCards) {
        		ArrayList<StaticAbility> staticAbilities = card.getStaticAbilities();
        		if (!staticAbilities.isEmpty())
        			cardsWithStAbs.add(card);
        	}
        	
        	//apply continuous effects
        	for (int layer = 4; layer < 9; layer++) {
	        	for (Card card : cardsWithStAbs) {
	        		ArrayList<StaticAbility> staticAbilities = card.getStaticAbilities();
	        		for (StaticAbility stAb : staticAbilities)
	        			if (stAb.getLayer() == layer)
	        				stAb.applyAbility("Continuous");
	        	}
        	}
        	
        	HashMap<String, Object> runParams = new HashMap<String, Object>();
        	AllZone.getTriggerHandler().runTrigger("Always", runParams);

            //card state effects like Glorious Anthem
            for (String effect : AllZone.getStaticEffects().getStateBasedMap().keySet()) {
                Command com = GameActionUtil.commands.get(effect);
                com.execute();
            }

            GameActionUtil.stAnimate.execute();

            CardList list = AllZoneUtil.getCardsInPlay();
            Card c;

            Iterator<Card> it = list.iterator();

            while (it.hasNext()) {
                c = it.next();

                if (c.isEquipped()) {
                    for (int i = 0; i < c.getEquippedBy().size(); i++) {
                        Card equipment = c.getEquippedBy().get(i);
                        if (!AllZoneUtil.isCardInPlay(equipment)) {
                            equipment.unEquipCard(c);
                            checkAgain = true;
                        }
                    }
                }//if isEquipped()

                if (c.isEquipping()) {
                    Card equippedCreature = c.getEquipping().get(0);
                    if (!AllZoneUtil.isCardInPlay(equippedCreature)) {
                        c.unEquipCard(equippedCreature);
                        checkAgain = true;
                    }

                    //make sure any equipment that has become a creature stops equipping
                    if (c.isCreature()) {
                        c.unEquipCard(equippedCreature);
                        checkAgain = true;
                    }
                }//if isEquipping()

                if (c.isAura()) {
                	// Check if Card Aura is attached to is a legal target
                    for (int i = 0; i < c.getEnchanting().size(); i++) {
                        Card perm = c.getEnchanting().get(i);
                        
                        SpellAbility sa = c.getSpellPermanent();
                        Target tgt = null;
                        if (sa != null)
                        	tgt = sa.getTarget();
                        
                        // I think the Keyword checks might be superfluous with the isValidCard check
                        if (!AllZoneUtil.isCardInPlay(perm)
                                || CardFactoryUtil.hasProtectionFrom(c, perm)
                                || ((c.hasKeyword("Enchant creature") || c.hasKeyword("Enchant tapped creature"))
                                && !perm.isCreature())
                                || (c.hasKeyword("Enchant tapped creature") && perm.isUntapped())
                                || (tgt != null && !perm.isValidCard(tgt.getValidTgts(), c.getController(), c)) ) {
       
                            c.unEnchantCard(perm);
                            moveToGraveyard(c);
                            checkAgain = true;
                        }
                    }
                }//if isAura

                if (c.isCreature()){
                	if (c.getNetDefense() <= c.getDamage() && !c.hasKeyword("Indestructible")) {
	                    destroy(c);
	                    AllZone.getCombat().removeFromCombat(c); //this is untested with instants and abilities but required for First Strike combat phase
	                    checkAgain = true;
	                } else if (c.getNetDefense() <= 0) {
	                	// TODO: This shouldn't be a destroy, and should happen before the damage check probably
	                    destroy(c);
	                    AllZone.getCombat().removeFromCombat(c);
	                    checkAgain = true;
	                }
                }

            }//while it.hasNext()

            if (!checkAgain) break; //do not continue the loop

        }//for q=0;q<2

        destroyLegendaryCreatures();
        destroyPlaneswalkers();

        GameActionUtil.stLandManaAbilities.execute();

        if (!refreeze)
            AllZone.getStack().unfreezeStack();
    }//checkStateEffects()


    /**
     * <p>destroyPlaneswalkers.</p>
     */
    private void destroyPlaneswalkers() {
        //get all Planeswalkers
        CardList list = AllZoneUtil.getTypeInPlay("Planeswalker");

        Card c;
        for (int i = 0; i < list.size(); i++) {
            c = list.get(i);

            if (c.getCounters(Counters.LOYALTY) <= 0) AllZone.getGameAction().moveToGraveyard(c);

            String subtype = c.getType().get(c.getType().size() - 1);
            CardList cl = list.getType(subtype);

            if (cl.size() > 1) {
                for (Card crd : cl) {
                    AllZone.getGameAction().moveToGraveyard(crd);
                }
            }
        }

    }

    /**
     * <p>destroyLegendaryCreatures.</p>
     */
    private void destroyLegendaryCreatures() {
        CardList a = AllZoneUtil.getTypeInPlay("Legendary");

        while (!a.isEmpty() && !AllZoneUtil.isCardInPlay("Mirror Gallery")) {
            CardList b = AllZoneUtil.getCardsInPlay(a.get(0).getName());
            a.remove(0);
            if (1 < b.size()) {
                for (int i = 0; i < b.size(); i++)
                    AllZone.getGameAction().sacrificeDestroy(b.get(i));
            }
        }
    }//destroyLegendaryCreatures()

    /**
     * <p>sacrifice.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a boolean.
     */
    public boolean sacrifice(Card c) {
        if (c.getName().equals("Mana Pool")) {
            System.out.println("Trying to sacrifice mana pool...");
            return false;
        }
        sacrificeDestroy(c);

        //Run triggers
        HashMap<String, Object> runParams = new HashMap<String, Object>();
        runParams.put("Card", c);
        AllZone.getTriggerHandler().runTrigger("Sacrificed", runParams);

        return true;
    }

    /**
     * <p>destroyNoRegeneration.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a boolean.
     */
    public boolean destroyNoRegeneration(Card c) {
        if (!AllZoneUtil.isCardInPlay(c) || c.hasKeyword("Indestructible")) return false;

        if (c.isEnchanted()) {
            CardList list = new CardList(c.getEnchantedBy().toArray());
            list = list.filter(new CardListFilter() {
                public boolean addCard(Card crd) {
                    return crd.hasKeyword("Totem armor");
                }
            });
            CardListUtil.sortCMC(list);

            if (list.size() != 0) {
                final Card crd;
                if (list.size() == 1) {
                    crd = list.get(0);
                } else {
                    if (c.getController().isHuman())
                        crd = GuiUtils.getChoiceOptional("Select totem armor to destroy", list.toArray());
                    else
                        crd = list.get(0);
                }

                final Card card = c;
                Ability_Static ability = new Ability_Static(crd, "0") {
                    public void resolve() {
                        destroy(crd);
                        card.setDamage(0);

                    }
                };

                StringBuilder sb = new StringBuilder();
                sb.append(crd).append(" - Totem armor: destroy this aura.");
                ability.setStackDescription(sb.toString());

                AllZone.getStack().add(ability);
                return false;
            }
        }//totem armor

        return sacrificeDestroy(c);
    }

    /**
     * <p>addSuspendTriggers.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a {@link forge.Card} object.
     */
    public static Card addSuspendTriggers(final Card c) {
        c.setSVar("HasteFromSuspend", "True");

        Command intoPlay = new Command() {
            private static final long serialVersionUID = -4514610171270596654L;

            public void execute() {
                if (AllZoneUtil.isCardInPlay(c) && c.isCreature())
                    c.addExtrinsicKeyword("Haste");
            }//execute()
        };

        c.addComesIntoPlayCommand(intoPlay);

        Command loseControl = new Command() {
            private static final long serialVersionUID = -4514610171270596654L;

            public void execute() {
                if (c.getSVar("HasteFromSuspend").equals("True")) {
                    c.setSVar("HasteFromSuspend", "False");
                    c.removeExtrinsicKeyword("Haste");
                }
            }//execute()
        };

        c.addChangeControllerCommand(loseControl);
        c.addLeavesPlayCommand(loseControl);
        return c;
    }

    /**
     * <p>sacrificeDestroy.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a boolean.
     */
    public boolean sacrificeDestroy(Card c) {
        if (!AllZoneUtil.isCardInPlay(c)) return false;

        Player owner = c.getOwner();
        if (!(owner.isComputer() || owner.isHuman()))
            throw new RuntimeException("GameAction : destroy() invalid card.getOwner() - " + c + " " + owner);

        boolean persist = (c.hasKeyword("Persist") && c.getCounters(Counters.M1M1) == 0) && !c.isToken();

        Card newCard = moveToGraveyard(c);

        // Destroy needs to be called with Last Known Information
        c.destroy();

        //System.out.println("Card " + c.getName() + " is getting sent to GY, and this turn it got damaged by: ");
        for (Card crd : c.getReceivedDamageFromThisTurn().keySet()) {
            if (c.getReceivedDamageFromThisTurn().get(crd) > 0) {
                //System.out.println(crd.getName() );
                GameActionUtil.executeVampiricEffects(crd);
            }
        }

        if (persist) {
            final Card persistCard = newCard;
            Ability persistAb = new Ability(persistCard, "0") {

                @Override
                public void resolve() {
                    if (AllZone.getZone(persistCard).is(Constant.Zone.Graveyard)) {
                        PlayerZone ownerPlay = AllZone.getZone(Constant.Zone.Battlefield, persistCard.getOwner());
                        Card card = moveTo(ownerPlay, persistCard);
                        card.addCounter(Counters.M1M1, 1);
                    }
                }
            };
            persistAb.setStackDescription(newCard.getName() + " - Returning from Persist");
            AllZone.getStack().add(persistAb);
        }
        return true;
    }//sacrificeDestroy()


    /**
     * <p>destroy.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a boolean.
     */
    public boolean destroy(Card c) {
        if (!AllZoneUtil.isCardInPlay(c)
                || (c.hasKeyword("Indestructible")
                && (!c.isCreature() || c.getNetDefense() > 0))) return false;

        if (c.canBeShielded() && c.getShield() > 0) {
            c.subtractShield();
            c.setDamage(0);
            c.tap();
            AllZone.getCombat().removeFromCombat(c);
            return false;
        }

        if (c.isEnchanted()) {
            CardList list = new CardList(c.getEnchantedBy().toArray());
            list = list.filter(new CardListFilter() {
                public boolean addCard(Card crd) {
                    return crd.hasKeyword("Totem armor");
                }
            });
            CardListUtil.sortCMC(list);


            if (list.size() != 0) {
                final Card crd;
                if (list.size() == 1) {
                    crd = list.get(0);
                } else {
                    if (c.getController().isHuman())
                        crd = GuiUtils.getChoiceOptional("Select totem armor to destroy", list.toArray());
                    else
                        crd = list.get(0);
                }

                c.setDamage(0);
                destroy(crd);
                System.out.println("Totem armor destroyed instead of original card");
                return false;
            }
        }//totem armor

        return sacrificeDestroy(c);
    }

    /**
     * <p>newGame.</p>
     * for Quest fantasy mode
     *
     * @param humanDeck    a {@link forge.deck.Deck} object.
     * @param computerDeck a {@link forge.deck.Deck} object.
     * @param human        a {@link forge.CardList} object.
     * @param humanLife    a int.
     * @param computerLife a int.
     * @param qa           a {@link forge.Quest_Assignment} object.
     * @param computer a {@link forge.CardList} object.
     */
    public void newGame(Deck humanDeck, Deck computerDeck, CardList human, CardList computer, int humanLife, int computerLife, Quest_Assignment qa) {
        this.newGame(humanDeck, computerDeck);

        AllZone.getComputerPlayer().setLife(computerLife, null);
        AllZone.getHumanPlayer().setLife(humanLife, null);

        if (qa != null) {
            computer.addAll(forge.quest.data.QuestUtil.getComputerCreatures(AllZone.getQuestData(), AllZone.getQuestAssignment()));
        }

        for (Card c : human) {
            for (Trigger trig : c.getTriggers()) {
                AllZone.getTriggerHandler().registerTrigger(trig);
            }

            AllZone.getHumanBattlefield().add(c);
            c.setSickness(true);
        }

        for (Card c : computer) {
            for (Trigger trig : c.getTriggers()) {
                AllZone.getTriggerHandler().registerTrigger(trig);
            }

            AllZone.getComputerBattlefield().add(c);
            c.setSickness(true);
        }
        Constant.Quest.fantasyQuest[0] = true;
    }

    private boolean Start_Cut = false;

    /**
     * <p>newGame.</p>
     *
     * @param humanDeck    a {@link forge.deck.Deck} object.
     * @param computerDeck a {@link forge.deck.Deck} object.
     */
    public void newGame(Deck humanDeck, Deck computerDeck) {
        //AllZone.getComputer() = new ComputerAI_Input(new ComputerAI_General());
        Constant.Quest.fantasyQuest[0] = false;

        AllZone.getGameInfo().setPreventCombatDamageThisTurn(false);
        AllZone.getGameInfo().setHumanNumberOfTimesMulliganed(0);
        AllZone.getGameInfo().setHumanMulliganedToZero(false);
        AllZone.getGameInfo().setComputerStartedThisGame(false);

        AllZone.getHumanPlayer().reset();
        AllZone.getComputerPlayer().reset();

        AllZone.getPhase().reset();
        AllZone.getStack().reset();
        AllZone.getCombat().reset();
        AllZone.getDisplay().showCombat("");
        AllZone.getDisplay().loadPrefs();

        AllZone.getHumanGraveyard().reset();
        AllZone.getHumanHand().reset();
        AllZone.getHumanLibrary().reset();
        AllZone.getHumanBattlefield().reset();
        AllZone.getHumanExile().reset();

        AllZone.getComputerGraveyard().reset();
        AllZone.getComputerHand().reset();
        AllZone.getComputerLibrary().reset();
        AllZone.getComputerBattlefield().reset();
        AllZone.getComputerExile().reset();

        AllZone.getInputControl().clearInput();

        AllZone.getStaticEffects().reset();

		// player.reset() now handles this
        //AllZone.getHumanPlayer().clearHandSizeOperations();
        //AllZone.getComputerPlayer().clearHandSizeOperations();
        
        AllZone.getTriggerHandler().clearRegistered();
        forge.card.trigger.Trigger.resetIDs();


        {//re-number cards just so their unique numbers are low, just for user friendliness
            CardFactoryInterface c = AllZone.getCardFactory();
            
            Card.resetUniqueNumber();

            Random generator = MyRandom.random;

            for (int i = 0; i < humanDeck.countMain(); i++) {
            	Card card = new Card();
            	
                String cardName = humanDeck.getMain(i);
                String setCode = "";
                if (cardName.contains("|")) {
                    String s[] = cardName.split("\\|", 2);
                    cardName = s[0];
                    setCode = s[1];
                }

                card = c.getCard(cardName, AllZone.getHumanPlayer());

                if (!setCode.equals(""))
                    card.setCurSetCode(setCode);
                else if ((card.getSets().size() > 0)) // && card.getCurSetCode().equals(""))
                    card.setRandomSetCode();

                if (!card.getCurSetCode().equals("")) {
                    int n = SetInfoUtil.getSetInfo_Code(card.getSets(), card.getCurSetCode()).PicCount;
                    if (n > 1)
                        card.setRandomPicture(generator.nextInt(n - 1) + 1);

                    card.setImageFilename(CardUtil.buildFilename(card));
                }
                
                // Assign random foiling on approximately 1:20 cards
                if (Constant.Runtime.RndCFoil[0] && Constant.Runtime.GameType[0].equals(Constant.GameType.Constructed)) {
                	if (MyRandom.percentTrue(5))
                		card.setFoil(MyRandom.random.nextInt(9) + 1);
                }

                //System.out.println("human random number:" + card.getRandomPicture());
                //}

                AllZone.getHumanLibrary().add(card);

                for (Trigger trig : card.getTriggers()) {
                    AllZone.getTriggerHandler().registerTrigger(trig);
                }
            }

            ArrayList<String> RAICards = new ArrayList<String>();
            for (int i = 0; i < computerDeck.countMain(); i++) {
            	Card card = new Card();
            	
                String cardName = computerDeck.getMain(i);
                String setCode = "";
                if (cardName.contains("|")) {
                    String s[] = cardName.split("\\|", 2);
                    cardName = s[0];
                    setCode = s[1];
                }

                card = c.getCard(cardName, AllZone.getComputerPlayer());

                //if(card.isBasicLand()) {
                //String PC = card.getSVar("PicCount");
                //int n = 0;
                //if (PC.matches("[0-9][0-9]?"))
                //	n = Integer.parseInt(PC);
                //if (n > 1)
                //    card.setRandomPicture(generator.nextInt(n));
                //System.out.println("computer random number:" + card.getRandomPicture());
                //}

                if (!setCode.equals(""))
                    card.setCurSetCode(setCode);
                else if ((card.getSets().size() > 0)) // && card.getCurSetCode().equals(""))
                    card.setRandomSetCode();

                if (!card.getCurSetCode().equals("")) {
                    int n = SetInfoUtil.getSetInfo_Code(card.getSets(), card.getCurSetCode()).PicCount;
                    if (n > 1)
                        card.setRandomPicture(generator.nextInt(n - 1) + 1);

                    card.setImageFilename(CardUtil.buildFilename(card));
                }

                // Assign random foiling on approximately 1:20 cards
                if (Constant.Runtime.RndCFoil[0] && Constant.Runtime.GameType[0].equals(Constant.GameType.Constructed)) {
                	if (MyRandom.percentTrue(5))
                		card.setFoil(MyRandom.random.nextInt(9) + 1);
                }
                
                AllZone.getComputerLibrary().add(card);

                for (Trigger trig : card.getTriggers()) {
                    AllZone.getTriggerHandler().registerTrigger(trig);
                }

                if (card.getSVar("RemAIDeck").equals("True"))
                    RAICards.add(card.getName());
                //get card picture so that it is in the image cache
                // ImageCache.getImage(card);
            }

            if (RAICards.size() > 0) {
                StringBuilder sb = new StringBuilder("AI deck contains the following cards that it can't play or may be buggy:\n");
                for (int i = 0; i < RAICards.size(); i++) {
                    sb.append(RAICards.get(i));
                    if (((i % 4) == 0) && (i > 0))
                        sb.append("\n");
                    else if (i != (RAICards.size() - 1))
                        sb.append(", ");
                }

                JOptionPane.showMessageDialog(null, sb.toString(), "", JOptionPane.INFORMATION_MESSAGE);

            }
        }//end re-numbering

        for (int i = 0; i < 100; i++)
            AllZone.getHumanPlayer().shuffle();

        //do this instead of shuffling Computer's deck
        boolean smoothLand = Constant.Runtime.Smooth[0];

        if (smoothLand) {
            Card[] c = smoothComputerManaCurve(AllZoneUtil.getPlayerCardsInLibrary(AllZone.getComputerPlayer()).toArray());
            AllZone.getComputerLibrary().setCards(c);
        } else {
            AllZone.getComputerLibrary().setCards(AllZoneUtil.getPlayerCardsInLibrary(AllZone.getComputerPlayer()).toArray());
            AllZone.getComputerPlayer().shuffle();
        }

        // Only cut/coin toss if it's the first game of the match
        if (Constant.Runtime.matchState.countWinLose() == 0) {
            // New code to determine who goes first. Delete this if it doesn't work properly
            if (isStartCut())
                seeWhoPlaysFirst();
            else
                seeWhoPlaysFirst_CoinToss();
        } else if (Constant.Runtime.matchState.didWinRecently())    // if player won last, AI starts
            computerStartsGame();

        for (int i = 0; i < 7; i++) {
            AllZone.getHumanPlayer().drawCard();
            AllZone.getComputerPlayer().drawCard();
        }

        // TODO: ManaPool should be moved to Player and be represented in the player panel
        ManaPool mp = AllZone.getManaPool();
        mp.setImageFilename("mana_pool");
        AllZone.getHumanBattlefield().add(mp);

        AllZone.getInputControl().setInput(new Input_Mulligan());
        Phase.setGameBegins(1);
    }//newGame()

    //this is where the computer cheats
    //changes AllZone.getComputerLibrary()

    /**
     * <p>smoothComputerManaCurve.</p>
     *
     * @param in an array of {@link forge.Card} objects.
     * @return an array of {@link forge.Card} objects.
     */
    Card[] smoothComputerManaCurve(Card[] in) {
        CardList library = new CardList(in);
        library.shuffle();

        //remove all land, keep non-basicland in there, shuffled
        CardList land = library.getType("Land");
        for (int i = 0; i < land.size(); i++)
            if (land.get(i).isLand()) library.remove(land.get(i));

        //non-basic lands are removed, because the computer doesn't seem to
        //effectively use them very well
        land = threadLand(land);

        try {
            //mana weave, total of 7 land
            //  The Following have all been reduced by 1, to account for the computer starting first.
            library.add(6, land.get(0));
            library.add(7, land.get(1));
            library.add(8, land.get(2));
            library.add(9, land.get(3));
            library.add(10, land.get(4));

            library.add(12, land.get(5));
            library.add(15, land.get(6));
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Error: cannot smooth mana curve, not enough land");
            return in;
        }

        //add the rest of land to the end of the deck
        for (int i = 0; i < land.size(); i++)
            if (!library.contains(land.get(i))) library.add(land.get(i));


        //check
        for (int i = 0; i < library.size(); i++)
            System.out.println(library.get(i));


        return library.toArray();
    }//smoothComputerManaCurve()

    //non-basic lands are removed, because the computer doesn't seem to
    //effectively used them very well

    /**
     * <p>threadLand.</p>
     *
     * @param in a {@link forge.CardList} object.
     * @return a {@link forge.CardList} object.
     */
    public CardList threadLand(CardList in) {
        //String[] basicLand = {"Forest", "Swamp", "Mountain", "Island", "Plains"}; //unused

        //Thread stuff with as large a spread of colors as possible:
        String[] allLand = {
                "Bayou", "Volcanic Island", "Savannah", "Badlands", "Tundra", "Taiga", "Underground Sea",
                "Plateau", "Tropical Island", "Scrubland", "Overgrown Tomb", "Steam Vents", "Temple Garden",
                "Blood Crypt", "Hallowed Fountain", "Stomping Ground", "Watery Grave", "Sacred Foundry",
                "Breeding Pool", "Godless Shrine", "Pendelhaven", "Flagstones of Trokair", "Forest", "Swamp",
                "Mountain", "Island", "Plains", "Tree of Tales", "Vault of Whispers", "Great Furnace",
                "Seat of the Synod", "Ancient Den", "Treetop Village", "Ghitu Encampment", "Faerie Conclave",
                "Forbidding Watchtower", "Savage Lands", "Arcane Sanctum", "Jungle Shrine",
                "Crumbling Necropolis", "Seaside Citadel", "Elfhame Palace", "Coastal Tower", "Salt Marsh",
                "Kher Keep", "Library of Alexandria", "Dryad Arbor"};


        ArrayList<CardList> land = new ArrayList<CardList>();

        //get different CardList of all Forest, Swamps, etc...
        CardList check;
        for (int i = 0; i < allLand.length; i++) {
            check = in.getName(allLand[i]);

            if (!check.isEmpty()) land.add(check);
        }
        /*
            //get non-basic land CardList
            check = in.filter(new CardListFilter()
            {
              public boolean addCard(Card c)
              {
                return c.isLand() && !c.isBasicLand();
              }
            });
            if(! check.isEmpty())
              land.add(check);
        */

        //thread all separate CardList's of land together to get something like
        //Mountain, Plains, Island, Mountain, Plains, Island
        CardList out = new CardList();

        int i = 0;
        while (!land.isEmpty()) {
            i = (i + 1) % land.size();

            check = land.get(i);
            if (check.isEmpty()) {
                //System.out.println("removed");
                land.remove(i);
                i--;
                continue;
            }

            out.add(check.get(0));
            check.remove(0);
        }//while

        return out;
    }//threadLand()


    /**
     * <p>getDifferentLand.</p>
     *
     * @param list a {@link forge.CardList} object.
     * @param land a {@link java.lang.String} object.
     * @return a int.
     */
    @SuppressWarnings("unused")
    // getDifferentLand
    private int getDifferentLand(CardList list, String land) {
        int out = 0;

        return out;
    }

    //decides who goes first when starting another game, used by newGame()

    /**
     * <p>seeWhoPlaysFirst_CoinToss.</p>
     */
    public void seeWhoPlaysFirst_CoinToss() {
        Object[] possibleValues = {ForgeProps.getLocalized(GAMEACTION_TEXT.HEADS), ForgeProps.getLocalized(GAMEACTION_TEXT.TAILS)};
        Object q = JOptionPane.showOptionDialog(null, ForgeProps.getLocalized(GAMEACTION_TEXT.HEADS_OR_TAILS), ForgeProps.getLocalized(GAMEACTION_TEXT.COIN_TOSS),
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, possibleValues, possibleValues[0]);

        int Flip = MyRandom.random.nextInt(2);
        String Human_Flip = " ";
        String Computer_Flip = " ";
        // JOptionPane.showMessageDialog(null, q, "", JOptionPane.INFORMATION_MESSAGE);
        if (q.equals(0)) {
            Human_Flip = ForgeProps.getLocalized(GAMEACTION_TEXT.HEADS);
            Computer_Flip = ForgeProps.getLocalized(GAMEACTION_TEXT.TAILS);
        } else {
            Human_Flip = ForgeProps.getLocalized(GAMEACTION_TEXT.TAILS);
            Computer_Flip = ForgeProps.getLocalized(GAMEACTION_TEXT.HEADS);
        }

        if ((Flip == 0 && q.equals(0)) || (Flip == 1 && q.equals(1)))
            JOptionPane.showMessageDialog(null, Human_Flip + "\r\n" + ForgeProps.getLocalized(GAMEACTION_TEXT.HUMAN_WIN), "", JOptionPane.INFORMATION_MESSAGE);
        else {
            computerStartsGame();
            JOptionPane.showMessageDialog(null, Computer_Flip + "\r\n" + ForgeProps.getLocalized(GAMEACTION_TEXT.COMPUTER_WIN), "", JOptionPane.INFORMATION_MESSAGE);
        }
    }//seeWhoPlaysFirst_CoinToss()

    private Card HumanCut = null;
    private Card ComputerCut = null;

    /**
     * <p>seeWhoPlaysFirst.</p>
     */
    public void seeWhoPlaysFirst() {

        CardList HLibrary = AllZoneUtil.getPlayerCardsInLibrary(AllZone.getHumanPlayer());
        HLibrary = HLibrary.filter(AllZoneUtil.nonlands);
        CardList CLibrary = AllZoneUtil.getPlayerCardsInLibrary(AllZone.getComputerPlayer());
        CLibrary = CLibrary.filter(AllZoneUtil.nonlands);

        boolean Starter_Determined = false;
        int Cut_Count = 0;
        int Cut_CountMax = 20;
        for (int i = 0; i < Cut_CountMax; i++) {
            if (Starter_Determined == true) break;

            if (HLibrary.size() > 0)
                setHumanCut(HLibrary.get(MyRandom.random.nextInt(HLibrary.size())));
            else {
                computerStartsGame();
                JOptionPane.showMessageDialog(null, ForgeProps.getLocalized(GAMEACTION_TEXT.HUMAN_MANA_COST) + "\r\n" + ForgeProps.getLocalized(GAMEACTION_TEXT.COMPUTER_STARTS), "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (CLibrary.size() > 0)
                setComputerCut(CLibrary.get(MyRandom.random.nextInt(CLibrary.size())));
            else {
                JOptionPane.showMessageDialog(null, ForgeProps.getLocalized(GAMEACTION_TEXT.COMPUTER_MANA_COST) + "\r\n" + ForgeProps.getLocalized(GAMEACTION_TEXT.HUMAN_STARTS), "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Cut_Count = Cut_Count + 1;
            AllZone.getGameAction().moveTo(AllZone.getZone(Constant.Zone.Library, AllZone.getHumanPlayer()), AllZone.getGameAction().getHumanCut());
            AllZone.getGameAction().moveTo(AllZone.getZone(Constant.Zone.Library, AllZone.getComputerPlayer()), AllZone.getGameAction().getComputerCut());

            StringBuilder sb = new StringBuilder();
            sb.append(ForgeProps.getLocalized(GAMEACTION_TEXT.HUMAN_CUT) + getHumanCut().getName() + " (" + getHumanCut().getManaCost() + ")" + "\r\n");
            sb.append(ForgeProps.getLocalized(GAMEACTION_TEXT.COMPUTER_CUT) + getComputerCut().getName() + " (" + getComputerCut().getManaCost() + ")" + "\r\n");
            sb.append("\r\n" + "Number of times the deck has been cut: " + Cut_Count + "\r\n");
            if (CardUtil.getConvertedManaCost(getComputerCut().getManaCost()) > CardUtil.getConvertedManaCost(getHumanCut().getManaCost())) {
                computerStartsGame();
                JOptionPane.showMessageDialog(null, sb + ForgeProps.getLocalized(GAMEACTION_TEXT.COMPUTER_STARTS), "", JOptionPane.INFORMATION_MESSAGE);
                return;
            } else if (CardUtil.getConvertedManaCost(getComputerCut().getManaCost()) < CardUtil.getConvertedManaCost(getHumanCut().getManaCost())) {
                JOptionPane.showMessageDialog(null, sb + ForgeProps.getLocalized(GAMEACTION_TEXT.HUMAN_STARTS), "", JOptionPane.INFORMATION_MESSAGE);
                return;
            } else {
                sb.append(ForgeProps.getLocalized(GAMEACTION_TEXT.EQUAL_CONVERTED_MANA) + "\r\n");
                if (i == Cut_CountMax - 1) {
                    sb.append(ForgeProps.getLocalized(GAMEACTION_TEXT.RESOLVE_STARTER));
                    if (MyRandom.random.nextInt(2) == 1)
                        JOptionPane.showMessageDialog(null, sb + ForgeProps.getLocalized(GAMEACTION_TEXT.HUMAN_WIN), "", JOptionPane.INFORMATION_MESSAGE);
                    else {
                        computerStartsGame();
                        JOptionPane.showMessageDialog(null, sb + ForgeProps.getLocalized(GAMEACTION_TEXT.COMPUTER_WIN), "", JOptionPane.INFORMATION_MESSAGE);
                    }
                    return;
                } else {
                    sb.append(ForgeProps.getLocalized(GAMEACTION_TEXT.CUTTING_AGAIN));
                }
                JOptionPane.showMessageDialog(null, sb, "", JOptionPane.INFORMATION_MESSAGE);
            }
        } // for-loop for multiple card cutting


    }//seeWhoPlaysFirst()

    /**
     * <p>computerStartsGame.</p>
     */
    public void computerStartsGame() {
        AllZone.getPhase().setPlayerTurn(AllZone.getComputerPlayer());
        AllZone.getGameInfo().setComputerStartedThisGame(true);
    }

    //if Card had the type "Aura" this method would always return true, since local enchantments are always attached to something
    //if Card is "Equipment", returns true if attached to something

    /**
     * <p>isAttachee.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a boolean.
     */
    public boolean isAttachee(Card c) {
        CardList list = AllZoneUtil.getCardsInPlay();

        for (int i = 0; i < list.size(); i++) {
            CardList check = new CardList(list.getCard(i).getAttachedCards());
            if (check.contains(c)) return true;
        }

        return false;
    }//isAttached(Card c)

    /**
     * <p>playCard.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a boolean.
     */
    public boolean playCard(Card c) {
        // this can only be called by the Human
        HashMap<String, SpellAbility> map = new HashMap<String, SpellAbility>();
        SpellAbility[] abilities = canPlaySpellAbility(c.getSpellAbility());
        ArrayList<String> choices = new ArrayList<String>();
        Player human = AllZone.getHumanPlayer();
        
        if (c.isLand() && human.canPlayLand()){
        	PlayerZone zone = AllZone.getZone(c);
        	
        	if (zone.is(Constant.Zone.Hand) || 
        		(!zone.is(Constant.Zone.Battlefield)) && c.hasKeyword("May be played"))
        		choices.add("Play land");
        }

        for (SpellAbility sa : abilities) {
            // for uncastables like lotus bloom, check if manaCost is blank
            sa.setActivatingPlayer(human);
            if (sa.canPlay() && (!sa.isSpell() || !sa.getManaCost().equals(""))) {
                choices.add(sa.toString());
                map.put(sa.toString(), sa);
            }
        }

        String choice;
        if (choices.size() == 0)
            return false;
        else if (choices.size() == 1)
            choice = choices.get(0);
        else
            choice = (String) GuiUtils.getChoiceOptional("Choose", choices.toArray());

        if (choice == null)
            return false;

        if (choice.equals("Play land")) {
            AllZone.getHumanPlayer().playLand(c);
            return true;
        }

        SpellAbility ability = map.get(choice);
        if (ability != null) {
            playSpellAbility(ability);
            return true;
        }
        return false;
    }

    /**
     * <p>playCardNoCost.</p>
     *
     * @param c a {@link forge.Card} object.
     */
    public void playCardNoCost(Card c) {
        //SpellAbility[] choices = (SpellAbility[]) c.getSpells().toArray();
        ArrayList<SpellAbility> choices = c.getBasicSpells();
        SpellAbility sa;

        //TODO: add Buyback, Kicker, ... , spells here
        /*
        ArrayList<SpellAbility> additional = c.getAdditionalCostSpells();
        for (SpellAbility s : additional)
        {
        	
        }
        */
        /*
         System.out.println(choices.length);
         for(int i = 0; i < choices.length; i++)
             System.out.println(choices[i]);
        */
        if (choices.size() == 0) return;
        else if (choices.size() == 1) sa = choices.get(0);
        else sa = (SpellAbility) GuiUtils.getChoiceOptional("Choose", choices.toArray());

        if (sa == null) return;

        // Ripple causes a crash because it doesn't set the activatingPlayer in this entrance
        if (sa.getActivatingPlayer() == null)
            sa.setActivatingPlayer(AllZone.getHumanPlayer());
        playSpellAbilityForFree(sa);
    }


    /**
     * <p>playSpellAbilityForFree.</p>
     *
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    public void playSpellAbilityForFree(final SpellAbility sa) {
        if (sa.getPayCosts() != null) {
            Target_Selection ts = new Target_Selection(sa.getTarget(), sa);
            Cost_Payment payment = new Cost_Payment(sa.getPayCosts(), sa);

            SpellAbility_Requirements req = new SpellAbility_Requirements(sa, ts, payment);
            req.setFree(true);
            req.fillRequirements();
        } else if (sa.getBeforePayMana() == null) {
            if (sa.isSpell()) {
                Card c = sa.getSourceCard();
                if (!c.isCopiedSpell())
                    AllZone.getGameAction().moveToStack(c);
            }
            boolean x = false;
            if (sa.getSourceCard().getManaCost().contains("X"))
                x = true;

            if (sa.isKickerAbility()) {
                Command paid1 = new Command() {
                    private static final long serialVersionUID = -6531785460264284794L;

                    public void execute() {
                        AllZone.getStack().add(sa);
                    }
                };
                AllZone.getInputControl().setInput(new Input_PayManaCost_Ability(sa.getAdditionalManaCost(), paid1));
            } else {
                AllZone.getStack().add(sa, x);
            }
        } else {
            sa.setManaCost("0"); // Beached As
            if (sa.isKickerAbility()) {
                sa.getBeforePayMana().setFree(false);
                sa.setManaCost(sa.getAdditionalManaCost());
            } else {
                sa.getBeforePayMana().setFree(true);
            }
            AllZone.getInputControl().setInput(sa.getBeforePayMana());
        }
    }

    int CostCutting_GetMultiMickerManaCostPaid = 0;
    String CostCutting_GetMultiMickerManaCostPaid_Colored = "";
    
    /**
     * <p>getSpellCostChange.</p>
     *
     * @param sa           a {@link forge.card.spellability.SpellAbility} object.
     * @param originalCost a {@link forge.card.mana.ManaCost} object.
     * @return a {@link forge.card.mana.ManaCost} object.
     */
    public ManaCost getSpellCostChange(SpellAbility sa, ManaCost originalCost) {
        // Beached
        Card originalCard = sa.getSourceCard();
        Player controller = originalCard.getController();
        SpellAbility spell = sa;
        String mana = originalCost.toString();
        ManaCost manaCost = new ManaCost(mana);
        if (sa.isXCost() && !originalCard.isCopiedSpell()) originalCard.setXManaCostPaid(0);
        
        if (Phase.getGameBegins() != 1)
        	return manaCost;

        if (spell.isSpell() == true) {
            if (originalCard.getName().equals("Avatar of Woe")) {
                Player player = AllZone.getPhase().getPlayerTurn();
                Player opponent = player.getOpponent();
                CardList PlayerCreatureList = AllZoneUtil.getPlayerGraveyard(player);
                PlayerCreatureList = PlayerCreatureList.getType("Creature");
                CardList OpponentCreatureList = AllZoneUtil.getPlayerGraveyard(opponent);
                OpponentCreatureList = OpponentCreatureList.getType("Creature");
                if ((PlayerCreatureList.size() + OpponentCreatureList.size()) >= 10) {
                    manaCost = new ManaCost("B B");
                } // Avatar of Woe
            } else if (originalCard.getName().equals("Avatar of Will")) {
                Player opponent = AllZone.getPhase().getPlayerTurn().getOpponent();
                CardList opponentHandList = AllZoneUtil.getPlayerHand(opponent);
                if (opponentHandList.size() == 0) {
                    manaCost = new ManaCost("U U");
                } // Avatar of Will
            } else if (originalCard.getName().equals("Avatar of Fury")) {
                Player opponent = AllZone.getPhase().getPlayerTurn().getOpponent();
                CardList opponentLand = AllZoneUtil.getPlayerLandsInPlay(opponent);
                if (opponentLand.size() >= 7) {
                    manaCost = new ManaCost("R R");
                } // Avatar of Fury
            } else if (originalCard.getName().equals("Avatar of Might")) {
                Player player = AllZone.getPhase().getPlayerTurn();
                Player opponent = player.getOpponent();
                CardList playerCreature = AllZoneUtil.getCreaturesInPlay(player);
                CardList opponentCreature = AllZoneUtil.getCreaturesInPlay(opponent);
                if (opponentCreature.size() - playerCreature.size() >= 4) {
                    manaCost = new ManaCost("G G");
                } // Avatar of Might
            }
        } // isSpell

        // Get Cost Reduction
        CardList Cards_In_Play = AllZoneUtil.getCardsInPlay();
        Cards_In_Play = Cards_In_Play.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                if (c.getKeyword().toString().contains("CostChange")) return true;
                return false;
            }
        });
        Cards_In_Play.add(originalCard);
        CardList Player_Play = AllZoneUtil.getPlayerCardsInPlay(controller);
        CardList Player_Hand = AllZoneUtil.getPlayerHand(controller);
        int XBonus = 0;
        int Max = 25;
        if (sa.isMultiKicker()) CostCutting_GetMultiMickerManaCostPaid_Colored = "";
        
        if (mana.toString().length() == 0) mana = "0";
        for (int i = 0; i < Cards_In_Play.size(); i++) {
            Card card = Cards_In_Play.get(i);
            ArrayList<String> a = card.getKeyword();
            int CostKeywords = 0;
            int CostKeyword_Number[] = new int[a.size()];
            for (int x = 0; x < a.size(); x++)
                if (a.get(x).toString().startsWith("CostChange")) {
                    CostKeyword_Number[CostKeywords] = x;
                    CostKeywords = CostKeywords + 1;
                }
            for (int CKeywords = 0; CKeywords < CostKeywords; CKeywords++) {
                String parse = card.getKeyword().get(CostKeyword_Number[CKeywords]).toString();
                String k[] = parse.split(":");
                if (card.equals(originalCard)) {
                    if (!k[4].equals("Self")) k[2] = "Owned";
                }
                if (k[6].equals("ChosenType")) k[6] = card.getChosenType();
                if (k[2].equals("More")) {
                    if (k[7].equals("OnlyOneBonus")) {  // Only Works for Color and Type
                        for (int string_no = 5; string_no < 7; string_no++) {
                            String spilt = k[string_no];
                            String color_spilt[] = spilt.split("/");

                            for (int cs_num = 0; cs_num < color_spilt.length; cs_num++) {
                                k[string_no] = color_spilt[cs_num];
                                if (string_no == 5 && CardUtil.getColors(originalCard).contains(k[5])) break;
                                if (string_no == 6 && (originalCard.isType(k[6]))) break;
                            }
                        }
                    }
                    if (k[7].contains("All Conditions")) { // Only Works for Color and Type
                        for (int string_no = 5; string_no < 7; string_no++) {
                            String spilt = k[string_no];
                            String color_spilt[] = spilt.split("/");
                            for (int cs_num = 0; cs_num < color_spilt.length; cs_num++) {
                                k[string_no] = color_spilt[cs_num];
                                if (string_no == 5) {
                                    if (CardUtil.getColors(originalCard).contains(k[5]) || k[5].equals("All")) {
                                    } else {
                                        k[5] = "Nullified";
                                        break;
                                    }
                                }
                                if (string_no == 6) {
                                    if (originalCard.isType(k[6]) || k[6].equals("All")) {
                                    } else {
                                        k[6] = "Nullified";
                                        break;
                                    }
                                }
                            }
                        }
                        if (!k[5].equals("Nullified")) k[5] = "All";
                        if (!k[6].equals("Nullified")) k[6] = "All";
                    }
                    if ((k[1].equals("Player") && card.getController().equals(controller)
                            || (k[1].equals("Opponent") && card.getController().equals(controller.getOpponent())) || k[1].equals("All"))
                            && ((k[4].equals("Spell") && sa.isSpell() == true) || (k[4].equals("Ability") && sa.isAbility() == true)
                            || (k[4].startsWith("Ability_Cycling") && sa.isCycling()) || (k[4].equals("Self") && originalCard.equals(card))
                            || (k[4].equals("Enchanted") && originalCard.getEnchantedBy().contains(card)) || k[4].equals("All"))
                            && ((CardUtil.getColors(originalCard).contains(k[5])) || k[5].equals("All"))
                            && ((originalCard.isType(k[6]))
                            || (!(originalCard.isType(k[6])) && k[7].contains("NonType")) || k[6].equals("All"))) {
                        if (k[7].contains("CardIsTapped")) {
                            if (card.isTapped() == false) k[3] = "0";
                        }
                        if (k[7].contains("TargetInPlay")) {
                            if (!Player_Play.contains(originalCard)) k[3] = "0";
                        }
                        if (k[7].contains("TargetInHand")) {
                            if (!Player_Hand.contains(originalCard)) k[3] = "0";
                        }
                        if (k[7].contains("NonType")) {
                            if (originalCard.isType(k[6])) k[3] = "0";
                        }
                        if (k[7].contains("OpponentTurn")) {
                            if (AllZone.getPhase().isPlayerTurn(controller)) k[3] = "0";
                        }
                        if (k[7].contains("Affinity")) {
                            String spilt = k[7];
                            String color_spilt[] = spilt.split("/");
                            k[7] = color_spilt[1];
                            CardList PlayerList = AllZoneUtil.getPlayerCardsInPlay(controller);
                            PlayerList = PlayerList.getType(k[7]);
                            k[3] = String.valueOf(PlayerList.size());
                        }
                        String[] Numbers = new String[Max];
                        if ("X".equals(k[3])) {
                            for (int no = 0; no < Max; no++) Numbers[no] = String.valueOf(no);
                            String Number_ManaCost = " ";
                            if (mana.toString().length() == 1) {
                                Number_ManaCost = mana.toString().substring(0, 1);
                            } else if (mana.toString().length() == 0) {
                                Number_ManaCost = "0"; // Should Never Occur
                            } else {
                                Number_ManaCost = mana.toString().substring(0, 2);
                            }
                            Number_ManaCost = Number_ManaCost.trim();
                            for (int check = 0; check < Max; check++) {
                                if (Number_ManaCost.equals(Numbers[check])) {
                                    int xValue = CardFactoryUtil.xCount(card, card.getSVar("X"));
                                    //if((spell.isXCost()) || (spell.isMultiKicker()) && (check - Integer.valueOf(k[3])) < 0) XBonus = XBonus - check + Integer.valueOf(k[3]);
                                    mana = mana.replaceFirst(String.valueOf(check), String.valueOf(check + xValue));
                                }
                                if (mana.equals("")) mana = "0";
                                manaCost = new ManaCost(mana);
                            }
                        } else if (!"WUGRB".contains(k[3])) {
                            for (int no = 0; no < Max; no++) Numbers[no] = String.valueOf(no);
                            String Number_ManaCost = " ";
                            if (mana.toString().length() == 1) Number_ManaCost = mana.toString().substring(0, 1);
                            else if (mana.toString().length() == 0) Number_ManaCost = "0"; // Should Never Occur
                            else Number_ManaCost = mana.toString().substring(0, 2);
                            Number_ManaCost = Number_ManaCost.trim();

                            for (int check = 0; check < Max; check++) {
                                if (Number_ManaCost.equals(Numbers[check])) {
                                    mana = mana.replaceFirst(String.valueOf(check), String.valueOf(check + Integer.valueOf(k[3])));
                                }
                                if (mana.equals("")) mana = "0";
                                manaCost = new ManaCost(mana);
                            }
                            if (!manaCost.toString().contains("0") && !manaCost.toString().contains("1") && !manaCost.toString().contains("2")
                                    && !manaCost.toString().contains("3") && !manaCost.toString().contains("4") && !manaCost.toString().contains("5")
                                    && !manaCost.toString().contains("6") && !manaCost.toString().contains("7") && !manaCost.toString().contains("8")
                                    && !manaCost.toString().contains("9")) {
                                mana = k[3] + " " + mana;
                                manaCost = new ManaCost(mana);
                            }
                        } else {
                            mana = mana + " " + k[3];
                            manaCost = new ManaCost(mana);
                        }
                    }
                }
            }
        }

        if (mana.equals("0") && spell.isAbility()) {
        } else {
            for (int i = 0; i < Cards_In_Play.size(); i++) {
                Card card = Cards_In_Play.get(i);
                ArrayList<String> a = card.getKeyword();
                int CostKeywords = 0;
                int CostKeyword_Number[] = new int[a.size()];
                for (int x = 0; x < a.size(); x++)
                    if (a.get(x).toString().startsWith("CostChange")) {
                        CostKeyword_Number[CostKeywords] = x;
                        CostKeywords = CostKeywords + 1;
                    }
                for (int CKeywords = 0; CKeywords < CostKeywords; CKeywords++) {
                    String parse = card.getKeyword().get(CostKeyword_Number[CKeywords]).toString();
                    String k[] = parse.split(":");
                    if (card.equals(originalCard)) {
                        if (!k[4].equals("Self")) k[2] = "Owned";
                    }
                    if (k[6].equals("ChosenType")) k[6] = card.getChosenType();
                    if (k[2].equals("Less")) {
                        if (k[7].equals("OnlyOneBonus")) { // Only Works for Color and Type
                            for (int string_no = 5; string_no < 7; string_no++) {
                                String spilt = k[string_no];
                                String color_spilt[] = spilt.split("/");

                                for (int cs_num = 0; cs_num < color_spilt.length; cs_num++) {
                                    k[string_no] = color_spilt[cs_num];
                                    if (string_no == 5 && CardUtil.getColors(originalCard).contains(k[5]))
                                        break;
                                    if (string_no == 6 && (originalCard.isType(k[6]))) break;
                                }
                            }
                        }
                        if (k[7].contains("All Conditions")) { // Only Works for Color and Type
                            for (int string_no = 5; string_no < 7; string_no++) {
                                String spilt = k[string_no];
                                String color_spilt[] = spilt.split("/");
                                for (int cs_num = 0; cs_num < color_spilt.length; cs_num++) {
                                    k[string_no] = color_spilt[cs_num];
                                    if (string_no == 5) {
                                        if (CardUtil.getColors(originalCard).contains(k[5]) || k[5].equals("All")) {
                                        } else {
                                            k[5] = "Nullified";
                                            break;
                                        }
                                    }
                                    if (string_no == 6) {
                                        if (originalCard.isType(k[6]) || k[6].equals("All")) {
                                        } else {
                                            k[6] = "Nullified";
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!k[5].equals("Nullified")) k[5] = "All";
                            if (!k[6].equals("Nullified")) k[6] = "All";
                        }
                        if ((k[1].equals("Player") && card.getController().equals(controller)
                                || (k[1].equals("Opponent") && card.getController().equals(controller.getOpponent())) || k[1].equals("All"))
                                && ((k[4].equals("Spell") && sa.isSpell() == true) || (k[4].equals("Ability") && sa.isAbility() == true)
                                || (k[4].startsWith("Ability_Cycling") && sa.isCycling()) || (k[4].equals("Self") && originalCard.equals(card))
                                || (k[4].equals("Enchanted") && originalCard.getEnchantedBy().contains(card)) || k[4].equals("All"))
                                && ((CardUtil.getColors(originalCard).contains(k[5])) || k[5].equals("All"))
                                && ((originalCard.isType(k[6]))
                                || (!(originalCard.isType(k[6])) && k[7].contains("NonType")) || k[6].equals("All"))) {
                            if (k[7].contains("CardIsTapped")) {
                                if (card.isTapped() == false) k[3] = "0";
                            }
                            if (k[7].contains("TargetInPlay")) {
                                if (!Player_Play.contains(originalCard)) k[3] = "0";
                            }
                            if (k[7].contains("TargetInHand")) {
                                if (!Player_Hand.contains(originalCard)) k[3] = "0";
                            }
                            if (k[7].contains("NonType")) {
                                if (originalCard.isType(k[6])) k[3] = "0";
                            }
                            if (k[7].contains("OpponentTurn")) {
                                if (AllZone.getPhase().isPlayerTurn(controller)) k[3] = "0";
                            }
                            if (k[7].contains("Affinity")) {
                                String spilt = k[7];
                                String color_spilt[] = spilt.split("/");
                                k[7] = color_spilt[1];
                                CardList PlayerList = AllZoneUtil.getPlayerCardsInPlay(controller);
                                PlayerList = PlayerList.getType(k[7]);
                                k[3] = String.valueOf(PlayerList.size());
                            }

                            String[] Numbers = new String[Max];
                            if (!"WUGRB".contains(k[3])) {

                                int value = 0;
                                if ("X".equals(k[3]))
                                    value = CardFactoryUtil.xCount(card, card.getSVar("X"));
                                else
                                    value = Integer.valueOf(k[3]);

                                for (int no = 0; no < Max; no++) Numbers[no] = String.valueOf(no);
                                String Number_ManaCost = " ";
                                if (mana.toString().length() == 1)
                                    Number_ManaCost = mana.toString().substring(0, 1);
                                else if (mana.toString().length() == 0)
                                    Number_ManaCost = "0";  // Should Never Occur
                                else Number_ManaCost = mana.toString().substring(0, 2);
                                Number_ManaCost = Number_ManaCost.trim();

                                for (int check = 0; check < Max; check++) {
                                    if (Number_ManaCost.equals(Numbers[check])) {
                                        if ((spell.isXCost()) || (spell.isMultiKicker()) && (check - value) < 0)
                                            XBonus = XBonus - check + value;
                                        if (check - value < 0) value = check;
                                        mana = mana.replaceFirst(String.valueOf(check), String.valueOf(check - value));
                                    }
                                    if (mana.equals("")) mana = "0";
                                    manaCost = new ManaCost(mana);
                                }
                            } else {
                                //   	 JOptionPane.showMessageDialog(null, Mana + " " + Mana.replaceFirst(k[3],""), "", JOptionPane.INFORMATION_MESSAGE);
                                if (mana.equals(mana.replaceFirst(k[3], ""))) {
                                    // if(sa.isXCost()) sa.getSourceCard().addXManaCostPaid(1); Not Included as X Costs are not in Colored Mana
                                    if (sa.isMultiKicker())
                                        CostCutting_GetMultiMickerManaCostPaid_Colored = CostCutting_GetMultiMickerManaCostPaid_Colored + k[3];
                                    //		 JOptionPane.showMessageDialog(null, CostCutting_GetMultiMickerManaCostPaid_Colored, "", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    mana = mana.replaceFirst(k[3], "");
                                    mana = mana.trim();
                                    if (mana.equals("")) mana = "0";
                                    manaCost = new ManaCost(mana);
                                }
                            }
                        }
                        mana = mana.trim();
                        if (mana.length() == 0 || mana.equals("0")) {
                            if (sa.isSpell() || sa.isCycling()) mana = "0";
                            else {
                                mana = "1";
                            }
                        }
                    }
                    manaCost = new ManaCost(mana);
                }
            }
        }
        if (sa.isXCost()) {
            for (int XPaid = 0; XPaid < XBonus; XPaid++) originalCard.addXManaCostPaid(1);
        }
        if (sa.isMultiKicker()) {
            CostCutting_GetMultiMickerManaCostPaid = 0;
            for (int XPaid = 0; XPaid < XBonus; XPaid++)
                CostCutting_GetMultiMickerManaCostPaid = CostCutting_GetMultiMickerManaCostPaid + 1;
        }
            
        if (originalCard.getName().equals("Khalni Hydra") && spell.isSpell() == true) {
            Player player = AllZone.getPhase().getPlayerTurn();
            CardList playerCreature = AllZoneUtil.getCreaturesInPlay(player);
            playerCreature = playerCreature.filter(AllZoneUtil.green);
            String manaC = manaCost + " ";
            if (playerCreature.size() > 0) {
                for (int i = 0; i < playerCreature.size(); i++) {
                    manaC = manaC.replaceFirst("G ", "");
                }
                manaC = manaC.trim();
                if (manaC.equals("")) manaC = "0";
                manaCost = new ManaCost(manaC);
            }
        } // Khalni Hydra
        return manaCost;
    }//GetSpellCostChange

    /**
     * <p>playSpellAbility.</p>
     *
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    public void playSpellAbility(SpellAbility sa) {
        sa.setActivatingPlayer(AllZone.getHumanPlayer());

		// Need to check PayCosts, and Ability + All SubAbilities for Target
        boolean newAbility = sa.getPayCosts() != null;
        SpellAbility ability = sa;
        while(ability != null && !newAbility){
            Target tgt = ability.getTarget();
            
            newAbility |= tgt != null;
            ability = ability.getSubAbility();
        }
        
        if (newAbility) {
            Target_Selection ts = new Target_Selection(sa.getTarget(), sa);
            Cost_Payment payment = null;
            if (sa.getPayCosts() == null) {
                payment = new Cost_Payment(new Cost("0", sa.getSourceCard().getName(), sa.isAbility()), sa);
            } else {
                payment = new Cost_Payment(sa.getPayCosts(), sa);
            }

            if (!sa.isTrigger())
                payment.changeCost();

            SpellAbility_Requirements req = new SpellAbility_Requirements(sa, ts, payment);
            req.fillRequirements();
        } else {
            ManaCost manaCost = new ManaCost(sa.getManaCost());
            if (sa.getSourceCard().isCopiedSpell() && sa.isSpell()) {
                manaCost = new ManaCost("0");
            } else {

                manaCost = getSpellCostChange(sa, new ManaCost(sa.getManaCost()));
            }
            if (manaCost.isPaid() && sa.getBeforePayMana() == null) {
                if (sa.getAfterPayMana() == null) {
                    Card source = sa.getSourceCard();
                    if (sa.isSpell() && !source.isCopiedSpell())
                        AllZone.getGameAction().moveToStack(source);

                    AllZone.getStack().add(sa);
                    if (sa.isTapAbility() && !sa.wasCancelled()) sa.getSourceCard().tap();
                    if (sa.isUntapAbility()) sa.getSourceCard().untap();
                    return;
                } else
                    AllZone.getInputControl().setInput(sa.getAfterPayMana());
            } else if (sa.getBeforePayMana() == null)
                AllZone.getInputControl().setInput(new Input_PayManaCost(sa));
            else
                AllZone.getInputControl().setInput(sa.getBeforePayMana());
        }
    }

    /**
     * <p>playSpellAbility_NoStack.</p>
     *
     * @param sa            a {@link forge.card.spellability.SpellAbility} object.
     * @param skipTargeting a boolean.
     */
    public void playSpellAbility_NoStack(SpellAbility sa, boolean skipTargeting) {
        sa.setActivatingPlayer(AllZone.getHumanPlayer());

        if (sa.getPayCosts() != null) {
            Target_Selection ts = new Target_Selection(sa.getTarget(), sa);
            Cost_Payment payment = new Cost_Payment(sa.getPayCosts(), sa);

            if (!sa.isTrigger())
                payment.changeCost();

            SpellAbility_Requirements req = new SpellAbility_Requirements(sa, ts, payment);
            req.setSkipStack(true);
            req.fillRequirements(skipTargeting);
        } else {
            ManaCost manaCost = new ManaCost(sa.getManaCost());
            if (sa.getSourceCard().isCopiedSpell() && sa.isSpell()) {
                manaCost = new ManaCost("0");
            } else {

                manaCost = getSpellCostChange(sa, new ManaCost(sa.getManaCost()));
            }
            if (manaCost.isPaid() && sa.getBeforePayMana() == null) {
                if (sa.getAfterPayMana() == null) {
                    AbilityFactory.resolve(sa, false);
                    if (sa.isTapAbility() && !sa.wasCancelled()) sa.getSourceCard().tap();
                    if (sa.isUntapAbility()) sa.getSourceCard().untap();
                    return;
                } else
                    AllZone.getInputControl().setInput(sa.getAfterPayMana());
            } else if (sa.getBeforePayMana() == null)
                AllZone.getInputControl().setInput(new Input_PayManaCost(sa, true));
            else
                AllZone.getInputControl().setInput(sa.getBeforePayMana());
        }
    }

    /**
     * <p>canPlaySpellAbility.</p>
     *
     * @param sa an array of {@link forge.card.spellability.SpellAbility} objects.
     * @return an array of {@link forge.card.spellability.SpellAbility} objects.
     */
    public SpellAbility[] canPlaySpellAbility(SpellAbility[] sa) {
        ArrayList<SpellAbility> list = new ArrayList<SpellAbility>();

        for (int i = 0; i < sa.length; i++) {
            sa[i].setActivatingPlayer(AllZone.getHumanPlayer());
            if (sa[i].canPlay())
                list.add(sa[i]);
        }

        SpellAbility[] array = new SpellAbility[list.size()];
        list.toArray(array);
        return array;
    }//canPlaySpellAbility()

    /**
     * <p>searchLibraryBasicLand.</p>
     *
     * @param player  a {@link forge.Player} object.
     * @param Zone1   a {@link java.lang.String} object.
     * @param tapLand a boolean.
     */
    public void searchLibraryBasicLand(Player player, String Zone1, boolean tapLand) {
        searchLibraryTwoLand("Basic", player, Zone1, tapLand, "", false);
    }

    /**
     * <p>searchLibraryTwoLand.</p>
     *
     * @param type          a {@link java.lang.String} object.
     * @param player        a {@link forge.Player} object.
     * @param Zone1         a {@link java.lang.String} object.
     * @param tapFirstLand  a boolean.
     * @param Zone2         a {@link java.lang.String} object.
     * @param tapSecondLand a boolean.
     */
    public void searchLibraryTwoLand(String type, Player player,
                                     String Zone1, boolean tapFirstLand,
                                     String Zone2, boolean tapSecondLand) {
        if (player.isHuman()) {
            humanSearchTwoLand(type, Zone1, tapFirstLand, Zone2, tapSecondLand);
        } else {
            aiSearchTwoLand(type, Zone1, tapFirstLand, Zone2, tapSecondLand);
        }

        player.shuffle();

    }

    /**
     * <p>searchLibraryTwoBasicLand.</p>
     *
     * @param player        a {@link forge.Player} object.
     * @param Zone1         a {@link java.lang.String} object.
     * @param tapFirstLand  a boolean.
     * @param Zone2         a {@link java.lang.String} object.
     * @param tapSecondLand a boolean.
     */
    public void searchLibraryTwoBasicLand(Player player,
                                          String Zone1, boolean tapFirstLand,
                                          String Zone2, boolean tapSecondLand) {
        searchLibraryTwoLand("Basic", player, Zone1, tapFirstLand, Zone2, tapSecondLand);
    }

    /**
     * <p>aiSearchTwoLand.</p>
     *
     * @param type          a {@link java.lang.String} object.
     * @param Zone1         a {@link java.lang.String} object.
     * @param tapFirstLand  a boolean.
     * @param Zone2         a {@link java.lang.String} object.
     * @param tapSecondLand a boolean.
     */
    private void aiSearchTwoLand(String type, String Zone1, boolean tapFirstLand,
                                 String Zone2, boolean tapSecondLand) {
        CardList land = AllZoneUtil.getPlayerCardsInLibrary(AllZone.getComputerPlayer());
        land = land.getType(type);
        PlayerZone firstZone = AllZone.getZone(Zone1, AllZone.getComputerPlayer());

        if (type.contains("Basic")) {
            // No need for special sorting for basic land
            // just shuffle to make the computer a little less predictable
            land.shuffle();
        } else {
            Comparator<Card> aiLandComparator = new Comparator<Card>() {
                private int scoreLand(Card a) {
                    String valakutName = "Valakut, the Molten Pinnacle";

                    int theScore = 0;
                    if (!a.isBasicLand()) {
                        // favor non-basic land
                        theScore++;
                        if (a.getName().contains(valakutName)) {
                            // TODO: Add names of other special lands
                            theScore++;
                        }
                    }
                    return theScore;
                }

                public int compare(Card a, Card b) {
                    int aScore = scoreLand(a);
                    int bScore = scoreLand(b);
                    return bScore - aScore;
                } // compare
            };//Comparator

            // Prioritize the land somewhat
            land.sort(aiLandComparator);
        }
        //3 branches: 1-no land in deck, 2-one land in deck, 3-two or more land in deck
        if (land.size() != 0) {
            //branch 2 - at least 1 land in library
            Card firstLand = land.remove(0);
            if (tapFirstLand)
                firstLand.tap();

            AllZone.getGameAction().moveTo(firstZone, firstLand);

            //branch 3
            if (Zone2.trim().length() != 0 && (land.size() != 0)) {
                PlayerZone secondZone = AllZone.getZone(Zone2, AllZone.getComputerPlayer());
                Card secondLand = land.remove(0);
                if (tapSecondLand)
                    secondLand.tap();
                AllZone.getGameAction().moveTo(secondZone, secondLand);
            }
        }
    }

    /**
     * <p>humanSearchTwoLand.</p>
     *
     * @param type          a {@link java.lang.String} object.
     * @param Zone1         a {@link java.lang.String} object.
     * @param tapFirstLand  a boolean.
     * @param Zone2         a {@link java.lang.String} object.
     * @param tapSecondLand a boolean.
     */
    private void humanSearchTwoLand(String type, String Zone1, boolean tapFirstLand, String Zone2, boolean tapSecondLand) {
        PlayerZone firstZone = AllZone.getZone(Zone1, AllZone.getHumanPlayer());

        CardList list = AllZoneUtil.getPlayerCardsInLibrary(AllZone.getHumanPlayer());
        list = list.getType(type);

        //3 branches: 1-no land in deck, 2-one land in deck, 3-two or more land in deck

        //branch 1
        if (list.size() == 0) return;

        // Check whether we were only asked for one land, and adjust the prompt accordingly
        boolean onlyOneLand = (Zone2.trim().length() == 0);
        String firstPrompt;
        if (onlyOneLand)
            firstPrompt = "Choose a land";
        else
            firstPrompt = "Choose first land";

        //branch 2
        Object o = GuiUtils.getChoiceOptional(firstPrompt, list.toArray());
        if (o != null) {
            Card c = (Card) o;
            list.remove(c);
            if (tapFirstLand)
                c.tap();
            moveTo(firstZone, c);
        }//if
        if ((list.size() == 0) || onlyOneLand) return;
        //branch 3
        o = GuiUtils.getChoiceOptional(ForgeProps.getLocalized(GAMEACTION_TEXT.CHOOSE_2ND_LAND), list.toArray());
        if (o != null) {
            PlayerZone secondZone = AllZone.getZone(Zone2, AllZone.getHumanPlayer());

            Card c = (Card) o;
            list.remove(c);
            if (tapSecondLand)
                c.tap();
            moveTo(secondZone, c);
        }
    }

    /**
     * <p>setComputerCut.</p>
     *
     * @param computerCut a {@link forge.Card} object.
     */
    public void setComputerCut(Card computerCut) {
        ComputerCut = computerCut;
    }

    /**
     * <p>getComputerCut.</p>
     *
     * @return a {@link forge.Card} object.
     */
    public Card getComputerCut() {
        return ComputerCut;
    }

    /**
     * <p>setStartCut.</p>
     *
     * @param start_Cut a boolean.
     */
    public void setStartCut(boolean start_Cut) {
        Start_Cut = start_Cut;
    }

    /**
     * <p>isStartCut.</p>
     *
     * @return a boolean.
     */
    public boolean isStartCut() {
        return Start_Cut;
    }

    /**
     * <p>setHumanCut.</p>
     *
     * @param humanCut a {@link forge.Card} object.
     */
    public void setHumanCut(Card humanCut) {
        HumanCut = humanCut;
    }

    /**
     * <p>getHumanCut.</p>
     *
     * @return a {@link forge.Card} object.
     */
    public Card getHumanCut() {
        return HumanCut;
    }
}
