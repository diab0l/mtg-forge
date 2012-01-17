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

import forge.Constant.Zone;
import forge.card.spellability.Ability;
import forge.card.spellability.SpellAbility;
import forge.gui.GuiUtils;

/**
 * <p>
 * Handles "until end of turn" effects and "at end of turn" triggers.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class EndOfTurn extends Phase implements java.io.Serializable {
    /** Constant <code>serialVersionUID=-3656715295379727275L</code>. */
    private static final long serialVersionUID = -3656715295379727275L;

    /**
     * <p>
     * Handles all the hardcoded events that happen "at end of turn".
     * </p>
     */
    @Override
    public final void executeAt() {

        // TODO - should this freeze the Stack?

        // Pyrohemia and Pestilence
        final CardList all = AllZoneUtil.getCardsIn(Zone.Battlefield);

        EndOfTurn.endOfTurnWallOfReverence();
        EndOfTurn.endOfTurnLighthouseChronologist();

        // reset mustAttackEntity for me
        AllZone.getPhaseHandler().getPlayerTurn().setMustAttackEntity(null);

        EndOfTurn.removeAttackedBlockedThisTurn();

        AllZone.getStaticEffects().rePopulateStateBasedList();

        for (final Card c : all) {
            if (!c.isFaceDown() && c.hasKeyword("At the beginning of the end step, sacrifice CARDNAME.")) {
                final Card card = c;
                final SpellAbility sac = new Ability(card, "0") {
                    @Override
                    public void resolve() {
                        if (AllZoneUtil.isCardInPlay(card)) {
                            AllZone.getGameAction().sacrifice(card);
                        }
                    }
                };
                final StringBuilder sb = new StringBuilder();
                sb.append("Sacrifice ").append(card);
                sac.setStackDescription(sb.toString());

                AllZone.getStack().addSimultaneousStackEntry(sac);

            }
            if (!c.isFaceDown() && c.hasKeyword("At the beginning of the end step, exile CARDNAME.")) {
                final Card card = c;
                final SpellAbility exile = new Ability(card, "0") {
                    @Override
                    public void resolve() {
                        if (AllZoneUtil.isCardInPlay(card)) {
                            AllZone.getGameAction().exile(card);
                        }
                    }
                };
                final StringBuilder sb = new StringBuilder();
                sb.append("Exile ").append(card);
                exile.setStackDescription(sb.toString());

                AllZone.getStack().addSimultaneousStackEntry(exile);

            }
            if (!c.isFaceDown() && c.hasKeyword("At the beginning of the end step, destroy CARDNAME.")) {
                final Card card = c;
                final SpellAbility destroy = new Ability(card, "0") {
                    @Override
                    public void resolve() {
                        if (AllZoneUtil.isCardInPlay(card)) {
                            AllZone.getGameAction().destroy(card);
                        }
                    }
                };
                final StringBuilder sb = new StringBuilder();
                sb.append("Destroy ").append(card);
                destroy.setStackDescription(sb.toString());

                AllZone.getStack().addSimultaneousStackEntry(destroy);

            }
            // Berserk is using this, so don't check isFaceDown()
            if (c.hasKeyword("At the beginning of the next end step, destroy CARDNAME if it attacked this turn.")) {
                if (c.getCreatureAttackedThisTurn()) {
                    final Card card = c;
                    final SpellAbility sac = new Ability(card, "0") {
                        @Override
                        public void resolve() {
                            if (AllZoneUtil.isCardInPlay(card)) {
                                AllZone.getGameAction().destroy(card);
                            }
                        }
                    };
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Destroy ").append(card);
                    sac.setStackDescription(sb.toString());

                    AllZone.getStack().addSimultaneousStackEntry(sac);

                } else {
                    c.removeAllExtrinsicKeyword("At the beginning of the next end step, "
                            + "destroy CARDNAME if it attacked this turn.");
                }
            }
            if (c.hasKeyword("An opponent gains control of CARDNAME at the beginning of the next end step.")) {
                final Card vale = c;
                final SpellAbility change = new Ability(vale, "0") {
                    @Override
                    public void resolve() {
                        if (AllZoneUtil.isCardInPlay(vale)) {
                            vale.addController(vale.getController().getOpponent());
                            // AllZone.getGameAction().changeController(
                            // new CardList(vale), vale.getController(),
                            // vale.getController().getOpponent());

                            vale.removeAllExtrinsicKeyword("An opponent gains control of CARDNAME "
                                    + "at the beginning of the next end step.");
                        }
                    }
                };
                final StringBuilder sb = new StringBuilder();
                sb.append(vale.getName()).append(" changes controllers.");
                change.setStackDescription(sb.toString());

                AllZone.getStack().addSimultaneousStackEntry(change);

            }
            if (c.getName().equals("Erg Raiders") && !c.getCreatureAttackedThisTurn() && !c.hasSickness()
                    && AllZone.getPhaseHandler().isPlayerTurn(c.getController())) {
                final Card raider = c;
                final SpellAbility change = new Ability(raider, "0") {
                    @Override
                    public void resolve() {
                        if (AllZoneUtil.isCardInPlay(raider)) {
                            raider.getController().addDamage(2, raider);
                        }
                    }
                };
                final StringBuilder sb = new StringBuilder();
                sb.append(raider).append(" deals 2 damage to controller.");
                change.setStackDescription(sb.toString());

                AllZone.getStack().addSimultaneousStackEntry(change);

            }
            if (c.hasKeyword("At the beginning of your end step, return CARDNAME to its owner's hand.")
                    && AllZone.getPhaseHandler().isPlayerTurn(c.getController())) {
                final Card source = c;
                final SpellAbility change = new Ability(source, "0") {
                    @Override
                    public void resolve() {
                        if (AllZoneUtil.isCardInPlay(source)) {
                            AllZone.getGameAction().moveToHand(source);
                        }
                    }
                };
                final StringBuilder sb = new StringBuilder();
                sb.append(source).append(" - At the beginning of your end step, return CARDNAME to its owner's hand.");
                change.setStackDescription(sb.toString());

                AllZone.getStack().addSimultaneousStackEntry(change);

            }

        }

        this.execute(this.at);

    } // executeAt()

    private static void endOfTurnWallOfReverence() {
        final Player player = AllZone.getPhaseHandler().getPlayerTurn();
        final CardList list = player.getCardsIn(Zone.Battlefield, "Wall of Reverence");

        Ability ability;
        for (int i = 0; i < list.size(); i++) {
            final Card card = list.get(i);
            ability = new Ability(list.get(i), "0") {
                @Override
                public void resolve() {
                    CardList creats = AllZoneUtil.getCreaturesInPlay(player);
                    creats = creats.getTargetableCards(this);
                    if (creats.size() == 0) {
                        return;
                    }

                    if (player.isHuman()) {
                        final Object o = GuiUtils.getChoiceOptional(
                                "Select target creature for Wall of Reverence life gain", creats.toArray());
                        if (o != null) {
                            final Card c = (Card) o;
                            final int power = c.getNetAttack();
                            player.gainLife(power, card);
                        }
                    } else { // computer
                        CardListUtil.sortAttack(creats);
                        final Card c = creats.get(0);
                        if (c != null) {
                            final int power = c.getNetAttack();
                            player.gainLife(power, card);
                        }
                    }
                } // resolve
            }; // ability

            final StringBuilder sb = new StringBuilder();
            sb.append(card).append(" - ").append(player).append(" gains life equal to target creature's power.");
            ability.setStackDescription(sb.toString());

            AllZone.getStack().addSimultaneousStackEntry(ability);

        }
    } // endOfTurnWallOfReverence()

    private static void endOfTurnLighthouseChronologist() {
        final Player player = AllZone.getPhaseHandler().getPlayerTurn();
        final Player opponent = player.getOpponent();
        CardList list = opponent.getCardsIn(Zone.Battlefield);

        list = list.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.getName().equals("Lighthouse Chronologist") && (c.getCounters(Counters.LEVEL) >= 7);
            }
        });

        Ability ability;
        for (int i = 0; i < list.size(); i++) {
            final Card card = list.get(i);
            ability = new Ability(list.get(i), "0") {
                @Override
                public void resolve() {
                    AllZone.getPhaseHandler().addExtraTurn(card.getController());
                }
            };

            final StringBuilder sb = new StringBuilder();
            sb.append(card).append(" - ").append(card.getController()).append(" takes an extra turn.");
            ability.setStackDescription(sb.toString());

            AllZone.getStack().addSimultaneousStackEntry(ability);

        }
    }

    private static void removeAttackedBlockedThisTurn() {
        // resets the status of attacked/blocked this turn
        final Player player = AllZone.getPhaseHandler().getPlayerTurn();
        final CardList list = AllZoneUtil.getCreaturesInPlay(player);

        for (int i = 0; i < list.size(); i++) {
            final Card c = list.get(i);
            if (c.getCreatureAttackedThisCombat()) {
                c.setCreatureAttackedThisCombat(false);
            }
            if (c.getCreatureBlockedThisCombat()) {
                c.setCreatureBlockedThisCombat(false);
                // do not reset setCreatureAttackedThisTurn(), this appears to
                // be combat specific
            }

            if (c.getCreatureGotBlockedThisCombat()) {
                c.setCreatureGotBlockedThisCombat(false);
            }
        }
    }

} // end class EndOfTurn
