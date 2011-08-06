package forge.card.trigger;

import java.util.HashMap;

import forge.Card;

public class Trigger_LifeLost extends Trigger {

	public Trigger_LifeLost(HashMap<String, String> params, Card host) {
		super(params, host);
	}

	@Override
	public boolean performTest(HashMap<String, Object> runParams) 
	{
		if(mapParams.containsKey("ValidPlayer"))
		{
			if(!matchesValid(runParams.get("Player"),mapParams.get("ValidPlayer").split(","),hostCard))
			{
				return false;
			}
		}
		
		return true;
	}

	@Override
	public Trigger getCopy() {
		Trigger copy =new Trigger_LifeLost(mapParams,hostCard);
		if(overridingAbility != null)
		{
			copy.setOverridingAbility(overridingAbility);
		}
		copy.setName(name);
        copy.setID(ID);
		
		return copy;
	}
	
	@Override
	public void setTriggeringObjects(Card c)
	{
		c.setTriggeringObject("LifeAmount",runParams.get("LifeAmount"));
        c.setTriggeringObject("Player",runParams.get("Player"));
	}
}
