import java.util.*;
//import java.util.Arrays;

public class ManaPool extends Card 
{
	private ArrayList<Ability_Mana> used = new ArrayList<Ability_Mana>();
	boolean[] spendAsCless ={true,true,true,true,true,true};
	boolean spendAll = true;
/*	private int cIndex(String s)
	{
		//String c =(s.length()==1 ? s : Input_PayManaCostUtil.getColor2(s)); 
		if(s.length()!=1) throw new IllegalArgumentException(s + "isn't an indexable single character.");
		if(!colors.contains(s)) return 0;
		return colors.indexOf(s) + 1;
	}
	private String indexC(int index)
	{
		if (index == 0) return "1";
		return colors.charAt(index - 1) + "";
	}*/
	private void updateKeywords()
	{
		extrinsicKeyword.clear();
		for(char c : has.toCharArray())
		/*for(int val=0;val<6;val++)
			for(int i=0; i < has[val]; i++)*/
				extrinsicKeyword.add("ManaPool:" + c);//indexC(val));*/
	}
	private ArrayList<String> extrinsicKeyword = new ArrayList<String>();
	public ArrayList<String> getExtrinsicKeyword() {return new ArrayList<String>(extrinsicKeyword);}
	public void setExtrinsicKeyword(ArrayList<String> a) 
	{
		extrinsicKeyword = new ArrayList<String>(a);
		//Arrays.fill(has, 0);
		has = "";
		for(String Manaword : extrinsicKeyword)
			if (Manaword.startsWith("ManaPool:"))
			{
				String[] cost=Manaword.split(":");
				if (cost[1].length() == 1) has+=cost[1];//[cIndex(cost[1])]++;
			}
		this.updateObservers();
	}
	public void addExtrinsicKeyword(String s) 
	{
		if (s.startsWith("ManaPool:"))
		{
			extrinsicKeyword.add(s);
			addMana(s.split(":")[1]);
		}
	}
	public void removeExtrinsicKeyword(String s) 
	{
		if (s.startsWith("ManaPool:"))
		{
			updateKeywords();
			extrinsicKeyword.remove(s);
			subtractOne(s.split(":")[1]);
			this.updateObservers();
		}
	}
	public int getExtrinsicKeywordSize() {updateKeywords(); return extrinsicKeyword.size(); }
	
	public ManaPool(String contents){this(); this.addMana(contents);}
	public ManaPool()
	{
		super();
		setName("Mana Pool");
		addIntrinsicKeyword("Shroud");
		addIntrinsicKeyword("Indestructible");
		clear();
	}
	public String getText()
	{
		//empty = true;
		String res="Mana available:\r\n";
		if (isEmpty()) return res+"None";
		//if(has[0]>0) {res+=Integer.toString(has[0]); empty = false;}
		for(char c : mcolors.toCharArray())//int j=0; j<colors.length();j++){char c=colors.charAt(j);
		{
			int n = containsColor(c);//has[cIndex(c+"")];
			if(n == 0) continue;
			if(c == '1') res += c;
			else
			{
				for(int i = 0; i< n ; i++)
					res +=c;//(c+"");
				//if (n > 0) {
				res+="("+n/*(Integer.toString(n)*/+")";// empty = false;}
			}
			res += "\r\n";
		}
		return res;		
	}
	
	public final static String colors = "WUBRG";
	public final static String mcolors = "1WUBRG";
	public boolean isEmpty(){ return has.equals(""); }
/*	private boolean empty = false;
	private int[] paid= new int[6];
	private int[] has = new int[6];*/
	String paid = "";
	String has = "";
	void sortContents()
	{
		has=sortContents(has);
		paid=sortContents(paid); 
	}
	String sortContents(String mana)
	{
		String res = "";
		for(char color : mcolors.toCharArray())
			for(char c : mana.toCharArray())
				if(c == color) mana += c;
		return res;
	}
	int containsColor(String color)
	{
		sortContents();
		if(color.length() > 1)
			throw new IllegalArgumentException(color + " is not a color");
		if(color.equals("")) return Integer.MAX_VALUE;
		return containsColor(color.charAt(0));
	}
	int containsColor(char color)
	{
		if(!has.contains(color + "")) return 0;
		return has.lastIndexOf(color) - has.indexOf(color) + 1;
	}
	
