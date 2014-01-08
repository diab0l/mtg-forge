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
package forge.gui.match.controllers;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import forge.Command;
import forge.FThreads;
import forge.Singletons;
import forge.deck.Deck;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates.Presets;
import forge.game.combat.Combat;
import forge.game.combat.CombatUtil;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.SOverlayUtils;
import forge.gui.framework.ICDoc;
import forge.gui.framework.SLayoutIO;
import forge.gui.match.CMatchUI;
import forge.gui.match.views.VDock;
import forge.gui.toolbox.FOptionPane;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.SaveOpenDialog;
import forge.gui.toolbox.SaveOpenDialog.Filetypes;
import forge.item.PaperCard;
import forge.properties.FileLocation;
import forge.properties.ForgePreferences.FPref;
import forge.view.FView;

/**
 * Controls the dock panel in the match UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 */
public enum CDock implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    private int arcState;
    private Game game;

    public void setModel(Game game0, LobbyPlayer player0) {
        game = game0;
    }

    public Player findAffectedPlayer() {
        return Singletons.getControl().getCurrentPlayer();
    }

    /**
     * End turn.
     */
    public void endTurn() {
        Player p = findAffectedPlayer();

        if (p != null) {
            p.getController().autoPassUntil(PhaseType.CLEANUP);
            if (!CPrompt.SINGLETON_INSTANCE.getInputControl().passPriority()) {
                p.getController().autoPassCancel();
            }
        }
    }

    public void revertLayout() {
        SOverlayUtils.genericOverlay();
        FView.SINGLETON_INSTANCE.getPnlContent().removeAll();

        FThreads.invokeInEdtLater(new Runnable(){
            @Override public void run() {
                SLayoutIO.loadLayout(null);
                SOverlayUtils.hideOverlay();
            }
        });
    }

    public void saveLayout() {
        final SaveOpenDialog dlgSave = new SaveOpenDialog();
        final FileLocation layoutFile = Singletons.getControl().getCurrentScreen().getLayoutFile();
        final File defFile = layoutFile != null ? new File(layoutFile.userPrefLoc) : null;
        final File saveFile = dlgSave.SaveDialog(defFile, Filetypes.LAYOUT);
        if (saveFile != null) {
            SLayoutIO.saveLayout(saveFile);
        }
    }

    public void openLayout() {
        SOverlayUtils.genericOverlay();

        final SaveOpenDialog dlgOpen = new SaveOpenDialog();
        final FileLocation layoutFile = Singletons.getControl().getCurrentScreen().getLayoutFile();
        final File defFile = layoutFile != null ? new File(layoutFile.userPrefLoc) : null;
        final File loadFile = dlgOpen.OpenDialog(defFile, Filetypes.LAYOUT);

        if (loadFile != null) {
            FView.SINGLETON_INSTANCE.getPnlContent().removeAll();
            // let it redraw everything first

            FThreads.invokeInEdtLater(new Runnable() {
                @Override
                public void run() {
                    if (loadFile != null) {
                        SLayoutIO.loadLayout(loadFile);
                        SLayoutIO.saveLayout(null);
                    }
                    SOverlayUtils.hideOverlay();
                }
            });
        }
    }

    /**
     * View deck list.
     */
    public void viewDeckList() {
        showDeck(game.getMatch().getPlayers().get(0).getDeck());
    }

    /**
     * @return int State of targeting arc preference:<br>
     * 0 = don't draw<br>
     * 1 = draw on card mouseover<br>
     * 2 = always draw
     */
    public int getArcState() {
        return arcState;
    }

    /** @param state0 int */
    private void refreshArcStateDisplay() {
        switch (arcState) {
        case 0:
            VDock.SINGLETON_INSTANCE.getBtnTargeting().setToolTipText("Targeting arcs: Off");
            VDock.SINGLETON_INSTANCE.getBtnTargeting().setIcon(FSkin.getIcon(FSkin.DockIcons.ICO_ARCSOFF));
            VDock.SINGLETON_INSTANCE.getBtnTargeting().repaintSelf();
            break;
        case 1:
            VDock.SINGLETON_INSTANCE.getBtnTargeting().setToolTipText("Targeting arcs: Card mouseover");
            VDock.SINGLETON_INSTANCE.getBtnTargeting().setIcon(FSkin.getIcon(FSkin.DockIcons.ICO_ARCSHOVER));
            VDock.SINGLETON_INSTANCE.getBtnTargeting().repaintSelf();
            break;
        default:
            VDock.SINGLETON_INSTANCE.getBtnTargeting().setIcon(FSkin.getIcon(FSkin.DockIcons.ICO_ARCSON));
            VDock.SINGLETON_INSTANCE.getBtnTargeting().setToolTipText("Targeting arcs: Always on");
            VDock.SINGLETON_INSTANCE.getBtnTargeting().repaintSelf();
            break;
        }

        Singletons.getModel().getPreferences().setPref(FPref.UI_TARGETING_OVERLAY, String.valueOf(arcState));
        //FModel.SINGLETON_INSTANCE.getPreferences().save();
    }

    /** Attack with everyone. */
    public void alphaStrike() {
        final PhaseHandler ph = game.getPhaseHandler();

        final Player p = findAffectedPlayer();
        final Game game = p.getGame();
        Combat combat = game.getCombat();
        if (ph.is(PhaseType.COMBAT_DECLARE_ATTACKERS, p) && combat!= null) { // ph.is(...) includes null check
            List<Player> defenders = p.getOpponents();

            for (Card c : CardLists.filter(p.getCardsIn(ZoneType.Battlefield), Presets.CREATURES)) {
                if (combat.isAttacking(c))
                    continue;

                for(Player defender : defenders)
                    if( CombatUtil.canAttack(c, defender, combat)) {
                        combat.addAttacker(c, defender);
                        break;
                    }
            }
            //human.updateObservers();

            // TODO Is this redrawing immediately?
            FView.SINGLETON_INSTANCE.getFrame().repaint();
        }
    }

    /** Toggle targeting overlay painting. */
    public void toggleTargeting() {
        arcState++;

        if (arcState == 3) { arcState = 0; }

        refreshArcStateDisplay();
        FView.SINGLETON_INSTANCE.getFrame().repaint(); // repaint the match UI
    }

    public void setArcState(int state) {
        arcState = state;
    }

    /**
     * Receives click and programmatic requests for viewing a player's library
     * (typically used in dev mode). Allows copy of the cardlist to clipboard.
     * 
     * @param targetDeck {@link forge.deck.Deck}
     */
    private void showDeck(Deck targetDeck) {
        if (null == targetDeck) {
            return;
        }

        final TreeMap<String, Integer> deckMap = new TreeMap<String, Integer>();

        for (final Entry<PaperCard, Integer> s : targetDeck.getMain()) {
            deckMap.put(s.getKey().getName(), s.getValue());
        }

        final String nl = System.getProperty("line.separator");
        final StringBuilder deckList = new StringBuilder();
        String dName = targetDeck.getName();

        if (dName != null) {
            deckList.append(dName + nl);
        }

        for (final Entry<String, Integer> s : deckMap.entrySet()) {
            deckList.append(s.getValue() + " x " + s.getKey() + nl);
        }

        String ttl = "Decklist";
        if (dName != null) {
            ttl += " - " + dName;
        }

        final StringBuilder msg = new StringBuilder();
        if (deckMap.keySet().size() <= 32) {
            msg.append(deckList.toString() + nl);
        }
        else {
            msg.append("Decklist too long for dialog." + nl + nl);
        }

        msg.append("Copy Decklist to Clipboard?");

        if (FOptionPane.showConfirmDialog(msg.toString(), ttl, "OK", "Cancel")) {
            final StringSelection ss = new StringSelection(deckList.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
        }
    }
    // End DeckListAction

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @SuppressWarnings("serial")
    @Override
    public void initialize() {
        final String temp = Singletons.getModel().getPreferences()
                .getPref(FPref.UI_TARGETING_OVERLAY);

        // Old preference used boolean; new preference needs 0-1-2
        // (none, mouseover, solid).  Can remove this conditional
        // statement after a while...Doublestrike 17-10-12
        if (temp.equals("0") || temp.equals("1")) {
            arcState = Integer.valueOf(temp);
        }
        else {
            arcState = 2;
        }

        refreshArcStateDisplay();

        VDock.SINGLETON_INSTANCE.getBtnConcede().setCommand(new Command() {
            @Override
            public void run() {
                CMatchUI.SINGLETON_INSTANCE.concede();
            }
        });
        VDock.SINGLETON_INSTANCE.getBtnSettings().setCommand(new Command() {
            @Override
            public void run() {
                SOverlayUtils.showOverlay();
            }
        });
        VDock.SINGLETON_INSTANCE.getBtnEndTurn().setCommand(new Command() {
            @Override
            public void run() {
                endTurn();
            }
        });
        VDock.SINGLETON_INSTANCE.getBtnViewDeckList().setCommand(new Command() {
            @Override
            public void run() {
                viewDeckList();
            }
        });
        VDock.SINGLETON_INSTANCE.getBtnRevertLayout().setCommand(new Command() {
            @Override
            public void run() {
                revertLayout();
            }
        });
        VDock.SINGLETON_INSTANCE.getBtnOpenLayout().setCommand(new Command() {
            @Override
            public void run() {
                openLayout();
            }
        });
        VDock.SINGLETON_INSTANCE.getBtnSaveLayout().setCommand(new Command() {
            @Override
            public void run() {
                saveLayout();
            }
        });
        VDock.SINGLETON_INSTANCE.getBtnAlphaStrike().setCommand(new Command() {
            @Override
            public void run() {
                alphaStrike();
            }
        });
        VDock.SINGLETON_INSTANCE.getBtnTargeting().setCommand(new Command() {
            @Override
            public void run() {
                toggleTargeting();
            }
        });
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
    }

}
