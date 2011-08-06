
package forge;


import forge.deck.Deck;
import forge.error.ErrorViewer;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.properties.NewConstants.LANG.Gui_WinLose.WINLOSE_TEXT;
import forge.quest.data.QuestMatchState;
import forge.quest.data.QuestPreferences;
import forge.quest.gui.QuestFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;


public class Gui_WinLose extends JFrame implements NewConstants {
    private static final long serialVersionUID = -5800412940994975483L;
    
    //private CardList          humanList;
    //private CardList          computerList;
    
    //private int               humanLife;
    //private int               computerLife;
    
    //private boolean           fantasyQuest     = false;
    
    private JLabel            titleLabel       = new JLabel();
    private JButton           continueButton   = new JButton();
    private JButton           restartButton    = new JButton();
    private JButton           quitButton       = new JButton();
    private JLabel            statsLabel       = new JLabel();
    private JPanel            jPanel2          = new JPanel();
    @SuppressWarnings("unused")
    // titledBorder1
    private TitledBorder      titledBorder1;
    @SuppressWarnings("unused")
    // border1
    private Border            border1;
    
    public static void main(String[] args) {
        Constant.Runtime.GameType[0] = Constant.GameType.Sealed;
        
        Constant.Runtime.matchState.addWin();
        Constant.Runtime.matchState.addLose();
        
        //setup limited deck
        Deck deck = new Deck(Constant.GameType.Sealed);
        CardList pack = new CardList(BoosterPack.getBoosterPack(1).toArray());
        
        for(int i = 0; i < pack.size(); i++)
            if((i % 2) == 0) deck.addSideboard(pack.get(i).getName());
            else deck.addMain(pack.get(i).getName());
        
        Constant.Runtime.HumanDeck[0] = deck;
        //end - setup limited deck
        
        new Gui_WinLose();
    }
    
    public Gui_WinLose(CardList human, CardList computer, int hLife, int cLife) {
    	/*
    	fantasyQuest = true;
    	
    	humanList = human;
    	computerList = computer;
    	
    	humanLife = hLife;
    	computerLife= cLife;
    	*/
    	try {
            jbInit();
        } catch(Exception ex) {
            ErrorViewer.showError(ex);
        }
        
        setup();
        
        Dimension screen = this.getToolkit().getScreenSize();
        setBounds(screen.width / 3, 100, //position
                215, 370); //size
        setVisible(true);
    }
    
    public Gui_WinLose() {
        /*
    	fantasyQuest = false;
    	
    	humanList = new CardList();
    	computerList = new CardList();
    	
    	humanLife = 20;
    	computerLife= 20;
    	*/
    	
    	try {
            jbInit();
        } catch(Exception ex) {
            ErrorViewer.showError(ex);
        }
        
        setup();
        
        Dimension screen = this.getToolkit().getScreenSize();
        setBounds(screen.width / 3, 100, //position
                215, 370); //size
        setVisible(true);
    }
    
