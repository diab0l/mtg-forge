package forge.quest.gui.main;

import forge.AllZone;
import forge.Card;
import forge.CardList;
import forge.CardUtil;
import forge.FileUtil;
import forge.Player;
import forge.deck.DeckManager;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.quest.data.QuestPreferences;
import forge.quest.data.QuestUtil;

import java.io.File;
import java.util.*;

/**
 * <p>QuestEventManager.</p>
 * MODEL - Manages collections of quest events (duelsquests, etc.)
 *
 * @author Forge
 * @version $Id$
 */
public class QuestEventManager {
    public List<QuestDuel> easyAIduels      = null;
    public List<QuestDuel> mediumAIduels    = null;
    public List<QuestDuel> hardAIduels      = null;
    public List<QuestDuel> veryHardAIduels  = null;
    
    public List<QuestDuel> allDuels             = null;
    public List<QuestChallenge> allChallenges   = null;
    
    /**<p>assembleAllEvents.</p>     * 
     * Reads all duel and challenge files and instantiates all events, 
     * and difficulty lists accordingly. Should be used sparingly.
     */
    public void assembleAllEvents() {
        this.allDuels = new ArrayList<QuestDuel>();
        this.allChallenges = new ArrayList<QuestChallenge>();
        
        List<String> contents;
        QuestEvent tempEvent;
        
        File file = ForgeProps.getFile(NewConstants.QUEST.DECKS);

        DeckManager manager = new DeckManager(file);
        
        File[] allFiles = ForgeProps.getFile(NewConstants.QUEST.DECKS).listFiles(DeckManager.DCKFileFilter);
        
        for(File f : allFiles) {
            contents = FileUtil.readFile(f);

            if(contents.get(0).trim().equals("[quest]")) {
                tempEvent = new QuestChallenge();
            assembleChallengeUniquedata(contents,(QuestChallenge)tempEvent);
                allChallenges.add((QuestChallenge)tempEvent);
            } // End if([quest])
            else {
                tempEvent = new QuestDuel();
                assembleDuelUniquedata(contents,(QuestDuel)tempEvent);
                allDuels.add((QuestDuel)tempEvent);
            }
            
            // Assemble metadata (may not be necessary later) and deck object.
            assembleEventMetadata(contents,tempEvent); 
            tempEvent.eventDeck = manager.getDeck(tempEvent.getName());
        } // End for(allFiles)
        
        assembleDuelDifficultyLists(); 
        
    } // End assembleAllEvents()
    
    /**
     * <p>assembleDuelUniqueData.</p>
     * Handler for any unique data contained in duel files.
     * 
     * @param contents
     * @param qd
     */
    private void assembleDuelUniquedata(List<String> contents, QuestDuel qd) {
        int eqpos;
        String key, value;
        
        for(String s : contents) {
            if (s.equals("[metadata]")) { break; }
            if (s.equals("[duel]"))    { continue; }
            if (s.equals(""))           { continue; }
            
            eqpos = s.indexOf('=');
            key = s.substring(0, eqpos);
            value = s.substring(eqpos + 1);

            if (key.equalsIgnoreCase("Name")) {
                qd.name = value;
            } 
        }
    }
    
    /**
     * <p>assembleChallengeUniquedata.</p>
     * Handler for any unique data contained in a challenge file.
     * 
     * @param contents
     * @param qc
     */
    private void assembleChallengeUniquedata(List<String> contents, QuestChallenge qc) {
        int eqpos;
        String key, value;
        
        // Unique properties
        for(String s : contents) {
            if (s.equals("[metadata]")) { break; }
            if (s.equals("[quest]"))    { continue; }
            if (s.equals(""))           { continue; }
            
            eqpos = s.indexOf('=');
            key = s.substring(0, eqpos);
            value = s.substring(eqpos + 1).trim();

            if (key.equalsIgnoreCase("ID")) {
                qc.id = Integer.parseInt(value);
            } 
            else if (key.equalsIgnoreCase("Repeat")) {
                qc.repeatable = Boolean.parseBoolean(value);
            } 
            else if (key.equalsIgnoreCase("AILife")) {
                qc.aiLife = Integer.parseInt(value);
            } 
            else if (key.equalsIgnoreCase("Wins")) {
                qc.winsReqd = Integer.parseInt(value);
            } 
            else if (key.equalsIgnoreCase("Credit Reward")) {
                qc.creditsReward = Integer.parseInt(value);
            }
            else if (key.equalsIgnoreCase("Card Reward")) {
                qc.cardReward = value;
                qc.cardRewardList = QuestUtil.generateCardRewardList(value);
            }
            // Human extra card list assembled here.
            else if(key.equalsIgnoreCase("HumanExtras") && !value.equals("")) {
                String[] names = value.split("\\|");
                CardList templist = new CardList();
                
                for(String n : names) { 
                    templist.add(readExtraCard(n, AllZone.getHumanPlayer()));
                }

                qc.humanExtraCards = templist;
            }
            // AI extra card list assembled here.
            else if(key.equalsIgnoreCase("AIExtras") && !value.equals("")) {
                String[] names = value.split("\\|");
                CardList templist = new CardList();

                for(String n : names) { 
                    templist.add(readExtraCard(n, AllZone.getComputerPlayer()));
                }
                
                qc.aiExtraCards = templist;
            }
            // Card reward list assembled here.
            else if(key.equalsIgnoreCase("Card Reward")) {
                qc.cardReward = value;
                qc.cardRewardList = QuestUtil.generateCardRewardList(value);
            } 
        }        
    }
    
