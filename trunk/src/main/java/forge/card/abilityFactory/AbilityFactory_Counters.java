package forge.card.abilityFactory;

import forge.*;
import forge.card.cardFactory.CardFactoryUtil;
import forge.card.spellability.*;
import forge.gui.GuiUtils;
import forge.gui.input.Input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * <p>AbilityFactory_Counters class.</p>
 *
 * @author Forge
 * @version $Id$
 */
public class AbilityFactory_Counters {
    // An AbilityFactory subclass for Putting or Removing Counters on Cards.

    // *******************************************
    // ********** PutCounters *****************
    // *******************************************

    /**
     * <p>createAbilityPutCounters.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityPutCounters(final AbilityFactory af) {

        final SpellAbility abPutCounter = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -1259638699008542484L;

            @Override
            public String getStackDescription() {
                return putStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return putCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                putResolve(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return putDoTriggerAI(af, this, mandatory);
            }

        };
        return abPutCounter;
    }

    /**
     * <p>createSpellPutCounters.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellPutCounters(final AbilityFactory af) {
        final SpellAbility spPutCounter = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -323471693082498224L;

            @Override
            public String getStackDescription() {
                return putStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                // if X depends on abCost, the AI needs to choose which card he would sacrifice first
                // then call xCount with that card to properly calculate the amount
                // Or choosing how many to sacrifice
                return putCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                putResolve(af, this);
            }

        };
        return spPutCounter;
    }

    /**
     * <p>createDrawbackPutCounters.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackPutCounters(final AbilityFactory af) {
        final SpellAbility dbPutCounter = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = -323471693082498224L;

            @Override
            public String getStackDescription() {
                return putStackDescription(af, this);
            }

            @Override
            public void resolve() {
                putResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return putPlayDrawbackAI(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return putDoTriggerAI(af, this, mandatory);
            }

        };
        return dbPutCounter;
    }

    /**
     * <p>putStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String putStackDescription(AbilityFactory af, SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        StringBuilder sb = new StringBuilder();

        if (!(sa instanceof Ability_Sub))
            sb.append(sa.getSourceCard().getName()).append(" - ");
        else
            sb.append(" ");

        Counters cType = Counters.valueOf(params.get("CounterType"));
        Card card = af.getHostCard();
        int amount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("CounterNum"), sa);

        sb.append("Put ").append(amount).append(" ").append(cType.getName())
                .append(" counter");
        if (amount != 1) sb.append("s");
        sb.append(" on ");

        ArrayList<Card> tgtCards;

        Target tgt = af.getAbTgt();
        if (tgt != null)
            tgtCards = tgt.getTargetCards();
        else {
            tgtCards = AbilityFactory.getDefinedCards(card, params.get("Defined"), sa);
        }

        Iterator<Card> it = tgtCards.iterator();
        while (it.hasNext()) {
            Card tgtC = it.next();
            if (tgtC.isFaceDown()) sb.append("Morph");
            else sb.append(tgtC);
            
            if (it.hasNext()) sb.append(", ");
        }

        sb.append(".");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>putCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean putCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        // AI needs to be expanded, since this function can be pretty complex based on what the expected targets could be
        HashMap<String, String> params = af.getMapParams();
        Random r = MyRandom.random;
        Cost abCost = sa.getPayCosts();
        Target abTgt = sa.getTarget();
        final Card source = sa.getSourceCard();
        CardList list;
        Card choice = null;
        String type = params.get("CounterType");
        String amountStr = params.get("CounterNum");

        Player player = af.isCurse() ? AllZone.getHumanPlayer() : AllZone.getComputerPlayer();


        list = AllZoneUtil.getPlayerCardsInPlay(player);
        list = list.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return CardFactoryUtil.canTarget(source, c) && !c.hasKeyword("CARDNAME can't have counters placed on it.");
            }
        });

        if (abTgt != null) {
            list = list.getValidCards(abTgt.getValidTgts(), source.getController(), source);

            if (list.size() < abTgt.getMinTargets(source, sa))
                return false;
        } else {    // "put counter on this"
            PlayerZone pZone = AllZone.getZone(source);
            // Don't activate Curse abilities on my cards and non-curse abilites on my opponents
            if (!pZone.getPlayer().equals(player))
                return false;
        }

        if (abCost != null) {
            // AI currently disabled for these costs
            if (abCost.getSacCost() && (!abCost.getSacThis() || source.isCreature())) {
                //only sacrifice something that's supposed to be sacrificed
                String sacType = abCost.getSacType();
                CardList typeList = AllZoneUtil.getPlayerCardsInPlay(AllZone.getComputerPlayer());
                typeList = typeList.getValidCards(sacType.split(","), source.getController(), source);
                if (ComputerUtil.getCardPreference(source, "SacCost", typeList) == null)
                    return false;
            }
            if (abCost.getLifeCost()) return false;
            if (abCost.getDiscardCost()) return false;

            if (abCost.getSubCounter()) {
                // A card has a 25% chance per counter to be able to pass through here
                // 4+ counters will always pass. 0 counters will never
                int currentNum = source.getCounters(abCost.getCounterType());
                double percent = .25 * (currentNum / abCost.getCounterNum());
                if (percent <= r.nextFloat())
                    return false;
            }
        }

        // TODO handle proper calculation of X values based on Cost
        int amount = AbilityFactory.calculateAmount(af.getHostCard(), amountStr, sa);

        if (amountStr.equals("X") && source.getSVar(amountStr).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            amount = ComputerUtil.determineLeftoverMana(sa);
            source.setSVar("PayX", Integer.toString(amount));
            // TODO:
        }

        //don't use it if no counters to add
        if (amount <= 0) return false;

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());

        // Targeting
        if (abTgt != null) {
            abTgt.resetTargets();
            // target loop
            while (abTgt.getNumTargeted() < abTgt.getMaxTargets(sa.getSourceCard(), sa)) {
                if (list.size() == 0) {
                    if (abTgt.getNumTargeted() < abTgt.getMinTargets(sa.getSourceCard(), sa) || abTgt.getNumTargeted() == 0) {
                        abTgt.resetTargets();
                        return false;
                    } else {
                        // TODO is this good enough? for up to amounts?
                        break;
                    }
                }

                if (af.isCurse()) {
                    choice = chooseCursedTarget(list, type, amount);
                } else {
                    choice = chooseBoonTarget(list, type);
                }

                if (choice == null) {    // can't find anything left
                    if (abTgt.getNumTargeted() < abTgt.getMinTargets(sa.getSourceCard(), sa) || abTgt.getNumTargeted() == 0) {
                        abTgt.resetTargets();
                        return false;
                    } else {
                        // TODO is this good enough? for up to amounts?
                        break;
                    }
                }
                list.remove(choice);
                abTgt.addTarget(choice);
            }
        } else {
            // Placeholder: No targeting necessary
            int currCounters = sa.getSourceCard().getCounters(Counters.valueOf(type));
            // each non +1/+1 counter on the card is a 10% chance of not activating this ability.

            if (!(type.equals("P1P1") || type.equals("ICE")) && r.nextFloat() < .1 * currCounters)
                return false;
        }

        //Don't use non P1P1/M1M1 counters before main 2 if possible
        if (AllZone.getPhase().isBefore(Constant.Phase.Main2) && !params.containsKey("ActivatingPhases")
                && !(type.equals("P1P1") || type.equals("M1M1")))
            return false;

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        if (AbilityFactory.playReusable(sa))
            return chance;

        return ((r.nextFloat() < .6667) && chance);
    }//putCanPlayAI

    /**
     * <p>putPlayDrawbackAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean putPlayDrawbackAI(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        boolean chance = true;
        Target abTgt = sa.getTarget();
        final Card source = sa.getSourceCard();
        CardList list;
        Card choice = null;
        String type = params.get("CounterType");
        String amountStr = params.get("CounterNum");
        final int amount = AbilityFactory.calculateAmount(af.getHostCard(), amountStr, sa);

        Player player = af.isCurse() ? AllZone.getHumanPlayer() : AllZone.getComputerPlayer();

        list = AllZoneUtil.getPlayerCardsInPlay(player);
        list = list.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return CardFactoryUtil.canTarget(source, c);
            }
        });

        if (abTgt != null) {
            list = list.getValidCards(abTgt.getValidTgts(), source.getController(), source);

            if (list.size() == 0)
                return false;

            abTgt.resetTargets();
            // target loop
            while (abTgt.getNumTargeted() < abTgt.getMaxTargets(sa.getSourceCard(), sa)) {
                if (list.size() == 0) {
                    if (abTgt.getNumTargeted() < abTgt.getMinTargets(sa.getSourceCard(), sa) || abTgt.getNumTargeted() == 0) {
                        abTgt.resetTargets();
                        return false;
                    } else {
                        break;
                    }
                }

                if (af.isCurse()) {
                    choice = chooseCursedTarget(list, type, amount);
                } else {

                }

                if (choice == null) {    // can't find anything left
                    if (abTgt.getNumTargeted() < abTgt.getMinTargets(sa.getSourceCard(), sa) || abTgt.getNumTargeted() == 0) {
                        abTgt.resetTargets();
                        return false;
                    } else {
                        // TODO is this good enough? for up to amounts?
                        break;
                    }
                }
                list.remove(choice);
                abTgt.addTarget(choice);
            }
        }

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        return chance;
    }//putPlayDrawbackAI

    /**
     * <p>putDoTriggerAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    private static boolean putDoTriggerAI(final AbilityFactory af, final SpellAbility sa, boolean mandatory) {
        // if there is a cost, it's gotta be optional
        if (!ComputerUtil.canPayCost(sa) && !mandatory)
            return false;

        HashMap<String, String> params = af.getMapParams();
        Target abTgt = sa.getTarget();
        final Card source = sa.getSourceCard();
        boolean chance = true;
        boolean preferred = true;
        CardList list;
        Player player = af.isCurse() ? AllZone.getHumanPlayer() : AllZone.getComputerPlayer();
        String type = params.get("CounterType");
        String amountStr = params.get("CounterNum");
        final int amount = AbilityFactory.calculateAmount(af.getHostCard(), amountStr, sa);

        if (abTgt == null) {
            // No target. So must be defined
            list = new CardList(AbilityFactory.getDefinedCards(source, params.get("Defined"), sa).toArray());

            if (!mandatory) {
                // TODO: If Trigger isn't mandatory, when wouldn't we want to put a counter?
                // things like Powder Keg, which are way too complex for the AI
            }
        } else {
            list = AllZoneUtil.getPlayerCardsInPlay(player);
            list = list.getTargetableCards(source);
            if (abTgt != null) {
                list = list.getValidCards(abTgt.getValidTgts(), source.getController(), source);
            }
            if (list.isEmpty() && mandatory) {
                // If there isn't any prefered cards to target, gotta choose non-preferred ones
                list = AllZoneUtil.getPlayerCardsInPlay(player.getOpponent());
                list = list.getTargetableCards(source);
                if (abTgt != null) {
                    list = list.getValidCards(abTgt.getValidTgts(), source.getController(), source);
                }
                preferred = false;
            }
            // Not mandatory, or the the list was regenerated and is still empty, so return false since there are no targets
            if (list.isEmpty())
                return false;

            Card choice = null;

            // Choose targets here:
            if (af.isCurse()) {
                if (preferred)
                    choice = chooseCursedTarget(list, type, amount);

                else {
                    if (type.equals("M1M1")) {
                        choice = CardFactoryUtil.AI_getWorstCreature(list);
                    } else {
                        choice = CardFactoryUtil.getRandomCard(list);
                    }
                }
            } else {
                if (preferred)
                    choice = chooseBoonTarget(list, type);

                else {
                    if (type.equals("P1P1")) {
                        choice = CardFactoryUtil.AI_getWorstCreature(list);
                    } else {
                        choice = CardFactoryUtil.getRandomCard(list);
                    }
                }
            }
            
            //TODO - I think choice can be null here.  Is that ok for addTarget()?
            abTgt.addTarget(choice);
        }

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.doTrigger(mandatory);

        return true;
    }

    /**
     * <p>chooseCursedTarget.</p>
     *
     * @param list a {@link forge.CardList} object.
     * @param type a {@link java.lang.String} object.
     * @param amount a int.
     * @return a {@link forge.Card} object.
     */
    private static Card chooseCursedTarget(CardList list, String type, final int amount) {
        Card choice;
        if (type.equals("M1M1")) {
            // try to kill the best killable creature, or reduce the best one
            CardList killable = list.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return c.getNetDefense() <= amount;
                }
            });
            if (killable.size() > 0)
                choice = CardFactoryUtil.AI_getBestCreature(killable);
            else
                choice = CardFactoryUtil.AI_getBestCreature(list);
        } else {
            // improve random choice here
            choice = CardFactoryUtil.getRandomCard(list);
        }
        return choice;
    }

    /**
     * <p>chooseBoonTarget.</p>
     *
     * @param list a {@link forge.CardList} object.
     * @param type a {@link java.lang.String} object.
     * @return a {@link forge.Card} object.
     */
    private static Card chooseBoonTarget(CardList list, String type) {
        Card choice;
        if (type.equals("P1P1")) {
            choice = CardFactoryUtil.AI_getBestCreature(list);
        } 
        else if(type.equals("DIVINITY")) {
        	list = list.filter(new CardListFilter() {
        		public boolean addCard(Card c) {
        			return c.getCounters(Counters.DIVINITY) == 0;
        		}
        	});
        	choice = CardFactoryUtil.AI_getMostExpensivePermanent(list, null, false);
        }
        else {
            // The AI really should put counters on cards that can use it.
            // Charge counters on things with Charge abilities, etc. Expand these above
            choice = CardFactoryUtil.getRandomCard(list);
        }
        return choice;
    }

    /**
     * <p>putResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void putResolve(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();

        Card card = af.getHostCard();
        String type = params.get("CounterType");
        int counterAmount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("CounterNum"), sa);

        ArrayList<Card> tgtCards;

        Target tgt = af.getAbTgt();
        if (tgt != null)
            tgtCards = tgt.getTargetCards();
        else {
            tgtCards = AbilityFactory.getDefinedCards(card, params.get("Defined"), sa);
        }

        for (Card tgtCard : tgtCards) {
            if (tgt == null || CardFactoryUtil.canTarget(card, tgtCard)) {
                if (AllZone.getZone(tgtCard).is(Constant.Zone.Battlefield))
                    tgtCard.addCounter(Counters.valueOf(type), counterAmount);
                else    // adding counters to something like re-suspend cards
                    tgtCard.addCounterFromNonEffect(Counters.valueOf(type), counterAmount);
            }
        }
    }

    // *******************************************
    // ********** RemoveCounters *****************
    // *******************************************

    /**
     * <p>createAbilityRemoveCounters.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityRemoveCounters(final AbilityFactory af) {
        final SpellAbility abRemCounter = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 8581011868395954121L;

            @Override
            public String getStackDescription() {
                return removeStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return removeCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                removeResolve(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return removeDoTriggerAI(af, this, mandatory);
            }

        };
        return abRemCounter;
    }

    /**
     * <p>createSpellRemoveCounters.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellRemoveCounters(final AbilityFactory af) {
        final SpellAbility spRemoveCounter = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -5065591869141835456L;

            @Override
            public String getStackDescription() {
                return removeStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                // if X depends on abCost, the AI needs to choose which card he would sacrifice first
                // then call xCount with that card to properly calculate the amount
                // Or choosing how many to sacrifice
                return removeCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                removeResolve(af, this);
            }

        };
        return spRemoveCounter;
    }

    /**
     * <p>createDrawbackRemoveCounters.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackRemoveCounters(final AbilityFactory af) {
        final SpellAbility spRemoveCounter = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = -5065591869141835456L;

            @Override
            public String getStackDescription() {
                return removeStackDescription(af, this);
            }

            @Override
            public void resolve() {
                removeResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return removePlayDrawbackAI(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return removeDoTriggerAI(af, this, mandatory);
            }

        };
        return spRemoveCounter;
    }

    /**
     * <p>removeStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String removeStackDescription(AbilityFactory af, SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        Card card = af.getHostCard();
        StringBuilder sb = new StringBuilder();

        if (!(sa instanceof Ability_Sub))
            sb.append(card).append(" - ");
        else
            sb.append(" ");

        Counters cType = Counters.valueOf(params.get("CounterType"));
        int amount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("CounterNum"), sa);

        sb.append("Remove ");
        if (params.containsKey("UpTo")) sb.append("up to ");
        sb.append(amount).append(" ").append(cType.getName()).append(" counter");
        if (amount != 1) sb.append("s");
        sb.append(" from");

        ArrayList<Card> tgtCards;

        Target tgt = af.getAbTgt();
        if (tgt != null)
            tgtCards = tgt.getTargetCards();
        else {
            tgtCards = AbilityFactory.getDefinedCards(card, params.get("Defined"), sa);
        }
        for (Card c : tgtCards)
            sb.append(" ").append(c);

        sb.append(".");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>removeCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean removeCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        // AI needs to be expanded, since this function can be pretty complex based on what the expected targets could be
        Random r = MyRandom.random;
        Cost abCost = sa.getPayCosts();
        //Target abTgt = sa.getTarget();
        final Card source = sa.getSourceCard();
        //CardList list;
        //Card choice = null;
        HashMap<String, String> params = af.getMapParams();

        String type = params.get("CounterType");
        //String amountStr = params.get("CounterNum");

        //TODO - currently, not targeted, only for Self

        //Player player = af.isCurse() ? AllZone.getHumanPlayer() : AllZone.getComputerPlayer();


        if (abCost != null) {
            // AI currently disabled for these costs
            if (abCost.getSacCost() && !abCost.getSacThis()) {
                //only sacrifice something that's supposed to be sacrificed
                String sacType = abCost.getSacType();
                CardList typeList = AllZoneUtil.getPlayerCardsInPlay(AllZone.getComputerPlayer());
                typeList = typeList.getValidCards(sacType.split(","), source.getController(), source);
                if (ComputerUtil.getCardPreference(source, "SacCost", typeList) == null)
                    return false;
            }
            if (abCost.getLifeCost()) return false;
            if (abCost.getDiscardCost()) return false;

            if (abCost.getSubCounter()) {
                // A card has a 25% chance per counter to be able to pass through here
                // 4+ counters will always pass. 0 counters will never
                int currentNum = source.getCounters(abCost.getCounterType());
                double percent = .25 * (currentNum / abCost.getCounterNum());
                if (percent <= r.nextFloat())
                    return false;
            }
        }

        // TODO handle proper calculation of X values based on Cost
        //final int amount = calculateAmount(af.getHostCard(), amountStr, sa);

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());

        //currently, not targeted

        // Placeholder: No targeting necessary
        int currCounters = sa.getSourceCard().getCounters(Counters.valueOf(type));
        // each counter on the card is a 10% chance of not activating this ability.
        if (r.nextFloat() < .1 * currCounters)
            return false;

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        return ((r.nextFloat() < .6667) && chance);
    }

    /**
     * <p>removePlayDrawbackAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean removePlayDrawbackAI(final AbilityFactory af, final SpellAbility sa) {
        // AI needs to be expanded, since this function can be pretty complex based on what the expected targets could be
        //Target abTgt = sa.getTarget();
        //final Card source = sa.getSourceCard();
        //CardList list;
        //Card choice = null;
        //HashMap<String,String> params = af.getMapParams();

        //String type = params.get("CounterType");
        //String amountStr = params.get("CounterNum");

        //TODO - currently, not targeted, only for Self

        //Player player = af.isCurse() ? AllZone.getHumanPlayer() : AllZone.getComputerPlayer();

        // TODO handle proper calculation of X values based on Cost
        //final int amount = calculateAmount(af.getHostCard(), amountStr, sa);

        // prevent run-away activations - first time will always return true
        boolean chance = true;

        //currently, not targeted

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        return chance;
    }

    /**
     * <p>removeDoTriggerAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    private static boolean removeDoTriggerAI(final AbilityFactory af, final SpellAbility sa, boolean mandatory) {
        // AI needs to be expanded, since this function can be pretty complex based on what the expected targets could be
        boolean chance = true;

        //TODO - currently, not targeted, only for Self

        // Note: Not many cards even use Trigger and Remove Counters. And even fewer are not mandatory
        // Since the targeting portion of this would be what

        if (!ComputerUtil.canPayCost(sa) && !mandatory)
            return false;

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.doTrigger(mandatory);

        return chance;
    }

    /**
     * <p>removeResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void removeResolve(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();

        Card card = af.getHostCard();
        String type = params.get("CounterType");
        int counterAmount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("CounterNum"), sa);

        ArrayList<Card> tgtCards;

        Target tgt = af.getAbTgt();
        if (tgt != null)
            tgtCards = tgt.getTargetCards();
        else {
            tgtCards = AbilityFactory.getDefinedCards(card, params.get("Defined"), sa);
        }

        for (Card tgtCard : tgtCards)
            if (tgt == null || CardFactoryUtil.canTarget(card, tgtCard)) {
                PlayerZone zone = AllZone.getZone(tgtCard);

                if (zone.is(Constant.Zone.Battlefield) || zone.is(Constant.Zone.Exile))
                    if (params.containsKey("UpTo") && sa.getActivatingPlayer().isHuman()) {
                        ArrayList<String> choices = new ArrayList<String>();
                        for (int i = 0; i <= counterAmount; i++) choices.add("" + i);
                        Object o = GuiUtils.getChoice("Select the number of " + type + " counters to remove", choices.toArray());
                        counterAmount = Integer.parseInt((String) o);
                    }
                tgtCard.subtractCounter(Counters.valueOf(type), counterAmount);
            }
    }

    // *******************************************
    // ********** Proliferate ********************
    // *******************************************

    /**
     * <p>createAbilityProliferate.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityProliferate(final AbilityFactory af) {
        final SpellAbility abProliferate = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -6617234927365102930L;

            @Override
            public boolean canPlayAI() {
                return proliferateShouldPlayAI(this);
            }

            @Override
            public void resolve() {
                proliferateResolve(af, this);
            }

            @Override
            public String getStackDescription() {
                return proliferateStackDescription(this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return proliferateDoTriggerAI(this, mandatory);
            }
        };

        return abProliferate;
    }

    /**
     * <p>createSpellProliferate.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellProliferate(final AbilityFactory af) {
        final SpellAbility spProliferate = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 1265466498444897146L;

            @Override
            public boolean canPlayAI() {
                return proliferateShouldPlayAI(this);
            }

            @Override
            public void resolve() {
                proliferateResolve(af, this);
            }

            @Override
            public boolean canPlay() {
                // super takes care of AdditionalCosts
                return super.canPlay();
            }

            @Override
            public String getStackDescription() {
                return proliferateStackDescription(this);
            }
        };

        return spProliferate;
    }

    /**
     * <p>createDrawbackProliferate.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackProliferate(final AbilityFactory af) {
        final SpellAbility dbProliferate = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = 1265466498444897146L;

            @Override
            public boolean canPlayAI() {
                return proliferateShouldPlayAI(this);
            }

            @Override
            public void resolve() {
                proliferateResolve(af, this);
            }

            @Override
            public String getStackDescription() {
                return proliferateStackDescription(this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return proliferateShouldPlayAI(this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return proliferateDoTriggerAI(this, mandatory);
            }
        };

        return dbProliferate;
    }

    /**
     * <p>proliferateStackDescription.</p>
     *
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String proliferateStackDescription(SpellAbility sa) {
        StringBuilder sb = new StringBuilder();
        if (!(sa instanceof Ability_Sub))
            sb.append(sa.getSourceCard()).append(" - ");
        else
            sb.append(" ");
        sb.append("Proliferate.");
        sb.append(" (You choose any number of permanents and/or players with ");
        sb.append("counters on them, then give each another counter of a kind already there.)");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>proliferateShouldPlayAI.</p>
     *
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean proliferateShouldPlayAI(SpellAbility sa) {
        boolean chance = true;
        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        // TODO: Make sure Human has poison counters or there are some counters we want to proliferate
        return chance;
    }

    /**
     * <p>proliferateDoTriggerAI.</p>
     *
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    private static boolean proliferateDoTriggerAI(SpellAbility sa, boolean mandatory) {
        boolean chance = true;
        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.doTrigger(mandatory);

        // TODO: Make sure Human has poison counters or there are some counters we want to proliferate
        return chance;
    }

    /**
     * <p>proliferateResolve.</p>
     *
     * @param AF a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void proliferateResolve(final AbilityFactory AF, SpellAbility sa) {
        CardList hperms = AllZoneUtil.getPlayerCardsInPlay(AllZone.getHumanPlayer());
        hperms = hperms.filter(new CardListFilter() {
            public boolean addCard(Card crd) {
                return !crd.getName().equals("Mana Pool") /*&& crd.hasCounters()*/;
            }
        });

        CardList cperms = AllZoneUtil.getPlayerCardsInPlay(AllZone.getComputerPlayer());
        cperms = cperms.filter(new CardListFilter() {
            public boolean addCard(Card crd) {
                return !crd.getName().equals("Mana Pool") /*&& crd.hasCounters()*/;
            }
        });

        if (AF.getHostCard().getController().isHuman()) {
            cperms.addAll(hperms);
            final CardList unchosen = cperms;
            AllZone.getInputControl().setInput(new Input() {
                private static final long serialVersionUID = -1779224307654698954L;

                @Override
                public void showMessage() {
                    ButtonUtil.enableOnlyCancel();
                    AllZone.getDisplay().showMessage("Proliferate: Choose permanents and/or players");
                }

                @Override
                public void selectButtonCancel() {
                    AllZone.getStack().chooseOrderOfSimultaneousStackEntryAll(); //Hacky intermittent solution to triggers that look for counters being put on. They used to wait for another priority passing after proliferate finished.
                    stop();
                }

                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if (!unchosen.contains(card)) return;
                    unchosen.remove(card);
                    ArrayList<String> choices = new ArrayList<String>();
                    for (Counters c_1 : Counters.values())
                        if (card.getCounters(c_1) != 0) choices.add(c_1.getName());
                    if (choices.size() > 0)
                        card.addCounter(Counters.getType((choices.size() == 1 ? choices.get(0) : GuiUtils.getChoice("Select counter type", choices.toArray()).toString())), 1);
                }

                boolean selComputer = false;
                boolean selHuman = false;

                @Override
                public void selectPlayer(Player player) {
                    if (player.isHuman() && selHuman == false) {
                        selHuman = true;
                        if (AllZone.getHumanPlayer().getPoisonCounters() > 0)
                            AllZone.getHumanPlayer().addPoisonCounters(1);
                    }
                    if (player.isComputer() && selComputer == false) {
                        selComputer = true;
                        if (AllZone.getComputerPlayer().getPoisonCounters() > 0)
                            AllZone.getComputerPlayer().addPoisonCounters(1);
                    }
                }
            });
        } else { //Compy
            cperms = cperms.filter(new CardListFilter() {
                public boolean addCard(Card crd) {
                    int pos = 0;
                    int neg = 0;
                    for (Counters c_1 : Counters.values()) {
                        if (crd.getCounters(c_1) != 0) {
                            if (CardFactoryUtil.isNegativeCounter(c_1))
                                neg++;
                            else
                                pos++;
                        }
                    }
                    return pos > neg;
                }
            });

            hperms = hperms.filter(new CardListFilter() {
                public boolean addCard(Card crd) {
                    int pos = 0;
                    int neg = 0;
                    for (Counters c_1 : Counters.values()) {
                        if (crd.getCounters(c_1) != 0) {
                            if (CardFactoryUtil.isNegativeCounter(c_1))
                                neg++;
                            else
                                pos++;
                        }
                    }
                    return pos < neg;
                }
            });

            StringBuilder sb = new StringBuilder();
            sb.append("<html>Proliferate: <br>Computer selects ");
            if (cperms.size() == 0 && hperms.size() == 0 && AllZone.getHumanPlayer().getPoisonCounters() == 0)
                sb.append("<b>nothing</b>.");
            else {
                if (cperms.size() > 0) {
                    sb.append("<br>From Computer's permanents: <br><b>");
                    for (Card c : cperms) {
                        sb.append(c);
                        sb.append(" ");
                    }
                    sb.append("</b><br>");
                }
                if (hperms.size() > 0) {
                    sb.append("<br>From Human's permanents: <br><b>");
                    for (Card c : cperms) {
                        sb.append(c);
                        sb.append(" ");
                    }
                    sb.append("</b><br>");
                }
                if (AllZone.getHumanPlayer().getPoisonCounters() > 0)
                    sb.append("<b>Human Player</b>.");
            }//else
            sb.append("</html>");


            //add a counter for each counter type, if it would benefit the computer
            for (Card c : cperms) {
                for (Counters c_1 : Counters.values())
                    if (c.getCounters(c_1) != 0) c.addCounter(c_1, 1);
            }

            //add a counter for each counter type, if it would screw over the player
            for (Card c : hperms) {
                for (Counters c_1 : Counters.values())
                    if (c.getCounters(c_1) != 0) c.addCounter(c_1, 1);
            }

            //give human a poison counter, if he has one
            if (AllZone.getHumanPlayer().getPoisonCounters() > 0)
                AllZone.getHumanPlayer().addPoisonCounters(1);

        } //comp
    }

    // *******************************************
    // ********** PutCounterAll ******************
    // *******************************************

    /**
     * <p>createAbilityPutCounterAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityPutCounterAll(final AbilityFactory af) {

        final SpellAbility abPutCounterAll = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -712473347429870385L;

            @Override
            public String getStackDescription() {
                return putAllStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return putAllCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                putAllResolve(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return putAllCanPlayAI(af, this);
            }

        };
        return abPutCounterAll;
    }

    /**
     * <p>createSpellPutCounterAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellPutCounterAll(final AbilityFactory af) {
        final SpellAbility spPutCounterAll = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -4400684695467183219L;

            @Override
            public String getStackDescription() {
                return putAllStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return putAllCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                putAllResolve(af, this);
            }

        };
        return spPutCounterAll;
    }

    /**
     * <p>createDrawbackPutCounterAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackPutCounterAll(final AbilityFactory af) {
        final SpellAbility dbPutCounterAll = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = -3101160929130043022L;

            @Override
            public String getStackDescription() {
                return putAllStackDescription(af, this);
            }

            @Override
            public void resolve() {
                putAllResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return putAllPlayDrawbackAI(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return putAllPlayDrawbackAI(af, this);
            }

        };
        return dbPutCounterAll;
    }

    /**
     * <p>putAllStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String putAllStackDescription(AbilityFactory af, SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        StringBuilder sb = new StringBuilder();

        if (!(sa instanceof Ability_Sub))
            sb.append(sa.getSourceCard().getName()).append(" - ");
        else
            sb.append(" ");

        Counters cType = Counters.valueOf(params.get("CounterType"));
        int amount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("CounterNum"), sa);

        sb.append("Put ").append(amount).append(" ").append(cType.getName()).append(" counter");
        if (amount != 1) sb.append("s");
        sb.append(" on each valid permanent.");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>putAllCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean putAllCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        // AI needs to be expanded, since this function can be pretty complex based on what the expected targets could be
        Random r = MyRandom.random;
        HashMap<String, String> params = af.getMapParams();
        Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();
        CardList hList;
        CardList cList;
        String type = params.get("CounterType");
        String amountStr = params.get("CounterNum");
        String valid = params.get("ValidCards");
        boolean curse = af.isCurse();
        Target tgt = sa.getTarget();

        hList = AllZoneUtil.getPlayerCardsInPlay(AllZone.getHumanPlayer());
        cList = AllZoneUtil.getPlayerCardsInPlay(AllZone.getComputerPlayer());

        hList = hList.getValidCards(valid, source.getController(), source);
        cList = cList.getValidCards(valid, source.getController(), source);

        if (abCost != null) {
            // AI currently disabled for these costs
            if (abCost.getSacCost()) {
                return false;
            }
            if (abCost.getLifeCost()) return false;
            if (abCost.getDiscardCost()) return false;
        }

        if (tgt != null) {
            Player pl;
            if (curse)
                pl = AllZone.getHumanPlayer();
            else
                pl = AllZone.getComputerPlayer();

            tgt.addTarget(pl);

            hList = hList.getController(pl);
            cList = cList.getController(pl);
        }

        // TODO improve X value to don't overpay when extra mana won't do anything more useful
        final int amount;
        if (amountStr.equals("X") && source.getSVar(amountStr).equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            amount = ComputerUtil.determineLeftoverMana(sa);
            source.setSVar("PayX", Integer.toString(amount));
        } else {
            amount = AbilityFactory.calculateAmount(af.getHostCard(), amountStr, sa);
        }

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());

        if (curse) {
            if (type.equals("M1M1")) {
                CardList killable = hList.filter(new CardListFilter() {
                    public boolean addCard(Card c) {
                        return c.getNetDefense() <= amount;
                    }
                });
                if (!(killable.size() > 2)) return false;
            } else {
                //make sure compy doesn't harm his stuff more than human's stuff
                if (cList.size() > hList.size()) return false;
            }
        } else {
            //human has more things that will benefit, don't play
            if (hList.size() >= cList.size()) return false;
        }

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        return ((r.nextFloat() < .6667) && chance);
    }

    /**
     * <p>putAllPlayDrawbackAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean putAllPlayDrawbackAI(final AbilityFactory af, final SpellAbility sa) {
        return putAllCanPlayAI(af, sa);
    }

    /**
     * <p>putAllResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void putAllResolve(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();

        String type = params.get("CounterType");
        int counterAmount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("CounterNum"), sa);
        String valid = params.get("ValidCards");

        CardList cards = AllZoneUtil.getCardsInPlay();
        cards = cards.getValidCards(valid, sa.getSourceCard().getController(), sa.getSourceCard());

        Target tgt = sa.getTarget();
        if (tgt != null) {
            Player pl = sa.getTargetPlayer();
            cards = cards.getController(pl);
        }

        for (Card tgtCard : cards) {
            if (AllZone.getZone(tgtCard).is(Constant.Zone.Battlefield))
                tgtCard.addCounter(Counters.valueOf(type), counterAmount);
            else    // adding counters to something like re-suspend cards
                tgtCard.addCounterFromNonEffect(Counters.valueOf(type), counterAmount);
        }
    }

    // *******************************************
    // ********** RemoveCounterAll ***************
    // *******************************************

    /**
     * <p>createAbilityRemoveCounterAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityRemoveCounterAll(final AbilityFactory af) {

        final SpellAbility abRemoveCounterAll = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 1189198508841846311L;

            @Override
            public String getStackDescription() {
                return removeCounterAllStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return removeCounterAllCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                removeCounterAllResolve(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return true;
            }

        };
        return abRemoveCounterAll;
    }

    /**
     * <p>createSpellRemoveCounterAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellRemoveCounterAll(final AbilityFactory af) {
        final SpellAbility spRemoveCounterAll = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 4173468877313664704L;

            @Override
            public String getStackDescription() {
                return removeCounterAllStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return removeCounterAllCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                removeCounterAllResolve(af, this);
            }

        };
        return spRemoveCounterAll;
    }

    /**
     * <p>createDrawbackRemoveCounterAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackRemoveCounterAll(final AbilityFactory af) {
        final SpellAbility dbRemoveCounterAll = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = 9210702927696563686L;

            @Override
            public String getStackDescription() {
                return removeCounterAllStackDescription(af, this);
            }

            @Override
            public void resolve() {
                removeCounterAllResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return removeCounterAllPlayDrawbackAI(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return removeCounterAllPlayDrawbackAI(af, this);
            }

        };
        return dbRemoveCounterAll;
    }

    /**
     * <p>removeCounterAllStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String removeCounterAllStackDescription(AbilityFactory af, SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        StringBuilder sb = new StringBuilder();

        if (!(sa instanceof Ability_Sub))
            sb.append(sa.getSourceCard()).append(" - ");
        else
            sb.append(" ");

        Counters cType = Counters.valueOf(params.get("CounterType"));
        int amount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("CounterNum"), sa);

        sb.append("Remove ").append(amount).append(" ").append(cType.getName()).append(" counter");
        if (amount != 1) sb.append("s");
        sb.append(" from each valid permanent.");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>removeCounterAllCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean removeCounterAllCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        //Heartmender is the only card using this, and it's from a trigger.
        //If at some point, other cards use this as a spell or ability, this will need to be implemented.
        return false;
    }

    /**
     * <p>removeCounterAllPlayDrawbackAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean removeCounterAllPlayDrawbackAI(final AbilityFactory af, final SpellAbility sa) {
        return removeCounterAllCanPlayAI(af, sa);
    }

    /**
     * <p>removeCounterAllResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void removeCounterAllResolve(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();

        String type = params.get("CounterType");
        int counterAmount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("CounterNum"), sa);
        String valid = params.get("ValidCards");

        CardList cards = AllZoneUtil.getCardsInPlay();
        cards = cards.getValidCards(valid, sa.getSourceCard().getController(), sa.getSourceCard());

        Target tgt = sa.getTarget();
        if (tgt != null) {
            Player pl = sa.getTargetPlayer();
            cards = cards.getController(pl);
        }

        for (Card tgtCard : cards) {
            tgtCard.subtractCounter(Counters.valueOf(type), counterAmount);
        }
    }

}//end class AbilityFactory_Counters
