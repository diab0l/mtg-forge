package forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.esotericsoftware.minlog.Log;

import forge.card.abilityFactory.AbilityFactory;
import forge.card.cardFactory.CardFactoryUtil;
import forge.card.mana.ManaCost;
import forge.card.spellability.Ability;
import forge.card.spellability.Ability_Mana;
import forge.card.spellability.Ability_Static;
import forge.card.spellability.Ability_Triggered;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Spell_Permanent;
import forge.card.spellability.Target;
import forge.card.spellability.Target_Selection;
import forge.gui.GuiUtils;
import forge.gui.input.Input;
import forge.gui.input.Input_PayManaCost_Ability;

public class MagicStack extends MyObservable {
    private ArrayList<SpellAbility> simultaneousStackEntryList = new ArrayList<SpellAbility>();

	private ArrayList<SpellAbility> stack = new ArrayList<SpellAbility>();
	private ArrayList<SpellAbility> frozenStack = new ArrayList<SpellAbility>();
	private boolean frozen = false;
    private boolean bResolving = false;
    private boolean splitSecondOnStack = false;

	private Object StormCount;
	private Object PlayerSpellCount;
	private Object PlayerCreatureSpellCount;
	private Object ComputerSpellCount;
	private Object ComputerCreatureSpellCount;

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	public void reset() {
		stack.clear();
		frozen = false;
		splitSecondOnStack = false;
		frozenStack.clear();
		this.updateObservers();
	}
	
	public boolean isSplitSecondOnStack() {
		return splitSecondOnStack;
	}
	
	public void setSplitSecondOnStack() {
		for(SpellAbility sa:stack) {
			if(sa.getSourceCard().hasKeyword("Split second")) {
				splitSecondOnStack = true;
				return;
			}
		}
		splitSecondOnStack = false;
	}

	public void freezeStack() {
		frozen = true;
	}

	public void addAndUnfreeze(SpellAbility ability) {
		ability.getRestrictions().abilityActivated();
		if(ability.getRestrictions().getActivationNumberSacrifice() != -1 &&
				ability.getRestrictions().getNumberTurnActivations() >= ability.getRestrictions().getActivationNumberSacrifice()) {
			ability.getSourceCard().addExtrinsicKeyword("At the beginning of the end step, sacrifice CARDNAME.");
		}
		// triggered abilities should go on the frozen stack
		if (!ability.isTrigger())
			frozen = false;
		
		this.add(ability);

		// if the ability is a spell, but not a copied spell and its not already on the stack zone, move there
		if (ability.isSpell()){
			Card source = ability.getSourceCard();
			if (!source.isCopiedSpell() && !AllZone.getZone(source).is(Constant.Zone.Stack))
				AllZone.GameAction.moveToStack(source);
		}

		if (ability.isTrigger())
			unfreezeStack();
	}

	public void unfreezeStack() {
		frozen = false;
		boolean checkState = !frozenStack.isEmpty();
		while (!frozenStack.isEmpty()) {
			SpellAbility sa = frozenStack.get(0);
			frozenStack.remove(0);
			this.add(sa);
		}
		if (checkState)
			AllZone.GameAction.checkStateEffects();
	}

	public void clearFrozen() {
		// TODO: frozen triggered abilities and undoable costs have nasty
		// consequences
		frozen = false;
		frozenStack.clear();
	}
	
    public void setResolving(boolean b) { bResolving = b; }
    public boolean getResolving() { return bResolving; }

	public void add(SpellAbility sp, boolean useX) {
		if (!useX)
			this.add(sp);
		else {
			
			// TODO make working triggered abilities!
			if (sp instanceof Ability_Mana || sp instanceof Ability_Triggered)
				sp.resolve();
			else {
				push(sp);
				if (sp.getTargetCard() != null)
					CardFactoryUtil.checkTargetingEffects(sp, sp.getTargetCard());
			}
		}
	}

