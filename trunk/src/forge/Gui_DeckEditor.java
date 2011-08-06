
package forge;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
// import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
//import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

import forge.error.ErrorViewer;
import forge.gui.game.CardDetailPanel;
import forge.gui.game.CardPicturePanel;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;


public class Gui_DeckEditor extends JFrame implements CardContainer, DeckDisplay, NewConstants {
    private static final long serialVersionUID     = 130339644136746796L;
    
    Gui_DeckEditor_Menu       customMenu;
    
    //private ImageIcon         upIcon               = Constant.IO.upIcon;
    //private ImageIcon         downIcon             = Constant.IO.downIcon;
    
    private TableModel        topModel;
    private TableModel        bottomModel;
    
    private JScrollPane       jScrollPane1         = new JScrollPane();
    private JScrollPane       jScrollPane2         = new JScrollPane();
    private JButton           removeButton         = new JButton();
    @SuppressWarnings("unused")
    // border1
    private Border            border1;
    private TitledBorder      titledBorder1;
    private Border            border2;
    private TitledBorder      titledBorder2;
    private JButton           addButton            = new JButton();
    private JButton           analysisButton       = new JButton();
    private JButton           changePictureButton  = new JButton();
    private JButton           removePictureButton  = new JButton();
    private JLabel            statsLabel           = new JLabel();
    private JTable            topTable             = new JTable();
    private JTable            bottomTable          = new JTable();
    private JScrollPane       jScrollPane3         = new JScrollPane();
    private JPanel            jPanel3              = new JPanel();
    private GridLayout        gridLayout1          = new GridLayout();
    private JLabel            statsLabel2          = new JLabel();
    private JLabel            jLabel1              = new JLabel();
    
    public JCheckBox          whiteCheckBox        = new JCheckBox("W", true);
    public JCheckBox          blueCheckBox         = new JCheckBox("U", true);
    public JCheckBox          blackCheckBox        = new JCheckBox("B", true);
    public JCheckBox          redCheckBox          = new JCheckBox("R", true);
    public JCheckBox          greenCheckBox        = new JCheckBox("G", true);
    public JCheckBox          colorlessCheckBox    = new JCheckBox("C", true);
    
    public JCheckBox          landCheckBox         = new JCheckBox("Land", true);
    public JCheckBox          creatureCheckBox     = new JCheckBox("Creature", true);
    public JCheckBox          sorceryCheckBox      = new JCheckBox("Sorcery", true);
    public JCheckBox          instantCheckBox      = new JCheckBox("Instant", true);
    public JCheckBox          planeswalkerCheckBox = new JCheckBox("Planeswalker", true);
    public JCheckBox          artifactCheckBox     = new JCheckBox("Artifact", true);
    public JCheckBox          enchantmentCheckBox  = new JCheckBox("Enchant", true);
    
    private CardList          top;
    private CardList          bottom;
    public Card               cCardHQ;
    private static File       previousDirectory    = null;
    
    private CardDetailPanel   detail               = new CardDetailPanel(null);
    private CardPicturePanel  picture              = new CardPicturePanel(null);
    private JPanel            glassPane;
    
    @Override
    public void setTitle(String message) {
        super.setTitle(message);
    }
    
    public void updateDisplay(CardList top, CardList bottom) {
        
        this.top = top;
        this.bottom = bottom;
        
        topModel.clear();
        bottomModel.clear();
        
        if(AllZone.NameChanger.shouldChangeCardName()) {
            top = new CardList(AllZone.NameChanger.changeCard(top.toArray()));
            bottom = new CardList(AllZone.NameChanger.changeCard(bottom.toArray()));
        }
        
        Card c;
        String cardName;
        ReadBoosterPack pack = new ReadBoosterPack();
        
        // update top
        for(int i = 0; i < top.size(); i++) {
            c = top.get(i);
            
            // add rarity to card if this is a sealed card pool
            
            cardName = AllZone.NameChanger.getOriginalName(c.getName());
            if(!pack.getRarity(cardName).equals("error")) {
                c.setRarity(pack.getRarity(cardName));
            }
            
            boolean filteredOut = filterByColor(c);
            
            if(!filteredOut) {
                filteredOut = filterByType(c);
            }
            
            if(!filteredOut) {
                topModel.addCard(c);
            }
        }// for
        
        // update bottom
        for(int i = 0; i < bottom.size(); i++) {
            c = bottom.get(i);
            
            // add rarity to card if this is a sealed card pool
            if(!customMenu.getGameType().equals(Constant.GameType.Constructed)) c.setRarity(pack.getRarity(c.getName()));
            
            bottomModel.addCard(c);
        }// for
        
        topModel.resort();
        bottomModel.resort();
    }// updateDisplay
    
