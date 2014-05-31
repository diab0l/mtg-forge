/** Forge: Play Magic: the Gathering.
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
package forge.screens.match;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import forge.GuiBase;
import forge.LobbyPlayer;
import forge.Singletons;
import forge.game.Game;
import forge.game.player.Player;
import forge.gui.SOverlayUtils;
import forge.gui.framework.FScreen;
import forge.model.FModel;
import forge.quest.QuestController;
import forge.screens.home.quest.CSubmenuChallenges;
import forge.screens.home.quest.CSubmenuDuels;
import forge.screens.home.quest.CSubmenuQuestDraft;
import forge.screens.home.quest.QuestDraftUtils;
import forge.screens.home.quest.VSubmenuQuestDraft;

/**
 * <p>
 * QuestWinLose.
 * </p>
 * Processes win/lose presentation for Quest events. This presentation is
 * displayed by WinLoseFrame. Components to be added to pnlCustom in
 * WinLoseFrame should use MigLayout.
 * 
 */
public class QuestDraftWinLose extends ControlWinLose {
    private final transient ViewWinLose view;
    
    private final transient QuestController qData;

    /**
     * Instantiates a new quest win lose handler.
     * 
     * @param view0 ViewWinLose object
     * @param match2
     */
    public QuestDraftWinLose(final ViewWinLose view0, Game lastGame) {
        super(view0, lastGame);
        this.view = view0;
        qData = FModel.getQuest();
    }

    /**
     * <p>
     * populateCustomPanel.
     * </p>
     * Checks conditions of win and fires various reward display methods
     * accordingly.
     * 
     * @return true, if successful
     */
    @Override
    public final boolean populateCustomPanel() {
        
        QuestController quest = FModel.getQuest();
        
        final LobbyPlayer questLobbyPlayer = GuiBase.getInterface().getQuestPlayer();
        List<Player> players = lastGame.getRegisteredPlayers();
        boolean gameHadHumanPlayer = false;
        for (Player p : players) {
            if (p.getLobbyPlayer().equals(questLobbyPlayer)) {
                gameHadHumanPlayer = true;
                break;
            }
        }
        
        if (lastGame.getMatch().isMatchOver()) {

            String winner = lastGame.getOutcome().getWinningPlayer().getName();
            
            quest.getAchievements().getCurrentDraft().setWinner(winner);
            quest.save();
            
        }
        
        if (!gameHadHumanPlayer) {
            
            if (lastGame.getMatch().isMatchOver()) {
                this.actionOnQuitMatch();
                QuestDraftUtils.matchInProgress = false;
                QuestDraftUtils.update();
            } else {
                this.actionOnContinue();
                QuestDraftUtils.update();
            }
            return false;
            
        }
        
        view.getBtnRestart().setEnabled(false);
        
        if (lastGame.getMatch().isMatchOver()) {
            view.getBtnContinue().setEnabled(false);
            //TODO Add match quit to quit button listeners before match is officially over
            view.getBtnQuit().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    QuestDraftUtils.matchInProgress = false;
                    QuestDraftUtils.continueMatches();
                }
            });
        } else {
            view.getBtnQuit().setEnabled(false);
        }
        
        CSubmenuQuestDraft.SINGLETON_INSTANCE.update();
        VSubmenuQuestDraft.SINGLETON_INSTANCE.populate();
        
        return false; //We're not awarding anything, so never display the custom panel.
        
    }
    
    public final void actionOnQuitMatch() {

        CSubmenuDuels.SINGLETON_INSTANCE.update();
        CSubmenuChallenges.SINGLETON_INSTANCE.update();

        qData.setCurrentEvent(null);
        qData.save();
        FModel.getQuestPreferences().save();
        Singletons.getControl().writeMatchPreferences();

        Singletons.getControl().endCurrentGame();
        Singletons.getControl().setCurrentScreen(FScreen.HOME_SCREEN);

        SOverlayUtils.hideOverlay();
        
    }
    
}
