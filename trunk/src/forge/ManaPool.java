
package forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import forge.gui.GuiUtils;


public class ManaPool extends Card {
	// current paying moved to SpellAbility
	
	private ArrayList<Mana> floatingMana = new ArrayList<Mana>();
	private int[] floatingTotals = new int[7];	// WUBRGCS
	private final static Map<String,Integer> map = new HashMap<String,Integer>();

    public final static String colors  = "WUBRG";
    public final static String mcolors = "1WUBRG";
    private Player owner;
	
    public ManaPool(Player player) {
        super();
        updateObservers();
        owner = player;
        this.setController(player);
        setName("Mana Pool");
        addIntrinsicKeyword("Shroud");
        addIntrinsicKeyword("Indestructible");
        setImmutable(true);
        clearPool();
        map.put(Constant.Color.White, 0);
        map.put(Constant.Color.Blue, 1);
        map.put(Constant.Color.Black, 2);
        map.put(Constant.Color.Red, 3);
        map.put(Constant.Color.Green, 4);
        map.put(Constant.Color.Colorless, 5);
        map.put(Constant.Color.Snow, 6);
    }
    
    @Override
    public String getText() {
    	Mana[] pool = floatingMana.toArray(new Mana[floatingMana.size()]);
    	
    	int[] normalMana = {0,0,0,0,0,0};
    	int[] snowMana = {0,0,0,0,0,0};
    	String[] manaStrings = { Constant.Color.White,Constant.Color.Blue, Constant.Color.Black, Constant.Color.Red, Constant.Color.Green, Constant.Color.Colorless };
    	
        for(Mana m:pool)
        {
        	if (m.isSnow())
        		snowMana[map.get(m.getColor())] += m.getAmount();
        	else
        		normalMana[map.get(m.getColor())] += m.getAmount();
        }
        
        StringBuilder sbNormal = new StringBuilder();
        StringBuilder sbSnow = new StringBuilder();
        if (!isEmpty()){
	        for(int i = 0; i < 6; i++){
	        	if (i == 5){
	        		// Put colorless first
	        		if (normalMana[i] > 0)
	        			sbNormal.insert(0, normalMana[i] + " ");
	        		if (snowMana[i] > 0)
	        			sbSnow.insert(0, snowMana[i] + " ");
	        	}
	        	else{
	        		if (normalMana[i] > 0)
	        			sbNormal.append(CardUtil.getShortColor(manaStrings[i])).append("(").append(normalMana[i]).append(") ");
	        		if (snowMana[i] > 0)
	        			sbSnow.append(CardUtil.getShortColor(manaStrings[i])).append("(").append(snowMana[i]).append(") ");
	        	}
	        }
        }
        
        sbNormal.insert(0, "Mana Available:\n");
        sbSnow.insert(0, "Snow Mana Available:\n");

        return sbNormal.toString() + "\n" + sbSnow.toString();
    }
    
    public int getAmountOfColor(String color){
    	return floatingTotals[map.get(color)];
    }
    
    public int getAmountOfColor(char color){
    	return getAmountOfColor(Character.toString(color));
	}
    
    public boolean isEmpty() { return floatingMana.size() == 0; }
    
    public static String oraclize(String manaCost) {
        // converts RB to (R/B)
        String[] parts = manaCost.split(" ");
        StringBuilder res = new StringBuilder();
        for(String s:parts) {
            if(s.length() == 2 && colors.contains(s.charAt(1) + "")) s = s.charAt(0) + "/" + s.charAt(1);
            if(s.length() == 3) s = "(" + s + ")";
            if(s.equals("S")) s = "(S)";//for if/when we implement snow mana
            if(s.equals("X")) s = "(X)";//X costs?
            res.append(s);
        }
        return res.toString();
    }

    public void addManaToPool(ArrayList<Mana> pool, Mana mana){
    	pool.add(mana);
    	if (pool.equals(floatingMana)){
    		floatingTotals[map.get(mana.getColor())] += mana.getAmount();
    		if (mana.isSnow())
    			floatingTotals[map.get(Constant.Color.Snow)] += mana.getAmount();
    	}
    }
    
    public void addManaToFloating(String manaStr, Card card) {
    	ArrayList<Mana> manaList = convertStringToMana(manaStr, card);
    	for(Mana m : manaList){
    		addManaToPool(floatingMana, m);
    	}
    	AllZone.GameAction.checkStateEffects();
    }
    