    /**
     * <p>readExtraCard.</p>
     * Creates single card for a string read from unique event properties.
     * 
     * @param name
     * @param owner
     * @return
     */
    private Card readExtraCard(String name, Player owner) {
        // Token card creation
        Card tempcard;
        if(name.startsWith("TOKEN")) {
            tempcard = QuestUtil.createToken(name);
            tempcard.addController(owner);
            tempcard.setOwner(owner);
        }
        // Standard card creation
        else {
            tempcard = AllZone.getCardFactory().getCard(name, owner);
            tempcard.setCurSetCode(tempcard.getMostRecentSet());
            tempcard.setImageFilename(CardUtil.buildFilename(tempcard));
        }
        return tempcard;
    }
    
    
    /**
     * <p>assembleEventMetadata.</p>
     * Handler for metadata contained in event files.
     * 
     * @param contents
     * @param qe
     */
    private void assembleEventMetadata(List<String> contents, QuestEvent qe) {
        int eqpos;
        String key, value;
        
        for(String s : contents) {
            s = s.trim();
            eqpos = s.indexOf('=');
            
            if (s.equals("[main]"))     { break; }
            if (s.equals("[metadata]")) { continue; }
            if (s.equals(""))           { continue; }
            if (eqpos==-1)              { continue; }
            
            key = s.substring(0, eqpos);
            value = s.substring(eqpos + 1);

            if (key.equalsIgnoreCase("Name")) {
                qe.name = value;
            }
            else if (key.equalsIgnoreCase("Title")) {
                qe.title = value;
            } 
            else if (key.equalsIgnoreCase("Difficulty")) {
                qe.difficulty = value;
            } 
            else if (key.equalsIgnoreCase("Description")) {
                qe.description = value;
            } 
            else if (key.equalsIgnoreCase("Icon")) {
                qe.icon = value;
            }
        }
    }
    
    /**
     * <p>getAllDuels.</p>
     * Returns complete list of all duel objects.
     *
     * @return a {@link java.util.List} object.
     */
    public List<QuestDuel> getAllDuels() {
        return this.allDuels;
    }
    
    /**
     * <p>getAllChallenges.</p>
     * Returns complete list of all challenge objects.
     *
     * @return a {@link java.util.List} object.
     */
    public List<QuestChallenge> getAllChallenges() {
        return this.allChallenges;
    }
    
    /**
     * <p>assembleDuelDifficultyLists.</p>
     * Assemble duel deck difficulty lists
     */
    private void assembleDuelDifficultyLists() {
        easyAIduels       = new ArrayList<QuestDuel>();
        mediumAIduels     = new ArrayList<QuestDuel>();
        hardAIduels       = new ArrayList<QuestDuel>();
        veryHardAIduels   = new ArrayList<QuestDuel>();
        String s;
        
        for(QuestDuel qd : allDuels) {
            s = qd.getDifficulty();
            if(s.equalsIgnoreCase("easy")) {
                easyAIduels.add(qd);
            }
            else if(s.equalsIgnoreCase("medium")) {
                mediumAIduels.add(qd);
            }
            else if(s.equalsIgnoreCase("hard")) {
                hardAIduels.add(qd);
            }
            else if(s.equalsIgnoreCase("very hard")) {
                veryHardAIduels.add(qd);
            }
        }   
    }

    /**
     * <p>getDuelOpponent.</p>
     * Returns specific duel opponent from current shuffle of available duels. 
     * This is to make sure that the opponents do not change 
     * when the deck editor is launched.
     *
     * @param aiDeck a {@link java.util.List} object.
     * @param number a int.
     * @return a {@link java.lang.String} object.
     */
    private static QuestDuel getDuelOpponentByNumber(List<QuestDuel> aiDeck, int n) {
        List<QuestDuel> deckListCopy = new ArrayList<QuestDuel>(aiDeck);
        Collections.shuffle(deckListCopy, new Random(AllZone.getQuestData().getRandomSeed()));

        return deckListCopy.get(n);
    }
    
