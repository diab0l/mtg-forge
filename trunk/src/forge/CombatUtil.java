package forge;
import java.util.ArrayList;

public class CombatUtil
{
  public static boolean canBlock(Card attacker, Card blocker)
  {
	
    if(attacker == null || blocker == null)
      return false;
    
    if (!canBlockProtection(attacker, blocker))
    	return false;

    //rare case:
    if (blocker.getKeyword().contains("Shadow") 
    	&& blocker.getKeyword().contains("This creature can block creatures with shadow as though they didn't have shadow."))
    	return false;
    
    /*
    if(attacker.getKeyword().contains("Shadow"))
        return blocker.getKeyword().contains("Shadow") || 
        	   blocker.getKeyword().contains("This creature can block creatures with shadow as though they didn't have shadow.");
    */
    if (attacker.getKeyword().contains("Shadow") && !blocker.getKeyword().contains("Shadow") 
    	&& !blocker.getKeyword().contains("This creature can block creatures with shadow as though they didn't have shadow.") )
    	return false;

    if(! attacker.getKeyword().contains("Shadow") &&
          blocker.getKeyword().contains("Shadow"))
        return false;
    
    if (attacker.getNetAttack() <= 2 && blocker.getName().equals("Sunweb"))
    	return false;
    

    PlayerZone blkPZ = AllZone.getZone(Constant.Zone.Play, blocker.getController());
    CardList blkCL = new CardList(blkPZ.getCards());
    CardList temp = new CardList();

    if(attacker.getKeyword().contains("Plainswalk"))
    {
        temp = blkCL.getType("Plains");
        if(!temp.isEmpty())
            return false;
    }

    if(attacker.getKeyword().contains("Islandwalk"))
    {
        temp = blkCL.getType("Island");
        if(!temp.isEmpty())
            return false;
    }

    if(attacker.getKeyword().contains("Swampwalk"))
    {
       temp = blkCL.getType("Swamp");
        if(!temp.isEmpty())
            return false;
    }

    if(attacker.getKeyword().contains("Mountainwalk"))
    {
        temp = blkCL.getType("Mountain");
        if(!temp.isEmpty())
            return false;
    }

    if(attacker.getKeyword().contains("Forestwalk"))
    {
        temp = blkCL.getType("Forest");
        if(!temp.isEmpty())
            return false;
    }
    
    if (attacker.getKeyword().contains("Legendary landwalk"))
    {
    	temp = blkCL.filter(new CardListFilter()
    	{
			public boolean addCard(Card c) {
				return c.isLand() && c.getType().contains("Legendary");
			}	
    	});
    	if (!temp.isEmpty())
    		return false;
    }
    
    if (attacker.getKeyword().contains("Nonbasic landwalk"))
    {
    	temp = blkCL.filter(new CardListFilter()
    	{
			public boolean addCard(Card c) {
				return c.isLand() && !c.isBasicLand();
			}	
    	});
    	if (!temp.isEmpty())
    		return false;
    }


    if(blocker.getKeyword().contains("This creature can block only creatures with flying.") && !attacker.getKeyword().contains("Flying"))
      return false;

    if(attacker.getKeyword().contains("Unblockable"))
      return false;

    if(blocker.getKeyword().contains("This creature cannot block") || blocker.getKeyword().contains("This creature can't attack or block"))
      return false;
    
    if (attacker.getKeyword().contains("Flying"))
    {
    	if (!blocker.getKeyword().contains("Flying") &&
    		!blocker.getKeyword().contains("This creature can block as though it had flying.") &&
    		!blocker.getKeyword().contains("Reach"))
    		return false;
    }
    
    if (attacker.getKeyword().contains("This creature can't be blocked except by creatures with flying") && !blocker.getKeyword().contains("Flying"))
    	return false;

    
    if (attacker.getKeyword().contains("Horsemanship"))
    {
    	if (!blocker.getKeyword().contains("Horsemanship"))
    			return false;
    }
    
    if (attacker.getName().equals("Taoist Warrior") || attacker.getName().equals("Zuo Ci, the Mocking Sage"))
    {
    	if (blocker.getKeyword().contains("Horsemanship"))
    		return false;
    }
    
    if (attacker.getKeyword().contains("Fear"))
    {
    	if (!blocker.getType().contains("Artifact") &&
    		!CardUtil.getColors(blocker).contains(Constant.Color.Black) /*&&
    		!CardUtil.getColors(blocker).contains(Constant.Color.Colorless) */) //should not include colorless, right?
    		return false;
    }
    
    if (attacker.getName().equals("Barrenton Cragtreads"))
    {
    	if (CardUtil.getColors(blocker).contains(Constant.Color.Red))
    		return false;
    }
    
    if (attacker.getName().equals("Amrou Seekers"))
    {
    	if (!blocker.getType().contains("Artifact") &&
        	!CardUtil.getColors(blocker).contains(Constant.Color.White))
        	return false;
    }
    
    if (attacker.getName().equals("Skirk Shaman"))
    {
    	if (!blocker.getType().contains("Artifact") &&
    		!CardUtil.getColors(blocker).contains(Constant.Color.Red))
    		return false;
    }
    
    if (attacker.getName().equals("Manta Ray"))
    {
    	if (!CardUtil.getColors(blocker).contains(Constant.Color.Blue))
    		return false;
    }
    /*
    if(attacker.getKeyword().contains("Flying"))
      return blocker.getKeyword().contains("Flying") ||
      blocker.getKeyword().contains("This creature can block as though it had flying.") ||
      blocker.getKeyword().contains("Reach");

    if(attacker.getKeyword().contains("Fear"))
      return blocker.getType().contains("Artifact")     ||
      CardUtil.getColors(blocker).contains(Constant.Color.Black) ||
      CardUtil.getColors(blocker).contains(Constant.Color.Colorless);

    if (attacker.getName().equals("Amrou Seekers"))
    	return blocker.getType().contains("Artifact") ||
    	CardUtil.getColors(blocker).contains(Constant.Color.White);

    if(attacker.getName().equals("Skirk Shaman"))
      return blocker.getType().contains("Artifact")     ||
      CardUtil.getColors(blocker).contains(Constant.Color.Red);
    */
    if(attacker.getName().equals("Goldmeadow Dodger"))
    	return blocker.getNetAttack() < 4;
    
    if(attacker.getName().equals("Juggernaut") && blocker.getType().contains("Wall"))
    	return false;

    return true;
  }//canBlock()
  public static boolean canAttack(Card c)
  {

	boolean moatPrevented = false;
	if (isMoatInPlay() || isMagusMoatInPlay())
	{
		if (!c.getKeyword().contains("Flying"))
		{
			moatPrevented = true;
		}
	}
	
	PlayerZone play = AllZone.getZone(Constant.Zone.Play,AllZone.GameAction.getOpponent(c.getController()));
	CardList list = new CardList(play.getCards());
	CardList temp = new CardList();
	
	if (c.getKeyword().contains("This creature can't attack unless defending player controls an Island"))
	{
		temp = list.getType("Island");
		if (temp.isEmpty())
			return false;
	}
	
    if(c.isTapped() || c.hasSickness() || c.getKeyword().contains("Defender") || moatPrevented || oppControlsBlazingArchon(c)
      || c.getKeyword().contains("This creature can't attack") || c.getKeyword().contains("This creature can't attack or block") )
      return false;

    //if Card has Haste, Card.hasSickness() will return false
    return true;
  }//canAttack()
  
