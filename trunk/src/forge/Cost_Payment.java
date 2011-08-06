package forge;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import forge.gui.GuiUtils;

public class Cost_Payment {
	private Ability_Cost cost = null;
	private SpellAbility ability = null;
	private Card card = null;
	private SpellAbility_Requirements req = null;

	public Ability_Cost getCost() { return cost; }
	public SpellAbility getAbility() { return ability; }
	public Card getCard() { return card; }
	
	public void setRequirements(SpellAbility_Requirements reqs) { req = reqs; } 
	public void setCancel(boolean cancel) { bCancel = cancel; } 
	public boolean isCanceled() { return bCancel; }
		
	// No default values so an error will be kicked if not set properly in constructor
	private boolean payTap;
	private boolean payUntap; 
	private boolean payMana;
	private boolean payXMana;
	private boolean paySubCounter;
	private boolean payAddCounter;
	private boolean paySac;
	private boolean payExile;
	private boolean payExileFromHand;
	private boolean payExileFromGrave;
	private boolean payLife;
	private boolean payDiscard;
	private boolean payTapXType;
	private boolean payReturn;
	
	private boolean bCancel = false;
	private boolean bXDefined = true;
	
	// why are these static? should they be attached to the CostPayment?
	private static CardList payTapXTypeTappedList = new CardList();
	static void addPayTapXTypeTappedList(Card c){
		payTapXTypeTappedList.add(c);
	}

	public void setPayMana(boolean bPay){	payMana = bPay;	}
	public void setPayXMana(boolean bPay){	payXMana = bPay;	}
	public void setPayDiscard(boolean bSac){	payDiscard = bSac;	}
	public void setPaySac(boolean bSac){	paySac = bSac;	}
	public void setPayExile(boolean bExile) { payExile = bExile; }
	public void setPayExileFromHand(boolean bExileFromHand) { payExileFromHand = bExileFromHand; }
	public void setPayExileFromGrave(boolean bExileFromGrave) { payExileFromGrave = bExileFromGrave; }
	public void setPayTapXType(boolean bTapX) { payTapXType = bTapX; }
	public void setPayReturn(boolean bReturn){	payReturn = bReturn; }
	
	public Cost_Payment(Ability_Cost cost, SpellAbility abil){
		this.cost = cost;
		this.ability = abil;
		card = this.ability.getSourceCard();
		payTap = !cost.getTap();
		payUntap = !cost.getUntap();
		payMana = cost.hasNoManaCost();
		payXMana = cost.hasNoXManaCost();
		paySubCounter = !cost.getSubCounter();
		payAddCounter = !cost.getAddCounter();
		paySac = !cost.getSacCost();
		payExile = !cost.getExileCost();
		payExileFromHand = !cost.getExileFromHandCost();
		payExileFromGrave = !cost.getExileFromGraveCost();
		payLife = !cost.getLifeCost();
		payDiscard = !cost.getDiscardCost();
		payTapXType = !cost.getTapXTypeCost();
		payReturn = !cost.getReturnCost();
	}
    
