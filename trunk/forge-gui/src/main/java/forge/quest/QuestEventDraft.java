/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.quest;

import forge.GuiBase;
import forge.card.CardEdition;
import forge.card.CardEdition.CardInSet;
import forge.card.CardRarity;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckGroup;
import forge.deck.DeckSection;
import forge.item.BoosterPack;
import forge.item.PaperCard;
import forge.limited.BoosterDraft;
import forge.limited.LimitedPoolType;
import forge.model.CardBlock;
import forge.model.FModel;
import forge.player.GamePlayerUtil;
import forge.quest.data.QuestPreferences.QPref;
import forge.quest.io.ReadPriceList;
import forge.util.NameGenerator;
import forge.util.storage.IStorage;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * QuestEvent.
 * </p>
 * 
 * MODEL - A basic event instance in Quest mode. Can be extended for use in
 * unique event types: battles, quests, and others.
 */
public class QuestEventDraft {
	
	public static class QuestDraftPrizes {
		
		public int credits;
		public List<BoosterPack> boosterPacks;
		public List<PaperCard> individualCards;
        public List<PaperCard> selectRareCards;
		
		public boolean hasCredits() {
			return credits > 0;
		}
		
		public boolean hasBoosterPacks() {
			return boosterPacks != null && boosterPacks.size() > 0;
		}
		
		public boolean hasIndividualCards() {
			return individualCards != null && individualCards.size() > 0;
		}
        
        public boolean selectRareFromSets() { return selectRareCards != null && selectRareCards.size() > 0; }
        
        public void addSelectedCard(final PaperCard card) {
            FModel.getQuest().getCards().addSingleCard(card, 1);
        }
		
	}
	
    public static final String UNDETERMINED = "quest_draft_undetermined_place";
    public static final String HUMAN = "quest_draft_human_place";
    public static final String DECK_NAME = "Tournament Deck";

    private static transient final ReadPriceList PRICE_LIST_READER = new ReadPriceList();
    private static transient final Map<String, Integer> MAP_PRICES = PRICE_LIST_READER.getPriceList();
    
    private String title = "Mystery Draft";
    private String boosterConfiguration = "";
    private String block = "";
    
    private int entryFee = 0;
    
    private String[] standings = new String[15];
    private String[] aiNames = new String[7];
    
    private int[] aiIcons = new int[7];
    
    private boolean started = false;
    private int age = FModel.getQuestPreferences().getPrefInt(QPref.WINS_NEW_DRAFT);
    
    public QuestEventDraft(final String title) {
        this.title = title;
    }
    
    public final String getTitle() {
        return this.title;
    }
    
    public final String[] getBoosterConfiguration() {
        return boosterConfiguration.split("/");
    }
    
    public final void setBoosterConfiguration(final String boosterConfiguration) {
        this.boosterConfiguration = boosterConfiguration;
    }

    public final void setEntryFee(final int entryFee) {
        this.entryFee = entryFee;
    }
    
    public final int getEntryFee() {
        return entryFee;
    }
    
    public final void setBlock(final String block) {
        this.block = block;
    }
    
    public final String getBlock() {
        return block;
    }
    
    public void setTitle(final String title0) {
        this.title = title0;
    }
    
    public void setStandings(String[] standings) {
        this.standings = standings;
    }
    
    public String[] getStandings() {
        return standings;
    }
    
    public String[] getAINames() {
        return aiNames;
    }
    
    public void setAINames(final String[] names) {
        aiNames = names;
    }
    
    public int[] getAIIcons() {
        return aiIcons;
    }
    
