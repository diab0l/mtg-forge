package forge;

import java.util.ArrayList;

public class SpellAbility_Restriction {
	// A class for handling SpellAbility Restrictions. These restrictions include: 
	// Zone, Phase, OwnTurn, Speed (instant/sorcery), Amount per Turn, Player, 
	// Threshold, Metalcraft, LevelRange, etc
	// Each value will have a default, that can be overridden (mostly by AbilityFactory)
	// The CanPlay function will use these values to determine if the current game state is ok with these restrictions
	
	// default values for Sorcery speed abilities
	private String activateZone = Constant.Zone.Battlefield;

	public void setActivateZone(String zone){
		activateZone = zone;
	}
	
	public String getActivateZone(){
		return activateZone;
	}
	
	private boolean bSorcerySpeed = false;
	
	public void setSorcerySpeed(boolean bSpeed){
		bSorcerySpeed = bSpeed;
	}
	
	public boolean getSorcerySpeed(){
		return bSorcerySpeed;
	}
	
	private boolean bAnyPlayer = false;
	
	public void setAnyPlayer(boolean anyPlayer){
		bAnyPlayer = anyPlayer;
	}
	
	public boolean getAnyPlayer(){
		return bAnyPlayer;
	}
	
	private boolean bPlayerTurn = false;
	
	public void setPlayerTurn(boolean bTurn){
		bPlayerTurn = bTurn;
	}
	
	public boolean getPlayerTurn(){
		return bPlayerTurn;
	}
	
	private boolean bOpponentTurn = false;
	
	public void setOpponentTurn(boolean bTurn){
		bOpponentTurn = bTurn;
	}
	
	public boolean getOpponentTurn(){
		return bOpponentTurn;
	}
	
	private int activationLimit = -1;
	private int numberTurnActivations = 0;
	private int activationNumberSacrifice = -1;
	
	public void setActivationLimit(int limit){
		activationLimit = limit;
	}
	
	public void abilityActivated(){
		numberTurnActivations++;
	}
	
	public int getNumberTurnActivations() {
		return numberTurnActivations;
	}
	
	public void resetTurnActivations(){
		numberTurnActivations = 0;
	}
	
	public void setActivationNumberSacrifice(int num) {
		activationNumberSacrifice = num;
	}
	
	public int getActivationNumberSacrifice() {
		return activationNumberSacrifice;
	}
	
	private ArrayList<String> activatePhases = new ArrayList<String>();
	
	public void setActivatePhases(String phases){
		for(String s : phases.split(","))
			activatePhases.add(s);
	}
	
	private int nCardsInHand = -1;
	public void setActivateCardsInHand(int cards){
		nCardsInHand = cards;
	}
	
	private boolean bNeedsThreshold = false;
	public void setThreshold(boolean bThreshold){
		bNeedsThreshold = bThreshold;
	}
	
	final private static int THRESHOLD = 7;
	
	private String sIsPresent = null;
	public void setIsPresent(String present){
		sIsPresent = present;
	}
	
	private String presentCompare = "GE1";	// Default Compare to Greater or Equal to 1
	public void setPresentCompare(String compare){
		presentCompare = compare;
	}
	
	private boolean pwAbility = false;
	public void setPlaneswalker(boolean bPlaneswalker) { pwAbility = bPlaneswalker; }
	public boolean getPlaneswalker() { return pwAbility; }
	
	/*
	 * Restrictions of the future
	 * (can level Min level Max be done with isPresent?)
		int levelMin = 0;
		int levelMax = 0;
	*/
	
	SpellAbility_Restriction(){	}

	public boolean canPlay(Card c, SpellAbility sa){
		if (!AllZone.getZone(c).getZoneName().equals(activateZone))
			return false;
		
		Player activator = sa.getActivatingPlayer();
		if (activator == null){
			activator = c.getController();
			System.out.println(c.getName() + " Did not have activater set in SpellAbility_Restriction.canPlay()");
		}
		
		if (bSorcerySpeed && !Phase.canCastSorcery(activator))
			return false;
		
		if (bPlayerTurn && !AllZone.Phase.isPlayerTurn(activator))
			return false;
		
		if (bOpponentTurn && AllZone.Phase.isPlayerTurn(activator))
			return false;
		
		if (!bAnyPlayer && !activator.equals(c.getController()))
			return false;
		
		if (activationLimit != -1 && numberTurnActivations >= activationLimit)
			return false;
		
		/*if( activationNumberSacrifice != -1 && numberTurnActivations >= (activationNumberSacrifice - 1))
			return false;*/
		
		if (activatePhases.size() > 0){
			boolean isPhase = false;
			String currPhase = AllZone.Phase.getPhase();
			for(String s : activatePhases){
				if (s.equals(currPhase)){
					isPhase = true;
					break;
				}
			}
			
			if (!isPhase)
				return false;
		}
		
		if (nCardsInHand != -1){
			// Can handle Library of Alexandria, or Hellbent
			if (AllZone.getZone(Constant.Zone.Hand, activator).size() != nCardsInHand)
				return false;
		}
		
		if (bNeedsThreshold){
			// Threshold
			if (AllZone.getZone(Constant.Zone.Graveyard, activator).size() < THRESHOLD)
				return false;
		}
		
		if (sIsPresent != null){
			CardList list = AllZoneUtil.getCardsInPlay();
			
			list = list.getValidCards(sIsPresent.split(","), sa.getActivatingPlayer(), sa.getSourceCard());
			
			int right = Integer.parseInt(presentCompare.substring(2));
			int left = list.size();
			
			if (!Card.compare(left, presentCompare, right))
				return false;
		}
		
		if (pwAbility){
			// Planeswalker abilities can only be activated as Sorceries
			if (!Phase.canCastSorcery(activator))
				return false;
			
			for(SpellAbility pwAbs : c.getSpellAbility()){
				// check all abilities on card that have their planeswalker restriction set to confirm they haven't been activated
				SpellAbility_Restriction restrict = pwAbs.getRestrictions();
				if (restrict.getPlaneswalker() && restrict.getNumberTurnActivations() > 0)
					return false;
			}
		}
			
		return true;
	}
}
