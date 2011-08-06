package forge.card.abilityFactory;

import java.util.ArrayList;
import java.util.HashMap;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.Card;
import forge.CardList;
import forge.CardListFilter;
import forge.Command;
import forge.ComputerUtil;
import forge.Constant;
import forge.GameActionUtil;
import forge.Player;
import forge.card.cardFactory.CardFactoryUtil;
import forge.card.spellability.Ability;
import forge.card.spellability.Ability_Activated;
import forge.card.spellability.Ability_Sub;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.gui.GuiUtils;
import forge.gui.input.Input;

//Type - Spell or Ability or SpellOrAbility
//CounterValid - a "valid" expression for types to counter
//SpellTarget - a "valid" expression for targets of the spell to counter
//Destination - send countered spell to: (only applies to Spells; ignored for Abilities)
//		-Graveyard (Default)
//		-Exile
//		-TopOfLibrary
//		-Hand
//		-BottomOfLibrary
//		-ShuffleIntoLibrary
//UnlessCost - counter target spell unless it's controller pays this cost
//PowerSink - true if the drawback type part of Power Sink should be used
//ExtraActions - implemented exactly as spCounter used them (can probably be updated to SubAbility/Drawback), then this param is eliminated

//Examples:
//A:SP$Counter | Cost$ 1 G | Type$ Ability | SpellDescription$ Counter target activated ability.
//A:AB$Counter | Cost$ G G | Type$ Spell | Destination$ Exile | CounterValid$ Color.Black | SpellDescription$ xxxxx

public class AbilityFactory_CounterMagic {

	private AbilityFactory af = null;
	private HashMap<String,String> params = null;
	private String targetType = null;
	private String destination = null;
	private String[] splitTargetingRestrictions = null;
	private String[] splitSpellTargetRestrictions = null;
	private String[] splitExtraActions;
	private String unlessCost = null;

	private final SpellAbility[] tgt = new SpellAbility[1];

	public AbilityFactory_CounterMagic(AbilityFactory newAF) {
		af = newAF;
		params = af.getMapParams();
		targetType = params.containsKey("Type") ? params.get("Type") : "Spell";
		destination = params.containsKey("Destination") ? params.get("Destination") : "Graveyard";
		if(params.containsKey("CounterValid")) {
			splitTargetingRestrictions = params.get("CounterValid").split(",");
		}
		else splitTargetingRestrictions = new String[] {"Card"};
		if(params.containsKey("SpellTarget")) {
			splitSpellTargetRestrictions = params.get("SpellTarget").split(",");
		}
		if(params.containsKey("ExtraActions")) {
			splitExtraActions = params.get("ExtraActions").split(" ");
		}
		else splitExtraActions = new String[] {"None"};
		
		if(params.containsKey("UnlessCost")) 
			unlessCost = params.get("UnlessCost").trim();

		tgt[0] = null;
	}

	public SpellAbility getAbilityCounter(final AbilityFactory AF) {
		final SpellAbility abCounter = new Ability_Activated(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()) {
			private static final long serialVersionUID = -3895990436431818899L;

			@Override
			public String getStackDescription() {
				// when getStackDesc is called, just build exactly what is happening
				return counterStackDescription(af, this);
			}

			@Override
			public boolean canPlay() {
				// super takes care of AdditionalCosts
				//important to keep super.canPlay() first due to targeting hack in counterCanPlay
				return super.canPlay() && counterCanPlay(af, this);	 
			}

			@Override
			public boolean canPlayAI() {
				return counterCanPlayAI(af, this);
			}

			@Override
			public void resolve() {
				counterResolve(af, this);
			}

			@Override
			public boolean doTrigger(boolean mandatory) {
				return counterCanPlayAI(af, this);
			}

		};
		return abCounter;
	}

	public SpellAbility getSpellCounter(final AbilityFactory AF) {
		final SpellAbility spCounter = new Spell(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()) {
			private static final long serialVersionUID = -4272851734871573693L;

			@Override
			public String getStackDescription() {
				return counterStackDescription(af, this);
			}

			@Override
			public boolean canPlay() {
				// super takes care of AdditionalCosts
				//important to keep super.canPlay() first due to targeting hack in counterCanPlay
				return super.canPlay() && counterCanPlay(af, this);	
			}

			@Override
			public boolean canPlayAI() {
				return counterCanPlayAI(af, this);
			}

			@Override
			public void resolve() {
				counterResolve(af, this);
			}

		};
		return spCounter;
	}

