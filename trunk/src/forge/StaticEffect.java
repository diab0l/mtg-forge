
package forge;


import java.util.ArrayList;
import java.util.HashMap;

public class StaticEffect {
	private Card			 	source				  			= new Card();
	private int			 		keywordNumber				  	= 0;	
    private CardList            affectedCards                	= new CardList();
	private int			 		xValue				  			= 0;
	private int					yValue							= 0;
	
	//for P/T
	private HashMap<Card, String> originalPT					= new HashMap<Card, String>();
	
	//for types
	private HashMap<Card, ArrayList<String>> types				= new HashMap<Card, ArrayList<String>>();
	private HashMap<Card, ArrayList<String>> originalTypes		= new HashMap<Card, ArrayList<String>>();
	private boolean				overwriteTypes					= false;
	
	
	//for colors
	private	String				colorDesc 						= "";
	private	HashMap<Card, Long>	timestamps						= new HashMap<Card, Long>();
	
	//original power/toughness
	public void addOriginalPT(Card c, int power, int toughness) {
		String pt = power+"/"+toughness;
		if(!originalPT.containsKey(c)) {
			originalPT.put(c, pt);
		}
	}
	
    public int getOriginalPower(Card c) {
    	int power = -1;
    	if(originalPT.containsKey(c)) {
			power = Integer.parseInt(originalPT.get(c).split("/")[0]);
		}
    	return power;
    }
    
    public int getOriginalToughness(Card c) {
    	int tough = -1;
    	if(originalPT.containsKey(c)) {
			tough = Integer.parseInt(originalPT.get(c).split("/")[1]);
		}
    	return tough;
    }
    
    public void clearAllOriginalPTs() {
    	originalPT.clear();
    }
	
	//should we overwrite types?
	public boolean isOverwriteTypes() {
		return overwriteTypes;
	}

	public void setOverwriteTypes(boolean overwriteTypes) {
		this.overwriteTypes = overwriteTypes;
	}

	//original types
	public void addOriginalType(Card c, String s) {
		if(!originalTypes.containsKey(c)) {
			ArrayList<String> list = new ArrayList<String>();
			list.add(s);
			originalTypes.put(c, list);
		}
		else originalTypes.get(c).add(s);
	}
	
	public void addOriginalTypes(Card c, ArrayList<String> s) {
		ArrayList<String> list = new ArrayList<String>(s);
		if(!originalTypes.containsKey(c)) {
			originalTypes.put(c, list);
		}
		else {
			originalTypes.remove(c);
			originalTypes.put(c, list);
		}
	}
    
    public ArrayList<String> getOriginalTypes(Card c) {
    	ArrayList<String> returnList = new ArrayList<String>();
    	if(originalTypes.containsKey(c)) {
			returnList.addAll(originalTypes.get(c));
		}
    	return returnList;
    }
    
    public void clearOriginalTypes(Card c) {
    	if(originalTypes.containsKey(c)) {
			originalTypes.get(c).clear();
		}
    }
    
    public void clearAllOriginalTypes() {
    	originalTypes.clear();
    }
	
	//statically assigned types
	public void addType(Card c, String s) {
		if(!types.containsKey(c)) {
			ArrayList<String> list = new ArrayList<String>();
			list.add(s);
			types.put(c, list);
		}
		else types.get(c).add(s);
	}
    
    public ArrayList<String> getTypes(Card c) {
    	ArrayList<String> returnList = new ArrayList<String>();
    	if(types.containsKey(c)) {
			returnList.addAll(types.get(c));
		}
    	return returnList;
    }
    
    public void removeType(Card c, String type) {
    	if(types.containsKey(c)) {
			types.get(c).remove(type);
		}
    }
    
    public void clearTypes(Card c) {
    	if(types.containsKey(c)) {
			types.get(c).clear();
		}
    }
    
    public void clearAllTypes() {
    	types.clear();
    }
	
	public String getColorDesc() {
		return colorDesc;
	}

	public void setColorDesc(String colorDesc) {
		this.colorDesc = colorDesc;
	}
    
    public HashMap<Card, Long> getTimestamps() {
		return timestamps;
	}
    
    public long getTimestamp(Card c) {
    	long stamp = -1;
    	Long l = timestamps.get(c);
    	if(null != l) {
    		stamp = l.longValue();
    	}
		return stamp;
	}
    
    public void addTimestamp(Card c, long timestamp) {
    	timestamps.put(c, new Long(timestamp));
    }
    
    public void clearTimestamps() {
    	timestamps.clear();
    }

	public void setSource(Card card) {
    	source = card;
    }
    
    public Card getSource() {
        return source;
    }
	
    public void setKeywordNumber(int i) {
    	keywordNumber = i;
    }
    
    public int getKeywordNumber() {
        return keywordNumber;
    }
    
    public CardList getAffectedCards() {
        return affectedCards;
    }
	
    public void setAffectedCards(CardList list) {
    	affectedCards = list;
    }
	
	public void setXValue(int x) {
    	xValue = x;
    }
    
    public int getXValue() {
        return xValue;
    }
    
    public void setYValue(int y) {
    	yValue = y;
    }
    
    public int getYValue() {
        return yValue;
    }
    
}//end class StaticEffect