	public static boolean canPayAdditionalCosts(Ability_Cost cost, SpellAbility ability){
		if (cost == null)
			return true;
		
		final Card card = ability.getSourceCard();
    	if (cost.getTap() && (card.isTapped() || card.isSick()))
    		return false;
    	
    	if (cost.getUntap() && (card.isUntapped() || card.isSick()))
    		return false;
    	
		if (cost.getTapXTypeCost()){
			PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
			CardList typeList = new CardList(play.getCards());
			    
			typeList = typeList.getValidCards(cost.getTapXType().split(";"),ability.getActivatingPlayer() ,ability.getSourceCard());
			
			if (cost.getTap()) {
				typeList = typeList.filter(new CardListFilter()
				{
					public boolean addCard(Card c)
					{
						return !c.equals(card) && c.isUntapped();
					}
				});
			}
			if (typeList.size() == 0)
			 	return false;
		}
    	
    	int countersLeft = 0;
    	if (cost.getSubCounter()){
			Counters c = cost.getCounterType();
			countersLeft = card.getCounters(c) - cost.getCounterNum();
			if (countersLeft < 0){
	    		return false;
			}
    	}
    	
    	if (cost.getAddCounter()){
    		// Adding Counters as a cost should always be able to be paid
    	}
    	
    	if (cost.getLifeCost()){
    		if (!card.getController().canPayLife(cost.getLifeAmount())) return false;
    	}
    	
    	if (cost.getDiscardCost()){
    		PlayerZone zone = AllZone.getZone(Constant.Zone.Hand, card.getController());
    		CardList handList = new CardList(zone.getCards());
    		String discType = cost.getDiscardType();
    		int discAmount = cost.getDiscardAmount();
    		
    		if (cost.getDiscardThis()){
    			if (!AllZone.getZone(card).getZoneName().equals(Constant.Zone.Hand))
    				return false;
    		}
    		else if (discType.equals("Hand")){
    			// this will always work
    		}
    		else if(discType.equals("LastDrawn")) {
    			Card c = card.getController().getLastDrawnCard();
    			CardList hand = AllZoneUtil.getPlayerHand(card.getController());
    			return hand.contains(c);
    		}
    		else{
    			if (!discType.equals("Any") && !discType.equals("Random")){
    				String validType[] = discType.split(";");

    				handList = handList.getValidCards(validType,ability.getActivatingPlayer() ,ability.getSourceCard());
    			}
	    		if (discAmount > handList.size()){
	    			// not enough cards in hand to pay
	    			return false;
	    		}
    		}
    	}
    	
		if (cost.getSacCost()){
			if (!cost.getSacThis()){
			    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
			    CardList typeList = new CardList(play.getCards());
			    
			    typeList = typeList.getValidCards(cost.getSacType().split(";"),ability.getActivatingPlayer() ,ability.getSourceCard()); 
				if (typeList.size() < cost.getSacAmount())
					return false;
			}
			else if (!AllZone.GameAction.isCardInPlay(card))
				return false;
		}
		
		if (cost.getExileCost()){
			if (!cost.getExileThis()){
			    CardList typeList = AllZoneUtil.getPlayerCardsInPlay(card.getController());
			    
			    typeList = typeList.getValidCards(cost.getExileType().split(";"),ability.getActivatingPlayer() ,ability.getSourceCard()); 
				if (typeList.size() < cost.getExileAmount())
					return false;
			}
			else if (!AllZone.GameAction.isCardInPlay(card))
				return false;
		}
		
		if (cost.getExileFromHandCost()){
			if (!cost.getExileFromHandThis()){
			    CardList typeList = AllZoneUtil.getPlayerHand(card.getController());
			    
			    typeList = typeList.getValidCards(cost.getExileFromHandType().split(";"),ability.getActivatingPlayer() ,ability.getSourceCard()); 
				if (typeList.size() < cost.getExileFromHandAmount())
					return false;
			}
			else if (!AllZoneUtil.isCardInPlayerHand(card.getController(), card))
				return false;
		}
		
		if (cost.getExileFromGraveCost()){
			if (!cost.getExileFromGraveThis()){
			    CardList typeList = AllZoneUtil.getPlayerGraveyard(card.getController());
			    
			    typeList = typeList.getValidCards(cost.getExileFromGraveType().split(";"),ability.getActivatingPlayer() ,ability.getSourceCard()); 
				if (typeList.size() < cost.getExileFromGraveAmount())
					return false;
			}
			else if (!AllZoneUtil.isCardInPlayerGraveyard(card.getController(), card))
				return false;
		}
		
		if (cost.getReturnCost()){
			if (!cost.getReturnThis()){
			    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
			    CardList typeList = new CardList(play.getCards());
			    
			    typeList = typeList.getValidCards(cost.getReturnType().split(";"),ability.getActivatingPlayer() ,ability.getSourceCard()); 
				if (typeList.size() < cost.getReturnAmount())
					return false;
			}
			else if (!AllZone.GameAction.isCardInPlay(card))
				return false;
		}
    	
    	return true;
    }
	
	public void setInput(Input in){
		AllZone.InputControl.setInput(in, true);
	}
	
