package forge.card.spellability;

import java.util.regex.Pattern;

import forge.AllZone;
import forge.Card;
import forge.Counters;
import forge.card.mana.ManaCost;

public class Cost {
	private boolean isAbility = true;
	
	private boolean sacCost = false;
	public boolean getSacCost() { return sacCost; }
	private String sacType = "";	// <type> or CARDNAME
	public String getSacType() { return sacType; }
	private boolean sacThis = false;
	public boolean getSacThis() { return sacThis; }
	private int sacAmount = 0;
	public int getSacAmount() { return sacAmount; }
	
	private boolean exileCost = false;
	public boolean getExileCost() { return exileCost; }
	private String exileType = "";	// <type> or CARDNAME
	public String getExileType() { return exileType; }
	private boolean exileThis = false;
	public boolean getExileThis() { return exileThis; }
	private int exileAmount = 0;
	public int getExileAmount() { return exileAmount; }
	
	private boolean exileFromHandCost = false;
	public boolean getExileFromHandCost() { return exileFromHandCost; }
	private String exileFromHandType = "";	// <type> or CARDNAME
	public String getExileFromHandType() { return exileFromHandType; }
	private boolean exileFromHandThis = false;
	public boolean getExileFromHandThis() { return exileFromHandThis; }
	private int exileFromHandAmount = 0;
	public int getExileFromHandAmount() { return exileFromHandAmount; }
	
	private boolean exileFromGraveCost = false;
	public boolean getExileFromGraveCost() { return exileFromGraveCost; }
	private String exileFromGraveType = "";	// <type> or CARDNAME
	public String getExileFromGraveType() { return exileFromGraveType; }
	private boolean exileFromGraveThis = false;
	public boolean getExileFromGraveThis() { return exileFromGraveThis; }
	private int exileFromGraveAmount = 0;
	public int getExileFromGraveAmount() { return exileFromGraveAmount; }
	
	private boolean exileFromTopCost = false;
	public boolean getExileFromTopCost() { return exileFromTopCost; }
	private String exileFromTopType = "";	// <type> or CARDNAME
	public String getExileFromTopType() { return exileFromTopType; }
	private boolean exileFromTopThis = false;
	public boolean getExileFromTopThis() { return exileFromTopThis; }
	private int exileFromTopAmount = 0;
	public int getExileFromTopAmount() { return exileFromTopAmount; }
    
	private boolean tapCost = false;
	public boolean getTap() { return tapCost; } 
	
	// future expansion of Ability_Cost class: tap untapped type
	private boolean tapXTypeCost = false;
	public boolean getTapXTypeCost() { return tapXTypeCost;}
	private int tapXTypeAmount = 0;
	public int getTapXTypeAmount() { return tapXTypeAmount; }
	private String tapXType = "";
	public String getTapXType() { return tapXType;}
	
	private boolean untapCost = false;
	public boolean getUntap() { return untapCost; } 
	
	private boolean subtractCounterCost = false;
	public boolean getSubCounter() { return subtractCounterCost; }
	
	private boolean addCounterCost = false;
	public boolean getAddCounter() { return addCounterCost; } 
	
	private int counterAmount = 0;
	public int getCounterNum() { return counterAmount; }
	private Counters counterType;
	public Counters getCounterType() { return counterType; }
	
	private boolean lifeCost = false;
	public boolean getLifeCost() { return lifeCost; }
	private int lifeAmount = 0;
	public int getLifeAmount() { return lifeAmount; }
	
	private boolean discardCost = false;
	public boolean getDiscardCost() { return discardCost; }
	private int discardAmount = 0;
	public int getDiscardAmount() { return discardAmount; }
	private String discardType = "";
	public String getDiscardType() { return discardType; }
	private boolean discardThis = false;
	public boolean getDiscardThis() { return discardThis;}
	
	private boolean returnCost = false;	// Return something to owner's hand
	public boolean getReturnCost() { return returnCost; }
	private String returnType = "";	// <type> or CARDNAME
	public String getReturnType() { return returnType; }
	private boolean returnThis = false;
	public boolean getReturnThis() { return returnThis; }
	private int returnAmount = 0;
	public int getReturnAmount() { return returnAmount; }
	
	public boolean hasNoManaCost() { return manaCost.equals("") || manaCost.equals("0"); }
	private String manaCost = "";
	public String getMana() { return manaCost; }	// Only used for Human to pay for non-X cost first
	public void setMana(String sCost) { manaCost = sCost; }
	
