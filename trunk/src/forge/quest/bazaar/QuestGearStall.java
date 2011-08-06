package forge.quest.bazaar;

import forge.QuestData;
import forge.error.ErrorViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class QuestGearStall extends QuestAbstractBazaarStall{
	
	private static final long serialVersionUID = -2124386606846472829L;
	
	private JLabel            titleLabel        = new JLabel();
	
	private JLabel 			  gearDescLabel  = new JLabel();

	private JLabel 			  gearPriceLabel = new JLabel();
	private JLabel 			  gearIconLabel  = new JLabel();
	
	private JLabel			  creditsLabel     	= new JLabel();
	
	private ImageIcon		  gearIcon	  	= new ImageIcon();
	    
	private JButton           gearButton 	= new JButton();
    
    
    public QuestGearStall() {
        super("Gear","GearIconSmall.png","");
        
        try {
            jbInit();
        } catch(Exception ex) {
            ErrorViewer.showError(ex);
        }
        
        setup();
        
    }
    
    //only do this ONCE:
    private void setup() {
    	gearButton.setBounds(new Rectangle(10, 297, 120, 50));
    	gearButton.setText(getButtonText());
    	//buyPlantButton.setIcon(icon);
    	gearButton.addActionListener(new java.awt.event.ActionListener() {
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

    	if (questData.getGearLevel() == 0) 
    	{
    		sb.append("<u><b>Adventurer's Map</b></u><br>");
    		sb.append("These ancient charts should facilitate navigation during your travels significantly.<br>");
    		sb.append("<u>Quest assignments become available more frequently</u>.");
    	}
    	else if (questData.getGearLevel() == 1)
    	{
    		sb.append("<u><b>Adventurer's Zeppelin</b></u><br>");
    		sb.append("This extremely comfortable airship allows for more efficient and safe travel to faraway destinations. <br>");
    		sb.append("<u>Quest assignments become available more frequently, adds +3 to max life</u>.");
    	}
    	else
    	{
    		sb.append("Currently nothing for sale. <br>Please check back later.");
    	}
    	
    	sb.append("</html>");
    	return sb.toString();
    }

    private long getPrice()
    {
    	long l = 0;
    	if (questData.getGearLevel() == 0)
    		l = 2000; 
    	else if (questData.getGearLevel() == 1)
    		l = 5000;
    	
    	return l;
    }
    
    private String getButtonText()
    {
    	return "Buy";
    }
    
    private String getImageString()
    {
    	if (questData.getGearLevel() == 0)
    		return "MapIconLarge.png";
    	else if (questData.getGearLevel() == 1)
    		return "ZeppelinIcon.png";
    	
    	return "";
    }
    
    private void jbInit() throws Exception {
        titleLabel.setFont(new Font("sserif", Font.BOLD, 22));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setText("Adventurer's Gear");
        titleLabel.setBounds(new Rectangle(155, 5, 198, 60));
        stallPanel.setLayout(null);
        
        /*
        potionStatsLabel.setFont(new Font("sserif", Font.BOLD, 12));
        potionStatsLabel.setText(getStats());
        potionStatsLabel.setBounds(new Rectangle(10, 65, 100, 15));
        */
        
        gearDescLabel.setFont(new Font("sserif", 0, 12));
        gearDescLabel.setText(getDesc());
        gearDescLabel.setBounds(new Rectangle(10, 80, 300, 150));
        
        gearPriceLabel.setFont(new Font("sserif", 0, 12));
        gearPriceLabel.setText("<html><b><u>Price</u></b>: " + getPrice() + " credits</html>");
        gearPriceLabel.setBounds(new Rectangle(10, 230, 150, 15));
        
        creditsLabel.setFont(new Font("sserif", 0, 12));
        creditsLabel.setText("Credits: " + questData.getCredits());
        creditsLabel.setBounds(new Rectangle(10, 265, 150, 15));
        
        gearIcon = getIcon(getImageString());
        gearIconLabel.setText("");
        gearIconLabel.setIcon(gearIcon);
        gearIconLabel.setBounds(new Rectangle(325, 100, 128, 128));
        gearIconLabel.setIconTextGap(0);
        
        //String fileName = "LeafIconSmall.png";
    	//ImageIcon icon = getIcon(fileName);
    	
        gearButton.setEnabled(true);
    	if (questData.getCredits() < getPrice() || questData.getGearLevel() >= 2)
    		gearButton.setEnabled(false);
       

        //jPanel2.add(quitButton, null);
        stallPanel.add(gearButton, null);
        stallPanel.add(titleLabel, null);
        stallPanel.add(gearDescLabel, null);
        stallPanel.add(gearIconLabel, null);
        stallPanel.add(gearPriceLabel, null);
        stallPanel.add(creditsLabel, null);
    }
    
    void learnEstatesButton_actionPerformed(ActionEvent e) throws Exception {
	    	questData.subtractCredits(getPrice());
	    	
	    	if (questData.getGearLevel() < 2)
	    	{
	    		questData.addGearLevel(1);
	    	}
	    	QuestData.saveData(questData);
	    	jbInit();
    }

}
