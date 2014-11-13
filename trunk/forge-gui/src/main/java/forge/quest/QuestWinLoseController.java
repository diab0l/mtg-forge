package forge.quest;

import forge.LobbyPlayer;
import forge.assets.FSkinProp;
import forge.card.CardEdition;
import forge.card.IUnOpenedProduct;
import forge.card.UnOpenedProduct;
import forge.game.GameEndReason;
import forge.game.GameFormat;
import forge.game.GameOutcome;
import forge.game.GameView;
import forge.game.player.GameLossReason;
import forge.game.player.PlayerOutcome;
import forge.game.player.PlayerStatistics;
import forge.game.player.PlayerView;
import forge.interfaces.IButton;
import forge.interfaces.IWinLoseView;
import forge.item.*;
import forge.model.FModel;
import forge.player.GamePlayerUtil;
import forge.properties.ForgePreferences.FPref;
import forge.quest.bazaar.QuestItemType;
import forge.quest.data.QuestPreferences;
import forge.quest.data.QuestPreferences.DifficultyPrefs;
import forge.quest.data.QuestPreferences.QPref;
import forge.util.MyRandom;
import forge.util.gui.SGuiChoose;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

public abstract class QuestWinLoseController {
    private final GameView lastGame;
    private final transient boolean wonMatch;
    private final transient boolean isAnte;
    private final transient QuestController qData;
    private final transient QuestEvent qEvent;

    public QuestWinLoseController(final GameView game0) {
        lastGame = game0;
        qData = FModel.getQuest();
        qEvent = qData.getCurrentEvent();
        wonMatch = lastGame.isMatchWonBy(GamePlayerUtil.getQuestPlayer());
        isAnte = FModel.getPreferences().getPrefBoolean(FPref.UI_ANTE);
    }

    public void showRewards(final IWinLoseView<? extends IButton> view) {
        view.getBtnRestart().setVisible(false);
        final QuestController qc = FModel.getQuest();

        // After the first game, reset the card shop pool to be able to buy back anted cards
        if (lastGame.getNumPlayedGamesInMatch() == 0) {
            qc.getCards().clearShopList();
            qc.getCards().getShopList();
        }

        final LobbyPlayer questLobbyPlayer = GamePlayerUtil.getQuestPlayer();
        PlayerView player = null;
        for (final PlayerView p : lastGame.getPlayers()) {
            if (p.getLobbyPlayer().equals(questLobbyPlayer)) {
                player = p;
            }
        }
        final PlayerView questPlayer = player;

        final boolean matchIsNotOver = !lastGame.isMatchOver();
        if (matchIsNotOver) {
            view.getBtnQuit().setText("Quit (-15 Credits)");
        }
        else {
            view.getBtnContinue().setVisible(false);
            if (wonMatch) {
                view.getBtnQuit().setText("Great!");
            }
            else {
                view.getBtnQuit().setText("OK");
            }
        }

        //give controller a chance to run remaining logic on a separate thread
        showRewards(new Runnable() {
            @Override
            public void run() {
                if (isAnte) {
                    // Won/lost cards should already be calculated (even in a draw)
                    GameOutcome.AnteResult anteResult = lastGame.getAnteResult(questPlayer);
                    if (anteResult != null) {
                        if (anteResult.wonCards != null) {
                            qc.getCards().addAllCards(anteResult.wonCards);
                        }
                        if (anteResult.lostCards != null) {
                            qc.getCards().loseCards(anteResult.lostCards);
                        }
                        anteReport(anteResult.wonCards, anteResult.lostCards, questPlayer.getLobbyPlayer().equals(lastGame.getWinningPlayer()));
                    }
                }

                if (matchIsNotOver) { return; } //skip remaining logic if match isn't over yet

                // TODO: We don't have a enum for difficulty?
                int difficulty = qData.getAchievements().getDifficulty();

                final int wins = qData.getAchievements().getWin();
                // Win case
                if (wonMatch) {
                    // Standard event reward credits
                    awardEventCredits();

                    // Challenge reward credits
                    if (qEvent instanceof QuestEventChallenge) {
                        awardChallengeWin();
                    }

                    else {
                        awardSpecialReward("Special bonus reward"); // If any
                        // Random rare for winning against a very hard deck
                        if (qEvent.getDifficulty() == QuestEventDifficulty.EXPERT) {
                            awardRandomRare("You've won a random rare for winning against a very hard deck.");
                        }
                    }
                    
                    awardWinStreakBonus();

                    // Random rare given at 50% chance (65% with luck upgrade)
                    if (getLuckyCoinResult()) {
                        awardRandomRare("You've won a random rare.");
                    }

                    // Award jackpot every 80 games won (currently 10 rares)

                    if ((wins > 0) && (((wins + 1) % 80) == 0)) {
                        awardJackpot();
                    }

                }
                // Lose case
                else {
                    penalizeLoss();
                }

                // Grant booster on a win, or on a loss in easy mode
                if (wonMatch || difficulty == 0) {
                    final int outcome = wonMatch ? wins : qData.getAchievements().getLost();
                    int winsPerBooster = FModel.getQuestPreferences().getPrefInt(DifficultyPrefs.WINS_BOOSTER, qData.getAchievements().getDifficulty());
                    if (winsPerBooster > 0 && (outcome + 1) % winsPerBooster == 0) {
                        awardBooster();
                    }
                }
            }
        });
    }

