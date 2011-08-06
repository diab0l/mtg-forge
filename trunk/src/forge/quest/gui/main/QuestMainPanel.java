package forge.quest.gui.main;

import forge.*;
import forge.gui.GuiUtils;
import forge.quest.gui.QuestAbstractPanel;
import forge.quest.gui.QuestFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


//presumes AllZone.QuestData is not null

//AllZone.QuestData should be set by Gui_QuestOptions
public class QuestMainPanel extends QuestAbstractPanel {
    private QuestData questData;

    JLabel creditsLabel = new JLabel();
    JLabel lifeLabel = new JLabel();
    JLabel statsLabel = new JLabel();
    JLabel titleLabel = new JLabel();
    JLabel nextQuestLabel = new JLabel();

    JComboBox petComboBox = new JComboBox();
    JComboBox deckComboBox = new JComboBox();

    JButton questButton = new JButton("Quests");
    JButton playButton = new JButton("Play");

    private QuestSelectablePanel selectedOpponent;

    JPanel nextMatchPanel = new JPanel();
    CardLayout nextMatchLayout = new CardLayout();

    boolean isShowingQuests = false;
    private JCheckBox devModeCheckBox = new JCheckBox("Developer Mode");
    private JCheckBox newGUICheckbox = new JCheckBox("Use new UI", true);
    private JCheckBox smoothLandCheckBox = new JCheckBox("Adjust AI Land");
    private JCheckBox petCheckBox = new JCheckBox("Summon Pet");

    private JCheckBox plantBox = new JCheckBox("Summon Plant");
    private static final String NO_DECKS_AVAILABLE = "No decks available";
    private static final String BATTLES = "Battles";
    private static final String QUESTS = "Quests";

    //TODO: Make this ordering permanent
    private static String lastUsedDeck;

    public QuestMainPanel(QuestFrame mainFrame) {
        super(mainFrame);
        questData = AllZone.QuestData;

        initUI();
    }

    private void initUI() {
        refresh();

        this.setLayout(new BorderLayout(5, 5));
        JPanel centerPanel = new JPanel(new BorderLayout());
        this.add(centerPanel, BorderLayout.CENTER);

        JPanel northPanel = createStatusPanel();
        this.add(northPanel, BorderLayout.NORTH);

        JPanel eastPanel = createSidePanel();
        this.add(eastPanel, BorderLayout.EAST);

        JPanel matchSettingsPanel = createMatchSettingsPanel();
        centerPanel.add(matchSettingsPanel, BorderLayout.SOUTH);

        JPanel nextGamePanel = createNextMatchPanel();
        centerPanel.add(nextGamePanel, BorderLayout.CENTER);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    private JPanel createNextMatchPanel() {
        nextMatchPanel = new JPanel();
        nextMatchPanel.setLayout(nextMatchLayout);
        nextMatchPanel.add(createBattlePanel(), BATTLES);
        nextMatchPanel.add(createQuestPanel(), QUESTS);
        return nextMatchPanel;
    }

    private JPanel createStatusPanel() {
        JPanel northPanel = new JPanel();
        JLabel modeLabel;
        JLabel difficultyLabel;//Create labels at the top
        titleLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 28));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(titleLabel);

        northPanel.add(Box.createVerticalStrut(5));

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusPanel.setAlignmentX(LEFT_ALIGNMENT);

        modeLabel = new JLabel(questData.getMode());
        statusPanel.add(modeLabel);
        statusPanel.add(Box.createHorizontalGlue());

        difficultyLabel = new JLabel(questData.getDifficulty());
        statusPanel.add(difficultyLabel);
        statusPanel.add(Box.createHorizontalGlue());

        statusPanel.add(statsLabel);