    public void setAIIcons(final int[] icons) {
        aiIcons = icons;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(final int age) {
        this.age = age;
    }
    
    public void addWin() {
        age--;
    }
	
	public void saveToRegularDraft() {
		String tournamentName = FModel.getQuest().getName() + " Tournament Deck " + new SimpleDateFormat("EEE d MMM yyyy HH-mm-ss").format(new Date());
		DeckGroup original = FModel.getQuest().getDraftDecks().get(QuestEventDraft.DECK_NAME);
		DeckGroup output = new DeckGroup(tournamentName);
		for (Deck aiDeck : original.getAiDecks()) {
			output.addAiDeck(copyDeck(aiDeck));
		}
		output.setHumanDeck(copyDeck(original.getHumanDeck(), tournamentName));
		FModel.getDecks().getDraft().add(output);
	}
	
	public void addToQuestDecks() {
		String deckName = "Tournament Deck " + new SimpleDateFormat("EEE d MMM yyyy HH-mm-ss").format(new Date());

		Deck tournamentDeck = FModel.getQuest().getDraftDecks().get(QuestEventDraft.DECK_NAME).getHumanDeck();
		Deck deck = new Deck(deckName);

		FModel.getQuest().getCards().addAllCards(tournamentDeck.getAllCardsInASinglePool().toFlatList());

		if (tournamentDeck.get(DeckSection.Main).countAll() > 0) {
			deck.getOrCreate(DeckSection.Main).addAll(tournamentDeck.get(DeckSection.Main));
			FModel.getQuest().getMyDecks().add(deck);
		}

		FModel.getQuest().getDraftDecks().delete(QuestEventDraft.DECK_NAME);
		FModel.getQuest().getAchievements().endCurrentTournament(FModel.getQuest().getAchievements().getCurrentDraft().getPlayerPlacement());
		FModel.getQuest().save();
	}

	private Deck copyDeck(final Deck deck) {

		Deck outputDeck = new Deck(deck.getName());

		outputDeck.putSection(DeckSection.Main, new CardPool(deck.get(DeckSection.Main)));
		outputDeck.putSection(DeckSection.Sideboard, new CardPool(deck.get(DeckSection.Sideboard)));

		return outputDeck;

	}

	private Deck copyDeck(final Deck deck, final String deckName) {

		Deck outputDeck = new Deck(deckName);

		outputDeck.putSection(DeckSection.Main, new CardPool(deck.get(DeckSection.Main)));
		outputDeck.putSection(DeckSection.Sideboard, new CardPool(deck.get(DeckSection.Sideboard)));

		return outputDeck;

	}
    
    public void setWinner(String playerName) {
        
        boolean isHumanPlayer = true;
        
        for (String name : aiNames) {
            if (playerName.equals(name)) {
                isHumanPlayer = false;
            }
        }
        
        int playerIndex = -1;
        
        if (isHumanPlayer) {
            for (int i = standings.length - 1; i >= 0; i--) {
                if (standings[i].equals(HUMAN)) {
                    playerIndex = i;
                    break;
                }
            }
        } else {
            
            String aiIndex = "";

            for (int i = aiNames.length - 1; i >= 0; i--) {
                if (aiNames[i].equals(playerName)) {
                    aiIndex = "" + (i + 1);
                    break;
                }
            }
            
            for (int i = standings.length - 1; i >= 0; i--) {
                if (standings[i].equals(aiIndex)) {
                    playerIndex = i;
                    break;
                }
            }
            
        }
        
        switch (playerIndex) {
            case 0:
            case 1:
                standings[8] = standings[playerIndex];
                break;
            case 2:
            case 3:
                standings[9] = standings[playerIndex];
                break;
            case 4:
            case 5:
                standings[10] = standings[playerIndex];
                break;
            case 6:
            case 7:
                standings[11] = standings[playerIndex];
                break;
            case 8:
            case 9:
                standings[12] = standings[playerIndex];
                break;
            case 10:
            case 11:
                standings[13] = standings[playerIndex];
                break;
            case 12:
            case 13:
                standings[14] = standings[playerIndex];
                break;
        }
        
    }
    
    /**
     * Generates the prizes for the player and saves them to the current quest.
     */
    public QuestDraftPrizes collectPrizes() {
        
        int place = getPlayerPlacement();
        int prizePool = entryFee * 9;
        
        int boosterPrices = 0;
        
        for (String boosterSet : boosterConfiguration.split("/")) {
            
            int value;
            String boosterName = FModel.getMagicDb().getEditions().get(boosterSet).getName() + " Booster Pack";

            if (MAP_PRICES.containsKey(boosterName)) {
                value = MAP_PRICES.get(boosterName);
            } else {
                value = 395;
            }
            
            boosterPrices += value;
        
        }

        prizePool -= boosterPrices * 8;

		QuestDraftPrizes prizes = null;
        
        switch (place) {
            case 1:
				prizes = generateFirstPlacePrizes(prizePool);
				break;
            case 2:
				prizes = generateSecondPlacePrizes(prizePool);
				break;
            case 3:
				prizes = generateThirdPlacePrizes();
				break;
            case 4:
				prizes = generateFourthPlacePrizes();
				break;
        }
		
		if (prizes != null) {

			if (prizes.hasCredits()) {
				FModel.getQuest().getAssets().addCredits(prizes.credits);
			}
			
			if (prizes.hasBoosterPacks()) {
				for (BoosterPack boosterPack : prizes.boosterPacks) {
					FModel.getQuest().getCards().addAllCards(boosterPack.getCards());
				}
			}
			
			if (prizes.hasIndividualCards()) {
				FModel.getQuest().getCards().addAllCards(prizes.individualCards);
			}
			
			return prizes;
			
		}
        
        return null;
        
    }
    
    private QuestDraftPrizes generateFirstPlacePrizes(final int prizePool) {
        
        int credits = 2 * (prizePool / 3); //First place gets 2/3 the total prize pool
        List<PaperCard> cards = new ArrayList<>();
        List<BoosterPack> boosters = new ArrayList<>();
        
        cards.add(getPromoCard());

        int creditsForPacks = (credits / 2); //Spend 50% of the credits on packs
        
        while (true) {
            BoosterPack pack = getBoosterPack();
            int price = getBoosterPrice(pack);
            if (price > creditsForPacks + creditsForPacks * 0.1f) { //Add a little room for near-same price packs.
                break;
            }
            creditsForPacks -= price;
            boosters.add(pack);
        }
        
        credits = (credits / 2) + creditsForPacks; //Add the leftover credits + 50%

		QuestDraftPrizes prizes = new QuestDraftPrizes();
		prizes.credits = credits;
		prizes.boosterPacks = boosters;
		prizes.individualCards = cards;
        awardSelectedRare(prizes);
		
        return prizes;
        
    }
    
    private QuestDraftPrizes generateSecondPlacePrizes(final int prizePool) {
        
        int credits = prizePool / 3; //Second place gets 1/3 the total prize pool
        List<PaperCard> cards = new ArrayList<>();
        List<BoosterPack> boosters = new ArrayList<>();
        
        cards.add(getPromoCard());

        int creditsForPacks = (credits / 4) * 3; //Spend 75% of the credits on packs
        
        while (true) {
            BoosterPack pack = getBoosterPack();
            int price = getBoosterPrice(pack);
            if (price > creditsForPacks + creditsForPacks * 0.1f) { //Add a little room for near-same price packs.
                break;
            }
            creditsForPacks -= price;
            boosters.add(pack);
        }
        
        credits = (credits / 4) + creditsForPacks; //Add the leftover credits + 25%

		QuestDraftPrizes prizes = new QuestDraftPrizes();
		prizes.credits = credits;
		prizes.boosterPacks = boosters;
		prizes.individualCards = cards;
        awardSelectedRare(prizes);

		return prizes;
        
    }
    
    private QuestDraftPrizes generateThirdPlacePrizes() {
        
        int credits = 0;
        List<PaperCard> cards = new ArrayList<>();
        
        cards.add(getPromoCard());
        
        List<BoosterPack> boosters = new ArrayList<>();
        boosters.add(getBoosterPack());

		QuestDraftPrizes prizes = new QuestDraftPrizes();
		prizes.credits = credits;
		prizes.boosterPacks = boosters;
		prizes.individualCards = cards;

		return prizes;
        
    }
    
    private QuestDraftPrizes generateFourthPlacePrizes() {
        
        int credits = 0;
        List<PaperCard> cards = new ArrayList<>();
        
        cards.add(getPromoCard());

		QuestDraftPrizes prizes = new QuestDraftPrizes();
		prizes.credits = credits;
		prizes.individualCards = cards;

		return prizes;
        
    }

    private void awardSelectedRare(final QuestDraftPrizes prizes) {
        
        List<PaperCard> possibleCards = new ArrayList<>();
    
        for (CardEdition edition : getAllEditions()) {
            for (CardInSet card : edition.getCards()) {
                if (card.rarity == CardRarity.Rare || card.rarity == CardRarity.MythicRare) {
                    PaperCard cardToAdd = FModel.getMagicDb().getCommonCards().getCard(card.name, edition.getCode());
                    if (cardToAdd != null) {
                        possibleCards.add(cardToAdd);
                    }
                }
            }
        }
        
        prizes.selectRareCards = possibleCards;
        
    }
    
    private BoosterPack getBoosterPack() {
        return BoosterPack.FN_FROM_SET.apply(getRandomEdition());
    }
    
    private PaperCard getPromoCard() {
        
        CardEdition randomEdition = getRandomEdition();
        List<CardInSet> cardsInEdition = new ArrayList<>();
        
        for (CardInSet card : randomEdition.getCards()) {
            if (card.rarity == CardRarity.Rare || card.rarity == CardRarity.MythicRare) {
                cardsInEdition.add(card);
            }
        }

        
        CardInSet randomCard;
        PaperCard promo = null;
        
        int attempts = 25;
        
        while (promo == null && attempts-- > 0) {
            randomCard = cardsInEdition.get((int) (Math.random() * cardsInEdition.size()));
            promo = FModel.getMagicDb().getCommonCards().getCard(randomCard.name, randomEdition.getCode());
        }
        
        if (promo == null) {
            return FModel.getQuest().getCards().addRandomRare();
        }
        
        return promo;
        
    }
    
    private CardEdition getRandomEdition() {
        
        List<CardEdition> editions = new ArrayList<>();
        for (String booster : boosterConfiguration.split("/")) {
            editions.add(FModel.getMagicDb().getEditions().get(booster));
        }
        
        return editions.get((int) (Math.random() * editions.size()));
        
    }

    private Set<CardEdition> getAllEditions() {
        
        Set<CardEdition> editions = new HashSet<>();
        for (String booster : boosterConfiguration.split("/")) {
            editions.add(FModel.getMagicDb().getEditions().get(booster));
        }

        return editions;

    }
    
    private int getBoosterPrice(final BoosterPack booster) {
        
        int value;
        
        String boosterName = booster.getName();

        if (MAP_PRICES.containsKey(boosterName)) {
            value = MAP_PRICES.get(boosterName);
        } else {
            value = 395;
        }
        
        return value;
        
    }
    
    public boolean playerHasMatchesLeft() {
        
        int playerIndex = -1;
        for (int i = standings.length - 1; i >= 0; i--) {
            if (standings[i].equals(HUMAN)) {
                playerIndex = i;
                break;
            }
        }
        
        int nextMatchIndex;
        
        switch (playerIndex) {
        
            case 0:
            case 1:
                nextMatchIndex = 8;
                break;
            case 2:
            case 3:
                nextMatchIndex = 9;
                break;
            case 4:
            case 5:
                nextMatchIndex = 10;
                break;
            case 6:
            case 7:
                nextMatchIndex = 11;
                break;
            case 8:
            case 9:
                nextMatchIndex = 12;
                break;
            case 10:
            case 11:
                nextMatchIndex = 13;
                break;
            case 12:
            case 13:
                nextMatchIndex = 14;
                break;
            case 14:
            default:
                nextMatchIndex = -1;
                break;
                
        }

        return nextMatchIndex != -1 && standings[nextMatchIndex].equals(UNDETERMINED);

    }
    
    public int getPlayerPlacement() {
        
        int playerIndex = -1;
        for (int i = standings.length - 1; i >= 0; i--) {
            if (standings[i].equals(HUMAN)) {
                playerIndex = i;
                break;
            }
        }
        
        if (playerIndex <= 7) {
            return 4;
        }
        
        if (playerIndex <= 11) {
            return 3;
        }
        
        if (playerIndex <= 13) {
            return 2;
        }
        
        if (playerIndex == 14) {
            return 1;
        }
            
        return -1;
        
    }

    public String getPlacementString() {
        
        int place = getPlayerPlacement();

        String output;

        switch (place) {
            case 1:
                output = "first";
                break;
            case 2:
                output = "second";
                break;
            case 3:
                output = "third";
                break;
            case 4:
                output = "fourth";
                break;
            default:
                output = "ERROR";
                break;
        }

        return output;

    }

    public boolean canEnter() {
        long creditsAvailable = FModel.getQuest().getAssets().getCredits();
        return creditsAvailable < getEntryFee();
    }

    public BoosterDraft enter() {
        FModel.getQuest().getAchievements().setCurrentDraft(this);
        FModel.getQuest().getAssets().subtractCredits(getEntryFee());
        return BoosterDraft.createDraft(LimitedPoolType.Block, FModel.getBlocks().get(getBlock()), getBoosterConfiguration());
    }

    public boolean isStarted() {
        return started;
    }
    
    public void start() {
        started = true;
        FModel.getQuest().save();
    }
    
    public void setStarted(final boolean started) {
        this.started = started;
    }

    @Override
    public String toString() {
        return title;
    }
    
    public static List<CardBlock> getAvailableBlocks(final QuestController quest) {

        List<CardBlock> possibleBlocks = new ArrayList<>();
        List<CardEdition> allowedQuestSets = new ArrayList<>();

        boolean questUsesLimitedCardPool = quest.getFormat() != null;

        if (questUsesLimitedCardPool) {

            List<String> allowedSetCodes = quest.getFormat().getAllowedSetCodes();

            for (String setCode : allowedSetCodes) {
                allowedQuestSets.add(FModel.getMagicDb().getEditions().get(setCode));
            }

        }

        List<CardBlock> blocks = new ArrayList<>();
        IStorage<CardBlock> storage = FModel.getBlocks();

        for (CardBlock b : storage) {
            if (b.getCntBoostersDraft() > 0) {
                blocks.add(b);
            }
        }

        if (questUsesLimitedCardPool) {
            for (CardBlock block : blocks) {

                boolean blockAllowed = true;

                for (CardEdition set : block.getSets()) {
                    if (!allowedQuestSets.contains(set)) {
                        blockAllowed = false;
                        break;
                    }
                }

                if (blockAllowed) {
                    possibleBlocks.add(block);
                }

            }
        } else {
            possibleBlocks.addAll(blocks);
        }

        return possibleBlocks.isEmpty() ? null : possibleBlocks;
        
    }
    
    /**
     * Generates a random draft event based on the provided quest's limitations.
     * @param quest The quest used to determine set availability.
     * @return The created draft or null in the event no draft could be created.
     */
    public static QuestEventDraft getRandomDraftOrNull(final QuestController quest) {
        
        List<CardBlock> possibleBlocks = getAvailableBlocks(quest);
        
        if (possibleBlocks == null) {
            return null;
        }
        
        Collections.shuffle(possibleBlocks);
        return getDraftOrNull(quest, possibleBlocks.get(0));
        
    }

    /**
     * Generates a  draft event based on the provided block.
     * @return The created draft or null in the event no draft could be created.
     */
    public static QuestEventDraft getDraftOrNull(final QuestController quest, final CardBlock block) {
        
        QuestEventDraft event = new QuestEventDraft(block.getName());
        
        if (block.getNumberSets() == 1) {
            String boosterConfiguration = "";
            for (int i = 0; i < block.getCntBoostersDraft(); i++) {
                boosterConfiguration += block.getSets()[0].getCode();
                if (i != block.getCntBoostersDraft() - 1) {
                    boosterConfiguration += "/";
                }
                event.boosterConfiguration = boosterConfiguration;
            }
        } else {
            List<String> possibleSetCombinations = getSetCombos(block);
            Collections.shuffle(possibleSetCombinations);
            event.boosterConfiguration = possibleSetCombinations.get(0);
        }
        
        event.block = block.getName();
        event.entryFee = calculateEntryFee(event.boosterConfiguration.split("/"));
        
        List<String> players = new ArrayList<>();
        players.add(HUMAN);
        players.add("1");
        players.add("2");
        players.add("3");
        players.add("4");
        players.add("5");
        players.add("6");
        players.add("7");
        
        Collections.shuffle(players);
        
        for (int i = 0; i < players.size(); i++) {
            event.standings[i] = players.get(i);
        }
        
        for (int i = 8; i < event.standings.length; i++) {
            event.standings[i] = UNDETERMINED;
        }
        
        List<String> usedNames = new ArrayList<>();
        usedNames.add(GamePlayerUtil.getGuiPlayer().getName());
        
        for (int i = 0; i < 7; i++) {
            event.aiNames[i] = NameGenerator.getRandomName("Any", "Any", usedNames);
            usedNames.add(event.aiNames[i]);
        }
        
        int numberOfIcons = GuiBase.getInterface().getAvatarCount();
        List<Integer> usedIcons = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            
            int icon;
            int attempts = 50;
            
            do {
                icon = (int) Math.floor(Math.random() * numberOfIcons);
            } while ((icon < 0 || usedIcons.contains(icon)) && attempts-- > 0);
            
            event.aiIcons[i] = icon;
            usedNames.add(event.aiNames[i]);
            usedIcons.add(icon);
            
        }
        
        return event;
        
    }
    