	public boolean payCost(){
		if (bCancel){
			req.finishPaying();
			return false;
		}
		
		if (!payTap && cost.getTap()){
			if (card.isUntapped()){
				card.tap();
				payTap = true;
			}
			else
				return false;
		}

		if (!payUntap && cost.getUntap()){
			if (card.isTapped()){
				card.untap();
				payUntap = true;
			}
			else
				return false;
		}
		
		int manaToAdd = 0;
		if (bXDefined && !cost.hasNoXManaCost()){
			// if X cost is a defined value, other than xPaid
			if (!card.getSVar("X").equals("Count$xPaid")){	
				// this currently only works for things about Targeted object
				manaToAdd = AbilityFactory.calculateAmount(card, "X", ability) * cost.getXMana();
				payXMana = true;	// Since the X-cost is being lumped into the mana cost
				payMana = false;
			}
		}
		bXDefined = false;
		
		if (!payMana){		// pay mana here
			setInput(input_payMana(getAbility(), this, manaToAdd));
			return false;
		}
		
		if (!payXMana && !cost.hasNoXManaCost()){		// pay X mana here
			card.setXManaCostPaid(0);
			setInput(input_payXMana(getCost().getXMana(), getAbility(), this));
			return false;
		}
		
		if (!payTapXType && cost.getTapXTypeCost()){
			PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
            CardList typeList = new CardList(play.getCards());
            typeList = typeList.getValidCards(cost.getTapXType().split(";"),ability.getActivatingPlayer() ,ability.getSourceCard());
            
            setInput(input_tapXCost(cost.getTapXTypeAmount(),cost.getTapXType(), typeList, ability, this));
			return false;
		}
		
		if (!paySubCounter && cost.getSubCounter()){	// pay counters here. 
			Counters type = cost.getCounterType();
			if (card.getCounters(type) >= cost.getCounterNum()){
				card.subtractCounter(type, cost.getCounterNum());
				paySubCounter = true;
			}
			else{
				bCancel = true;
				req.finishPaying();
				return false;
			}
		}
		
		if (!payAddCounter && cost.getAddCounter()){	// add counters here.
			card.addCounterFromNonEffect(cost.getCounterType(), cost.getCounterNum());
			payAddCounter = true;
		}
		
		if (!payLife && cost.getLifeCost()){			// pay life here
			StringBuilder sb = new StringBuilder();
			sb.append(getCard().getName());
			sb.append(" - Pay ");
			sb.append(cost.getLifeAmount());
			sb.append(" Life?");
			Object[] possibleValues = {"Yes", "No"};
        	Object choice = JOptionPane.showOptionDialog(null, sb.toString(), getCard().getName() + " - Cost",  
        			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
        			null, possibleValues, possibleValues[0]);
            if(choice.equals(0)) {
            	  AllZone.HumanPlayer.payLife(cost.getLifeAmount(), null);
            	  payLife = true;
            }
			else{
				bCancel = true;
				req.finishPaying();
				return false;
			}
		}
		
		if (!payDiscard && cost.getDiscardCost()){			// discard here
    		PlayerZone zone = AllZone.getZone(Constant.Zone.Hand, card.getController());
    		CardList handList = new CardList(zone.getCards());
    		String discType = cost.getDiscardType();
    		int discAmount = cost.getDiscardAmount();
    		
    		if (cost.getDiscardThis()){
    			card.getController().discard(card, ability);
    			payDiscard = true;
    		}
    		else if (discType.equals("Hand")){
    			card.getController().discardHand(ability);
    			payDiscard = true;
    		}
    		else if( discType.equals("LastDrawn") ) {
    			if(handList.contains(card.getController().getLastDrawnCard())) {
    				card.getController().discard(card.getController().getLastDrawnCard(), ability);
    				payDiscard = true;
    			}
    			
    		}
    		else{
    			if (discType.equals("Random")){
    				card.getController().discardRandom(discAmount, ability);
    				payDiscard = true;
    			}
    			else{
	    			if (!discType.equals("Any")){
	    				String validType[] = discType.split(";");
	    				handList = handList.getValidCards(validType,ability.getActivatingPlayer() ,ability.getSourceCard());
	    			}
	    			setInput(input_discardCost(discAmount, discType, handList, ability, this));
	    			return false;
    			}
    		}
		}
		
		if (!paySac && cost.getSacCost()){					// sacrifice stuff here
    		if (cost.getSacThis())
    			setInput(sacrificeThis(ability, this));
    		else
    			setInput(sacrificeType(ability, cost.getSacType(), this));
    		return false;
    	}
		
		if (!payExile && cost.getExileCost()){					// exile stuff here
    		if (cost.getExileThis())
    			setInput(exileThis(ability, this));
    		else
    			setInput(exileType(ability, cost.getExileType(), this));
    		return false;
    	}
		
		if (!payExileFromHand && cost.getExileFromHandCost()){					// exile stuff here
    		if (cost.getExileFromHandThis())
    			setInput(exileFromHandThis(ability, this));
    		else
    			setInput(exileFromHandType(ability, cost.getExileFromHandType(), this));
    		return false;
    	}
		
		if (!payExileFromGrave && cost.getExileFromGraveCost()){					// exile stuff here
    		if (cost.getExileFromGraveThis())
    			setInput(exileFromGraveThis(ability, this));
    		else
    			setInput(exileFromGraveType(ability, cost.getExileFromGraveType(), this));
    		return false;
    	}
		
		if (!payReturn && cost.getReturnCost()){					// return stuff here
    		if (cost.getReturnThis())
    			setInput(returnThis(ability, this));
    		else
    			setInput(returnType(ability, cost.getReturnType(), this));
    		return false;
    	}

		resetUndoList();
		req.finishPaying();
		return true;
	}

	public boolean isAllPaid(){
		// if you add a new Cost type add it here
		return (payTap && payUntap && payMana && payXMana && paySubCounter && payAddCounter &&
				paySac && payExile && payLife && payDiscard && payTapXType && payReturn &&
				payExileFromHand && payExileFromGrave);
	}
	
	public void resetUndoList(){
		// todo: clear other undoLists here?
		payTapXTypeTappedList.clear();
	}
	
	public void cancelPayment(){
		// unpay anything we can.
		if (cost.getTap() && payTap){
			// untap if tapped
			card.untap();
		}
		if (cost.getUntap() && payUntap){
			// tap if untapped
			card.tap();
		}
		// refund mana
        AllZone.ManaPool.unpaid(ability, false);
        
		if (cost.getTapXTypeCost()){ // Can't depend on payTapXType if canceling before tapping enough

			for (Card c:payTapXTypeTappedList)
				c.untap();	
			//needed?
			payTapXTypeTappedList.clear();
		}
        
        // refund counters
        if (cost.getSubCounter() && paySubCounter){
			card.addCounterFromNonEffect(cost.getCounterType(), cost.getCounterNum());
        }
        
        // remove added counters
        if (cost.getAddCounter() && payAddCounter){
			card.subtractCounter(cost.getCounterType(), cost.getCounterNum());
        }
        
        // refund life
        if (cost.getLifeCost() && payLife){
        	card.getController().payLife(cost.getLifeAmount()*-1, null);
        }
        
        // can't really undiscard things
        
		// can't really unsacrifice things
        
        //can't really unexile things
        
        // can't really unexile things from hand
        
        // can't really unreturn things
	}
    
