package forge;
import java.util.*;

public class CardListUtil
{
  public static CardList filterToughness(CardList in, int atLeastToughness)
  {
    CardList out = new CardList();
    for(int i = 0; i < in.size(); i++)
      if(in.get(i).getNetDefense() <= atLeastToughness)
        out.add(in.get(i));

    return out;
  }

  //the higher the defense the better

  public static void sortDefense(CardList list)
  {
    Comparator<Card> com = new Comparator<Card>()
    {
      public int compare(Card a, Card b)
      {
        return b.getNetDefense() - a.getNetDefense();
      }
    };
    list.sort(com);
  }//sortDefense()

  //the higher the attack the better
  public static void sortAttack(CardList list)
  {
    Comparator<Card> com = new Comparator<Card>()
    {
      public int compare(Card a, Card b)
      {
        
        if (CombatUtil.isDoranInPlay())
        	return b.getNetDefense() - a.getNetDefense();
        else
        	return b.getNetAttack() - a.getNetAttack();
      }
    };
    list.sort(com);
  }//sortAttack()


  //the lower the attack the better
  public static void sortAttackLowFirst(CardList list)
  {
    Comparator<Card> com = new Comparator<Card>()
    {
      public int compare(Card a, Card b)
      {
        if (CombatUtil.isDoranInPlay())
        	return a.getNetDefense() - b.getNetDefense();
        else
        	return a.getNetAttack() - b.getNetAttack();
      }
    };
    list.sort(com);
  }//sortAttackLowFirst()

  public static void sortNonFlyingFirst(CardList list)
  {
    sortFlying(list);
    list.reverse();
  }//sortNonFlyingFirst

  //the creature with flying are better
  public static void sortFlying(CardList list)
  {
    Comparator<Card> com = new Comparator<Card>()
    {
      public int compare(Card a, Card b)
      {
        if(a.getKeyword().contains("Flying") && b.getKeyword().contains("Flying"))
          return 0;
        else if(a.getKeyword().contains("Flying"))
          return -1;
        else if(b.getKeyword().contains("Flying"))
          return 1;

        return 0;
      }
    };
    list.sort(com);
  }//sortFlying()
  
  //sort by keyword
  public static void sortByKeyword(CardList list, String kw)
  {
	final String keyword = kw;
    Comparator<Card> com = new Comparator<Card>()
    {
      public int compare(Card a, Card b)
      {
        if(a.getKeyword().contains(keyword) && b.getKeyword().contains(keyword))
          return 0;
        else if(a.getKeyword().contains(keyword))
          return -1;
        else if(b.getKeyword().contains(keyword))
          return 1;

        return 0;
      }
    };
    list.sort(com);
  }//sortByKeyword()
  
  public static void sortByDestroyEffect(CardList list)
  {
	  Comparator<Card> com = new Comparator<Card>()
	  {
	      public int compare(Card a, Card b)
	      {
	    	ArrayList<String> aKeywords = a.getKeyword();
	    	ArrayList<String> bKeywords = b.getKeyword();
	    	
	    	boolean aContains = false;
	    	boolean bContains = false;
	    	
	    	for (String kw : aKeywords)
	    	{
	    		if (kw.startsWith("Whenever") && kw.contains("into a graveyard from the battlefield,"))
	    		{	
	    			aContains = true;
	    			break;
	    		}
	    	}
	    	
	    	for (String kw : bKeywords)
	    	{
	    		if (kw.startsWith("Whenever") && kw.contains("into a graveyard from the battlefield,"))
	    		{	
	    			bContains = true;
	    			break;
	    		}
	    	}
	        if( aContains && bContains)
	          return 0;
	        else if(aContains)
	          return 1;
	        else if(bContains)
	          return -1;

	        return 0;
	     }
	   };
	   list.sort(com);
  }
  
  public static void sortByTapped(CardList list)
  {
	  Comparator<Card> com = new Comparator<Card>()
	  {
	      public int compare(Card a, Card b)
	      {
	    	
	        if( a.isTapped() && b.isTapped())
	          return 0;
	        else if(a.isTapped())
	          return 1;
	        else if(b.isTapped())
	          return -1;

	        return 0;
	     }
	   };
	   list.sort(com);
  }
  
  
  public static void sortCMC(CardList list)
  {
     Comparator<Card> com = new Comparator<Card>()
     {
        public int compare(Card a, Card b)
        {
           int cmcA = CardUtil.getConvertedManaCost(a.getManaCost());
           int cmcB = CardUtil.getConvertedManaCost(b.getManaCost());
           
           if (cmcA == cmcB)
              return 0;
           if (cmcA > cmcB)
              return -1;
           if (cmcB > cmcA)
              return 1;
              
           return 0;
        }
     };
     list.sort(com);
  }//sortCMC
  
  
  public static CardList getColor(CardList list, final String color)
  {
    return list.filter(new CardListFilter()
    {
      public boolean addCard(Card c)
      {
        return CardUtil.getColor(c).equals(color);
      }
    });
  }//getColor()
  
  public static CardList getGoldCards(CardList list)
  {
	  return list.filter(new CardListFilter()
	  {
		 public boolean addCard(Card c)
		 {
			 return CardUtil.getColors(c).size() >= 2;
		 }
	  });
  }
  
  public static int sumAttack(CardList c)
  {
    int attack = 0;
    
    for(int i  = 0; i < c.size(); i++){
      //if(c.get(i).isCreature() && c.get(i).hasSecondStrike()) {
       if(c.get(i).isCreature() && (!c.get(i).hasFirstStrike() || (c.get(i).hasDoubleStrike() && c.get(i).hasFirstStrike())) ) {
    	  if (!CombatUtil.isDoranInPlay())	
    		  attack += c.get(i).getNetAttack();
    	  else if(CombatUtil.isDoranInPlay())
    		  attack += c.get(i).getNetDefense();       
      }
    }
    //System.out.println("Total attack: " +attack);
    return attack;
  }//sumAttack()
  
  public static int sumFirstStrikeAttack(CardList c)
  {
    int attack = 0;
   
    for(int i  = 0; i < c.size(); i++){
      if(c.get(i).isCreature() && (c.get(i).hasFirstStrike() || c.get(i).hasDoubleStrike())) {
         if (!CombatUtil.isDoranInPlay())   
            attack += c.get(i).getNetAttack();
         else if(CombatUtil.isDoranInPlay())
            attack += c.get(i).getNetDefense(); 
      }
    }
    System.out.println("Total First Strike attack: " +attack);
    return attack;
  }//sumFirstStrikeAttack()
}