	public ManaCost getMultiKickerSpellCostChange(SpellAbility sa) {
		int Max = 25;
		String[] Numbers = new String[Max];
		for (int no = 0; no < Max; no++)
			Numbers[no] = String.valueOf(no);
		
		ManaCost manaCost = new ManaCost(sa.getManaCost());
		String Mana = manaCost.toString();
		
		int MultiKickerPaid = AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid;
		
		String Number_ManaCost = " ";
		
		if (Mana.toString().length() == 1)
			Number_ManaCost = Mana.toString().substring(0, 1);
		
		else if (Mana.toString().length() == 0)
			Number_ManaCost = "0"; // Should Never Occur
		
		else
			Number_ManaCost = Mana.toString().substring(0, 2);
		Number_ManaCost = Number_ManaCost.trim();

		for (int check = 0; check < Max; check++) {
			if (Number_ManaCost.equals(Numbers[check])) {

				if (check - MultiKickerPaid < 0) {
					MultiKickerPaid = MultiKickerPaid - check;
					AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid = MultiKickerPaid;
					Mana = Mana.replaceFirst(String.valueOf(check), "0");
				} 
				else {
					Mana = Mana.replaceFirst(String.valueOf(check), String.valueOf(check - MultiKickerPaid));
					MultiKickerPaid = 0;
					AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid = MultiKickerPaid;
				}
			}
			Mana = Mana.trim();
			if (Mana.equals(""))
				Mana = "0";
			manaCost = new ManaCost(Mana);
		}
		String Color_cut = AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid_Colored;

		for (int Colored_Cut = 0; Colored_Cut < Color_cut.length(); Colored_Cut++) {
			if ("WUGRB".contains(Color_cut.substring(Colored_Cut, Colored_Cut + 1))) {
				
				if (!Mana.equals(Mana.replaceFirst((Color_cut.substring(Colored_Cut, Colored_Cut + 1)), ""))) {
					Mana = Mana.replaceFirst(Color_cut.substring(Colored_Cut, Colored_Cut + 1), "");
					AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid_Colored = AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid_Colored
							.replaceFirst(Color_cut.substring(Colored_Cut, Colored_Cut + 1), "");
					Mana = Mana.trim();
					if (Mana.equals(""))
						Mana = "0";
					manaCost = new ManaCost(Mana);
				}
			}
		}

		return manaCost;
	}
	
	//TODO - this may be able to use a straight copy of MultiKicker cost change
	public ManaCost getReplicateSpellCostChange(SpellAbility sa) {
		ManaCost manaCost = new ManaCost(sa.getManaCost());
		//String Mana = manaCost.toString();
		return manaCost;
	}