	private void counterResolve(final AbilityFactory af, final SpellAbility sa) {
		Card source = sa.getSourceCard();
		//copied from spCounter
		if(matchSpellAbility(sa.getSourceCard(), tgt[0], splitTargetingRestrictions, splitSpellTargetRestrictions, targetType) 
				&& AllZone.Stack.contains(tgt[0])
				&& !tgt[0].getSourceCard().keywordsContain("CARDNAME can't be countered.")) {
			final SpellAbility tgtSA = tgt[0];
			Card tgtSACard = tgtSA.getSourceCard();

			System.out.println("Send countered spell to " + destination);

			if(unlessCost != null) {
				String unlessCostFinal = unlessCost;
				if(unlessCost.equals("X"))
				{
					unlessCostFinal = Integer.toString(CardFactoryUtil.xCount(af.getHostCard(), af.getHostCard().getSVar("X")));
				}
				
				Ability ability = new Ability(af.getHostCard(), unlessCostFinal) {
                    @Override
                    public void resolve() {
                        ;
                    }
                };
                
                final Command unpaidCommand = new Command() {
                    private static final long serialVersionUID = 8094833091127334678L;
                    
                    public void execute() {
                    	removeFromStack(tgtSA,sa);
                    	if(params.containsKey("PowerSink")) doPowerSink(AllZone.HumanPlayer);
                    }
                };
                
                if(AllZone.Stack.peek().getActivatingPlayer().isHuman()) {
                	GameActionUtil.payManaDuringAbilityResolve(af.getHostCard() + "\r\n", ability.getManaCost(), 
                			Command.Blank, unpaidCommand);
                } else {
                    if(ComputerUtil.canPayCost(ability)) ComputerUtil.playNoStack(ability);
                    else {
                        removeFromStack(tgtSA,sa);
                        if(params.containsKey("PowerSink")) doPowerSink(AllZone.ComputerPlayer);
                    }
                }
                doExtraActions(tgtSA,sa);
			}
			else
			{
				removeFromStack(tgtSA,sa);
				doExtraActions(tgtSA,sa);
			}
			
			if(tgtSA.isAbility() && params.containsKey("DestroyPermanent")) {
				AllZone.GameAction.destroy(tgtSACard);
			}
			
		}

		if (af.hasSubAbility()){
			Ability_Sub abSub = sa.getSubAbility();
			if (abSub != null){
				abSub.resolve();
			}
			else{
				String DrawBack = params.get("SubAbility");
				CardFactoryUtil.doDrawBack(DrawBack, 0, source.getController(), source.getController().getOpponent(), source.getController(), source, null, sa);
			}
		}

		//reset tgts
		tgt[0] = null;
		
	}
	