    public void payComputerCosts(){
    	// ******** NOTE for Adding Costs ************
    	// make sure ComputerUtil.canPayAdditionalCosts() is updated so the AI knows if they can Pay the cost
    	ArrayList<Card> sacCard = new ArrayList<Card>();
    	ArrayList<Card> exileCard = new ArrayList<Card>();
    	CardList exileFromGraveCard = new CardList();
    	ArrayList<Card> tapXCard = new ArrayList<Card>();
    	ArrayList<Card> returnCard = new ArrayList<Card>();
    	ability.setActivatingPlayer(AllZone.ComputerPlayer);
    	
    	// double check if something can be sacrificed here. Real check is in ComputerUtil.canPayAdditionalCosts()
    	if (cost.getSacCost()){
    		if (cost.getSacThis())
    			sacCard.add(card);
    		else{
    			for(int i = 0; i < cost.getSacAmount(); i++)
    				sacCard.add(ComputerUtil.chooseSacrificeType(cost.getSacType(), card, ability.getTargetCard()));
    		}
    		
	    	if (sacCard.size() != cost.getSacAmount()){
	    		System.out.println("Couldn't find a valid card to sacrifice for: "+card.getName());
	    		return;
	    	}
    	}
    	
    	// double check if something can be exiled here. Real check is in ComputerUtil.canPayAdditionalCosts()
    	if (cost.getExileCost()){
    		if (cost.getExileThis())
    			exileCard.add(card);
    		else{
    			for(int i = 0; i < cost.getExileAmount(); i++)
    				exileCard.add(ComputerUtil.chooseExileType(cost.getExileType(), card, ability.getTargetCard()));
    		}
    		
	    	if (exileCard.size() != cost.getExileAmount()){
	    		System.out.println("Couldn't find a valid card to exile for: "+card.getName());
	    		return;
	    	}
    	}
    	
    	if (cost.getExileFromGraveCost()){
    		if (cost.getExileFromGraveThis())
    			exileFromGraveCard.add(card);
    		else{
    			exileFromGraveCard = ComputerUtil.chooseExileFromGraveType(
    					cost.getExileFromGraveType(), card, ability.getTargetCard(),cost.getExileFromGraveAmount());
    		}
    		
	    	if (exileFromGraveCard.size() != cost.getExileFromGraveAmount()){
	    		System.out.println("Couldn't find a valid card to exile for: "+card.getName());
	    		return;
	    	}
    	}
    	
    	if (cost.getReturnCost()){
    		if (cost.getReturnThis())
    			returnCard.add(card);
    		else{
    			for(int i = 0; i < cost.getReturnAmount(); i++)
    				returnCard.add(ComputerUtil.chooseReturnType(cost.getReturnType(), card, ability.getTargetCard()));
    		}
    		
	    	if (returnCard.size() != cost.getReturnAmount()){
	    		System.out.println("Couldn't find a valid card to return for: "+card.getName());
	    		return;
	    	}
    	}
    	
    	if (cost.getDiscardThis()){
    		if(!AllZoneUtil.getPlayerHand(card.getController()).contains(card.getController().getLastDrawnCard())) {
    			return;
    		}
			if (!AllZone.getZone(card).getZoneName().equals(Constant.Zone.Hand))
				return;
    	}
    	
    	if (cost.getTapXTypeCost()) {
    		boolean tap = cost.getTap();
    		
    		for(int i = 0; i < cost.getTapXTypeAmount(); i++)
    			tapXCard.add(ComputerUtil.chooseTapType(cost.getTapXType(), card, tap, i));
    		
    		if (tapXCard.size() != cost.getTapXTypeAmount()){
	    		System.out.println("Couldn't find a valid card to tap for: "+card.getName());
	    		return;
	    	}
    	}
    	
    	// double check if counters available? Real check is in ComputerUtil.canPayAdditionalCosts()
    	if (cost.getSubCounter() && cost.getCounterNum() > card.getCounters(cost.getCounterType())){
    		System.out.println("Not enough " + cost.getCounterType() + " on " + card.getName());
    		return;
    	}
    	
    	if (cost.getTap())
    		card.tap();

    	if (cost.getUntap())
    		card.untap();
    	
    	if (!cost.hasNoManaCost())
    		ComputerUtil.payManaCost(ability);
    	
		if (cost.getTapXTypeCost()){
			for (Card c : tapXCard)
				c.tap();
		}
    	
    	if (cost.getSubCounter())
    		card.subtractCounter(cost.getCounterType(), cost.getCounterNum());
    	
    	if (cost.getAddCounter()){
			card.addCounterFromNonEffect(cost.getCounterType(), cost.getCounterNum());
    	}
    	
    	if (cost.getLifeCost())
    		AllZone.ComputerPlayer.payLife(cost.getLifeAmount(), null);
    	
    	if (cost.getDiscardCost()){
    		String discType = cost.getDiscardType();
    		int discAmount = cost.getDiscardAmount();
    		
    		if (cost.getDiscardThis()){
    			card.getController().discard(card, ability);
    		}
    		else if (discType.equals("Hand")){
    			card.getController().discardHand(ability);
    		}
    		else{
    			if (discType.equals("Random")){
    				card.getController().discardRandom(discAmount, ability);
    			}
    			else{
	    			if (!discType.equals("Any")){
	    				String validType[] = discType.split(";");
	    				AllZone.GameAction.AI_discardNumType(discAmount, validType, ability);
	    			}
	    			else{
	    				AllZone.ComputerPlayer.discard(discAmount, ability, false);
	    			}
    			}
    		}
    	}
    	
		if (cost.getSacCost()){
			for(Card c : sacCard)
				AllZone.GameAction.sacrifice(c);
		}
		
		if (cost.getExileCost()){
			for(Card c : exileCard)
				AllZone.GameAction.exile(c);
		}
		
		if (cost.getExileFromGraveCost()){
			for(Card c : exileFromGraveCard)
				AllZone.GameAction.exile(c);
		}
		
		if (cost.getReturnCost()){
			for(Card c : returnCard)
				AllZone.GameAction.moveToHand(c);
		}

		AllZone.Stack.addAndUnfreeze(ability);
    }
    