    public void updateDisplay() {
        //updateDisplay(this.top, this.bottom);
        
        topModel.clear();
        
        if(AllZone.NameChanger.shouldChangeCardName()) {
            top = new CardList(AllZone.NameChanger.changeCard(top.toArray()));
            bottom = new CardList(AllZone.NameChanger.changeCard(bottom.toArray()));
        }
        
        Card c;
        String cardName;
        ReadBoosterPack pack = new ReadBoosterPack();
        
        // update top
        for(int i = 0; i < top.size(); i++) {
            c = top.get(i);
            
            // add rarity to card if this is a sealed card pool
            
            cardName = AllZone.NameChanger.getOriginalName(c.getName());
            if(!pack.getRarity(cardName).equals("error")) {
                c.setRarity(pack.getRarity(cardName));
            }
            
            boolean filteredOut = filterByColor(c);
            
            if(!filteredOut) {
                filteredOut = filterByType(c);
            }
            
            if(!filteredOut) {
                topModel.addCard(c);
            }
        }// for
        
        topModel.resort();
    }
    
    private boolean filterByColor(Card c) {
        boolean filterOut = false;
        
        if(!whiteCheckBox.isSelected()) {
            if(CardUtil.getColors(c).contains(Constant.Color.White)) {
                filterOut = true;
            }
        }
        
        if(!blueCheckBox.isSelected()) {
            if(CardUtil.getColors(c).contains(Constant.Color.Blue)) {
                filterOut = true;
            }
        }
        
        if(!blackCheckBox.isSelected()) {
            if(CardUtil.getColors(c).contains(Constant.Color.Black)) {
                filterOut = true;
            }
        }
        
        if(!redCheckBox.isSelected()) {
            if(CardUtil.getColors(c).contains(Constant.Color.Red)) {
                filterOut = true;
            }
        }
        
        if(!greenCheckBox.isSelected()) {
            if(CardUtil.getColors(c).contains(Constant.Color.Green)) {
                filterOut = true;
            }
        }
        
        if(!colorlessCheckBox.isSelected()) {
            if(CardUtil.getColors(c).contains(Constant.Color.Colorless)) {
                filterOut = true;
            }
        }
        
        return filterOut;
    }
    
    private boolean filterByType(Card c) {
        boolean filterOut = false;
        
        if(!landCheckBox.isSelected() && c.isLand()) {
            filterOut = true;
        }
        
        if(!creatureCheckBox.isSelected() && c.isCreature()) {
            filterOut = true;
        }
        
        if(!sorceryCheckBox.isSelected() && c.isSorcery()) {
            filterOut = true;
        }
        
        if(!instantCheckBox.isSelected() && c.isInstant()) {
            filterOut = true;
        }
        
        if(!planeswalkerCheckBox.isSelected() && c.isPlaneswalker()) {
            filterOut = true;
        }
        
        if(!artifactCheckBox.isSelected() && c.isArtifact()) {
            filterOut = true;
        }
        
        if(!enchantmentCheckBox.isSelected() && c.isEnchantment()) {
            filterOut = true;
        }
        
        return filterOut;
    }
    
    //top shows available card pool
    //if constructed, top shows all cards
    //if sealed, top shows 5 booster packs
    //if draft, top shows cards that were chosen
    
    public TableModel getTopTableModel() {
        return topModel;
    }
    
    public CardList getTop() {
        return topModel.getCards();
    }
    
    //bottom shows cards that the user has chosen for his library
    public CardList getBottom() {
        return bottomModel.getCards();
    }
    
