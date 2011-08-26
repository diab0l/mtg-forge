package forge.card.abilityFactory;

import forge.*;
import forge.card.cardFactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.cost.CostUtil;
import forge.card.spellability.*;
import forge.gui.GuiUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JOptionPane;

/**
 * <p>AbilityFactory_Protection class.</p>
 *
 * @author dennis.r.friedrichsen (slapshot5 on slightlymagic.net)
 * @version $Id$
 */
public class AbilityFactory_Protection {

    /**
     * <p>getSpellProtection.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellProtection(final AbilityFactory af) {
        SpellAbility spProtect = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
			private static final long serialVersionUID = 4678736312735724916L;

			@Override
            public boolean canPlayAI() {
                return protectCanPlayAI(af, this);
            }

            @Override
            public String getStackDescription() {
                return protectStackDescription(af, this);
            }

            @Override
            public void resolve() {
                protectResolve(af, this);
            }//resolve
        };//SpellAbility

        return spProtect;
    }

    /**
     * <p>getAbilityProtection.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityProtection(final AbilityFactory af) {
        final SpellAbility abProtect = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
			private static final long serialVersionUID = -5295298887428747473L;

			@Override
            public boolean canPlayAI() {
                return protectCanPlayAI(af, this);
            }

            @Override
            public String getStackDescription() {
                return protectStackDescription(af, this);
            }

            @Override
            public void resolve() {
                protectResolve(af, this);
            }//resolve()

            @Override
            public boolean doTrigger(boolean mandatory) {
                return protectTriggerAI(af, this, mandatory);
            }


        };//SpellAbility

        return abProtect;
    }

    /**
     * <p>getDrawbackProtection.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackProtection(final AbilityFactory af) {
        SpellAbility dbProtect = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
			private static final long serialVersionUID = 8342800124705819366L;

			@Override
            public boolean canPlayAI() {
                return protectCanPlayAI(af, this);
            }

            @Override
            public String getStackDescription() {
                return protectStackDescription(af, this);
            }

            @Override
            public void resolve() {
                protectResolve(af, this);
            }//resolve

            @Override
            public boolean chkAI_Drawback() {
                return protectDrawbackAI(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return protectTriggerAI(af, this, mandatory);
            }
        };//SpellAbility

        return dbProtect;
    }
    
    private static boolean hasProtectionFrom(Card card, String color) {
    	ArrayList<String> onlyColors = new ArrayList<String>(Arrays.asList(Constant.Color.onlyColors));
    	
    	//make sure we have a valid color
    	if(!onlyColors.contains(color)) return false;
    	
    	String protection = "Protection from " + color;
    	if(card.hasKeyword(protection)) return true;
    	else return false;
    }
    
    private static boolean hasProtectionFromAny(Card card, ArrayList<String> colors) {
    	boolean protect = false;
    	for(String color : colors) {
    		protect |= hasProtectionFrom(card, color);
    	}
    	return protect;
    }
    
    private static boolean hasProtectionFromAll(Card card, ArrayList<String> colors) {
    	boolean protect = true;
    	if(colors.size() < 1) return false;
    	
    	for(String color : colors) {
    		protect &= hasProtectionFrom(card, color);
    	}
    	return protect;
    }

    /**
     * <p>getProtectCreatures.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.CardList} object.
     */
    private static CardList getProtectCreatures(AbilityFactory af, SpellAbility sa) {
    	final Card hostCard = af.getHostCard();
    	final ArrayList<String> gains = getProtectionList(hostCard, af.getMapParams());

        CardList list = AllZoneUtil.getCreaturesInPlay(AllZone.getComputerPlayer());
        list = list.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                if(!CardFactoryUtil.canTarget(hostCard, c))
                    return false;

                //Don't add duplicate protections
                if(hasProtectionFromAll(c, gains)) return false;

                //will the creature attack (only relevant for sorcery speed)?
                if(CardFactoryUtil.AI_doesCreatureAttack(c) && AllZone.getPhase().isBefore(Constant.Phase.Combat_Declare_Attackers)
                        && AllZone.getPhase().isPlayerTurn(AllZone.getComputerPlayer()))
                    return true;

                //is the creature blocking and unable to destroy the attacker or would be destroyed itself?
                if(c.isBlocking() && (CombatUtil.blockerWouldBeDestroyed(c)
                        || CombatUtil.attackerWouldBeDestroyed(AllZone.getCombat().getAttackerBlockedBy(c))))
                    return true;

                //is the creature in blocked and the blocker would survive
                if(AllZone.getPhase().isAfter(Constant.Phase.Combat_Declare_Blockers) && AllZone.getCombat().isAttacking(c)
                        && AllZone.getCombat().isBlocked(c)
                        && CombatUtil.blockerWouldBeDestroyed(AllZone.getCombat().getBlockers(c).get(0)))
                    return true;

                return false;
            }
        });
        return list;
    }//getProtectCreatures()

    /**
     * <p>protectCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean protectCanPlayAI(AbilityFactory af, SpellAbility sa) {
    	HashMap<String,String> params = af.getMapParams();
    	Card hostCard = af.getHostCard();
        // if there is no target and host card isn't in play, don't activate
        if(af.getAbTgt() == null && !AllZoneUtil.isCardInPlay(hostCard))
            return false;

        Cost cost = sa.getPayCosts();
        
        // temporarily disabled until better AI
        if (!CostUtil.checkLifeCost(cost, hostCard, 4))
            return false;

        if (!CostUtil.checkDiscardCost(cost, hostCard))
            return false;
            
        if (!CostUtil.checkCreatureSacrificeCost(cost, hostCard))
            return false;
            
        if (!CostUtil.checkRemoveCounterCost(cost, hostCard))
            return false;

        // Phase Restrictions
        if(AllZone.getStack().size() == 0 && AllZone.getPhase().isBefore(Constant.Phase.Combat_FirstStrikeDamage)) {
        	// Instant-speed protections should not be cast outside of combat when the stack is empty
        	if(!AbilityFactory.isSorcerySpeed(sa))
        		return false;
        }
        else if(AllZone.getStack().size() > 0) {
        	// TODO: protection something only if the top thing on the stack will kill it via damage or destroy
        	return false;
        }

        if(af.getAbTgt() == null || !af.getAbTgt().doesTarget()) {
        	ArrayList<Card> cards = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa);

        	if(cards.size() == 0)
        		return false;

        	/*
            // when this happens we need to expand AI to consider if its ok for everything?
            for (Card card : cards) {
                // TODO: if AI doesn't control Card and Pump is a Curse, than maybe use?

            }*/
        }
        else
        	return protectTgtAI(af, sa, false);

        return false;
    }//protectPlayAI()

    /**
     * <p>protectTgtAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    private static boolean protectTgtAI(AbilityFactory af, SpellAbility sa, boolean mandatory) {
        if (!mandatory && AllZone.getPhase().isAfter(Constant.Phase.Combat_Declare_Blockers_InstantAbility))
            return false;
        
        Card source = sa.getSourceCard();

        Target tgt = af.getAbTgt();
        tgt.resetTargets();
        CardList list = getProtectCreatures(af, sa);

        list = list.getValidCards(tgt.getValidTgts(), sa.getActivatingPlayer(), sa.getSourceCard());
        
        /*
         * TODO - What this should probably do is if it's time for instants and abilities after Human
         * declares attackers, determine desired protection before assigning blockers.
         * 
         * The other time we want protection is if I'm targeted by a damage or destroy spell on the stack
         * 
         * Or, add protection (to make it unblockable) when Compy is attacking.
         */

        if(AllZone.getStack().size() == 0) {
            // If the cost is tapping, don't activate before declare attack/block
            if(sa.getPayCosts() != null && sa.getPayCosts().getTap()) {
                if(AllZone.getPhase().isBefore(Constant.Phase.Combat_Declare_Attackers) && AllZone.getPhase().isPlayerTurn(AllZone.getComputerPlayer()))
                    list.remove(sa.getSourceCard());
                if(AllZone.getPhase().isBefore(Constant.Phase.Combat_Declare_Blockers) && AllZone.getPhase().isPlayerTurn(AllZone.getHumanPlayer()))
                    list.remove(sa.getSourceCard());
            }
        }

        if(list.isEmpty())
            return mandatory && protectMandatoryTarget(af, sa, mandatory);

        while(tgt.getNumTargeted() < tgt.getMaxTargets(source, sa)) {
            Card t = null;
            //boolean goodt = false;

            if(list.isEmpty()) {
                if(tgt.getNumTargeted() < tgt.getMinTargets(source, sa) || tgt.getNumTargeted() == 0) {
                    if(mandatory)
                        return protectMandatoryTarget(af, sa, mandatory);

                    tgt.resetTargets();
                    return false;
                }
                else {
                    // TODO is this good enough? for up to amounts?
                    break;
                }
            }

            t = CardFactoryUtil.AI_getBestCreature(list);
            tgt.addTarget(t);
            list.remove(t);
        }

        return true;
    }//protectTgtAI()

    /**
     * <p>protectMandatoryTarget.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    private static boolean protectMandatoryTarget(AbilityFactory af, SpellAbility sa, boolean mandatory) {
    	final HashMap<String,String> params = af.getMapParams();
    	final Card host = af.getHostCard();
    	
        CardList list = AllZoneUtil.getCardsInPlay();
        Target tgt = sa.getTarget();
        list = list.getValidCards(tgt.getValidTgts(), sa.getActivatingPlayer(), sa.getSourceCard());

        if(list.size() < tgt.getMinTargets(sa.getSourceCard(), sa)) {
            tgt.resetTargets();
            return false;
        }

        // Remove anything that's already been targeted
        for(Card c : tgt.getTargetCards())
            list.remove(c);

        CardList pref = list.getController(AllZone.getComputerPlayer());
        pref = pref.filter(new CardListFilter() {
        	public boolean addCard(Card c) {
        		return !hasProtectionFromAll(c, getProtectionList(host, params));
        	}
        });
        CardList pref2 = list.getController(AllZone.getComputerPlayer());
        pref = pref.filter(new CardListFilter() {
        	public boolean addCard(Card c) {
        		return !hasProtectionFromAny(c, getProtectionList(host, params));
        	}
        });
        CardList forced = list.getController(AllZone.getHumanPlayer());
        Card source = sa.getSourceCard();

        while(tgt.getNumTargeted() < tgt.getMaxTargets(source, sa)) {
            if(pref.isEmpty())
                break;

            Card c;
            if(pref.getNotType("Creature").size() == 0)
                c = CardFactoryUtil.AI_getBestCreature(pref);
            else
                c = CardFactoryUtil.AI_getMostExpensivePermanent(pref, source, true);

            pref.remove(c);

            tgt.addTarget(c);
        }
        
        while(tgt.getNumTargeted() < tgt.getMaxTargets(source, sa)) {
            if(pref2.isEmpty())
                break;

            Card c;
            if(pref2.getNotType("Creature").size() == 0)
                c = CardFactoryUtil.AI_getBestCreature(pref2);
            else
                c = CardFactoryUtil.AI_getMostExpensivePermanent(pref2, source, true);

            pref2.remove(c);

            tgt.addTarget(c);
        }

        while(tgt.getNumTargeted() < tgt.getMinTargets(source, sa)) {
            if(forced.isEmpty())
                break;

            Card c;
            if(forced.getNotType("Creature").size() == 0)
                c = CardFactoryUtil.AI_getWorstCreature(forced);
            else
                c = CardFactoryUtil.AI_getCheapestPermanent(forced, source, true);

            forced.remove(c);

            tgt.addTarget(c);
        }

        if(tgt.getNumTargeted() < tgt.getMinTargets(sa.getSourceCard(), sa)) {
            tgt.resetTargets();
            return false;
        }

        return true;
    }//protectMandatoryTarget()

    /**
     * <p>protectTriggerAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    private static boolean protectTriggerAI(AbilityFactory af, SpellAbility sa, boolean mandatory) {
        if(!ComputerUtil.canPayCost(sa))
            return false;

        if(sa.getTarget() == null) {
            if(mandatory)
                return true;
        }
        else {
            return protectTgtAI(af, sa, mandatory);
        }

        return true;
    }//protectTriggerAI

    /**
     * <p>protectDrawbackAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean protectDrawbackAI(AbilityFactory af, SpellAbility sa) {
        Card host = af.getHostCard();

        if(af.getAbTgt() == null || !af.getAbTgt().doesTarget()) {
            if(host.isCreature()) {
            	//TODO
            }
        }
        else
            return protectTgtAI(af, sa, false);

        return true;
    }//protectDrawbackAI()

    /**
     * <p>protectStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String protectStackDescription(AbilityFactory af, SpellAbility sa) {
    	HashMap<String,String> params = af.getMapParams();
    	Card host = af.getHostCard();

    	final ArrayList<String> gains = getProtectionList(host, params);
    	boolean choose = (params.containsKey("Choices")) ? true : false;
    	String joiner = choose ? "or" : "and";

    	StringBuilder sb = new StringBuilder();

    	ArrayList<Card> tgtCards;
    	Target tgt = af.getAbTgt();
    	if(tgt != null)
    		tgtCards = tgt.getTargetCards();
    	else
    		tgtCards = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa);

    	if(tgtCards.size() > 0) {

    		if(sa instanceof Ability_Sub)
    			sb.append(" ");
    		else
    			sb.append(host).append(" - ");

    		Iterator<Card> it = tgtCards.iterator();
            while (it.hasNext()) {
                Card tgtC = it.next();
                if (tgtC.isFaceDown()) sb.append("Morph");
                else sb.append(tgtC);

                if (it.hasNext()) sb.append(", ");
            }

    		sb.append(" gain");
    		if(tgtCards.size() == 1) sb.append("s");
    		sb.append(" protection from ");
    		
    		if(choose) sb.append("your choice of ");

    		for(int i = 0; i < gains.size(); i++) {
    			if (i != 0)
    				sb.append(", ");
    			
    			if (i == gains.size() - 1)
    				sb.append(joiner).append(" ");
    			
    			sb.append(gains.get(i));
    		}

    		if(!params.containsKey("Permanent"))
    			sb.append(" until end of turn");
    		
    		sb.append(".");
    	}

    	Ability_Sub abSub = sa.getSubAbility();
    	if(abSub != null) {
    		sb.append(abSub.getStackDescription());
    	}

    	return sb.toString();
    }//protectStackDescription()

    /**
     * <p>protectResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void protectResolve(AbilityFactory af, SpellAbility sa) {
    	HashMap<String,String> params = af.getMapParams();
    	final Card host = af.getHostCard();
    	
    	boolean isChoice = params.get("Gains").contains("Choice");
    	ArrayList<String> choices = getProtectionList(host, params);
    	final ArrayList<String> gains = new ArrayList<String>();
        if(isChoice) {        	
        	if(sa.getActivatingPlayer().isHuman()) {
        		Object o = GuiUtils.getChoice("Choose a protection", choices.toArray());

                if(null == o) return;
                String choice = (String) o;
                gains.add(choice);
            }
        	else {
        		//TODO - needs improvement
        		String choice = choices.get(0);
                gains.add(choice);
                JOptionPane.showMessageDialog(null, "Computer chooses "+gains, ""+host, JOptionPane.PLAIN_MESSAGE); 
            }
        }
        else
        	gains.addAll(choices);
    	
        ArrayList<Card> tgtCards;
        Target tgt = af.getAbTgt();
        if(tgt != null) {
            tgtCards = tgt.getTargetCards();
        }
        else {
            tgtCards = AbilityFactory.getDefinedCards(host, params.get("Defined"), sa);
        }

        int size = tgtCards.size();
        for(int j = 0; j < size; j++) {
            final Card tgtC = tgtCards.get(j);

            // only pump things in play
            if(!AllZoneUtil.isCardInPlay(tgtC))
                continue;

            // if this is a target, make sure we can still target now
            if(tgt != null && !CardFactoryUtil.canTarget(host, tgtC))
            	continue;

            for(String gain : gains) {
            	tgtC.addExtrinsicKeyword("Protection from "+gain);
            }

            if(!params.containsKey("Permanent")) {
            	// If not Permanent, remove protection at EOT
            	final Command untilEOT = new Command() {
            		private static final long serialVersionUID = 7682700789217703789L;

            		public void execute() {
            			if(AllZoneUtil.isCardInPlay(tgtC)) {
            				for (String gain : gains) {
            					tgtC.removeExtrinsicKeyword("Protection from "+gain);
            				}
            			}
            		}
            	};
                if(params.containsKey("UntilEndOfCombat")) AllZone.getEndOfCombat().addUntil(untilEOT);
                else AllZone.getEndOfTurn().addUntil(untilEOT);
            }
        }
    }//protectResolve()
    
    private static ArrayList<String> getProtectionList(Card host, HashMap<String,String> params) {
    	final ArrayList<String> gains = new ArrayList<String>();
    	
    	String gainStr = params.get("Gains"); 
    	if(gainStr.equals("Choice")) {
    		String choices = params.get("Choices");

    		// Replace AnyColor with the 5 colors
    		if (choices.contains("AnyColor")){
    			gains.addAll(Arrays.asList(Constant.Color.onlyColors));
    			choices = choices.replaceAll("AnyColor,?", "");
    		}
    		// Add any remaining choices
    		if (choices.length() > 0)
    			gains.addAll(Arrays.asList(choices.split(",")));
    	}
    	else {
    		gains.addAll(Arrays.asList(gainStr.split(",")));
    	}
        return gains;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    // *************************************************************************
    // ************************** ProtectionAll ********************************
    // *************************************************************************
    /**
     * <p>getSpellProtectionAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellProtectionAll(final AbilityFactory af) {
        SpellAbility spProtectAll = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
			private static final long serialVersionUID = 7205636088393235571L;

			@Override
            public boolean canPlayAI() {
                return protectAllCanPlayAI(af, this);
            }

            @Override
            public String getStackDescription() {
                return protectAllStackDescription(af, this);
            }

            @Override
            public void resolve() {
                protectAllResolve(af, this);
            }//resolve
        };//SpellAbility

        return spProtectAll;
    }

    /**
     * <p>getAbilityProtectionAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityProtectionAll(final AbilityFactory af) {
        final SpellAbility abProtectAll = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
			private static final long serialVersionUID = -8491026929105907288L;

			@Override
            public boolean canPlayAI() {
                return protectAllCanPlayAI(af, this);
            }

            @Override
            public String getStackDescription() {
                return protectAllStackDescription(af, this);
            }

            @Override
            public void resolve() {
                protectAllResolve(af, this);
            }//resolve()

            @Override
            public boolean doTrigger(boolean mandatory) {
                return protectAllTriggerAI(af, this, mandatory);
            }


        };//SpellAbility

        return abProtectAll;
    }

    /**
     * <p>getDrawbackProtectionAll.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackProtectionAll(final AbilityFactory af) {
        SpellAbility dbProtectAll = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
			private static final long serialVersionUID = 5096939345199247701L;

			@Override
            public boolean canPlayAI() {
                return protectAllCanPlayAI(af, this);
            }

            @Override
            public String getStackDescription() {
                return protectAllStackDescription(af, this);
            }

            @Override
            public void resolve() {
                protectAllResolve(af, this);
            }//resolve

            @Override
            public boolean chkAI_Drawback() {
                return protectAllDrawbackAI(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return protectAllTriggerAI(af, this, mandatory);
            }
        };//SpellAbility

        return dbProtectAll;
    }

    /**
     * <p>protectAllCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean protectAllCanPlayAI(AbilityFactory af, SpellAbility sa) {
    	Card hostCard = af.getHostCard();
        // if there is no target and host card isn't in play, don't activate
        if(af.getAbTgt() == null && !AllZoneUtil.isCardInPlay(hostCard))
            return false;

        Cost cost = sa.getPayCosts();
        
        // temporarily disabled until better AI
        if (!CostUtil.checkLifeCost(cost, hostCard, 4))
            return false;

        if (!CostUtil.checkDiscardCost(cost, hostCard))
            return false;
            
        if (!CostUtil.checkSacrificeCost(cost, hostCard))
            return false;
            
        if (!CostUtil.checkRemoveCounterCost(cost, hostCard))
            return false;

        return false;
    }//protectAllCanPlayAI()

    /**
     * <p>protectAllTriggerAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    private static boolean protectAllTriggerAI(AbilityFactory af, SpellAbility sa, boolean mandatory) {
        if(!ComputerUtil.canPayCost(sa))
            return false;

        return true;
    }//protectAllTriggerAI

    /**
     * <p>protectAllDrawbackAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean protectAllDrawbackAI(AbilityFactory af, SpellAbility sa) {
        return protectAllTriggerAI(af, sa, false);
    }//protectAllDrawbackAI()

    /**
     * <p>protectAllStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String protectAllStackDescription(AbilityFactory af, SpellAbility sa) {
    	HashMap<String,String> params = af.getMapParams();
    	Card host = af.getHostCard();

    	StringBuilder sb = new StringBuilder();

    	ArrayList<Card> tgtCards;
    	Target tgt = af.getAbTgt();
    	if(tgt != null)
    		tgtCards = tgt.getTargetCards();
    	else
    		tgtCards = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa);

    	if(tgtCards.size() > 0) {

    		if(sa instanceof Ability_Sub)
    			sb.append(" ");
    		else
    			sb.append(host).append(" - ");

            if (params.containsKey("SpellDescription")) {
                sb.append(params.get("SpellDescription"));
            } else {
                sb.append("Valid card gain protection");
                if(!params.containsKey("Permanent"))
        			sb.append(" until end of turn");
                sb.append(".");
            }
    	}

    	Ability_Sub abSub = sa.getSubAbility();
    	if(abSub != null) {
    		sb.append(abSub.getStackDescription());
    	}

    	return sb.toString();
    }//protectStackDescription()

    /**
     * <p>protectAllResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void protectAllResolve(AbilityFactory af, SpellAbility sa) {
    	HashMap<String,String> params = af.getMapParams();
    	final Card host = af.getHostCard();
    	
    	boolean isChoice = params.get("Gains").contains("Choice");
    	ArrayList<String> choices = getProtectionList(host, params);
    	final ArrayList<String> gains = new ArrayList<String>();
        if(isChoice) {        	
        	if(sa.getActivatingPlayer().isHuman()) {
        		Object o = GuiUtils.getChoice("Choose a protection", choices.toArray());

                if(null == o) return;
                String choice = (String) o;
                gains.add(choice);
            }
        	else {
        		//TODO - needs improvement
        		String choice = choices.get(0);
                gains.add(choice);
                JOptionPane.showMessageDialog(null, "Computer chooses "+gains, ""+host, JOptionPane.PLAIN_MESSAGE); 
            }
        }
        else
        	gains.addAll(choices);
    	
        String valid = params.get("ValidCards");
        CardList list = AllZoneUtil.getCardsInPlay();
        list = list.getValidCards(valid, sa.getActivatingPlayer(), host);
        

        for(final Card tgtC : list) {
        	if(AllZoneUtil.isCardInPlay(tgtC)) {
        		for(String gain : gains) {
        			tgtC.addExtrinsicKeyword("Protection from "+gain);
        		}

        		if(!params.containsKey("Permanent")) {
        			// If not Permanent, remove protection at EOT
        			final Command untilEOT = new Command() {
						private static final long serialVersionUID = -6573962672873853565L;

						public void execute() {
        					if(AllZoneUtil.isCardInPlay(tgtC)) {
        						for (String gain : gains) {
        							tgtC.removeExtrinsicKeyword("Protection from "+gain);
        						}
        					}
        				}
        			};
        			if(params.containsKey("UntilEndOfCombat")) AllZone.getEndOfCombat().addUntil(untilEOT);
        			else AllZone.getEndOfTurn().addUntil(untilEOT);
        		}
        	}
        }
    }//protectAllResolve()
    
}//end class AbilityFactory_Protection