	public boolean hasNoXManaCost() { return manaXCost == 0; }
	private int manaXCost = 0;
	public int getXMana() { return manaXCost; }
	public void setXMana(int xCost) { manaXCost = xCost; }
	
	public boolean isOnlyManaCost() {
		return !sacCost && !exileCost && !exileFromHandCost && !exileFromGraveCost && !exileFromTopCost && !tapCost && 
			!tapXTypeCost && !untapCost && !subtractCounterCost && !addCounterCost && !lifeCost && !discardCost && !returnCost;
	}
	
	public String getTotalMana() { 
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < manaXCost; i++)
			sb.append("X ");
		
		if (!hasNoManaCost())
			sb.append(manaCost);	
		
		if (sb.toString().equals(""))
			return "0";
		
		return sb.toString().trim();
	}
	
	
	private String name;
	
	public Cost(String parse, String cardName, boolean bAbility)
	{
		isAbility = bAbility;
		// when adding new costs for cost string, place them here
		name = cardName;
		
        String tapXStr = "tapXType<";
        if (parse.contains(tapXStr))
        {
        	tapXTypeCost = true;
        	String[] splitStr = abCostParse(parse, tapXStr, 2);
        	parse = abUpdateParse(parse, tapXStr);
        	
        	tapXTypeAmount = Integer.parseInt(splitStr[0]);
        	tapXType = splitStr[1];
        }
		
		String subStr = "SubCounter<";
        if(parse.contains(subStr)) {
        	// SubCounter<NumCounters/CounterType>
        	subtractCounterCost = true;
        	String[] splitStr = abCostParse(parse, subStr, 2);
        	parse = abUpdateParse(parse, subStr);

        	counterAmount = Integer.parseInt(splitStr[0]);
        	counterType = Counters.valueOf(splitStr[1]);
        }
        
		String addStr = "AddCounter<";
        if(parse.contains(addStr)) {
        	// AddCounter<NumCounters/CounterType>
        	addCounterCost = true;
        	String[] splitStr = abCostParse(parse, addStr, 2);
        	parse = abUpdateParse(parse, addStr);

        	counterAmount = Integer.parseInt(splitStr[0]);
        	counterType = Counters.valueOf(splitStr[1]);
        }    
		
        String lifeStr = "PayLife<";
        if(parse.contains(lifeStr)) {
        	// PayLife<LifeCost>
        	lifeCost = true;
        	String[] splitStr = abCostParse(parse, lifeStr, 1);
        	parse = abUpdateParse(parse, lifeStr);
        	
        	lifeAmount = Integer.parseInt(splitStr[0]);
        }
        
        String discStr = "Discard<";
        if (parse.contains(discStr)){
        	// Discard<NumCards/DiscardType>
        	discardCost = true;
        	String[] splitStr = abCostParse(parse, discStr, 2);
        	parse = abUpdateParse(parse, discStr);
        	
        	discardAmount = Integer.parseInt(splitStr[0]);
        	discardType = splitStr[1];
        	discardThis = (discardType.equals("CARDNAME"));
        }
        
        String sacStr = "Sac<";
        if(parse.contains(sacStr)) {
        	// todo: maybe separate SacThis from SacType? not sure if any card would use both
        	sacCost = true;
        	String[] splitStr = abCostParse(parse, sacStr, 2);
        	parse = abUpdateParse(parse, sacStr);
        	
        	sacAmount = Integer.parseInt(splitStr[0]);
        	sacType = splitStr[1];
        	sacThis = (sacType.equals("CARDNAME"));
        }
        
        String exileStr = "Exile<";
        if(parse.contains(exileStr)) {
        	exileCost = true;
        	String[] splitStr = abCostParse(parse, exileStr, 2);
        	parse = abUpdateParse(parse, exileStr);
        	
        	exileAmount = Integer.parseInt(splitStr[0]);
        	exileType = splitStr[1];
        	exileThis = (exileType.equals("CARDNAME"));
        }
        
        String exileFromHandStr = "ExileFromHand<";
        if(parse.contains(exileFromHandStr)) {
        	exileFromHandCost = true;
        	String[] splitStr = abCostParse(parse, exileFromHandStr, 2);
        	parse = abUpdateParse(parse, exileFromHandStr);
        	
        	exileFromHandAmount = Integer.parseInt(splitStr[0]);
        	exileFromHandType = splitStr[1];
        	exileFromHandThis = (exileFromHandType.equals("CARDNAME"));
        }
        
        String exileFromGraveStr = "ExileFromGrave<";
        if(parse.contains(exileFromGraveStr)) {
        	exileFromGraveCost = true;
        	String[] splitStr = abCostParse(parse, exileFromGraveStr, 2);
        	parse = abUpdateParse(parse, exileFromGraveStr);
        	
        	exileFromGraveAmount = Integer.parseInt(splitStr[0]);
        	exileFromGraveType = splitStr[1];
        	exileFromGraveThis = (exileFromGraveType.equals("CARDNAME"));
        }
        
        String exileFromTopStr = "ExileFromTop<";
        if(parse.contains(exileFromTopStr)) {
        	exileFromTopCost = true;
        	String[] splitStr = abCostParse(parse, exileFromTopStr, 2);
        	parse = abUpdateParse(parse, exileFromTopStr);
        	
        	exileFromTopAmount = Integer.parseInt(splitStr[0]);
        	exileFromTopType = splitStr[1];
        	exileFromTopThis = false;
        }
        
        String returnStr = "Return<";
        if(parse.contains(returnStr)) {
        	returnCost = true;
        	String[] splitStr = abCostParse(parse, returnStr, 2);
        	parse = abUpdateParse(parse, returnStr);
        	
        	returnAmount = Integer.parseInt(splitStr[0]);
        	returnType = splitStr[1];
        	returnThis = (returnType.equals("CARDNAME"));
        }
        
        if (parse.contains("Untap")){
        	untapCost = true;
            parse = parse.replace("Untap", "").trim();
        }
        
        if (parse.contains("Q")){
        	untapCost = true;
            parse = parse.replace("Q", "").trim();
        }
        
        if(parse.contains("T")) {
            tapCost = true;
            parse = parse.replace("T", "");
            parse = parse.trim();
        }
        
        String stripXCost = parse.replaceAll("X", "");
        
        manaXCost = parse.length() - stripXCost.length();
        
        manaCost = stripXCost.trim();
        if (manaCost.equals(""))
        	manaCost = "0";
	}
	
	String[] abCostParse(String parse, String subkey, int numParse){
    	int startPos = parse.indexOf(subkey);
    	int endPos = parse.indexOf(">", startPos);
    	String str = parse.substring(startPos, endPos);
    	
    	str = str.replace(subkey, "");

		String[] splitStr = str.split("/", numParse);
		return splitStr;
	}
	
	String abUpdateParse(String parse, String subkey){
    	int startPos = parse.indexOf(subkey);
    	int endPos = parse.indexOf(">", startPos);
    	String str = parse.substring(startPos, endPos+1);
    	return parse.replace(str, "").trim();
	}
	
	public void changeCost(SpellAbility sa){
		if (getTotalMana() != "0"){	// 11/15/10 use getTotalMana() to account for X reduction
		    String mana = getTotalMana();
			manaCost = AllZone.GameAction.getSpellCostChange(sa, new ManaCost(mana)).toString();
		}
	}
	
	public void refundPaidCost(Card source){
		// prereq: isUndoable is called first
		if (tapCost)
			source.untap();
		else if (untapCost)
			source.tap();
		
		if (subtractCounterCost)
			source.addCounterFromNonEffect(counterType, counterAmount);
		else if (addCounterCost)
			source.subtractCounter(counterType, counterAmount);

		// refund chained mana abilities?
	}

	public boolean isUndoable() {
		return !(sacCost || exileCost || exileFromHandCost || exileFromGraveCost || tapXTypeCost || discardCost ||
				returnCost || lifeCost || exileFromTopCost) && hasNoXManaCost() && hasNoManaCost();
	}
	

	public boolean isReusuableResource() {
		return !(sacCost || exileCost || exileFromHandCost || tapXTypeCost || discardCost ||
				returnCost || lifeCost) && isAbility;
		// todo: add/sub counter? Maybe check if it's we're adding a positive counter, or removing a negative counter
	}
	
	public String toString()
	{
		if (isAbility)
			return abilityToString();
		else
			return spellToString(true);
	}
	
	// maybe add a conversion method that turns the amounts into words 1=a(n), 2=two etc.
	
	public String toStringAlt(){
		return spellToString(false);
	}
	
	private String spellToString(boolean bFlag) {
		StringBuilder cost = new StringBuilder();
		
		if (bFlag)
			cost.append("As an additional cost to cast ").append(name).append(", ");
		
		boolean first = true;

		if (!bFlag){
			// usually no additional mana cost for spells
			// only three Alliances cards have additional mana costs, but they are basically kicker/multikicker
			if (!getTotalMana().equals("0")){
				cost.append("pay ").append(getTotalMana());
				first = false;
			}
		}
		
		if (tapCost || untapCost){	
			// tap cost for spells will not be in this form.
		}
		
		if (subtractCounterCost || addCounterCost){
			// add counterCost only appears in this form, which is currently on supported: 
			// put a -1/-1 counter on a creature you control.
			
			// subtractCounter for spells will not be in this form

		}
		
		if (lifeCost){
			if (first)
				cost.append("pay ");
			else
				cost.append("and pay ");
			cost.append(lifeAmount);
			cost.append(" Life");

			first = false;
		}
		
		if (discardCost){
			cost.append(discardString(first));
			first = false;
		}
		
		if (sacCost){
			cost.append(sacString(first));
			first = false;
		}
		
		if (exileCost){
			cost.append(exileString(first));
			first = false;
		}
		
		if(exileFromHandCost) {
			cost.append(exileFromHandString(first));
			first = false;
		}
		
		if(exileFromGraveCost) {
			cost.append(exileFromGraveString(first));
			first = false;
		}
		
		if(exileFromTopCost) {
			cost.append(exileFromTopString(first));
			first = false;
		}
		
		if (returnCost){
			cost.append(returnString(first));
			first = false;
		}
		
		if (first)
			return "";
		
		if (bFlag)
			cost.append(".").append("\n");
		
		return cost.toString();
	}

	private String abilityToString() {
		StringBuilder cost = new StringBuilder();
		boolean first = true;
		if (manaXCost > 0){
			for(int i = 0; i < manaXCost; i++){
				cost.append("X").append(" ");
			}
			first = false;
		}
		
		if (!(manaCost.equals("0") || manaCost.equals(""))){
			cost.append(manaCost);
			first = false;
		}
		
		if (tapCost){
			if (first)
				cost.append("Tap");
			else
				cost.append(", tap");
			first = false;
		}
		
		if (untapCost){
			if (first)
				cost.append("Untap ");
			else
				cost.append(", untap");
			first = false;
		}
		
		if (tapXTypeCost){
			if (first)
				cost.append("Tap ");
			else
				cost.append(", tap ");
			cost.append(convertIntAndTypeToWords(tapXTypeAmount, "untapped " + tapXType));
			cost.append(" you control");
//			cost.append(tapXType);	// needs IsValid String converter
			first = false;
		}
		
		if (subtractCounterCost){
			if (counterType.getName().equals("Loyalty"))
				cost.append("-").append(counterAmount);
			else{
				if (first)
					cost.append("Remove ");
				else
					cost.append(", remove ");

				cost.append(convertIntAndTypeToWords(counterAmount, counterType.getName() + " counter"));

				cost.append(" from ");
				cost.append(name);
			}

			first = false;
		}
		
		if (addCounterCost){
			if (counterType.getName().equals("Loyalty"))
				cost.append("+").append(counterAmount);
			else{
				if (first)
					cost.append("Put ");
				else
					cost.append(", put ");

				cost.append(convertIntAndTypeToWords(counterAmount, counterType.getName() + " counter"));

				cost.append(" on ");
				cost.append(name);
			}
			first = false;
		}
		
		if (lifeCost){
			if (first)
				cost.append("Pay ");
			else
				cost.append(", Pay ");
			cost.append(lifeAmount);
			cost.append(" Life");

			first = false;
		}
		
		if (discardCost){
			cost.append(discardString(first));
			first = false;
		}
		
		if (sacCost){
			cost.append(sacString(first));
			first = false;
		}
		
		if (exileCost){
			cost.append(exileString(first));
			first = false;
		}
		
		if (exileFromHandCost){
			cost.append(exileFromHandString(first));
			first = false;
		}
		
		if (exileFromGraveCost){
			cost.append(exileFromGraveString(first));
			first = false;
		}
		
		if( exileFromTopCost ) {
			cost.append( exileFromTopString(first) );
			first = false;
		}
		
		if (returnCost){
			cost.append(returnString(first));
			first = false;
		}
		
		if (first)	// No costs, append 0
			cost.append("0");
		
		cost.append(": ");
		return cost.toString();
	}

	public String discardString(boolean first){
		StringBuilder cost = new StringBuilder();
		if (first){
			if (isAbility)
				cost.append("Discard ");
			else
				cost.append("discard ");
		}
		else{
			if (isAbility)
				cost.append(", discard ");
			else
				cost.append("and discard ");
		}
		
		if (discardThis){
			cost.append(name);
		}
		else if (discardType.equals("Hand")){
			cost.append("your hand");
		}
		else if(discardType.equals("LastDrawn")) {
			cost.append("last drawn card");
		}
		else{
			if (!discardType.equals("Any") && !discardType.equals("Card") && !discardType.equals("Random")){
				cost.append(convertIntAndTypeToWords(discardAmount, discardType + " card"));
			} else
				cost.append(convertIntAndTypeToWords(discardAmount, "card"));
			
			if (discardType.equals("Random"))
				cost.append(" at random");
		}
		return cost.toString();
	}
	
	public String sacString(boolean first)
	{
		StringBuilder cost = new StringBuilder();
		if (first){
			if (isAbility)
				cost.append("Sacrifice ");
			else
				cost.append("sacrifice ");
		}
		else{
			cost.append(", sacrifice ");
		}
		
		if (sacType.equals("CARDNAME"))
			cost.append(name);
		else
			cost.append(convertIntAndTypeToWords(sacAmount, sacType));
			
		return cost.toString();
	}
	
	public String exileString(boolean first) {
		StringBuilder cost = new StringBuilder();
		if(first) {
			if(isAbility)
				cost.append("Exile ");
			else
				cost.append("exile ");
		}
		else {
			cost.append(", exile ");
		}
		
		if(exileType.equals("CARDNAME"))
			cost.append(name);
		else 
			cost.append(convertIntAndTypeToWords(exileAmount, exileType));
		
		return cost.toString();
	}
	
	public String exileFromHandString(boolean first) {
		StringBuilder cost = new StringBuilder();
		if(first) {
			if(isAbility)
				cost.append("Exile ");
			else
				cost.append("exile ");
		}
		else {
			cost.append(", exile ");
		}
		
		if(exileType.equals("CARDNAME"))
			cost.append(name);
		else {
			cost.append(convertIntAndTypeToWords(exileFromHandAmount, exileFromHandType));
			cost.append(" from your hand");
		}
		return cost.toString();
	}
	
	public String exileFromGraveString(boolean first) {
		StringBuilder cost = new StringBuilder();
		if(first) {
			if(isAbility)
				cost.append("Exile ");
			else
				cost.append("exile ");
		}
		else {
			cost.append(", exile ");
		}
		
		if(exileType.equals("CARDNAME"))
			cost.append(name);
		else {
			cost.append(convertIntAndTypeToWords(exileFromGraveAmount, exileFromGraveType));
			cost.append(" from your graveyard");
		}
		return cost.toString();
	}
	
	public String exileFromTopString(boolean first) {
		StringBuilder cost = new StringBuilder();
		if(first) {
			if(isAbility)
				cost.append("Exile ");
			else
				cost.append("exile ");
		}
		else {
			cost.append(", Exile ");
		}
		
		if(exileType.equals("CARDNAME"))
			cost.append(name).append(" from the top of you library");
		else {
			cost.append("the top ");
			cost.append(convertIntAndTypeToWords(exileFromTopAmount, exileFromTopType));
			cost.append(" of your library");
		}
		return cost.toString();
	}

	public String returnString(boolean first)
	{
		StringBuilder cost = new StringBuilder();
		if (first){
			if (isAbility)
				cost.append("Return ");
			else
				cost.append("return ");
		}
		else{
			cost.append(", return ");
		}
		String pronoun = "its";
		if (returnType.equals("CARDNAME"))
			cost.append(name);
		else{
			cost.append(convertIntAndTypeToWords(returnAmount, returnType));
			
			if (returnAmount > 1){
				pronoun = "their";
			}
			cost.append(" you control");
		}
		cost.append(" to ").append(pronoun).append(" owner's hand");
		return cost.toString();
	}
	
// TODO: If an Ability_Cost needs to pay more than 10 of something, fill this array as appropriate
	private static final String[] numNames = { "zero", "a", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten" }; 
	private static final Pattern vowelPattern = Pattern.compile("^[aeiou]", Pattern.CASE_INSENSITIVE);
	
	
	private String convertIntAndTypeToWords(int i, String type){
		StringBuilder sb = new StringBuilder();
		
		if (i >= numNames.length) {
			sb.append(i);
		}
		else if(1 == i && vowelPattern.matcher(type).find())
			sb.append("an"); 
		else
			sb.append(numNames[i]);
		
		sb.append(" ");
		sb.append(type);
		if (1 != i)
			sb.append("s");
		
		return sb.toString();
	}
}
