
package forge;


import java.util.ArrayList;


public class Input_Block_Planeswalker extends Input {
    private static final long serialVersionUID = 8504632360578751473L;
    
    private Card              currentAttacker  = null;
    private ArrayList<Card>   allBlocking      = new ArrayList<Card>();
    
    @Override
    public void showMessage() {
        //for Castle Raptors, since it gets a bonus if untapped
        for(String effect:AllZone.StaticEffects.getStateBasedMap().keySet()) {
            Command com = GameActionUtil.commands.get(effect);
            com.execute();
        }
        
        GameActionUtil.executeCardStateEffects();
        

        //could add "Reset Blockers" button
        ButtonUtil.enableOnlyOK();
        
        if(currentAttacker == null) AllZone.Display.showMessage("Planeswalker Combat\r\nTo Block, click on your Opponents attacker first , then your blocker(s)");
        else AllZone.Display.showMessage("Select a creature to block " + currentAttacker.getName() + " ("
                + currentAttacker.getUniqueNumber() + ") ");
        
        CombatUtil.showCombat();
    }
    
    @Override
    public void selectButtonOK() {
        ButtonUtil.reset();
        
        //AllZone.Phase.nextPhase();
        //for debugging: System.out.println("need to nextPhase(Input_Block_Planeswalker.selectButtonOK) = true; Note, this has not been tested, did it work?");
        AllZone.Phase.setNeedToNextPhase(true);
        this.stop();
    }
    
    @Override
    public void selectCard(Card card, PlayerZone zone) {
        //is attacking?
        if(CardUtil.toList(AllZone.pwCombat.getAttackers()).contains(card)) {
            currentAttacker = card;
        } else if(zone.is(Constant.Zone.Play, AllZone.HumanPlayer) && card.isCreature() && card.isUntapped()
                && CombatUtil.canBlock(currentAttacker, card)) {
            if(currentAttacker != null && (!allBlocking.contains(card))) {
                allBlocking.add(card);
                AllZone.pwCombat.addBlocker(currentAttacker, card);
            }
        }
        showMessage();
    }//selectCard()
}
