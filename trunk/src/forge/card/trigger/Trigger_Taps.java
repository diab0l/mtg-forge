package forge.card.trigger;

import java.util.HashMap;

import forge.Card;

public class Trigger_Taps extends Trigger {

	public Trigger_Taps(HashMap<String, String> params, Card host) {
		super(params, host);
	}

	@Override
	public boolean performTest(HashMap<String, Object> runParams) 
	{
		Card tapper = (Card)runParams.get("Card");

		if(mapParams.containsKey("ValidCard"))
		{
			if(!tapper.isValidCard(mapParams.get("ValidCard").split(","), hostCard.getController(), hostCard))
			{
				return false;
			}
		}
		
		return true;
	}

	@Override
	public Trigger getCopy() {
		Trigger copy = new Trigger_Taps(mapParams,hostCard);
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
		c.setTriggeringObject("Card",runParams.get("Card"));
	}
}