  public static boolean canDestroyAttacker(Card attacker, Card defender)
  {
	  
	if (attacker.getName().equals("Sylvan Basilisk"))
		return false;
	
	int flankingMagnitude = 0;
	if (attacker.getKeyword().contains("Flanking") && !defender.getKeyword().contains("Flanking"))
	{
		
		String kw = "";
		ArrayList<String> list = attacker.getKeyword();
		
		for (int i=0;i<list.size();i++)
		{
			  kw = list.get(i);
			  if (kw.equals("Flanking"))
				  flankingMagnitude++;
		}
		if (flankingMagnitude >= defender.getNetDefense() - defender.getDamage())		
			return false;
		
	}//flanking
	
	if (attacker.getName().equals("Cho-Manno, Revolutionary"))
		return false;
	
	//this usually doesn't happen, unless the attacker got pro {color} after being blocked, or the defender became {color}
	if (attacker.getKeyword().contains("Protection from white") && CardUtil.getColors(defender).contains(Constant.Color.White))
		return false;
	if (attacker.getKeyword().contains("Protection from blue") && CardUtil.getColors(defender).contains(Constant.Color.Blue))
		return false;
	if (attacker.getKeyword().contains("Protection from black") && CardUtil.getColors(defender).contains(Constant.Color.Black))
		return false;
	if (attacker.getKeyword().contains("Protection from red") && CardUtil.getColors(defender).contains(Constant.Color.Red))
		return false;
	if (attacker.getKeyword().contains("Protection from green") && CardUtil.getColors(defender).contains(Constant.Color.Green))
		return false;
	
	if (attacker.getKeyword().contains("Protection from artifacts") && defender.isArtifact())
		return false;
	
	if (attacker.getKeyword().contains("Protection from creatures"))
		return false;
	
	if (attacker.getKeyword().contains("Protection from Dragons") && (defender.getType().contains("Dragon") || defender.getKeyword().contains("Changeling") ))
		return false;
	if (attacker.getKeyword().contains("Protection from Demons") && (defender.getType().contains("Demon") || defender.getKeyword().contains("Changeling") ))
		return false;
	if (attacker.getKeyword().contains("Protection from Goblins") && (defender.getType().contains("Goblin") || defender.getKeyword().contains("Changeling") ))
		return false;
	
	
	int defenderDamage = defender.getNetAttack() - flankingMagnitude;
	int attackerDamage = attacker.getNetAttack();
	
	if (isDoranInPlay()) {
		defenderDamage = defender.getNetDefense() - flankingMagnitude;
		attackerDamage = attacker.getNetDefense();
	}
	int defenderLife = defender.getNetDefense() - flankingMagnitude;
	int attackerLife = attacker.getNetDefense();

	if (defender.getKeyword().contains("Double Strike"))
	{
		if (defender.getKeyword().contains("Deathtouch"))
			return true;
		
		if (attacker.getKeyword().contains("Double Strike"))
		{
			if (defenderDamage >= attackerLife - attacker.getDamage())
				return true;
			if ( ((defenderLife - defender.getDamage()) > attackerDamage) && 
				 (attackerLife - attacker.getDamage() <= 2*defenderDamage) && !attacker.getKeyword().contains("Deathtouch"))
				return true;
		}
		else if (attacker.getKeyword().contains("First Strike")) //hmm, same as previous if ?
		{
			if (defenderDamage >= attackerLife - attacker.getDamage())
				return true;
			if ( ((defenderLife - defender.getDamage()) > attackerDamage) && 
				 ((attackerLife - attacker.getDamage()) <= 2*defenderDamage) && !attacker.getKeyword().contains("Deathtouch"))
				return true;
		}		
		else //no double nor first strike for attacker
		{
			if (defenderDamage >= attackerLife - attacker.getDamage())
				return true;
			if ((attackerLife - attacker.getDamage()) <= 2*defenderDamage)
				return true;
		}
		
	}//defender double strike
	
	else if(defender.getKeyword().contains("First Strike"))
	{
		if (defender.getKeyword().contains("Deathtouch"))
			return true;
		
		if (defenderDamage >= attackerLife - attacker.getDamage())
			return true;
	}//defender first strike
	
	else //no double nor first strike for defender
	{
		if (attacker.getKeyword().contains("Double Strike"))
		{
			if (defenderDamage >= attackerLife - attacker.getDamage() && 
				attackerDamage*2 < defenderLife - defender.getDamage())
				return true;
			
			if (defender.getKeyword().contains("Deathtouch") && !attacker.getKeyword().contains("Deathtouch") &&
				attackerDamage*2 < defenderLife - defender.getDamage())
				return true;
		}
		else if (attacker.getKeyword().contains("First Strike")) //same as previous if?
		{
			if (defenderDamage >= attackerLife - attacker.getDamage() && !attacker.getKeyword().contains("Deathtouch") &&
				attackerDamage < defenderLife - defender.getDamage())
				return true;
			
			if (defender.getKeyword().contains("Deathtouch") && !attacker.getKeyword().contains("Deathtouch") &&
				attackerDamage < defenderLife - defender.getDamage())
				return true;
		}
		else //no double nor first strike for attacker
		{
			if (defender.getKeyword().contains("Deathtouch"))
				return true;
			
			return defenderDamage >= attackerLife - attacker.getDamage();
		}
		
	}//defender no double/first strike
	return false; //should never arrive here
  } //canDestroyAttacker
  