	public static String oraclize(String manaCost){
		//if(!manaCost.contains(" ")) return manaCost;
		String[] parts = manaCost.split(" ");
		String res="";
		for (String s : parts)
		{
			if (s.length()==2 && colors.contains(s.charAt(1) + "")) s=s.charAt(0)+"/"+s.charAt(1);
			if (s.length()==3) s="(" + s + ")";
			if (s.equals("S")) s="(S)";//for if/when we implement snow mana
			if (s.equals("X")) s="(X)";//X costs?
			res +=s;
		}		
		return res;
	}
	public ArrayList<String> getColors()
	  {
	    ArrayList<String> mana  = new ArrayList<String>();
	    for(char c : mcolors.toCharArray())//int i = 1; i <= 5; i++)
	    {
	    	if (containsColor(c)/*has[i]*/>0)
	    		mana.add(getColor("" + c));//olors.charAt(i-1)+""));
	    }
	    //if(has[0]>0) mana.add(Constant.Color.Colorless);
	    return mana;
	  }
	String getColor(String s){return Input_PayManaCostUtil.getColor(s);}
	public void addMana(Ability_Mana am){addMana(!am.Mana().contains("X") ? am.Mana() : am.Mana().replaceAll("X", am.getX()+""));}
	public void addMana(String mana){
		if (mana.length()<=1) {addOne(mana); return;}
		String[] cost=mana.split("");
		String Colorless = "";
		int cless = 0;
		for(String s : cost)
		{
			if(s.trim().equals("")) continue;//mana.split gave me a "" for some reason
			if(colors.contains(s))
			{ 
				addOne(s);//has[colors.indexOf(s) + 1]++;
				if (!Colorless.trim().equals(""))
				{
					try{
						cless+= Integer.parseInt(Colorless);
						Colorless="";
					}catch(NumberFormatException ex)
					{
						throw new RuntimeException("Mana_Pool : Error, noncolor mana cost is not a number - " +Colorless);
					}
				}
			}
			else Colorless+=s;
		}
		addOne(cless + "");
		//has[0]+=cless;
	}
	public void addOne(String Mana)
	{
		if(Mana.trim().equals("")) return;
//		int cInt = cIndex(Mana);
		if(Mana.length() == 1 && colors.contains(Mana))//cInt > 0)
			has+=Mana;//[cInt]++;
		else try
		{
			for(int i = Integer.parseInt(Mana); i>0;i--)//	has[cInt]+= Integer.parseInt(Mana);
				has = "1" + has;
		}
		catch(NumberFormatException ex)
		{
			throw new RuntimeException("Mana_Pool.AddOne : Error, noncolor mana cost is not a number - " + Mana);
		}
	}

