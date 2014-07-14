package forge.screens.match;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.badlogic.gdx.Input.Keys;

import forge.Forge;
import forge.LobbyPlayer;
import forge.match.MatchUtil;
import forge.menu.FMenuBar;
import forge.model.FModel;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;
import forge.screens.FScreen;
import forge.screens.match.views.VAvatar;
import forge.screens.match.views.VDevMenu;
import forge.screens.match.views.VGameMenu;
import forge.screens.match.views.VLog;
import forge.screens.match.views.VManaPool;
import forge.screens.match.views.VPlayerPanel;
import forge.screens.match.views.VPlayerPanel.InfoTab;
import forge.screens.match.views.VPlayers;
import forge.screens.match.views.VPrompt;
import forge.screens.match.views.VStack;
import forge.sound.MusicPlaylist;
import forge.Forge.KeyInputAdapter;
import forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinTexture;
import forge.assets.FSkinColor.Colors;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.combat.Combat;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.toolbox.FEvent;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FScrollPane;
import forge.util.Callback;

public class MatchScreen extends FScreen {
    public static FSkinColor BORDER_COLOR = FSkinColor.get(Colors.CLR_BORDERS);

    private final Map<Player, VPlayerPanel> playerPanels = new HashMap<Player, VPlayerPanel>();
    private final VPrompt prompt;
    private final VLog log;
    private final VStack stack;
    private final VDevMenu devMenu;
    private final FieldScroller scroller;
    private VPlayerPanel bottomPlayerPanel, topPlayerPanel;

