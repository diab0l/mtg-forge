package forge;
import java.util.*;


public class StateBasedEffects
{
	//this is used to keep track of all state-based effects in play:
	private HashMap<String, Integer> stateBasedMap = new HashMap<String, Integer>();
	
	//this is used to define all cards that are state-based effects, and map the corresponding commands to their cardnames
	private static HashMap<String, String[]> cardToEffectsList = new HashMap<String, String[]>();
	
	public StateBasedEffects() 
	{
		initStateBasedEffectsList();
	}
	
	
	public void initStateBasedEffectsList()
	{	//value has to be an array, since certain cards have multiple commands associated with them
		cardToEffectsList.put("Conspiracy", new String[] {"Conspiracy"});
		cardToEffectsList.put("Serra Avatar", new String[] {"Serra_Avatar"});
		cardToEffectsList.put("Avatar",  new String[] {"Ajani_Avatar_Token"});
		cardToEffectsList.put("Windwright Mage", new String[] {"Windwright_Mage"});
		//cardToEffectsList.put("Baru, Fist of Krosa", new String[] {"Baru"});
		//cardToEffectsList.put("Reach of Brances", new String[] {"Reach_of_Branches"});
		//cardToEffectsList.put("Essence Warden", new String[] {"Essence_Warden"});
		//cardToEffectsList.put("Soul Warden", new String[] {"Soul_Warden"});
		//cardToEffectsList.put("Wirewood Hivemaster", new String[] {"Wirewood_Hivemaster"});
		//cardToEffectsList.put("Angelic Chorus", new String[] {"Angelic_Chorus"});
		cardToEffectsList.put("Uril, the Miststalker", new String[] {"Uril"});
		cardToEffectsList.put("Rabid Wombat", new String[] {"Rabid_Wombat"});
		cardToEffectsList.put("Kithkin Rabble", new String[] {"Kithkin_Rabble"});
		cardToEffectsList.put("Nightmare", new String[] {"Nightmare"});
		cardToEffectsList.put("Aven Trailblazer", new String[] {"Aven_Trailblazer"});
		cardToEffectsList.put("Rakdos Pit Dragon", new String[] {"Rakdos_Pit_Dragon"});
		cardToEffectsList.put("Nyxathid", new String[] {"Nyxathid"});
		cardToEffectsList.put("Lord of Extinction", new String[] {"Lord_of_Extinction"});
		cardToEffectsList.put("Terravore", new String[] {"Terravore"});
		cardToEffectsList.put("Magnivore", new String[] {"Magnivore"});
		cardToEffectsList.put("Tarmogoyf", new String[] {"Tarmogoyf"});
		cardToEffectsList.put("Dakkon Blackblade", new String[] {"Dakkon"});
		cardToEffectsList.put("Korlash, Heir to Blackblade", new String[] {"Korlash"});
		cardToEffectsList.put("Dauntless Dourbark", new String[] {"Dauntless_Dourbark"});
		cardToEffectsList.put("Vexing Beetle", new String[] {"Vexing_Beetle"});
		cardToEffectsList.put("Kird Ape", new String[] {"Kird_Ape"});
		cardToEffectsList.put("Sedge Troll", new String[] {"Sedge_Troll"});
		cardToEffectsList.put("Hedge Troll", new String[] {"Hedge_Troll"});
		cardToEffectsList.put("Wild Nacatl", new String[] {"Wild_Nacatl"});
		cardToEffectsList.put("Liu Bei, Lord of Shu", new String[] {"Liu_Bei"});
		cardToEffectsList.put("Bant Sureblade", new String[] {"Bant_Sureblade"});
		cardToEffectsList.put("Esper Stormblade", new String[] {"Esper_Stormblade"});
		cardToEffectsList.put("Grixis Grimblade", new String[] {"Grixis_Grimblade"});
		cardToEffectsList.put("Jund Hackblade", new String[] {"Jund_Hackblade"});
		cardToEffectsList.put("Naya Hushblade", new String[] {"Naya_Hushblade"});
		cardToEffectsList.put("Mystic Enforcer", new String[] {"Mystic_Enforcer"});
		cardToEffectsList.put("Nimble Mongoose", new String[] {"Nimble_Mongoose"});
		cardToEffectsList.put("Werebear", new String[] {"Werebear"});
		cardToEffectsList.put("Divinity of Pride", new String[] {"Divinity_of_Pride"});
		cardToEffectsList.put("Yavimaya Enchantress", new String[] {"Yavimaya_Enchantress"});
		cardToEffectsList.put("Knight of the Reliquary", new String[] {"Knight_of_the_Reliquary"});
		cardToEffectsList.put("Zuberi, Golden Feather", new String[] {"Zuberi"});
		cardToEffectsList.put("Loxodon Punisher", new String[] {"Loxodon_Punisher"});
		cardToEffectsList.put("Master of Etherium", new String[] {"Master_of_Etherium", "Master_of_Etherium_Pump", "Master_of_Etherium_Other"});
		cardToEffectsList.put("Relentless Rats", new String[] {"Relentless_Rats_Other"});
		cardToEffectsList.put("Privileged Position", new String[] {"Privileged_Position", "Privileged_Position_Other"});
		cardToEffectsList.put("Elvish Archdruid", new String[] {"Elvish_Archdruid_Pump", "Elvish_Archdruid_Other"});
		cardToEffectsList.put("Wizened Cenn", new String[] {"Wizened_Cenn_Pump", "Wizened_Cenn_Other"});
		cardToEffectsList.put("Timber Protector", new String[] {"Timber_Protector_Pump", "Timber_Protector_Other"});
		cardToEffectsList.put("Goblin Chieftain", new String[] {"Goblin_Chieftain_Pump", "Goblin_Chieftain_Other"});
		cardToEffectsList.put("Goblin King", new String[] {"Goblin_King_Pump", "Goblin_King_Other"});
		cardToEffectsList.put("Merfolk Sovereign", new String[] {"Merfolk_Sovereign_Pump", "Merfolk_Sovereign_Other"});
		cardToEffectsList.put("Lord of Atlantis", new String[] {"Lord_of_Atlantis_Pump","Lord_of_Atlantis_Other"});
		cardToEffectsList.put("Elvish Champion", new String[] {"Elvish_Champion_Pump","Elvish_Champion_Other"});
		cardToEffectsList.put("Field Marshal", new String[] {"Field_Marshal_Pump", "Field_Marshal_Other"});
		cardToEffectsList.put("Aven Brigadier", new String[] {"Aven_Brigadier_Soldier_Pump", "Aven_Brigadier_Bird_Pump", "Aven_Brigadier_Other"});
		cardToEffectsList.put("Scion of Oona", new String[] {"Scion_of_Oona_Pump", "Scion_of_Oona_Other"});
		cardToEffectsList.put("Covetous Dragon", new String[] {"Covetous_Dragon"});
		cardToEffectsList.put("Tethered Griffin", new String[] {"Tethered_Griffin"});
		cardToEffectsList.put("Shared Triumph", new String[] {"Shared_Triumph"});
		cardToEffectsList.put("Crucible of Fire", new String[] {"Crucible_of_Fire"});
		cardToEffectsList.put("Glorious Anthem", new String[] {"Glorious_Anthem"});
		cardToEffectsList.put("Gaea's Anthem", new String[] {"Gaeas_Anthem"});
		cardToEffectsList.put("Bad Moon", new String[] {"Bad_Moon"});
		cardToEffectsList.put("Crusade", new String[] {"Crusade"});
		cardToEffectsList.put("Honor of the Pure", new String[] {"Honor_of_the_Pure"});
		cardToEffectsList.put("Beastmaster Ascension", new String[] {"Beastmaster_Ascension"});
		cardToEffectsList.put("Spidersilk Armor", new String[] {"Spidersilk_Armor"});
		cardToEffectsList.put("Eldrazi Monument", new String[] {"Eldrazi_Monument"});
		cardToEffectsList.put("Muraganda Petroglyphs", new String[] {"Muraganda_Petroglyphs"});
		cardToEffectsList.put("Engineered Plague", new String[] {"Engineered_Plague"});
		cardToEffectsList.put("Night of Souls' Betrayal", new String[] {"Night_of_Souls_Betrayal"});
		cardToEffectsList.put("Thelonite Hermit", new String[] {"Thelonite_Hermit"});
		cardToEffectsList.put("Jacques le Vert", new String[] {"Jacques"});
		cardToEffectsList.put("Kaysa", new String[] {"Kaysa"});
		cardToEffectsList.put("Meng Huo, Barbarian King", new String[] {"Meng_Huo"});
		cardToEffectsList.put("Eladamri, Lord of Leaves", new String[] {"Eladamri"});
		cardToEffectsList.put("Tolsimir Wolfblood", new String[] {"Tolsimir"});
		cardToEffectsList.put("Imperious Perfect", new String[] {"Imperious_Perfect"});
		cardToEffectsList.put("Mad Auntie", new String[] {"Mad_Auntie"});
		cardToEffectsList.put("Veteran Armorer", new String[] {"Veteran_Armorer"});
		cardToEffectsList.put("Radiant, Archangel", new String[] {"Radiant_Archangel"});
		cardToEffectsList.put("Castle", new String[] {"Castle"});
		cardToEffectsList.put("Castle Raptors", new String[] {"Castle_Raptors"});
		cardToEffectsList.put("Levitation", new String[] {"Levitation"});
		cardToEffectsList.put("Knighthood", new String[] {"Knighthood"});
		cardToEffectsList.put("Absolute Law", new String[] {"Absolute_Law"});
		cardToEffectsList.put("Absolute Grace", new String[] {"Absolute_Grace"});
		cardToEffectsList.put("Mobilization", new String[] {"Mobilization"});
		cardToEffectsList.put("Serra's Blessing", new String[] {"Serras_Blessing"});
		cardToEffectsList.put("Cover of Darkness", new String[] {"Cover_of_Darkness"});
		cardToEffectsList.put("Steely Resolve", new String[] {"Steely_Resolve"});
		cardToEffectsList.put("Concordant Crossroads", new String[] {"Concordant_Crossroads"});
		cardToEffectsList.put("Mass Hysteria", new String[] {"Mass_Hysteria"});
		cardToEffectsList.put("Fervor", new String[] {"Fervor"});
		cardToEffectsList.put("Madrush Cyclops", new String[] {"Madrush_Cyclops"});
		cardToEffectsList.put("Rolling Stones", new String[] {"Rolling_Stones"});
		cardToEffectsList.put("Sun Quan, Lord of Wu", new String[] {"Sun_Quan"});
		cardToEffectsList.put("Kobold Overlord", new String[] {"Kobold_Overlord"});
		cardToEffectsList.put("Kinsbaile Cavalier", new String[] {"Kinsbaile_Cavalier"});
		cardToEffectsList.put("Wren's Run Packmaster", new String[] {"Wrens_Run_Packmaster"});
		cardToEffectsList.put("Sliver Legion", new String[] {"Sliver_Legion"});
		cardToEffectsList.put("Muscle Sliver", new String[] {"Muscle_Sliver"});
		cardToEffectsList.put("Bonesplitter Sliver", new String[] {"Bonesplitter_Sliver"});
		cardToEffectsList.put("Might Sliver", new String[] {"Might_Sliver"});
		cardToEffectsList.put("Watcher Sliver", new String[] {"Watcher_Sliver"});
		cardToEffectsList.put("Winged Sliver", new String[] {"Winged_Sliver"});
		cardToEffectsList.put("Synchronous Sliver", new String[] {"Synchronous_Sliver"});
		cardToEffectsList.put("Fury Sliver", new String[] {"Fury_Sliver"});
		cardToEffectsList.put("Plated Sliver", new String[] {"Plated_Sliver"});
		cardToEffectsList.put("Sidewinder Sliver", new String[] {"Sidewinder_Sliver"});
		cardToEffectsList.put("Crystalline Sliver", new String[] {"Crystalline_Sliver"});
		cardToEffectsList.put("Essence Sliver", new String[] {"Essence_Sliver"});
		cardToEffectsList.put("Sinew Sliver", new String[] {"Sinew_Sliver"});
		cardToEffectsList.put("Horned Sliver", new String[] {"Horned_Sliver"});
		cardToEffectsList.put("Heart Sliver", new String[] {"Heart_Sliver"});
		cardToEffectsList.put("Reflex Sliver", new String[] {"Reflex_Sliver"});
		cardToEffectsList.put("Gemhide Sliver", new String[] {"Gemhide_Sliver"});
		cardToEffectsList.put("Blade Sliver", new String[] {"Blade_Sliver"});
		cardToEffectsList.put("Battering Sliver", new String[] {"Battering_Sliver"});
		cardToEffectsList.put("Marrow-Gnawer", new String[] {"Marrow_Gnawer"});
		cardToEffectsList.put("Joiner Adept", new String[] {"Joiner_Adept"});
		cardToEffectsList.put("Meddling Mage", new String[] {"Meddling_Mage"});
		cardToEffectsList.put("Gaddock Teeg", new String[] {"Gaddock_Teeg"});
		cardToEffectsList.put("Iona, Shield of Emeria", new String[] {"Iona_Shield_of_Emeria"});
	}
	
