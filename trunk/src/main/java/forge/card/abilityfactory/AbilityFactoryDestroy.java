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
package forge.card.abilityfactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;

import forge.Card;

import forge.CardLists;
import forge.CardUtil;
import forge.Counters;
import forge.Singletons;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.cost.CostUtil;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.game.player.ComputerUtil;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;


/**
 * <p>
 * AbilityFactory_Destroy class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class AbilityFactoryDestroy {
    // An AbilityFactory subclass for destroying permanents
    // *********************************************************************************
    // ************************** DESTROY
    // **********************************************
    // *********************************************************************************
    /**
     * <p>
     * createAbilityDestroy.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityDestroy(final AbilityFactory af) {
        class AbilityDestroy extends AbilityActivated {
            public AbilityDestroy(final Card ca, final Cost co, final Target t) {
                super(ca, co, t);
            }

            @Override
            public AbilityActivated getCopy() {
                AbilityActivated res = new AbilityDestroy(getSourceCard(),
                        getPayCosts(), getTarget() == null ? null : new Target(getTarget()));
                CardFactoryUtil.copySpellAbility(this, res);
                return res;
            }

            private static final long serialVersionUID = -4153613567150919283L;

            @Override
            public String getStackDescription() {
                return AbilityFactoryDestroy.destroyStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryDestroy.destroyCanPlayAI(getActivatingPlayer(), af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryDestroy.destroyResolve(af, this);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryDestroy.destroyDoTriggerAI(getActivatingPlayer(), af, this, mandatory);
            }
        }
        final SpellAbility abDestroy = new AbilityDestroy(af.getHostCard(), af.getAbCost(), af.getAbTgt());

        return abDestroy;
    }

    /**
     * <p>
     * createSpellDestroy.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellDestroy(final AbilityFactory af) {
        final SpellAbility spDestroy = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -317810567632846523L;

            @Override
            public String getStackDescription() {
                return AbilityFactoryDestroy.destroyStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryDestroy.destroyCanPlayAI(getActivatingPlayer(), af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryDestroy.destroyResolve(af, this);
            }

            @Override
            public boolean canPlayFromEffectAI(final boolean mandatory, final boolean withOutManaCost) {
                if (withOutManaCost) {
                    return AbilityFactoryDestroy.destroyDoTriggerAINoCost(getActivatingPlayer(), af, this, mandatory);
                }
                return AbilityFactoryDestroy.destroyDoTriggerAI(getActivatingPlayer(), af, this, mandatory);
            }

        };
        return spDestroy;
    }

    /**
     * <p>
     * createDrawbackDestroy.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.AbilitySub} object.
     */
    public static AbilitySub createDrawbackDestroy(final AbilityFactory af) {
        class DrawbackDestroy extends AbilitySub {
            public DrawbackDestroy(final Card ca, final Target t) {
                super(ca, t);
            }

            @Override
            public AbilitySub getCopy() {
                AbilitySub res = new DrawbackDestroy(getSourceCard(),
                        getTarget() == null ? null : new Target(getTarget()));
                CardFactoryUtil.copySpellAbility(this, res);
                return res;
            }

            private static final long serialVersionUID = -4153613567150919283L;

            @Override
            public String getStackDescription() {
                return AbilityFactoryDestroy.destroyStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryDestroy.destroyCanPlayAI(getActivatingPlayer(), af, this);
            }

            @Override
            public boolean chkAIDrawback() {
                return AbilityFactoryDestroy.destroyCanPlayAI(getActivatingPlayer(), af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryDestroy.destroyResolve(af, this);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryDestroy.destroyDoTriggerAI(getActivatingPlayer(), af, this, mandatory);
            }
        }
        final AbilitySub dbDestroy = new DrawbackDestroy(af.getHostCard(), af.getAbTgt());

        return dbDestroy;
    }

    /**
     * <p>
     * destroyCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean destroyCanPlayAI(final Player ai, final AbilityFactory af, final SpellAbility sa) {
        // AI needs to be expanded, since this function can be pretty complex
        // based on what the expected targets could be
        final Random r = MyRandom.getRandom();
        final Cost abCost = sa.getPayCosts();
        final Target abTgt = sa.getTarget();
        final Card source = sa.getSourceCard();
        final HashMap<String, String> params = af.getMapParams();
        final boolean noRegen = params.containsKey("NoRegen");
        List<Card> list;

        if (abCost != null) {
            if (!CostUtil.checkSacrificeCost(ai, abCost, source)) {
                return false;
            }

            if (!CostUtil.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }

            if (!CostUtil.checkDiscardCost(ai, abCost, source)) {
                return false;
            }
        }

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());

        // Targeting
        if (abTgt != null) {
            abTgt.resetTargets();
            list = ai.getOpponent().getCardsIn(ZoneType.Battlefield);
            list = CardLists.getTargetableCards(list, sa);
            list = CardLists.getValidCards(list, abTgt.getValidTgts(), source.getController(), source);
            if (params.containsKey("AITgts")) {
                list = CardLists.getValidCards(list, params.get("AITgts"), sa.getActivatingPlayer(), source);
            }
            list = CardLists.getNotKeyword(list, "Indestructible");
            if (!AbilityFactory.playReusable(ai, sa)) {
                list = CardLists.filter(list, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return (!c.hasKeyword("Undying") || c.getCounters(Counters.P1P1) > 0);
                    }
                });
            }

            // If NoRegen is not set, filter out creatures that have a
            // regeneration shield
            if (!noRegen) {
                // TODO filter out things that might be tougher?
                list = CardLists.filter(list, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return ((c.getShield() == 0) && !ComputerUtil.canRegenerate(c));
                    }
                });
            }


            if (list.size() == 0) {
                return false;
            }
            // target loop
            while (abTgt.getNumTargeted() < abTgt.getMaxTargets(sa.getSourceCard(), sa)) {
                if (list.size() == 0) {
                    if ((abTgt.getNumTargeted() < abTgt.getMinTargets(sa.getSourceCard(), sa))
                            || (abTgt.getNumTargeted() == 0)) {
                        abTgt.resetTargets();
                        return false;
                    } else {
                        // TODO is this good enough? for up to amounts?
                        break;
                    }
                }

                Card choice = null;
                // If the targets are only of one type, take the best
                if (CardLists.getNotType(list, "Creature").isEmpty()) {
                    choice = CardFactoryUtil.getBestCreatureAI(list);
                } else if (CardLists.getNotType(list, "Land").isEmpty()) {
                    choice = CardFactoryUtil.getBestLandAI(list);
                } else {
                    choice = CardFactoryUtil.getMostExpensivePermanentAI(list, sa, true);
                }

                if (choice == null) { // can't find anything left
                    if ((abTgt.getNumTargeted() < abTgt.getMinTargets(sa.getSourceCard(), sa))
                            || (abTgt.getNumTargeted() == 0)) {
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
            if (params.containsKey("Defined")) {
                list = new ArrayList<Card>(AbilityFactory.getDefinedCards(af.getHostCard(), params.get("Defined"), sa));
                if (list.isEmpty()
                        || !CardLists.filterControlledBy(list, ai).isEmpty()
                        || CardLists.getNotKeyword(list, "Indestructible").isEmpty()) {
                    return false;
                }
            }
        }

        final AbilitySub subAb = sa.getSubAbility();
        if (subAb != null) {
            chance &= subAb.chkAIDrawback();
        }

        return chance;
    }

    /**
     * <p>
     * destroyDoTriggerAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @return a boolean.
     */
    private static boolean destroyDoTriggerAI(final Player ai, final AbilityFactory af, final SpellAbility sa, final boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa, ai)) {
            return false;
        }
        return destroyDoTriggerAINoCost(ai, af, sa, mandatory);
    }

    /**
     * <p>
     * destroyDoTriggerAINoCost.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @return a boolean.
     */
    private static boolean destroyDoTriggerAINoCost(final Player ai, final AbilityFactory af, final SpellAbility sa, final boolean mandatory) {

        final Target tgt = sa.getTarget();
        final Card source = sa.getSourceCard();
        final HashMap<String, String> params = af.getMapParams();
        final boolean noRegen = params.containsKey("NoRegen");
        final Player opp = ai.getOpponent();
        if (tgt != null) {
            List<Card> list;
            list = Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield);
            list = CardLists.getTargetableCards(list, sa);
            list = CardLists.getValidCards(list, tgt.getValidTgts(), source.getController(), source);

            if ((list.size() == 0) || (list.size() < tgt.getMinTargets(sa.getSourceCard(), sa))) {
                return false;
            }

            tgt.resetTargets();

            List<Card> preferred = CardLists.getNotKeyword(list, "Indestructible");
            preferred = CardLists.filterControlledBy(preferred, opp);

            // If NoRegen is not set, filter out creatures that have a
            // regeneration shield
            if (!noRegen) {
                // TODO filter out things that could regenerate in response?
                // might be tougher?
                preferred = CardLists.filter(preferred, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return c.getShield() == 0;
                    }
                });
            }

            for (final Card c : preferred) {
                list.remove(c);
            }

            while (tgt.getNumTargeted() < tgt.getMaxTargets(sa.getSourceCard(), sa)) {
                if (preferred.size() == 0) {
                    if ((tgt.getNumTargeted() == 0)
                            || (tgt.getNumTargeted() < tgt.getMinTargets(sa.getSourceCard(), sa))) {
                        if (!mandatory) {
                            tgt.resetTargets();
                            return false;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    Card c;
                    if (CardLists.getNotType(preferred, "Creature").size() == 0) {
                        c = CardFactoryUtil.getBestCreatureAI(preferred);
                    } else if (CardLists.getNotType(preferred, "Land").size() == 0) {
                        c = CardFactoryUtil.getBestLandAI(preferred);
                    } else {
                        c = CardFactoryUtil.getMostExpensivePermanentAI(preferred, sa, false);
                    }
                    tgt.addTarget(c);
                    preferred.remove(c);
                }
            }

            while (tgt.getNumTargeted() < tgt.getMinTargets(sa.getSourceCard(), sa)) {
                if (list.size() == 0) {
                    break;
                } else {
                    Card c;
                    if (CardLists.getNotType(list, "Creature").size() == 0) {
                        c = CardFactoryUtil.getWorstCreatureAI(list);
                    } else {
                        c = CardFactoryUtil.getCheapestPermanentAI(list, sa, false);
                    }
                    tgt.addTarget(c);
                    list.remove(c);
                }
            }

            if (tgt.getNumTargeted() < tgt.getMinTargets(sa.getSourceCard(), sa)) {
                return false;
            }
        } else {
            if (!mandatory) {
                return false;
            }
        }

        final AbilitySub subAb = sa.getSubAbility();
        if (subAb != null) {
            return subAb.doTrigger(mandatory);
        }

        return true;
    }

    /**
     * <p>
     * destroyStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String destroyStackDescription(final AbilityFactory af, final SpellAbility sa) {
        final HashMap<String, String> params = af.getMapParams();
        final boolean noRegen = params.containsKey("NoRegen");
        final StringBuilder sb = new StringBuilder();
        final Card host = af.getHostCard();

        final String conditionDesc = params.get("ConditionDescription");
        if (conditionDesc != null) {
            sb.append(conditionDesc).append(" ");
        }

        ArrayList<Card> tgtCards;

        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgtCards = tgt.getTargetCards();
        } else {
            tgtCards = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa);
        }

        if (sa instanceof AbilitySub) {
            sb.append(" ");
        } else {
            sb.append(host).append(" - ");
        }

        if (params.containsKey("Sacrifice")) {
            sb.append("Sacrifice ");
        } else {
            sb.append("Destroy ");
        }

        final Iterator<Card> it = tgtCards.iterator();
        while (it.hasNext()) {
            final Card tgtC = it.next();
            if (tgtC.isFaceDown()) {
                sb.append("Morph ").append("(").append(tgtC.getUniqueNumber()).append(")");
            } else {
                sb.append(tgtC);
            }

            if (it.hasNext()) {
                sb.append(", ");
            }
        }

        if (af.getMapParams().containsKey("Radiance")) {
            sb.append(" and each other ").append(af.getMapParams().get("ValidTgts"))
                    .append(" that shares a color with ");
            if (tgtCards.size() > 1) {
                sb.append("them");
            } else {
                sb.append("it");
            }
        }

        if (noRegen) {
            sb.append(". ");
            if (tgtCards.size() == 1) {
                sb.append("It");
            } else {
                sb.append("They");
            }
            sb.append(" can't be regenerated");
        }
        sb.append(".");

        final AbilitySub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>
     * destroyResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void destroyResolve(final AbilityFactory af, final SpellAbility sa) {
        final HashMap<String, String> params = af.getMapParams();
        final Card card = sa.getSourceCard();

        final boolean remDestroyed = params.containsKey("RememberDestroyed");
        if (remDestroyed) {
            card.clearRemembered();
        }

        final boolean noRegen = params.containsKey("NoRegen");
        final boolean sac = params.containsKey("Sacrifice");

        ArrayList<Card> tgtCards;
        final ArrayList<Card> untargetedCards = new ArrayList<Card>();

        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgtCards = tgt.getTargetCards();
        } else {
            tgtCards = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa);
        }

        if (params.containsKey("Radiance")) {
            for (final Card c : CardUtil.getRadiance(af.getHostCard(), tgtCards.get(0),
                    params.get("ValidTgts").split(","))) {
                untargetedCards.add(c);
            }
        }

        for (final Card tgtC : tgtCards) {
            if (tgtC.isInPlay() && ((tgt == null) || tgtC.canBeTargetedBy(sa))) {
                boolean destroyed = false;
                if (sac) {
                    destroyed = Singletons.getModel().getGame().getAction().sacrifice(tgtC, sa);
                } else if (noRegen) {
                    destroyed = Singletons.getModel().getGame().getAction().destroyNoRegeneration(tgtC);
                } else {
                    destroyed = Singletons.getModel().getGame().getAction().destroy(tgtC);
                } if (destroyed  && remDestroyed) {
                    card.addRemembered(tgtC);
                }
            }
        }

        for (final Card unTgtC : untargetedCards) {
            if (unTgtC.isInPlay()) {
                boolean destroyed = false;
                if (sac) {
                    destroyed = Singletons.getModel().getGame().getAction().sacrifice(unTgtC, sa);
                } else if (noRegen) {
                    destroyed = Singletons.getModel().getGame().getAction().destroyNoRegeneration(unTgtC);
                } else {
                    destroyed = Singletons.getModel().getGame().getAction().destroy(unTgtC);
                } if (destroyed  && remDestroyed) {
                    card.addRemembered(unTgtC);
                }
            }
        }
    }

    // *********************************************************************************
    // ************************ DESTROY ALL
    // ********************************************
    // *********************************************************************************
    /**
     * <p>
     * createAbilityDestroyAll.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityDestroyAll(final AbilityFactory af) {
        class AbilityDestroyAll extends AbilityActivated {
            public AbilityDestroyAll(final Card ca, final Cost co, final Target t) {
                super(ca, co, t);
            }

            @Override
            public AbilityActivated getCopy() {
                AbilityActivated res = new AbilityDestroyAll(getSourceCard(),
                        getPayCosts(), getTarget() == null ? null : new Target(getTarget()));
                CardFactoryUtil.copySpellAbility(this, res);
                return res;
            }

            private static final long serialVersionUID = -1376444173137861437L;

            private final HashMap<String, String> params = af.getMapParams();
            private final boolean noRegen = this.params.containsKey("NoRegen");

            @Override
            public String getStackDescription() {
                return AbilityFactoryDestroy.destroyAllStackDescription(af, this, this.noRegen);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryDestroy.destroyAllCanPlayAI(getActivatingPlayer(), af, this, this.noRegen);
            }

            @Override
            public void resolve() {
                AbilityFactoryDestroy.destroyAllResolve(af, this, this.noRegen);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryDestroy.destroyAllCanPlayAI(getActivatingPlayer(), af, this, this.noRegen);
            }
        }
        final SpellAbility abDestroyAll = new AbilityDestroyAll(af.getHostCard(), af.getAbCost(), af.getAbTgt());

        return abDestroyAll;
    }

    /**
     * <p>
     * createSpellDestroyAll.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellDestroyAll(final AbilityFactory af) {
        final SpellAbility spDestroyAll = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -3712659336576469102L;

            private final HashMap<String, String> params = af.getMapParams();
            private final boolean noRegen = this.params.containsKey("NoRegen");

            @Override
            public String getStackDescription() {
                return AbilityFactoryDestroy.destroyAllStackDescription(af, this, this.noRegen);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryDestroy.destroyAllCanPlayAI(getActivatingPlayer(), af, this, this.noRegen);
            }

            @Override
            public void resolve() {
                AbilityFactoryDestroy.destroyAllResolve(af, this, this.noRegen);
            }

        };
        return spDestroyAll;
    }

    /**
     * <p>
     * createDrawbackDestroyAll.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackDestroyAll(final AbilityFactory af) {
        class DrawbackDestroyAll extends AbilitySub {
            public DrawbackDestroyAll(final Card ca, final Target t) {
                super(ca, t);
            }

            @Override
            public AbilitySub getCopy() {
                AbilitySub res = new DrawbackDestroyAll(getSourceCard(),
                        getTarget() == null ? null : new Target(getTarget()));
                CardFactoryUtil.copySpellAbility(this, res);
                return res;
            }

            private static final long serialVersionUID = -242160421677518351L;

            private final HashMap<String, String> params = af.getMapParams();
            private final boolean noRegen = this.params.containsKey("NoRegen");

            @Override
            public String getStackDescription() {
                return AbilityFactoryDestroy.destroyAllStackDescription(af, this, this.noRegen);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryDestroy.destroyAllCanPlayAI(getActivatingPlayer(), af, this, this.noRegen);
            }

            @Override
            public void resolve() {
                AbilityFactoryDestroy.destroyAllResolve(af, this, this.noRegen);
            }

            @Override
            public boolean chkAIDrawback() {
                return true;
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryDestroy.destroyAllTriggerAI(getActivatingPlayer(), af, this, mandatory);
            }
        }
        final SpellAbility dbDestroyAll = new DrawbackDestroyAll(af.getHostCard(), af.getAbTgt());

        return dbDestroyAll;
    }

    /**
     * <p>
     * destroyAllStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param noRegen
     *            a boolean.
     * @return a {@link java.lang.String} object.
     */
    private static String destroyAllStackDescription(final AbilityFactory af, final SpellAbility sa,
            final boolean noRegen) {

        final StringBuilder sb = new StringBuilder();
        final String name = af.getHostCard().getName();
        final HashMap<String, String> params = af.getMapParams();
        if (!(sa instanceof AbilitySub)) {
            sb.append(sa.getSourceCard().getName()).append(" - ");
        } else {
            sb.append(" ");
        }

        if (params.containsKey("SpellDescription")) {
            sb.append(params.get("SpellDescription"));
        } else {
            final String conditionDesc = params.get("ConditionDescription");
            if (conditionDesc != null) {
                sb.append(conditionDesc).append(" ");
            }

            ArrayList<Card> tgtCards;

            final Target tgt = sa.getTarget();
            if (tgt != null) {
                tgtCards = tgt.getTargetCards();
            } else {
                tgtCards = new ArrayList<Card>();
                tgtCards.add(sa.getSourceCard());
            }

            sb.append(name).append(" - Destroy permanents.");

            if (noRegen) {
                sb.append(" They can't be regenerated");
            }
        }

        final AbilitySub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>
     * destroyAllCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param noRegen
     *            a boolean.
     * @return a boolean.
     */
    private static boolean destroyAllTriggerAI(final Player ai, final AbilityFactory af, final SpellAbility sa, final boolean mandatory) {
        final Card source = sa.getSourceCard();
        final HashMap<String, String> params = af.getMapParams();
        final Target tgt = sa.getTarget();
        String valid = "";
        if (params.containsKey("ValidCards")) {
            valid = params.get("ValidCards");
        }
        List<Card> humanlist = ai.getOpponent().getCardsIn(ZoneType.Battlefield);
        List<Card> computerlist = ai.getCardsIn(ZoneType.Battlefield);
        if (sa.getTarget() != null) {
            tgt.resetTargets();
            sa.getTarget().addTarget(ai.getOpponent());
            computerlist.clear();
        }
        if (mandatory) {
            return true;
        }
        humanlist = CardLists.getValidCards(humanlist, valid.split(","), source.getController(), source);
        computerlist = CardLists.getValidCards(computerlist, valid.split(","), source.getController(), source);
        humanlist = CardLists.filter(humanlist, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return !(c.hasKeyword("Indestructible") || c.getSVar("SacMe").length() > 0);
            }
        });
        computerlist = CardLists.filter(computerlist, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return !(c.hasKeyword("Indestructible") || c.getSVar("SacMe").length() > 0);
            }
        });
        if (humanlist.isEmpty() && !computerlist.isEmpty()) {
            return false;
        }

        // if only creatures are affected evaluate both lists and pass only if
        // human creatures are more valuable
        if ((CardLists.getNotType(humanlist, "Creature").size() == 0) && (CardLists.getNotType(computerlist, "Creature").size() == 0)) {
            if (CardFactoryUtil.evaluateCreatureList(computerlist) >= CardFactoryUtil.evaluateCreatureList(humanlist)
                    && !computerlist.isEmpty()) {
                return false;
            }
        } // otherwise evaluate both lists by CMC and pass only if human
          // permanents are more valuable
        else if (CardFactoryUtil.evaluatePermanentList(computerlist) >= CardFactoryUtil.evaluatePermanentList(humanlist)) {
            return false;
        }

        final AbilitySub subAb = sa.getSubAbility();
        if (subAb != null && !subAb.chkAIDrawback()) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * destroyAllCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param noRegen
     *            a boolean.
     * @return a boolean.
     */
    private static boolean destroyAllCanPlayAI(final Player ai, final AbilityFactory af, final SpellAbility sa, final boolean noRegen) {
        // AI needs to be expanded, since this function can be pretty complex
        // based on what the expected targets could be
        final Random r = MyRandom.getRandom();
        final Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();
        final HashMap<String, String> params = af.getMapParams();
        String valid = "";

        if (params.containsKey("ValidCards")) {
            valid = params.get("ValidCards");
        }

        if (valid.contains("X") && source.getSVar("X").equals("Count$xPaid")) {
            // Set PayX here to maximum value.
            final int xPay = ComputerUtil.determineLeftoverMana(sa, ai);
            source.setSVar("PayX", Integer.toString(xPay));
            valid = valid.replace("X", Integer.toString(xPay));
        }

        List<Card> humanlist = ai.getOpponent().getCardsIn(ZoneType.Battlefield);
        List<Card> computerlist = ai.getCardsIn(ZoneType.Battlefield);

        final Target tgt = sa.getTarget();

        if (sa.getTarget() != null) {
            tgt.resetTargets();
            sa.getTarget().addTarget(ai.getOpponent());
            computerlist.clear();
        }

        humanlist = CardLists.getValidCards(humanlist, valid.split(","), source.getController(), source);
        computerlist = CardLists.getValidCards(computerlist, valid.split(","), source.getController(), source);

        humanlist = CardLists.filter(humanlist, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return !(c.hasKeyword("Indestructible") || c.getSVar("SacMe").length() > 0);
            }
        });
        computerlist = CardLists.filter(computerlist, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return !(c.hasKeyword("Indestructible") || c.getSVar("SacMe").length() > 0);
            }
        });

        if (abCost != null) {
            // AI currently disabled for some costs

            if (!CostUtil.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }
        }

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= Math.pow(.6667, sa.getActivationsThisTurn());

        // if only creatures are affected evaluate both lists and pass only if
        // human creatures are more valuable
        if ((CardLists.getNotType(humanlist, "Creature").size() == 0) && (CardLists.getNotType(computerlist, "Creature").size() == 0)) {
            if ((CardFactoryUtil.evaluateCreatureList(computerlist) + 200) >= CardFactoryUtil
                    .evaluateCreatureList(humanlist)) {
                return false;
            }
        } // only lands involved
        else if ((CardLists.getNotType(humanlist, "Land").size() == 0) && (CardLists.getNotType(computerlist, "Land").size() == 0)) {
            if ((CardFactoryUtil.evaluatePermanentList(computerlist) + 1) >= CardFactoryUtil
                    .evaluatePermanentList(humanlist)) {
                return false;
            }
        } // otherwise evaluate both lists by CMC and pass only if human
          // permanents are more valuable
        else if ((CardFactoryUtil.evaluatePermanentList(computerlist) + 3) >= CardFactoryUtil
                .evaluatePermanentList(humanlist)) {
            return false;
        }

        final AbilitySub subAb = sa.getSubAbility();
        if (subAb != null) {
            chance &= subAb.chkAIDrawback();
        }

        return chance;
    }

    /**
     * <p>
     * destroyAllResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param noRegen
     *            a boolean.
     */
    private static void destroyAllResolve(final AbilityFactory af, final SpellAbility sa, final boolean noRegen) {
        final HashMap<String, String> params = af.getMapParams();

        final Card card = sa.getSourceCard();

        final Target tgt = sa.getTarget();
        Player targetPlayer = null;
        if (tgt != null) {
            for (final Object o : tgt.getTargets()) {
                if (o instanceof Player) {
                    targetPlayer = (Player) o;
                }
            }
        }

        String valid = "";

        if (params.containsKey("ValidCards")) {
            valid = params.get("ValidCards");
        }

        // Ugh. If calculateAmount needs to be called with DestroyAll it _needs_
        // to use the X variable
        // We really need a better solution to this
        if (valid.contains("X")) {
            valid = valid.replace("X", Integer.toString(AbilityFactory.calculateAmount(card, "X", sa)));
        }

        List<Card> list = Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield);

        if (targetPlayer != null) {
            list = CardLists.filterControlledBy(list, targetPlayer);
        }

        list = AbilityFactory.filterListByType(list, valid, sa);

        final boolean remDestroyed = params.containsKey("RememberDestroyed");
        if (remDestroyed) {
            card.clearRemembered();
        }

        if (noRegen) {
            for (int i = 0; i < list.size(); i++) {
                if (Singletons.getModel().getGame().getAction().destroyNoRegeneration(list.get(i)) && remDestroyed) {
                    card.addRemembered(list.get(i));
                }
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (Singletons.getModel().getGame().getAction().destroy(list.get(i)) && remDestroyed) {
                    card.addRemembered(list.get(i));
                }
            }
        }
    }

} // end class AbilityFactory_Destroy
