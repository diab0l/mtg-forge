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

import com.esotericsoftware.minlog.Log;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.Card;
import forge.CardList;
import forge.CardListFilter;
import forge.CardUtil;
import forge.Constant;
import forge.Constant.Zone;
import forge.Counters;
import forge.PhaseHandler;
import forge.Player;
import forge.PlayerZone;
import forge.Singletons;
import forge.card.cost.Cost;
import forge.card.spellability.Ability;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.gui.GuiUtils;

/**
 * <p>
 * CardFactory_Planeswalkers class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CardFactoryPlaneswalkers {

    /**
     * <p>
     * getCard.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @param cardName
     *            a {@link java.lang.String} object.
     * @return a {@link forge.Card} object.
     */
    public static Card getCard(final Card card, final String cardName) {
        // All Planeswalkers set their loyality in the beginning
        if (card.getBaseLoyalty() > 0) {
            card.addComesIntoPlayCommand(CardFactoryUtil.entersBattleFieldWithCounters(card, Counters.LOYALTY,
                    card.getBaseLoyalty()));
        }

        // *************** START *********** START **************************
        if (cardName.equals("Sarkhan the Mad")) {

            // Planeswalker book-keeping
            final int[] turn = new int[1];
            turn[0] = -1;

            // ability1
            /*
             * 0: Reveal the top card of your library and put it into your hand.
             * Sarkhan the Mad deals damage to himself equal to that card's
             * converted mana cost.
             */
            final SpellAbility ability1 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.addCounterFromNonEffect(Counters.LOYALTY, 0);
                    turn[0] = Singletons.getModel().getGameState().getPhaseHandler().getTurn();

                    final Player player = card.getController();
                    final PlayerZone lib = player.getZone(Constant.Zone.Library);

                    final Card topCard = lib.get(0);
                    final int convertedManaTopCard = CardUtil.getConvertedManaCost(topCard.getManaCost());
                    final CardList showTop = new CardList();
                    showTop.add(topCard);
                    GuiUtils.chooseOneOrNone("Revealed top card: ", showTop.toArray());

                    // now, move it to player's hand
                    Singletons.getModel().getGameAction().moveToHand(topCard);

                    // now, do X damage to Sarkhan
                    card.addDamage(convertedManaTopCard, card);

                } // resolve()

                @Override
                public boolean canPlayAI() {
                    // the computer isn't really smart enough to play this
                    // effectively, and it doesn't really
                    // help unless there are no cards in his hand
                    return false;
                }

                @Override
                public boolean canPlay() {
                    // looks like standard Planeswalker stuff...
                    // maybe should check if library is empty, or 1 card?
                    return AllZone.getZoneOf(card).is(Constant.Zone.Battlefield)
                            && (turn[0] != Singletons.getModel().getGameState().getPhaseHandler().getTurn()) && PhaseHandler.canCastSorcery(card.getController());
                } // canPlay()
            };
            final StringBuilder ab1 = new StringBuilder();
            ab1.append("0: Reveal the top card of your library and put it ");
            ab1.append("into your hand. Sarkhan the Mad deals damage ");
            ab1.append("to himself equal to that card's converted mana cost.");
            ability1.setDescription(ab1.toString());

            final StringBuilder stack1 = new StringBuilder();
            stack1.append(card.getName()).append(" - Reveal top card and do damage.");
            ability1.setStackDescription(stack1.toString());

            // ability2
            /*
             * -2: Target creature's controller sacrifices it, then that player
             * puts a 5/5 red Dragon creature token with flying onto the
             * battlefield.
             */
            final Target target2 = new Target(card, "TgtC");
            final Cost cost2 = new Cost(card, "SubCounter<2/LOYALTY>", true);
            final SpellAbility ability2 = new AbilityActivated(card, cost2, target2) {
                private static final long serialVersionUID = 4322453486268967722L;

                @Override
                public void resolve() {
                    // card.subtractCounter(Counters.LOYALTY, 2);
                    turn[0] = Singletons.getModel().getGameState().getPhaseHandler().getTurn();

                    final Card target = this.getTargetCard();
                    Singletons.getModel().getGameAction().sacrifice(target);
                    // in makeToken, use target for source, so it goes into the
                    // correct Zone
                    CardFactoryUtil.makeToken("Dragon", "R 5 5 Dragon", target.getController(), "R", new String[] {
                            "Creature", "Dragon" }, 5, 5, new String[] { "Flying" });

                } // resolve()

                @Override
                public boolean canPlayAI() {
                    CardList creatures = AllZoneUtil.getCreaturesInPlay(AllZone.getComputerPlayer());
                    creatures = creatures.filter(new CardListFilter() {
                        @Override
                        public boolean addCard(final Card c) {
                            return !(c.isToken() && c.isType("Dragon"));
                        }
                    });
                    return creatures.size() >= 1;
                }

                @Override
                public void chooseTargetAI() {
                    CardList cards = AllZone.getComputerPlayer().getCardsIn(Zone.Battlefield);
                    // avoid targeting the dragon tokens we just put in play...
                    cards = cards.filter(new CardListFilter() {
                        @Override
                        public boolean addCard(final Card c) {
                            return !(c.isToken() && c.isType("Dragon"));
                        }
                    });
                    this.setTargetCard(CardFactoryUtil.getCheapestCreatureAI(cards, this, true));
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Sarkhan the Mad caused sacrifice of: ");
                    sb.append(CardFactoryUtil.getCheapestCreatureAI(cards, this, true));
                    Log.debug("Sarkhan the Mad", sb.toString());
                }

                @Override
                public boolean canPlay() {
                    return AllZone.getZoneOf(card).is(Constant.Zone.Battlefield)
                            && (card.getCounters(Counters.LOYALTY) >= 2) && (turn[0] != Singletons.getModel().getGameState().getPhaseHandler().getTurn())
                            && PhaseHandler.canCastSorcery(card.getController());
                } // canPlay()
            };
            final StringBuilder ab2 = new StringBuilder();
            ab2.append("-2: Target creature's controller sacrifices it, ");
            ab2.append("then that player puts a 5/5 red Dragon creature ");
            ab2.append("token with flying onto the battlefield.");
            ability2.setDescription(ab2.toString());

            // ability3
            /*
             * -4: Each Dragon creature you control deals damage equal to its
             * power to target player.
             */
            final Target target3 = new Target(card, "Select target player", "Player");
            final Cost cost3 = new Cost(card, "SubCounter<4/LOYALTY>", true);
            final SpellAbility ability3 = new AbilityActivated(card, cost3, target3) {
                private static final long serialVersionUID = -5488579738767048060L;

                @Override
                public void resolve() {
                    // card.subtractCounter(Counters.LOYALTY, 4);
                    turn[0] = Singletons.getModel().getGameState().getPhaseHandler().getTurn();

                    final Player target = this.getTargetPlayer();
                    final Player player = card.getController();
                    final CardList dragons = player.getCardsIn(Zone.Battlefield).getType("Dragon");
                    for (int i = 0; i < dragons.size(); i++) {
                        final Card dragon = dragons.get(i);
                        final int damage = dragon.getNetAttack();
                        target.addDamage(damage, dragon);
                    }

                } // resolve()

                @Override
                public boolean canPlayAI() {
                    this.setTargetPlayer(AllZone.getHumanPlayer());
                    final CardList dragons = AllZone.getComputerPlayer().getCardsIn(Zone.Battlefield).getType("Dragon");
                    return (card.getCounters(Counters.LOYALTY) >= 4) && (dragons.size() >= 1);
                }

                @Override
                public boolean canPlay() {
                    return AllZone.getZoneOf(card).is(Constant.Zone.Battlefield)
                            && (card.getCounters(Counters.LOYALTY) >= 4) && (turn[0] != Singletons.getModel().getGameState().getPhaseHandler().getTurn())
                            && PhaseHandler.canCastSorcery(card.getController());
                } // canPlay()
            };
            final StringBuilder ab3 = new StringBuilder();
            ab3.append("-4: Each Dragon creature you control ");
            ab3.append("deals damage equal to its power to target player.");
            ability3.setDescription(ab3.toString());

            card.addSpellAbility(ability1);
            card.addSpellAbility(ability2);
            card.addSpellAbility(ability3);

            card.setSVars(card.getSVars());
            card.setSets(card.getSets());

            return card;
        } // *************** END ************ END **************************

        return card;
    }

} // end class CardFactoryPlaneswalkers