	public HashMap<String, String[]> getCardToEffectsList()
	{
		return cardToEffectsList;
	}
	
	public void addStateBasedEffect(String s)
	{
		if (stateBasedMap.containsKey(s))
			stateBasedMap.put(s, stateBasedMap.get(s)+1);
		else 
			stateBasedMap.put(s, 1);
	}
	
	public void removeStateBasedEffect(String s)
	{
		stateBasedMap.put(s, stateBasedMap.get(s)-1);
		if(stateBasedMap.get(s) == 0)
			stateBasedMap.remove(s);
	}
	
	public HashMap<String, Integer> getStateBasedMap()
	{
		return stateBasedMap;
	}
	
	public void reset()
	{
		stateBasedMap.clear();
	}
	
	public void rePopulateStateBasedList()
	{
		reset();
		PlayerZone playerZone = AllZone.getZone(Constant.Zone.Play,
				Constant.Player.Human);
		PlayerZone computerZone = AllZone.getZone(Constant.Zone.Play,
				Constant.Player.Computer);

		CardList cards = new CardList();
		cards.addAll(playerZone.getCards());
		cards.addAll(computerZone.getCards());
		
		System.out.println("== Start add state effects ==");
		for (int i=0;i<cards.size();i++)
		{
			Card c = cards.get(i);
			if (cardToEffectsList.containsKey(c.getName()) )
			{
				String[] effects = getCardToEffectsList().get(c.getName());
				for (String effect : effects) {
					addStateBasedEffect(effect);
					System.out.println("Added " + effect);
				}
				
			}
		}
		System.out.println("== End add state effects ==");

	}
}
