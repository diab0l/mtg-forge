/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Nate
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
package forge.planarconquest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import forge.FThreads;
import forge.GuiBase;
import forge.LobbyPlayer;
import forge.card.CardType;
import forge.deck.Deck;
import forge.game.GameRules;
import forge.game.GameType;
import forge.game.GameView;
import forge.game.player.RegisteredPlayer;
import forge.interfaces.IButton;
import forge.interfaces.IWinLoseView;
import forge.item.PaperCard;
import forge.match.HostedMatch;
import forge.model.FModel;
import forge.planarconquest.ConquestPlane.AwardPool;
import forge.planarconquest.ConquestPreferences.CQPref;
import forge.player.GamePlayerUtil;
import forge.player.LobbyPlayerHuman;
import forge.properties.ForgePreferences.FPref;
import forge.quest.BoosterUtils;
import forge.util.Aggregates;
import forge.util.ItemPool;
import forge.util.storage.IStorage;

public class ConquestController {
    private ConquestData model;
    private transient IStorage<Deck> decks;
    private transient GameRunner gameRunner;
    private LobbyPlayerHuman humanPlayer;

    public ConquestController() {
    }

    public String getName() {
        return model == null ? null : model.getName();
    }

    public ConquestData getModel() {
        return model;
    }

    public IStorage<Deck> getDecks() {
        return decks;
    }

    public void load(final ConquestData model0) {
        model = model0;
        decks = model == null ? null : model.getDeckStorage();
    }

    public void save() {
        if (model != null) {
            model.saveData();
        }
    }

    /*private void playGame(final ConquestCommander commander, final int opponentIndex, final boolean isHumanDefending, final IVCommandCenter commandCenter) {
        gameRunner = new GameRunner(commander, opponentIndex, isHumanDefending, commandCenter);
        gameRunner.invokeAndWait();

        //after game finished
        if (gameRunner.wonGame) {
        }
        gameRunner = null;
    }*/

    public class GameRunner {
        private class Lock {
        }
        private final Lock lock = new Lock();

        public final ConquestCommander commander;
        public final ConquestEvent event;

        private GameRunner(final ConquestCommander commander0, final ConquestEvent event0) {
            commander = commander0;
            event = event0;
        }