    private void anteReport(final List<PaperCard> cardsWon, List<PaperCard> cardsLost, boolean hasWon) {
        // Generate Swing components and attach.
        if (cardsWon != null && !cardsWon.isEmpty()) {
            showCards("Spoils! Cards won from ante.", cardsWon);
        }
        if (cardsLost != null && !cardsLost.isEmpty()) {
            showCards("Looted! Cards lost to ante.", cardsLost);
        }
    }

    public void actionOnQuit() {
        final int x = FModel.getQuestPreferences().getPrefInt(QPref.PENALTY_LOSS);

        // Record win/loss in quest data
        if (wonMatch) {
            qData.getAchievements().addWin();
        }
        else {
            qData.getAchievements().addLost();
            qData.getAssets().subtractCredits(x);
        }

        // Reset cards and zeppelin use
        if (qData.getAssets().hasItem(QuestItemType.ZEPPELIN)) {
            qData.getAssets().setItemLevel(QuestItemType.ZEPPELIN, 1);
        }

        if (qEvent instanceof QuestEventChallenge) {
            final String id = ((QuestEventChallenge) qEvent).getId();
            qData.getAchievements().getCurrentChallenges().remove(id);
            qData.getAchievements().addLockedChallenge(id);

            // Increment challenge counter to limit challenges available
            qData.getAchievements().addChallengesPlayed();
        }

        qData.setCurrentEvent(null);
        qData.save();
        FModel.getQuestPreferences().save();
    }
    

