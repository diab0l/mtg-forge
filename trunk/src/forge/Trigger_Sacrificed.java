package forge;

import java.util.HashMap;

public class Trigger_Sacrificed extends Trigger {

	public Trigger_Sacrificed(HashMap<String, String> params, Card host) {
		super(params, host);
	}

	@Override
	public boolean performTest(HashMap<String, Object> runParams) {
		Card sac = ((Card)runParams.get("Sacrificed"));
		if(mapParams.containsKey("ValidPlayer"))
		{
			if(!matchesValid(sac.getController(),mapParams.get("ValidPlayer").split(","),hostCard))
			{
				return false;
			}
		}
		if(mapParams.containsKey("ValidCard"))
		{
			if(!sac.isValidCard(mapParams.get("ValidCard").split(","), hostCard.getController(), hostCard))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public Trigger getCopy() {
		return new Trigger_Sacrificed(mapParams,hostCard);
	}
}