	public void changeCost(){
		cost.changeCost(ability);
	}
	

	// ******************************************************************************
	// *********** Inputs used by Cost_Payment below here ***************************
	// ******************************************************************************
	
	public static Input input_payMana(final SpellAbility sa, final Cost_Payment payment, int manaToAdd){
		final ManaCost manaCost;
	    
        if(Phase.GameBegins == 1)  {
        	if(sa.getSourceCard().isCopiedSpell() && sa.isSpell()) {
                manaCost = new ManaCost("0"); 
        	} else {
    		    String mana = payment.getCost().getMana().replace("X", "").trim();
        		manaCost = new ManaCost(mana);
        		manaCost.increaseColorlessMana(manaToAdd);
        	}
        }
        else
        {
        	manaCost = new ManaCost(sa.getManaCost());
        }
        
		Input payMana = new Input(){
			private ManaCost            mana = manaCost;
		    private static final long  serialVersionUID = 3467312982164195091L;
		    
		    private final String       originalManaCost = payment.getCost().getMana();
	        
		    private void resetManaCost() {
		    	mana = new ManaCost(originalManaCost);
		    }
		    
		    @Override
		    public void selectCard(Card card, PlayerZone zone) {
		    	// prevent cards from tapping themselves if ability is a tapability, although it should already be tapped
		        if(sa.getSourceCard().equals(card) && sa.isTapAbility()) {
		            return;
		        }

		        mana = Input_PayManaCostUtil.activateManaAbility(sa, card, mana);
		        
		        if(mana.isPaid()) 
		        	done();
		        else
			        if (AllZone.InputControl.getInput() == this)
			        	showMessage();
		    }
		    
		    private void done() {
		    	resetManaCost();
		    	payment.setPayMana(true);
		    	stop();
		    	payment.payCost();
		    }
		    
		    @Override
		    public void selectButtonCancel() {
		        resetManaCost();
		        payment.setCancel(true);
		        payment.payCost();
		        AllZone.Human_Battlefield.updateObservers();//DO NOT REMOVE THIS, otherwise the cards don't always tap
		        stop();
		    }
		    
		    @Override
		    public void showMessage() {
		        ButtonUtil.enableOnlyCancel();
		        String displayMana = mana.toString().replace("X", "").trim();
		        AllZone.Display.showMessage("Pay Mana Cost: " + displayMana);
		        if(mana.isPaid()) 
		        	done(); 
		    }
		};
	    return payMana;
	}

	public static Input input_payXMana(final int numX, final SpellAbility sa, final Cost_Payment payment){
		Input payX = new Input(){
			private static final long serialVersionUID = -6900234444347364050L;
			int 					xPaid = 0;
			ManaCost 				manaCost = new ManaCost(Integer.toString(numX));
			
		    @Override
		    public void showMessage() {
		    	if (manaCost.toString().equals(Integer.toString(numX))) // Can only cancel if partially paid an X value
		    		ButtonUtil.enableAll();
		    	else
		    		ButtonUtil.enableOnlyCancel();
		    	
		        AllZone.Display.showMessage("Pay X Mana Cost for " + sa.getSourceCard().getName()+"\n"+xPaid+ " Paid so far.");
		    }
		    
		    // selectCard 
		    @Override
		    public void selectCard(Card card, PlayerZone zone) {
		        if(sa.getSourceCard().equals(card) && sa.isTapAbility()) {
		        	// this really shouldn't happen but just in case
		            return;
		        }

		        manaCost = Input_PayManaCostUtil.activateManaAbility(sa, card, manaCost);
		        if(manaCost.isPaid()){
		        	manaCost = new ManaCost(Integer.toString(numX));
		        	xPaid++;
		        }
		        
		        if (AllZone.InputControl.getInput() == this)
		        	showMessage();
		    }
		    
		    @Override
		    public void selectButtonCancel() {
		        payment.setCancel(true);
		        payment.payCost();
		        AllZone.Human_Battlefield.updateObservers();//DO NOT REMOVE THIS, otherwise the cards don't always tap
		        stop();
		    }
		    
		    @Override
		    public void selectButtonOK() {
		    	payment.setPayXMana(true);
		    	payment.getCard().setXManaCostPaid(xPaid);
		    	stop();
		    	payment.payCost();
		    }
			
		};

		return payX;
	}
	
    
    public static Input input_discardCost(final int nCards, final String discType, final CardList handList, SpellAbility sa, final Cost_Payment payment) {
        final SpellAbility sp = sa;
    	Input target = new Input() {
            private static final long serialVersionUID = -329993322080934435L;
            
            int                       nDiscard                = 0;
            
            @Override
            public void showMessage() {
            	boolean any = discType.equals("Any") ? true : false;
            	if (AllZone.Human_Hand.getCards().length == 0) stop();
            	StringBuilder type = new StringBuilder("");
            	if (any || !discType.equals("Card")){
            		type.append(" ").append(discType);
            	}
            	StringBuilder sb = new StringBuilder();
            	sb.append("Select ");
            	if(any) {
            		sb.append("any ");
            	}
            	else {
            		sb.append("a ").append(type.toString()).append(" ");
            	}
            	sb.append("card to discard.");
            	if(nCards > 1) {
            		sb.append(" You have ");
            		sb.append(nCards - nDiscard);
            		sb.append(" remaining.");
            	}
                AllZone.Display.showMessage(sb.toString());
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	cancel();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(zone.is(Constant.Zone.Hand) && handList.contains(card) ) {
                	// send in CardList for Typing
                	card.getController().discard(card, sp);
                    handList.remove(card);
                    nDiscard++;
                    
                    //in case no more cards in hand
                    if(nDiscard == nCards) 
                    	done();
                    else if (AllZone.Human_Hand.getCards().length == 0)	// this really shouldn't happen
                    	cancel();
                    else
                    	showMessage();
                }
            }
            
            public void cancel(){
            	payment.setCancel(true);
            	stop();
            	payment.payCost();
            }
            
            public void done(){
            	payment.setPayDiscard(true);
            	stop();
            	payment.payCost();
            }
        };

        return target;
    }//input_discard() 
	