    /**
     * <p>
     * awardEventCredits.
     * </p>
     * Generates and displays standard rewards for gameplay and skill level.
     * 
     */
    private void awardEventCredits() {
        // TODO use q.qdPrefs to write bonus credits in prefs file
        final StringBuilder sb = new StringBuilder();

        int credTotal = 0;
        int credBase = 0;
        int credGameplay = 0;
        int credUndefeated = 0;
        int credEstates = 0;

        // Basic win bonus
        final int base = FModel.getQuestPreferences().getPrefInt(QPref.REWARDS_BASE);
        final double multiplier = qEvent.getDifficulty().getMultiplier();

        credBase = (int) (base * multiplier);
        
        sb.append(StringUtils.capitalize(qEvent.getDifficulty().getTitle()));
        sb.append(" opponent: ").append(credBase).append(" credits.\n");

        final int creditsForPreviousWins = (int) ((Double.parseDouble(FModel.getQuestPreferences()
                .getPref(QPref.REWARDS_WINS_MULTIPLIER)) * qData.getAchievements().getWin()));
        credBase += creditsForPreviousWins;
        
        sb.append("Bonus for previous wins: ").append(creditsForPreviousWins).append(
                  creditsForPreviousWins != 1 ? " credits.\n" : " credit.\n");
        
        // Gameplay bonuses (for each game win)
        boolean hasNeverLost = true;
        int lifeDifferenceCredits = 0;

        final LobbyPlayer localHuman = GamePlayerUtil.getQuestPlayer();
        for (final GameOutcome game : lastGame.getOutcomesOfMatch()) {
            if (!game.isWinner(localHuman)) {
                hasNeverLost = false;
                continue; // no rewards for losing a game
            }
            // Alternate win

            // final PlayerStatistics aiRating = game.getStatistics(computer.getName());
            PlayerStatistics humanRating = null;
            for (Entry<LobbyPlayer, PlayerStatistics> kvRating : game) {
                if (kvRating.getKey().equals(localHuman)) {
                    humanRating = kvRating.getValue();
                    continue;
                }

                final PlayerOutcome outcome = kvRating.getValue().getOutcome();
                final GameLossReason whyAiLost = outcome.lossState;
                int altReward = getCreditsRewardForAltWin(whyAiLost);

                String winConditionName = "Unknown (bug)";
                if (game.getWinCondition() == GameEndReason.WinsGameSpellEffect) {
                    winConditionName = game.getWinSpellEffect();
                    altReward = getCreditsRewardForAltWin(null);
                }
                else {
                    switch (whyAiLost) {
                    case Poisoned:
                        winConditionName = "Poison";
                        break;
                    case Milled:
                        winConditionName = "Milled";
                        break;
                    case SpellEffect:
                        winConditionName = outcome.loseConditionSpell;
                        break;
                    default:
                        break;
                    }
                }

                if (altReward > 0) {
                    credGameplay += altReward;
                    sb.append(String.format("Alternate win condition: <u>%s</u>! Bonus: %d credits.\n",
                            winConditionName, altReward));
                }
            }
            // Mulligan to zero
            final int cntCardsHumanStartedWith = humanRating.getOpeningHandSize();
            final int mulliganReward = FModel.getQuestPreferences().getPrefInt(QPref.REWARDS_MULLIGAN0);

            if (0 == cntCardsHumanStartedWith) {
                credGameplay += mulliganReward;
                sb.append(String.format("Mulliganed to zero and still won! Bonus: %d credits.\n", mulliganReward));
            }
            
            // Early turn bonus
            final int winTurn = game.getLastTurnNumber();
            final int turnCredits = getCreditsRewardForWinByTurn(winTurn);
            
            if (winTurn == 0) {
                sb.append("Won on turn zero!");
            }
            else if (winTurn == 1) {
                sb.append("Won in one turn!");
            }
            else if (winTurn <= 5) {
                sb.append("Won by turn 5!");
            }
            else if (winTurn <= 10) {
                sb.append("Won by turn 10!");
            }
            else if (winTurn <= 15) {
                sb.append("Won by turn 15!");
            }
            
            if (turnCredits > 0) {
                credGameplay += turnCredits;
                sb.append(String.format(" Bonus: %d credits.\n", turnCredits));
            }
            
            if (game.getLifeDelta() >= 50) {
                lifeDifferenceCredits += Math.max(Math.min((game.getLifeDelta() - 46) / 4, 750), 0);
            }
            
        } // End for(game)
        
        if (lifeDifferenceCredits > 0) {
            sb.append(String.format("Life total difference: %d credits.\n", lifeDifferenceCredits));
        }

        // Undefeated bonus
        if (hasNeverLost) {
            credUndefeated += FModel.getQuestPreferences().getPrefInt(QPref.REWARDS_UNDEFEATED);
            final int reward = FModel.getQuestPreferences().getPrefInt(QPref.REWARDS_UNDEFEATED);
            sb.append(String.format("You have not lost once! Bonus: %d credits.\n", reward));
        }

        // Estates bonus
        credTotal = credBase + credGameplay + credUndefeated + lifeDifferenceCredits;
        double estateValue = 0;
        switch (qData.getAssets().getItemLevel(QuestItemType.ESTATES)) {
        case 1:
            estateValue = .1;
            break;
        case 2:
            estateValue = .15;
            break;
        case 3:
            estateValue = .2;
            break;
        default:
            break;
        }
        if (estateValue > 0) {
            credEstates = (int) (estateValue * credTotal);
            sb.append("Estates bonus (").append((int) (100 * estateValue)).append("%): ").append(credEstates).append(" credits.\n");
            credTotal += credEstates;
        }

        // Final output
        String congrats = "\n";
        if (credTotal < 100) {
            congrats += "You've earned";
        }
        else if (credTotal < 250) {
            congrats += "Could be worse: ";
        }
        else if (credTotal < 500) {
            congrats += "A respectable";
        }
        else if (credTotal < 750) {
            congrats += "An impressive";
        }
        else {
            congrats += "Spectacular match!";
        }

        sb.append(String.format("%s %d credits in total.", congrats, credTotal));
        qData.getAssets().addCredits(credTotal);

        showMessage(sb.toString(), "Gameplay Results", FSkinProp.ICO_QUEST_GOLD);
    }

