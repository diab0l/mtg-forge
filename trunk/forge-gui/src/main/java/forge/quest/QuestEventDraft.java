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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Function;

import forge.GuiBase;
import forge.card.CardEdition;
import forge.card.CardEdition.CardInSet;
import forge.card.CardRarity;
import forge.item.BoosterPack;
import forge.item.PaperCard;
import forge.limited.LimitedPoolType;
import forge.model.CardBlock;
import forge.model.FModel;
import forge.quest.io.ReadPriceList;
import forge.util.NameGenerator;
import forge.util.storage.IStorage;

/**
 * <p>
 * QuestEvent.
 * </p>
 * 
 * MODEL - A basic event instance in Quest mode. Can be extended for use in
 * unique event types: battles, quests, and others.
 */
public class QuestEventDraft {

    public static final String UNDETERMINED = "quest_draft_undetermined_place";
    public static final String HUMAN = "quest_draft_human_place";
    public static final String DECK_NAME = "Tournament Deck";
    
    public static final Function<QuestEventDraft, String> FN_GET_NAME = new Function<QuestEventDraft, String>() {
        @Override public final String apply(QuestEventDraft qe) { return qe.title; }  
    };

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
    private int age = 15;
    
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
    
    public void setStanding(int position, String player) {
        standings[position] = player;
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
    
    public int getHumanLatestStanding() {
        int humanIndex = 0;
        for (int i = getStandings().length - 1; i >= 0; i--) {
            if (getStandings()[i].equals(HUMAN)) {
                humanIndex = i;
                break;
            }
        }
        return humanIndex;
    }
    
    public int getOpponentIndex(int playerIndex) {
        int result = (playerIndex % 2 == 0) ? playerIndex + 1 : playerIndex - 1;
        if (result == 15) {
            result = -1;
        }
        return result;
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
     * Generates the prizes for the player in an Object array.
     * Index 0: int - credits
     * Index 1: 
     * Index 2: ArrayList<PaperCard> - single cards
     */
    public Object[] getPrizes() {
        
        int place = getPlayerPlacement();
        int prizePool = entryFee * 8;
        
        int boosterPrices = 0;
        
        for (String boosterSet : boosterConfiguration.split("/")) {
            
            int value = 0;
            String boosterName = FModel.getMagicDb().getEditions().get(boosterSet).getName() + " Booster Pack";
            
            if (MAP_PRICES.containsKey(boosterName)) {
                value = MAP_PRICES.get(boosterName);
            } else {
                value = 395;
            }
            
            boosterPrices += value;
        
        }
        
        prizePool -= boosterPrices * 8;
        
        switch (place) {
            case 1:
                return generateFirstPlacePrizes(prizePool);
            case 2:
                return generateSecondPlacePrizes(prizePool);
            case 3:
                return generateThirdPlacePrizes(prizePool);
            case 4:
                return generateFourthPlacePrizes(prizePool);
        }
        
        return null;
        
    }
    
    private Object[] generateFirstPlacePrizes(final int prizePool) {
        
        int credits = 2 * (prizePool / 3); //First place gets 2/3 the total prize pool
        List<PaperCard> cards = new ArrayList<PaperCard>();
        List<BoosterPack> boosters = new ArrayList<BoosterPack>();
        
        cards.add(getPromoCard());

        int creditsForPacks = (credits / 2); //Spend 50% of the credits on packs
        
        while (true) {
            BoosterPack pack = getBoosterPack();
            int price = getBoosterPrice(pack);
            if (price > creditsForPacks + 150) { //Add a little room for near-same price packs.
                break;
            }
            creditsForPacks -= price;
            boosters.add(pack);
        }
        
        credits = (credits / 2) + creditsForPacks; //Add the leftover credits + 50%
        
        return new Object[] { credits, boosters, cards };
        
    }
    
    private Object[] generateSecondPlacePrizes(final int prizePool) {
        
        int credits = prizePool / 3; //Second place gets 1/3 the total prize pool
        List<PaperCard> cards = new ArrayList<PaperCard>();
        List<BoosterPack> boosters = new ArrayList<BoosterPack>();
        
        cards.add(getPromoCard());

        int creditsForPacks = (credits / 4) * 3; //Spend 75% of the credits on packs
        
        while (true) {
            BoosterPack pack = getBoosterPack();
            int price = getBoosterPrice(pack);
            if (price > creditsForPacks + 50) { //Add a little room for near-same price packs.
                break;
            }
            creditsForPacks -= price;
            boosters.add(pack);
        }
        
        credits = (credits / 4) + creditsForPacks; //Add the leftover credits + 25%
        
        return new Object[] { credits, boosters, cards };
        
    }
    
    private Object[] generateThirdPlacePrizes(final int prizePool) {
        
        int credits = 0;
        List<PaperCard> cards = new ArrayList<PaperCard>();
        
        cards.add(getPromoCard());
        
        List<BoosterPack> boosters = new ArrayList<BoosterPack>();
        boosters.add(getBoosterPack());
        
        return new Object[] { credits, boosters, cards };
        
    }
    
    private Object[] generateFourthPlacePrizes(final int prizePool) {
        
        int credits = 0;
        List<PaperCard> cards = new ArrayList<PaperCard>();
        
        cards.add(getPromoCard());
        
        return new Object[] { credits, null, cards };
        
    }
    
    private BoosterPack getBoosterPack() {
        return BoosterPack.FN_FROM_SET.apply(getRandomEdition());
    }
    
    private PaperCard getPromoCard() {
        
        CardEdition randomEdition = getRandomEdition();
        List<CardInSet> cardsInEdition = new ArrayList<CardInSet>();
        
        for (CardInSet card : randomEdition.getCards()) {
            if (card.rarity == CardRarity.Rare || card.rarity == CardRarity.MythicRare) {
                cardsInEdition.add(card);
            }
        }

        
        CardInSet randomCard = cardsInEdition.get((int) (Math.random() * cardsInEdition.size()));
        PaperCard promo = null;
        
        int attempts = 25;
        
        while (promo == null && attempts-- > 0) {
            randomCard = cardsInEdition.get((int) (Math.random() * cardsInEdition.size()));
            promo = FModel.getMagicDb().getCommonCards().getCard(randomCard.name, randomEdition.getCode());
        }
        
        if (promo == null) {
            final PaperCard c = FModel.getQuest().getCards().addRandomRare();
            return c;
        }
        
        return promo;
        
    }
    
    private CardEdition getRandomEdition() {
        
        List<CardEdition> editions = new ArrayList<CardEdition>();
        for (String booster : boosterConfiguration.split("/")) {
            editions.add(FModel.getMagicDb().getEditions().get(booster));
        }
        
        return editions.get((int) (Math.random() * editions.size()));
        
    }
    
    private int getBoosterPrice(BoosterPack booster) {
        
        int value = 0;
        String boosterName = booster.getName() + " Booster Pack";
        
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
        
        int nextMatchIndex = -1;
        
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
        
        if (nextMatchIndex == -1) {
            //No matches left in tournament
            return false;
        }
        
        if (!standings[nextMatchIndex].equals(UNDETERMINED)) {
            return false;
        }
        
        return true;
        
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
    
    public static String getPlacementString(int place) {
        
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
    
    /**
     * Generates a random draft event based on the provided quest's limitations.
     * Returns null in the event no draft could be created.
     * @param quest
     * @return
     */
    public static QuestEventDraft getRandomDraftOrNull(final QuestController quest) {
        
        List<CardBlock> possibleBlocks = new ArrayList<CardBlock>();
        List<CardEdition> allowedQuestSets = new ArrayList<CardEdition>();
        
        boolean questUsesLimitedCardPool = quest.getFormat() != null;
        
        if (questUsesLimitedCardPool) {
            
            List<String> allowedSetCodes = quest.getFormat().getAllowedSetCodes();
            
            for (String setCode : allowedSetCodes) {
                allowedQuestSets.add(FModel.getMagicDb().getEditions().get(setCode));
            }
            
        }
        
        LimitedPoolType draftType = LimitedPoolType.Block;
        
        List<CardBlock> blocks = new ArrayList<CardBlock>();
        IStorage<CardBlock> storage = draftType == LimitedPoolType.Block ? FModel.getBlocks() : FModel.getFantasyBlocks();

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
        
        if (possibleBlocks.isEmpty()) {
            return null;
        }
        
        Collections.shuffle(possibleBlocks);
        CardBlock selectedBlock = possibleBlocks.get(0);

        final Stack<String> sets = new Stack<String>();
        for (int k = selectedBlock.getSets().length - 1; k >= 0; k--) {
            sets.add(selectedBlock.getSets()[k].getCode());
        }
        
        for (String setCode : selectedBlock.getMetaSetNames()) {
            if (selectedBlock.getMetaSet(setCode).isDraftable()) {
                sets.push(setCode);
            }
        }
        
        QuestEventDraft event = new QuestEventDraft(selectedBlock.getName());
        
        if (selectedBlock.getNumberSets() == 1) {
            String boosterConfiguration = "";
            for (int i = 0; i < selectedBlock.getCntBoostersDraft(); i++) {
                boosterConfiguration += selectedBlock.getSets()[0].getCode();
                if (i != selectedBlock.getCntBoostersDraft() - 1) {
                    boosterConfiguration += "/";
                }
                event.boosterConfiguration = boosterConfiguration;
            }
        } else {
            List<String> possibleSetCombinations = getSetCombos(sets);
            Collections.shuffle(possibleSetCombinations);
            event.boosterConfiguration = possibleSetCombinations.get(0);
        }
        
        event.block = selectedBlock.getName();
        event.entryFee = calculateEntryFee(event.boosterConfiguration.split("/"));
        
        List<String> players = new ArrayList<String>();
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
        
        List<String> usedNames = new ArrayList<String>();
        usedNames.add(GuiBase.getInterface().getGuiPlayer().getName());
        
        for (int i = 0; i < 7; i++) {
            event.aiNames[i] = NameGenerator.getRandomName("Any", "Any", usedNames);
            usedNames.add(event.aiNames[i]);
        }
        
        int numberOfIcons = GuiBase.getInterface().getAvatarCount();
        List<Integer> usedIcons = new ArrayList<Integer>();
        
        for (int i = 0; i < 7; i++) {
            
            int icon = -1;
            int attempts = 50;
            
            do {
                icon = (int) Math.floor(Math.random() * numberOfIcons);
            } while ((icon < 0 || usedIcons.contains(icon)) && attempts-- > 0);
            
            event.aiIcons[i] = icon;
            usedNames.add(event.aiNames[i]);
            
        }
        
        return event;
        
    }
    
    private static int calculateEntryFee(String[] boosters) {
        
        int entryFee = 0;
        
        for (String boosterSet : boosters) {
            
            int value = 0;
            String boosterName = FModel.getMagicDb().getEditions().get(boosterSet).getName() + " Booster Pack";
            
            if (MAP_PRICES.containsKey(boosterName)) {
                value = MAP_PRICES.get(boosterName);
            } else {
                value = 395;
            }
            
            entryFee += value;
        
        }
        
        return (int) (entryFee * 1.65);
        
    }
    
    private static List<String> getSetCombos(final List<String> setz) {
        String[] sets = setz.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        List<String> setCombos = new ArrayList<String>();
        if (sets.length >= 2) {
            setCombos.add(String.format("%s/%s/%s", sets[0], sets[0], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[0], sets[0], sets[1]));
            setCombos.add(String.format("%s/%s/%s", sets[0], sets[1], sets[1]));
            if (sets.length >= 3) {
                setCombos.add(String.format("%s/%s/%s", sets[0], sets[1], sets[2]));
                setCombos.add(String.format("%s/%s/%s", sets[0], sets[2], sets[2]));
            }
            setCombos.add(String.format("%s/%s/%s", sets[1], sets[0], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[1], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[1], sets[1], sets[1]));
            if (sets.length >= 3) {
                setCombos.add(String.format("%s/%s/%s", sets[1], sets[1], sets[2]));
                setCombos.add(String.format("%s/%s/%s", sets[1], sets[2], sets[2]));
            }
        }
        if (sets.length >= 3) {
            setCombos.add(String.format("%s/%s/%s", sets[2], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[2], sets[2], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[2], sets[2], sets[1]));
            setCombos.add(String.format("%s/%s/%s", sets[2], sets[2], sets[2]));
        } // Beyond 3, skimp on the choice configurations, or the list will be enormous!
        if (sets.length >= 4) {
            setCombos.add(String.format("%s/%s/%s", sets[3], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[3], sets[2], sets[1]));
        }
        if (sets.length >= 5) {
            setCombos.add(String.format("%s/%s/%s", sets[4], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[4], sets[3], sets[2]));
            setCombos.add(String.format("%s/%s/%s", sets[4], sets[2], sets[0]));
        }
        if (sets.length >= 6) {
            setCombos.add(String.format("%s/%s/%s", sets[5], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[5], sets[3], sets[2]));
            setCombos.add(String.format("%s/%s/%s", sets[5], sets[4], sets[3]));
            setCombos.add(String.format("%s/%s/%s", sets[5], sets[2], sets[0]));
        }
        if (sets.length >= 7) {
            setCombos.add(String.format("%s/%s/%s", sets[6], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[6], sets[3], sets[2]));
            setCombos.add(String.format("%s/%s/%s", sets[6], sets[5], sets[4]));
            setCombos.add(String.format("%s/%s/%s", sets[6], sets[3], sets[0]));
        }
        if (sets.length >= 8) {
            setCombos.add(String.format("%s/%s/%s", sets[7], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[7], sets[3], sets[2]));
            setCombos.add(String.format("%s/%s/%s", sets[7], sets[5], sets[4]));
            setCombos.add(String.format("%s/%s/%s", sets[7], sets[6], sets[5]));
            setCombos.add(String.format("%s/%s/%s", sets[7], sets[3], sets[0]));
        }
        if (sets.length >= 9) {
            setCombos.add(String.format("%s/%s/%s", sets[8], sets[1], sets[0]));
            setCombos.add(String.format("%s/%s/%s", sets[8], sets[3], sets[2]));
            setCombos.add(String.format("%s/%s/%s", sets[8], sets[5], sets[4]));
            setCombos.add(String.format("%s/%s/%s", sets[8], sets[7], sets[6]));
            setCombos.add(String.format("%s/%s/%s", sets[8], sets[4], sets[0]));
        }
        return setCombos;
    }
    
}