  public static boolean canDestroyBlocker(Card defender, Card attacker)
  {
	if (attacker.getName().equals("Sylvan Basilisk"))
		return true;
	
	int flankingMagnitude = 0;
	if (attacker.getKeyword().contains("Flanking") && !defender.getKeyword().contains("Flanking"))
	{
		
		String kw = "";
		ArrayList<String> list = attacker.getKeyword();
		
		for (int i=0;i<list.size();i++)
		{
			  kw = list.get(i);
			  if (kw.equals("Flanking"))
				  flankingMagnitude++;
		}
		if (flankingMagnitude >= defender.getNetDefense() - defender.getDamage())		
			return true;
		
	}//flanking
	if (defender.getName().equals("Cho-Manno, Revolutionary"))
		return false;
	
	
	if (defender.getKeyword().contains("Protection from white") && CardUtil.getColors(attacker).contains(Constant.Color.White))
		return false;
	if (defender.getKeyword().contains("Protection from blue") && CardUtil.getColors(attacker).contains(Constant.Color.Blue))
		return false;
	if (defender.getKeyword().contains("Protection from black") && CardUtil.getColors(attacker).contains(Constant.Color.Black))
		return false;
	if (defender.getKeyword().contains("Protection from red") && CardUtil.getColors(attacker).contains(Constant.Color.Red))
		return false;
	if (defender.getKeyword().contains("Protection from green") && CardUtil.getColors(attacker).contains(Constant.Color.Green))
		return false;
	
	if (defender.getKeyword().contains("Protection from artifacts") && attacker.isArtifact())
		return false;
	
	if (defender.getKeyword().contains("Protection from creatures"))
		return false;
	
	if (defender.getKeyword().contains("Protection from Dragons") && (attacker.getType().contains("Dragon") || attacker.getKeyword().contains("Changeling") ))
		return false;
	if (defender.getKeyword().contains("Protection from Demons") && (attacker.getType().contains("Demon") || attacker.getKeyword().contains("Changeling") ))
		return false;
	if (defender.getKeyword().contains("Protection from Goblins") && (attacker.getType().contains("Goblin") || attacker.getKeyword().contains("Changeling") ))
		return false;

	int defenderDamage = defender.getNetAttack() - flankingMagnitude;
	int attackerDamage = attacker.getNetAttack();
	
	if (isDoranInPlay()) {
		defenderDamage = defender.getNetDefense() - flankingMagnitude;
		attackerDamage = attacker.getNetDefense();
	}
	int defenderLife = defender.getNetDefense() - flankingMagnitude;
	int attackerLife = attacker.getNetDefense();

	if (attacker.getKeyword().contains("Double Strike"))
	{
		if (attacker.getKeyword().contains("Deathtouch"))
			return true;
		
		if (defender.getKeyword().contains("Double Strike"))
		{
			if (defenderDamage >= attackerLife - attacker.getDamage())
				return true;
			if ( ((attackerLife - attacker.getDamage()) > defenderDamage) && 
				 (defenderLife - defender.getDamage() <= 2*attackerDamage) && !defender.getKeyword().contains("Deathtouch"))
				return true;
		}
		else if (defender.getKeyword().contains("First Strike")) //hmm, same as previous if ?
		{
			if (attackerDamage >= defenderLife - defender.getDamage())
				return true;
			if ( ((attackerLife - attacker.getDamage()) > defenderDamage) && 
				 ((defenderLife - defender.getDamage()) <= 2*attackerDamage) && !defender.getKeyword().contains("Deathtouch"))
				return true;
		}		
		else //no double nor first strike for defender
		{
			if (attackerDamage >= defenderLife - defender.getDamage())
				return true;
			if ((defenderLife - defender.getDamage()) <= 2*attackerDamage)
				return true;
		}
		
	}//attacker double strike
	
	else if(attacker.getKeyword().contains("First Strike"))
	{
		if (attacker.getKeyword().contains("Deathtouch"))
			return true;
		if (attackerDamage >= defenderLife - defender.getDamage())
			return true;
	}//attacker first strike
	
	else //no double nor first strike for attacker
	{
		if (defender.getKeyword().contains("Double Strike"))
		{
			if (attackerDamage >= defenderLife - defender.getDamage() && 
				defenderDamage < attackerLife - attacker.getDamage())
				return true;
		}
		else if (defender.getKeyword().contains("First Strike")) //same as previous if?
		{
			if (attackerDamage >= defenderLife - defender.getDamage() && 
				defenderDamage < attackerLife - attacker.getDamage())
				return true;
		}
		else //no double nor first strike for defender
		{
			if (attacker.getKeyword().contains("Deathtouch"))
				return true;
			
			return attackerDamage >= defenderLife - defender.getDamage();
		}
		
	}//attacker no double/first strike
	return false; //should never arrive here
  }//canDestroyBlocker

