package forge.quest.gui.main;


import forge.deck.Deck;
import forge.gui.GuiUtils;
import forge.quest.data.QuestBattleManager;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>QuestBattle class.</p>
 *
 * @author Forge
 * @version $Id$
 */
public class QuestBattle extends QuestSelectablePanel {
    /** Constant <code>serialVersionUID=3112668476017792084L</code> */
    private static final long serialVersionUID = 3112668476017792084L;

    private String deckName;
    
    private static String       oppName;
    private static String       oppDiff;
    private static String       oppDesc;
    private static String       oppIconAddress;
    private static ImageIcon    icon;
    private static Deck         oppDeck;
    
    /**
     * <p>Constructor for QuestBattle.</p>
     *
     * @param name a {@link java.lang.String}, stores display name of opponent.
     * @param diff a {@link java.lang.String} stores difficulty of opponent.
     * @param description a {@link java.lang.String} stores description of opponent's deck.
     * @param icon a {@link javax.swing.ImageIcon} stores opponent's icon.
     */
    private QuestBattle(String name, String deck, String diff, String desc, ImageIcon icon) {
        super(name, diff, desc, icon);
        this.deckName = deck;
    }
    
    /**
     * <p>getBattles.</p>
     * 
     * Returns list of QuestBattle objects storing data 
     * of the battles currently available.
     *
     * @return a {@link java.util.List} object.
     */
    
    // There's got to be a better place for this method.
    public static List<QuestSelectablePanel> getBattles() {
        List<QuestSelectablePanel> opponentList = new ArrayList<QuestSelectablePanel>();

        String[] oppDecks = QuestBattleManager.getOpponents();
        for (String oppDeckName : oppDecks) {
            // Get deck object and properties for this opponent.
            oppDeck    = QuestBattleManager.getAIDeckNewFormat(oppDeckName);
            
            oppName         = oppDeck.getMetadata("DisplayName");
            oppDiff         = oppDeck.getMetadata("Difficulty");
            oppDesc         = oppDeck.getMetadata("Description");
            oppIconAddress  = oppDeck.getMetadata("Icon");
            
            icon  = GuiUtils.getIconFromFile(oppName + ".jpg");
            
            // If non-default icon defined, use it            
            if(!oppIconAddress.equals("")) {
                icon = GuiUtils.getIconFromFile(oppIconAddress + ".jpg");
            }
            
            // Add to list of current quest opponents.
            opponentList.add(
                    new QuestBattle(oppName, oppDeckName, oppDiff, oppDesc, icon)
            );
        }

        return opponentList;
    }

    /** {@inheritDoc}  */
    @Override
    public String getName() {
        // Called by ???? to get deck name for image icon generation.
        // Exception should be thrown somewhere if image can't be found.
        return deckName;
    }
}