    /**
     * <p>getChallengeOpponentByNumber.</p>
     * Returns specific challenge event using its ID. 
     * This is to make sure that the opponents do not change 
     * when the deck editor is launched.
     * 
     * @param n
     * @return
     */
    private QuestChallenge getChallengeEventByNumber(int n) {
        for(QuestChallenge qc : allChallenges) {
            if(qc.getId()==n) {
                return qc;
            }
        }
        return null;
    }


    /**
     * <p>generateDuels.</p>
     * Generates an array of new duel opponents based on current win conditions.
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public List<QuestDuel> generateDuels() {
        
        int index = AllZone.getQuestData().getDifficultyIndex();
        List<QuestDuel> duelOpponents = new ArrayList<QuestDuel>();

        if (AllZone.getQuestData().getWin() < QuestPreferences.getWinsForMediumAI(index)) {
            duelOpponents.add(getDuelOpponentByNumber(easyAIduels, 0));
            duelOpponents.add(getDuelOpponentByNumber(easyAIduels, 1));
            duelOpponents.add(getDuelOpponentByNumber(easyAIduels, 2));
        }
        else if (AllZone.getQuestData().getWin() == QuestPreferences.getWinsForMediumAI(index)) {
            duelOpponents.add(getDuelOpponentByNumber(easyAIduels, 0));
            duelOpponents.add(getDuelOpponentByNumber(mediumAIduels, 0));
            duelOpponents.add(getDuelOpponentByNumber(mediumAIduels, 1));
        }
        else if (AllZone.getQuestData().getWin() < QuestPreferences.getWinsForHardAI(index)) {
            duelOpponents.add(getDuelOpponentByNumber(mediumAIduels, 0));
            duelOpponents.add(getDuelOpponentByNumber(mediumAIduels, 1));
            duelOpponents.add(getDuelOpponentByNumber(mediumAIduels, 2));
        }

        else if (AllZone.getQuestData().getWin() == QuestPreferences.getWinsForHardAI(index)) {
            duelOpponents.add( getDuelOpponentByNumber(mediumAIduels, 0));
            duelOpponents.add(getDuelOpponentByNumber(hardAIduels, 0));
            duelOpponents.add(getDuelOpponentByNumber(hardAIduels, 1));
        }

        else if (AllZone.getQuestData().getWin() >= QuestPreferences.getWinsForVeryHardAI(index)) {
            duelOpponents.add(getDuelOpponentByNumber(hardAIduels, 0));
            duelOpponents.add(getDuelOpponentByNumber(hardAIduels, 1));
            duelOpponents.add(getDuelOpponentByNumber(veryHardAIduels, 0));
        }
        else {
            duelOpponents.add(getDuelOpponentByNumber(veryHardAIduels, 0));
            duelOpponents.add(getDuelOpponentByNumber(veryHardAIduels, 1));
            duelOpponents.add(getDuelOpponentByNumber(veryHardAIduels, 2));
        }
        
        return duelOpponents;
    }
    
    /**
     * <p>generateChallenges.</p>
     * Generates an array of new challenge opponents based on current win conditions.
     *
     * @return a {@link java.util.List} object.
     */
    public List<QuestChallenge> generateChallenges() {
        forge.quest.data.QuestData questData = AllZone.getQuestData();

        List<QuestChallenge> challengeOpponents = new ArrayList<QuestChallenge>();
        
        int maxChallenges = questData.getWin() / 10;
        if (maxChallenges > 5) {
            maxChallenges = 5;
        }
        
        // Generate IDs as needed. 
        if (questData.getAvailableChallenges() == null || 
                questData.getAvailableChallenges().size() < maxChallenges) { 
            
            List<Integer> unlockedChallengeIds = new ArrayList<Integer>();
            List<Integer> availableChallengeIds = new ArrayList<Integer>();
            
            for(QuestChallenge qc : allChallenges) {
                if (qc.getWinsReqd() <= questData.getWin() &&
                         !questData.getCompletedChallenges().contains(qc.getId())) {
                    unlockedChallengeIds.add(qc.getId());
                }
            }                        

            Collections.shuffle(unlockedChallengeIds);
            
            for (int i = 0; i < maxChallenges; i++) {
                availableChallengeIds.add(unlockedChallengeIds.get(i));
            }
            
            questData.setAvailableChallenges(availableChallengeIds);
            questData.saveData();
        } 
        
        // Finally, pull challenge events from available IDs and return.
        for(int i : questData.getAvailableChallenges()) {
            challengeOpponents.add(getChallengeEventByNumber(i));                
        }
        
        return challengeOpponents;
    }

}
