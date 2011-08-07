package forge.card.abilityFactory;

import forge.*;
import forge.card.spellability.*;
import forge.card.staticAbility.StaticAbility;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>AbilityFactory_Animate class.</p>
 *
 * @author Forge
 * @version $Id: $
 */
public class AbilityFactory_Animate {

    //**************************************************************
    //************************** Animate ***************************
    //**************************************************************

    /**
     * <p>createAbilityAnimate.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityAnimate(final AbilityFactory af) {
        final SpellAbility abAnimate = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 1938171749867735155L;

            @Override
            public boolean canPlayAI() {
                return animateCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                animateResolve(af, this);
            }

            @Override
            public String getStackDescription() {
                return animateStackDescription(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return animateTriggerAI(af, this, mandatory);
            }
        };
        return abAnimate;
    }

    /**
     * <p>createSpellAnimate.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellAnimate(final AbilityFactory af) {
        final SpellAbility spAnimate = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -4047747186919390147L;

            @Override
            public boolean canPlayAI() {
                return animateCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                animateResolve(af, this);
            }

            @Override
            public String getStackDescription() {
                return animateStackDescription(af, this);
            }
        };
        return spAnimate;
    }

    /**
     * <p>createDrawbackAnimate.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackAnimate(final AbilityFactory af) {
        final SpellAbility dbAnimate = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = -8659938411460952874L;

            @Override
            public void resolve() {
                animateResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return animatePlayDrawbackAI(af, this);
            }

            @Override
            public String getStackDescription() {
                return animateStackDescription(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return animateTriggerAI(af, this, mandatory);
            }
        };
        return dbAnimate;
    }

    /**
     * <p>animateStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String animateStackDescription(final AbilityFactory af, SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        Card host = af.getHostCard();
        Map<String, String> svars = host.getSVars();

        int power = -1;
        if (params.containsKey("Power")) power = AbilityFactory.calculateAmount(host, params.get("Power"), sa);
        int toughness = -1;
        if (params.containsKey("Toughness"))
            toughness = AbilityFactory.calculateAmount(host, params.get("Toughness"), sa);

        boolean permanent = params.containsKey("Permanent") ? true : false;
        final ArrayList<String> types = new ArrayList<String>();
        if (params.containsKey("Types")) types.addAll(Arrays.asList(params.get("Types").split(",")));
        final ArrayList<String> keywords = new ArrayList<String>();
        if (params.containsKey("Keywords")) keywords.addAll(Arrays.asList(params.get("Keywords").split(" & ")));
        //allow SVar substitution for keywords
        for (int i = 0; i < keywords.size(); i++) {
            String k = keywords.get(i);
            if (svars.containsKey(k)) {
                keywords.add("\"" + k + "\"");
                keywords.remove(k);
            }
        }
        ArrayList<String> colors = new ArrayList<String>();
        if (params.containsKey("Colors")) colors.addAll(Arrays.asList(params.get("Colors").split(",")));

        StringBuilder sb = new StringBuilder();

        if (sa instanceof Ability_Sub)
            sb.append(" ");
        else
            sb.append(sa.getSourceCard().getName()).append(" - ");

        Target tgt = af.getAbTgt();
        ArrayList<Card> tgts;
        if (tgt != null)
            tgts = tgt.getTargetCards();
        else
            tgts = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa);

        for (Card c : tgts) {
            sb.append(c).append(" ");
        }
        sb.append("become");
        if (tgts.size() == 1) sb.append("s a");
        //if power is -1, we'll assume it's not just setting toughness
        if (power != -1) sb.append(" ").append(power).append("/").append(toughness);

        if (colors.size() > 0) sb.append(" ");
        if(colors.contains("ChosenColor")) {
        	sb.append("color of that player's choice");
        }
        else {
        	for (int i = 0; i < colors.size(); i++) {
        		sb.append(colors.get(i));
        		if (i < (colors.size() - 1)) sb.append(" and ");
        	}
        }
        sb.append(" ");
        for (int i = types.size() - 1; i >= 0; i--) {
            sb.append(types.get(i));
            sb.append(" ");
        }
        if (keywords.size() > 0) sb.append("with ");
        for (int i = 0; i < keywords.size(); i++) {
            sb.append(keywords.get(i));
            if (i < (keywords.size() - 1)) sb.append(" and ");
        }
        //sb.append(abilities)
        //sb.append(triggers)
        if (!permanent) {
            if (params.containsKey("UntilEndOfCombat")) sb.append(" until end of combat.");
            else if(params.containsKey("UntilHostLeavesPlay")) sb.append(" until ").append(host).append(" leaves the battlefield.");
            else sb.append(" until end of turn.");
        } else sb.append(".");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null)
            sb.append(abSub.getStackDescription());

        return sb.toString();
    }//end animateStackDescription()

    /**
     * <p>animateCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean animateCanPlayAI(final AbilityFactory af, SpellAbility sa) {

        HashMap<String, String> params = af.getMapParams();
        Target tgt = af.getAbTgt();
        Card source = sa.getSourceCard();

        boolean useAbility = true;

        //TODO - add some kind of check to answer "Am I going to attack with this?"
        //TODO - add some kind of check for during human turn to answer "Can I use this to block something?"

        //don't use instant speed animate abilities outside computers Combat_Begin step
        if (!AllZone.getPhase().is(Constant.Phase.Combat_Begin) && AllZone.getPhase().isPlayerTurn(AllZone.getComputerPlayer())
                && !AbilityFactory.isSorcerySpeed(sa) && !params.containsKey("ActivatingPhases")
                && !params.containsKey("Permanent"))
            return false;
        
        //don't use instant speed animate abilities outside humans Combat_Declare_Attackers_InstantAbility step
        if ((!AllZone.getPhase().is(Constant.Phase.Combat_Declare_Attackers_InstantAbility)
        		|| AllZone.getCombat().getAttackers().length == 0)
        		&& AllZone.getPhase().isPlayerTurn(AllZone.getHumanPlayer()))
            return false;

        //don't activate during main2 unless this effect is permanent
        if (AllZone.getPhase().is(Constant.Phase.Main2) && !params.containsKey("Permanent"))
            return false;

        if (null == tgt) {
            ArrayList<Card> defined = AbilityFactory.getDefinedCards(source, params.get("Defined"), sa);

            boolean bFlag = false;
            for (Card c : defined) {
                bFlag |= (!c.isCreature() && !c.isTapped() && !(c.getTurnInZone() == AllZone.getPhase().getTurn()));
                
                //for creatures that could be improved (like Figure of Destiny)
                int power = -5;
                if (params.containsKey("Power")) power = AbilityFactory.calculateAmount(source, params.get("Power"), sa);
                int toughness = -5;
                if (params.containsKey("Toughness"))
                    toughness = AbilityFactory.calculateAmount(source, params.get("Toughness"), sa);
                if(power + toughness> c.getCurrentPower() + c.getCurrentToughness())
                	bFlag = true;
                
            }

            if (!bFlag)    // All of the defined stuff is animated, not very useful
                return false;
        } else {
            tgt.resetTargets();
            useAbility &= animateTgtAI(af, sa);
        }

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            useAbility &= subAb.chkAI_Drawback();

        return useAbility;
    }// end animateCanPlayAI()

    /**
     * <p>animatePlayDrawbackAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean animatePlayDrawbackAI(final AbilityFactory af, SpellAbility sa) {
        // AI should only activate this during Human's turn
        boolean chance = animateTgtAI(af, sa);

        // TODO: restrict the subAbility a bit

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        return chance;
    }

    /**
     * <p>animateTriggerAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    private static boolean animateTriggerAI(final AbilityFactory af, SpellAbility sa, boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa))    // If there is a cost payment
            return false;

        boolean chance = animateTgtAI(af, sa);

        // Improve AI for triggers. If source is a creature with:
        // When ETB, sacrifice a creature. Check to see if the AI has something to sacrifice

        // Eventually, we can call the trigger of ETB abilities with not mandatory as part of the checks to cast something


        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        return chance || mandatory;
    }

    /**
     * <p>animateTgtAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean animateTgtAI(AbilityFactory af, SpellAbility sa) {
        //This is reasonable for now.  Kamahl, Fist of Krosa and a sorcery or two are the only things
        //that animate a target.  Those can just use SVar:RemAIDeck:True until this can do a reasonably
        //good job of picking a good target
        return false;
    }

    /**
     * <p>animateResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void animateResolve(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        Card source = sa.getSourceCard();
        Card host = af.getHostCard();
        Map<String, String> svars = host.getSVars();
        long timest = -1;

        //AF specific params
        int power = -1;
        if (params.containsKey("Power")) power = AbilityFactory.calculateAmount(host, params.get("Power"), sa);
        int toughness = -1;
        if (params.containsKey("Toughness"))
            toughness = AbilityFactory.calculateAmount(host, params.get("Toughness"), sa);
        
        if (power != -1 || toughness != -1)
        	timest = AllZone.getNextTimestamp();
        
        final long timestamp = timest;

        boolean permanent = params.containsKey("Permanent") ? true : false;

        final ArrayList<String> types = new ArrayList<String>();
        if (params.containsKey("Types")) types.addAll(Arrays.asList(params.get("Types").split(",")));

        //allow ChosenType - overrides anything else specified
        if (types.contains("ChosenType")) {
            types.clear();
            types.add(host.getChosenType());
        }

        final ArrayList<String> keywords = new ArrayList<String>();
        if (params.containsKey("Keywords")) keywords.addAll(Arrays.asList(params.get("Keywords").split(" & ")));
        //allow SVar substitution for keywords
        for (int i = 0; i < keywords.size(); i++) {
            String k = keywords.get(i);
            if (svars.containsKey(k)) {
                keywords.add(svars.get(k));
                keywords.remove(k);
            }
        }

        //colors to be added or changed to
        String tmpDesc = "";
        if (params.containsKey("Colors")) {
            String colors = params.get("Colors");
            if (colors.equals("ChosenColor")) {
                tmpDesc = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(host.getChosenColor().split(","))));
            } else {
                tmpDesc = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(colors.split(","))));
            }
        }
        final String finalDesc = tmpDesc;

        //abilities to add to the animated being
        ArrayList<String> abilities = new ArrayList<String>();
        if (params.containsKey("Abilities")) abilities.addAll(Arrays.asList(params.get("Abilities").split(",")));

        //triggers to add to the animated being
        ArrayList<String> triggers = new ArrayList<String>();
        if (params.containsKey("Triggers")) triggers.addAll(Arrays.asList(params.get("Triggers").split(",")));
        
        //static abilities to add to the animated being
        ArrayList<String> stAbs = new ArrayList<String>();
        if (params.containsKey("staticAbilities")) stAbs.addAll(Arrays.asList(params.get("staticAbilities").split(",")));
        
        //sVars to add to the animated being
        ArrayList<String> sVars = new ArrayList<String>();
        if (params.containsKey("sVars")) sVars.addAll(Arrays.asList(params.get("sVars").split(",")));

        Target tgt = af.getAbTgt();
        ArrayList<Card> tgts;
        if (tgt != null)
            tgts = tgt.getTargetCards();
        else
            tgts = AbilityFactory.getDefinedCards(source, params.get("Defined"), sa);

        for (final Card c : tgts) {

            final long colorTimestamp = doAnimate(c, af, power, toughness, types, finalDesc, keywords, timestamp);

            //give abilities
            final ArrayList<SpellAbility> addedAbilities = new ArrayList<SpellAbility>();
            if (abilities.size() > 0) {
                for (String s : abilities) {
                    AbilityFactory newAF = new AbilityFactory();
                    String actualAbility = host.getSVar(s);
                    SpellAbility grantedAbility = newAF.getAbility(actualAbility, c);
                    addedAbilities.add(grantedAbility);
                    c.addSpellAbility(grantedAbility);
                }
            }

            //Grant triggers
            final ArrayList<Trigger> addedTriggers = new ArrayList<Trigger>();
            if (triggers.size() > 0) {
                for (String s : triggers) {
                    String actualTrigger = host.getSVar(s);
                    Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, c);
                    addedTriggers.add(c.addTrigger(parsedTrigger));
                    AllZone.getTriggerHandler().registerTrigger(parsedTrigger);
                }
            }
            
            //give static abilities (should only be used by cards to give itself a static ability)
            if (stAbs.size() > 0) {
                for (String s : stAbs) {
                	String actualAbility = host.getSVar(s);
                    c.addStaticAbility(actualAbility);
                }
            }
            
            //give sVars
            if (sVars.size() > 0) {
                for (String s : sVars) {
                	String actualsVar = host.getSVar(s);
                    c.setSVar(s, actualsVar);
                }
            }
            
            final boolean givesStAbs = (stAbs.size() > 0);

            final Command unanimate = new Command() {
                private static final long serialVersionUID = -5861759814760561373L;

                public void execute() {
                    doUnanimate(c, af, finalDesc, keywords, addedAbilities, addedTriggers, colorTimestamp, givesStAbs, timestamp);
                }
            };

            if (!permanent) {
                if (params.containsKey("UntilEndOfCombat")) AllZone.getEndOfCombat().addUntil(unanimate);
                else if(params.containsKey("UntilHostLeavesPlay")) {
                	host.addLeavesPlayCommand(unanimate);
                }
                else AllZone.getEndOfTurn().addUntil(unanimate);
            }
        }
    }//animateResolve

    /**
     * <p>doAnimate.</p>
     *
     * @param c a {@link forge.Card} object.
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param power a int.
     * @param toughness a int.
     * @param types a {@link java.util.ArrayList} object.
     * @param colors a {@link java.lang.String} object.
     * @param keywords a {@link java.util.ArrayList} object.
     * @return a long.
     */
    private static long doAnimate(Card c, AbilityFactory af, int power, int toughness, ArrayList<String> types, String colors, 
    		ArrayList<String> keywords, long timestamp) {
        HashMap<String, String> params = af.getMapParams();
        
		boolean removeSuperTypes = false;
	    boolean removeCardTypes = false;
	    boolean removeSubTypes = false;
	    
	    if (params.containsKey("OverwriteTypes")) {
	    	removeSuperTypes = true;
		    removeCardTypes = true;
		    removeSubTypes = true;
	    }
	    
	    if (params.containsKey("KeepSupertypes"))
	    	removeSuperTypes = false;
	    
	    if (params.containsKey("KeepCardTypes"))
		    removeCardTypes = false;
        
        if (power != -1 || toughness != -1)
    		c.addNewPT(power, toughness, timestamp);

        if (!types.isEmpty())
        	c.addChangedCardTypes(types, removeSuperTypes, removeCardTypes, removeSubTypes, removeSubTypes, timestamp);
        	
        for (String k : keywords) {
            if (k.startsWith("HIDDEN"))
                c.addExtrinsicKeyword(k);
                //this maybe should just blindly add since multiple instances of a keyword sometimes have effects
                //practically, this shouldn't matter though, and will display more cleanly
            else if (!c.getIntrinsicKeyword().contains(k) || CardUtil.isStackingKeyword(k))
                c.addIntrinsicKeyword(k);
        }

        long colorTimestamp = c.addColor(colors, c, !params.containsKey("OverwriteColors"), true);
        return colorTimestamp;
    }