    public static ArrayList<Mana> convertStringToMana(String manaStr, Card card){
    	ArrayList<Mana> manaList = new ArrayList<Mana>();
    	manaStr = manaStr.trim();
    	String[] manaArr = manaStr.split(" ");
    	
    	String color = "";
    	int total = 0;
    	int genericTotal = 0;
    	
    	for(String c : manaArr){
    		String longStr = Input_PayManaCostUtil.getLongColorString(c);
    		if (longStr.equals(Constant.Color.Colorless))
    			genericTotal += Integer.parseInt(c);
    		else if (color.equals("")){	
    			color = longStr;
    			total = 1;
    		}
    		else if (color.equals(longStr)){
    			total++;
    		}
    		else{	// more than one color generated
    			// add aggregate color
    			manaList.add(new Mana(color, total, card));
    			
    			color = longStr;
    			total = 1;
    		}
    	}
    	if (total > 0)
    		manaList.add(new Mana(color, total, card));
    	if (genericTotal > 0)	
    		manaList.add(new Mana(Constant.Color.Colorless, genericTotal, card));
    	
    	return manaList;
    }
    
    public void clearPool()
    {
    	if (floatingMana.size() == 0) return;
    	
    	if(AllZoneUtil.isCardInPlay("Omnath, Locus of Mana", owner)) {
	    	// Omnath in play, clear all non-green mana
	    	int i = 0;
	    	while(i < floatingMana.size()){
	    		if (floatingMana.get(i).isColor(Constant.Color.Green)){
	    			i++;
	    			continue;
	    		}
	    		floatingMana.remove(i);
	    	}
    	}
    	else
    		floatingMana.clear();
    }

    public Mana getManaFrom(ArrayList<Mana> pool, String manaStr)
    {
    	String[] colors = manaStr.split("/");
    	boolean wantSnow = false;
    	for(int i = 0; i < colors.length; i++){
    		colors[i] = Input_PayManaCostUtil.getLongColorString(colors[i]);
    		if (colors[i].equals(Constant.Color.Snow))
    			wantSnow = true;
    	}
    	
    	Mana choice = null;
    	ArrayList<Mana> manaChoices = new ArrayList<Mana>();
    	
    	for(Mana mana : pool){
    		if (mana.isColor(colors)){
    			if (choice == null)
    				choice = mana;
    			else if (choice.isSnow() && !mana.isSnow())
    				choice = mana;
    		}
    		else if (wantSnow && mana.isSnow()){
				if (choice == null)
					choice = mana;
				else if (choice.isColor(Constant.Color.Colorless)){
					// do nothing Snow Colorless should be used first to pay for Snow mana
				}
				else if (mana.isColor(Constant.Color.Colorless)){
					// give preference to Colorless Snow mana over Colored snow mana
					choice = mana;
				}
				else if (floatingTotals[map.get(mana.getColor())] > floatingTotals[map.get(choice.getColor())]){
					// give preference to Colored mana that there is more of to pay Snow costs
					choice = mana;
				}
    		}
    		else if (colors[0].equals(Constant.Color.Colorless)){	// colorless
    			if (choice == null && mana.isColor(Constant.Color.Colorless))
    				choice = mana;	// Colorless fits the bill nicely
    			else if (choice == null){
    				manaChoices.add(mana);
    			}
    			else if (choice.isSnow() && !mana.isSnow()){	// nonSnow colorless is better to spend than Snow colorless
    				choice = mana;
    			}
    		}
    	}
    	
    	if (choice != null)
    		return choice;
    	
    	if (colors[0].equals(Constant.Color.Colorless)){
    		if (manaChoices.size() == 1)
    			choice = manaChoices.get(0);
    		else if (manaChoices.size() > 1){
    	    	int[] normalMana = {0,0,0,0,0,0};
    	    	int[] snowMana = {0,0,0,0,0,0};
    	    	String[] manaStrings = { Constant.Color.White,Constant.Color.Blue, Constant.Color.Black, Constant.Color.Red, Constant.Color.Green, Constant.Color.Colorless };
    	    	
    			// loop through manaChoices adding 
    	    	for(Mana m : manaChoices){
    	        	if (m.isSnow())
    	        		snowMana[map.get(m.getColor())] += m.getAmount();
    	        	else
    	        		normalMana[map.get(m.getColor())] += m.getAmount();
    	    	}
    	    	
    	    	int totalMana = 0;
    	    	ArrayList<String> alChoice = new ArrayList<String>();
    	    	for(int i = 0; i < normalMana.length; i++){
    	    		totalMana += normalMana[i];
    	    		totalMana += snowMana[i];
    	    		if (normalMana[i] > 0){
    	    			alChoice.add(manaStrings[i]+"("+normalMana[i]+")");
    	    		}
    	    		if (snowMana[i] > 0){
    	    			alChoice.add("{S}"+manaStrings[i]+"("+snowMana[i]+")");
    	    		}
    	    	}
    	    	
    	    	if (alChoice.size() == 1){
    	    		choice = manaChoices.get(0);
    	    		return choice;
    	    	}
    	    	
    	    	int numColorless = Integer.parseInt(manaStr);
    	    	if (numColorless >= totalMana){
    	    		choice = manaChoices.get(0);
    	    		return choice;
    	    	}
    	    	
	    		Object o = GuiUtils.getChoiceOptional("Pay Mana from Mana Pool", alChoice.toArray());
	    		if (o != null){
	    			String ch = o.toString();
	    			boolean grabSnow = ch.startsWith("{S}");
	    			ch = ch.replace("{S}", "");
	    			
	    			ch = ch.substring(0, ch.indexOf("("));
	    			
	    	    	for(Mana m : manaChoices){
	    	        	if (m.isColor(ch) && (!grabSnow || (grabSnow && m.isSnow()))){
	    	        		if (choice == null)
	    	        			choice = m;
	    	        		else if (choice.isSnow() && !m.isSnow())
	    	        			choice = m;
	    	        	}
	    	    	}
	    		}
	    	}
	    }
    	
    	return choice;
    }
    