	public void add(final SpellAbility sp) {
		if (sp instanceof Ability_Mana) { // Mana Abilities go straight through
			sp.resolve();
			sp.resetOnceResolved();
			return;
		}

		if (frozen) {
			frozenStack.add(sp);
			return;
		}

		// if activating player slips through the cracks, assign activating
		// Player to the controller here
		if (null == sp.getActivatingPlayer()) {
			sp.setActivatingPlayer(sp.getSourceCard().getController());
			System.out.println(sp.getSourceCard().getName() + " - activatingPlayer not set before adding to stack.");
		}
		
		if (AllZone.Phase.is(Constant.Phase.Cleanup)){	// If something triggers during Cleanup, need to repeat
			AllZone.Phase.repeatPhase();
		}
		
		// TODO: triggered abilities need to be fixed 
		if (!(sp instanceof Ability_Triggered || sp instanceof Ability_Static))	
			AllZone.Phase.setPriority(sp.getActivatingPlayer());	// when something is added we need to setPriority
				
		// WheneverKeyword Test
		boolean ActualEffectTriggered = false;
		if (sp.getSourceCard().getKeyword().toString().contains("WheneverKeyword")) {
			ArrayList<String> a = sp.getSourceCard().getKeyword();
			int WheneverKeywords = 0;
			int WheneverKeyword_Number[] = new int[a.size()];
			
			for (int x = 0; x < a.size(); x++)
				if (a.get(x).toString().startsWith("WheneverKeyword")) {
					WheneverKeyword_Number[WheneverKeywords] = x;
					WheneverKeywords = WheneverKeywords + 1;
				}

			for (int CKeywords = 0; CKeywords < WheneverKeywords; CKeywords++) {
				String parse = sp.getSourceCard().getKeyword().get(
						WheneverKeyword_Number[CKeywords]).toString();
				String k[] = parse.split(":");
				if (k[1].equals("ActualSpell")
						&& ActualEffectTriggered == false) {
					AllZone.GameAction.checkWheneverKeyword(sp.getSourceCard(),"ActualSpell", null);
					sp.getSourceCard().removeIntrinsicKeyword(parse);
					ActualEffectTriggered = true;
				}
			}

		}
		if (!ActualEffectTriggered) {
			// // WheneverKeyword Test: Added one } at end
			if (sp instanceof Ability_Triggered || sp instanceof Ability_Static)
				// TODO make working triggered ability
				sp.resolve();
			else {
				if (sp.isKickerAbility()) {
					sp.getSourceCard().setKicked(true);
					SpellAbility[] sa = sp.getSourceCard().getSpellAbility();
					int AbilityNumber = 0;
					
					for (int i = 0; i < sa.length; i++)
						if (sa[i] == sp)
							AbilityNumber = i;
					
					sp.getSourceCard().setAbilityUsed(AbilityNumber);
				}
				if (sp.getSourceCard().isCopiedSpell())
					push(sp);
				
				else if (!sp.isMultiKicker() && !sp.isReplicate() && !sp.isXCost()) {
					push(sp);
				}
				
				else if (sp.getPayCosts() != null && ! sp.isReplicate()){
					push(sp);
				}
				
				else if (sp.isXCost()) {
					// TODO: convert any X costs to use abCost so it happens earlier
					final SpellAbility sa = sp;
					final Ability ability = new Ability(sp.getSourceCard(), sa.getXManaCost()) {
						public void resolve() {
							Card crd = this.getSourceCard();
							crd.addXManaCostPaid(1);
						}
					};

					final Command unpaidCommand = new Command() {
						private static final long serialVersionUID = -3342222770086269767L;

						public void execute() {
							push(sa);
						}
					};

					final Command paidCommand = new Command() {
						private static final long serialVersionUID = -2224875229611007788L;

						public void execute() {
							ability.resolve();
							Card crd = sa.getSourceCard();
							AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Pay X cost for " + crd.getName()
											 + " (X=" + crd.getXManaCostPaid() + ")\r\n", 
													ability.getManaCost(), this, unpaidCommand, true));
						}
					};
					
					Card crd = sa.getSourceCard();
					if (sp.getSourceCard().getController().equals(AllZone.HumanPlayer)) {
						AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Pay X cost for " +
										sp.getSourceCard().getName() + " (X=" + crd.getXManaCostPaid() + ")\r\n", 
												ability.getManaCost(), paidCommand, unpaidCommand, true));
					} 
					