    public void show(final Command exitCommand) {
        final Command exit = new Command() {
            private static final long serialVersionUID = 5210924838133689758L;
            
            public void execute() {
                Gui_DeckEditor.this.dispose();
                exitCommand.execute();
            }
        };
        
        customMenu = new Gui_DeckEditor_Menu(this, exit);
        this.setJMenuBar(customMenu);
        

        //do not change this!!!!
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                customMenu.close();
            }
        });
        

        setup();
        
        //show cards, makes this user friendly
        customMenu.newConstructed();
        
        topModel.sort(1, true);
        bottomModel.sort(1, true);
    }//show(Command)
    
    private void addListeners() {
        MouseInputListener l = new MouseInputListener() {
            public void mouseReleased(MouseEvent e) {
                redispatchMouseEvent(e);
            }
            
            public void mousePressed(MouseEvent e) {
                redispatchMouseEvent(e);
            }
            
            public void mouseExited(MouseEvent e) {
                redispatchMouseEvent(e);
            }
            
            public void mouseEntered(MouseEvent e) {
                redispatchMouseEvent(e);
            }
            
            public void mouseClicked(MouseEvent e) {
                redispatchMouseEvent(e);
            }
            
            public void mouseMoved(MouseEvent e) {
                redispatchMouseEvent(e);
            }
            
            public void mouseDragged(MouseEvent e) {
                redispatchMouseEvent(e);
            }
            
            private void redispatchMouseEvent(MouseEvent e) {
                Container content = getContentPane();
                Point glassPoint = e.getPoint();
                Point contentPoint = SwingUtilities.convertPoint(glassPane, glassPoint, content);
                
                Component component = SwingUtilities.getDeepestComponentAt(content, contentPoint.x, contentPoint.y);
                if(component == null || !SwingUtilities.isDescendingFrom(component, picture)) {
                    glassPane.setVisible(false);
                }
            }
        };
        
        glassPane.addMouseMotionListener(l);
        glassPane.addMouseListener(l);
        
        picture.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Card c = picture.getCard();
                if(c == null) return;
                Image i = ImageCache.getOriginalImage(c);
                if(i == null) return;
                if(i.getWidth(null) < 300) return;
                glassPane.setVisible(true);
            }
        });
    }//addListeners()
    
    private void setup() {
        addListeners();
        
        //construct topTable, get all cards
        topModel = new TableModel(new CardList(), this);
        topModel.addListeners(topTable);
        
        topTable.setModel(topModel);
        topModel.resizeCols(topTable);
        
        //construct bottomModel
        bottomModel = new TableModel(this);
        bottomModel.addListeners(bottomTable);
        
        bottomTable.setModel(bottomModel);
        topModel.resizeCols(bottomTable);
        
        //get stats from deck
        bottomModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent ev) {
                CardList deck = bottomModel.getCards();
                statsLabel.setText(getStats(deck));
            }
        });
        

        //get stats from all cards
        topModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent ev) {
                CardList deck = topModel.getCards();
                statsLabel2.setText(getStats(deck));
            }
        });
        
        //TODO use this as soon the deck editor has resizable GUI
        //Use both so that when "un"maximizing, the frame isn't tiny
        setSize(1024, 740);
        setExtendedState(Frame.MAXIMIZED_BOTH);
    }//setupAndDisplay()
    
    private String getStats(CardList deck) {
        int total = deck.size();
        int creature = deck.getType("Creature").size();
        int land = deck.getType("Land").size();
        
        StringBuffer show = new StringBuffer();
        show.append("Total - ").append(total).append(", Creatures - ").append(creature).append(", Land - ").append(land);
        String[] color = Constant.Color.Colors;
        for(int i = 0; i < 5; i++)
        	show.append(", ").append(color[i]).append(" - ").append(CardListUtil.getColor(deck, color[i]).size());
        
        return show.toString();
    }//getStats()
    
    public Gui_DeckEditor() {
        try {
            jbInit();
        } catch(Exception ex) {
            ErrorViewer.showError(ex);
        }
    }
    
    public Card getCard() {
        return detail.getCard();
    }
    
    public void setCard(Card card) {
        detail.setCard(card);
        picture.setCard(card);
    }
    
    private void jbInit() throws Exception {
        border1 = new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(148, 145, 140));
        titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
                "All Cards");
        border2 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
        titledBorder2 = new TitledBorder(border2, "Deck");
        this.getContentPane().setLayout(null);
        jScrollPane1.setBorder(titledBorder1);
        jScrollPane2.setBorder(titledBorder2);
        //removeButton.setIcon(upIcon);
        if(!Gui_NewGame.useLAFFonts.isSelected()) removeButton.setFont(new java.awt.Font("Dialog", 0, 13));
        removeButton.setText("Remove Card");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeButton_actionPerformed(e);
            }
        });
        addButton.setText("Add Card");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addButton_actionPerformed(e);
            }
        });
        //addButton.setIcon(downIcon);
        if(!Gui_NewGame.useLAFFonts.isSelected()) addButton.setFont(new java.awt.Font("Dialog", 0, 13));
        
        analysisButton.setText("Deck Analysis");
        analysisButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                analysisButton_actionPerformed(e);
            }
        });
        if(!Gui_NewGame.useLAFFonts.isSelected()) analysisButton.setFont(new java.awt.Font("Dialog", 0, 13));
        
        changePictureButton.setText("Change picture...");
        changePictureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changePictureButton_actionPerformed(e);
            }
        });
        if(!Gui_NewGame.useLAFFonts.isSelected()) changePictureButton.setFont(new java.awt.Font("Dialog", 0, 10));
        
        removePictureButton.setText("Remove picture...");
        removePictureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removePictureButton_actionPerformed(e);
            }
        });
        if(!Gui_NewGame.useLAFFonts.isSelected()) removePictureButton.setFont(new java.awt.Font("Dialog", 0, 10));
        
        /**
         * Type filtering
         */
        Font f = new Font("Tahoma", Font.PLAIN, 10);
        if(!Gui_NewGame.useLAFFonts.isSelected()) landCheckBox.setFont(f);
        landCheckBox.setOpaque(false);
        landCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        if(!Gui_NewGame.useLAFFonts.isSelected()) creatureCheckBox.setFont(f);
        creatureCheckBox.setOpaque(false);
        creatureCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        if(!Gui_NewGame.useLAFFonts.isSelected()) sorceryCheckBox.setFont(f);
        sorceryCheckBox.setOpaque(false);
        sorceryCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        if(!Gui_NewGame.useLAFFonts.isSelected()) instantCheckBox.setFont(f);
        instantCheckBox.setOpaque(false);
        instantCheckBox.addItemListener(new ItemListener() {
            
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        if(!Gui_NewGame.useLAFFonts.isSelected()) planeswalkerCheckBox.setFont(f);
        planeswalkerCheckBox.setOpaque(false);
        planeswalkerCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        if(!Gui_NewGame.useLAFFonts.isSelected()) artifactCheckBox.setFont(f);
        artifactCheckBox.setOpaque(false);
        artifactCheckBox.addItemListener(new ItemListener() {
            
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        if(!Gui_NewGame.useLAFFonts.isSelected()) enchantmentCheckBox.setFont(f);
        enchantmentCheckBox.setOpaque(false);
        enchantmentCheckBox.addItemListener(new ItemListener() {
            
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        
        /**
         * Color filtering
         */
        whiteCheckBox.setOpaque(false);
        whiteCheckBox.addItemListener(new ItemListener() {
            
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        blueCheckBox.setOpaque(false);
        blueCheckBox.addItemListener(new ItemListener() {
            
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        blackCheckBox.setOpaque(false);
        blackCheckBox.addItemListener(new ItemListener() {
            
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        redCheckBox.setOpaque(false);
        redCheckBox.addItemListener(new ItemListener() {
            
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        greenCheckBox.setOpaque(false);
        greenCheckBox.addItemListener(new ItemListener() {
            
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        colorlessCheckBox.setOpaque(false);
        colorlessCheckBox.addItemListener(new ItemListener() {
            
            public void itemStateChanged(ItemEvent e) {
                updateDisplay();
            }
        });
        picture.addMouseListener(new CustomListener());
        if(!Gui_NewGame.useLAFFonts.isSelected()) statsLabel.setFont(new java.awt.Font("Dialog", 0, 14));
        statsLabel.setText("Total - 0, Creatures - 0 Land - 0");
        //Do not lower statsLabel any lower, we want this to be visible at 1024 x 768 screen size
        this.setTitle("Deck Editor");
        jScrollPane3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jPanel3.setLayout(gridLayout1);
        gridLayout1.setColumns(1);
        gridLayout1.setRows(0);
        statsLabel2.setText("Total - 0, Creatures - 0 Land - 0");
        if(!Gui_NewGame.useLAFFonts.isSelected()) statsLabel2.setFont(new java.awt.Font("Dialog", 0, 14));
        jLabel1.setText("Click on the column name (like name or color) to sort the cards");
        
        
        this.getContentPane().setLayout(new MigLayout("fill"));
        
        this.getContentPane().add(jLabel1, "span 3, wrap");
        
        this.getContentPane().add(jScrollPane1, "span 2 2, pushy, grow");      
        this.getContentPane().add(detail, "w 239, h 323, grow, flowy, wrap");
        this.getContentPane().add(changePictureButton, "align 50% 0%,split 2, flowx");
        this.getContentPane().add(removePictureButton, "align 50% 0%, wrap");
        
        this.getContentPane().add(statsLabel2, "span 2");
        this.getContentPane().add(picture, "wmin 239, hmin 323, grow, span 1 4, wrap");
        
        this.getContentPane().add(addButton, "align 50% 50%, w 146, h 49, sg button, span 1 2, split 2");
        this.getContentPane().add(removeButton, "w 146, h 49, sg button");
        
        this.getContentPane().add(landCheckBox, ", egx checkbox, split 7");
        this.getContentPane().add(creatureCheckBox, "");
        this.getContentPane().add(sorceryCheckBox, "");
        this.getContentPane().add(instantCheckBox, "");
        this.getContentPane().add(planeswalkerCheckBox, "");
        this.getContentPane().add(artifactCheckBox, "");
        this.getContentPane().add(enchantmentCheckBox, "wrap");
        
        this.getContentPane().add(whiteCheckBox, "split 7");
        this.getContentPane().add(blueCheckBox, "");
        this.getContentPane().add(blackCheckBox, "");
        this.getContentPane().add(redCheckBox, "");
        this.getContentPane().add(greenCheckBox, "");
        this.getContentPane().add(colorlessCheckBox, "");
        this.getContentPane().add(analysisButton, "wmin 166, hmin 25, wrap");       
        
        this.getContentPane().add(jScrollPane2, "span 2, grow, wrap");
        this.getContentPane().add(statsLabel, "span 2");

        jScrollPane2.getViewport().add(bottomTable, null);
        jScrollPane1.getViewport().add(topTable, null);

        glassPane = new JPanel() {
            private static final long serialVersionUID = 7394924497724994317L;
            
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                Image image = ImageCache.getOriginalImage(picture.getCard());
                g.drawImage(image, glassPane.getWidth() - image.getWidth(null), glassPane.getHeight()
                        - image.getHeight(null), null);
            }
        };
        setGlassPane(glassPane);
    }
    
    void addButton_actionPerformed(ActionEvent e) {
        setTitle("Deck Editor : " + customMenu.getDeckName() + " : unsaved");
        
        int n = topTable.getSelectedRow();
        if(n != -1) {
            Card c = topModel.rowToCard(n);
            bottomModel.addCard(c);
            bottomModel.resort();
            
            if(!Constant.GameType.Constructed.equals(customMenu.getGameType())) {
                topModel.removeCard(c);
            }
            
            //3 conditions" 0 cards left, select the same row, select next row
            int size = topModel.getRowCount();
            if(size != 0) {
                if(size == n) n--;
                topTable.addRowSelectionInterval(n, n);
            }
        }//if(valid row)
    }//addButton_actionPerformed
    
    void analysisButton_actionPerformed(ActionEvent e) {
        
        if(bottomModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Cards in deck not found.", "Analysis Deck",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            Gui_DeckEditor g = Gui_DeckEditor.this;
            GUI_DeckAnalysis dAnalysis = new GUI_DeckAnalysis(g, bottomModel);
            dAnalysis.setVisible(true);
            g.setEnabled(false);
        }
    }
    
    void changePictureButton_actionPerformed(ActionEvent e) {
        if(cCardHQ != null) {
            File file = getImportFilename();
            if(file != null) {
                String fileName = GuiDisplayUtil.cleanString(cCardHQ.getName()) + ".jpg";
                File base = ForgeProps.getFile(IMAGE_BASE);
                File f = new File(base, fileName);
                f.delete();
                
                try {
                    
                    f.createNewFile();
                    FileOutputStream fos = new FileOutputStream(f);
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buff = new byte[32 * 1024];
                    int length;
                    while(fis.available() > 0) {
                        length = fis.read(buff);
                        if(length > 0) fos.write(buff, 0, length);
                    }
                    fos.flush();
                    fis.close();
                    fos.close();
                    setCard(cCardHQ);
                    
                } catch(IOException e1) {
                    e1.printStackTrace();
                }
                
            }
        }
    }
    
    private File getImportFilename() {
        JFileChooser chooser = new JFileChooser(previousDirectory);
        ImagePreviewPanel preview = new ImagePreviewPanel();
        chooser.setAccessory(preview);
        chooser.addPropertyChangeListener(preview);
        chooser.addChoosableFileFilter(dckFilter);
        int returnVal = chooser.showOpenDialog(null);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            previousDirectory = file.getParentFile();
            return file;
        }
        

        return null;
        
    }
    
    private FileFilter dckFilter = new FileFilter() {
                                     
                                     @Override
                                     public boolean accept(File f) {
                                         return f.getName().endsWith(".jpg") || f.isDirectory();
                                     }
                                     
                                     @Override
                                     public String getDescription() {
                                         return "*.jpg";
                                     }
                                     
                                 };
    
    
    void removePictureButton_actionPerformed(ActionEvent e) {
        if(cCardHQ != null) {
            String options[] = {"Yes", "No"};
            int value = JOptionPane.showOptionDialog(null,
                    "Do you want delete " + cCardHQ.getName() + " picture?", "Delete picture",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if(value == 0) {
                String fileName = GuiDisplayUtil.cleanString(cCardHQ.getName()) + ".jpg";
                File base = ForgeProps.getFile(IMAGE_BASE);
                File f = new File(base, fileName);
                f.delete();
                JOptionPane.showMessageDialog(null, "Picture " + cCardHQ.getName() + " deleted.",
                        "Delete picture", JOptionPane.INFORMATION_MESSAGE);
                setCard(cCardHQ);
            }
        }
        
    }
    
    void removeButton_actionPerformed(ActionEvent e) {
        setTitle("Deck Editor : " + customMenu.getDeckName() + " : unsaved");
        
        int n = bottomTable.getSelectedRow();
        if(n != -1) {
            Card c = bottomModel.rowToCard(n);
            bottomModel.removeCard(c);
            
            if(!Constant.GameType.Constructed.equals(customMenu.getGameType())) {
                topModel.addCard(c);
                topModel.resort();
            }
            
            //3 conditions" 0 cards left, select the same row, select next row
            int size = bottomModel.getRowCount();
            if(size != 0) {
                if(size == n) n--;
                bottomTable.addRowSelectionInterval(n, n);
            }
        }//if(valid row)
    }//
    

    @SuppressWarnings("unused")
    // stats_actionPerformed
    private void stats_actionPerformed(CardList list) {

    }
    
    //refresh Gui from deck, Gui shows the cards in the deck
    @SuppressWarnings("unused")
    // refreshGui
    private void refreshGui() {
        Deck deck = Constant.Runtime.HumanDeck[0];
        if(deck == null) //this is just a patch, i know
        deck = new Deck(Constant.Runtime.GameType[0]);
        
        topModel.clear();
        bottomModel.clear();
        
        Card c;
        ReadBoosterPack pack = new ReadBoosterPack();
        for(int i = 0; i < deck.countMain(); i++) {
            c = AllZone.CardFactory.getCard(deck.getMain(i), Constant.Player.Human);
            
            //add rarity to card if this is a sealed card pool
            if(Constant.Runtime.GameType[0].equals(Constant.GameType.Sealed)) c.setRarity(pack.getRarity(c.getName()));
            
            bottomModel.addCard(c);
        }//for
        
        if(deck.isSealed() || deck.isDraft()) {
            //add sideboard to GUI
            for(int i = 0; i < deck.countSideboard(); i++) {
                c = AllZone.CardFactory.getCard(deck.getSideboard(i), Constant.Player.Human);
                c.setRarity(pack.getRarity(c.getName()));
                topModel.addCard(c);
            }
        } else {
            CardList all = AllZone.CardFactory.getAllCards();
            for(int i = 0; i < all.size(); i++)
                topModel.addCard(all.get(i));
        }
        
        topModel.resort();
        bottomModel.resort();
    }////refreshGui()
    
    public class CustomListener extends MouseAdapter {
//        TODO reenable
//        public void mouseEntered(MouseEvent e) {
//            
//            if(picturePanel.getComponentCount() != 0) {
//                
//                if(GuiDisplayUtil.IsPictureHQExists(cCardHQ)) {
//                    int cWidth = 0;
//                    try {
//                        cWidth = GuiDisplayUtil.getPictureHQwidth(cCardHQ);
//                    } catch(IOException e2) {
//                        // TODO Auto-generated catch block
//                        e2.printStackTrace();
//                    }
//                    int cHeight = 0;
//                    try {
//                        cHeight = GuiDisplayUtil.getPictureHQheight(cCardHQ);
//                    } catch(IOException e2) {
//                        // TODO Auto-generated catch block
//                        e2.printStackTrace();
//                    }
//                    
//                    if(cWidth >= 312 && cHeight >= 445) {
//                        if(hq == null) {
//                            hq = new GUI_PictureHQ(Gui_DeckEditor.this, cCardHQ);
//                        }
//                        try {
//                            hq.letsGo(Gui_DeckEditor.this, cCardHQ);
//                        } catch(IOException e1) {
//                            e1.printStackTrace();
//                        }
//                    }
//                    
//                }
//            }
//            
//        }
    }
    
}
