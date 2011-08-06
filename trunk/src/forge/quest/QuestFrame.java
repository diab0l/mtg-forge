package forge.quest;

import forge.AllZone;
import forge.Gui_NewGame;
import forge.QuestData;
import forge.gui.GuiUtils;
import forge.quest.bazaar.QuestBazaarPanel;
import forge.quest.main.QuestMainPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.util.HashMap;
import java.util.Map;

public class QuestFrame extends JFrame {
	private static final long serialVersionUID = -2832625381531838412L;
	
	JPanel visiblePanel;
    CardLayout questLayout;

    public static final String MAIN_PANEL = "Main";
    public static final String BAZAAR_PANEL = "Bazaar";

    Map<String, QuestAbstractPanel> subPanelMap = new HashMap<String, QuestAbstractPanel>();

    public QuestFrame() throws HeadlessException {
        this.setTitle("Quest Mode");

        visiblePanel = new JPanel(new BorderLayout());
        visiblePanel.setBorder(new EmptyBorder(2,2,2,2));
        questLayout = new CardLayout();
        visiblePanel.setLayout(questLayout);

        QuestAbstractPanel newPanel = new QuestMainPanel(this);
        visiblePanel.add(newPanel, MAIN_PANEL);
        subPanelMap.put(MAIN_PANEL, newPanel);

        newPanel = new QuestBazaarPanel(this);
        visiblePanel.add(newPanel, BAZAAR_PANEL);
        subPanelMap.put(BAZAAR_PANEL, newPanel);


        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(visiblePanel, BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(1024, 768));
        this.setMinimumSize(new Dimension(800, 600));

        questLayout.show(visiblePanel, MAIN_PANEL);

        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.pack();
        this.setVisible(true);

        GuiUtils.centerFrame(this);

    }



    public void showPane(String paneName){
        subPanelMap.get(paneName).refreshState();
        questLayout.show(visiblePanel, paneName);
    }

    public void returnToMainMenu() {
        QuestData.saveData(AllZone.QuestData);
        (new Gui_NewGame()).setVisible(true) ;
        this.dispose();
    }
}