    public void removeManaFromFloating(ManaCost mc, Card c){
    	removeManaFrom(floatingMana, mc, c);
    }
    
    public void removeManaFrom(ArrayList<Mana> pool, ManaCost mc, Card c){
		int i = 0;
		Mana choice = null;
		boolean flag = false;
		while(i < pool.size()){
			Mana mana = pool.get(i);
			if (flag)	c = this;
			if (c == this && mc.isNeeded(mana)){
				c = mana.getSourceCard();
				flag = true;
			}
			if (mana.fromSourceCard(c)){
				choice = mana;
			}
			i++;
		}
		removeManaFrom(pool, choice);
    }
    
    public void findAndRemoveFrom(ArrayList<Mana> pool, Mana mana){
    	Mana set = null;
    	for (Mana m : pool){
    		if (m.getSourceCard().equals(mana.getSourceCard()) && m.getColor().equals(mana.getColor())){
    			set = m;
    			break;
    		}
    	}
    	removeManaFrom(pool, set);
    }
    
    public void removeManaFrom(ArrayList<Mana> pool, Mana choice){
		if (choice != null){
			if (choice.getAmount() == 1)
				pool.remove(choice);
			else
				choice.decrementAmount();
	    	if (pool.equals(floatingMana)){
	    		floatingTotals[map.get(choice.getColor())] -= choice.getAmount();
	    		if (choice.isSnow())
	    			floatingTotals[map.get(Constant.Color.Snow)] -= choice.getAmount();
	    	}
	    }
    }
    
    
    public static String[] formatMana(Ability_Mana manaAbility) {
        return formatMana(manaAbility.mana(), true);
    }//wrapper
    
    public static String[] formatMana(String Mana_2){
    	//turns "G G" -> {"G","G"}, "2 UG"->"{"2","U/G"}, "B W U R G" -> {"B","W","U","R","G"}, etc.
        return formatMana(Mana_2, false);
    }
    
