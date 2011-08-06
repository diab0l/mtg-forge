package forge;

import java.util.ArrayList;
import java.util.HashMap;

public class TriggerHandler {

	private ArrayList<Trigger> registeredTriggers = new ArrayList<Trigger>();
	private ArrayList<String> suppressedModes = new ArrayList<String>();

    private ArrayList<Trigger> delayedTriggers = new ArrayList<Trigger>();

	public void suppressMode(String mode)
	{
		suppressedModes.add(mode);
	}
	
	public void clearSuppression(String mode)
	{
		suppressedModes.remove(mode);
	}
	
	public static Trigger parseTrigger(String name,String trigParse,Card host)
	{
		Trigger ret = TriggerHandler.parseTrigger(trigParse, host);
		ret.setName(name);
		return ret;
	}
	
	public static Trigger parseTrigger(String trigParse,Card host)
	{
		HashMap<String,String> mapParams = parseParams(trigParse);
		Trigger ret = null;
		
		String mode = mapParams.get("Mode");
		if(mode.equals("AbilityCast"))
		{
			ret = new Trigger_SpellAbilityCast(mapParams,host);
		}
		else if(mode.equals("AttackerBlocked"))
		{
			ret = new Trigger_AttackerBlocked(mapParams,host);
		}
		else if(mode.equals("AttackerUnblocked"))
		{
			ret = new Trigger_AttackerUnblocked(mapParams,host);
		}
		else if(mode.equals("Attacks"))
		{
			ret = new Trigger_Attacks(mapParams,host);
		}
		else if(mode.equals("Blocks"))
		{
			ret = new Trigger_Blocks(mapParams,host);
		}
		else if(mode.equals("ChangesZone"))
		{
			ret = new Trigger_ChangesZone(mapParams,host);
		}
		else if(mode.equals("CounterAdded")) {
			ret = new Trigger_CounterAdded(mapParams,host);
		}
		else if(mode.equals("Cycled"))
		{
			ret = new Trigger_Cycled(mapParams,host);
		}
		else if(mode.equals("DamageDone"))
		{
			ret = new Trigger_DamageDone(mapParams,host);
		}
		else if(mode.equals("Discarded"))
		{
			ret = new Trigger_Discarded(mapParams,host);
		}
		else if(mode.equals("Drawn"))
		{
			ret = new Trigger_Drawn(mapParams,host);
		}
		else if(mode.equals("LandPlayed"))
		{
			ret = new Trigger_LandPlayed(mapParams,host);
		}
		else if(mode.equals("LifeGained"))
		{
			ret = new Trigger_LifeGained(mapParams,host);
		}
		else if(mode.equals("LifeLost"))
		{
			ret = new Trigger_LifeLost(mapParams,host);
		}
		else if(mode.equals("Phase"))
		{
			ret = new Trigger_Phase(mapParams,host);
		}
		else if(mode.equals("Sacrificed"))
		{
			ret = new Trigger_Sacrificed(mapParams,host);
		}
		else if(mode.equals("SpellAbilityCast"))
		{
			ret = new Trigger_SpellAbilityCast(mapParams,host);
		}
		else if(mode.equals("SpellCast"))
		{
			ret = new Trigger_SpellAbilityCast(mapParams,host);
		}
		else if(mode.equals("Taps"))
		{
			ret = new Trigger_Taps(mapParams,host);
		}
		else if(mode.equals("TurnFaceUp"))
		{
			ret = new Trigger_TurnFaceUp(mapParams,host);
		}
		else if(mode.equals("Untaps"))
		{
			ret = new Trigger_Untaps(mapParams,host);
		}
		
		return ret;
	}
	
	private static HashMap<String,String> parseParams(String trigParse)
	{
		HashMap<String,String> mapParams = new HashMap<String,String>();
		
		if(trigParse.length() == 0)
			throw new RuntimeException("TriggerFactory : registerTrigger -- trigParse too short");
		
		String params[] = trigParse.split("\\|");
		
		for(int i=0;i<params.length;i++)
		{
			params[i] = params[i].trim();
		}
		
		for(String param : params)
		{
			String[] splitParam = param.split("\\$");
			for(int i=0;i<splitParam.length;i++)
			{
				splitParam[i] = splitParam[i].trim();
			}
			
			if(splitParam.length != 2)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("TriggerFactory Parsing Error in registerTrigger() : Split length of ");
				sb.append(param).append(" is not 2.");
				throw new RuntimeException(sb.toString());
			}
			
			mapParams.put(splitParam[0], splitParam[1]);
		}
		
