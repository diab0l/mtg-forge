package forge;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Map;
import java.util.Random;

public class ComputerUtil
{
  @SuppressWarnings("unused") // random
private static Random random = new Random();

  //if return true, go to next phase
  static public boolean playCards()
  {
    return playCards(getSpellAbility());
  }

  //if return true, go to next phase
  static public boolean playCards(SpellAbility[] all)
  {
    //not sure "playing biggest spell" matters?
    sortSpellAbilityByCost(all);
//    MyRandom.shuffle(all);

    for(int i = 0; i < all.length; i++)
    {
      if(canPayCost(all[i]) && all[i].canPlay() && all[i].canPlayAI())
      {
    	
        if(all[i].isSpell() && AllZone.GameAction.isCardInZone(all[i].getSourceCard(),AllZone.Computer_Hand))
          AllZone.Computer_Hand.remove(all[i].getSourceCard());

        if(all[i] instanceof Ability_Tap)
          all[i].getSourceCard().tap();

        payManaCost(all[i]);
        all[i].chooseTargetAI();
        all[i].getBeforePayManaAI().execute();
        AllZone.Stack.add(all[i]);

        return false;
      }
    }//while
    return true;
  }//playCards()
  
  //this is used for AI's counterspells
  final static public void playStack(SpellAbility sa)
  {
	  if (canPayCost(sa))
	  {
		  if (AllZone.GameAction.isCardInZone(sa.getSourceCard(),AllZone.Computer_Hand))
	    		AllZone.Computer_Hand.remove(sa.getSourceCard());
	  
	  
		  if (sa.getSourceCard().getKeyword().contains("Cantrip"))
		      	AllZone.GameAction.drawCard(sa.getSourceCard().getController());
		  payManaCost(sa);
		  
		  AllZone.Stack.add(sa);
	  }
	  
  }
  
  final static public void playNoStack(SpellAbility sa)
  {
    if(canPayCost(sa))
    {
      if(sa.isSpell())
      {
    	if (AllZone.GameAction.isCardInZone(sa.getSourceCard(),AllZone.Computer_Hand))
    		AllZone.Computer_Hand.remove(sa.getSourceCard());
        //probably doesn't really matter anyways
        //sa.getSourceCard().comesIntoPlay(); - messes things up, maybe for the future fix this
      }

      if(sa instanceof Ability_Tap)
        sa.getSourceCard().tap();

      if (sa.getSourceCard().getKeyword().contains("Cantrip"))
      	AllZone.GameAction.drawCard(sa.getSourceCard().getController());
      
      payManaCost(sa);
      sa.resolve();

      //destroys creatures if they have lethal damage, etc..
      AllZone.GameAction.checkStateEffects();
    }
  }//play()

