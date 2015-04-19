package forge.deck;

import forge.FThreads;
import forge.Forge;
import forge.deck.Deck;
import forge.deck.FDeckEditor.EditorType;
import forge.deck.io.DeckPreferences;
import forge.error.BugReporter;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.gauntlet.GauntletData;
import forge.gauntlet.GauntletUtil;
import forge.itemmanager.DeckManager;
import forge.itemmanager.ItemManagerConfig;
import forge.model.FModel;
import forge.player.GamePlayerUtil;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;
import forge.quest.QuestController;
import forge.quest.QuestEvent;
import forge.quest.QuestEventChallenge;
import forge.quest.QuestUtil;
import forge.screens.FScreen;
import forge.screens.LoadingOverlay;
import forge.toolbox.FButton;
import forge.toolbox.FComboBox;
import forge.toolbox.FEvent;
import forge.toolbox.GuiChoose;
import forge.toolbox.ListChooser;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FOptionPane;
import forge.util.Callback;
import forge.util.Utils;
import forge.util.storage.IStorage;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FDeckChooser extends FScreen {
    public static final float PADDING = Utils.scale(5);

    private FComboBox<DeckType> cmbDeckTypes;
    private DeckType selectedDeckType;
    private boolean needRefreshOnActivate;
    private Callback<Deck> callback;
    private NetDeckCategory netDeckCategory;
    private boolean refreshingDeckType;

    private final DeckManager lstDecks;
    private final FButton btnNewDeck = new FButton("New Deck");
    private final FButton btnEditDeck = new FButton("Edit Deck");
    private final FButton btnViewDeck = new FButton("View Deck");
    private final FButton btnRandom = new FButton("Random Deck");

    private RegisteredPlayer player;
    private boolean isAi;

    private final ForgePreferences prefs = FModel.getPreferences();
    private FPref stateSetting = null;

    //Show screen to select a deck
    private static FDeckChooser deckChooserForPrompt;
    public static void promptForDeck(String title, GameType gameType, boolean forAi, final Callback<Deck> callback) {
        FThreads.assertExecutedByEdt(true);
        if (deckChooserForPrompt == null) {
            deckChooserForPrompt = new FDeckChooser(gameType, forAi, null);
        }
        else { //reuse same deck chooser
            deckChooserForPrompt.setIsAi(forAi);
        }
        deckChooserForPrompt.setHeaderCaption(title);
        deckChooserForPrompt.callback = callback;
        Forge.openScreen(deckChooserForPrompt);
    }

    public FDeckChooser(GameType gameType0, boolean isAi0, FEventHandler selectionChangedHandler) {
        super("");
        lstDecks = new DeckManager(gameType0);
        isAi = isAi0;

        lstDecks.setItemActivateHandler(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                if (lstDecks.getGameType() == GameType.DeckEditorTest) {
                    //for Deck Editor, edit deck instead of accepting
                    editSelectedDeck();
                    return;
                }
                accept();
            }
        });
        btnNewDeck.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                EditorType editorType;
                switch (lstDecks.getGameType()) {
                case Commander:
                    editorType = EditorType.Commander;
                    break;
                case Archenemy:
                    editorType = EditorType.Archenemy;
                    break;
                case Planechase:
                    editorType = EditorType.Planechase;
                    break;
                default:
                    editorType = EditorType.Constructed;
                    break;
                }
                FDeckEditor editor;
                switch (selectedDeckType) {
                case COLOR_DECK:
                case THEME_DECK:
                case RANDOM_DECK:
                    final DeckProxy deck = lstDecks.getSelectedItem();
                    if (deck != null) {
                        Deck generatedDeck = deck.getDeck();
                        if (generatedDeck == null) { return; }

                        generatedDeck = (Deck)generatedDeck.copyTo(""); //prevent deck having a name by default
                        editor = new FDeckEditor(editorType, generatedDeck, true);
                    }
                    else {
                        FOptionPane.showErrorDialog("You must select something before you can generate a new deck.");
                        return;
                    }
                    break;
                default:
                    editor = new FDeckEditor(editorType, "", false);
                    break;
                }
                editor.setSaveHandler(new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        //ensure user returns to custom user deck and that list is refreshed if new deck is saved
                        if (!needRefreshOnActivate) {
                            needRefreshOnActivate = true;
                            setSelectedDeckType(DeckType.CUSTOM_DECK);
                        }
                    }
                });
                Forge.openScreen(editor);
            }
        });
        btnEditDeck.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                editSelectedDeck();
            }
        });
        btnViewDeck.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                if (selectedDeckType != DeckType.COLOR_DECK && selectedDeckType != DeckType.THEME_DECK) {
                    FDeckViewer.show(getDeck());
                }
            }
        });
        btnRandom.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                if (lstDecks.getGameType() == GameType.DeckEditorTest) {
                    //for Deck Editor, test deck instead of randomly selecting deck
                    testSelectedDeck();
                    return;
                }
                if (selectedDeckType == DeckType.COLOR_DECK) {
                    DeckgenUtil.randomSelectColors(lstDecks);
                }
                else {
                    DeckgenUtil.randomSelect(lstDecks);
                }
                accept();
            }
        });
        switch (lstDecks.getGameType()) {
        case Constructed:
            break; //delay initialize for constructed until saved decks can be reloaded
        case Commander:
        case Gauntlet:
        case DeckEditorTest:
            initialize(null, DeckType.CUSTOM_DECK);
            break;
        default:
            initialize(null, DeckType.RANDOM_DECK);
            break;
        }
        lstDecks.setSelectionChangedHandler(selectionChangedHandler);
    }

    private void accept() {
        Forge.back();
        if (callback != null) {
            callback.run(getDeck());
        }
    }

    @Override
    public void onActivate() {
        if (needRefreshOnActivate) {
            needRefreshOnActivate = false;
            refreshDecksList(DeckType.CUSTOM_DECK, true, null);
            switch (lstDecks.getGameType()) {
            case Commander:
                lstDecks.setSelectedString(DeckPreferences.getCommanderDeck());
                break;
            case Archenemy:
                lstDecks.setSelectedString(DeckPreferences.getSchemeDeck());
                break;
            case Planechase:
                lstDecks.setSelectedString(DeckPreferences.getPlanarDeck());
                break;
            default:
                lstDecks.setSelectedString(DeckPreferences.getCurrentDeck());
                break;
            }
        }
    }

    private void editSelectedDeck() {
        final DeckProxy deck = lstDecks.getSelectedItem();
        if (deck == null) { return; }

        if (selectedDeckType == DeckType.CUSTOM_DECK) {
            editDeck(deck);
            return;
        }

        //see if deck with selected name exists already
        final IStorage<Deck> decks = FModel.getDecks().getConstructed();
        Deck existingDeck = decks.get(deck.getName());
        if (existingDeck != null) {
            setSelectedDeckType(DeckType.CUSTOM_DECK);
            editDeck(new DeckProxy(existingDeck, "Constructed", lstDecks.getGameType(), decks));
            return;
        }

        //prompt to duplicate deck if deck doesn't exist already
        FOptionPane.showConfirmDialog(selectedDeckType + " cannot be edited directly. Would you like to duplicate " + deck.getName() + " for editing as a custom user deck?",
                "Duplicate Deck?", "Duplicate", "Cancel", new Callback<Boolean>() {
            @Override
            public void run(Boolean result) {
                if (result) {
                    Deck copiedDeck = (Deck)deck.getDeck().copyTo(deck.getName());
                    decks.add(copiedDeck);
                    setSelectedDeckType(DeckType.CUSTOM_DECK);
                    editDeck(new DeckProxy(copiedDeck, "Constructed", lstDecks.getGameType(), decks));
                }
            }
        });
    }

    private void editDeck(DeckProxy deck) {
        EditorType editorType;
        switch (lstDecks.getGameType()) {
        case Commander:
            editorType = EditorType.Commander;
            DeckPreferences.setCommanderDeck(deck.getName());
            break;
        case Archenemy:
            editorType = EditorType.Archenemy;
            DeckPreferences.setSchemeDeck(deck.getName());
            break;
        case Planechase:
            editorType = EditorType.Planechase;
            DeckPreferences.setPlanarDeck(deck.getName());
            break;
        default:
            editorType = EditorType.Constructed;
            DeckPreferences.setCurrentDeck(deck.getName());
            break;
        }
        needRefreshOnActivate = true;
        Forge.openScreen(new FDeckEditor(editorType, deck, true));
    }

    public void initialize(FPref savedStateSetting, DeckType defaultDeckType) {
        stateSetting = savedStateSetting;
        selectedDeckType = defaultDeckType;

        if (cmbDeckTypes == null) { //initialize components with delayed initialization the first time this is populated
            cmbDeckTypes = new FComboBox<DeckType>();
            switch (lstDecks.getGameType()) {
            case Constructed:
            case Gauntlet:
                cmbDeckTypes.addItem(DeckType.CUSTOM_DECK);
                cmbDeckTypes.addItem(DeckType.PRECONSTRUCTED_DECK);
                cmbDeckTypes.addItem(DeckType.QUEST_OPPONENT_DECK);
                cmbDeckTypes.addItem(DeckType.COLOR_DECK);
                cmbDeckTypes.addItem(DeckType.THEME_DECK);
                cmbDeckTypes.addItem(DeckType.RANDOM_DECK);
                cmbDeckTypes.addItem(DeckType.NET_DECK);
                break;
            case Commander:
                cmbDeckTypes.addItem(DeckType.CUSTOM_DECK);
                cmbDeckTypes.addItem(DeckType.RANDOM_DECK);
                cmbDeckTypes.addItem(DeckType.NET_DECK);
                break;
            case DeckEditorTest:
                cmbDeckTypes.addItem(DeckType.CUSTOM_DECK);
                cmbDeckTypes.addItem(DeckType.PRECONSTRUCTED_DECK);
                cmbDeckTypes.addItem(DeckType.QUEST_OPPONENT_DECK);
                cmbDeckTypes.addItem(DeckType.NET_DECK);
                break;
            default:
                cmbDeckTypes.addItem(DeckType.CUSTOM_DECK);
                cmbDeckTypes.addItem(DeckType.RANDOM_DECK);
                break;
            }
            cmbDeckTypes.setAlignment(HAlignment.CENTER);
            restoreSavedState();
            cmbDeckTypes.setChangedHandler(new FEventHandler() {
                @Override
                public void handleEvent(final FEvent e) {
                    if (cmbDeckTypes.getSelectedItem() == DeckType.NET_DECK && !refreshingDeckType) {
                        FThreads.invokeInBackgroundThread(new Runnable() { //needed for loading net decks
                            @Override
                            public void run() {
                                final NetDeckCategory category = NetDeckCategory.selectAndLoad(lstDecks.getGameType());

                                FThreads.invokeInEdtLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (category == null) {
                                            cmbDeckTypes.setSelectedItem(selectedDeckType); //restore old selection if user cancels
                                            if (selectedDeckType == DeckType.NET_DECK && netDeckCategory != null) {
                                                cmbDeckTypes.setText(netDeckCategory.getDeckType());
                                            }
                                            return;
                                        }

                                        netDeckCategory = category;
                                        refreshDecksList(DeckType.NET_DECK, true, e);
                                    }
                                });
                            }
                        });
                        return;
                    }
                    refreshDecksList(cmbDeckTypes.getSelectedItem(), false, e);
                }
            });
            add(cmbDeckTypes);
            add(lstDecks);
            add(btnNewDeck);
            add(btnEditDeck);
            add(btnViewDeck);
            add(btnRandom);
        }
        else {
            restoreSavedState(); //ensure decks refreshed and state restored in case any deleted or added since last loaded
        }
    }

    private void refreshDecksList(DeckType deckType, boolean forceRefresh, FEvent e) {
        if (selectedDeckType == deckType && !forceRefresh) { return; }
        selectedDeckType = deckType;

        if (e == null) {
            refreshingDeckType = true;
            cmbDeckTypes.setSelectedItem(deckType);
            refreshingDeckType = false;
        }
        if (deckType == null) { return; }

        int maxSelections = 1;
        Iterable<DeckProxy> pool;
        ItemManagerConfig config;

        switch (deckType) {
        case CUSTOM_DECK:
            switch (lstDecks.getGameType()) {
            case Commander:
                pool = DeckProxy.getAllCommanderDecks();
                config = ItemManagerConfig.COMMANDER_DECKS;
                break;
            case Archenemy:
                pool = DeckProxy.getAllSchemeDecks();
                config = ItemManagerConfig.SCHEME_DECKS;
                break;
            case Planechase:
                pool = DeckProxy.getAllPlanarDecks();
                config = ItemManagerConfig.PLANAR_DECKS;
                break;
            default:
                pool = DeckProxy.getAllConstructedDecks();
                config = ItemManagerConfig.CONSTRUCTED_DECKS;
                break;
            }
            break;
        case COLOR_DECK:
            maxSelections = 3;
            pool = ColorDeckGenerator.getColorDecks(lstDecks, isAi);
            config = ItemManagerConfig.STRING_ONLY;
            break;
        case THEME_DECK:
            pool = DeckProxy.getAllThemeDecks();
            config = ItemManagerConfig.STRING_ONLY;
            break;
        case QUEST_OPPONENT_DECK:
            pool = DeckProxy.getAllQuestEventAndChallenges();
            config = ItemManagerConfig.QUEST_EVENT_DECKS;
            break;
        case PRECONSTRUCTED_DECK:
            pool = DeckProxy.getAllPreconstructedDecks(QuestController.getPrecons());
            config = ItemManagerConfig.PRECON_DECKS;
            break;
        case RANDOM_DECK:
            pool = RandomDeckGenerator.getRandomDecks(lstDecks, isAi);
            config = ItemManagerConfig.STRING_ONLY;
            break;
        case NET_DECK:
            if (netDeckCategory != null) {
                cmbDeckTypes.setText(netDeckCategory.getDeckType());
            }
            pool = DeckProxy.getNetDecks(netDeckCategory);
            config = ItemManagerConfig.NET_DECKS;
            break;
        default:
            BugReporter.reportBug("Unsupported deck type: " + deckType);
            return;
        }

        lstDecks.setSelectionSupport(1, maxSelections);
        lstDecks.setPool(pool);
        lstDecks.setup(config);

        if (config == ItemManagerConfig.STRING_ONLY) {
            //hide edit/view buttons for string-only lists
            btnNewDeck.setWidth(getWidth() - 2 * PADDING);
            btnEditDeck.setVisible(false);
            btnViewDeck.setVisible(false);
            btnRandom.setWidth(btnNewDeck.getWidth());

            btnNewDeck.setText("Generate New Deck");
            switch (deckType) {
            case COLOR_DECK:
                btnRandom.setText("Random Colors");
                break;
            case THEME_DECK:
                btnRandom.setText("Random Theme");
                break;
            default:
                btnRandom.setText("Random Deck");
                break;
            }
        }
        else {
            btnNewDeck.setWidth(btnEditDeck.getWidth());
            btnEditDeck.setVisible(true);
            btnViewDeck.setVisible(true);
            btnRandom.setWidth(btnNewDeck.getWidth());

            btnNewDeck.setText("New Deck");

            if (lstDecks.getGameType() == GameType.DeckEditorTest) {
                //handle special case of Deck Editor screen where this button will start a game with the deck
                btnRandom.setText("Test Deck");
            }
            else {
                btnRandom.setText("Random Deck");
            }
        }

        btnRandom.setLeft(getWidth() - PADDING - btnRandom.getWidth());

        if (e != null) { //set default list selection if from combo box change event
            if (deckType == DeckType.COLOR_DECK) {
                // default selection = basic two color deck
                lstDecks.setSelectedIndices(new Integer[]{0, 1});
            }
            else {
                lstDecks.setSelectedIndex(0);
            }
        }
    }

    @Override
    protected void doLayout(float startY, float width, float height) {
        float x = PADDING;
        float y = startY + PADDING;
        width -= 2 * x;

        float fieldHeight = cmbDeckTypes.getHeight();
        float totalButtonHeight = 2 * fieldHeight + PADDING;

        cmbDeckTypes.setBounds(x, y, width, fieldHeight);
        y += cmbDeckTypes.getHeight() + 1;
        lstDecks.setBounds(x, y, width, height - y - totalButtonHeight - 2 * PADDING); //leave room for buttons at bottom

        y += lstDecks.getHeight() + PADDING;
        float buttonWidth = (width - PADDING) / 2;

        if (btnEditDeck.isVisible()) {
            btnNewDeck.setBounds(x, y, buttonWidth, fieldHeight);
        }
        else {
            btnNewDeck.setBounds(x, y, width, fieldHeight);
        }
        btnEditDeck.setBounds(x + buttonWidth + PADDING, y, buttonWidth, fieldHeight);
        y += fieldHeight + PADDING;

        btnViewDeck.setBounds(x, y, buttonWidth, fieldHeight);
        if (btnViewDeck.isVisible()) {
            btnRandom.setBounds(x + buttonWidth + PADDING, y, buttonWidth, fieldHeight);
        }
        else {
            btnRandom.setBounds(x, y, width, fieldHeight);
        }
    }

    public DeckType getSelectedDeckType() { return selectedDeckType; }
    public void setSelectedDeckType(DeckType selectedDeckType0) {
        refreshDecksList(selectedDeckType0, false, null);
    }

    public DeckManager getLstDecks() { return lstDecks; }

    public Deck getDeck() {
        DeckProxy proxy = lstDecks.getSelectedItem();
        if (proxy == null) { return null; }
        return proxy.getDeck();
    }

    /** Generates deck from current list selection(s). */
    public RegisteredPlayer getPlayer(boolean forceRefresh) {
        if (player != null && !forceRefresh) {
            return player;
        }

        if (lstDecks.getSelectedIndex() < 0) {
            player = null;
        }
        else if (selectedDeckType == DeckType.QUEST_OPPONENT_DECK) {
            //Special branch for quest events
            QuestEvent event = DeckgenUtil.getQuestEvent(lstDecks.getSelectedItem().getName());
            player = new RegisteredPlayer(event.getEventDeck());
            if (event instanceof QuestEventChallenge) {
                player.setStartingLife(((QuestEventChallenge) event).getAiLife());
            }
            player.setCardsOnBattlefield(QuestUtil.getComputerStartingCards(event));
        }
        else {
            player = new RegisteredPlayer(getDeck());
        }
        return player;
    }

    public final boolean isAi() {
        return isAi;
    }

    public void setIsAi(boolean isAiDeck) {
        isAi = isAiDeck;
    }

    private final String SELECTED_DECK_DELIMITER = "::";

    public void saveState() {
        if (stateSetting == null) {
            throw new NullPointerException("State setting missing. Specify first using the initialize() method.");
        }
        prefs.setPref(stateSetting, getState());
        prefs.save();
    }

    private String getState() {
        StringBuilder state = new StringBuilder();
        if (cmbDeckTypes.getSelectedItem() == null || cmbDeckTypes.getSelectedItem() == DeckType.NET_DECK) {
            //handle special case of net decks
            if (netDeckCategory == null) { return ""; }
            state.append(NetDeckCategory.PREFIX + netDeckCategory.getName());
        }
        else {
            state.append(cmbDeckTypes.getSelectedItem().name());
        }
        state.append(";");
        joinSelectedDecks(state, SELECTED_DECK_DELIMITER);
        return state.toString();
    }

    private void joinSelectedDecks(StringBuilder state, String delimiter) {
        Iterable<DeckProxy> selectedDecks = lstDecks.getSelectedItems();
        boolean isFirst = true;
        if (selectedDecks != null) {
            for (DeckProxy deck : selectedDecks) {
                if (isFirst) {
                    isFirst = false;
                }
                else {
                    state.append(delimiter);
                }
                state.append(deck.toString());
            }
        }
    }

    private void restoreSavedState() {
        DeckType oldDeckType = selectedDeckType;
        if (stateSetting == null) {
            //if can't restore saved state, just refresh deck list
            refreshDecksList(oldDeckType, true, null);
            return;
        }

        String savedState = prefs.getPref(stateSetting);
        refreshDecksList(getDeckTypeFromSavedState(savedState), true, null);
        if (!lstDecks.setSelectedStrings(getSelectedDecksFromSavedState(savedState))) {
            //if can't select old decks, just refresh deck list
            refreshDecksList(oldDeckType, true, null);
        }
    }

    private DeckType getDeckTypeFromSavedState(String savedState) {
        try {
            if (StringUtils.isBlank(savedState)) {
                return selectedDeckType;
            }
            else {
                String deckType = savedState.split(";")[0];
                if (deckType.startsWith(NetDeckCategory.PREFIX)) {
                    netDeckCategory = NetDeckCategory.selectAndLoad(lstDecks.getGameType(), deckType.substring(NetDeckCategory.PREFIX.length()));
                    return DeckType.NET_DECK;
                }
                return DeckType.valueOf(deckType);
            }
        }
        catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage() + ". Using default : " + selectedDeckType);
            return selectedDeckType;
        }
    }

    private List<String> getSelectedDecksFromSavedState(String savedState) {
        try {
            if (StringUtils.isBlank(savedState)) {
                return new ArrayList<String>();
            }
            else {
                return Arrays.asList(savedState.split(";")[1].split(SELECTED_DECK_DELIMITER));
            }
        }
        catch (Exception ex) {
            System.err.println(ex + " [savedState=" + savedState + "]");
            return new ArrayList<String>();
        }
    }

    public FComboBox<DeckType> getDecksComboBox() {
        return cmbDeckTypes;
    }

    //create quick gauntlet for testing deck
    private void testSelectedDeck() {
        final DeckProxy deckProxy = lstDecks.getSelectedItem();
        if (deckProxy == null) { return; }
        final Deck userDeck = deckProxy.getDeck();
        if (userDeck == null) { return; }

        GuiChoose.getInteger("How many opponents are you willing to face?", 1, 50, new Callback<Integer>() {
            @Override
            public void run(final Integer numOpponents) {
                if (numOpponents == null) { return; }

                ListChooser<DeckType> chooser = new ListChooser<DeckType>(
                        "Choose allowed deck types for opponents", 0, 5, Arrays.asList(new DeckType[] {
                        DeckType.CUSTOM_DECK,
                        DeckType.PRECONSTRUCTED_DECK,
                        DeckType.QUEST_OPPONENT_DECK,
                        DeckType.COLOR_DECK,
                        DeckType.THEME_DECK
                }), null, new Callback<List<DeckType>>() {
                    @Override
                    public void run(final List<DeckType> allowedDeckTypes) {
                        if (allowedDeckTypes == null || allowedDeckTypes.isEmpty()) { return; }

                        LoadingOverlay.show("Loading new game...", new Runnable() {
                            @Override
                            public void run() {
                                GauntletData gauntlet = GauntletUtil.createQuickGauntlet(userDeck, numOpponents, allowedDeckTypes);
                                FModel.setGauntletData(gauntlet);

                                List<RegisteredPlayer> players = new ArrayList<RegisteredPlayer>();
                                RegisteredPlayer humanPlayer = new RegisteredPlayer(userDeck).setPlayer(GamePlayerUtil.getGuiPlayer());
                                players.add(humanPlayer);
                                players.add(new RegisteredPlayer(gauntlet.getDecks().get(gauntlet.getCompleted())).setPlayer(GamePlayerUtil.createAiPlayer()));

                                gauntlet.startRound(players, humanPlayer);
                            }
                        });
                    }
                });
                chooser.show(null, true);
            }
        });
    }
}
