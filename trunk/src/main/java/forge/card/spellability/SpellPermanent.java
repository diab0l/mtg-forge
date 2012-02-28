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

import forge.AllZone;
import forge.AllZoneUtil;
import forge.ButtonUtil;
import forge.Card;
import forge.CardList;
import forge.CardUtil;
import forge.Command;
import forge.CommandReturn;
import forge.ComputerAIGeneral;
import forge.ComputerUtil;
import forge.Constant;
import forge.Constant.Zone;
import forge.Player;
import forge.Singletons;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.trigger.Trigger;
import forge.control.input.Input;

/**
 * <p>
 * Spell_Permanent class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class SpellPermanent extends Spell {
    /** Constant <code>serialVersionUID=2413495058630644447L</code>. */
    private static final long serialVersionUID = 2413495058630644447L;

    private boolean willChampion = false;
    private String championValid = null;
    private String championValidDesc = "";

    /** The champion input comes. */
    private final Input championInputComes = new Input() {
        private static final long serialVersionUID = -7503268232821397107L;

        @Override
        public void showMessage() {
            final CardList choice = (CardList) SpellPermanent.this.championGetCreature.execute();

            this.stopSetNext(CardFactoryUtil.inputTargetChampionSac(SpellPermanent.this.getSourceCard(),
                    SpellPermanent.this.championAbilityComes, choice, "Select another "
                            + SpellPermanent.this.championValidDesc + " you control to exile", false, false));
            ButtonUtil.disableAll(); // target this card means: sacrifice this
                                     // card
        }
    };

    private final CommandReturn championGetCreature = new CommandReturn() {
        @Override
        public Object execute() {
            final CardList cards = SpellPermanent.this.getSourceCard().getController().getCardsIn(Zone.Battlefield);
            return cards.getValidCards(SpellPermanent.this.championValid, SpellPermanent.this.getSourceCard()
                    .getController(), SpellPermanent.this.getSourceCard());
        }
    }; // CommandReturn

    /** The champion ability comes. */
    private final SpellAbility championAbilityComes = new Ability(this.getSourceCard(), "0") {
        @Override
        public void resolve() {

            final Card source = this.getSourceCard();
            final Player controller = source.getController();

            final CardList creature = (CardList) SpellPermanent.this.championGetCreature.execute();
            if (creature.size() == 0) {
                Singletons.getModel().getGameAction().sacrifice(source);
                return;
            } else if (controller.isHuman()) {
                AllZone.getInputControl().setInput(SpellPermanent.this.championInputComes);
            } else { // Computer
                CardList computer = AllZone.getComputerPlayer().getCardsIn(Zone.Battlefield);
                computer = computer.getValidCards(SpellPermanent.this.championValid, controller, source);
                computer.remove(source);

                computer.shuffle();
                if (computer.size() != 0) {
                    final Card c = computer.get(0);
                    source.setChampionedCard(c);
                    if (AllZoneUtil.isCardInPlay(c)) {
                        Singletons.getModel().getGameAction().exile(c);
                    }

                    // Run triggers
                    final HashMap<String, Object> runParams = new HashMap<String, Object>();
                    runParams.put("Card", source);
                    runParams.put("Championed", source.getChampionedCard());
                    AllZone.getTriggerHandler().runTrigger("Championed", runParams);
                } else {
                    Singletons.getModel().getGameAction().sacrifice(this.getSourceCard());
                }
            } // computer
        } // resolve()
    };

    /** The champion command comes. */
    private final Command championCommandComes = new Command() {

        private static final long serialVersionUID = -3580408066322945328L;

        @Override
        public void execute() {
            final StringBuilder sb = new StringBuilder();
            sb.append(SpellPermanent.this.getSourceCard()).append(
                    " - When CARDNAME enters the battlefield, sacrifice it unless you exile a creature you control.");
            SpellPermanent.this.championAbilityComes.setStackDescription(sb.toString());
            AllZone.getStack().addSimultaneousStackEntry(SpellPermanent.this.championAbilityComes);
        } // execute()
    }; // championCommandComes

    /** The champion command leaves play. */
    private final Command championCommandLeavesPlay = new Command() {

        private static final long serialVersionUID = -5903638227914705191L;

        @Override
        public void execute() {

            final SpellAbility ability = new Ability(SpellPermanent.this.getSourceCard(), "0") {
                @Override
                public void resolve() {
                    final Card c = this.getSourceCard().getChampionedCard();
                    if ((c != null) && !c.isToken() && AllZoneUtil.isCardExiled(c)) {
                        Singletons.getModel().getGameAction().moveToPlay(c);
                    }
                } // resolve()
            }; // SpellAbility

            final StringBuilder sb = new StringBuilder();
            sb.append(SpellPermanent.this.getSourceCard()).append(
                    " - When CARDNAME leaves the battlefield, exiled card returns to the battlefield.");
            ability.setStackDescription(sb.toString());

            AllZone.getStack().addSimultaneousStackEntry(ability);
        } // execute()
    }; // championCommandLeavesPlay

    // /////
    // //////////////////

    /**
     * <p>
     * Constructor for Spell_Permanent.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     */
    public SpellPermanent(final Card sourceCard) {
        // Add Costs for all SpellPermanents
        this(sourceCard, new Cost(sourceCard.getManaCost(), sourceCard.getName(), false), null);
    } // Spell_Permanent()

    /**
     * <p>
     * Constructor for Spell_Permanent.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param cost
     *            a {@link forge.card.cost.Cost} object.
     * @param tgt
     *            a {@link forge.card.spellability.Target} object.
     */
    public SpellPermanent(final Card sourceCard, final Cost cost, final Target tgt) {
        this(sourceCard, cost, tgt, true);
    } // Spell_Permanent()

    /**
     * Instantiates a new spell_ permanent.
     * 
     * @param sourceCard
     *            the source card
     * @param cost
     *            the cost
     * @param tgt
     *            the tgt
     * @param setDesc
     *            the set desc
     */
    public SpellPermanent(final Card sourceCard, final Cost cost, final Target tgt, final boolean setDesc) {
        super(sourceCard, cost, tgt);

        if (CardFactoryUtil.hasKeyword(sourceCard, "Champion") != -1) {
            final int n = CardFactoryUtil.hasKeyword(sourceCard, "Champion");

            final String toParse = sourceCard.getKeyword().get(n).toString();
            final String[] parsed = toParse.split(":");
            this.willChampion = true;
            this.championValid = parsed[1];
            if (parsed.length > 2) {
                this.championValidDesc = parsed[2];
            } else {
                this.championValidDesc = this.championValid;
            }
        }

        if (sourceCard.isCreature()) {

            final StringBuilder sb = new StringBuilder();
            sb.append(sourceCard.getName()).append(" - Creature ").append(sourceCard.getNetAttack());
            sb.append(" / ").append(sourceCard.getNetDefense());
            this.setStackDescription(sb.toString());
        } else {
            this.setStackDescription(sourceCard.getName());
        }

        if (setDesc) {
            this.setDescription(this.getStackDescription());
        }

        if (this.willChampion) {
            sourceCard.addComesIntoPlayCommand(this.championCommandComes);
            sourceCard.addLeavesPlayCommand(this.championCommandLeavesPlay);
        }

    } // Spell_Permanent()

    /** {@inheritDoc} */
    @Override
    public boolean canPlay() {
        final Card source = this.getSourceCard();

        final Player turn = AllZone.getPhaseHandler().getPlayerTurn();

        if (source.getName().equals("Serra Avenger")) {
            if (turn.equals(source.getController()) && (turn.getTurn() <= 3)) {
                return false;
            }
        }

        // Flash handled by super.canPlay
        return super.canPlay();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPlayAI() {

        final Card card = this.getSourceCard();
        String mana = this.getPayCosts().getTotalMana();

        if (mana.contains("X")) {
            // Set PayX here to maximum value.
            final int xPay = ComputerUtil.determineLeftoverMana(this);
            if (xPay <= 0) {
                return false;
            }
            card.setSVar("PayX", Integer.toString(xPay));
        }
        // Wait for Main2 if possible
        if (AllZone.getPhaseHandler().is(Constant.Phase.MAIN1)) {
            boolean wait = true;
            if (card.getSVar("PlayMain1").equals("TRUE")) {
                wait = false;
            }
            if ((card.isCreature() && (ComputerAIGeneral.hasACardGivingHaste() 
                    || card.hasKeyword("Haste"))) || card.hasKeyword("Exalted")) {
                wait = false;
            }
            // get all cards the computer controls with BuffedBy
            final CardList buffed = AllZone.getComputerPlayer().getCardsIn(Zone.Battlefield);
            for (int j = 0; j < buffed.size(); j++) {
                final Card buffedcard = buffed.get(j);
                if (buffedcard.getSVar("BuffedBy").length() > 0) {
                    final String buffedby = buffedcard.getSVar("BuffedBy");
                    final String[] bffdby = buffedby.split(",");
                    if (card.isValid(bffdby, buffedcard.getController(), buffedcard)) {
                        wait = false;
                    }
                }
            } // BuffedBy

            // get all cards the human controls with AntiBuffedBy
            final CardList antibuffed = AllZone.getHumanPlayer().getCardsIn(Zone.Battlefield);
            for (int k = 0; k < antibuffed.size(); k++) {
                final Card buffedcard = antibuffed.get(k);
                if (buffedcard.getSVar("AntiBuffedBy").length() > 0) {
                    final String buffedby = buffedcard.getSVar("AntiBuffedBy");
                    final String[] bffdby = buffedby.split(",");
                    if (card.isValid(bffdby, buffedcard.getController(), buffedcard)) {
                        wait = false;
                    }
                }
            } // AntiBuffedBy
            final CardList vengevines = AllZone.getComputerPlayer().getCardsIn(Zone.Graveyard, "Vengevine");
            if (vengevines.size() > 0) {
                final CardList creatures = AllZone.getComputerPlayer().getCardsIn(Zone.Hand);
                final CardList creatures2 = new CardList();
                for (int i = 0; i < creatures.size(); i++) {
                    if (creatures.get(i).isCreature()
                            && (CardUtil.getConvertedManaCost(creatures.get(i).getManaCost()) <= 3)) {
                        creatures2.add(creatures.get(i));
                    }
                }
                if (((creatures2.size() + CardUtil.getThisTurnCast("Creature.YouCtrl", vengevines.get(0))
                        .size()) > 1)
                        && card.isCreature()
                        && (CardUtil.getConvertedManaCost(card.getManaCost()) <= 3)) {
                    wait = false;
                }
            } // AI Improvement for Vengevine Beached As End
            if (wait) {
                return false;
            }
        }
        // save cards with flash for surprise blocking
        if (card.hasKeyword("Flash")
                && !ComputerAIGeneral.hasETBTrigger(card)
                && (AllZone.getPhaseHandler().isPlayerTurn(AllZone.getComputerPlayer()) || AllZone.getPhaseHandler()
                        .isBefore(Constant.Phase.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY))) {
            return false;
        }
        // Prevent the computer from summoning Ball Lightning type creatures after attacking
        if (card.hasKeyword("At the beginning of the end step, sacrifice CARDNAME.")
                && (AllZone.getPhaseHandler().isPlayerTurn(AllZone.getHumanPlayer()) || AllZone.getPhaseHandler()
                        .isAfter(Constant.Phase.COMBAT_DECLARE_ATTACKERS))) {
            return false;
        }

        return canPlayFromEffectAI(false, true);
    } // canPlayAI()
    
    /** {@inheritDoc} */
    @Override
    public boolean canPlayFromEffectAI(final boolean mandatory, final boolean withOutManaCost) {
        if (mandatory) {
            return true;
        }
        final Card card = this.getSourceCard();
        String mana = this.getPayCosts().getTotalMana();
        // check on legendary
        if (card.isType("Legendary")) {
            final CardList list = AllZone.getComputerPlayer().getCardsIn(Zone.Battlefield);
            if (list.containsName(card.getName())) {
                return false;
            }
        }
        if (card.isPlaneswalker()) {
            CardList list = AllZone.getComputerPlayer().getCardsIn(Zone.Battlefield);
            list = list.getType("Planeswalker");

            for (int i = 0; i < list.size(); i++) {
                final String subtype = card.getType().get(card.getType().size() - 1);
                final CardList cl = list.getType(subtype);

                if (cl.size() > 0) {
                    return false;
                }
            }
        }
        if (card.isType("World")) {
            CardList list = AllZone.getComputerPlayer().getCardsIn(Zone.Battlefield);
            list = list.getType("World");
            if (list.size() > 0) {
                return false;
            }
        }

        if (card.isCreature() && (card.getNetDefense() <= 0) && !card.hasStartOfKeyword("etbCounter")
                && !card.getText().contains("Modular") && !mana.contains("X")) {
            return false;
        }

        if (this.willChampion) {
            final Object o = this.championGetCreature.execute();
            if (o == null) {
                return false;
            }

            final CardList cl = (CardList) this.championGetCreature.execute();
            if ((o == null) || !(cl.size() > 0) || !this.getSourceCard().isInZone(Constant.Zone.Hand)) {
                return false;
            }
        }
        if (!SpellPermanent.checkETBEffects(card, this, null)) {
            return false;
        }
        return super.canPlayAI();
    }

    /**
     * <p>
     * checkETBEffects.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param api
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean checkETBEffects(final Card card, final SpellAbility sa, final String api) {

        if (card.isCreature() && AllZoneUtil.isCardInPlay("Torpor Orb")) {
            return true;
        }

        // Trigger play improvements
        final ArrayList<Trigger> triggers = card.getTriggers();
        for (final Trigger tr : triggers) {
            // These triggers all care for ETB effects

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

            if (!tr.requirementsCheck()) {
                continue;
            }

            if (tr.getOverridingAbility() != null) {
                // Abilities yet
                continue;
            }

            // Maybe better considerations
            final AbilityFactory af = new AbilityFactory();
            final String execute = params.get("Execute");
            if (execute == null) {
                continue;
            }
            final SpellAbility exSA = af.getAbility(card.getSVar(execute), card);

            if ((api != null) && !af.getAPI().equals(api)) {
                continue;
            }

            if (sa != null) {
                exSA.setActivatingPlayer(sa.getActivatingPlayer());
            } else {
                exSA.setActivatingPlayer(AllZone.getComputerPlayer());
            }

            // Run non-mandatory trigger.
            // These checks only work if the Executing SpellAbility is an
            // Ability_Sub.
            if ((exSA instanceof AbilitySub) && !exSA.doTrigger(false)) {
                // AI would not run this trigger if given the chance

                // if trigger is mandatory, return false
                if (params.get("OptionalDecider") == null) {
                    return false;
                }
                // else
                // otherwise, return false 50% of the time?
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void resolve() {
        final Card c = this.getSourceCard();
        c.addController(this.getActivatingPlayer());
        Singletons.getModel().getGameAction().moveTo(this.getActivatingPlayer().getZone(Constant.Zone.Battlefield), c);
    }
}
