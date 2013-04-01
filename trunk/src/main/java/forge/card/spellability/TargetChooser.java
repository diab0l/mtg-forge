/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.card.spellability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Predicate;

import forge.Card;
import forge.CardLists;
import forge.FThreads;
import forge.card.ability.AbilityUtils;
import forge.control.input.InputSelectTargets;
import forge.game.GameState;
import forge.game.player.Player;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;

/**
 * <p>
 * Target_Selection class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class TargetChooser {
    private final SpellAbility ability;


    public TargetChooser(final SpellAbility sa) {
        this.ability = sa;
    }

    private final Target getTgt() {
        return this.ability.getTarget();
    }

    private final Card getCard() {
        return this.ability.getSourceCard();
    }

    private TargetChooser subSelection = null;

    private boolean bCancel = false;
    private boolean bTargetingDone = false;

    /**
     * <p>
     * setCancel.
     * </p>
     * 
     * @param done
     *            a boolean.
     */
    public final void setCancel(final boolean done) {
        this.bCancel = done;
    }

    /**
     * <p>
     * isCanceled.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isCanceled() {
        return this.bCancel || this.subSelection != null && this.subSelection.isCanceled();
    }

    public final boolean doesTarget() {
        Target tg = getTgt();
        return tg != null && tg.doesTarget();
    }

    /**
     * <p>
     * resetTargets.
     * </p>
     */
    public final void clearTargets() {
        Target tg = getTgt();
        if (tg != null) {
            tg.resetTargets();
            tg.calculateStillToDivide(this.ability.getParam("DividedAsYouChoose"), this.getCard(), this.ability);
        }
    }

    public final boolean chooseTargets() {
        Target tgt = getTgt();
        final boolean canTarget = doesTarget();
        final int minTargets = canTarget ? tgt.getMinTargets(getCard(), ability) : 0;
        final int maxTargets = canTarget ? tgt.getMaxTargets(getCard(), ability) : 0;
        final int numTargeted = canTarget ? tgt.getNumTargeted() : 0;

        boolean hasEnoughTargets = minTargets == 0 || numTargeted >= minTargets;
        boolean hasAllTargets = numTargeted == maxTargets && maxTargets > 0;

        // if not enough targets chosen, reset and cancel Ability
        if (this.bTargetingDone && !hasEnoughTargets) this.bCancel = true;
        if (this.bCancel) return false;
        

        if (!canTarget || this.bTargetingDone && hasEnoughTargets || hasAllTargets || tgt.isDividedAsYouChoose() && tgt.getStillToDivide() == 0) {
            final AbilitySub abSub = this.ability.getSubAbility();
            if (abSub == null) // if no more SubAbilities finish targeting
                return true;

            // Has Sub Ability
            this.subSelection = new TargetChooser(abSub);
            this.subSelection.clearTargets();
            return this.subSelection.chooseTargets();
        }

        if (!tgt.hasCandidates(this.ability, true) && !hasEnoughTargets) {
            // Cancel ability if there aren't any valid Candidates
            this.bCancel = true;
            return false;
        }
        
        final List<ZoneType> zone = tgt.getZone();
        final boolean mandatory = tgt.getMandatory() && tgt.hasCandidates(this.ability, true);
        
        if (zone.size() == 1 && zone.get(0) == ZoneType.Stack) {
            // If Zone is Stack, the choices are handled slightly differently
           this.chooseCardFromStack(mandatory);
        } else {
            List<Card> validTargets = this.chooseValidInput();
            if (zone.size() == 1 && zone.get(0) == ZoneType.Battlefield) {
                InputSelectTargets inp = new InputSelectTargets(validTargets, ability, mandatory);
                FThreads.setInputAndWait(inp);
                bCancel = inp.hasCancelled();
                bTargetingDone = inp.hasPressedOk();
            } else {
                this.chooseCardFromList(validTargets, true, mandatory);
            }
        }
        // some inputs choose cards 1-by-1 and need to be called again, 
        // moreover there are sub-abilities that also need targets
        return chooseTargets();
    }

    /**
     * Gets the unique targets.
     * 
     * @param ability
     *            the ability
     * @return the unique targets
     */
    public static final ArrayList<Object> getUniqueTargets(final SpellAbility ability) {
        final ArrayList<Object> targets = new ArrayList<Object>();
        SpellAbility child = ability;
        while (child instanceof AbilitySub) {
            child = ((AbilitySub) child).getParent();
            if (child != null && child.getTarget() != null) {
                targets.addAll(child.getTarget().getTargets());
            }
        }

        return targets;
    }

    // these have been copied over from CardFactoryUtil as they need two extra
    // parameters for target selection.
    // however, due to the changes necessary for SA_Requirements this is much
    // different than the original

    /**
     * <p>
     * chooseValidInput.
     * </p>
     * @return 
     */
    public final List<Card> chooseValidInput() {
        final Target tgt = this.getTgt();
        final GameState game = ability.getActivatingPlayer().getGame();
        final List<ZoneType> zone = tgt.getZone();

        final boolean canTgtStack = zone.contains(ZoneType.Stack);
        List<Card> choices = CardLists.getTargetableCards(CardLists.getValidCards(game.getCardsIn(zone), tgt.getValidTgts(), this.ability.getActivatingPlayer(), this.ability.getSourceCard()), this.ability);
        if (canTgtStack) {
            // Since getTargetableCards doesn't have additional checks if one of the Zones is stack
            // Remove the activating card from targeting itself if its on the Stack
            Card activatingCard = tgt.getSourceCard();
            if (activatingCard.isInZone(ZoneType.Stack)) {
                choices.remove(tgt.getSourceCard());
            }
        }
        ArrayList<Object> objects = getUniqueTargets(this.ability);

        if (tgt.isUniqueTargets()) {
            for (final Object o : objects) {
                if ((o instanceof Card) && objects.contains(o)) {
                    choices.remove(o);
                }
            }
        }

        // Remove cards already targeted
        final List<Card> targeted = tgt.getTargetCards();
        for (final Card c : targeted) {
            if (choices.contains(c)) {
                choices.remove(c);
            }
        }

        // If all cards (including subability targets) must have the same controller
        if (tgt.isSameController() && !objects.isEmpty()) {
            final List<Card> list = new ArrayList<Card>();
            for (final Object o : objects) {
                if (o instanceof Card) {
                    list.add((Card) o);
                }
            }
            if (!list.isEmpty()) {
                final Card card = list.get(0);
                choices = CardLists.filter(choices, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return c.sharesControllerWith(card);
                    }
                });
            }
        }

        // If all cards must be from the same zone
        if (tgt.isSingleZone() && !targeted.isEmpty()) {
            choices = CardLists.filterControlledBy(choices, targeted.get(0).getController());
        }
        // If all cards must be from different zones
        if (tgt.isDifferentZone() && !targeted.isEmpty()) {
            choices = CardLists.filterControlledBy(choices, targeted.get(0).getController().getOpponent());
        }
        // If all cards must have different controllers
        if (tgt.isDifferentControllers() && !targeted.isEmpty()) {
            final List<Player> availableControllers = new ArrayList<Player>(game.getPlayers());
            for (int i = 0; i < targeted.size(); i++) {
                availableControllers.remove(targeted.get(i).getController());
            }
            choices = CardLists.filterControlledBy(choices, availableControllers);
        }
        // If the cards can't share a creature type
        if (tgt.isWithoutSameCreatureType() && !targeted.isEmpty()) {
            final Card card = targeted.get(0);
            choices = CardLists.filter(choices, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    return !c.sharesCreatureTypeWith(card);
                }
            });
        }
        // If the cards must have a specific controller
        if (tgt.getDefinedController() != null) {
            List<Player> pl = AbilityUtils.getDefinedPlayers(getCard(), tgt.getDefinedController(), this.ability);
            if (pl != null && !pl.isEmpty()) {
                Player controller = pl.get(0);
                choices = CardLists.filterControlledBy(choices, controller);
            } else {
                choices.clear();
            }
        }
        return choices;
    } // input_targetValid

    /**
     * <p>
     * chooseCardFromList.
     * </p>
     * 
     * @param choices
     *            a {@link forge.CardList} object.
     * @param targeted
     *            a boolean.
     * @param mandatory
     *            a boolean.
     */
    private final void chooseCardFromList(final List<Card> choices, final boolean targeted, final boolean mandatory) {
        // Send in a list of valid cards, and popup a choice box to target
        final GameState game = ability.getActivatingPlayer().getGame(); 

        final List<Card> crdsBattle = new ArrayList<Card>();
        final List<Card> crdsExile = new ArrayList<Card>();
        final List<Card> crdsGrave = new ArrayList<Card>();
        final List<Card> crdsLibrary = new ArrayList<Card>();
        final List<Card> crdsStack = new ArrayList<Card>();
        for (final Card inZone : choices) {
            Zone zz = game.getZoneOf(inZone);
            if (zz.is(ZoneType.Battlefield))    crdsBattle.add(inZone);
            else if (zz.is(ZoneType.Exile))     crdsExile.add(inZone);
            else if (zz.is(ZoneType.Graveyard)) crdsGrave.add(inZone);
            else if (zz.is(ZoneType.Library))   crdsLibrary.add(inZone);
            else if (zz.is(ZoneType.Stack))     crdsStack.add(inZone);
        }
        List<Object> choicesFiltered = new ArrayList<Object>();
        if (!crdsBattle.isEmpty()) {
            choicesFiltered.add("--CARDS ON BATTLEFIELD:--");
            choicesFiltered.addAll(crdsBattle);
        }
        if (!crdsExile.isEmpty()) {
            choicesFiltered.add("--CARDS IN EXILE:--");
            choicesFiltered.addAll(crdsExile);
        }
        if (!crdsGrave.isEmpty()) {
            choicesFiltered.add("--CARDS IN GRAVEYARD:--");
            choicesFiltered.addAll(crdsGrave);
        }
        if (!crdsLibrary.isEmpty()) {
            choicesFiltered.add("--CARDS IN LIBRARY:--");
            choicesFiltered.addAll(crdsLibrary);
        }
        if (!crdsStack.isEmpty()) {
            choicesFiltered.add("--CARDS IN STACK:--");
            choicesFiltered.addAll(crdsStack);
        }

        final String msgDone = "[FINISH TARGETING]";
        if (this.getTgt().isMinTargetsChosen(this.ability.getSourceCard(), this.ability)) {
            // is there a more elegant way of doing this?
            choicesFiltered.add(msgDone);
        }
        
        final Object chosen = GuiChoose.oneOrNone(getTgt().getVTSelection(), choicesFiltered);
        if (chosen == null) {
            this.setCancel(true);
            return;
        }
        if (msgDone.equals(chosen)) {
            bTargetingDone = true;
            return;
        }
        
        if (chosen instanceof Card )
            this.getTgt().addTarget(chosen);
    }

    /**
     * <p>
     * chooseCardFromStack.
     * </p>
     * 
     * @param mandatory
     *            a boolean.
     */
    private final void chooseCardFromStack(final boolean mandatory) {
        final Target tgt = this.getTgt();
        final String message = tgt.getVTSelection();
        final String doneDummy = "[FINISH TARGETING]";

        // Find what's targetable, then allow human to choose
        final ArrayList<SpellAbility> choosables = getTargetableOnStack();

        final HashMap<String, SpellAbility> map = new HashMap<String, SpellAbility>();

        for (final SpellAbility sa : choosables) {
            if (!tgt.getTargetSAs().contains(sa)) {
                map.put(choosables.indexOf(sa) + ". " + sa.getStackDescription(), sa);
            }
        }

        if (tgt.isMinTargetsChosen(this.ability.getSourceCard(), this.ability)) {
            map.put(doneDummy, null);
        }


        if (map.isEmpty()) {
            setCancel(true);
        } else {
            final String madeChoice = GuiChoose.oneOrNone(message, map.keySet());
            if (madeChoice != null) {
                if (madeChoice.equals(doneDummy)) {
                    bTargetingDone = true;
                } else {
                    tgt.addTarget(map.get(madeChoice));
                }
            } else {
                setCancel(true);
            }
        }
    }

    // TODO The following three functions are Utility functions for
    // TargetOnStack, probably should be moved
    // The following should be select.getTargetableOnStack()
    /**
     * <p>
     * getTargetableOnStack.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param tgt
     *            a {@link forge.card.spellability.Target} object.
     * @return a {@link java.util.ArrayList} object.
     */
    private ArrayList<SpellAbility> getTargetableOnStack() {
        final ArrayList<SpellAbility> choosables = new ArrayList<SpellAbility>();

        final GameState game = ability.getActivatingPlayer().getGame();
        for (int i = 0; i < game.getStack().size(); i++) {
            choosables.add(game.getStack().peekAbility(i));
        }

        for (int i = 0; i < choosables.size(); i++) {
            if (!TargetChooser.matchSpellAbility(ability, choosables.get(i), getTgt())) {
                choosables.remove(i);
            }
        }
        return choosables;
    }

    /**
     * <p>
     * matchSpellAbility.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param topSA
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param tgt
     *            a {@link forge.card.spellability.Target} object.
     * @return a boolean.
     */
    public static boolean matchSpellAbility(final SpellAbility sa, final SpellAbility topSA, final Target tgt) {
        final String saType = tgt.getTargetSpellAbilityType();

        if (null == saType) {
            // just take this to mean no restrictions - carry on.
        } else if (topSA instanceof Spell) {
            if (!saType.contains("Spell")) {
                return false;
            }
        } else if (topSA.isTrigger()) {
            if (!saType.contains("Triggered")) {
                return false;
            }
        } else if (topSA instanceof AbilityActivated) {
            if (!saType.contains("Activated")) {
                return false;
            }
        } else {
            return false; //Static ability? Whatever.
        }

        final String splitTargetRestrictions = tgt.getSAValidTargeting();
        if (splitTargetRestrictions != null) {
            // TODO What about spells with SubAbilities with Targets?

            final Target matchTgt = topSA.getTarget();

            if (matchTgt == null) {
                return false;
            }

            boolean result = false;

            for (final Object o : matchTgt.getTargets()) {
                if (TargetChooser.matchesValid(o, splitTargetRestrictions.split(","), sa)) {
                    result = true;
                    break;
                }
            }

            if (!result) {
                return false;
            }
        }

        return topSA.getSourceCard().isValid(tgt.getValidTgts(), sa.getActivatingPlayer(), sa.getSourceCard());
    }


    private static boolean matchesValid(final Object o, final String[] valids, final SpellAbility sa) {
        final Card srcCard = sa.getSourceCard();
        final Player activatingPlayer = sa.getActivatingPlayer();
        if (o instanceof Card) {
            final Card c = (Card) o;
            return c.isValid(valids, activatingPlayer, srcCard);
        }

        if (o instanceof Player) {
            Player p = (Player) o;
            if (p.isValid(valids, sa.getActivatingPlayer(), sa.getSourceCard())) {
                return true;
            }
        }

        return false;
    }
}