    public static Input sacrificeThis(final SpellAbility sa, final Cost_Payment payment) {
        Input target = new Input() {
            private static final long serialVersionUID = 2685832214519141903L;
            
            @Override
            public void showMessage() {
            	Card card = sa.getSourceCard();
                if(card.getController().equals(AllZone.HumanPlayer) && AllZone.GameAction.isCardInPlay(card)) {
        			StringBuilder sb = new StringBuilder();
        			sb.append(card.getName());
        			sb.append(" - Sacrifice?");
        			Object[] possibleValues = {"Yes", "No"};
                	Object choice = JOptionPane.showOptionDialog(null, sb.toString(), card.getName() + " - Cost",  
                			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                			null, possibleValues, possibleValues[0]);
                    if(choice.equals(0)) {
                    	payment.setPaySac(true);
                    	payment.getAbility().addSacrificedCost(card);
                    	AllZone.GameAction.sacrifice(card);
                    	stop();
                    	payment.payCost();
                    }
                    else{
                    	payment.setCancel(true);
                    	stop();
                    	payment.payCost();
                    }
                }
            }
        };
        
        return target;
    }//input_sacrifice()
    
    public static Input sacrificeType(final SpellAbility sa, final String type, final Cost_Payment payment){
        Input target = new Input() {
            private static final long serialVersionUID = 2685832214519141903L;
            private CardList typeList;
            private int nSacrifices = 0;
            private int nNeeded = payment.getCost().getSacAmount();
            
            @Override
            public void showMessage() {
            	StringBuilder msg = new StringBuilder("Sacrifice ");
            	int nLeft = nNeeded - nSacrifices;
            	msg.append(nLeft).append(" ");
            	msg.append(type);
            	if (nLeft > 1){
            		msg.append("s");
            	}
            	
                PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, sa.getSourceCard().getController());
                typeList = new CardList(play.getCards());
                typeList = typeList.getValidCards(type.split(";"),sa.getActivatingPlayer(), sa.getSourceCard());
                AllZone.Display.showMessage(msg.toString());
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	cancel();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(typeList.contains(card)) {
                	nSacrifices++;
                	payment.getAbility().addSacrificedCost(card);
                	AllZone.GameAction.sacrifice(card);
                	typeList.remove(card);
                    //in case nothing else to sacrifice
                    if(nSacrifices == nNeeded) 
                    	done();
                    else if (typeList.size() == 0)	// this really shouldn't happen
                    	cancel();
                    else
                    	showMessage();
                }
            }
            
            public void done(){
            	payment.setPaySac(true);
            	stop();
            	payment.payCost();
            }
            
            public void cancel(){
            	payment.setCancel(true);
            	stop();
            	payment.payCost();
            }
        };

