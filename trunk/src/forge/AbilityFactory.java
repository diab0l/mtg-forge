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
	private boolean isDb = false;
	
	public boolean isAbility()
	{
		return isAb;
	}
	
	public boolean isSpell()
	{
		return isSp;
	}
	
	public boolean isDrawback() {
		return isDb;
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
	
	public boolean isCurse(){
		return mapParams.containsKey("IsCurse");
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
		
		for (int aCnt = 0; aCnt < a.length; aCnt ++)
		    a[aCnt] = a[aCnt].trim();
		
		if (!(a.length > 1))
			throw new RuntimeException("AbilityFactory : getAbility -- a[] too short in " + hostCard.getName());
			
		for (int i=0; i<a.length; i++)
		{
			String aa[] = a[i].split("\\$");
			
			for (int aaCnt = 0; aaCnt < aa.length; aaCnt ++)
		        aa[aaCnt] = aa[aaCnt].trim();
			
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
		else if (mapParams.containsKey("DB")) {
			isDb = true;
			API = mapParams.get("DB");
		}
		else
			throw new RuntimeException("AbilityFactory : getAbility -- no API in " + hostCard.getName());

		if (!isDb){
			if (!mapParams.containsKey("Cost"))
				throw new RuntimeException("AbilityFactory : getAbility -- no Cost in " + hostCard.getName());
			abCost = new Ability_Cost(mapParams.get("Cost"), hostCard.getName(), isAb);
		}
		
		if (mapParams.containsKey("ValidTgts"))
		{
			hasValid = true;
			isTargeted = true;
		}
		
		if (mapParams.containsKey("Tgt"))
		{
			isTargeted = true;
		}
		
		if (isTargeted)
		{
			String min = mapParams.containsKey("TargetMin") ? mapParams.get("TargetMin") : "1";
			String max = mapParams.containsKey("TargetMax") ? mapParams.get("TargetMax") : "1";
			
			if (hasValid){
				// TgtPrompt now optional
				String prompt = mapParams.containsKey("TgtPrompt") ? mapParams.get("TgtPrompt") : "Select target " + mapParams.get("ValidTgts");
				abTgt = new Target(prompt, mapParams.get("ValidTgts").split(","), min, max);
			}
			else
				abTgt = new Target(mapParams.get("Tgt"), min, max);
			
			if (mapParams.containsKey("TgtZone"))	// if Targeting something not in play, this Key should be set
				abTgt.setZone(mapParams.get("TgtZone"));
		}
		
		hasSubAb = mapParams.containsKey("SubAbility");
		
		hasSpDesc = mapParams.containsKey("SpellDescription");		
		
		// ***********************************
		// Match API keywords
		
	      if (API.equals("DealDamage"))
	      {
			AbilityFactory_DealDamage dd = new AbilityFactory_DealDamage(this);

			if (isAb)
				SA = dd.getAbility();
			else if (isSp)
				SA = dd.getSpell();
			else if (isDb)
				SA = dd.getDrawback();
	      }
		
		if (API.equals("PutCounter")){
			if (isAb)
				SA = AbilityFactory_Counters.createAbilityPutCounters(this);
			if (isSp){
				SA = AbilityFactory_Counters.createSpellPutCounters(this);
			}
		}
		
		if (API.equals("RemoveCounter")){
			if (isAb)
				SA = AbilityFactory_Counters.createAbilityRemoveCounters(this);
			if (isSp){
				SA = AbilityFactory_Counters.createSpellRemoveCounters(this);
			}
		}
		
		if (API.equals("Proliferate")){
			if (isAb)
				SA = AbilityFactory_Counters.createAbilityProliferate(this);
			if (isSp){
				SA = AbilityFactory_Counters.createSpellProliferate(this);
			}
		}

		if (API.equals("Fetch")){
			if (isAb)
				SA = AbilityFactory_Fetch.createAbilityFetch(this);
			if (isSp){
				SA = AbilityFactory_Fetch.createSpellFetch(this);
			}
		}
		
		if (API.equals("Retrieve")){
			if (isAb)
				SA = AbilityFactory_Fetch.createAbilityRetrieve(this);
			if (isSp){
				SA = AbilityFactory_Fetch.createSpellRetrieve(this);
			}
		}
		
		if (API.equals("Pump"))
		{
			AbilityFactory_Pump afPump = new AbilityFactory_Pump(this);
			
			if (isAb)
				SA = afPump.getAbility();
			if (isSp)
				SA = afPump.getSpell();
			
			hostCard.setSVar("PlayMain1", "TRUE");
		}
		
		if (API.equals("GainLife")){
			if (isAb)
				SA = AbilityFactory_AlterLife.createAbilityGainLife(this);
			else if (isSp)
				SA = AbilityFactory_AlterLife.createSpellGainLife(this);
			else if (isDb)
				SA = AbilityFactory_AlterLife.createDrawbackGainLife(this);
		}

		if (API.equals("LoseLife")){
			if (isAb)
				SA = AbilityFactory_AlterLife.createAbilityLoseLife(this);
			else if (isSp)
				SA = AbilityFactory_AlterLife.createSpellLoseLife(this);
			else if (isDb)
				SA = AbilityFactory_AlterLife.createDrawbackLoseLife(this);
		}
		
		if (API.equals("Fog")){
			if (isAb)
				SA = AbilityFactory_Combat.createAbilityFog(this);
			if (isSp)
				SA = AbilityFactory_Combat.createSpellFog(this);
		}
		
		if (API.equals("Bounce")){
			if (isAb)
				SA = AbilityFactory_Bounce.createAbilityBounce(this);
			if (isSp)
				SA = AbilityFactory_Bounce.createSpellBounce(this);
			hostCard.setSVar("PlayMain1", "TRUE");
		}
		
		if (API.equals("Untap")){
			if (isAb)
				SA = AbilityFactory_PermanentState.createAbilityUntap(this);
			else if (isSp)
				SA = AbilityFactory_PermanentState.createSpellUntap(this);
		}
		
		if (API.equals("Tap")){
			if (isAb)
				SA = AbilityFactory_PermanentState.createAbilityTap(this);
			else if (isSp)
				SA = AbilityFactory_PermanentState.createSpellTap(this);
		}
		
		if (API.equals("Regenerate")){
			if (isAb)
				SA = AbilityFactory_Regenerate.getAbility(this);
			else if (isSp)
				SA = AbilityFactory_Regenerate.getSpell(this);
		}
		
		if (API.equals("Draw")){
			if (isAb)
				SA = AbilityFactory_ZoneAffecting.createAbilityDraw(this);
			else if (isSp)
				SA = AbilityFactory_ZoneAffecting.createSpellDraw(this);
			else if (isDb)
				SA = AbilityFactory_ZoneAffecting.createDrawbackDraw(this);
		}
		
		if (API.equals("Mill")){
			if (isAb)
				SA = AbilityFactory_ZoneAffecting.createAbilityMill(this);
			else if (isSp)
				SA = AbilityFactory_ZoneAffecting.createSpellMill(this);
			else if (isDb)
				SA = AbilityFactory_ZoneAffecting.createDrawbackMill(this);
		}
		
		if (API.equals("Destroy")){
			if (isAb)
				SA = AbilityFactory_Destroy.createAbilityDestroy(this);
			if (isSp){
				SA = AbilityFactory_Destroy.createSpellDestroy(this);
			}
		}
		
		if (API.equals("DestroyAll")){
			if (isAb)
				SA = AbilityFactory_Destroy.createAbilityDestroyAll(this);
			if (isSp){
				SA = AbilityFactory_Destroy.createSpellDestroyAll(this);
			}
		}
		
		if(API.equals("Token")){
			AbilityFactory_Token AFT = new AbilityFactory_Token(this);
			
			if(isAb)
				SA = AFT.getAbility();
			if(isSp)
				SA = AFT.getSpell();
			if(isDb)
				SA = AFT.getDrawback();
		}
		
		if (API.equals("GainControl")) {
			AbilityFactory_GainControl afControl = new AbilityFactory_GainControl(this);
			
			if (isAb)
				SA = afControl.getAbility();
			if (isSp)
				SA = afControl.getSpell();
		}
		
		if (API.equals("Discard")){
			if (isAb)
				SA = AbilityFactory_ZoneAffecting.createAbilityDiscard(this);
			else if (isSp)
				SA = AbilityFactory_ZoneAffecting.createSpellDiscard(this);
			else if (isDb)
				SA = AbilityFactory_ZoneAffecting.createDrawbackDiscard(this);
		}
		
		if(API.equals("Counter")){
			AbilityFactory_CounterMagic c = new AbilityFactory_CounterMagic(this);
			ComputerAI_counterSpells2.KeywordedCounterspells.add(hostC.getName());
			
			if(isAb)
				SA = c.getAbilityCounter(this);
			if(isSp)
				SA = c.getSpellCounter(this);
		}
		
		if (SA != null){
			if(hasSubAbility())
				SA.setSubAbility(getSubAbility());
		}
		
		// *********************************************
		// set universal properties of the SpellAbility
        if (hasSpDesc)
        {
        	StringBuilder sb = new StringBuilder();
        	
        	if (mapParams.containsKey("PrecostDesc"))
        		sb.append(mapParams.get("PrecostDesc")).append(" ");
        	if (mapParams.containsKey("CostDesc"))
        		sb.append(mapParams.get("CostDesc")).append(" ");
        	else sb.append(abCost.toString());
        	
        	sb.append(mapParams.get("SpellDescription"));
        	
        	SA.setDescription(sb.toString());
        }
        
        if (!isTargeted)
        	SA.setStackDescription(hostCard.getName());
        
        // SpellAbility_Restrictions should be added in here
        
        if (mapParams.containsKey("ActivatingZone"))
        	SA.getRestrictions().setActivateZone(mapParams.get("ActivatingZone"));
        
        if (mapParams.containsKey("SorcerySpeed"))
        	SA.getRestrictions().setSorcerySpeed(true);
        
        if (mapParams.containsKey("PlayerTurn"))
        	SA.getRestrictions().setPlayerTurn(true);
        
        if (mapParams.containsKey("OpponentTurn"))
        	SA.getRestrictions().setOpponentTurn(true);
        
        if (mapParams.containsKey("AnyPlayer"))
        	SA.getRestrictions().setAnyPlayer(true);
        
        if (mapParams.containsKey("ActivationLimit"))
        	SA.getRestrictions().setActivationLimit(Integer.parseInt(mapParams.get("ActivationLimit")));
        
        if(mapParams.containsKey("ActivationNumberSacrifice"))
        	SA.getRestrictions().setActivationNumberSacrifice(Integer.parseInt(mapParams.get("ActivationNumberSacrifice")));

        if (mapParams.containsKey("ActivatingPhases"))
        	SA.getRestrictions().setActivatePhases(mapParams.get("ActivatingPhases"));
        
        if (mapParams.containsKey("ActivatingCardsInHand"))
        	SA.getRestrictions().setActivateCardsInHand(Integer.parseInt(mapParams.get("ActivatingCardsInHand")));
        
        if (mapParams.containsKey("Threshold"))
        	SA.getRestrictions().setThreshold(true);
        
        if (mapParams.containsKey("IsPresent")){
        	SA.getRestrictions().setIsPresent(mapParams.get("IsPresent"));
        	if (mapParams.containsKey("PresentCompare"))
        		SA.getRestrictions().setPresentCompare(mapParams.get("PresentCompare"));
        }
        
        return SA;
	}
	
	// Easy creation of SubAbilities
	public Ability_Sub getSubAbility(){
		Ability_Sub abSub = null;

       String sSub = getMapParams().get("SubAbility");
       
       if (sSub.startsWith("SVar="))
          sSub = getHostCard().getSVar(sSub.split("=")[1]);
       
       if (sSub.startsWith("DB$"))
       {
          AbilityFactory afDB = new AbilityFactory();
          abSub = (Ability_Sub)afDB.getAbility(sSub, getHostCard());
       }
       else{
    	   // Older style Drawback doesn't create an abSub
    	   // on Resolution use getMapParams().get("SubAbility"); to call Drawback
       }

        return abSub;
	}
	
	public static int calculateAmount(Card card, String amount, SpellAbility ability){
		// amount can be anything, not just 'X' as long as sVar exists
		
		// If Amount is -X, strip the minus sign before looking for an SVar of that kind
		int multiplier = 1;
		if (amount.startsWith("-")){
			multiplier = -1;
			amount = amount.substring(1);
		}
		
		if (!card.getSVar(amount).equals(""))
		{
			String calcX[] = card.getSVar(amount).split("\\$");
			if (calcX.length == 1 || calcX[1].equals("none"))
				return 0;
			
			if (calcX[0].startsWith("Count"))
			{
				return CardFactoryUtil.xCount(card, calcX[1]) * multiplier;
			}
			else if (ability != null && calcX[0].startsWith("Sacrificed"))
			{
				return CardFactoryUtil.handlePaid(ability.getSacrificedCost(), calcX[1]) * multiplier;
			}
			else if (ability != null && calcX[0].startsWith("Discarded"))
			{
				return CardFactoryUtil.handlePaid(ability.getDiscardedCost(), calcX[1]) * multiplier;
			}
			else if (ability != null && calcX[0].startsWith("Targeted"))
			{
				CardList list = new CardList(ability.getTarget().getTargetCards().toArray());
				return CardFactoryUtil.handlePaid(list, calcX[1]) * multiplier;
			}
			else
				return 0;
		}

		return Integer.parseInt(amount) * multiplier;
	}
}