					else // computer
					{
						int neededDamage = CardFactoryUtil.getNeededXDamage(sa);

						while (ComputerUtil.canPayCost(ability) && neededDamage != sa.getSourceCard().getXManaCostPaid()) {
							ComputerUtil.playNoStack(ability);
						}
						push(sa);
					}
				} 
				
				else if (sp.isMultiKicker()){
					// TODO: convert multikicker support in abCost so this doesn't happen here
					// both X and multi is not supported yet
				
					final SpellAbility sa = sp;
					final Ability ability = new Ability(sp.getSourceCard(), sp.getMultiKickerManaCost()) {
						public void resolve() {
							this.getSourceCard().addMultiKickerMagnitude(1);
						}
					};

					final Command unpaidCommand = new Command() {
						private static final long serialVersionUID = -3342222770086269767L;

						public void execute() {
							push(sa);
						}
					};

					final Command paidCommand = new Command() {
						private static final long serialVersionUID = -6037161763374971106L;

						public void execute() {
							ability.resolve();
							ManaCost manaCost = getMultiKickerSpellCostChange(ability);
							if (manaCost.isPaid()) {
								this.execute();
							} else {
								if (AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid == 0
										&& AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid_Colored.equals("")) {
									
									AllZone.InputControl.setInput(new Input_PayManaCost_Ability(
											"Multikicker for "+ sa.getSourceCard() + "\r\n"
											+ "Times Kicked: " + sa.getSourceCard().getMultiKickerMagnitude() + "\r\n", 
											manaCost.toString(), this, unpaidCommand));
								} 
								
								else {
									AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Multikicker for "
											+ sa.getSourceCard() + "\r\n" + "Mana in Reserve: "
											+ ((AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid != 0) ? 
											AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid : "")
											+ AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid_Colored + "\r\n"
											+ "Times Kicked: " + sa.getSourceCard().getMultiKickerMagnitude() + "\r\n", 
									manaCost.toString(), this, unpaidCommand));
								}
							}
						}
					};

					if (sp.getSourceCard().getController().equals(
							AllZone.HumanPlayer)) {
						ManaCost manaCost = getMultiKickerSpellCostChange(ability);

						if (manaCost.isPaid()) {
							paidCommand.execute();
						} else {
							if (AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid == 0
									&& AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid_Colored.equals("")) {
								AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Multikicker for "
									+ sa.getSourceCard() + "\r\n" + "Times Kicked: " 
									+ sa.getSourceCard().getMultiKickerMagnitude() + "\r\n", 
								manaCost.toString(), paidCommand, unpaidCommand));
							} else {
								AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Multikicker for "
									+ sa.getSourceCard() + "\r\n" + "Mana in Reserve: " + 
									((AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid != 0) ? 
											AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid: "")
									+ AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid_Colored
									+ "\r\n" + "Times Kicked: " + sa.getSourceCard().getMultiKickerMagnitude() + "\r\n", 
									manaCost.toString(), paidCommand, unpaidCommand));
							}
						}
					} 
					
					else // computer
					{
						while (ComputerUtil.canPayCost(ability))
							ComputerUtil.playNoStack(ability);
						push(sa);
					}
				}
				
				else if (sp.isReplicate()){
					// TODO: convert multikicker/replicate support in abCost so this doesn't happen here
					// X and multi and replicate are not supported yet
				
					final SpellAbility sa = sp;
					final Ability ability = new Ability(sp.getSourceCard(), sp.getReplicateManaCost()) {
						public void resolve() {
							this.getSourceCard().addReplicateMagnitude(1);
						}
					};

					final Command unpaidCommand = new Command() {
						private static final long serialVersionUID = -3180458633098297855L;

						public void execute() {
							push(sa);
							for(int i = 0; i < sp.getSourceCard().getReplicateMagnitude(); i++) {
								AllZone.CardFactory.copySpellontoStack(sp.getSourceCard(), sp.getSourceCard(), false);
							}
						}
					};

					final Command paidCommand = new Command() {
						private static final long serialVersionUID = 132624005072267304L;

						public void execute() {
							ability.resolve();
							ManaCost manaCost = getReplicateSpellCostChange(ability);
							if (manaCost.isPaid()) {
								this.execute();
							} else {
								/*
								if (AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid == 0
										&& AllZone.GameAction.CostCutting_GetMultiMickerManaCostPaid_Colored.equals("")) {
									
									AllZone.InputControl.setInput(new Input_PayManaCost_Ability(
											"Replicate for "+ sa.getSourceCard() + "\r\n"
											+ "Times Kicked: " + sa.getSourceCard().getMultiKickerMagnitude() + "\r\n", 
											manaCost.toString(), this, unpaidCommand));
								} 
								
								else {*/
									AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Replicate for "
											+ sa.getSourceCard() + "\r\n" 
											+ "Times Replicated: " + sa.getSourceCard().getReplicateMagnitude() + "\r\n", 
									manaCost.toString(), this, unpaidCommand));
								//}
							}
						}
					};

					if (sp.getSourceCard().getController().equals(
							AllZone.HumanPlayer)) {
						ManaCost manaCost = getMultiKickerSpellCostChange(ability);

						if (manaCost.isPaid()) {
							paidCommand.execute();
						} else {
								AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Replicate for "
									+ sa.getSourceCard() + "\r\n" + 
									"Times Replicated: " + sa.getSourceCard().getReplicateMagnitude() + "\r\n", 
									manaCost.toString(), paidCommand, unpaidCommand));
						}
					} 
					
					else // computer
					{
						while (ComputerUtil.canPayCost(ability))
							ComputerUtil.playNoStack(ability);
						push(sa);
					}
				}

			}
			
			//Run trigger
    		HashMap<String,Object> runParams = new HashMap<String,Object>();
    		runParams.put("CastSA", sp);
    		AllZone.TriggerHandler.runTrigger("SpellAbilityCast", runParams);
    		
    		if(sp.isSpell())
    		{
    			AllZone.TriggerHandler.runTrigger("SpellCast", runParams);
    		}
    		if(sp.isAbility())
    		{
    			AllZone.TriggerHandler.runTrigger("AbilityCast", runParams);
    		}
    		if(sp.isCycling())
    		{
    			runParams.clear();
    			runParams.put("Card", sp.getSourceCard());
    			AllZone.TriggerHandler.runTrigger("Cycled", runParams);
    		}

		}
		
		if(sp instanceof Spell_Permanent && sp.getSourceCard().getName().equals("Mana Vortex")) {
			final SpellAbility counter = new Ability(sp.getSourceCard(), "0") {
				@Override
				public void resolve() {
					Input in = new Input() {
						private static final long serialVersionUID = -2042489457719935420L;

						@Override
						public void showMessage() {
							AllZone.Display.showMessage("Mana Vortex - select a land to sacrifice");
							ButtonUtil.enableOnlyCancel();
						}

						@Override
						public void selectButtonCancel() {
							AllZone.Stack.pop();
							AllZone.GameAction.moveToGraveyard(sp.getSourceCard());
							stop();
						}

						@Override
						public void selectCard(Card c, PlayerZone zone) {
							if(zone.is(Constant.Zone.Battlefield) && c.getController().equals(AllZone.HumanPlayer)
									&& c.isLand()) {
								AllZone.GameAction.sacrifice(c);
								stop();
							}
						}
					};
					SpellAbility prev = peek();
					if(prev instanceof Spell_Permanent && prev.getSourceCard().getName().equals("Mana Vortex")) {
						if(sp.getSourceCard().getController().isHuman()) {
							AllZone.InputControl.setInput(in);
						}
						else {//Computer
							CardList lands = AllZoneUtil.getPlayerLandsInPlay(AllZone.ComputerPlayer);
							if(!lands.isEmpty()) {
								AllZone.ComputerPlayer.sacrificePermanent("prompt", lands);
							}
							else {
								AllZone.Stack.pop();
								AllZone.GameAction.moveToGraveyard(sp.getSourceCard());
							}
						}
					}
					
				}//resolve()
			};//SpellAbility
			counter.setStackDescription(sp.getSourceCard().getName()+" - counter Mana Vortex unless you sacrifice a land.");
			add(counter);
		}
		
		/*
		 * Whenever a player casts a spell, counter it if a card with the same name
		 * is in a graveyard or a nontoken permanent with the same name is on the battlefield.
		 */
		if(sp.isSpell() && AllZoneUtil.isCardInPlay("Bazaar of Wonders")) {
			boolean found = false;
			CardList all = AllZoneUtil.getCardsInPlay();
			all = all.filter(AllZoneUtil.nonToken);
			CardList graves = AllZoneUtil.getCardsInGraveyard();
			all.add(graves);
			
			for(Card c:all) {
				if(sp.getSourceCard().getName().equals(c.getName())) found = true;
			}
			
			if(found) {			
				CardList bazaars = AllZoneUtil.getCardsInPlay("Bazaar of Wonders");  //should only be 1...
				for(final Card bazaar:bazaars) {
					final SpellAbility counter = new Ability(bazaar, "0") {
						@Override
						public void resolve() {
							if(AllZone.Stack.size() > 0) AllZone.Stack.pop();
						}//resolve()
					};//SpellAbility
					counter.setStackDescription(bazaar.getName()+" - counter "+sp.getSourceCard().getName()+".");
					add(counter);
				}
			}
		}
		
		//Lurking Predators
		if(sp.isSpell())
		{
			CardListFilter filter = new CardListFilter() {
				public boolean addCard(Card c)
				{
					return c.getName().equals("Lurking Predators");
				}
			};
			
			CardList lurkingPredators = new CardList();
			if(sp.getSourceCard().getController() == AllZone.HumanPlayer)
			{
				lurkingPredators.add(new CardList(AllZone.Computer_Battlefield.getCards()));
			}
			else
			{
				lurkingPredators.add(new CardList(AllZone.Human_Battlefield.getCards()));
			}
			
			lurkingPredators = lurkingPredators.filter(filter);
			
			for(int i=0;i<lurkingPredators.size();i++)
			{
				StringBuilder revealMsg = new StringBuilder("");
				if(lurkingPredators.get(i).getController() == AllZone.HumanPlayer)
				{
					revealMsg.append("You reveal: ");
					if(AllZone.Human_Library.size() == 0)
					{
						revealMsg.append("Nothing!");
						GameActionUtil.showInfoDialg(revealMsg.toString());
						continue;
					}
					Card revealed = AllZone.Human_Library.get(0);

					revealMsg.append(revealed.getName());
					if(!revealed.isType("Creature"))
					{
						revealMsg.append("\n\rPut it on the bottom of your library?");
						if(GameActionUtil.showYesNoDialog(lurkingPredators.get(i), revealMsg.toString()))
						{
							AllZone.GameAction.moveToBottomOfLibrary(revealed);
						}
						else
						{
							AllZone.GameAction.moveToLibrary(revealed);
						}
					}
					else
					{
						GameActionUtil.showInfoDialg(revealMsg.toString());
						AllZone.GameAction.moveToPlay(revealed);
					}
				}
				else
				{
					revealMsg.append("Computer reveals: ");
					if(AllZone.Computer_Library.size() == 0)
					{
						revealMsg.append("Nothing!");
						GameActionUtil.showInfoDialg(revealMsg.toString());
						continue;
					}
					Card revealed = AllZone.Computer_Library.get(0);
					revealMsg.append(revealed.getName());
					if(!revealed.isType("Creature"))
					{
						GameActionUtil.showInfoDialg(revealMsg.toString());
						if(lurkingPredators.size() > i)
						{
							AllZone.GameAction.moveToBottomOfLibrary(revealed);
						}
						else
						{
							AllZone.GameAction.moveToLibrary(revealed);
						}
					}
					else
					{
						GameActionUtil.showInfoDialg(revealMsg.toString());
						AllZone.GameAction.moveToPlay(revealed);
					}
					
				}
			}
		}
		
		if (sp.getTargetCard() != null)
			CardFactoryUtil.checkTargetingEffects(sp, sp.getTargetCard());
	}

	public int size() {
		return stack.size();
	}

	// Push should only be used by add.
	private void push(SpellAbility sp) {
		if (null == sp.getActivatingPlayer()) {
			sp.setActivatingPlayer(sp.getSourceCard().getController());
			System.out.println(sp.getSourceCard().getName() + " - activatingPlayer not set before adding to stack.");
		}
		
		stack.add(0, sp);
		setSplitSecondOnStack();

		this.updateObservers();
		
		if (sp.isSpell() && !sp.getSourceCard().isCopiedSpell()) {
			Phase.setStormCount(Phase.getStormCount() + 1);
			if (sp.getSourceCard().getController() == AllZone.HumanPlayer) {
				Phase.PlayerSpellCount = Phase.PlayerSpellCount + 1;
				if (sp.getSourceCard().isCreature()) {
					Phase.PlayerCreatureSpellCount = Phase.PlayerCreatureSpellCount + 1;
				}
			} 
			
			else {
				Phase.ComputerSpellCount = Phase.ComputerSpellCount + 1;
				if (sp.getSourceCard().isCreature()) {
					Phase.ComputerCreatureSpellCount = Phase.ComputerCreatureSpellCount + 1;
				}
			}
			// attempt to counter human spell 

			GameActionUtil.executePlayCardEffects(sp);
		}
	}

	public void resolveStack() {
		// Resolving the Stack
		GuiDisplayUtil.updateGUI();
		this.freezeStack();	// freeze the stack while we're in the middle of resolving
		setResolving(true);
		
		SpellAbility sa = AllZone.Stack.pop();
		
		AllZone.Phase.resetPriority();	// ActivePlayer gains priority first after Resolve
		Card source = sa.getSourceCard();
		boolean fizzle = hasFizzled(sa, source);
		Command buyback = null;
		
		
		if (!fizzle) {
			final Card crd = source;
			if (sa.isBuyBackAbility()) {
				buyback = new Command() {
					private static final long serialVersionUID = -2559488318473330418L;

					public void execute() {
						AllZone.GameAction.moveToHand(crd);
					}
				};
				source.addReplaceMoveToGraveyardCommand(buyback);
			}

			// To stop Copied Spells from going into the graveyard.
			if (sa.getSourceCard().isCopiedSpell()) {
				source.addReplaceMoveToGraveyardCommand(new Command() {
					private static final long serialVersionUID = -2559488318473330418L;

					public void execute() {
					}
				});
			}
			if(sa.getAbilityFactory() != null)
			{
				AbilityFactory.handleRemembering(sa.getAbilityFactory());
			}
			sa.resolve();
		} else {
			// TODO: Spell fizzles, what's the best way to alert player?
			Log.debug(source.getName() + " ability fizzles.");
		}

		// Handle cards that need to be moved differently
		if (sa.isFlashBackAbility()){
			AllZone.GameAction.exile(source);
			sa.setFlashBackAbility(false);
		}

		else if (fizzle && sa.isSpell())
			AllZone.GameAction.moveToGraveyard(source);
		
		else if (sa.isAbility())
		{}	// don't send to graveyard if the spell is using an ability

		// If Spell and still on the Stack then let it goto the graveyard or replace its own movement
		else if (!source.isCopiedSpell() && (source.isInstant() || source.isSorcery()) && 
				AllZone.getZone(source).is(Constant.Zone.Stack)){
			if (source.getReplaceMoveToGraveyard().size() == 0)
				AllZone.GameAction.moveToGraveyard(source);
			else{
				source.replaceMoveToGraveyard();
				if (buyback != null)
					source.getReplaceMoveToGraveyard().remove(buyback);
			}
		}

		// After SA resolves we have to do a handful of things
		setResolving(false);
		this.unfreezeStack(); 
		sa.resetOnceResolved();

		AllZone.GameAction.checkStateEffects();
		
		AllZone.Phase.setNeedToNextPhase(false);

		if (AllZone.Phase.inCombat()) 
			CombatUtil.showCombat();

		GuiDisplayUtil.updateGUI();
	}
	
	public boolean hasFizzled(SpellAbility sa, Card source){
		// By default this has not fizzled
		boolean fizzle = false;
		
		boolean firstTarget = true;

		SpellAbility fizzSA = sa;
		
		while(true){
			Target tgt = fizzSA.getTarget();
			if (tgt != null && tgt.getMinTargets(source, fizzSA) == 0 && tgt.getNumTargeted() == 0){
				// Don't assume fizzled for minTargets == 0 and nothing is targeted
			}
			else if (firstTarget && (tgt != null || fizzSA.getTargetCard() != null || fizzSA.getTargetPlayer() != null)){
				// If there is at least 1 target, fizzle switches because ALL targets need to be invalid
				fizzle = true;
				firstTarget = false;	
			}
			
			if (tgt != null){
				// With multi-targets, as long as one target is still legal, we'll try to go through as much as possible
				ArrayList<Object> tgts = tgt.getTargets();
				for(Object o : tgts){
					if (o instanceof Player){
						Player p = (Player)o;
						fizzle &= !(p.canTarget(fizzSA.getTargetCard()));
					}
					if (o instanceof Card){
						Card card = (Card)o;
						fizzle &= !(CardFactoryUtil.isTargetStillValid(fizzSA, card));
					}
					if (o instanceof SpellAbility){
						SpellAbility tgtSA = (SpellAbility)o;
						fizzle &= !(Target_Selection.matchSpellAbility(fizzSA, tgtSA, tgt));
					}
				}
			}
			else if (fizzSA.getTargetCard() != null) {
				// Fizzling will only work for Abilities that use the Target class,
				// since the info isn't available otherwise
				fizzle &= !CardFactoryUtil.isTargetStillValid(fizzSA, fizzSA.getTargetCard());
			} 
			else if (fizzSA.getTargetPlayer() != null) {
				fizzle &= !fizzSA.getTargetPlayer().canTarget(source);
			}
			
			if (fizzSA.getSubAbility() != null)
				fizzSA = fizzSA.getSubAbility();
			else
				break;
		}
	
		return fizzle;
	}
	

	public SpellAbility pop() {
		SpellAbility sp = (SpellAbility) stack.remove(0);
		this.updateObservers();
		setSplitSecondOnStack();
		return sp;
	}

	// index = 0 is the top, index = 1 is the next to top, etc...
	public SpellAbility peek(int index) {
		return (SpellAbility) stack.get(index);
	}

	public SpellAbility peek() {
		return peek(0);
	}
	
	public void remove(SpellAbility sa) {
		stack.remove(sa);
		frozenStack.remove(sa);
		this.updateObservers();
	}

	public boolean contains(SpellAbility sa) {
		return stack.contains(sa);
	}

	public ArrayList<Card> getSourceCards() {
		ArrayList<Card> a = new ArrayList<Card>();
		Iterator<SpellAbility> it = stack.iterator();
		while (it.hasNext())
			a.add(((SpellAbility) it.next()).getSourceCard());

		return a;
	}

	public void setStormCount(Object stormCount) {
		StormCount = stormCount;
	}

	public Object getStormCount() {
		return StormCount;
	}

	public void setPlayerCreatureSpellCount(Object playerCreatureSpellCount) {
		PlayerCreatureSpellCount = playerCreatureSpellCount;
	}

	public Object getPlayerCreatureSpellCount() {
		return PlayerCreatureSpellCount;
	}

	public void setPlayerSpellCount(Object playerSpellCount) {
		PlayerSpellCount = playerSpellCount;
	}

	public Object getPlayerSpellCount() {
		return PlayerSpellCount;
	}

	public void setComputerSpellCount(Object computerSpellCount) {
		ComputerSpellCount = computerSpellCount;
	}

	public Object getComputerSpellCount() {
		return ComputerSpellCount;
	}

	public void setComputerCreatureSpellCount(Object computerCreatureSpellCount) {
		ComputerCreatureSpellCount = computerCreatureSpellCount;
	}

	public Object getComputerCreatureSpellCount() {
		return ComputerCreatureSpellCount;
	}

    public void addSimultaneousStackEntry(SpellAbility sa)
    {
        simultaneousStackEntryList.add(sa);
        /*
        *Debug output.
        System.out.println("STO add! Size:" + simultaneousStackEntryList.size());
        */
    }

    public void chooseOrderOfSimultaneousStackEntryAll()
    {
        /*
        *Debug output.
        if(simultaneousStackEntryList.size() > 0)
        {
            System.out.println("STO run! Size:" + simultaneousStackEntryList.size());
        }
        */
        chooseOrderOfSimultaneousStackEntry(AllZone.Phase.getPlayerTurn());
        chooseOrderOfSimultaneousStackEntry(AllZone.Phase.getPlayerTurn().getOpponent());
    }

    public void chooseOrderOfSimultaneousStackEntry(Player activePlayer)
    {
        if(simultaneousStackEntryList.size() == 0)
            return;

        ArrayList<SpellAbility> activePlayerSAs = new ArrayList<SpellAbility>();
        for(int i=0;i<simultaneousStackEntryList.size();i++)
        {
            if(simultaneousStackEntryList.get(i).getActivatingPlayer() == null)
            {
                if(simultaneousStackEntryList.get(i).getSourceCard().getController().equals(activePlayer))
                {
                    activePlayerSAs.add(simultaneousStackEntryList.get(i));
                    simultaneousStackEntryList.remove(i);
                    i--;
                }
            }
            else
            {
                if(simultaneousStackEntryList.get(i).getActivatingPlayer().equals(activePlayer))
                {
                    activePlayerSAs.add(simultaneousStackEntryList.get(i));
                    simultaneousStackEntryList.remove(i);
                    i--;
                }
            }
        }
        if(activePlayerSAs.size() == 0)
            return;

        if(activePlayer.isComputer())
        {
            for(SpellAbility sa : activePlayerSAs)
            {
                sa.doTrigger(sa.isMandatory());
				ComputerUtil.playStack(sa);
            }
        }
        else
        {
            while(activePlayerSAs.size() > 1)
            {
               SpellAbility next = (SpellAbility) GuiUtils.getChoice("Choose which spell or ability to put on the stack next.", activePlayerSAs.toArray());
               activePlayerSAs.remove(next);

                if(next.isTrigger())
                {
                    System.out.println("Stack order: AllZone.GameAction.playSpellAbility(next)");
                    AllZone.GameAction.playSpellAbility(next);
                }
                else
                {
                    System.out.println("Stack order: AllZone.Stack.add(next)");
                    add(next);
                }
            }

            if(activePlayerSAs.get(0).isTrigger())
                AllZone.GameAction.playSpellAbility(activePlayerSAs.get(0));
            else
            add(activePlayerSAs.get(0));
            //AllZone.GameAction.playSpellAbility(activePlayerSAs.get(0));
        }
    }

}
