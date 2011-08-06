package forge;

import java.util.HashMap;

public class Trigger_Phase extends Trigger {

	public Trigger_Phase(HashMap<String, String> params, Card host) {
		super(params, host);
	}

	@Override
	public boolean performTest(HashMap<String, Object> runParams) {
		if(mapParams.containsKey("Phase"))
		{
			if(!mapParams.get("Phase").equals(runParams.get("Phase")))
			{
				return false;
			}
		}
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
		Trigger copy = new Trigger_Phase(mapParams,hostCard);
		if(overridingAbility != null)
		{
			copy.setOverridingAbility(overridingAbility);
		}
		copy.setName(name);
		
		return copy;
	}
	
	@Override
	public void setTriggeringObjects(Card c)
	{
        c.setTriggeringObject("Player",runParams.get("Player"));
	}
}