        northPanel.add(statusPanel);
        return northPanel;
    }

    private JPanel createSidePanel() {
        JPanel panel = new JPanel();
        JPanel optionsPanel;//Create options checkbox list
        optionsPanel = createOptionsPanel();

        List<Component> eastComponents = new ArrayList<Component>();
        //Create buttons

        JButton mainMenuButton = new JButton("Return to Main Menu");
        mainMenuButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                mainFrame.returnToMainMenu();
            }
        });
        eastComponents.add(mainMenuButton);

        JButton cardShopButton = new JButton("Card Shop");
        cardShopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                QuestMainPanel.this.showCardShop();
            }
        });
        eastComponents.add(cardShopButton);
        cardShopButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));

        JButton bazaarButton = null;
        if (questData.getMode().equals(QuestData.FANTASY)) {

            bazaarButton = new JButton("Bazaar");
            bazaarButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    QuestMainPanel.this.showBazaar();
                }
            });
            eastComponents.add(bazaarButton);
            bazaarButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        }


        questButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                QuestMainPanel.this.showQuests();
            }
        });
        eastComponents.add(questButton);
        questButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        questButton.setPreferredSize(new Dimension(0, 60));


        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                QuestMainPanel.this.launchGame();
            }
        });

        playButton.setFont(new Font(Font.DIALOG, Font.BOLD, 28));
        playButton.setPreferredSize(new Dimension(0, 100));


        eastComponents.add(playButton);
        eastComponents.add(optionsPanel);

        GuiUtils.setWidthToMax(eastComponents);

        panel.add(mainMenuButton);
        GuiUtils.addGap(panel);
        panel.add(optionsPanel);
        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalGlue());

        if (questData.getMode().equals(QuestData.FANTASY)) {

            panel.add(this.lifeLabel);
            this.lifeLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
            this.lifeLabel.setIcon(GuiUtils.getResizedIcon(GuiUtils.getIconFromFile("Life.png"), 30, 30));
        }

        GuiUtils.addGap(panel);
        panel.add(this.creditsLabel);
        this.creditsLabel.setIcon(GuiUtils.getResizedIcon(GuiUtils.getIconFromFile("CoinStack.png"), 30, 30));
        this.creditsLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        GuiUtils.addGap(panel, 10);
        panel.add(cardShopButton);

        if (questData.getMode().equals(QuestData.FANTASY)) {
            GuiUtils.addGap(panel);
            panel.add(bazaarButton);
        }

        panel.add(Box.createVerticalGlue());

        panel.add(questButton);
        this.nextQuestLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
        panel.add(nextQuestLabel);
        GuiUtils.addGap(panel);

        panel.add(playButton);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JPanel createOptionsPanel() {
        JPanel optionsPanel;
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

        optionsPanel.add(this.newGUICheckbox);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(this.smoothLandCheckBox);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(this.devModeCheckBox);
        optionsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Options"));
        return optionsPanel;
    }

    private JPanel createMatchSettingsPanel() {

        JPanel matchPanel = new JPanel();
        matchPanel.setLayout(new BoxLayout(matchPanel, BoxLayout.Y_AXIS));

        JPanel deckPanel = new JPanel();
        deckPanel.setLayout(new BoxLayout(deckPanel, BoxLayout.X_AXIS));

        JLabel deckLabel = new JLabel("Use Deck");
        deckPanel.add(deckLabel);
        GuiUtils.addGap(deckPanel);

        this.deckComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                playButton.setEnabled(canGameBeLaunched());
                lastUsedDeck = (String) deckComboBox.getSelectedItem();
            }
        });

        deckPanel.add(this.deckComboBox);
        GuiUtils.addGap(deckPanel);

        JButton editDeckButton = new JButton("Deck Editor");
        editDeckButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                showDeckEditor();
            }
        });
        deckPanel.add(editDeckButton);
        deckPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, deckPanel.getPreferredSize().height));
        deckPanel.setAlignmentX(LEFT_ALIGNMENT);
        matchPanel.add(deckPanel);


        GuiUtils.addGap(matchPanel);

        if (questData.getMode().equals(QuestData.FANTASY)) {
            JPanel petPanel = new JPanel();
            petPanel.setLayout(new BoxLayout(petPanel, BoxLayout.X_AXIS));

            this.petCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    if (petCheckBox.isSelected()) {
                        questData.setSelectedPet((String) petComboBox.getSelectedItem());
                    }
                    else {
                        questData.setSelectedPet("No Plant/Pet");
                    }

                    petComboBox.setEnabled(petCheckBox.isSelected());
                }
            });

            petPanel.add(this.petCheckBox);
            GuiUtils.addGap(petPanel);
            this.petComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    if (petCheckBox.isSelected()) {
                        questData.setSelectedPet((String) petComboBox.getSelectedItem());
                    }
                    else {
                        questData.setSelectedPet("No Plant/Pet");
                    }
                }
            });
            petPanel.add(this.petComboBox);
            matchPanel.add(petPanel);
            petPanel.setMaximumSize(petPanel.getPreferredSize());
            petPanel.setAlignmentX(LEFT_ALIGNMENT);
        }
        return matchPanel;
    }

    private JPanel createBattlePanel() {
        JPanel BattlePanel = new JPanel();
        BattlePanel.setLayout(new BoxLayout(BattlePanel, BoxLayout.Y_AXIS));
        BattlePanel.setBorder(new TitledBorder(new EtchedBorder(), "Available Battles"));

        List<QuestSelectablePanel> Battles = QuestBattle.getBattles();

        for (QuestSelectablePanel Battle : Battles) {
            BattlePanel.add(Battle);
            Battle.addMouseListener(new SelectionAdapter(Battle));

            GuiUtils.addGap(BattlePanel, 3);
        }

        BattlePanel.setAlignmentX(LEFT_ALIGNMENT);

        return BattlePanel;
    }

    private JPanel createQuestPanel() {
        JPanel questPanel = new JPanel();
        questPanel.setLayout(new BoxLayout(questPanel, BoxLayout.Y_AXIS));
        questPanel.setBorder(new TitledBorder(new EtchedBorder(), "Available Quests"));


        List<QuestQuest> quests = QuestQuest.getQuests();

        for (QuestQuest quest : quests) {
            questPanel.add(quest);
            quest.addMouseListener(new SelectionAdapter(quest));

            GuiUtils.addGap(questPanel, 3);
        }


        return questPanel;
    }

    void refresh() {
        QuestData.saveData(AllZone.QuestData);

        devModeCheckBox.setSelected(Constant.Runtime.DevMode[0]);
        smoothLandCheckBox.setSelected(Constant.Runtime.Smooth[0]);
        newGUICheckbox.setSelected(Gui_NewGame.preferences.newGui);

        creditsLabel.setText(" " + questData.getCredits());
        statsLabel.setText(questData.getWin() + " wins / " + questData.getLost() + " losses");
        titleLabel.setText(questData.getRank());

        //copy lastUsedDeck as removal triggers selection change. 
        String lastUsedDeck = QuestMainPanel.lastUsedDeck;
        deckComboBox.removeAllItems();

        if (questData.getDeckNames().size() > 0) {
            deckComboBox.setEnabled(true);

            List<String> deckNames = new ArrayList<String>(questData.getDeckNames());

            Collections.sort(deckNames, new Comparator<String>() {
                public int compare(String s, String s1) {
                    return s.compareToIgnoreCase(s1);
                }
            });

            if (deckNames.contains(lastUsedDeck)){
                deckNames.remove(lastUsedDeck);
                deckNames.add(0,lastUsedDeck);
            }

            for (String deckName : deckNames) {
                deckComboBox.addItem(deckName);
            }
        }

        else {
            deckComboBox.addItem(NO_DECKS_AVAILABLE);
            deckComboBox.setEnabled(false);
        }
        deckComboBox.setMinimumSize(new Dimension(150, 0));

        questButton.setEnabled(nextQuestInWins() == 0);

        playButton.setEnabled(canGameBeLaunched());

        if (questData.getMode().equals(QuestData.FANTASY)) {
            lifeLabel.setText(" " + questData.getLife());
            
            petComboBox.removeAllItems();

            List<String> petList = QuestUtil.getPetNames(questData);

            if (petList.size() > 0) {
                petComboBox.setEnabled(true);
                petCheckBox.setEnabled(true);
                for (String aPetList : petList) {
                    petComboBox.addItem(aPetList);
                }
            }

            else {
                petComboBox.addItem("No pets available");
                petComboBox.setEnabled(false);
                petCheckBox.setEnabled(false);
            }

            if (questData.getSelectedPet() == null || questData.getSelectedPet().equals("No Plant/Pet")) {
                petCheckBox.setSelected(false);
                petComboBox.setEnabled(false);
            }
            else {
                petCheckBox.setSelected(true);
                petComboBox.setSelectedItem(questData.getSelectedPet());
            }
        }

        if (nextQuestInWins() > 0) {
            nextQuestLabel.setText("Next Quest in " + nextQuestInWins() + " Wins.");
        }
        else {
            nextQuestLabel.setText("Next Quest available now.");
        }
    }

    private int nextQuestInWins() {

        if (questData.getWin() < 25) {
            return 25 - questData.getWin();
        }

        int questsPlayed = questData.getQuestsPlayed();
        int mul = 6;

        if (questData.getGearLevel() == 1) {
            mul = 5;
        }
        else if (questData.getGearLevel() == 2) {
            mul = 4;
        }

        int delta = (questsPlayed * mul) - questData.getWin();

        return (delta > 0) ? delta : 0;
    }


    void showDeckEditor() {
        Command exit = new Command() {
            private static final long serialVersionUID = -5110231879431074581L;

            public void execute() {
                //saves all deck data
                QuestData.saveData(AllZone.QuestData);

                new QuestFrame();
            }
        };

        Gui_Quest_DeckEditor g = new Gui_Quest_DeckEditor();

        g.show(exit);
        g.setVisible(true);
        mainFrame.dispose();
    }//deck editor button

    void showBazaar() {
        mainFrame.showBazaarPane();
    }

    void showCardShop() {
        Command exit = new Command() {
            private static final long serialVersionUID = 8567193482568076362L;

            public void execute() {
                //saves all deck data
                QuestData.saveData(AllZone.QuestData);

                new QuestFrame();
            }
        };

        Gui_CardShop g = new Gui_CardShop(questData);

        g.show(exit);
        g.setVisible(true);

        this.mainFrame.dispose();

    }//card shop button

    private void launchGame() {

        //TODO: This is a temporary hack to see if the image cache affects the heap usage significantly.
        ImageCache.clear();

        String humanDeckName = (String) deckComboBox.getSelectedItem();
        Deck humanDeck = questData.getDeck(humanDeckName);
        Constant.Runtime.HumanDeck[0] = humanDeck;
        moveDeckToTop(humanDeckName);

        Constant.Quest.oppIconName[0] = getMatchIcon();

        // Dev Mode occurs before Display
        Constant.Runtime.DevMode[0] = devModeCheckBox.isSelected();

        //DO NOT CHANGE THIS ORDER, GuiDisplay needs to be created before cards are added
        if (newGUICheckbox.isSelected()) {
            AllZone.Display = new GuiDisplay4();
        }
        else {
            AllZone.Display = new GuiDisplay3();
        }

        Gui_NewGame.preferences.newGui = newGUICheckbox.isSelected();

        Constant.Runtime.Smooth[0] = smoothLandCheckBox.isSelected();


        if (isShowingQuests) {
            setupQuest(humanDeck);
        }

        else {
            setupBattle(humanDeck);
        }

        AllZone.Display.setVisible(true);
        mainFrame.dispose();
    }


    void setupBattle(Deck humanDeck) {

        Deck computer = questData.ai_getDeckNewFormat((selectedOpponent).getName());
        Constant.Runtime.ComputerDeck[0] = computer;

        AllZone.GameAction.newGame(
                humanDeck,
                computer,
                QuestUtil.getHumanPlantAndPet(questData),
                new CardList(),
                QuestUtil.getLife(questData),
                20,
                null);
    }

    private void setupQuest(Deck humanDeck) {
        Quest_Assignment selectedQuest = ((QuestQuest) selectedOpponent).getQuestAssignment();

        Deck computerDeck = questData.ai_getDeckNewFormat("quest" + selectedQuest.getId());
        Constant.Runtime.ComputerDeck[0] = computerDeck;

        AllZone.QuestAssignment = selectedQuest;

        int extraLife = 0;

        if (questData.getGearLevel() == 2) {
            extraLife = 3;
        }

        AllZone.GameAction.newGame(
                humanDeck,
                computerDeck,
                QuestUtil.getHumanPlantAndPet(questData, selectedQuest),
                new CardList(),
                questData.getLife() + extraLife,
                selectedQuest.getComputerLife(),
                selectedQuest);

    }

    String getMatchIcon(){
        String oppIconName;

        if (isShowingQuests){
            Quest_Assignment selectedQuest = ((QuestQuest) selectedOpponent).getQuestAssignment();
            oppIconName = selectedQuest.getIconName();
        }

        else{
            oppIconName = selectedOpponent.getName();
            oppIconName = oppIconName.substring(0, oppIconName.length() - 1).trim() + ".jpg";
        }
        return oppIconName;
    }
    void showQuests() {
        if (isShowingQuests) {
            this.nextMatchLayout.show(nextMatchPanel, BATTLES);
            isShowingQuests = false;
            questButton.setText("Quests");
        }
        else {
            this.nextMatchLayout.show(nextMatchPanel, QUESTS);
            isShowingQuests = true;
            questButton.setText("Battles");
        }

        if (selectedOpponent != null) {
            selectedOpponent.setSelected(false);
        }

        selectedOpponent = null;

        refresh();
    }

    class SelectionAdapter extends MouseAdapter {
        QuestSelectablePanel selectablePanel;

        SelectionAdapter(QuestSelectablePanel selectablePanel) {
            super();
            this.selectablePanel = selectablePanel;
        }

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {

            if (selectedOpponent != null) {
                selectedOpponent.setSelected(false);
            }

            selectablePanel.setSelected(true);

            selectedOpponent = selectablePanel;
            playButton.setEnabled(canGameBeLaunched());
        }

    }

    private void moveDeckToTop(String humanDeckName) {
        this.lastUsedDeck = humanDeckName;
    }


    boolean canGameBeLaunched() {
        return !(NO_DECKS_AVAILABLE.equals(deckComboBox.getSelectedItem()) ||
                selectedOpponent == null);
    }

    @Override
    public void refreshState() {
        this.refresh();
    }

}
