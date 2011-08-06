
package forge;

import com.esotericsoftware.minlog.Log;


public class ComputerAI_Input extends Input {
    private static final long serialVersionUID = -3091338639571662216L;
    
    private final Computer    computer;
    
    public ComputerAI_Input(Computer i_computer) {
        computer = i_computer;
    }
    
    //wrapper method that ComputerAI_StackNotEmpty class calls
    //ad-hoc way for ComptuerAI_StackNotEmpty to get to the Computer class
    public void stackNotEmpty() {
        computer.stack_not_empty();
    }
    
    @Override
    public void showMessage() {
        ButtonUtil.disableAll();
        AllZone.Display.showMessage("Phase: "
                + AllZone.Phase.getPhase()
                + "\nAn error may have occurred. Please send the \"Stack Report\" and the \"Detailed Error Trace\" to the Forge forum.");
        
        think();
    }//getMessage();
    
    public Computer getComputer() {
        return computer;
    }
    
    private void think() {
        final String phase = AllZone.Phase.getPhase();
        
        if(phase.equals(Constant.Phase.Main1)) {
        	Log.debug("Computer main1");
        	computer.main1();
        }

        else if(phase.equals(Constant.Phase.Combat_Declare_Attackers_InstantAbility)) {
            computer.declare_attackers_before();
        }

        else if(phase.equals(Constant.Phase.Combat_Declare_Attackers)) {
            computer.declare_attackers();
        } else if(phase.equals(Constant.Phase.Combat_Declare_Blockers_InstantAbility)) {
            computer.declare_blockers_after();
        } else if (phase.equals(Constant.Phase.Combat_After_Declare_Blockers))
        {
        	computer.after_declare_blockers();
        } else if(phase.equals(Constant.Phase.Main2)) {
        	Log.debug("Computer main2");
            computer.main2();
        } else if(phase.equals(Constant.Phase.At_End_Of_Turn)) {
            AllZone.EndOfTurn.executeAt();
            
            //AllZone.Phase.nextPhase();
            //for debugging: System.out.println("need to nextPhase(ComputerAI_Input) = true");
            AllZone.Phase.setNeedToNextPhase(true);
            
            //Do not updateObservers here, do it in nextPhase
            //AllZone.InputControl.updateObservers();
        } else if(phase.equals(Constant.Phase.Until_End_Of_Turn)) {
            AllZone.EndOfTurn.executeUntil();
            
            //AllZone.Phase.nextPhase();
            //for debugging: System.out.println("need to nextPhase(ComputerAI_Input.think(),phase.equals(Until_End_Of_Turn) = true");
            AllZone.Phase.setNeedToNextPhase(true);
            
            //do not updateObservers() here! Do it in nextPhase().
            //AllZone.InputControl.updateObservers();
        }

        //takes place during the human's turn, like blocking etc...
        else if(phase.equals(Constant.Phase.Combat_Declare_Blockers)) {
            computer.declare_blockers();
        } else if(phase.equals(Constant.Phase.End_Of_Turn)) {
            computer.end_of_turn();
        } else if(phase.equals(Constant.Phase.End_Of_Combat)) {
        	computer.end_of_combat();
        }
    }//think
}
