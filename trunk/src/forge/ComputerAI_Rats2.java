package forge;
import java.util.*;

public class ComputerAI_Rats2 implements Computer
{
	private int numberPlayLand = CardFactoryUtil.getCanPlayNumberOfLands(Constant.Player.Computer);
    
    private static Random random = new Random();
        
    public void main1()
    {
	if(numberPlayLand > 0)
	{
	    numberPlayLand--;
	    ComputerUtil.playLand();
	}

//	if(playActivatedAbilities() || playSorcery())
//	    return;
	Card[] c = AllZone.Computer_Hand.getCards();
	System.out.println("Computer Thinking " +new CardList(c));	
	
	//AllZone.Phase.nextPhase();
	//for debugging: System.out.println("need to nextPhase(ComputerAI_Rats2.main1) = true");
	AllZone.Phase.setNeedToNextPhase(true);
    }
    public void main2()
    {
    	numberPlayLand = CardFactoryUtil.getCanPlayNumberOfLands(Constant.Player.Computer);
	  
	  //AllZone.Phase.nextPhase();
	  //for debugging: System.out.println("need to nextPhase(ComputerAI_Rats2.main2) = true");
	  AllZone.Phase.setNeedToNextPhase(true);
    }
    public void declare_blockers()
    {
	if(random.nextBoolean() || AllZone.Computer_Life.getLife() < 10)
	{
	    //presumes all creatures the Computer owns are the same size, because they are Relentless Rats
	    Card block[] = CardUtil.toCard(PlayerZoneUtil.getUntappedCreatures(AllZone.Computer_Play));
	    Card att[] = AllZone.Combat.getAttackers();
	    int blockIndex = 0;
	    
	    for(int i = 0; i < att.length && blockIndex < block.length; i++)
	    {
		if(att[i].getNetDefense() <= block[blockIndex].getNetAttack() &&
		CombatUtil.canBlock(att[i], block[blockIndex]))
		    AllZone.Combat.addBlocker(att[i], block[blockIndex++]);
	    }
	}
	
	//AllZone.Phase.nextPhase();
	//for debugging: System.out.println("need to nextPhase(Computer_AI_Rats2.declare_blockers) = true");
    AllZone.Phase.setNeedToNextPhase(true);
    }//declare_blockers()
    
    public void declare_attackers_before()
    {
    	 AllZone.Phase.setNeedToNextPhase(true);
    }
    
    public void declare_attackers()
    {
	Card[] a = CardUtil.toCard(PlayerZoneUtil.getUntappedCreatures(AllZone.Computer_Play));
	Card[] b = CardUtil.toCard(PlayerZoneUtil.getUntappedCreatures(AllZone.Human_Play));	
//for(int i = 0; i <a.length; i++)
//    System.out.println(a[i].getAttack());
	
	if(b.length == 0)
	{
	    Card[] att = a;

	    for(int i = 0; i < att.length; i++)
	    {
		att[i].tap();
		AllZone.Combat.addAttacker(att[i]);
	    }
	}	
	else if(a.length != 0)
	{	    
	    MoveAttack m = new MoveAttack(a, b, AllZone.Computer_Life.getLife(), AllZone.Human_Life.getLife());
	    m.max(m, 2, true);
	    Card[] att = m.getBestMove().getAttackers().toArray();

	    for(int i = 0; i < att.length; i++)
	    {
		att[i].tap();
		AllZone.Combat.addAttacker(att[i]);
	    }
	}
	
	//AllZone.Phase.nextPhase();
	//for debugging: System.out.println("need to nextPhase(ComputerAI_Rats2.declare_attackers) = true");
    AllZone.Phase.setNeedToNextPhase(true);
    }//declare_attackers()
    
    public Card[] getLibrary()
    {
	ArrayList<Card> a = new ArrayList<Card>();
	CardFactory cf = AllZone.CardFactory;
	
	//computer library
	for(int i = 0; i < 18; i++)
	    a.add(cf.getCard("Swamp", Constant.Player.Computer));
	//    for(int i = 0; i < 3; i++)
	//      a.add(cf.getCard("Steel Wall", Constant.Player.Computer));
	
	for(int i = 0; i < 0;i ++)
	{
	    a.add(cf.getCard("Nantuko Shade", Constant.Player.Computer));
	    a.add(cf.getCard("Hymn to Tourach", Constant.Player.Computer));
	}
	for(int i = 0; i < 2;i ++)
	{
	    a.add(cf.getCard("Royal Assassin", Constant.Player.Computer));
	}
	while(a.size() < 40)
	    a.add(cf.getCard("Relentless Rats", Constant.Player.Computer));

	
	return CardUtil.toCard(a);
    }//getLibrary()
    
    public void declare_blockers_after(){playInstantAndAbilities();}
    
    public void end_of_combat()
    {
    	AllZone.Phase.setNeedToNextPhase(true);
    }
    
    public void end_of_turn()                 {playInstantAndAbilities();}
    
    private void playInstantAndAbilities()
    {
      //	if(playActivatedAbilities())
      //	    return;

      //AllZone.Phase.nextPhase();
      //for debugging: System.out.println("need to nextPhase(ComputerAI_Rats2.playInstantAndAbilities) = true");
      AllZone.Phase.setNeedToNextPhase(true);
    }

    public void addNumberPlayLands(int n)
    {
    	numberPlayLand += n;
    }
    
    public void setNumberPlayLands(int n)
    {
    	numberPlayLand = n;
    }
    
    public void stack_not_empty() 
    {
	  AllZone.InputControl.resetInput();
	  AllZone.InputControl.updateObservers();    	
    }    
}