	private void doPowerSink(Player p) {
		//get all lands with mana abilities
		CardList lands = AllZoneUtil.getPlayerLandsInPlay(p);
		lands = lands.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				return c.getManaAbility().size() > 0;
			}
		});
		//tap them
		for(Card c:lands) c.tap();
		
		//empty mana pool
		if(p.isHuman()) AllZone.ManaPool.clearPool();
	}

	private String counterStackDescription(AbilityFactory af, SpellAbility sa){

		StringBuilder sb = new StringBuilder();

		if (!(sa instanceof Ability_Sub))
			sb.append(sa.getSourceCard().getName()).append(" - ");
		else
			sb.append(" ");

		sb.append("countering ");
		sb.append(tgt[0].getSourceCard().getName());
		if(tgt[0].isAbility()) sb.append("'s ability.");
		else sb.append(".");
		
		if(tgt[0].isAbility() && params.containsKey("DestroyPermanent")) {
			sb.append("  Destroy "+tgt[0].getSourceCard()).append(".");
		}

		Ability_Sub abSub = sa.getSubAbility();
		if (abSub != null){
			sb.append(abSub.getStackDescription());
		}

		return sb.toString();
	}//end counterStackDescription

	private boolean counterCanPlay(final AbilityFactory af, final SpellAbility sa) {
		ArrayList<SpellAbility> choosables = new ArrayList<SpellAbility>();

		for(int i = 0; i < AllZone.Stack.size(); i++) {
			choosables.add(AllZone.Stack.peek(i));
		}

		for(int i = 0; i < choosables.size(); i++) {
			if(!matchSpellAbility(sa.getSourceCard(), choosables.get(i), 
					splitTargetingRestrictions, splitSpellTargetRestrictions, targetType)) {
				choosables.remove(i);
			}
		}

		if(tgt[0] == null && choosables.size() > 0 ) AllZone.InputControl.setInput(getInput(sa));

		return choosables.size() > 0;
	}

	private boolean counterCanPlayAI(final AbilityFactory af, final SpellAbility sa){
		boolean toReturn = false;
		if(AllZone.Stack.size() < 1) {
			return false;
		}
		
		SpellAbility topSA = AllZone.Stack.peek();
		if (!CardFactoryUtil.isCounterable(topSA.getSourceCard()))
			return false;
		
		if(matchSpellAbility(sa.getSourceCard(), topSA, splitTargetingRestrictions, splitSpellTargetRestrictions, targetType)) {
			tgt[0] = topSA;
			toReturn = true;
		}
		else
			return false;
		
		Card source = sa.getSourceCard();
		if (unlessCost != null && unlessCost.equals("X") && source.getSVar(unlessCost).equals("Count$xPaid")){
			int xPay = ComputerUtil.determineLeftoverMana(sa);
			if (xPay == 0)	// todo: compare xPay to human's leftover mana
				return false;
			source.setSVar("PayX", Integer.toString(xPay));
		}
		
		Ability_Sub subAb = sa.getSubAbility();
		if (subAb != null)
			toReturn &= subAb.chkAI_Drawback();
		
		return toReturn;
	}
	
	private void removeFromStack(SpellAbility tgtSA,SpellAbility srcSA)
	{
		AllZone.Stack.remove(tgtSA);
		
		if(tgtSA.isAbility())  {
			//For Ability-targeted counterspells - do not move it anywhere, even if Destination$ is specified.
		}
		else if(destination.equals("Graveyard")) {
			AllZone.GameAction.moveToGraveyard(tgtSA.getSourceCard());
		}
		else if(destination.equals("Exile")) {
			AllZone.GameAction.exile(tgtSA.getSourceCard());
		}
		else if(destination.equals("TopOfLibrary")) {
			AllZone.GameAction.moveToLibrary(tgtSA.getSourceCard());
		}
		else if(destination.equals("Hand")) {
			AllZone.GameAction.moveToHand(tgtSA.getSourceCard());
		}
		else if(destination.equals("BottomOfLibrary")) {
			AllZone.GameAction.moveToBottomOfLibrary(tgtSA.getSourceCard());
		}
		else if(destination.equals("ShuffleIntoLibrary")) {
			AllZone.GameAction.moveToBottomOfLibrary(tgtSA.getSourceCard());
			tgtSA.getSourceCard().getController().shuffle();
		}
		else {
			throw new IllegalArgumentException("AbilityFactory_CounterMagic: Invalid Destination argument for card " + srcSA.getSourceCard().getName());
		}		
	}
	
	private void doExtraActions(SpellAbility tgtSA,SpellAbility srcSA)
	{
		for(int ea = 0; ea < splitExtraActions.length; ea++) {
			boolean isOptional = false;

			if(splitExtraActions[0].equals("None")) {
				break;
			}
			String ActionID = splitExtraActions[ea].substring(0,splitExtraActions[ea].indexOf('('));

			Player Target = null;

			String ActionParams = splitExtraActions[ea].substring(splitExtraActions[ea].indexOf('(')+1);
			ActionParams = ActionParams.substring(0,ActionParams.length()-1);

			String[] SplitActionParams = ActionParams.split(",");

			System.out.println("Extra Action: " + ActionID);
			System.out.println("Parameters: " + ActionParams);

			if(ActionID.startsWith("My-")) {
				ActionID = ActionID.substring(3);
				Target = srcSA.getSourceCard().getController();
			}
			else if(ActionID.startsWith("Opp-")) {
				ActionID = ActionID.substring(4);
				Target = srcSA.getSourceCard().getController().getOpponent();
			}
			else if(ActionID.startsWith("CC-")) {
				ActionID = ActionID.substring(3);
				Target = tgtSA.getSourceCard().getController();
			}

			if(ActionID.startsWith("May-")) {
				ActionID = ActionID.substring(4);
				isOptional = true;
			}

			if(ActionID.equals("Draw")) {
				if(isOptional) {
					if(Target == AllZone.HumanPlayer) {
						if(GameActionUtil.showYesNoDialog(srcSA.getSourceCard(), "Do you want to draw " + SplitActionParams[0] + " card(s)?")) {
							Target.drawCards(Integer.parseInt(SplitActionParams[0]));
						}
					}
					else {
						//AI decision-making, only draws a card if it doesn't risk discarding it.
						
						if(AllZoneUtil.getPlayerHand(AllZone.ComputerPlayer).size() + Integer.parseInt(SplitActionParams[0]) < 6) {
							Target.drawCards(Integer.parseInt(SplitActionParams[0]));
						}
					}
				}
				else {
					Target.drawCards(Integer.parseInt(SplitActionParams[0]));
				}

			}
			else if(ActionID.equals("Discard")) {
				if(isOptional) {
					if(Target == AllZone.HumanPlayer) {
						if(GameActionUtil.showYesNoDialog(srcSA.getSourceCard(), "Do you want to discard " + SplitActionParams[0] + " card(s)?")) {
							Target.discard(Integer.parseInt(SplitActionParams[0]), srcSA, true);
						}
					}
					else {
						//AI decisionmaking. Should take Madness cards and the like into account in the future.  Right now always refuses to discard.
					}
				}
				else {
					Target.discard(Integer.parseInt(SplitActionParams[0]), srcSA, true);
				}
			}
			else if(ActionID.equals("LoseLife")) {
				if(isOptional) {
					if(Target == AllZone.HumanPlayer) {
						if(GameActionUtil.showYesNoDialog(srcSA.getSourceCard(), "Do you want to lose " + SplitActionParams[0] + " life?")) {
							Target.loseLife(Integer.parseInt(SplitActionParams[0]), srcSA.getSourceCard());
						}
					}
					else {
						//AI decisionmaking. Not sure why one would ever want to agree to this, except for the rare case of Near-Death Experience+Ali Baba.
					}
				}
				else {
					Target.loseLife(Integer.parseInt(SplitActionParams[0]), srcSA.getSourceCard());
				}

			}
			else if(ActionID.equals("GainLife")) {
				if(isOptional) {
					if(Target == AllZone.HumanPlayer) {
						if(GameActionUtil.showYesNoDialog(srcSA.getSourceCard(), "Do you want to gain" + SplitActionParams[0] + "life?")) {
							Target.gainLife(Integer.parseInt(SplitActionParams[0]), srcSA.getSourceCard());
						}
					}
					else {
						//AI decisionmaking. Not sure why one would ever want to decline this, except for the rare case of Near-Death Experience.
						Target.gainLife(Integer.parseInt(SplitActionParams[0]), srcSA.getSourceCard());
					}
				}
				else {
					Target.gainLife(Integer.parseInt(SplitActionParams[0]), srcSA.getSourceCard());
				}
			}
			else if(ActionID.equals("RevealHand")) {
				if(isOptional) {
					System.out.println(Target);
					if(Target == AllZone.HumanPlayer) {
						if(GameActionUtil.showYesNoDialog(srcSA.getSourceCard(), "Do you want to reveal your hand?")) {
							//Does nothing now, of course, but sometime in the future the AI may be able to remember cards revealed and prioritize discard spells accordingly.
						}
					}
					else {
						//AI decisionmaking. Not sure why one would ever want to agree to this
					}
				}
				else {
					System.out.println(Target);
					if(Target == AllZone.HumanPlayer) {
						//Does nothing now, of course, but sometime in the future the AI may be able to remember cards revealed and prioritize discard spells accordingly.
					}
					else {
						CardList list = AllZoneUtil.getPlayerHand(AllZone.ComputerPlayer);
						GuiUtils.getChoiceOptional("Revealed cards",list.toArray());
					}
				}
			}
			else if(ActionID.equals("RearrangeTopOfLibrary")) {
				if(isOptional) {
					if(Target == AllZone.HumanPlayer) {
						if(GameActionUtil.showYesNoDialog(srcSA.getSourceCard(), "Do you want to rearrange the top " + SplitActionParams[0] + " cards of your library?")) {
							AllZoneUtil.rearrangeTopOfLibrary(srcSA.getSourceCard(), Target, Integer.parseInt(SplitActionParams[0]), false);
						}
					}
					else {
						//AI decisionmaking. AI simply can't atm, and wouldn't know how best to do it anyway.
					}
				}
				else {
					if(Target == AllZone.HumanPlayer) {
						AllZoneUtil.rearrangeTopOfLibrary(srcSA.getSourceCard(), Target, Integer.parseInt(SplitActionParams[0]), false);
					}
					else {
						CardList list = AllZoneUtil.getCardsInZone(Constant.Zone.Hand, AllZone.ComputerPlayer);
						GuiUtils.getChoiceOptional("Revealed cards",list.toArray());
					}
				}
			}
			else {
				throw new IllegalArgumentException("AbilityFactory_CounterMagic: Invalid Extra Action for card " + srcSA.getSourceCard().getName());
			}
		}
	}

	private static boolean matchSpellAbility(Card srcCard, SpellAbility sa, String[] splitRestrictions, String[] splitTargetRestrictions, String targetType) {
		boolean result = true;
		
		if(targetType.equals("Spell")) {
			if(sa.isAbility()) {
				System.out.println(srcCard.getName() + " can only counter spells, not abilities.");
				return false;
			}
		}
		else if(targetType.equals("Ability")) {
			if(sa.isSpell()) {
				System.out.println(srcCard.getName() + " can only counter abilities, not spells.");
				return false;
			}
		}
		else if(targetType.equals("SpellOrAbility")) {
			//Do nothing. This block is only for clarity and enforcing parameters.
		}
		else {
			throw new IllegalArgumentException("Invalid target type for card " + srcCard.getName());
		}
		
		if(splitTargetRestrictions != null)
		{
			result = false;
			if(sa.getTarget() != null)
			{
				for(Object o : sa.getTarget().getTargets())
				{
					if(matchesValid(o,splitTargetRestrictions,srcCard))
					{
						result = true;
						break;
					}
				}
			}
			else
			{
				return false;
			}
		}
		
		if(!matchesValid(sa.getSourceCard(),splitRestrictions,srcCard))
		{
			return false;
		}
		
		return result;
	}//matchSpellAbility
	
	private static boolean matchesValid(Object o,String[] valids,Card srcCard)
	{
		if(o instanceof Card)
		{
			Card c = (Card)o;
			return c.isValidCard(valids, srcCard.getController(), srcCard);
		}
		
		if(o instanceof Player)
		{
			for(String v : valids)
			{
				if(v.equalsIgnoreCase("Player"))
				{
					return true;
				}
				if(v.equalsIgnoreCase("Opponent"))
				{
					if(o.equals(srcCard.getController().getOpponent()))
					{
						return true;
					}
				}
				if(v.equalsIgnoreCase("You"))
				{
					return o.equals(srcCard.getController());
				}
			}
		}
		
		return false;
	}

	private Input getInput(final SpellAbility sa) {
		Input runtime = new Input() {

			private static final long serialVersionUID = 5360660530175041997L;

			@Override
			public void showMessage() {
				ArrayList<SpellAbility> choosables = new ArrayList<SpellAbility>();

				for(int i = 0; i < AllZone.Stack.size(); i++) {
					choosables.add(AllZone.Stack.peek(i));
				}

				for(int i = 0; i < choosables.size(); i++) {
					if(!matchSpellAbility(sa.getSourceCard(), choosables.get(i), splitTargetingRestrictions, splitSpellTargetRestrictions, targetType) || choosables.get(i).getSourceCard().equals(sa.getSourceCard())) {
						choosables.remove(i);
					}
				}
				HashMap<String,SpellAbility> map = new HashMap<String,SpellAbility>();

				for(SpellAbility sa : choosables) {
					map.put(sa.getStackDescription(),sa);
				}

				String[] choices = new String[map.keySet().size()];
				choices = map.keySet().toArray(choices);

				String madeChoice = GuiUtils.getChoice("Select target spell.",choices);

				tgt[0] = map.get(madeChoice);
				System.out.println(tgt[0]);
				stop();
			}//showMessage()
		};//Input
		return runtime;
	}

}
