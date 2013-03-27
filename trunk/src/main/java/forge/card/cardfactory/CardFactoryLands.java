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
package forge.card.cardfactory;

import java.util.List;

import javax.swing.JOptionPane;

import forge.Card;
import forge.CardLists;
import forge.Command;
import forge.FThreads;
import forge.Singletons;
import forge.control.input.InputSelectCards;
import forge.game.player.Player;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.gui.GuiDialog;

/**
 * <p>
 * CardFactoryLands class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
class CardFactoryLands {

    /** 
     * TODO: Write javadoc for this type.
     *
     */
    private static final class InputRevealCardType extends InputSelectCards {
        private final String type;
        private final Card card;
        private static final long serialVersionUID = -2774066137824255680L;

        /**
         * TODO: Write javadoc for Constructor.
         * @param min
         * @param max
         * @param type
         * @param card
         */
        private InputRevealCardType(int min, int max, String type, Card card) {
            super(min, max);
            this.type = type;
            this.card = card;
        }

        @Override
        public String getMessage() {
            return card.getName() + " - Reveal a card.";
        }

        @Override
        protected boolean isValidChoice(Card c) {
            Zone zone = Singletons.getModel().getGame().getZoneOf(c);
            return zone.is(ZoneType.Hand) && c.isType(type) && c.getController() == card.getController();
        }
    }

    /**
     * <p>
     * getCard.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @param cardName
     *            a {@link java.lang.String} object.
     * @param cf
     *            a {@link forge.card.cardfactory.CardFactoryInterface} object.
     * @return a {@link forge.Card} object.
     */
    public static void buildCard(final Card card, final String cardName) {

        // *************** START *********** START **************************
        // Ravinca Dual Lands
        if (cardName.equals("Blood Crypt") || cardName.equals("Breeding Pool") || cardName.equals("Godless Shrine")
                || cardName.equals("Hallowed Fountain") || cardName.equals("Overgrown Tomb")
                || cardName.equals("Sacred Foundry") || cardName.equals("Steam Vents")
                || cardName.equals("Stomping Ground") || cardName.equals("Temple Garden")
                || cardName.equals("Watery Grave")) {
            // if this isn't done, computer plays more than 1 copy
            card.clearSpellKeepManaAbility();

            card.addComesIntoPlayCommand(new Command() {
                private static final long serialVersionUID = 7352127748114888255L;

                @Override
                public void execute() {
                    if (card.getController().isHuman()) {
                        this.humanExecute();
                    } else {
                        this.computerExecute();
                    }
                }

                public void computerExecute() {
                    boolean needsTheMana = false;
                    final Player ai = card.getController();
                    if (ai.getLife() > 3 && ai.canPayLife(2)) {
                        final int landsize = ai.getLandsInPlay().size();
                        for (Card c : ai.getCardsIn(ZoneType.Hand)) {
                            if (landsize == c.getCMC()) {
                                needsTheMana = true;
                            }
                        }
                    }
                    if (needsTheMana) {
                        ai.payLife(2, card);
                    } else {
                        this.tapCard();
                    }
                }

                public void humanExecute() {
                    final Player human = card.getController();
                    if (human.canPayLife(2)) {

                        final String question = String.format("Pay 2 life? If you don't, %s enters the battlefield tapped.", card.getName());

                        if (GuiDialog.confirm(card, question.toString())) {
                            human.payLife(2, card);
                        } else {
                            this.tapCard();
                        }
                    } // if
                    else {
                        this.tapCard();
                    }
                } // execute()

                private void tapCard() {
                    // it enters the battlefield this way, and should not fire triggers
                    card.setTapped(true);
                }
            });
        } // *************** END ************ END **************************

        // *************** START *********** START **************************
        // Lorwyn Dual Lands, and a couple Morningtide...
        else if (cardName.equals("Ancient Amphitheater") || cardName.equals("Auntie's Hovel")
                || cardName.equals("Gilt-Leaf Palace") || cardName.equals("Secluded Glen")
                || cardName.equals("Wanderwine Hub") || cardName.equals("Rustic Clachan")
                || cardName.equals("Murmuring Bosk") || cardName.equals("Primal Beyond")) {

            String shortTemp = "";
            if (cardName.equals("Ancient Amphitheater")) {
                shortTemp = "Giant";
            }
            else if (cardName.equals("Auntie's Hovel")) {
                shortTemp = "Goblin";
            }
            else if (cardName.equals("Gilt-Leaf Palace")) {
                shortTemp = "Elf";
            }
            else if (cardName.equals("Secluded Glen")) {
                shortTemp = "Faerie";
            }
            else if (cardName.equals("Wanderwine Hub")) {
                shortTemp = "Merfolk";
            }
            else if (cardName.equals("Rustic Clachan")) {
                shortTemp = "Kithkin";
            }
            else if (cardName.equals("Murmuring Bosk")) {
                shortTemp = "Treefolk";
            }
            else if (cardName.equals("Primal Beyond")) {
                shortTemp = "Elemental";
            }

            final String type = shortTemp;

            card.addComesIntoPlayCommand(new Command() {
                private static final long serialVersionUID = -5646344170306812481L;

                @Override
                public void execute() {
                    if (card.getController().isHuman()) {
                        this.humanExecute();
                    } else {
                        this.computerExecute();
                    }
                }

                public void computerExecute() {
                    List<Card> hand = CardLists.getType(card.getController().getCardsIn(ZoneType.Hand), type);
                    if (hand.size() > 0) {
                        this.revealCard(hand.get(0));
                    } else {
                        card.setTapped(true);
                    }
                }

                public void humanExecute() {
                    InputSelectCards inp = new InputRevealCardType(0, 1, type, card);
                    FThreads.setInputAndWait(inp);
                    
                    if ( inp.hasCancelled() || inp.getSelected().isEmpty() ) {
                        card.setTapped(true);
                    } else {
                        String cardName = inp.getSelected().get(0).getName();
                        JOptionPane.showMessageDialog(null, "Revealed card: " + cardName, cardName, JOptionPane.PLAIN_MESSAGE);
                    }

                } // execute()

                private void revealCard(final Card c) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(c.getController()).append(" reveals ").append(c.getName());
                    JOptionPane.showMessageDialog(null, sb.toString(), card.getName(), JOptionPane.PLAIN_MESSAGE);
                }
            });
        } // *************** END ************ END **************************
    }

} // end class CardFactoryLands
