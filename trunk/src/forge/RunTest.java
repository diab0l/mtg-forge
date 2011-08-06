package forge;
import java.util.*;

public class RunTest
{
    @SuppressWarnings("unchecked") // HashSet needs <type>
	static void test()
    {
	{
	    Card c;
	    CardFactory cf = AllZone.CardFactory;
	    //********* test Card
	    c = cf.getCard("Elvish Warrior", "a");
	    check("1", c.getOwner().equals("a"));
	    check("1.1", c.getName().equals("Elvish Warrior"));
	    check("2", c.getManaCost().equals("G G"));
	    check("2.1", c.getType().contains("Creature"));
	    check("2.2", c.getType().contains("Elf"));
	    check("2.3", c.getType().contains("Warrior"));
	    check("3", c.getText().equals(""));
	    check("4", c.getNetAttack() == 2);
	    check("5", c.getNetDefense() == 3);
	    check("6", c.getKeyword().isEmpty());
	    
	    c = cf.getCard("Shock", "");
	    check("14", c.getType().contains("Instant"));
	    //check("15", c.getText().equals("Shock deals 2 damge to target creature or player."));
	    
	    c = cf.getCard("Bayou", "");
	    check("17", c.getManaCost().equals(""));
	    check("18", c.getType().contains("Land"));
	    check("19", c.getType().contains("Swamp"));
	    check("20", c.getType().contains("Forest"));
	    
	    //********* test ManaCost
	    ManaCost manaCost = new ManaCost("G");
	    check("21", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Green);
	    check("22", manaCost.isPaid());
	    
	    manaCost = new ManaCost("7");
	    check("23", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Black);
	    check("24", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Blue);
	    check("25", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Colorless);
	    check("26", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Green);
	    check("27", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Red);
	    check("28", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.White);
	    check("29", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.White);
	    check("30", manaCost.isPaid());
	    
	    manaCost = new ManaCost("2 W W G G B B U U R R");
	    check("31", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.White);
	    check("32", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.White);
	    check("32.1", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Black);
	    check("33", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Black);
	    check("34", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Blue);
	    check("35", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Blue);
	    check("36", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Green);
	    check("37", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Green);
	    check("38", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Red);
	    check("39", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Red);
	    check("40", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Red);
	    check("41", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Blue);
	    check("42", manaCost.isPaid());
	    
	    manaCost = new ManaCost("G G");
	    check("43", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Green);
	    check("44", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Green);
	    check("45", manaCost.isPaid());
	    
	    manaCost = new ManaCost("1 U B");
	    check("45", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Black);
	    check("46", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Blue);
	    check("47", ! manaCost.isPaid());
	    manaCost.payMana(Constant.Color.Blue);
	    check("48", manaCost.isPaid());
	    
	    //********* test CardUtile.getColors()
	    c = new Card();
	    c.setManaCost("G");
	    ArrayList<?> color = CardUtil.getColors(c);
	    check("49", color.contains(Constant.Color.Green));
	    check("50", color.size() == 1);
	    
	    c = new Card();
	    c.setManaCost("W B G R U");
	    color = CardUtil.getColors(c);
	    Set set = new HashSet(color);
	    check("51", set.size() == 5);
	    check("52", color.contains(Constant.Color.Black));
	    check("53", color.contains(Constant.Color.Blue));
	    check("54", color.contains(Constant.Color.Green));
	    check("55", color.contains(Constant.Color.Red));
	    check("56", color.contains(Constant.Color.White));
	    
	    c = new Card();
	    c.setManaCost("2");
	    color = CardUtil.getColors(c);
	    check("57", color.size() == 1);
	    check("58", color.contains(Constant.Color.Colorless));
	    
	    c = new Card();
	    color = CardUtil.getColors(c);
	    check("59", color.size() == 1);
	    check("60", color.contains(Constant.Color.Colorless));
	    
	    c = new Card();
	    c.setManaCost("");
	    color = CardUtil.getColors(c);
	    check("61", color.size() == 1);
	    check("62", color.contains(Constant.Color.Colorless));
	    
	    c = cf.getCard("Bayou", "a");
	    color = CardUtil.getColors(c);
	    check("63", color.size() == 1);
	    check("64", color.contains(Constant.Color.Colorless));
	    
	    c = cf.getCard("Elvish Warrior", "a");
	    color = CardUtil.getColors(c);
	    check("65", color.size() == 1);
	    check("66", color.contains(Constant.Color.Green));
	    
	    c = new Card();
	    c.setManaCost("11 W W B B U U R R G G");
	    color = CardUtil.getColors(c);
	    check("67", color.size() == 5);
	    
	    c = new Card();
	    c = cf.getCard("Elvish Warrior", "a");
	    c.setManaCost("11");
	    color = CardUtil.getColors(c);
	    check("68", color.size() == 1);
	    check("69", color.contains(Constant.Color.Colorless));
	    
	    check("70", c.isCreature());
	    check("71", ! c.isArtifact());
	    check("72", ! c.isBasicLand());
	    check("73", ! c.isEnchantment());
	    check("74", ! c.isGlobalEnchantment());
	    check("75", ! c.isInstant());
	    check("76", ! c.isLand());
	    check("77", ! c.isLocalEnchantment());
	    check("78", c.isPermanent());
	    check("79", ! c.isSorcery());
	    check("80", ! c.isTapped());
	    check("81", c.isUntapped());
	    
	    c = cf.getCard("Swamp", "a");
	    check("82", c.isBasicLand());
	    check("83", c.isLand());
	    
	    c = cf.getCard("Bayou", "a");
	    check("84", ! c.isBasicLand());
	    check("85", c.isLand());
	    
	    c = cf.getCard("Shock", "a");
	    check("86", ! c.isCreature());
	    check("87", ! c.isArtifact());
	    check("88", ! c.isBasicLand());
	    check("89", ! c.isEnchantment());
	    check("90", ! c.isGlobalEnchantment());
	    check("91", c.isInstant());
	    check("92", ! c.isLand());
	    check("93", ! c.isLocalEnchantment());
	    check("94", ! c.isPermanent());
	    check("95", ! c.isSorcery());
	    check("96", ! c.isTapped());
	    check("97", c.isUntapped());
	    
	    //test Input_PayManaCostUtil
	    check("98", Input_PayManaCostUtil.getLongColorString("G").equals(Constant.Color.Green));
	    check("99", Input_PayManaCostUtil.getLongColorString("1").equals(Constant.Color.Colorless));
	    
	    /*
	    check("101", Input_PayManaCostUtil.isManaNeeded(Constant.Color.Green, new ManaCost("5")) == true);
	    check("102", Input_PayManaCostUtil.isManaNeeded(Constant.Color.Blue, new ManaCost("4")) == true);
	    check("103", Input_PayManaCostUtil.isManaNeeded(Constant.Color.White, new ManaCost("3")) == true);
	    check("104", Input_PayManaCostUtil.isManaNeeded(Constant.Color.Black, new ManaCost("2")) == true);
	    check("105", Input_PayManaCostUtil.isManaNeeded(Constant.Color.Red, new ManaCost("1")) == true);
	    */
	    /*
	    ManaCost cost = new ManaCost("1 B B");
	    Input_PayManaCostUtil.isManaNeeded(Constant.Color.Black, cost);
	    cost.subtractMana(Constant.Color.Black);
	    cost.subtractMana(Constant.Color.Green);
	    check("106", Input_PayManaCostUtil.isManaNeeded(Constant.Color.Green, cost) == false);
		*/
	    
	    c = new Card();
	    Card c2 = new Card();
	    c.addIntrinsicKeyword("Flying");
	    c2.addIntrinsicKeyword("Flying");
	    //check("107", CombatUtil.canBlock(c, c2));
	    //check("108", CombatUtil.canBlock(c2, c));
	    
	    c = new Card();
	    c2 = new Card();
	    c2.addIntrinsicKeyword("Flying");
	    check("109", CombatUtil.canBlock(c, c2));
	    check("110", ! CombatUtil.canBlock(c2, c));
	    
	    
	    c = cf.getCard("Fyndhorn Elves", "");
	    c2 = cf.getCard("Talas Warrior", "");
	    check("110a", !CombatUtil.canBlock(c2, c));
	    check("110b", CombatUtil.canBlock(c, c2));
	    
	    c = new Card();
	    c.setName("1");
	    c.setUniqueNumber(1);
	    c2 = new Card();
	    c2.setName("2");
	    c2.setUniqueNumber(2);
	    
	    //test CardList
	    CardList cardList = new CardList(new Card[]
	    {c, c2});
	    check("111", cardList.contains(c));
	    check("112", cardList.contains(c2));
	    check("113", cardList.containsName(c));
	    check("114", cardList.containsName(c.getName()));
	    check("115", cardList.containsName(c2));
	    check("116", cardList.containsName(c2.getName()));
	    
	    c = new Card();
	    check("117", c.hasSickness() == true);
	    c.addIntrinsicKeyword("Haste");
	    check("118", c.hasSickness() == false);
	    
	    
	}
	
	{
	    CardFactory cf = AllZone.CardFactory;
	    CardList c1 = new CardList();
	    c1.add(cf.getCard("Shock", ""));
	    c1.add(cf.getCard("Royal Assassin", ""));
	    c1.add(cf.getCard("Hymn to Tourach", ""));
	    
	    CardList c2 = c1.filter(new CardListFilter()
	    {
		public boolean addCard(Card c)
		{
		    return c.isCreature();
		}
	    });
	    check("119", c2.containsName("Royal Assassin"));
	    check("119", c2.size() == 1);
	    
	    c2 = c1.filter(new CardListFilter()
	    {
		public boolean addCard(Card c)
		{
		    return c.isInstant();
		}
	    });
	    check("120", c2.containsName("Shock"));
	    check("121", c2.size() == 1);
	    
	    c2 = c1.filter(new CardListFilter()
	    {
		public boolean addCard(Card c)
		{
		    return c.getName().equals("Hymn to Tourach");
		}
	    });
	    check("120", c2.containsName("Hymn to Tourach"));
	    check("121", c2.size() == 1);
	    
	    Card card = cf.getCard("Sylvan Basilisk", "");
	    Card card2 = cf.getCard("Exalted Angel", "");
	    
	    check("121a", !CombatUtil.canDestroyAttacker(card, card2));
	}
	{
	    check("122", CardUtil.getConvertedManaCost("0") == 0);
	    check("123", CardUtil.getConvertedManaCost("R") == 1);
	    check("124", CardUtil.getConvertedManaCost("R R") == 2);
	    check("125", CardUtil.getConvertedManaCost("R R R") == 3);
	    check("126", CardUtil.getConvertedManaCost("1") == 1);
	    check("127", CardUtil.getConvertedManaCost("2/R 2/G 2/W 2/B 2/U") == 10);
	}
    }//test()
    static void check(String message, boolean ok)
    {
	if(! ok)
	    throw new RuntimeException("RunTest test error : " +message);
    }    
    public static void main(String args[])
    {
	RunTest.test();
    }
}