    /**
     * <p>
     * awardRandomRare.
     * </p>
     * Generates and displays a random rare win case.
     * 
     */
    private void awardRandomRare(final String message) {
        final PaperCard c = qData.getCards().addRandomRare();
        final List<PaperCard> cardsWon = new ArrayList<PaperCard>();
        cardsWon.add(c);

        showCards(message, cardsWon);
    }
    
    /**
     * <p>
     * awardWinStreakBonus.
     * </p>
     * Generates and displays a reward for maintaining a win streak.
     * 
     */
    private void awardWinStreakBonus() {
        int currentStreak = (qData.getAchievements().getWinStreakCurrent() + 1) % 50;

        final List<PaperCard> cardsWon = new ArrayList<>();
        List<PaperCard> cardsToAdd;
        String typeWon = "";
        boolean addDraftToken = false;
        
        switch (currentStreak) {
            case 3:
                cardsWon.addAll(qData.getCards().addRandomCommon(1));
                typeWon = "common";
                break;
            case 5:
                cardsWon.addAll(qData.getCards().addRandomUncommon(1));
                typeWon = "uncommon";
                break;
            case 7:
                cardsWon.addAll(qData.getCards().addRandomRareNotMythic(1));
                typeWon = "rare";
                break;
            case 10:
                cardsToAdd = qData.getCards().addRandomMythicRare(1);
                if (cardsToAdd != null) {
                    cardsWon.addAll(cardsToAdd);
                    typeWon = "mythic rare";
                } else {
                    cardsWon.addAll(qData.getCards().addRandomRareNotMythic(3));
                    typeWon = "rare";
                }
                break;
            case 25:
                cardsToAdd = qData.getCards().addRandomMythicRare(5);
                if (cardsToAdd != null) {
                    cardsWon.addAll(cardsToAdd);
                    typeWon = "mythic rare";
                } else {
                    cardsWon.addAll(qData.getCards().addRandomRareNotMythic(15));
                    typeWon = "rare";
                }
                addDraftToken = true;
                break;
            case 0: //The 50th win in the streak is 0, since (50 % 50 == 0)
                cardsToAdd = qData.getCards().addRandomMythicRare(10);
                if (cardsToAdd != null) {
                    cardsWon.addAll(cardsToAdd);
                    typeWon = "mythic rare";
                } else {
                    cardsWon.addAll(qData.getCards().addRandomRareNotMythic(30));
                    typeWon = "rare";
                }
                addDraftToken = true;
                break;
            default:
                return;
        }

        if (addDraftToken) {
            showMessage("For achieving a 25 win streak, you have been awarded a draft token!\nUse these tokens to generate new tournaments.", "Bonus Draft Token Reward", FSkinProp.ICO_QUEST_COIN);
            qData.getAchievements().addDraftToken();
        }

        if (cardsWon.size() > 0) {
            showCards("You have achieved a " + (currentStreak == 0 ? "50" : currentStreak) + " win streak and won " + cardsWon.size() + " " + typeWon + " card" + ((cardsWon.size() != 1) ? "s" : "") + "!", cardsWon);
        }
    }

    /**
     * <p>
     * awardJackpot.
     * </p>
     * Generates and displays jackpot win case.
     * 
     */
    private void awardJackpot() {
        final List<PaperCard> cardsWon = qData.getCards().addRandomRare(10);
        showCards("You just won 10 random rares!", cardsWon);
    }