    public static String[] formatMana(String Mana_2, boolean parsed) {
        String Mana = Mana_2;
        //if (Mana.isEmpty()) return null;
        if(Mana.trim().equals("")) return null;
        if(!parsed) 
        	Mana = oraclize(Mana);
        try {
            String[] Colorless = { Integer.toString(Integer.parseInt(Mana)) };
            return Colorless;
        } catch(NumberFormatException ex) {}
        
        ArrayList<String> res = new ArrayList<String>();
        int Colorless = 0;
        String clessString = "";
        boolean parentheses = false;
        String current = "";
        
        for(int i = 0; i < Mana.length(); i++) {
            char c = Mana.charAt(i);
            if(c == '(') {
                parentheses = true;
                continue;
            }//Split cost handling ("(" +<W/U/B/R/G/2> + "/" + <W/U/B/R/G> + ")")
            else if(parentheses) {
                if(c != ')') {
                    current += c;
                    continue;
                } else {
                    parentheses = false;
                    res.add(current);
                    current = "";
                    continue;
                }
            }
            String s = c + "";
            if(colors.contains(s)) {
                res.add(s);
                if(clessString.trim().equals("")) continue;
                try {
                    Colorless += Integer.parseInt(clessString.trim());
                } catch(NumberFormatException ex) {
                    throw new RuntimeException(
                            "Mana_Pool.getManaParts : Error, sum of noncolor mana parts is not a number - "
                                    + clessString);
                }
                clessString = "";
            } else clessString += s;
        }
        for(int i = 0; i < Colorless; i++)
        	res.add("1");

        return res.toArray(new String[0]);
    }
    
    private ManaCost subtractMultiple(SpellAbility sa, String[] cost, ManaCost m){
        for(String s:cost){
        	if (isEmpty())
        		break;
        	
        	int num = 1;
        	try{
        		num = Integer.parseInt(s); 
        	}
        	catch(NumberFormatException e){
        		// Not an integer, that's fine
        	}
        	
        	for(int i = 0; i < num; i++){
            	if (isEmpty())
            		break;
            		
            	m = subtractOne(sa, m, s);
        	}
        }
    	return m;
    }
    
    public ManaCost subtractMana(SpellAbility sa, ManaCost m, Ability_Mana... mAbilities) {
    	ArrayList<Ability_Mana> paidAbs = sa.getPayingManaAbilities();
    	
        if(mAbilities.length == 0) {
        	// paying from Mana Pool
            if(m.isPaid() || isEmpty()) return m;

            String[] cost = formatMana(m.toString());
            return subtractMultiple(sa, cost, m);
        }
        
        // paying via Mana Abilities
        for(Ability_Mana mability:mAbilities) {
        	paidAbs.add(mability);
            String[] cost = formatMana(mability);
            m = subtractMultiple(sa, cost, m);
        }

        return m;
    }
    
    public void subtractOne(String manaStr) {
        // Just subtract from floating, used by removeExtrinsicKeyword
    	ManaCost manaCost = new ManaCost(manaStr);
    	if (manaStr.trim().equals("") || manaCost.isPaid()) return;
    	
    	// get a mana of this type from floating, bail if none available
    	Mana mana = getManaFrom(floatingMana, manaStr);
    	if (mana == null) return;	// no matching mana in the pool
    	
    	Mana[] manaArray = mana.toSingleArray();
    	
    	for(int i = 0; i< manaArray.length; i++){
    		Mana m = manaArray[i];
	    	if (manaCost.isNeeded(m)){
	    		manaCost.payMana(m);
	    		findAndRemoveFrom(floatingMana, m);
	    	}
	    	else
	    		break;
    	}
    }
    
    public ManaCost subtractOne(SpellAbility sa, ManaCost manaCost, String manaStr) {
    	if (manaStr.trim().equals("") || manaCost.isPaid()) return manaCost;
    	
    	ArrayList<Mana> payMana = sa.getPayingMana();
    	
    	// get a mana of this type from floating, bail if none available
    	Mana mana = getManaFrom(floatingMana, manaStr);
    	if (mana == null) return manaCost;	// no matching mana in the pool
    	
    	Mana[] manaArray = mana.toSingleArray();
    	
    	for(int i = 0; i< manaArray.length; i++){
    		Mana m = manaArray[i];
	    	if (manaCost.isNeeded(m)){
	    		manaCost.payMana(m);
	    		payMana.add(m);
	    		findAndRemoveFrom(floatingMana, m);
	    	}
	    	else
	    		break;
    	}
        return manaCost;
    }
    
    public int totalMana() {
        int total = 0;
        for(Mana c:floatingMana)
        	total += c.getAmount();
        return total;
    }
    
