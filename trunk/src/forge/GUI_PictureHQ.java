package forge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;



/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class GUI_PictureHQ extends JDialog
{
	private static final long serialVersionUID = 7046993858415055058L;

	public JPanel jPanelPictureHQ;
	
 

  public GUI_PictureHQ(JFrame frame, String c) 
    { 
	 super(frame); 
	  this.setUndecorated(true);
	
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    {
    	jPanelPictureHQ = new JPanel();
    	BorderLayout jPanelPictureHQLayout = new BorderLayout();
    	jPanelPictureHQ.setLayout(jPanelPictureHQLayout);
    	getContentPane().add(jPanelPictureHQ, BorderLayout.CENTER);
        jPanelPictureHQ.setBorder(BorderFactory.createEtchedBorder());;
       	jPanelPictureHQ.removeAll();
    	jPanelPictureHQ.add(GuiDisplayUtil.getPictureHQ(c));
    	jPanelPictureHQ.revalidate();
    	jPanelPictureHQ.addMouseListener(new CustomListener());
    	
    }

    pack();
  }

  public void letsGo(JFrame frame, String c) throws IOException 
  { 
    GUI_PictureHQ trayWindow = new GUI_PictureHQ(frame, c);
    int heightHQ = GuiDisplayUtil.getPictureHQheight(c);
    int widthHQ = GuiDisplayUtil.getPictureHQwidth(c);
    trayWindow.setBounds(frame.getBounds().x+ frame.getBounds().width-widthHQ-17,frame.getBounds().y+ frame.getBounds().height-heightHQ-17, widthHQ, heightHQ);
    trayWindow.pack();   
    trayWindow.setVisible(true);
    
  }
  
  public class CustomListener implements MouseListener {

      public void mouseClicked(MouseEvent e) {
      	        	
      }

      public void mouseEntered(MouseEvent e) { 
      	
      }

      public void mouseExited(MouseEvent e) {
    	  
    	  dispose();
          
      }

      public void mousePressed(MouseEvent e) {             
           
      }

      public void mouseReleased(MouseEvent e) {           
           
      }
 }
  
}