    /**
     * <p>
     * awardBooster.
     * </p>
     * Generates and displays booster pack win case.
     * 
     */
    private void awardBooster() {
        List<PaperCard> cardsWon = null;

        String title;
        if (qData.getFormat() == null) {
            final List<GameFormat> formats = new ArrayList<GameFormat>();
            String preferredFormat = FModel.getQuestPreferences().getPref(QPref.BOOSTER_FORMAT);

            GameFormat pref = null;
            for (GameFormat f : FModel.getFormats().getOrderedList()) {
                formats.add(f);
                if (f.toString().equals(preferredFormat)) {
                    pref = f;
                }
            }

            Collections.sort(formats);

            final GameFormat selected = SGuiChoose.getChoices("Choose bonus booster format", 1, 1, formats, pref, null).get(0);
            FModel.getQuestPreferences().setPref(QPref.BOOSTER_FORMAT, selected.toString());

            cardsWon = qData.getCards().generateQuestBooster(selected.getFilterPrinted());
            qData.getCards().addAllCards(cardsWon);

            title = "Bonus booster pack from the \"" + selected.getName() + "\" format!";
        }
        else {
            final List<String> sets = new ArrayList<String>();

            for (SealedProduct.Template bd : FModel.getMagicDb().getBoosters()) {
                if (bd != null && qData.getFormat().isSetLegal(bd.getEdition())) {
                    sets.add(bd.getEdition());
                }
            }

            int maxChoices = 1;
            if (wonMatch) {
                maxChoices++;
                final int wins = qData.getAchievements().getWin();
                if ((wins + 1) % 5 == 0) { maxChoices++; }
                if ((wins + 1) % 20 == 0) { maxChoices++; }
                if ((wins + 1) % 50 == 0) { maxChoices++; }
                maxChoices += qData.getAssets().getItemLevel(QuestItemType.MEMBERSHIP_TOKEN);
            }

            List<CardEdition> options = new ArrayList<CardEdition>();
            
            while(!sets.isEmpty() && maxChoices > 0) {
                int ix = MyRandom.getRandom().nextInt(sets.size());
                String set = sets.get(ix);
                sets.remove(ix);
                options.add(FModel.getMagicDb().getEditions().get(set));
                maxChoices--;
            }

            final CardEdition chooseEd = SGuiChoose.one("Choose bonus booster set", options);

            IUnOpenedProduct product = new UnOpenedProduct(FModel.getMagicDb().getBoosters().get(chooseEd.getCode()));
            cardsWon = product.get();
            qData.getCards().addAllCards(cardsWon);
            title = "Bonus " + chooseEd.getName() + " booster pack!";
        }

        if (cardsWon != null) {
            //sort cards alphabetically so colors appear together and rares appear on top
            Collections.sort(cardsWon, new Comparator<PaperCard>() {
                @Override
                public int compare(PaperCard c1, PaperCard c2) {
                    return c1.getName().compareTo(c2.getName());
                }
            });
            Collections.sort(cardsWon, new Comparator<PaperCard>() {
                @Override
                public int compare(PaperCard c1, PaperCard c2) {
                    return c1.getRules().getColor().compareTo(c2.getRules().getColor());
                }
            });
            Collections.sort(cardsWon, new Comparator<PaperCard>() {
                @Override
                public int compare(PaperCard c1, PaperCard c2) {
                    return c2.getRarity().compareTo(c1.getRarity());
                }
            });
            showCards(title, cardsWon);
        }
    }

    /**
     * <p>
     * awardChallengeWin.
     * </p>
     * Generates and displays win case for challenge event.
     * 
     */
    private void awardChallengeWin() {
        final long questRewardCredits = ((QuestEventChallenge) qEvent).getCreditsReward();

        final StringBuilder sb = new StringBuilder();
        sb.append("Challenge completed.\n\n");
        sb.append("Challenge bounty: " + questRewardCredits + " credits.");

        qData.getAssets().addCredits(questRewardCredits);

        showMessage(sb.toString(), "Challenge Rewards for \"" + ((QuestEventChallenge) qEvent).getTitle() + "\"", FSkinProp.ICO_QUEST_BOX);

        awardSpecialReward(null);
    }

