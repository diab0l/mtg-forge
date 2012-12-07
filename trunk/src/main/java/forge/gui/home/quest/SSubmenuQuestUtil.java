package forge.gui.home.quest;

import java.awt.Color;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.base.Supplier;

import forge.Card;
import forge.Singletons;
import forge.card.CardEdition;
import forge.control.FControl;
import forge.deck.Deck;
import forge.game.GameType;
import forge.game.MatchStartHelper;
import forge.game.PlayerStartConditions;
import forge.game.player.LobbyPlayer;
import forge.game.player.PlayerType;
import forge.gui.SOverlayUtils;
import forge.gui.deckeditor.CDeckEditorUI;
import forge.gui.deckeditor.controllers.CEditorQuestCardShop;
import forge.gui.toolbox.FSkin;
import forge.quest.QuestController;
import forge.quest.QuestEvent;
import forge.quest.QuestEventChallenge;
import forge.quest.QuestMode;
import forge.quest.QuestUtil;
import forge.quest.QuestUtilUnlockSets;
import forge.quest.bazaar.QuestItemType;
import forge.quest.bazaar.QuestPetController;
import forge.quest.data.QuestAchievements;
import forge.quest.data.QuestAssets;
import forge.quest.data.QuestPreferences.QPref;

/** 
 * Utilities for the quest submenu, all over the MVC spectrum.
 * If a piece of code can be reused, it's dumped here.
 * 
 * <br><br><i>(S at beginning of class name denotes a static factory.)</i>
 */
public class SSubmenuQuestUtil {
    private static QuestEvent event;

    /**
     * <p>
     * nextChallengeInWins.
     * </p>
     * 
     * @return a int.
     */
    public static int nextChallengeInWins() {
        final QuestController qData = Singletons.getModel().getQuest();
        final int challengesPlayed = qData.getAchievements().getChallengesPlayed();

        final int wins = qData.getAchievements().getWin();
        final int turnsToUnlock = Singletons.getModel().getQuest().getChallengesManager().getTurnsToUnlockChallenge();
        final int delta;

        // First challenge unlocks after minimum wins reached.
        if (wins < 2 * turnsToUnlock) {
            delta = 2 * turnsToUnlock - wins;
        }
        else {
            // More than enough wins
            if (wins / turnsToUnlock > challengesPlayed) {
                delta = 0;
            }
            // This part takes the "unlimited challenge" bug into account;
            // a player could have an inflated challengesPlayed value.
            // Added 09-2012, can be removed after a while.
            else if (wins < challengesPlayed * turnsToUnlock) {
                delta = (challengesPlayed * turnsToUnlock - wins) + turnsToUnlock;
            }
            // Data OK, but not enough wins yet (default).
            else {
                delta = turnsToUnlock - wins % turnsToUnlock;
            }
        }

        return (delta > 0) ? delta : 0;
    }

    private static void updatePlantAndPetForView(final IVQuestStats view, final QuestController qCtrl) {
        for (int iSlot = 0; iSlot < QuestController.MAX_PET_SLOTS; iSlot++) {
            final List<QuestPetController> petList = qCtrl.getPetsStorage().getAvaliablePets(iSlot, qCtrl.getAssets());
            final String currentPetName = qCtrl.getSelectedPet(iSlot);

            if (iSlot == 0) { // Plant visiblity
                if (petList.isEmpty()) {
                    view.getCbPlant().setVisible(false);
                }
                else {
                    view.getCbPlant().setVisible(true);
                    view.getCbPlant().setSelected(currentPetName != null);
                }
            }
            if (iSlot == 1) {
                view.getCbxPet().removeAllItems();

                // Pet list visibility
                if (petList.size() > 0) {
                    view.getCbxPet().setVisible(true);
                    view.getCbxPet().addItem("Don't summon a pet");

                    for (final QuestPetController pet : petList) {
                        String name = "Summon " + pet.getName();
                        view.getCbxPet().addItem(name);
                        if (pet.getName().equals(currentPetName)) {
                            view.getCbxPet().setSelectedItem(name);
                        }
                    }
                } else {
                    view.getCbxPet().setVisible(false);
                }
            }
        }

        if (view.equals(VSubmenuChallenges.SINGLETON_INSTANCE)) {
            view.getLblZep().setVisible(qCtrl.getAssets().hasItem(QuestItemType.ZEPPELIN));
            view.getLblZep().setEnabled(qCtrl.getAssets().getItemLevel(
                    QuestItemType.ZEPPELIN) == 2 ? false : true);
            view.getLblZep().setForeground(qCtrl.getAssets().getItemLevel(
                    QuestItemType.ZEPPELIN) == 2 ? Color.gray : FSkin.getColor(FSkin.Colors.CLR_TEXT));
        }
        else {
            view.getLblZep().setVisible(false);
        }
    }