    /**
     * <p>doUnanimate.</p>
     *
     * @param c a {@link forge.Card} object.
     * @param originalPower a int.
     * @param originalToughness a int.
     * @param originalTypes a {@link java.util.ArrayList} object.
     * @param colorDesc a {@link java.lang.String} object.
     * @param originalKeywords a {@link java.util.ArrayList} object.
     * @param addedAbilities a {@link java.util.ArrayList} object.
     * @param addedTriggers a {@link java.util.ArrayList} object.
     * @param timestamp a long.
     */
    private static void doUnanimate(Card c, AbilityFactory af, String colorDesc, 
    		ArrayList<String> originalKeywords, ArrayList<SpellAbility> addedAbilities, ArrayList<Trigger> addedTriggers, 
    		long colorTimestamp, boolean givesStAbs, long timestamp) {
    	HashMap<String, String> params = af.getMapParams();
    	
    	c.removeNewPT(timestamp);
        
        //remove all static abilities
        if (givesStAbs)
        	c.setStaticAbilities(new ArrayList<StaticAbility>());
        	
        if (params.containsKey("Types"))
        	c.removeChangedCardTypes(timestamp);

        c.removeColor(colorDesc, c, !params.containsKey("OverwriteColors"), colorTimestamp);

        for (String k : originalKeywords) {
            if (k.startsWith("HIDDEN"))
                c.removeExtrinsicKeyword(k);
            //TODO - may want to look at saving off intrinsic and extrinsic separately and add back that way
            c.removeIntrinsicKeyword(k);
        }

        for (SpellAbility sa : addedAbilities) {
            c.removeSpellAbility(sa);
        }

        for (Trigger t : addedTriggers) {
            AllZone.getTriggerHandler().removeRegisteredTrigger(t);
            c.removeTrigger(t);
        }

        //any other unanimate cleanup
        if (!c.isCreature()) c.unEquipAllCards();
    }

