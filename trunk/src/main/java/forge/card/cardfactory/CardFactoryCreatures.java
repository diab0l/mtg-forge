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
import com.google.common.collect.Iterables;

import forge.Card;
import forge.CardLists;
import forge.Singletons;
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
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;
import forge.game.Game;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.input.InputSelectCards;

/**
 * <p>
 * CardFactory_Creatures class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CardFactoryCreatures {
    /** 
     * TODO: Write javadoc for this type.
     *
     */
    public static final class InputSelectCardsForDreadnought extends InputSelectCards {
        private static final long serialVersionUID = 2698036349873486664L;
        protected final Player player;
        
        public InputSelectCardsForDreadnought(Player p, int min, int max) {
            super(min, max);
            player = p;
        }

        @Override
        protected String getMessage() {
            return String.format(message, selected.size(), getTotalPower());
        }

        @Override
        protected boolean isValidChoice(Card c) {
            return c.isCreature() && player.getZone(ZoneType.Battlefield).contains(c);
        } // selectCard()

        @Override
        protected boolean hasEnoughTargets() {
            return getTotalPower() >= 12;
        }

        private int getTotalPower() {
            int sum = 0;
            for (final Card c : selected) {
                sum += c.getNetAttack();
            }
            return sum;
        }
    }


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

    private static void getCard_SurturedGhoul(final Card card) {
        final Command intoPlay = new Command() {
            private static final long serialVersionUID = -75234586897814L;

            @Override
            public void run() {
                final Game game = card.getGame();
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
                final Game game = player.getGame();
                if (player.isHuman()) {
                    final InputSelectCards target = new InputSelectCardsForDreadnought(player, 0, Integer.MAX_VALUE); // Input
                    String toDisplay = cardName + " - Select any number of creatures to sacrifice.\n" +
                            "Currently, (%d) selected with a total power of: %d\n\n" + "Click OK when Done.";
                    target.setMessage(toDisplay);
                    target.setCancelAllowed(true);
                    Singletons.getControl().getInputQueue().setInputAndWait(target);
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
