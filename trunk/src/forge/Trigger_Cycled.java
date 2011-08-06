package forge;

import java.util.HashMap;

public class Trigger_Cycled extends Trigger {

	public Trigger_Cycled(HashMap<String, String> params, Card host) {
		super(params, host);
	}

	@Override
	public Trigger getCopy() {
		Trigger copy = new Trigger_Cycled(mapParams, hostCard);
		if(overridingAbility != null)
		{
			copy.setOverridingAbility(overridingAbility);
		}
		copy.setName(name);
		
		return copy;
	}

	@Override
	public Card getTriggeringCard(HashMap<String, Object> runParams) {
		return (Card)runParams.get("Card");
	}

	@Override
	public boolean performTest(HashMap<String, Object> runParams) {
		if(mapParams.containsKey("ValidCard"))
		{
			if(!matchesValid(runParams.get("Card"),mapParams.get("ValidCard").split(","), hostCard))
			{
				return false;
			}
		}
		return true;
	}

}
