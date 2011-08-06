package forge.quest.gui.main;


import forge.gui.GuiUtils;
import forge.quest.data.QuestBattleManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class QuestBattle extends QuestSelectablePanel {
	private static final long serialVersionUID = 3112668476017792084L;

	static TreeMap<String, DeckInfo> nameDeckMap = new TreeMap<String, DeckInfo>();

    String deckName;

    static {
        buildDeckList();
    }

    private static void buildDeckList() {
        //TODO: Build this list dynamically from the deck files.

        addToDeckList("Abraham Lincoln 3", "hard", "WUR flying creatures deck with Flamebreak and Earthquake");
        addToDeckList("Albert Einstein 2", "medium", "WG deck with Garruk Wildspeaker, Needle Storm and Retribution of the Meek");
        addToDeckList("Albert Einstein 3", "hard", "WG deck with Garruk Wildspeaker, Needle Storm and Retribution of the Meek");
        addToDeckList("Aquaman 1", "easy", "WU Control deck");
        addToDeckList("Aquaman 2", "medium", "WU Caw-Blade deck");
        addToDeckList("Aquaman 3", "hard", "WU Caw-Blade deck");
        addToDeckList("Aragorn 2", "medium", "WUBRG Landfall deck");
//        addToDeckList("Ash 1", "easy", "WB Singleton deck");
//        addToDeckList("Ash 2", "medium", "WB Singleton deck");
//        addToDeckList("Ash 3", "hard", "WB Oversold Cemetery deck");
//        addToDeckList("Atomic Robo 1", "easy", "Artifact Singleton deck");
//        addToDeckList("Atomic Robo 2", "medium", "Artifact Singleton deck");
//        addToDeckList("Atomic Robo 3", "hard", "Artifact Standard Constructed deck");
        
        addToDeckList("Bamm Bamm Rubble 1", "easy", "WUBRG Domain deck");
        addToDeckList("Barney Rubble 1", "easy", "WU Sovereigns of Lost Alara deck with walls and auras");
        addToDeckList("Barney Rubble 2", "medium", "WU Sovereigns of Lost Alara deck with walls and auras");
        addToDeckList("Barney Rubble 3", "hard", "WU Sovereigns of Lost Alara deck with walls and auras");
        addToDeckList("Bart Simpson 1", "easy", "UBG deck with Rathi Trapper and Paralyzing Grasp");
        addToDeckList("Bart Simpson 2", "medium", "WUG deck with Minister of Impediments and Paralyzing Grasp");
        addToDeckList("Bart Simpson 3", "hard", "WUG deck with Harrier Griffin and Entangling Vines");
        addToDeckList("Batman 3", "hard", "RG Valakut Titan deck");
//        addToDeckList("Bear 1", "easy", "G Bear theme deck");
//        addToDeckList("Bear 2", "medium", "2/2s with abilities deck");
//        addToDeckList("Bear 3", "hard", "Token 2/2s, a lot of Token 2/2s deck");
//        addToDeckList("Beast 2", "medium", "GR Furnace Celebration deck");
//        addToDeckList("Beast 3", "hard", "UWB Standard Constructed deck");
        addToDeckList("Bela Lugosi 3", "hard", "B Vampire Aggro deck");
        addToDeckList("Betty Rubble 3", "hard", "G deck with Broodwarden and Eldrazi Spawn tokens");
        addToDeckList("Blackbeard 3", "hard", "W Soldiers deck with Preeminent Captain, Captain of the Watch and Daru Warchief");
        addToDeckList("Boba Fett 3", "hard", "WRG Dragons deck with Chandra Nalaar, Crucible of Fire and Dragon Roost");
        addToDeckList("Boris Karloff 3", "hard", "WR Boros Aggro deck with Kors, levelers and threat removal");
        addToDeckList("Boromir 2", "medium", "G Elvish Piper deck with Quicksilver Amulet and huge creatures");
        addToDeckList("Boromir 3", "hard", "G Elvish Piper deck with Quicksilver Amulet and huge creatures");
        addToDeckList("Boromir 4", "very hard", "UG Show and Tell deck with huge creatures");
//        addToDeckList("Brood 2", "medium", "W Battlecry deck");
        addToDeckList("Buffy 1", "easy", "BRG Vampires deck with wither and Sorceress Queen");
        addToDeckList("Buffy 2", "medium", "BRG Vampires deck with wither and Sorceress Queen");
        addToDeckList("Buffy 3", "hard", "BRG Vampires deck with wither and Sorceress Queen");
        
        addToDeckList("C3PO 1", "easy", "BR Goblin deck with Goblin Ringleader, Mad Auntie and Sensation Gorger");
        addToDeckList("C3PO 2", "medium", "BR Goblin deck with Goblin Ringleader, Kiki-Jiki, Mad Auntie and Sensation Gorger");
        addToDeckList("C3PO 3", "hard", "BR Goblin deck with Goblin Ringleader, Kiki-Jiki, Mad Auntie and Sensation Gorger");
//        addToDeckList("Cable 2", "medium", "UR Artifact deck");
//        addToDeckList("Cable 3", "hard", "R Artifact deck deck");
//        addToDeckList("Captain America 2", "medium", "Bant Exalted deck");
//        addToDeckList("Captain America 3", "hard", "Bant Exalted deck");
        addToDeckList("Catwoman 1", "easy", "WG Cat deck with Kjeldoran War Cry");
        addToDeckList("Catwoman 2", "medium", "WRG Cat deck with Lightning Helix");
//        addToDeckList("Colbert 2", "medium", "WG Cats deck");
//        addToDeckList("Colbert 3", "hard", "WUR Extended deck");
//        addToDeckList("Colossus 2", "medium", "RG Changeling deck");
//        addToDeckList("Colossus 3", "hard", "UG Standard Constructed deck");
        addToDeckList("Comic Book Guy 3", "hard", "WR deck with Roc and Rukh Eggs, Flamebrake, Earthquake and Auriok Champion");
//        addToDeckList("Conan 3", "hard", "Red monsters deck");
        addToDeckList("Crocodile Dundee 1", "easy", "Mono R deck with Mudbrawler Cohort and Bloodmark Mentor");
        addToDeckList("Crocodile Dundee 2", "medium", "Mono R deck with Mudbrawler Cohort and Bloodmark Mentor");
        addToDeckList("Crocodile Dundee 3", "hard", "Mono R deck with Mudbrawler Cohort and Bloodmark Mentor");
        addToDeckList("Cyclops 3", "hard", "WUBRG Slivers deck with a few spells");
        
        addToDeckList("Da Vinci 1", "easy", "Mono B deck with Ashenmoor Cohort, Badmoon and some Fear");
        addToDeckList("Da Vinci 2", "medium", "Mono B deck with Korlash, Heir to Blackblade, Badmoon and threat removal");
        addToDeckList("Da Vinci 3", "hard", "Mono B deck with Korlash, Heir to Blackblade, Badmoon and threat removal");
//        addToDeckList("Darkseid 2", "medium", "B Sacrifice");
        addToDeckList("Darrin Stephens 1", "easy", "U Affinity deck with Affinity for artifacts and Modular cards");
        addToDeckList("Darrin Stephens 2", "medium", "U Affinity deck with Affinity for artifacts and Modular cards");
        addToDeckList("Darrin Stephens 3", "hard", "U Affinity deck with Affinity for artifacts and Modular cards");
        addToDeckList("Darrin Stephens 4", "very hard", "U Affinity deck");
        addToDeckList("Darth Vader 3", "hard", "WU Battle of Wits style alternate win type deck with WoG");
        addToDeckList("Data 3", "hard", "B deck with Korlash, Heir to Blackblade and Liliana Vess");
        addToDeckList("Deadpool 2", "medium", "BR deck with Ashenmoor Liege and Grixis Grimblade");
        addToDeckList("Deadpool 3", "hard", "BR deck with Ashenmoor Liege and Grixis Grimblade");
//        addToDeckList("Dick Grayson 3", "hard", "WU Constructed");
        addToDeckList("Dino 2", "medium", "Mono brown affinity deck with Affinity for artifacts and Modular cards");
        addToDeckList("Dino 3", "hard", "Mono brown affinity deck with Affinity for artifacts and Modular cards");
//        addToDeckList("Dinosaur 1", "easy", "GR Large Creatures");
//        addToDeckList("Dinosaur 2", "medium", "WGR Naya");
        addToDeckList("Doc Holiday 1", "easy", "WUG Morph deck with Regenerate creatures");
        addToDeckList("Doc Holiday 2", "medium", "WUG Morph deck with Regenerate creatures");
        addToDeckList("Doc Holiday 3", "hard", "WUG Morph deck with Regenerate creatures");
//        addToDeckList("Dog 2", "medium", "GRB Sacrifice");
        addToDeckList("Doran 3", "hard", "WBG Doran, the Siege Tower deck with high toughness creatures");
//        addToDeckList("Dr Doom 2", "medium", "GWB");
//        addToDeckList("Dr Doom 3", "hard", "GWB Constructed");
//        addToDeckList("Dr Fate 3", "hard", "UB Infect");
        addToDeckList("Dr No 3", "hard", "WUB Combo & Control deck with The Rack, Balance, Propaganda and discard spells");
        
        addToDeckList("Electro 3", "hard", "WRG deck with Arashi, the Sky Asunder and Ball Lightning");
        addToDeckList("Elrond 1", "easy", "WG Aura deck with Rabid Wombat");
        addToDeckList("Elrond 2", "medium", "RGW Aura deck with Rabid Wombat");
        addToDeckList("Elrond 3", "hard", "RGW Aura deck with Kor Spiritdancer");
//        addToDeckList("En Sabah Nur 2", "medium", "RUB Singleton");
//        addToDeckList("En Sabah Nur 3", "hard", "UBR Standard Constructed");
        addToDeckList("Endora 2", "medium", "WG Enchantress deck with enchantments and cards with enchantment effects");
        addToDeckList("Endora 3", "hard", "WG Enchantress deck with enchantments and cards with enchantment effects");
        
        addToDeckList("Fat Albert 1", "easy", "WUBRG Winter Orb deck with Keldon Warlord and mana Elves/Slivers");
        addToDeckList("Fat Albert 2", "medium", "WUBRG Winter Orb deck with Keldon Warlord and mana Elves/Slivers");
        addToDeckList("Fat Albert 3", "hard", "UG Winter Orb deck with Kalonian Behemoth and mana Elves/Slivers");
//        addToDeckList("Fin Fang Foom 1", "easy", "B Artifact");
//        addToDeckList("Fin Fang Foom 2", "medium", "G Infect");
//        addToDeckList("Fin Fang Foom 3", "hard", "GB Infect");
        addToDeckList("Fred Flintstone 3", "hard", "WUG deck with Phytohydra and Lorescale Coatl");
        addToDeckList("Frodo 1", "easy", "WRG Zoo deck with some threat removal");
        addToDeckList("Frodo 2", "medium", "WRG Zoo deck with some threat removal and Glorious Anthem");
        addToDeckList("Frodo 3", "hard", "WRG Zoo deck with threat removal and Glorious Anthems");
        
        addToDeckList("Galadriel 2", "medium", "G Deck with Amulet of Vigor, mana ramp, Time Vault and Howl of the Night Pack");
        addToDeckList("Galahad 1", "easy", "WB Knight deck with Kinsbaile Cavalier and Knight Exemplar");
        addToDeckList("Galahad 2", "medium", "WB Knight deck with Kinsbaile Cavalier and Knight Exemplar");
        addToDeckList("Galahad 3", "hard", "WB Knight deck with Kinsbaile Cavalier and Knight Exemplar");
//        addToDeckList("Gambit 3", "hard", "UR deck with Demigod of Revenge");
        addToDeckList("Genghis Khan 1", "easy", "WRG deck with mana ramp, Armageddon and Llanowar Behemoth");
        addToDeckList("Genghis Khan 2", "medium", "WG deck with mana ramp, Armageddon and Llanowar Behemoth");
        addToDeckList("Genghis Khan 3", "hard", "WRG deck with mana ramp, Armageddon, Llanowar Behemoth and Elspeth, Knight-Errant");
        addToDeckList("George of the Jungle 1", "easy", "WR deck with Belligerent Hatchling, Battlegate Mimic and Ajani Vengeant");
        addToDeckList("George of the Jungle 2", "medium", "WR deck with Belligerent Hatchling, Battlegate Mimic and Ajani Vengeant");
        addToDeckList("George of the Jungle 3", "hard", "WR deck with Belligerent Hatchling, Battlegate Mimic and Ajani Vengeant");
//        addToDeckList("Ghost Rider 3", "hard", "W Aggressive Life deck");
        addToDeckList("Gimli 2", "medium", "WB Indestructible permanents deck with mass removal");
//        addToDeckList("Goblin King 2", "medium", "RG Singleton deck");
//        addToDeckList("Goblin King 3", "hard", "RG Extended deck");
//        addToDeckList("Goblin Recruit 2", "medium", "RG Skullclamp deck");
//        addToDeckList("Goblin Recruit 3", "hard", "BR Goblin Sacrifice deck");
        addToDeckList("Gold Finger 3", "hard", "U control deck with various counter spells, Serra Sphinx and Memnarch");
        addToDeckList("Grampa Simpson 1", "easy", "WR Double Strike deck with equipments and auras");
        addToDeckList("Grampa Simpson 2", "medium", "WR Double Strike deck with equipments and auras");
        addToDeckList("Grampa Simpson 3", "hard", "WRG Double Strike deck with equipments and auras");
//        addToDeckList("Green Arrow 2", "medium", "G Anti-Air deck");
//        addToDeckList("Green Arrow 3", "hard", "G Angry Large Monsters deck");
        addToDeckList("Green Lantern 3", "hard", "UBR Nicol Bolas, Planeswalker deck with threat removal and several creatures");
//        addToDeckList("Gunslinger 3", "hard", "WBRG Cascade deck");
        
        addToDeckList("Han Solo 3", "hard", "WG enchantments deck with Sigil of the Empty Throne");
//        addToDeckList("Hans 2", "medium", "WRG Allies deck");
        addToDeckList("Harry Potter 1", "easy", "U Mill and counter spell deck");
        addToDeckList("Harry Potter 2", "medium", "UB Mill and counter spell deck");
        addToDeckList("Harry Potter 3", "hard", "UB Mill and counter spell deck with card draw");
        addToDeckList("Hellboy 3", "hard", "BR direct damage deck");
        addToDeckList("Hercules 1", "easy", "GW Deck with Safehold Duo, Bant Sureblade and Naya Hushblade");
        addToDeckList("Hercules 2", "medium", "GW Deck with Bant Sureblade and Naya Hushblade");
        addToDeckList("Hercules 3", "hard", "GW Deck with Wilt-Leaf Liege, Bant Sureblade and Naya Hushblade");
        addToDeckList("Higgins 3", "hard", "UBR Grixis Control deck with threat removal and some creatures");
        addToDeckList("Homer Simpson 1", "easy", "UBR Morph deck with Regenerate creatures and Raise Dead");
        addToDeckList("Homer Simpson 2", "medium", "UBR Morph deck with Regenerate creatures and Raise Dead");
        addToDeckList("Homer Simpson 3", "hard", "UBR Morph deck with Regenerate creatures, card draw and creature buff");
//        addToDeckList("Hulk 2", "medium", "G Men with Pants deck");
//        addToDeckList("Hulk 3", "hard", "G Midrange deck");
        
        addToDeckList("Iceman 3", "hard", "UB Bounce and Control deck");
        addToDeckList("Indiana Jones 1", "easy", "UBR Sol'kanar the Swamp King and buff");
        addToDeckList("Indiana Jones 2", "medium", "UBR Sol'kanar the Swamp King, buff and Raise Dead");
        addToDeckList("Indiana Jones 3", "hard", "UBR Sol'kanar the Swamp King, buff and Terminate");
        
        addToDeckList("Jabba the Hut 3", "hard", "WUG Exalted deck with land walkers");
//        addToDeckList("Jack 2", "medium", "BG Aggressive deck");
//        addToDeckList("Jack 3", "hard", "WUB Sphinx Cascade deck");
        addToDeckList("Jack Sparrow 1", "easy", "UB Pirate deck with card draw and counter spells");
        addToDeckList("Jack Sparrow 2", "medium", "UB Pirate deck with card draw and threat removal");
        addToDeckList("Jack Sparrow 3", "hard", "UB Pirate deck with card draw and creature control");
        addToDeckList("James Bond 1", "easy", "WG Agro deck with several Slivers");
        addToDeckList("James Bond 2", "medium", "WG Agro deck with several Slivers and Glorious Anthem");
        addToDeckList("James Bond 3", "hard", "WGR Agro deck with several Slivers and Glorious Anthem");
        addToDeckList("James T Kirk 3", "hard", "B discard deck with Liliana Vess");
//        addToDeckList("Jason Todd 3", "hard", "BRG Sacrifice deck");
        addToDeckList("Joe Kidd 1", "easy", "WB deck with Voracious Hatchling and Nightsky Mimic");
        addToDeckList("Joe Kidd 2", "medium", "WB deck with Voracious Hatchling and Nightsky Mimic");
        addToDeckList("Joe Kidd 3", "hard", "WB deck with Voracious Hatchling and Nightsky Mimic");
//        addToDeckList("Joker 2", "medium", "WG Novablast deck");
//        addToDeckList("Jon Stewart 2", "medium", "WG Midrange deck");
//        addToDeckList("Jon Stewart 3", "hard", "WG Extended deck");
        
        addToDeckList("Kang 2", "medium", "UB deck with Glen Elendra Liege, Gravelgill Duo and Dire Undercurrents");
        addToDeckList("Kang 3", "hard", "UB deck with Glen Elendra Liege and Dire Undercurrents");
        addToDeckList("King Arthur 1", "easy", "WG Knight deck with Wilt-Leaf Cavaliers, Knight of the Skyward Eye and Leonin Skyhunter");
        addToDeckList("King Arthur 2", "medium", "WG Knight deck with Wilt-Leaf Cavaliers and Knights with flanking");
        addToDeckList("King Arthur 3", "hard", "WG Knight deck with Sir Shandlar of Eberyn and Knights with first strike");
        addToDeckList("King Edward 1", "easy", "WUBRG Elementals deck with Tribal Flames");
        addToDeckList("King Edward 2", "medium", "WUBRG Elementals deck with Tribal Flames");
        addToDeckList("King Edward 3", "hard", "WUBRG Elementals deck with Tribal Flames and Horde of Notions");
        addToDeckList("King Kong 1", "easy", "WBG Squirrel deck with tokens, changelings, Deranged Hermit and curse type auras");
        addToDeckList("King Kong 2", "medium", "WBG Squirrel deck with tokens, changelings, Deranged Hermit and curse type auras");
        addToDeckList("King Kong 3", "hard", "WRG Squirrel deck with tokens, changelings, Deranged Hermit and threat removal");
        addToDeckList("Kojak 1", "easy", "Mono U deck with Sunken City, Inundate, counterspells and bounce");
        addToDeckList("Kojak 2", "medium", "Mono U deck with Sunken City, Inundate, counterspells and bounce");
        addToDeckList("Kojak 3", "hard", "Mono U deck with Sunken City, Inundate, counterspells and bounce");
//        addToDeckList("Krypto 3", "hard", "UBG Standard Constructed deck");
        
//        addToDeckList("Lex 3", "hard", "Ninjas deck");
//        addToDeckList("Link 3", "hard", "GUR Standard Constructed deck");
        addToDeckList("Lisa Simpson 3", "hard", "WG Devour deck with tokens, Skullmulcher and Gluttonous Slime");
//        addToDeckList("Lucifer 2", "medium", "W Sacrifice deck");
//        addToDeckList("Lucifer 3", "hard", "W Sacrifice deck");
        addToDeckList("Luke Skywalker 3", "hard", "WU Rebels deck with Training Grounds");
        
        addToDeckList("Maggie Simpson 3", "hard", "BRG jund deck with Sprouting Thrinax, Jund Hackblade and Bloodbraid Elf");
        addToDeckList("Magneto 3", "hard", "B Shriekmaw deck with creature removal and re-animation");
        addToDeckList("Magnum 1", "easy", "UG deck with Sturdy Hatchling and Shorecrasher Mimic");
        addToDeckList("Magnum 2", "medium", "UG deck with Sturdy Hatchling and Shorecrasher Mimic");
        addToDeckList("Magnum 3", "hard", "UG deck with Sturdy Hatchling and Shorecrasher Mimic");
        addToDeckList("Marge Simpson 3", "hard", "RG deck with tokens which are devoured by R and RG creatures with devour");
//        addToDeckList("Michael 3", "hard", "W Angels deck");
        addToDeckList("Morpheus 3", "hard", "G Elf deck with Overrun, Gaea's Anthem, Imperious Perfect and other pumps");
        addToDeckList("Mr Slate 2", "medium", "WUG Merfolk deck with Lord of Atlantis, Stonybrook Banneret and Stonybrook Schoolmaster");
        addToDeckList("Mr Slate 3", "hard", "WUG Merfolk deck with Lord of Atlantis, Stonybrook Banneret and Stonybrook Schoolmaster");
//        addToDeckList("Mummy 1", "easy", "W Life deck");
        
//        addToDeckList("Namor 2", "medium", "U Control deck");
//        addToDeckList("Namor 3", "hard", "U Standard Constructed deck");
        addToDeckList("Napoleon 3", "hard", "WBG Wall deck with Rolling Stones and Doran, the Siege Tower");
        addToDeckList("Ned Flanders 1", "easy", "B reanimator deck with a few large creatures and some spells");
        addToDeckList("Ned Flanders 2", "medium", "B reanimator deck with a few large creatures and some spells");
        addToDeckList("Ned Flanders 3", "hard", "B reanimator deck with a few large creatures and some spells");
        addToDeckList("Ned Flanders 4", "very hard", "B reanimator deck with a few large creatures and some spells");
        addToDeckList("Neo 3", "hard", "RG deck with Groundbreaker and other attack once then sacrifice at EoT creatures");
        addToDeckList("Newton 3", "hard", "WB Relentless Rats deck with Thrumming Stone, Vindicate and Swords to Plowshares");
        
//        addToDeckList("Odin 3", "hard", "WR Standard deck");
//        addToDeckList("Owlman 2", "medium", "U Ebony Owl deck");
//        addToDeckList("Owlman 3", "hard", "B Control Standard deck");
        
        addToDeckList("Pebbles Flintstone 2", "medium", "WU Meekstone deck with Meekstone, Marble Titan and creatures with vigilance");
        addToDeckList("Pebbles Flintstone 3", "hard", "WU Meekstone deck with Meekstone, Marble Titan and creatures with vigilance");
//        addToDeckList("Phoenix 3", "hard", "R Burn");
        addToDeckList("Picard 3", "hard", "WUG Elf deck with elf lords");
        addToDeckList("Pinky and the Brain 3", "hard", "WB Royal Assassin deck with WoG, Damnation, Liliana Vess and Beacon of Unrest");
//        addToDeckList("Predator 2", "medium", "WG Purity Ramp deck");
//        addToDeckList("Predator 3", "hard", "UG Beastmaster Ascension deck");
        addToDeckList("Professor X 3", "hard", "WUB Esper Artifacts deck with Master of Etherium and Esper Stormblade");
        
        addToDeckList("R2-D2 3", "hard", "U Black Vise deck with bounce (Boomerang) spells and Howling Mine");
        addToDeckList("Radagast 2", "medium ", "G Muraganda Petroglyphs deck with vanilla creatures and a few tokens");
        addToDeckList("Radiant 3", "medium ", "WUB flying creature deck with Radiant, Archangel, Gravitational Shift and Moat");
//        addToDeckList("Ras Al Ghul 2", "medium", "RG Biorhythm deck");
//        addToDeckList("Ras Al Ghul 3", "hard", "WG Eldrazi Monument deck");
//        addToDeckList("Raven 1", "easy", "Birds deck");
//        addToDeckList("Raven 2", "medium", "Birds deck");
//        addToDeckList("Raven 3", "hard", " Possessed Birds deck");
//        addToDeckList("Red Skull 2", "medium", "BR Metalcraft deck");
//        addToDeckList("Robin 2", "medium", "G Big Green deck");
//        addToDeckList("Robin 3", "hard", "WG Standard deck");
        addToDeckList("Rocky 1", "easy", "WUR Pro red deck with Flamebreak, Tremor, Pyroclasm");
        addToDeckList("Rocky 2", "medium", "WUR Pro red deck with Flamebreak, Tremor, Pyroclasm");
        addToDeckList("Rocky 3", "hard", "WUR Pro red deck with Flamebreak, Tremor, Pyroclasm");
        addToDeckList("Rogue 3", "hard", "R Dragon deck with Tarox Bladewing, Dragon Roost and Chandra Nalaar");
        
//        addToDeckList("Sabertooth 2", "medium", "G Smokestack deck");
        addToDeckList("Samantha Stephens 1", "easy", "WU Painter's Servant anti-red deck");
        addToDeckList("Samantha Stephens 2", "medium", "WU Painter's Servant anti-red deck");
        addToDeckList("Samantha Stephens 3", "hard", "WU Painter's Servant anti-red deck with Grindstone combo");
        addToDeckList("Samantha Stephens 4", "very hard", "WU Painter's Servant - Grindstone combo");
        addToDeckList("Saruman 2", "medium", "UBR discard deck with Megrim, Liliana's Caress and Burning Inquiry ");
        addToDeckList("Saruman 3", "hard", "UBR discard deck with Megrim, Liliana's Caress and Burning Inquiry ");
        addToDeckList("Sauron 2", "medium", "UB Black Vise deck with Underworld Dreams, lots of card draw for both players");
        addToDeckList("Scooby Doo 3", "hard", "WR Giants Aggro deck with a few changelings");
        addToDeckList("Scotty 2", "medium", "WBG Pestilence deck with Castle, Penumbra Kavu, Spider and Wurm but no pro black");
        addToDeckList("Seabiscuit 1", "easy", "W Metalcraft deck with Ardent Recruit and Indomitable Archangel");
        addToDeckList("Seabiscuit 2", "medium", "W Metalcraft deck with Ardent Recruit and Indomitable Archangel");
        addToDeckList("Seabiscuit 3", "hard", "W Metalcraft deck with Ardent Recruit and Indomitable Archangel");
        addToDeckList("Secret Squirrel 3", "hard", "G Squirrel deck with Squirrel Mob, Deranged Hermit, Coat of Arms and Nut Collector");
//        addToDeckList("Sentinel 2", "medium", "WB Token deck");
//        addToDeckList("Sentinel 3", "hard", "WB Token deck");
//        addToDeckList("Shelob 1", "easy", "G Reach deck");
        addToDeckList("Sherlock Holmes 1", "easy", "Mono G deck with Baru, Fist of Krosa, land fetch and some buff cards");
        addToDeckList("Sherlock Holmes 2", "medium", "Mono G deck with Baru, Fist of Krosa and lots of good green creatures");
        addToDeckList("Sherlock Holmes 3", "hard", "Mono G deck with Baru, Fist of Krosa and lots of great green creatures");
        addToDeckList("Silver Surfer 3", "hard", "G beat down deck with many creatures and several pump spells");
        addToDeckList("Spiderman 2", "medium", "W weenie deck with WoG, Armageddon and Mass Calcify");
        addToDeckList("Spock 2", "medium", "G Elf singleton deck with several Winnower Patrol and Wolf-Skull Shaman");
//        addToDeckList("Starfire 2", "medium", "Incarnations deck");
//        addToDeckList("Starfire 3", "hard", "Incarnations deck");
        addToDeckList("Storm 1", "easy", "WBR Lifelink deck with Phyrexian Arena");
        addToDeckList("Storm 2", "medium", "WBRG Lifelink deck with Phyrexian Arena");
        addToDeckList("Storm 3", "hard", "WUBRG Lifelink deck with Phyrexian Arena");
//        addToDeckList("Superboy 3", "hard", "R Artifact deck");
        addToDeckList("Superman 1", "easy", "WUBRG Slivers deck with Raise Dead and Breath of Life");
        addToDeckList("Superman 2", "medium", "WUBRG Slivers deck with Zombify and Tribal Flames");
//        addToDeckList("Swamp Thing 1", "easy", "BG deck");
//        addToDeckList("Swamp Thing 2", "medium", "BG deck");
//        addToDeckList("Swamp Thing 3", "hard", "BG deck");
        
        addToDeckList("Tarzan 1", "easy", "WG deck with jungle creatures and pump spells");
        addToDeckList("Tarzan 2", "medium", "WG deck with Silverback Ape, jungle creatures and pump spells");
        addToDeckList("Terminator 3", "hard", "U deck with Master of Etherium, Control Magic, Memnarch and many artifacts");
        addToDeckList("The Great Gazoo 3", "hard", "WR deck with, red damage all spells and pro from red creatures");
//        addToDeckList("Thing 2", "medium", "WG Elves deck");
//        addToDeckList("Thing 3", "hard", "G Garruk Elves deck");
//        addToDeckList("Thor 1", "easy", "WR Singleton deck");
//        addToDeckList("Thor 2", "medium", "BR 1cc deck");
//        addToDeckList("Thor 3", "hard", "WR Constructed deck");
//        addToDeckList("Thugs 2", "medium", "WG Elves deck");
//        addToDeckList("Thugs 3", "hard", "WG Strength in Numbers deck");
        addToDeckList("Totoro 2", "medium", "UBG deck with spirits and arcane spells");
        addToDeckList("Treebeard 1", "easy", "G Treefolk deck with Bosk Banneret, Dauntless Dourbark and Leaf-Crowned Elder");
        addToDeckList("Treebeard 2", "medium", "WBG Treefolk deck with Bosk Banneret, Dauntless Dourbark, Timber Protector, Leaf-Crowned Elder and Doran");
        addToDeckList("Treebeard 3", "hard", "WBG Treefolk deck with Bosk Banneret, Dauntless Dourbark, Timber Protector, Leaf-Crowned Elder and Doran");
        
        addToDeckList("Uncle Owen 3", "hard", "WUB Control deck");
        
//        addToDeckList("Vampire 2", "medium", "Vampire Singleton");
//        addToDeckList("Vampire 3", "hard", "Vampire Constructed");
        
//        addToDeckList("Werewolf 2", "medium", "UGB UBG Fungal Shambler deck");
//        addToDeckList("White Knight 1", "easy", "W Common Knights deck");
//        addToDeckList("White Knight 2", "medium", "Singleton Knights deck");
//        addToDeckList("White Knight 3", "hard", "Knights Standard deck");
        addToDeckList("Wilma Flintstone 1", "easy", "BG deck with Noxious Hatchling and Woodlurker Mimic");
        addToDeckList("Wilma Flintstone 2", "medium", "BG deck with Noxious Hatchling and Woodlurker Mimic");
        addToDeckList("Wilma Flintstone 3", "hard", "BG deck with Noxious Hatchling and Woodlurker Mimic");
        addToDeckList("Wally 3", "hard", "WB Artifact deck with Tempered Steel");
        addToDeckList("Wolverine 3", "hard", "BG deck with Nightmare, Korlash, Heir to Blackblade and Kodama's Reach");
        addToDeckList("Wyatt Earp 1", "easy", "Mono W deck with Crovax, Ascendant Hero, Crusade and small to medium sized creatures.");
        addToDeckList("Wyatt Earp 2", "medium", "Mono W deck with Crovax, Ascendant Hero, Crusade and small to medium sized creatures.");
        addToDeckList("Wyatt Earp 3", "hard", "Mono W deck with Crovax, Ascendant Hero, Honor of the Pure and small to medium sized creatures.");
        
//        addToDeckList("Xavier 2", "medium", "UR Twitch");
    }

    private static void addToDeckList(String name, String difficulty, String description) {
        nameDeckMap.put(name, new DeckInfo(name, description, difficulty));
    }

    public static String getDescription(String deckName) {
        if (nameDeckMap.containsKey(deckName)){
            return nameDeckMap.get(deckName).description;
        }

        else{
            System.out.println("Deck " +deckName+" does not have a description.");
            return "";
        }
    }

    private static class DeckInfo {
        String name;
        String difficulty;
        String description;

        private DeckInfo(String name, String description, String difficulty) {
            this.description = description;
            this.difficulty = difficulty;
            this.name = name;
        }
    }

    public static List<QuestSelectablePanel> getBattles(){
        List<QuestSelectablePanel> opponentList = new ArrayList<QuestSelectablePanel>();

        String[] opponentNames = QuestBattleManager.getOpponents();
        for (String opponentName : opponentNames) {

                String oppIconName = opponentName.substring(0, opponentName.length() - 1).trim() + ".jpg";
                ImageIcon icon = GuiUtils.getIconFromFile(oppIconName);

            try {
                opponentList.add(new QuestBattle(opponentName,
                        nameDeckMap.get(opponentName).difficulty,
                        nameDeckMap.get(opponentName).description,
                        icon));
            }
            catch (NullPointerException e) {
                System.out.println("Missing Deck Description. Fix me:" + opponentName);
                opponentList.add(new QuestBattle(opponentName,
                        "<<Unknown>>",
                        "<<Unknown>>",
                        icon));

            }
        }

        return opponentList;
    }

    private QuestBattle(String name, String difficulty, String description, ImageIcon icon) {
        super(name.substring(0, name.length()-2), difficulty, description, icon);

        this.deckName = name;
    }

    @Override
    public String getName() {
        return deckName;
    }
}
