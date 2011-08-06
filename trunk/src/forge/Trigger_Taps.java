package forge;

import java.util.HashMap;

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
				System.out.println("Test failed: Untapper not valid.");
				return false;
			}
		}
		
		return true;
	}

}
