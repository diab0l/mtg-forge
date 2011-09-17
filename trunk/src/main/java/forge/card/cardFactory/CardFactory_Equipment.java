package forge.card.cardFactory;


import forge.*;
import forge.card.cost.Cost;
import forge.card.spellability.*;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;
import forge.gui.input.Input;

import java.util.ArrayList;


/**
 * <p>CardFactory_Equipment class.</p>
 *
 * @author Forge
 * @version $Id$
 */
class CardFactory_Equipment {

    /**
     * <p>shouldEquip.</p>
     *
     * @param c a {@link forge.Card} object.
     * @return a int.
     */
    public static int shouldEquip(Card c) {
        ArrayList<String> a = c.getKeyword();
        for (int i = 0; i < a.size(); i++) {

            // Keyword renamed to eqPump, was VanillaEquipment
            if (a.get(i).toString().startsWith("eqPump")) {
                return i;
            }
        }
        return -1;
    }


    /**
     * <p>getCard.</p>
     *
     * @param card a {@link forge.Card} object.
     * @param cardName a {@link java.lang.String} object.
     * @param owner a {@link forge.Player} object.
     * @return a {@link forge.Card} object.
     */
    public static Card getCard(final Card card, String cardName) {

        //*************** START *********** START **************************
        if (cardName.equals("Umbral Mantle")) {
            Cost abCost = new Cost("0", cardName, true);
            Target target = new Target(card, "Select target creature you control", "Creature.YouCtrl".split(","));
            final Ability_Activated equip = new Ability_Activated(card, abCost, target) {
                private static final long serialVersionUID = -6122939616068165612L;

                @Override
                public void resolve() {
                    if (AllZoneUtil.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {

                        if (card.isEquipping()) {
                            Card crd = card.getEquipping().get(0);
                            if (crd.equals(getTargetCard())) {
                                return;
                            }

                            card.unEquipCard(crd);
                        }

                        card.equipCard(getTargetCard());
                    }
                }

                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };//equip ability

            equip.setType("Extrinsic");

            final Ability untapboost = new Ability(card, "3") {
                Command EOT(final Card c) {
                    return new Command() {
                        private static final long serialVersionUID = -8840812331316327448L;

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
                    getSourceCard().addTempAttackBoost(2);
                    getSourceCard().addTempDefenseBoost(2);
                    AllZone.getEndOfTurn().addUntil(EOT(getSourceCard()));
                }

                @Override
                public boolean canPlay() {
                    return (getSourceCard().isTapped()
                            && !getSourceCard().hasSickness()
                            && super.canPlay());
                }
            };//equiped creature's ability
            untapboost.makeUntapAbility();
            Command onEquip = new Command() {

                private static final long serialVersionUID = -4784079305541955698L;

                public void execute() {
                    if (card.isEquipping()) {
                        Card crd = card.getEquipping().get(0);

                        StringBuilder sbDesc = new StringBuilder();
                        sbDesc.append("3, Untap: ").append(crd).append(" gets +2/+2 until end of turn");
                        untapboost.setDescription(sbDesc.toString());

                        StringBuilder sbStack = new StringBuilder();
                        sbStack.append(crd).append(" - +2/+2 until EOT");
                        untapboost.setStackDescription(sbStack.toString());

                        crd.addSpellAbility(untapboost);
                    }
                }//execute()
            };//Command


            Command onUnEquip = new Command() {
                private static final long serialVersionUID = -3427116314295067303L;

                public void execute() {
                    if (card.isEquipping()) {
                        Card crd = card.getEquipping().get(0);
                        crd.removeSpellAbility(untapboost);
                    }

                }//execute()
            };//Command

            equip.setBeforePayMana(CardFactoryUtil.input_equipCreature(equip));
            equip.getRestrictions().setSorcerySpeed(true);


            equip.setDescription("Equip: 0");
            card.addSpellAbility(equip);

            card.addEquipCommand(onEquip);
            card.addUnEquipCommand(onUnEquip);
        } //*************** END ************ END **************************


        //*************** START *********** START **************************
        else if (cardName.equals("Hedron Matrix")) {
            /*
                * Equipped creature gets +X/+X, where X is its converted mana cost.
                */
            final Ability equip = new Ability(card, "4") {

                //not changed
                @Override
                public void resolve() {
                    if (AllZoneUtil.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {

                        if (card.isEquipping()) {
                            Card crd = card.getEquipping().get(0);
                            if (crd.equals(getTargetCard())) {
                                return;
                            }

                            card.unEquipCard(crd);
                        }
                        card.equipCard(getTargetCard());
                    }
                }

                //not changed
                @Override
                public boolean canPlay() {
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && Phase.canCastSorcery(card.getController())
                            && super.canPlay();
                }

                //not changed
                @Override
                public boolean canPlayAI() {
                    return getCreature().size() != 0
                            && !card.isEquipping()
                            && super.canPlayAI();
                }

                //not changed
                @Override
                public void chooseTargetAI() {
                    Card target = CardFactoryUtil.AI_getBestCreature(getCreature());
                    setTargetCard(target);
                }

                //not changed
                CardList getCreature() {
                    CardList list = AllZoneUtil.getCreaturesInPlay(AllZone.getComputerPlayer());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return CardFactoryUtil.AI_doesCreatureAttack(c)
                                    && CardFactoryUtil.canTarget(card, c)
                                    && (!c.hasKeyword("Defender"));
                        }
                    });

                    // Is there at least 1 Loxodon Punisher and/or Goblin Gaveleer to target
                    CardList equipMagnetList = list;
                    equipMagnetList = equipMagnetList.getEquipMagnets();

                    if (equipMagnetList.size() != 0) {
                        return equipMagnetList;
                    }

                    return list;
                }//getCreature()
            };//equip ability

            Command onEquip = new Command() {
                private static final long serialVersionUID = -5356474407155702171L;

                public void execute() {
                    if (card.isEquipping()) {
                        Card crd = card.getEquipping().get(0);
                        int pump = CardUtil.getConvertedManaCost(crd.getManaCost());
                        crd.addSemiPermanentAttackBoost(pump);
                        crd.addSemiPermanentDefenseBoost(pump);
                    }
                }//execute()
            };//Command

            Command onUnEquip = new Command() {
                private static final long serialVersionUID = 5196262972986079207L;

                public void execute() {
                    if (card.isEquipping()) {
                        Card crd = card.getEquipping().get(0);
                        int pump = CardUtil.getConvertedManaCost(crd.getManaCost());
                        crd.addSemiPermanentAttackBoost(-pump);
                        crd.addSemiPermanentDefenseBoost(-pump);

                    }

                }//execute()
            };//Command

            equip.setBeforePayMana(CardFactoryUtil.input_equipCreature(equip));

            equip.setDescription("Equip: 4");
            card.addSpellAbility(equip);

            card.addEquipCommand(onEquip);
            card.addUnEquipCommand(onUnEquip);

        } //*************** END ************ END **************************


        //*************** START *********** START **************************
        else if (cardName.equals("Blade of the Bloodchief")) {
            final Ability triggeredAbility = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if (card.getEquipping().size() != 0) {
                        Card equipping = card.getEquipping().get(0);
                        if (equipping.isType("Vampire")) {
                            equipping.addCounter(Counters.P1P1, 2);
                        } else {
                            equipping.addCounter(Counters.P1P1, 1);
                        }
                    }
                }
            };

