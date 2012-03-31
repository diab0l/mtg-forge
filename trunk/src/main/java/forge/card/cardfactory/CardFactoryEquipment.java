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

import forge.Card;
import forge.CardList;
import forge.Counters;
import forge.card.cost.Cost;
import forge.card.spellability.Ability;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;

/**
 * <p>
 * CardFactoryEquipment class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
class CardFactoryEquipment {

    /**
     * <p>
     * shouldEquip.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a int.
     */
    public static int shouldEquip(final Card c) {
        final ArrayList<String> a = c.getKeyword();
        for (int i = 0; i < a.size(); i++) {

            // Keyword renamed to eqPump, was VanillaEquipment
            if (a.get(i).toString().startsWith("eqPump")) {
                return i;
            }
        }
        return -1;
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
     * @return a {@link forge.Card} object.
     */
    public static Card getCard(final Card card, final String cardName) {

        // *************** START *********** START **************************
        /*if (cardName.equals("Umbral Mantle")) {
            final Cost abCost = new Cost("0", cardName, true);
            final Target target = new Target(card, "Select target creature you control", "Creature.YouCtrl".split(","));
            final AbilityActivated equip = new AbilityActivated(card, abCost, target) {
                private static final long serialVersionUID = -6122939616068165612L;

                @Override
                public void resolve() {
                    if (AllZoneUtil.isCardInPlay(this.getTargetCard()) && this.getTargetCard().canBeTargetedBy(this)) {

                        if (card.isEquipping()) {
                            final Card crd = card.getEquipping().get(0);
                            if (crd.equals(this.getTargetCard())) {
                                return;
                            }

                            card.unEquipCard(crd);
                        }

                        card.equipCard(this.getTargetCard());
                    }
                }

                @Override
                public boolean canPlayAI() {
                    return false;
                }
            }; // equip ability

            equip.setType("Extrinsic");

            final Ability untapboost = new Ability(card, "3") {
                Command eot(final Card c) {
                    return new Command() {
                        private static final long serialVersionUID = -8840812331316327448L;

                        @Override
                        public void execute() {
                            if (AllZoneUtil.isCardInPlay(getSourceCard())) {
                                c.addTempAttackBoost(-2);
                                c.addTempDefenseBoost(-2);
                            }

                        }
                    };
                }

                @Override
                public void resolve() {
                    this.getSourceCard().addTempAttackBoost(2);
                    this.getSourceCard().addTempDefenseBoost(2);
                    AllZone.getEndOfTurn().addUntil(this.eot(this.getSourceCard()));
                }

                @Override
                public boolean canPlay() {
                    return (this.getSourceCard().isTapped() && !this.getSourceCard().hasSickness() && super.canPlay());
                }
            }; // equiped creature's ability
            untapboost.makeUntapAbility();
            final Command onEquip = new Command() {

                private static final long serialVersionUID = -4784079305541955698L;

                @Override
                public void execute() {
                    if (card.isEquipping()) {
                        final Card crd = card.getEquipping().get(0);

                        final StringBuilder sbDesc = new StringBuilder();
                        sbDesc.append("3, Untap: ").append(crd).append(" gets +2/+2 until end of turn");
                        untapboost.setDescription(sbDesc.toString());

                        final StringBuilder sbStack = new StringBuilder();
                        sbStack.append(crd).append(" - +2/+2 until EOT");
                        untapboost.setStackDescription(sbStack.toString());

                        crd.addSpellAbility(untapboost);
                    }
                } // execute()
            }; // Command

            final Command onUnEquip = new Command() {
                private static final long serialVersionUID = -3427116314295067303L;

                @Override
                public void execute() {
                    if (card.isEquipping()) {
                        final Card crd = card.getEquipping().get(0);
                        crd.removeSpellAbility(untapboost);
                    }

                } // execute()
            }; // Command

            equip.setBeforePayMana(CardFactoryUtil.inputEquipCreature(equip));
            equip.getRestrictions().setSorcerySpeed(true);

            equip.setDescription("Equip: 0");
            card.addSpellAbility(equip);

            card.addEquipCommand(onEquip);
            card.addUnEquipCommand(onUnEquip);
        }*/ // *************** END ************ END **************************

        // *************** START *********** START **************************
        /*if (cardName.equals("Hedron Matrix")) {
             //
             //Equipped creature gets +X/+X, where X is its converted mana cost.
             //
            final Ability equip = new Ability(card, "4") {

                // not changed
                @Override
                public void resolve() {
                    if (AllZoneUtil.isCardInPlay(this.getTargetCard()) && this.getTargetCard().canBeTargetedBy(this)) {

                        if (card.isEquipping()) {
                            final Card crd = card.getEquipping().get(0);
                            if (crd.equals(this.getTargetCard())) {
                                return;
                            }

                            card.unEquipCard(crd);
                        }
                        card.equipCard(this.getTargetCard());
                    }
                }

                // not changed
                @Override
                public boolean canPlay() {
                    return AllZone.getZoneOf(card).is(Constant.Zone.Battlefield)
                            && PhaseHandler.canCastSorcery(card.getController()) && super.canPlay();
                }

                // not changed
                @Override
                public boolean canPlayAI() {
                    return (this.getCreature().size() != 0) && !card.isEquipping() && super.canPlayAI();
                }

                // not changed
                @Override
                public void chooseTargetAI() {
                    final Card target = CardFactoryUtil.getBestCreatureAI(this.getCreature());
                    this.setTargetCard(target);
                }

                // not changed
                CardList getCreature() {
                    CardList list = AllZoneUtil.getCreaturesInPlay(AllZone.getComputerPlayer());
                    list = list.getTargetableCards(this).filter(new CardListFilter() {
                        @Override
                        public boolean addCard(final Card c) {
                            return CardFactoryUtil.doesCreatureAttackAI(c) && (!c.hasKeyword("Defender"));
                        }
                    });

                    // Is there at least 1 Loxodon Punisher and/or Goblin
                    // Gaveleer to target
                    CardList equipMagnetList = list;
                    equipMagnetList = equipMagnetList.getEquipMagnets();

                    if (equipMagnetList.size() != 0) {
                        return equipMagnetList;
                    }

                    return list;
                } // getCreature()
            }; // equip ability

            final Command onEquip = new Command() {
                private static final long serialVersionUID = -5356474407155702171L;

                @Override
                public void execute() {
                    if (card.isEquipping()) {
                        final Card crd = card.getEquipping().get(0);
                        final int pump = CardUtil.getConvertedManaCost(crd.getManaCost());
                        crd.addSemiPermanentAttackBoost(pump);
                        crd.addSemiPermanentDefenseBoost(pump);
                    }
                } // execute()
            }; // Command

            final Command onUnEquip = new Command() {
                private static final long serialVersionUID = 5196262972986079207L;

                @Override
                public void execute() {
                    if (card.isEquipping()) {
                        final Card crd = card.getEquipping().get(0);
                        final int pump = CardUtil.getConvertedManaCost(crd.getManaCost());
                        crd.addSemiPermanentAttackBoost(-pump);
                        crd.addSemiPermanentDefenseBoost(-pump);

                    }

                } // execute()
            }; // Command

            equip.setBeforePayMana(CardFactoryUtil.inputEquipCreature(equip));

            equip.setDescription("Equip: 4");
            card.addSpellAbility(equip);

            card.addEquipCommand(onEquip);
            card.addUnEquipCommand(onUnEquip);

        }*/ // *************** END ************ END **************************

        // *************** START *********** START **************************
        /*else*/ if (cardName.equals("Blade of the Bloodchief")) {
            final Ability triggeredAbility = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if (card.getEquipping().size() != 0) {
                        final Card equipping = card.getEquipping().get(0);
                        if (equipping.isType("Vampire")) {
                            equipping.addCounter(Counters.P1P1, 2);
                        } else {
                            equipping.addCounter(Counters.P1P1, 1);
                        }
                    }
                }
            };

            final StringBuilder sbTrig = new StringBuilder();
            sbTrig.append("Mode$ ChangesZone | Origin$ Battlefield | Destination$ Graveyard | ");
            sbTrig.append("ValidCard$ Creature | TriggerZones$ Battlefield | Execute$ TrigOverride | ");
            sbTrig.append("TriggerDescription$ Whenever a creature is put into a graveyard ");
            sbTrig.append("from the battlefield, put a +1/+1 counter on equipped creature. ");
            sbTrig.append("If equipped creature is a Vampire, put two +1/+1 counters on it instead.");
            final Trigger myTrigger = TriggerHandler.parseTrigger(sbTrig.toString(), card, true);
            myTrigger.setOverridingAbility(triggeredAbility);

            card.addTrigger(myTrigger);
        } // *************** END ************ END **************************

        if (CardFactoryEquipment.shouldEquip(card) != -1) {
            final int n = CardFactoryEquipment.shouldEquip(card);
            if (n != -1) {
                final String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);

                final String[] k = parse.split(":");
                String tmpCost;
                tmpCost = k[0].substring(6);
                String keywordsUnsplit = "";
                String[] extrinsicKeywords = { "none" }; // for equips with no
                                                         // keywords to add

                // final String manaCost = tmpCost.trim();
                final Cost abCost = new Cost(card, tmpCost.trim(), true);
                int power = 0;
                int tough = 0;

                final String[] ptk = k[1].split("/");
                // keywords in first cell
                if (ptk.length == 1) {
                    keywordsUnsplit = ptk[0];
                } else {
                    // parse the power/toughness boosts in first two cells
                    for (int i = 0; i < 2; i++) {
                        if (ptk[i].matches("[\\+\\-][0-9]")) {
                            ptk[i] = ptk[i].replace("+", "");
                        }
                    }

                    power = Integer.parseInt(ptk[0].trim());
                    tough = Integer.parseInt(ptk[1].trim());

                    if (ptk.length > 2) { // keywords in third cell
                        keywordsUnsplit = ptk[2];
                    }
                }

                if (keywordsUnsplit.length() > 0) // then there is at least one
                                                  // extrinsic keyword to assign
                {
                    final String[] tempKwds = keywordsUnsplit.split("&");
                    extrinsicKeywords = new String[tempKwds.length];

                    for (int i = 0; i < tempKwds.length; i++) {
                        extrinsicKeywords[i] = tempKwds[i].trim();
                    }
                }

                card.addSpellAbility(CardFactoryUtil.eqPumpEquip(card, power, tough, extrinsicKeywords, abCost));
                card.addEquipCommand(CardFactoryUtil.eqPumpOnEquip(card, power, tough, extrinsicKeywords, abCost));
                card.addUnEquipCommand(CardFactoryUtil.eqPumpUnEquip(card, power, tough, extrinsicKeywords, abCost));

            }
        } // eqPump (was VanillaEquipment)

        if (card.hasKeyword("Living Weapon")) {
            card.removeIntrinsicKeyword("Living Weapon");
            final Ability etbAbility = new Ability(card, "0") {

                @Override
                public void resolve() {
                    final String[] types = new String[] { "Creature", "Germ" };
                    final String[] keywords = new String[0];
                    final CardList germs = CardFactoryUtil.makeToken("Germ", "B 0 0 Germ", card.getController(), "B",
                            types, 1, 1, keywords);

                    card.equipCard(germs.get(0));

                    for (final Card c : germs) {
                        c.setBaseAttack(0);
                        c.setBaseDefense(0);
                    }
                }

            };

            final StringBuilder sbTrig = new StringBuilder();
            sbTrig.append("Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | ");
            sbTrig.append("ValidCard$ Card.Self | Execute$ TrigOverriding | TriggerDescription$ ");
            sbTrig.append("Living Weapon (When this Equipment enters the battlefield, ");
            sbTrig.append("put a 0/0 black Germ creature token onto the battlefield, then attach this to it.)");
            final Trigger etbTrigger = TriggerHandler.parseTrigger(sbTrig.toString(), card, true);
            etbTrigger.setOverridingAbility(etbAbility);

            card.addTrigger(etbTrigger);
        }

        return card;
    }
}
