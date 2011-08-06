package forge;

import java.util.HashMap;

public class AbilityFactory {
	
	private Card hostC = null;
	
	public Card getHostCard()
	{
		return hostC;
	}
		
	private HashMap<String,String> mapParams = new HashMap<String,String>();
	
	public HashMap<String,String> getMapParams()
	{
		return mapParams;
	}
	
	private boolean isAb = false;
	private boolean isSp = false;
	
	public boolean isAbility()
	{
		return isAb;
	}
	
	public boolean isSpell()
	{
		return isSp;
	}
	
	private Ability_Cost abCost = null;
	
	public Ability_Cost getAbCost()
	{
		return abCost;
	}
	
	private boolean isTargeted = false;
	private boolean hasValid = false;
	private Target abTgt = null;
	
	public boolean isTargeted()
	{
		return isTargeted;
	}
	
	public boolean hasValid()
	{
		return hasValid;
	}
	
	public Target getAbTgt()
	{
		return abTgt;
	}
	
	private boolean isCurse = false;
	public boolean isCurse(){
		return isCurse;
	}

	private boolean hasSubAb = false;
	
	public boolean hasSubAbility()
	{
		return hasSubAb;
	}
	
	private boolean hasSpDesc = false;

	public boolean hasSpDescription()
	{
		return hasSpDesc;
	}

	//*******************************************************
	
	public SpellAbility getAbility(String abString, Card hostCard){
		
		SpellAbility SA = null;
		
		hostC = hostCard;
		
		if (!(abString.length() > 0))
			throw new RuntimeException("AbilityFactory : getAbility -- abString too short in " + hostCard.getName());
		
		String a[] = abString.split("\\|");
		
		if (!(a.length > 1))
			throw new RuntimeException("AbilityFactory : getAbility -- a[] too short in " + hostCard.getName());
			
		for (int i=0; i<a.length; i++)
		{
			String aa[] = a[i].split("\\$");
			
			if (!(aa.length == 2))
				throw new RuntimeException("AbilityFactory : getAbility -- aa.length not 2 in " + hostCard.getName());
			
			mapParams.put(aa[0], aa[1]);
		}
		
		// parse universal parameters
		
		String API = "";
		if (mapParams.containsKey("AB"))
		{
			isAb = true;
			API = mapParams.get("AB");
		}
		else if (mapParams.containsKey("SP"))
		{
			isSp = true;
			API = mapParams.get("SP");
		}
		else
			throw new RuntimeException("AbilityFactory : getAbility -- no API in " + hostCard.getName());

		
		if (!mapParams.containsKey("Cost"))
			throw new RuntimeException("AbilityFactory : getAbility -- no Cost in " + hostCard.getName());
		abCost = new Ability_Cost(mapParams.get("Cost"), hostCard.getName(), isAb);
		
		
		if (mapParams.containsKey("ValidTgts"))
		{
			hasValid = true;
			isTargeted = true;
		}
		
		if (mapParams.containsKey("ValidCards"))
			hasValid = true;
		
		if (mapParams.containsKey("Tgt"))
		{
			isTargeted = true;
		}
		
		if (isTargeted)
		{
			if (hasValid)
				abTgt = new Target("TgtV", mapParams.get("TgtPrompt"), mapParams.get("ValidTgts").split(","));
			else
				abTgt = new Target(mapParams.get("Tgt"));
		}
		
		isCurse = mapParams.containsKey("IsCurse");	
		
		hasSubAb = mapParams.containsKey("SubAbility");
		
		hasSpDesc = mapParams.containsKey("SpellDescription");		
		
		// ***********************************
		// Match API keywords
		
		if (API.equals("DealDamage"))
		{
			final int NumDmg[] = {-1};
            final String NumDmgX[] = {"none"};
            String tmpND = mapParams.get("NumDmg");
            if (tmpND.length() > 0)
            {
            	if (tmpND.matches("X"))
            		NumDmgX[0] = hostCard.getSVar(tmpND.substring(1));
            	
            	else if (tmpND.matches("[0-9][0-9]?"))
            		NumDmg[0] = Integer.parseInt(tmpND);
            }

			AbilityFactory_DealDamage dd =  new AbilityFactory_DealDamage();
            
            if (isAb)
				SA = dd.getAbility(this, NumDmg[0], NumDmgX[0]);
			else if (isSp)
				SA = dd.getSpell(this, NumDmg[0], NumDmgX[0]);
			
            
		}
		
		// additional API keywords here
		if (API.equals("PutCounter")){
			if (isAb)
				SA = AbilityFactory_Counters.createAbilityPutCounters(this);
			if (isSp){
				// todo: createSpellPutCounters
			}
		}

		// *********************************************
		// set universal properties of the SpellAbility
		if (isSp){	
			// Ability_Activated sets abTgt and abCost in the constructor so this only needs to be set for Spells
			// Once Spell constructors set Tgt and abCost this block should be removed
	        if (isTargeted)
	        	SA.setTarget(abTgt);
	        
	        SA.setPayCosts(abCost);
		}
        
        if (hasSpDesc)
        {
        	String desc = mapParams.get("SpellDescription");
        	if (isAb)
        		desc = abCost.toString() + desc;
        	
        	SA.setDescription(desc);
        }

		
        return SA;
	}
	
	
}

