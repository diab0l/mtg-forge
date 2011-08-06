package forge;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import forge.error.ErrorViewer;


public class Gui_QuestOptions extends JFrame {
    private static final long serialVersionUID    = 2018518804206822235L;
    
    private QuestData         questData           = new QuestData();
    
    private JLabel            jLabel1             = new JLabel();
    private JButton           continueQuestButton = new JButton();
    private JPanel            jPanel1             = new JPanel();
    @SuppressWarnings("unused")
    // border1
    private Border            border1;
    private TitledBorder      titledBorder1;
    private JPanel            jPanel2             = new JPanel();
    private GridLayout        gridLayout1         = new GridLayout();
    private JRadioButton      easyRadio           = new JRadioButton();
    private JRadioButton      hardRadio           = new JRadioButton();
    private JRadioButton      mediumRadio         = new JRadioButton();
    private JRadioButton      veryHardRadio       = new JRadioButton();
    private JButton           newQuestButton      = new JButton();
    private JTextArea         jTextArea1          = new JTextArea();
    private ButtonGroup       buttonGroup1        = new ButtonGroup();
    private JPanel            jPanel3             = new JPanel();
    private Border            border2;
    private TitledBorder      titledBorder2;
    
    public static void main(String[] args) {
        new Gui_QuestOptions();
    }
    
    public Gui_QuestOptions() {
        try {
            jbInit();
        } catch(Exception ex) {
            ErrorViewer.showError(ex);
        }
        
        setup();
        setupRadioButtonText();
        
        setVisible(true);
    }
    
    private void setup() {
        //make the text look correct on the screen
        jTextArea1.setBackground(getBackground());
        
        //center window on the screen
        Dimension screen = this.getToolkit().getScreenSize();
        setBounds(screen.width / 4, 50, //position
                420, 490); //size
        
        //if user closes this window, go back to "New Game" screen
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                Gui_QuestOptions.this.dispose();
                new Gui_NewGame();
            }
        });
        
        //is there any saved data?
        if(!questData.hasSaveFile()) continueQuestButton.setEnabled(false);
    }//setup()
    
    //show total number of games for each difficulty
    private void setupRadioButtonText() {
        String[] diff = questData.getDifficutlyChoices();
        JRadioButton[] b = {easyRadio, mediumRadio, hardRadio, veryHardRadio};
        
        for(int i = 0; i < diff.length; i++) {
            b[i].setText(diff[i] + " - " + questData.getTotalNumberOfGames(diff[i]));
        }
        
    }//setupRadioButtonText()
    
    private void jbInit() throws Exception {
        border1 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
        titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
                "Quest Length");
        border2 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
        titledBorder2 = new TitledBorder(border2, "Continue");
        jLabel1.setFont(new java.awt.Font("Dialog", 0, 25));
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setText("Quest Options");
        jLabel1.setBounds(new Rectangle(104, 0, 213, 63));
        this.setTitle("Quest Options");
        this.getContentPane().setLayout(null);
        continueQuestButton.setBounds(new Rectangle(70, 23, 159, 35));
        continueQuestButton.setFont(new java.awt.Font("Dialog", 0, 18));
        continueQuestButton.setText("Continue Quest");
        continueQuestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                continueQuestButton_actionPerformed(e);
            }
        });
        jPanel1.setBorder(titledBorder1);
        jPanel1.setBounds(new Rectangle(67, 63, 303, 259));
        jPanel1.setLayout(null);
        jPanel2.setBounds(new Rectangle(12, 27, 271, 101));
        jPanel2.setLayout(gridLayout1);
        gridLayout1.setColumns(1);
        gridLayout1.setRows(4);
        easyRadio.setText("Easy - 50 games");
        hardRadio.setText("Hard - 200 games");
        mediumRadio.setText("Medium - 100 games");
        veryHardRadio.setText("Very Hard - 300 games");
        newQuestButton.setBounds(new Rectangle(78, 206, 145, 38));
        newQuestButton.setFont(new java.awt.Font("Dialog", 0, 18));
        newQuestButton.setText("New Quest");
        newQuestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newQuestButton_actionPerformed(e);
            }
        });
        jTextArea1.setEnabled(false);
        jTextArea1.setFont(new java.awt.Font("Dialog", 0, 14));
        jTextArea1.setDisabledTextColor(Color.black);
        jTextArea1.setEditable(false);
        jTextArea1.setText("Note: Starting a new quest will delete your current quest data");
        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setBounds(new Rectangle(13, 141, 274, 50));
        jPanel3.setBorder(titledBorder2);
        jPanel3.setBounds(new Rectangle(68, 338, 289, 86));
        jPanel3.setLayout(null);
        jPanel2.add(easyRadio, null);
        jPanel2.add(mediumRadio, null);
        jPanel2.add(hardRadio, null);
        jPanel2.add(veryHardRadio, null);
        jPanel1.add(newQuestButton, null);
        jPanel1.add(jTextArea1, null);
        this.getContentPane().add(jPanel1, null);
        this.getContentPane().add(jPanel3, null);
        jPanel3.add(continueQuestButton, null);
        this.getContentPane().add(jLabel1, null);
        jPanel1.add(jPanel2, null);
        buttonGroup1.add(easyRadio);
        buttonGroup1.add(mediumRadio);
        buttonGroup1.add(hardRadio);
        buttonGroup1.add(veryHardRadio);
    }
    
    void continueQuestButton_actionPerformed(ActionEvent e) {
        //set global variable
        AllZone.QuestData = QuestData.loadData();
        dispose();
        new Gui_Quest();
    }
    
    void newQuestButton_actionPerformed(ActionEvent e) {
        String difficulty = "";
        
        if(easyRadio.isSelected()) difficulty = questData.EASY;
        
        else if(mediumRadio.isSelected()) difficulty = questData.MEDIUM;
        
        else if(hardRadio.isSelected()) difficulty = questData.HARD;
        
        else if(veryHardRadio.isSelected()) difficulty = questData.VERY_HARD;
        
        else //user didn't select a difficulty
        return;
        
        //give the user a few cards to build a deck
        questData.newGame(difficulty);
        
        copyAIDecks(questData, QuestData.loadData());
        QuestData.saveData(questData);
        
        questData.readAIQuestDeckFiles();
        
        //set global variable
        AllZone.QuestData = questData;
        
        dispose();
        new Gui_Quest();
    }
    
    //copy AI decks from old to newData
    void copyAIDecks(QuestData newData, QuestData old) {
        ArrayList<String> a = old.ai_getDeckNames();
        for(int i = 0; i < a.size(); i++)
            newData.ai_addDeck(old.ai_getDeck(a.get(i).toString()));
    }
}