    //**************************************************************
    // ************************ AnimateAll *************************
    //**************************************************************

    /**
     * <p>createAbilityAnimateAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityAnimateAll(final AbilityFactory af) {
        final SpellAbility abAnimateAll = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = -4969632476557290609L;

            @Override
            public boolean canPlayAI() {
                return animateAllCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                animateAllResolve(af, this);
            }

            @Override
            public String getStackDescription() {
                return animateAllStackDescription(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return animateAllTriggerAI(af, this, mandatory);
            }
        };
        return abAnimateAll;
    }

    /**
     * <p>createSpellAnimateAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellAnimateAll(final AbilityFactory af) {
        final SpellAbility spAnimateAll = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 2946847609068706237L;

            @Override
            public boolean canPlayAI() {
                return animateAllCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                animateAllResolve(af, this);
            }

            @Override
            public String getStackDescription() {
                return animateAllStackDescription(af, this);
            }
        };
        return spAnimateAll;
    }

    /**
     * <p>createDrawbackAnimateAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackAnimateAll(final AbilityFactory af) {
        final SpellAbility dbAnimateAll = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = 2056843302051205632L;

            @Override
            public void resolve() {
                animateAllResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return animateAllPlayDrawbackAI(af, this);
            }

            @Override
            public String getStackDescription() {
                return animateAllStackDescription(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return animateAllTriggerAI(af, this, mandatory);
            }
        };
        return dbAnimateAll;
    }

    /**
     * <p>animateAllStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String animateAllStackDescription(final AbilityFactory af, SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();

        StringBuilder sb = new StringBuilder();

        if (sa instanceof Ability_Sub)
            sb.append(" ");
        else
            sb.append(sa.getSourceCard()).append(" - ");

        String desc = "";
        if (params.containsKey("SpellDescription")) {
            desc = params.get("SpellDescription");
        } else {
            desc = "Animate all valid cards.";
        }

        sb.append(desc);

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null)
            sb.append(abSub.getStackDescription());

        return sb.toString();
    }

    /**
     * <p>animateAllCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean animateAllCanPlayAI(final AbilityFactory af, SpellAbility sa) {
        boolean useAbility = false;

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            useAbility &= subAb.chkAI_Drawback();

        return useAbility;
    }// end animateAllCanPlayAI()

    /**
     * <p>animateAllPlayDrawbackAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean animateAllPlayDrawbackAI(final AbilityFactory af, SpellAbility sa) {
        boolean chance = false;

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        return chance;
    }

    /**
     * <p>animateAllTriggerAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    private static boolean animateAllTriggerAI(final AbilityFactory af, SpellAbility sa, boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa))    // If there is a cost payment
            return false;

        boolean chance = false;

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        return chance || mandatory;
    }

    /**
     * <p>animateAllResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void animateAllResolve(final AbilityFactory af, final SpellAbility sa) {
        HashMap<String, String> params = af.getMapParams();
        Card host = af.getHostCard();
        Map<String, String> svars = host.getSVars();
        long timest = -1;

        //AF specific params
        int power = -1;
        if (params.containsKey("Power")) power = AbilityFactory.calculateAmount(host, params.get("Power"), sa);
        int toughness = -1;
        if (params.containsKey("Toughness"))
            toughness = AbilityFactory.calculateAmount(host, params.get("Toughness"), sa);
        
        if (power != -1 || toughness != -1)
        	timest = AllZone.getNextTimestamp();
        
        final long timestamp = timest;

        boolean permanent = params.containsKey("Permanent") ? true : false;

        final ArrayList<String> types = new ArrayList<String>();
        if (params.containsKey("Types")) types.addAll(Arrays.asList(params.get("Types").split(",")));

        //allow ChosenType - overrides anything else specified
        if (types.contains("ChosenType")) {
            types.clear();
            types.add(host.getChosenType());
        }

        final ArrayList<String> keywords = new ArrayList<String>();
        if (params.containsKey("Keywords")) keywords.addAll(Arrays.asList(params.get("Keywords").split(" & ")));
        //allow SVar substitution for keywords
        for (int i = 0; i < keywords.size(); i++) {
            String k = keywords.get(i);
            if (svars.containsKey(k)) {
                keywords.add(svars.get(k));
                keywords.remove(k);
            }
        }

        //colors to be added or changed to
        String tmpDesc = "";
        if (params.containsKey("Colors")) {
            String colors = params.get("Colors");
            if (colors.equals("ChosenColor")) {
                tmpDesc = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(host.getChosenColor().split(","))));
            } else {
                tmpDesc = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(colors.split(","))));
            }
        }
        final String finalDesc = tmpDesc;

        //abilities to add to the animated being
        ArrayList<String> abilities = new ArrayList<String>();
        if (params.containsKey("Abilities")) abilities.addAll(Arrays.asList(params.get("Abilities").split(",")));

        //triggers to add to the animated being
        ArrayList<String> triggers = new ArrayList<String>();
        if (params.containsKey("Triggers")) triggers.addAll(Arrays.asList(params.get("Triggers").split(",")));

        String valid = "";

        if (params.containsKey("ValidCards"))
            valid = params.get("ValidCards");

        CardList list = AllZoneUtil.getCardsInPlay();
        list = list.getValidCards(valid.split(","), host.getController(), host);

        for (final Card c : list) {

            final long colorTimestamp = doAnimate(c, af, power, toughness, types, finalDesc, keywords, timestamp);

            //give abilities
            final ArrayList<SpellAbility> addedAbilities = new ArrayList<SpellAbility>();
            if (abilities.size() > 0) {
                for (String s : abilities) {
                    AbilityFactory newAF = new AbilityFactory();
                    String actualAbility = host.getSVar(s);
                    SpellAbility grantedAbility = newAF.getAbility(actualAbility, c);
                    addedAbilities.add(grantedAbility);
                    c.addSpellAbility(grantedAbility);
                }
            }

            //Grant triggers
            final ArrayList<Trigger> addedTriggers = new ArrayList<Trigger>();
            if (triggers.size() > 0) {
                for (String s : triggers) {
                    String actualTrigger = host.getSVar(s);
                    Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, c);
                    addedTriggers.add(c.addTrigger(parsedTrigger));
                    AllZone.getTriggerHandler().registerTrigger(parsedTrigger);
                }
            }

            final Command unanimate = new Command() {
                private static final long serialVersionUID = -5861759814760561373L;

                public void execute() {
                    doUnanimate(c, af, finalDesc, keywords, addedAbilities, addedTriggers, colorTimestamp, false, timestamp);
                }
            };

            if (!permanent) {
                if (params.containsKey("UntilEndOfCombat")) AllZone.getEndOfCombat().addUntil(unanimate);
                else AllZone.getEndOfTurn().addUntil(unanimate);
            }
        }
    }//animateAllResolve

}//end class AbilityFactory_Animate