    private void setup() {
    	AllZone.GameInfo.clearColorChanges();
        QuestMatchState winLose = Constant.Runtime.matchState;
        Phase.setGameBegins(0);
        //3 is the match length, 3 is the number of games
        //disable buttons if match is up, or human player won 2 or lost 2 games already
        if((winLose.countWinLose() == 3) || (winLose.getWin() == 2) || (winLose.getLose() == 2)) {
//      editDeckButton.setEnabled(false);
            continueButton.setEnabled(false);
            quitButton.grabFocus();
        }
        
        if (winLose.getWin()==2)
        	restartButton.setEnabled(false);
        
        //show Wins and Loses
        statsLabel.setText(ForgeProps.getLocalized(WINLOSE_TEXT.WON) + winLose.getWin() + ForgeProps.getLocalized(WINLOSE_TEXT.LOST) + winLose.getLose());
        
        //show "You Won" or "You Lost"
        if(winLose.didWinRecently()) 
        {
        	titleLabel.setText(ForgeProps.getLocalized(WINLOSE_TEXT.WIN));
        	
        	int game = 0;
        	if (winLose.getWinTurns()[0] != 0)
        		game = 1;
        	int turn = AllZone.Phase.getTurn();
        	if (AllZone.GameInfo.isComputerStartedThisGame())
        		turn--;
        	
        	if (turn < 1)
        		turn = 1;
        	
        	winLose.setWinTurn(game, turn);
        	winLose.setMulliganedToZero(game, AllZone.GameInfo.getHumanMulliganedToZero());
        	
        	//winLose.setWinTurn(winLose.countWinLose()-1, AllZone.Phase.getTurn());
        	
        	//System.out.println("CountwinLose:" + winLose.countWinLose());
        	//System.out.println("You won by turn: " + AllZone.Phase.getTurn());
        }
        else 
        {
        	titleLabel.setText(ForgeProps.getLocalized(WINLOSE_TEXT.LOSE));
        	//System.out.println("You lost by turn: " + AllZone.Phase.getTurn());
        }
    }//setup();
    
