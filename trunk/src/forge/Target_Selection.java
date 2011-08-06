package forge;

public class Target_Selection {
	private Target target = null;
	private SpellAbility ability = null;
	private Card card = null;

	public Target getTgt() { return target; }
	public SpellAbility getAbility() { return ability; }
	public Card getCard() { return card; }
	
	private SpellAbility_Requirements req = null;
	public void setRequirements(SpellAbility_Requirements reqs) { req = reqs; } 
	
	private boolean bCancel = false;
	public void setCancel(boolean done) { bCancel = done; }
	public boolean isCanceled() { return bCancel; }
	private boolean bDoneTarget = false;
	public void setDoneTarget(boolean done) { bDoneTarget = done; } 
	
	final private Input changeInput = new Input() {
		private static final long serialVersionUID = -5750122411788688459L; };
	
	public Target_Selection(Target tgt, SpellAbility sa){
		target = tgt;
		ability = sa;
		card = sa.getSourceCard();
	}
	
	public boolean doesTarget(){
		if (target == null)
			return false;
		return target.doesTarget();
	}
	
	public void resetTargets(){
		if (target != null)
			target.resetTargets();
	}
	
	public void incrementTargets(){
		if (target != null)
			target.incrementTargets();
	}
	
	public boolean chooseTargets(){
		// if not enough targets chosen, reset and cancel Ability
		if (bCancel || bDoneTarget && target.getNumTargeted() < target.getMinTargets()){
			bCancel = true;
			target.resetTargets();
			return false;
		}
		
		// if we haven't reached minimum targets, or we're still less than Max targets keep choosing
		// targeting, with forward code for multiple target abilities 
		if (!bDoneTarget && target.getMinTargets() > 0 && target.getNumTargeted() < target.getMaxTargets()){
			if (target.canTgtCreature() && target.canTgtPlayer())
				changeInput.stopSetNext(targetCreaturePlayer(ability, Command.Blank, true, this, req));
			else if(target.canTgtCreature()) 
				changeInput.stopSetNext(targetCreature(ability, this, req));
	        else if(target.canTgtPlayer()) 
	        	changeInput.stopSetNext(targetPlayer(ability, this, req));
	        else if (target.canTgtValid())
	        	changeInput.stopSetNext(input_targetValid(ability, target.getValidTgts(), target.getVTSelection(), this, req));
	        return false;
		}
		
		return true;
	}
	
    public static Input targetCreaturePlayer(final SpellAbility ability, final Command paid, final boolean targeted, 
    		final Target_Selection select, final SpellAbility_Requirements req) {
        Input target = new Input() {
            private static final long serialVersionUID = 2781418414287281005L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage("Select target Creature, Player, or Planeswalker");
                // when multi targets (Arc Mage) are added, need this: 
                // if payment.targeted < mintarget only enable cancel
                // else if payment.targeted < maxtarget enable cancel and ok
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	select.setCancel(true);
                stop();
                req.finishedTargeting();
            }
            
            @Override     
            public void selectButtonOK() {
            	select.setDoneTarget(true);
                stop();
                req.finishedTargeting();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if((card.isCreature() || card.isPlaneswalker()) && zone.is(Constant.Zone.Play)
                        && (!targeted || CardFactoryUtil.canTarget(ability, card))) {
                    ability.setTargetCard(card);
                    done();
                }
            }//selectCard()
            
            @Override
            public void selectPlayer(String player) {
                ability.setTargetPlayer(player);
                // if multitarget increment then select again
                done();
            }
            
            void done() {
            	select.incrementTargets();
                paid.execute();
                stop();
                req.finishedTargeting();
            }
        };
        return target;
    }//input_targetCreaturePlayer()
    
	public static Input targetCreature(final SpellAbility ability, final Target_Selection select, final SpellAbility_Requirements req) {
        Input target = new Input() {
            private static final long serialVersionUID = 2781418414287281005L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage("Select target Creature");
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	select.setCancel(true);
                stop();
                req.finishedTargeting();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(card.isCreature() && zone.is(Constant.Zone.Play) && (CardFactoryUtil.canTarget(ability, card))) {
                    ability.setTargetCard(card);
                    done();
                }
            }//selectCard()
            
            void done() {
            	select.incrementTargets();
            	stop();
            	req.finishedTargeting();
            }
        };
        return target;
    }//targetCreature()
    
    public static Input targetPlayer(final SpellAbility ability, final Target_Selection select, final SpellAbility_Requirements req) {
        Input target = new Input() {
            private static final long serialVersionUID = 2781418414287281005L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage("Select target Player or Planeswalker");
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	select.setCancel(true);
                stop();
                req.finishedTargeting();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(card.isPlaneswalker() && zone.is(Constant.Zone.Play) && (!CardFactoryUtil.canTarget(ability, card))) {
                    ability.setTargetCard(card);
                    done();
                }
            }//selectCard()
            
            @Override
            public void selectPlayer(String player) {
                ability.setTargetPlayer(player);
                done();
            }
            
            void done() {
            	select.incrementTargets();
                stop();
                req.finishedTargeting();
            }
        };
        return target;
    }//targetPlayer()
    
    // these have been copied over from CardFactoryUtil as they need two extra parameters for target selection.
	// however, due to the changes necessary for SA_Requirements this is much different than the original
    public static Input input_targetValid(final SpellAbility sa, final String[] Tgts, final String message, 
    		final Target_Selection select, final SpellAbility_Requirements req)
    {
    	return new Input() {
			private static final long serialVersionUID = -2397096454771577476L;

			@Override
	        public void showMessage() {
	            CardList allCards = new CardList();
	            allCards.addAll(AllZone.Human_Play.getCards());
	            allCards.addAll(AllZone.Computer_Play.getCards());
	            CardList choices = allCards.getValidCards(Tgts);
	            
	            boolean canTargetPlayer = false;
	            for(String s : Tgts)
	            	if (s.equals("player"))
	            		canTargetPlayer = true;

	            stopSetNext(input_targetSpecific(sa, choices, message, true, canTargetPlayer, select, req));
	        }
    	};
    }//input_targetValid

    //CardList choices are the only cards the user can successful select
    public static Input input_targetSpecific(final SpellAbility spell, final CardList choices, final String message, 
    		final boolean targeted, final boolean bTgtPlayer, final Target_Selection select, final SpellAbility_Requirements req) {
        Input target = new Input() {
			private static final long serialVersionUID = -1091595663541356356L;

			@Override
            public void showMessage() {
                AllZone.Display.showMessage(message);
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	select.setCancel(true);
                stop();
                req.finishedTargeting();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(targeted && !CardFactoryUtil.canTarget(spell, card)) {
                    AllZone.Display.showMessage("Cannot target this card (Shroud? Protection? Restrictions?).");
                } 
                else if(choices.contains(card)) {
                    spell.setTargetCard(card);
                    done();
                }
            }//selectCard()
            
            @Override
            public void selectPlayer(String player) {
            	if (bTgtPlayer){	// todo: check if the player has Shroud too
	            	spell.setTargetPlayer(player);
	                done();
            	}
            }
            
            void done() {
            	select.incrementTargets();
                stop();
                req.finishedTargeting();
            }
        };
        return target;
    }//input_targetSpecific()
    
}
