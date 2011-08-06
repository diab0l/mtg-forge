package forge.quest.gui.main;


import forge.gui.GuiUtils;
import forge.quest.data.QuestBattleManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class QuestBattle extends QuestSelectablePanel {

    static TreeMap<String, DeckInfo> nameDeckMap = new TreeMap<String, DeckInfo>();

    String deckName;

    static {
        buildDeckList();
    }

    private static void buildDeckList() {
        //TODO: Build this list dynamically from the deck files.

        addToDeckList("Abraham Lincoln 3", "hard", "WUR flying creatures with Flamebreak and Earthquake");
        addToDeckList("Albert Einstein 2", "medium", "Garruk Wildspeaker, W+G creatures with Needle Storm and Retribution of the Meek");
        addToDeckList("Albert Einstein 3", "hard", "Garruk Wildspeaker, W+G creatures with Needle Storm and Retribution of the Meek");
//        addToDeckList("Aquaman 1", "easy", "WU Singleton");
//        addToDeckList("Aquaman 2", "medium", "WU Caw-Blade");
//        addToDeckList("Aquaman 3", "hard", "WU Caw-Blade Constructed");
        addToDeckList("Aragorn 2", "medium", "WBRG Landfall deck");
//        addToDeckList("Ash 1", "easy", "WB Singleton");
//        addToDeckList("Ash 2", "medium", "WB Singleton ");
//        addToDeckList("Ash 3", "hard", "WB Oversold Cemetery deck");
//        addToDeckList("Atomic Robo 1", "easy", "Artifact Singleton");
//        addToDeckList("Atomic Robo 2", "medium", " Artifact Singleton ");
//        addToDeckList("Atomic Robo 3", "hard", "Artifact Standard Constructed");
        
        addToDeckList("Bamm Bamm Rubble 1", "easy", "WUBRG domain deck, creatures and spells with the Domain ability");
        addToDeckList("Barney Rubble 1", "easy", "WU Sovereigns of Lost Alara deck with walls and auras");
        addToDeckList("Barney Rubble 2", "medium", "WU Sovereigns of Lost Alara deck with walls and auras");
        addToDeckList("Barney Rubble 3", "hard", "WU Sovereigns of Lost Alara deck with walls and auras");
        addToDeckList("Bart Simpson 1", "easy", "BUG creatures that will tap your creatures and will use auras to keep them tapped");
        addToDeckList("Bart Simpson 2", "medium", "WUG creatures that will tap your creatures and will use auras to keep them tapped");
        addToDeckList("Bart Simpson 3", "hard", "WUG creatures that will tap your creatures and will use auras to keep them tapped");
        addToDeckList("Batman 3", "hard", "Creatures with Exalted and Unblockable abilities, WoG and Armageddon");
//        addToDeckList("Bear 1", "easy", "G Bear theme");
//        addToDeckList("Bear 2", "medium", "2/2s with abilities ");
//        addToDeckList("Bear 3", "hard", "Token 2/2s, a lot of Token 2/2s");
//        addToDeckList("Beast 2", "medium", "GR Furnace Celebration");
//        addToDeckList("Beast 3", "hard", "UWB Standard Constructed");
        addToDeckList("Bela Lugosi 3", "hard", "Vampire deck, B creatures, little to no spells");
        addToDeckList("Betty Rubble 3", "hard", "Summer Bloom deck with mods, features Plant + Eldrazi Spawn tokens");
        addToDeckList("Blackbeard 3", "hard", "W Soldiers with Preeminent Captain, Captain of the Watch and Daru Warchief");
        addToDeckList("Boba Fett 3", "hard", "Dragons, Chandra Nalaar, Crucible of Fire and Dragon Roost");
        addToDeckList("Boris Karloff 3", "hard", "Boros Aggro (RW) deck with mods, Kors, levelers and threat removal");
        addToDeckList("Boromir 2", "medium", "Elvish Piper and Quicksilver Amulet with huge creatures");
        addToDeckList("Boromir 3", "hard", "Elvish Piper and Quicksilver Amulet with huge creatures");
//        addToDeckList("Brood 2", "medium", "W Battlecry");
        addToDeckList("Buffy 1", "easy", "Vampires and creatures with wither + Sorceress Queen");
        addToDeckList("Buffy 2", "medium", "Vampires and creatures with wither + Sorceress Queen");
        addToDeckList("Buffy 3", "hard", "Vampires and creatures with wither + Sorceress Queen");
        
        addToDeckList("C3PO 1", "easy", "BR Goblins, Goblin Ringleader, Mad Auntie and Sensation Gorger");
        addToDeckList("C3PO 2", "medium", "BR Goblins, Goblin Ringleader, Kiki-Jiki, Mad Auntie and Sensation Gorger");
        addToDeckList("C3PO 3", "hard", "BR Goblins, Goblin Ringleader, Kiki-Jiki, Mad Auntie and Sensation Gorger");
//        addToDeckList("Cable 2", "medium", "RU Artifact");
//        addToDeckList("Cable 3", "hard", "R Artifact deck");
//        addToDeckList("Captain America 2", "medium", "Bant Exalted");
//        addToDeckList("Captain America 3", "hard", "Bant Exalted");
        addToDeckList("Catwoman 1", "easy", "Cat creatures G+W");
        addToDeckList("Catwoman 2", "medium", "Cats creatures G+W+R with Lightning Helix");
//        addToDeckList("Colbert 2", "medium", "GW Cats");
//        addToDeckList("Colbert 3", "hard", "RWU Extended");
//        addToDeckList("Colossus 2", "medium", "GR Changeling");
//        addToDeckList("Colossus 3", "hard", "GU Standard Constructed");
        addToDeckList("Comic Book Guy 3", "hard", "Roc and Rukh Eggs, Flamebrake, Earthquake, Auriok Champion, Kor Firewalker");
//        addToDeckList("Conan 3", "hard", "Red monsters");
        addToDeckList("Crocodile Dundee 1", "easy", "Mono red deck with Mudbrawler Cohort and Bloodmark Mentor");
        addToDeckList("Crocodile Dundee 2", "medium", "Mono red deck with Mudbrawler Cohort and Bloodmark Mentor");
        addToDeckList("Crocodile Dundee 3", "hard", "Mono red deck with Mudbrawler Cohort and Bloodmark Mentor");
        addToDeckList("Cyclops 3", "hard", "Slivers mainly, some spells");
        
        addToDeckList("Da Vinci 1", "easy", "Mono black deck, Ashenmoor Cohort + Badmoon + some Fear");
        addToDeckList("Da Vinci 2", "medium", "Mono black deck, Korlash, Heir to Blackblade + Badmoon + threat removal");
        addToDeckList("Da Vinci 3", "hard", "Mono black deck, Korlash, Heir to Blackblade + Badmoon + threat removal");
//        addToDeckList("Darkseid 2", "medium", "B Sacrifice");
        addToDeckList("Darrin Stephens 1", "easy", "U Affinity deck, Affinity for artifacts and Modular cards");
        addToDeckList("Darrin Stephens 2", "medium", "U Affinity deck, Affinity for artifacts and Modular cards");
        addToDeckList("Darrin Stephens 3", "hard", "U Affinity deck, Affinity for artifacts and Modular cards");
        addToDeckList("Darth Vader 3", "hard", "UW Battle of Wits style alternate win type deck, WoG");
        addToDeckList("Data 3", "hard", "Korlash, Heir to Blackblade, Liliana Vess");
//        addToDeckList("Deadpool 2", "medium", "BR Singleton");
//        addToDeckList("Deadpool 3", "hard", "B/R Extended, Good luck!");
//        addToDeckList("Dick Grayson 3", "hard", "WU Constructed");
        addToDeckList("Dino 2", "medium", "Mono brown affinity deck, Affinity for artifacts and Modular cards");
        addToDeckList("Dino 3", "hard", "Mono brown affinity deck, Affinity for artifacts and Modular cards");
//        addToDeckList("Dinosaur 1", "easy", "GR Large Creatures");
//        addToDeckList("Dinosaur 2", "medium", "WGR Naya");
        addToDeckList("Doc Holiday 1", "easy", "Morph + Regenerate GWU creatures");
        addToDeckList("Doc Holiday 2", "medium", "Morph + Regenerate GWU creatures");
        addToDeckList("Doc Holiday 3", "hard", "Morph + Regenerate GWU creatures");
//        addToDeckList("Dog 2", "medium", "GRB Sacrifice");
        addToDeckList("Doran 3", "hard", "WBG Doran, the Siege Tower deck with high toughness creatures");
//        addToDeckList("Dr Doom 2", "medium", "GWB");
//        addToDeckList("Dr Doom 3", "hard", "GWB Constructed");
//        addToDeckList("Dr Fate 3", "hard", "UB Infect");
        addToDeckList("Dr No 3", "hard", "The Rack, Balance, Propaganda, discard spells");
        
        addToDeckList("Electro 3", "hard", "Stormfront deck with mods, Arashi, the Sky Asunder + Ball Lightning");
        addToDeckList("Elrond 2", "medium", "Aura Gnarlid, Rabid Wombat and Uril with lots of auras");
//        addToDeckList("En Sabah Nur 2", "medium", "RUB Singleton");
//        addToDeckList("En Sabah Nur 3", "hard", "UBR Standard Constructed");
        addToDeckList("Endora 2", "medium", "Enchantress deck, enchantments + cards with enchantment effects");
        addToDeckList("Endora 3", "hard", "Enchantress deck, enchantments + cards with enchantment effects");
        
        addToDeckList("Fat Albert 1", "easy", "Winter Orb, Keldon Warlord, mana Elves/Slivers + several 4/4 creatures");
        addToDeckList("Fat Albert 2", "medium", "Winter Orb, Keldon Warlord, mana Elves/Slivers + several 5/5 creatures");
        addToDeckList("Fat Albert 3", "hard", "Winter Orb, Keldon Warlord, mana Elves/Slivers + several 6/6 creatures");
//        addToDeckList("Fin Fang Foom 1", "easy", "B Artifact");
//        addToDeckList("Fin Fang Foom 2", "medium", "G Infect");
//        addToDeckList("Fin Fang Foom 3", "hard", "GB Infect");
        addToDeckList("Fred Flintstone 3", "hard", "Predator's Garden deck with mods, featuring Lorescale Coatl");
        addToDeckList("Frodo 1", "easy", "Zoo Easy, some creature removal");
        addToDeckList("Frodo 2", "medium", "Zoo Medium, some creature removal + Glorious Anthem");
        addToDeckList("Frodo 3", "hard", "Zoo Hard, more creature removal + Glorious Anthems");
        
        addToDeckList("Galadriel 2", "medium", "Amulet of Vigor, green mana ramp, time vault and Howl of the Night Pack");
        addToDeckList("Galahad 1", "easy", "Knight deck with Kinsbaile Cavalier and Knight Exemplar");
        addToDeckList("Galahad 2", "medium", "Knight deck with Kinsbaile Cavalier and Knight Exemplar");
        addToDeckList("Galahad 3", "hard", "Knight deck with Kinsbaile Cavalier and Knight Exemplar");
//        addToDeckList("Gambit 3", "hard", "UR, Watch out for Demigod of Revenge");
        addToDeckList("Genghis Khan 1", "easy", "Mana Elves + Birds + Armageddon, Llanowar Behemoth");
        addToDeckList("Genghis Khan 2", "medium", "Mana Elves + Birds + Armageddon, Llanowar Behemoth");
        addToDeckList("Genghis Khan 3", "hard", "Mana Elves + Birds + Armageddon, Llanowar Behemoth + Elspeth, Knight-Errant");
        addToDeckList("George of the Jungle 1", "easy", "Belligerent Hatchling, Battlegate Mimic, Ajani Vengeant + a few RW spells");
        addToDeckList("George of the Jungle 2", "medium", "Belligerent Hatchling, Battlegate Mimic, Ajani Vengeant + some RW spells");
        addToDeckList("George of the Jungle 3", "hard", "Belligerent Hatchling, Battlegate Mimic, Ajani Vengeant + many RW spells");
//        addToDeckList("Ghost Rider 3", "hard", "W Aggressive Life");
        addToDeckList("Gimli 2", "medium", "Indestructible permanents with lots of mass removal");
//        addToDeckList("Goblin King 2", "medium", "GR Singleton");
//        addToDeckList("Goblin King 3", "hard", "G/R Extended");
//        addToDeckList("Goblin Recruit 2", "medium", "GR Skullclamp");
//        addToDeckList("Goblin Recruit 3", "hard", "RB Goblin Sacrifice");
        addToDeckList("Gold Finger 3", "hard", "U control deck, various counter spells and Serra Sphinx + Memnarch");
        addToDeckList("Grampa Simpson 1", "easy", "WR double strike deck, various equipments and auras");
        addToDeckList("Grampa Simpson 2", "medium", "WR double strike deck, various equipments and auras");
        addToDeckList("Grampa Simpson 3", "hard", "WR double strike deck, various equipments and auras");
//        addToDeckList("Green Arrow 2", "medium", "G Anti-Air");
//        addToDeckList("Green Arrow 3", "hard", "G Angry Large Monsters");
        addToDeckList("Green Lantern 3", "hard", "Nicol Bolas, Planeswalker + threat removal and several creatures");
//        addToDeckList("Gunslinger 3", "hard", "WGRB Cascade");
        
        addToDeckList("Han Solo 3", "hard", "WG enchantments deck with Sigil of the Empty Throne");
//        addToDeckList("Hans 2", "medium", "GRW Allies");
        addToDeckList("Harry Potter 1", "easy", "Mill and counter spell deck");
        addToDeckList("Harry Potter 2", "medium", "Mill and counter spell deck");
        addToDeckList("Harry Potter 3", "hard", "Various milling cards, some speed up and counter spells");
        addToDeckList("Hellboy 3", "hard", "A BR direct damage deck");
        addToDeckList("Higgins 3", "hard", "Grixis Control deck, lots of threat removal and some creatures");
        addToDeckList("Homer Simpson 1", "easy", "Morph + Regenerate BRU creatures, + Raise Dead");
        addToDeckList("Homer Simpson 2", "medium", "Morph + Regenerate BRU creatures, + Raise Dead");
        addToDeckList("Homer Simpson 3", "hard", "Morph + Regenerate BRU creatures, + card draw and creature buff");
//        addToDeckList("Hulk 2", "medium", "G Men with Pants");
//        addToDeckList("Hulk 3", "hard", "G Midrange");
        
        addToDeckList("Iceman 3", "hard", "BU Bounce and Control style deck");
        addToDeckList("Indiana Jones 1", "easy", "Sol'kanar + buff");
        addToDeckList("Indiana Jones 2", "medium", "Sol'kanar + buff + Raise Dead");
        addToDeckList("Indiana Jones 3", "hard", "Sol'kanar + buff + Terminate");
        
        addToDeckList("Jabba the Hut 3", "hard", "Creatures with exalted and land walking abilities");
//        addToDeckList("Jack 2", "medium", "BG Aggressive");
//        addToDeckList("Jack 3", "hard", "WBU Sphinx Cascade");
        addToDeckList("Jack Sparrow 1", "easy", "Pirate type creatures + draw cards + counter spells");
        addToDeckList("Jack Sparrow 2", "medium", "Pirate type creatures + draw cards + threat removal");
        addToDeckList("Jack Sparrow 3", "hard", "Pirate type creatures + draw cards + creature control");
        addToDeckList("James Bond 1", "easy", "WG Agro with several Slivers");
        addToDeckList("James Bond 2", "medium", "WG Agro with several Slivers + Glorious Anthem");
        addToDeckList("James Bond 3", "hard", "WGR Agro");
        addToDeckList("James T Kirk 3", "hard", "40 card black discard deck + Liliana Vess");
//        addToDeckList("Jason Todd 3", "hard", "GBR Sacrifice");
        addToDeckList("Joe Kidd 1", "easy", "Voracious Hatchling, Nightsky Mimic, no planeswalkers + a few WB spells");
        addToDeckList("Joe Kidd 2", "medium", "Voracious Hatchling, Nightsky Mimic, no planeswalkers + some WB spells");
        addToDeckList("Joe Kidd 3", "hard", "Voracious Hatchling, Nightsky Mimic, no planeswalkers + many WB spells");
//        addToDeckList("Joker 2", "medium", "GW Novablast");
//        addToDeckList("Jon Stewart 2", "medium", "WG Midrange");
//        addToDeckList("Jon Stewart 3", "hard", "WG Extended");
        
//        addToDeckList("Kang 2", "medium", "BU Singleton");
//        addToDeckList("Kang 3", "hard", "RB Extended");
        addToDeckList("King Arthur 1", "easy", "Wilt-Leaf Cavaliers; Knight of the Skyward Eye and Leonin Skyhunter");
        addToDeckList("King Arthur 2", "medium", "Wilt-Leaf Cavaliers; Knights with flanking");
        addToDeckList("King Arthur 3", "hard", "Sir Shandlar of Eberyn; Knights with first strike");
        addToDeckList("King Edward 1", "easy", "Elementals, 5 color deck with Tribal Flames");
        addToDeckList("King Edward 2", "medium", "Elementals, 5 color deck with Tribal Flames");
        addToDeckList("King Edward 3", "hard", "Elementals, 5 color deck with Tribal Flames featuring Horde of Notions");
        addToDeckList("King Kong 1", "easy", "Squirrel tokens, changelings and Deranged Hermit + curse type auras");
        addToDeckList("King Kong 2", "medium", "Squirrel tokens, changelings and Deranged Hermit + curse type auras");
        addToDeckList("King Kong 3", "hard", "Squirrel tokens, changelings and Deranged Hermit + threat removal");
        addToDeckList("Kojak 1", "easy", "Mono blue deck with Sunken City, Inundate, counterspells and bounce");
        addToDeckList("Kojak 2", "medium", "Mono blue deck with Sunken City, Inundate, counterspells and bounce");
        addToDeckList("Kojak 3", "hard", "Mono blue deck with Sunken City, Inundate, counterspells and bounce");
//        addToDeckList("Krypto 3", "hard", "GUB Standard Constructed");
        
//        addToDeckList("Lex 3", "hard", "Ninjas!");
//        addToDeckList("Link 3", "hard", "GUR Standard Constructed");
        addToDeckList("Lisa Simpson 3", "hard", "GW deck, creates tokens which are devoured by Skullmulcher and Gluttonous Slime");
//        addToDeckList("Lucifer 2", "medium", "W Sacrifice");
//        addToDeckList("Lucifer 3", "hard", "W Sacrifice");
        addToDeckList("Luke Skywalker 3", "hard", "GWU weenie style deck with Garruk Wildspeaker and Gaea's Anthem");
        
        addToDeckList("Maggie Simpson 3", "hard", "This is a jund deck from the deck forum with some modifications");
        addToDeckList("Magneto 3", "hard", "Shriekmaw, Assassins, creature removal + Liliana Vess");
        addToDeckList("Magnum 1", "easy", "Sturdy Hatchling, Shorecrasher Mimic, Garruk & Jace, the Mind Sculptor + GU spells");
        addToDeckList("Magnum 2", "medium", "Sturdy Hatchling, Shorecrasher Mimic, Garruk & Jace, the Mind Sculptor + GU spells");
        addToDeckList("Magnum 3", "hard", "Sturdy Hatchling, Shorecrasher Mimic, Garruk & Jace, the Mind Sculptor + GU spells");
        addToDeckList("Marge Simpson 3 ", "hard", "RG deck, creates tokens which are devoured by R and RG creatures with devour");
//        addToDeckList("Michael 3", "hard", "W Angels");
        addToDeckList("Morpheus 3", "hard", "Elves with Overrun, Gaea's Anthem, Imperious Perfect and other pumps");
        addToDeckList("Mr Slate 2", "medium", "Don't Go in the Water deck with mods, Merfolk and Merfolk pumps");
        addToDeckList("Mr Slate 3", "hard", "Don't Go in the Water deck with mods, Merfolk and Merfolk pumps");
//        addToDeckList("Mummy 1", "easy", "W Life");
        
//        addToDeckList("Namor 2", "medium", "U Control");
//        addToDeckList("Namor 3", "hard", "U Standard Constructed");
        addToDeckList("Napoleon 3", "hard", "Walls, Rolling Stones and Doran, the Siege Tower");
        addToDeckList("Ned Flanders 1", "easy", "B reanimator deck, a few large creatures and some spells");
        addToDeckList("Ned Flanders 2", "medium", "B reanimator deck, a few large creatures and some spells");
        addToDeckList("Ned Flanders 3", "hard", "B reanimator deck, a few large creatures and some spells");
        addToDeckList("Neo 3", "hard", "RG with Groundbreaker and other attack once then sacrifice at EoT creatures");
        addToDeckList("Newton 3", "hard", "Relentless Rats, Ratcatcher, Aluren and Harmonize");
        
//        addToDeckList("Odin 3", "hard", "WR Standard");
//        addToDeckList("Owlman 2", "medium", "U Ebony Owl");
//        addToDeckList("Owlman 3", "hard", "B Control Standard");
        
        addToDeckList("Pebbles Flintstone 2", "medium", "WU Meekstone deck, Meekstone, Marble Titan and creatures with vigilance");
        addToDeckList("Pebbles Flintstone 3", "hard", "WU Meekstone deck, Meekstone, Marble Titan and creatures with vigilance");
//        addToDeckList("Phoenix 3", "hard", "R Burn");
        addToDeckList("Picard 3", "hard", "UWG Elf deck similar to Morpheus but also has flying elves");
        addToDeckList("Pinky and the Brain 3", "hard", "Royal Assassin, WoG + Damnation, Liliana Vess, Beacon of Unrest");
//        addToDeckList("Predator 2", "medium", "GW Purity Ramp");
//        addToDeckList("Predator 3", "hard", "GU Beastmaster Ascension");
        addToDeckList("Professor X 3", "hard", "Master of Etherium + Vedalken Archmage and many artifacts");
        
        addToDeckList("R2-D2 3", "hard", "Black Vise, bounce (Boomerang) spells, Howling Mine");
        addToDeckList("Radagast 2", "medium ", "Muraganda Petroglyphs, green vanilla creatures and a few tokens");
        addToDeckList("Radiant 3", "medium ", "Flying Creatures with Radiant, Archangel, Gravitational Shift and Moat");
//        addToDeckList("Ras Al Ghul 2", "medium", "GR Biorhythm");
//        addToDeckList("Ras Al Ghul 3", "hard", "WG Eldrazi Monument");
//        addToDeckList("Raven 1", "easy", "Birds!");
//        addToDeckList("Raven 2", "medium", "Birds!");
//        addToDeckList("Raven 3", "hard", " Possessed Birds!");
//        addToDeckList("Red Skull 2", "medium", "BR Metalcraft");
//        addToDeckList("Robin 2", "medium", "G Big Green");
//        addToDeckList("Robin 3", "hard", "GW Standard");
        addToDeckList("Rocky 1", "easy", "Pro red, Flamebreak + Tremor + Pyroclasm but no Pyrohemia");
        addToDeckList("Rocky 2", "medium", "Pro red, Flamebreak + Tremor + Pyroclasm but no Pyrohemia");
        addToDeckList("Rocky 3", "hard", "Pro red, Flamebreak + Tremor + Pyroclasm but no Pyrohemia");
        addToDeckList("Rogue 3", "hard", "Dragons including Tarox Bladewing, Dragon Roost, Chandra Nalaar");
        
//        addToDeckList("Sabertooth 2", "medium", "G Smokestack");
        addToDeckList("Samantha Stephens 1", "easy", "WU Painter's Servant anti-red deck");
        addToDeckList("Samantha Stephens 2", "medium", "WU Painter's Servant anti-red deck");
        addToDeckList("Samantha Stephens 3", "hard", "WU Painter's Servant anti-red deck with Grindstone combo");
        addToDeckList("Saruman 2", "medium", "Discard deck with Megrim, Liliana's Caress and Burning Inquiry ");
        addToDeckList("Saruman 3", "hard", "Discard deck with Megrim, Liliana's Caress and Burning Inquiry ");
        addToDeckList("Sauron 2", "medium", "Black Vise and Underworld Dreams with lots of card draw for both players");
        addToDeckList("Scooby Doo 3", "hard", "Red deck, Dragonmaster Outcast, Rakdos Pit Dragon, Kamahl, Pit Fighter");
        addToDeckList("Scotty 2", "medium", "Pestilence + Castle + Penumbra Kavu/Spider/Wurm but no pro black");
        addToDeckList("Seabiscuit 1", "easy", "White Metalcraft deck, Ardent Recruit, Indomitable Archangel etc");
        addToDeckList("Seabiscuit 2", "medium", "White Metalcraft deck, Ardent Recruit, Indomitable Archangel etc");
        addToDeckList("Seabiscuit 3", "hard", "White Metalcraft deck, Ardent Recruit, Indomitable Archangel etc");
        addToDeckList("Secret Squirrel 3", "hard", "Squirrel deck, Squirrel Mob + Deranged Hermit + Coat of Arms + Nut Collector");
//        addToDeckList("Sentinel 2", "medium", "WB Token");
//        addToDeckList("Sentinel 3", "hard", "WB Token");
//        addToDeckList("Shelob 1", "easy", "G Reach");
        addToDeckList("Sherlock Holmes 1", "easy", "Mono green deck, Baru, Fist of Krosa + land fetch + some buff cards.");
        addToDeckList("Sherlock Holmes 2", "medium", "Mono green deck, Baru, Fist of Krosa + lots of good green creatures.");
        addToDeckList("Sherlock Holmes 3", "hard", "Mono green deck, Baru, Fist of Krosa + lots of great green creatures.");
        addToDeckList("Silver Surfer 3", "hard", "Green creature beat down deck with several pump spells");
        addToDeckList("Spiderman 2", "medium", "White weenies with WoG, Armageddon, Mass Calcify");
        addToDeckList("Spock 2", "medium", "Elf deck with just a single copy of most of the elves");
//        addToDeckList("Starfire 2", "medium", "Incarnations");
//        addToDeckList("Starfire 3", "hard", "Incarnations");
        addToDeckList("Storm 1", "easy", "Creatures with Lifelink + filler");
        addToDeckList("Storm 2", "medium", "Creatures with Lifelink + filler");
        addToDeckList("Storm 3", "hard", "Creatures with Lifelink + filler");
//        addToDeckList("Superboy 3", "hard", "Artifact Red");
        addToDeckList("Superman 1", "easy", "Slivers deck, Raise Dead + Breath of Life");
        addToDeckList("Superman 2", "medium", "Slivers deck, Zombify + Tribal Flames");
//        addToDeckList("Swamp Thing 1", "easy", "GB");
//        addToDeckList("Swamp Thing 2", "medium", "GB");
//        addToDeckList("Swamp Thing 3", "hard", "GB");
        
        addToDeckList("Tarzan 1", "easy", "Jungle creatures + pump spells");
        addToDeckList("Tarzan 2", "medium", "Tarzan with Silverback Ape + pump spells");
        addToDeckList("Terminator 3", "hard", "Master of Etherium + Control Magic and Memnarch + many artifacts");
        addToDeckList("The Great Gazoo 3", "hard", "Sun Lotion deck, red damage all spells and pro from red creatures");
//        addToDeckList("Thing 2", "medium", "GW Elves");
//        addToDeckList("Thing 3", "hard", "G Garruk Elves");
//        addToDeckList("Thor 1", "easy", "WR Singleton");
//        addToDeckList("Thor 2", "medium", "BR 1cc");
//        addToDeckList("Thor 3", "hard", "WR Constructed");
//        addToDeckList("Thugs 2", "medium", "GW Elves");
//        addToDeckList("Thugs 3", "hard", "GW Strength in Numbers");
        addToDeckList("Totoro 2", "medium", "Blue, black, green deck with spirits and arcane spells");
        addToDeckList("Treebeard 1", "easy", "Treefolk creatures, a lumberjack's dream. Bosk Banneret, Dauntless Dourbark, Leaf-Crowned Elder");
        addToDeckList("Treebeard 2", "medium", "Treefolk creatures. Bosk Banneret, Dauntless Dourbark, Timber Protector, Leaf-Crowned Elder, Doran");
        addToDeckList("Treebeard 3", "hard", "Treefolk creatures. Bosk Banneret, Dauntless Dourbark, Timber Protector, Leaf-Crowned Elder, Doran");
        
        addToDeckList("Uncle Owen 3", "hard", "Creature removal/control with Liliana Vess");
        
//        addToDeckList("Vampire 2", "medium", "Vampire Singleton");
//        addToDeckList("Vampire 3", "hard", "Vampire Constructed");
        
//        addToDeckList("Werewolf 2", "medium", "UGB Fungal Shambler");
//        addToDeckList("White Knight 1", "easy", "W Common Knights");
//        addToDeckList("White Knight 2", "medium", "Singleton Knights");
//        addToDeckList("White Knight 3", "hard", "Knights Standard");
        addToDeckList("Wilma Flintstone 1", "easy", "Noxious Hatchling, Woodlurker Mimic, Liliana Vess + a few BG spells");
        addToDeckList("Wilma Flintstone 2", "medium", "Noxious Hatchling, Woodlurker Mimic, Liliana Vess + some BG spells");
        addToDeckList("Wilma Flintstone 3", "hard", "Noxious Hatchling, Woodlurker Mimic, Liliana Vess + many BG spells");
        addToDeckList("Wolverine 3", "hard", "Nightmare + Korlash, Heir to Blackblade + Kodama's Reach");
        addToDeckList("Wyatt Earp 1", "easy", "Mono white deck, Crovax, Ascendant Hero + Crusade + small to medium sized creatures.");
        addToDeckList("Wyatt Earp 2", "medium", "Mono white deck, Crovax, Ascendant Hero + Crusade + small to medium sized creatures.");
        addToDeckList("Wyatt Earp 3", "hard", "Mono white deck, Crovax, Ascendant Hero + Honor of the Pure + small to medium sized creatures.");
        
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