    private static int calculateEntryFee(String[] boosters) {
        
        int entryFee = 0;
        
        for (String boosterSet : boosters) {
            
            int value;
            String boosterName = FModel.getMagicDb().getEditions().get(boosterSet).getName() + " Booster Pack";
            
            if (MAP_PRICES.containsKey(boosterName)) {
                value = MAP_PRICES.get(boosterName);
            } else {
                value = 395;
            }
            
            entryFee += value;
        
        }
        
        return (int) (entryFee * 1.5);
        
    }
    
    private static List<String> getSetCombos(final CardBlock block) {

        List<String> setCombos = new ArrayList<>();
        CardEdition[] sets = block.getSets();
        
        boolean inverseDraftOrder = false; // before Mirrodin Besieged, sets were drafted in the opposite order (old->new instead of new->old)
        for (CardEdition set : sets) {
            Calendar cal = Calendar.getInstance();
            // This is set to Scars of Mirrodin date to account for the fact that MBS is drafted as a part of the Scars of Mirrodin block.
            // Setting it to the date of Mirrodin Besieged makes it treat all drafts that feature Scars of Mirrodin incorrectly.
            cal.set(Calendar.YEAR, 2010); 
            cal.set(Calendar.MONTH, Calendar.OCTOBER);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date SOMDate = cal.getTime();

            if (set.getDate().before(SOMDate)) {
                inverseDraftOrder = true;
            }
        }

        Arrays.sort(sets, new Comparator<CardEdition>() {
            @Override
            public int compare(CardEdition set1, CardEdition set2) {
                if (set1.getDate().after(set2.getDate())) {
                    return -1;
                } else if (set1.getDate().before(set2.getDate())) {
                    return 1;
                }
                return 0;
            }
        });
        
        if (sets.length == 2) {
            
            if (sets[0].getCards().length < 200) {
                if (!inverseDraftOrder) {
                    setCombos.add(String.format("%s/%s/%s", sets[0].getCode(), sets[1].getCode(), sets[1].getCode()));
                } else {
                    setCombos.add(String.format("%s/%s/%s", sets[1].getCode(), sets[1].getCode(), sets[0].getCode()));
                }
            } else if (sets[1].getCards().length < 200) {
                if (!inverseDraftOrder) {
                    setCombos.add(String.format("%s/%s/%s", sets[1].getCode(), sets[0].getCode(), sets[0].getCode()));
                } else {
                    setCombos.add(String.format("%s/%s/%s", sets[0].getCode(), sets[0].getCode(), sets[1].getCode()));
                }
            } else {
                setCombos.add(String.format("%s/%s/%s", sets[1].getCode(), sets[1].getCode(), sets[1].getCode()));
                if (!inverseDraftOrder) {
                    setCombos.add(String.format("%s/%s/%s", sets[0].getCode(), sets[1].getCode(), sets[1].getCode()));
                    setCombos.add(String.format("%s/%s/%s", sets[0].getCode(), sets[0].getCode(), sets[1].getCode()));
                } else {
                    setCombos.add(String.format("%s/%s/%s", sets[1].getCode(), sets[1].getCode(), sets[0].getCode()));
                    setCombos.add(String.format("%s/%s/%s", sets[1].getCode(), sets[0].getCode(), sets[0].getCode()));
                }
                setCombos.add(String.format("%s/%s/%s", sets[0].getCode(), sets[0].getCode(), sets[0].getCode()));
            }
            
        } else if (sets.length >= 3) {

            if (!inverseDraftOrder) {
                setCombos.add(String.format("%s/%s/%s", sets[0].getCode(), sets[1].getCode(), sets[2].getCode()));
            } else {
                setCombos.add(String.format("%s/%s/%s", sets[2].getCode(), sets[1].getCode(), sets[0].getCode()));
            }
            setCombos.add(String.format("%s/%s/%s", sets[2].getCode(), sets[2].getCode(), sets[2].getCode()));
            
        }
        
        return setCombos;
        
    }
    
}
