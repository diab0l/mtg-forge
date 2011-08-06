package forge;
import java.util.*;

public class Input_PayManaCostUtil
{
  //all mana abilities start with this and typical look like "tap: add G"
  //mana abilities are Strings and are retrieved by calling card.getKeyword()
  //taps any card that has mana ability, not just land
  public static ManaCost tapCard(Card card, ManaCost manaCost)
  {
    if(card instanceof ManaPool) return ((ManaPool)card).subtractMana(manaCost);
	ArrayList<Ability_Mana> abilities = getManaAbilities(card);

    StringBuilder cneeded = new StringBuilder();
    for(String color : Constant.Color.ManaColors)
    	if(manaCost.isNeeded(color))
    		cneeded.append(getShortColorString(color));
    Iterator<Ability_Mana> it = abilities.iterator();//you can't remove unneeded abilities inside a for(am:abilities) loop :(
    while(it.hasNext())
    {
    	Ability_Mana ma = it.next();
    	if (!ma.canPlay()) it.remove();
    	else if (!canMake(ma, cneeded.toString())) it.remove();
    }
    if(abilities.isEmpty())
    	return manaCost;
    //String color;
    Ability_Mana chosen = abilities.get(0);
    if(1 < abilities.size())
    {
    	HashMap<String, Ability_Mana> ability = new HashMap<String, Ability_Mana>();
    	for(Ability_Mana am : abilities)
    		ability.put(am.toString(), am);
    	chosen = (Ability_Mana) AllZone.Display.getChoice("Choose mana ability", abilities.toArray());
    }
   {
	   if (chosen.isReflectedMana()) {
		   // Choose the mana color
		   Ability_Reflected_Mana arm = (Ability_Reflected_Mana) chosen;
		   arm.chooseManaColor();

		   // Only resolve if the choice wasn't cancelled and the mana was actually needed
		   if (arm.wasCancelled()) {
			   return manaCost;
		   } else {
			   String color = chosen.mana();
			   if (!manaCost.isNeeded(color)) {
				   // Don't tap the card if the user chose something invalid
				   arm.reset(); // Invalidate the choice
				   return manaCost;
			   }
		   }
		   // A valid choice was made -- resolve the ability and tap the card
		   arm.resolve();
		   arm.getSourceCard().tap();
	   } else {
		   AllZone.GameAction.playSpellAbility(chosen);
	   }
    	manaCost = AllZone.ManaPool.subtractMana(manaCost, chosen);
    	AllZone.Human_Play.updateObservers();//DO NOT REMOVE THIS, otherwise the cards don't always tap (copied)
    	return manaCost;	
    }

  }
  public static ArrayList<Ability_Mana> getManaAbilities(Card card)
  {
	  return card.getManaAbility();
  }
  //color is like "G", returns "Green"
  public static boolean canMake(Ability_Mana am, String mana)
  {
	  if(mana.contains("1")) return true;
	  if(mana.contains("S") && am.isSnow()) return true;
	  if(am.isReflectedMana()) {
		  for( String color:((Ability_Reflected_Mana)am).getPossibleColors()) {
			  if (mana.contains(getShortColorString(color))) {
			  return true;
			  }
		  }
		  return false;
	  }
	  for(String color : ManaPool.getManaParts(am))
  		if(mana.contains(color)) return true;
  	  return false;
  }
  

  public static String getLongColorString(String color)
  {
    Map<String, String> m = new HashMap<String, String>();
    m.put("G", Constant.Color.Green);
    m.put("R", Constant.Color.Red);
    m.put("U", Constant.Color.Blue);
    m.put("B", Constant.Color.Black);
    m.put("W", Constant.Color.White);
    m.put("S", Constant.Color.Snow);

    Object o = m.get(color);

    if(o == null)
      o = Constant.Color.Colorless;


    return o.toString();
  }
  
  public static String getShortColorString(String color)
  {
     Map<String, String> m = new HashMap<String, String>();
     m.put(Constant.Color.Green, "G");
     m.put(Constant.Color.Red, "R");
     m.put(Constant.Color.Blue, "U");
     m.put(Constant.Color.Black, "B");
     m.put(Constant.Color.White, "W");
     m.put(Constant.Color.Colorless, "1");
     m.put(Constant.Color.Snow, "S");
      
     Object o = m.get(color);
    
     return o.toString();
  }
  
}
