
package forge;


public abstract class Input implements java.io.Serializable {
    private static final long serialVersionUID = -6539552513871194081L;
    
    private boolean           isFree           = false;
    
    //showMessage() is always the first method called
    public void showMessage() {
        AllZone.Display.showMessage("Blank Input");
    }
    
    public void selectCard(Card c, PlayerZone zone) {}
    
    public void selectPlayer(String player) {}
    
    public void selectButtonOK() {}
    
    public void selectButtonCancel() {}
    
    //helper methods, since they are used alot
    //to be used by anything in CardFactory like SetTargetInput
    //NOT TO BE USED by Input_Main or any of the "regular" Inputs objects that are not set using AllZone.InputControl.setInput(Input)
    final public void stop() {
        //clears a "temp" Input like Input_PayManaCost if there is one
        AllZone.InputControl.resetInput();
        AllZone.InputControl.updateObservers();
        
        if(AllZone.Phase.isNeedToNextPhase() == true) {
            //for debugging: System.out.println("There better be no nextPhase in the stack.");
            AllZone.Phase.setNeedToNextPhase(false);
            AllZone.Phase.nextPhase();
        }
    }
    
    final public void stopPayCost(SpellAbility sa) {
//	AllZone.InputControl.updateObservers();
        AllZone.GameAction.playSpellAbility(sa);
    }
    
    //exits the "current" Input and sets the next Input
    final public void stopSetNext(Input in) {
        stop();
        AllZone.InputControl.setInput(in);
    }
    
    @Override
    public String toString() {
        return "blank";
    }//returns the Input name like "EmptyStack"
    
    public void setFree(boolean isFree) {
        this.isFree = isFree;
    }
    
    public boolean isFree() {
        return isFree;
    }
}