  public static void removeAllDamage()
  {
    Card[] c = AllZone.Human_Play.getCards();
    for(int i = 0; i < c.length; i++)
      c[i].setDamage(0);

    c = AllZone.Computer_Play.getCards();
    for(int i = 0; i < c.length; i++)
      c[i].setDamage(0);
  }
  public static void showCombat()
  {
    //clear
    AllZone.Display.showCombat("");

    Card attack[] = AllZone.Combat.getAttackers();
    Card defend[] = null;
    String display = "";
    String attackerName = "";
    String blockerName = "";

    //loop through attackers
    for(int i = 0; i < attack.length; i++)
    {
      GameActionUtil.executeExaltedEffects2(attack[i], AllZone.Combat);
      checkDeclareAttackers(attack[i]);
      attackerName = attack[i].getName();
      if (attack[i].isFaceDown())
    	  attackerName = "Morph";
      display += attackerName +" (" +attack[i].getUniqueNumber() +") "  +attack[i].getNetAttack() +"/" +attack[i].getNetDefense()   +" is attacking \n";
      
      defend = AllZone.Combat.getBlockers(attack[i]).toArray();

      //loop through blockers
      for(int inner = 0; inner < defend.length; inner++)
      {
    	checkDeclareBlockers(defend[inner]);
    	blockerName = defend[inner].getName();
    	if(defend[inner].isFaceDown())
    		blockerName = "Morph";
        display += "     " +blockerName  +" (" +defend[inner].getUniqueNumber()  +") "  +defend[inner].getNetAttack()  +"/" +defend[inner].getNetDefense()    +" is blocking \n";
      }
    }//while - loop through attackers
    String s = display + getPlaneswalkerBlockers();
    AllZone.Display.showCombat(s.trim());

  }//showBlockers()

  private static String getPlaneswalkerBlockers()
  {
    Card attack[] = AllZone.pwCombat.getAttackers();
    Card defend[] = null;
    String display = "";

    if(attack.length != 0)
      display = "Planeswalker Combat\r\n";

    String attackerName = "";
    String blockerName = "";
    //loop through attackers
    for(int i = 0; i < attack.length; i++)
    {
      GameActionUtil.executeExaltedEffects2(attack[i], AllZone.pwCombat);
      checkDeclareAttackers(attack[i]);
      attackerName = attack[i].getName();
         if (attack[i].isFaceDown())
       	  attackerName = "Morph";
         
      display += attackerName +" (" +attack[i].getUniqueNumber() +") "  +attack[i].getNetAttack() +"/" +attack[i].getNetDefense()   +" is attacking \n";

      defend = AllZone.pwCombat.getBlockers(attack[i]).toArray();

      //loop through blockers
      for(int inner = 0; inner < defend.length; inner++)
      {
    	checkDeclareBlockers(defend[inner]);
      	blockerName = defend[inner].getName();
      	if(defend[inner].isFaceDown())
      		blockerName = "Morph";
        display += "     " +blockerName  +" (" +defend[inner].getUniqueNumber()  +") "  +defend[inner].getNetAttack()  +"/" +defend[inner].getNetDefense()    +" is blocking \n";
      }
    }//while - loop through attackers

    return display;
  }//getPlaneswalkerBlockers()
  
  private static boolean canBlockProtection(Card attacker, Card blocker)
  {
	  ArrayList<String> list = attacker.getKeyword();
	  
	  String kw = "";
	  for (int i=0;i<list.size();i++)
	  {  
		  kw = list.get(i);
		  
		  if (kw.equals("Protection from creatures") || kw.equals("Protection from everything"))
			  return false;
		  if (kw.equals("Protection from artifacts") && blocker.isArtifact())
			  return false;
		  
		  if (kw.equals("Protection from Dragons") && (blocker.getType().contains("Dragon") || blocker.getKeyword().contains("Changeling") ))
			  return false;
		  if (kw.equals("Protection from Demons") && (blocker.getType().contains("Demon") || blocker.getKeyword().contains("Changeling") ))
			  return false;
		  if (kw.equals("Protection from Goblins") && (blocker.getType().contains("Goblin") || blocker.getKeyword().contains("Changeling") ))
			  return false;
		  
		  //pro colors:
		  if (kw.equals("Protection from white") && CardUtil.getColors(blocker).contains(Constant.Color.White))
			  return false;
		  if (kw.equals("Protection from blue") && CardUtil.getColors(blocker).contains(Constant.Color.Blue))
			  return false;
		  if (kw.equals("Protection from black") && CardUtil.getColors(blocker).contains(Constant.Color.Black))
			  return false;
		  if (kw.equals("Protection from red") && CardUtil.getColors(blocker).contains(Constant.Color.Red))
			  return false;
		  if (kw.equals("Protection from green") && CardUtil.getColors(blocker).contains(Constant.Color.Green))
			  return false;
	  }
	  return true;
  }
  
  public static boolean isDoranInPlay()
  {
	  CardList all = new CardList();
	  all.addAll(AllZone.Human_Play.getCards());
	  all.addAll(AllZone.Computer_Play.getCards());
	  
	  all = all.getName("Doran, the Siege Tower");
	  if (all.size() > 0)
		  return true;
	  else
		  return false;
  }
  public static boolean oppControlsBlazingArchon(Card c)
  {
	  String opp = AllZone.GameAction.getOpponent(c.getController());
	  
	  CardList list = new CardList();
	  list.addAll(AllZone.getZone(Constant.Zone.Play, opp).getCards());
	  
	  list = list.getName("Blazing Archon");
	  if (list.size() > 0)
		  return true;
	  else 
		  return false;
  }
  
  public static boolean isMoatInPlay()
  {
	  CardList all = new CardList();
	  all.addAll(AllZone.Human_Play.getCards());
	  all.addAll(AllZone.Computer_Play.getCards());
	  
	  all = all.getName("Moat");
	  if (all.size() > 0)
		  return true;
	  else
		  return false;
  }
  
  public static boolean isMagusMoatInPlay()
  {
	  CardList all = new CardList();
	  all.addAll(AllZone.Human_Play.getCards());
	  all.addAll(AllZone.Computer_Play.getCards());
	  
	  all = all.getName("Magus of the Moat");
	  if (all.size() > 0)
		  return true;
	  else
		  return false;
  }
  
