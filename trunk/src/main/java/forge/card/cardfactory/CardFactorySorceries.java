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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JOptionPane;

import com.google.common.collect.Iterables;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.FThreads;
import forge.CardPredicates.Presets;
import forge.Constant;
import forge.Singletons;
import forge.card.CardType;
import forge.card.cost.Cost;
import forge.card.mana.ManaCost;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.control.input.InputPayManaExecuteCommands;
import forge.control.input.InputSelectCards;
import forge.control.input.InputSelectCardsFromList;
import forge.game.GameState;
import forge.game.ai.ComputerUtil;
import forge.game.player.AIPlayer;
import forge.game.player.HumanPlayer;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.util.Aggregates;

/**
 * <p>
 * CardFactory_Sorceries class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CardFactorySorceries {

    private static final SpellAbility getBrilliantUltimatum(final Card card) {
        return new Spell(card) {
            private static final long serialVersionUID = 1481112451519L;

            @Override
            public void resolve() {

                Card choice = null;

                // check for no cards in hand on resolve
                final List<Card> lib = card.getController().getCardsIn(ZoneType.Library);
                final List<Card> cards = new ArrayList<Card>();
                final List<Card> exiled = new ArrayList<Card>();
                if (lib.size() == 0) {
                    JOptionPane.showMessageDialog(null, "No more cards in library.", "",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                int count = 5;
                if (lib.size() < 5) {
                    count = lib.size();
                }
                for (int i = 0; i < count; i++) {
                    cards.add(lib.get(i));
                }
                for (int i = 0; i < count; i++) {
                    exiled.add(lib.get(i));
                    Singletons.getModel().getGame().getAction().exile(lib.get(i));
                }
                final List<Card> pile1 = new ArrayList<Card>();
                final List<Card> pile2 = new ArrayList<Card>();
                boolean stop = false;
                int pile1CMC = 0;
                int pile2CMC = 0;

                final StringBuilder msg = new StringBuilder();
                msg.append("Revealing top ").append(count).append(" cards of library: ");
                GuiChoose.one(msg.toString(), cards);
                // Human chooses
                if (card.getController().isComputer()) {
                    for (int i = 0; i < count; i++) {
                        if (!stop) {
                            choice = GuiChoose.oneOrNone("Choose cards to put into the first pile: ",
                                    cards);
                            if (choice != null) {
                                pile1.add(choice);
                                cards.remove(choice);
                                pile1CMC = pile1CMC + choice.getCMC();
                            } else {
                                stop = true;
                            }
                        }
                    }
                    for (int i = 0; i < count; i++) {
                        if (!pile1.contains(exiled.get(i))) {
                            pile2.add(exiled.get(i));
                            pile2CMC = pile2CMC + exiled.get(i).getCMC();
                        }
                    }
                    final StringBuilder sb = new StringBuilder();
                    sb.append("You have spilt the cards into the following piles");
                    sb.append("\r\n").append("\r\n");
                    sb.append("Pile 1: ").append("\r\n");
                    for (int i = 0; i < pile1.size(); i++) {
                        sb.append(pile1.get(i).getName()).append("\r\n");
                    }
                    sb.append("\r\n").append("Pile 2: ").append("\r\n");
                    for (int i = 0; i < pile2.size(); i++) {
                        sb.append(pile2.get(i).getName()).append("\r\n");
                    }
                    JOptionPane.showMessageDialog(null, sb, "", JOptionPane.INFORMATION_MESSAGE);
                    if (pile1CMC >= pile2CMC) {
                        JOptionPane.showMessageDialog(null, "Computer chooses the Pile 1", "",
                                JOptionPane.INFORMATION_MESSAGE);
                        for (int i = 0; i < pile1.size(); i++) {
                            final List<SpellAbility> choices = pile1.get(i).getBasicSpells();

                            for (final SpellAbility sa : choices) {
                                if (sa.canPlayAI()) {
                                    ComputerUtil.playStackFree(sa.getActivatingPlayer(), sa);
                                    if (pile1.get(i).isPermanent()) {
                                        exiled.remove(pile1.get(i));
                                    }
                                    break;
                                }
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Computer chooses the Pile 2", "",
                                JOptionPane.INFORMATION_MESSAGE);
                        for (int i = 0; i < pile2.size(); i++) {
                            final List<SpellAbility> choices = pile2.get(i).getBasicSpells();

                            for (final SpellAbility sa : choices) {
                                if (sa.canPlayAI()) {
                                    ComputerUtil.playStackFree(sa.getActivatingPlayer(), sa);
                                    if (pile2.get(i).isPermanent()) {
                                        exiled.remove(pile2.get(i));
                                    }
                                    break;
                                }
                            }
                        }
                    }

                } else { // Computer chooses (It picks the highest converted
                         // mana cost card and 1 random card.)
                    Card biggest = exiled.get(0);

                    for (final Card c : exiled) {
                        if (biggest.getManaCost().getCMC() < c.getManaCost().getCMC()) {
                            biggest = c;
                        }
                    }

                    pile1.add(biggest);
                    cards.remove(biggest);
                    if (cards.size() > 2) {
                        final Card random = Aggregates.random(cards);
                        pile1.add(random);
                    }
                    for (int i = 0; i < count; i++) {
                        if (!pile1.contains(exiled.get(i))) {
                            pile2.add(exiled.get(i));
                        }
                    }
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Choose a pile to add to your hand: ");
                    sb.append("\r\n").append("\r\n");
                    sb.append("Pile 1: ").append("\r\n");
                    for (int i = 0; i < pile1.size(); i++) {
                        sb.append(pile1.get(i).getName()).append("\r\n");
                    }
                    sb.append("\r\n").append("Pile 2: ").append("\r\n");
                    for (int i = 0; i < pile2.size(); i++) {
                        sb.append(pile2.get(i).getName()).append("\r\n");
                    }
                    final Object[] possibleValues = { "Pile 1", "Pile 2" };
                    final Object q = JOptionPane.showOptionDialog(null, sb, "Brilliant Ultimatum",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, possibleValues,
                            possibleValues[0]);

                    List<Card> chosen;
                    if (q.equals(0)) {
                        chosen = pile1;
                    } else {
                        chosen = pile2;
                    }

                    final int numChosen = chosen.size();
                    for (int i = 0; i < numChosen; i++) {
                        final Card check = GuiChoose.oneOrNone("Select spells to play in reverse order: ", chosen);
                        if (check == null) {
                            break;
                        }

                        final Card playing = check;
                        if (playing.isLand()) {
                            if (card.getController().canPlayLand(playing)) {
                                card.getController().playLand(playing);
                            } else {
                                JOptionPane.showMessageDialog(null, "You can't play any more lands this turn.", "",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        } else {
                            ((HumanPlayer)card.getController()).playCardWithoutManaCost(playing);
                        }
                        chosen.remove(playing);
                    }

                }
                pile1.clear();
                pile2.clear();
            } // resolve()

            @Override
            public boolean canPlayAI() {
                final List<Card> cards = getActivatingPlayer().getCardsIn(ZoneType.Library);
                return cards.size() >= 8;
            }
        }; // SpellAbility

    }

    private static final void balanceLands(Spell card) {

        List<List<Card>> lands = new ArrayList<List<Card>>();
        for (Player p : Singletons.getModel().getGame().getPlayers()) {

            lands.add(p.getLandsInPlay());
        }

        int min = Integer.MAX_VALUE;
        for (List<Card> l : lands) {
            int s = l.size();
            min = Math.min(min, s);
        }
        Iterator<List<Card>> ll = lands.iterator();
        for (Player p : Singletons.getModel().getGame().getPlayers()) {

            List<Card> l = ll.next();
            int sac = l.size() - min;
            if (sac == 0) {
                continue;
            }
            if (p.isComputer()) {
                CardLists.shuffle(l);
                for (int i = 0; i < sac; i++) {
                    Singletons.getModel().getGame().getAction().sacrifice(l.get(i), card);
                }
            } else {
                final List<Card> list = CardLists.getType(p.getCardsIn(ZoneType.Battlefield), "Land");

                InputSelectCards inp = new InputSelectCardsFromList(sac, sac, list);
                inp.setMessage("Select %d more land(s) to sacrifice");
                FThreads.setInputAndWait(inp);
                for( Card crd : inp.getSelected() )
                    p.getGame().getAction().sacrifice(crd, card);
            }
        }
    }

    private static final void balanceHands(Spell spell) {
        int min = Integer.MAX_VALUE;
        for (Player p : Singletons.getModel().getGame().getPlayers()) {
            min = Math.min(min, p.getZone(ZoneType.Hand).size());
        }

        for (Player p : Singletons.getModel().getGame().getPlayers()) {
            List<Card> hand = p.getCardsIn(ZoneType.Hand);
            int sac = hand.size() - min;
            if (sac == 0) {
                continue;
            }
            if (p.isHuman()) {
                InputSelectCards sc = new InputSelectCardsFromList(sac, sac, hand);
                sc.setMessage("Select %d more card(s) to discard");
                FThreads.setInputAndWait(sc);
                for( Card c : sc.getSelected())
                    p.discard(c, spell);
            } else {
                final List<Card> toDiscard = ((AIPlayer)p).getAi().getCardsToDiscard(sac, (String[])null, spell);
                for (int i = 0; i < toDiscard.size(); i++) {
                    p.discard(toDiscard.get(i), spell);
                }
            }
        }
    }

    private static final void balanceCreatures(Spell card) {
        List<List<Card>> creats = new ArrayList<List<Card>>();
        for (Player p : Singletons.getModel().getGame().getPlayers()) {

            creats.add(p.getCreaturesInPlay());
        }
        int min = Integer.MAX_VALUE;
        for (List<Card> h : creats) {
            int s = h.size();
            min = Math.min(min, s);
        }
        Iterator<List<Card>> cc = creats.iterator();
        for (Player p : Singletons.getModel().getGame().getPlayers()) {

            List<Card> c = cc.next();
            int sac = c.size() - min;
            if (sac == 0) {
                continue;
            }
            if (p.isComputer()) {
                CardLists.sortByPowerAsc(c);
                CardLists.sortByCmcDesc(c);
                Collections.reverse(c);
                for (int i = 0; i < sac; i++) {
                    p.getGame().getAction().sacrifice(c.get(i), card);
                }
            } else {
                final List<Card> list = CardLists.getType(p.getCardsIn(ZoneType.Battlefield), "Creature");
                InputSelectCards inp = new InputSelectCardsFromList(sac, sac, list);
                inp.setMessage("Select %d more creature(s) to sacrifice");
                FThreads.setInputAndWait(inp);
                for( Card crd : inp.getSelected() )
                    p.getGame().getAction().sacrifice(crd, card);

            }
        }
    }
    
    private static final SpellAbility getBalance(final Card card) {
        return new Spell(card) {
            private static final long serialVersionUID = -5941893280103164961L;

            @Override
            public void resolve() {
                balanceLands(this);
                balanceHands(this);
                balanceCreatures(this);
            }

            @Override
            public boolean canPlayAI() {
                int diff = 0;
                final Player ai = getActivatingPlayer();
                final Player opp = ai.getOpponent();
                final List<Card> humLand = opp.getLandsInPlay();
                final List<Card> compLand = ai.getLandsInPlay();
                diff += humLand.size() - compLand.size();

                final List<Card> humCreats = opp.getCreaturesInPlay();
                List<Card> compCreats = ai.getCreaturesInPlay();
                compCreats = CardLists.filter(compCreats, CardPredicates.Presets.CREATURES);
                diff += 1.5 * (humCreats.size() - compCreats.size());

                final List<Card> humHand = opp.getCardsIn(ZoneType.Hand);
                final List<Card> compHand = ai.getCardsIn(ZoneType.Hand);
                diff += 0.5 * (humHand.size() - compHand.size());

                return diff > 2;
            }
        };
    }

    private static final SpellAbility getPatriarchsBidding(final Card card) {

        final SpellAbility spell = new Spell(card) {
            private static final long serialVersionUID = -2182173662547136798L;

            @Override
            public void resolve() {
                List<String> types = new ArrayList<String>();
                for (Player p : Singletons.getModel().getGame().getPlayers()) {
                    if (p.isHuman()) {
                         types.add(GuiChoose.one("Which creature type?", Constant.CardTypes.CREATURE_TYPES));
                    } else {
                        final HashMap<String, Integer> countInGraveyard = new HashMap<String, Integer>();
                        final List<Card> aiGrave = p.getCardsIn(ZoneType.Graveyard);
                        for (final Card c : Iterables.filter(aiGrave, CardPredicates.Presets.CREATURES)) {
                            for (final String type : c.getType()) {
                                if (CardType.isACreatureType(type)) {
                                    Integer oldVal = countInGraveyard.get(type);
                                    countInGraveyard.put(type, 1 + (oldVal != null ? oldVal : 0));
                                 }
                            }
                        }
                        String maxKey = "";
                        int maxCount = -1;
                        for (final Entry<String, Integer> entry : countInGraveyard.entrySet()) {
                            if (entry.getValue() > maxCount) {
                                maxKey = entry.getKey();
                                maxCount = entry.getValue();
                            }
                        }
                        types.add(maxKey.equals("") ? "Sliver" : maxKey);
                    }
                }

                List<Card> bidded = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Graveyard), CardPredicates.Presets.CREATURES);
                for (final Card c : bidded) {
                    for (int i = 0; i < types.size(); i++) {
                        if (c.isType(types.get(i))) {
                            Singletons.getModel().getGame().getAction().moveToPlay(c);
                            i = types.size(); // break inner loop
                        }
                    }
                }
            } // resolve()
        }; // SpellAbility
        final StringBuilder sb = new StringBuilder();
        sb.append(card.getName()).append(" - choose a creature type.");
        spell.setStackDescription(sb.toString());
        return spell;
    }

    private static final SpellAbility getTransmuteArtifact(final Card card) {
        /*
         * Sacrifice an artifact. If you do, search your library for an
         * artifact card. If that card's converted mana cost is less than or
         * equal to the sacrificed artifact's converted mana cost, put it
         * onto the battlefield. If it's greater, you may pay X, where X is
         * the difference. If you do, put it onto the battlefield. If you
         * don't, put it into its owner's graveyard. Then shuffle your
         * library.
         */

        final Cost abCost = new Cost(card, "U U", false);
        return new Spell(card, abCost, null) {
            private static final long serialVersionUID = -8497142072380944393L;

            @Override
            public boolean canPlayAI() {
                return false;
            }

            @Override
            public void resolve() {
                final Player p = card.getController();
                int baseCMC = -1;
                final Card[] newArtifact = new Card[1];

                // Sacrifice an artifact
                List<Card> arts = CardLists.filter(p.getCardsIn(ZoneType.Battlefield), Presets.ARTIFACTS);
                final Object toSac = GuiChoose.oneOrNone("Sacrifice an artifact", arts);
                if (toSac != null) {
                    final Card c = (Card) toSac;
                    baseCMC = c.getCMC();
                    Singletons.getModel().getGame().getAction().sacrifice(c, this);
                } else {
                    return;
                }

                // Search your library for an artifact
                final List<Card> lib = p.getCardsIn(ZoneType.Library);
                GuiChoose.oneOrNone("Looking at Library", lib);
                final List<Card> libArts = CardLists.filter(lib, Presets.ARTIFACTS);
                final Object o = GuiChoose.oneOrNone("Search for artifact", libArts);
                if (o != null) {
                    newArtifact[0] = (Card) o;
                } else {
                    return;
                }

                final int newCMC = newArtifact[0].getCMC();

                // if <= baseCMC, put it onto the battlefield
                final GameState game = Singletons.getModel().getGame(); 
                if (newCMC <= baseCMC) {
                    game.getAction().moveToPlay(newArtifact[0]);
                } else {
                    final int diffCost = newCMC - baseCMC;
                    InputPayManaExecuteCommands inp = new InputPayManaExecuteCommands(p, "Pay difference in artifacts CMC", ManaCost.get(diffCost));
                    FThreads.setInputAndWait(inp);
                    if ( inp.isPaid() )
                        game.getAction().moveToPlay(newArtifact[0]);
                    else
                        game.getAction().moveToGraveyard(newArtifact[0]);
                }

                // finally, shuffle library
                p.shuffle();

            } // resolve()
        }; // SpellAbility
    }

    public static void buildCard(final Card card, final String cardName) {

        if (cardName.equals("Brilliant Ultimatum")) { card.addSpellAbility(getBrilliantUltimatum(card));
        } else if (cardName.equals("Balance")) { card.addSpellAbility(getBalance(card));
        } else if (cardName.equals("Patriarch's Bidding")) { card.addSpellAbility(getPatriarchsBidding(card));
        } else if (cardName.equals("Transmute Artifact")) { card.addSpellAbility(getTransmuteArtifact(card));
        }
    } // getCard
}