        public final void invokeAndWait() {
            FThreads.assertExecutedByEdt(false); //not supported if on UI thread
            FThreads.invokeInEdtLater(new Runnable() {
                @Override
                public void run() {
                    //commandCenter.startGame(GameRunner.this);
                }
            });
            try {
                synchronized(lock) {
                    lock.wait();
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void finishStartingGame() {
            //determine game variants
            Set<GameType> variants = new HashSet<GameType>();
            event.addVariants(variants);

            final RegisteredPlayer humanStart = new RegisteredPlayer(commander.getDeck());
            final RegisteredPlayer aiStart = new RegisteredPlayer(event.getOpponentDeck());

            if (variants.contains(GameType.Commander)) { //add 10 starting life for both players if playing a Commander game
                humanStart.setStartingLife(humanStart.getStartingLife() + 10);
                aiStart.setStartingLife(aiStart.getStartingLife() + 10);
                humanStart.assignCommander();
                aiStart.assignCommander();
            }
            if (variants.contains(GameType.Planechase)) { //generate planar decks if planechase variant being applied
                humanStart.setPlanes(generatePlanarPool());
                aiStart.setPlanes(generatePlanarPool());
            }

            String humanPlayerName = commander.getPlayerName();
            String aiPlayerName = event.getOpponentName();
            if (humanPlayerName.equals(aiPlayerName)) {
                aiPlayerName += " (AI)"; //ensure player names are distinct
            }

            final List<RegisteredPlayer> starter = new ArrayList<RegisteredPlayer>();
            humanPlayer = new LobbyPlayerHuman(humanPlayerName);
            humanPlayer.setAvatarCardImageKey(commander.getCard().getImageKey(false));
            starter.add(humanStart.setPlayer(humanPlayer));

            final LobbyPlayer aiPlayer = GamePlayerUtil.createAiPlayer(aiPlayerName, -1);
            aiPlayer.setAvatarCardImageKey(event.getAvatarImageKey());
            starter.add(aiStart.setPlayer(aiPlayer));

            final boolean useRandomFoil = FModel.getPreferences().getPrefBoolean(FPref.UI_RANDOM_FOIL);
            for (final RegisteredPlayer rp : starter) {
                rp.setRandomFoil(useRandomFoil);
            }
            final GameRules rules = new GameRules(GameType.PlanarConquest);
            rules.setGamesPerMatch(1); //only play one game at a time
            rules.setManaBurn(FModel.getPreferences().getPrefBoolean(FPref.UI_MANABURN));
            rules.setCanCloneUseTargetsImage(FModel.getPreferences().getPrefBoolean(FPref.UI_CLONE_MODE_SOURCE));
            final HostedMatch hostedMatch = GuiBase.getInterface().hostMatch();
            FThreads.invokeInEdtNowOrLater(new Runnable(){
                @Override
                public void run() {
                    hostedMatch.startMatch(rules, null, starter, humanStart, GuiBase.getInterface().getNewGuiGame());
                }
            });
        }

        private void finish() {
            synchronized(lock) { //release game lock once game finished
                lock.notify();
            }
        }

        private List<PaperCard> generatePlanarPool() {
            String planeName = model.getCurrentPlane().getName();
            List<PaperCard> pool = new ArrayList<PaperCard>();
            List<PaperCard> otherPlanes = new ArrayList<PaperCard>();
            List<PaperCard> phenomenons = new ArrayList<PaperCard>();

            for (PaperCard c : FModel.getMagicDb().getVariantCards().getAllCards()) {
                CardType type = c.getRules().getType();
                if (type.isPlane()) {
                    if (type.hasSubtype(planeName)) {
                        pool.add(c); //always include card in pool if it matches the current plane
                    }
                    else {
                        otherPlanes.add(c);
                    }
                }
                else if (type.isPhenomenon()) {
                    phenomenons.add(c);
                }
            }

            //add between 0 and 2 phenomenons (where 2 is the most supported)
            int numPhenomenons = Aggregates.randomInt(0, 2);
            for (int i = 0; i < numPhenomenons; i++) {
                pool.add(Aggregates.removeRandom(phenomenons));
            }

            //add enough additional plane cards to reach a minimum 10 card deck
            while (pool.size() < 10) {
                pool.add(Aggregates.removeRandom(otherPlanes));
            }
            return pool;
        }
    }

    public void showGameRewards(final GameView game, final IWinLoseView<? extends IButton> view) {
        if (game.isMatchWonBy(humanPlayer)) {
            view.getBtnQuit().setText("Great!");

            //give controller a chance to run remaining logic on a separate thread
            view.showRewards(new Runnable() {
                @Override
                public void run() {
                    awardBooster(view);
                }
            });
        }
        else {
            view.getBtnQuit().setText("OK");
        }
    }

    public void onGameFinished(final GameView game) {
        if (game.isMatchWonBy(humanPlayer)) {
            model.addWin(gameRunner.event);
        }
        else {
            model.addLoss(gameRunner.event);
        }

        FModel.getConquest().save();
        FModel.getConquestPreferences().save();

        gameRunner.finish();
    }

    private void awardBooster(final IWinLoseView<? extends IButton> view) {
        AwardPool pool = FModel.getConquest().getModel().getCurrentPlane().getAwardPool();
        ConquestPreferences prefs = FModel.getConquestPreferences();
        List<PaperCard> rewards = new ArrayList<PaperCard>();
        int boostersPerMythic = prefs.getPrefInt(CQPref.BOOSTERS_PER_MYTHIC);
        int raresPerBooster = prefs.getPrefInt(CQPref.BOOSTER_RARES);
        for (int i = 0; i < raresPerBooster; i++) {
            if (pool.getMythics().isEmpty() || Aggregates.randomInt(1, boostersPerMythic) > 1) {
                pool.getRares().rewardCard(rewards);
            }
            else {
                pool.getMythics().rewardCard(rewards);
            }
        }

        int uncommonsPerBooster = prefs.getPrefInt(CQPref.BOOSTER_UNCOMMONS);
        for (int i = 0; i < uncommonsPerBooster; i++) {
            pool.getUncommons().rewardCard(rewards);
        }

        int commonsPerBooster = prefs.getPrefInt(CQPref.BOOSTER_COMMONS);
        for (int i = 0; i < commonsPerBooster; i++) {
            pool.getCommons().rewardCard(rewards);
        }

        BoosterUtils.sort(rewards);

        //remove any already unlocked cards from booster, calculating credit to reward instead
        //also build list of all rewards including replacement shards for each duplicate card
        //build this list in reverse order so commons appear first
        int shards = 0;
        List<ConquestReward> allRewards = new ArrayList<ConquestReward>();
        for (int i = rewards.size() - 1; i >= 0; i--) {
            int replacementShards = 0;
            PaperCard card = rewards.get(i);
            if (model.hasUnlockedCard(card)) {
                rewards.remove(i);
                replacementShards = pool.getShardValue(card);
                shards += replacementShards;
            }
            allRewards.add(new ConquestReward(card, replacementShards));
        }

        view.showConquestRewards("Booster Awarded", allRewards);
        model.unlockCards(rewards);
        model.rewardAEtherShards(shards);
    }

    public int calculateShardCost(ItemPool<PaperCard> filteredCards, int unfilteredCount) {
        if (filteredCards.isEmpty()) { return 0; }

        AwardPool pool = FModel.getConquest().getModel().getCurrentPlane().getAwardPool();

        //determine average value of filtered cards
        int totalValue = 0;
        for (Entry<PaperCard, Integer> entry : filteredCards) {
            totalValue += pool.getShardValue(entry.getKey());
        }
        float averageValue = totalValue / filteredCards.countDistinct();
        float multiplier = 1f + (float)FModel.getConquestPreferences().getPrefInt(CQPref.AETHER_MARKUP) / 100f;

        //TODO: Increase multiplier based on average percentage of cards filtered out for each rarity

        return Math.round(averageValue * multiplier);
    }
}
