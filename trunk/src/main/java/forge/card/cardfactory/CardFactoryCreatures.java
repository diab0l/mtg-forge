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
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.FThreads;
import forge.CardPredicates.Presets;
import forge.Command;
import forge.CounterType;
import forge.card.cost.Cost;
import forge.card.mana.ManaCost;
import forge.card.spellability.Ability;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.AbilityStatic;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellPermanent;
import forge.card.spellability.Target;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;
import forge.control.input.InputSelectCards;
import forge.control.input.InputSelectCardsFromList;
import forge.game.GameState;
import forge.game.ai.ComputerUtilCard;
import forge.game.ai.ComputerUtilCombat;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.util.Aggregates;

/**
 * <p>
 * CardFactory_Creatures class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CardFactoryCreatures {
    private static void getCard_SphinxJwar(final Card card) {
        final SpellAbility ability1 = new AbilityStatic(card, ManaCost.ZERO) {
            @Override
            public void resolve() {
                final Player player = card.getController();
                final PlayerZone lib = player.getZone(ZoneType.Library);

                if (lib.size() < 1 || !this.getActivatingPlayer().equals(card.getController())) {
                    return;
                }

                final List<Card> cl = new ArrayList<Card>();
                cl.add(lib.get(0));

                GuiChoose.oneOrNone("Top card", cl);
            }

            @Override
            public boolean canPlayAI() {
                return false;
            }
        }; // SpellAbility

        final StringBuilder sb1 = new StringBuilder();
        sb1.append(card.getName()).append(" - look at top card of library.");
        ability1.setStackDescription(sb1.toString());

        ability1.setDescription("You may look at the top card of your library.");
        card.addSpellAbility(ability1);
    }

    private static void getCard_MasterOfTheWildHunt(final Card card) {
        final Cost abCost = new Cost("T", true);
        final Target abTgt = new Target(card, "Target a creature to Hunt", "Creature".split(","));
        class MasterOfTheWildHuntAbility extends AbilityActivated {
            public MasterOfTheWildHuntAbility(final Card ca, final Cost co, final Target t) {
                super(ca, co, t);
            }

            @Override
            public AbilityActivated getCopy() {
                return new MasterOfTheWildHuntAbility(getSourceCard(),
                        getPayCosts(), new Target(getTarget()));
            }

            private static final long serialVersionUID = 35050145102566898L;
            private final Predicate<Card> untappedCreature = Predicates.and(CardPredicates.Presets.UNTAPPED, CardPredicates.Presets.CREATURES);

            @Override
            public boolean canPlayAI() {
                List<Card> wolves = CardLists.getType(getActivatingPlayer().getCardsIn(ZoneType.Battlefield), "Wolf");
                Iterable<Card> untappedWolves = Iterables.filter(wolves, untappedCreature);

                final int totalPower = Aggregates.sum(untappedWolves, CardPredicates.Accessors.fnGetNetAttack);
                if (totalPower == 0) {
                    return false;
                }

                List<Card> targetables = new ArrayList<Card>(getActivatingPlayer().getOpponent().getCardsIn(ZoneType.Battlefield));

                targetables = CardLists.filter(CardLists.getTargetableCards(targetables, this), new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return c.isCreature() && (c.getNetDefense() <= totalPower);
                    }
                });

                if (targetables.size() == 0) {
                    return false;
                }

                this.getTarget().resetTargets();
                this.setTargetCard(ComputerUtilCard.getBestCreatureAI(targetables));

                return true;
            }

            @Override
            public void resolve() {
                List<Card> wolves = CardLists.getType(card.getController().getCardsIn(ZoneType.Battlefield), "Wolf");
                wolves = CardLists.filter(wolves, untappedCreature);

                final Card target = this.getTargetCard();

                if (wolves.isEmpty() || !target.canBeTargetedBy(this)) {
                    return;
                }

                for (final Card c : wolves) {
                    c.tap();
                    target.addDamage(c.getNetAttack(), c);
                }

                if (target.getController().isHuman()) { // Human choose spread damage
                    final int netAttack = target.getNetAttack();
                    for (int x = 0; x < netAttack; x++) {
                        InputSelectCards inp = new InputSelectCardsFromList(1,1,wolves);
                        inp.setMessage("Select target wolf to damage for " + getSourceCard());
                        FThreads.setInputAndWait(inp);
                        inp.getSelected().get(0).addDamage(1, target);
                    }
                } else { // AI Choose spread Damage
                    final List<Card> damageableWolves = CardLists.filter(wolves, new Predicate<Card>() {
                        @Override
                        public boolean apply(final Card c) {
                            return (ComputerUtilCombat.predictDamageTo(c, target.getNetAttack(), target, false) > 0);
                        }
                    });

                    if (damageableWolves.size() == 0) {
                        // can't damage
                        // anything
                        return;
                    }

                    List<Card> wolvesLeft = CardLists.filter(damageableWolves, new Predicate<Card>() {
                        @Override
                        public boolean apply(final Card c) {
                            return !c.hasKeyword("Indestructible");
                        }
                    });

                    for (int i = 0; i < target.getNetAttack(); i++) {
                        wolvesLeft = CardLists.filter(wolvesLeft, new Predicate<Card>() {
                            @Override
                            public boolean apply(final Card c) {
                                return (ComputerUtilCombat.getDamageToKill(c) > 0)
                                        && ((ComputerUtilCombat.getDamageToKill(c) <= target.getNetAttack()) || target
                                                .hasKeyword("Deathtouch"));
                            }
                        });

                        // Kill Wolves that can be killed first
                        if (wolvesLeft.size() > 0) {
                            final Card best = ComputerUtilCard.getBestCreatureAI(wolvesLeft);
                            best.addDamage(1, target);
                            if ((ComputerUtilCombat.getDamageToKill(best) <= 0) || target.hasKeyword("Deathtouch")) {
                                wolvesLeft.remove(best);
                            }
                        } else {
                            // Add -1/-1s to Random Indestructibles
                            if (target.hasKeyword("Infect") || target.hasKeyword("Wither")) {
                                final List<Card> indestructibles = CardLists.filter(damageableWolves, new Predicate<Card>() {
                                    @Override
                                    public boolean apply(final Card c) {
                                        return c.hasKeyword("Indestructible");
                                    }
                                });
                                CardLists.shuffle(indestructibles);
                                indestructibles.get(0).addDamage(1, target);
                            }

                            // Then just add Damage randomnly

                            else {
                                CardLists.shuffle(damageableWolves);
                                wolves.get(0).addDamage(1, target);
                            }
                        }
                    }
                }
                target.getController().getGame().getAction().checkStateEffects();
            } // resolve()

            @Override
            public String getDescription() {
                final StringBuilder sb = new StringBuilder();
                sb.append("Tap: Tap all untapped Wolf creatures you control. ");
                sb.append("Each Wolf tapped this way deals damage equal to its ");
                sb.append("power to target creature. That creature deals damage ");
                sb.append("equal to its power divided as its controller ");
                sb.append("chooses among any number of those Wolves.");
                return sb.toString();
            }
        }
        final AbilityActivated ability = new MasterOfTheWildHuntAbility(card, abCost, abTgt);
        card.addSpellAbility(ability);
    }

    private static void getCard_SurturedGhoul(final Card card) {
        final Command intoPlay = new Command() {
            private static final long serialVersionUID = -75234586897814L;

            @Override
            public void run() {
                final GameState game = card.getGame();
                int intermSumPower = 0;
                int intermSumToughness = 0;
                // intermSumPower = intermSumToughness = 0;
                List<Card> creats = CardLists.filter(card.getController().getCardsIn(ZoneType.Graveyard), new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return c.isCreature() && !c.equals(card);
                    }
                });

                if (card.getController().isHuman()) {
                    if (!creats.isEmpty()) {
                        final List<Card> selection = GuiChoose.noneOrMany("Select creatures to exile", creats);

                        for (int m = 0; m < selection.size(); m++) {
                            intermSumPower += selection.get(m).getBaseAttack();
                            intermSumToughness += selection.get(m).getBaseDefense();
                            game.getAction().exile(selection.get(m));
                        }
                    }

                } // human
                else {
                    for (int i = 0; i < creats.size(); i++) {
                        final Card c = creats.get(i);
                        if ((c.getNetAttack() <= 2) && (c.getNetDefense() <= 3)) {
                            intermSumPower += c.getBaseAttack();
                            intermSumToughness += c.getBaseDefense();
                            game.getAction().exile(c);
                        }
                        // is this needed?
                        card.getController().getZone(ZoneType.Battlefield).updateObservers();
                    }
                }
                card.setBaseAttack(intermSumPower);
                card.setBaseDefense(intermSumToughness);
            }
        };
        // Do not remove SpellAbilities created by AbilityFactory or
        // Keywords.
        card.clearFirstSpell();
        card.addComesIntoPlayCommand(intoPlay);
        card.addSpellAbility(new SpellPermanent(card) {
            private static final long serialVersionUID = 304885517082977723L;

            @Override
            public boolean canPlayAI() {
                return Iterables.any(getActivatingPlayer().getCardsIn(ZoneType.Graveyard), Presets.CREATURES);
            }
        });
    }
    
    private static void getCard_PhyrexianDreadnought(final Card card, final String cardName) {
        final Player player = card.getController();

        final Ability sacOrSac = new Ability(card, ManaCost.NO_COST) {
            @Override
            public void resolve() {
                final GameState game = player.getGame();
                if (player.isHuman()) {
                    final InputSelectCards target = new InputSelectCards(0, Integer.MAX_VALUE) {
                        private static final long serialVersionUID = 2698036349873486664L;
                        
                        @Override
                        public String getMessage() {
                            String toDisplay = cardName + " - Select any number of creatures to sacrifice.  ";
                            toDisplay += "Currently, (" + selected.size() + ") selected with a total power of: " + getTotalPower();
                            toDisplay += "  Click OK when Done.";
                            return toDisplay;
                        }

                        @Override
                        protected boolean isValidChoice(Card c) {
                            Zone zone = game.getZoneOf(c);
                            return c.isCreature() && zone.is(ZoneType.Battlefield, player);
                        } // selectCard()

                        @Override
                        protected boolean hasEnoughTargets() {
                            return getTotalPower() >= 12;
                        };

                        private int getTotalPower() {
                            int sum = 0;
                            for (final Card c : selected) {
                                sum += c.getNetAttack();
                            }
                            return sum;
                        }
                    }; // Input

                    target.setCancelAllowed(true);
                    FThreads.setInputAndWait(target);
                    if(!target.hasCancelled()) {
                        for (final Card sac : target.getSelected()) {
                            game.getAction().sacrifice(sac, null);
                        }
                    } else {
                        game.getAction().sacrifice(card, null);
                    }
                }
            } // end resolve
        }; // end sacOrSac

        final StringBuilder sbTrig = new StringBuilder();
        sbTrig.append("Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | ValidCard$ Card.Self | ");
        sbTrig.append("Execute$ TrigOverride | TriggerDescription$  ");
        sbTrig.append("When CARDNAME enters the battlefield, sacrifice it unless ");
        sbTrig.append("you sacrifice any number of creatures with total power 12 or greater.");
        final Trigger myTrigger = TriggerHandler.parseTrigger(sbTrig.toString(), card, true);
        myTrigger.setOverridingAbility(sacOrSac);

        card.addTrigger(myTrigger);
    }



//    // This is a hardcoded card template
//
//    private static void getCard_(final Card card, final String cardName) {
//    }

    public static void buildCard(final Card card, final String cardName) {

        if (cardName.equals("Sphinx of Jwar Isle")) {
            getCard_SphinxJwar(card);
        } else if (cardName.equals("Master of the Wild Hunt")) {
            getCard_MasterOfTheWildHunt(card);
        } else if (cardName.equals("Sutured Ghoul")) {
            getCard_SurturedGhoul(card);
        } else if (cardName.equals("Phyrexian Dreadnought")) {
            getCard_PhyrexianDreadnought(card, cardName);
        }

        // ***************************************************
        // end of card specific code
        // ***************************************************

        final int iLvlUp = CardFactoryUtil.hasKeyword(card, "Level up");
        final int iLvlMax = CardFactoryUtil.hasKeyword(card, "maxLevel");
        
        if (iLvlUp != -1 && iLvlMax != -1) {
            final String parse = card.getKeyword().get(iLvlUp);
            final String parseMax = card.getKeyword().get(iLvlMax);
            card.addSpellAbility(makeLevellerAbility(card, parse, parseMax));
            card.setLevelUp(true);
        } // level up
    }


    private static SpellAbility makeLevellerAbility(final Card card, final String strLevelCost, final String strMaxLevel) {
        card.removeIntrinsicKeyword(strLevelCost);
        card.removeIntrinsicKeyword(strMaxLevel);

        final String[] k = strLevelCost.split(":");
        final String manacost = k[1];

        final String[] l = strMaxLevel.split(":");
        final int maxLevel = Integer.parseInt(l[1]);

        class LevelUpAbility extends AbilityActivated {
            public LevelUpAbility(final Card ca, final String s) {
                super(ca, new Cost(manacost, true), null);
            }

            @Override
            public AbilityActivated getCopy() {
                AbilityActivated levelUp = new LevelUpAbility(getSourceCard(), getPayCosts().toString());
                levelUp.getRestrictions().setSorcerySpeed(true);
                return levelUp;
            }

            private static final long serialVersionUID = 3998280279949548652L;

            @Override
            public void resolve() {
                card.addCounter(CounterType.LEVEL, 1, true);
            }

            @Override
            public boolean canPlayAI() {
                // Todo: Improve Level up code
                return card.getCounters(CounterType.LEVEL) < maxLevel;
            }

            @Override
            public String getDescription() {
                final StringBuilder sbDesc = new StringBuilder();
                sbDesc.append("Level up ").append(manacost).append(" (").append(manacost);
                sbDesc.append(": Put a level counter on this. Level up only as a sorcery.)");
                return sbDesc.toString();
            }
        }
        final SpellAbility levelUp = new LevelUpAbility(card, manacost);
        levelUp.getRestrictions().setSorcerySpeed(true);
        final StringBuilder sbStack = new StringBuilder();
        sbStack.append(card).append(" - put a level counter on this.");
        levelUp.setStackDescription(sbStack.toString());
        return levelUp;
    }
}