            final Trigger myTrigger = TriggerHandler.parseTrigger("Mode$ ChangesZone | Origin$ Battlefield | Destination$ Graveyard | ValidCard$ Creature | TriggerZones$ Battlefield | Execute$ TrigOverride | TriggerDescription$ Whenever a creature is put into a graveyard from the battlefield, put a +1/+1 counter on equipped creature. If equipped creature is a Vampire, put two +1/+1 counters on it instead.", card, true);
            myTrigger.setOverridingAbility(triggeredAbility);

            card.addTrigger(myTrigger);
        } //*************** END ************ END **************************


        //*************** START *********** START **************************
        else if (cardName.equals("Piston Sledge")) {

            final Input in = new Input() {
                private static final long serialVersionUID = 1782826197612459365L;

                @Override
                public void showMessage() {
                    CardList list = AllZoneUtil.getCreaturesInPlay(card.getController());
                    list = list.filter(AllZoneUtil.getCanTargetFilter(card));
                    AllZone.getDisplay().showMessage(card + " - Select target creature you control to attach");
                    ButtonUtil.disableAll();
                    if (list.size() == 0) {
                        stop();
                    }
                }

                @Override
                public void selectCard(Card c, PlayerZone z) {
                    if (z.is(Constant.Zone.Battlefield, card.getController()) && c.isCreature()
                            && CardFactoryUtil.canTarget(card, c)) {
                        card.equipCard(c);
                        stop();
                    }
                }

            };

            final SpellAbility comesIntoPlayAbility = new Ability(card, "0") {
                @Override
                public void resolve() {
                    AllZone.getInputControl().setInput(in);
                }//resolve()
            }; //comesIntoPlayAbility

            Command intoPlay = new Command() {
                private static final long serialVersionUID = 2985015252466920757L;

                public void execute() {

                    StringBuilder sb = new StringBuilder();
                    sb.append("When Piston Sledge enters the battlefield, attach it to target creature you control.");
                    comesIntoPlayAbility.setStackDescription(sb.toString());

                    AllZone.getStack().addSimultaneousStackEntry(comesIntoPlayAbility);

                }
            };

            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************


        if (shouldEquip(card) != -1) {
            int n = shouldEquip(card);
            if (n != -1) {
                String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);

                String k[] = parse.split(":");
                String tmpCost;
                tmpCost = k[0].substring(6);
                String keywordsUnsplit = "";
                String extrinsicKeywords[] = {"none"};    // for equips with no keywords to add

                //final String manaCost = tmpCost.trim();
                final Cost abCost = new Cost(tmpCost.trim(), card.getName(), true);
                int power = 0;
                int tough = 0;

                String ptk[] = k[1].split("/");

                if (ptk.length == 1)     // keywords in first cell
                {
                    keywordsUnsplit = ptk[0];
                } else // parse the power/toughness boosts in first two cells
                {
                    for (int i = 0; i < 2; i++) {
                        if (ptk[i].matches("[\\+\\-][0-9]")) {
                            ptk[i] = ptk[i].replace("+", "");
                        }
                    }

                    power = Integer.parseInt(ptk[0].trim());
                    tough = Integer.parseInt(ptk[1].trim());


                    if (ptk.length > 2) {     // keywords in third cell
                        keywordsUnsplit = ptk[2];
                    }
                }

                if (keywordsUnsplit.length() > 0)    // then there is at least one extrinsic keyword to assign
                {
                    String tempKwds[] = keywordsUnsplit.split("&");
                    extrinsicKeywords = new String[tempKwds.length];

                    for (int i = 0; i < tempKwds.length; i++) {
                        extrinsicKeywords[i] = tempKwds[i].trim();
                    }
                }

                card.addSpellAbility(CardFactoryUtil.eqPump_Equip(card, power, tough, extrinsicKeywords, abCost));
                card.addEquipCommand(CardFactoryUtil.eqPump_onEquip(card, power, tough, extrinsicKeywords, abCost));
                card.addUnEquipCommand(CardFactoryUtil.eqPump_unEquip(card, power, tough, extrinsicKeywords, abCost));

            }
        }// eqPump (was VanillaEquipment)

        if (card.hasKeyword("Living Weapon")) {
            card.removeIntrinsicKeyword("Living Weapon");
            final Ability etbAbility = new Ability(card, "0") {

                @Override
                public void resolve() {
                    String[] types = new String[]{"Creature", "Germ"};
                    String[] keywords = new String[0];
                    CardList germs = CardFactoryUtil.makeToken("Germ", "B 0 0 Germ", card.getController(), "B", types, 1, 1, keywords);

                    card.equipCard(germs.get(0));

                    for (Card c : germs) {
                        c.setBaseAttack(0);
                        c.setBaseDefense(0);
                    }
                }

            };

            final Trigger etbTrigger = TriggerHandler.parseTrigger("Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | ValidCard$ Card.Self | Execute$ TrigOverriding | TriggerDescription$ Living Weapon (When this Equipment enters the battlefield, put a 0/0 black Germ creature token onto the battlefield, then attach this to it.)", card, true);
            etbTrigger.setOverridingAbility(etbAbility);

            card.addTrigger(etbTrigger);
        }

        return card;
    }
}