    /**
     * Updates all quest info in a view, using
     * retrieval methods dictated in IVQuestStats.<br>
     * - Stats<br>
     * - Pets<br>
     * - Current deck info<br>
     * - "Challenge In" info<br>
     * 
     * @param view0 {@link forge.gui.home.quest.IVQuestStats}
     */
    public static void updateQuestView(final IVQuestStats view0) {
        final QuestController qCtrl = Singletons.getModel().getQuest();
        final QuestAchievements qA = qCtrl.getAchievements();
        final QuestAssets qS = qCtrl.getAssets();

        if (qA == null) { return; }

        // Fantasy UI display
        view0.getLblNextChallengeInWins().setVisible(true);
        view0.getBtnBazaar().setVisible(true);
        view0.getLblLife().setVisible(true);

        // Stats panel
        view0.getLblCredits().setText("Credits: " + qS.getCredits());
        view0.getLblLife().setText("Life: " + qS.getLife(qCtrl.getMode()));
        view0.getLblWins().setText("Wins: " + qA.getWin());
        view0.getLblLosses().setText("Losses: " + qA.getLost());

        // Show or hide the set unlocking button

        view0.getBtnUnlock().setVisible(qCtrl.getUnlocksTokens() > 0);

        // Challenge in wins
        final int num = SSubmenuQuestUtil.nextChallengeInWins();
        final String str;
        if (num == 0) {
            str = "Your exploits have been noticed. An opponent has challenged you.";
        }
        else if (num == 1) {
            str = "A new challenge will be available after 1 more win.";
        }
        else {
            str = "A new challenge will be available in " + num + " wins.";
        }

        view0.getLblNextChallengeInWins().setText(str);

        view0.getLblWinStreak().setText(
                "Win streak: " + qA.getWinStreakCurrent()
                + " (Best:" + qA.getWinStreakBest() + ")");

        // Current deck message
        final JLabel lblCurrentDeck = view0.getLblCurrentDeck();
        if (SSubmenuQuestUtil.getCurrentDeck() == null) {
            lblCurrentDeck.setForeground(Color.red.darker());
            lblCurrentDeck.setText("Build, then select a deck in the \"Decks\" submenu.  ");
        }
        else {
            lblCurrentDeck.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
            lblCurrentDeck.setText("Your current deck is \""
                    + SSubmenuQuestUtil.getCurrentDeck().getName() + "\".");
        }

        // Start panel: pet, plant, zep.
        if (qCtrl.getMode() == QuestMode.Fantasy) {
            updatePlantAndPetForView(view0, qCtrl);
        }
        else {
            // Classic mode display changes
            view0.getCbxPet().setVisible(false);
            view0.getCbPlant().setVisible(false);
            view0.getLblZep().setVisible(false);
            view0.getLblNextChallengeInWins().setVisible(false);
            view0.getBtnBazaar().setVisible(false);
            view0.getLblLife().setVisible(false);
        }
    }

    /** @return {@link forge.deck.Deck} */
    public static Deck getCurrentDeck() {
        Deck d = null;

        if (Singletons.getModel().getQuest().getAssets() != null) {
            d = Singletons.getModel().getQuest().getMyDecks().get(
                Singletons.getModel().getQuestPreferences().getPreference(QPref.CURRENT_DECK));
        }

        return d;
    }

    /** Updates the current selected quest event, used when game is started.
     * @param event0 {@link forge.quest.QuestEvent}
     */
    public static void setEvent(final QuestEvent event0) {
        SSubmenuQuestUtil.event = event0;
    }

    /** */
    public static void showSpellShop() {
        CDeckEditorUI.SINGLETON_INSTANCE.setCurrentEditorController(
                new CEditorQuestCardShop(Singletons.getModel().getQuest()));
        FControl.SINGLETON_INSTANCE.changeState(FControl.DECK_EDITOR_QUEST);
    }

    /** */
    public static void showBazaar() {
        Singletons.getControl().changeState(FControl.QUEST_BAZAAR);
        Singletons.getView().getFrame().validate();
    }

