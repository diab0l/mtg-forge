import java.util.*;

class SimpleCombat
{
    private HashMap<Card,CardList> map = new HashMap<Card,CardList>();
    private CardList attackers = new CardList();
    
    public SimpleCombat() {}
    public SimpleCombat(CardList attackingCreatures)
    {
	CardList a = attackingCreatures;
	for(int i = 0; i < a.size(); i++)
	    addAttacker(a.get(i));
    }
    public CardList getAttackers() {return attackers;}
    public void addAttacker(Card c)
    {
	attackers.add(c);
	map.put(c, new CardList());
    }
    public CardList getBlockers(Card attacker)
    {
	return (CardList)map.get(attacker);
    }
    public void addBlocker(Card attacker, Card blocker)
    {
	CardList list = (CardList)map.get(attacker);
	if(list == null)
	    throw new RuntimeException("SimpleCombat : addBlocker() attacker not found - " +attacker);
	
	list.add(blocker);
    }
    public CardList getUnblockedAttackers()
    {
	CardList list = new CardList();
	Iterator<Card> it = map.keySet().iterator();
	while(it.hasNext())
	{
	    Card attack = (Card) it.next();
	    CardList block = (CardList) map.get(attack);
	    if(block.size() == 0)
		list.add(attack);
	}
	
	return list;
    }
    //creatures destroy each other in combat damage
    public CardList[] combatDamage()
    {
	//aDestroy holds the number of creatures of A's that were destroyed
	CardList aDestroy = new CardList();
	CardList bDestroy = new CardList();
	
	CardList allAttackers = this.getAttackers();
	for(int i = 0; i < allAttackers.size(); i++)
	{
	    Card attack = allAttackers.get(i);
	    //for now, CardList blockers should only hold 1 Card
	    CardList blockers = (CardList) map.get(attack);
	    if(blockers.size() == 0)
	    {
	    }
	    else
	    {
	        
			Card block = blockers.get(0);
			int blockerDamage = block.getNetAttack();
			int attackerDamage = attack.getNetAttack();
	    	if(CombatUtil.isDoranInPlay()) {
	    		blockerDamage = block.getNetDefense();
	    		attackerDamage = attack.getNetDefense();
	    	}
			
			if(attack.getNetDefense() <= blockerDamage)
			    aDestroy.add(attack);
			
			if(block.getNetDefense() <= attackerDamage)
			    bDestroy.add(block);
	    }
	}//while
	return new CardList[] {aDestroy, bDestroy};
    }//combatDamage()        
    public String toString()
    {
	String s = "";
	CardList attack = this.getAttackers();
	CardList block;
	for(int i = 0; i < attack.size(); i++)
	{
	    block = this.getBlockers(attack.get(i));
	    if(block.isEmpty())
		s +=attack.get(i) +" ";
	    else
		s += attack.get(i) +" - " +block.get(0) +" ";		
	}
	
	return s;
    }
}//Combat