        return target;
    }//sacrificeType()
    
    public static Input exileThis(final SpellAbility sa, final Cost_Payment payment) {
        Input target = new Input() {
			private static final long serialVersionUID = 678668673002725001L;

			@Override
            public void showMessage() {
            	Card card = sa.getSourceCard();
                if(card.getController().equals(AllZone.HumanPlayer) && AllZone.GameAction.isCardInPlay(card)) {
        			StringBuilder sb = new StringBuilder();
        			sb.append(card.getName());
        			sb.append(" - Exile?");
        			Object[] possibleValues = {"Yes", "No"};
                	Object choice = JOptionPane.showOptionDialog(null, sb.toString(), card.getName() + " - Cost",  
                			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                			null, possibleValues, possibleValues[0]);
                    if(choice.equals(0)) {
                    	payment.setPayExile(true);
                    	AllZone.GameAction.exile(card);
                    	stop();
                    	payment.payCost();
                    }
                    else{
                    	payment.setCancel(true);
                    	stop();
                    	payment.payCost();
                    }
                }
            }
        };

        return target;
    }//input_exile()
    
    public static Input exileFromHandThis(final SpellAbility spell, final Cost_Payment payment) {
        Input target = new Input() {
			private static final long serialVersionUID = 2651542083913697972L;

			@Override
            public void showMessage() {
            	Card card = spell.getSourceCard();
                if(card.getController().equals(AllZone.HumanPlayer) && AllZoneUtil.isCardInPlayerHand(card.getController(), card)) {
        			StringBuilder sb = new StringBuilder();
        			sb.append(card.getName());
        			sb.append(" - Exile?");
        			Object[] possibleValues = {"Yes", "No"};
                	Object choice = JOptionPane.showOptionDialog(null, sb.toString(), card.getName() + " - Cost",  
                			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                			null, possibleValues, possibleValues[0]);
                    if(choice.equals(0)) {
                    	payment.setPayExileFromHand(true);
                    	AllZone.GameAction.exile(card);
                    	stop();
                    	payment.payCost();
                    }
                    else{
                    	payment.setCancel(true);
                    	stop();
                    	payment.payCost();
                    }
                }
            }
        };
        return target;
    }//input_exile()
    
    public static Input exileFromGraveThis(final SpellAbility spell, final Cost_Payment payment) {
        Input target = new Input() {
			private static final long serialVersionUID = 6237561876518762902L;

			@Override
            public void showMessage() {
            	Card card = spell.getSourceCard();
                if(card.getController().equals(AllZone.HumanPlayer) && AllZoneUtil.isCardInPlayerGraveyard(card.getController(), card)) {
        			StringBuilder sb = new StringBuilder();
        			sb.append(card.getName());
        			sb.append(" - Exile?");
        			Object[] possibleValues = {"Yes", "No"};
                	Object choice = JOptionPane.showOptionDialog(null, sb.toString(), card.getName() + " - Cost",  
                			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                			null, possibleValues, possibleValues[0]);
                    if(choice.equals(0)) {
                    	payment.setPayExileFromGrave(true);
                    	AllZone.GameAction.exile(card);
                    	stop();
                    	payment.payCost();
                    }
                    else{
                    	payment.setCancel(true);
                    	stop();
                    	payment.payCost();
                    }
                }
            }
        };
        return target;
    }//input_exile()
    
    public static Input exileType(final SpellAbility sa, final String type, final Cost_Payment payment){
        Input target = new Input() {
			private static final long serialVersionUID = 1403915758082824694L;
			
			private CardList typeList;
            private int nExiles = 0;
            private int nNeeded = payment.getCost().getExileAmount();
            
            @Override
            public void showMessage() {
            	StringBuilder msg = new StringBuilder("Exile ");
            	int nLeft = nNeeded - nExiles;
            	msg.append(nLeft).append(" ");
            	msg.append(type);
            	if (nLeft > 1){
            		msg.append("s");
            	}
            	
                PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, sa.getSourceCard().getController());
                typeList = new CardList(play.getCards());
                typeList = typeList.getValidCards(type.split(";"), sa.getActivatingPlayer(), sa.getSourceCard());
                AllZone.Display.showMessage(msg.toString());
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	cancel();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(typeList.contains(card)) {
                	nExiles++;
                	AllZone.GameAction.exile(card);
                	typeList.remove(card);
                    //in case nothing else to exile
                    if(nExiles == nNeeded) 
                    	done();
                    else if (typeList.size() == 0)	// this really shouldn't happen
                    	cancel();
                    else
                    	showMessage();
                }
            }
            
            public void done(){
            	payment.setPayExile(true);
            	stop();
            	payment.payCost();
            }
            
            public void cancel(){
            	payment.setCancel(true);
            	stop();
            	payment.payCost();
            }
        };

        return target;
    }//exileType()
    
    public static Input exileFromHandType(final SpellAbility spell, final String type, final Cost_Payment payment){
        Input target = new Input() {
			private static final long serialVersionUID = 759041801001973859L;
			private CardList typeList;
            private int nExiles = 0;
            private int nNeeded = payment.getCost().getExileFromHandAmount();
            
            @Override
            public void showMessage() {
            	StringBuilder msg = new StringBuilder("Exile ");
            	int nLeft = nNeeded - nExiles;
            	msg.append(nLeft).append(" ");
            	msg.append(type);
            	if (nLeft > 1){
            		msg.append("s");
            	}
            	msg.append(" from your hand");
            	
                PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, spell.getSourceCard().getController());
                typeList = new CardList(hand.getCards());
                typeList = typeList.getValidCards(type.split(";"),spell.getActivatingPlayer() ,spell.getSourceCard());
                AllZone.Display.showMessage(msg.toString());
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	cancel();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(typeList.contains(card)) {
                	nExiles++;
                	AllZone.GameAction.exile(card);
                	typeList.remove(card);
                    //in case nothing else to exile
                    if(nExiles == nNeeded) 
                    	done();
                    else if (typeList.size() == 0)	// this really shouldn't happen
                    	cancel();
                    else
                    	showMessage();
                }
            }
            
            public void done(){
            	payment.setPayExileFromHand(true);
            	stop();
            	payment.payCost();
            }
            
            public void cancel(){
            	payment.setCancel(true);
            	stop();
            	payment.payCost();
            }
        };
        return target;
    }//exileFromHandType()
    
    public static Input exileFromGraveType(final SpellAbility spell, final String type, final Cost_Payment payment){
        Input target = new Input() {
			private static final long serialVersionUID = 734256837615635021L;

			@Override
            public void showMessage() {
            	//Card card = spell.getSourceCard();
        		CardList typeList;
                int nNeeded = payment.getCost().getExileFromGraveAmount();
                PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, spell.getSourceCard().getController());
                typeList = new CardList(grave.getCards());
                typeList = typeList.getValidCards(type.split(";"),spell.getActivatingPlayer() ,spell.getSourceCard());
                
                for (int i=0; i < nNeeded; i++) {
                    if (typeList.size() == 0) 
                    	cancel();
                        
                    Object o = GuiUtils.getChoiceOptional("Select a card", typeList.toArray());
                    
                    if (o != null) {
                        Card c = (Card) o;
                        typeList.remove(c);
                    	AllZone.GameAction.exile(c);
                    	if (i == nNeeded-1) done();
                    }
                }
			}
			
            @Override
            public void selectButtonCancel() {
            	cancel();
            }
			
            public void done(){
            	payment.setPayExileFromGrave(true);
            	stop();
            	payment.payCost();
            }
            
            public void cancel(){
            	payment.setCancel(true);
            	stop();
            	payment.payCost();
            }
        };
        return target;
    }//exileFromGraveType()
    
    public static Input input_tapXCost(final int nCards, final String cardType, final CardList cardList, SpellAbility sa, final Cost_Payment payment) {
        //final SpellAbility sp = sa;
    	Input target = new Input() {

			private static final long serialVersionUID = 6438988130447851042L;
			int                       nTapped                = 0;
            
            @Override
            public void showMessage() {
            	if (cardList.size() == 0) stop();
            	
            	int left = nCards - nTapped;
                AllZone.Display.showMessage("Select a "+ cardType + " to tap (" +left + " left)");
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	cancel();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(zone.is(Constant.Zone.Battlefield) && cardList.contains(card) && card.isUntapped() ) {
                	// send in CardList for Typing
                    card.tap();
                    payTapXTypeTappedList.add(card);
                    cardList.remove(card);
                    nTapped++;
                    
                    if(nTapped == nCards) 
                    	done();
                    else if (cardList.size() == 0)	// this really shouldn't happen
                    	cancel();
                    else
                    	showMessage();
                }
            }
            
            public void cancel(){
            	payment.setCancel(true);
            	stop();
            	payment.payCost();
            }
            
            public void done(){
            	payment.setPayTapXType(true);
            	stop();
            	payment.payCost();
            }
        };

        return target;
    }//input_tapXCost() 
    
    public static Input returnThis(final SpellAbility sa, final Cost_Payment payment) {
        Input target = new Input() {
            private static final long serialVersionUID = 2685832214519141903L;
            
            @Override
            public void showMessage() {
            	Card card = sa.getSourceCard();
                if(card.getController().equals(AllZone.HumanPlayer) && AllZone.GameAction.isCardInPlay(card)) {
        			StringBuilder sb = new StringBuilder();
        			sb.append(card.getName());
        			sb.append(" - Return to Hand?");
        			Object[] possibleValues = {"Yes", "No"};
                	Object choice = JOptionPane.showOptionDialog(null, sb.toString(), card.getName() + " - Cost",  
                			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                			null, possibleValues, possibleValues[0]);
                    if(choice.equals(0)) {
                    	payment.setPayReturn(true);
                    	AllZone.GameAction.moveToHand(card);
                    	stop();
                    	payment.payCost();
                    }
                    else{
                    	payment.setCancel(true);
                    	stop();
                    	payment.payCost();
                    }
                }
            }
        };

        return target;
    }//input_sacrifice()
    
    public static Input returnType(final SpellAbility sa, final String type, final Cost_Payment payment){
        Input target = new Input() {
            private static final long serialVersionUID = 2685832214519141903L;
            private CardList typeList;
            private int nReturns = 0;
            private int nNeeded = payment.getCost().getReturnAmount();
            
            @Override
            public void showMessage() {
            	StringBuilder msg = new StringBuilder("Return ");
            	int nLeft = nNeeded - nReturns;
            	msg.append(nLeft).append(" ");
            	msg.append(type);
            	if (nLeft > 1){
            		msg.append("s");
            	}
            	
                PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, sa.getSourceCard().getController());
                typeList = new CardList(play.getCards());
                typeList = typeList.getValidCards(type.split(";"), sa.getActivatingPlayer(), sa.getSourceCard());
                AllZone.Display.showMessage(msg.toString());
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	cancel();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(typeList.contains(card)) {
                	nReturns++;
                	AllZone.GameAction.moveToHand(card);
                	typeList.remove(card);
                    //in case nothing else to return
                    if(nReturns == nNeeded) 
                    	done();
                    else if (typeList.size() == 0)	// this really shouldn't happen
                    	cancel();
                    else
                    	showMessage();
                }
            }
            
            public void done(){
            	payment.setPayReturn(true);
            	stop();
            	payment.payCost();
            }
            
            public void cancel(){
            	payment.setCancel(true);
            	stop();
            	payment.payCost();
            }
        };

        return target;
    }//returnType()  
}
