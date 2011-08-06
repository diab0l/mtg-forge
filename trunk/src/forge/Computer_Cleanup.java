package forge;
import java.util.Random;

public class Computer_Cleanup extends Input
{
	private static final long serialVersionUID = -145924458598185438L;

	public void showMessage()
    {
	Random r = new Random();
	Card[] c = AllZone.Computer_Hand.getCards();
	while(7 < c.length)
	{
	    AllZone.GameAction.discard(c[r.nextInt(c.length)]);
	    c = AllZone.Computer_Hand.getCards();
	}
	
	CombatUtil.removeAllDamage();
	
	//AllZone.Phase.nextPhase();
	//for debugging: System.out.println("need to nextPhase(Computer_Cleanup.showMessage()) = true");
    AllZone.Phase.setNeedToNextPhase(true);
    }
}