    /**
     * <p>
     * awardSpecialReward.
     * </p>
     * This builds the card reward based on the string data.
     * @param message String, reward text to be displayed, if any
     */
    private void awardSpecialReward(String message) {
        final List<InventoryItem> itemsWon = ((QuestEvent) qEvent).getCardRewardList();

        if (itemsWon == null || itemsWon.isEmpty()) {
            return;
        }

        final List<PaperCard> cardsWon = new ArrayList<PaperCard>();

        for (InventoryItem ii : itemsWon) {
            if (ii instanceof PaperCard) {
                cardsWon.add((PaperCard) ii);
            }
            else if (ii instanceof TournamentPack || ii instanceof BoosterPack) {
                List<PaperCard> boosterCards = new ArrayList<PaperCard>();
                SealedProduct booster = null;
                if (ii instanceof BoosterPack) {
                    booster = (BoosterPack) ((BoosterPack) ii).clone();
                    boosterCards.addAll(booster.getCards());
                }
                else if (ii instanceof TournamentPack) {
                    booster = (TournamentPack) ((TournamentPack) ii).clone();
                    boosterCards.addAll(booster.getCards());
                }
                if (!boosterCards.isEmpty()) {
                    qData.getCards().addAllCards(boosterCards);
                    showCards("Extra " + ii.getName() + "!", boosterCards);
                }
            }
            else if (ii instanceof IQuestRewardCard) {
                final List<PaperCard> cardChoices = ((IQuestRewardCard) ii).getChoices();
                final PaperCard chosenCard = (null == cardChoices ? null : SGuiChoose.one("Choose " + ((IQuestRewardCard) ii).getName(), cardChoices));
                if (null != chosenCard) {
                    cardsWon.add(chosenCard);
                }
            }
        }
        if (cardsWon != null && !cardsWon.isEmpty()) {
            if (message == null) {
                message = "Cards Won";
            }
            showCards(message, cardsWon);
            qData.getCards().addAllCards(cardsWon);
        }
    }

    private void penalizeLoss() {
        final int x = FModel.getQuestPreferences().getPrefInt(QPref.PENALTY_LOSS);
        showMessage("You lose! You have lost " + x + " credits.", "Gameplay Results", FSkinProp.ICO_QUEST_HEART);
    }

    /**
     * <p>
     * getLuckyCoinResult.
     * </p>
     * A chance check, for rewards like random rares.
     * 
     * @return boolean
     */
    private boolean getLuckyCoinResult() {
        final boolean hasCoin = qData.getAssets().getItemLevel(QuestItemType.LUCKY_COIN) >= 1;

        return MyRandom.getRandom().nextFloat() <= (hasCoin ? 0.65f : 0.5f);
    }

    /**
     * <p>
     * getCreditsRewardForAltWin.
     * </p>
     * Retrieves credits for win under special conditions.
     * 
     * @param whyAiLost GameLossReason
     * @return int
     */
    private int getCreditsRewardForAltWin(final GameLossReason whyAiLost) {
        QuestPreferences qp = FModel.getQuestPreferences();
        if (null == whyAiLost) {
            // Felidar, Helix Pinnacle, etc.
            return qp.getPrefInt(QPref.REWARDS_ALTERNATIVE);
        }
        switch (whyAiLost) {
        case LifeReachedZero:
            return 0; // nothing special here, ordinary kill
        case Milled:
            return qp.getPrefInt(QPref.REWARDS_MILLED);
        case Poisoned:
            return qp.getPrefInt(QPref.REWARDS_POISON);
        case SpellEffect: // Door to Nothingness, etc.
            return qp.getPrefInt(QPref.REWARDS_ALTERNATIVE);
        default:
            return 0;
        }
    }

    /**
     * <p>
     * getCreditsRewardForWinByTurn.
     * </p>
     * Retrieves credits for win on or under turn count.
     * 
     * @param iTurn int - turn count 
     * @return int credits won
     */
    private int getCreditsRewardForWinByTurn(final int iTurn) {
        int credits;

        if (iTurn <= 1) {
            credits = FModel.getQuestPreferences().getPrefInt(QPref.REWARDS_TURN1);
        }
        else if (iTurn <= 5) {
            credits = FModel.getQuestPreferences().getPrefInt(QPref.REWARDS_TURN5);
        }
        else if (iTurn <= 10) {
            credits = FModel.getQuestPreferences().getPrefInt(QPref.REWARDS_TURN10);
        }
        else if (iTurn <= 15) {
            credits = FModel.getQuestPreferences().getPrefInt(QPref.REWARDS_TURN15);
        }
        else {
            credits = 0;
        }

        return credits;
    }

    protected abstract void showRewards(Runnable runnable);
    protected abstract void showCards(String title, List<PaperCard> cards);
    protected abstract void showMessage(String message, String title, FSkinProp icon);
}