    private void jbInit() throws Exception {
        titledBorder1 = new TitledBorder("");
        border1 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
        titleLabel.setFont(new java.awt.Font("Dialog", 0, 26));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setText(ForgeProps.getLocalized(WINLOSE_TEXT.WIN));
        this.getContentPane().setLayout(new MigLayout("fill"));
        continueButton.setText(ForgeProps.getLocalized(WINLOSE_TEXT.CONTINUE));
        continueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                continueButton_actionPerformed(e);
            }
        });
        restartButton.setText(ForgeProps.getLocalized(WINLOSE_TEXT.RESTART));
        restartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restartButton_actionPerformed(e);
            }
        });
        quitButton.setText(ForgeProps.getLocalized(WINLOSE_TEXT.QUIT));
        quitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quitButton_actionPerformed(e);
            }
        });
        statsLabel.setFont(new java.awt.Font("Dialog", 0, 16));
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        jPanel2.setBorder(BorderFactory.createLineBorder(Color.black));
        jPanel2.setLayout(new MigLayout("align center"));
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                this_windowClosing(e);
            }
        });
        this.getContentPane().add(titleLabel, "align center, grow, wrap");
        this.getContentPane().add(statsLabel, "align center, grow, wrap");
        this.getContentPane().add(jPanel2, "grow");
        jPanel2.add(continueButton, "sg buttons, w 80%, h 20%, wrap");
        jPanel2.add(quitButton, "sg buttons, wrap");
        jPanel2.add(restartButton, "sg buttons");
        
    }
    
    void editDeckButton_actionPerformed(ActionEvent e) {
        Command exit = new Command() {
            private static final long serialVersionUID = 4735992294414389187L;
            
            public void execute() {
                new Gui_WinLose();
            }
        };
        Gui_DeckEditor editor = new Gui_DeckEditor();
        
        editor.show(exit);
        
        dispose();
    }//editDeckButton_actionPerformed()
    
    void continueButton_actionPerformed(ActionEvent e) {
        //open up "Game" screen
    	//AllZone.Computer_Play.reset();//sometimes computer has creature in play in the 2nd game of the match
        
    	if (!Constant.Quest.fantasyQuest[0])
    		AllZone.GameAction.newGame(Constant.Runtime.HumanDeck[0], Constant.Runtime.ComputerDeck[0]);
    	else{
    		int extraLife = 0;
    		if (AllZone.QuestAssignment != null) {
    			forge.quest.data.QuestUtil.setupQuest(AllZone.QuestAssignment);
    			if (AllZone.QuestData.getInventory().hasItem("Zeppelin"))
    				extraLife = 3;
    		}
    		//AllZone.GameAction.newGame(Constant.Runtime.HumanDeck[0], Constant.Runtime.ComputerDeck[0], humanList, computerList, humanLife, computerLife);
    		CardList humanList = forge.quest.data.QuestUtil.getHumanPlantAndPet(AllZone.QuestData, AllZone.QuestAssignment);
    		CardList computerList = new CardList();
    		

    		int humanLife = AllZone.QuestData.getLife() + extraLife;
    		int computerLife = 20;
    		if (AllZone.QuestAssignment!=null)
    			computerLife = AllZone.QuestAssignment.getComputerLife();
    		
    		AllZone.GameAction.newGame(Constant.Runtime.HumanDeck[0], Constant.Runtime.ComputerDeck[0], humanList, computerList, humanLife, computerLife, AllZone.QuestAssignment);
    	}
        AllZone.Display.setVisible(true);
        
        dispose();
    }
    
    void restartButton_actionPerformed(ActionEvent e) {
        Constant.Runtime.matchState.reset();
        
        if (!Constant.Quest.fantasyQuest[0])
    		AllZone.GameAction.newGame(Constant.Runtime.HumanDeck[0], Constant.Runtime.ComputerDeck[0]);
    	else{
    		int extraLife = 0;
    		//AllZone.GameAction.newGame(Constant.Runtime.HumanDeck[0], Constant.Runtime.ComputerDeck[0], humanList, computerList, humanLife, computerLife);
    		if (AllZone.QuestAssignment != null) {
    			forge.quest.data.QuestUtil.setupQuest(AllZone.QuestAssignment);
    			if (AllZone.QuestData.getInventory().hasItem("Zeppelin"))
    				extraLife = 3;
    		}
    			
    		CardList humanList = forge.quest.data.QuestUtil.getHumanPlantAndPet(AllZone.QuestData, AllZone.QuestAssignment);
    		//CardList computerList = QuestUtil.getComputerCreatures(AllZone.QuestData, AllZone.QuestAssignment);
    		CardList computerList = new CardList();
    		
    		int humanLife = AllZone.QuestData.getLife() +extraLife;
    		int computerLife = 20;
    		
    		if (AllZone.QuestAssignment!=null)
    			computerLife = AllZone.QuestAssignment.getComputerLife();
    		
    		AllZone.GameAction.newGame(Constant.Runtime.HumanDeck[0], Constant.Runtime.ComputerDeck[0], humanList, computerList, humanLife, computerLife, AllZone.QuestAssignment);
    	}
        AllZone.Display.setVisible(true);
        
        dispose();
    }
    
    private String getWinText(long creds, QuestMatchState winLose, forge.quest.data.QuestData q)
    {
    	// TODO use q.qdPrefs to write bonus credits in prefs file 
    	StringBuilder sb = new StringBuilder();
    	String[] wins = winLose.getWinMethods();
    	
    	sb.append("<html>");

    	
    	for (String s : wins)
    	{
    		if (s != null) {
    			sb.append("Alternate win condition: ");
    			sb.append("<u>");
    			sb.append(s);
    			sb.append("</u>");
    			sb.append("! Bonus: <b>+");
    			
    			if (s.equals("Poison Counters"))
    				sb.append(QuestPreferences.getMatchRewardPoisonWinBonus());
    			else if (s.equals("Milled"))
    				sb.append(QuestPreferences.getMatchRewardMilledWinBonus());
    			else if (s.equals("Battle of Wits") || 
	    			s.equals("Felidar Sovereign") || s.equals("Helix Pinnacle") || s.equals("Epic Struggle") ||
	    			s.equals("Door to Nothingness") || s.equals("Barren Glory") || s.equals("Near-Death Experience") ||
	    			s.equals("Mortal Combat") || s.equals("Test of Endurance") ) {

	    			sb.append(QuestPreferences.getMatchRewardAltWinBonus());
	    		}
    			
    			sb.append(" credits</b>.<br>");
    		}
    	}
    	
    	int[] winTurns = winLose.getWinTurns();
    	
    	for (int i : winTurns)
    	{
    		System.out.println("Quest, won by turn:" + i);
			
    		if (i != 0){
    			int turnCredits = 0;
    			
        		if (i == 1){
        			sb.append("Won in one turn!");
        			turnCredits += QuestPreferences.getMatchRewardWinFirst();
        		}
    			else if (i <= 5){
    				sb.append("Won by turn 5!");
    				turnCredits += QuestPreferences.getMatchRewardWinByFifth();
    			}
    			else if (i <= 10){
    				sb.append("Won by turn 10!");
    				turnCredits += QuestPreferences.getMatchRewardWinByTen();
    			}
    			else if (i <= 15){
    				sb.append("Won by turn 15!");
    				turnCredits += QuestPreferences.getMatchRewardWinByFifteen();
    			}
        		if (turnCredits > 0)
        			sb.append(" Bonus: <b>+").append(turnCredits).append(" credits</b>.<br>");
    		}
    	}
    	
    	boolean[] mulliganedToZero = winLose.getMulliganedToZero();

    	for (boolean b : mulliganedToZero)
    	{
    		if (b){
    			sb.append("Mulliganed to zero and still won! Bonus: <b>+");
    			sb.append(QuestPreferences.getMatchMullToZero()).append(" credits</b>.<br>");
    		}
    	}
    	
    	if (winLose.getLose()==0)
    		sb.append("You have not lost once! Bonus: <b>+");
    		sb.append(QuestPreferences.getMatchRewardNoLosses()).append(" credits</b>.<br>");
    	
    	if(q.getInventory().getItemLevel("Estates") == 1)
    		sb.append("Estates bonus: <b>10%</b>.<br>");
    	else if(q.getInventory().getItemLevel("Estates") == 2)
    		sb.append("Estates bonus: <b>15%</b>.<br>");
    	else if(q.getInventory().getItemLevel("Estates") == 3)
    		sb.append("Estates bonus: <b>20%</b>.<br>");
    	
    	sb.append("You have earned <b>" + creds + " credits</b> in total.");
    	
    	sb.append("</html>");
    	return sb.toString();
    }
    
    private ImageIcon getCardIcon(String fileName)
    {
    	File base = ForgeProps.getFile(IMAGE_BASE);
    	File file = new File(base, fileName);
    	ImageIcon icon = new ImageIcon(file.toString());
    	return icon;
    }
    
    private ImageIcon getIcon(String fileName)
    {
    	File base = ForgeProps.getFile(IMAGE_ICON);
    	File file = new File(base, fileName);
    	ImageIcon icon = new ImageIcon(file.toString());
    	return icon;
    }
    
    void quitButton_actionPerformed(ActionEvent e) {
        //are we on a quest?
        if(AllZone.QuestData == null) {
        	new Gui_NewGame();
        }
        else { //Quest
            QuestMatchState matchState = Constant.Runtime.matchState;
            forge.quest.data.QuestData quest = AllZone.QuestData;
            
            boolean wonMatch = false;
            if(matchState.getWin() == 2){
            	quest.addWin();
            	wonMatch = true;
            }
            else quest.addLost();
            
            //System.out.println("QuestData cardpoolsize:" + AllZone.QuestData.getCardpool().size());
            if(AllZone.QuestData.getShopList()!= null)
            	AllZone.QuestData.clearShopList();
            
            if(AllZone.QuestData.getAvailableQuests()!= null)
            	AllZone.QuestData.clearAvailableQuests();
            
            if(quest.shouldAddCards(wonMatch)) {
                quest.addCards();
                String fileName = "BookIcon.png";
                ImageIcon icon = getIcon(fileName);
                
                StringBuilder sb = new StringBuilder();
                sb.append("You have won the following new cards:\n");
                for (int i=0; i<quest.getAddedCards().size(); i++)
                	sb.append(quest.getAddedCards().get(i) + "\n");
                
                JOptionPane.showMessageDialog(null, sb.toString(), "", JOptionPane.INFORMATION_MESSAGE, icon );
            }
            
            if (wonMatch){
            	long creds = quest.getCreditsToAdd(matchState);
            	String s = getWinText(creds, matchState, quest);
            	            	
            	String fileName = "GoldIcon.png";
            	ImageIcon icon = getIcon(fileName);
            	
            	JOptionPane.showMessageDialog(null, s, "",  JOptionPane.INFORMATION_MESSAGE, icon);
            	int wins = quest.getWin();
            	if (wins > 0 && wins % 80 == 0)	// at every 80 wins, give 10 random rares
            	{
            		quest.addRandomRare(10);
            		fileName = "BoxIcon.png";
            		icon = getIcon(fileName);
            		JOptionPane.showMessageDialog(null, "You just won 10 random rares!", "",  JOptionPane.INFORMATION_MESSAGE, icon);
            	}
            	
            	if (AllZone.QuestAssignment!=null)
            	{
            		AllZone.QuestData.addQuestsPlayed();
            		Quest_Assignment qa = AllZone.QuestAssignment;
            		
            		StringBuilder sb = new StringBuilder();
            		sb.append("Quest Completed - \r\n");
            		
            		if (qa.getCardRewardList()!= null)
            		{
            			sb.append("You won the following cards:\r\n\r\n");
            			for (String cardName:qa.getCardRewardList())
            			{
            				sb.append(cardName);
            				sb.append("\r\n");
            				
            				AllZone.QuestData.addCard(cardName);
            			}
            			AllZone.QuestData.addToNewList(qa.getCardRewardList());
            			sb.append("\r\n");
            		}
            		sb.append("Quest Bounty: ");
            		sb.append(qa.getCreditsReward());
            		
            		AllZone.QuestData.addCredits(qa.getCreditsReward());
            		
            		fileName = "BoxIcon.png";
            		icon = getIcon(fileName);
            		JOptionPane.showMessageDialog(null, sb.toString(), "Quest Rewards for " +qa.getName() ,  JOptionPane.INFORMATION_MESSAGE, icon);
            	}/* 
            	else if(quest.getDifficultyIndex() == 4) {
                    Card c = AllZone.CardFactory.getCard(quest.addRandomRare(), AllZone.HumanPlayer);
                    c.setCurSetCode(c.getMostRecentSet());
                	fileName = CardUtil.buildFilename(c) +".jpg";
                	icon = getCardIcon(fileName);
                    
                    JOptionPane.showMessageDialog(null, "", "You have won a random rare for winning against a very hard deck.", JOptionPane.INFORMATION_MESSAGE, icon);
                }*/
            	
            }
            else
            {
            	quest.subtractCredits(15);
            	String fileName = "HeartIcon.png";
            	ImageIcon icon = getIcon(fileName);
            	
            	JOptionPane.showMessageDialog(null, "You lose! You have lost 15 credits.", "Awwww", JOptionPane.INFORMATION_MESSAGE, icon);
            }
            
            if(quest.shouldAddAdditionalCards(wonMatch)) {
                Card c = AllZone.CardFactory.getCard(quest.addRandomRare(), AllZone.HumanPlayer);
                c.setCurSetCode(c.getMostRecentSet());
            	String fileName = CardUtil.buildFilename(c) +".jpg";
            	ImageIcon icon = getCardIcon(fileName);
                
                JOptionPane.showMessageDialog(null, "", "You have won a random rare.", JOptionPane.INFORMATION_MESSAGE, icon);
            }
            
            matchState.reset();
            
            AllZone.QuestAssignment = null;
            
            quest.saveData();

            new QuestFrame();
        }//else - on quest
        
        dispose();
        
        //clear Image caches, so the program doesn't get slower and slower
        //not needed with soft values - will shrink as needed
//        ImageUtil.rotatedCache.clear();
//        ImageCache.cache.clear();
    }
    
    void this_windowClosing(WindowEvent e) {
        quitButton_actionPerformed(null);
    }
}
