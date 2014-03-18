package forge.screens.match;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.model.FModel;
import forge.screens.FScreen;
import forge.screens.match.views.VAvatar;
import forge.screens.match.views.VHeader;
import forge.screens.match.views.VPlayerPanel;
import forge.screens.match.views.VPrompt;
import forge.toolbox.VCardZoom;
import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinTexture;
import forge.assets.FSkinColor.Colors;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class MatchScreen extends FScreen {
    public static FSkinColor BORDER_COLOR = FSkinColor.get(Colors.CLR_BORDERS);

    private final Map<Player, VPlayerPanel> playerPanels = new HashMap<Player, VPlayerPanel>();
    private final VHeader header;
    private final VPrompt prompt;
    private final VCardZoom cardZoom;

    private VPlayerPanel bottomPlayerPanel, topPlayerPanel;

    public MatchScreen(List<VPlayerPanel> playerPanels0) {
        super(false, null, false); //match screen has custom header

        for (VPlayerPanel playerPanel : playerPanels0) {
            playerPanels.put(playerPanel.getPlayer(), add(playerPanel));
        }
        bottomPlayerPanel = playerPanels0.get(0);
        topPlayerPanel = playerPanels0.get(1);
        topPlayerPanel.setFlipped(true);
        bottomPlayerPanel.setSelectedZone(ZoneType.Hand);

        header = add(new VHeader());

        prompt = add(new VPrompt("", "",
                new Runnable() {
                    @Override
                    public void run() {
                        FControl.getInputProxy().selectButtonOK();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        FControl.getInputProxy().selectButtonCancel();
                    }
                }));
        cardZoom = add(new VCardZoom());
    }

    public void updatePlayers() {
        header.getTabPlayers().update();
    }

    public void updateLog() {
        header.getTabLog().update();
    }

    public void updateCombat() {
        header.getTabCombat().update();
    }

    public void updateStack() {
        header.getTabStack().update();
    }

    public VPrompt getPrompt() {
        return prompt;
    }

    public VCardZoom getCardZoom() {
        return cardZoom;
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
    public boolean onClose(boolean canCancel) {
        FModel.getPreferences().writeMatchPreferences();
        FModel.getPreferences().save();
        return true;
    }

    @Override
    public void drawBackground(Graphics g) {
        super.drawBackground(g);
        float midField = topPlayerPanel.getBottom();
        float y = midField - topPlayerPanel.getField().getHeight();
        float w = getWidth();

        g.drawImage(FSkinTexture.BG_MATCH, 0, y, w, midField + bottomPlayerPanel.getField().getHeight() - y);

        //field separator lines
        if (topPlayerPanel.getSelectedTab() == null) {
            y++; //ensure border goes all the way across under avatar
        }
        g.drawLine(1, BORDER_COLOR, 0, y, w, y);
        y = midField;
        g.drawLine(1, BORDER_COLOR, 0, y, w, y);
        y += bottomPlayerPanel.getField().getHeight();
        g.drawLine(1, BORDER_COLOR, 0, y, w, y);
    }

    @Override
    protected void doLayout(float startY, float width, float height) {
        header.setBounds(0, 0, width, VHeader.HEIGHT);
        startY = VHeader.HEIGHT;

        //determine player panel heights based on visibility of zone displays
        float topPlayerPanelHeight, bottomPlayerPanelHeight;
        float cardRowsHeight = height - startY - VPrompt.HEIGHT - 2 * VAvatar.HEIGHT;
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

        topPlayerPanel.setBounds(0, startY, width, topPlayerPanelHeight);
        bottomPlayerPanel.setBounds(0, height - VPrompt.HEIGHT - bottomPlayerPanelHeight, width, bottomPlayerPanelHeight);
        prompt.setBounds(0, height - VPrompt.HEIGHT, width, VPrompt.HEIGHT);
        cardZoom.setBounds(0, 0, width, height);
    }
}
