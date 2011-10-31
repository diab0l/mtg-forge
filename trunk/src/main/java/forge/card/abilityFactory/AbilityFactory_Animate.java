package forge.card.abilityFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.Card;
import forge.CardList;
import forge.CardUtil;
import forge.Command;
import forge.ComputerUtil;
import forge.Constant;
import forge.Constant.Zone;
import forge.Player;
import forge.card.spellability.Ability_Activated;
import forge.card.spellability.Ability_Sub;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.card.staticAbility.StaticAbility;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;

/**
 * <p>
 * AbilityFactory_Animate class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public final class AbilityFactory_Animate {

    private AbilityFactory_Animate() {
        throw new AssertionError();
    }

    // **************************************************************
    // ************************** Animate ***************************
    // **************************************************************

    /**
     * <p>
     * createAbilityAnimate.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityAnimate(final AbilityFactory af) {
        final SpellAbility abAnimate = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 1938171749867735155L;

            @Override
            public boolean canPlayAI() {
                return AbilityFactory_Animate.animateCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                AbilityFactory_Animate.animateResolve(af, this);
            }

            @Override
            public String getStackDescription() {
                return AbilityFactory_Animate.animateStackDescription(af, this);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactory_Animate.animateTriggerAI(af, this, mandatory);
            }
        };
        return abAnimate;
    }

    /**
     * <p>
     * createSpellAnimate.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellAnimate(final AbilityFactory af) {
        final SpellAbility spAnimate = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -4047747186919390147L;

            @Override
            public boolean canPlayAI() {
                return AbilityFactory_Animate.animateCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                AbilityFactory_Animate.animateResolve(af, this);
            }

            @Override
            public String getStackDescription() {
                return AbilityFactory_Animate.animateStackDescription(af, this);
            }
        };
        return spAnimate;
    }

    /**
     * <p>
     * createDrawbackAnimate.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackAnimate(final AbilityFactory af) {
        final SpellAbility dbAnimate = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = -8659938411460952874L;

            @Override
            public void resolve() {
                AbilityFactory_Animate.animateResolve(af, this);
            }

            @Override
            public boolean chkAIDrawback() {
                return AbilityFactory_Animate.animatePlayDrawbackAI(af, this);
            }

            @Override
            public String getStackDescription() {
                return AbilityFactory_Animate.animateStackDescription(af, this);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactory_Animate.animateTriggerAI(af, this, mandatory);
            }
        };
        return dbAnimate;
    }

    /**
     * <p>
     * animateStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String animateStackDescription(final AbilityFactory af, final SpellAbility sa) {
        final HashMap<String, String> params = af.getMapParams();
        final Card host = af.getHostCard();
        final Map<String, String> svars = host.getSVars();

        int power = -1;
        if (params.containsKey("Power")) {
            power = AbilityFactory.calculateAmount(host, params.get("Power"), sa);
        }
        int toughness = -1;
        if (params.containsKey("Toughness")) {
            toughness = AbilityFactory.calculateAmount(host, params.get("Toughness"), sa);
        }

        final boolean permanent = params.containsKey("Permanent");
        final ArrayList<String> types = new ArrayList<String>();
        if (params.containsKey("Types")) {
            types.addAll(Arrays.asList(params.get("Types").split(",")));
        }
        final ArrayList<String> keywords = new ArrayList<String>();
        if (params.containsKey("Keywords")) {
            keywords.addAll(Arrays.asList(params.get("Keywords").split(" & ")));
        }
        // allow SVar substitution for keywords
        for (int i = 0; i < keywords.size(); i++) {
            final String k = keywords.get(i);
            if (svars.containsKey(k)) {
                keywords.add("\"" + k + "\"");
                keywords.remove(k);
            }
        }
        final ArrayList<String> colors = new ArrayList<String>();
        if (params.containsKey("Colors")) {
            colors.addAll(Arrays.asList(params.get("Colors").split(",")));
        }

        final StringBuilder sb = new StringBuilder();

        if (sa instanceof Ability_Sub) {
            sb.append(" ");
        } else {
            sb.append(sa.getSourceCard().getName()).append(" - ");
        }

        final Target tgt = af.getAbTgt();
        ArrayList<Card> tgts;
        if (tgt != null) {
            tgts = tgt.getTargetCards();
        } else {
            tgts = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa);
        }

        for (final Card c : tgts) {
            sb.append(c).append(" ");
        }
        sb.append("become");
        if (tgts.size() == 1) {
            sb.append("s a");
        }
        // if power is -1, we'll assume it's not just setting toughness
        if (power != -1) {
            sb.append(" ").append(power).append("/").append(toughness);
        }

        if (colors.size() > 0) {
            sb.append(" ");
        }
        if (colors.contains("ChosenColor")) {
            sb.append("color of that player's choice");
        } else {
            for (int i = 0; i < colors.size(); i++) {
                sb.append(colors.get(i));
                if (i < (colors.size() - 1)) {
                    sb.append(" and ");
                }
            }
        }
        sb.append(" ");
        if (types.contains("ChosenType")) {
            sb.append("type of player's choice ");
        } else {
            for (int i = types.size() - 1; i >= 0; i--) {
                sb.append(types.get(i));
                sb.append(" ");
            }
        }
        if (keywords.size() > 0) {
            sb.append("with ");
        }
        for (int i = 0; i < keywords.size(); i++) {
            sb.append(keywords.get(i));
            if (i < (keywords.size() - 1)) {
                sb.append(" and ");
            }
        }
        // sb.append(abilities)
        // sb.append(triggers)
        if (!permanent) {
            if (params.containsKey("UntilEndOfCombat")) {
                sb.append(" until end of combat.");
            } else if (params.containsKey("UntilHostLeavesPlay")) {
                sb.append(" until ").append(host).append(" leaves the battlefield.");
            } else if (params.containsKey("UntilYourNextUpkeep")) {
                sb.append(" until your next upkeep.");
            } else {
                sb.append(" until end of turn.");
            }
        } else {
            sb.append(".");
        }

        final Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    } // end animateStackDescription()

    /**
     * <p>
     * animateCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean animateCanPlayAI(final AbilityFactory af, final SpellAbility sa) {

        final HashMap<String, String> params = af.getMapParams();
        final Target tgt = af.getAbTgt();
        final Card source = sa.getSourceCard();

        boolean useAbility = true;

        // TODO - add some kind of check to answer
        // "Am I going to attack with this?"
        // TODO - add some kind of check for during human turn to answer
        // "Can I use this to block something?"

        // don't use instant speed animate abilities outside computers
        // Combat_Begin step
        if (!AllZone.getPhase().is(Constant.Phase.COMBAT_BEGIN)
                && AllZone.getPhase().isPlayerTurn(AllZone.getComputerPlayer()) && !AbilityFactory.isSorcerySpeed(sa)
                && !params.containsKey("ActivationPhases") && !params.containsKey("Permanent")) {
            return false;
        }

        // don't use instant speed animate abilities outside humans
        // Combat_Declare_Attackers_InstantAbility step
        if ((!AllZone.getPhase().is(Constant.Phase.COMBAT_DECLARE_ATTACKERS_INSTANT_ABILITY) || (AllZone.getCombat()
                .getAttackers().length == 0)) && AllZone.getPhase().isPlayerTurn(AllZone.getHumanPlayer())) {
            return false;
        }

        // don't activate during main2 unless this effect is permanent
        if (AllZone.getPhase().is(Constant.Phase.MAIN2) && !params.containsKey("Permanent")) {
            return false;
        }

        if (null == tgt) {
            final ArrayList<Card> defined = AbilityFactory.getDefinedCards(source, params.get("Defined"), sa);

            boolean bFlag = false;
            for (final Card c : defined) {
                bFlag |= (!c.isCreature() && !c.isTapped() && !(c.getTurnInZone() == AllZone.getPhase().getTurn()));

                // for creatures that could be improved (like Figure of Destiny)
                if (c.isCreature() && (params.containsKey("Permanent") || (!c.isTapped() && !c.isSick()))) {
                    int power = -5;
                    if (params.containsKey("Power")) {
                        power = AbilityFactory.calculateAmount(source, params.get("Power"), sa);
                    }
                    int toughness = -5;
                    if (params.containsKey("Toughness")) {
                        toughness = AbilityFactory.calculateAmount(source, params.get("Toughness"), sa);
                    }
                    if ((power + toughness) > (c.getCurrentPower() + c.getCurrentToughness())) {
                        bFlag = true;
                    }
                }

            }

            if (!bFlag) { // All of the defined stuff is animated, not very
                          // useful
                return false;
            }
        } else {
            tgt.resetTargets();
            useAbility &= AbilityFactory_Animate.animateTgtAI(af, sa);
        }

        final Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null) {
            useAbility &= subAb.chkAIDrawback();
        }

        return useAbility;
    } // end animateCanPlayAI()

    /**
     * <p>
     * animatePlayDrawbackAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean animatePlayDrawbackAI(final AbilityFactory af, final SpellAbility sa) {
        // AI should only activate this during Human's turn
        boolean chance = AbilityFactory_Animate.animateTgtAI(af, sa);

        // TODO - restrict the subAbility a bit

        final Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null) {
            chance &= subAb.chkAIDrawback();
        }

        return chance;
    }

    /**
     * <p>
     * animateTriggerAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @return a boolean.
     */
    private static boolean animateTriggerAI(final AbilityFactory af, final SpellAbility sa, final boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa)) { // If there is a cost payment
            return false;
        }

        boolean chance = AbilityFactory_Animate.animateTgtAI(af, sa);

        // Improve AI for triggers. If source is a creature with:
        // When ETB, sacrifice a creature. Check to see if the AI has something
        // to sacrifice

        // Eventually, we can call the trigger of ETB abilities with
        // not mandatory as part of the checks to cast something

        final Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null) {
            chance &= subAb.chkAIDrawback();
        }

        return chance || mandatory;
    }

    /**
     * <p>
     * animateTgtAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean animateTgtAI(final AbilityFactory af, final SpellAbility sa) {
        // This is reasonable for now. Kamahl, Fist of Krosa and a sorcery or
        // two are the only things
        // that animate a target. Those can just use SVar:RemAIDeck:True until
        // this can do a reasonably
        // good job of picking a good target
        return false;
    }

    /**
     * <p>
     * animateResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void animateResolve(final AbilityFactory af, final SpellAbility sa) {
        final HashMap<String, String> params = af.getMapParams();
        final Card source = sa.getSourceCard();
        final Card host = af.getHostCard();
        final Map<String, String> svars = host.getSVars();
        long timest = -1;

        // AF specific params
        int power = -1;
        if (params.containsKey("Power")) {
            power = AbilityFactory.calculateAmount(host, params.get("Power"), sa);
        }
        int toughness = -1;
        if (params.containsKey("Toughness")) {
            toughness = AbilityFactory.calculateAmount(host, params.get("Toughness"), sa);
        }

        // Every Animate event needs a unique time stamp
        timest = AllZone.getNextTimestamp();

        final long timestamp = timest;

        final boolean permanent = params.containsKey("Permanent");

        final ArrayList<String> types = new ArrayList<String>();
        if (params.containsKey("Types")) {
            types.addAll(Arrays.asList(params.get("Types").split(",")));
        }

        final ArrayList<String> removeTypes = new ArrayList<String>();
        if (params.containsKey("RemoveTypes")) {
            removeTypes.addAll(Arrays.asList(params.get("RemoveTypes").split(",")));
        }

        // allow ChosenType - overrides anything else specified
        if (types.contains("ChosenType")) {
            types.clear();
            types.add(host.getChosenType());
        }

        final ArrayList<String> keywords = new ArrayList<String>();
        if (params.containsKey("Keywords")) {
            keywords.addAll(Arrays.asList(params.get("Keywords").split(" & ")));
        }

        final ArrayList<String> hiddenKeywords = new ArrayList<String>();
        if (params.containsKey("HiddenKeywords")) {
            hiddenKeywords.addAll(Arrays.asList(params.get("HiddenKeywords").split(" & ")));
        }
        // allow SVar substitution for keywords
        for (int i = 0; i < keywords.size(); i++) {
            final String k = keywords.get(i);
            if (svars.containsKey(k)) {
                keywords.add(svars.get(k));
                keywords.remove(k);
            }
        }

        // colors to be added or changed to
        String tmpDesc = "";
        if (params.containsKey("Colors")) {
            final String colors = params.get("Colors");
            if (colors.equals("ChosenColor")) {

                tmpDesc = CardUtil.getShortColorsString(host.getChosenColor());
            } else {
                tmpDesc = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(colors.split(","))));
            }
        }
        final String finalDesc = tmpDesc;

        // abilities to add to the animated being
        final ArrayList<String> abilities = new ArrayList<String>();
        if (params.containsKey("Abilities")) {
            abilities.addAll(Arrays.asList(params.get("Abilities").split(",")));
        }

        // triggers to add to the animated being
        final ArrayList<String> triggers = new ArrayList<String>();
        if (params.containsKey("Triggers")) {
            triggers.addAll(Arrays.asList(params.get("Triggers").split(",")));
        }

        // static abilities to add to the animated being
        final ArrayList<String> stAbs = new ArrayList<String>();
        if (params.containsKey("staticAbilities")) {
            stAbs.addAll(Arrays.asList(params.get("staticAbilities").split(",")));
        }

        // sVars to add to the animated being
        final ArrayList<String> sVars = new ArrayList<String>();
        if (params.containsKey("sVars")) {
            sVars.addAll(Arrays.asList(params.get("sVars").split(",")));
        }

        final Target tgt = af.getAbTgt();
        ArrayList<Card> tgts;
        if (tgt != null) {
            tgts = tgt.getTargetCards();
        } else {
            tgts = AbilityFactory.getDefinedCards(source, params.get("Defined"), sa);
        }

        for (final Card c : tgts) {

            final long colorTimestamp = AbilityFactory_Animate.doAnimate(c, af, power, toughness, types, removeTypes,
                    finalDesc, keywords, hiddenKeywords, timestamp);

            // give abilities
            final ArrayList<SpellAbility> addedAbilities = new ArrayList<SpellAbility>();
            if (abilities.size() > 0) {
                for (final String s : abilities) {
                    final AbilityFactory newAF = new AbilityFactory();
                    final String actualAbility = host.getSVar(s);
                    final SpellAbility grantedAbility = newAF.getAbility(actualAbility, c);
                    addedAbilities.add(grantedAbility);
                    c.addSpellAbility(grantedAbility);
                }
            }

            // remove abilities
            final ArrayList<SpellAbility> removedAbilities = new ArrayList<SpellAbility>();
            if (params.containsKey("OverwriteAbilities") || params.containsKey("RemoveAllAbilities")) {
                for (final SpellAbility ab : c.getSpellAbilities()) {
                    if (ab.isAbility()) {
                        c.removeSpellAbility(ab);
                        removedAbilities.add(ab);
                    }
                }
            }

            // Grant triggers
            final ArrayList<Trigger> addedTriggers = new ArrayList<Trigger>();
            if (triggers.size() > 0) {
                for (final String s : triggers) {
                    final String actualTrigger = host.getSVar(s);
                    final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, c, false);
                    addedTriggers.add(c.addTrigger(parsedTrigger));
                }
            }

            // suppress triggers from the animated card
            final ArrayList<Trigger> removedTriggers = new ArrayList<Trigger>();
            if (params.containsKey("OverwriteTriggers") || params.containsKey("RemoveAllAbilities")) {
                final ArrayList<Trigger> triggersToRemove = c.getTriggers();
                for (final Trigger trigger : triggersToRemove) {
                    trigger.setSuppressed(true);
                    removedTriggers.add(trigger);
                }
            }

            // give static abilities (should only be used by cards to give
            // itself a static ability)
            if (stAbs.size() > 0) {
                for (final String s : stAbs) {
                    final String actualAbility = host.getSVar(s);
                    c.addStaticAbility(actualAbility);
                }
            }

            // give sVars
            if (sVars.size() > 0) {
                for (final String s : sVars) {
                    final String actualsVar = host.getSVar(s);
                    c.setSVar(s, actualsVar);
                }
            }

            final boolean givesStAbs = (stAbs.size() > 0);

            final Command unanimate = new Command() {
                private static final long serialVersionUID = -5861759814760561373L;

                @Override
                public void execute() {
                    AbilityFactory_Animate.doUnanimate(c, af, finalDesc, hiddenKeywords, addedAbilities, addedTriggers,
                            colorTimestamp, givesStAbs, removedAbilities, timestamp);

                    // give back suppressed triggers
                    for (final Trigger t : removedTriggers) {
                        t.setSuppressed(false);
                    }
                }
            };

            if (!permanent) {
                if (params.containsKey("UntilEndOfCombat")) {
                    AllZone.getEndOfCombat().addUntil(unanimate);
                } else if (params.containsKey("UntilHostLeavesPlay")) {
                    host.addLeavesPlayCommand(unanimate);
                } else if (params.containsKey("UntilYourNextUpkeep")) {
                    AllZone.getUpkeep().addUntil(host.getController(), unanimate);
                } else {
                    AllZone.getEndOfTurn().addUntil(unanimate);
                }
            }
        }
    } // animateResolve

    /**
     * <p>
     * doAnimate.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param power
     *            a int.
     * @param toughness
     *            a int.
     * @param types
     *            a {@link java.util.ArrayList} object.
     * @param colors
     *            a {@link java.lang.String} object.
     * @param keywords
     *            a {@link java.util.ArrayList} object.
     * @return a long.
     */
    private static long doAnimate(final Card c, final AbilityFactory af, final int power, final int toughness,
            final ArrayList<String> types, final ArrayList<String> removeTypes, final String colors,
            final ArrayList<String> keywords, final ArrayList<String> hiddenKeywords, final long timestamp) {
        final HashMap<String, String> params = af.getMapParams();

        boolean removeSuperTypes = false;
        boolean removeCardTypes = false;
        boolean removeSubTypes = false;
        boolean removeCreatureTypes = false;

        if (params.containsKey("OverwriteTypes")) {
            removeSuperTypes = true;
            removeCardTypes = true;
            removeSubTypes = true;
            removeCreatureTypes = true;
        }

        if (params.containsKey("KeepSupertypes")) {
            removeSuperTypes = false;
        }

        if (params.containsKey("KeepCardTypes")) {
            removeCardTypes = false;
        }

        if (params.containsKey("RemoveSuperTypes")) {
            removeSuperTypes = true;
        }

        if (params.containsKey("RemoveCardTypes")) {
            removeCardTypes = true;
        }

        if (params.containsKey("RemoveSubTypes")) {
            removeSubTypes = true;
        }

        if (params.containsKey("RemoveCreatureTypes")) {
            removeCreatureTypes = true;
        }

        if ((power != -1) || (toughness != -1)) {
            c.addNewPT(power, toughness, timestamp);
        }

        if (!types.isEmpty() || !removeTypes.isEmpty() || removeCreatureTypes) {
            c.addChangedCardTypes(types, removeTypes, removeSuperTypes, removeCardTypes, removeSubTypes,
                    removeCreatureTypes, timestamp);
        }

        c.addChangedCardKeywords(keywords, null, params.containsKey("RemoveAllAbilities"), timestamp);

        for (final String k : hiddenKeywords) {
            c.addExtrinsicKeyword(k);
        }

        final long colorTimestamp = c.addColor(colors, c, !params.containsKey("OverwriteColors"), true);
        return colorTimestamp;
    }

    /**
     * <p>
     * doUnanimate.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param originalPower
     *            a int.
     * @param originalToughness
     *            a int.
     * @param originalTypes
     *            a {@link java.util.ArrayList} object.
     * @param colorDesc
     *            a {@link java.lang.String} object.
     * @param originalKeywords
     *            a {@link java.util.ArrayList} object.
     * @param addedAbilities
     *            a {@link java.util.ArrayList} object.
     * @param addedTriggers
     *            a {@link java.util.ArrayList} object.
     * @param timestamp
     *            a long.
     */
    private static void doUnanimate(final Card c, final AbilityFactory af, final String colorDesc,
            final ArrayList<String> addedKeywords, final ArrayList<SpellAbility> addedAbilities,
            final ArrayList<Trigger> addedTriggers, final long colorTimestamp, final boolean givesStAbs,
            final ArrayList<SpellAbility> removedAbilities, final long timestamp) {
        final HashMap<String, String> params = af.getMapParams();

        c.removeNewPT(timestamp);

        c.removeChangedCardKeywords(timestamp);

        // remove all static abilities
        if (givesStAbs) {
            c.setStaticAbilities(new ArrayList<StaticAbility>());
        }

        if (params.containsKey("Types") || params.containsKey("RemoveTypes")
                || params.containsKey("RemoveCreatureTypes")) {
            c.removeChangedCardTypes(timestamp);
        }

        c.removeColor(colorDesc, c, !params.containsKey("OverwriteColors"), colorTimestamp);

        for (final String k : addedKeywords) {
            c.removeExtrinsicKeyword(k);
        }

        for (final SpellAbility sa : addedAbilities) {
            c.removeSpellAbility(sa);
        }

        for (final SpellAbility sa : removedAbilities) {
            c.addSpellAbility(sa);
        }

        for (final Trigger t : addedTriggers) {
            c.removeTrigger(t);
        }

        // any other unanimate cleanup
        if (!c.isCreature()) {
            c.unEquipAllCards();
        }
    }

    // **************************************************************
    // ************************ AnimateAll *************************
    // **************************************************************

    /**
     * <p>
     * createAbilityAnimateAll.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityAnimateAll(final AbilityFactory af) {
        final SpellAbility abAnimateAll = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -4969632476557290609L;

            @Override
            public boolean canPlayAI() {
                return AbilityFactory_Animate.animateAllCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                AbilityFactory_Animate.animateAllResolve(af, this);
            }

            @Override
            public String getStackDescription() {
                return AbilityFactory_Animate.animateAllStackDescription(af, this);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactory_Animate.animateAllTriggerAI(af, this, mandatory);
            }
        };
        return abAnimateAll;
    }

    /**
     * <p>
     * createSpellAnimateAll.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellAnimateAll(final AbilityFactory af) {
        final SpellAbility spAnimateAll = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 2946847609068706237L;

            @Override
            public boolean canPlayAI() {
                return AbilityFactory_Animate.animateAllCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                AbilityFactory_Animate.animateAllResolve(af, this);
            }

            @Override
            public String getStackDescription() {
                return AbilityFactory_Animate.animateAllStackDescription(af, this);
            }
        };
        return spAnimateAll;
    }

    /**
     * <p>
     * createDrawbackAnimateAll.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackAnimateAll(final AbilityFactory af) {
        final SpellAbility dbAnimateAll = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = 2056843302051205632L;

            @Override
            public void resolve() {
                AbilityFactory_Animate.animateAllResolve(af, this);
            }

            @Override
            public boolean chkAIDrawback() {
                return AbilityFactory_Animate.animateAllPlayDrawbackAI(af, this);
            }

            @Override
            public String getStackDescription() {
                return AbilityFactory_Animate.animateAllStackDescription(af, this);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactory_Animate.animateAllTriggerAI(af, this, mandatory);
            }
        };
        return dbAnimateAll;
    }

    /**
     * <p>
     * animateAllStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String animateAllStackDescription(final AbilityFactory af, final SpellAbility sa) {
        final HashMap<String, String> params = af.getMapParams();

        final StringBuilder sb = new StringBuilder();

        if (sa instanceof Ability_Sub) {
            sb.append(" ");
        } else {
            sb.append(sa.getSourceCard()).append(" - ");
        }

        String desc = "";
        if (params.containsKey("SpellDescription")) {
            desc = params.get("SpellDescription");
        } else {
            desc = "Animate all valid cards.";
        }

        sb.append(desc);

        final Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>
     * animateAllCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean animateAllCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        boolean useAbility = false;

        final Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null) {
            useAbility &= subAb.chkAIDrawback();
        }

        return useAbility;
    } // end animateAllCanPlayAI()

    /**
     * <p>
     * animateAllPlayDrawbackAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean animateAllPlayDrawbackAI(final AbilityFactory af, final SpellAbility sa) {
        boolean chance = false;

        final Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null) {
            chance &= subAb.chkAIDrawback();
        }

        return chance;
    }

    /**
     * <p>
     * animateAllTriggerAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @return a boolean.
     */
    private static boolean animateAllTriggerAI(final AbilityFactory af, final SpellAbility sa, final boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa)) { // If there is a cost payment
            return false;
        }

        boolean chance = false;

        final Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null) {
            chance &= subAb.chkAIDrawback();
        }

        return chance || mandatory;
    }

    /**
     * <p>
     * animateAllResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void animateAllResolve(final AbilityFactory af, final SpellAbility sa) {
        final HashMap<String, String> params = af.getMapParams();
        final Card host = af.getHostCard();
        final Map<String, String> svars = host.getSVars();
        long timest = -1;

        // AF specific params
        int power = -1;
        if (params.containsKey("Power")) {
            power = AbilityFactory.calculateAmount(host, params.get("Power"), sa);
        }
        int toughness = -1;
        if (params.containsKey("Toughness")) {
            toughness = AbilityFactory.calculateAmount(host, params.get("Toughness"), sa);
        }

        // Every Animate event needs a unique time stamp
        timest = AllZone.getNextTimestamp();

        final long timestamp = timest;

        final boolean permanent = params.containsKey("Permanent");

        final ArrayList<String> types = new ArrayList<String>();
        if (params.containsKey("Types")) {
            types.addAll(Arrays.asList(params.get("Types").split(",")));
        }

        final ArrayList<String> removeTypes = new ArrayList<String>();
        if (params.containsKey("RemoveTypes")) {
            removeTypes.addAll(Arrays.asList(params.get("RemoveTypes").split(",")));
        }

        // allow ChosenType - overrides anything else specified
        if (types.contains("ChosenType")) {
            types.clear();
            types.add(host.getChosenType());
        }

        final ArrayList<String> keywords = new ArrayList<String>();
        if (params.containsKey("Keywords")) {
            keywords.addAll(Arrays.asList(params.get("Keywords").split(" & ")));
        }

        final ArrayList<String> hiddenKeywords = new ArrayList<String>();
        if (params.containsKey("HiddenKeywords")) {
            hiddenKeywords.addAll(Arrays.asList(params.get("HiddenKeywords").split(" & ")));
        }
        // allow SVar substitution for keywords
        for (int i = 0; i < keywords.size(); i++) {
            final String k = keywords.get(i);
            if (svars.containsKey(k)) {
                keywords.add(svars.get(k));
                keywords.remove(k);
            }
        }

        // colors to be added or changed to
        String tmpDesc = "";
        if (params.containsKey("Colors")) {
            final String colors = params.get("Colors");
            if (colors.equals("ChosenColor")) {
                tmpDesc = CardUtil.getShortColorsString(host.getChosenColor());
            } else {
                tmpDesc = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(colors.split(","))));
            }
        }
        final String finalDesc = tmpDesc;

        // abilities to add to the animated being
        final ArrayList<String> abilities = new ArrayList<String>();
        if (params.containsKey("Abilities")) {
            abilities.addAll(Arrays.asList(params.get("Abilities").split(",")));
        }

        // triggers to add to the animated being
        final ArrayList<String> triggers = new ArrayList<String>();
        if (params.containsKey("Triggers")) {
            triggers.addAll(Arrays.asList(params.get("Triggers").split(",")));
        }

        // sVars to add to the animated being
        final ArrayList<String> sVars = new ArrayList<String>();
        if (params.containsKey("sVars")) {
            sVars.addAll(Arrays.asList(params.get("sVars").split(",")));
        }

        String valid = "";

        if (params.containsKey("ValidCards")) {
            valid = params.get("ValidCards");
        }

        CardList list;
        ArrayList<Player> tgtPlayers = null;

        final Target tgt = af.getAbTgt();
        if (tgt != null) {
            tgtPlayers = tgt.getTargetPlayers();
        } else if (params.containsKey("Defined")) {
            // use it
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
        }

        if ((tgtPlayers == null) || tgtPlayers.isEmpty()) {
            list = AllZoneUtil.getCardsIn(Zone.Battlefield);
        } else {
            list = tgtPlayers.get(0).getCardsIn(Zone.Battlefield);
        }

        list = list.getValidCards(valid.split(","), host.getController(), host);

        for (final Card c : list) {

            final long colorTimestamp = AbilityFactory_Animate.doAnimate(c, af, power, toughness, types, removeTypes,
                    finalDesc, keywords, hiddenKeywords, timestamp);

            // give abilities
            final ArrayList<SpellAbility> addedAbilities = new ArrayList<SpellAbility>();
            if (abilities.size() > 0) {
                for (final String s : abilities) {
                    final AbilityFactory newAF = new AbilityFactory();
                    final String actualAbility = host.getSVar(s);
                    final SpellAbility grantedAbility = newAF.getAbility(actualAbility, c);
                    addedAbilities.add(grantedAbility);
                    c.addSpellAbility(grantedAbility);
                }
            }

            // remove abilities
            final ArrayList<SpellAbility> removedAbilities = new ArrayList<SpellAbility>();
            if (params.containsKey("OverwriteAbilities")) {
                for (final SpellAbility ab : c.getSpellAbilities()) {
                    if (ab.isAbility()) {
                        c.removeSpellAbility(ab);
                        removedAbilities.add(ab);
                    }
                }
            }

            // Grant triggers
            final ArrayList<Trigger> addedTriggers = new ArrayList<Trigger>();
            if (triggers.size() > 0) {
                for (final String s : triggers) {
                    final String actualTrigger = host.getSVar(s);
                    final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, c, false);
                    addedTriggers.add(c.addTrigger(parsedTrigger));
                }
            }

            // give sVars
            if (sVars.size() > 0) {
                for (final String s : sVars) {
                    final String actualsVar = host.getSVar(s);
                    c.setSVar(s, actualsVar);
                }
            }

            final Command unanimate = new Command() {
                private static final long serialVersionUID = -5861759814760561373L;

                @Override
                public void execute() {
                    AbilityFactory_Animate.doUnanimate(c, af, finalDesc, hiddenKeywords, addedAbilities, addedTriggers,
                            colorTimestamp, false, removedAbilities, timestamp);
                }
            };

            if (!permanent) {
                if (params.containsKey("UntilEndOfCombat")) {
                    AllZone.getEndOfCombat().addUntil(unanimate);
                } else {
                    AllZone.getEndOfTurn().addUntil(unanimate);
                }
            }
        }
    } // animateAllResolve

} // end class AbilityFactory_Animate