	public static String[] getManaParts(Ability_Mana manaAbility){return getManaParts(manaAbility.Mana());}//wrapper
	public static String[] getManaParts(String Mana_2)//turns "G G" -> {"G","G"}, "2 UG"->"{"2","U/G"}, "B W U R G" -> {"B","W","U","R","G"}, etc.
	{
		String Mana=Mana_2;
		//if (Mana.isEmpty()) return null;
		if (Mana.trim().equals("")) return null;
		Mana=oraclize(Mana);
		try
		{
			String[] Colorless = {Integer.parseInt(Mana)+""};
			return Colorless;
		}
		catch(NumberFormatException ex)	{}
		
		ArrayList<String> res= new ArrayList<String>();
		int Colorless = 0;
		String clessString = "";
		boolean parentheses=false;
		String current="";
		
		for(int i=0; i<Mana.length();i++){char c=Mana.charAt(i);
			if (c=='('){parentheses=true; continue;}//Split cost handling ("(" +<W/U/B/R/G/2> + "/" + <W/U/B/R/G> + ")")
			else if(parentheses){
				if(c!=')') {current+=c; continue;}
				else {
					parentheses=false;
					res.add(current);
					current="";
					continue;
				}
			}
			String s = c+"";
			if(colors.contains(s))
			{
				res.add(s);
				if(clessString.trim().equals("")) continue;
				try
				{
					Colorless += Integer.parseInt(clessString);
				}
				catch(NumberFormatException ex)
				{
					throw new RuntimeException("Mana_Pool.getManaParts : Error, sum of noncolor mana parts is not a number - " + clessString);
				}
				clessString = "";
			}
			else clessString+=s;
		}
		if(Colorless > 0) res.add(0, Colorless+"");
		return res.toArray(new String[0]);
	}
	public ManaCost subtractMana(ManaCost m){
		spendAll = true;//TODO:check something? GUI?
		String mana = oraclize(m.toString());
		if (isEmpty() || mana.equals(null)) return m;
		if (mana.length()==1)
		{
			m=subtractOne(m,mana);
			return m;
		}
		String[] cost=getManaParts(m.toString());
		for(String s : cost)
			m=subtractOne(m, s);
		return m;
	}
	public ManaCost subtractMana(ManaCost m, Ability_Mana mability)
	{
		used.add(mability);
		for(String c : getManaParts(mability))
    	{
    		if(c.equals("")) continue; // some sort of glitch
    		m=subtractOne(m, c);
    	}
		return m;
	}
	public void subtractOne(String Mana){subtractOne(new ManaCost(Mana),Mana);}
	public ManaCost subtractOne(ManaCost manaCost, String Mana)
	{
		if(Mana.trim().equals("") || manaCost.toString().trim().equals("")) return manaCost;
		if(colors.contains(Mana))//Index(Mana) > 0 )
		{
			  if(!manaCost.isNeeded(Mana) || //has[cIndex(Mana)]
					  containsColor(Mana)==0) return manaCost;
			  manaCost.subtractMana(Input_PayManaCostUtil.getColor(Mana));
			  has.replaceFirst(Mana, "");//[cIndex(Mana)]--;
			  paid+=Mana;//[cIndex(Mana)]++;
		}
		else
		{
			if (!Mana.contains("(") && !Mana.equals("1"))
			{
				int cless;
				try
				{
					cless = Integer.parseInt(Mana);
				}
				catch(NumberFormatException ex)
				{
					throw new RuntimeException("Mana_Pool.SubtractOne : Error, noncolor mana cost is not a number - " + Mana);
				}
				//if (cless == 0) return manaCost;
				if(cless>totalMana()) {manaCost=subtractOne(manaCost,totalMana()+""); return manaCost;}
				else while(totalMana()>0 && cless>0)
				{
					cless--;
					subtractOne("1");
				}
			}
			else if (Mana.equals("1") && containsColor('1')>0 && manaCost.isNeeded(Constant.Color.Colorless))
			{
				has.replaceFirst("1", "");
				paid+=Mana;//[0]++;
				manaCost.subtractMana(Constant.Color.Colorless);
				return manaCost;
			}
			else
			{
				//if (has[0]>0){manaCost=subtractOne(manaCost,"1"); cless--; continue;}
				String chosen;
				ArrayList<String> choices = getColors();
				if(!Mana.equals("1"))
				{
					choices.clear();
					if(containsColor(Mana.charAt(3))>0)
						choices.add(getColor(Mana.charAt(3) + ""));
					if(Mana.charAt(1) == '2' ? choices.isEmpty() : containsColor(Mana.charAt(1))>0)
						choices.add(getColor(Mana.charAt(1) + ""));
				}
				if(choices.size() == 0) return manaCost;
				chosen = choices.get(0);
				if (choices.size()> 1)
					chosen = (String)AllZone.Display.getChoiceOptional("Choose mana to pay" + Mana, choices.toArray());
				if (chosen == null) {spendAll = false; return manaCost;}
				manaCost=subtractOne(manaCost,getColor2(chosen));
			}
		}
		return manaCost;
	}
	public String getColor2(String s){return Input_PayManaCostUtil.getColor2(s);}//wrapper

	public int hasMana(String color){
		String s =(color.length()==1? color : Input_PayManaCostUtil.getColor2(color));
		Mana_Part.checkSingleMana(s);
		return(containsColor(color));
	}
	public int totalMana(){
		/*int res = 0;
		for (int n : has)
			res += n;*/
		sortContents();
		return has.length();//res;
	}
	public void clear(){
		used.clear();
		paid = "";//Arrays.fill(paid, 0);
		has = "";//Arrays.fill(has, 0);
	}
	public void paid(){
		used.clear();
		has = "";//Arrays.fill(paid, 0);
		sortContents();
	}
	public void unpaid(){
		String hasbak = has;
		has = paid;
		if (!used.isEmpty())
		{
			for (Ability_Mana am : used)
			{
				if (am.undoable())
				{
					for(String c : getManaParts(am))//paid[cIndex(am.Mana())]--;
				    	subtractOne(c);
					am.undo();
				}
			}
				
		}
		//for(int i = 0; i < 6; i++)
			//has[i]+=paid[i];
		has += hasbak;
		paid();
	}

}