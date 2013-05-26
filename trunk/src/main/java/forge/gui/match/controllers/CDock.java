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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import forge.Card;
import forge.CardLists;
import forge.FThreads;
import forge.CardPredicates.Presets;
import forge.Command;
import forge.deck.Deck;
import forge.game.GameState;
import forge.game.phase.CombatUtil;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.SOverlayUtils;
import forge.gui.framework.ICDoc;
import forge.gui.framework.SLayoutIO;
import forge.gui.match.views.VDock;
import forge.gui.toolbox.FOverlay;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.SaveOpenDialog;
import forge.gui.toolbox.SaveOpenDialog.Filetypes;
import forge.item.CardPrinted;
import forge.model.FModel;
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
    private GameState game;
    private LobbyPlayer player;

    public void setModel(GameState game0, LobbyPlayer player0) {
        game = game0;
        player = player0;
    }

    private Player findAffectedPlayer() { 
        // try current priority
        Player currentPriority = game.getPhaseHandler().getPriorityPlayer();
        if( currentPriority.getLobbyPlayer() == player ) 
            return currentPriority;
        
        // otherwise find just any player, belonging to this lobbyplayer
        for(Player p : game.getPlayers())
            if(p.getLobbyPlayer() == player )
                return p;
        
        return null;
    }

    /** Concede game, bring up WinLose UI. */
    public void concede() {
        if (FOverlay.SINGLETON_INSTANCE.getPanel().isShowing()) {
            return;
        }
        
        final Player p = findAffectedPlayer();
        if( p == null ) return;
        // if( c.isMindSlaved() ) return

        game.getInputQueue().invokeGameAction(new Runnable() {
            @Override
            public void run() {
                p.concede();
                p.getGame().getAction().checkStateEffects();
            }
        });
        game = null; // no second entry possible;

    }

    /**
     * End turn.
     */
    public void endTurn() {
        Player p = findAffectedPlayer();

        if( p != null ) 
            p.getController().autoPassTo(PhaseType.CLEANUP);
    }

    private void revertLayout() {
        SOverlayUtils.genericOverlay();
        FView.SINGLETON_INSTANCE.getPnlContent().removeAll();

        FThreads.invokeInEdtLater(new Runnable(){
            @Override public void run() {
                SLayoutIO.loadLayout(null);
                SOverlayUtils.hideOverlay();
            }
        });

    }

    private void saveLayout() {
        final SaveOpenDialog dlgSave = new SaveOpenDialog();
        final File defFile = new File(SLayoutIO.getFilePreferred());
        final File saveFile = dlgSave.SaveDialog(defFile, Filetypes.LAYOUT);
        if (saveFile != null) {
            SLayoutIO.saveLayout(saveFile);
        }
    }

    private void openLayout() {
        SOverlayUtils.genericOverlay();

        final SaveOpenDialog dlgOpen = new SaveOpenDialog();
        final File defFile = new File(SLayoutIO.getFilePreferred());
        final File loadFile = dlgOpen.OpenDialog(defFile, Filetypes.LAYOUT);

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

    /**
     * View deck list.
     */
    private void viewDeckList() {
        
        showDeck(game.getMatch().getPlayers().get(0).getValue().getCurrentDeck());
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

        FModel.SINGLETON_INSTANCE.getPreferences()
            .setPref(FPref.UI_TARGETING_OVERLAY, String.valueOf(arcState));
        //FModel.SINGLETON_INSTANCE.getPreferences().save();
    }

    /** Attack with everyone. */
    public void alphaStrike() {
        final PhaseHandler ph = game.getPhaseHandler();

        Player p = findAffectedPlayer();
        if (ph.is(PhaseType.COMBAT_DECLARE_ATTACKERS, p)) { // ph.is(...) includes null check
            List<Player> defenders = p.getOpponents();
            
            for (Card c : CardLists.filter(p.getCardsIn(ZoneType.Battlefield), Presets.CREATURES)) {
                if (c.isAttacking())
                    continue;
                
                for(Player defender : defenders)
                    if( CombatUtil.canAttack(c, defender, game.getCombat())) {
                        game.getCombat().addAttacker(c, defender);
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

        for (final Entry<CardPrinted, Integer> s : targetDeck.getMain()) {
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
        } else {
            msg.append("Decklist too long for dialog." + nl + nl);
        }

        msg.append("Copy Decklist to Clipboard?");

        int rcMsg = JOptionPane.showConfirmDialog(null, msg, ttl, JOptionPane.OK_CANCEL_OPTION);

        if (rcMsg == JOptionPane.OK_OPTION) {
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
    @Override
    public void initialize() {
        final String temp = FModel.SINGLETON_INSTANCE.getPreferences()
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

        VDock.SINGLETON_INSTANCE.getBtnConcede()
        .addMouseListener(new MouseAdapter() { @Override public void mousePressed(final MouseEvent e) { concede(); } });

        VDock.SINGLETON_INSTANCE.getBtnSettings()
        .addMouseListener(new MouseAdapter() { @Override public void mousePressed(final MouseEvent e) { SOverlayUtils.showOverlay(); } });

        VDock.SINGLETON_INSTANCE.getBtnEndTurn()
        .addMouseListener(new MouseAdapter() { @Override public void mousePressed(final MouseEvent e) { endTurn(); } });

        VDock.SINGLETON_INSTANCE.getBtnViewDeckList()
        .addMouseListener(new MouseAdapter() { @Override public void mousePressed(final MouseEvent e) { viewDeckList(); } });

        VDock.SINGLETON_INSTANCE.getBtnRevertLayout()
        .addMouseListener(new MouseAdapter() { @Override public void mousePressed(final MouseEvent e) { revertLayout(); } });

        VDock.SINGLETON_INSTANCE.getBtnOpenLayout()
        .addMouseListener(new MouseAdapter() { @Override public void mousePressed(final MouseEvent e) { openLayout(); } });

        VDock.SINGLETON_INSTANCE.getBtnSaveLayout()
        .addMouseListener(new MouseAdapter() { @Override public void mousePressed(final MouseEvent e) { saveLayout(); } });

        VDock.SINGLETON_INSTANCE.getBtnAlphaStrike()
        .addMouseListener(new MouseAdapter() { @Override public void mousePressed(final MouseEvent e) { alphaStrike(); } });

        VDock.SINGLETON_INSTANCE.getBtnTargeting()
        .addMouseListener(new MouseAdapter() { @Override public void mousePressed(final MouseEvent e) { toggleTargeting(); } });
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
    }

}