  private static void checkDeclareAttackers(Card c) //this method checks triggered effects of attacking creatures, right before defending player declares blockers
  {  
	  if (AllZone.Phase.getPhase().equals("Declare Attackers"))
	  {
		  
		  //Beastmaster Ascension
		  if (!c.getCreatureAttackedThisTurn())
		  {
			  PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
			  CardList list = new CardList(play.getCards());
			  list = list.getName("Beastmaster Ascension");
			  
			  for(Card var:list) {
	              var.addCounter(Counters.QUEST, 1); 
			  }
		  } //BMA
		  
		  //Fervent Charge
		  if (!c.getCreatureAttackedThisTurn())
		  {
			  PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
			  CardList list = new CardList(play.getCards());
			  list = list.getName("Fervent Charge");
			  
			  for(Card var:list) {
	              final Card crd = c;
	              Ability ability2 = new Ability(var,"0")
				  {
					 public void resolve() 
					 {
						 
				         final Command untilEOT = new Command()
				         {

							private static final long serialVersionUID = 4495506596523335907L;

							public void execute()
				            {
				              if(AllZone.GameAction.isCardInPlay(crd))
				              {
				                crd.addTempAttackBoost(-2);
				                crd.addTempDefenseBoost(-2);
				              }
				            }
				          };//Command

				          
				          if(AllZone.GameAction.isCardInPlay(crd))
				          {
				            crd.addTempAttackBoost(2);
				            crd.addTempDefenseBoost(2);

				            AllZone.EndOfTurn.addUntil(untilEOT);
				          }
					 }//resolve
					 
				  };//ability
				  
				  ability2.setStackDescription(var.getName() + " - " + c.getName() + " gets +2/+2 until EOT.");
				  AllZone.Stack.add(ability2);
	              
			  }
		  }//Fervent Charge
		  
		  if (c.getName().equals("Zhang He, Wei General") && !c.getCreatureAttackedThisTurn())
		  {
			 final PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
			 
	         //final Card crd = c;
	         Ability ability2 = new Ability(c,"0")
	         {
	        	 public void resolve() 
	        	 {
	        		 CardList cl = new CardList(play.getCards());
	        		 cl = cl.filter(new CardListFilter()
	        		 {
	        			 public boolean addCard(Card c)
	        			 {
	        				 return c.isCreature() && !c.getName().equals("Zhang He, Wei General");
	        			 }
	        		 });
	        		 
	        		 final CardList creatures = cl;
	        		 
		        	 final Command untilEOT = new Command()
			         {

						private static final long serialVersionUID = 8799962485775380711L;
						
						
						
						public void execute()
			            {
							  for (Card creat:creatures){
					              if(AllZone.GameAction.isCardInPlay(creat))
					              {
					                creat.addTempAttackBoost(-1);
					              }
							  }
				            }
				     	};//Command
		
				        for (Card creat:creatures){  
						    if(AllZone.GameAction.isCardInPlay(creat) )
						    {
						        creat.addTempAttackBoost(1);
						    }
				        }
				        AllZone.EndOfTurn.addUntil(untilEOT);
	        	 }
	         };
	         
	         ability2.setStackDescription(c.getName() + " - all other creatures you control get +1/+0 until end of turn.");
			 AllZone.Stack.add(ability2);
		  }//Zhang He
		  
		  if (c.getName().equals("Soltari Champion") && !c.getCreatureAttackedThisTurn())
		  {
			 final PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
			 
	         final Card crd = c;
	         Ability ability2 = new Ability(c,"0")
	         {
	        	 public void resolve() 
	        	 {
	        		 CardList cl = new CardList(play.getCards());
	        		 cl = cl.filter(new CardListFilter()
	        		 {
	        			 public boolean addCard(Card c)
	        			 {
	        				 return c.isCreature() && !c.equals(crd);
	        			 }
	        		 });
	        		 
	        		 final CardList creatures = cl;
	        		 
		        	 final Command untilEOT = new Command()
			         {

						private static final long serialVersionUID = -8434529949884582940L;

						public void execute()
			            {
							  for (Card creat:creatures){
					              if(AllZone.GameAction.isCardInPlay(creat))
					              {
					                creat.addTempAttackBoost(-1);
					                creat.addTempDefenseBoost(-1);
					              }
							  }
				            }
				     	};//Command
		
				        for (Card creat:creatures){  
						    if(AllZone.GameAction.isCardInPlay(creat) )
						    {
						        creat.addTempAttackBoost(1);
						        creat.addTempDefenseBoost(1);
						    }
				        }
				        AllZone.EndOfTurn.addUntil(untilEOT);
	        	 }
	         };
	         
	         ability2.setStackDescription(c.getName() + " - all other creatures you control get +1/+1 until end of turn.");
			 AllZone.Stack.add(ability2);
		  }//Soltari Champion
		  
		  if (c.getName().equals("Goblin General") && !c.getCreatureAttackedThisTurn())
		  {
			 final PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
			 
	         //final Card crd = c;
	         Ability ability2 = new Ability(c,"0")
	         {
	        	 public void resolve() 
	        	 {
	        		 CardList cl = new CardList(play.getCards());
	        		 cl = cl.filter(new CardListFilter()
	        		 {
	        			 public boolean addCard(Card c)
	        			 {
	        				 return c.isCreature() && (c.getType().contains("Goblin") || c.getKeyword().contains("Changeling") );
	        			 }
	        		 });
	        		 
	        		 final CardList creatures = cl;
	        		 
		        	 final Command untilEOT = new Command()
			         {

						private static final long serialVersionUID = -8434529949884582940L;

						public void execute()
			            {
							  for (Card creat:creatures){
					              if(AllZone.GameAction.isCardInPlay(creat))
					              {
					                creat.addTempAttackBoost(-1);
					                creat.addTempDefenseBoost(-1);
					              }
							  }
				            }
				     	};//Command
		
				        for (Card creat:creatures){  
						    if(AllZone.GameAction.isCardInPlay(creat) )
						    {
						        creat.addTempAttackBoost(1);
						        creat.addTempDefenseBoost(1);
						    }
				        }
				        AllZone.EndOfTurn.addUntil(untilEOT);
	        	 }
	         };
	         
	         ability2.setStackDescription(c.getName() + " - Goblin creatures you control get +1/+1 until end of turn.");
			 AllZone.Stack.add(ability2);
		  }//Goblin General
		  
		  if(c.getName().equals("Zur the Enchanter") && !c.getCreatureAttackedThisTurn())
		  {
			  PlayerZone library = AllZone.getZone(Constant.Zone.Library, c.getController());
			  PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
			  PlayerZone oppPlay = AllZone.getZone(Constant.Zone.Play, AllZone.GameAction.getOpponent(c.getController()));
			  
	          CardList enchantments = new CardList(library.getCards());   
	          enchantments = enchantments.filter(new CardListFilter(){

				public boolean addCard(Card c) {
					if (c.isEnchantment() && CardUtil.getConvertedManaCost(c.getManaCost()) <= 3)
						return true;
					else
						return false;
				}
	          });
	          
	          if (enchantments.size() > 0) {
		          if (c.getController().equals("Human"))
		          {
			          Object o = AllZone.Display.getChoiceOptional("Pick an enchantment to put into play", enchantments.toArray());
			          if(o != null)
			          {
			        	  Card crd = (Card)o;
			        	  library.remove(crd);
			        	  play.add(crd);
			        	  
			        	  if (crd.isAura())
			        	  {  
			        		  Object obj = null;
			        		  if (crd.getKeyword().contains("Enchant creature"))
			        		  {
				        		  CardList creats = new CardList(play.getCards());
				        		  creats.addAll(oppPlay.getCards());
				        		  creats = creats.getType("Creature");
				        		  obj = AllZone.Display.getChoiceOptional("Pick a creature to attach "+crd.getName() + " to",creats.toArray() );
			        		  }
			        		  else if (crd.getKeyword().contains("Enchant land") || crd.getKeyword().contains("Enchant land you control"))
			        		  {
			        			  CardList lands = new CardList(play.getCards());
				        		  //lands.addAll(oppPlay.getCards());
				        		  lands = lands.getType("Land");
				        		  if (lands.size() > 0)
				        			  obj = AllZone.Display.getChoiceOptional("Pick a land to attach "+crd.getName() + " to",lands.toArray() );
			        		  }
			        		  if (obj != null)
			        		  {
			        			  Card target = (Card)obj;
			        			  if(AllZone.GameAction.isCardInPlay(target)) {
			        	        	  crd.enchantCard(target);
			        			  }
			        		  }
			        		  
			        	  }
			        	  AllZone.GameAction.shuffle(c.getController());
			        	  //we have to have cards like glorious anthem take effect immediately:
			        	  for (String effect : AllZone.StateBasedEffects.getStateBasedMap().keySet() ) {
			      			Command com = GameActionUtil.commands.get(effect);
			      			com.execute();
			      		  }
			        	  GameActionUtil.executeCardStateEffects(); 
			        	  
			          }
		          }
		          else if (c.getController().equals("Computer"))
		          {
		        	  enchantments = enchantments.filter(new CardListFilter()
		        	  {
		        		 public boolean addCard(Card c) 
		        		 {
		        			return !c.isAura(); 
		        		 }
		        	  });
		        	  if (enchantments.size() > 0)
		        	  {
			        	  Card card = enchantments.get(0);
			        	  library.remove(card);
			        	  play.add(card);
			        	  AllZone.GameAction.shuffle(c.getController());
			        	  //we have to have cards like glorious anthem take effect immediately:
			        	  GameActionUtil.executeCardStateEffects();
		        	  }
		          }
	          } //enchantments.size > 0
		  }//Zur the enchanter
		  
		  else if(c.getName().equals("Yore-Tiller Nephilim") && !c.getCreatureAttackedThisTurn())
		  {
			  PlayerZone grave  = AllZone.getZone(Constant.Zone.Graveyard, c.getController());
			  PlayerZone play  = AllZone.getZone(Constant.Zone.Play, c.getController());
			  
			  CardList creatures = new CardList(grave.getCards());
	          creatures = creatures.getType("Creature");          
	          
	          if (creatures.size() > 0) {
	        	  if (c.getController().equals("Human"))
		          {
			          Object o = AllZone.Display.getChoiceOptional("Pick a creature to put into play", creatures.toArray());
			          if(o != null)
			          {
			        	  Card card = (Card)o;
			        	  grave.remove(card);
			        	  play.add(card);
			        	  
			        	  card.tap();
			        	  AllZone.Combat.addAttacker(card);
			        	  //the card that gets put into play tapped and attacking might trigger another ability:
			        	  //however, this turns out to be incorrect rules-wise
			        	  //checkDeclareAttackers(card); 
			          }
		          }
	        	  else if (c.getController().equals("Computer"))
		          {
		        	  Card card = creatures.get(0);
		        	  grave.remove(card);
		        	  play.add(card);
		        	  
		        	  card.tap();
		        	  AllZone.Combat.addAttacker(card);
		        	  //checkDeclareAttackers(card);
		          }
	        	  
	          } //if (creatures.size() > 0) 
		  }//Yore-Tiller Nephilim
		  else if(c.getName().equals("Flowstone Charger") && !c.getCreatureAttackedThisTurn())
		  {
			  final Card charger = c;
			  Ability ability2 = new Ability(c,"0")
			  {
				 public void resolve() 
				 {
					 
			         final Command untilEOT = new Command()
			         {
						private static final long serialVersionUID = -1703473800920781454L;

						public void execute()
			            {
			              if(AllZone.GameAction.isCardInPlay(charger))
			              {
			                charger.addTempAttackBoost(-3);
			                charger.addTempDefenseBoost(3);
			              }
			            }
			          };//Command

			          
			          if(AllZone.GameAction.isCardInPlay(charger))
			          {
			            charger.addTempAttackBoost(3);
			            charger.addTempDefenseBoost(-3);

			            AllZone.EndOfTurn.addUntil(untilEOT);
			          }
				 }//resolve
				 
			  };//ability
			  
			  ability2.setStackDescription(c.getName() + " - gets +3/-3 until EOT.");
			  AllZone.Stack.add(ability2);
			  
			  
		  }//Flowstone Charger
		  
		  else if(c.getName().equals("Witch-Maw Nephilim") && !c.getCreatureAttackedThisTurn() && c.getNetAttack() >= 10)
		  {
			  final Card charger = c;
			  Ability ability2 = new Ability(c,"0")
			  {
				 public void resolve() 
				 {
					 
			         final Command untilEOT = new Command()
			         {
						private static final long serialVersionUID = -1703473800920781454L;

						public void execute()
			            {
			              if(AllZone.GameAction.isCardInPlay(charger))
			              {
			                charger.removeIntrinsicKeyword("Trample");			               
			              }
			            }
			          };//Command

			          
			          if(AllZone.GameAction.isCardInPlay(charger))
			          {
			            charger.addIntrinsicKeyword("Trample");			           

			            AllZone.EndOfTurn.addUntil(untilEOT);
			          }
				 }//resolve
				 
			  };//ability
			  
			  ability2.setStackDescription(c.getName() + " -  gains trample until end of turn if its power is 10 or greater.");
			  AllZone.Stack.add(ability2);
			  
			  
		  }//Witch-Maw Nephilim
		  
		  else if(c.getName().equals("Jedit Ojanen of Efrava") && !c.getCreatureAttackedThisTurn())
		  {
			  final Card jedit = c;
			  Ability ability2 = new Ability(c,"0")
			  {
				 public void resolve() 
				 {
					 Card card = new Card();

			          card.setOwner(jedit.getController());
			          card.setController(jedit.getController());
			          
					  card.setName("Cat Warrior");
			          card.setImageName("G 2 2 Cat Warrior");
			          card.setManaCost("G");
			          card.setToken(true);
			          card.addIntrinsicKeyword("Forestwalk");
			          
			          card.addType("Creature");
			          card.addType("Cat");
			          card.addType("Warrior");
			          card.setBaseAttack(2);
			          card.setBaseDefense(2);
			          
			          PlayerZone play = AllZone.getZone(Constant.Zone.Play, jedit.getController());
			          play.add(card);
			          
			          //(anger) :
			          GameActionUtil.executeCardStateEffects(); 
			          
				 }
			 }; //Ability
			 
			  
			 ability2.setStackDescription(c.getName() + " - put a 2/2 green Cat Warrior creature token with forestwalk into play.");
			 AllZone.Stack.add(ability2);
		  
		  }//Jedit
		  
		  else if(c.getName().equals("Preeminent Captain") && !c.getCreatureAttackedThisTurn())
		  {
			  System.out.println("Preeminent Captain Attacks");
			  PlayerZone hand  = AllZone.getZone(Constant.Zone.Hand, c.getController());
			  PlayerZone play  = AllZone.getZone(Constant.Zone.Play, c.getController());
			  
			  CardList soldiers = new CardList(hand.getCards());
	          soldiers = soldiers.getType("Soldier");          
	          
	          if (soldiers.size() > 0) {
	        	  if (c.getController().equals("Human"))
		          {
			          Object o = AllZone.Display.getChoiceOptional("Pick a soldier to put into play", soldiers.toArray());
			          if(o != null)
			          {
			        	  Card card = (Card)o;
			        	  hand.remove(card);
			        	  play.add(card);
			        	  
			        	  card.tap();
			        	  AllZone.Combat.addAttacker(card);
			        	  //the card that gets put into play tapped and attacking might trigger another ability:
			        	  //however, this turns out to be incorrect rules-wise
			        	  //checkDeclareAttackers(card); 
			        	  card.setCreatureAttackedThisTurn(true);
			          }
		          }
	        	  else if (c.getController().equals("Computer"))
		          {
		        	  Card card = soldiers.get(0);
		        	  hand.remove(card);
		        	  play.add(card);
		        	  
		        	  card.tap();
		        	  AllZone.Combat.addAttacker(card);
		        	  //checkDeclareAttackers(card);
		        	  card.setCreatureAttackedThisTurn(true);
		          }
	        	  
	          } //if (creatures.size() > 0) 
		  }//Preeminent Captain
		  
		  else if(c.getName().equals("Nemesis of Reason") && !c.getCreatureAttackedThisTurn())
	      {
	              String player =  AllZone.GameAction.getOpponent(c.getController());
	              //if (c.getController().equals(Constant.Player.Human))
	              //player="Human";
	              //else if (c.getController().equals(Constant.Player.Computer))
	              //player="Computer";
	                
	                PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
	                PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
	                CardList libList = new CardList(lib.getCards());

	                int max = 10;
	                if (libList.size() < 10)
	                   max = libList.size();
	                
	                for (int i=0;i<max;i++)
	                {
	                   Card c1 = libList.get(i);
	                   lib.remove(c1);
	                   grave.add(c1);
	                }

	      }//Nemesis of Reason
		  
		  else if(c.getName().equals("Sapling of Colfenor") && !c.getCreatureAttackedThisTurn())
		  {
			     String player =  c.getController();
			    
	             
	             PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
	             PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
	             if (lib.size() > 0 ) {
                	CardList cl = new CardList();
			    	cl.add(lib.get(0));
			    	AllZone.Display.getChoiceOptional("Top card", cl.toArray());
                                     };
	            Card top = lib.get(0);
	            if (top.getType().contains("Creature")) 
	                                                  {
	            	AllZone.GameAction.getPlayerLife(player).addLife(top.getBaseDefense());
	            	AllZone.GameAction.getPlayerLife(player).subtractLife(top.getBaseAttack());
	            	hand.add(top);
	            	lib.remove(top);
	            	                                  };
	            	                                  

		  }//Sapling of Colfenor
		  c.setCreatureAttackedThisTurn(true);
		  
	  }//if Phase = declare attackers
  }//checkDeclareAttackers
  private static void checkDeclareBlockers(Card c)
  {
	  if (AllZone.Phase.getPhase().equals("Declare Blockers"))
	  {
		  if(c.getName().equals("Jedit Ojanen of Efrava") && !c.getCreatureBlockedThisTurn())
		  {
			  Card card = new Card();

	          card.setOwner(c.getController());
	          card.setController(c.getController());

			  card.setName("Cat Warrior");
	          card.setImageName("G 2 2 Cat Warrior");
	          card.setManaCost("G");
	          card.setToken(true);
	          card.addIntrinsicKeyword("Forestwalk");
	          
	          card.addType("Creature");
	          card.addType("Cat");
	          card.addType("Warrior");
	          card.setBaseAttack(2);
	          card.setBaseDefense(2);
	          
	          PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
	          play.add(card);
	          
	          //(anger) :
	          GameActionUtil.executeCardStateEffects(); 

			 c.setCreatureBlockedThisTurn(true);
			  
		  
		  }//Jedit
		  else if(c.getName().equals("Shield Sphere") && !c.getCreatureBlockedThisTurn())
		  {				
			 //int toughness = c.getNetDefense();
    		 //c.setDefense(toughness-1);
			 c.addCounter(Counters.P0M1, 1);
			 
			 c.setCreatureBlockedThisTurn(true);
			  
			 //ability2.setStackDescription(c.getName() + " blocks and gets a 0/-1 counter.");
			 //AllZone.Stack.add(ability2);
		  
		  }//Shield Sphere
		  
		  else if(c.getName().equals("Meglonoth") && !c.getCreatureBlockedThisTurn())
		  {
			 PlayerLife oppLife = AllZone.GameAction.getPlayerLife(AllZone.GameAction.getOpponent(c.getController()));
			 oppLife.subtractLife(c.getNetAttack());
			
						 
			 c.setCreatureBlockedThisTurn(true);
			  
			 //ability2.setStackDescription(c.getName() + " blocks and deals damage equal to its power to attacking player.");
			 //AllZone.Stack.add(ability2);
		  
		  }//Shield Sphere
		  
	  }//if Phase == declare blockers
  }//checkDeclareBlockers
  
