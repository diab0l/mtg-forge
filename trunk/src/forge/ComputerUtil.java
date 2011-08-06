package forge;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Map;


public class ComputerUtil
{

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
	    	all[i].setActivatingPlayer(AllZone.ComputerPlayer);
	    	if(canPayCost(all[i]) && all[i].canPlay() && all[i].canPlayAI())
	    	{
		    	
	    		if(all[i].isSpell() && AllZone.GameAction.isCardInZone(all[i].getSourceCard(),AllZone.Computer_Hand))
		        	AllZone.Computer_Hand.remove(all[i].getSourceCard());
		
		        Ability_Cost cost = all[i].getPayCosts();
		        Target tgt = all[i].getTarget();
		        
		        if (cost == null){
			        if(all[i] instanceof Ability_Tap)
			        	all[i].getSourceCard().tap();
			
			        payManaCost(all[i]);
			        all[i].chooseTargetAI();
			        all[i].getBeforePayManaAI().execute();
			        AllZone.Stack.add(all[i]);
		        }
		        else{
		        	if (tgt != null && tgt.doesTarget())
		        		all[i].chooseTargetAI();
		        	
		        	Cost_Payment pay = new Cost_Payment(cost, all[i]);
		        	pay.payComputerCosts();
		        }
		
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
	  
		  sa.setActivatingPlayer(AllZone.ComputerPlayer);
	  
		  if (sa.getSourceCard().getKeyword().contains("Draw a card."))
		      	AllZone.GameAction.drawCard(sa.getSourceCard().getController());
		  payManaCost(sa);
		  
		  AllZone.Stack.add(sa);
	  }
  }
  
  final static public void playStackFree(SpellAbility sa)
  {
	  sa.setActivatingPlayer(AllZone.ComputerPlayer);
	  
	  if (AllZone.GameAction.isCardInZone(sa.getSourceCard(),AllZone.Computer_Hand))
		  AllZone.Computer_Hand.remove(sa.getSourceCard());
	  
	  
	  if (sa.getSourceCard().getKeyword().contains("Draw a card."))
		      	AllZone.GameAction.drawCard(sa.getSourceCard().getController());
		  
	  AllZone.Stack.add(sa);
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

      sa.setActivatingPlayer(AllZone.ComputerPlayer);
      
      if(sa instanceof Ability_Tap)
        sa.getSourceCard().tap();
      
      payManaCost(sa);
      // todo(sol): if sa has targets, if all of them are invalid, counter the spell
      sa.resolve();

      if (sa.getSourceCard().getKeyword().contains("Draw a card."))
        	AllZone.GameAction.drawCard(sa.getSourceCard().getController());

      for (int i=0; i<sa.getSourceCard().getKeyword().size(); i++)
      {
      	String k = sa.getSourceCard().getKeyword().get(i);
      	if (k.startsWith("Scry"))
      	{
      		String kk[] = k.split(" ");
      		AllZone.GameAction.scry(sa.getSourceCard().getController(), Integer.parseInt(kk[1]));
      	}
      }

      //destroys creatures if they have lethal damage, etc..
      AllZone.GameAction.checkStateEffects();
    }
  }//play()

  //gets Spells of cards in hand and Abilities of cards in play
  //checks to see
  //1. if canPlay() returns true, 2. can pay for mana
  static public SpellAbility[] getSpellAbility()
  {
    CardList all = new CardList();
    all.addAll(AllZone.Computer_Play.getCards());
    all.addAll(AllZone.Computer_Hand.getCards());
    all.addAll(CardFactoryUtil.getFlashbackUnearthCards(AllZone.ComputerPlayer).toArray());
    
    CardList humanPlayable = new CardList();
    humanPlayable.addAll(AllZone.Human_Play.getCards());
    humanPlayable = humanPlayable.filter(new CardListFilter()
    {
      public boolean addCard(Card c)
      {
        return (c.canAnyPlayerActivate());
      }
    });
    
    all.addAll(humanPlayable.toArray());
    
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
    CardList land = getAvailableMana();
   
    if(sa.getSourceCard().isLand() /*&& sa.isTapAbility()*/)
    {
       land.remove(sa.getSourceCard());
    }
 // Beached - Delete old
    String mana = sa.getPayCosts() != null ? sa.getPayCosts().getMana() : sa.getManaCost();
    ManaCost cost = new ManaCost(mana);
    
    cost = AllZone.GameAction.GetSpellCostChange(sa, cost);
    if(cost.isPaid())
        return canPayAdditionalCosts(sa);
 // Beached - Delete old
    ArrayList<String> colors;

    for(int i = 0; i < land.size(); i++)
    {
      colors = getColors(land.get(i));
      int once = 0;
     
      for(int j =0; j < colors.size(); j++)
      {
         if(cost.isNeeded(colors.get(j)) && once == 0)
         {
          //System.out.println(j + " color:" +colors.get(j));
           cost.payMana(colors.get(j));
           //System.out.println("thinking, I just subtracted " + colors.get(j) + ", cost is now: " + cost.toString());
           once++;
         }

         if(cost.isPaid()) {
            //System.out.println("Cost is paid.");
            return canPayAdditionalCosts(sa);
         }
      }
    }
    return false;
  }//canPayCost()
  
  static public boolean canPayAdditionalCosts(SpellAbility sa)
  {
	  	// Add additional cost checks here before attempting to activate abilities
		Ability_Cost cost = sa.getPayCosts();
		if (cost == null)
			return true;
	  	Card card = sa.getSourceCard();

    	if (cost.getTap() && (card.isTapped() || card.isSick()))
    		return false;
    	
    	if (cost.getUntap() && (card.isUntapped() || card.isSick()))
    		return false;
		
		if (cost.getTapXTypeCost())
		{
			PlayerZone play = AllZone.getZone(Constant.Zone.Play, AllZone.ComputerPlayer);
			CardList typeList = new CardList(play.getCards());
			typeList = typeList.getValidCards(cost.getTapXType().split(","));
			
			if (cost.getTap())
				typeList.remove(sa.getSourceCard());
			typeList = typeList.filter( new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isUntapped();
				}
			});
			
			if (cost.getTapXTypeAmount() > typeList.size())
				return false;
		}
    	
		if (cost.getSubCounter()){
			Counters c = cost.getCounterType();
			if (card.getCounters(c) - cost.getCounterNum() < 0 || !AllZone.GameAction.isCardInPlay(card)){
				return false;
			}
		}
		
		if (cost.getLifeCost()){
			if (AllZone.ComputerPlayer.getLife() <= cost.getLifeAmount())
				return false;
		}
	  
		if (cost.getDiscardCost()){
    		PlayerZone zone = AllZone.getZone(Constant.Zone.Hand, card.getController());
    		CardList handList = new CardList(zone.getCards());
    		String discType = cost.getDiscardType();
    		int discAmount = cost.getDiscardAmount();
    		
    		if (cost.getDiscardThis()){
    			if (!AllZone.getZone(card).equals(Constant.Zone.Hand))
    				return false;
    		}
    		else if (discType.equals("Hand")){
    			// this will always work
    		}
    		else{
    			if (!discType.equals("Any") && !discType.equals("Random")){
    				String validType[] = discType.split(",");
    				handList = handList.getValidCards(validType);
    			}
	    		if (discAmount > handList.size()){
	    			// not enough cards in hand to pay
	    			return false;
	    		}
    		}
		}
		
		if (cost.getSacCost()){
			  // if there's a sacrifice in the cost, just because we can Pay it doesn't mean we want to. 
			if (!cost.getSacThis()){
			    PlayerZone play = AllZone.getZone(Constant.Zone.Play, AllZone.ComputerPlayer);
			    CardList typeList = new CardList(play.getCards());
			    typeList = typeList.getValidCards(cost.getSacType().split(","));
			    Card target = sa.getTargetCard();
				if (target != null && target.getController().equals(AllZone.ComputerPlayer)) // don't sacrifice the card we're pumping
					  typeList.remove(target);
				
				if (cost.getSacAmount() > typeList.size())
					return false;
			}
			else if (cost.getSacThis() && !AllZone.GameAction.isCardInPlay(card))
				return false;
		}
		
		if (cost.getExileCost()){
			  // if there's an exile in the cost, just because we can Pay it doesn't mean we want to. 
			if (!cost.getExileThis()){
			    PlayerZone play = AllZone.getZone(Constant.Zone.Play, AllZone.ComputerPlayer);
			    CardList typeList = new CardList(play.getCards());
			    typeList = typeList.getValidCards(cost.getExileType().split(","));
			    Card target = sa.getTargetCard();
				if (target != null && target.getController().equals(AllZone.ComputerPlayer)) // don't exile the card we're pumping
					  typeList.remove(target);
				
				if (cost.getExileAmount() > typeList.size())
					return false;
			}
			else if (cost.getExileThis() && !AllZone.GameAction.isCardInPlay(card))
				return false;
		}
		
		if (cost.getReturnCost()){
			  // if there's a return in the cost, just because we can Pay it doesn't mean we want to. 
			if (!cost.getReturnThis()){
			    PlayerZone play = AllZone.getZone(Constant.Zone.Play, AllZone.ComputerPlayer);
			    CardList typeList = new CardList(play.getCards());
			    typeList = typeList.getValidCards(cost.getReturnType().split(","));
			    Card target = sa.getTargetCard();
				if (target != null && target.getController().equals(AllZone.ComputerPlayer)) // don't bounce the card we're pumping
					  typeList.remove(target);
				
				if (cost.getReturnAmount() > typeList.size())
					return false;
			}
			else if (!AllZone.GameAction.isCardInPlay(card))
				return false;
		}
		
		return true;
  }
  
  static public boolean canPayCost(String cost)
  {
    if(cost.equals(("0")))
       return true;

    CardList land = getAvailableMana();
    
    ManaCost manacost = new ManaCost(cost);
    ArrayList<String> colors;

    for(int i = 0; i < land.size(); i++)
    {
      colors = getColors(land.get(i));
      int once = 0;
      
      for(int j =0; j < colors.size(); j++)
      {
	      if(manacost.isNeeded(colors.get(j)) && once == 0)
	      { 
	        manacost.payMana(colors.get(j));
	        once++;
	      }

	      if(manacost.isPaid()) {
	    	  return true;
	      }
      }
    }
    return false;
  }//canPayCost()


  static public void payManaCost(SpellAbility sa)
  {
    CardList land = getAvailableMana();
   
    //this is to prevent errors for land cards that have abilities that cost mana.
    if(sa.getSourceCard().isLand() /*&& sa.isTapAbility()*/)
    {
       land.remove(sa.getSourceCard());
    }
    
    String mana = sa.getPayCosts() != null ? sa.getPayCosts().getMana() : sa.getManaCost();
    
    ManaCost cost = AllZone.GameAction.GetSpellCostChange(sa, new ManaCost(mana));
    // Beached - Delete old
    if(cost.isPaid())
        return;
 // Beached - Delete old
    ArrayList<String> colors;

    for(int i = 0; i < land.size(); i++)
    {
       colors = getColors(land.get(i));
      for(int j = 0; j <colors.size();j++)
      {
         if(cost.isNeeded(colors.get(j)) && land.get(i).isUntapped())
         {
            land.get(i).tap();
            cost.payMana(colors.get(j));
            
            if (land.get(i).getName().equals("Undiscovered Paradise")) {
            	land.get(i).setBounceAtUntap(true);
            }
            
            if (land.get(i).getName().equals("Forbidden Orchard")) {
            	AllZone.Stack.add(CardFactoryUtil.getForbiddenOrchardAbility(land.get(i), AllZone.HumanPlayer));
            }
            
            //System.out.println("just subtracted " + colors.get(j) + ", cost is now: " + cost.toString());

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
	  	if (land.isReflectedLand()){
	  		// Reflected lands (Exotic Orchard and Reflecting Pool) have one
	  		// mana ability, and it has a method called 'getPossibleColors"
	  		ArrayList<Ability_Mana> amList = land.getManaAbility();
	  		colors = ((Ability_Reflected_Mana)amList.get(0)).getPossibleColors();
	  	} else {  		 
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
	  	} 	
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

        for (Ability_Mana am : c.getAIPlayableMana())
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
  static public void chooseLandsToPlay()
  {
		ArrayList<Card> landList = PlayerZoneUtil.getCardType(AllZone.Computer_Hand, "Land");
		
		if (AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer, "Crucible of Worlds").size() > 0)
		{
			CardList lands = AllZoneUtil.getPlayerTypeInGraveyard(AllZone.ComputerPlayer, "Land");
			for (Card crd : lands)
				landList.add(crd);
		}
		
		while(!landList.isEmpty() && (AllZone.GameInfo.computerNumberLandPlaysLeft() > 0 ||
		    	AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer, "Fastbond").size() > 0)){
			// play as many lands as you can
		    int ix = 0;
		    while (landList.get(ix).isReflectedLand() && (ix+1 < landList.size())) {
		    	// Skip through reflected lands. Choose last if they are all reflected.
		    	ix++;
		    }

	    	Card land = landList.get(ix);
		    landList.remove(ix);
		    playLand(land, AllZone.getZone(land));
		    
		    AllZone.GameAction.checkStateEffects();
		}
  }
  
  static public void playLand(Card land, PlayerZone zone)
  {
	    zone.remove(land);
	    AllZone.Computer_Play.add(land);
	    CardFactoryUtil.playLandEffects(land);
	    AllZone.GameInfo.incrementComputerPlayedLands();
  }
  
  static public Card chooseSacrificeType(String type, Card activate, Card target){
      PlayerZone play = AllZone.getZone(Constant.Zone.Play, AllZone.ComputerPlayer);
      CardList typeList = new CardList(play.getCards());
      typeList = typeList.getValidCards(type.split(","));
	  if (target != null && target.getController().equals(AllZone.ComputerPlayer) && typeList.contains(target)) // don't sacrifice the card we're pumping
		  typeList.remove(target);
	  
	  if (typeList.size() == 0)
		  return null;
	  
      CardListUtil.sortAttackLowFirst(typeList);
	  return typeList.get(0);
  }
  
  static public Card chooseExileType(String type, Card activate, Card target){
	  //logic is the same as sacrifice...
      return chooseSacrificeType(type, activate, target);
  }
  
  static public Card chooseTapType(String type, Card activate, boolean tap, int index){
	  PlayerZone play = AllZone.getZone(Constant.Zone.Play, AllZone.ComputerPlayer);
      CardList typeList = new CardList(play.getCards());
      typeList = typeList.getValidCards(type.split(","));
	  
      //is this needed?
      typeList = typeList.filter(new CardListFilter()
	  {
		 public boolean addCard(Card c)
		 {
			 return c.isUntapped();
		 }
	  });
      
      if (tap)
    	  typeList.remove(activate);
    	  
	  if (typeList.size() == 0 || index >= typeList.size())
		  return null;
	  
      CardListUtil.sortAttackLowFirst(typeList);
	  return typeList.get(index);
  }
  
  static public Card chooseReturnType(String type, Card activate, Card target){
      PlayerZone play = AllZone.getZone(Constant.Zone.Play, AllZone.ComputerPlayer);
      CardList typeList = new CardList(play.getCards());
      typeList = typeList.getValidCards(type.split(","));
	  if (target != null && target.getController().equals(AllZone.ComputerPlayer) && typeList.contains(target)) // don't bounce the card we're pumping
		  typeList.remove(target);
	  
	  if (typeList.size() == 0)
		  return null;
	  
      CardListUtil.sortAttackLowFirst(typeList);
	  return typeList.get(0);
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
        AllZone.Human_Play.getCards()   ,  AllZone.HumanPlayer.getLife());

    return att.getAttackers();
  }
  static public Combat getBlockers()
  {
    ComputerUtil_Block2 block = new ComputerUtil_Block2(
      AllZone.Combat.getAttackers()   ,
      AllZone.Computer_Play.getCards(), AllZone.ComputerPlayer.getLife());

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