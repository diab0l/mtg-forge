package forge;

import forge.error.ErrorViewer;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;

public class Gui_Treasury extends JFrame implements NewConstants{
	
	private static final long serialVersionUID = 2409591658245091210L;
		
	private JFrame 			  shopsGUI;
	private JLabel            titleLabel        = new JLabel();
	
	private JLabel 			  estatesDescLabel  = new JLabel();
	//private JLabel		  estatesStatsLabel = new JLabel();
	private JLabel 			  estatesPriceLabel = new JLabel();
	private JLabel 			  estatesIconLabel  = new JLabel();
	
	private JLabel			  creditsLabel     	= new JLabel();
	
	private ImageIcon		  estatesIcon	  	= new ImageIcon();
	    
	private JButton           learnEstatesButton= new JButton();
    private JButton			  quitButton 	    = new JButton();
    
    private forge.quest.data.QuestData 		  questData 	    = AllZone.QuestData;
    
    public Gui_Treasury(JFrame parent) {
        try {
            jbInit();
        } catch(Exception ex) {
            ErrorViewer.showError(ex);
        }
        
        shopsGUI = parent;
        
        setup();
        
        //for some reason, the Bazaar window does not return when closing with X
        //for now, just disable X closing:
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        
        Dimension screen = this.getToolkit().getScreenSize();
        setBounds(screen.width / 3, 100, //position
                530, 430); //size
        setVisible(true);
        
        
    }
    
    //only do this ONCE:
    private void setup() {
    	learnEstatesButton.setBounds(new Rectangle(10, 297, 120, 50));
    	learnEstatesButton.setText(getButtonText());
    	//buyPlantButton.setIcon(icon);
    	learnEstatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
					learnEstatesButton_actionPerformed(e);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
        
    }//setup();
    
    private String getDesc()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<html>");

    	if (questData.getEstatesLevel() == 0) 
    	{
    		sb.append("<u>Level 1 Estates</u><br>");
    		sb.append("Gives a bonus of <b>10%</b> to match winnings.<br>");
    		sb.append("Improves sell percentage by <b>1.0%</b>.");
    	}
    	else if (questData.getEstatesLevel() == 1)
    	{
    		sb.append("<u>Level 2 Estates</u><br>");
    		sb.append("Gives a bonus of <b>15%</b> to match winnings.<br>");
    		sb.append("Improves sell percentage by <b>1.75%</b>.");
    	}
    	else if (questData.getEstatesLevel() == 2)
    	{
    		sb.append("<u>Level 3 Estates</u><br>");
    		sb.append("Gives a bonus of <b>20%</b> to match winnings.<br>");
    		sb.append("Improves sell percentage by <b>2.5%</b>.");
    	}
    	else if (questData.getEstatesLevel() >= 3 && questData.getLuckyCoinLevel() == 0)
    	{
    		sb.append("Estates Level Maxed out.<br>");
    		sb.append("<u><b>Lucky Coin</b></u><br>");
    		sb.append("This coin is believed to give good luck to its owner.<br>");
    		sb.append("Improves the chance of getting a random <br>rare after each match by <b>15%</b>.");
    		/*sb.append("Current Level: 3/3<br>");
    		sb.append("Gives a bonus of <b>20%</b> to match winnings.<br>");
    		sb.append("Improves sell percentage by <b>2.5%</b>.");*/
    	}
    	else
    	{
    		sb.append("Currently nothing for sale at the Treasury. <br>Please check back later.");
    	}
    	