    public MatchScreen(Game game, LobbyPlayer localPlayer, List<VPlayerPanel> playerPanels0) {
        super(new FMenuBar());

        scroller = add(new FieldScroller());
        for (VPlayerPanel playerPanel : playerPanels0) {
            playerPanels.put(playerPanel.getPlayer(), scroller.add(playerPanel));
        }
        bottomPlayerPanel = playerPanels0.get(0);
        topPlayerPanel = playerPanels0.get(1);
        topPlayerPanel.setFlipped(true);
        bottomPlayerPanel.setSelectedZone(ZoneType.Hand);

        prompt = add(new VPrompt("", "",
                new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        FControl.getInputProxy().selectButtonOK();
                    }
                },
                new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        FControl.getInputProxy().selectButtonCancel();
                    }
                }));

        log = new VLog(game.getGameLog());
        log.setDropDownContainer(this);
        stack = new VStack(game.getStack(), localPlayer);
        stack.setDropDownContainer(this);
        devMenu = new VDevMenu();
        devMenu.setDropDownContainer(this);

        VPlayers players = new VPlayers();
        players.setDropDownContainer(this);

        FMenuBar menuBar = (FMenuBar)getHeader();
        menuBar.addTab("Game", new VGameMenu());
        menuBar.addTab("Players (" + playerPanels.size() + ")", players);
        menuBar.addTab("Log", log);
        menuBar.addTab("Dev", devMenu);
        menuBar.addTab("Stack (0)", stack);
    }

    @Override
    public void onActivate() {
        //update dev menu visibility here so returning from Settings screen allows update
        devMenu.getMenuTab().setVisible(ForgePreferences.DEV_MODE);
    }

    public VLog getLog() {
        return log;
    }

    public VStack getStack() {
        return stack;
    }

    public VPrompt getPrompt() {
        return prompt;
    }

    public VPlayerPanel getTopPlayerPanel() {
        return topPlayerPanel;
    }

    public VPlayerPanel getBottomPlayerPanel() {
        return bottomPlayerPanel;
    }

    public Map<Player, VPlayerPanel> getPlayerPanels() {
        return playerPanels;
    }

    @Override
    public void onClose(Callback<Boolean> canCloseCallback) {
        FControl.writeMatchPreferences();
        Forge.getSoundSystem().setBackgroundMusic(MusicPlaylist.MENUS);
        super.onClose(canCloseCallback);
    }

    @Override
    protected void doLayout(float startY, float width, float height) {
        scroller.setBounds(0, startY, width, height - VPrompt.HEIGHT - startY);
        prompt.setBounds(0, height - VPrompt.HEIGHT, width, VPrompt.HEIGHT);
    }

    @Override
    protected void drawOverlay(Graphics g) {
        //draw arrows for paired cards
        HashSet<Card> pairedCards = new HashSet<Card>();
        for (VPlayerPanel playerPanel : playerPanels.values()) {
            for (Card card : playerPanel.getField().getRow1().getOrderedCards()) {
                if (pairedCards.contains(card)) { continue; } //prevent arrows going both ways

                Card paired = card.getPairedWith();
                if (paired != null) {
                    TargetingOverlay.drawArrow(g, card, paired);
                }
            }
        }

        //draw arrows for combat
        final Combat combat = FControl.getGame().getCombat();
        if (combat != null) {
            //connect each attacker with planeswalker it's attacking if applicable
            for (Card planeswalker : combat.getDefendingPlaneswalkers()) {
                for (Card attacker : combat.getAttackersOf(planeswalker)) {
                    TargetingOverlay.drawArrow(g, attacker, planeswalker);
                }
            }

            //connect each blocker with the attacker it's blocking
            for (Card blocker : combat.getAllBlockers()) {
                for (Card attacker : combat.getAttackersBlockedBy(blocker)) {
                    TargetingOverlay.drawArrow(g, blocker, attacker);
                }
            }
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        switch (keyCode) {
        case Keys.ENTER:
        case Keys.SPACE:
            if (prompt.getBtnOk().trigger()) { //trigger OK on Enter or Space
                return true;
            }
            return prompt.getBtnCancel().trigger(); //trigger Cancel if can't trigger OK
        case Keys.ESCAPE:
            return prompt.getBtnCancel().trigger(); //otherwise trigger Cancel
        case Keys.BACK:
            return true; //suppress Back button so it's not bumped when trying to press OK or Cancel buttons
        case Keys.A: //alpha strike on Ctrl+A
            if (KeyInputAdapter.isCtrlKeyDown()) {
                FControl.alphaStrike();
                return true;
            }
            break;
        case Keys.E: //end turn on Ctrl+E
            if (KeyInputAdapter.isCtrlKeyDown()) {
                FControl.endCurrentTurn();
                return true;
            }
            break;
        case Keys.Q: //concede game on Ctrl+Q
            if (KeyInputAdapter.isCtrlKeyDown()) {
                FControl.concede();
                return true;
            }
            break;
        case Keys.Z: //undo on Ctrl+Z
            if (KeyInputAdapter.isCtrlKeyDown()) {
                MatchUtil.undoLastAction();
                return true;
            }
            break;
        }
        return super.keyDown(keyCode);
    }

    @Override
    public void showMenu() {
        //don't show menu from this screen since it's too easy to bump the menu button when trying to press OK or Cancel
    }

    private class FieldScroller extends FScrollPane {
        private float extraHeight = 0;

        @Override
        public void drawBackground(Graphics g) {
            super.drawBackground(g);
            float midField = topPlayerPanel.getBottom();
            float y = midField - topPlayerPanel.getField().getHeight();
            float w = getWidth();

            if (FModel.getPreferences().getPrefBoolean(FPref.UI_MATCH_IMAGE_VISIBLE)) {
                g.drawImage(FSkinTexture.BG_MATCH, 0, y, w, midField + bottomPlayerPanel.getField().getHeight() - y);
            }

            //field separator lines
            if (topPlayerPanel.getSelectedTab() == null) {
                y++; //ensure border goes all the way across under avatar
            }
            g.drawLine(1, BORDER_COLOR, 0, y, w, y);

            y = midField;
            g.drawLine(1, BORDER_COLOR, 0, y, w, y);

            y = midField + bottomPlayerPanel.getField().getHeight();
            g.drawLine(1, BORDER_COLOR, 0, y, w, y);
        }

        @Override
        protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
            float totalHeight = visibleHeight + extraHeight;

            //determine player panel heights based on visibility of zone displays
            float topPlayerPanelHeight, bottomPlayerPanelHeight;
            float cardRowsHeight = totalHeight - 2 * VAvatar.HEIGHT;
            if (topPlayerPanel.getSelectedTab() == null) {
                if (bottomPlayerPanel.getSelectedTab() != null) {
                    topPlayerPanelHeight = cardRowsHeight * 2f / 5f;
                    bottomPlayerPanelHeight = cardRowsHeight * 3f / 5f;
                }
                else {
                    topPlayerPanelHeight = cardRowsHeight / 2f;
                    bottomPlayerPanelHeight = topPlayerPanelHeight;
                }
            }
            else if (bottomPlayerPanel.getSelectedTab() == null) {
                topPlayerPanelHeight = cardRowsHeight * 3f / 5f;
                bottomPlayerPanelHeight = cardRowsHeight * 2f / 5f;
            }
            else {
                topPlayerPanelHeight = cardRowsHeight / 2f;
                bottomPlayerPanelHeight = topPlayerPanelHeight;
            }
            topPlayerPanelHeight += VAvatar.HEIGHT;
            bottomPlayerPanelHeight += VAvatar.HEIGHT;

            topPlayerPanel.setBounds(0, 0, visibleWidth, topPlayerPanelHeight);
            bottomPlayerPanel.setBounds(0, totalHeight - bottomPlayerPanelHeight, visibleWidth, bottomPlayerPanelHeight);
            return new ScrollBounds(visibleWidth, totalHeight);
        }

        @Override
        public boolean zoom(float x, float y, float amount) {
            //adjust position for current scroll positions
            float staticHeight = 2 * VAvatar.HEIGHT; //take out avatar rows that don't scale
            float oldScrollHeight = getScrollHeight() - staticHeight;
            float oldScrollTop = getScrollTop();
            y += oldScrollTop - VAvatar.HEIGHT;

            //build map of all horizontal scroll panes and their current scrollWidths and adjusted X values
            Map<FScrollPane, Pair<Float, Float>> horzScrollPanes = new HashMap<FScrollPane, Pair<Float, Float>>();
            backupHorzScrollPanes(topPlayerPanel, x, horzScrollPanes);
            backupHorzScrollPanes(bottomPlayerPanel, x, horzScrollPanes);

            float zoom = oldScrollHeight / (getHeight() - staticHeight);
            extraHeight += amount * zoom; //scale amount by current zoom
            if (extraHeight < 0) {
                extraHeight = 0;
            }
            revalidate(); //apply change in height to all scroll panes

            //adjust scroll top to keep y position the same
            float newScrollHeight = getScrollHeight() - staticHeight;
            float ratio = newScrollHeight / oldScrollHeight;
            float yAfter = y * ratio;
            setScrollTop(oldScrollTop + yAfter - y);

            //adjust scroll left of all horizontal scroll panes to keep x position the same
            float startX = x;
            for (Entry<FScrollPane, Pair<Float, Float>> entry : horzScrollPanes.entrySet()) {
                FScrollPane horzScrollPane = entry.getKey();
                float oldScrollLeft = entry.getValue().getLeft();
                x = startX + oldScrollLeft;
                float xAfter = x * ratio;
                horzScrollPane.setScrollLeft(oldScrollLeft + xAfter - x);
            }

            return true;
        }

        private void backupHorzScrollPanes(VPlayerPanel playerPanel, float x, Map<FScrollPane, Pair<Float, Float>> horzScrollPanes) {
            backupHorzScrollPane(playerPanel.getField().getRow1(), x, horzScrollPanes);
            backupHorzScrollPane(playerPanel.getField().getRow2(), x, horzScrollPanes);
            for (InfoTab tab : playerPanel.getTabs()) {
                if (tab.getDisplayArea() instanceof VManaPool) {
                    continue; //don't include Mana pool in this
                }
                backupHorzScrollPane(tab.getDisplayArea(), x, horzScrollPanes);
            }
        }
        private void backupHorzScrollPane(FScrollPane scrollPane, float x, Map<FScrollPane, Pair<Float, Float>> horzScrollPanes) {
            horzScrollPanes.put(scrollPane, Pair.of(scrollPane.getScrollLeft(), scrollPane.getScrollWidth()));
        }
    }
}