  //gets Spell's of cards in hand and Abilities of cards in play
  //checks to see
  //1. if canPlay() returns true, 2. can pay for mana
  static public SpellAbility[] getSpellAbility()
  {
    CardList all = new CardList();
    all.addAll(AllZone.Computer_Play.getCards());
    all.addAll(AllZone.Computer_Hand.getCards());
    all.addAll(CardFactoryUtil.getFlashbackCards(Constant.Player.Computer).toArray());
    
    all = all.filter(new CardListFilter()
    {
      public boolean addCard(Card c)
      {
        if(c.isBasicLand())
          return false;

        return true;
      }
    });
    

    ArrayList<SpellAbility> spellAbility = new ArrayList<SpellAbility>();
    for(int outer = 0; outer < all.size(); outer++)
    {
      SpellAbility[] sa = all.get(outer).getSpellAbility();
      for(int i = 0; i < sa.length; i++)
        if(sa[i].canPlayAI() && canPayCost(sa[i]) /*&& sa[i].canPlay()*/)
          spellAbility.add(sa[i]);//this seems like it needs to be copied, not sure though
    }

    SpellAbility[] sa = new SpellAbility[spellAbility.size()];
    spellAbility.toArray(sa);
    return sa;
  }
  static public boolean canPlay(SpellAbility sa)
  {
    return sa.canPlayAI() && canPayCost(sa);
  }
  static public boolean canPayCost(SpellAbility sa)
  {
    if(sa.getManaCost().equals(("0")))
       return true;

    CardList land = getAvailableMana();
    
    if(sa.getSourceCard().isLand() /*&& sa.isTapAbility()*/)
    {
    	land.remove(sa.getSourceCard());
    }
    
    ManaCost cost = new ManaCost(sa.getManaCost());
    ArrayList<String> colors = new ArrayList<String>();

    for(int i = 0; i < land.size(); i++)
    {
      colors = getColors(land.get(i));
      int once = 0;
      
      for(int j =0; j < colors.size(); j++)
      {
	      if(cost.isNeeded(colors.get(j)) && once == 0)
	      { 
	    	//System.out.println(j + " color:" +colors.get(j));
	        cost.subtractMana(colors.get(j));
	        System.out.println("thinking, I just subtracted " + colors.get(j) + ", cost is now: " + cost.toString());
	        once++;
	      }

	      if(cost.isPaid()) {
	    	  //System.out.println("Cost is paid.");
	    	  return true;
	      }
      }
    }
    return false;
  }//canPayCost()


  static public void payManaCost(SpellAbility sa)
  {
    if(sa.getManaCost().equals(("0")))
       return;

    CardList land = getAvailableMana();
    
    //this is to prevent errors for land cards that have abilities that cost mana.
    if(sa.getSourceCard().isLand() /*&& sa.isTapAbility()*/)
    {
    	land.remove(sa.getSourceCard());
    }
    
    ManaCost cost = new ManaCost(sa.getManaCost());
    ArrayList<String> colors = new ArrayList<String>();

    for(int i = 0; i < land.size(); i++)
    {
      colors = getColors(land.get(i));
      
      for(int j = 0; j <colors.size();j++)
      {
    	  if(cost.isNeeded(colors.get(j)) && land.get(i).isUntapped())
    	  {
    		  land.get(i).tap();
    		  cost.subtractMana(colors.get(j));
    		  System.out.println("just subtracted " + colors.get(j) + ", cost is now: " + cost.toString());
    		  
    	  }
    	  if(cost.isPaid())
    	     break;
      }

      
    }
    if(! cost.isPaid())
      throw new RuntimeException("ComputerUtil : payManaCost() cost was not paid for " + sa.getSourceCard().getName());
  }//payManaCost()
  
  

  //get the color that the land could produce
  //Swamps produce Black
  /*    unused
  public static String getColor(Card land)
  {
    Map<String,String> map = new HashMap<String,String>();
    map.put("tap: add B", Constant.Color.Black);
    map.put("tap: add W", Constant.Color.White);
    map.put("tap: add G", Constant.Color.Green);
    map.put("tap: add R", Constant.Color.Red);
    map.put("tap: add U", Constant.Color.Blue);
    map.put("tap: add 1", Constant.Color.Colorless);

    //this fails on Vine Trellis and probably 9th Pain Lands
    try{
      Object o = land.getKeyword().get(0);
      return map.get(o).toString();
    }catch(Exception ex)//I hope this fixes "the problem" that I can't re-create
    {
      return Constant.Color.Colorless;
    }
  }
  */
  public static ArrayList<String> getColors(Card land)
  {
	ArrayList<String> colors = new ArrayList<String>();
	if (land.getKeyword().contains("tap: add B"))
		colors.add(Constant.Color.Black);
	if (land.getKeyword().contains("tap: add W"))
		colors.add(Constant.Color.White);
	if (land.getKeyword().contains("tap: add G"))
		colors.add(Constant.Color.Green);
	if (land.getKeyword().contains("tap: add R"))
		colors.add(Constant.Color.Red);
	if (land.getKeyword().contains("tap: add U"))
		colors.add(Constant.Color.Blue);
	if (land.getKeyword().contains("tap: add 1"))
		colors.add(Constant.Color.Colorless);
	
	return colors;		
	  
  }
/*
  //only works with mono-colored spells
  static public void payManaCost(int convertedCost)
  {
    CardList land = getAvailableMana();
    //converted colered mana requirements into colorless
    ManaCost cost = new ManaCost("" +convertedCost);
    Card c;
    for(int i = 0; i < land.size(); i++)
    {
      if(cost.isPaid())
        break;

      land.get(i).tap();
      cost.subtractMana(Constant.Color.Red);
    }//for
    if(! cost.isPaid())
      throw new RuntimeException("ComputerUtil : payManaCost() cost was not paid");
  }//payManaCost()
*/

