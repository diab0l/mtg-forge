package forge;

import java.util.ArrayList;

public class QuestUtil {
	
	public static int getLife(QuestData qd)
	{
		return qd.getLife();
	}
	
	public static CardList getComputerCreatures(QuestData qd)
	{
		return new CardList();
	}
	
	public static CardList getComputerCreatures(QuestData qd, Quest_Assignment qa)
	{
		CardList list = new CardList();
		if (qa!=null)
		{
			ArrayList<String> compCards = qa.getCompy();
			
			for (String s:compCards)
			{
				Card c = AllZone.CardFactory.getCard(s, Constant.Player.Computer);
				list.add(c);
			}
		}
		return list;
	}
	
	public static CardList getHumanPlantAndPet(QuestData qd)
	{
		CardList list = new CardList();
		
		if (qd.getWolfPetLevel() > 0)
			list.add(getWolfPetToken(qd.getWolfPetLevel()));
		
		if (qd.getPlantLevel() > 0) 
			list.add(getPlantToken(qd.getPlantLevel()));

		return list;
	}
	
	public static CardList getHumanPlantAndPet(QuestData qd, Quest_Assignment qa)
	{
		CardList list = getHumanPlantAndPet(qd);
		
		if (qa!=null)
			list.addAll(qa.getHuman().toArray());
		
		return list;
	}
	
	//makeToken(String name, String imageName, Card source, String manaCost, String[] types, int baseAttack, int baseDefense, String[] intrinsicKeywords) {
	
	public static Card getPlantToken(int level)
	{
		String imageName = "";
		int baseAttack = 0;
		int baseDefense = 0;
		
		String keyword = "";
		
		if (level == 1)
		{
			imageName = "G 0 1 Plant Wall";
			baseDefense = 1;
		}
		else if (level == 2)
		{
			imageName = "G 0 2 Plant Wall";
			baseDefense = 2;
		}
		else if (level == 3)
		{
			imageName = "G 0 3 Plant Wall";
			baseDefense = 3;
		}
		else if (level == 4)
		{
			imageName = "G 1 3 Plant Wall";
			baseDefense = 3;
			baseAttack = 1;
		}
		else if (level == 5)
		{
			imageName = "G 1 3 Plant Wall Deathtouch";
			baseDefense = 3;
			baseAttack = 1;
			keyword = "Deathtouch";
		}
		

        Card c = new Card();
        c.setName("Plant Wall");
        
        c.setImageName(imageName);
        
        c.setController(Constant.Player.Human);
        c.setOwner(Constant.Player.Human);
        
        c.setManaCost("G");
        c.setToken(true);

        c.addType("Creature");
        c.addType("Plant");
        c.addType("Wall");
        
        c.addIntrinsicKeyword("Defender");
        if (!keyword.equals(""))
        	c.addIntrinsicKeyword("Deathtouch");
        
        c.setBaseAttack(baseAttack);
        c.setBaseDefense(baseDefense);
        
        
        return c;
	}//getPlantToken
	
	public static Card getWolfPetToken(int level)
	{
		String imageName = "";
		int baseAttack = 0;
		int baseDefense = 0;
		
		if (level == 1)
		{
			imageName = "G 1 1 Wolf Pet";
			baseDefense = 1;
			baseAttack = 1;
		}
		else if (level == 2)
		{
			imageName = "G 1 2 Wolf Pet";
			baseDefense = 2;
			baseAttack = 1;
		}
		else if (level == 3)
		{
			imageName = "G 2 2 Wolf Pet";
			baseDefense = 2;
			baseAttack = 2;
		}
		else if (level == 4)
		{
			imageName = "G 2 2 Wolf Pet Flanking";
			baseDefense = 2;
			baseAttack = 2;
		}
		

        Card c = new Card();
        c.setName("Wolf Pet");
        
        c.setImageName(imageName);
        
        c.setController(Constant.Player.Human);
        c.setOwner(Constant.Player.Human);
        
        c.setManaCost("G");
        c.setToken(true);

        c.addType("Creature");
        c.addType("Wolf");
        c.addType("Pet");
        
        if (level >= 4)
        	c.addIntrinsicKeyword("Flanking");
        
        c.setBaseAttack(baseAttack);
        c.setBaseDefense(baseDefense);
        
        return c;
	}//getWolfPetToken
	
	public static void setupQuest(Quest_Assignment qa)
	{
		/*
		 *  Gold = 0
		 *  Colorless = 1
		 *  Black = 2
		 *  Blue = 3
		 *  Green = 4
		 *  Red = 5
		 *  White = 6
		 */
		
		QuestData_BoosterPack pack = new QuestData_BoosterPack(); 
		qa.clearCompy();
		
		int id = qa.getId();
		if (id == 1) //White Dungeon
		{
			CardList humanList = new CardList();
			Card c = AllZone.CardFactory.getCard("Adventuring Gear", Constant.Player.Human);
			humanList.add(c);
			
			qa.setHuman(humanList);

			for (int i=0;i<2;i++)
				qa.addCompy("Savannah Lions");
			
			qa.setCardRewardList(pack.getRare(3, 6));
		}
		else if (id == 2) //Blue Dungeon
		{
			qa.setCardRewardList(pack.getRare(3, 3));
		}
		else if (id == 3) //Black Dungeon
		{
			qa.setCardRewardList(pack.getRare(3, 2));
		}
		else if (id == 4) //Red Dungeon
		{
			qa.setCardRewardList(pack.getRare(3, 5));
		}
		else if (id == 5) //Green Dungeon
		{
			qa.setCardRewardList(pack.getRare(3, 4));
		}
		else if (id == 6) //Colorless Dungeon
		{
			for (int i=0;i<2;i++)
				qa.addCompy("Ornithopter");
			qa.setCardRewardList(pack.getRare(3, 1));
		}
		else if (id == 7) //Gold Dungeon
		{
			CardList humanList = new CardList();
			Card c = AllZone.CardFactory.getCard("Trailblazer's Boots", Constant.Player.Human);
			humanList.add(c);
			
			qa.setHuman(humanList);
			qa.setCardRewardList(pack.getRare(3, 0));			
		}
		else if (id == 8)
		{
			CardList humanList = new CardList();
			for (int i=0;i<3;i++)
			{
				Card c = CardFactoryUtil.makeToken("Sheep", "G 0 1 Sheep", Constant.Player.Human, "G", 
													new String[] {"Creature","Sheep"}, 0, 1, new String[]{""}).get(0);
				humanList.add(c);
			}
			qa.setHuman(humanList);
			qa.setCardRewardList(pack.getRare(3));			
		}
		else if (id == 9)
		{
			CardList humanList = new CardList();
			Card c = AllZone.CardFactory.getCard("Trusty Machete", Constant.Player.Human);
			humanList.add(c);
			
			qa.setHuman(humanList);
			
			for (int i=0;i<3;i++)
				qa.addCompy("Wall of Wood");
			
			qa.setCardRewardList(pack.getRare(4, 4));			
		}
	}
	
	
}//QuestUtil
