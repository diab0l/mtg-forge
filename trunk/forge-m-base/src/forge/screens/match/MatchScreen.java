package forge.screens.match;

import java.util.ArrayList;
import java.util.List;
import forge.screens.FScreen;
import forge.screens.match.views.VAvatar;
import forge.screens.match.views.VPlayerPanel;
import forge.screens.match.views.VPrompt;
import forge.screens.match.views.VStack;
import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinTexture;
import forge.assets.FSkinColor.Colors;
import forge.game.Match;
import forge.game.player.RegisteredPlayer;
import forge.game.zone.ZoneType;

public class MatchScreen extends FScreen {
    public static FSkinColor BORDER_COLOR = FSkinColor.get(Colors.CLR_BORDERS);

    private final Match match;
    private final List<VPlayerPanel> playerPanels;
    //private final VLog log;
    private final VStack stack;
    private final VPrompt prompt;

    private VPlayerPanel bottomPlayerPanel, topPlayerPanel;

    public MatchScreen(Match match0) {
        super(true, "Game", true);
        match = match0;

        playerPanels = new ArrayList<VPlayerPanel>();
        for (RegisteredPlayer player : match.getPlayers()) {
            playerPanels.add(add(new VPlayerPanel(player)));
        }
        bottomPlayerPanel = playerPanels.get(0);
        topPlayerPanel = playerPanels.get(1);
        topPlayerPanel.setFlipped(true);
        bottomPlayerPanel.setSelectedZone(ZoneType.Hand);

        //log = add(new VLog());
        stack = add(new VStack());
        prompt = add(new VPrompt());

        FControl.startGame(match0, this);
    }

    public VPrompt getPrompt() {
        return prompt;
    }

    public VStack getStack() {
        return stack;
    }

    public List<VPlayerPanel> getPlayerPanels() {
        return playerPanels;
    }

    @Override
    public void drawBackground(Graphics g) {
        super.drawBackground(g);
        float midField = topPlayerPanel.getBottom();
        float y = midField - topPlayerPanel.getField().getHeight();
        float w = getWidth();

        g.drawImage(FSkinTexture.BG_MATCH, 0, y, w, midField + bottomPlayerPanel.getField().getHeight() - y);

        //field separator lines
        if (topPlayerPanel.getSelectedZone() == null) {
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
        //determine player panel heights based on visibility of zone displays
        float topPlayerPanelHeight, bottomPlayerPanelHeight;
        float cardRowsHeight = height - startY - VPrompt.HEIGHT - 2 * VAvatar.HEIGHT;
        if (topPlayerPanel.getSelectedZone() == null) {
            if (bottomPlayerPanel.getSelectedZone() != null) {
                topPlayerPanelHeight = cardRowsHeight * 2f / 5f;
                bottomPlayerPanelHeight = cardRowsHeight * 3f / 5f;
            }
            else {
                topPlayerPanelHeight = cardRowsHeight / 2f;
                bottomPlayerPanelHeight = topPlayerPanelHeight;
            }
        }
        else if (bottomPlayerPanel.getSelectedZone() == null) {
            topPlayerPanelHeight = cardRowsHeight * 3f / 5f;
            bottomPlayerPanelHeight = cardRowsHeight * 2f / 5f;
        }
        else {
            topPlayerPanelHeight = cardRowsHeight / 2f;
            bottomPlayerPanelHeight = topPlayerPanelHeight;
        }
        topPlayerPanelHeight += VAvatar.HEIGHT;
        bottomPlayerPanelHeight += VAvatar.HEIGHT;

        //log.setBounds(0, startY, width - FScreen.HEADER_HEIGHT, VLog.HEIGHT);
        topPlayerPanel.setBounds(0, startY, width, topPlayerPanelHeight);
        stack.setBounds(0, startY + topPlayerPanelHeight - VStack.HEIGHT / 2, VStack.WIDTH, VStack.HEIGHT);
        bottomPlayerPanel.setBounds(0, height - VPrompt.HEIGHT - bottomPlayerPanelHeight, width, bottomPlayerPanelHeight);
        prompt.setBounds(0, height - VPrompt.HEIGHT, width, VPrompt.HEIGHT);
    }
}