    /** */
    public static void chooseAndUnlockEdition() {
        final QuestController qData = Singletons.getModel().getQuest();
        ImmutablePair<CardEdition, Integer> toUnlock = QuestUtilUnlockSets.chooseSetToUnlock(qData, false, null);
        if (toUnlock == null) {
            return;
        }

        CardEdition unlocked = toUnlock.left;
        qData.getAssets().subtractCredits(toUnlock.right);
        JOptionPane.showMessageDialog(null, "You have successfully unlocked " + unlocked.getName() + "!",
                unlocked.getName() + " unlocked!",
                JOptionPane.PLAIN_MESSAGE);

        QuestUtilUnlockSets.doUnlock(qData, unlocked);
    }

    /** */
    public static void startGame() {
        final QuestController qData = Singletons.getModel().getQuest();

        Deck deck = SSubmenuQuestUtil.getCurrentDeck();
        if (deck == null) {
            String msg = "Please select a Quest Deck.";
            JOptionPane.showMessageDialog(null, msg, "No Deck", JOptionPane.ERROR_MESSAGE);
            System.out.println(msg);
            return;
        } else if (!deck.meetsGameTypeRequirements(GameType.Quest)) {
            String msg = "Chosen Deck doesn't meet the requirements (Minimum 40 cards). Please edit or choose a different deck.";
            JOptionPane.showMessageDialog(null, msg, "Invalid Deck", JOptionPane.ERROR_MESSAGE);
            System.out.println(msg);
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SOverlayUtils.startGameOverlay();
                SOverlayUtils.showOverlay();
            }
        });

        final SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
            @Override
            public Object doInBackground() {

                qData.getChallengesManager().randomizeOpponents();
                qData.getDuelsManager().randomizeOpponents();
                qData.setCurrentEvent(event);
                qData.save();

                return null;
            }

            @Override
            public void done() {
                SOverlayUtils.hideOverlay();
            }
        };
        worker.execute();
        PlayerStartConditions humanStart = new PlayerStartConditions(SSubmenuQuestUtil.getCurrentDeck());
        PlayerStartConditions aiStart = new PlayerStartConditions(event.getEventDeck());

        if (qData.getMode() == QuestMode.Fantasy) {
            int lifeAI = 20;
            int extraLifeHuman = 0;

            if (event instanceof QuestEventChallenge) {
                lifeAI = ((QuestEventChallenge) event).getAILife();

                if (qData.getAssets().hasItem(QuestItemType.ZEPPELIN)) {
                    extraLifeHuman = 3;
                }
            }

            humanStart.setStartingLife(qData.getAssets().getLife(qData.getMode()) + extraLifeHuman);
            aiStart.setStartingLife(lifeAI);

            humanStart.setCardsOnBattlefield(new Supplier<Iterable<Card>>() {
                @Override public Iterable<Card> get() { return QuestUtil.getHumanStartingCards(qData, event); } });
            aiStart.setCardsOnBattlefield(new Supplier<Iterable<Card>>() {
                @Override public Iterable<Card> get() { return QuestUtil.getComputerStartingCards(event); } });
        } // End isFantasy

        MatchStartHelper msh = new MatchStartHelper();
        msh.addPlayer(Singletons.getControl().getLobby().getQuestPlayer(), humanStart);

        LobbyPlayer aiPlayer = Singletons.getControl().getLobby().findLocalPlayer(PlayerType.COMPUTER, event.getTitle());
        aiPlayer.setPicture(event.getIconFilename());
        msh.addPlayer(aiPlayer, aiStart);

        Singletons.getModel().getMatch().initMatch(GameType.Quest, msh.getPlayerMap());
        Singletons.getModel().getMatch().startRound();
    }

    /** Duplicate in DeckEditorQuestMenu and
     * probably elsewhere...can streamline at some point
     * (probably shouldn't be here).
     * 
     * @param in &emsp; {@link java.lang.String}
     * @return {@link java.lang.String}
     */
    public static String cleanString(final String in) {
        final StringBuffer out = new StringBuffer();
        final char[] c = in.toCharArray();

        for (int i = 0; (i < c.length) && (i < 20); i++) {
            if (Character.isLetterOrDigit(c[i]) || (c[i] == '-') || (c[i] == '_') || (c[i] == ' ')) {
                out.append(c[i]);
            }
        }

        return out.toString();
    }
}

