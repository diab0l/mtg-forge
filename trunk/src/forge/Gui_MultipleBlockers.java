package forge;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Gui_MultipleBlockers extends JFrame
{
  private static final long serialVersionUID = -3585314684734680978L;

  private int assignDamage;
  private Card att;
  private GuiDisplay2 guiDisplay;

  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel mainPanel = new JPanel();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JLabel numberLabel = new JLabel();
  private JPanel jPanel3 = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JPanel creaturePanel = new JPanel();

  public static void main(String[] args)
  {
    CardList list = new CardList();
    list.add(AllZone.CardFactory.getCard("Elvish Piper", ""));
    list.add(AllZone.CardFactory.getCard("Lantern Kami", ""));
    list.add(AllZone.CardFactory.getCard("Frostling", ""));
    list.add(AllZone.CardFactory.getCard("Frostling", ""));
    
    
    for(int i = 0; i < 2; i++)
      new Gui_MultipleBlockers(list.get(i),list, i+1, null);
  }

  @SuppressWarnings("deprecation") // dialog.show is deprecated
  public Gui_MultipleBlockers(Card attacker, CardList creatureList, int damage, GuiDisplay2 display)
  {
    this();
    assignDamage = damage;
    updateDamageLabel();//update user message about assigning damage
    guiDisplay = display;
    att = attacker;

    for(int i = 0; i < creatureList.size(); i++)
      creaturePanel.add(GuiDisplayUtil.getCardPanel(creatureList.get(i)));

    JDialog dialog = new JDialog(this, true);
    dialog.setTitle("Multiple Blockers");
    dialog.setContentPane(mainPanel);
    dialog.setSize(470, 260);
    dialog.show();
  }
  public Gui_MultipleBlockers() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
//    setSize(470, 280);
//    show();
  }
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    this.setTitle("Multiple Blockers");
    mainPanel.setLayout(null);
    numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
    numberLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    numberLabel.setText("Assign");
    numberLabel.setBounds(new Rectangle(52, 30, 343, 24));
    jPanel3.setLayout(borderLayout3);
    jPanel3.setBounds(new Rectangle(26, 75, 399, 114));
    creaturePanel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        creaturePanel_mousePressed(e);
      }
    });
    creaturePanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        creaturePanel_mouseMoved(e);
      }
    });
    mainPanel.add(jPanel3, null);
    jPanel3.add(jScrollPane1, BorderLayout.CENTER);
    mainPanel.add(numberLabel, null);
    jScrollPane1.getViewport().add(creaturePanel, null);
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
  }

  void okButton_actionPerformed(ActionEvent e) {
    dispose();
  }

  void creaturePanel_mousePressed(MouseEvent e) {
    Object o = creaturePanel.getComponentAt(e.getPoint());
    if(o instanceof CardPanel)
    {
      CardPanel cardPanel = (CardPanel)o;
      Card c = cardPanel.getCard();
      
      CardList cl = new CardList();
      cl.add(att);
      AllZone.GameAction.setAssignedDamage(c, cl, c.getAssignedDamage() + 1);
      //c.setAssignedDamage(c.getAssignedDamage() + 1);

      if(guiDisplay != null)
        guiDisplay.updateCardDetail(c);
    }
    //reduce damage, show new user message, exit if necessary
    assignDamage--;
    updateDamageLabel();
    if(assignDamage == 0)
      dispose();
  }//creaturePanel_mousePressed()
  void updateDamageLabel()
  {
    numberLabel.setText("Assign " +assignDamage +" damage - click on card to assign damage");
  }

  void creaturePanel_mouseMoved(MouseEvent e)
  {
    Object o = creaturePanel.getComponentAt(e.getPoint());
    if(o instanceof CardPanel)
    {
      CardPanel cardPanel = (CardPanel)o;
      Card c = cardPanel.getCard();

      if(guiDisplay != null)
        guiDisplay.updateCardDetail(c);
    }
  }
}