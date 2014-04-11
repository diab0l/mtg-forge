package forge.screens.constructed;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.google.common.base.Predicate;

import forge.Forge;
import forge.Forge.Graphics;
import forge.assets.FSkin;
import forge.assets.FSkinColor;
import forge.assets.FSkinColor.Colors;
import forge.assets.FSkinImage;
import forge.assets.FTextureRegionImage;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckProxy;
import forge.deck.DeckSection;
import forge.deck.DeckType;
import forge.deck.DeckgenUtil;
import forge.deck.FDeckChooser;
import forge.game.GameType;
import forge.game.player.LobbyPlayer;
import forge.game.player.RegisteredPlayer;
import forge.item.PaperCard;
import forge.model.CardCollections;
import forge.model.FModel;
import forge.net.FServer;
import forge.net.Lobby;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;
import forge.screens.FScreen;
import forge.screens.LaunchScreen;
import forge.toolbox.FButton;
import forge.toolbox.FCheckBox;
import forge.toolbox.FComboBox;
import forge.toolbox.FContainer;
import forge.toolbox.FEvent;
import forge.toolbox.FList;
import forge.toolbox.FToggleSwitch;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FScrollPane;
import forge.toolbox.FTextField;
import forge.util.Aggregates;
import forge.util.Lang;
import forge.util.MyRandom;
import forge.util.NameGenerator;
import forge.util.Utils;
import forge.util.storage.IStorage;

public class ConstructedScreen extends LaunchScreen {
    private static final FSkinColor PLAYER_BORDER_COLOR = FSkinColor.get(Colors.CLR_BORDERS).alphaColor(0.8f);
    private static final ForgePreferences prefs = FModel.getPreferences();
    private static final float PADDING = 5;
    private static final int MAX_PLAYERS = 8;
    private static final int VARIANTS_FONT_SIZE = 12;

    // General variables
    private final FLabel lblPlayers = new FLabel.Builder().text("Players:").fontSize(VARIANTS_FONT_SIZE).build();
    private final FComboBox<Integer> cmbPlayerCount;
    private List<Integer> teams = new ArrayList<Integer>(MAX_PLAYERS);
    private List<Integer> archenemyTeams = new ArrayList<Integer>(MAX_PLAYERS);

    // Variants frame and variables
    private final FLabel lblVariants = new FLabel.Builder().text("Variants:").fontSize(VARIANTS_FONT_SIZE).build();
    private final FComboBox<Object> cmbVariants;
    private final Set<GameType> appliedVariants = new TreeSet<GameType>();

    private final List<PlayerPanel> playerPanels = new ArrayList<PlayerPanel>(MAX_PLAYERS);
    private final FScrollPane playersScroll = new FScrollPane() {
        @Override
        protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
            float y = 0;
            float height;
            for (int i = 0; i < getNumPlayers(); i++) {
                height = playerPanels.get(i).getPreferredHeight();
                playerPanels.get(i).setBounds(0, y, visibleWidth, height);
                y += height;
            }
            return new ScrollBounds(visibleWidth, y);
        }