  static public CardList getAvailableMana()
  {
    CardList list = new CardList(AllZone.Computer_Play.getCards());
    CardList mana = list.filter(new CardListFilter()
    {
      public boolean addCard(Card c)
      {
        //if(c.isCreature() && c.hasSickness())
        //  return false;

        for (Ability_Mana am : c.getBasicMana())
        	if (am.canPlay()) return true;
        return false;
      }
    });//CardListFilter
    
    CardList sortedMana = new CardList();
    
    for (int i=0; i<mana.size();i++)
    {
    	Card card = mana.get(i);
    	if (card.isBasicLand()){
    		sortedMana.add(card);
    		mana.remove(card);
    	}
    }
    for (int j=0; j<mana.size();j++)
    {
    	sortedMana.add(mana.get(j));
    }
    
    return sortedMana;
    
  }//getAvailableMana()

  //plays a land if one is available
  static public void playLand()
  {
    ArrayList<Card> landList = PlayerZoneUtil.getCardType(AllZone.Computer_Hand, "Land");
    if(! landList.isEmpty())
    {
      AllZone.Computer_Hand.remove(landList.get(0));
      AllZone.Computer_Play.add(landList.get(0));
      
      AllZone.GameAction.checkStateEffects();
    }
  }
  static public void untapDraw()
  {
    AllZone.GameAction.drawCard(Constant.Player.Computer);

    CardList permanent = new CardList(AllZone.Computer_Play.getCards());
    for(int i = 0; i < permanent.size(); i++)
      permanent.get(i).untap();
  }
  static public CardList getPossibleAttackers()
  {
	  CardList list = new CardList(AllZone.Computer_Play.getCards());
	  list = list.filter(new CardListFilter()
	  {
		public boolean addCard(Card c) {
			return c.isCreature() && CombatUtil.canAttack(c);
		}
	  });
	  return list;
  }
  static public Combat getAttackers()
  {
    ComputerUtil_Attack2 att = new ComputerUtil_Attack2(
        AllZone.Computer_Play.getCards(),
        AllZone.Human_Play.getCards()   ,  AllZone.Human_Life.getLife());

    return att.getAttackers();
  }
  static public Combat getBlockers()
  {
    ComputerUtil_Block2 block = new ComputerUtil_Block2(
      AllZone.Combat.getAttackers()   ,
      AllZone.Computer_Play.getCards(), AllZone.Computer_Life.getLife());

    return block.getBlockers();
  }
  
@SuppressWarnings("unchecked") // Comparator needs type
static void sortSpellAbilityByCost(SpellAbility sa[])
  {
    //sort from highest cost to lowest
    //we want the highest costs first
    Comparator c = new Comparator()
    {
      public int compare(Object a, Object b)
      {
        int a1 = CardUtil.getConvertedManaCost((SpellAbility)a);
        int b1 = CardUtil.getConvertedManaCost((SpellAbility)b);

        //puts creatures in front of spells
        if(((SpellAbility)a).getSourceCard().isCreature())
          a1 += 1;

        if(((SpellAbility)b).getSourceCard().isCreature())
          b1 += 1;


        return b1 - a1;
      }
    };//Comparator
    Arrays.sort(sa, c);
  }//sortSpellAbilityByCost()
}