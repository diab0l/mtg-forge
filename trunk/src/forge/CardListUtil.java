package forge;
import java.util.ArrayList;
import java.util.Comparator;

import com.esotericsoftware.minlog.Log;

import forge.card.cardFactory.CardFactoryUtil;

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

  // sort by "best" using the EvaluateCreature function
  // the best creatures will be first in the list
  public static void sortByEvaluateCreature(CardList list)
  {
    Comparator<Card> com = new Comparator<Card>()
    {
      public int compare(Card a, Card b)
      {
    	return CardFactoryUtil.evaluateCreature(b) - CardFactoryUtil.evaluateCreature(a);
      }
    };
    list.sort(com);
  }//sortByEvaluateCreature()
  
  // sort by "best" using the EvaluateCreature function
  // the best creatures will be first in the list
  public static void sortByMostExpensive(CardList list)
  {
    Comparator<Card> com = new Comparator<Card>()
    {
      public int compare(Card a, Card b)
      {
    	return b.getCMC() - a.getCMC();
      }
    };
    list.sort(com);
  }//sortByEvaluateCreature()
  
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
        if (a.hasKeyword("Flying") && b.hasKeyword("Flying"))
          return 0;
        else if (a.hasKeyword("Flying"))
          return -1;
        else if (b.hasKeyword("Flying"))
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
        if (a.hasKeyword(keyword) && b.hasKeyword(keyword))
          return 0;
        else if (a.hasKeyword(keyword))
          return -1;
        else if (b.hasKeyword(keyword))
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
  
  public static void sortByIndestructible(CardList list)
  {
	  final ArrayList<String> arrList = new ArrayList<String>();
	  arrList.add("Timber Protector");
	  arrList.add("Eldrazi Monument");
	  
	  Comparator<Card> com = new Comparator<Card>()
	  {
	      public int compare(Card a, Card b)
	      {
	        if( arrList.contains(a.getName()) && arrList.contains(b.getName()))
	          return 0;
	        else if(arrList.contains(a.getName()))
	          return 1;
	        else if(arrList.contains(b.getName()))
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
  
  public static void sortByName(CardList list)
  {
	  Comparator<Card> com = new Comparator<Card>()
	  {
		public int compare(Card a, Card b) {
			String aName = a.getName();
			String bName = b.getName();
			
			return aName.compareTo(bName);
		}
		  
	  };
	  list.sort(com);
  }
  
  public static void sortBySelectable(CardList list, String type)
  {
	  final String t = type;
	  Comparator<Card> com = new Comparator<Card>()
	  {
	      public int compare(Card a, Card b)
	      {
	        if ( a.isType(t) 
	        		&& b.isType(t))
	          return 0;
	        else if (a.hasKeyword(t))
	          return 1;
	        else if (b.hasKeyword(t))
	          return -1;

	        return 0;
	     }
	   };
	   list.sort(com);
  }
  
  public static void sortByTextLen(CardList list)
  {
	  Comparator<Card> com = new Comparator<Card>()
	  {
		  public int compare(Card a, Card b)
		  {
			  int aLen = a.getText().length();
			  int bLen = b.getText().length();
			  
			  if (aLen == bLen)
				  return 0;
			  else if (aLen > bLen)
				  return 1;
			  else if (bLen > aLen)
				  return -1;
			
			  return 0;
		  }
	  };
	  list.sort(com);
  }
  
  //Sorts from high to low
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
        return CardUtil.getColors(c).contains(color);
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
    		  attack += c.get(i).getNetCombatDamage();    
      }
    }
    //System.out.println("Total attack: " +attack);
    return attack;
  }//sumAttack()
  
  public static int sumDefense(CardList c)
  {
    int defense = 0;
    
    for(int i  = 0; i < c.size(); i++){
      //if(c.get(i).isCreature() && c.get(i).hasSecondStrike()) {
       if(c.get(i).isCreature() )
    	  defense += c.get(i).getNetDefense();       
    }
    //System.out.println("Total attack: " +attack);
    return defense;
  }//sumAttack()
  
  public static int sumFirstStrikeAttack(CardList c)
  {
    int attack = 0;
   
    for(int i  = 0; i < c.size(); i++){
      if(c.get(i).isCreature() && (c.get(i).hasFirstStrike() || c.get(i).hasDoubleStrike())) {  
            attack += c.get(i).getNetCombatDamage();
      }
    }
    Log.debug("Total First Strike attack: " +attack);
    return attack;
  }//sumFirstStrikeAttack()

 //Get the total converted mana cost of a card list
  public static int sumCMC(CardList c)
  {
    int cmc = 0;
    
    for(int i  = 0; i < c.size(); i++){
    	  cmc += CardUtil.getConvertedManaCost(c.get(i).getManaCost());
    }
	//System.out.println("Total CMC: " +cmc);

    return cmc;

  }//sumCMC
}