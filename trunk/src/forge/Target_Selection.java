package forge;

import java.util.ArrayList;

import forge.gui.GuiUtils;

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
	
	public boolean chooseTargets(){
		// if not enough targets chosen, reset and cancel Ability
		if (bCancel || (bDoneTarget && !target.isMinTargetsChosen(card, ability))){
			bCancel = true;
			req.finishedTargeting();
			return false;
		}
		else if (!doesTarget() || bDoneTarget && target.isMinTargetsChosen(card, ability) || target.isMaxTargetsChosen(card, ability)){
			Ability_Sub abSub = ability.getSubAbility();
			
			if (abSub == null){
				// if no more SubAbilities finish targeting
				req.finishedTargeting();
				return true;
			}
			else{
				// Has Sub Ability
				Target_Selection ts = new Target_Selection(abSub.getTarget(), abSub);
				ts.setRequirements(req);
				ts.resetTargets();
				boolean flag = ts.chooseTargets();

				return flag;
			}
		}
		
		//targets still needed
		if(target.getMandatory())
		{
			AllZone.InputControl.setInput(input_targetValid(ability, target.getValidTgts(), target.getVTSelection(), this, req, target.hasCandidates()));
		}
		else
		{
			AllZone.InputControl.setInput(input_targetValid(ability, target.getValidTgts(), target.getVTSelection(), this, req, false));
		}
        return false;
	}

    // these have been copied over from CardFactoryUtil as they need two extra parameters for target selection.
	// however, due to the changes necessary for SA_Requirements this is much different than the original
    public static Input input_targetValid(final SpellAbility sa, final String[] Tgts, final String message, 
    		final Target_Selection select, final SpellAbility_Requirements req, final boolean mandatory)
    {
    	Input target = new Input() {
			private static final long serialVersionUID = -2397096454771577476L;

			@Override
	        public void showMessage() {
				Target tgt = select.getTgt();
				String zone = tgt.getZone();
				
				CardList choices = AllZoneUtil.getCardsInZone(zone).getValidCards(Tgts, sa.getActivatingPlayer(), sa.getSourceCard());
				
				// Remove cards already targeted
				ArrayList<Card> targeted = tgt.getTargetCards();
				for(Card c : targeted){
					if (choices.contains(c))
						choices.remove(c);
				}
				
				if (zone.equals(Constant.Zone.Battlefield)){
		            boolean canTargetPlayer = false;
		            boolean canTargetOpponent = false;
		            for(String s : Tgts){
		            	
		            	if (s.equalsIgnoreCase("Player"))
		            		canTargetPlayer = true;
		            	if (s.equalsIgnoreCase("Opponent"))
		            		canTargetOpponent = true;
		            }
	
		            stopSetNext(input_targetSpecific(sa, choices, message, true, canTargetPlayer, canTargetOpponent, select, req,mandatory));
				}
				else{
					stopSetNext(input_cardFromList(sa, choices, message, true, select, req,mandatory));
				}
	        }
    	};
    	
        return target;
    }//input_targetValid

    //CardList choices are the only cards the user can successful select
    public static Input input_targetSpecific(final SpellAbility sa, final CardList choices, final String message, 
    		final boolean targeted, final boolean bTgtPlayer, final boolean bTgtOpponent, final Target_Selection select, final SpellAbility_Requirements req, final boolean mandatory) {
        Input target = new Input() {
			private static final long serialVersionUID = -1091595663541356356L;

			@Override
            public void showMessage() {
				// TODO: Update choices here, remove anything already targeted
				
				StringBuilder sb = new StringBuilder();
				sb.append("Targeted: ");
				sb.append(select.getTgt().getTargetedString());
				sb.append("\n");
				sb.append(message);
				
                AllZone.Display.showMessage(sb.toString());
                
                Target t = select.getTgt();
                // If reached Minimum targets, enable OK button
                if (t.getMinTargets(sa.getSourceCard(), sa) > t.getNumTargeted())
                	ButtonUtil.enableOnlyCancel();
                else
                	ButtonUtil.enableAll();
                
                if(mandatory)
                {
                	ButtonUtil.disableCancel();
                }
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
            	done();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(targeted && !CardFactoryUtil.canTarget(sa, card)) {
                    AllZone.Display.showMessage("Cannot target this card (Shroud? Protection? Restrictions?).");
                } 
                else if(choices.contains(card)) {
                	select.getTgt().addTarget(card);
                    done();
                }
            }//selectCard()
            
            @Override
            public void selectPlayer(Player player) {
            	if ((bTgtPlayer || (bTgtOpponent && player.equals(sa.getActivatingPlayer().getOpponent()))) && 
	            	player.canTarget(sa.getSourceCard())){
	            		select.getTgt().addTarget(player);
		                done();
	            	}
            }
            
            void done() {
                stop();

                select.chooseTargets();
            }
        };

        return target;
    }//input_targetSpecific()
    
    
    public static Input input_cardFromList(final SpellAbility sa, final CardList choices, final String message, 
    		final boolean targeted, final Target_Selection select, final SpellAbility_Requirements req, final boolean mandatory){
    	// Send in a list of valid cards, and popup a choice box to target 
		final Card dummy = new Card();
		dummy.setName("[FINISH TARGETING]");
    	
	    Input target = new Input() {
	        private static final long serialVersionUID = 9027742835781889044L;
	        
	        @Override
	        public void showMessage() {
	        	Target tgt = select.getTgt();
	        	
	        	CardList choicesWithDone = choices;
	        	if (tgt.isMinTargetsChosen(sa.getSourceCard(), sa)){
	        		// is there a more elegant way of doing this?
	        		choicesWithDone.add(dummy);
	        	}
	            Object check = GuiUtils.getChoiceOptional(message, choicesWithDone.toArray());
	            if(check != null) {
	            	Card c = (Card) check;
	            	if (c.equals(dummy))
	            		select.setDoneTarget(true);
	            	else
	            		tgt.addTarget(c);
	            }
	            else
	            	select.setCancel(true);
	            
	            done();
	        }//showMessage()
	        
	        public void done(){
                stop();
                select.chooseTargets();
	        }
	    };//Input

	    return target;
    } 
}