  public static void checkBlockedAttackers(Card a, Card b)
  {
	  System.out.println(a.getName() + " got blocked by " + b.getName());
	  
	  if(a.getKeyword().contains("Flanking") && !b.getKeyword().contains("Flanking"))
	  {
		  int flankingMagnitude = 0;
		  String kw = "";
		  ArrayList<String> list = a.getKeyword();
				
		  for (int i=0;i<list.size();i++)
		  {
		 	  kw = list.get(i);
			  if (kw.equals("Flanking"))
				  flankingMagnitude++;
		  }  
		  final int mag = flankingMagnitude;
		  final Card blocker = b;
		  Ability ability2 = new Ability(b,"0")
		  {
			 public void resolve() 
			 {
				 
		         final Command untilEOT = new Command()
		         {

					private static final long serialVersionUID = 7662543891117427727L;

					public void execute()
		            {
		              if(AllZone.GameAction.isCardInPlay(blocker))
		              {
		                blocker.addTempAttackBoost(mag);
		                blocker.addTempDefenseBoost(mag);
		              }
		            }
		          };//Command

		          
		          if(AllZone.GameAction.isCardInPlay(blocker))
		          {
		            blocker.addTempAttackBoost(-mag);
		            blocker.addTempDefenseBoost(-mag);

		            AllZone.EndOfTurn.addUntil(untilEOT);
		          }
			 }//resolve
			 
		  };//ability
		  b.setCreatureAttackedThisTurn(true);
		  
		  ability2.setStackDescription(b.getName() + " - gets -" +mag+"/-" + mag +" until EOT.");
		  AllZone.Stack.add(ability2);
		  
	  }//flanking
	  if((a.getName().equals("Chambered Nautilus") || a.getName().equals("Slith Strider")) && !a.getCreatureGotBlockedThisTurn())
	  {
		  String player = a.getController();
		  AllZone.GameAction.drawCard(player);  
	  }
	  else if (a.getName().equals("Corrupt Official") && !a.getCreatureGotBlockedThisTurn())
	  {
		  String opp = b.getController(); 
		  AllZone.GameAction.discardRandom(opp);
	  }
	  else if (a.getName().equals("Sylvan Basilisk"))
	  {
		  AllZone.GameAction.destroy(b);
		  System.out.println("destroyed blocker " + b.getName());
	  }
	  else if (a.getName().equals("Alley Grifters") && !a.getCreatureGotBlockedThisTurn())
	  {
		  String player = a.getController();
		  String opp = b.getController(); 
		  
		  if (player.equals(Constant.Player.Computer))
		  {
			  PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, opp);
			  CardList list = new CardList(hand.getCards());
			  
			  Object o = AllZone.Display.getChoiceOptional("Select card to discard", list.toArray());
			  if (o==null)
				  AllZone.GameAction.discardRandom(opp);
			  else
			  {
				  Card c = (Card)o;
				  AllZone.GameAction.discard(c);	
			  }
		  }
		  else //computer just discards at random
		  {
			  AllZone.GameAction.discardRandom(opp);
		  }
	  }//Alley Grifters
	  else if (a.getName().equals("Quagmire Lamprey"))
	  {
		  b.addCounter(Counters.M1M1, 1);
	  }
	  else if (a.getName().equals("Elven Warhounds"))
	  {
		  PlayerZone play = AllZone.getZone(Constant.Zone.Play, b.getController());
		  PlayerZone library = AllZone.getZone(Constant.Zone.Library, b.getController());
		  
		  play.remove(b);
		  library.add(b, 0);
	  }
	  else if (a.getName().equals("Saprazzan Heir") && !a.getCreatureBlockedThisTurn())
	  {
		  String player = a.getController();
		  AllZone.GameAction.drawCard(player);
		  AllZone.GameAction.drawCard(player);
		  AllZone.GameAction.drawCard(player);
	  }
	  else if ((a.getName().equals("Silkenfist Order") || a.getName().equals("Silkenfist Fighter")) && !a.getCreatureBlockedThisTurn())
	  {
		  a.untap();
	  }
	  
	  a.setCreatureGotBlockedThisTurn(true);
  }
}//Class CombatUtil