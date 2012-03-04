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
package forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.esotericsoftware.minlog.Log;

import forge.Constant.Zone;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellPermanent;
import forge.card.trigger.Trigger;

/**
 * <p>
 * ComputerAI_General class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class ComputerAIGeneral implements Computer {

    /**
     * <p>
     * Constructor for ComputerAI_General.
     * </p>
     */
    public ComputerAIGeneral() {

    }

    /**
     * <p>
     * main.
     * </p>
     */
    @Override
    public final void main() {
        ComputerUtil.chooseLandsToPlay();

        if (AllZone.getStack().size() == 0) {
            this.playCards();
        } else {
            this.stackResponse();
        }
    } // main()


    /**
     * <p>
     * playCards.
     * </p>
     * 
     * @param phase
     *            a {@link java.lang.String} object.
     */
    private void playCards() {
        final CardList list = getAvailableSpellAbilities();

        final boolean nextPhase = ComputerUtil.playSpellAbilities(getSpellAbilities(list));

        if (nextPhase) {
            Singletons.getModel().getGameState().getPhaseHandler().passPriority();
        }
    } // playCards()

    /**
     * <p>
     * hasACardGivingHaste.
     * </p>
     * 
     * @return a boolean.
     */
    public static boolean hasACardGivingHaste() {
        final CardList all = AllZone.getComputerPlayer().getCardsIn(Zone.Battlefield);
        all.addAll(CardFactoryUtil.getExternalZoneActivationCards(AllZone.getComputerPlayer()));
        all.addAll(AllZone.getComputerPlayer().getCardsIn(Zone.Hand));

        for (final Card c : all) {
            for (final SpellAbility sa : c.getSpellAbility()) {
                if (sa.getAbilityFactory() == null) {
                    continue;
                }
                final AbilityFactory af = sa.getAbilityFactory();
                final HashMap<String, String> abilityParams = af.getMapParams();
                if (abilityParams.containsKey("AB") && !abilityParams.get("AB").equals("Pump")) {
                    continue;
                }
                if (abilityParams.containsKey("SP") && !abilityParams.get("SP").equals("Pump")) {
                    continue;
                }
                if (abilityParams.containsKey("KW") && abilityParams.get("KW").contains("Haste")) {
                    return true;
                }
            }
        }

        return false;
    } // hasACardGivingHaste

    /**
     * <p>
     * getAvailableSpellAbilities.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    private CardList getAvailableSpellAbilities() {
        final Player computer = AllZone.getComputerPlayer();
        final Player human = AllZone.getHumanPlayer();
        CardList all = computer.getCardsIn(Zone.Hand);
        all.addAll(computer.getCardsIn(Zone.Battlefield));
        all.addAll(computer.getCardsIn(Zone.Exile));
        all.addAll(computer.getCardsIn(Zone.Graveyard));
        if (!computer.getCardsIn(Zone.Library).isEmpty()) {
            all.add(computer.getCardsIn(Zone.Library).get(0));
        }
        all.addAll(human.getCardsIn(Zone.Exile));
        all.addAll(human.getCardsIn(Zone.Battlefield));
        return all;
    }

    /**
     * <p>
     * hasETBTrigger.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public static boolean hasETBTrigger(final Card card) {
        final ArrayList<Trigger> triggers = card.getTriggers();
        for (final Trigger tr : triggers) {
            final HashMap<String, String> params = tr.getMapParams();
            if (!params.get("Mode").equals("ChangesZone")) {
                continue;
            }

            if (!params.get("Destination").equals("Battlefield")) {
                continue;
            }

            if (params.containsKey("ValidCard") && !params.get("ValidCard").contains("Self")) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * <p>
     * getPossibleETBCounters.
     * </p>
     * 
     * @return a {@link java.util.ArrayList} object.
     */
    private ArrayList<SpellAbility> getPossibleETBCounters() {
        final Player computer = AllZone.getComputerPlayer();
        final Player human = AllZone.getHumanPlayer();
        CardList all = computer.getCardsIn(Zone.Hand);
        all.addAll(computer.getCardsIn(Zone.Exile));
        all.addAll(computer.getCardsIn(Zone.Graveyard));
        if (!computer.getCardsIn(Zone.Library).isEmpty()) {
            all.add(computer.getCardsIn(Zone.Library).get(0));
        }
        all.addAll(human.getCardsIn(Zone.Exile));

        final ArrayList<SpellAbility> spellAbilities = new ArrayList<SpellAbility>();
        for (final Card c : all) {
            for (final SpellAbility sa : c.getSpellAbility()) {
                if (sa instanceof SpellPermanent) {
                    if (SpellPermanent.checkETBEffects(c, sa, "Counter")) {
                        spellAbilities.add(sa);
                    }
                }
            }
        }
        return spellAbilities;
    }

    /**
     * Returns the spellAbilities from the card list.
     * 
     * @param l
     *            a {@link forge.CardList} object.
     * @return an array of {@link forge.card.spellability.SpellAbility} objects.
     */
    private ArrayList<SpellAbility> getSpellAbilities(final CardList l) {
        final ArrayList<SpellAbility> spellAbilities = new ArrayList<SpellAbility>();
        for (final Card c : l) {
            for (final SpellAbility sa : c.getSpellAbility()) {
                spellAbilities.add(sa);
            }
        }
        return spellAbilities;
    }

    /**
     * <p>
     * getPlayableCounters.
     * </p>
     * 
     * @param l
     *            a {@link forge.CardList} object.
     * @return a {@link java.util.ArrayList} object.
     */
    private ArrayList<SpellAbility> getPlayableCounters(final CardList l) {
        final ArrayList<SpellAbility> spellAbility = new ArrayList<SpellAbility>();
        for (final Card c : l) {
            for (final SpellAbility sa : c.getSpellAbility()) {
                // Check if this AF is a Counterpsell
                if ((sa.getAbilityFactory() != null) && sa.getAbilityFactory().getAPI().equals("Counter")) {
                    spellAbility.add(sa);
                }
            }
        }

        return spellAbility;
    }

    /**
     * <p>
     * begin_combat.
     * </p>
     */
    @Override
    public final void beginCombat() {
        this.stackResponse();
    }

    /**
     * <p>
     * declare_attackers.
     * </p>
     */
    @Override
    public final void declareAttackers() {
        // 12/2/10(sol) the decision making here has moved to getAttackers()

        AllZone.setCombat(ComputerUtil.getAttackers());

        final List<Card> att = AllZone.getCombat().getAttackers();
        if (!att.isEmpty()) {
            Singletons.getModel().getGameState().getPhaseHandler().setCombat(true);
        }

        for (final Card element : att) {
            // tapping of attackers happens after Propaganda is paid for
            final StringBuilder sb = new StringBuilder();
            sb.append("Computer just assigned ");
            sb.append(element.getName()).append(" as an attacker.");
            Log.debug(sb.toString());
        }

        AllZone.getComputerPlayer().getZone(Zone.Battlefield).updateObservers();

        Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
    }

    /**
     * <p>
     * declare_attackers_after.
     * </p>
     */
    @Override
    public final void declareAttackersAfter() {
        this.stackResponse();
    }

    /**
     * <p>
     * declare_blockers.
     * </p>
     */
    @Override
    public final void declareBlockers() {
        final CardList blockers = AllZoneUtil.getCreaturesInPlay(AllZone.getComputerPlayer());

        AllZone.setCombat(ComputerUtilBlock.getBlockers(AllZone.getCombat(), blockers));

        Singletons.getModel().getGameState().getPhaseHandler().setNeedToNextPhase(true);
    }

    /**
     * <p>
     * declare_blockers_after.
     * </p>
     */
    @Override
    public final void declareBlockersAfter() {
        this.stackResponse();
    }

    /**
     * <p>
     * end_of_combat.
     * </p>
     */
    @Override
    public final void endOfCombat() {
        this.stackResponse();
    }

    // end of Human's turn
    /**
     * <p>
     * end_of_turn.
     * </p>
     */
    @Override
    public final void endOfTurn() {
        this.stackResponse();
    }

    /**
     * <p>
     * stack_not_empty.
     * </p>
     */
    @Override
    public final void stackNotEmpty() {
        this.stackResponse();
    }

    /**
     * <p>
     * stackResponse.
     * </p>
     */
    public final void stackResponse() {
        // if top of stack is empty
        final CardList cards = getAvailableSpellAbilities();
        if (AllZone.getStack().size() == 0) {
            final ArrayList<SpellAbility> sas = this.getSpellAbilities(cards);
            boolean pass = (sas.size() == 0)
                    || Singletons.getModel().getGameState().getPhaseHandler().is(Constant.Phase.END_OF_TURN, AllZone.getComputerPlayer());
            if (!pass) { // Each AF should check the phase individually
                pass = ComputerUtil.playSpellAbilities(sas);
            }

            if (pass) {
                Singletons.getModel().getGameState().getPhaseHandler().passPriority();
            }
            return;
        }

        // if top of stack is owned by me
        if (AllZone.getStack().peekInstance().getActivatingPlayer().isComputer()) {
            // probably should let my stuff resolve to force Human to respond to
            // it
            Singletons.getModel().getGameState().getPhaseHandler().passPriority();
            return;
        }

        // top of stack is owned by human,
        ArrayList<SpellAbility> possibleCounters = getPlayableCounters(cards);

        if ((possibleCounters.size() > 0) && ComputerUtil.playCounterSpell(possibleCounters)) {
            // Responding CounterSpell is on the Stack trying to Counter the Spell
            // If playCounterSpell returns true, a Spell is hitting the Stack
            return;
        }

        possibleCounters.clear();
        possibleCounters = this.getPossibleETBCounters();
        if ((possibleCounters.size() > 0) && !ComputerUtil.playSpellAbilities(possibleCounters)) {
            // Responding Permanent w/ ETB Counter is on the Stack
            // Singletons.getModel().getGameState().getPhaseHandler().passPriority();
            return;
        }
        final ArrayList<SpellAbility> sas = this.getSpellAbilities(cards);
        if (sas.size() > 0) {
            // Spell not Countered
            if (!ComputerUtil.playSpellAbilities(sas)) {
                return;
            }
        }
        // if this hasn't been covered above, just PassPriority()
        Singletons.getModel().getGameState().getPhaseHandler().passPriority();
    }
}