    public void clearPay(SpellAbility ability, boolean refund) {
    	ArrayList<Ability_Mana> payAbs = ability.getPayingManaAbilities(); 
    	ArrayList<Mana> payMana = ability.getPayingMana();
    	
    	payAbs.clear();
        // move non-undoable paying mana back to floating
        if (refund){	
	        for(Mana m : payMana)
	        	addManaToPool(floatingMana, m);
        }
        
        payMana.clear();
    }
    
    public boolean accountFor(SpellAbility sa, String[] mana, Card c){
    	// todo: account for unpaying mana in payMana and floatingPool
    	ArrayList<Mana> payMana = sa.getPayingMana(); 
    	
    	ArrayList<Mana> removePaying = new ArrayList<Mana>();
    	ArrayList<Mana> removeFloating = new ArrayList<Mana>();
    	
    	int i = 0, j = 0;
    	boolean usePay = payMana.size() > 0;
    	boolean flag = false;
    	
    	String manaStr = mana[i];
    	String color = Input_PayManaCostUtil.getLongColorString(manaStr);
    	
    	if (!usePay && floatingMana.size() == 0)
    		return false;
    	
    	while(i < mana.length){
    		
    		Mana m = usePay ? payMana.get(j) : floatingMana.get(j);
    		
			if (m.fromSourceCard(c) && m.getColor().equals(color)){
				int amt = m.getColorlessAmount();
				if (amt > 0){
					int difference = Integer.parseInt(manaStr) - amt;
					if (difference > 0)
						manaStr = Integer.toString(difference);
					else{
						i += amt;
						if (i < mana.length)
							manaStr = mana[i];
					}
				}
				else{
					i += m.getAmount();
					if (i < mana.length)
						manaStr = mana[i];
				}
				color = Input_PayManaCostUtil.getLongColorString(manaStr);
				if (usePay)
					removePaying.add(m);
				else
					removeFloating.add(m);
				
				if (i == mana.length)	// If mana has been depleted, break from loop. All Accounted for!
					break;
			}
    		
    		j++;	// increase j until we reach the end of paying, then reset and use floating.
			if (usePay){
				if (payMana.size() == j){
					j = 0;
					usePay = false;
				}
			}
			if (!usePay && floatingMana.size() == j && !flag)
				return false;
    	}
    	
			
    	for(int k = 0; k < removePaying.size(); k++){
    		removeManaFrom(payMana, removePaying.get(k));
    	}
    	for(int k = 0; k < removeFloating.size(); k++){
    		removeManaFrom(floatingMana, removeFloating.get(k));
    	}
    	return true;
    }

    
    public void unpaid(SpellAbility sa, boolean untap) {
    	// todo: having some crash in here related to undo and not tracking abilities properly
    	ArrayList<Ability_Mana> payAbs = sa.getPayingManaAbilities(); 
    	
    	// go through paidAbilities if they are undoable 
        for(Ability_Mana am:payAbs) {
            if(am.isUndoable()) {
            	String[] formattedMana = formatMana(am);
            	if (accountFor(sa, formattedMana, am.getSourceCard())){
	                am.undo();
            	}
                // else can't account let clearPay move paying back to floating
            }
        }
        
    	// move leftover pay back to floating
        clearPay(sa, true);
    }
    
    private void updateKeywords() {
        extrinsicKeyword.clear();
        for(Mana m:floatingMana)
            extrinsicKeyword.add("ManaPool:" + m.toString());
    }
    
    private ArrayList<String> extrinsicKeyword = new ArrayList<String>();
    
    @Override
    public ArrayList<String> getExtrinsicKeyword() {
        return new ArrayList<String>(extrinsicKeyword);
    }
    
    @Override
    public void addExtrinsicKeyword(String s) {
        if(s.startsWith("ManaPool:")) {
            extrinsicKeyword.add(s);
            addManaToFloating(s.split(":")[1], this);
        }
    }
    
    @Override
    public void removeExtrinsicKeyword(String s) {
        if(s.startsWith("ManaPool:")) {
            updateKeywords();
            extrinsicKeyword.remove(s);
            subtractOne(s.split(":")[1]);
            this.updateObservers();
        }
    }
    
    @Override
    public int getExtrinsicKeywordSize() {
        updateKeywords();
        return extrinsicKeyword.size();
    }
}