    	sb.append("</html>");
    	return sb.toString();
    }

    private long getPrice()
    {
    	long l = 0;
    	if (questData.getEstatesLevel() == 0)
    		l = 500;
    	else if (questData.getEstatesLevel() == 1)
    		l = 750;
    	else if (questData.getEstatesLevel() == 2)
    		l = 1000;
    	else if (questData.getEstatesLevel() >= 3 && questData.getLuckyCoinLevel() == 0)
    		l = 500;
    		
    		
    	return l;
    }
    
    private String getButtonText()
    {
    	if (questData.getEstatesLevel() < 3)
    		return "Learn Estates";
    	else
    		return "Buy Coin";
    }
    
    private String getImageString()
    {
    	if (questData.getEstatesLevel() < 3)
    		return "GoldIconLarge.png";
    	else
    		return "CoinIcon.png";
    }
    
    private void jbInit() throws Exception {
        titleLabel.setFont(new java.awt.Font("sserif", Font.BOLD, 22));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setText("Treasury");
        titleLabel.setBounds(new Rectangle(130, 5, 198, 60));
        this.getContentPane().setLayout(null);
        
        /*
        potionStatsLabel.setFont(new Font("sserif", Font.BOLD, 12));
        potionStatsLabel.setText(getStats());
        potionStatsLabel.setBounds(new Rectangle(10, 65, 100, 15));
        */
        
        estatesDescLabel.setFont(new Font("sserif", 0, 12));
        estatesDescLabel.setText(getDesc());
        estatesDescLabel.setBounds(new Rectangle(10, 80, 300, 150));
        
        estatesPriceLabel.setFont(new Font("sserif", 0, 12));
        estatesPriceLabel.setText("<html><b><u>Price</u></b>: " + getPrice() + " credits</html>");
        estatesPriceLabel.setBounds(new Rectangle(10, 230, 150, 15));
        
        creditsLabel.setFont(new Font("sserif", 0, 12));
        creditsLabel.setText("Credits: " + questData.getCredits());
        creditsLabel.setBounds(new Rectangle(10, 265, 150, 15));
        
        estatesIcon = getIcon(getImageString());
        estatesIconLabel.setText("");
        estatesIconLabel.setIcon(estatesIcon);
        estatesIconLabel.setBounds(new Rectangle(255, 65, 256, 256));
        estatesIconLabel.setIconTextGap(0);
        
        //String fileName = "LeafIconSmall.png";
    	//ImageIcon icon = getIcon(fileName);
    	
    	learnEstatesButton.setEnabled(true);
    	if (questData.getCredits() < getPrice() || (questData.getEstatesLevel() >= 3 && questData.getLuckyCoinLevel() >= 1))
    		learnEstatesButton.setEnabled(false);
       
        quitButton.setBounds(new Rectangle(140, 297, 120, 50));
        quitButton.setText("Quit");
        quitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quitButton_actionPerformed(e);
            }
        });


        //jPanel2.add(quitButton, null);
        this.getContentPane().add(learnEstatesButton, null);
        this.getContentPane().add(titleLabel, null);
        this.getContentPane().add(estatesDescLabel, null);
        this.getContentPane().add(estatesIconLabel, null);
        this.getContentPane().add(estatesPriceLabel, null);
        this.getContentPane().add(creditsLabel, null);
        this.getContentPane().add(quitButton,null);
    }
    
    void learnEstatesButton_actionPerformed(ActionEvent e) throws Exception {
	    	questData.subtractCredits(getPrice());
	    	
	    	if (questData.getEstatesLevel() < 3)
	    	{
	    		questData.addEstatesLevel(1);
	    	}
	    	else if (questData.getLuckyCoinLevel() < 1)
	    	{
	    		questData.addLuckyCoinLevel(1);
	    	}
	    	questData.saveData();
	    	jbInit();
    }
    
    private ImageIcon getIcon(String fileName)
    {
    	File base = ForgeProps.getFile(IMAGE_ICON);
    	File file = new File(base, fileName);
    	ImageIcon icon = new ImageIcon(file.toString());
    	return icon;
    }
    
    void quitButton_actionPerformed(ActionEvent e) {
    	questData.saveData();
        //new Gui_Shops();
    	shopsGUI.setVisible(true);
    	
        dispose();
       
    }
    
    void this_windowClosing(WindowEvent e) {
        quitButton_actionPerformed(null);
    }
    
}