		return mapParams;
	}
	
	public void registerTrigger(Trigger trig)
	{
		registeredTriggers.add(trig);
	}
	
	public void clearRegistered()
	{
		registeredTriggers.clear();
	}
	
	public void removeRegisteredTrigger(Trigger trig)
	{
		registeredTriggers.remove(trig);
	}
	
	public void removeAllFromCard(Card crd)
	{
		for(int i=0;i<registeredTriggers.size();i++)
		{
			if(registeredTriggers.get(i).getHostCard().equals(crd))
			{
				registeredTriggers.remove(i);
				i--;
			}
		}
	}
	
	public void runTrigger(String mode,HashMap<String,Object> runParams)
	{
		if(suppressedModes.contains(mode))
		{
			return;
		}
		//AP
		for(Trigger regtrig : registeredTriggers)
		{
			if(regtrig.getHostCard().getController().equals(AllZone.Phase.getPlayerTurn()))
			{
				runSingleTrigger(regtrig,mode,runParams);
			}
		}
        for(int i=0;i<delayedTriggers.size();i++)
        {
            Trigger deltrig = delayedTriggers.get(i);
            if(deltrig.getHostCard().getController().equals(AllZone.Phase.getPlayerTurn()))
            {
                if(runSingleTrigger(deltrig,mode,runParams))
                {
                    delayedTriggers.remove(i);
                    i--;
                }
            }
        }
		
		//NAP
		for(Trigger regtrig : registeredTriggers)
		{
			if(regtrig.getHostCard().getController().equals(AllZone.Phase.getPlayerTurn().getOpponent()))
			{
				runSingleTrigger(regtrig,mode,runParams);
			}
		}
        for(int i=0;i<delayedTriggers.size();i++)
        {
            Trigger deltrig = delayedTriggers.get(i);
            if(deltrig.getHostCard().getController().equals(AllZone.Phase.getPlayerTurn().getOpponent()))
            {
                if(runSingleTrigger(deltrig,mode,runParams))
                {
                    delayedTriggers.remove(i);
                    i--;
                }
            }
        }

	}

    //Checks if the conditions are right for a single trigger to go off, and runs it if so.
    //Return true if the trigger went off, false otherwise.
	private boolean runSingleTrigger(final Trigger regtrig, final String mode, final HashMap<String,Object> runParams)
	{
		if(!regtrig.zonesCheck())
		{
			return false;
		}
        if(!regtrig.phasesCheck())
        {
            return false;
        }
		if(!regtrig.requirementsCheck())
		{
			return false;
		}
		
		if(regtrig.getHostCard().isFaceDown())
		{
			return false;
		}
		
		HashMap<String,String> trigParams = regtrig.getMapParams();
		final Player[] decider = new Player[1];
		final boolean isOptional = false;

		if(mode.equals(trigParams.get("Mode")))
		{
			if(!regtrig.performTest(runParams))
			{
				return false;
			}				
			
			// Any trigger should cause the phase not to skip
			AllZone.Phase.setSkipPhase(false);
			
			regtrig.setRunParams(runParams);
			
			//All tests passed, execute ability.
			AbilityFactory AF = new AbilityFactory();
			
			final SpellAbility[] sa = new SpellAbility[1];
			Card host = AllZoneUtil.getCardState(regtrig.getHostCard());
			
			// This will fix the Oblivion Ring issue, but is this the right fix?
			for(Card c : regtrig.getHostCard().getRemembered())
				host.addRemembered(c);
			
			sa[0] = regtrig.getOverridingAbility();
			if(sa[0] == null)
			{
                if(!trigParams.containsKey("Execute"))
                {
                    sa[0] = new Ability(regtrig.getHostCard(),"0") {
                        @Override
                        public void resolve()
                        {
                        }
                    };
                }
                else
                {
				    sa[0] = AF.getAbility(host.getSVar(trigParams.get("Execute")), host, regtrig.getTriggeringCard(runParams));
                }
			}

			sa[0].setActivatingPlayer(host.getController());
			if(sa[0].getStackDescription().equals(""))
			{
				sa[0].setStackDescription(sa[0].toString());
			}
			
			boolean mand = false;
			if(trigParams.containsKey("Optional"))
			{				
				if(trigParams.get("Optional").equals("True"))
				{
					decider[0] = host.getController();
					mand = false;
					if (sa[0].getTarget() != null)
						sa[0].getTarget().setMandatory(false);
				}
				else if(trigParams.get("Optional").equals("OpponentDecides"))
				{
					mand = false;
					decider[0] = host.getController().getOpponent();
				}				
			}
			else
			{
				mand = true;
				if(sa[0].getTarget() != null)
				{
					sa[0].getTarget().setMandatory(true);
				}
			}
			final boolean isMandatory = mand;

			//Wrapper ability that checks the requirements again just before resolving, for intervening if clauses.
			//Yes, it must wrap ALL SpellAbility methods in order to handle possible corner cases.
			//(The trigger can have a hardcoded OverridingAbility which can make use of any of the methods)
			final Ability wrapperAbility = new Ability(regtrig.getHostCard(),"0") {
				@Override
				public void addDiscardedCost(Card c)
				{
					sa[0].addDiscardedCost(c);
				}
				
				@Override 
				public void addSacrificedCost(Card c)
				{
					sa[0].addSacrificedCost(c);
				}
				
				@Override
				public boolean canPlay()
				{
					return sa[0].canPlay();
				}
				
				@Override
				public boolean canPlayAI()
				{
					return sa[0].canPlayAI();
				}
				
				@Override
				public void chooseTargetAI()
				{
					sa[0].chooseTargetAI();
				}
				
				@Override
				public SpellAbility copy()
				{
					return sa[0].copy();
				}
				
				@Override
			    public boolean doTrigger(boolean mandatory){
					return sa[0].doTrigger(mandatory);
			    }
				
				@Override
				public AbilityFactory getAbilityFactory()
				{
					return sa[0].getAbilityFactory();
				}
				
				@Override
				public Player getActivatingPlayer()
				{
					return sa[0].getActivatingPlayer();
				}
				
				@Override
				public Input getAfterPayMana()
				{
					return sa[0].getAfterPayMana();
				}
			
				@Override
				public Input getAfterResolve()
				{
					return sa[0].getAfterResolve();
				}
				
				@Override
				public Input getBeforePayMana()
				{
					return sa[0].getBeforePayMana();
				}
				
				@Override
				public Command getBeforePayManaAI()
				{
					return sa[0].getBeforePayManaAI();
				}
				
				@Override
				public Command getCancelCommand()
				{
					return sa[0].getCancelCommand();
				}
				
				@Override
				public CommandArgs getChooseTargetAI()
				{
					return sa[0].getChooseTargetAI();
				}
				
				@Override
				public String getDescription()
				{
					return sa[0].getDescription();
				}
				
				@Override
				public CardList getDiscardedCost()
				{
					return sa[0].getDiscardedCost();
				}
				
				@Override
				public String getMultiKickerManaCost()
				{
					return sa[0].getMultiKickerManaCost();
				}
				
				@Override
				public SpellAbility_Restriction getRestrictions()
				{
					return sa[0].getRestrictions();
				}
				
				@Override
				public CardList getSacrificedCost()
				{
					return sa[0].getSacrificedCost();
				}
				
				@Override
				public CardList getExiledCost() {
					return sa[0].getExiledCost();
				}
				
				@Override
				public Card getSourceCard()
				{
					return sa[0].getSourceCard();
				}
				
				@Override
				public String getStackDescription()
				{
					StringBuilder sb = new StringBuilder(regtrig.toString());
					if(getTarget() != null)
					{
						sb.append(" (Targeting ");
						for(Object o : getTarget().getTargets())
						{
							sb.append(o.toString());
							sb.append(", ");
						}
						if (sb.toString().endsWith(", "))
                        {
							sb.setLength(sb.length()-2);
                        }
						else
                        {
                            sb.append("ERROR");
                        }
						sb.append(")");
					}
					
					return sb.toString();
				}
				
				@Override
				public Ability_Sub getSubAbility()
				{
					return sa[0].getSubAbility();
				}
				
				@Override
				public Target getTarget()
				{
					return sa[0].getTarget();
				}
				
				@Override
				public Card getTargetCard()
				{
					return sa[0].getTargetCard();
				}
				
				@Override
				public CardList getTargetList()
				{
					return sa[0].getTargetList();
				}
				
				@Override
				public Player getTargetPlayer()
				{
					return sa[0].getTargetPlayer();
				}
				
				@Override
				public String getXManaCost()
				{
					return sa[0].getXManaCost();
				}
				
				@Override
				public boolean isAbility()
				{
					return sa[0].isAbility();
				}
				
				@Override
				public boolean isBuyBackAbility()
				{
					return sa[0].isBuyBackAbility();
				}
				
				@Override
				public boolean isCycling()
				{
					return sa[0].isCycling();
				}
				
				@Override
				public boolean isExtrinsic()
				{
					return sa[0].isExtrinsic();
				}
				
				@Override
				public boolean isFlashBackAbility()
				{
					return sa[0].isFlashBackAbility();
				}
				
				@Override
				public boolean isIntrinsic()
				{
					return sa[0].isIntrinsic();
				}
				
				@Override
				public boolean isKickerAbility()
				{
					return sa[0].isKickerAbility();
				}
				
				@Override
				public boolean isKothThirdAbility()
				{
					return sa[0].isKothThirdAbility();
				}
				
				@Override
				public boolean isMultiKicker()
				{
					return sa[0].isMultiKicker();
				}
				
				@Override
				public boolean isSpell()
				{
					return sa[0].isSpell();
				}
				
				@Override
				public boolean isTapAbility()
				{
					return sa[0].isTapAbility();
				}
				
				@Override
				public boolean isUntapAbility()
				{
					return sa[0].isUntapAbility();
				}
				
				@Override
				public boolean isXCost()
				{
					return sa[0].isXCost();
				}
				
				@Override
				public void resetDiscardedCost()
				{
					sa[0].resetDiscardedCost();
				}
				
				@Override
				public void resetOnceResolved()
				{
					// Fixing an issue with Targeting + Paying Mana
					//sa[0].resetOnceResolved();
				}
				
				@Override
				public void resetSacrificedCost()
				{
					sa[0].resetSacrificedCost();
				}
				
				@Override
				public void resetExiledCost()
				{
					sa[0].resetExiledCost();
				}
				
				@Override
				public void setAbilityFactory(AbilityFactory af)
				{
					sa[0].setAbilityFactory(af);
				}
				
				@Override
				public void setActivatingPlayer(Player player)
				{
					sa[0].setActivatingPlayer(player);
				}
				
				@Override
				public void setAdditionalManaCost(String cost)
				{
					sa[0].setAdditionalManaCost(cost);
				}
				
				@Override
				public void setAfterPayMana(Input in)
				{
					sa[0].setAfterPayMana(in);
				}
				
				@Override
				public void setAfterResolve(Input in)
				{
					sa[0].setAfterResolve(in);
				}
				
				@Override
				public void setBeforePayMana(Input in)
				{
					sa[0].setBeforePayMana(in);
				}
				
				@Override
				public void setBeforePayManaAI(Command c)
				{
					sa[0].setBeforePayManaAI(c);
				}
				
				@Override
				public void setCancelCommand(Command cancelCommand)
				{
					sa[0].setCancelCommand(cancelCommand);
				}
				
				@Override
				public void setChooseTargetAI(CommandArgs c)
				{
					sa[0].setChooseTargetAI(c);
				}
				
				@Override
				public void setDescription(String s)
				{
					sa[0].setDescription(s);
				}
				
				@Override
				public void setFlashBackAbility(boolean flashBackAbility)
				{
					sa[0].setFlashBackAbility(flashBackAbility);
				}
				
				@Override
				public void setIsBuyBackAbility(boolean b)
				{
					sa[0].setIsBuyBackAbility(b);
				}
				
				@Override
				public void setIsCycling(boolean b)
				{
					sa[0].setIsCycling(b);
				}
				
				@Override
				public void setIsMultiKicker(boolean b)
				{
					sa[0].setIsMultiKicker(b);
				}
				
				@Override
				public void setIsXCost(boolean b)
				{
					sa[0].setIsXCost(b);
				}
				
				@Override
				public void setKickerAbility(boolean kab)
				{
					sa[0].setKickerAbility(kab);
				}
				
				@Override
				public void setKothThirdAbility(boolean kothThirdAbility)
				{
					sa[0].setKothThirdAbility(kothThirdAbility);
				}
				
				@Override
				public void setManaCost(String cost)
				{
					sa[0].setManaCost(cost);
				}
				
				@Override
				public void setMultiKickerManaCost(String cost)
				{
					sa[0].setMultiKickerManaCost(cost);
				}
				
				@Override
				public void setPayCosts(Ability_Cost abCost)
				{
					sa[0].setPayCosts(abCost);
				}
				
				@Override
				public void setRestrictions(SpellAbility_Restriction restrict)
				{
					sa[0].setRestrictions(restrict);
				}
				
				@Override
				public void setSourceCard(Card c)
				{
					sa[0].setSourceCard(c);
				}
				
				@Override
				public void setStackDescription(String s)
				{
					sa[0].setStackDescription(s);
				}
				
				@Override
				public void setSubAbility(Ability_Sub subAbility)
				{
					sa[0].setSubAbility(subAbility);
				}
				
				@Override
				public void setTarget(Target tgt)
				{
					sa[0].setTarget(tgt);
				}
				
				@Override
				public void setTargetCard(Card card)
				{
					sa[0].setTargetCard(card);
				}
				
				@Override
				public void setTargetList(CardList list)
				{
					sa[0].setTargetList(list);
				}
				
				@Override
				public void setTargetPlayer(Player p)
				{
					sa[0].setTargetPlayer(p);
				}
				
				@Override
				public void setType(String s)
				{
					sa[0].setType(s);
				}
				
				@Override
				public void setXManaCost(String cost)
				{
					sa[0].setXManaCost(cost);
				}
				
				@Override
				public boolean wasCancelled()
				{
					return sa[0].wasCancelled();
				}
				
				////////////////////////////////////////
				//THIS ONE IS ALL THAT MATTERS
				////////////////////////////////////////
				@Override
				public void resolve()
				{
					if(!regtrig.requirementsCheck())
					{
						return;
					}
					
					if(decider[0] != null)
					{
						if(decider[0].equals(AllZone.HumanPlayer))
						{
							StringBuilder buildQuestion = new StringBuilder("Use triggered ability of ");
							buildQuestion.append(regtrig.getHostCard().getName()).append("(").append(regtrig.getHostCard().getUniqueNumber()).append(")?");
							buildQuestion.append("\r\n(");
							buildQuestion.append(regtrig.getMapParams().get("TriggerDescription").replace("CARDNAME", regtrig.getHostCard().getName()));
							buildQuestion.append(")");
							if(!GameActionUtil.showYesNoDialog(regtrig.getHostCard(), buildQuestion.toString()))
							{
								return;
							}
						}
						else
						{
							// This isn't quite right, but better than canPlayAI
							if(!sa[0].doTrigger(isMandatory))
							{
								return;
							}
						}
					}
					
					if(sa[0].getSourceCard().getController().isHuman())
					{
						AllZone.GameAction.playSpellAbility_NoStack(sa[0], true);
					}
					else
					{
						// commented out because i don't think this should be called again here
						//sa[0].doTrigger(isMandatory);
						ComputerUtil.playNoStack(sa[0]);
					}

                    //Add eventual delayed trigger.
                    if(regtrig.getMapParams().containsKey("DelayedTrigger"))
                    {
                        String SVarName = regtrig.getMapParams().get("DelayedTrigger");
                        Trigger deltrig = parseTrigger(regtrig.getHostCard().getSVar(SVarName),regtrig.getHostCard());
                        delayedTriggers.add(deltrig);
                    }
				}
			};
			wrapperAbility.setTrigger(true);
			
			if(host.getController().isHuman())
			{
				AllZone.GameAction.playSpellAbility(wrapperAbility);
			}
			else
			{
				wrapperAbility.doTrigger(isMandatory);
				ComputerUtil.playStack(wrapperAbility);
			}

            return true;
		}

        return false;
	}
}