        @Override
        protected void drawOverlay(Graphics g) {
            g.drawLine(1.5f, PLAYER_BORDER_COLOR, 0, 0, getWidth(), 0);
        }
    };

    // Variants
    private final List<PaperCard> vgdAllAvatars = new ArrayList<PaperCard>();
    private final List<PaperCard> vgdAllAiAvatars = new ArrayList<PaperCard>();
    private final List<PaperCard> nonRandomHumanAvatars = new ArrayList<PaperCard>();
    private final List<PaperCard> nonRandomAiAvatars = new ArrayList<PaperCard>();
    private int lastArchenemy = 0;
    private Vector<Object> humanListData = new Vector<Object>();
    private Vector<Object> aiListData = new Vector<Object>();

    public ConstructedScreen() {
        super("Constructed");

        add(lblPlayers);
        cmbPlayerCount = add(new FComboBox<Integer>());
        cmbPlayerCount.setFontSize(VARIANTS_FONT_SIZE);
        for (int i = 2; i <= MAX_PLAYERS; i++) {
            cmbPlayerCount.addItem(i);
        }
        cmbPlayerCount.setSelectedItem(2);
        cmbPlayerCount.setChangedHandler(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                int numPlayers = getNumPlayers();
                for (int i = 0; i < MAX_PLAYERS; i++) {
                    playerPanels.get(i).setVisible(i < numPlayers);
                }
                playersScroll.revalidate();
            }
        });

        add(lblVariants);
        cmbVariants = add(new FComboBox<Object>());
        cmbVariants.setFontSize(VARIANTS_FONT_SIZE);
        cmbVariants.addItem("(None)");
        cmbVariants.addItem(GameType.Vanguard);
        cmbVariants.addItem(GameType.Commander);
        cmbVariants.addItem(GameType.Planechase);
        cmbVariants.addItem(GameType.Archenemy);
        cmbVariants.addItem(GameType.ArchenemyRumble);
        cmbVariants.addItem("Advanced....");
        cmbVariants.setChangedHandler(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                if (cmbVariants.getSelectedIndex() <= 0) {
                    appliedVariants.clear();
                    playersScroll.revalidate();
                }
                else if (cmbVariants.getSelectedIndex() == cmbVariants.getItemCount() - 1) {
                    Forge.openScreen(new AdvancedVariants());
                }
                else {
                    appliedVariants.clear();
                    appliedVariants.add((GameType)cmbVariants.getSelectedItem());
                    playersScroll.revalidate();
                }
            }
        });

        // Construct individual player panels
        for (int i = 0; i < MAX_PLAYERS; i++) {
            teams.add(i + 1);
            archenemyTeams.add(i == 0 ? 1 : 2);

            PlayerPanel player = new PlayerPanel(i);
            playerPanels.add(player);

            // Populate players panel
            player.setVisible(i < getNumPlayers());

            playersScroll.add(player);
        }

        add(playersScroll);

        getDeckChooser(0).initialize(FPref.CONSTRUCTED_P1_DECK_STATE, DeckType.PRECONSTRUCTED_DECK);
        getDeckChooser(1).initialize(FPref.CONSTRUCTED_P2_DECK_STATE, DeckType.COLOR_DECK);
        getDeckChooser(2).initialize(FPref.CONSTRUCTED_P3_DECK_STATE, DeckType.COLOR_DECK);
        getDeckChooser(3).initialize(FPref.CONSTRUCTED_P4_DECK_STATE, DeckType.COLOR_DECK);
        getDeckChooser(4).initialize(FPref.CONSTRUCTED_P5_DECK_STATE, DeckType.COLOR_DECK);
        getDeckChooser(5).initialize(FPref.CONSTRUCTED_P6_DECK_STATE, DeckType.COLOR_DECK);
        getDeckChooser(6).initialize(FPref.CONSTRUCTED_P7_DECK_STATE, DeckType.COLOR_DECK);
        getDeckChooser(7).initialize(FPref.CONSTRUCTED_P8_DECK_STATE, DeckType.COLOR_DECK);

        updatePlayersFromPrefs();
    }

    @Override
    protected void doLayoutAboveBtnStart(float startY, float width, float height) {
        float x = PADDING;
        float y = startY + PADDING;
        float fieldHeight = cmbPlayerCount.getHeight();
        lblPlayers.setBounds(x, y, lblPlayers.getAutoSizeBounds().width + PADDING / 2, fieldHeight);
        x += lblPlayers.getWidth();
        cmbPlayerCount.setBounds(x, y, Utils.AVG_FINGER_WIDTH, fieldHeight);
        x += cmbPlayerCount.getWidth() + PADDING;
        lblVariants.setBounds(x, y, lblVariants.getAutoSizeBounds().width + PADDING / 2, fieldHeight);
        x += lblVariants.getWidth();
        cmbVariants.setBounds(x, y, width - x - PADDING, fieldHeight);

        y += cmbPlayerCount.getHeight() + PADDING;
        playersScroll.setBounds(0, y, width, height - y);
    }

    public final FDeckChooser getDeckChooser(int playernum) {
        return playerPanels.get(playernum).deckChooser;
    }

    public int getNumPlayers() {
        return cmbPlayerCount.getSelectedItem();
    }
    public void setNumPlayers(int numPlayers) {
        cmbPlayerCount.setSelectedItem(numPlayers);
    }

    @Override
    protected boolean buildLaunchParams(LaunchParams launchParams) {
        launchParams.gameType = GameType.Constructed;

        if (!isEnoughTeams()) {
            FOptionPane.showMessageDialog("There are not enough teams! Please adjust team allocations.");
            return false;
        }

        for (int i = 0; i < getNumPlayers(); i++) {
            if (getDeckChooser(i).getPlayer() == null) {
                FOptionPane.showMessageDialog("Please specify a deck for " + getPlayerName(i));
                return false;
            }
        } // Is it even possible anymore? I think current implementation assigns decks automatically.

        final List<GameType> variantTypes = new ArrayList<GameType>();
        variantTypes.addAll(appliedVariants);

        boolean checkLegality = FModel.getPreferences().getPrefBoolean(FPref.ENFORCE_DECK_LEGALITY);
        if (checkLegality && !variantTypes.contains(GameType.Commander)) { //Commander deck replaces regular deck and is checked later
            for (int i = 0; i < getNumPlayers(); i++) {
                String name = getPlayerName(i);
                String errMsg = GameType.Constructed.getDecksFormat().getDeckConformanceProblem(getDeckChooser(i).getPlayer().getDeck());
                if (errMsg != null) {
                    FOptionPane.showErrorDialog(name + "'s deck " + errMsg, "Invalid Deck");
                    return false;
                }
            }
        }

        Lobby lobby = FServer.getLobby();
        List<RegisteredPlayer> players = new ArrayList<RegisteredPlayer>();
        for (int i = 0; i < getNumPlayers(); i++) {
            PlayerPanel playerPanel = playerPanels.get(i);
            String name = getPlayerName(i);
            LobbyPlayer lobbyPlayer = playerPanel.isPlayerAI() ? lobby.getAiPlayer(name,
                    getPlayerAvatar(i)) : lobby.getGuiPlayer();
            RegisteredPlayer rp = playerPanel.deckChooser.getPlayer();

            if (variantTypes.isEmpty()) {
                rp.setTeamNumber(getTeam(i));
                players.add(rp.setPlayer(lobbyPlayer));
            }
            else {
                Deck deck = null;
                boolean isCommanderMatch = variantTypes.contains(GameType.Commander);
                if (isCommanderMatch) {
                    Object selected = playerPanel.commanderDeckList.getSelectedValue();
                    if (selected instanceof String) {
                        String sel = (String) selected;
                        IStorage<Deck> comDecks = FModel.getDecks().getCommander();
                        if (sel.equals("Random") && comDecks.size() > 0) {
                            deck = Aggregates.random(comDecks);                            
                        }
                    }
                    else {
                        deck = (Deck) selected;
                    }
                    if (deck == null) { //Can be null if player deselects the list selection or chose Generate
                        deck = DeckgenUtil.generateCommanderDeck(isPlayerAI(i));
                    }
                    if (checkLegality) {
                        String errMsg = GameType.Commander.getDecksFormat().getDeckConformanceProblem(deck);
                        if (errMsg != null) {
                            FOptionPane.showErrorDialog(name + "'s deck " + errMsg, "Invalid Commander Deck");
                            return false;
                        }
                    }
                }

                // Initialise variables for other variants
                deck = deck == null ? rp.getDeck() : deck;
                Iterable<PaperCard> schemes = null;
                boolean playerIsArchenemy = isPlayerArchenemy(i);
                Iterable<PaperCard> planes = null;
                PaperCard vanguardAvatar = null;

                //Archenemy
                if (variantTypes.contains(GameType.ArchenemyRumble)
                        || (variantTypes.contains(GameType.Archenemy) && playerIsArchenemy)) {
                    Object selected = playerPanel.schemeDeckList.getSelectedValue();
                    CardPool schemePool = null;
                    if (selected instanceof String) {
                        String sel = (String) selected;
                        if (sel.contains("Use deck's scheme section")) {
                            if (deck.has(DeckSection.Schemes)) {
                                schemePool = deck.get(DeckSection.Schemes);
                            }
                            else {
                                sel = "Random";
                            }
                        }
                        IStorage<Deck> sDecks = FModel.getDecks().getScheme();
                        if (sel.equals("Random") && sDecks.size() != 0) {
                            schemePool = Aggregates.random(sDecks).get(DeckSection.Schemes);                            
                        }
                    }
                    else {
                        schemePool = ((Deck) selected).get(DeckSection.Schemes);
                    }
                    if (schemePool == null) { //Can be null if player deselects the list selection or chose Generate
                        schemePool = DeckgenUtil.generateSchemeDeck();
                    }
                    if (checkLegality) {
                        String errMsg = GameType.Archenemy.getDecksFormat().getSchemeSectionConformanceProblem(schemePool);
                        if (errMsg != null) {
                            FOptionPane.showErrorDialog(name + "'s deck " + errMsg, "Invalid Scheme Deck");
                            return false;
                        }
                    }
                    schemes = schemePool.toFlatList();
                }

                //Planechase
                if (variantTypes.contains(GameType.Planechase)) {
                    Object selected = playerPanel.planarDeckList.getSelectedValue();
                    CardPool planePool = null;
                    if (selected instanceof String) {
                        String sel = (String) selected;
                        if (sel.contains("Use deck's planes section")) {
                            if (deck.has(DeckSection.Planes)) {
                                planePool = deck.get(DeckSection.Planes);
                            } else {
                                sel = "Random";
                            }
                        }
                        IStorage<Deck> pDecks = FModel.getDecks().getPlane();
                        if (sel.equals("Random") && pDecks.size() != 0) {
                            planePool = Aggregates.random(pDecks).get(DeckSection.Planes);                            
                        }
                    }
                    else {
                        planePool = ((Deck) selected).get(DeckSection.Planes);
                    }
                    if (planePool == null) { //Can be null if player deselects the list selection or chose Generate
                        planePool = DeckgenUtil.generatePlanarDeck();
                    }
                    if (checkLegality) {
                        String errMsg = GameType.Planechase.getDecksFormat().getPlaneSectionConformanceProblem(planePool);
                        if (null != errMsg) {
                            FOptionPane.showErrorDialog(name + "'s deck " + errMsg, "Invalid Planar Deck");
                            return false;
                        }
                    }
                    planes = planePool.toFlatList();
                }

                //Vanguard
                if (variantTypes.contains(GameType.Vanguard)) {
                    Object selected = playerPanel.vgdAvatarList.getSelectedValue();
                    if (selected instanceof String) {
                        String sel = (String) selected;
                        if (sel.contains("Use deck's default avatar") && deck.has(DeckSection.Avatar)) {
                            vanguardAvatar = deck.get(DeckSection.Avatar).get(0);
                        }
                        else { //Only other string is "Random"
                            if (!isPlayerAI(i)) { //Human
                                vanguardAvatar = Aggregates.random(getNonRandomHumanAvatars());
                            }
                            else { //AI
                                vanguardAvatar = Aggregates.random(getNonRandomAiAvatars());
                            }
                        }
                    }
                    else {
                        vanguardAvatar = (PaperCard)selected;
                    }
                    if (vanguardAvatar == null) {
                        FOptionPane.showErrorDialog("No Vanguard avatar selected for " + name
                                + ". Please choose one or disable the Vanguard variant");
                        return false;
                    }
                }

                rp = RegisteredPlayer.forVariants(variantTypes, deck, schemes, playerIsArchenemy, planes, vanguardAvatar);
                rp.setTeamNumber(getTeam(i));
                players.add(rp.setPlayer(lobbyPlayer));
            }
            getDeckChooser(i).saveState();
        }

        return true;
    }

    private class PlayerPanel extends FContainer {
        private final int index;

        private final FLabel nameRandomiser;
        private final FLabel avatarLabel = new FLabel.Builder().opaque(true).iconScaleFactor(0.99f).alphaComposite(1).iconInBackground(true).build();
        private int avatarIndex;

        private final FTextField txtPlayerName = new FTextField("Player name");
        private final FToggleSwitch humanAiSwitch = new FToggleSwitch("Human", "AI");

        private FComboBox<Object> teamComboBox = new FComboBox<Object>();
        private FComboBox<Object> aeTeamComboBox = new FComboBox<Object>();

        private final FLabel deckBtn = new FLabel.ButtonBuilder().text("Select a deck").build();
        private final FLabel deckLabel = newLabel("Deck:");

        private boolean playerIsArchenemy = false;
        private final FLabel scmDeckSelectorBtn = new FLabel.ButtonBuilder().text("Select a scheme deck").build();
        private final FLabel scmDeckEditor = new FLabel.ButtonBuilder().text("Scheme Deck Editor").build();
        private final FLabel scmLabel = newLabel("Scheme deck:");

        private final FLabel cmdDeckSelectorBtn = new FLabel.ButtonBuilder().text("Select a Commander deck").build();
        private final FLabel cmdDeckEditor = new FLabel.ButtonBuilder().text("Commander Deck Editor").build();
        private final FLabel cmdLabel = newLabel("Commander deck:");

        private final FLabel pchDeckSelectorBtn = new FLabel.ButtonBuilder().text("Select a planar deck").build();
        private final FLabel pchDeckEditor = new FLabel.ButtonBuilder().text("Planar Deck Editor").build();
        private final FLabel pchLabel = newLabel("Planar deck:");

        private final FLabel vgdSelectorBtn = new FLabel.ButtonBuilder().text("Select a Vanguard avatar").build();
        private final FLabel vgdLabel = newLabel("Vanguard:");

        private final FDeckChooser deckChooser;
        private final DeckList schemeDeckList, commanderDeckList, planarDeckList, vgdAvatarList;

        public PlayerPanel(final int index0) {
            super();
            index = index0;
            playerIsArchenemy = index == 0;
            deckChooser = new FDeckChooser(isPlayerAI());
            deckChooser.getLstDecks().setSelectionChangedHandler(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    deckBtn.setText(deckChooser.getSelectedDeckType().toString() + ": " +
                            Lang.joinHomogenous(deckChooser.getLstDecks().getSelectedItems(), DeckProxy.FN_GET_NAME));
                }
            });
            deckChooser.initialize();
            schemeDeckList = new DeckList();
            commanderDeckList = new DeckList();
            planarDeckList = new DeckList();
            vgdAvatarList = new DeckList();

            createAvatar();
            add(avatarLabel);

            createNameEditor();
            add(newLabel("Name:"));
            add(txtPlayerName);

            nameRandomiser = createNameRandomizer();
            add(nameRandomiser);

            humanAiSwitch.setToggled(index != 0);
            humanAiSwitch.setChangedHandler(humanAiSwitched);
            add(humanAiSwitch);

            add(newLabel("Team:"));
            populateTeamsComboBoxes();
            teamComboBox.setChangedHandler(teamChangedHandler);
            aeTeamComboBox.setChangedHandler(teamChangedHandler);
            add(teamComboBox);
            add(aeTeamComboBox);

            add(deckLabel);
            add(deckBtn);
            deckBtn.setCommand(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                }
            });

            add(cmdLabel);
            add(cmdDeckSelectorBtn);
            add(cmdDeckEditor);

            add(scmLabel);
            add(scmDeckSelectorBtn);
            add(scmDeckEditor);

            add(pchLabel);
            add(pchDeckSelectorBtn);
            add(pchDeckEditor);

            add(vgdLabel);
            add(vgdSelectorBtn);

            addHandlersToVariantsControls();
            updateVariantControlsVisibility();

            final CardCollections decks = FModel.getDecks();
            
            commanderDeckList.list.addItem("Generate");
            if (decks.getCommander().size() > 0) {
                commanderDeckList.list.addItem("Random");
                for (Deck comDeck : decks.getCommander()) {
                    commanderDeckList.list.addItem(comDeck);
                }
            }
            commanderDeckList.setSelectedIndex(0);

            schemeDeckList.list.addItem("Use deck's scheme section (random if unavailable)");
            schemeDeckList.list.addItem("Generate");
            if (decks.getScheme().size() > 0) {
                schemeDeckList.list.addItem("Random");
                for (Deck schemeDeck : decks.getScheme()) {
                    schemeDeckList.list.addItem(schemeDeck);
                }
            }
            schemeDeckList.setSelectedIndex(0);

            planarDeckList.list.addItem("Use deck's planes section (random if unavailable)");
            planarDeckList.list.addItem("Generate");
            if (decks.getPlane().size() > 0) {
                planarDeckList.list.addItem("Random");
                for (Deck planarDeck : decks.getPlane()) {
                    planarDeckList.list.addItem(planarDeck);
                }                
            }
            planarDeckList.setSelectedIndex(0);

            updateVanguardList();
        }

        @Override
        protected void doLayout(float width, float height) {
            float x = PADDING;
            float y = PADDING;
            float fieldHeight = txtPlayerName.getHeight();
            float avatarSize = 2 * fieldHeight + PADDING;

            avatarLabel.setBounds(x, y, avatarSize, avatarSize);
            x += avatarSize + PADDING;
            float w = width - x - fieldHeight - 2 * PADDING;
            txtPlayerName.setBounds(x, y, w, fieldHeight);
            x += w + PADDING;
            nameRandomiser.setBounds(x, y, fieldHeight, fieldHeight);

            y += fieldHeight + PADDING;
            humanAiSwitch.setSize(humanAiSwitch.getAutoSizeWidth(fieldHeight), fieldHeight);
            x = width - humanAiSwitch.getWidth() - PADDING;
            humanAiSwitch.setPosition(x, y);
            w = x - avatarSize - 3 * PADDING;
            x = avatarSize + 2 * PADDING;
            teamComboBox.setBounds(x, y, w, fieldHeight);
            aeTeamComboBox.setBounds(x, y, width, fieldHeight);

            y += fieldHeight + PADDING;
            x = PADDING;
            deckLabel.setBounds(x, y, avatarSize, fieldHeight);
            x += avatarSize + PADDING;
            w = width - x - PADDING;
            deckBtn.setBounds(x, y, w, fieldHeight);
        }

        private float getPreferredHeight() {
            int rows = 3;
            if (!appliedVariants.isEmpty()) {
                if (appliedVariants.contains(GameType.Archenemy)) {
                    rows++;
                }
                else if (appliedVariants.contains(GameType.ArchenemyRumble)) {
                    rows++;
                }
                if (appliedVariants.contains(GameType.Planechase)) {
                    rows++;
                }
                if (appliedVariants.contains(GameType.Vanguard)) {
                    rows++;
                }
            }
            return rows * (txtPlayerName.getHeight() + PADDING) + PADDING;
        }

        @Override
        protected void drawOverlay(Graphics g) {
            float y = getHeight();
            g.drawLine(1, PLAYER_BORDER_COLOR, 0, y, getWidth(), y);
        }

        private final FEventHandler humanAiSwitched = new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                updateVanguardList();
            }
        };

        //Listens to name text fields and gives the appropriate player focus.
        //Also saves the name preference when leaving player one's text field. */
        private FEventHandler nameChangedHandler = new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                final Object source = e.getSource();
                if (source instanceof FTextField) { // the text box
                    FTextField nField = (FTextField)source;
                    String newName = nField.getText().trim();
                    if (index == 0 && !StringUtils.isBlank(newName)
                            && StringUtils.isAlphanumericSpace(newName) && prefs.getPref(FPref.PLAYER_NAME) != newName) {
                        prefs.setPref(FPref.PLAYER_NAME, newName);
                        prefs.save();
                    }
                }
            }
        };

        private FEventHandler avatarCommand = new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                setRandomAvatar();

                //TODO: Support selecting avatar with option at top or bottom to select a random avatar
                
                /*final FLabel avatar = (FLabel)e.getSource();

                final AvatarSelector aSel = new AvatarSelector(getPlayerName(), avatarIndex, getUsedAvatars());
                for (final FLabel lbl : aSel.getSelectables()) {
                    lbl.setCommand(new FEventHandler() {
                        @Override
                        public void handleEvent(FEvent e) {
                            setAvatar(Integer.valueOf(lbl.getName().substring(11)));
                            aSel.setVisible(false);
                        }
                    });
                }
                
                aSel.setVisible(true);
                aSel.dispose();*/

                if (index < 2) {
                    updateAvatarPrefs();
                }
            }
        };

        public void updateVariantControlsVisibility() {
            // Commander deck replaces basic deck, so hide that
            deckLabel.setVisible(!appliedVariants.contains(GameType.Commander));
            deckBtn.setVisible(!appliedVariants.contains(GameType.Commander));
            cmdDeckSelectorBtn.setVisible(appliedVariants.contains(GameType.Commander));
            cmdDeckEditor.setVisible(appliedVariants.contains(GameType.Commander));
            cmdLabel.setVisible(appliedVariants.contains(GameType.Commander));

            boolean archenemyVisiblity = appliedVariants.contains(GameType.ArchenemyRumble)
                    || (appliedVariants.contains(GameType.Archenemy) && playerIsArchenemy);
            scmDeckSelectorBtn.setVisible(archenemyVisiblity);
            scmDeckEditor.setVisible(archenemyVisiblity);
            scmLabel.setVisible(archenemyVisiblity);

            teamComboBox.setVisible(!appliedVariants.contains(GameType.Archenemy));
            aeTeamComboBox.setVisible(appliedVariants.contains(GameType.Archenemy));
            aeTeamComboBox.setEnabled(!(appliedVariants.contains(GameType.Archenemy) && playerIsArchenemy));

            pchDeckSelectorBtn.setVisible(appliedVariants.contains(GameType.Planechase));
            pchDeckEditor.setVisible(appliedVariants.contains(GameType.Planechase));
            pchLabel.setVisible(appliedVariants.contains(GameType.Planechase));

            vgdSelectorBtn.setVisible(appliedVariants.contains(GameType.Vanguard));
            vgdLabel.setVisible(appliedVariants.contains(GameType.Vanguard));
        }

        public boolean isPlayerAI() {
            return humanAiSwitch.isToggled();
        }

        public void setVanguardButtonText(String text) {
            vgdSelectorBtn.setText(text);
        }

        private void populateTeamsComboBoxes() {
            aeTeamComboBox.addItem("Archenemy");
            aeTeamComboBox.addItem("Heroes");
            aeTeamComboBox.setSelectedIndex(archenemyTeams.get(index) - 1);
            aeTeamComboBox.setEnabled(playerIsArchenemy);

            for (int i = 1; i <= MAX_PLAYERS; i++) {
                teamComboBox.addItem("Team " + i);
            }
            teamComboBox.setSelectedIndex(teams.get(index) - 1);
            teamComboBox.setEnabled(true);
        }

        private FEventHandler teamChangedHandler = new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                @SuppressWarnings("unchecked")
                FComboBox<Object> cb = (FComboBox<Object>)e.getSource();
                if (cb.getSelectedIndex() == -1) {
                    return;
                }
                if (appliedVariants.contains(GameType.Archenemy)) {
                    String sel = (String) cb.getSelectedItem();
                    if (sel.contains("Archenemy")) {
                        lastArchenemy = index;
                        for (PlayerPanel pp : playerPanels) {
                            int i = pp.index;
                            archenemyTeams.set(i, i == lastArchenemy ? 1 : 2);
                            pp.aeTeamComboBox.setSelectedIndex(i == lastArchenemy ? 0 : 1);
                            pp.toggleIsPlayerArchenemy();
                        }
                    }
                }
                else {
                    teams.set(index, cb.getSelectedIndex() + 1);
                }
            }
        };

        public void toggleIsPlayerArchenemy() {
            if (appliedVariants.contains(GameType.Archenemy)) {
                playerIsArchenemy = lastArchenemy == index;
            }
            else {
                playerIsArchenemy = appliedVariants.contains(GameType.ArchenemyRumble);
            }
            updateVariantControlsVisibility();
        }

        private void addHandlersToVariantsControls() {
            // Archenemy buttons
            scmDeckSelectorBtn.setCommand(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                }
            });

            scmDeckEditor.setCommand(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    Predicate<PaperCard> predSchemes = new Predicate<PaperCard>() {
                        @Override
                        public boolean apply(PaperCard arg0) {
                            return arg0.getRules().getType().isScheme();
                        }
                    };

                    /*Forge.setCurrentScreen(FScreen.DECK_EDITOR_ARCHENEMY);
                    CDeckEditorUI.SINGLETON_INSTANCE.setEditorController(
                            new CEditorVariant(FModel.getDecks().getScheme(), predSchemes, DeckSection.Schemes, FScreen.DECK_EDITOR_PLANECHASE));*/
                }
            });

            // Commander buttons
            cmdDeckSelectorBtn.setCommand(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                }
            });

            cmdDeckEditor.setCommand(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    //Forge.setCurrentScreen(FScreen.DECK_EDITOR_COMMANDER);
                }
            });

            // Planechase buttons
            pchDeckSelectorBtn.setCommand(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                }
            });

            pchDeckEditor.setCommand(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    /*Predicate<PaperCard> predPlanes = new Predicate<PaperCard>() {
                        @Override
                        public boolean apply(PaperCard arg0) {
                            return arg0.getRules().getType().isPlane() || arg0.getRules().getType().isPhenomenon();
                        }
                    };

                    Forge.setCurrentScreen(FScreen.DECK_EDITOR_PLANECHASE);
                    CDeckEditorUI.SINGLETON_INSTANCE.setEditorController(
                            new CEditorVariant(FModel.getDecks().getPlane(), predPlanes, DeckSection.Planes, FScreen.DECK_EDITOR_PLANECHASE));*/
                }
            });

            // Vanguard buttons
            vgdSelectorBtn.setCommand(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                }
            });
        }

        private FLabel createNameRandomizer() {
            final FLabel newNameBtn = new FLabel.Builder().iconInBackground(false)
                    .icon(FSkinImage.EDIT).opaque(false).build();
            newNameBtn.setCommand(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    String newName = getNewName();
                    if (null == newName) {
                        return;
                    }
                    txtPlayerName.setText(newName);

                    if (index == 0) {
                        prefs.setPref(FPref.PLAYER_NAME, newName);
                        prefs.save();
                    }
                }
            });
            return newNameBtn;
        }

        private void createNameEditor() {
            String name;
            if (index == 0) {
                name = FModel.getPreferences().getPref(FPref.PLAYER_NAME);
                if (name.isEmpty()) {
                    name = "Human";
                }
            }
            else {
                name = NameGenerator.getRandomName("Any", "Any", getPlayerNames());
            }

            txtPlayerName.setText(name);
            txtPlayerName.setFontSize(14);
            txtPlayerName.setChangedHandler(nameChangedHandler);
        }

        private void createAvatar() {
            String[] currentPrefs = prefs.getPref(FPref.UI_AVATARS).split(",");
            if (index < currentPrefs.length) {
                avatarIndex = Integer.parseInt(currentPrefs[index]);
                avatarLabel.setIcon(new FTextureRegionImage(FSkin.getAvatars().get(avatarIndex)));
            }
            else {
                setRandomAvatar();
            }

            avatarLabel.setCommand(avatarCommand);
        }

        //Applies a random avatar, avoiding avatars already used.
        public void setRandomAvatar() {
            int random = 0;

            List<Integer> usedAvatars = getUsedAvatars();
            do {
                random = MyRandom.getRandom().nextInt(FSkin.getAvatars().size());
            } while (usedAvatars.contains(random));
            setAvatar(random);
        }

        public void setAvatar(int newAvatarIndex) {
            avatarIndex = newAvatarIndex;
            avatarLabel.setIcon(new FTextureRegionImage(FSkin.getAvatars().get(newAvatarIndex)));
        }

        public int getAvatarIndex() {
            return avatarIndex;
        }

        public void setPlayerName(String string) {
            txtPlayerName.setText(string);
        }

        public String getPlayerName() {
            return txtPlayerName.getText();
        }

        private void changeDeck(final GameType forGameType) {
            switch (forGameType) {
            case Constructed:
                Forge.openScreen(deckChooser);
                break;
            case Archenemy:
            case ArchenemyRumble:
                if (playerIsArchenemy) {
                    Forge.openScreen(schemeDeckList);
                }
                else {
                    Forge.openScreen(deckChooser);
                }
                break;
            case Commander:
                Forge.openScreen(commanderDeckList);
                break;
            case Planechase:
                Forge.openScreen(planarDeckList);
                break;
            case Vanguard:
                updateVanguardList();
                Forge.openScreen(vgdAvatarList);
                break;
            default:
                break;
            }
        }

        /** update vanguard list. */
        public void updateVanguardList() {
            Object lastSelection = vgdAvatarList.getSelectedValue();
            vgdAvatarList.setSelectedIndex(-1);
            vgdAvatarList.list.setListData(isPlayerAI() ? aiListData : humanListData);
            if (lastSelection != null) {
                vgdAvatarList.setSelectedValue(lastSelection);
            }
            if (vgdAvatarList.getSelectedIndex() == -1) {
                vgdAvatarList.setSelectedIndex(0);
            }
        }

        private class DeckList extends FScreen {
            private final FList<Object> list;
            private int selectedIndex;

            private DeckList() {
                super(true, "", false);
                list = new FList<Object>();
            }

            public int getSelectedIndex() {
                return selectedIndex;
            }

            public void setSelectedIndex(int index) {
                selectedIndex = index;
            }

            public Object getSelectedValue() {
                return list.getItemAt(selectedIndex);
            }

            public void setSelectedValue(Object value) {
                selectedIndex = list.getIndexOf(value);
            }

            @Override
            protected void doLayout(float startY, float width, float height) {
                list.setBounds(0, startY, width, height - startY);
            }
        }
    }

    /** Saves avatar prefs for players one and two. */
    private void updateAvatarPrefs() {
        int pOneIndex = playerPanels.get(0).getAvatarIndex();
        int pTwoIndex = playerPanels.get(1).getAvatarIndex();

        prefs.setPref(FPref.UI_AVATARS, pOneIndex + "," + pTwoIndex);
        prefs.save();
    }

    /** Updates the avatars from preferences on update. */
    public void updatePlayersFromPrefs() {
        ForgePreferences prefs = FModel.getPreferences();

        // Avatar
        String[] avatarPrefs = prefs.getPref(FPref.UI_AVATARS).split(",");
        for (int i = 0; i < avatarPrefs.length; i++) {
            int avatarIndex = Integer.parseInt(avatarPrefs[i]);
            playerPanels.get(i).setAvatar(avatarIndex);
        }

        // Name
        String prefName = prefs.getPref(FPref.PLAYER_NAME);
        playerPanels.get(0).setPlayerName(StringUtils.isBlank(prefName) ? "Human" : prefName);
    }

    /** Adds a pre-styled FLabel component with the specified title. */
    private FLabel newLabel(String title) {
        return new FLabel.Builder().text(title).fontSize(14).align(HAlignment.RIGHT).build();
    }

    private List<Integer> getUsedAvatars() {
        List<Integer> usedAvatars = Arrays.asList(-1,-1,-1,-1,-1,-1,-1,-1);
        int i = 0;
        for (PlayerPanel pp : playerPanels) {
            usedAvatars.set(i++, pp.avatarIndex);
        }
        return usedAvatars;
    }

    private final String getNewName() {
        final String title = "Get new random name";
        final String message = "What type of name do you want to generate?";
        final FSkinImage icon = FOptionPane.QUESTION_ICON;
        final String[] genderOptions = new String[]{ "Male", "Female", "Any" };
        final String[] typeOptions = new String[]{ "Fantasy", "Generic", "Any" };

        final int genderIndex = FOptionPane.showOptionDialog(message, title, icon, genderOptions, 2);
        if (genderIndex < 0) {
            return null;
        }
        final int typeIndex = FOptionPane.showOptionDialog(message, title, icon, typeOptions, 2);
        if (typeIndex < 0) {
            return null;
        }

        final String gender = genderOptions[genderIndex];
        final String type = typeOptions[typeIndex];

        String confirmMsg, newName;
        List<String> usedNames = getPlayerNames();
        do {
            newName = NameGenerator.getRandomName(gender, type, usedNames);
            confirmMsg = "Would you like to use the name \"" + newName + "\", or try again?";
        } while (!FOptionPane.showConfirmDialog(confirmMsg, title, "Use this name", "Try again", true));

        return newName;
    }

    private List<String> getPlayerNames() {
        List<String> names = new ArrayList<String>();
        for (PlayerPanel pp : playerPanels) {
            names.add(pp.getPlayerName());
        }
        return names;
    }

    public String getPlayerName(int i) {
        return playerPanels.get(i).getPlayerName();
    }

    public int getPlayerAvatar(int i) {
        return playerPanels.get(i).getAvatarIndex();
    }

    public boolean isEnoughTeams() {
        int lastTeam = -1;
        final List<Integer> teamList = appliedVariants.contains(GameType.Archenemy) ? archenemyTeams : teams;

        for (int i = 0; i < getNumPlayers(); i++) {
            if (lastTeam == -1) {
                lastTeam = teamList.get(i);
            }
            else if (lastTeam != teamList.get(i)) {
                return true;
            }
        }
        return false;
    }

    /////////////////////////////////////////////
    //========== Various listeners in build order
    
    private class AdvancedVariants extends FScreen {
        private final FCheckBox vntVanguard = add(new FCheckBox("Vanguard"));
        private final FCheckBox vntCommander = add(new FCheckBox("Commander"));
        private final FCheckBox vntPlanechase = add(new FCheckBox("Planechase"));
        private final FCheckBox vntArchenemy = add(new FCheckBox("Archenemy"));
        private final FCheckBox vntArchenemyRumble = add(new FCheckBox("Archenemy Rumble"));

        private FButton btnApply = add(new FButton("Apply Variants", new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                appliedVariants.clear();
                if (vntVanguard.isSelected()) {
                    appliedVariants.add(GameType.Vanguard);
                }
                if (vntCommander.isSelected()) {
                    appliedVariants.add(GameType.Commander);
                }
                if (vntPlanechase.isSelected()) {
                    appliedVariants.add(GameType.Planechase);
                }
                if (vntArchenemy.isSelected()) {
                    appliedVariants.add(GameType.Archenemy);
                }
                else if (vntArchenemyRumble.isSelected()) {
                    appliedVariants.add(GameType.ArchenemyRumble);
                }
                Forge.back();
                playersScroll.revalidate();
            }
        }));

        private AdvancedVariants() {
            super(true, "Variants - Advanced", false);
            vntVanguard.setSelected(appliedVariants.contains(GameType.Vanguard));
            vntCommander.setSelected(appliedVariants.contains(GameType.Vanguard));
            vntPlanechase.setSelected(appliedVariants.contains(GameType.Vanguard));
            vntArchenemy.setSelected(appliedVariants.contains(GameType.Archenemy));
            vntArchenemyRumble.setSelected(appliedVariants.contains(GameType.ArchenemyRumble));

            //prevent selecting both archenemy types
            vntArchenemy.setCommand(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    if (vntArchenemy.isSelected()) {
                        vntArchenemyRumble.setSelected(false);
                    }
                }
            });
            vntArchenemyRumble.setCommand(new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    if (vntArchenemyRumble.isSelected()) {
                        vntArchenemy.setSelected(false);
                    }
                }
            });
        }

        @Override
        protected void doLayout(float startY, float width, float height) {
            float x = PADDING;
            float y = startY + PADDING;
            width -= 2 * x;

            float checkBoxHeight = vntVanguard.getAutoSizeBounds().height * 1.5f;
            vntVanguard.setBounds(x, y, width, checkBoxHeight);
            y += checkBoxHeight + PADDING;
            vntCommander.setBounds(x, y, width, checkBoxHeight);
            y += checkBoxHeight + PADDING;
            vntPlanechase.setBounds(x, y, width, checkBoxHeight);
            y += checkBoxHeight + PADDING;
            vntArchenemy.setBounds(x, y, width, checkBoxHeight);
            y += checkBoxHeight + PADDING;
            vntArchenemyRumble.setBounds(x, y, width, checkBoxHeight);

            float buttonHeight = Utils.AVG_FINGER_HEIGHT;
            btnApply.setBounds(x, height - buttonHeight - PADDING, width, buttonHeight);
        }
    }
    

    /** This listener unlocks the relevant buttons for players
     * and enables/disables archenemy combobox as appropriate. */
    /*private ItemListener iListenerVariants = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent arg0) {
            FCheckBox cb = (FCheckBox) arg0.getSource();
            GameType variantType = null;

            if (cb == vntVanguard) {
                variantType = GameType.Vanguard;
            }
            else if (cb == vntCommander) {
                variantType = GameType.Commander;
            }
            else if (cb == vntPlanechase) {
                variantType = GameType.Planechase;
            }
            else if (cb == vntArchenemy) {
                variantType = archenemyType.contains("Classic") ? GameType.Archenemy : GameType.ArchenemyRumble;
                comboArchenemy.setEnabled(vntArchenemy.isSelected());
                if (arg0.getStateChange() != ItemEvent.SELECTED) {
                    appliedVariants.remove(GameType.Archenemy);
                    appliedVariants.remove(GameType.ArchenemyRumble);
                }
            }

            if (null != variantType) {
                if (arg0.getStateChange() == ItemEvent.SELECTED) {
                    appliedVariants.add(variantType);
                    currentGameMode = variantType;
                }
                else {
                    appliedVariants.remove(variantType);
                    if (currentGameMode == variantType) {
                        currentGameMode = GameType.Constructed;
                    }
                }
            }

            for (PlayerPanel pp : playerPanels) {
                pp.toggleIsPlayerArchenemy();
                pp.updateVariantControlsVisibility();
            }
        }
    };

    // Listens to the archenemy combo box
    private ActionListener aeComboListener = new ActionListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void actionPerformed(ActionEvent e) {
            FComboBox<String> cb = (FComboBox<String>)e.getSource();
            archenemyType = (String)cb.getSelectedItem();
            GameType mode = archenemyType.contains("Classic") ? GameType.Archenemy : GameType.ArchenemyRumble;
            appliedVariants.remove(GameType.Archenemy);
            appliedVariants.remove(GameType.ArchenemyRumble);
            appliedVariants.add(mode);

            currentGameMode = mode;
            for (PlayerPanel pp : playerPanels) {
                pp.toggleIsPlayerArchenemy();
                pp.updateVariantControlsVisibility();
            }
        }
    };

    //This listener will look for a vanguard avatar being selected in the lists
    //and update the corresponding detail panel.
    private ListSelectionListener vgdLSListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int index = vgdAvatarLists.indexOf(e.getSource());
            Object obj = vgdAvatarLists.get(index).getSelectedValue();
            PlayerPanel pp = playerPanels.get(index);
            CardDetailPanel cdp = vgdAvatarDetails.get(index);

            if (obj instanceof PaperCard) {
                pp.setVanguardButtonText(((PaperCard) obj).getName());
                cdp.setCard(Card.getCardForUi((PaperCard) obj));
                cdp.setVisible(true);
                refreshPanels(false, true);
            }
            else {
                pp.setVanguardButtonText((String) obj);
                cdp.setVisible(false);
            }
        }
    };*/

    /////////////////////////////////////
    //========== METHODS FOR VARIANTS

    public Set<GameType> getAppliedVariants() {
        return appliedVariants;
    }

    public int getTeam(final int playerIndex) {
        return appliedVariants.contains(GameType.Archenemy) ? archenemyTeams.get(playerIndex) : teams.get(playerIndex);
    }

    public boolean isPlayerAI(final int playernum) {
        return playerPanels.get(playernum).isPlayerAI();
    }

    public boolean isPlayerArchenemy(final int playernum) {
        return playerPanels.get(playernum).playerIsArchenemy;
    }

    /** Return all the Vanguard avatars. */
    public Iterable<PaperCard> getAllAvatars() {
        if (vgdAllAvatars.isEmpty()) {
            for (PaperCard c : FModel.getMagicDb().getVariantCards().getAllCards()) {
                if (c.getRules().getType().isVanguard()) {
                    vgdAllAvatars.add(c);
                }
            }
        }
        return vgdAllAvatars;
    }

    /** Return the Vanguard avatars not flagged RemAIDeck. */
    public List<PaperCard> getAllAiAvatars() {
        return vgdAllAiAvatars;
    }

    /** Return the Vanguard avatars not flagged RemRandomDeck. */
    public List<PaperCard> getNonRandomHumanAvatars() {
        return nonRandomHumanAvatars;
    }

    /** Return the Vanguard avatars not flagged RemAIDeck or RemRandomDeck. */
    public List<PaperCard> getNonRandomAiAvatars() {
        return nonRandomAiAvatars;
    }

    /** Populate vanguard lists. */
    private void populateVanguardLists() {
        humanListData.add("Use deck's default avatar (random if unavailable)");
        humanListData.add("Random");
        aiListData.add("Use deck's default avatar (random if unavailable)");
        aiListData.add("Random");
        for (PaperCard cp : getAllAvatars()) {
            humanListData.add(cp);
            if (!cp.getRules().getAiHints().getRemRandomDecks()) {
                nonRandomHumanAvatars.add(cp);
            }
            if (!cp.getRules().getAiHints().getRemAIDecks()) {
                aiListData.add(cp);
                vgdAllAiAvatars.add(cp);
                if (!cp.getRules().getAiHints().getRemRandomDecks()) {
                    nonRandomAiAvatars.add(cp);
                }
            }
        }
    }
}
